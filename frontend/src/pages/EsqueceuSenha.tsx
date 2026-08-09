import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { Mail, ArrowLeft, ShieldAlert, CheckCircle2 } from 'lucide-react';

export default function EsqueceuSenha() {
  const [email, setEmail] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!email) {
      setLocalError('Por favor, informe o seu e-mail.');
      return;
    }

    setSubmitting(true);
    setLocalError(null);
    setSuccessMessage(null);

    try {
      const res = await api.post<{ mensagem: string }>('/api/auth/esqueceu-senha', { email });
      setSuccessMessage(res.mensagem || 'Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha.');
      setEmail('');
    } catch (err: any) {
      setLocalError(err.message || 'Ocorreu um erro ao solicitar a redefinição de senha. Tente novamente.');
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
            Recuperação de Acesso
          </span>
        </div>

        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <h1 style={{
            fontSize: '2.4rem',
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
            Esqueceu sua senha? Digite seu e-mail cadastrado para receber as instruções.
          </p>
        </div>

        {successMessage && (
          <div style={{
            background: 'rgba(16, 185, 129, 0.08)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            borderRadius: '8px',
            padding: '16px',
            color: 'var(--color-success)',
            fontSize: '0.88rem',
            marginBottom: '24px',
            lineHeight: '1.5',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '10px'
          }}>
            <CheckCircle2 size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <strong>Solicitação enviada!</strong>
              <p style={{ marginTop: '4px', margin: 0 }}>
                {successMessage}
              </p>
            </div>
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
            <div>{localError}</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="cyber-input-group" style={{ marginBottom: '28px' }}>
            <label className="cyber-input-label">E-mail Cadastrado</label>
            <input
              type="email"
              className="cyber-input"
              placeholder="exemplo@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={submitting}
              autoComplete="email"
              required
            />
          </div>

          <button
            type="submit"
            className="cyber-btn"
            disabled={submitting}
            style={{ width: '100%', padding: '14px', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
          >
            {submitting ? 'Enviando...' : (
              <>
                Enviar link de redefinição <Mail size={16} />
              </>
            )}
          </button>
        </form>

        <div style={{
          textAlign: 'center',
          fontSize: '0.9rem',
          color: 'var(--text-secondary)',
        }}>
          <Link
            to="/login"
            style={{
              color: 'var(--color-accent-blue)',
              textDecoration: 'none',
              fontWeight: 600,
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              transition: 'all 0.2s'
            }}
            onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'}
            onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}
          >
            <ArrowLeft size={16} /> Voltar para o login
          </Link>
        </div>
      </div>
    </div>
  );
}
