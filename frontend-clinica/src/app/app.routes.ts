import { Routes } from '@angular/router';

import { Login } from './login/login';
import { RegistroUsuario } from './registro-usuario/registro-usuario';
import { MainLayout } from './layout/main-layout/main-layout';

import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: Login
  },

  // Registro público solo para pacientes
  {
    path: 'registro-usuario',
    component: RegistroUsuario
  },

  // Menú principal después del login
  {
    path: 'app',
    component: MainLayout,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: 'pacientes',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'RECEPCIONISTA']
        },
        loadComponent: () =>
          import('./pacientes/pacientes').then(m => m.Pacientes)
      },

      {
        path: 'especialidades',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN']
        },
        loadComponent: () =>
          import('./especialidades/especialidades').then(m => m.Especialidades)
      },

      {
        path: 'medicos',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN']
        },
        loadComponent: () =>
          import('./medicos/medicos').then(m => m.Medicos)
      },

      {
        path: 'horarios',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'MEDICO']
        },
        loadComponent: () =>
          import('./horarios/horarios').then(m => m.Horarios)
      },

      {
        path: 'citas',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'RECEPCIONISTA', 'MEDICO', 'PACIENTE']
        },
        loadComponent: () =>
          import('./citas/citas').then(m => m.Citas)
      },

      {
        path: 'historial-medico',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'RECEPCIONISTA', 'MEDICO', 'PACIENTE']
        },
        loadComponent: () =>
          import('./historial-medico/historial-medico').then(m => m.HistorialMedico)
      },

      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN']
        },
        loadComponent: () =>
          import('./usuarios/usuarios').then(m => m.Usuarios)
      },

      {
        path: '',
        redirectTo: 'citas',
        pathMatch: 'full'
      }
    ]
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];