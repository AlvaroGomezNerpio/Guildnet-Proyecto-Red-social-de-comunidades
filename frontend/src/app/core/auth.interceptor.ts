import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Anade el token JWT a las peticiones al backend y, si el backend responde 401
 * (token caducado o no valido), cierra la sesion y lleva al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();
  const isApiCall = req.url.startsWith(environment.apiUrl);

  const request = token && isApiCall
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      const isAuthEndpoint = req.url.includes('/api/v1/auth/');
      if (error instanceof HttpErrorResponse && error.status === 401 && isApiCall && !isAuthEndpoint && token) {
        auth.logout(true);
      }
      return throwError(() => error);
    })
  );
};
