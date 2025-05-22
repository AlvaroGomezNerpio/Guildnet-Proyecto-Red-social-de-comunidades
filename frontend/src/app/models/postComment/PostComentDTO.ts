import { CommunityProfileDTO } from "../communityProfile/CommunityProfileDTO";

export interface PostComentDTO {
  id: number;
  content: string;
  communityProfile: CommunityProfileDTO;
}
