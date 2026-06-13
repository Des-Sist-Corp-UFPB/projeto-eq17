-- Migração V5: Adequação à LGPD (Campos de consentimento e tabela de auditoria)

-- Adiciona campos de consentimento da LGPD na tabela de usuários
ALTER TABLE usuario ADD COLUMN aceitou_termos_lgpd BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN data_aceite_lgpd TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuario ADD COLUMN versao_termo_lgpd VARCHAR(50);

-- Criação da tabela de Auditoria
CREATE TABLE auditoria (
    id                BIGSERIAL PRIMARY KEY,
    usuario_id        BIGINT                   REFERENCES usuario(id) ON DELETE SET NULL,
    acao              VARCHAR(100)             NOT NULL,
    descricao         TEXT                     NOT NULL,
    data_hora         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    endereco_ip       VARCHAR(45),
    entidade_afetada  VARCHAR(100),
    entidade_id       BIGINT
);

-- Índices para otimização de consultas na auditoria
CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_auditoria_data_hora ON auditoria(data_hora);
