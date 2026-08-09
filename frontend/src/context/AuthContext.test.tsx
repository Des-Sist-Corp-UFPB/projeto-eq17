import { render, screen, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext';
import { api } from '../services/api';

vi.mock('../services/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

function TestConsumer() {
  const { user, isAuthenticated, login, logout, register } = useAuth();
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'LOGGED_IN' : 'LOGGED_OUT'}</span>
      <span data-testid="user-email">{user?.email || 'NO_USER'}</span>
      <button onClick={() => login('test@email.com', 'senha123').catch(() => {})}>LoginBtn</button>
      <button onClick={() => logout().catch(() => {})}>LogoutBtn</button>
      <button onClick={() => register('Nome', 'test@email.com', 'senha123', true, '1.0').catch(() => {})}>RegisterBtn</button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('deve verificar o usuário logado ao inicializar', async () => {
    vi.mocked(api.get).mockResolvedValue({ id: 1, nome: 'João', email: 'joao@email.com' });

    await act(async () => {
      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');
    expect(screen.getByTestId('user-email')).toHaveTextContent('joao@email.com');
  });

  it('deve permanecer deslogado se a chamada /me retornar erro', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Unauthorized'));

    await act(async () => {
      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
    expect(screen.getByTestId('user-email')).toHaveTextContent('NO_USER');
  });

  it('deve realizar login e atualizar o usuário no estado', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Unauthorized'));
    vi.mocked(api.post).mockResolvedValue({ mensagem: 'Login realizado com sucesso' });

    await act(async () => {
      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
    });

    vi.mocked(api.get).mockResolvedValue({ id: 1, nome: 'Maria', email: 'maria@email.com' });

    await act(async () => {
      screen.getByText('LoginBtn').click();
    });

    expect(api.post).toHaveBeenCalledWith('/api/auth/login', expect.anything(), true);
    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');
    expect(screen.getByTestId('user-email')).toHaveTextContent('maria@email.com');
  });

  it('deve realizar cadastro com sucesso', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Unauthorized'));
    vi.mocked(api.post).mockResolvedValue({ id: 2, nome: 'Nome', email: 'test@email.com' });

    await act(async () => {
      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
    });

    await act(async () => {
      screen.getByText('RegisterBtn').click();
    });

    expect(api.post).toHaveBeenCalledWith('/api/auth/register', {
      nome: 'Nome',
      email: 'test@email.com',
      senha: 'senha123',
      aceitouTermosLgpd: true,
      versaoTermoLgpd: '1.0',
    });
  });

  it('deve realizar logout zerando o estado do usuário', async () => {
    vi.mocked(api.get).mockResolvedValue({ id: 1, nome: 'João', email: 'joao@email.com' });

    await act(async () => {
      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');

    vi.mocked(api.post).mockResolvedValue({});

    await act(async () => {
      screen.getByText('LogoutBtn').click();
    });

    expect(api.post).toHaveBeenCalledWith('/api/auth/logout');
    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
  });
});
