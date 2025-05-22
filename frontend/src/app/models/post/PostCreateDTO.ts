export interface PostCreateDTO {
  title: string;
  content: string;
  tags: string[];
  profileId: number;
  communityId: number;
}
