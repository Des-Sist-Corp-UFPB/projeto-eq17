import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import { KeyRound, ShieldAlert, CheckCircle2, ArrowLeft } from 'lucide-react';

export default function RedefinirSenha() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const navigate = useNavigate();

  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [validatingToken, setValidatingToken] = useState(true);
  const [tokenValido, setTokenValido] = useState<boolean | null>(null);
  const [localError, setLocalError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!token) {
      setTokenValido(false);
      setValidatingToken(false);
      return;
    }

    let isMounted = true;
    api.get<{ valido: boolean }>(`/api/auth/validar-token-redefinicao?token=${encodeURIComponent(token)}`)
      .then((res) => {
        if (isMounted) {
          setTokenValido(res.valido);
        }
      })
      .catch(() => {
        if (isMounted) {
          setTokenValido(false);
        }
      })
      .finally(() => {
        if (isMounted) {
          setValidatingToken(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [token]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!novaSenha || !confirmarSenha) {
      setLocalError('Por favor, preencha todos os campos.');
      return;
    }

    if (novaSenha.length < 6) {
      setLocalError('A nova senha deve ter pelo menos 6 caracteres.');
      return;
    }

    if (novaSenha !== confirmarSenha) {
      setLocalError('As senhas digitadas não coincidem.');
      return;
    }

    setSubmitting(true);
    setLocalError(null);

    try {
      const res = await api.post<{ mensagem: string }>('/api/auth/redefinir-senha', {
        token,
        novaSenha,
      });

      setSuccessMessage(res.mensagem || 'Senha redefinida com sucesso!');
      setTimeout(() => {
        navigate('/login');
      }, 2500);
    } catch (err: any) {
      setLocalError(err.message || 'Erro ao redefinir a senha. O token pode ser inválido ou ter expirado.');
    } finally {
      setSubmitting(false);
    }
  }

  if (validatingToken) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontFamily: 'var(--font-mono)',
        fontSize: '0.9rem',
        color: 'var(--color-accent-blue)',
        letterSpacing: '0.1em'
      }}>
        VERIFICANDO TOKEN DE RECOVERY...
      </div>
    );
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
            Redefinição de Senha
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
            Crie uma nova senha para a sua conta
          </p>
        </div>

        {tokenValido === false ? (
          <div>
            <div style={{
              background: 'rgba(239, 68, 68, 0.08)',
              border: '1px solid rgba(239, 68, 68, 0.2)',
              borderRadius: '8px',
              padding: '16px',
              color: 'var(--color-danger)',
              fontSize: '0.88rem',
              marginBottom: '24px',
              lineHeight: '1.4',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '10px'
            }}>
              <ShieldAlert size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
              <div>
                <strong>Link inválido ou expirado!</strong>
                <p style={{ marginTop: '4px', margin: 0 }}>
                  Este link de redefinição de senha não é válido ou já expirou. Por favor, solicite um novo link de recuperação.
                </p>
              </div>
            </div>

            <Link
              to="/esqueceu-senha"
              className="cyber-btn"
              style={{
                width: '100%',
                padding: '14px',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                textDecoration: 'none'
              }}
            >
              Solicitar novo link
            </Link>
          </div>
        ) : (
          <>
            {successMessage && (
              <div style={{
                background: 'rgba(16, 185, 129, 0.08)',
                border: '1px solid rgba(16, 185, 129, 0.2)',
                borderRadius: '8px',
                padding: '16px',
                color: 'var(--color-success)',
                fontSize: '0.88rem',
                marginBottom: '24px',
                lineHeight: '1.4',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px'
              }}>
                <CheckCircle2 size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <strong>Senha redefinida com sucesso!</strong>
                  <p style={{ marginTop: '4px', margin: 0 }}>
                    {successMessage} Redirecionando para a página de login...
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
              <div className="cyber-input-group">
                <label className="cyber-input-label">Nova Senha</label>
                <input
                  type="password"
                  className="cyber-input"
                  placeholder="Mínimo 6 caracteres"
                  value={novaSenha}
                  onChange={(e) => setNovaSenha(e.target.value)}
                  disabled={submitting || !!successMessage}
                  autoComplete="new-password"
                  required
                />
              </div>

              <div className="cyber-input-group" style={{ marginBottom: '28px' }}>
                <label className="cyber-input-label">Confirmar Nova Senha</label>
                <input
                  type="password"
                  className="cyber-input"
                  placeholder="Repita a nova senha"
                  value={confirmarSenha}
                  onChange={(e) => setConfirmarSenha(e.target.value)}
                  disabled={submitting || !!successMessage}
                  autoComplete="new-password"
                  required
                />
              </div>

              <button
                type="submit"
                className="cyber-btn"
                disabled={submitting || !!successMessage}
                style={{ width: '100%', padding: '14px', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              >
                {submitting ? 'Redefinindo...' : (
                  <>
                    Redefinir Senha <KeyRound size={16} />
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
          </>
        )}
      </div>
    </div>
  );
}
