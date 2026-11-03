import { CommunityProfileDTO } from '../communityProfile/CommunityProfileDTO';

export interface ProfileCommentDTO {
  id: number;
  content: string;
  createdAt: string | null;
  authorProfile: CommunityProfileDTO;
  targetProfile: CommunityProfileDTO;
}
