import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface ApiResponse<T> { statusCode: number; data: T[]; }
export interface Categoria { id: number; nombre: string; }
export interface Producto { id: number; nombre: string; precio: number; stock: number; categoria: string; }
export interface ProductoRequest { nombre: string; precio: number; stock: number; categoriaId: number; }
export interface Usuario { id: number; nombre: string; correo: string; }
export interface UsuarioRequest { nombre: string; correo: string; password?: string; }

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  // Todas tus respuestas vienen como { statusCode, data: [...] }; aquí nos quedamos con data
  private datos<T>(obs: Observable<ApiResponse<T>>) { return obs.pipe(map(r => r.data)); }

  login(correo: string, password: string) {
    return this.datos(this.http.post<ApiResponse<Usuario>>('/api/auth/login', { correo, password }));
  }

  categorias() { return this.datos(this.http.get<ApiResponse<Categoria>>('/api/categorias')); }

  productos() { return this.datos(this.http.get<ApiResponse<Producto>>('/api/productos')); }
  crearProducto(p: ProductoRequest) { return this.http.post<ApiResponse<Producto>>('/api/productos', p); }
  actualizarProducto(id: number, p: ProductoRequest) { return this.http.put<ApiResponse<Producto>>(`/api/productos/${id}`, p); }
  eliminarProducto(id: number) { return this.http.delete<ApiResponse<unknown>>(`/api/productos/${id}`); }

  usuarios() { return this.datos(this.http.get<ApiResponse<Usuario>>('/api/usuarios')); }
  crearUsuario(u: UsuarioRequest) { return this.http.post<ApiResponse<Usuario>>('/api/usuarios', u); }
  actualizarUsuario(id: number, u: UsuarioRequest) { return this.http.put<ApiResponse<Usuario>>(`/api/usuarios/${id}`, u); }
  eliminarUsuario(id: number) { return this.http.delete<ApiResponse<unknown>>(`/api/usuarios/${id}`); }
}
