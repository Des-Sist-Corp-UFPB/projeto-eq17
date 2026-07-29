import { render, screen, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Dashboard from './Dashboard';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

vi.mock('../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.mocked(useAuth).mockReturnValue({
      user: { id: 1, nome: 'João Silva', email: 'joao@email.com' },
      isAuthenticated: true,
      isLoading: false,
      isInitialLoading: false,
      error: null,
      clearError: vi.fn(),
      login: vi.fn(),
      logout: vi.fn(),
      register: vi.fn(),
    });
  });

  it('deve carregar e exibir as casas do usuário logado', async () => {
    vi.mocked(api.get).mockImplementation((url) => {
      if (url === '/api/dashboard') {
        return Promise.resolve({
          usuarioLogado: { id: 1, nome: 'João Silva', email: 'joao@email.com' },
          casas: [
            { id: 10, nome: 'República Central', endereco: 'Rua das Flores, 123', criadoEm: '2026-01-01' },
          ],
        });
      }
      if (url === '/api/notificacoes') {
        return Promise.resolve([]);
      }
      return Promise.resolve({});
    });

    await act(async () => {
      render(
        <MemoryRouter>
          <Dashboard />
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(screen.getByText('República Central')).toBeInTheDocument();
      expect(screen.getByText('Rua das Flores, 123')).toBeInTheDocument();
    });
  });
});
