import { Link, useNavigate } from 'react-router-dom';
import { ShieldCheck, ArrowLeft, Eye, Database, FileText, Trash2, Shield } from 'lucide-react';

export default function PoliticaPrivacidade() {
  const navigate = useNavigate();

  return (
    <div style={{
      minHeight: '100vh',
      padding: '40px 24px',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
    }}>
      <div className="cyber-card" style={{
        width: '100%',
        maxWidth: '800px',
        padding: '40px',
        margin: '20px 0',
      }}>
        {/* Top Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <button
            onClick={() => navigate(-1)}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--color-accent-blue)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              fontFamily: 'var(--font-mono)',
              fontSize: '0.85rem',
              fontWeight: 600,
              padding: '0',
            }}
          >
            <ArrowLeft size={16} /> VOLTAR
          </button>
          <span className="cyber-badge cyber-badge-blue">
            <ShieldCheck size={14} /> LGPD CONFORME
          </span>
        </div>

        <div style={{ marginBottom: '36px' }}>
          <h1 style={{
            fontSize: '2.5rem',
            fontWeight: 800,
            color: 'var(--text-primary)',
            letterSpacing: '-0.02em',
            lineHeight: 1.2,
          }}>
            Política de Privacidade
          </h1>
          <p style={{
            fontSize: '0.95rem',
            color: 'var(--text-secondary)',
            marginTop: '8px',
            fontFamily: 'var(--font-mono)',
          }}>
            Termos de Uso e Proteção de Dados Pessoais • Versão 1.0 (Junho/2026)
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px', color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '0.95rem' }}>
          
          <section>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-primary)', marginBottom: '12px', fontSize: '1.25rem' }}>
              <Eye size={18} style={{ color: 'var(--color-accent-blue)' }} /> 1. Quais dados coletamos?
            </h3>
            <p>
              Para o correto funcionamento do HomeHub (Gestão de Repúblicas Universitárias), coletamos e processamos os seguintes dados pessoais fornecidos diretamente por você ou resultantes da utilização do sistema:
            </p>
            <ul style={{ paddingLeft: '20px', marginTop: '8px' }}>
              <li><strong>Dados Cadastrais:</strong> Nome completo, endereço de e-mail e senha (armazenada de forma criptografada usando algoritmo hash forte BCrypt).</li>
              <li><strong>Dados de Associação:</strong> Histórico de participação nas repúblicas (casas), incluindo o papel desempenhado (Administrador ou Morador).</li>
              <li><strong>Dados Financeiros e de Despesas:</strong> Despesas lançadas, chaves PIX fornecidas para pagamento, e histórico de pagamentos informados ou confirmados.</li>
              <li><strong>Dados de Tarefas:</strong> Tarefas domésticas atribuídas a você e o status de conclusão delas.</li>
              <li><strong>Logs de Auditoria:</strong> Ações realizadas na plataforma (cadastro, login, exportação de dados, exclusão de conta, transações financeiras e alteração de tarefas) associadas ao seu endereço IP para fins de segurança e rastreabilidade conforme exigido legalmente.</li>
            </ul>
          </section>

          <section>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-primary)', marginBottom: '12px', fontSize: '1.25rem' }}>
              <Database size={18} style={{ color: 'var(--color-accent-blue)' }} /> 2. Qual a finalidade do tratamento?
            </h3>
            <p>
              Todos os dados coletados têm fins específicos ligados diretamente à execução do serviço da plataforma:
            </p>
            <ul style={{ paddingLeft: '20px', marginTop: '8px' }}>
              <li>Viabilizar a divisão matemática exata e o rateio de despesas comuns entre os moradores da casa.</li>
              <li>Atribuir responsabilidades domésticas e organizar tarefas entre os integrantes de uma república.</li>
              <li>Garantir a segurança física e digital das informações por meio de logs de auditoria detalhados e prevenção a fraudes ou acessos indevidos.</li>
            </ul>
          </section>

          <section>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-primary)', marginBottom: '12px', fontSize: '1.25rem' }}>
              <FileText size={18} style={{ color: 'var(--color-accent-blue)' }} /> 3. Como os dados são armazenados e protegidos?
            </h3>
            <p>
              Seus dados pessoais são armazenados em banco de dados relacional seguro. Adotamos práticas rígidas de segurança da informação, como criptografia de senhas por BCrypt, controle estrito de acessos baseado no princípio do privilégio mínimo (apenas moradores autorizados acessam dados de suas respectivas casas) e logs de auditoria imutáveis.
            </p>
          </section>

          <section>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-primary)', marginBottom: '12px', fontSize: '1.25rem' }}>
              <Shield size={18} style={{ color: 'var(--color-accent-blue)' }} /> 4. Quais são seus direitos como titular dos dados?
            </h3>
            <p>
              Em total conformidade com a Lei Geral de Proteção de Dados (LGPD - Lei nº 13.709/2018), asseguramos a você o exercício dos seguintes direitos:
            </p>
            <ul style={{ paddingLeft: '20px', marginTop: '8px' }}>
              <li><strong>Direito de Acesso:</strong> Você pode consultar ou exportar a totalidade dos dados que o sistema armazena sobre você a qualquer momento no formato estruturado JSON.</li>
              <li><strong>Direito de Exclusão/Anonimização:</strong> Você tem o direito de solicitar a eliminação dos seus dados pessoais coletados pela plataforma.</li>
            </ul>
          </section>

          <section>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-primary)', marginBottom: '12px', fontSize: '1.25rem' }}>
              <Trash2 size={18} style={{ color: 'var(--color-accent-blue)' }} /> 5. Como solicitar a exclusão ou exportação dos dados?
            </h3>
            <p>
              Você pode exportar seus dados pessoais e gerenciar a exclusão de sua conta através das configurações de perfil no seu painel principal (Dashboard). 
            </p>
            <div style={{
              background: 'rgba(79, 70, 229, 0.04)',
              border: '1px solid rgba(79, 70, 229, 0.1)',
              borderRadius: '10px',
              padding: '16px',
              marginTop: '12px',
              fontSize: '0.9rem'
            }}>
              <strong>Importante sobre despesas pendentes:</strong> Conforme as políticas de conformidade do HomeHub, o sistema impede a exclusão física caso existam despesas ou pagamentos em aberto sob sua responsabilidade. Nesses cenários, os dados cadastrais (nome, e-mail e foto) são <strong>anonimizados</strong> de forma definitiva, substituindo-os por dados genéricos para preservar a integridade histórica dos registros financeiros dos outros moradores de sua república.
            </div>
          </section>

        </div>

        <div style={{
          borderTop: '1px solid var(--border-muted)',
          marginTop: '40px',
          paddingTop: '24px',
          display: 'flex',
          justifyContent: 'center'
        }}>
          <Link to="/login" className="cyber-btn cyber-btn-secondary" style={{ textDecoration: 'none' }}>
            Ir para a Página Inicial
          </Link>
        </div>
      </div>
    </div>
  );
}
