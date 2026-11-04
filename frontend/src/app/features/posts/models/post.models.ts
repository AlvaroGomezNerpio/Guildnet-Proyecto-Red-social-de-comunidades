import { CommunityProfileDTO } from '../../profiles/models/profile.models';
import { PostCommentDTO } from './post-comment.models';

/** Categoría oficial de publicaciones de una comunidad ("Guía", "Pregunta"...). */
export interface PostCategoryDTO {
  id: number;
  name: string;
  textColor: string;
  backgroundColor: string;
  communityId: number;
}

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
  category: PostCategoryDTO | null;
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
  category: PostCategoryDTO | null;
}

export interface PostCreateDTO {
  title: string;
  content: string;
  tags: string[];
  communityId: number;
  categoryId: number | null;
}

export interface PostUpdateDTO {
  title: string;
  content: string;
  tags: string[];
  categoryId: number | null;
}
