import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HistorialMedicoService } from '../core/historial-medico.service';
import { AuthService } from '../core/auth.service';
import { PacienteService } from '../core/paciente.service';
import { CitaService } from '../core/cita.service';

@Component({
  selector: 'app-historial-medico',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './historial-medico.html',
  styleUrl: './historial-medico.css'
})
export class HistorialMedico implements OnInit {

  pacientes: any[] = [];

  pacienteIdSeleccionado: number | null = null;

  registros: any[] = [];

  paginaActual = 1;
  limite = 10;
  totalPaginas = 0;
  totalRegistros = 0;
  sort = 'desc';

  mensaje = '';
  error = '';

  rol = '';

  cargando = false;

  constructor(
    private historialService: HistorialMedicoService,
    private authService: AuthService,
    private pacienteService: PacienteService,
    private citaService: CitaService
  ) {}

  ngOnInit(): void {

    this.rol = (this.authService.getRol() || '').toUpperCase();

    if (this.esPaciente()) {
      const pacienteId = this.authService.getPacienteId();

      if (!pacienteId) {
        this.error = 'No hay paciente asociado a este usuario';
        return;
      }

      this.pacienteIdSeleccionado = Number(pacienteId);
      this.consultarHistorial();
      return;
    }

    if (this.esAdmin() || this.esRecepcionista()) {
      this.cargarPacientes();
      return;
    }

    if (this.esMedico()) {
      this.cargarPacientesDelMedico();
      return;
    }
  }

  cargarPacientes(): void {

    this.error = '';
    this.mensaje = '';
    this.pacientes = [];

    this.pacienteService.listar().subscribe({
      next: (data: any[]) => {
        this.pacientes = data;
      },
      error: (err: any) => {
        this.mostrarError(err, 'Error al cargar pacientes');
      }
    });
  }

  cargarPacientesDelMedico(): void {

    this.error = '';
    this.mensaje = '';
    this.pacientes = [];

    const medicoId = this.authService.getMedicoId();

    if (!medicoId) {
      this.error = 'No hay médico asociado a este usuario';
      return;
    }

    this.citaService.listarPorMedico(medicoId).subscribe({
      next: (citas: any[]) => {

        const idsPacientes = [
          ...new Set(
            citas
              .map((c: any) => Number(c.pacienteId))
              .filter((id: number) => !!id)
          )
        ];

        if (idsPacientes.length === 0) {
          this.mensaje = 'No tiene pacientes con citas registradas.';
          return;
        }

        idsPacientes.forEach((id: number) => {

          const yaExiste = this.pacientes.some(
            p => Number(p.id) === Number(id)
          );

          if (yaExiste) {
            return;
          }

          this.pacienteService.buscarPorId(id).subscribe({
            next: (paciente: any) => {
              this.pacientes.push(paciente);
            },
            error: () => {
              console.log('No se pudo cargar paciente con ID:', id);
            }
          });

        });
      },
      error: (err: any) => {
        this.mostrarError(err, 'No fue posible cargar los pacientes del médico');
      }
    });
  }

  consultarHistorial(): void {

    this.mensaje = '';
    this.error = '';
    this.registros = [];

    if (!this.pacienteIdSeleccionado) {
      this.error = 'Debe seleccionar un paciente';
      return;
    }

    this.cargando = true;

    this.historialService
      .consultarPorPaciente(
        this.pacienteIdSeleccionado,
        this.paginaActual,
        this.limite,
        this.sort
      )
      .subscribe({
        next: (res: any) => {

          this.cargando = false;

          this.registros = res.data?.registros || [];

          this.totalRegistros =
            res.data?.paginacion?.totalRegistros || 0;

          this.paginaActual =
            res.data?.paginacion?.paginaActual || 1;

          this.totalPaginas =
            res.data?.paginacion?.totalPaginas || 0;

          if (this.registros.length === 0) {
            this.mensaje = 'No hay registros clínicos para este paciente.';
          }
        },
        error: (err: any) => {
          this.cargando = false;
          this.mostrarError(err, 'No fue posible cargar el historial médico');
        }
      });
  }

  cambiarPaciente(): void {
    this.paginaActual = 1;
    this.consultarHistorial();
  }

  paginaAnterior(): void {

    if (this.paginaActual <= 1) {
      return;
    }

    this.paginaActual--;
    this.consultarHistorial();
  }

  paginaSiguiente(): void {

    if (this.paginaActual >= this.totalPaginas) {
      return;
    }

    this.paginaActual++;
    this.consultarHistorial();
  }

  cambiarLimite(): void {
    this.paginaActual = 1;

    if (this.pacienteIdSeleccionado) {
      this.consultarHistorial();
    }
  }

  cambiarOrden(): void {
    this.paginaActual = 1;

    if (this.pacienteIdSeleccionado) {
      this.consultarHistorial();
    }
  }

  obtenerPacienteNombre(id: number): string {
    const paciente = this.pacientes.find(
      p => Number(p.id) === Number(id)
    );

    return paciente
      ? `${paciente.nombre} ${paciente.apellido}`
      : String(id);
  }

  mostrarError(err: any, mensajeDefault: string): void {

    console.error(mensajeDefault, err);

    if (err.status === 0) {
      this.error = 'No fue posible comunicarse con el microservicio correspondiente.';
      return;
    }

    if (err.status === 401) {
      this.error = 'Su sesión ha expirado. Por favor, inicie sesión nuevamente.';
      return;
    }

    if (err.status === 403) {
      this.error =
        err.error?.message ||
        'No tiene permisos para consultar este historial médico.';
      return;
    }

    if (err.status === 400) {
      this.error =
        err.error?.message ||
        'Parámetros de paginación inválidos.';
      return;
    }

    if (err.status === 404) {
      this.error =
        err.error?.message ||
        'Paciente no encontrado.';
      return;
    }

    this.error =
      err.error?.message ||
      err.error ||
      mensajeDefault;
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
}