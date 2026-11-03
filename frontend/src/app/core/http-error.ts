import { HttpErrorResponse } from '@angular/common/http';

/**
 * Extrae un mensaje legible de un error HTTP. El backend responde con
 * Problem Details ({ detail, errors }), asi que se usa ese texto si existe.
 */
export function errorMessage(error: unknown, fallback = 'Ha ocurrido un error inesperado.'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se puede conectar con el servidor.';
    }
    const body = typeof error.error === 'string' ? safeParse(error.error) : error.error;
    if (body?.errors && typeof body.errors === 'object') {
      const first = Object.values(body.errors)[0];
      if (typeof first === 'string') return first;
    }
    if (typeof body?.detail === 'string' && body.detail) {
      return body.detail;
    }
  }
  return fallback;
}

function safeParse(text: string): any {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}
