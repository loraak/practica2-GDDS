package com.practica2.practica2pom.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String nombre;

    protected Categoria() {}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
