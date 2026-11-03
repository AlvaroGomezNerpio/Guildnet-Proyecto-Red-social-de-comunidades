import { CommunityProfileDTO } from '../communityProfile/CommunityProfileDTO';

export interface PostCommentDTO {
  id: number;
  content: string;
  createdAt: string | null;
  communityProfile: CommunityProfileDTO;
}
