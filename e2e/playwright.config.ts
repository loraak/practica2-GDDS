import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './tests',
    timeout: 30_000,
    reporter: [
        ['list'],
        ['html', { open: 'never' }],
        ['json', { outputFile: 'results.json' }],   // lo usaremos en el paso 6 para mandar el resultado a OpenObserve
    ],
    use: {
        baseURL: process.env.BASE_URL ?? 'http://localhost:4200',
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
    },
    projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});