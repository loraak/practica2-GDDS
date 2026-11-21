import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, Usuario, UsuarioRequest } from '../api.service';

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule],
  templateUrl: './usuarios.html',
})
export class Usuarios implements OnInit {
  private api = inject(ApiService);
  usuarios = signal<Usuario[]>([]);
  editandoId = signal<number | null>(null);
  error = signal('');
  form: UsuarioRequest = { nombre: '', correo: '', password: '' };

  ngOnInit() { this.cargar(); }
  cargar() { this.api.usuarios().subscribe(u => this.usuarios.set(u)); }

  guardar() {
    const id = this.editandoId();
    const peticion = id ? this.api.actualizarUsuario(id, this.form) : this.api.crearUsuario(this.form);
    peticion.subscribe({
      next: () => { this.cancelar(); this.cargar(); },
      error: e => this.error.set(e.error?.data?.[0]?.error ?? 'Error al guardar'),
    });
  }

  editar(u: Usuario) {
    this.form = { nombre: u.nombre, correo: u.correo, password: '' }; // vacío = no cambiar
    this.editandoId.set(u.id);
  }

  cancelar() {
    this.form = { nombre: '', correo: '', password: '' };
    this.editandoId.set(null);
    this.error.set('');
  }

  eliminar(id: number) {
    this.api.eliminarUsuario(id).subscribe({
      next: () => this.cargar(),
      error: e => this.error.set(e.error?.data?.[0]?.error ?? 'No se pudo eliminar'),
    });
  }
}
