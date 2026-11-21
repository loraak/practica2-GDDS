package com.practica2.practica2pom.controladores;

import com.practica2.practica2pom.ApiResponse;
import com.practica2.practica2pom.CredencialesInvalidasException;
import com.practica2.practica2pom.DTOs.Dto;
import com.practica2.practica2pom.entities.Usuario;
import com.practica2.practica2pom.repositorios.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class UsuarioController {
    private final UsuarioRepository usuarioRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioController(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    // :) Correo y contraseña correctos -> 200 con el usuario.
    // :( Correo inexistente o contraseña incorrecta -> 401.
    @PostMapping("/auth/login")
    public ApiResponse login(@Valid @RequestBody Dto.LoginRequest r) {
        Usuario u = usuarioRepo.findByCorreo(r.correo())
                .filter(x -> encoder.matches(r.password(), x.getPassword()))
                .orElseThrow(CredencialesInvalidasException::new);
        return ApiResponse.ok(List.of(Dto.UsuarioResponse.from(u)));
    }

    @GetMapping("/usuarios")
    public ApiResponse listar() {
        return ApiResponse.ok(usuarioRepo.findAll(Sort.by("id")).stream()
                .map(Dto.UsuarioResponse::from).toList());
    }

    @GetMapping("/usuarios/{id}")
    public ApiResponse obtener(@PathVariable long id) {
        return ApiResponse.ok(List.of(Dto.UsuarioResponse.from(buscar(id))));
    }

    // :( Correo duplicado -> 409 (lo atrapa el handler de DataAccessException).
    @PostMapping("/usuarios")
    public ApiResponse crear(@Valid @RequestBody Dto.UsuarioRequest r) {
        Usuario saved = usuarioRepo.save(
                new Usuario(r.nombre(), r.correo(), encoder.encode(r.password())));
        return ApiResponse.ok(List.of(Dto.UsuarioResponse.from(saved)));
    }

    @PutMapping("/usuarios/{id}")
    public ApiResponse actualizar(@PathVariable long id,
                                  @Valid @RequestBody Dto.UsuarioUpdateRequest r) {
        Usuario u = buscar(id);
        u.actualizar(r.nombre(), r.correo());
        if (r.password() != null && !r.password().isBlank()) {
            u.cambiarPassword(encoder.encode(r.password()));
        }
        return ApiResponse.ok(List.of(Dto.UsuarioResponse.from(usuarioRepo.save(u))));
    }

    @DeleteMapping("/usuarios/{id}")
    public ApiResponse eliminar(@PathVariable long id) {
        if (!usuarioRepo.existsById(id)) throw new NoSuchElementException("Usuario no encontrado");
        usuarioRepo.deleteById(id);
        return ApiResponse.ok(List.of());
    }

    private Usuario buscar(long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}