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

    // :) Hay clientes -> 200, lista de clientes.
    // :( No hay clientes -> 200, lista vacía.
    @GetMapping("/clientes")
    public ApiResponse clientes() {
        return ApiResponse.ok(clienteRepo.findAll().stream()
                .map(Dto.ClienteResponse::from).toList());
    }

    // :) Nombre y correo válidos -> 200, cliente con ID.
    // :( Nombre vacío, correo vacío, correo con formato inválido -> 400.
    // :( Correo duplicado -> 409.
    // :( JSON mal formato -> 400.
    @PostMapping("/clientes")
    public ApiResponse crearCliente(@Valid @RequestBody Dto.ClienteRequest r) {
        Cliente saved = clienteRepo.save(new Cliente(r.nombre(), r.correo()));
        return ApiResponse.ok(List.of(Dto.ClienteResponse.from(saved)));
    }

    // :) Órdenes con productos -> 200, lista ordenada por ID.
    // :( No hay órdenes -> 200, lista vacía.
    @GetMapping("/ordenes")
    @Transactional(readOnly = true)
    public ApiResponse ordenes() {
        return ApiResponse.ok(ordenRepo.findAllByOrderByIdAsc().stream()
                .map(Dto.OrdenResponse::from).toList());
    }

    // :) Cliente existente, 1 producto con stock o no hay stock -> 200, orden creada y stock descontado.
    // :( clienteId nulo, items nulo, cantidad cero, cantidad mayor al stock disponible, JSOn mal hecho -> 400.
    // :( clienteId inexistente -> 404.
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
