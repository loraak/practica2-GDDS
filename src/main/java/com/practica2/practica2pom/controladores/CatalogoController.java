package com.practica2.practica2pom.controladores;

import com.practica2.practica2pom.ApiResponse;
import com.practica2.practica2pom.DTOs.Dto;
import com.practica2.practica2pom.entities.Categoria;
import com.practica2.practica2pom.entities.Producto;
import com.practica2.practica2pom.repositorios.CategoriaRepository;
import com.practica2.practica2pom.repositorios.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class CatalogoController {
    private final CategoriaRepository categoriaRepo;
    private final ProductoRepository productoRepo;

    public CatalogoController(CategoriaRepository categoriaRepo, ProductoRepository productoRepo) {
        this.categoriaRepo = categoriaRepo;
        this.productoRepo = productoRepo;
    }

    // :) Hay categorías -> 200 con una lista ordenada por ID.
    // :( No hay categorías -> 200 pero con una lista vacía.
    @GetMapping("/categorias")
    public ApiResponse categorias() {
        return ApiResponse.ok(categoriaRepo.findAll(Sort.by("id")).stream()
                .map(Dto.CategoriaResponse::from).toList());
    }

    // :) Hay productos -> 200 con una lista ordenada por ID.
    // :( No hay categorías -> 200 pero con una lista vacía.
    @GetMapping("/productos")
    public ApiResponse productos() {
        return ApiResponse.ok(productoRepo.findAllByOrderByIdAsc().stream()
                .map(Dto.ProductoResponse::from).toList());
    }

    // :) Datos completos y categoría existente -> 200, producto con el ID asignado.
    // :) Stock omitido o null -> 200, stock queda en 0.
    // :( Nombre, precio, stock, categoriaID nulo, JSON mal hecho, body vacío, tipo de dato incorrecto. -> 400.
    // :( categoriaId inexistente -> 404 ("Categoría no encontrada").
    // :( Content-Type distinto de JSON -> 415.
    @PostMapping("/productos")
    public ApiResponse crearProducto(@Valid @RequestBody Dto.ProductoRequest r) {
        Categoria categoria = categoriaRepo.findById(r.categoriaId())
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada :|"));
        Producto saved = productoRepo.save(new Producto(
                r.nombre(), r.precio(), r.stock() == null ? 0 : r.stock(), categoria));
        return ApiResponse.ok(List.of(Dto.ProductoResponse.from(saved)));
    }

    // :) Producto existente -> 200, lista vacía, y se invoca deleteById.
    // :( ID inexistente -> 404, no se invoca deleteById.
    // :( ID no numérico -> 400.
    // :( Eliminar dos veces el mismo ID -> 200 el primero, 404 la segunda petición.
    // :( Producto referenciado en una orden -> 409
    @DeleteMapping("/productos/{id}")
    public ApiResponse eliminarProducto(@PathVariable long id) {
        if (!productoRepo.existsById(id)) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        productoRepo.deleteById(id);
        return ApiResponse.ok(List.of());
    }

    @PutMapping("/productos/{id}")
    public ApiResponse actualizarProducto(@PathVariable long id,
                                          @Valid @RequestBody Dto.ProductoRequest r) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        Categoria categoria = categoriaRepo.findById(r.categoriaId())
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada :|"));
        p.actualizar(r.nombre(), r.precio(), r.stock() == null ? p.getStock() : r.stock(), categoria);
        return ApiResponse.ok(List.of(Dto.ProductoResponse.from(productoRepo.save(p))));
    }

    @PatchMapping("/productos/{id}/stock")
    public ApiResponse actualizarStock(@PathVariable long id,
                                       @Valid @RequestBody Dto.StockRequest r) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        p.establecerStock(r.stock());
        return ApiResponse.ok(List.of(Dto.ProductoResponse.from(productoRepo.save(p))));
    }
}
