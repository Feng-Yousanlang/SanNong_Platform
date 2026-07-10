import type { UserIdentity } from './auth';

export interface NavItem {
  path: string;
  label: string;
}

export type FeatureAccent = 'forest' | 'gold' | 'earth' | 'sky' | 'mint' | 'amber';

export interface HomeFeature {
  title: string;
  desc: string;
  path: string;
  accent: FeatureAccent;
}

export type AppRoute =
  | '/'
  | '/loans'
  | '/appointment'
  | '/buy'
  | '/knowledge'
  | '/products'
  | '/need'
  | '/price'
  | '/profile'
  | '/admin';

export type RoleNavMap = Record<UserIdentity, NavItem[]>;
export type RouteRolesMap = Partial<Record<AppRoute, UserIdentity[]>>;
export type HomeFeaturesMap = Record<UserIdentity, HomeFeature[]>;
