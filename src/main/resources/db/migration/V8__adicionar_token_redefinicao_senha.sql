ALTER TABLE usuario ADD COLUMN token_redefinicao_senha VARCHAR(100);
ALTER TABLE usuario ADD COLUMN validade_token_redefinicao TIMESTAMP;
