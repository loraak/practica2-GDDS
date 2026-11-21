import { test, expect } from '@playwright/test';

const ADMIN = { correo: 'admin@practica2.com', password: 'admin123' };

test.describe('API - salud y login', () => {
    test('GET /api/health responde 200', async ({ request }) => {
        const res = await request.get('/api/health');
        expect(res.status()).toBe(200);
        expect((await res.json()).statusCode).toBe(200);
    });

    test('login correcto -> 200 sin exponer password', async ({ request }) => {
        const res = await request.post('/api/auth/login', { data: ADMIN });
        expect(res.status()).toBe(200);
        const body = await res.json();
        expect(body.data[0].correo).toBe(ADMIN.correo);
        expect(body.data[0].password).toBeUndefined();
    });

    test('login con contraseña incorrecta -> 401', async ({ request }) => {
        const res = await request.post('/api/auth/login', { data: { ...ADMIN, password: 'mala' } });
        expect(res.status()).toBe(401);
    });
});

test.describe('API - CRUD de productos', () => {
    test('crear, consultar, actualizar, cambiar stock y eliminar', async ({ request }) => {
        const nombre = `API-Prod-${Date.now()}`;

        // Crear
        let res = await request.post('/api/productos', {
            data: { nombre, precio: 100, stock: 5, categoriaId: 1 },
        });
        expect(res.status()).toBe(200);
        const id = (await res.json()).data[0].id;

        // Consultar por id
        res = await request.get(`/api/productos/${id}`);
        expect(res.status()).toBe(200);
        expect((await res.json()).data[0].nombre).toBe(nombre);

        // Actualizar
        res = await request.put(`/api/productos/${id}`, {
            data: { nombre: `${nombre}-edit`, precio: 150, stock: 8, categoriaId: 2 },
        });
        expect(res.status()).toBe(200);
        expect((await res.json()).data[0].precio).toBe(150);

        // Cambiar stock
        res = await request.patch(`/api/productos/${id}/stock`, { data: { stock: 20 } });
        expect(res.status()).toBe(200);
        expect((await res.json()).data[0].stock).toBe(20);

        // Eliminar y comprobar que ya no existe
        res = await request.delete(`/api/productos/${id}`);
        expect(res.status()).toBe(200);
        res = await request.get(`/api/productos/${id}`);
        expect(res.status()).toBe(404);
    });

    test('producto inválido -> 400', async ({ request }) => {
        const res = await request.post('/api/productos', { data: { nombre: '', precio: -5 } });
        expect(res.status()).toBe(400);
    });

    test('categoría inexistente -> 404', async ({ request }) => {
        const res = await request.post('/api/productos', {
            data: { nombre: 'X', precio: 1, stock: 1, categoriaId: 9999 },
        });
        expect(res.status()).toBe(404);
    });
});

test.describe('API - CRUD de usuarios', () => {
    test('crear, actualizar, login con el nuevo usuario y eliminar', async ({ request }) => {
        const correo = `user${Date.now()}@test.com`;

        let res = await request.post('/api/usuarios', {
            data: { nombre: 'Prueba', correo, password: 'secreto1' },
        });
        expect(res.status()).toBe(200);
        const id = (await res.json()).data[0].id;

        res = await request.put(`/api/usuarios/${id}`, {
            data: { nombre: 'Prueba Editada', correo, password: 'nueva123' },
        });
        expect(res.status()).toBe(200);
        expect((await res.json()).data[0].nombre).toBe('Prueba Editada');

        res = await request.post('/api/auth/login', { data: { correo, password: 'nueva123' } });
        expect(res.status()).toBe(200);

        res = await request.delete(`/api/usuarios/${id}`);
        expect(res.status()).toBe(200);
        res = await request.get(`/api/usuarios/${id}`);
        expect(res.status()).toBe(404);
    });

    test('correo duplicado -> 409', async ({ request }) => {
        const res = await request.post('/api/usuarios', {
            data: { nombre: 'Dup', correo: ADMIN.correo, password: 'secreto1' },
        });
        expect(res.status()).toBe(409);
    });
});