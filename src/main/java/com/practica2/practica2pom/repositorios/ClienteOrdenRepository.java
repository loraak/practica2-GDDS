package com.practica2.practica2pom.repositorios;

import com.practica2.practica2pom.entities.ClienteOrden;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteOrdenRepository extends JpaRepository<ClienteOrden, Long> {
    @EntityGraph(attributePaths = {"cliente", "items", "items.producto"})
    List<ClienteOrden> findAllByOrderByIdAsc();
}
