import { ChangeDetectionStrategy, Component, computed, input, model, signal } from '@angular/core';
import { TagBadgeComponent } from '../tag-badge/tag-badge.component';
import { contrast, isHexColor, readableOn } from '../../../core/theme/community-palette';

interface BadgePreset {
  name: string;
  background: string;
}

/** Fondos predefinidos; el texto se calcula para que siempre se lea. */
const PRESETS: readonly BadgePreset[] = [
  { name: 'Violeta', background: '#7c3aed' },
  { name: 'Índigo', background: '#4f46e5' },
  { name: 'Azul', background: '#2563eb' },
  { name: 'Cian', background: '#0891b2' },
  { name: 'Esmeralda', background: '#059669' },
  { name: 'Lima', background: '#84cc16' },
  { name: 'Ámbar', background: '#f59e0b' },
  { name: 'Naranja', background: '#ea580c' },
  { name: 'Rojo', background: '#dc2626' },
  { name: 'Rosa', background: '#db2777' },
  { name: 'Grafito', background: '#334155' },
  { name: 'Plata', background: '#e2e8f0' },
];

/** Contraste mínimo de WCAG AA para texto. */
const MIN_CONTRAST = 4.5;

/**
 * Selector de colores para las etiquetas de roles y títulos: atajos
 * predefinidos (el primero, el color de la comunidad), colores exactos,
 * texto automático legible y vista previa.
 *
 *   <app-badge-color-picker [(text)]="texto" [(background)]="fondo" [label]="nombre" [communityColor]="color" />
 */
@Component({
  selector: 'app-badge-color-picker',
  imports: [TagBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './badge-color-picker.component.html',
  styleUrl: './badge-color-picker.component.css',
})
export class BadgeColorPickerComponent {
  readonly text = model.required<string>();
  readonly background = model.required<string>();
  /** Texto de la vista previa (el nombre del rol o del título). */
  readonly label = input('');
  /** Color principal de la comunidad, ofrecido como primera opción. */
  readonly communityColor = input<string | null>(null);

  /** Mientras esté activo, el color del texto se ajusta solo al cambiar el fondo. */
  protected readonly autoText = signal(true);

  /** Identificador único de los campos (puede haber varios selectores en la página). */
  private static nextId = 0;
  protected readonly uid = `gn-badge-${BadgeColorPickerComponent.nextId++}`;

  protected readonly presets = computed<BadgePreset[]>(() => {
    const own = this.communityColor();
    return isHexColor(own) ? [{ name: 'Comunidad', background: own.toLowerCase() }, ...PRESETS] : [...PRESETS];
  });

  protected readonly ratio = computed(() =>
    isHexColor(this.text()) && isHexColor(this.background()) ? contrast(this.text(), this.background()) : 21
  );
  protected readonly lowContrast = computed(() => this.ratio() < MIN_CONTRAST);

  protected readonly textFor = readableOn;

  /** Atajo marcado: el primero que coincide (el de la comunidad puede repetir uno de la lista). */
  private readonly activeName = computed(
    () => this.presets().find((p) => p.background === this.background().toLowerCase())?.name ?? null
  );

  protected isActive(preset: BadgePreset): boolean {
    return this.activeName() === preset.name;
  }

  protected choose(preset: BadgePreset): void {
    this.autoText.set(true);
    this.setBackground(preset.background);
  }

  protected setBackground(value: string): void {
    const color = this.normalize(value);
    if (!color) return;
    this.background.set(color);
    if (this.autoText()) this.text.set(readableOn(color));
  }

  protected setText(value: string): void {
    const color = this.normalize(value);
    if (!color) return;
    this.autoText.set(false);
    this.text.set(color);
  }

  protected toggleAuto(enabled: boolean): void {
    this.autoText.set(enabled);
    if (enabled) this.text.set(readableOn(this.background()));
  }

  private normalize(raw: string): string | null {
    const value = (raw.startsWith('#') ? raw : '#' + raw).trim().toLowerCase();
    return isHexColor(value) ? value : null;
  }
}
