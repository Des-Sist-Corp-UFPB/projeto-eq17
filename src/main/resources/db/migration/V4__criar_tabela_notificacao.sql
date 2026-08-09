-- Migração V4: Criação da tabela de notificações dos usuários
CREATE TABLE notificacao (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT                   NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    titulo        VARCHAR(150)             NOT NULL,
    mensagem      TEXT                     NOT NULL,
    tipo          VARCHAR(50)              NOT NULL, -- DESPESA_CRIADA, VENCIMENTO_PROXIMO, TAREFA_ATRIBUIDA
    lida          BOOLEAN                  NOT NULL DEFAULT FALSE,
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notificacao_usuario ON notificacao(usuario_id);
CREATE INDEX idx_notificacao_lida ON notificacao(usuario_id, lida);
