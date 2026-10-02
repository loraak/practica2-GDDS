package com.practica2.practica2pom.repositorios;

import com.practica2.practica2pom.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
