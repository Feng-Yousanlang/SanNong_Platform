import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import Modal from './Modal';

describe('Modal', () => {
  it('renders nothing when closed', () => {
    const { container } = render(
      <Modal open={false} title="标题" onClose={() => {}} footer={undefined} wide={false}>
        内容
      </Modal>,
    );
    expect(container).toBeEmptyDOMElement();
  });

  it('renders title, body and footer when open', () => {
    render(
      <Modal open title="详情" onClose={() => {}} footer={<button type="button">确定</button>} wide={false}>
        正文
      </Modal>,
    );
    expect(screen.getByRole('dialog', { name: '详情' })).toBeInTheDocument();
    expect(screen.getByText('正文')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '确定' })).toBeInTheDocument();
  });

  it('calls onClose when backdrop is clicked', () => {
    const onClose = vi.fn();
    render(
      <Modal open title="关闭测试" onClose={onClose} footer={undefined} wide={false}>
        内容
      </Modal>,
    );
    fireEvent.click(screen.getByRole('presentation'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
