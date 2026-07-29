import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import IaAssistant from './IaAssistant';
import { api } from '../services/api';

vi.mock('../services/api', () => ({
  api: {
    post: vi.fn(),
  },
}));

describe('IaAssistant Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve abrir o chat da IA ao clicar no botão e enviar mensagens', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({
      resposta: 'Sua conta de energia foi lançada com sucesso.',
    });

    render(<IaAssistant casaId={1} />);

    const openButton = screen.getByTitle('Conversar com assistente de IA');
    fireEvent.click(openButton);

    const input = screen.getByPlaceholderText('Pergunte ao assistente da república...');
    expect(input).toBeInTheDocument();

    fireEvent.change(input, {
      target: { value: 'Lançar luz 100 reais' },
    });

    const form = input.closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/chat', {
        mensagem: 'Lançar luz 100 reais',
      });
      expect(screen.getByText('Sua conta de energia foi lançada com sucesso.')).toBeInTheDocument();
    });
  });
});
