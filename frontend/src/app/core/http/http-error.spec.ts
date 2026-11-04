import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './http-error';

describe('errorMessage', () => {
  it('usa el detalle de Problem Details', () => {
    const error = new HttpErrorResponse({ status: 409, error: { detail: 'El correo ya está en uso' } });
    expect(errorMessage(error)).toBe('El correo ya está en uso');
  });

  it('prioriza el primer error de validación', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { detail: 'La petición contiene datos no válidos', errors: { email: 'El email no es válido' } },
    });
    expect(errorMessage(error)).toBe('El email no es válido');
  });

  it('entiende respuestas de texto con JSON', () => {
    const error = new HttpErrorResponse({ status: 403, error: '{"detail":"Sin permiso"}' });
    expect(errorMessage(error)).toBe('Sin permiso');
  });

  it('avisa cuando el servidor no responde', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toBe('No se puede conectar con el servidor.');
  });

  it('devuelve el mensaje por defecto en otros casos', () => {
    expect(errorMessage(new Error('x'), 'Algo falló')).toBe('Algo falló');
  });
});
