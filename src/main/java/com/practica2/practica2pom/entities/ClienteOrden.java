package com.practica2.practica2pom.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes")
public class ClienteOrden {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "creado_en", nullable = false)
    private String creadoEn = LocalDateTime.now().format(FMT);

    @OneToMany(mappedBy =  "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenItem> items = new ArrayList<>();

    protected ClienteOrden() {}

    public ClienteOrden(Cliente cliente) {
        this.cliente = cliente;
    }

    public void agregarProducto(Producto producto, int cantidad) {
        items.add(new OrdenItem(this, producto, cantidad));
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public String getCreadoEn() { return creadoEn; }
    public List<OrdenItem>  getItems() { return items; }
}
