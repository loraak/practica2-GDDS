package com.practica2.practica2pom.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    protected Producto() {}

    public Producto(String nombre, double precio, int stock, Categoria categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public void quitarStock(int cantidad) {
        if (cantidad > stock) {
            throw new IllegalArgumentException("Stock insuficiente: " + nombre);
        }
        stock -= cantidad;
    }

    public void actualizar(String nombre, double precio, int stock, Categoria categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public void establecerStock(int stock) {
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public Categoria getCategoria() { return categoria; }
}
