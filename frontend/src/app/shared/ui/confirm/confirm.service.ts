import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title?: string;
  message: string;
  confirmLabel?: string;
  /** Muestra el botón de confirmar en rojo (acciones destructivas). */
  danger?: boolean;
}

interface PendingConfirm extends ConfirmOptions {
  resolve: (accepted: boolean) => void;
}

/**
 * Diálogo de confirmación propio (sustituye a window.confirm).
 *
 * if (await this.confirm.ask({ message: '¿Borrar?', danger: true })) { ... }
 */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly pending = signal<PendingConfirm | null>(null);

  ask(options: ConfirmOptions): Promise<boolean> {
    this.pending()?.resolve(false);
    return new Promise((resolve) => this.pending.set({ ...options, resolve }));
  }

  answer(accepted: boolean): void {
    this.pending()?.resolve(accepted);
    this.pending.set(null);
  }
}
