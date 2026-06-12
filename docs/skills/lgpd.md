name: Implementação de LGPD no Sistema de Moradias Compartilhadas
description: Implements LGPD (Brazilian Data Protection Law) compliance features, including user consent, privacy policy, data access requests, account deletion requests, and audit logging. Ensures transparency, traceability, and proper handling of personal data while following the project's architecture and business rules.

## Objetivo

Garantir que o sistema esteja minimamente adequado aos princípios da Lei Geral de Proteção de Dados (LGPD - Lei nº 13.709/2018), implementando funcionalidades que permitam transparência, consentimento, acesso aos dados, exclusão de conta e rastreabilidade de ações.

Esta skill deve ser utilizada sempre que forem implementadas funcionalidades relacionadas a usuários, autenticação, armazenamento de dados pessoais ou operações que manipulem informações sensíveis.

---

# Escopo desta Skill

Os seguintes requisitos LGPD fazem parte da primeira versão do projeto:

1. Consentimento para tratamento de dados pessoais.
2. Política de privacidade.
3. Exportação dos dados do usuário.
4. Solicitação de exclusão da conta.
5. Auditoria de ações relevantes.

---

# 1. Consentimento de Uso dos Dados

## Objetivo

Registrar que o usuário concordou com o tratamento de seus dados pessoais antes de utilizar a plataforma.

## Regras

* O cadastro só pode ser concluído após o aceite explícito dos termos.
* O aceite não pode vir marcado por padrão.
* O sistema deve armazenar:

  * Data e hora do aceite.
  * Versão do termo aceito.

## Exemplo de Campos

Usuario

* aceitouTermosLgpd
* dataAceiteLgpd
* versaoTermoLgpd

## Critérios de Aceitação

* Usuário não consegue se cadastrar sem aceitar os termos.
* Data e versão do termo ficam registradas no banco.

---

# 2. Política de Privacidade

## Objetivo

Permitir que qualquer usuário consulte facilmente como seus dados são tratados.

## Regras

Criar uma página pública:

/politica-de-privacidade

A página deve explicar:

* Quais dados são coletados.
* Finalidade da coleta.
* Como os dados são armazenados.
* Direitos do titular dos dados.
* Como solicitar exclusão da conta.

## Dados Tratados pelo Sistema

* Nome.
* Email.
* Dados de participação em casas.
* Despesas.
* Pagamentos.
* Tarefas.
* Registros de auditoria.

## Critérios de Aceitação

* Página acessível sem autenticação.
* Link disponível na tela de cadastro e login.

---

# 3. Direito de Acesso aos Dados

## Objetivo

Permitir que o usuário visualize ou exporte todos os dados que o sistema mantém sobre ele.

## Endpoint Sugerido

GET /meus-dados

## Dados Exportados

* Dados cadastrais.
* Casas das quais participa.
* Histórico de despesas.
* Histórico de pagamentos.
* Histórico de tarefas.
* Registros de auditoria relacionados ao usuário.

## Formato

Preferencialmente JSON.

Opcionalmente PDF.

## Critérios de Aceitação

* Apenas o próprio usuário pode acessar seus dados.
* Dados retornados devem ser completos e legíveis.

---

# 4. Solicitação de Exclusão da Conta

## Objetivo

Garantir ao usuário o direito de solicitar a remoção de seus dados pessoais.

## Regra de Negócio

O usuário NÃO poderá ser excluído fisicamente se possuir:

* Despesas pendentes.
* Pagamentos pendentes.
* Responsabilidades financeiras em aberto.

Nesses casos deve ser realizada anonimização.

## Estratégia de Anonimização

Substituir:

* Nome.
* Email.

Por informações genéricas.

Exemplo:

Nome:
Usuario Removido #123

Email:
[removido123@sistema.local](mailto:removido123@sistema.local)

## Critérios de Aceitação

* Histórico financeiro permanece íntegro.
* Dados pessoais deixam de identificar o usuário.
* Operação registrada na auditoria.

---

# 5. Auditoria

## Objetivo

Manter rastreabilidade das ações importantes executadas no sistema.

## Entidade Sugerida

Auditoria

* id
* usuarioId
* acao
* descricao
* dataHora
* enderecoIp
* entidadeAfetada
* entidadeId

## Ações que Devem Ser Auditadas

### Usuários

* Cadastro.
* Login.
* Solicitação de exclusão.
* Exportação de dados.

### Despesas

* Criação.
* Alteração.
* Exclusão lógica.
* Confirmação de pagamento.

### Tarefas

* Criação.
* Alteração.
* Conclusão.

### Casas

* Criação.
* Entrada de moradores.
* Remoção de moradores.

## Exemplo de Registro

Usuario: João Silva

Ação:
CONFIRMAR_PAGAMENTO

Descrição:
Confirmou pagamento da conta de energia referente ao mês de junho.

Data:
2026-06-12T20:30:00

## Critérios de Aceitação

* Toda ação relevante gera registro.
* Registros nunca podem ser alterados manualmente.
* Apenas administradores podem consultar auditorias completas.

---

# Boas Práticas

* Utilizar Soft Delete sempre que possível.
* Não armazenar senhas em texto puro.
* Utilizar BCryptPasswordEncoder.
* Não expor dados pessoais desnecessários em APIs.
* Aplicar princípio do menor privilégio.
* Toda funcionalidade LGPD deve possuir testes automatizados.

---

# Fora do Escopo da Primeira Versão

As seguintes funcionalidades poderão ser implementadas futuramente:

* Gestão de consentimentos múltiplos.
* Revogação de consentimento.
* Notificações de vazamento.
* Controle avançado de retenção de dados.
* Relatório de impacto à proteção de dados.
* Painel administrativo LGPD.

---

# Instruções para Agentes de IA

Sempre que implementar funcionalidades relacionadas a usuários:

1. Verificar se existe impacto LGPD.
2. Avaliar necessidade de auditoria.
3. Não excluir dados financeiros históricos.
4. Priorizar anonimização em vez de exclusão física.
5. Respeitar controle de acesso aos dados pessoais.
6. Garantir que o usuário consiga acessar seus próprios dados.
7. Registrar ações críticas na tabela de auditoria.
8. Manter compatibilidade com Spring Security e arquitetura em camadas.
9. Nunca implementar lógica de LGPD diretamente em Controllers.
10. Toda regra de negócio deve estar na camada Service.
