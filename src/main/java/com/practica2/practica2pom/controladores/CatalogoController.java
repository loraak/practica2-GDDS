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

    @GetMapping("/categorias")
    public ApiResponse categorias() {
        return ApiResponse.ok(categoriaRepo.findAll(Sort.by("id")).stream()
                .map(Dto.CategoriaResponse::from).toList());
    }

    @GetMapping("/productos")
    public ApiResponse productos() {
        return ApiResponse.ok(productoRepo.findAllByOrderByIdAsc().stream()
                .map(Dto.ProductoResponse::from).toList());
    }

    @PostMapping("/productos")
    public ApiResponse crearProducto(@Valid @RequestBody Dto.ProductoRequest r) {
        Categoria categoria = categoriaRepo.findById(r.categoriaId())
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada :|"));
        Producto saved = productoRepo.save(new Producto(
                r.nombre(), r.precio(), r.stock() == null ? 0 : r.stock(), categoria));
        return ApiResponse.ok(List.of(Dto.ProductoResponse.from(saved)));
    }

    @DeleteMapping("/productos/{id}")
    public ApiResponse eliminarProducto(@PathVariable long id) {
        if (!productoRepo.existsById(id)) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        productoRepo.deleteById(id);
        return ApiResponse.ok(List.of());
    }
}
