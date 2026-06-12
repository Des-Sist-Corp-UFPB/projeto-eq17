-- Migração V5: Adição de ativação de usuário e tabela de tokens de verificação de e-mail
ALTER TABLE usuario ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT FALSE;

-- Ativar o usuário administrador padrão cadastrado na V2
UPDATE usuario SET ativo = TRUE WHERE email = 'admin@republicas.com';

-- Tabela de Tokens de Verificação de E-mail
CREATE TABLE token_verificacao (
    id            BIGSERIAL PRIMARY KEY,
    token         VARCHAR(100)             NOT NULL UNIQUE,
    usuario_id    BIGINT                   NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    expira_em     TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_token_verificacao_token ON token_verificacao(token);
