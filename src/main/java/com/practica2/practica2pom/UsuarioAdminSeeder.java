package com.practica2.practica2pom;

import com.practica2.practica2pom.entities.Usuario;
import com.practica2.practica2pom.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAdminSeeder implements CommandLineRunner {
    private final UsuarioRepository repo;
    @Value("${app.admin.correo:admin@practica2.com}") private String correo;
    @Value("${app.admin.password:admin123}") private String password;

    public UsuarioAdminSeeder(UsuarioRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (!repo.existsByCorreo(correo)) {
            repo.save(new Usuario("Administrador", correo, new BCryptPasswordEncoder().encode(password)));
        }
    }
}