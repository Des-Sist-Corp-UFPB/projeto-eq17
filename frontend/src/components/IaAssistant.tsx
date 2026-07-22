import React, { useState, useEffect, useRef } from 'react';
import { api } from '../services/api';
import { MessageSquare, X, Send, Sparkles, User, Loader2 } from 'lucide-react';

interface Mensagem {
  id: number;
  remetente: 'usuario' | 'ia';
  texto: string;
}

export default function IaAssistant() {
  const [isOpen, setIsOpen] = useState(false);
  const [mensagens, setMensagens] = useState<Mensagem[]>([
    {
      id: 1,
      remetente: 'ia',
      texto: 'Olá! Sou o **HomeHub Assistant**. Como posso ajudar você e sua república hoje? Você pode me pedir coisas como:\n\n- *"Qual o meu saldo devedor?"*\n- *"Lança uma despesa de supermercado no valor de R$ 150 vencendo amanhã"* \n- *"Quem é o responsável por pagar as contas?"*\n- *"Envie um aviso para os moradores de que a internet vai cair amanhã"*'
    }
  ]);
  const [inputText, setInputText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  // Rolagem automática para a última mensagem
  useEffect(() => {
    if (chatEndRef.current) {
      chatEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [mensagens, isOpen]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || isLoading) return;

    const userMessageText = inputText;
    setInputText('');

    const novoIdUsuario = Date.now();
    setMensagens(prev => [...prev, { id: novoIdUsuario, remetente: 'usuario', texto: userMessageText }]);
    setIsLoading(true);

    try {
      const response = await api.post<{ resposta: string }>('/api/chat', { mensagem: userMessageText });
      
      setMensagens(prev => [...prev, { 
        id: Date.now() + 1, 
        remetente: 'ia', 
        texto: response.resposta || 'Não consegui processar a resposta da inteligência.' 
      }]);
    } catch (err: any) {
      setMensagens(prev => [...prev, { 
        id: Date.now() + 1, 
        remetente: 'ia', 
        texto: '❌ Desculpe, ocorreu um erro ao tentar falar com a IA: ' + err.message 
      }]);
    } finally {
      setIsLoading(false);
    }
  };

  // Helper para renderizar quebras de linha e negrito básico
  const formatarMensagem = (texto: string) => {
    return texto.split('\n').map((linha, index) => {
      // Substituição simples para negrito **texto**
      const partes = linha.split(/\*\*([^*]+)\*\*/g);
      const linhaFormatada = partes.map((parte, i) => {
        if (i % 2 === 1) {
          return <strong key={i}>{parte}</strong>;
        }
        return parte;
      });

      return (
        <span key={index} style={{ display: 'block', minHeight: linha.trim() === '' ? '12px' : 'auto' }}>
          {linhaFormatada}
        </span>
      );
    });
  };

  return (
    <>
      {/* Botão flutuante para abrir o chat */}
      <button
        onClick={() => setIsOpen(true)}
        style={{
          position: 'fixed',
          bottom: '24px',
          right: '24px',
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          background: 'linear-gradient(135deg, var(--color-primary, #6366f1) 0%, var(--color-accent-orange, #f97316) 100%)',
          color: 'white',
          border: 'none',
          boxShadow: '0 4px 14px rgba(99, 102, 241, 0.4)',
          cursor: 'pointer',
          display: isOpen ? 'none' : 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
          transition: 'all 0.3s ease',
        }}
        title="Conversar com assistente de IA"
      >
        <Sparkles size={24} />
      </button>

      {/* Caixa de diálogo do chat */}
      {isOpen && (
        <div
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            width: '380px',
            height: '550px',
            backgroundColor: '#ffffff',
            borderRadius: '16px',
            boxShadow: '0 8px 30px rgba(0, 0, 0, 0.15)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            zIndex: 9999,
            border: '1px solid rgba(0,0,0,0.08)',
            fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
          }}
        >
          {/* Cabeçalho do Chat */}
          <div
            style={{
              padding: '16px',
              background: 'linear-gradient(135deg, var(--color-primary, #6366f1) 0%, #4f46e5 100%)',
              color: 'white',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <div
                style={{
                  background: 'rgba(255, 255, 255, 0.2)',
                  padding: '6px',
                  borderRadius: '8px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Sparkles size={18} />
              </div>
              <div>
                <h6 style={{ margin: 0, fontWeight: 600, fontSize: '14px' }}>HomeHub Assistant</h6>
                <span style={{ fontSize: '11px', opacity: 0.8 }}>IA especialista em repúblicas</span>
              </div>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              style={{
                background: 'transparent',
                border: 'none',
                color: 'white',
                cursor: 'pointer',
                opacity: 0.8,
                padding: '4px',
              }}
            >
              <X size={18} />
            </button>
          </div>

          {/* Corpo do Chat / Mensagens */}
          <div
            style={{
              flex: 1,
              padding: '16px',
              overflowY: 'auto',
              backgroundColor: '#f8fafc',
              display: 'flex',
              flexDirection: 'column',
              gap: '12px',
            }}
          >
            {mensagens.map(msg => (
              <div
                key={msg.id}
                style={{
                  alignSelf: msg.remetente === 'usuario' ? 'flex-end' : 'flex-start',
                  maxWidth: '85%',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: msg.remetente === 'usuario' ? 'flex-end' : 'flex-start',
                }}
              >
                {/* Nome do Remetente */}
                <span style={{ fontSize: '10px', color: '#94a3b8', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  {msg.remetente === 'usuario' ? (
                    <>Você <User size={10} /></>
                  ) : (
                    <>HomeHub AI <Sparkles size={10} style={{ color: 'var(--color-accent-orange, #f97316)' }} /></>
                  )}
                </span>
                {/* Balão da Mensagem */}
                <div
                  style={{
                    padding: '12px 14px',
                    borderRadius: msg.remetente === 'usuario' ? '12px 12px 0 12px' : '12px 12px 12px 0',
                    backgroundColor: msg.remetente === 'usuario' ? '#6366f1' : '#ffffff',
                    color: msg.remetente === 'usuario' ? '#ffffff' : '#334155',
                    fontSize: '13px',
                    lineHeight: '1.5',
                    boxShadow: msg.remetente === 'usuario' ? '0 2px 6px rgba(99, 102, 241, 0.2)' : '0 2px 6px rgba(0,0,0,0.03)',
                    border: msg.remetente === 'usuario' ? 'none' : '1px solid rgba(0,0,0,0.04)',
                    whiteSpace: 'pre-wrap',
                  }}
                >
                  {formatarMensagem(msg.texto)}
                </div>
              </div>
            ))}

            {/* Indicador de Carregamento (IA pensando) */}
            {isLoading && (
              <div style={{ alignSelf: 'flex-start', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontSize: '10px', color: '#94a3b8' }}>Digitando...</span>
                <Loader2 size={14} className="animate-spin" style={{ color: '#6366f1', animation: 'spin 1s linear infinite' }} />
              </div>
            )}
            <div ref={chatEndRef} />
          </div>

          {/* Formulário de Input do Chat */}
          <form
            onSubmit={handleSend}
            style={{
              padding: '12px',
              backgroundColor: '#ffffff',
              borderTop: '1px solid rgba(0,0,0,0.06)',
              display: 'flex',
              gap: '8px',
            }}
          >
            <input
              type="text"
              value={inputText}
              onChange={e => setInputText(e.target.value)}
              placeholder="Pergunte ao assistente da república..."
              disabled={isLoading}
              style={{
                flex: 1,
                padding: '10px 14px',
                borderRadius: '24px',
                border: '1px solid #cbd5e1',
                fontSize: '13px',
                outline: 'none',
                transition: 'border-color 0.2s',
              }}
              onFocus={e => (e.target.style.borderColor = '#6366f1')}
              onBlur={e => (e.target.style.borderColor = '#cbd5e1')}
            />
            <button
              type="submit"
              disabled={!inputText.trim() || isLoading}
              style={{
                width: '38px',
                height: '38px',
                borderRadius: '50%',
                backgroundColor: inputText.trim() && !isLoading ? '#6366f1' : '#f1f5f9',
                color: inputText.trim() && !isLoading ? '#ffffff' : '#94a3b8',
                border: 'none',
                cursor: inputText.trim() && !isLoading ? 'pointer' : 'default',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transition: 'all 0.2s',
              }}
            >
              <Send size={16} />
            </button>
          </form>
        </div>
      )}

      {/* Estilo para animação spin em CSS inline */}
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </>
  );
}
