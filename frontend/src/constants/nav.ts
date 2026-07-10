import type {
  AppRoute,
  HomeFeaturesMap,
  RoleNavMap,
  RouteRolesMap,
  UserIdentity,
} from '../types';

/** 各角色可见路由；身份 1~5 见 types/auth.ts */
export const ROLE_NAV: RoleNavMap = {
  '1': [
    { path: '/', label: '首页' },
    { path: '/loans', label: '融资服务' },
    { path: '/appointment', label: '专家预约' },
    { path: '/products', label: '农产品管理' },
    { path: '/knowledge', label: '知识库' },
    { path: '/profile', label: '个人中心' },
  ],
  '2': [
    { path: '/', label: '首页' },
    { path: '/buy', label: '农产品商城' },
    { path: '/need', label: '求购平台' },
    { path: '/appointment', label: '专家预约' },
    { path: '/price', label: '物价预测' },
    { path: '/knowledge', label: '知识库' },
    { path: '/profile', label: '个人中心' },
  ],
  '3': [
    { path: '/', label: '首页' },
    { path: '/appointment', label: '预约管理' },
    { path: '/knowledge', label: '知识库' },
    { path: '/profile', label: '个人中心' },
  ],
  '4': [
    { path: '/', label: '首页' },
    { path: '/loans', label: '贷款审批' },
    { path: '/profile', label: '个人中心' },
  ],
  '5': [
    { path: '/', label: '首页' },
    { path: '/admin', label: '用户管理' },
    { path: '/knowledge', label: '知识库' },
    { path: '/profile', label: '个人中心' },
  ],
};

/** 路由 → 允许访问的角色 */
export const ROUTE_ROLES: RouteRolesMap = {
  '/': ['1', '2', '3', '4', '5'],
  '/loans': ['1', '4'],
  '/appointment': ['1', '2', '3'],
  '/buy': ['2'],
  '/knowledge': ['1', '2', '3', '5'],
  '/products': ['1'],
  '/need': ['2'],
  '/price': ['2'],
  '/profile': ['1', '2', '3', '4', '5'],
  '/admin': ['5'],
};

export function filterNavByIdentity(identity: string) {
  const id = (String(identity || '1') as UserIdentity);
  return ROLE_NAV[id] || ROLE_NAV['1'];
}

export function canAccessRoute(identity: string, path: string) {
  const id = String(identity || '');
  const allowed = ROUTE_ROLES[path as AppRoute];
  if (!allowed) return true;
  return allowed.includes(id as UserIdentity);
}

export const HOME_FEATURES: HomeFeaturesMap = {
  '1': [
    { title: '融资服务', desc: '申请农业贷款、查看还款计划', path: '/loans', accent: 'gold' },
    { title: '专家预约', desc: '预约农业专家在线咨询', path: '/appointment', accent: 'forest' },
    { title: '农产品管理', desc: '发布与管理自家农产品', path: '/products', accent: 'earth' },
    { title: '知识库', desc: '学习农业种植与经营知识', path: '/knowledge', accent: 'sky' },
  ],
  '2': [
    { title: '农产品商城', desc: '选购新鲜农产品', path: '/buy', accent: 'mint' },
    { title: '求购平台', desc: '发布求购需求', path: '/need', accent: 'amber' },
    { title: '专家预约', desc: '获取专业农业指导', path: '/appointment', accent: 'forest' },
    { title: '物价预测', desc: '查看菜价走势预测', path: '/price', accent: 'gold' },
  ],
  '3': [
    { title: '预约管理', desc: '审核用户预约、查看日程', path: '/appointment', accent: 'forest' },
    { title: '知识库', desc: '分享农业专业知识', path: '/knowledge', accent: 'sky' },
  ],
  '4': [
    { title: '贷款审批', desc: '审核农户贷款申请', path: '/loans', accent: 'gold' },
    { title: '产品管理', desc: '管理贷款金融产品', path: '/loans', accent: 'earth' },
  ],
  '5': [
    { title: '用户管理', desc: '管理平台用户与权限', path: '/admin', accent: 'amber' },
    { title: '知识库', desc: '维护平台知识内容', path: '/knowledge', accent: 'sky' },
  ],
};

export const PRICE_PRODUCTS = [
  '白菜',
  '萝卜',
  '土豆',
  '西红柿',
  '黄瓜',
  '茄子',
  '青椒',
  '洋葱',
  '大蒜',
  '生姜',
] as const;

export type PriceProduct = (typeof PRICE_PRODUCTS)[number];
