/**
 * Paletas de color de las comunidades.
 *
 * Cada comunidad elige dos colores exactos (principal y acento). A partir de
 * ellos se calculan todos los tonos que usa la interfaz (fondos suaves, texto
 * resaltado, texto sobre botones...) ajustándolos para que se lean bien tanto
 * en tema claro como oscuro, elija la comunidad el color que elija.
 */

export type HeadingFont = 'default' | 'serif' | 'rounded' | 'pixel' | 'hand';

export interface CommunityPalette {
  primary: string;
  accent: string;
  /** Tipografía de los títulos; sin indicar, la de GuildNet. */
  font?: HeadingFont;
}

export interface HeadingFontOption {
  id: HeadingFont;
  name: string;
  family: string;
}

/** Tipografías para los títulos de la comunidad (las fuentes se cargan solo si se usan). */
export const HEADING_FONTS: readonly HeadingFontOption[] = [
  { id: 'default', name: 'Moderna', family: "'Space Grotesk Variable', 'Inter Variable', sans-serif" },
  { id: 'serif', name: 'Clásica', family: "'Fraunces Variable', Georgia, serif" },
  { id: 'rounded', name: 'Redondeada', family: "'Nunito Variable', 'Inter Variable', sans-serif" },
  { id: 'pixel', name: 'Pixel', family: "'Pixelify Sans Variable', monospace" },
  { id: 'hand', name: 'Manuscrita', family: "'Caveat Variable', cursive" },
];

export function headingFontFamily(font: HeadingFont | null | undefined): string {
  return (HEADING_FONTS.find((f) => f.id === font) ?? HEADING_FONTS[0]).family;
}

export interface PalettePreset extends CommunityPalette {
  id: string;
  name: string;
}

export const DEFAULT_PALETTE: CommunityPalette = { primary: '#7c3aed', accent: '#06b6d4' };

/** Atajos para elegir una paleta con un clic; después se puede retocar a mano. */
export const PALETTE_PRESETS: readonly PalettePreset[] = [
  { id: 'guildnet', name: 'GuildNet', ...DEFAULT_PALETTE },
  { id: 'esmeralda', name: 'Esmeralda', primary: '#059669', accent: '#14b8a6' },
  { id: 'atardecer', name: 'Atardecer', primary: '#ea580c', accent: '#db2777' },
  { id: 'oceano', name: 'Océano', primary: '#0284c7', accent: '#22d3ee' },
  { id: 'carmesi', name: 'Carmesí', primary: '#dc2626', accent: '#9f1239' },
  { id: 'bosque', name: 'Bosque', primary: '#15803d', accent: '#65a30d' },
  { id: 'dorado', name: 'Dorado', primary: '#d97706', accent: '#f59e0b' },
  { id: 'medianoche', name: 'Medianoche', primary: '#4338ca', accent: '#1e3a8a' },
  { id: 'sakura', name: 'Sakura', primary: '#db2777', accent: '#a78bfa' },
  { id: 'grafito', name: 'Grafito', primary: '#475569', accent: '#94a3b8' },
];

/** Fondos de la página en cada tema (deben coincidir con styles.scss). */
const PAGE_BG = { light: '#f5f6fb', dark: '#0e1020' } as const;
const CARD_BG = { light: '#ffffff', dark: '#151829' } as const;
const WHITE = '#ffffff';
const INK = '#111322';

/** Contraste mínimo de WCAG AA para texto normal. */
const MIN_TEXT_CONTRAST = 4.5;
/** Contraste mínimo para que un elemento grande (botón, borde) se distinga del fondo. */
const MIN_UI_CONTRAST = 3;

type Rgb = [number, number, number];

const HEX_RE = /^#[0-9a-f]{6}$/i;

export function isHexColor(value: string | null | undefined): value is string {
  return !!value && HEX_RE.test(value);
}

function toRgb(hex: string): Rgb {
  const n = Number.parseInt(hex.slice(1), 16);
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
}

function toHex([r, g, b]: Rgb): string {
  return '#' + [r, g, b].map((c) => Math.round(c).toString(16).padStart(2, '0')).join('');
}

/** Mezcla dos colores: amount = 0 devuelve a, amount = 1 devuelve b. */
export function mix(a: string, b: string, amount: number): string {
  const ca = toRgb(a);
  const cb = toRgb(b);
  return toHex([0, 1, 2].map((i) => ca[i] + (cb[i] - ca[i]) * amount) as Rgb);
}

