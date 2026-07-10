import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';
import type { UserIdentity } from '../types';

interface RoleGuardProps {
  allowed: UserIdentity[];
  children: ReactNode;
  fallback?: string;
}

export default function RoleGuard({ allowed, children, fallback = '/' }: RoleGuardProps) {
  const { identity } = useAuth();
  const id = String(identity || '');

  if (!allowed.includes(id as UserIdentity)) {
    return <Navigate to={fallback} replace />;
  }

  return children;
}
