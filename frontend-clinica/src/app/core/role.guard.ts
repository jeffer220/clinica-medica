import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  const rolUsuario = authService.getRol();
  const rolesPermitidos = route.data?.['roles'] as string[];

  if (!rolUsuario) {
    authService.logout();
    router.navigate(['/login']);
    return false;
  }

  if (!rolesPermitidos || !rolesPermitidos.includes(rolUsuario.toUpperCase())) {
    router.navigate(['/app/citas']);
    return false;
  }

  return true;
};