import { CommunityProfileDTO } from '../communityProfile/CommunityProfileDTO';

export interface PostDTO {
  id: number;
  title: string;
  content: string;
  tags: string[];
  likes: number;
  comments: number;
  createdAt: string | null;
  communityProfile: CommunityProfileDTO;
  communityId: number;
}
