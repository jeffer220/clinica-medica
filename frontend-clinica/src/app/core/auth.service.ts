import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { API_URLS } from './api-config';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  rol: string;
  pacienteId?: number;
  medicoId?: number;
}

export interface AuthResponse {
  status: string;
  message: string;
  data: {
    access_token: string;
    token_type: string;
    expires_in: number;
    usuario: {
      id: number;
      email: string;
      rol: string;
      pacienteId?: number;
      medicoId?: number;
    };
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(
    private http: HttpClient
  ) {}

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${API_URLS.auth}/login`,
      data
    ).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  registrar(data: RegisterRequest): Observable<any> {
    return this.http.post<any>(
      `${API_URLS.auth}/register`,
      data
    );
  }

  listarUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(
      `${API_URLS.auth}/usuarios`
    );
  }

  guardarSesion(res: AuthResponse): void {

    sessionStorage.clear();
    localStorage.clear();

    sessionStorage.setItem(
      'token',
      res.data.access_token
    );

    sessionStorage.setItem(
      'rol',
      res.data.usuario.rol
    );

    sessionStorage.setItem(
      'email',
      res.data.usuario.email
    );

    sessionStorage.setItem(
      'userId',
      String(res.data.usuario.id)
    );

    if (res.data.usuario.pacienteId != null) {
      sessionStorage.setItem(
        'pacienteId',
        String(res.data.usuario.pacienteId)
      );
    }

    if (res.data.usuario.medicoId != null) {
      sessionStorage.setItem(
        'medicoId',
        String(res.data.usuario.medicoId)
      );
    }
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  getRol(): string | null {
    return sessionStorage.getItem('rol');
  }

  getEmail(): string | null {
    return sessionStorage.getItem('email');
  }

  getUserId(): string | null {
    return sessionStorage.getItem('userId');
  }

  getPacienteId(): string | null {
    return sessionStorage.getItem('pacienteId');
  }

  getMedicoId(): string | null {
    return sessionStorage.getItem('medicoId');
  }

  estaAutenticado(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    sessionStorage.clear();
    localStorage.clear();
  }
}