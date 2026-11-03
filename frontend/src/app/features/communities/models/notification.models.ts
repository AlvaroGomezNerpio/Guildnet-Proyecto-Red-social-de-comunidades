import { CommunityProfileDTO } from '../../profiles/models/profile.models';

export enum NotificationType {
  COMMENT_PROFILE = 'COMMENT_PROFILE',
  COMMENT_POST = 'COMMENT_POST',
  LIKE_POST = 'LIKE_POST',
  SYSTEM = 'SYSTEM',
  WELCOME = 'WELCOME',
  ACHIEVEMENT_UNLOCKED = 'ACHIEVEMENT_UNLOCKED',
  TITLE_ASSIGNED = 'TITLE_ASSIGNED',
  TITLE_REMOVED = 'TITLE_REMOVED',
  ROLE_ASSIGNED = 'ROLE_ASSIGNED',
  ROLE_REMOVED = 'ROLE_REMOVED'
}

export interface NotificationDTO {
  id: number;
  message: string;
  type: NotificationType;
  read: boolean;
  createdAt: string;
  receiver: CommunityProfileDTO;
  sender: CommunityProfileDTO | null;
}
