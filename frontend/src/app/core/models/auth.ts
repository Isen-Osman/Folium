export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthUser {
  id: number;
  name: string | null;
  surname: string | null;
  email: string;
}

export interface LoginResponse extends AuthUser {
  accessToken: string;
  tokenType: string;
}

export interface RegisterRequest {
  name: string;
  surname: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface RegisterResponse {
  id: number;
  name: string;
  surname: string;
  email: string;
}
