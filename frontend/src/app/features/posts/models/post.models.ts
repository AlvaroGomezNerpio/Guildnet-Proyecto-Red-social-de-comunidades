import { CommunityProfileDTO } from '../../profiles/models/profile.models';
import { PostCommentDTO } from './post-comment.models';

export interface PostDTO {
  id: number;
  title: string;
  content: string;
  tags: string[];
  likes: number;
  comments: number;
  createdAt: string | null;
  updatedAt: string | null;
  communityProfile: CommunityProfileDTO;
  communityId: number;
}

export interface PostDetailDTO {
  id: number;
  title: string;
  content: string;
  tags: string[];
  createdAt: string | null;
  updatedAt: string | null;
  postComment: PostCommentDTO[];
  communityProfile: CommunityProfileDTO;
  communityId: number;
}

export interface PostCreateDTO {
  title: string;
  content: string;
  tags: string[];
  communityId: number;
}

export interface PostUpdateDTO {
  title: string;
  content: string;
  tags: string[];
}
