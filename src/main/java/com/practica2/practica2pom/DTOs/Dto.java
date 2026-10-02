package com.practica2.practica2pom.DTOs;

import com.practica2.practica2pom.entities.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public final class Dto {
    private Dto() {}

    public record ProductoRequest(
            @NotBlank String nombre,
            @NotNull @PositiveOrZero Double precio,
            @PositiveOrZero Integer stock,
            @NotNull Long categoriaId) {}

    public record ClienteRequest(
            @NotBlank String nombre,
            @NotBlank @Email String correo){}

    public record OrdenItemRequest(
            @NotNull Long productoId,
            @NotNull @Positive Integer cantidad) {}

    public record OrdenRequest(
            @NotNull Long clienteId,
            @NotEmpty @Valid List<OrdenItemRequest> items) {}

    public record StockRequest(@NotNull @PositiveOrZero Integer stock) {}

    public record CategoriaResponse(Long id, String nombre) {
        public static CategoriaResponse from (Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre());
        }
    }

    public record ProductoResponse(Long id, String nombre, double precio, int stock, String categoria) {
        public static ProductoResponse from (Producto p) {
            return new ProductoResponse(p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), p.getCategoria().getNombre());
        }
    }

    public record ClienteResponse(Long id, String nombre, String correo) {
        public static ClienteResponse from (Cliente c) {
            return new ClienteResponse(c.getId(), c.getNombre(), c.getCorreo());
        }
    }

    public record OrdenItemResponse(String producto, int cantidad, double unidadPrecio, double subtotal) {
        public static OrdenItemResponse from(OrdenItem i) {
            return new OrdenItemResponse(i.getProducto().getNombre(), i.getCantidad(), i.getUnidadPrecio(), i.getCantidad() * i.getUnidadPrecio());
        }
    }

    public record OrdenResponse(Long ordenId, String creadoEn, String cliente, List<OrdenItemResponse> items, double total) {
        public static OrdenResponse from (ClienteOrden o) {
            List<OrdenItemResponse> items = o.getItems().stream().map(OrdenItemResponse::from).toList();
            double total = items.stream().mapToDouble(OrdenItemResponse::subtotal).sum();
            return new OrdenResponse(o.getId(), o.getCreadoEn(), o.getCliente().getNombre(), items, total);
        }
    }
}
