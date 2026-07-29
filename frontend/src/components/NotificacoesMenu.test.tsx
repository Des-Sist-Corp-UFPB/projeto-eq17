import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import NotificacoesMenu from './NotificacoesMenu';
import { api } from '../services/api';

vi.mock('../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('NotificacoesMenu Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve carregar e renderizar as notificações do usuário', async () => {
    const mockNotificacoes = [
      {
        id: 1,
        titulo: 'Nova Despesa',
        mensagem: 'Luz lançada',
        lida: false,
        criadoEm: '2026-07-29T10:00:00Z',
      },
    ];

    vi.mocked(api.get).mockResolvedValueOnce(mockNotificacoes);

    render(<NotificacoesMenu />);

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/api/notificacoes');
    });

    const bellButton = screen.getByRole('button');
    fireEvent.click(bellButton);

    expect(screen.getByText('Nova Despesa')).toBeInTheDocument();
    expect(screen.getByText('Luz lançada')).toBeInTheDocument();
  });
});
