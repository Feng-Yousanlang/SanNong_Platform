import { apiClient } from './client';
import { extractList, unwrapResponse } from './utils';

export async function fetchExperts() {
  const { data } = await apiClient.get('/api/experts/');
  return extractList(unwrapResponse(data));
}

export async function fetchExpertDetail(expertId) {
  const { data } = await apiClient.get(`/api/experts/${expertId}`);
  const payload = unwrapResponse(data);
  if (payload && typeof payload === 'object' && Array.isArray(payload.experts) && payload.experts[0]) {
    return payload.experts[0];
  }
  return payload;
}

export async function createAppointment(body) {
  const { data } = await apiClient.post('/api/expert-appointment/create', body);
  return data;
}

export async function fetchUserAppointments(userId) {
  const { data } = await apiClient.get('/api/expert-appointment/user/list', { params: { userId } });
  return unwrapResponse(data);
}

export async function cancelAppointment(body) {
  const { data } = await apiClient.post('/api/expert-appointment/cancel', body);
  return data;
}

export async function fetchPendingAppointments(expertId) {
  const { data } = await apiClient.get('/api/expert-appointment/pending', {
    params: { userId: expertId, page: 1, size: 100 },
  });
  return unwrapResponse(data);
}

export async function reviewAppointment(body) {
  const { data } = await apiClient.post('/api/expert-appointment/review', body);
  return data;
}

export async function fetchExpertSchedule(params) {
  const { data } = await apiClient.get('/api/expert-appointment/schedule', { params });
  return unwrapResponse(data);
}

export async function askExpert(body) {
  const { data } = await apiClient.post('/api/user/question', body);
  return data;
}

export async function answerQuestion(body) {
  const { data } = await apiClient.post('/api/expert/answer', body);
  return data;
}

export async function askAiExpert(body) {
  const { data } = await apiClient.post('/api/expert/ask/api', body);
  return data;
}
