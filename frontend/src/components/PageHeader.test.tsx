import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import PageHeader from './PageHeader';

describe('PageHeader', () => {
  it('renders title and subtitle', () => {
    render(<PageHeader title="农产品商城" subtitle="选购新鲜农产品" />);
    expect(screen.getByRole('heading', { name: '农产品商城' })).toBeInTheDocument();
    expect(screen.getByText('选购新鲜农产品')).toBeInTheDocument();
  });

  it('renders optional actions', () => {
    render(
      <PageHeader
        title="管理"
        actions={<button type="button">新增</button>}
      />,
    );
    expect(screen.getByRole('button', { name: '新增' })).toBeInTheDocument();
  });
});
