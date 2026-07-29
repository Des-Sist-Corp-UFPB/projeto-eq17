import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Login from './Login';
import { useAuth } from '../context/AuthContext';

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Login Page Component', () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.restoreAllMocks();
    vi.mocked(useAuth).mockReturnValue({
      login: mockLogin,
      user: null,
      isAuthenticated: false,
      isLoading: false,
      isInitialLoading: false,
      error: null,
      clearError: vi.fn(),
      logout: vi.fn(),
      register: vi.fn(),
    });
  });

  const getSubmitButton = () => screen.getByRole('button', { name: /^Entrar$/i });

  it('deve renderizar os campos de e-mail, senha e botão de login', () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    expect(screen.getByPlaceholderText('exemplo@email.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
    expect(getSubmitButton()).toBeInTheDocument();
    expect(screen.getByText('Esqueceu sua senha?')).toBeInTheDocument();
  });

  it('deve exibir mensagem de erro se tentar submeter formulário em branco', async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.click(getSubmitButton());

    expect(await screen.findByText('Por favor, preencha todos os campos.')).toBeInTheDocument();
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('deve chamar a função de login e navegar ao submeter credenciais válidas', async () => {
    mockLogin.mockResolvedValueOnce(undefined);

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('exemplo@email.com'), {
      target: { value: 'teste@email.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'senha123' },
    });

    fireEvent.click(getSubmitButton());

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('teste@email.com', 'senha123');
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('deve exibir erro de credenciais inválidas em caso de falha', async () => {
    mockLogin.mockRejectedValueOnce(new Error('Credenciais inválidas'));

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('exemplo@email.com'), {
      target: { value: 'errado@email.com' },
    });
    fireEvent.change(screen.getByPlaceholderText('••••••••'), {
      target: { value: 'errada' },
    });

    fireEvent.click(getSubmitButton());

    expect(await screen.findByText('Falha na autenticação')).toBeInTheDocument();
  });
});
