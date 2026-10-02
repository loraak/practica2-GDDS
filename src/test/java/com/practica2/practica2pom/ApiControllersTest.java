package com.practica2.practica2pom;

import com.practica2.practica2pom.controladores.CatalogoController;
import com.practica2.practica2pom.controladores.VentasController;
import com.practica2.practica2pom.entities.Categoria;
import com.practica2.practica2pom.entities.Cliente;
import com.practica2.practica2pom.entities.ClienteOrden;
import com.practica2.practica2pom.entities.Producto;
import com.practica2.practica2pom.repositorios.CategoriaRepository;
import com.practica2.practica2pom.repositorios.ClienteOrdenRepository;
import com.practica2.practica2pom.repositorios.ClienteRepository;
import com.practica2.practica2pom.repositorios.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest({CatalogoController.class, VentasController.class})
class ApiControllersTest {

    @Autowired MockMvc mvc;
    @MockitoBean
    CategoriaRepository categoriaRepo;
    @MockitoBean
    ProductoRepository productoRepo;
    @MockitoBean
    ClienteRepository clienteRepo;
    @MockitoBean
    ClienteOrdenRepository ordenRepo;

    // ------------------------- helpers -------------------------

    private Categoria cat(String nombre) {
        Categoria c = BeanUtils.instantiateClass(Categoria.class);
        ReflectionTestUtils.setField(c, "nombre", nombre);
        return c;
    }

    private Producto prod(long id, String nombre, double precio, int stock, Categoria c) {
        Producto p = new Producto(nombre, precio, stock, c);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private static final String PRODUCTO_OK =
            """
            {"nombre":"Croquetas","precio":250.5,"stock":10,"categoriaId":1}
            """;

    // =================== 1. GET /api/categorias ===================

    @Test
    void getCategorias_ok() throws Exception {
        Categoria gatos = cat("Gatos");
        ReflectionTestUtils.setField(gatos, "id", 1L);
        when(categoriaRepo.findAll(any(Sort.class))).thenReturn(List.of(gatos));

        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data[0].nombre").value("Gatos"));
    }

    @Test
    void getCategorias_vacio() throws Exception {
        when(categoriaRepo.findAll(any(Sort.class))).thenReturn(List.of());
        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =================== 2. GET /api/productos ===================

    @Test
    void getProductos_ok() throws Exception {
        when(productoRepo.findAllByOrderByIdAsc())
                .thenReturn(List.of(prod(1, "Croquetas", 250.5, 10, cat("Perros"))));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Croquetas"))
                .andExpect(jsonPath("$.data[0].categoria").value("Perros"));
    }

    @Test
    void getProductos_vacio() throws Exception {
        when(productoRepo.findAllByOrderByIdAsc()).thenReturn(List.of());
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =================== 3. POST /api/productos ===================

    @Test
    void crearProducto_ok() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat("Perros")));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Croquetas"))
                .andExpect(jsonPath("$.data[0].stock").value(10));
    }

