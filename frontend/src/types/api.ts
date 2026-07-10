export interface ApiResult<T = unknown> {
  code: number | string;
  message?: string;
  data?: T;
}

export interface LoginResponseData {
  token: string;
  identity: string;
  id: string;
}
