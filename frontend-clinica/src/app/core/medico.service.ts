import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URLS } from './api-config';

@Injectable({
  providedIn: 'root'
})
export class MedicoService {

  constructor(
    private http: HttpClient
  ) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(
      API_URLS.medicos
    );
  }

  listarActivos(): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.medicos}/activos`
    );
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(
      `${API_URLS.medicos}/${id}`
    );
  }

  listarPorEspecialidad(especialidadId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.medicos}/especialidad/${especialidadId}`
    );
  }

  guardar(medico: any): Observable<any> {
    return this.http.post<any>(
      API_URLS.medicos,
      medico
    );
  }

  actualizar(id: number, medico: any): Observable<any> {
    return this.http.put<any>(
      `${API_URLS.medicos}/${id}`,
      medico
    );
  }

  activar(id: number): Observable<any> {
    return this.http.put<any>(
      `${API_URLS.medicos}/${id}/activar`,
      {}
    );
  }

  desactivar(id: number): Observable<any> {
    return this.http.put<any>(
      `${API_URLS.medicos}/${id}/desactivar`,
      {}
    );
  }

  eliminar(id: number): Observable<string> {
    return this.http.delete(
      `${API_URLS.medicos}/${id}`,
      {
        responseType: 'text'
      }
    );
  }
}