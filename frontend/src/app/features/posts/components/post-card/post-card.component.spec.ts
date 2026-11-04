import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PostCardComponent } from './post-card.component';
import { PostDTO } from '../../models/post.models';

const post: PostDTO = {
  id: 7,
  title: 'Quedada el sábado',
  content: '<p>Este sábado a las <strong>17:00</strong> en la ludoteca.</p>',
  tags: ['quedada', 'ocio'],
  likes: 3,
  comments: 2,
  createdAt: new Date().toISOString(),
  updatedAt: null,
  communityId: 1,
  pinned: false,
  communityProfile: {
    id: 5,
    username: 'Bruno',
    description: null,
    profileImage: null,
    featuredTitle: null,
    userId: 2,
    communityId: 1,
    rol: { id: 9, name: 'Moderador', textColor: '#ffffff', backgroundColor: '#6f42c1', communityId: 1 },
    titles: [],
  },
};

describe('PostCardComponent', () => {
  function setup(showAuthor = true) {
    TestBed.configureTestingModule({ imports: [PostCardComponent], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(PostCardComponent);
    fixture.componentRef.setInput('post', post);
    fixture.componentRef.setInput('showAuthor', showAuthor);
    fixture.detectChanges();
    return fixture;
  }

  const render = (showAuthor = true): HTMLElement => setup(showAuthor).nativeElement as HTMLElement;

  it('muestra título, autor, rol, etiquetas y contadores', () => {
    const el = render();
    expect(el.querySelector('.gn-post-title')?.textContent).toContain('Quedada el sábado');
    expect(el.textContent).toContain('Bruno');
    expect(el.textContent).toContain('Moderador');
    expect(el.textContent).toContain('#quedada');
    expect(el.textContent).toContain('3');
    expect(el.querySelector('a[href="/posts/7"]')).not.toBeNull();
  });

  it('muestra un resumen en texto plano y despliega el contenido completo', () => {
    const fixture = setup();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('strong')).toBeNull();
    expect(el.textContent).toContain('Este sábado a las 17:00 en la ludoteca.');

    const toggle = Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Ver más'))!;
    toggle.click();
    fixture.detectChanges();
    expect(el.querySelector('.gn-post-content strong')?.textContent).toBe('17:00');
    expect(el.textContent).toContain('Ver menos');
  });

  it('puede ocultar el autor', () => {
    const el = render(false);
    expect(el.textContent).not.toContain('Bruno');
  });
});
