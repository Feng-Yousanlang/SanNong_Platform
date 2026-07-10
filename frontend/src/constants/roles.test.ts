import { describe, expect, it } from 'vitest';
import { getRoleLabel, isUserIdentity, REGISTER_ROLE_OPTIONS, ROLE_LABELS } from './roles';

describe('roles', () => {
  it('isUserIdentity should validate platform roles', () => {
    expect(isUserIdentity('1')).toBe(true);
    expect(isUserIdentity('5')).toBe(true);
    expect(isUserIdentity('9')).toBe(false);
    expect(isUserIdentity('')).toBe(false);
  });

  it('getRoleLabel should map known identities', () => {
    expect(getRoleLabel('3')).toBe('专家');
    expect(getRoleLabel('4')).toBe('银行工作人员');
    expect(getRoleLabel('unknown')).toBe('未知角色');
  });

  it('ROLE_LABELS should cover all five identities', () => {
    expect(Object.keys(ROLE_LABELS)).toEqual(['1', '2', '3', '4', '5']);
  });

  it('REGISTER_ROLE_OPTIONS should only expose farmer and buyer', () => {
    expect(REGISTER_ROLE_OPTIONS.map((item) => item.value)).toEqual(['1', '2']);
  });
});
