import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import EsqueceuSenha from './EsqueceuSenha';
import { api } from '../services/api';

vi.mock('../services/api', () => ({
  api: {
    post: vi.fn(),
  },
}));

describe('EsqueceuSenha Page Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve renderizar o título e o campo de e-mail', () => {
    render(
      <MemoryRouter>
        <EsqueceuSenha />
      </MemoryRouter>
    );

    expect(screen.getByText(/Recuperação de Acesso/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText('exemplo@email.com')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Enviar link de redefinição/i })).toBeInTheDocument();
  });

  it('deve submeter a solicitação de redefinição de senha com sucesso', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({
      mensagem: 'Se o e-mail estiver cadastrado, você receberá as instruções.',
    });

    render(
      <MemoryRouter>
        <EsqueceuSenha />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('exemplo@email.com'), {
      target: { value: 'usuario@email.com' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Enviar link de redefinição/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/auth/esqueceu-senha', {
        email: 'usuario@email.com',
      });
      expect(screen.getByText('Solicitação enviada!')).toBeInTheDocument();
      expect(screen.getByText('Se o e-mail estiver cadastrado, você receberá as instruções.')).toBeInTheDocument();
    });
  });

  it('deve exibir mensagem de erro em caso de falha na requisição da API', async () => {
    vi.mocked(api.post).mockRejectedValueOnce(new Error('Erro de conexão'));

    render(
      <MemoryRouter>
        <EsqueceuSenha />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('exemplo@email.com'), {
      target: { value: 'usuario@email.com' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Enviar link de redefinição/i }));

    expect(await screen.findByText('Erro de conexão')).toBeInTheDocument();
  });
});
