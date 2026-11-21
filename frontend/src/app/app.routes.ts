import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Productos } from './productos/productos';
import { Usuarios } from './usuarios/usuarios';
import { authGuard } from './auth.service';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'productos', component: Productos, canActivate: [authGuard] },
  { path: 'usuarios', component: Usuarios, canActivate: [authGuard] },
  { path: '**', redirectTo: 'productos' },
];
