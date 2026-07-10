import type { UserIdentity } from '../types';

export const ROLE_LABELS: Record<UserIdentity, string> = {
  '1': '农户',
  '2': '买家',
  '3': '专家',
  '4': '银行工作人员',
  '5': '平台管理员',
};

export const ROLE_OPTIONS: { value: UserIdentity; label: string }[] = [
  { value: '1', label: '农户' },
  { value: '2', label: '买家' },
  { value: '3', label: '专家' },
  { value: '4', label: '银行工作人员' },
  { value: '5', label: '平台管理员' },
];

export const REGISTER_ROLE_OPTIONS: { value: '1' | '2'; label: string }[] = [  { value: '1', label: '农户' },
  { value: '2', label: '买家' },
];

export function isUserIdentity(value: string): value is UserIdentity {
  return value === '1' || value === '2' || value === '3' || value === '4' || value === '5';
}

export function getRoleLabel(identity: string): string {
  return isUserIdentity(identity) ? ROLE_LABELS[identity] : '未知角色';
}
