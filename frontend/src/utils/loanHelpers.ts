type LoanProductLike = Record<string, unknown>;

export function getLoanProductId(product: LoanProductLike = {}): string | number | null {
  const id = product.fpId ?? product.productId ?? product.id;
  return id != null ? (id as string | number) : null;
}

export function getLoanProductName(product: LoanProductLike = {}): string {
  return String(product.fpName ?? product.productName ?? product.name ?? '未命名产品');
}

export function formatAmountRange(product: LoanProductLike = {}): string {
  const min = product.minAmount ?? product.min_amount;
  const max = product.maxAmount ?? product.max_amount;
  if (min != null && max != null) {
    return `${Number(min).toLocaleString('zh-CN')} ~ ${Number(max).toLocaleString('zh-CN')} 元`;
  }
  if (max != null) return `最高 ${Number(max).toLocaleString('zh-CN')} 元`;
  if (min != null) return `最低 ${Number(min).toLocaleString('zh-CN')} 元`;
  return '—';
}

export function formatLoanRate(product: LoanProductLike = {}): string {
  const rate = product.annualRate ?? product.interestRate ?? product.rate;
  return rate != null ? `${rate}%` : '—';
}

export function formatLoanTerm(product: LoanProductLike = {}): string {
  const term = product.term ?? product.loanTerm;
  return term != null ? `${term} 月` : '—';
}

export function parseLoanTags(product: LoanProductLike = {}): string[] {
  const raw = product.tags ?? product.tagList;
  if (!raw) return [];
  if (Array.isArray(raw)) return raw.filter(Boolean).map(String);
  return String(raw)
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean);
}

export function resolveApplicationId(item: LoanProductLike = {}): string | number | null {
  const id = item.applicationId ?? item.loanApplicationId ?? item.id;
  return id != null ? (id as string | number) : null;
}
