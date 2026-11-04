/**
 * Acceso al token JWT guardado en el navegador.
 *
 * Está separado de AuthService para que el interceptor HTTP pueda leer el token
 * sin depender de AuthService, que a su vez hace peticiones HTTP al crearse.
 */
const TOKEN_KEY = 'token';

export const tokenStorage = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },
  clear(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch {
      // Sin acceso al almacenamiento: no hay nada que borrar.
    }
  },
};
