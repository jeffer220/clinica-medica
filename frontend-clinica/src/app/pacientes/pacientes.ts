import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { Router, RouterLink } from '@angular/router';

import { PacienteService } from '../core/paciente.service';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-pacientes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pacientes.html',
  styleUrl: './pacientes.css'
})
export class Pacientes implements OnInit {

  pacientes: any[] = [];

  editandoId: number | null = null;

  mensaje = '';
  error = '';

  paciente = this.crearPacienteVacio();

  crearUsuarioPaciente = false;

  usuarioPaciente = this.crearUsuarioPacienteVacio();

  mostrarPassword = false;

  constructor(
    private pacienteService: PacienteService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.listarPacientes();
  }

  crearPacienteVacio(): any {
    return {
      nombre: '',
      apellido: '',
      telefono: '',
      email: '',
      fechaNacimiento: '',
      direccion: '',
      seguroMedico: ''
    };
  }

  crearUsuarioPacienteVacio(): any {
    return {
      username: '',
      password: ''
    };
  }

  listarPacientes(): void {
    this.pacienteService.listar().subscribe({
      next: (data) => {
        this.pacientes = data;
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al listar pacientes';
      }
    });
  }

  guardarPaciente(): void {

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

    if (this.crearUsuarioPaciente && !this.editandoId) {

      if (
        !this.usuarioPaciente.username ||
        !this.usuarioPaciente.password
      ) {
        this.error = 'Debe completar usuario y contraseña del paciente';
        return;
      }
    }

    if (this.editandoId) {

      this.pacienteService.actualizar(this.editandoId, this.paciente).subscribe({
        next: () => {
          this.mensaje = 'Paciente actualizado correctamente';
          this.cancelarEdicion();
          this.listarPacientes();
        },
        error: (err) => {
          this.error =
            err.error?.message ||
            'Error al actualizar paciente';
        }
      });

      return;
    }

    this.pacienteService.guardar(this.paciente).subscribe({

      next: (pacienteGuardado) => {

        if (this.crearUsuarioPaciente) {

          const payload = {
            username: this.usuarioPaciente.username,
            email: this.paciente.email,
            password: this.usuarioPaciente.password,
            rol: 'PACIENTE',
            pacienteId: pacienteGuardado.id
          };

          this.authService.registrar(payload).subscribe({

            next: () => {
              this.mensaje = 'Paciente y usuario registrados correctamente';
              this.limpiarFormulario();
              this.listarPacientes();
            },

            error: (err) => {
              this.error =
                err.error?.message ||
                'El paciente fue registrado, pero ocurrió un error al crear el usuario';

              this.listarPacientes();
            }
          });

        } else {

          this.mensaje = 'Paciente guardado correctamente';
          this.limpiarFormulario();
          this.listarPacientes();
        }
      },

      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al guardar paciente';
      }
    });
  }

  editarPaciente(paciente: any): void {

    this.editandoId = paciente.id;

    this.crearUsuarioPaciente = false;
    this.usuarioPaciente = this.crearUsuarioPacienteVacio();
    this.mostrarPassword = false;

    this.paciente = {
      nombre: paciente.nombre,
      apellido: paciente.apellido,
      telefono: paciente.telefono,
      email: paciente.email || '',
      fechaNacimiento: paciente.fechaNacimiento || paciente.fecha_nacimiento,
      direccion: paciente.direccion,
      seguroMedico: paciente.seguroMedico || paciente.seguro_medico || ''
    };
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.limpiarFormulario();
  }

  limpiarFormulario(): void {
    this.paciente = this.crearPacienteVacio();
    this.usuarioPaciente = this.crearUsuarioPacienteVacio();
    this.crearUsuarioPaciente = false;
    this.mostrarPassword = false;
  }

  eliminarPaciente(id: number): void {

    if (!confirm('¿Deseas eliminar este paciente?')) {
      return;
    }

    this.pacienteService.eliminar(id).subscribe({
      next: () => {
        this.mensaje = 'Paciente eliminado correctamente';
        this.listarPacientes();
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al eliminar paciente';
      }
    });
  }

  generarUsuarioSugerido(): void {

    if (!this.paciente.nombre || !this.paciente.apellido) {
      return;
    }

    this.usuarioPaciente.username =
      `${this.paciente.nombre}.${this.paciente.apellido}`
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/\s+/g, '');
  }

  cambiarCrearUsuario(): void {
    this.error = '';
    this.mostrarPassword = false;

    if (this.crearUsuarioPaciente) {

      this.usuarioPaciente = this.crearUsuarioPacienteVacio();

      if (!this.paciente.email) {
        this.error = 'Debe ingresar el email del paciente antes de crearle usuario';
      }

      this.generarUsuarioSugerido();

    } else {
      this.usuarioPaciente = this.crearUsuarioPacienteVacio();
    }
  }

  alternarMostrarPassword(): void {
    this.mostrarPassword = !this.mostrarPassword;
  }

  emailValido(email: string): boolean {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}