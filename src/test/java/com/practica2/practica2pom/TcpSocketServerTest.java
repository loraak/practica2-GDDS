package com.practica2.practica2pom;

import com.practica2.practica2pom.entities.Categoria;
import com.practica2.practica2pom.entities.Producto;
import com.practica2.practica2pom.repositorios.CategoriaRepository;
import com.practica2.practica2pom.repositorios.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TcpSocketServerTest {

    private ProductoRepository productoRepo;
    private CategoriaRepository categoriaRepo;
    private TcpSocketServer server;

    @BeforeEach
    void setUp() {
        productoRepo = mock(ProductoRepository.class);
        categoriaRepo = mock(CategoriaRepository.class);
        server = new TcpSocketServer(productoRepo, categoriaRepo);
    }

    // ------------------------- helpers -------------------------

    private String procesar(String raw) throws Exception {
        Method m = TcpSocketServer.class.getDeclaredMethod("procesar", String.class);
        m.setAccessible(true);
        return (String) m.invoke(server, raw);
    }

    private Categoria cat(String nombre) {
        Categoria c = BeanUtils.instantiateClass(Categoria.class);
        ReflectionTestUtils.setField(c, "nombre", nombre);
        return c;
    }

    private Producto prod(long id, String nombre, double precio, int stock) {
        Producto p = new Producto(nombre, precio, stock, cat("Perros"));
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private static final String INSERT_OK =
            "{insert:{\"nombre\":\"Croquetas\",\"precio\":250.5,\"stock\":10,\"categoriaId\":1}}";

    // ------------------------- formato del mensaje -------------------------

    @Test
    void mensajeNulo_error() throws Exception {
        assertTrue(procesar(null).contains("Mensaje vacio"));
    }

    @Test
    void mensajeVacio_error() throws Exception {
        assertTrue(procesar("").contains("Mensaje vacio"));
    }

    @Test
    void mensajeSoloEspacios_error() throws Exception {
        assertTrue(procesar("   ").contains("Mensaje vacio"));
    }

    @Test
    void sinLlaves_formatoInvalido() throws Exception {
        assertTrue(procesar("get:all").contains("Formato invalido"));
    }

    @Test
    void sinDosPuntos_formatoInvalido() throws Exception {
        assertTrue(procesar("{get}").contains("falta ':'"));
    }

    @Test
    void comandoDesconocido_error() throws Exception {
        String r = procesar("{borrar:1}");
        assertTrue(r.contains("\"statusCode\":400"));
        assertTrue(r.contains("Comando no reconocido: borrar"));
    }

    @Test
    void comandoEnMayusculas_seNormaliza() throws Exception {
        when(productoRepo.findAllByOrderByIdAsc()).thenReturn(List.of());
        assertTrue(procesar("{GET:all}").contains("\"statusCode\":200"));
    }

    // ------------------------- insert -------------------------

    @Test
    void insert_ok() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat("Perros")));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> {
            Producto p = i.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 7L);
            return p;
        });

        String r = procesar(INSERT_OK);

        assertTrue(r.contains("\"statusCode\":200"));
        assertTrue(r.contains("Croquetas"));
        assertTrue(r.contains("Perros"));
        verify(productoRepo).save(any(Producto.class));
    }

    @Test
    void insert_stockOmitido_quedaEnCero() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat("Perros")));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> {
            Producto p = i.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 7L);
            return p;
        });

        String r = procesar("{insert:{\"nombre\":\"X\",\"precio\":10,\"categoriaId\":1}}");

        assertTrue(r.contains("\"statusCode\":200"));
        assertTrue(r.contains("\"stock\":0"));
    }

    @Test
    void insert_categoriaInexistente_error() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.empty());

        String r = procesar(INSERT_OK);

        assertTrue(r.contains("\"statusCode\":400"));
        assertTrue(r.contains("Categoria no encontrada"));
        verify(productoRepo, never()).save(any());
    }

    @Test
    void insert_jsonInvalido_error() throws Exception {
        String r = procesar("{insert:{nombre: sin comillas}}");
        assertTrue(r.contains("\"statusCode\":400"));
        verify(productoRepo, never()).save(any());
    }

    @Test
    void insert_sinPrecio_error() throws Exception {
        String r = procesar("{insert:{\"nombre\":\"X\",\"categoriaId\":1}}");
        assertTrue(r.contains("\"statusCode\":400"));
        verify(productoRepo, never()).save(any());
    }

    @Test
    void insert_precioNoNumerico_error() throws Exception {
        String r = procesar("{insert:{\"nombre\":\"X\",\"precio\":\"abc\",\"categoriaId\":1}}");
        assertTrue(r.contains("\"statusCode\":400"));
    }

    // ------------------------- get -------------------------

    @Test
    void get_all_devuelveLista() throws Exception {
        when(productoRepo.findAllByOrderByIdAsc()).thenReturn(List.of(prod(1, "Croquetas", 100, 5)));

        String r = procesar("{get:all}");

        assertTrue(r.contains("\"statusCode\":200"));
        assertTrue(r.contains("Croquetas"));
    }

    @Test
    void get_vacio_equivaleAAll() throws Exception {
        when(productoRepo.findAllByOrderByIdAsc()).thenReturn(List.of());
        assertTrue(procesar("{get:}").contains("\"data\":[]"));
    }

    @Test
    void get_porId_ok() throws Exception {
        when(productoRepo.findWithCategoriaById(1L)).thenReturn(Optional.of(prod(1, "Croquetas", 100, 5)));

        String r = procesar("{get:1}");

        assertTrue(r.contains("\"statusCode\":200"));
        assertTrue(r.contains("Croquetas"));
    }

    @Test
    void get_idInexistente_error() throws Exception {
        when(productoRepo.findWithCategoriaById(99L)).thenReturn(Optional.empty());

        String r = procesar("{get:99}");

        assertTrue(r.contains("\"statusCode\":400"));
        assertTrue(r.contains("Producto no encontrado"));
    }

    @Test
    void get_idNoNumerico_error() throws Exception {
        String r = procesar("{get:abc}");
        assertTrue(r.contains("\"statusCode\":400"));
    }

    // ------------------------- socket real (cubre start/escuchar/atenderCliente) -------------------------

    @Test
    void socketReal_respondeAlCliente() throws Exception {
        when(productoRepo.findAllByOrderByIdAsc()).thenReturn(List.of());
        server.start();

        Socket intento = null;
        for (int i = 0; i < 30 && intento == null; i++) {
            try {
                intento = new Socket("localhost", 6061);
            } catch (java.io.IOException e) {
                Thread.sleep(100);
            }
        }
        assertNotNull(intento, "No se pudo conectar al puerto 6061");

        final Socket socket = intento;
        try (socket;
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("{get:all}");
            String respuesta = in.readLine();
            assertTrue(respuesta.contains("\"statusCode\":200"));
        }
    }
}