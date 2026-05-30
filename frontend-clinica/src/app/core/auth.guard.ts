import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

function tokenExpirado(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));

    if (!payload.exp) {
      return true;
    }

    const expiracion = payload.exp * 1000;
    const ahora = Date.now();

    return ahora >= expiracion;

  } catch (error) {
    return true;
  }
}

export const authGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  if (!token || tokenExpirado(token)) {
    authService.logout();
    router.navigate(['/login']);
    return false;
  }

  return true;
};