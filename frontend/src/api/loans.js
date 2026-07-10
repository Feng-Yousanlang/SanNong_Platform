import { apiClient } from './client';
import { unwrapResponse } from './utils';

export async function fetchLoanProducts() {
  const { data } = await apiClient.get('/api/loan/products');
  return unwrapResponse(data);
}

export async function applyLoan(formData) {
  const { data } = await apiClient.post('/api/loan/apply', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function fetchApplications(userId) {
  const { data } = await apiClient.get('/api/loan/applications', { params: { userId } });
  return unwrapResponse(data);
}

export async function fetchRepaymentPlan(userId) {
  const { data } = await apiClient.get('/api/loan/repayment-plan', { params: { userId } });
  return unwrapResponse(data);
}

export async function fetchRepayments(userId) {
  const { data } = await apiClient.get('/api/loan/repayments', { params: { userId } });
  return unwrapResponse(data);
}

export async function submitRepay(body) {
  const { data } = await apiClient.post('/api/loan/repay', body);
  return data;
}

export async function fetchPendingLoans() {
  const { data } = await apiClient.get('/api/loan/pending');
  return unwrapResponse(data);
}

export async function approveLoan(body) {
  const { data } = await apiClient.post('/api/loan/approve', body);
  return data;
}

export async function fetchBankApprovals(userId) {
  const { data } = await apiClient.get('/api/loan/approvals', { params: { userId } });
  return unwrapResponse(data);
}

export async function createLoanProduct(body) {
  const { data } = await apiClient.post('/api/loan/products', body);
  return data;
}

export async function updateLoanProduct(productId, body) {
  const { data } = await apiClient.put(`/api/loan/products/${productId}`, body);
  return data;
}

export async function deleteLoanProduct(productId) {
  const { data } = await apiClient.delete(`/api/loan/products/${productId}`);
  return data;
}

export async function fetchLoanStatuses() {
  const { data } = await apiClient.get('/api/loan/status');
  return unwrapResponse(data);
}
