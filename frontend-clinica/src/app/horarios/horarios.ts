import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HorarioService } from '../core/horario.service';
import { MedicoService } from '../core/medico.service';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-horarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './horarios.html',
  styleUrl: './horarios.css'
})
export class Horarios implements OnInit {

  horarios: any[] = [];
  medicos: any[] = [];

  mensaje = '';
  error = '';

  rol = '';

  editandoId: number | null = null;

  dias = [
    'LUNES',
    'MARTES',
    'MIERCOLES',
    'JUEVES',
    'VIERNES',
    'SABADO',
    'DOMINGO'
  ];

  horario = this.crearHorarioVacio();

  constructor(
    private horarioService: HorarioService,
    private medicoService: MedicoService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.rol = (this.authService.getRol() || '').toUpperCase();

    if (this.esAdmin()) {
      this.cargarMedicos();
      this.listar();
    }

    if (this.esMedico()) {
      this.prepararHorarioMedico();
      this.listarMisHorarios();
    }

    if (!this.esAdmin() && !this.esMedico()) {
      this.error = 'Este rol no tiene permisos para gestionar horarios';
    }
  }

  crearHorarioVacio(): any {
    return {
      medico: {
        id: null as number | null
      },
      diaSemana: '',
      horaInicio: '',
      horaFin: '',
      disponible: true
    };
  }

  esAdmin(): boolean {
    return this.rol === 'ADMIN';
  }

  esMedico(): boolean {
    return this.rol === 'MEDICO';
  }

  prepararHorarioMedico(): void {
    const medicoId = this.authService.getMedicoId();

    if (!medicoId) {
      this.error = 'No hay médico asociado a este usuario';
      return;
    }

    this.horario.medico.id = Number(medicoId);
  }

  cargarMedicos(): void {
    this.medicoService.listarActivos().subscribe({
      next: (data) => {
        this.medicos = data;
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al cargar médicos';
      }
    });
  }

  listar(): void {
    this.horarioService.listar().subscribe({
      next: (data) => {
        this.horarios = data;
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al listar horarios';
      }
    });
  }

  listarMisHorarios(): void {
    const medicoId = this.authService.getMedicoId();

    if (!medicoId) {
      this.error = 'No hay médico asociado a este usuario';
      return;
    }

    this.horarioService.listarPorMedico(Number(medicoId)).subscribe({
      next: (data) => {
        this.horarios = data;
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al listar mis horarios';
      }
    });
  }

  recargarHorarios(): void {
    if (this.esMedico()) {
      this.listarMisHorarios();
    } else if (this.esAdmin()) {
      this.listar();
    }
  }

  existeCruceHorario(): boolean {
    const medicoIdNuevo = Number(this.horario.medico.id);
    const diaNuevo = this.horario.diaSemana;
    const inicioNuevo = this.horario.horaInicio;
    const finNuevo = this.horario.horaFin;

    return this.horarios.some(h => {

      if (this.editandoId && h.id === this.editandoId) {
        return false;
      }

      const medicoIdExistente = h.medico?.id || h.medicoId;
      const diaExistente = h.diaSemana;

      const inicioExistente = String(h.horaInicio).substring(0, 5);
      const finExistente = String(h.horaFin).substring(0, 5);

      const mismoMedico = Number(medicoIdExistente) === medicoIdNuevo;
      const mismoDia = diaExistente === diaNuevo;

      if (!mismoMedico || !mismoDia) {
        return false;
      }

      return inicioNuevo < finExistente && finNuevo > inicioExistente;
    });
  }

  guardar(): void {
    this.mensaje = '';
    this.error = '';

    if (this.esMedico()) {
      const medicoId = this.authService.getMedicoId();

      if (!medicoId) {
        this.error = 'No hay médico asociado a este usuario';
        return;
      }

      this.horario.medico.id = Number(medicoId);
    }

    if (!this.horario.medico.id) {
      this.error = 'Debe seleccionar un médico';
      return;
    }

    if (!this.horario.diaSemana) {
      this.error = 'Debe seleccionar un día';
      return;
    }

    if (!this.horario.horaInicio) {
      this.error = 'Debe seleccionar la hora de inicio';
      return;
    }

    if (!this.horario.horaFin) {
      this.error = 'Debe seleccionar la hora de fin';
      return;
    }

    if (this.horario.horaInicio >= this.horario.horaFin) {
      this.error = 'La hora de inicio debe ser menor que la hora de fin';
      return;
    }

    if (this.existeCruceHorario()) {
      this.error = 'Ya existe un horario registrado para este médico en ese día y rango de hora';
      return;
    }

    const payload = {
      medico: {
        id: Number(this.horario.medico.id)
      },
      diaSemana: this.horario.diaSemana,
      horaInicio: this.normalizarHora(this.horario.horaInicio),
      horaFin: this.normalizarHora(this.horario.horaFin),
      disponible: this.horario.disponible
    };

    if (this.editandoId) {
      this.horarioService.actualizar(this.editandoId, payload).subscribe({
        next: () => {
          this.mensaje = 'Horario actualizado correctamente';
          this.cancelarEdicion();
          this.recargarHorarios();
        },
        error: (err) => {
          this.error =
            err.error?.message ||
            'Error al actualizar horario';
        }
      });

      return;
    }

    this.horarioService.guardar(payload).subscribe({
      next: () => {
        this.mensaje = 'Horario registrado correctamente';

        this.horario = this.crearHorarioVacio();

        if (this.esMedico()) {
          this.prepararHorarioMedico();
        }

        this.recargarHorarios();
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al guardar horario';
      }
    });
  }

  editarHorario(h: any): void {
    this.mensaje = '';
    this.error = '';

    this.editandoId = h.id;

    this.horario = {
      medico: {
        id: h.medico?.id || h.medicoId || null
      },
      diaSemana: h.diaSemana,
      horaInicio: h.horaInicio ? String(h.horaInicio).substring(0, 5) : '',
      horaFin: h.horaFin ? String(h.horaFin).substring(0, 5) : '',
      disponible: h.disponible
    };

    if (this.esMedico()) {
      this.prepararHorarioMedico();
    }
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.horario = this.crearHorarioVacio();

    if (this.esMedico()) {
      this.prepararHorarioMedico();
    }
  }

  activar(id: number): void {
    this.horarioService.activar(id).subscribe({
      next: () => {
        this.mensaje = 'Horario activado';
        this.recargarHorarios();
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al activar horario';
      }
    });
  }

  desactivar(id: number): void {
    this.horarioService.desactivar(id).subscribe({
      next: () => {
        this.mensaje = 'Horario desactivado';
        this.recargarHorarios();
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al desactivar horario';
      }
    });
  }

  normalizarHora(hora: string): string {
    return hora && hora.length === 5
      ? `${hora}:00`
      : hora;
  }
}