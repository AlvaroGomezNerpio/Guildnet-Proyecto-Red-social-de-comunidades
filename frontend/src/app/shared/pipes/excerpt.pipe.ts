import { Pipe, PipeTransform } from '@angular/core';

/** Convierte HTML en texto plano recortado, para mostrar un resumen (p. ej. de una publicación). */
@Pipe({ name: 'excerpt' })
export class ExcerptPipe implements PipeTransform {
  transform(html: string | null | undefined, maxLength = 160): string {
    if (!html) return '';
    const text = toPlainText(html);
    if (text.length <= maxLength) return text;
    const cut = text.slice(0, maxLength);
    const lastSpace = cut.lastIndexOf(' ');
    return (lastSpace > maxLength * 0.6 ? cut.slice(0, lastSpace) : cut).trimEnd() + '…';
  }
}

function toPlainText(html: string): string {
  const doc = new DOMParser().parseFromString(html.replace(/<(br|\/p|\/li|\/h\d)>/gi, ' $&'), 'text/html');
  return (doc.body.textContent ?? '').replace(/\s+/g, ' ').trim();
}
