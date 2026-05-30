import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EspecialidadService } from '../core/especialidad.service';

@Component({
  selector: 'app-especialidades',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './especialidades.html',
  styleUrl: './especialidades.css'
})
export class Especialidades implements OnInit {
  especialidades: any[] = [];
  especialidad = { nombre: '' };
  mensaje = '';
  error = '';

  constructor(private especialidadService: EspecialidadService) {}

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    this.especialidadService.listar().subscribe({
      next: (data) => this.especialidades = data,
      error: (err) => this.error = err.error?.message || 'Error al listar especialidades'
    });
  }

  guardar(): void {
    this.mensaje = '';
    this.error = '';

    this.especialidadService.guardar(this.especialidad).subscribe({
      next: () => {
        this.mensaje = 'Especialidad registrada correctamente';
        this.especialidad = { nombre: '' };
        this.listar();
      },
      error: (err) => this.error = err.error?.message || 'Error al guardar especialidad'
    });
  }
}
