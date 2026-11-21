import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',   // o './login.component.html', según como lo generó
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);
  correo = '';
  password = '';
  error = signal('');

  entrar() {
    this.error.set('');
    this.auth.login(this.correo, this.password).subscribe({
      next: () => this.router.navigate(['/productos']),
      error: () => this.error.set('Correo o contraseña incorrectos'),
    });
  }
}
