import { DestroyRef, Injectable, Signal, inject, signal } from '@angular/core';
import { CommunityService } from '../../features/communities/services/community.service';
import { CommunityPalette, paletteOf } from './community-palette';

/**
 * Paleta de la comunidad que se está viendo. La aplica el contenedor principal
 * de la aplicación (app.component), así que la cabecera y el pie mantienen
 * siempre los colores de GuildNet.
 *
 * Las páginas no la usan directamente sino con {@link injectCommunityTheme},
 * que la retira sola al salir de la página.
 */
@Injectable({ providedIn: 'root' })
export class CommunityThemeService {
  private readonly communityService = inject(CommunityService);

  readonly palette = signal<CommunityPalette | null>(null);

  /** Paletas ya conocidas, para no volver a pedir la comunidad en cada página. */
  private readonly cache = new Map<number, CommunityPalette>();
  /** Página que ha fijado la paleta actual. */
  private owner: object | null = null;

  /** Recuerda la paleta de una comunidad (p. ej. al cargarla o tras editarla). */
  remember(communityId: number, palette: CommunityPalette): void {
    this.cache.set(communityId, palette);
  }

  set(owner: object, palette: CommunityPalette | null): void {
    this.owner = owner;
    this.palette.set(palette);
  }

  useCommunity(owner: object, communityId: number): void {
    this.owner = owner;
    const cached = this.cache.get(communityId);
    if (cached) {
      this.palette.set(cached);
      return;
    }
    this.communityService.getCommunityById(communityId).subscribe({
      next: (community) => {
        const palette = paletteOf(community);
        this.cache.set(communityId, palette);
        if (this.owner === owner) this.palette.set(palette);
      },
      error: () => undefined,
    });
  }

  release(owner: object): void {
    if (this.owner !== owner) return;
    this.owner = null;
    this.palette.set(null);
  }
}

export interface CommunityThemeHandle {
  /** Paleta que se está mostrando (null mientras no se conoce). */
  readonly palette: Signal<CommunityPalette | null>;
  /** Aplica la paleta de la comunidad indicada (la pide al backend si no la conoce). */
  useCommunity(communityId: number): void;
  /** Aplica una paleta concreta, p. ej. la vista previa del formulario de ajustes. */
  set(palette: CommunityPalette | null): void;
  /** Guarda la paleta de la comunidad para otras páginas y la aplica. */
  apply(communityId: number, palette: CommunityPalette): void;
}

/**
 * Para usar en el constructor de una página de comunidad. La paleta se retira
 * automáticamente al destruirse la página.
 */
export function injectCommunityTheme(): CommunityThemeHandle {
  const service = inject(CommunityThemeService);
  const owner = {};
  inject(DestroyRef).onDestroy(() => service.release(owner));
  return {
    palette: service.palette.asReadonly(),
    useCommunity: (communityId) => service.useCommunity(owner, communityId),
    set: (palette) => service.set(owner, palette),
    apply: (communityId, palette) => {
      service.remember(communityId, palette);
      service.set(owner, palette);
    },
  };
}
