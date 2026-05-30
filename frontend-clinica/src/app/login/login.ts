import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  email = '';
  password = '';

  mensaje = '';
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {

    this.mensaje = '';
    this.error = '';

    this.authService.login({
      email: this.email,
      password: this.password
    }).subscribe({

      next: (res) => {

        this.mensaje = res.message || 'Inicio de sesión correcto';

        const rol = (res.data.usuario.rol || '').toUpperCase();

        if (rol === 'ADMIN') {
          this.router.navigate(['/app/medicos']);

        } else if (rol === 'RECEPCIONISTA') {
          this.router.navigate(['/app/pacientes']);

        } else if (rol === 'MEDICO') {
          this.router.navigate(['/app/citas']);

        } else if (rol === 'PACIENTE') {
          this.router.navigate(['/app/citas']);

        } else {
          this.router.navigate(['/login']);
        }
      },

      error: (err) => {
        this.error =
          err.error?.message ||
          'Credenciales incorrectas';
      }
    });
  }
}