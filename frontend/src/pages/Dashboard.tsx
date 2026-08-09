import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { LogOut, Plus, MapPin, Calendar, LayoutDashboard, ShieldAlert, Download, Trash2 } from 'lucide-react';
import NotificacoesMenu from '../components/NotificacoesMenu';
import IaAssistant from '../components/IaAssistant';


interface Casa {
  id: number;
  nome: string;
  endereco: string;
  criadoEm: string;
}

interface DashboardData {
  usuarioLogado: {
    id: number;
    nome: string;
    email: string;
  };
  casas: Casa[];
}

export default function Dashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Form State para criar casa
  const [showModal, setShowModal] = useState(false);
  const [nome, setNome] = useState('');
  const [endereco, setEndereco] = useState('');
  const [modalError, setModalError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  const { logout, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  async function fetchDashboardData() {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get<DashboardData>('/api/dashboard');
      setData(response);
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar dados do dashboard');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateCasa(e: React.FormEvent) {
    e.preventDefault();
    if (!nome || !endereco) {
      setModalError('Preencha o nome e o endereço.');
      return;
    }

    setCreating(true);
    setModalError(null);
    try {
      const novaCasa = await api.post<Casa>('/api/casas', { nome, endereco });
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          casas: [...prev.casas, novaCasa]
        };
      });
      setNome('');
      setEndereco('');
      setShowModal(false);
    } catch (err: any) {
      setModalError(err.message || 'Erro ao criar república.');
    } finally {
      setCreating(false);
    }
  }

  async function handleExportarDados() {
    try {
      const response = await api.get<any>('/api/meus-dados');
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(response, null, 2));
      const downloadAnchor = document.createElement('a');
      downloadAnchor.setAttribute("href", dataStr);
      downloadAnchor.setAttribute("download", `meus-dados-homehub-${user?.id || 'export'}.json`);
      document.body.appendChild(downloadAnchor);
      downloadAnchor.click();
      downloadAnchor.remove();
    } catch (err: any) {
      alert(err.message || 'Erro ao exportar dados.');
    }
  }

  async function handleExcluirConta() {
    const confirmacao = window.confirm(
      "Tem certeza que deseja solicitar a exclusão de sua conta? Esta ação é definitiva.\n\n" +
      "Se você possuir pendências financeiras (despesas ou pagamentos em aberto nas repúblicas), seus dados serão anonimizados para integridade dos dados comuns do grupo."
    );
    if (!confirmacao) return;

    try {
      await api.delete('/api/meus-dados');
      alert("Sua conta foi excluída ou anonimizada com sucesso. Você será desconectado.");
      logout().then(() => navigate('/login'));
    } catch (err: any) {
      alert(err.message || 'Erro ao processar exclusão de conta.');
    }
  }

  if (loading) {
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
        LOADING PROTOCOL // SECURE SESSION...
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', paddingBottom: '80px' }}>
      {/* Header */}
      <header style={{
        background: 'var(--bg-secondary)',
        borderBottom: '1px solid var(--border-muted)',
        position: 'sticky',
        top: 0,
        zIndex: 50,
        padding: '16px 0',
        boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.05)'
      }}>
        <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <LayoutDashboard size={20} style={{ color: 'var(--color-accent-blue)' }} />
            <span style={{
              fontFamily: 'var(--font-display)',
              fontSize: '1.25rem',
              fontWeight: 800,
              letterSpacing: '-0.02em',
              color: 'var(--text-primary)'
            }}>
              Home<span style={{ color: 'var(--color-accent-blue)' }}>Hub</span>
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <NotificacoesMenu />

            {/* Menu do Perfil com Dropdown */}
            <div style={{ position: 'relative' }}>
              <button
                onClick={() => setShowProfileMenu(!showProfileMenu)}
                style={{
                  background: 'none',
                  border: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  fontSize: '0.85rem',
                  cursor: 'pointer',
                  padding: '8px 14px',
                  borderRadius: '10px',
                  transition: 'all 0.2s',
                  color: 'var(--text-primary)',
                  backgroundColor: showProfileMenu ? 'var(--border-muted)' : 'transparent'
                }}
                onMouseEnter={(e) => {
                  if (!showProfileMenu) e.currentTarget.style.backgroundColor = 'var(--border-muted)';
                }}
                onMouseLeave={(e) => {
                  if (!showProfileMenu) e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                <div style={{
                  width: '24px',
                  height: '24px',
                  borderRadius: '50%',
                  backgroundColor: 'rgba(79, 70, 229, 0.1)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--color-accent-blue)',
                  fontWeight: 700,
                  fontSize: '0.75rem',
                  fontFamily: 'var(--font-mono)'
                }}>
                  {user?.nome ? user.nome.substring(0, 2).toUpperCase() : 'U'}
                </div>
                <span style={{ fontWeight: 600 }}>{user?.nome}</span>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>▼</span>
              </button>

              {showProfileMenu && (
                <>
                  {/* Overlay invisível para fechar ao clicar fora */}
                  <div 
                    onClick={() => setShowProfileMenu(false)}
                    style={{
                      position: 'fixed',
                      top: 0,
                      left: 0,
                      width: '100vw',
                      height: '100vh',
                      zIndex: 90,
                      background: 'transparent'
                    }}
                  />
                  
                  {/* Menu Dropdown */}
                  <div style={{
                    position: 'absolute',
                    right: 0,
                    top: '100%',
                    marginTop: '8px',
                    width: '260px',
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border-muted)',
                    borderRadius: '12px',
                    boxShadow: '0 10px 25px -5px rgba(15, 23, 42, 0.1), 0 8px 10px -6px rgba(15, 23, 42, 0.1)',
                    zIndex: 100,
                    padding: '8px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '4px'
                  }}>
                    {/* Cabeçalho do perfil */}
                    <div style={{ padding: '10px 12px', borderBottom: '1px solid var(--border-muted)', marginBottom: '4px' }}>
                      <div style={{ fontWeight: 700, fontSize: '0.85rem', color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.nome}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.email}</div>
                    </div>

                    {/* Opção Exportar */}
                    <button
                      onClick={() => {
                        setShowProfileMenu(false);
                        handleExportarDados();
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '10px 12px',
                        fontSize: '0.85rem',
                        color: 'var(--text-secondary)',
                        cursor: 'pointer',
                        borderRadius: '8px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        transition: 'all 0.2s',
                        fontFamily: 'var(--font-body)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = 'rgba(79, 70, 229, 0.05)';
                        e.currentTarget.style.color = 'var(--color-accent-blue)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                        e.currentTarget.style.color = 'var(--text-secondary)';
                      }}
                    >
                      <Download size={14} />
                      Exportar meus Dados (JSON)
                    </button>

                    {/* Opção Excluir */}
                    <button
                      onClick={() => {
                        setShowProfileMenu(false);
                        handleExcluirConta();
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '10px 12px',
                        fontSize: '0.85rem',
                        color: 'var(--color-danger)',
                        cursor: 'pointer',
                        borderRadius: '8px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        transition: 'all 0.2s',
                        fontFamily: 'var(--font-body)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.05)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      <Trash2 size={14} />
                      Excluir Minha Conta
                    </button>

                    <div style={{ borderTop: '1px solid var(--border-muted)', margin: '4px 0' }} />

                    {/* Botão Sair */}
                    <button
                      onClick={() => {
                        setShowProfileMenu(false);
                        logout().then(() => navigate('/login'));
                      }}
                      style={{
                        background: 'none',
                        border: 'none',
                        width: '100%',
                        textAlign: 'left',
                        padding: '10px 12px',
                        fontSize: '0.85rem',
                        color: 'var(--text-secondary)',
                        cursor: 'pointer',
                        borderRadius: '8px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        transition: 'all 0.2s',
                        fontFamily: 'var(--font-body)'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = 'var(--border-muted)';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      <LogOut size={14} />
                      Sair da Conta
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container" style={{ marginTop: '48px' }}>
        {error && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.2)',
            borderRadius: '8px',
            padding: '16px',
            color: 'var(--color-danger)',
            marginBottom: '32px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}>
            <ShieldAlert size={20} />
            <span>{error}</span>
          </div>
        )}

        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-end',
          marginBottom: '36px',
          borderBottom: '1px solid var(--border-muted)',
          paddingBottom: '20px'
        }}>
          <div>
            <span className="cyber-badge cyber-badge-blue" style={{ marginBottom: '8px' }}>
              Painel de Controle
            </span>
            <h2 style={{ fontSize: '1.8rem', letterSpacing: '-0.01em' }}>Suas Repúblicas</h2>
          </div>

          <button
            onClick={() => setShowModal(true)}
            className="cyber-btn"
            style={{ fontSize: '0.85rem', padding: '10px 20px' }}
          >
            Nova República <Plus size={16} />
          </button>
        </div>

        {/* Repúblicas Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
          gap: '24px'
        }}>
          {data?.casas.map((casa) => (
            <div
              key={casa.id}
              className="cyber-card"
              style={{
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                minHeight: '200px'
              }}
            >
              <div>
                <h3 style={{
                  fontSize: '1.3rem',
                  marginBottom: '12px',
                  letterSpacing: '-0.01em',
                  color: 'var(--text-primary)'
                }}>
                  {casa.nome}
                </h3>
                
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '8px' }}>
                  <MapPin size={14} style={{ flexShrink: 0, marginTop: '2px', color: 'var(--color-accent-blue)' }} />
                  <span>{casa.endereco}</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                  <Calendar size={14} style={{ color: 'var(--text-muted)' }} />
                  <span>Registrada em {new Date(casa.criadoEm).toLocaleDateString('pt-BR')}</span>
                </div>
              </div>

              <div style={{ marginTop: '24px' }}>
                <button
                  onClick={() => navigate(`/casas/${casa.id}`)}
                  className="cyber-btn cyber-btn-secondary"
                  style={{ width: '100%', fontSize: '0.8rem', padding: '10px' }}
                >
                  Acessar Painel
                </button>
              </div>
            </div>
          ))}

          {/* Empty state creation card */}
          {data?.casas.length === 0 && (
            <div
              onClick={() => setShowModal(true)}
              style={{
                border: '2px dashed var(--border-muted)',
                borderRadius: '12px',
                padding: '40px 24px',
                textAlign: 'center',
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '220px',
                background: 'rgba(0,0,0,0.01)'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = 'var(--color-accent-blue)';
                e.currentTarget.style.background = 'rgba(79, 70, 229, 0.02)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = 'var(--border-muted)';
                e.currentTarget.style.background = 'rgba(0,0,0,0.01)';
              }}
            >
              <Plus size={28} style={{ color: 'var(--text-muted)', marginBottom: '12px' }} />
              <h4 style={{ color: 'var(--text-secondary)', marginBottom: '8px', fontSize: '1rem' }}>Nenhuma República Cadastrada</h4>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Clique aqui para registrar a primeira república e começar a gerenciar.</p>
            </div>
          )}
        </div>
      </main>

      {/* Modal Criar República */}
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100vw',
          height: '100vh',
          background: 'rgba(15, 23, 42, 0.3)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '24px'
        }}>
          <div className="cyber-card" style={{ width: '100%', maxWidth: '480px', position: 'relative' }}>
            <h3 style={{ marginBottom: '24px', fontSize: '1.4rem' }}>Cadastrar República</h3>

            {modalError && (
              <div style={{
                background: 'rgba(239, 68, 68, 0.08)',
                border: '1px solid rgba(239, 68, 68, 0.2)',
                borderRadius: '8px',
                padding: '12px',
                color: 'var(--color-danger)',
                fontSize: '0.85rem',
                marginBottom: '20px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <ShieldAlert size={16} />
                <span>{modalError}</span>
              </div>
            )}

            <form onSubmit={handleCreateCasa}>
              <div className="cyber-input-group">
                <label className="cyber-input-label">Nome da República</label>
                <input
                  type="text"
                  className="cyber-input"
                  placeholder="Ex: República dos Programadores"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  disabled={creating}
                />
              </div>

              <div className="cyber-input-group" style={{ marginBottom: '28px' }}>
                <label className="cyber-input-label">Endereço Completo</label>
                <input
                  type="text"
                  className="cyber-input"
                  placeholder="Ex: Av. Principal, 123 - Centro"
                  value={endereco}
                  onChange={(e) => setEndereco(e.target.value)}
                  disabled={creating}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="cyber-btn cyber-btn-secondary"
                  disabled={creating}
                  style={{ padding: '10px 20px', fontSize: '0.85rem' }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="cyber-btn"
                  disabled={creating}
                  style={{ padding: '10px 20px', fontSize: '0.85rem' }}
                >
                  {creating ? 'Salvando...' : 'Salvar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      <IaAssistant />
    </div>
  );
}
