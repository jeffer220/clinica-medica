import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from './api-config';

@Injectable({ providedIn: 'root' })
export class PacienteService {
  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(API_URLS.pacientes);
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${API_URLS.pacientes}/${id}`);
  }

  guardar(paciente: any): Observable<any> {
    return this.http.post(API_URLS.pacientes, paciente);
  }

  actualizar(id: number, paciente: any): Observable<any> {
    return this.http.put(`${API_URLS.pacientes}/${id}`, paciente);
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete(`${API_URLS.pacientes}/${id}`, { responseType: 'text' });
  }
}
