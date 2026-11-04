import { ChangeDetectionStrategy, Component, HostListener, input, output } from '@angular/core';

/**
 * Ventana modal controlada desde Angular (sin depender del JavaScript de Bootstrap).
 *
 * <app-modal [open]="mostrar" title="Título" (closed)="mostrar = false">
 *   contenido
 *   <ng-container modal-footer>botones</ng-container>
 * </app-modal>
 */
@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalComponent {
  readonly open = input(false);
  readonly title = input('');
  readonly size = input<'sm' | 'md' | 'lg' | 'xl'>('md');
  readonly closed = output<void>();

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.open()) this.closed.emit();
  }

  close(): void {
    this.closed.emit();
  }
}
