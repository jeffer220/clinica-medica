import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MedicoService } from '../core/medico.service';
import { EspecialidadService } from '../core/especialidad.service';

@Component({
  selector: 'app-medicos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './medicos.html',
  styleUrl: './medicos.css'
})
export class Medicos implements OnInit {
  medicos: any[] = [];
  especialidades: any[] = [];
  editandoId: number | null = null;
  mensaje = '';
  error = '';

  medico = this.crearMedicoVacio();

  constructor(
    private medicoService: MedicoService,
    private especialidadService: EspecialidadService
  ) {}

  ngOnInit(): void {
    this.listar();
    this.cargarEspecialidades();
  }

  crearMedicoVacio(): any {
    return {
      colegiado: '',
      nombre: '',
      apellido: '',
      telefono: '',
      correo: '',
      especialidad: { id: null },
      direccion: '',
      centroHospitalario: '',
      edad: null,
      observacion: ''
    };
  }

  cargarEspecialidades(): void {
    this.especialidadService.listar().subscribe({
      next: (data) => this.especialidades = data,
      error: (err) => this.error = err.error?.message || 'Error al cargar especialidades'
    });
  }

  listar(): void {
    this.medicoService.listar().subscribe({
      next: (data) => this.medicos = data,
      error: (err) => this.error = err.error?.message || 'Error al listar médicos'
    });
  }

  guardar(): void {
    this.mensaje = '';
    this.error = '';

    const payload = {
      ...this.medico,
      edad: this.medico.edad ? Number(this.medico.edad) : null,
      especialidad: { id: Number(this.medico.especialidad.id) }
    };

    const operacion = this.editandoId
      ? this.medicoService.actualizar(this.editandoId, payload)
      : this.medicoService.guardar(payload);

    operacion.subscribe({
      next: () => {
        this.mensaje = this.editandoId ? 'Médico actualizado correctamente' : 'Médico registrado correctamente';
        this.cancelarEdicion();
        this.listar();
      },
      error: (err) => this.error = err.error?.message || 'Error al guardar médico'
    });
  }

  editar(m: any): void {
    this.editandoId = m.id;
    this.medico = {
      colegiado: m.colegiado,
      nombre: m.nombre,
      apellido: m.apellido,
      telefono: m.telefono,
      correo: m.correo,
      especialidad: { id: m.especialidad?.id },
      direccion: m.direccion,
      centroHospitalario: m.centroHospitalario,
      edad: m.edad,
      observacion: m.observacion
    };
  }

  cancelarEdicion(): void {
    this.editandoId = null;
    this.medico = this.crearMedicoVacio();
  }

  activar(id: number): void {
    this.medicoService.activar(id).subscribe({
      next: () => { this.mensaje = 'Médico activado'; this.listar(); },
      error: (err) => this.error = err.error?.message || 'Error al activar médico'
    });
  }

  desactivar(id: number): void {
    this.medicoService.desactivar(id).subscribe({
      next: () => { this.mensaje = 'Médico desactivado'; this.listar(); },
      error: (err) => this.error = err.error?.message || 'Error al desactivar médico'
    });
  }

  eliminar(id: number): void {
    if (!confirm('¿Deseas eliminar este médico?')) return;

    this.medicoService.eliminar(id).subscribe({
      next: () => { this.mensaje = 'Médico eliminado'; this.listar(); },
      error: (err) => this.error = err.error?.message || 'Error al eliminar médico'
    });
  }
}
