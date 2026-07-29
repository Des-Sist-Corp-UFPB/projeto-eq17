import { render, screen, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import CasaDetalhes from './CasaDetalhes';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

vi.mock('../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

describe('CasaDetalhes Component', () => {
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

  it('deve carregar e renderizar os detalhes da república', async () => {
    const mockCasaData = {
      casa: { id: 1, nome: 'República Primavera', endereco: 'Rua Principal, 100', criadoEm: '2026-01-01' },
      moradorLogado: { id: 1, papel: 'ADMINISTRADOR' },
      moradores: [
        { id: 1, usuarioId: 1, nome: 'João Silva', email: 'joao@email.com', papel: 'ADMINISTRADOR' }
      ],
      despesas: [],
      tarefas: [],
      notificacoes: [],
    };

    vi.mocked(api.get).mockImplementation((url) => {
      if (url.includes('/api/casas/1')) {
        return Promise.resolve(mockCasaData);
      }
      if (url === '/api/notificacoes') {
        return Promise.resolve([]);
      }
      return Promise.resolve({});
    });

    await act(async () => {
      render(
        <MemoryRouter initialEntries={['/casas/1']}>
          <Routes>
            <Route path="/casas/:id" element={<CasaDetalhes />} />
          </Routes>
        </MemoryRouter>
      );
    });

    await waitFor(() => {
      expect(screen.getByText('República Primavera')).toBeInTheDocument();
      expect(screen.getByText('Rua Principal, 100')).toBeInTheDocument();
    });
  });
});
