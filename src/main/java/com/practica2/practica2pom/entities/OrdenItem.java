package com.practica2.practica2pom.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "orden_items")
public class OrdenItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private ClienteOrden orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "unidad_precio", nullable = false)
    private double unidadPrecio;

    protected OrdenItem() {}

    public OrdenItem(ClienteOrden orden, Producto producto, int cantidad) {
        this.orden = orden;
        this.producto = producto;
        this.cantidad = cantidad;
        this.unidadPrecio = producto.getPrecio();
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getUnidadPrecio() { return unidadPrecio; }
}
