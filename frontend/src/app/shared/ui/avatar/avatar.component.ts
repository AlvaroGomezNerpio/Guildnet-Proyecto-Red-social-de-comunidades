import { ChangeDetectionStrategy, Component, input, signal, effect } from '@angular/core';

const DEFAULT_AVATAR = 'default-avatar.svg';

/** Imagen circular de perfil o comunidad, con imagen por defecto si no hay o falla. */
@Component({
  selector: 'app-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <img
      class="gn-avatar"
      [src]="current()"
      [alt]="alt()"
      [style.width.px]="size()"
      [style.height.px]="size()"
      loading="lazy"
      (error)="current.set(fallback)"
    />
  `,
  styles: `
    :host {
      display: inline-flex;
      flex-shrink: 0;
    }
    .gn-avatar {
      border-radius: 50%;
      object-fit: cover;
      background: var(--gn-surface-2, #dee2e6);
      border: 2px solid var(--gn-avatar-border, transparent);
    }
  `,
})
export class AvatarComponent {
  readonly src = input<string | null | undefined>(null);
  readonly size = input(40);
  readonly alt = input('');

  protected readonly fallback = DEFAULT_AVATAR;
  protected readonly current = signal(DEFAULT_AVATAR);

  constructor() {
    effect(() => this.current.set(this.src() || DEFAULT_AVATAR));
  }
}
