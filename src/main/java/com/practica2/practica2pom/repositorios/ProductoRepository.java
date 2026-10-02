package com.practica2.practica2pom.repositorios;

import com.practica2.practica2pom.entities.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @EntityGraph(attributePaths =  "categoria")
    List<Producto> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findWithCategoriaById(Long id);
}
