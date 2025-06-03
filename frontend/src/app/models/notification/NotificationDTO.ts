import { NotificationType } from './NotificationType';
import { CommunityProfileDTO } from '../communityProfile/CommunityProfileDTO';

export interface NotificationDTO {
  id: number;
  message: string;
  type: NotificationType;
  read: boolean;
  createdAt: string;
  receiver: CommunityProfileDTO;
  sender: CommunityProfileDTO | null;
}
