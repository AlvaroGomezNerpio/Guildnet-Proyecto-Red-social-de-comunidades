import { CommunityProfileDTO } from '../communityProfile/CommunityProfileDTO';

export interface ProfileCommentDTO {
  id: number;
  content: string;
  authorProfile: CommunityProfileDTO;
  targetProfile: CommunityProfileDTO;
}
