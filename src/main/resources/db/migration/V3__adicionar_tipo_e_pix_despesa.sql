-- Migração V3: Adiciona o tipo de despesa (FIXA, OCASIONAL) e a chave PIX para pagamento
ALTER TABLE despesa ADD COLUMN tipo VARCHAR(50) NOT NULL DEFAULT 'OCASIONAL';
ALTER TABLE despesa ADD COLUMN chave_pix VARCHAR(150);
