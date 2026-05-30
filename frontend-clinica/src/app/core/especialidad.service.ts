import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URLS } from './api-config';

@Injectable({
  providedIn: 'root'
})
export class EspecialidadService {

  constructor(
    private http: HttpClient
  ) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(
      API_URLS.especialidades
    );
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(
      `${API_URLS.especialidades}/${id}`
    );
  }

  guardar(especialidad: any): Observable<any> {
    return this.http.post<any>(
      API_URLS.especialidades,
      especialidad
    );
  }
}
