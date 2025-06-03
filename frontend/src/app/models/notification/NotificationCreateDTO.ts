import { NotificationType } from './NotificationType';

export interface NotificationCreateDTO {
  message: string;
  type: NotificationType;
  receiverProfileId: number;
  senderProfileId?: number; 
}
