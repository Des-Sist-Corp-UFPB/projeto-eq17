# Roteiro de Apresentação — HomeHub (Gestão de Repúblicas)
Este roteiro foi estruturado para destacar os pontos fortes exigidos na disciplina de **Desenvolvimento de Sistemas Corporativos (DSC)** pelo professor **Rodrigo Rebouças**.

A apresentação foi planejada para durar cerca de **20 a 25 minutos**, alternando entre explicações arquiteturais, demonstração de código de alto nível e uma live demo funcional do sistema.

---

## ⏱️ Cronograma Sugerido

```
[00:00 - 02:00]  Abertura e Contextualização
[02:00 - 06:00]  Arquitetura Técnica & Stack
[06:00 - 09:00]  Regras de Negócio Implementadas
[09:00 - 15:00]  Diferenciais Corporativos (Auditoria, LGPD, IA, MCP)
[15:00 - 19:00]  Qualidade de Código & DevOps (Tests, SAST, CI/CD)
[19:00 - 25:00]  Live Demo (Demonstração Prática)
[25:00 - 28:00]  Encerramento e Perguntas
```

---

## 🎙️ Passo a Passo da Apresentação

### 1. Abertura e Contextualização (Est. 2 min)
*   **Apresentação:** Diga seu nome e introduza o projeto **HomeHub**.
*   **O Problema:** Morar em república envolve atrito constante: divisão desigual de despesas comuns, falta de transparência sobre quem pagou o quê, perda de comprovantes e desorganização nas tarefas diárias de limpeza/manutenção.
*   **A Solução:** Um ecossistema de gestão inteligente voltado para estudantes, unindo facilidade de uso com recursos avançados de governança (controle de auditoria, adequação à LGPD, automação de tarefas por IA e suporte a clientes inteligentes via Model Context Protocol).

---

### 2. Arquitetura Técnica & Stack (Est. 4 min)
Explique como o projeto foi estruturado para suportar escalabilidade, separação de responsabilidades e facilidade de deploy.

*   **Padrão Single Page Application (SPA):** 
    *   Migração da arquitetura clássica server-side baseada em Thymeleaf/HTMX pura para um frontend em **React (TypeScript + Vite)** com backend em **Spring Boot 3.4.5**.
    *   **Ponto de Destaque no Código:** Mostre como o `SpaController.java` redireciona rotas não pertencentes à API para o `index.html`, permitindo ao React Router gerenciar as rotas no client-side de forma homogênea.
*   **Persistência e Migrações:**
    *   **PostgreSQL 16** como banco de dados.
    *   **Flyway 11** para controle de histórico de banco. Destaque que foram aplicadas **7 migrations** (`V2` a `V7`), mostrando a evolução do banco de dados (como criação do esquema inicial, adequação à LGPD e controle de tokens de e-mail).
*   **Padrões de Projeto Aplicados:**
    *   Camadas bem definidas: `Controller` (REST API), `Service` (regras de negócio com transações anotadas `@Transactional`), `Repository` (abstração de banco via Spring Data JPA) e `Domain` (entidades mapeadas).
    *   **Records Java** como DTOs (Data Transfer Objects) imutáveis para tráfego seguro de dados entre camadas.
    *   Tratamento global de exceções centralizado por meio de um `@ControllerAdvice`.

---

### 3. Regras de Negócio Principais (Est. 3 min)
Mostre que as funcionalidades básicas de um sistema de repúblicas foram mapeadas com rigor.

*   **Gestão de Repúblicas e Moradores:** Cadastro de casas e associação de moradores a essas casas com papéis específicos (`PapelMorador`).
*   **Lançamento e Divisão Inteligente de Despesas:** 
    *   Lançamento de despesas comuns (Internet, Aluguel, Água, etc.).
    *   Divisão proporcional automática entre os moradores ativos sob a forma de `DespesaRateio`.
*   **Controle de Pagamentos com Comprovantes:**
    *   O morador anexa o comprovante de pagamento no sistema.
    *   O administrador da república analisa o arquivo enviado (usando `UploadStorageService`) e pode aprovar ou rejeitar o pagamento.
*   **Gestão de Tarefas:** Criação e delegação de tarefas domésticas, controlando prazos e status (`StatusTarefa`).

---

### 4. Diferenciais Corporativos — O "Efeito UAU" (Est. 6 min)
*Esta é a parte crucial onde você demonstra recursos que vão além do básico de um projeto escolar comum.*

