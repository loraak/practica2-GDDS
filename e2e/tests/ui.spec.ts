import { test, expect, Page } from '@playwright/test';

async function login(page: Page, correo = 'admin@practica2.com', password = 'admin123') {
    await page.goto('/login');
    await page.getByTestId('login-correo').fill(correo);
    await page.getByTestId('login-password').fill(password);
    await page.getByTestId('login-entrar').click();
}

test.describe('UI - autenticación', () => {
    test('sin sesión, /productos redirige al login', async ({ page }) => {
        await page.goto('/productos');
        await expect(page).toHaveURL(/\/login/);
    });

    test('credenciales incorrectas muestran error', async ({ page }) => {
        await login(page, 'admin@practica2.com', 'mala');
        await expect(page.getByTestId('login-error')).toBeVisible();
    });

    test('login correcto entra a productos y logout regresa al login', async ({ page }) => {
        await login(page);
        await expect(page).toHaveURL(/\/productos/);
        await expect(page.getByTestId('usuario-actual')).toHaveText('Administrador');
        await expect(page.getByTestId('tabla-productos')).toBeVisible();

        await page.getByTestId('logout').click();
        await expect(page).toHaveURL(/\/login/);
    });
});

test.describe('UI - CRUD', () => {
    test.beforeEach(async ({ page }) => {
        await login(page);
        await expect(page).toHaveURL(/\/productos/);
    });

    test('crear, editar y eliminar un producto', async ({ page }) => {
        const nombre = `UI-Prod-${Date.now()}`;
        const tabla = page.getByTestId('tabla-productos');

        // Crear
        await page.getByTestId('prod-nombre').fill(nombre);
        await page.getByTestId('prod-precio').fill('99.5');
        await page.getByTestId('prod-stock').fill('7');
        await page.getByTestId('prod-categoria').selectOption({ label: 'Perros' });
        await page.getByTestId('prod-guardar').click();

        const fila = tabla.locator('tr', { hasText: nombre });
        await expect(fila).toBeVisible();
        await expect(fila).toContainText('Perros');

        // Editar
        await fila.getByTestId('prod-editar').click();
        await page.getByTestId('prod-stock').fill('30');
        await page.getByTestId('prod-guardar').click();
        await expect(tabla.locator('tr', { hasText: nombre })).toContainText('30');

        // Eliminar
        await tabla.locator('tr', { hasText: nombre }).getByTestId('prod-eliminar').click();
        await expect(tabla.locator('tr', { hasText: nombre })).toHaveCount(0);
    });
});