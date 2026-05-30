import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {

  const token = sessionStorage.getItem('token');

  let authReq = req;

  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error) => {

      if (error.status === 401 || error.status === 403) {
        sessionStorage.clear();
        localStorage.clear();
        window.location.href = '/login';
      }

      return throwError(() => error);
    })
  );
};