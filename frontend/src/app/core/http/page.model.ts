/** Página de resultados tal y como la devuelve el backend. */
export interface Page<T> {
  content: T[];
  /** Número de página (empieza en 0). */
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
