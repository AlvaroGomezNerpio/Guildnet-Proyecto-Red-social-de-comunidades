/** Actividad que se mide para conseguir un título automático. */
export type TitleMetric = 'POSTS' | 'COMMENTS' | 'LIKES_RECEIVED' | 'DAYS_MEMBER';

export const TITLE_METRICS: readonly { id: TitleMetric; label: string; unit: (n: number) => string }[] = [
  { id: 'POSTS', label: 'Publicaciones escritas', unit: (n) => (n === 1 ? 'publicación' : 'publicaciones') },
  { id: 'COMMENTS', label: 'Comentarios escritos', unit: (n) => (n === 1 ? 'comentario' : 'comentarios') },
  { id: 'LIKES_RECEIVED', label: '«Me gusta» recibidos', unit: (n) => (n === 1 ? '«me gusta» recibido' : '«me gusta» recibidos') },
  { id: 'DAYS_MEMBER', label: 'Días en la comunidad', unit: (n) => (n === 1 ? 'día en la comunidad' : 'días en la comunidad') },
];

/** "Al llegar a 10 publicaciones". */
export function describeRule(metric: TitleMetric, threshold: number): string {
  const m = TITLE_METRICS.find((x) => x.id === metric);
  return m ? `Al llegar a ${threshold.toLocaleString('es-ES')} ${m.unit(threshold)}` : '';
}

export interface TitleDTO {
  id: number;
  title: string;
  textColor: string;
  backgroundColor: string;
  communityId: number;
  /** Regla automática (null: título manual). */
  autoMetric?: TitleMetric | null;
  autoThreshold?: number | null;
}

export interface CreateTitleRequest {
  title: string;
  textColor: string;
  backgroundColor: string;
  communityId: number;
}

export interface UpdateTitleRequest {
  title: string;
  textColor: string;
  backgroundColor: string;
}

export interface TitleRuleRequest {
  metric: TitleMetric;
  threshold: number;
}
