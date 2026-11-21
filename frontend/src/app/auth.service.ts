import { Injectable, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { tap } from 'rxjs';
import { ApiService, Usuario } from './api.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);
  usuario = signal<Usuario | null>(JSON.parse(localStorage.getItem('usuario') ?? 'null'));

  login(correo: string, password: string) {
    return this.api.login(correo, password).pipe(tap(data => {
      localStorage.setItem('usuario', JSON.stringify(data[0]));
      this.usuario.set(data[0]);
    }));
  }

  logout() {
    localStorage.removeItem('usuario');
    this.usuario.set(null);
  }
}

// Si no hay sesión, manda al login
export const authGuard: CanActivateFn = () =>
  inject(AuthService).usuario() ? true : inject(Router).createUrlTree(['/login']);
