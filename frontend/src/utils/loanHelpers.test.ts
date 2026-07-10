import { describe, expect, it } from 'vitest';
import {
  formatAmountRange,
  formatLoanRate,
  formatLoanTerm,
  getLoanProductId,
  getLoanProductName,
  parseLoanTags,
  resolveApplicationId,
} from './loanHelpers';

describe('loanHelpers', () => {
  it('resolves product id and name from mixed backend fields', () => {
    expect(getLoanProductId({ fpId: 3 })).toBe(3);
    expect(getLoanProductId({ productId: 8 })).toBe(8);
    expect(getLoanProductName({ productName: '春耕贷' })).toBe('春耕贷');
    expect(getLoanProductName({})).toBe('未命名产品');
  });

  it('formats amount range and rate', () => {
    expect(formatAmountRange({ minAmount: 1000, maxAmount: 50000 })).toContain('1,000');
    expect(formatAmountRange({ maxAmount: 50000 })).toContain('最高');
    expect(formatAmountRange({ minAmount: 1000 })).toContain('最低');
    expect(formatAmountRange({})).toBe('—');
    expect(formatLoanRate({ annualRate: 4.5 })).toBe('4.5%');
    expect(formatLoanTerm({ term: 12 })).toBe('12 月');
  });

  it('parses comma separated tags and application ids', () => {
    expect(parseLoanTags({ tags: '低息,农户专享' })).toEqual(['低息', '农户专享']);
    expect(parseLoanTags({ tagList: ['A', 'B'] })).toEqual(['A', 'B']);
    expect(resolveApplicationId({ loanApplicationId: 99 })).toBe(99);
  });
});
