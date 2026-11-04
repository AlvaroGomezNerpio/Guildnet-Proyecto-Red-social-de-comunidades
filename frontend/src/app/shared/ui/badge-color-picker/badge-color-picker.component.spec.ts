import { TestBed } from '@angular/core/testing';
import { BadgeColorPickerComponent } from './badge-color-picker.component';

describe('BadgeColorPickerComponent', () => {
  function create(text = '#ffffff', background = '#7c3aed', communityColor: string | null = '#ea580c') {
    const fixture = TestBed.createComponent(BadgeColorPickerComponent);
    fixture.componentRef.setInput('text', text);
    fixture.componentRef.setInput('background', background);
    fixture.componentRef.setInput('communityColor', communityColor);
    fixture.componentRef.setInput('label', 'Moderador');
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;
    return { fixture, component: fixture.componentInstance, element };
  }

  const presets = (element: HTMLElement) => Array.from(element.querySelectorAll<HTMLButtonElement>('.gn-badge-preset'));

  it('ofrece primero el color de la comunidad', () => {
    const { element } = create();
    expect(presets(element)[0].textContent).toContain('Comunidad');
    expect(presets(element)[0].style.backgroundColor).toBe('rgb(234, 88, 12)');
  });

  it('al elegir un fondo claro, el texto pasa a oscuro para que se lea', () => {
    const { fixture, component, element } = create();
    presets(element).find((b) => b.textContent?.includes('Plata'))!.click();
    fixture.detectChanges();
    expect(component.background()).toBe('#e2e8f0');
    expect(component.text()).toBe('#111322');
  });

  it('si se elige el texto a mano, deja de ajustarse solo y avisa del poco contraste', () => {
    const { fixture, component, element } = create('#ffffff', '#e2e8f0');
    const textHex = element.querySelectorAll<HTMLInputElement>('input.font-monospace')[1];
    textHex.value = '#f1f5f9';
    textHex.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(component.text()).toBe('#f1f5f9');
    expect(element.textContent).toContain('Poco contraste');

    presets(element).find((b) => b.textContent?.includes('Azul'))!.click();
    fixture.detectChanges();
    expect(component.text()).toBe('#ffffff');
    expect(element.textContent).not.toContain('Poco contraste');
  });
});
