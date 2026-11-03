import { CommunityProfileDTO } from '../../profiles/models/profile.models';

export interface PostCommentDTO {
  id: number;
  content: string;
  createdAt: string | null;
  communityProfile: CommunityProfileDTO;
}

export interface PostCommentCreateDTO {
  postId: number;
  content: string;
}

export interface PostCommentUpdateDTO {
  content: string;
}
