import { ExcerptPipe } from './excerpt.pipe';
import { RelativeTimePipe } from './relative-time.pipe';

describe('RelativeTimePipe', () => {
  const pipe = new RelativeTimePipe();
  const now = new Date('2026-11-04T12:00:00Z');

  it('muestra "ahora mismo" para menos de un minuto', () => {
    expect(pipe.transform('2026-11-04T11:59:30Z', now)).toBe('ahora mismo');
  });

  it('usa minutos, horas y días', () => {
    expect(pipe.transform('2026-11-04T11:55:00Z', now)).toBe('hace 5 minutos');
    expect(pipe.transform('2026-11-04T09:00:00Z', now)).toBe('hace 3 horas');
    expect(pipe.transform('2026-11-03T12:00:00Z', now)).toBe('ayer');
  });

  it('devuelve vacío si no hay fecha válida', () => {
    expect(pipe.transform(null, now)).toBe('');
    expect(pipe.transform('no-es-fecha', now)).toBe('');
  });
});

describe('ExcerptPipe', () => {
  const pipe = new ExcerptPipe();

  it('quita las etiquetas HTML', () => {
    expect(pipe.transform('<p>Hola <strong>mundo</strong></p><p>Adiós</p>')).toBe('Hola mundo Adiós');
  });

  it('recorta por palabras y añade puntos suspensivos', () => {
    const result = pipe.transform('<p>uno dos tres cuatro cinco seis siete</p>', 20);
    expect(result.endsWith('…')).toBe(true);
    expect(result.length).toBeLessThanOrEqual(21);
    expect(result).not.toContain('<');
  });

  it('no ejecuta scripts del contenido', () => {
    expect(pipe.transform('<img src=x onerror="alert(1)">texto')).toBe('texto');
  });
});
