import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { 
  ArrowLeft, Users, DollarSign, CheckSquare, Trash2, 
  Check, X, Eye, FileText, Send, ShieldAlert, Award, Calendar, MapPin
} from 'lucide-react';
import NotificacoesMenu from '../components/NotificacoesMenu';

interface Morador {
  id: number;
  nome: string;
  email: string;
  papel: 'ADMINISTRADOR' | 'MORADOR';
}

interface DespesaRateio {
  id: number;
  morador: Morador;
  valorDevido: number;
  statusPagamento: 'PENDENTE' | 'INFORMADO' | 'CONFIRMADO' | 'REJEITADO' | null;
  pagamentoId: number | null;
  comprovante: string | null;
  dataPagamento: string | null;
}

interface Despesa {
  id: number;
  descricao: string;
  valorTotal: number;
  vencimento: string;
  status: 'PENDENTE' | 'PARCIALMENTE_PAGA' | 'PAGA';
  responsavel: Morador;
  rateios: DespesaRateio[];
  tipo: 'FIXA' | 'OCASIONAL';
  chavePix: string | null;
}

interface Tarefa {
  id: number;
  descricao: string;
  status: 'PENDENTE' | 'CONCLUIDA';
  responsavel: Morador | null;
}

interface Casa {
  id: number;
  nome: string;
  endereco: string;
  criadoEm: string;
}

interface DetalhesCasaResponse {
  casa: Casa;
  moradorLogado: Morador;
  moradores: Morador[];
  despesas: Despesa[];
  tarefas: Tarefa[];
}