function luminance(hex: string): number {
  const [r, g, b] = toRgb(hex).map((c) => {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4;
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

/** Relación de contraste WCAG entre dos colores (de 1 a 21). */
export function contrast(a: string, b: string): number {
  const la = luminance(a);
  const lb = luminance(b);
  return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
}

/**
 * Texto que se lee sobre un color de fondo: blanco si es posible, si no casi
 * negro y, en los tonos intermedios donde ninguno llega, negro puro (que
 * siempre alcanza al menos 4,58:1 si el blanco no llega).
 */
export function readableOn(background: string): string {
  if (contrast(background, WHITE) >= MIN_TEXT_CONTRAST) return WHITE;
  if (contrast(background, INK) >= MIN_TEXT_CONTRAST) return INK;
  return contrast(background, WHITE) >= contrast(background, '#000000') ? WHITE : '#000000';
}

/**
 * Acerca el color a blanco o negro, poco a poco, hasta que alcance el contraste
 * pedido con todos los fondos indicados.
 */
function ensureContrast(color: string, backgrounds: string[], target: number, towards: string): string {
  let result = color;
  for (let step = 1; step <= 20; step++) {
    if (backgrounds.every((bg) => contrast(result, bg) >= target)) return result;
    result = mix(color, towards, step / 20);
  }
  return result;
}

/**
 * Variables CSS que aplican la paleta a un bloque de la página.
 * Las consume la clase .gn-themed de styles.scss.
 */
export function paletteCssVars(palette: CommunityPalette, mode: 'light' | 'dark'): Record<string, string> {
  const primary = palette.primary.toLowerCase();
  const accent = palette.accent.toLowerCase();
  const dark = mode === 'dark';
  const page = PAGE_BG[mode];
  const card = CARD_BG[mode];
  const towardsText = dark ? WHITE : '#000000';

  // Fondos y bordes suaves teñidos con el color principal
  const subtle = dark ? mix(primary, page, 0.78) : mix(primary, WHITE, 0.86);
  const borderSubtle = dark ? mix(primary, page, 0.5) : mix(primary, WHITE, 0.6);

  // Texto en color (enlaces, etiquetas): debe leerse sobre la página, las tarjetas y el fondo suave
  const emphasis = ensureContrast(primary, [page, card, subtle], MIN_TEXT_CONTRAST, towardsText);

  // Botones: el color exacto elegido, con el texto que mejor se lea encima
  const onPrimary = readableOn(primary);
  const hover = mix(primary, onPrimary === WHITE ? '#000000' : WHITE, 0.15);

  // Elementos de interfaz pintados con el color principal (bordes, iconos): que se distingan del fondo
  const uiColor = ensureContrast(primary, [page, card], MIN_UI_CONTRAST, towardsText);

  const [r, g, b] = toRgb(uiColor);
  const [er, eg, eb] = toRgb(emphasis);
  // En oscuro el degradado se oscurece un poco para que el texto blanco encima no deslumbre
  const gradFrom = dark ? mix(primary, '#000000', 0.2) : primary;
  const gradTo = dark ? mix(accent, '#000000', 0.2) : accent;

  return {
    '--gn-c-primary': primary,
    '--gn-c-primary-hover': hover,
    '--gn-c-on-primary': onPrimary,
    '--gn-c-ui': uiColor,
    '--gn-c-ui-rgb': `${r}, ${g}, ${b}`,
    '--gn-c-emphasis': emphasis,
    '--gn-c-emphasis-rgb': `${er}, ${eg}, ${eb}`,
    '--gn-c-subtle': subtle,
    '--gn-c-border-subtle': borderSubtle,
    '--gn-c-gradient': `linear-gradient(135deg, ${gradFrom} 0%, ${gradTo} 100%)`,
    '--gn-c-on-gradient': readableOn(mix(gradFrom, gradTo, 0.5)),
    '--gn-c-glow': `rgba(${toRgb(primary).join(', ')}, 0.3)`,
    '--gn-c-heading-font': headingFontFamily(palette.font),
  };
}

export interface PaletteWarning {
  mode: 'light' | 'dark';
  message: string;
}

/**
 * Avisos para el formulario cuando el color principal se distingue poco del
 * fondo. La interfaz ajusta igualmente los textos para que se lean, pero los
 * botones y el degradado usan el color tal cual.
 */
export function paletteWarnings(palette: CommunityPalette): PaletteWarning[] {
  const warnings: PaletteWarning[] = [];
  if (contrast(palette.primary, PAGE_BG.light) < 1.6) {
    warnings.push({ mode: 'light', message: 'El color principal es muy claro: los botones casi no se distinguirán en el tema claro.' });
  }
  if (contrast(palette.primary, PAGE_BG.dark) < 1.6) {
    warnings.push({ mode: 'dark', message: 'El color principal es muy oscuro: los botones casi no se distinguirán en el tema oscuro.' });
  }
  return warnings;
}

/** Paleta de una comunidad (o la de GuildNet si no tiene una válida). */
export function paletteOf(
  community: { primaryColor?: string | null; accentColor?: string | null; headingFont?: HeadingFont | null } | null | undefined
): CommunityPalette {
  const primary = community?.primaryColor;
  const accent = community?.accentColor;
  const font = HEADING_FONTS.some((f) => f.id === community?.headingFont) ? community!.headingFont! : 'default';
  return {
    primary: isHexColor(primary) ? primary : DEFAULT_PALETTE.primary,
    accent: isHexColor(accent) ? accent : DEFAULT_PALETTE.accent,
    font,
  };
}
