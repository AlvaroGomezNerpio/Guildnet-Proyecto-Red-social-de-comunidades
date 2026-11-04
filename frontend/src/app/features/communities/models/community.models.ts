import { HeadingFont } from '../../../core/theme/community-palette';

/** Enlace externo de la comunidad (Discord, web, redes...). */
export interface CommunityLink {
  label: string;
  url: string;
}

/** Campos de personalización que comparten la creación y la edición. */
export interface CommunityCustomization {
  primaryColor: string;
  accentColor: string;
  headingFont: HeadingFont;
  /** Cómo se llama a los miembros ("Entrenadores"...). Vacío: "miembros". */
  membersLabel: string | null;
  welcomeMessage: string | null;
  requireRulesAcceptance: boolean;
  links: CommunityLink[];
}

export interface CommunityResponseDTO extends CommunityCustomization {
  id: number;
  name: string;
  description: string;
  rules: string;
  image: string;
  banner: string;
  tags: string[];
  ownerId: number;
  memberCount: number;
}

export interface CreateCommunityRequest extends CommunityCustomization {
  name: string;
  description: string;
  rules: string;
  tags: string[];
}

export interface UpdateCommunityRequest extends CommunityCustomization {
  name: string;
  description: string;
  rules: string;
  tags: string[];
}

const LINK_URL = /^https?:\/\/[^\s]+$/;

/** ¿Es una dirección de enlace válida (http/https y con algo después)? */
export function isValidLinkUrl(url: string): boolean {
  const value = url.trim();
  return LINK_URL.test(value) && value.length > 'https://'.length;
}

/** Personalización de una comunidad nueva: la de GuildNet. */
export function defaultCustomization(): CommunityCustomization {
  return {
    primaryColor: '#7c3aed',
    accentColor: '#06b6d4',
    headingFont: 'default',
    membersLabel: null,
    welcomeMessage: null,
    requireRulesAcceptance: false,
    links: [],
  };
}

export function customizationOf(c: CommunityResponseDTO): CommunityCustomization {
  return {
    primaryColor: c.primaryColor,
    accentColor: c.accentColor,
    headingFont: c.headingFont ?? 'default',
    membersLabel: c.membersLabel,
    welcomeMessage: c.welcomeMessage,
    requireRulesAcceptance: c.requireRulesAcceptance,
    links: c.links ?? [],
  };
}

/** Antes de enviar: recorta los textos y quita los enlaces que se han dejado a medias. */
export function cleanCustomization(c: CommunityCustomization): CommunityCustomization {
  return {
    ...c,
    membersLabel: c.membersLabel?.trim() || null,
    welcomeMessage: c.welcomeMessage?.trim() || null,
    links: c.links
      .map((l) => ({ label: l.label.trim(), url: l.url.trim() }))
      .filter((l) => l.label && isValidLinkUrl(l.url)),
  };
}
