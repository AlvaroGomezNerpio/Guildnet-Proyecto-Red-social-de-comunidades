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
  /** Fijada en lo alto de la comunidad. */
  pinned: boolean;
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
  pinned: boolean;
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
