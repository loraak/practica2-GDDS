package com.practica2.practica2pom.controladores;

import com.practica2.practica2pom.ApiResponse;
import com.practica2.practica2pom.DTOs.Dto;
import com.practica2.practica2pom.entities.Cliente;
import com.practica2.practica2pom.entities.ClienteOrden;
import com.practica2.practica2pom.entities.Producto;
import com.practica2.practica2pom.repositorios.ClienteOrdenRepository;
import com.practica2.practica2pom.repositorios.ClienteRepository;
import com.practica2.practica2pom.repositorios.ProductoRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class VentasController {
    private final ClienteRepository clienteRepo;
    private final ProductoRepository productoRepo;
    private final ClienteOrdenRepository ordenRepo;

    public VentasController(ClienteRepository clienteRepo, ProductoRepository productoRepo, ClienteOrdenRepository ordenRepo) {
        this.clienteRepo = clienteRepo;
        this.productoRepo = productoRepo;
        this.ordenRepo = ordenRepo;
    }

    @GetMapping("/clientes")
    public ApiResponse clientes() {
        return ApiResponse.ok(clienteRepo.findAll().stream()
                .map(Dto.ClienteResponse::from).toList());
    }

    @PostMapping("/clientes")
    public ApiResponse crearCliente(@Valid @RequestBody Dto.ClienteRequest r) {
        Cliente saved = clienteRepo.save(new Cliente(r.nombre(), r.correo()));
        return ApiResponse.ok(List.of(Dto.ClienteResponse.from(saved)));
    }

    @GetMapping("/ordenes")
    @Transactional(readOnly = true)
    public ApiResponse ordenes() {
        return ApiResponse.ok(ordenRepo.findAllByOrderByIdAsc().stream()
                .map(Dto.OrdenResponse::from).toList());
    }

    @PostMapping("/ordenes")
    @Transactional
    public ApiResponse crearOrden(@Valid @RequestBody Dto.OrdenRequest r) {
        Cliente cliente = clienteRepo.findById(r.clienteId())
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        ClienteOrden orden = new ClienteOrden(cliente);
        for (Dto.OrdenItemRequest item : r.items()) {
            Producto producto = productoRepo.findById(item.productoId())
                    .orElseThrow(() -> new NoSuchElementException("Producto no encontrao': " + item.productoId()));
            producto.quitarStock(item.cantidad());
            orden.agregarProducto(producto, item.cantidad());
        }
        return ApiResponse.ok(List.of(Dto.OrdenResponse.from(ordenRepo.save(orden))));
    }
}
