export interface TitleDTO {
  id: number;
  title: string;
  textColor: string;
  backgroundColor: string;
  communityId: number;
}

export interface CreateTitleRequest {
  title: string;
  textColor: string;
  backgroundColor: string;
  communityId: number;
}

export interface UpdateTitleRequest {
  title: string;
  textColor: string;
  backgroundColor: string;
}