#### A. Rastreabilidade Corporativa (Log de Auditoria)
*   **O que é:** Um sistema que intercepta e salva na tabela `auditoria` do banco todas as alterações importantes executadas.
*   **O que audita:** Login de usuários (sucesso/falha), ações em repúblicas, adição de moradores, lançamentos de despesas e atualizações de tarefas.
*   **Como foi feito:** 
    *   No login: `AuthenticationEventListener.java` intercepta os eventos de sucesso e falha do Spring Security.
    *   Nas operações de negócio: Centralizado no `AuditoriaService.java` e chamado dentro de transações de modificação.
    *   Grava dados fundamentais: Usuário que operou, ação, IP de origem, entidade modificada, ID da entidade e a data/hora.

#### B. Conformidade LGPD (Direito do Usuário)
*   **O que é:** Adequação à Lei Geral de Proteção de Dados por meio do `LgpdController.java`.
*   **Recursos:**
    1.  **Direito de Portabilidade (Exportação):** O usuário pode baixar um arquivo contendo todas as informações sobre ele salvas no sistema em formato JSON.
    2.  **Direito ao Esquecimento (Remoção):** Permite a exclusão completa e segura da conta e dos dados pessoais do usuário, sem corromper a consistência do banco de dados (desvinculando dados históricos necessários).

#### C. Integrações Externas e Autenticação OAuth2
*   **Google OAuth2:** Login social integrado usando o Spring Security OAuth2 Client e o `CustomOAuth2SuccessHandler.java`. Se for o primeiro login do usuário, ele é cadastrado automaticamente com as informações públicas cedidas pelo Google.
*   **Confirmação de Conta via E-mail:** Registro padrão exige confirmação. O sistema gera um Token UUID de verificação, envia um e-mail em HTML por protocolo SMTP usando Gmail e o `EmailService.java`. O login é bloqueado se o e-mail não estiver confirmado.

#### D. Inteligência Artificial e Model Context Protocol (MCP)
*   **Assistente IA Embutido:** Uso do **Spring AI** integrado à API da OpenAI (OpenAI LLM).
*   **Chamada de Funções (Function Calling / Tools):** O assistente de IA não apenas responde dúvidas, mas executa ações diretas e seguras no banco de dados através do `RepublicaTools.java` (ex: lançar despesas, dividir contas e notificar moradores). Qualquer alteração disparada pela IA passa pela validação do usuário executor e grava no Log de Auditoria.
*   **Servidor MCP Completo:** O HomeHub expõe um endpoint SSE (`/mcp/messages`) e atua como um servidor de protocolo MCP, permitindo que IDEs habilitadas com IA (como o Cursor) ou assistentes de IA se conectem ao sistema da república, acessem o recurso do extrato financeiro (`republica://casa/{casaId}/extrato`) e invoquem ferramentas autorizadas de forma segura.

---

### 5. Qualidade de Código & DevOps (Est. 4 min)
Demonstre que a aplicação é robusta contra falhas e que segue as melhores práticas de entrega contínua.

*   **Testes com Testcontainers:**
    *   Destaque a cobertura de **94.29% no backend (Java)** e **100% no frontend (React)**.
    *   **Destaque Técnico:** Os testes de integração não usam bancos em memória fictícios (como H2). Eles sobem um container Docker real do **PostgreSQL** por meio de **Testcontainers**, garantindo que as migrations Flyway e as queries nativas se comportem exatamente como no ambiente de produção.
*   **Análise Estática de Segurança (SAST):**
    *   O projeto executa verificações de vulnerabilidade de dependências (OWASP Dependency Check consultando a API oficial do NVD), além de escaneamento de bugs com SpotBugs/FindSecBugs e verificação de containers/filesystem com Trivy.
*   **Pipeline CI/CD (GitHub Actions):**
    *   Mostre o arquivo de workflow `.github/workflows/deploy.yml`. Ele roda testes e SAST a cada push na branch `main`, gera a imagem Docker de produção e realiza o deploy no servidor `https://dsc.rodrigor.com` (atualizando a imagem por meio de chaves SSH de deploy configuradas).

---

## 🚀 Como Executar Localmente para Apresentação
```bash
# 1. Certifique-se de que o Docker Desktop está rodando.
# 2. No terminal, suba o banco de dados e a interface administrativa:
docker compose -f docker/docker-compose.dev.yml up postgres adminer -d

# 3. No terminal da aplicação backend, execute o Spring Boot:
mvn spring-boot:run

# 4. (Opcional) No terminal da pasta frontend, para rodar em modo desenvolvimento React:
cd frontend
npm run dev
```

---

> **Dica de Ouro para a Apresentação:** Deixe o Docker Desktop aberto e o banco Postgres rodando. Certifique-se de que a variável `SPRING_AI_OPENAI_API_KEY` esteja devidamente configurada no seu arquivo `.env` local para que o Chatbot com IA funcione perfeitamente durante a Live Demo!
