import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import Register from './Register';
import { useAuth } from '../context/AuthContext';

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn(),
}));

describe('Register Page Component', () => {
  const mockRegister = vi.fn();

  beforeEach(() => {
    vi.restoreAllMocks();
    vi.mocked(useAuth).mockReturnValue({
      register: mockRegister,
      user: null,
      isAuthenticated: false,
      isInitialLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });
  });

  it('deve renderizar os campos do formulário de cadastro', () => {
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );

    expect(screen.getByPlaceholderText('Ex: João Silva')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('exemplo@email.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Mínimo 6 caracteres')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Cadastrar/i })).toBeInTheDocument();
  });

  it('deve exigir que o usuário aceite a Política de Privacidade', async () => {
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('Ex: João Silva'), { target: { value: 'João' } });
    fireEvent.change(screen.getByPlaceholderText('exemplo@email.com'), { target: { value: 'joao@email.com' } });
    fireEvent.change(screen.getByPlaceholderText('Mínimo 6 caracteres'), { target: { value: 'senha123' } });

    fireEvent.click(screen.getByRole('button', { name: /Cadastrar/i }));

    expect(await screen.findByText(/Você precisa ler e aceitar a Política de Privacidade/i)).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('deve submeter o cadastro quando todos os campos estiverem válidos e termos aceitos', async () => {
    mockRegister.mockResolvedValueOnce(undefined);

    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText('Ex: João Silva'), { target: { value: 'João Silva' } });
    fireEvent.change(screen.getByPlaceholderText('exemplo@email.com'), { target: { value: 'joao@email.com' } });
    fireEvent.change(screen.getByPlaceholderText('Mínimo 6 caracteres'), { target: { value: 'senha123' } });
    fireEvent.click(screen.getByRole('checkbox'));

    fireEvent.click(screen.getByRole('button', { name: /Cadastrar/i }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith('João Silva', 'joao@email.com', 'senha123', true, '1.0');
    });
  });
});
