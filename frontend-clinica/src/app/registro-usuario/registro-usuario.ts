import { Component } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { RouterLink } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { PacienteService } from '../core/paciente.service';

@Component({
  selector: 'app-registro-usuario',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro-usuario.html',
  styleUrl: './registro-usuario.css'
})
export class RegistroUsuario {

  paciente = {
    nombre: '',
    apellido: '',
    telefono: '',
    email: '',
    fechaNacimiento: '',
    direccion: '',
    seguroMedico: ''
  };

  usuario = {
    username: '',
    password: ''
  };

  mensaje = '';
  error = '';

  constructor(
    private authService: AuthService,
    private pacienteService: PacienteService
  ) {}

  registrar(): void {

    this.mensaje = '';
    this.error = '';

    if (
      !this.paciente.nombre ||
      !this.paciente.apellido ||
      !this.paciente.telefono ||
      !this.paciente.email ||
      !this.paciente.fechaNacimiento ||
      !this.paciente.direccion
    ) {
      this.error = 'Debe completar los datos obligatorios del paciente';
      return;
    }

    if (!this.emailValido(this.paciente.email)) {
      this.error = 'Debe ingresar un email válido para el paciente';
      return;
    }

    if (
      !this.usuario.username ||
      !this.usuario.password
    ) {
      this.error = 'Debe completar usuario y contraseña';
      return;
    }

    const pacientePayload = {
      nombre: this.paciente.nombre,
      apellido: this.paciente.apellido,
      telefono: this.paciente.telefono,
      email: this.paciente.email,
      fechaNacimiento: this.paciente.fechaNacimiento,
      direccion: this.paciente.direccion,
      seguroMedico: this.paciente.seguroMedico
    };

    this.pacienteService.guardar(pacientePayload).subscribe({

      next: (pacienteGuardado) => {

        const usuarioPayload = {
          username: this.usuario.username,
          email: this.paciente.email,
          password: this.usuario.password,
          rol: 'PACIENTE',
          pacienteId: pacienteGuardado.id
        };

        this.authService.registrar(usuarioPayload).subscribe({

          next: () => {

            this.mensaje = 'Paciente registrado correctamente. Ya puedes iniciar sesión.';

            this.paciente = {
              nombre: '',
              apellido: '',
              telefono: '',
              email: '',
              fechaNacimiento: '',
              direccion: '',
              seguroMedico: ''
            };

            this.usuario = {
              username: '',
              password: ''
            };
          },

          error: (err) => {
            this.error =
              err.error?.message ||
              err.error ||
              'El paciente fue creado, pero ocurrió un error al crear el usuario';
          }
        });
      },

      error: (err) => {
        this.error =
          err.error?.message ||
          err.error ||
          'Error al registrar los datos del paciente';
      }
    });
  }

  emailValido(email: string): boolean {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  }
}