    @Test
    void crearProducto_stockOmitido_quedaEnCero() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat("Perros")));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"Croquetas","precio":250,"categoriaId":1}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].stock").value(0));
    }

    @Test
    void crearProducto_precioCero_esValido() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat("Perros")));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"Muestra","precio":0,"stock":1,"categoriaId":1}
                    """))
                .andExpect(status().isOk());
    }

    @Test
    void crearProducto_nombreVacio_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"","precio":10,"stock":1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[0].field").value("nombre"));
    }

    @Test
    void crearProducto_nombreAusente_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"precio":10,"stock":1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_precioNegativo_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"X","precio":-5,"stock":1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[0].field").value("precio"));
    }

    @Test
    void crearProducto_precioNulo_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"X","stock":1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_stockNegativo_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"X","precio":10,"stock":-1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[0].field").value("stock"));
    }

    @Test
    void crearProducto_categoriaNula_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"X","precio":10,"stock":1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_categoriaInexistente_404() throws Exception {
        when(categoriaRepo.findById(1L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_OK))
                .andExpect(status().isNotFound());
        verify(productoRepo, never()).save(any());
    }

    @Test
    void crearProducto_jsonMalFormado_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{nombre: X"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[0].error").value("JSON inválido"));
    }

    @Test
    void crearProducto_bodyVacio_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_tipoIncorrecto_400() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"X","precio":"abc","stock":1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_contentTypeIncorrecto_415() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.TEXT_PLAIN).content(PRODUCTO_OK))
                .andExpect(status().isUnsupportedMediaType());
    }

    // =================== 4. DELETE /api/productos/{id} ===================

    @Test
    void eliminarProducto_ok() throws Exception {
        when(productoRepo.existsById(1L)).thenReturn(true);
        mvc.perform(delete("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        verify(productoRepo).deleteById(1L);
    }

    @Test
    void eliminarProducto_inexistente_404() throws Exception {
        when(productoRepo.existsById(99L)).thenReturn(false);
        mvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound());
        verify(productoRepo, never()).deleteById(anyLong());
    }

    @Test
    void eliminarProducto_idNoNumerico_400() throws Exception {
        mvc.perform(delete("/api/productos/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarProducto_dosVeces_segundaDa404() throws Exception {
        when(productoRepo.existsById(1L)).thenReturn(true, false);
        mvc.perform(delete("/api/productos/1")).andExpect(status().isOk());
        mvc.perform(delete("/api/productos/1")).andExpect(status().isNotFound());
    }

    @Test
    void eliminarProducto_referenciadoEnOrden_409() throws Exception {
        when(productoRepo.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("FK")).when(productoRepo).deleteById(1L);
        mvc.perform(delete("/api/productos/1"))
                .andExpect(status().isConflict());
    }

    // =================== 5. GET /api/clientes ===================

    @Test
    void getClientes_ok() throws Exception {
        when(clienteRepo.findAll()).thenReturn(List.of(new Cliente("Ana", "ana@mail.com")));
        mvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].correo").value("ana@mail.com"));
    }

    @Test
    void getClientes_vacio() throws Exception {
        when(clienteRepo.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =================== 6. POST /api/clientes ===================

    @Test
    void crearCliente_ok() throws Exception {
        when(clienteRepo.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"Ana","correo":"ana@mail.com"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Ana"));
    }

    @Test
    void crearCliente_nombreVacio_400() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"","correo":"ana@mail.com"}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearCliente_correoAusente_400() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"Ana"}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearCliente_correoInvalido_400() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"Ana","correo":"sin-arroba"}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[0].field").value("correo"));
    }

    @Test
    void crearCliente_correoDuplicado_409() throws Exception {
        when(clienteRepo.save(any(Cliente.class))).thenThrow(new DataIntegrityViolationException("UNIQUE"));
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"Ana","correo":"ana@mail.com"}
                    """))
                .andExpect(status().isConflict());
    }

    @Test
    void crearCliente_jsonMalFormado_400() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("{{"))
                .andExpect(status().isBadRequest());
    }

    // =================== 7. GET /api/ordenes ===================

    @Test
    void getOrdenes_ok() throws Exception {
        Producto p = prod(1, "Croquetas", 100, 10, cat("Perros"));
        ClienteOrden o = new ClienteOrden(new Cliente("Ana", "ana@mail.com"));
        o.agregarProducto(p, 2);
        when(ordenRepo.findAllByOrderByIdAsc()).thenReturn(List.of(o));

        mvc.perform(get("/api/ordenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].cliente").value("Ana"))
                .andExpect(jsonPath("$.data[0].total").value(200.0))
                .andExpect(jsonPath("$.data[0].items[0].cantidad").value(2));
    }

    @Test
    void getOrdenes_vacio() throws Exception {
        when(ordenRepo.findAllByOrderByIdAsc()).thenReturn(List.of());
        mvc.perform(get("/api/ordenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // =================== 8. POST /api/ordenes ===================

    @Test
    void crearOrden_ok_descuentaStock() throws Exception {
        Producto p = prod(1, "Croquetas", 100, 10, cat("Perros"));
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(new Cliente("Ana", "ana@mail.com")));
        when(productoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(ordenRepo.save(any(ClienteOrden.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":1,"cantidad":3}]}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].total").value(300.0));

        assertEquals(7, p.getStock());
    }

    @Test
    void crearOrden_variosProductos_ok() throws Exception {
        Producto p1 = prod(1, "Croquetas", 100, 10, cat("Perros"));
        Producto p2 = prod(2, "Arena", 50, 5, cat("Gatos"));
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(new Cliente("Ana", "ana@mail.com")));
        when(productoRepo.findById(1L)).thenReturn(Optional.of(p1));
        when(productoRepo.findById(2L)).thenReturn(Optional.of(p2));
        when(ordenRepo.save(any(ClienteOrden.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":1,"cantidad":1},{"productoId":2,"cantidad":2}]}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].total").value(200.0));

        assertEquals(9, p1.getStock());
        assertEquals(3, p2.getStock());
    }

    @Test
    void crearOrden_clienteInexistente_404() throws Exception {
        when(clienteRepo.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":99,"items":[{"productoId":1,"cantidad":1}]}
                    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearOrden_clienteNulo_400() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"items":[{"productoId":1,"cantidad":1}]}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearOrden_itemsVacios_400() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[]}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearOrden_itemsNulos_400() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearOrden_productoInexistente_404() throws Exception {
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(new Cliente("Ana", "ana@mail.com")));
        when(productoRepo.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":99,"cantidad":1}]}
                    """))
                .andExpect(status().isNotFound());
        verify(ordenRepo, never()).save(any());
    }

    @Test
    void crearOrden_cantidadCero_400() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":1,"cantidad":0}]}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearOrden_cantidadNegativa_400() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":1,"cantidad":-2}]}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearOrden_stockInsuficiente_400() throws Exception {
        Producto p = prod(1, "Croquetas", 100, 2, cat("Perros"));
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(new Cliente("Ana", "ana@mail.com")));
        when(productoRepo.findById(1L)).thenReturn(Optional.of(p));

        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":1,"cantidad":5}]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data[0].error").value("Stock insuficiente: Croquetas"));
        verify(ordenRepo, never()).save(any());
    }

    @Test
    void crearOrden_mismoProductoRepetido_superaStock_400() throws Exception {
        Producto p = prod(1, "Croquetas", 100, 5, cat("Perros"));
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(new Cliente("Ana", "ana@mail.com")));
        when(productoRepo.findById(1L)).thenReturn(Optional.of(p));

        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"clienteId":1,"items":[{"productoId":1,"cantidad":3},{"productoId":1,"cantidad":3}]}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearOrden_jsonMalFormado_400() throws Exception {
        mvc.perform(post("/api/ordenes").contentType(MediaType.APPLICATION_JSON).content("[[["))
                .andExpect(status().isBadRequest());
    }

    // =================== 9. PUT /api/productos/{id} ===================

    @Test
    void actualizarProducto_ok() throws Exception {
        Producto p = prod(1, "Viejo", 10, 3, cat("Perros"));
        when(productoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(categoriaRepo.findById(1L)).thenReturn(Optional.of(cat("Gatos")));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_OK))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Croquetas"))
                .andExpect(jsonPath("$.data[0].categoria").value("Gatos"));
    }

    @Test
    void actualizarProducto_inexistente_404() throws Exception {
        when(productoRepo.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(put("/api/productos/99").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_OK))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProducto_categoriaInexistente_404() throws Exception {
        when(productoRepo.findById(1L)).thenReturn(Optional.of(prod(1, "Viejo", 10, 3, cat("Perros"))));
        when(categoriaRepo.findById(1L)).thenReturn(Optional.empty());
        mvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_OK))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProducto_datosInvalidos_400() throws Exception {
        mvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nombre":"","precio":-1,"categoriaId":1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarProducto_idNoNumerico_400() throws Exception {
        mvc.perform(put("/api/productos/abc").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_OK))
                .andExpect(status().isBadRequest());
    }

    // =================== 10. PATCH /api/productos/{id}/stock ===================

    @Test
    void actualizarStock_ok() throws Exception {
        Producto p = prod(1, "Croquetas", 10, 3, cat("Perros"));
        when(productoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepo.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(patch("/api/productos/1/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"stock":50}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].stock").value(50));
    }

    @Test
    void actualizarStock_negativo_400() throws Exception {
        mvc.perform(patch("/api/productos/1/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"stock":-1}
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarStock_nulo_400() throws Exception {
        mvc.perform(patch("/api/productos/1/stock").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarStock_productoInexistente_404() throws Exception {
        when(productoRepo.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(patch("/api/productos/99/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"stock":5}
                    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarStock_tipoIncorrecto_400() throws Exception {
        mvc.perform(patch("/api/productos/1/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"stock":"mucho"}
                    """))
                .andExpect(status().isBadRequest());
    }
}