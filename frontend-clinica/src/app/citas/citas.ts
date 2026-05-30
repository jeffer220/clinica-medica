import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CitaService } from '../core/cita.service';
import { PacienteService } from '../core/paciente.service';
import { MedicoService } from '../core/medico.service';
import { EspecialidadService } from '../core/especialidad.service';
import { AuthService } from '../core/auth.service';
import { HorarioService } from '../core/horario.service';
import { HistorialMedicoService } from '../core/historial-medico.service';

@Component({
  selector: 'app-citas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './citas.html',
  styleUrl: './citas.css'
})
export class Citas implements OnInit {

  citas: any[] = [];
  citasFuturas: any[] = [];
  citasPasadas: any[] = [];

  pacientes: any[] = [];
  medicos: any[] = [];
  medicosCatalogo: any[] = [];
  medicosSugeridos: any[] = [];

  especialidades: any[] = [];

  horariosMedico: any[] = [];
  diasPermitidos: string[] = [];
  mensajeHorario = '';

  fechasDisponibles: string[] = [];
  horasDisponibles: string[] = [];
  citasDelMedico: any[] = [];

  citaReprogramando: any = null;
  horariosReprogramacion: any[] = [];
  diasPermitidosReprogramacion: string[] = [];
  fechasDisponiblesReprogramacion: string[] = [];
  horasDisponiblesReprogramacion: string[] = [];
  citasDelMedicoReprogramacion: any[] = [];
  mensajeHorarioReprogramacion = '';

  atendiendoCita: any = null;

  registroClinico = {
    diagnosticoPrincipal: '',
    recetaMedica: '',
    observaciones: '',
    resultadoLaboratorio: ''
  };

  mensaje = '';
  error = '';

  rol: string = '';

  reprogramandoId: number | null = null;

  mostrarResumenCita = false;
  resumenCita: any = null;
  comprobanteCita: any = null;
  payloadCitaPendiente: any = null;

  nuevaCita = this.crearCitaVacia();

  reprogramacion = {
    fecha: '',
    hora: '',
    observaciones: ''
  };

  constructor(
    private citaService: CitaService,
    private pacienteService: PacienteService,
    private medicoService: MedicoService,
    private especialidadService: EspecialidadService,
    private authService: AuthService,
    private horarioService: HorarioService,
    private historialMedicoService: HistorialMedicoService
  ) {}

  ngOnInit(): void {
    this.rol = (this.authService.getRol() || '').toUpperCase();
    this.cargarDatos();
    this.listarCitas();
  }

  generarCodigoCita(): string {
    const ahora = new Date();

    const year = ahora.getFullYear();
    const month = String(ahora.getMonth() + 1).padStart(2, '0');
    const day = String(ahora.getDate()).padStart(2, '0');

    const hours = String(ahora.getHours()).padStart(2, '0');
    const minutes = String(ahora.getMinutes()).padStart(2, '0');
    const seconds = String(ahora.getSeconds()).padStart(2, '0');

    return `CITA-${year}${month}${day}-${hours}${minutes}${seconds}`;
  }

  crearCitaVacia(): any {
    return {
      codigoCita: this.generarCodigoCita(),
      pacienteId: null,
      medicoId: null,
      especialidadId: null,
      fecha: '',
      hora: '',
      observaciones: ''
    };
  }

  cargarDatos(): void {

    if (this.esAdmin() || this.esRecepcionista()) {
      this.pacienteService.listar().subscribe({
        next: (data) => {
          this.pacientes = data;
        },
        error: (err) => {
          this.error =
            err.error?.message ||
            'Error al cargar pacientes';
        }
      });
    }

    this.especialidadService.listar().subscribe({
      next: (data) => {
        this.especialidades = data;
      },
      error: (err) => {
        this.error =
          err.error?.message ||
          'Error al cargar especialidades';
      }
    });

    this.medicoService.listarActivos().subscribe({
      next: (data) => {
        this.medicosCatalogo = data;
      },
      error: () => {
        this.medicosCatalogo = [];
      }
    });
  }

