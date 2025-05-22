import { CommunityProfileDTO } from "../communityProfile/CommunityProfileDTO";

export interface PostDTO {
  id: number;
  title: string;
  content: string;
  tags: string[];
  likes: number;
  coments: number;
  communityProfile: CommunityProfileDTO;
  communityId: number;
}
