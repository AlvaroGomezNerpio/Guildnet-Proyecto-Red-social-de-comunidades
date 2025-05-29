import { PostComentDTO } from "../postComment/PostComentDTO";
import { CommunityProfileDTO } from "../communityProfile/CommunityProfileDTO";

export interface PostDetailDTO {
  id: number;
  title: string;
  content: string;
  tags: string[];
  postComment: PostComentDTO[];
  communityProfile: CommunityProfileDTO;
  communityId: number;
}
