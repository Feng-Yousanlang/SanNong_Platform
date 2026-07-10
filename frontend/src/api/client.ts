import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  try {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  } catch {
    /* ignore storage errors */
  }

  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
  }

  return config;
});

export { API_BASE };
