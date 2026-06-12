import React, { useState, useEffect, useRef } from 'react';
import { api } from '../services/api';
import { Bell, Check, DollarSign, Calendar, CheckSquare, BellOff } from 'lucide-react';

interface Notificacao {
  id: number;
  titulo: string;
  mensagem: string;
  tipo: 'DESPESA_CRIADA' | 'VENCIMENTO_PROXIMO' | 'TAREFA_ATRIBUIDA';
  lida: boolean;
  criadoEm: string;
}

export default function NotificacoesMenu() {
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [naoLidasCount, setNaoLidasCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchNotificacoes();
    // Poll a cada 15 segundos para atualizar as notificações em segundo plano
    const interval = setInterval(fetchNotificacoes, 15000);
    return () => clearInterval(interval);
  }, []);

  // Fecha o menu se clicar fora dele
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  async function fetchNotificacoes() {
    try {
      const list = await api.get<Notificacao[]>('/api/notificacoes');
      setNotificacoes(list);
      
      const countRes = await api.get<{ count: number }>('/api/notificacoes/nao-lidas/count');
      setNaoLidasCount(countRes.count);
    } catch (err) {
      console.error('Erro ao carregar notificações', err);
    }
  }

  async function handleMarcarComoLida(id: number, e: React.MouseEvent) {
    e.stopPropagation();
    try {
      await api.put(`/api/notificacoes/${id}/lida`, {});
      setNotificacoes(prev => 
        prev.map(n => n.id === id ? { ...n, lida: true } : n)
      );
      setNaoLidasCount(prev => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Erro ao marcar notificação como lida', err);
    }
  }

  async function handleMarcarTodasComoLidas() {
    try {
      await api.put('/api/notificacoes/lidas', {});
      setNotificacoes(prev => 
        prev.map(n => ({ ...n, lida: true }))
      );
      setNaoLidasCount(0);
    } catch (err) {
      console.error('Erro ao marcar todas como lidas', err);
    }
  }

  const getIcon = (tipo: string) => {
    switch (tipo) {
      case 'DESPESA_CRIADA':
        return <DollarSign size={16} style={{ color: 'var(--color-accent-blue)' }} />;
      case 'VENCIMENTO_PROXIMO':
        return <Calendar size={16} style={{ color: 'var(--color-danger)' }} />;
      case 'TAREFA_ATRIBUIDA':
        return <CheckSquare size={16} style={{ color: 'var(--color-accent-orange)' }} />;
      default:
        return <Bell size={16} />;
    }
  };

  return (
    <div ref={dropdownRef} style={{ position: 'relative' }}>
      {/* Botão do Sininho */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          background: 'transparent',
          border: 'none',
          color: 'var(--text-secondary)',
          cursor: 'pointer',
          padding: '8px',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          position: 'relative',
          transition: 'background 0.2s, color 0.2s'
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
          e.currentTarget.style.color = 'var(--text-primary)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = 'transparent';
          e.currentTarget.style.color = 'var(--text-secondary)';
        }}
      >
        <Bell size={20} />
        {naoLidasCount > 0 && (
          <span
            style={{
              position: 'absolute',
              top: '4px',
              right: '4px',
              background: 'var(--color-danger)',
              color: '#ffffff',
              fontSize: '0.65rem',
              fontWeight: 'bold',
              borderRadius: '10px',
              padding: '2px 6px',
              minWidth: '18px',
              textAlign: 'center',
              boxShadow: '0 0 8px var(--color-danger)'
            }}
          >
            {naoLidasCount}
          </span>
        )}
      </button>

      {/* Dropdown Menu */}
      {isOpen && (
        <div
          style={{
            position: 'absolute',
            top: '45px',
            right: 0,
            width: '360px',
            maxHeight: '480px',
            background: 'var(--bg-secondary)',
            border: '1px solid var(--border-muted)',
            borderRadius: '12px',
            boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.3), 0 8px 10px -6px rgba(0, 0, 0, 0.3)',
            zIndex: 100,
            display: 'flex',
            flexDirection: 'column',
            backdropFilter: 'blur(8px)'
          }}
        >
          {/* Header */}
          <div
            style={{
              padding: '12px 16px',
              borderBottom: '1px solid var(--border-muted)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}
          >
            <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-primary)' }}>
              Notificações
            </span>
            {naoLidasCount > 0 && (
              <button
                onClick={handleMarcarTodasComoLidas}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'var(--color-accent-blue)',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  padding: 0
                }}
                onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'}
                onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}
              >
                Ler todas
              </button>
            )}
          </div>

          {/* List */}
          <div
            style={{
              overflowY: 'auto',
              flexGrow: 1,
              display: 'flex',
              flexDirection: 'column'
            }}
          >
            {notificacoes.length === 0 ? (
              <div
                style={{
                  padding: '32px 16px',
                  textAlign: 'center',
                  color: 'var(--text-muted)',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '8px'
                }}
              >
                <BellOff size={24} style={{ opacity: 0.5 }} />
                <span style={{ fontSize: '0.85rem' }}>Nenhuma notificação por aqui</span>
              </div>
            ) : (
              notificacoes.map((n) => (
                <div
                  key={n.id}
                  style={{
                    padding: '14px 16px',
                    borderBottom: '1px solid var(--border-muted)',
                    display: 'flex',
                    gap: '12px',
                    alignItems: 'flex-start',
                    background: n.lida ? 'transparent' : 'rgba(0, 242, 254, 0.02)',
                    transition: 'background 0.2s',
                    position: 'relative'
                  }}
                >
                  {/* Icon */}
                  <div
                    style={{
                      background: 'rgba(255, 255, 255, 0.02)',
                      border: '1px solid var(--border-muted)',
                      borderRadius: '8px',
                      padding: '8px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}
                  >
                    {getIcon(n.tipo)}
                  </div>

                  {/* Body */}
                  <div style={{ flexGrow: 1, minWidth: 0 }}>
                    <div
                      style={{
                        fontWeight: n.lida ? 600 : 700,
                        fontSize: '0.85rem',
                        color: 'var(--text-primary)',
                        marginBottom: '4px'
                      }}
                    >
                      {n.titulo}
                    </div>
                    <div
                      style={{
                        fontSize: '0.78rem',
                        color: 'var(--text-secondary)',
                        lineHeight: '1.3',
                        marginBottom: '6px',
                        wordBreak: 'break-word'
                      }}
                    >
                      {n.mensagem}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                      {new Date(n.criadoEm).toLocaleString('pt-BR')}
                    </div>
                  </div>

                  {/* Actions */}
                  {!n.lida && (
                    <button
                      onClick={(e) => handleMarcarComoLida(n.id, e)}
                      style={{
                        background: 'transparent',
                        border: 'none',
                        color: 'var(--text-muted)',
                        cursor: 'pointer',
                        padding: '4px',
                        borderRadius: '4px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                      title="Marcar como lida"
                      onMouseEnter={(e) => e.currentTarget.style.color = 'var(--color-success)'}
                      onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}
                    >
                      <Check size={14} />
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
