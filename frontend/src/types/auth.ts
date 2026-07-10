export type UserIdentity = '1' | '2' | '3' | '4' | '5';

export type RegisterIdentity = '1' | '2';
export interface AuthState {
  token: string;
  identity: UserIdentity | '';
  userId: string;
  username: string;
}

export interface LoginParseResult {
  isSuccess: boolean;
  tokenValue: string;
  identity: string | undefined;
  userId: string | undefined;
  message?: string;
}

export interface RegisterPayload {
  username: string;
  name: string;
  password: string;
  passwordConfirm: string;
  identity: RegisterIdentity;
}

export interface AuthContextValue extends AuthState {
  isAuthenticated: boolean;
  roleLabel: string;
  loginFromResponse: (json: unknown, username?: string) => LoginParseResult;
  logout: () => void;
}
