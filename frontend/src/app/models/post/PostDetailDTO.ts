import { CommunityProfileDTO } from '../communityProfile/CommunityProfileDTO';
import { PostCommentDTO } from '../postComment/PostCommentDTO';

export interface PostDetailDTO {
  id: number;
  title: string;
  content: string;
  tags: string[];
  createdAt: string | null;
  postComment: PostCommentDTO[];
  communityProfile: CommunityProfileDTO;
  communityId: number;
}
