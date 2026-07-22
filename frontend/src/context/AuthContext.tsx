import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { api } from '../services/api';

export interface User {
  id: number;
  nome: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isInitialLoading: boolean;
  error: string | null;
  login: (email: string, targetPassword: string) => Promise<void>;
  register: (name: string, email: string, targetPassword: string, aceitouTermosLgpd: boolean, versaoTermoLgpd: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isInitialLoading, setIsInitialLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Verifica se o usuário já está logado na inicialização
  useEffect(() => {
    checkAuthStatus();
  }, []);

  async function checkAuthStatus() {
    try {
      const data = await api.get<User>('/api/auth/me');
      setUser(data);
      setIsAuthenticated(true);
    } catch {
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
      setIsInitialLoading(false);
    }
  }

  async function login(email: string, targetPassword: string) {
    setIsLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      params.append('username', email);
      params.append('password', targetPassword);

      await api.post('/api/auth/login', params.toString(), true);
      
      // Busca dados do usuário após login de sucesso
      const userData = await api.get<User>('/api/auth/me');
      setUser(userData);
      setIsAuthenticated(true);
    } catch (err: any) {
      setError(err.message || 'Erro ao realizar login');
      setIsAuthenticated(false);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }

  async function register(name: string, email: string, targetPassword: string, aceitouTermosLgpd: boolean, versaoTermoLgpd: string) {
    setIsLoading(true);
    setError(null);
    try {
      await api.post('/api/auth/register', {
        nome: name,
        email,
        senha: targetPassword,
        aceitouTermosLgpd,
        versaoTermoLgpd
      });
      
      // Auto login após cadastro efetuado com sucesso
      await login(email, targetPassword);
    } catch (err: any) {
      setError(err.message || 'Erro ao realizar cadastro');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }

  async function logout() {
    setIsLoading(true);
    try {
      await api.post('/api/auth/logout');
    } catch {
      // Ignora erro no logout
    } finally {
      setUser(null);
      setIsAuthenticated(false);
      setIsLoading(false);
    }
  }

  function clearError() {
    setError(null);
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        isInitialLoading,
        error,
        login,
        register,
        logout,
        clearError,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return context;
}
