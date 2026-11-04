import { DOCUMENT, Injectable, computed, inject, signal } from '@angular/core';

export type ThemePreference = 'light' | 'dark' | 'auto';

const STORAGE_KEY = 'gn-theme';

/**
 * Tema claro/oscuro. Aplica el atributo data-bs-theme de Bootstrap en <html>.
 * "auto" sigue la preferencia del sistema operativo y reacciona si cambia.
 * (index.html aplica el tema guardado antes de arrancar Angular para evitar parpadeos.)
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly media = this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)');

  readonly preference = signal<ThemePreference>(this.readPreference());
  private readonly systemDark = signal(this.media?.matches ?? false);

  /** Tema que se está mostrando realmente. */
  readonly active = computed<'light' | 'dark'>(() => {
    const pref = this.preference();
    return pref === 'auto' ? (this.systemDark() ? 'dark' : 'light') : pref;
  });

  constructor() {
    this.media?.addEventListener('change', (event) => {
      this.systemDark.set(event.matches);
      this.apply();
    });
    this.apply();
  }

  set(preference: ThemePreference): void {
    this.preference.set(preference);
    try {
      localStorage.setItem(STORAGE_KEY, preference);
    } catch {
      // El navegador no permite guardar la preferencia: se aplica igualmente en esta sesión
    }
    this.apply();
  }

  /** Alterna entre claro y oscuro. */
  toggle(): void {
    this.set(this.active() === 'dark' ? 'light' : 'dark');
  }

  private apply(): void {
    this.document.documentElement.setAttribute('data-bs-theme', this.active());
  }

  private readPreference(): ThemePreference {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored === 'light' || stored === 'dark' || stored === 'auto' ? stored : 'auto';
    } catch {
      return 'auto';
    }
  }
}
