import { describe, expect, it } from 'vitest';
import { canAccessRoute, filterNavByIdentity, HOME_FEATURES } from './nav';

describe('role navigation', () => {
  it('farmer should not access buyer marketplace', () => {
    expect(canAccessRoute('1', '/buy')).toBe(false);
    expect(canAccessRoute('1', '/products')).toBe(true);
  });

  it('buyer should access need and price pages', () => {
    expect(canAccessRoute('2', '/need')).toBe(true);
    expect(canAccessRoute('2', '/price')).toBe(true);
    expect(canAccessRoute('2', '/products')).toBe(false);
  });

  it('expert should only access appointment and knowledge among business routes', () => {
    expect(canAccessRoute('3', '/appointment')).toBe(true);
    expect(canAccessRoute('3', '/knowledge')).toBe(true);
    expect(canAccessRoute('3', '/loans')).toBe(false);
    expect(canAccessRoute('3', '/admin')).toBe(false);
  });

  it('bank staff should access loans but not products', () => {
    expect(canAccessRoute('4', '/loans')).toBe(true);
    expect(canAccessRoute('4', '/buy')).toBe(false);
  });

  it('admin should access admin page only among restricted routes', () => {
    expect(canAccessRoute('5', '/admin')).toBe(true);
    expect(canAccessRoute('5', '/buy')).toBe(false);
    expect(canAccessRoute('5', '/knowledge')).toBe(true);
  });

  it('filterNavByIdentity should hide buyer-only menu for farmer', () => {
    const items = filterNavByIdentity('1');
    const paths = items.map((item) => item.path);

    expect(paths).toContain('/products');
    expect(paths).not.toContain('/buy');
    expect(paths).not.toContain('/need');
  });

  it('HOME_FEATURES should provide role-specific entry cards', () => {
    expect(HOME_FEATURES['1'].some((item) => item.path === '/products')).toBe(true);
    expect(HOME_FEATURES['2'].some((item) => item.path === '/buy')).toBe(true);
    expect(HOME_FEATURES['5'].some((item) => item.path === '/admin')).toBe(true);
  });
});
