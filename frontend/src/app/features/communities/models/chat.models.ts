export interface ChatMessageDTO {
  id: number;
  content: string;
  sentAt: string;
  communityId: number;
  channelId: number;
  profileId: number;
  username: string;
  profileImage: string | null;
}

/** Canal de chat de una comunidad. Los crea la propia comunidad (no hay canales por defecto). */
export interface ChatChannelDTO {
  id: number;
  name: string;
  description: string | null;
  communityId: number;
  /** Mensajes nuevos para el usuario (sin contar los suyos). */
  unreadCount: number;
}

export interface ChatChannelRequest {
  name: string;
  description: string | null;
}
