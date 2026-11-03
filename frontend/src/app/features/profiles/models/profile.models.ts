import { RoleDTO } from '../../roles/models/role.models';
import { TitleDTO } from './title.models';

export interface CommunityProfileDTO {
  id: number;
  username: string;
  description: string | null;
  profileImage: string | null;
  featuredTitle: TitleDTO | null;
  userId: number;
  communityId: number;
  rol: RoleDTO;
  titles: TitleDTO[];
}

export interface UpdateCommunityProfileRequest {
  userName: string;
  description: string;
}
