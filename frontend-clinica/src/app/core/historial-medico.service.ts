import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { API_URLS } from './api-config';

@Injectable({
  providedIn: 'root'
})
export class HistorialMedicoService {

  constructor(
    private http: HttpClient
  ) {}

  consultarPorPaciente(
    pacienteId: number,
    page: number = 1,
    limit: number = 10,
    sort: string = 'desc'
  ): Observable<any> {

    return this.http.get<any>(
      `${API_URLS.historialMedico}/pacientes/${pacienteId}/registros?page=${page}&limit=${limit}&sort=${sort}`
    );
  }

  registrarAtencion(registro: any): Observable<any> {
    return this.http.post<any>(
      `${API_URLS.historialMedico}/registros`,
      registro
    );
  }
}