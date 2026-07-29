import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogIn, ShieldAlert } from 'lucide-react';

export default function Login() {
  const [searchParams] = useSearchParams();
  const showRegisteredMsg = searchParams.get('registered') === 'true';
  const showConfirmedMsg = searchParams.get('confirmed') === 'true';
  const showConfirmedErrorMsg = searchParams.get('confirmed') === 'false';
  const confirmedErrorText = searchParams.get('error') || 'Token de confirmação inválido ou expirado.';
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!email || !password) {
      setLocalError('Por favor, preencha todos os campos.');
      return;
    }

    setSubmitting(true);
    setLocalError(null);

    try {
      await login(email, password);
      navigate('/');
    } catch (err: any) {
      if (err.message && (err.message.includes('não foi verificado') || err.message.includes('verificado'))) {
        setLocalError('unverified_email');
      } else {
        setLocalError('invalid_credentials');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px',
      position: 'relative',
    }}>
      <div className="cyber-card" style={{ width: '100%', maxWidth: '420px', padding: '40px 32px' }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>
          <span className="cyber-badge cyber-badge-blue">
            Acesso ao Sistema
          </span>
        </div>

        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <h1 style={{
            fontSize: '2.6rem',
            fontWeight: 800,
            color: 'var(--text-primary)',
            letterSpacing: '-0.03em',
            lineHeight: 1.1,
          }}>
            Home<span style={{ color: 'var(--color-accent-blue)' }}>Hub</span>
          </h1>
          <p style={{
            fontSize: '0.95rem',
            color: 'var(--text-secondary)',
            marginTop: '8px',
          }}>
            Gestão de Repúblicas Universitárias
          </p>
        </div>

        {showRegisteredMsg && (
          <div style={{
            background: 'rgba(16, 185, 129, 0.08)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            borderRadius: '8px',
            padding: '12px 16px',
            color: 'var(--color-success)',
            fontSize: '0.85rem',
            marginBottom: '24px',
            lineHeight: '1.4'
          }}>
            <strong>Cadastro realizado com sucesso!</strong>
            <p style={{ marginTop: '4px', margin: 0 }}>
              Um e-mail de confirmação foi enviado. Por favor, verifique sua caixa de entrada para confirmar seu endereço antes de fazer login.
            </p>
          </div>
        )}

        {showConfirmedMsg && (
          <div style={{
            background: 'rgba(16, 185, 129, 0.08)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            borderRadius: '8px',
            padding: '12px 16px',
            color: 'var(--color-success)',
            fontSize: '0.85rem',
            marginBottom: '24px',
            lineHeight: '1.4'
          }}>
            <strong>E-mail verificado com sucesso!</strong>
            <p style={{ marginTop: '4px', margin: 0 }}>
              Sua conta foi ativada. Agora você já pode fazer login para acessar o sistema.
            </p>
          </div>
        )}

        {showConfirmedErrorMsg && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.2)',
            borderRadius: '8px',
            padding: '12px 16px',
            color: 'var(--color-danger)',
            fontSize: '0.85rem',
            marginBottom: '24px',
            lineHeight: '1.4'
          }}>
            <strong>Falha na ativação da conta</strong>
            <p style={{ marginTop: '4px', margin: 0 }}>
              {confirmedErrorText}
            </p>
          </div>
        )}

        {localError && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.2)',
            borderRadius: '8px',
            padding: '12px 16px',
            color: 'var(--color-danger)',
            fontSize: '0.85rem',
            marginBottom: '24px',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '8px',
            lineHeight: '1.4'
          }}>
            <ShieldAlert size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              {localError === 'unverified_email' ? (
                <>
                  <strong>E-mail não verificado!</strong>
                  <p style={{ marginTop: '4px', margin: 0 }}>
                    Sua conta ainda não está ativa. Por favor, verifique sua caixa de entrada (e caixa de spam) para encontrar o link de ativação enviado.
                  </p>
                </>
              ) : localError === 'invalid_credentials' ? (
                <>
                  <strong>Falha na autenticação</strong>
                  <p style={{ marginTop: '4px', margin: 0 }}>
                    E-mail ou senha incorretos. Por favor, verifique suas credenciais e tente novamente.
                  </p>
                </>
              ) : (
                <span>{localError}</span>
              )}
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="cyber-input-group">
            <label className="cyber-input-label">E-mail</label>
            <input
              type="email"
              className="cyber-input"
              placeholder="exemplo@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={submitting}
              autoComplete="username"
            />
          </div>

          <div className="cyber-input-group" style={{ marginBottom: '28px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <label className="cyber-input-label" style={{ marginBottom: 0 }}>Senha</label>
              <Link
                to="/esqueceu-senha"
                style={{
                  fontSize: '0.8rem',
                  color: 'var(--color-accent-blue)',
                  textDecoration: 'none',
                  fontWeight: 600,
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'}
                onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}
              >
                Esqueceu sua senha?
              </Link>
            </div>
            <input
              type="password"
              className="cyber-input"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={submitting}
              autoComplete="current-password"
            />
          </div>


          <button
            type="submit"
            className="cyber-btn"
            disabled={submitting}
            style={{ width: '100%', padding: '14px', marginBottom: '24px' }}
          >
            {submitting ? 'Autenticando...' : (
              <>
                Entrar <LogIn size={16} />
              </>
            )}
          </button>
        </form>

        <div style={{ display: 'flex', alignItems: 'center', margin: '20px 0', color: 'var(--text-secondary)' }}>
          <hr style={{ flex: 1, border: 'none', borderTop: '1px solid var(--border-muted)' }} />
          <span style={{ padding: '0 10px', fontSize: '0.8rem', fontFamily: 'var(--font-mono)' }}>OU</span>
          <hr style={{ flex: 1, border: 'none', borderTop: '1px solid var(--border-muted)' }} />
        </div>

        <button
          type="button"
          onClick={() => {
            window.location.href = (import.meta.env.VITE_API_URL || '') + '/oauth2/authorization/google';
          }}
          className="cyber-btn-secondary"
          disabled={submitting}
          style={{ width: '100%', padding: '14px', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
          </svg>
          Entrar com o Google
        </button>

        <div style={{
          textAlign: 'center',
          fontSize: '0.9rem',
          color: 'var(--text-secondary)',
        }}>
          Não possui cadastro?{' '}
          <Link
            to="/register"
            style={{
              color: 'var(--color-accent-blue)',
              textDecoration: 'none',
              fontWeight: 600,
              transition: 'all 0.2s'
            }}
            onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'}
            onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}
          >
            Criar conta
          </Link>
        </div>

        <div style={{
          textAlign: 'center',
          fontSize: '0.8rem',
          marginTop: '16px',
          color: 'var(--text-secondary)',
        }}>
          Ao acessar você concorda com a nossa{' '}
          <Link
            to="/politica-de-privacidade"
            style={{
              color: 'var(--color-accent-blue)',
              textDecoration: 'underline',
            }}
          >
            Política de Privacidade
          </Link>
        </div>
      </div>
    </div>
  );
}
