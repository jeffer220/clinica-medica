import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../core/auth.service';
import { MedicoService } from '../core/medico.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios implements OnInit {

  usuario = {
    username: '',
    email: '',
    password: '',
    rol: 'MEDICO',
    medicoId: null as number | null
  };

  roles = [
    'ADMIN',
    'RECEPCIONISTA',
    'MEDICO'
  ];

  medicos: any[] = [];
  medicosDisponibles: any[] = [];
  usuarios: any[] = [];

  mensaje = '';
  error = '';

  constructor(
    private authService: AuthService,
    private medicoService: MedicoService
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {

    this.cargarMedicos();
    this.cargarUsuarios();
  }

  cargarMedicos(): void {

    this.medicoService.listarActivos().subscribe({
      next: (data: any[]) => {
        this.medicos = data;
        this.filtrarMedicosDisponibles();
      },
      error: (err: any) => {
        this.error =
          err.error?.message ||
          'Error al cargar médicos';
      }
    });
  }

  cargarUsuarios(): void {

    this.authService.listarUsuarios().subscribe({
      next: (data: any[]) => {
        this.usuarios = data;
        this.filtrarMedicosDisponibles();
      },
      error: (err: any) => {
        this.error =
          err.error?.message ||
          'Error al cargar usuarios';
      }
    });
  }

  filtrarMedicosDisponibles(): void {

    if (!this.medicos || !this.usuarios) {
      return;
    }

    const medicosConUsuario = this.usuarios
      .filter((u: any) =>
        String(u.rol).toUpperCase() === 'MEDICO' &&
        u.medicoId != null
      )
      .map((u: any) => Number(u.medicoId));

    this.medicosDisponibles = this.medicos.filter((medico: any) =>
      !medicosConUsuario.includes(Number(medico.id))
    );
  }

  cambiarRol(): void {

    this.mensaje = '';
    this.error = '';

    this.usuario.medicoId = null;
    this.usuario.username = '';
    this.usuario.email = '';
    this.usuario.password = '';
  }

  seleccionarMedico(): void {

    const medicoSeleccionado = this.medicosDisponibles.find(
      (m: any) => Number(m.id) === Number(this.usuario.medicoId)
    );

    if (!medicoSeleccionado) {
      this.usuario.username = '';
      this.usuario.email = '';
      return;
    }

    if (!medicoSeleccionado.correo) {
      this.error = 'El médico seleccionado no tiene correo registrado';
      this.usuario.email = '';
      this.usuario.username = '';
      return;
    }

    this.error = '';

    this.usuario.email = medicoSeleccionado.correo;

    this.usuario.username =
      `${medicoSeleccionado.nombre}.${medicoSeleccionado.apellido}`
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/\s+/g, '');
  }

  registrar(): void {

    this.mensaje = '';
    this.error = '';

    if (!this.usuario.username || !this.usuario.email || !this.usuario.password) {
      this.error = 'Debe completar usuario, email y contraseña';
      return;
    }

    const payload: any = {
      username: this.usuario.username,
      email: this.usuario.email,
      password: this.usuario.password,
      rol: this.usuario.rol
    };

    if (this.usuario.rol === 'MEDICO') {

      if (!this.usuario.medicoId) {
        this.error = 'Debe seleccionar un médico';
        return;
      }

      payload.medicoId = this.usuario.medicoId;
    }

    this.authService.registrar(payload).subscribe({
      next: () => {

        this.mensaje = 'Usuario registrado correctamente';

        this.usuario = {
          username: '',
          email: '',
          password: '',
          rol: 'MEDICO',
          medicoId: null
        };

        this.cargarDatos();
      },
      error: (err: any) => {
        this.error =
          err.error?.message ||
          'Error al registrar usuario';
      }
    });
  }
}