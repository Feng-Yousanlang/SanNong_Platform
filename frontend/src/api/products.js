import { apiClient } from './client';
import { extractList, unwrapResponse } from './utils';

export async function fetchBuyerProducts(nums = 20) {
  const { data } = await apiClient.get('/api/products/buyer', { params: { nums } });
  return extractList(unwrapResponse(data));
}

export async function fetchBuyerProductDetail(productId) {
  const { data } = await apiClient.get(`/api/products/buyer/${productId}`);
  return unwrapResponse(data);
}

export async function addToCart(body) {
  const payload = {
    userId: body.userId,
    productId: body.productId,
    amount: body.amount ?? body.quantity,
  };
  const { data } = await apiClient.post('/api/products/buyer/shop', payload);
  return data;
}

export async function fetchCart(userId) {
  const { data } = await apiClient.get('/api/products/buyer/showshop', { params: { userId } });
  return unwrapResponse(data);
}

export async function removeFromCart(body) {
  const { data } = await apiClient.delete('/api/products/buyer/shop/delete', { data: { cartId: body.cartId } });
  return data;
}

export async function checkoutCartItem(body) {
  const { data } = await apiClient.post('/api/products/buyer/buyshop', body);
  return data;
}

export async function fetchSavedAddress(userId) {
  const { data } = await apiClient.get('/api/products/buyer/getSavedAddress', { params: { userId } });
  return unwrapResponse(data);
}

export async function purchaseProduct(body) {
  const payload = {
    userId: body.userId,
    productId: body.productId,
    amount: body.amount ?? body.quantity,
    getAddress: body.getAddress ?? body.address,
  };
  const { data } = await apiClient.post('/api/products/buyer/purchase', payload);
  return data;
}

export async function fetchPurchaseHistory(userId) {
  const { data } = await apiClient.get('/api/products/buyer/showPurchase', { params: { userId } });
  return unwrapResponse(data);
}

export async function receiveProduct(body) {
  const { data } = await apiClient.post('/api/products/buyer/receiveProduct', body);
  return data;
}

export async function cancelPurchase(body) {
  const { data } = await apiClient.post('/api/products/buyer/cancelPurchase', body);
  return data;
}

export async function fetchPriceForecast(productName) {
  const { data } = await apiClient.get('/api/products/forecast', { params: { productName } });
  return unwrapResponse(data);
}

export async function fetchFarmerProducts(userId) {
  const { data } = await apiClient.get('/api/products/farmer/getMyProducts', { params: { userId } });
  return unwrapResponse(data);
}

export async function createFarmerProduct(formData) {
  const { data } = await apiClient.post('/api/products/farmer/newProduct', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function deleteFarmerProduct(body) {
  const { data } = await apiClient.post('/api/products/farmer/deleteProduct', body);
  return data;
}

export async function fetchFarmerPendingOrders(userId) {
  const { data } = await apiClient.get('/api/products/farmer/showAllPurchase', { params: { userId } });
  return unwrapResponse(data);
}

export async function sendProduct(body) {
  const { data } = await apiClient.post('/api/products/farmer/sendProduct', body);
  return data;
}

export async function fetchSoldOutProducts(userId) {
  const { data } = await apiClient.get('/api/products/farmer/soldout', { params: { userId } });
  return unwrapResponse(data);
}
