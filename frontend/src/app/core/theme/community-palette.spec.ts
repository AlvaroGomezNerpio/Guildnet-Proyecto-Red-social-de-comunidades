import { PALETTE_PRESETS, contrast, paletteCssVars, paletteOf, paletteWarnings, readableOn } from './community-palette';

describe('community-palette', () => {
  it('calcula el contraste WCAG', () => {
    expect(contrast('#000000', '#ffffff')).toBeCloseTo(21, 0);
    expect(contrast('#777777', '#777777')).toBeCloseTo(1, 5);
  });

  it('elige el texto que mejor se lee sobre cada color', () => {
    expect(readableOn('#1e3a8a')).toBe('#ffffff');
    expect(readableOn('#fde047')).not.toBe('#ffffff');
  });

  it('los textos en color siempre se leen, aunque el color elegido sea muy claro u oscuro', () => {
    const colores = ['#fde047', '#ffffff', '#000000', '#0e1020', '#22d3ee', ...PALETTE_PRESETS.map((p) => p.primary)];
    for (const primary of colores) {
      for (const mode of ['light', 'dark'] as const) {
        const vars = paletteCssVars({ primary, accent: '#06b6d4' }, mode);
        const page = mode === 'light' ? '#f5f6fb' : '#0e1020';
        expect(contrast(vars['--gn-c-emphasis'], page), `${primary} en ${mode}`).toBeGreaterThanOrEqual(4.5);
        expect(contrast(vars['--gn-c-on-primary'], vars['--gn-c-primary'])).toBeGreaterThanOrEqual(4.5);
      }
    }
  });

  it('avisa solo cuando el color se confunde con el fondo', () => {
    expect(paletteWarnings({ primary: '#7c3aed', accent: '#06b6d4' })).toEqual([]);
    expect(paletteWarnings({ primary: '#fde047', accent: '#06b6d4' }).map((w) => w.mode)).toEqual(['light']);
    expect(paletteWarnings({ primary: '#101225', accent: '#06b6d4' }).map((w) => w.mode)).toEqual(['dark']);
  });

  it('usa la paleta de GuildNet si la comunidad no tiene colores válidos', () => {
    expect(paletteOf({ primaryColor: 'red; x', accentColor: null })).toEqual({ primary: '#7c3aed', accent: '#06b6d4' });
    expect(paletteOf({ primaryColor: '#ea580c', accentColor: '#db2777' })).toEqual({ primary: '#ea580c', accent: '#db2777' });
  });
});
