import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { API_URLS } from './api-config';

@Injectable({
  providedIn: 'root'
})
export class HorarioService {

  constructor(
    private http: HttpClient
  ) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(
      API_URLS.horarios
    );
  }

  listarPorMedico(medicoId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.horarios}/medico/${medicoId}`
    );
  }

  listarDisponiblesPorMedico(medicoId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.horarios}/medico/${medicoId}/disponibles`
    );
  }

  guardar(horario: any): Observable<any> {
    return this.http.post(
      API_URLS.horarios,
      horario
    );
  }

  actualizar(id: number, horario: any): Observable<any> {
    return this.http.put(
      `${API_URLS.horarios}/${id}`,
      horario
    );
  }

  activar(id: number): Observable<any> {
    return this.http.put(
      `${API_URLS.horarios}/${id}/activar`,
      {}
    );
  }

  desactivar(id: number): Observable<any> {
    return this.http.put(
      `${API_URLS.horarios}/${id}/desactivar`,
      {}
    );
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete(
      `${API_URLS.horarios}/${id}`,
      { responseType: 'text' }
    );
  }

  validar(
    medicoId: number,
    diaSemana: string,
    hora: string
  ): Observable<boolean> {

    const params = new HttpParams()
      .set('medicoId', medicoId)
      .set('diaSemana', diaSemana)
      .set('hora', hora);

    return this.http.get<boolean>(
      `${API_URLS.horarios}/validar`,
      { params }
    );
  }
}