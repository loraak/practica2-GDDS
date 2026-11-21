package com.practica2.practica2pom;

import com.practica2.practica2pom.controladores.UsuarioController;
import com.practica2.practica2pom.entities.Usuario;
import com.practica2.practica2pom.repositorios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioRepository usuarioRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private Usuario usuario(long id, String correo, String passPlano) {
        Usuario u = new Usuario("Karol", correo, encoder.encode(passPlano));
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private void saveDevuelveArgumento() {
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            if (u.getId() == null) ReflectionTestUtils.setField(u, "id", 5L);
            return u;
        });
    }

    // ---------- login ----------
    @Test
    void login_ok() throws Exception {
        when(usuarioRepo.findByCorreo("k@test.com")).thenReturn(Optional.of(usuario(1, "k@test.com", "secreto1")));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"k@test.com\",\"password\":\"secreto1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].correo").value("k@test.com"))
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
    }

    @Test
    void login_passwordIncorrecto_401() throws Exception {
        when(usuarioRepo.findByCorreo("k@test.com")).thenReturn(Optional.of(usuario(1, "k@test.com", "secreto1")));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"k@test.com\",\"password\":\"otra\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void login_correoInexistente_401() throws Exception {
        when(usuarioRepo.findByCorreo(anyString())).thenReturn(Optional.empty());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"no@test.com\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- CRUD ----------
    @Test
    void listar_ok() throws Exception {
        when(usuarioRepo.findAll(any(Sort.class))).thenReturn(List.of(usuario(1, "k@test.com", "secreto1")));
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void obtener_noExiste_404() throws Exception {
        when(usuarioRepo.findById(9L)).thenReturn(Optional.empty());
        mvc.perform(get("/api/usuarios/9")).andExpect(status().isNotFound());
    }

    @Test
    void crear_ok_guardaHash() throws Exception {
        saveDevuelveArgumento();
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@test.com\",\"password\":\"secreto1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(5));
        verify(usuarioRepo).save(argThat(u -> !u.getPassword().equals("secreto1")));
    }

    @Test
    void crear_invalido_400() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"correo\":\"malo\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void crear_correoDuplicado_409() throws Exception {
        when(usuarioRepo.save(any(Usuario.class))).thenThrow(new DataIntegrityViolationException("dup"));
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@test.com\",\"password\":\"secreto1\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizar_ok_conPassword() throws Exception {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario(1, "k@test.com", "secreto1")));
        saveDevuelveArgumento();
        mvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Karol V\",\"correo\":\"kv@test.com\",\"password\":\"nueva123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Karol V"));
    }

    @Test
    void actualizar_noExiste_404() throws Exception {
        when(usuarioRepo.findById(9L)).thenReturn(Optional.empty());
        mvc.perform(put("/api/usuarios/9").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\",\"correo\":\"x@test.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_ok() throws Exception {
        when(usuarioRepo.existsById(1L)).thenReturn(true);
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isOk());
        verify(usuarioRepo).deleteById(1L);
    }

    @Test
    void eliminar_noExiste_404() throws Exception {
        when(usuarioRepo.existsById(9L)).thenReturn(false);
        mvc.perform(delete("/api/usuarios/9")).andExpect(status().isNotFound());
        verify(usuarioRepo, never()).deleteById(anyLong());
    }
}