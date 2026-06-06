-- Migração V1: Criação das tabelas do sistema de gestão de repúblicas e moradias compartilhadas

-- Tabela de Usuários
CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(150)             NOT NULL,
    email         VARCHAR(150)             NOT NULL UNIQUE,
    senha         VARCHAR(100)             NOT NULL,
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Tabela de Casas (Repúblicas)
CREATE TABLE casa (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(150)             NOT NULL,
    endereco      TEXT                     NOT NULL,
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Tabela de Moradores (Associação de Usuário a uma Casa com papel)
CREATE TABLE morador (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT                   NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    casa_id       BIGINT                   NOT NULL REFERENCES casa(id) ON DELETE CASCADE,
    papel         VARCHAR(50)              NOT NULL, -- ADMINISTRADOR, MORADOR
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_usuario_casa UNIQUE(usuario_id, casa_id)
);

-- Tabela de Despesas da Casa
CREATE TABLE despesa (
    id             BIGSERIAL PRIMARY KEY,
    casa_id        BIGINT                   NOT NULL REFERENCES casa(id) ON DELETE CASCADE,
    descricao      VARCHAR(200)             NOT NULL,
    valor_total    NUMERIC(10, 2)           NOT NULL CHECK (valor_total >= 0),
    vencimento     DATE                     NOT NULL,
    responsavel_id BIGINT                   NOT NULL REFERENCES morador(id) ON DELETE RESTRICT,
    status         VARCHAR(50)              NOT NULL, -- PENDENTE, PARCIALMENTE_PAGA, PAGA, ATRASADA
    criado_em      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    excluido       BOOLEAN                  NOT NULL DEFAULT FALSE
);

-- Tabela de Rateio de Despesa entre Moradores
CREATE TABLE despesa_rateio (
    id            BIGSERIAL PRIMARY KEY,
    despesa_id    BIGINT                   NOT NULL REFERENCES despesa(id) ON DELETE CASCADE,
    morador_id    BIGINT                   NOT NULL REFERENCES morador(id) ON DELETE CASCADE,
    valor_devido  NUMERIC(10, 2)           NOT NULL CHECK (valor_devido >= 0),
    CONSTRAINT unique_despesa_morador UNIQUE(despesa_id, morador_id)
);

-- Tabela de Pagamentos individuais do rateio
CREATE TABLE pagamento (
    id            BIGSERIAL PRIMARY KEY,
    rateio_id     BIGINT                   NOT NULL REFERENCES despesa_rateio(id) ON DELETE CASCADE,
    data_pagamento TIMESTAMP WITH TIME ZONE,
    comprovante   VARCHAR(255),
    status        VARCHAR(50)              NOT NULL, -- PENDENTE, INFORMADO, CONFIRMADO, REJEITADO
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Tabela de Tarefas Domésticas
CREATE TABLE tarefa (
    id             BIGSERIAL PRIMARY KEY,
    casa_id        BIGINT                   NOT NULL REFERENCES casa(id) ON DELETE CASCADE,
    descricao      VARCHAR(255)             NOT NULL,
    status         VARCHAR(50)              NOT NULL, -- PENDENTE, EM_ANDAMENTO, CONCLUIDA
    responsavel_id BIGINT                            REFERENCES morador(id) ON DELETE SET NULL,
    criado_em      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Índices de performance
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_morador_casa ON morador(casa_id);
CREATE INDEX idx_despesa_casa ON despesa(casa_id);
CREATE INDEX idx_tarefa_casa ON tarefa(casa_id);

-- Inserção de usuário administrador padrão (senha: admin123)
-- Hash BCrypt gerado para 'admin123'
INSERT INTO usuario (nome, email, senha)
VALUES ('Administrador', 'admin@republicas.com', '$2a$10$8.ZGyfMR8RXcLzI68D.TNu42I5x88c/9P9D.P6f1lM29q4b1lM29q');
