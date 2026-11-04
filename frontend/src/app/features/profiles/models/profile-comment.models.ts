import { CommunityProfileDTO } from './profile.models';

export interface ProfileCommentDTO {
  id: number;
  content: string;
  createdAt: string | null;
  updatedAt: string | null;
  authorProfile: CommunityProfileDTO;
  targetProfile: CommunityProfileDTO;
}

export interface ProfileCommentCreateUpdateDTO {
  content: string;
}