  listarCitas(): void {

    this.error = '';

    const rol = (this.authService.getRol() || '').toUpperCase();

    if (rol === 'ADMIN' || rol === 'RECEPCIONISTA') {

      this.citaService.listar().subscribe({
        next: (data) => {
          this.citas = data;

          this.cargarPacientesDeCitas(data);
          this.cargarMedicosDeCitas(data);
          this.cargarEspecialidadesDeCitas(data);
        },
        error: (err) => {
          this.mostrarError(err, 'Error al listar citas');
        }
      });

    } else if (rol === 'PACIENTE') {

      this.citaService.misCitas().subscribe({
        next: (data) => {
          this.citas = data;

          this.cargarMedicosDeCitas(data);
          this.cargarEspecialidadesDeCitas(data);

          this.clasificarCitasPaciente();
        },
        error: (err) => {
          this.mostrarError(err, 'Error al listar mis citas');
        }
      });

    } else if (rol === 'MEDICO') {

      const medicoId = this.authService.getMedicoId();

      if (!medicoId) {
        this.error = 'No hay médico asociado a este usuario';
        return;
      }

      this.citaService.listarPorMedico(medicoId).subscribe({
        next: (data) => {
          this.citas = data;

          this.cargarPacientesDeCitas(data);
          this.cargarMedicosDeCitas(data);
          this.cargarEspecialidadesDeCitas(data);
        },
        error: (err) => {
          this.mostrarError(err, 'Error al listar citas del médico');
        }
      });

    } else {
      this.error = 'Rol no reconocido';
    }
  }

  cargarPacientesDeCitas(citas: any[]): void {

    const idsPacientes = [
      ...new Set(
        citas
          .map(c => Number(c.pacienteId))
          .filter(id => !!id)
      )
    ];

    idsPacientes.forEach(id => {

      const yaExiste = this.pacientes.some(
        p => Number(p.id) === Number(id)
      );

      if (yaExiste) {
        return;
      }

      this.pacienteService.buscarPorId(id).subscribe({
        next: (paciente) => {
          this.pacientes.push(paciente);
        },
        error: () => {
          console.log('No se pudo cargar paciente con ID:', id);
        }
      });

    });
  }

  cargarMedicosDeCitas(citas: any[]): void {

    const idsMedicos = [
      ...new Set(
        citas
          .map(c => Number(c.medicoId))
          .filter(id => !!id)
      )
    ];

    idsMedicos.forEach(id => {

      const yaExiste = this.medicosCatalogo.some(
        m => Number(m.id) === Number(id)
      );

      if (yaExiste) {
        return;
      }

      this.medicoService.buscarPorId(id).subscribe({
        next: (medico) => {
          this.medicosCatalogo.push(medico);
        },
        error: () => {
          console.log('No se pudo cargar médico con ID:', id);
        }
      });

    });
  }

  cargarEspecialidadesDeCitas(citas: any[]): void {

    const idsEspecialidades = [
      ...new Set(
        citas
          .map(c => Number(c.especialidadId))
          .filter(id => !!id)
      )
    ];

    idsEspecialidades.forEach(id => {

      const yaExiste = this.especialidades.some(
        e => Number(e.id) === Number(id)
      );

      if (yaExiste) {
        return;
      }

      this.especialidadService.buscarPorId(id).subscribe({
        next: (especialidad) => {
          this.especialidades.push(especialidad);
        },
        error: () => {
          console.log('No se pudo cargar especialidad con ID:', id);
        }
      });

    });
  }

  clasificarCitasPaciente(): void {

    const ahora = new Date();

    this.citasFuturas = [];
    this.citasPasadas = [];

    this.citas.forEach(cita => {

      const fechaHoraCita = new Date(`${cita.fecha}T${cita.hora}`);

      if (
        fechaHoraCita >= ahora &&
        cita.estado !== 'ATENDIDA' &&
        cita.estado !== 'CANCELADA'
      ) {
        this.citasFuturas.push(cita);
      } else {
        this.citasPasadas.push(cita);
      }
    });
  }

  pacienteSinCitas(): boolean {
    return this.esPaciente() &&
      this.citasFuturas.length === 0 &&
      this.citasPasadas.length === 0;
  }

  cargarMedicosPorEspecialidad(): void {

    this.error = '';
    this.mensajeHorario = '';
    this.medicosSugeridos = [];

    this.medicos = [];
    this.horariosMedico = [];
    this.diasPermitidos = [];
    this.fechasDisponibles = [];
    this.horasDisponibles = [];
    this.citasDelMedico = [];

    this.nuevaCita.medicoId = null;
    this.nuevaCita.fecha = '';
    this.nuevaCita.hora = '';

    if (!this.nuevaCita.especialidadId) {
      return;
    }

    this.medicoService
      .listarPorEspecialidad(Number(this.nuevaCita.especialidadId))
      .subscribe({
        next: (data) => {
          this.medicos = data;

          this.medicos.forEach(medico => {
            const existe = this.medicosCatalogo.some(
              x => Number(x.id) === Number(medico.id)
            );

            if (!existe) {
              this.medicosCatalogo.push(medico);
            }
          });

          if (this.medicos.length === 0) {
            this.error = 'No hay médicos registrados para esta especialidad';
          }
        },
        error: (err) => {
          this.mostrarError(err, 'Error al cargar médicos por especialidad');
        }
      });
  }

