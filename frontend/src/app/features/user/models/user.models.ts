export interface UpdateUserRequest {
  username?: string;
  email?: string;
  tags?: string[];
}

export interface UpdateUserTagsRequest {
  tags: string[];
}
