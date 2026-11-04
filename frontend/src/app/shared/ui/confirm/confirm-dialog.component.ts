import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ModalComponent } from '../modal/modal.component';
import { ConfirmService } from './confirm.service';

/** Diálogo de confirmación global; se coloca una sola vez en el componente raíz. */
@Component({
  selector: 'app-confirm-dialog',
  imports: [ModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let request = confirm.pending();
    <app-modal [open]="!!request" [title]="request?.title ?? 'Confirmar'" size="sm" (closed)="confirm.answer(false)">
      <p class="mb-0">{{ request?.message }}</p>
      <ng-container modal-footer>
        <button type="button" class="btn btn-outline-secondary" (click)="confirm.answer(false)">Cancelar</button>
        <button
          type="button"
          class="btn"
          [class.btn-danger]="request?.danger"
          [class.btn-primary]="!request?.danger"
          (click)="confirm.answer(true)"
        >
          {{ request?.confirmLabel ?? 'Aceptar' }}
        </button>
      </ng-container>
    </app-modal>
  `,
})
export class ConfirmDialogComponent {
  readonly confirm = inject(ConfirmService);
}