  cargarHorariosDelMedico(): void {

    this.mensajeHorario = '';
    this.medicosSugeridos = [];
    this.horariosMedico = [];
    this.diasPermitidos = [];
    this.fechasDisponibles = [];
    this.horasDisponibles = [];
    this.citasDelMedico = [];

    this.nuevaCita.fecha = '';
    this.nuevaCita.hora = '';

    if (!this.nuevaCita.medicoId) {
      return;
    }

    this.horarioService
      .listarDisponiblesPorMedico(Number(this.nuevaCita.medicoId))
      .subscribe({
        next: (horarios) => {

          this.horariosMedico = horarios;

          this.diasPermitidos = [
            ...new Set(
              horarios.map(h => String(h.diaSemana).toUpperCase())
            )
          ];

          if (this.diasPermitidos.length === 0) {
            this.mensajeHorario =
              'Este médico no tiene horarios disponibles registrados.';
            this.sugerirMedicosDisponibles();
            return;
          }

          this.mensajeHorario =
            'Días disponibles: ' + this.diasPermitidos.join(', ');

          this.generarFechasDisponibles();

          this.citaService
            .listarPorMedico(String(this.nuevaCita.medicoId))
            .subscribe({
              next: (citas) => {
                this.citasDelMedico = citas;
              },
              error: () => {
                this.citasDelMedico = [];
              }
            });
        },
        error: (err) => {
          this.mostrarError(err, 'Error al cargar horarios del médico');
        }
      });
  }

  generarFechasDisponibles(): void {

    this.fechasDisponibles = [];

    const hoy = new Date();

    for (let i = 0; i <= 60; i++) {

      const fecha = new Date();
      fecha.setDate(hoy.getDate() + i);

      const fechaTexto = this.formatearFecha(fecha);
      const diaSemana = this.obtenerDiaSemana(fechaTexto);

      if (this.diasPermitidos.includes(diaSemana)) {
        this.fechasDisponibles.push(fechaTexto);
      }
    }
  }

  cargarHorasDisponibles(): void {

    this.error = '';
    this.horasDisponibles = [];
    this.nuevaCita.hora = '';

    if (!this.nuevaCita.fecha || !this.nuevaCita.medicoId) {
      return;
    }

    const diaSemana = this.obtenerDiaSemana(this.nuevaCita.fecha);

    const horariosDelDia = this.horariosMedico.filter(
      h => String(h.diaSemana).toUpperCase() === diaSemana
    );

    horariosDelDia.forEach(horario => {

      const horasGeneradas = this.generarHorasEntre(
        horario.horaInicio,
        horario.horaFin
      );

      this.horasDisponibles.push(...horasGeneradas);
    });

    this.horasDisponibles = this.horasDisponibles.filter(hora => {
      return !this.citasDelMedico.some(cita =>
        cita.fecha === this.nuevaCita.fecha &&
        String(cita.hora).substring(0, 5) === hora &&
        cita.estado !== 'CANCELADA'
      );
    });

    this.horasDisponibles = [...new Set(this.horasDisponibles)];

    if (this.horasDisponibles.length === 0) {
      this.error = 'No hay horas disponibles para esa fecha';
      this.sugerirMedicosDisponibles();
    }
  }

  generarHorasEntre(horaInicio: string, horaFin: string): string[] {

    const horas: string[] = [];

    const [inicioHora, inicioMinuto] =
      horaInicio.substring(0, 5).split(':').map(Number);

    const [finHora, finMinuto] =
      horaFin.substring(0, 5).split(':').map(Number);

    const inicio = new Date();
    inicio.setHours(inicioHora, inicioMinuto, 0, 0);

    const fin = new Date();
    fin.setHours(finHora, finMinuto, 0, 0);

    while (inicio < fin) {

      const hora = String(inicio.getHours()).padStart(2, '0');
      const minuto = String(inicio.getMinutes()).padStart(2, '0');

      horas.push(`${hora}:${minuto}`);

      inicio.setMinutes(inicio.getMinutes() + 30);
    }

    return horas;
  }

