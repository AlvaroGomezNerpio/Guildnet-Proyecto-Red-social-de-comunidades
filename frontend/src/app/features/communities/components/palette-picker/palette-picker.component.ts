import { ChangeDetectionStrategy, Component, computed, model } from '@angular/core';
import { PaletteDirective } from '../../../../core/theme/palette.directive';
import {
  CommunityPalette,
  PALETTE_PRESETS,
  PalettePreset,
  isHexColor,
  paletteWarnings,
} from '../../../../core/theme/community-palette';

/**
 * Selector de la paleta de una comunidad: atajos predefinidos y colores
 * exactos, con vista previa en tema claro y oscuro.
 *
 *   <app-palette-picker [(primary)]="primary" [(accent)]="accent" />
 */
@Component({
  selector: 'app-palette-picker',
  imports: [PaletteDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './palette-picker.component.html',
  styleUrl: './palette-picker.component.css',
})
export class PalettePickerComponent {
  readonly primary = model.required<string>();
  readonly accent = model.required<string>();

  protected readonly presets = PALETTE_PRESETS;
  protected readonly palette = computed<CommunityPalette>(() => ({ primary: this.primary(), accent: this.accent() }));
  protected readonly warnings = computed(() => paletteWarnings(this.palette()));
  protected readonly activePreset = computed(
    () =>
      this.presets.find(
        (p) => p.primary === this.primary().toLowerCase() && p.accent === this.accent().toLowerCase()
      )?.id ?? null
  );

  protected choose(preset: PalettePreset): void {
    this.primary.set(preset.primary);
    this.accent.set(preset.accent);
  }

  /** Campo de texto hexadecimal: solo se aplica cuando el valor es válido. */
  protected typed(which: 'primary' | 'accent', raw: string): void {
    const value = (raw.startsWith('#') ? raw : '#' + raw).trim().toLowerCase();
    if (isHexColor(value)) this[which].set(value);
  }

  protected picked(which: 'primary' | 'accent', event: Event): void {
    this[which].set((event.target as HTMLInputElement).value.toLowerCase());
  }

  protected swap(): void {
    const primary = this.primary();
    this.primary.set(this.accent());
    this.accent.set(primary);
  }
}
