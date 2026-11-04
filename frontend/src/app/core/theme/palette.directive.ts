import { Directive, ElementRef, effect, inject, input } from '@angular/core';
import { ThemeService } from './theme.service';
import { CommunityPalette, paletteCssVars } from './community-palette';

/**
 * Aplica la paleta de una comunidad a un bloque de la página:
 *
 *   <div [gnPalette]="palette">…</div>
 *
 * Calcula los tonos para el tema activo (claro u oscuro) y los recalcula si
 * cambia. Con null, el bloque usa los colores de GuildNet.
 */
@Directive({
  selector: '[gnPalette]',
  host: { '[class.gn-themed]': '!!gnPalette()' },
})
export class PaletteDirective {
  readonly gnPalette = input<CommunityPalette | null | undefined>(null);
  /** Fuerza un tema concreto (p. ej. en una vista previa) en lugar del activo. */
  readonly gnPaletteMode = input<'light' | 'dark' | null>(null);

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly theme = inject(ThemeService);
  private applied: string[] = [];

  constructor() {
    effect(() => {
      const palette = this.gnPalette();
      const style = this.element.nativeElement.style;
      this.applied.forEach((name) => style.removeProperty(name));
      this.applied = [];
      if (!palette) return;

      const vars = paletteCssVars(palette, this.gnPaletteMode() ?? this.theme.active());
      for (const [name, value] of Object.entries(vars)) {
        style.setProperty(name, value);
        this.applied.push(name);
      }
    });
  }
}
