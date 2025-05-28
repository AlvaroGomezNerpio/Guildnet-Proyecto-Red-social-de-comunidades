import { RoleDTO } from '../role/RoleDTO';
import { TitleDTO } from '../title/TitleDTO';

export interface CommunityProfileDTO {
  id: number;
  username: string;
  description: string | null;
  profileImage: string | null;
  featuredTitle: TitleDTO | null;
  userId: number;
  communityId: number;
  roles: RoleDTO[];
  titles: TitleDTO[];
}
