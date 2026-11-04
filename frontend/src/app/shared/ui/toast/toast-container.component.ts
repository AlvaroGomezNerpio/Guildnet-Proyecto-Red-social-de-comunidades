import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

/** Contenedor de avisos; se coloca una sola vez en el componente raíz. */
@Component({
  selector: 'app-toast-container',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toast-container position-fixed bottom-0 end-0 p-3" aria-live="polite">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast show align-items-center border-0 gn-toast gn-toast-{{ toast.kind }}" role="status">
          <div class="d-flex">
            <div class="toast-body">
              <i
                class="bi me-2"
                [class.bi-check-circle-fill]="toast.kind === 'success'"
                [class.bi-exclamation-triangle-fill]="toast.kind === 'error'"
                [class.bi-info-circle-fill]="toast.kind === 'info'"
              ></i>
              {{ toast.message }}
            </div>
            <button type="button" class="btn-close me-2 m-auto" aria-label="Cerrar" (click)="toastService.dismiss(toast.id)"></button>
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .toast-container {
      z-index: 1090;
    }
    .gn-toast {
      animation: gn-toast-in 0.2s ease-out;
    }
    @keyframes gn-toast-in {
      from { opacity: 0; transform: translateY(8px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);
}