  formatearFecha(fecha: Date): string {

    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  obtenerDiaSemana(fecha: string): string {

    const fechaObj = new Date(`${fecha}T00:00:00`);
    const dia = fechaObj.getDay();

    const dias: any = {
      0: 'DOMINGO',
      1: 'LUNES',
      2: 'MARTES',
      3: 'MIERCOLES',
      4: 'JUEVES',
      5: 'VIERNES',
      6: 'SABADO'
    };

    return dias[dia];
  }

  guardarCita(): void {

    this.mensaje = '';
    this.error = '';
    this.comprobanteCita = null;
    this.medicosSugeridos = [];

    const rol = (this.authService.getRol() || '').toUpperCase();

    if (!this.nuevaCita.codigoCita) {
      this.nuevaCita.codigoCita = this.generarCodigoCita();
    }

    if (!this.nuevaCita.especialidadId) {
      this.error = 'Debe seleccionar una especialidad';
      return;
    }

    if (!this.nuevaCita.medicoId) {
      this.error = 'Debe seleccionar un médico';
      return;
    }

    if (!this.nuevaCita.fecha) {
      this.error = 'Debe seleccionar una fecha disponible';
      return;
    }

    if (!this.nuevaCita.hora) {
      this.error = 'Debe seleccionar una hora disponible';
      return;
    }

    let pacienteId = this.nuevaCita.pacienteId;

    if (rol === 'PACIENTE') {
      pacienteId = Number(this.authService.getPacienteId());
    }

    if (!pacienteId) {
      this.error = 'Debe seleccionar un paciente';
      return;
    }

    const payload = {
      codigoCita: this.nuevaCita.codigoCita,
      pacienteId: Number(pacienteId),
      medicoId: Number(this.nuevaCita.medicoId),
      especialidadId: Number(this.nuevaCita.especialidadId),
      fecha: this.nuevaCita.fecha,
      hora: this.normalizarHora(this.nuevaCita.hora),
      observaciones: this.nuevaCita.observaciones
    };

    this.payloadCitaPendiente = payload;

    this.resumenCita = {
      codigoCita: payload.codigoCita,
      paciente: this.esPaciente()
        ? 'Paciente actual'
        : this.obtenerPacienteNombre(payload.pacienteId),
      medico: this.obtenerMedicoNombre(payload.medicoId),
      especialidad: this.obtenerEspecialidadNombre(payload.especialidadId),
      fecha: payload.fecha,
      hora: payload.hora,
      observaciones: payload.observaciones || 'Sin observaciones'
    };

    this.mostrarResumenCita = true;
  }

  confirmarCita(): void {

    if (!this.payloadCitaPendiente) {
      this.error = 'No hay una cita pendiente para confirmar';
      return;
    }

    this.citaService.guardar(this.payloadCitaPendiente).subscribe({
      next: (data) => {
        this.mensaje = 'Cita registrada correctamente';

        this.comprobanteCita = data;

        this.nuevaCita = this.crearCitaVacia();
        this.payloadCitaPendiente = null;
        this.resumenCita = null;
        this.mostrarResumenCita = false;

        this.mensajeHorario = '';
        this.horariosMedico = [];
        this.diasPermitidos = [];
        this.fechasDisponibles = [];
        this.horasDisponibles = [];
        this.citasDelMedico = [];
        this.medicos = [];
        this.medicosSugeridos = [];

        this.listarCitas();
      },
      error: (err) => {
        this.mostrarError(err, 'Error al registrar cita');
        this.sugerirMedicosDisponibles();
      }
    });
  }

  cancelarResumenCita(): void {
    this.mostrarResumenCita = false;
    this.resumenCita = null;
    this.payloadCitaPendiente = null;
  }

  sugerirMedicosDisponibles(): void {

    this.medicosSugeridos = [];

    if (!this.nuevaCita.especialidadId || !this.nuevaCita.medicoId) {
      return;
    }

    this.medicosSugeridos = this.medicos.filter(m =>
      Number(m.id) !== Number(this.nuevaCita.medicoId)
    );
  }

  cancelar(id: number): void {

    if (!confirm('¿Deseas cancelar esta cita?')) {
      return;
    }

    this.citaService.cancelar(id).subscribe({
      next: () => {
        this.mensaje = 'Cita cancelada correctamente';
        this.listarCitas();
      },
      error: (err) => {
        this.mostrarError(err, 'Error al cancelar cita');
      }
    });
  }

  prepararAtencion(cita: any): void {

    this.mensaje = '';
    this.error = '';

    this.atendiendoCita = cita;

    this.registroClinico = {
      diagnosticoPrincipal: '',
      recetaMedica: '',
      observaciones: '',
      resultadoLaboratorio: ''
    };

    this.cargarPacientesDeCitas([cita]);
    this.cargarMedicosDeCitas([cita]);
    this.cargarEspecialidadesDeCitas([cita]);
  }

  cancelarAtencion(): void {

    this.atendiendoCita = null;

    this.registroClinico = {
      diagnosticoPrincipal: '',
      recetaMedica: '',
      observaciones: '',
      resultadoLaboratorio: ''
    };
  }

  guardarAtencion(): void {

    this.mensaje = '';
    this.error = '';

    
  if (!this.atendiendoCita) {
    return;
  }

    if (!this.registroClinico.diagnosticoPrincipal.trim()) {
      this.error = 'Debe ingresar el diagnóstico principal';
      return;
    }

    const payload = {
      pacienteId: this.atendiendoCita.pacienteId,
      medicoId: this.atendiendoCita.medicoId,
      especialidadId: this.atendiendoCita.especialidadId,
      citaId: this.atendiendoCita.id,
      fechaConsulta: `${this.atendiendoCita.fecha}T${String(this.atendiendoCita.hora).substring(0, 8)}`,
      diagnosticoPrincipal: this.registroClinico.diagnosticoPrincipal,
      recetaMedica: this.registroClinico.recetaMedica,
      observaciones: this.registroClinico.observaciones,
      resultadoLaboratorio: this.registroClinico.resultadoLaboratorio
    };

    this.historialMedicoService.registrarAtencion(payload).subscribe({
      next: () => {

        this.citaService.atender(this.atendiendoCita.id).subscribe({
          next: () => {
            this.mensaje = 'Atención médica registrada y cita marcada como atendida';
            this.cancelarAtencion();
            this.listarCitas();
          },
          error: (err) => {
            this.mostrarError(
              err,
              'El registro clínico fue guardado, pero no se pudo marcar la cita como atendida'
            );
          }
        });

      },
      error: (err) => {
        this.mostrarError(err, 'Error al registrar la atención médica');
      }
    });
  }

  prepararReprogramacion(cita: any): void {

    this.reprogramandoId = cita.id;
    this.citaReprogramando = cita;

    this.reprogramacion = {
      fecha: '',
      hora: '',
      observaciones: cita.observaciones || ''
    };

    this.horariosReprogramacion = [];
    this.diasPermitidosReprogramacion = [];
    this.fechasDisponiblesReprogramacion = [];
    this.horasDisponiblesReprogramacion = [];
    this.citasDelMedicoReprogramacion = [];
    this.mensajeHorarioReprogramacion = '';

    this.cargarDisponibilidadParaReprogramar(cita);
  }

  cargarDisponibilidadParaReprogramar(cita: any): void {

    this.error = '';

    if (!cita || !cita.medicoId) {
      this.error = 'No se pudo identificar el médico de la cita';
      return;
    }

    this.horarioService
      .listarDisponiblesPorMedico(Number(cita.medicoId))
      .subscribe({
        next: (horarios) => {

          this.horariosReprogramacion = horarios;

          this.diasPermitidosReprogramacion = [
            ...new Set(
              horarios.map(h => String(h.diaSemana).toUpperCase())
            )
          ];

          if (this.diasPermitidosReprogramacion.length === 0) {
            this.mensajeHorarioReprogramacion =
              'Este médico no tiene horarios disponibles registrados.';
            return;
          }

          this.mensajeHorarioReprogramacion =
            'Días disponibles: ' + this.diasPermitidosReprogramacion.join(', ');

          this.generarFechasDisponiblesReprogramacion();

          this.citaService
            .listarPorMedico(String(cita.medicoId))
            .subscribe({
              next: (citas) => {
                this.citasDelMedicoReprogramacion = citas;
              },
              error: () => {
                this.citasDelMedicoReprogramacion = [];
              }
            });
        },
        error: (err) => {
          this.mostrarError(err, 'Error al cargar disponibilidad para reprogramar');
        }
      });
  }

  generarFechasDisponiblesReprogramacion(): void {

    this.fechasDisponiblesReprogramacion = [];

    const hoy = new Date();

    for (let i = 0; i <= 60; i++) {

      const fecha = new Date();
      fecha.setDate(hoy.getDate() + i);

      const fechaTexto = this.formatearFecha(fecha);
      const diaSemana = this.obtenerDiaSemana(fechaTexto);

      if (this.diasPermitidosReprogramacion.includes(diaSemana)) {
        this.fechasDisponiblesReprogramacion.push(fechaTexto);
      }
    }
  }

  cargarHorasDisponiblesReprogramacion(): void {

    this.error = '';
    this.horasDisponiblesReprogramacion = [];
    this.reprogramacion.hora = '';

    if (
      !this.reprogramacion.fecha ||
      !this.citaReprogramando ||
      !this.citaReprogramando.medicoId
    ) {
      return;
    }

    const diaSemana = this.obtenerDiaSemana(this.reprogramacion.fecha);

    const horariosDelDia = this.horariosReprogramacion.filter(
      h => String(h.diaSemana).toUpperCase() === diaSemana
    );

    horariosDelDia.forEach(horario => {

      const horasGeneradas = this.generarHorasEntre(
        horario.horaInicio,
        horario.horaFin
      );

      this.horasDisponiblesReprogramacion.push(...horasGeneradas);
    });

    this.horasDisponiblesReprogramacion =
      this.horasDisponiblesReprogramacion.filter(hora => {

        return !this.citasDelMedicoReprogramacion.some(cita => {

          const mismaCita = Number(cita.id) === Number(this.reprogramandoId);

          return !mismaCita &&
            cita.fecha === this.reprogramacion.fecha &&
            String(cita.hora).substring(0, 5) === hora &&
            cita.estado !== 'CANCELADA';
        });
      });

    this.horasDisponiblesReprogramacion = [
      ...new Set(this.horasDisponiblesReprogramacion)
    ];

    if (this.horasDisponiblesReprogramacion.length === 0) {
      this.error = 'No hay horas disponibles para esa fecha';
    }
  }

  reprogramar(): void {

    if (!this.reprogramandoId) {
      return;
    }

    if (!this.reprogramacion.fecha) {
      this.error = 'Debe seleccionar una nueva fecha disponible';
      return;
    }

    if (!this.reprogramacion.hora) {
      this.error = 'Debe seleccionar una nueva hora disponible';
      return;
    }

    const payload = {
      fecha: this.reprogramacion.fecha,
      hora: this.normalizarHora(this.reprogramacion.hora),
      observaciones: this.reprogramacion.observaciones
    };

    this.citaService.reprogramar(this.reprogramandoId, payload).subscribe({
      next: () => {
        this.mensaje = 'Cita reprogramada correctamente';
        this.cancelarReprogramacion();
        this.listarCitas();
      },
      error: (err) => {
        this.mostrarError(err, 'Error al reprogramar cita');
      }
    });
  }

  cancelarReprogramacion(): void {

    this.reprogramandoId = null;
    this.citaReprogramando = null;

    this.reprogramacion = {
      fecha: '',
      hora: '',
      observaciones: ''
    };

    this.horariosReprogramacion = [];
    this.diasPermitidosReprogramacion = [];
    this.fechasDisponiblesReprogramacion = [];
    this.horasDisponiblesReprogramacion = [];
    this.citasDelMedicoReprogramacion = [];
    this.mensajeHorarioReprogramacion = '';
  }

  obtenerPacienteNombre(id: number): string {

    const p = this.pacientes.find(x => Number(x.id) === Number(id));

    return p ? `${p.nombre} ${p.apellido}` : String(id);
  }

  obtenerMedicoNombre(id: number): string {

    const listaMedicos = [
      ...this.medicos,
      ...this.medicosCatalogo
    ];

    const m = listaMedicos.find(x => Number(x.id) === Number(id));

    return m ? `${m.nombre} ${m.apellido}` : String(id);
  }

  obtenerEspecialidadNombre(id: number): string {

    const e = this.especialidades.find(x => Number(x.id) === Number(id));

    return e ? e.nombre : String(id);
  }

  normalizarHora(hora: string): string {

    return hora && hora.length === 5
      ? `${hora}:00`
      : hora;
  }

  mostrarError(err: any, mensajeDefault: string): void {

    console.error(mensajeDefault, err);

    if (err.status === 0) {
      this.error = 'No se pudo conectar con el backend. Verifica que los microservicios estén encendidos y que CORS esté configurado.';
      return;
    }

    if (typeof err.error === 'string') {
      this.error = err.error;
      return;
    }

    if (err.error?.message) {
      this.error = err.error.message;
      return;
    }

    this.error = mensajeDefault;
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