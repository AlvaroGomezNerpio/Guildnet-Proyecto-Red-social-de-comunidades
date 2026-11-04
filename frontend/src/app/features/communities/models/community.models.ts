export interface CommunityResponseDTO {
  id: number;
  name: string;
  description: string;
  rules: string;
  image: string;
  banner: string;
  /** Paleta propia de la comunidad (#rrggbb). */
  primaryColor: string;
  accentColor: string;
  tags: string[];
  ownerId: number;
}

export interface CreateCommunityRequest {
  name: string;
  description: string;
  rules: string;
  tags: string[];
  primaryColor: string;
  accentColor: string;
}

export interface UpdateCommunityRequest {
  name: string;
  description: string;
  rules: string;
  tags: string[];
  primaryColor: string;
  accentColor: string;
}
