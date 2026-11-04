import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Injector, inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { tokenStorage } from './token.storage';

/**
 * Anade el token JWT a las peticiones al backend y, si el backend responde 401
 * (token caducado o no valido), cierra la sesion y lleva al login.
 *
 * AuthService se obtiene solo cuando hace falta: AuthService pide el usuario
 * actual al crearse, y si el interceptor lo inyectara de entrada habria una
 * dependencia circular que haria fallar esa primera peticion.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const injector = inject(Injector);
  const token = tokenStorage.get();
  const isApiCall = req.url.startsWith(environment.apiUrl);

  const request = token && isApiCall
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      const isAuthEndpoint = req.url.includes('/api/v1/auth/');
      if (error instanceof HttpErrorResponse && error.status === 401 && isApiCall && !isAuthEndpoint && token) {
        injector.get(AuthService).logout(true);
      }
      return throwError(() => error);
    })
  );
};
