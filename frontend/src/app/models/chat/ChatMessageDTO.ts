export interface ChatMessageDTO {
  id: number;
  content: string;
  sentAt: string;
  communityId: number;
  profileId: number;
  username: string;
  profileImage: string | null;
}
