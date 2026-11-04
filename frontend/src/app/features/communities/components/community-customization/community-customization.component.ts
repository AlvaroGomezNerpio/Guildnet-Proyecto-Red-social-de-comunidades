import { ChangeDetectionStrategy, Component, computed, input, model } from '@angular/core';
import { PalettePickerComponent } from '../palette-picker/palette-picker.component';
import { CommunityCustomization, CommunityLink, isValidLinkUrl } from '../../models/community.models';
import { HEADING_FONTS, HeadingFont, headingFontFamily } from '../../../../core/theme/community-palette';
import { linkIcon } from '../community-links/link-icon';

/** Máximo de enlaces por comunidad (lo valida también el backend). */
export const MAX_LINKS = 6;

/**
 * Secciones de personalización de una comunidad, compartidas por los
 * formularios de crear y editar: apariencia, identidad, normas y enlaces.
 *
 *   <app-community-customization [(value)]="customization" [communityName]="name" />
 */
@Component({
  selector: 'app-community-customization',
  imports: [PalettePickerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './community-customization.component.html',
  styleUrl: './community-customization.component.css',
})
export class CommunityCustomizationComponent {
  readonly value = model.required<CommunityCustomization>();
  /** Nombre de la comunidad, para las vistas previas. */
  readonly communityName = input('');

  protected readonly fonts = HEADING_FONTS;
  protected readonly maxLinks = MAX_LINKS;
  protected readonly familyOf = headingFontFamily;
  protected readonly iconOf = linkIcon;

  protected readonly membersPreview = computed(() => `1.204 ${this.value().membersLabel?.trim() || 'miembros'}`);

  protected update(patch: Partial<CommunityCustomization>): void {
    this.value.update((current) => ({ ...current, ...patch }));
  }

  protected setFont(font: HeadingFont): void {
    this.update({ headingFont: font });
  }

  protected text(event: Event): string {
    return (event.target as HTMLInputElement | HTMLTextAreaElement).value;
  }

  // ------------------------------------------------------------------ Enlaces

  protected addLink(): void {
    if (this.value().links.length >= MAX_LINKS) return;
    this.update({ links: [...this.value().links, { label: '', url: 'https://' }] });
  }

  protected setLink(index: number, patch: Partial<CommunityLink>): void {
    this.update({ links: this.value().links.map((link, i) => (i === index ? { ...link, ...patch } : link)) });
  }

  protected removeLink(index: number): void {
    this.update({ links: this.value().links.filter((_, i) => i !== index) });
  }

  protected moveLink(index: number, delta: -1 | 1): void {
    const links = [...this.value().links];
    const target = index + delta;
    if (target < 0 || target >= links.length) return;
    [links[index], links[target]] = [links[target], links[index]];
    this.update({ links });
  }

  protected readonly isValidUrl = isValidLinkUrl;
}
