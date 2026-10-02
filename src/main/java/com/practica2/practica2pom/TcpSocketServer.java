package com.practica2.practica2pom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practica2.practica2pom.entities.Categoria;
import com.practica2.practica2pom.entities.Producto;
import com.practica2.practica2pom.repositorios.CategoriaRepository;
import com.practica2.practica2pom.repositorios.ProductoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class TcpSocketServer {

    private static final int PORT = 6061;

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public TcpSocketServer(ProductoRepository productoRepo, CategoriaRepository categoriaRepo) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
    }

    @PostConstruct
    public void start() {
        Thread hilo = new Thread(this::escuchar, "tcp-6061-listener");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void escuchar() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor TCP escuchando en el puerto " + PORT);
            while (true) {
                Socket cliente = serverSocket.accept();
                new Thread(() -> atenderCliente(cliente)).start();
            }
        } catch (IOException e) {
            System.err.println("Error en servidor TCP: " + e.getMessage());
        }
    }

    private void atenderCliente(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String linea = in.readLine();
            out.println(procesar(linea));

        } catch (IOException e) {
            System.err.println("Error atendiendo cliente TCP: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private String procesar(String raw) {
        try {
            if (raw == null || raw.isBlank()) return error("Mensaje vacio");
            String linea = raw.trim();

            if (!linea.startsWith("{") || !linea.endsWith("}")) {
                return error("Formato invalido, se espera {comando:elemento}");
            }

            String interior = linea.substring(1, linea.length() - 1);
            int sep = interior.indexOf(':');
            if (sep == -1) return error("Formato invalido, falta ':'");

            String comando = interior.substring(0, sep).trim().toLowerCase();
            String elemento = interior.substring(sep + 1).trim();

            return switch (comando) {
                case "insert" -> insertar(elemento);
                case "get" -> obtener(elemento);
                default -> error("Comando no reconocido: " + comando);
            };
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String insertar(String jsonBody) throws IOException {
        Map<String, Object> body = mapper.readValue(jsonBody, Map.class);

        String nombre = String.valueOf(body.get("nombre"));
        double precio = ((Number) body.get("precio")).doubleValue();
        int stock = body.get("stock") == null ? 0 : ((Number) body.get("stock")).intValue();
        long categoriaId = ((Number) body.get("categoriaId")).longValue();

        Categoria categoria = categoriaRepo.findById(categoriaId)
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada"));

        Producto guardado = productoRepo.save(new Producto(nombre, precio, stock, categoria));
        return ok(List.of(aMapa(guardado)));
    }

    private String obtener(String elemento) {
        if (elemento.isBlank() || elemento.equalsIgnoreCase("all")) {
            List<Map<String, Object>> productos = productoRepo.findAllByOrderByIdAsc().stream()
                    .map(this::aMapa).toList();
            return ok(productos);
        }

        long id = Long.parseLong(elemento);
        Producto producto = productoRepo.findWithCategoriaById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        return ok(List.of(aMapa(producto)));
    }

    private Map<String, Object> aMapa(Producto p) {
        return Map.of(
                "id", p.getId(),
                "name", p.getNombre(),
                "price", p.getPrecio(),
                "stock", p.getStock(),
                "category", p.getCategoria().getNombre()
        );
    }

    private String ok(List<?> data) {
        try {
            return mapper.writeValueAsString(new ApiResponse(200, data));
        } catch (IOException e) {
            return "{\"statusCode\":500,\"data\":[]}";
        }
    }

    private String error(String mensaje) {
        String limpio = String.valueOf(mensaje).replace("\"", "'");
        return "{\"statusCode\":400,\"data\":[{\"error\":\"" + limpio + "\"}]}";
    }
}