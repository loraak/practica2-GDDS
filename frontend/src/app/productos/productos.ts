import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';          // ← esta línea
import { FormsModule } from '@angular/forms';
import { ApiService, Categoria, Producto, ProductoRequest } from '../api.service';

@Component({
  selector: 'app-productos',
  imports: [FormsModule, CurrencyPipe],                    // ← y CurrencyPipe aquí
  templateUrl: './productos.html',
})

export class Productos implements OnInit {
  private api = inject(ApiService);
  productos = signal<Producto[]>([]);
  categorias = signal<Categoria[]>([]);
  editandoId = signal<number | null>(null);
  error = signal('');
  form: ProductoRequest = this.vacio();

  ngOnInit() {
    this.api.categorias().subscribe(c => this.categorias.set(c));
    this.cargar();
  }

  cargar() { this.api.productos().subscribe(p => this.productos.set(p)); }

  vacio(): ProductoRequest { return { nombre: '', precio: 0, stock: 0, categoriaId: 1 }; }

  guardar() {
    const id = this.editandoId();
    const peticion = id ? this.api.actualizarProducto(id, this.form) : this.api.crearProducto(this.form);
    peticion.subscribe({
      next: () => { this.cancelar(); this.cargar(); },
      error: e => this.error.set(e.error?.data?.[0]?.error ?? 'Error al guardar'),
    });
  }

  editar(p: Producto) {
    const cat = this.categorias().find(c => c.nombre === p.categoria);
    this.form = { nombre: p.nombre, precio: p.precio, stock: p.stock, categoriaId: cat?.id ?? 1 };
    this.editandoId.set(p.id);
  }

  cancelar() {
    this.form = this.vacio();
    this.editandoId.set(null);
    this.error.set('');
  }

  eliminar(id: number) {
    this.api.eliminarProducto(id).subscribe({
      next: () => this.cargar(),
      error: e => this.error.set(e.error?.data?.[0]?.error ?? 'No se pudo eliminar'),
    });
  }
}
