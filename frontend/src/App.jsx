import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import RoleGuard from './components/RoleGuard';
import Layout from './components/Layout';
import Login from './pages/Login';
import Home from './pages/Home';
import Loans from './pages/Loans';
import Appointment from './pages/Appointment';
import Buy from './pages/Buy';
import Knowledge from './pages/Knowledge';
import Products from './pages/Products';
import Need from './pages/Need';
import Price from './pages/Price';
import Profile from './pages/Profile';
import Admin from './pages/Admin';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Home />} />
            <Route
              path="/loans"
              element={
                <RoleGuard allowed={['1', '4']}>
                  <Loans />
                </RoleGuard>
              }
            />
            <Route
              path="/appointment"
              element={
                <RoleGuard allowed={['1', '2', '3']}>
                  <Appointment />
                </RoleGuard>
              }
            />
            <Route
              path="/buy"
              element={
                <RoleGuard allowed={['2']}>
                  <Buy />
                </RoleGuard>
              }
            />
            <Route
              path="/knowledge"
              element={
                <RoleGuard allowed={['1', '2', '3', '5']}>
                  <Knowledge />
                </RoleGuard>
              }
            />
            <Route
              path="/products"
              element={
                <RoleGuard allowed={['1']}>
                  <Products />
                </RoleGuard>
              }
            />
            <Route
              path="/need"
              element={
                <RoleGuard allowed={['2']}>
                  <Need />
                </RoleGuard>
              }
            />
            <Route
              path="/price"
              element={
                <RoleGuard allowed={['2']}>
                  <Price />
                </RoleGuard>
              }
            />
            <Route path="/profile" element={<Profile />} />
            <Route
              path="/admin"
              element={
                <RoleGuard allowed={['5']}>
                  <Admin />
                </RoleGuard>
              }
            />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
