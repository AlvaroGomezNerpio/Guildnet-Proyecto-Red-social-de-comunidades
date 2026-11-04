import { ConfirmService } from './confirm/confirm.service';
import { ToastService } from './toast/toast.service';

describe('ConfirmService', () => {
  it('resuelve true al aceptar y limpia el diálogo', async () => {
    const service = new ConfirmService();
    const answer = service.ask({ message: '¿Seguro?' });
    expect(service.pending()?.message).toBe('¿Seguro?');
    service.answer(true);
    await expect(answer).resolves.toBe(true);
    expect(service.pending()).toBeNull();
  });

  it('cancela la pregunta anterior si se abre otra', async () => {
    const service = new ConfirmService();
    const first = service.ask({ message: 'Primera' });
    service.ask({ message: 'Segunda' });
    await expect(first).resolves.toBe(false);
    expect(service.pending()?.message).toBe('Segunda');
  });
});

describe('ToastService', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('muestra avisos y los retira solos', () => {
    const service = new ToastService();
    service.success('Guardado');
    service.error('Fallo');
    expect(service.toasts().map((t) => t.kind)).toEqual(['success', 'error']);

    vi.advanceTimersByTime(4000);
    expect(service.toasts().map((t) => t.message)).toEqual(['Fallo']);

    vi.advanceTimersByTime(3000);
    expect(service.toasts()).toEqual([]);
  });
});
