export interface CommunityResponseDTO {
  id: number;
  name: string;
  description: string;
  rules: string;
  image: string;
  banner: string;
  tags: string[];
  ownerId: number;
}

export interface CreateCommunityRequest {
  name: string;
  description: string;
  rules: string;
  tags: string[];
}

export interface UpdateCommunityRequest {  name: string;
  description: string;
  rules: string;
  tags: string[];
}
