import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { API_URLS } from './api-config';

@Injectable({
  providedIn: 'root'
})
export class CitaService {

  constructor(
    private http: HttpClient
  ) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(
      API_URLS.citas
    );
  }

  misCitas(): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.citas}/mis-citas`
    );
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(
      `${API_URLS.citas}/${id}`
    );
  }

  listarPorPaciente(pacienteId: number | string): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.citas}/paciente/${pacienteId}`
    );
  }

  listarPorMedico(medicoId: number | string): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.citas}/medico/${medicoId}`
    );
  }

  guardar(cita: any): Observable<any> {
    return this.http.post<any>(
      API_URLS.citas,
      cita
    );
  }

  cancelar(id: number): Observable<any> {
    return this.http.put<any>(
      `${API_URLS.citas}/${id}/cancelar`,
      {}
    );
  }

  atender(id: number): Observable<any> {
    return this.http.put<any>(
      `${API_URLS.citas}/${id}/atender`,
      {}
    );
  }

  reprogramar(id: number, cita: any): Observable<any> {
    return this.http.put<any>(
      `${API_URLS.citas}/${id}/reprogramar`,
      cita
    );
  }
}