export default function CasaDetalhes() {
  const { id } = useParams<{ id: string }>();
  const [data, setData] = useState<DetalhesCasaResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modals & Forms State
  const [showMoradorModal, setShowMoradorModal] = useState(false);
  const [moradorEmail, setMoradorEmail] = useState('');
  const [moradorError, setMoradorError] = useState<string | null>(null);
  const [addingMorador, setAddingMorador] = useState(false);

  const [showDespesaModal, setShowDespesaModal] = useState(false);
  const [despesaDesc, setDespesaDesc] = useState('');
  const [despesaValor, setDespesaValor] = useState('');
  const [despesaVenc, setDespesaVenc] = useState('');
  const [despesaRespId, setDespesaRespId] = useState('');
  const [despesaTipo, setDespesaTipo] = useState<'FIXA' | 'OCASIONAL'>('OCASIONAL');
  const [despesaChavePix, setDespesaChavePix] = useState('');
  const [despesaError, setDespesaError] = useState<string | null>(null);
  const [creatingDespesa, setCreatingDespesa] = useState(false);

  const [showTarefaModal, setShowTarefaModal] = useState(false);
  const [tarefaDesc, setTarefaDesc] = useState('');
  const [tarefaRespId, setTarefaRespId] = useState('');
  const [tarefaError, setTarefaError] = useState<string | null>(null);
  const [creatingTarefa, setCreatingTarefa] = useState(false);

  // Rateio Modal
  const [selectedDespesa, setSelectedDespesa] = useState<Despesa | null>(null);
  const [showRateioModal, setShowRateioModal] = useState(false);
  const [comprovanteArquivo, setComprovanteArquivo] = useState<File | null>(null);
  const [informingPagamento, setInformingPagamento] = useState(false);
  const [rateioError, setRateioError] = useState<string | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    fetchDetalhes();
  }, [id]);

  async function fetchDetalhes() {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get<DetalhesCasaResponse>(`/api/casas/${id}`);
      setData(response);
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar detalhes da república.');
    } finally {
      setLoading(false);
    }
  }

  // Morador
  async function handleAddMorador(e: React.FormEvent) {
    e.preventDefault();
    if (!moradorEmail) {
      setMoradorError('E-mail é obrigatório.');
      return;
    }

    setAddingMorador(true);
    setMoradorError(null);
    try {
      const novoMorador = await api.post<Morador>(`/api/casas/${id}/moradores`, { email: moradorEmail });
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          moradores: [...prev.moradores, novoMorador]
        };
      });
      setMoradorEmail('');
      setShowMoradorModal(false);
    } catch (err: any) {
      setMoradorError(err.message || 'Erro ao adicionar morador.');
    } finally {
      setAddingMorador(false);
    }
  }

  // Despesa
  async function handleCreateDespesa(e: React.FormEvent) {
    e.preventDefault();
    if (!despesaDesc || !despesaValor || !despesaVenc || !despesaRespId) {
      setDespesaError('Preencha todos os campos obrigatórios.');
      return;
    }

    setCreatingDespesa(true);
    setDespesaError(null);
    try {
      const novaDespesa = await api.post<Despesa>(`/api/despesas/casa/${id}`, {
        descricao: despesaDesc,
        valorTotal: parseFloat(despesaValor),
        vencimento: despesaVenc,
        responsavelId: parseInt(despesaRespId),
        tipo: despesaTipo,
        chavePix: despesaChavePix || null
      });

      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          despesas: [...prev.despesas, novaDespesa]
        };
      });

      setDespesaDesc('');
      setDespesaValor('');
      setDespesaVenc('');
      setDespesaRespId('');
      setDespesaTipo('OCASIONAL');
      setDespesaChavePix('');
      setShowDespesaModal(false);
    } catch (err: any) {
      setDespesaError(err.message || 'Erro ao criar despesa.');
    } finally {
      setCreatingDespesa(false);
    }
  }

  async function handleDeleteDespesa(despesaId: number) {
    if (!window.confirm('Tem certeza que deseja excluir esta despesa?')) return;
    try {
      await api.delete(`/api/despesas/${despesaId}`);
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          despesas: prev.despesas.filter(d => d.id !== despesaId)
        };
      });
    } catch (err: any) {
      alert(err.message || 'Erro ao deletar despesa.');
    }
  }

  // Tarefa
  async function handleCreateTarefa(e: React.FormEvent) {
    e.preventDefault();
    if (!tarefaDesc) {
      setTarefaError('A descrição da tarefa é obrigatória.');
      return;
    }

    setCreatingTarefa(true);
    setTarefaError(null);
    try {
      const novaTarefa = await api.post<Tarefa>(`/api/tarefas/casa/${id}`, {
        descricao: tarefaDesc,
        responsavelId: tarefaRespId ? parseInt(tarefaRespId) : null
      });

      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          tarefas: [novaTarefa, ...prev.tarefas]
        };
      });

      setTarefaDesc('');
      setTarefaRespId('');
      setShowTarefaModal(false);
    } catch (err: any) {
      setTarefaError(err.message || 'Erro ao criar tarefa.');
    } finally {
      setCreatingTarefa(false);
    }
  }

  async function handleToggleTarefaStatus(tarefaId: number, currentStatus: string) {
    const nextStatus = currentStatus === 'PENDENTE' ? 'CONCLUIDA' : 'PENDENTE';
    try {
      const updated = await api.put<Tarefa>(`/api/tarefas/${tarefaId}/status`, { status: nextStatus });
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          tarefas: prev.tarefas.map(t => t.id === tarefaId ? updated : t)
        };
      });
    } catch (err: any) {
      alert(err.message || 'Erro ao atualizar status da tarefa.');
    }
  }

  async function handleDeleteTarefa(tarefaId: number) {
    if (!window.confirm('Tem certeza que deseja excluir esta tarefa?')) return;
    try {
      await api.delete(`/api/tarefas/${tarefaId}`);
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          tarefas: prev.tarefas.filter(t => t.id !== tarefaId)
        };
      });
    } catch (err: any) {
      alert(err.message || 'Erro ao excluir tarefa.');
    }
  }

  // Pagamentos & Rateios
  async function handleInformPagamento(rateioId: number) {
    if (!comprovanteArquivo) {
      setRateioError('Por favor, selecione um arquivo de comprovante (Imagem ou PDF).');
      return;
    }

    setInformingPagamento(true);
    setRateioError(null);
    try {
      const formData = new FormData();
      formData.append('comprovante', comprovanteArquivo);

      const updatedDespesa = await api.postMultipart<Despesa>(`/api/despesas/rateio/${rateioId}/pagar`, formData);
      
      // Atualiza os dados
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          despesas: prev.despesas.map(d => d.id === updatedDespesa.id ? updatedDespesa : d)
        };
      });
      
      // Atualiza o modal de visualização
      setSelectedDespesa(updatedDespesa);
      setComprovanteArquivo(null);
    } catch (err: any) {
      setRateioError(err.message || 'Erro ao informar pagamento.');
    } finally {
      setInformingPagamento(false);
    }
  }

  async function handleFastPayment(rateioId: number) {
    try {
      // Cria uma imagem PNG de 1x1 transparente fictícia
      const b64 = "iVBOR0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";
      const byteCharacters = atob(b64);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: 'image/png' });
      const file = new File([blob], 'comprovante_rapido.png', { type: 'image/png' });

      const formData = new FormData();
      formData.append('comprovante', file);

      const updatedDespesa = await api.postMultipart<Despesa>(`/api/despesas/rateio/${rateioId}/pagar`, formData);
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          despesas: prev.despesas.map(d => d.id === updatedDespesa.id ? updatedDespesa : d)
        };
      });
      if (selectedDespesa && selectedDespesa.id === updatedDespesa.id) {
        setSelectedDespesa(updatedDespesa);
      }
    } catch (err: any) {
      alert(err.message || 'Erro ao registrar pagamento rápido.');
    }
  }

  async function handleVisualizarComprovante(pagamentoId: number) {
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL || ''}/api/despesas/pagamento/${pagamentoId}/comprovante`, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Não foi possível obter o comprovante ou acesso negado.');
      }

      const blob = await response.blob();
      const fileUrl = URL.createObjectURL(blob);
      window.open(fileUrl, '_blank');
    } catch (err: any) {
      alert(err.message || 'Erro ao carregar o comprovante.');
    }
  }

  async function handleConfirmPagamento(pagamentoId: number) {
    try {
      const updatedDespesa = await api.post<Despesa>(`/api/despesas/pagamento/${pagamentoId}/confirmar`);
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          despesas: prev.despesas.map(d => d.id === updatedDespesa.id ? updatedDespesa : d)
        };
      });
      setSelectedDespesa(updatedDespesa);
    } catch (err: any) {
      alert(err.message || 'Erro ao confirmar pagamento.');
    }
  }

  const renderMinhaParte = (d: Despesa) => {
    const meuRateio = d.rateios?.find(r => r.morador.email === moradorLogado.email);
    if (!meuRateio) return null;

    const valorIndividual = meuRateio.valorDevido;

    if (meuRateio.statusPagamento === 'CONFIRMADO') {
      return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--color-success)' }}>
          <Check size={16} />
          <span style={{ fontSize: '0.8rem', fontWeight: 600 }}>Pago (R$ {valorIndividual.toFixed(2)})</span>
        </div>
      );
    }

    if (meuRateio.statusPagamento === 'INFORMADO') {
      return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--color-accent-orange)' }}>
          <div className="cyber-spinner" style={{ width: '12px', height: '12px', border: '2px solid var(--color-accent-orange)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 1s linear infinite', marginRight: '4px' }} />
          <span style={{ fontSize: '0.8rem', fontWeight: 600 }}>Aguardando Confirmação (R$ {valorIndividual.toFixed(2)})</span>
        </div>
      );
    }

    // PENDENTE ou REJEITADO
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <input
          type="checkbox"
          checked={false}
          onChange={() => handleFastPayment(meuRateio.id)}
          style={{
            width: '16px',
            height: '16px',
            cursor: 'pointer',
            accentColor: 'var(--color-accent-blue)'
          }}
          title="Marcar como enviado"
        />
        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          Marcar Pago (R$ {valorIndividual.toFixed(2)})
        </span>
      </div>
    );
  };

  async function handleRejeitarPagamento(pagamentoId: number) {
    try {
      const updatedDespesa = await api.post<Despesa>(`/api/despesas/pagamento/${pagamentoId}/rejeitar`);
      setData(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          despesas: prev.despesas.map(d => d.id === updatedDespesa.id ? updatedDespesa : d)
        };
      });
      setSelectedDespesa(updatedDespesa);
    } catch (err: any) {
      alert(err.message || 'Erro ao rejeitar pagamento.');
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
        LOADING HOUSE PROTOCOL // SECURE NODE...
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="container" style={{ marginTop: '80px', textAlign: 'center' }}>
        <ShieldAlert size={48} style={{ color: 'var(--color-danger)', marginBottom: '16px' }} />
        <h3 style={{ marginBottom: '16px' }}>Erro ao Carregar República</h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>{error || 'Não foi possível encontrar a república solicitada.'}</p>
        <button onClick={() => navigate('/')} className="cyber-btn cyber-btn-secondary">
          Voltar ao Dashboard
        </button>
      </div>
    );
  }

  const { casa, moradorLogado, moradores, despesas, tarefas } = data;
  const isAdm = moradorLogado.papel === 'ADMINISTRADOR';
  const despesasFixas = despesas.filter(d => d.tipo === 'FIXA');
  const despesasOcasionais = despesas.filter(d => d.tipo === 'OCASIONAL');

  return (
    <div style={{ minHeight: '100vh', paddingBottom: '120px' }}>
      {/* Top Banner Navigation */}
      <div style={{
        background: 'var(--bg-secondary)',
        borderBottom: '1px solid var(--border-muted)',
        padding: '16px 0',
        boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.05)'
      }}>
        <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <button 
            onClick={() => navigate('/')} 
            className="cyber-btn cyber-btn-secondary"
            style={{ padding: '8px 16px', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <ArrowLeft size={14} /> Painel Principal
          </button>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <NotificacoesMenu />
            <span className="cyber-badge cyber-badge-blue">
              República #{casa.id} | {moradorLogado.papel === 'ADMINISTRADOR' ? 'Administrador' : 'Morador'}
            </span>
          </div>
        </div>
      </div>

      {/* House Title Area */}
      <div className="container" style={{ marginTop: '40px' }}>
        <div className="cyber-card" style={{ marginBottom: '40px', padding: '32px' }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'flex-start', gap: '20px' }}>
            <div>
              <h1 style={{ fontSize: '2.2rem', color: 'var(--text-primary)', marginBottom: '16px', fontWeight: 800, letterSpacing: '-0.02em' }}>{casa.nome}</h1>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '8px' }}>
                <MapPin size={16} style={{ color: 'var(--color-accent-blue)' }} />
                <span>{casa.endereco}</span>
              </div>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <Calendar size={16} />
                <span>Criada em {new Date(casa.criadoEm).toLocaleDateString('pt-BR')}</span>
              </div>
            </div>

            <div style={{
              background: 'rgba(255, 255, 255, 0.02)',
              border: '1px solid var(--border-muted)',
              borderRadius: '8px',
              padding: '16px 24px',
              textAlign: 'center'
            }}>
              <span style={{ display: 'block', fontSize: '0.75rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '4px' }}>Seu Perfil Local</span>
              <span className={`cyber-badge ${isAdm ? 'cyber-badge-orange' : 'cyber-badge-blue'}`} style={{ fontSize: '0.8rem', padding: '6px 12px' }}>
                {isAdm ? <Award size={12} /> : null} {moradorLogado.papel}
              </span>
            </div>
          </div>
        </div>

        {/* Dashboard Sections Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr',
          gap: '32px',
          alignItems: 'start'
        }}>
          {/* Row 1: Moradores & Tarefas */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
            gap: '32px'
          }}>
            {/* Moradores Card */}
            <div className="cyber-card" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '1px solid var(--border-muted)', paddingBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Users size={18} style={{ color: 'var(--color-accent-blue)' }} />
                  <h3>Moradores ({moradores.length})</h3>
                </div>
                <button 
                  onClick={() => setShowMoradorModal(true)} 
                  className="cyber-btn"
                  style={{ padding: '6px 12px', fontSize: '0.75rem' }}
                >
                  Convidar
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', flexGrow: 1 }}>
                {moradores.map((m) => (
                  <div 
                    key={m.id} 
                    style={{ 
                      display: 'flex', 
                      justifyContent: 'space-between', 
                      alignItems: 'center', 
                      background: 'rgba(255,255,255,0.02)',
                      padding: '12px 16px',
                      borderRadius: '8px',
                      border: '1px solid var(--border-muted)'
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{m.nome}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>{m.email}</div>
                    </div>
                    
                    <span className={`cyber-badge ${m.papel === 'ADMINISTRADOR' ? 'cyber-badge-orange' : 'cyber-badge-blue'}`} style={{ fontSize: '0.65rem' }}>
                      {m.papel}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Tarefas Card */}
            <div className="cyber-card" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '1px solid var(--border-muted)', paddingBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <CheckSquare size={18} style={{ color: 'var(--color-accent-blue)' }} />
                  <h3>Tarefas ({tarefas.length})</h3>
                </div>
                <button 
                  onClick={() => setShowTarefaModal(true)} 
                  className="cyber-btn"
                  style={{ padding: '6px 12px', fontSize: '0.75rem' }}
                >
                  Nova Tarefa
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', flexGrow: 1 }}>
                {tarefas.map((t) => (
                  <div 
                    key={t.id} 
                    style={{ 
                      display: 'flex', 
                      justifyContent: 'space-between', 
                      alignItems: 'center', 
                      background: 'rgba(255,255,255,0.02)',
                      padding: '12px 16px',
                      borderRadius: '8px',
                      border: '1px solid var(--border-muted)',
                      opacity: t.status === 'CONCLUIDA' ? 0.6 : 1
                    }}
                  >
                    <div style={{ flexGrow: 1, marginRight: '16px' }}>
                      <div style={{ 
                        fontWeight: 600, 
                        fontSize: '0.95rem',
                        textDecoration: t.status === 'CONCLUIDA' ? 'line-through' : 'none',
                        color: t.status === 'CONCLUIDA' ? 'var(--text-muted)' : 'var(--text-primary)'
                      }}>
                        {t.descricao}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        {t.responsavel ? `Responsável: ${t.responsavel.nome}` : 'Sem responsável designado'}
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <button
                        onClick={() => handleToggleTarefaStatus(t.id, t.status)}
                        className={`cyber-btn ${t.status === 'CONCLUIDA' ? 'cyber-btn-secondary' : ''}`}
                        style={{ padding: '6px 10px', fontSize: '0.7rem' }}
                      >
                        {t.status === 'CONCLUIDA' ? 'Reabrir' : 'Concluir'}
                      </button>
                      
                      {isAdm && (
                        <button
                          onClick={() => handleDeleteTarefa(t.id)}
                          style={{
                            background: 'transparent',
                            border: 'none',
                            color: 'var(--text-muted)',
                            cursor: 'pointer',
                            padding: '4px'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.color = 'var(--color-danger)'}
                          onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}
                        >
                          <Trash2 size={14} />
                        </button>
                      )}
                    </div>
                  </div>
                ))}

                {tarefas.length === 0 && (
                  <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '24px 0', fontSize: '0.9rem' }}>
                    Nenhuma tarefa cadastrada.
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Row 2: Despesas */}
          <div className="cyber-card" style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-muted)', paddingBottom: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <DollarSign size={18} style={{ color: 'var(--color-accent-blue)' }} />
                <h3>Despesas Coletivas ({despesas.length})</h3>
              </div>
              <button 
                onClick={() => setShowDespesaModal(true)} 
                className="cyber-btn"
                style={{ padding: '8px 16px', fontSize: '0.8rem' }}
              >
                Lançar Despesa
              </button>
            </div>

            {/* Sub-seção: Despesas Fixas */}
            <div>
              <h4 style={{ marginBottom: '16px', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--color-accent-blue)' }}></span>
                Despesas Fixas (Mensais)
              </h4>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', minWidth: '800px' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-muted)', color: 'var(--text-secondary)', fontSize: '0.8rem', fontFamily: 'var(--font-mono)' }}>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Descrição</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Valor</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Vencimento</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Responsável</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Status</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Minha Parte</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase', textAlign: 'center' }}>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {despesasFixas.map((d) => (
                      <tr 
                        key={d.id} 
                        style={{ 
                          borderBottom: '1px solid var(--border-muted)',
                          fontSize: '0.9rem',
                          transition: 'background 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.01)'}
                        onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                      >
                        <td style={{ padding: '16px' }}>
                          <div style={{ fontWeight: 600 }}>{d.descricao}</div>
                          {d.chavePix && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px' }}>
                              <span 
                                onClick={() => {
                                  navigator.clipboard.writeText(d.chavePix!);
                                  alert('Chave PIX copiada: ' + d.chavePix);
                                }}
                                className="cyber-badge cyber-badge-blue"
                                style={{ 
                                  fontSize: '0.7rem', 
                                  cursor: 'pointer', 
                                  padding: '2px 6px',
                                  fontFamily: 'var(--font-mono)'
                                }}
                                title="Clique para copiar a chave PIX"
                              >
                                PIX: {d.chavePix} (Copiar)
                              </span>
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '16px' }}>
                          <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>R$ {d.valorTotal.toFixed(2)}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            R$ {(d.valorTotal / moradores.length).toFixed(2)} p/ pessoa ({moradores.length} mor.)
                          </div>
                        </td>
                        <td style={{ padding: '16px' }}>{new Date(d.vencimento + 'T00:00:00').toLocaleDateString('pt-BR')}</td>
                        <td style={{ padding: '16px' }}>{d.responsavel.nome}</td>
                        <td style={{ padding: '16px' }}>
                          <span className={`cyber-badge ${
                            d.status === 'PAGA' ? 'cyber-badge-success' : 
                            d.status === 'PARCIALMENTE_PAGA' ? 'cyber-badge-orange' : 'cyber-badge-danger'
                          }`} style={{ fontSize: '0.65rem' }}>
                            {d.status.replace('_', ' ')}
                          </span>
                        </td>
                        <td style={{ padding: '16px' }}>{renderMinhaParte(d)}</td>
                        <td style={{ padding: '16px', display: 'flex', gap: '8px', justifyContent: 'center' }}>
                          <button
                            onClick={() => {
                              setSelectedDespesa(d);
                              setShowRateioModal(true);
                            }}
                            className="cyber-btn cyber-btn-secondary"
                            style={{ padding: '6px 10px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                          >
                            <Eye size={12} /> Rateio
                          </button>
                          
                          {isAdm && (
                            <button
                              onClick={() => handleDeleteDespesa(d.id)}
                              className="cyber-btn cyber-btn-danger"
                              style={{ padding: '6px 10px', fontSize: '0.75rem' }}
                            >
                              <Trash2 size={12} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}

                    {despesasFixas.length === 0 && (
                      <tr>
                        <td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)' }}>
                          Nenhuma despesa fixa lançada.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Sub-seção: Despesas Ocasionais */}
            <div>
              <h4 style={{ marginBottom: '16px', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--color-accent-orange)' }}></span>
                Despesas Ocasionais (Extras)
              </h4>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', minWidth: '800px' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-muted)', color: 'var(--text-secondary)', fontSize: '0.8rem', fontFamily: 'var(--font-mono)' }}>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Descrição</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Valor</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Vencimento</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Responsável</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Status</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase' }}>Minha Parte</th>
                      <th style={{ padding: '12px 16px', textTransform: 'uppercase', textAlign: 'center' }}>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {despesasOcasionais.map((d) => (
                      <tr 
                        key={d.id} 
                        style={{ 
                          borderBottom: '1px solid var(--border-muted)',
                          fontSize: '0.9rem',
                          transition: 'background 0.2s'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.01)'}
                        onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                      >
                        <td style={{ padding: '16px' }}>
                          <div style={{ fontWeight: 600 }}>{d.descricao}</div>
                          {d.chavePix && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px' }}>
                              <span 
                                onClick={() => {
                                  navigator.clipboard.writeText(d.chavePix!);
                                  alert('Chave PIX copiada: ' + d.chavePix);
                                }}
                                className="cyber-badge cyber-badge-blue"
                                style={{ 
                                  fontSize: '0.7rem', 
                                  cursor: 'pointer', 
                                  padding: '2px 6px',
                                  fontFamily: 'var(--font-mono)'
                                }}
                                title="Clique para copiar a chave PIX"
                              >
                                PIX: {d.chavePix} (Copiar)
                              </span>
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '16px' }}>
                          <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>R$ {d.valorTotal.toFixed(2)}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            R$ {(d.valorTotal / moradores.length).toFixed(2)} p/ pessoa ({moradores.length} mor.)
                          </div>
                        </td>
                        <td style={{ padding: '16px' }}>{new Date(d.vencimento + 'T00:00:00').toLocaleDateString('pt-BR')}</td>
                        <td style={{ padding: '16px' }}>{d.responsavel.nome}</td>
                        <td style={{ padding: '16px' }}>
                          <span className={`cyber-badge ${
                            d.status === 'PAGA' ? 'cyber-badge-success' : 
                            d.status === 'PARCIALMENTE_PAGA' ? 'cyber-badge-orange' : 'cyber-badge-danger'
                          }`} style={{ fontSize: '0.65rem' }}>
                            {d.status.replace('_', ' ')}
                          </span>
                        </td>
                        <td style={{ padding: '16px' }}>{renderMinhaParte(d)}</td>
                        <td style={{ padding: '16px', display: 'flex', gap: '8px', justifyContent: 'center' }}>
                          <button
                            onClick={() => {
                              setSelectedDespesa(d);
                              setShowRateioModal(true);
                            }}
                            className="cyber-btn cyber-btn-secondary"
                            style={{ padding: '6px 10px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                          >
                            <Eye size={12} /> Rateio
                          </button>
                          
                          {isAdm && (
                            <button
                              onClick={() => handleDeleteDespesa(d.id)}
                              className="cyber-btn cyber-btn-danger"
                              style={{ padding: '6px 10px', fontSize: '0.75rem' }}
                            >
                              <Trash2 size={12} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}

                    {despesasOcasionais.length === 0 && (
                      <tr>
                        <td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)' }}>
                          Nenhuma despesa ocasional lançada.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modal Convidar Morador */}
      {showMoradorModal && (
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
          <div className="cyber-card" style={{ width: '100%', maxWidth: '440px' }}>
            <h3 style={{ marginBottom: '24px', fontSize: '1.3rem' }}>Convidar Morador</h3>

            {moradorError && (
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
                <span>{moradorError}</span>
              </div>
            )}

            <form onSubmit={handleAddMorador}>
              <div className="cyber-input-group" style={{ marginBottom: '24px' }}>
                <label className="cyber-input-label">E-mail do Usuário cadastrado</label>
                <input
                  type="email"
                  className="cyber-input"
                  placeholder="morador@email.com"
                  value={moradorEmail}
                  onChange={(e) => setMoradorEmail(e.target.value)}
                  disabled={addingMorador}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  onClick={() => setShowMoradorModal(false)}
                  className="cyber-btn cyber-btn-secondary"
                  disabled={addingMorador}
                  style={{ padding: '8px 16px', fontSize: '0.8rem' }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="cyber-btn"
                  disabled={addingMorador}
                  style={{ padding: '8px 16px', fontSize: '0.8rem' }}
                >
                  {addingMorador ? 'Adicionando...' : 'Adicionar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Lançar Despesa */}
      {showDespesaModal && (
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
          <div className="cyber-card" style={{ width: '100%', maxWidth: '480px' }}>
            <h3 style={{ marginBottom: '24px', fontSize: '1.3rem' }}>Lançar Nova Despesa</h3>

            {despesaError && (
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
                <span>{despesaError}</span>
              </div>
            )}

            <form onSubmit={handleCreateDespesa}>
              <div className="cyber-input-group">
                <label className="cyber-input-label">Descrição da despesa</label>
                <input
                  type="text"
                  className="cyber-input"
                  placeholder="Ex: Energia - Junho"
                  value={despesaDesc}
                  onChange={(e) => setDespesaDesc(e.target.value)}
                  disabled={creatingDespesa}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="cyber-input-group">
                  <label className="cyber-input-label">Valor Total (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    className="cyber-input"
                    placeholder="0.00"
                    value={despesaValor}
                    onChange={(e) => setDespesaValor(e.target.value)}
                    disabled={creatingDespesa}
                  />
                </div>

                <div className="cyber-input-group">
                  <label className="cyber-input-label">Vencimento</label>
                  <input
                    type="date"
                    className="cyber-input"
                    value={despesaVenc}
                    onChange={(e) => setDespesaVenc(e.target.value)}
                    disabled={creatingDespesa}
                  />
                </div>
              </div>

              <div className="cyber-input-group" style={{ marginBottom: '20px' }}>
                <label className="cyber-input-label">Responsável pelo pagamento</label>
                <select
                  className="cyber-input"
                  value={despesaRespId}
                  onChange={(e) => setDespesaRespId(e.target.value)}
                  disabled={creatingDespesa}
                  style={{ background: 'var(--bg-input)' }}
                >
                  <option value="">Selecione o morador...</option>
                  {moradores.map(m => (
                    <option key={m.id} value={m.id}>{m.nome}</option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '28px' }}>
                <div className="cyber-input-group" style={{ marginBottom: 0 }}>
                  <label className="cyber-input-label">Tipo de Despesa</label>
                  <select
                    className="cyber-input"
                    value={despesaTipo}
                    onChange={(e) => setDespesaTipo(e.target.value as 'FIXA' | 'OCASIONAL')}
                    disabled={creatingDespesa}
                    style={{ background: 'var(--bg-input)' }}
                  >
                    <option value="OCASIONAL">Ocasional</option>
                    <option value="FIXA">Fixa (Mensal)</option>
                  </select>
                </div>

                <div className="cyber-input-group" style={{ marginBottom: 0 }}>
                  <label className="cyber-input-label">Chave PIX (Opcional)</label>
                  <input
                    type="text"
                    className="cyber-input"
                    placeholder="Chave PIX para pagamento"
                    value={despesaChavePix}
                    onChange={(e) => setDespesaChavePix(e.target.value)}
                    disabled={creatingDespesa}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  onClick={() => setShowDespesaModal(false)}
                  className="cyber-btn cyber-btn-secondary"
                  disabled={creatingDespesa}
                  style={{ padding: '8px 16px', fontSize: '0.8rem' }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="cyber-btn"
                  disabled={creatingDespesa}
                  style={{ padding: '8px 16px', fontSize: '0.8rem' }}
                >
                  {creatingDespesa ? 'Salvando...' : 'Lançar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Nova Tarefa */}
      {showTarefaModal && (
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
          <div className="cyber-card" style={{ width: '100%', maxWidth: '440px' }}>
            <h3 style={{ marginBottom: '24px', fontSize: '1.3rem' }}>Criar Nova Tarefa</h3>

            {tarefaError && (
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
                <span>{tarefaError}</span>
              </div>
            )}

            <form onSubmit={handleCreateTarefa}>
              <div className="cyber-input-group">
                <label className="cyber-input-label">Descrição da tarefa</label>
                <input
                  type="text"
                  className="cyber-input"
                  placeholder="Ex: Comprar itens de limpeza"
                  value={tarefaDesc}
                  onChange={(e) => setTarefaDesc(e.target.value)}
                  disabled={creatingTarefa}
                />
              </div>

              <div className="cyber-input-group" style={{ marginBottom: '28px' }}>
                <label className="cyber-input-label">Designar Responsável (Opcional)</label>
                <select
                  className="cyber-input"
                  value={tarefaRespId}
                  onChange={(e) => setTarefaRespId(e.target.value)}
                  disabled={creatingTarefa}
                  style={{ background: 'var(--bg-input)' }}
                >
                  <option value="">Não atribuído</option>
                  {moradores.map(m => (
                    <option key={m.id} value={m.id}>{m.nome}</option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  onClick={() => setShowTarefaModal(false)}
                  className="cyber-btn cyber-btn-secondary"
                  disabled={creatingTarefa}
                  style={{ padding: '8px 16px', fontSize: '0.8rem' }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="cyber-btn"
                  disabled={creatingTarefa}
                  style={{ padding: '8px 16px', fontSize: '0.8rem' }}
                >
                  {creatingTarefa ? 'Criando...' : 'Criar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Rateios e Pagamentos */}
      {showRateioModal && selectedDespesa && (
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
          <div className="cyber-card" style={{ width: '100%', maxWidth: '640px', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px', borderBottom: '1px solid var(--border-muted)', paddingBottom: '12px' }}>
              <div>
                <span className="cyber-badge cyber-badge-blue" style={{ marginBottom: '6px' }}>DIVISÃO DE DESPESA</span>
                <h3 style={{ fontSize: '1.4rem' }}>{selectedDespesa.descricao}</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '4px' }}>
                  Total: <strong style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>R$ {selectedDespesa.valorTotal.toFixed(2)}</strong> | 
                  Pagar a: <strong style={{ color: 'var(--text-primary)' }}>{selectedDespesa.responsavel.nome}</strong>
                </p>
              </div>
              <button 
                onClick={() => {
                  setShowRateioModal(false);
                  setRateioError(null);
                  setComprovanteArquivo(null);
                }}
                style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            {rateioError && (
              <div style={{
                background: 'rgba(239, 68, 68, 0.08)',
                border: '1px solid rgba(239, 68, 68, 0.2)',
                borderRadius: '8px',
                padding: '12px',
                color: 'var(--color-danger)',
                fontSize: '0.85rem',
                marginBottom: '16px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <ShieldAlert size={16} />
                <span>{rateioError}</span>
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
              {selectedDespesa.rateios.map((r) => {
                const isMyRateio = r.morador.id === moradorLogado.id;
                const podeVerComprovante = isMyRateio || isAdm || (selectedDespesa.responsavel.id === moradorLogado.id);
                
                return (
                  <div 
                    key={r.id}
                    style={{
                      background: 'rgba(255, 255, 255, 0.01)',
                      border: '1px solid var(--border-muted)',
                      borderRadius: '8px',
                      padding: '16px',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '12px'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px' }}>
                      <div>
                        <span style={{ fontWeight: 600, fontSize: '0.95rem' }}>
                          {r.morador.nome} {isMyRateio ? ' (Você)' : ''}
                        </span>
                        <span style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '2px', fontFamily: 'var(--font-mono)' }}>
                          Devido: R$ {r.valorDevido.toFixed(2)}
                        </span>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span className={`cyber-badge ${
                          r.statusPagamento === 'CONFIRMADO' ? 'cyber-badge-success' :
                          r.statusPagamento === 'INFORMADO' ? 'cyber-badge-orange' : 'cyber-badge-danger'
                        }`} style={{ fontSize: '0.65rem' }}>
                          {r.statusPagamento || 'PENDENTE'}
                        </span>
                      </div>
                    </div>

                    {/* Exibe o comprovante informado se houver */}
                    {r.comprovante && (
                      <div style={{ 
                        fontSize: '0.8rem', 
                        color: 'var(--text-secondary)', 
                        background: 'rgba(0,0,0,0.05)', 
                        padding: '10px 12px', 
                        borderRadius: '6px', 
                        borderLeft: '2px solid var(--color-accent-blue)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between'
                      }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <FileText size={14} style={{ color: 'var(--color-accent-blue)' }} />
                          {podeVerComprovante ? (
                            <button
                              onClick={() => handleVisualizarComprovante(r.pagamentoId!)}
                              style={{
                                background: 'none',
                                border: 'none',
                                color: 'var(--color-accent-blue)',
                                textDecoration: 'underline',
                                fontWeight: 600,
                                cursor: 'pointer',
                                padding: 0,
                                fontSize: '0.8rem',
                                fontFamily: 'var(--font-body)'
                              }}
                            >
                              Visualizar Comprovante Pix
                            </button>
                          ) : (
                            <span style={{ color: 'var(--text-muted)' }}>Comprovante Anexado (Privado)</span>
                          )}
                        </div>

                        {r.dataPagamento && (
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            Informado em {new Date(r.dataPagamento).toLocaleString('pt-BR')}
                          </span>
                        )}
                      </div>
                    )}

                    {/* Ações para o morador logado pagar o seu rateio */}
                    {isMyRateio && (r.statusPagamento === 'PENDENTE' || r.statusPagamento === 'REJEITADO') && (
                      <div style={{ 
                        marginTop: '8px', 
                        display: 'flex', 
                        gap: '12px', 
                        alignItems: 'flex-end', 
                        background: 'rgba(0, 242, 254, 0.02)',
                        padding: '12px',
                        borderRadius: '6px',
                        border: '1px dashed rgba(0, 242, 254, 0.2)'
                      }}>
                        <div className="cyber-input-group" style={{ flexGrow: 1, marginBottom: 0 }}>
                          <label className="cyber-input-label" style={{ fontSize: '0.7rem' }}>Anexar Comprovante Pix (Imagem ou PDF)</label>
                          <input
                            type="file"
                            className="cyber-input"
                            accept="image/*,application/pdf"
                            onChange={(e) => {
                              if (e.target.files && e.target.files[0]) {
                                setComprovanteArquivo(e.target.files[0]);
                              }
                            }}
                            disabled={informingPagamento}
                            style={{ padding: '6px 12px', fontSize: '0.85rem' }}
                          />
                        </div>
                        <button
                          onClick={() => handleInformPagamento(r.id)}
                          className="cyber-btn"
                          disabled={informingPagamento}
                          style={{ padding: '8px 16px', fontSize: '0.8rem', height: '38px', flexShrink: 0 }}
                        >
                          <Send size={12} /> Enviar
                        </button>
                      </div>
                    )}

                    {/* Ações para o Administrador confirmar/rejeitar o pagamento dos moradores */}
                    {isAdm && r.statusPagamento === 'INFORMADO' && r.pagamentoId && (
                      <div style={{ 
                        marginTop: '8px', 
                        display: 'flex', 
                        gap: '12px', 
                        background: 'rgba(255, 78, 0, 0.02)',
                        padding: '12px',
                        borderRadius: '6px',
                        border: '1px dashed rgba(255, 78, 0, 0.2)'
                      }}>
                        <div style={{ flexGrow: 1, fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center' }}>
                          Revisar comprovante de {r.morador.nome}
                        </div>
                        
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            onClick={() => handleRejeitarPagamento(r.pagamentoId!)}
                            className="cyber-btn cyber-btn-danger"
                            style={{ padding: '6px 12px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                          >
                            <X size={12} /> Recusar
                          </button>
                          
                          <button
                            onClick={() => handleConfirmPagamento(r.pagamentoId!)}
                            className="cyber-btn"
                            style={{ padding: '6px 12px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                          >
                            <Check size={12} /> Confirmar
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button
                onClick={() => {
                  setShowRateioModal(false);
                  setRateioError(null);
                  setComprovanteArquivo(null);
                }}
                className="cyber-btn cyber-btn-secondary"
                style={{ padding: '10px 20px', fontSize: '0.85rem' }}
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
