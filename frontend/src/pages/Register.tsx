import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserPlus, ShieldAlert } from 'lucide-react';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!name || !email || !password) {
      setLocalError('Por favor, preencha todos os campos.');
      return;
    }

    if (password.length < 6) {
      setLocalError('A senha deve ter pelo menos 6 caracteres.');
      return;
    }

    setSubmitting(true);
    setLocalError(null);

    try {
      await register(name, email, password);
      navigate('/');
    } catch (err: any) {
      setLocalError(err.message || 'Erro ao efetuar cadastro. Verifique os dados.');
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
      <div className="cyber-card" style={{ 
        width: '100%', 
        maxWidth: '420px', 
        padding: '40px 32px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>
          <span className="cyber-badge cyber-badge-orange">
            Cadastro de Usuário
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
            Criar Conta
          </h1>
          <p style={{
            fontSize: '0.95rem',
            color: 'var(--text-secondary)',
            marginTop: '8px',
          }}>
            Cadastre-se no HomeHub
          </p>
        </div>

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
            alignItems: 'center',
            gap: '8px'
          }}>
            <ShieldAlert size={16} style={{ flexShrink: 0 }} />
            <span>{localError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="cyber-input-group">
            <label className="cyber-input-label">Nome Completo</label>
            <input
              type="text"
              className="cyber-input"
              placeholder="Ex: João Silva"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={submitting}
              autoComplete="name"
            />
          </div>

          <div className="cyber-input-group">
            <label className="cyber-input-label">E-mail corporativo / pessoal</label>
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
            <label className="cyber-input-label">Senha de acesso</label>
            <input
              type="password"
              className="cyber-input"
              placeholder="Mínimo 6 caracteres"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={submitting}
              autoComplete="new-password"
            />
          </div>

          <button
            type="submit"
            className="cyber-btn cyber-btn-orange"
            disabled={submitting}
            style={{ width: '100%', padding: '14px', marginBottom: '24px' }}
          >
            {submitting ? 'Registrando...' : (
              <>
                Cadastrar <UserPlus size={16} />
              </>
            )}
          </button>
        </form>

        <div style={{
          textAlign: 'center',
          fontSize: '0.9rem',
          color: 'var(--text-secondary)',
        }}>
          Já possui conta?{' '}
          <Link
            to="/login"
            style={{
              color: 'var(--color-accent-orange)',
              textDecoration: 'none',
              fontWeight: 600,
              transition: 'all 0.2s'
            }}
            onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'}
            onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}
          >
            Acessar login
          </Link>
        </div>
      </div>
    </div>
  );
}
