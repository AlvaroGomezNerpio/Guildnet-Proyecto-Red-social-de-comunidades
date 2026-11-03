export interface UserDTO {
  id: number;
  username: string;
  email: string;
  profileImage: string;
  tags: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
