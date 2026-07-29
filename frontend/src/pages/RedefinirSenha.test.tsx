import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import RedefinirSenha from './RedefinirSenha';
import { api } from '../services/api';

vi.mock('../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('RedefinirSenha Page Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve exibir mensagem de token inválido se nenhum token for fornecido na URL', async () => {
    render(
      <MemoryRouter initialEntries={['/redefinir-senha']}>
        <RedefinirSenha />
      </MemoryRouter>
    );

    expect(await screen.findByText('Link inválido ou expirado!')).toBeInTheDocument();
  });

  it('deve validar o token via API e exibir o formulário se o token for válido', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ valido: true });

    render(
      <MemoryRouter initialEntries={['/redefinir-senha?token=token123']}>
        <RedefinirSenha />
      </MemoryRouter>
    );

    expect(await screen.findByPlaceholderText('Mínimo 6 caracteres')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Repita a nova senha')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/api/auth/validar-token-redefinicao?token=token123');
  });

  it('deve validar se as senhas coincidem antes de submeter', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ valido: true });

    render(
      <MemoryRouter initialEntries={['/redefinir-senha?token=token123']}>
        <RedefinirSenha />
      </MemoryRouter>
    );

    await screen.findByPlaceholderText('Mínimo 6 caracteres');

    fireEvent.change(screen.getByPlaceholderText('Mínimo 6 caracteres'), {
      target: { value: 'senha123' },
    });
    fireEvent.change(screen.getByPlaceholderText('Repita a nova senha'), {
      target: { value: 'diferente' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Redefinir Senha/i }));

    expect(await screen.findByText('As senhas digitadas não coincidem.')).toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();
  });

  it('deve enviar a nova senha com sucesso', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ valido: true });
    vi.mocked(api.post).mockResolvedValueOnce({ mensagem: 'Senha redefinida com sucesso!' });

    render(
      <MemoryRouter initialEntries={['/redefinir-senha?token=token123']}>
        <RedefinirSenha />
      </MemoryRouter>
    );

    await screen.findByPlaceholderText('Mínimo 6 caracteres');

    fireEvent.change(screen.getByPlaceholderText('Mínimo 6 caracteres'), {
      target: { value: 'novaSenha123' },
    });
    fireEvent.change(screen.getByPlaceholderText('Repita a nova senha'), {
      target: { value: 'novaSenha123' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Redefinir Senha/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/auth/redefinir-senha', {
        token: 'token123',
        novaSenha: 'novaSenha123',
      });
      expect(screen.getByText('Senha redefinida com sucesso!')).toBeInTheDocument();
    });
  });
});
