import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css'
})
export class MainLayout {

  rol: string = '';
  email: string = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaAutenticado()) {
      localStorage.clear();
      this.router.navigate(['/login']);
      return;
    }

    this.rol = (this.authService.getRol() || '').toUpperCase();
    this.email = this.authService.getEmail() || '';
  }

  esAdmin(): boolean {
    return this.rol === 'ADMIN';
  }

  esRecepcionista(): boolean {
    return this.rol === 'RECEPCIONISTA';
  }

  esMedico(): boolean {
    return this.rol === 'MEDICO';
  }

  esPaciente(): boolean {
    return this.rol === 'PACIENTE';
  }

  logout(): void {
    localStorage.clear();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}