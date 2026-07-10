import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import DetailLine from './DetailLine';

describe('DetailLine', () => {
  it('renders label and value', () => {
    render(<DetailLine label="产地" value="山东" />);
    expect(screen.getByText('产地')).toBeInTheDocument();
    expect(screen.getByText('山东')).toBeInTheDocument();
  });

  it('shows dash when value is missing', () => {
    render(<DetailLine label="备注" value={null} />);
    expect(screen.getByText('—')).toBeInTheDocument();
  });
});
