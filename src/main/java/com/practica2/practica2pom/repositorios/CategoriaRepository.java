package com.practica2.practica2pom.repositorios;

import com.practica2.practica2pom.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
