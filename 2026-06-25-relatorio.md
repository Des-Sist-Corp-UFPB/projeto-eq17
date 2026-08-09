# Relatório de Avaliação — EQ17 (DSC)

| | |
|---|---|
| **Data** | 2026-06-25 |
| **Repositório** | https://github.com/des-sist-corp-ufpb/projeto-eq17 |
| **Aplicação** | https://eq17.dsc.rodrigor.com |
| **Período de atividade** | 2026-06-22 → 2026-06-25 |
| **Total de commits** (sem merges, branch main) | 2 |
| **Integrantes** | @Ramonzin-dev, Ramon Alves Da Silva (@GimiliOgrande) |

---

## 1. Tecnologias

- Spring Boot 3.5.14
- Thymeleaf
- Flyway (4 migrations)
- Spring Security
- Testcontainers

---

## 2. Análise Funcional

### Endpoints REST (24 mapeados)

| Método | Path | Arquivo |
|--------|------|---------|
| `GET` | `/api/auth/me` | `AuthController.java` |
| `POST` | `/api/auth/register` | `CadastroController.java` |
| `GET` | `/api/casas/{id}` | `CasaController.java` |
| `POST` | `/api/casas` | `CasaController.java` |
| `POST` | `/api/casas/{id}/moradores` | `CasaController.java` |
| `GET` | `/api/dashboard` | `DashboardController.java` |
| `DELETE` | `/api/despesas/{id}` | `DespesaController.java` |
| `GET` | `/api/despesas/pagamento/{pagamentoId}/comprovante` | `DespesaController.java` |
| `GET` | `/api/despesas/{id}/rateios` | `DespesaController.java` |
| `POST` | `/api/despesas/casa/{casaId}` | `DespesaController.java` |
| `POST` | `/api/despesas/pagamento/{pagamentoId}/confirmar` | `DespesaController.java` |
| `POST` | `/api/despesas/pagamento/{pagamentoId}/rejeitar` | `DespesaController.java` |
| `POST` | `/api/despesas/rateio/{rateioId}/pagar` | `DespesaController.java` |
| `DELETE` | `/api/meus-dados` | `LgpdController.java` |
| `GET` | `/api/meus-dados` | `LgpdController.java` |
| `GET` | `/api/notificacoes` | `NotificacaoController.java` |
| `GET` | `/api/notificacoes/nao-lidas/count` | `NotificacaoController.java` |
| `PUT` | `/api/notificacoes/lidas` | `NotificacaoController.java` |
| `PUT` | `/api/notificacoes/{id}/lida` | `NotificacaoController.java` |
| `GET` | `/ping` | `PingController.java` |
| `GET` | `/` | `SpaController.java` |
| `DELETE` | `/api/tarefas/{id}` | `TarefaController.java` |
| `POST` | `/api/tarefas/casa/{casaId}` | `TarefaController.java` |
| `PUT` | `/api/tarefas/{id}/status` | `TarefaController.java` |

### Entidades / Tabelas (18 encontradas)

- `tarefa`
- `notificacao`
- `auditoria`
- `morador`
- `despesa`
- `pagamento`
- `casa`
- `usuario`
- `despesa_rateio`
- `auditoria (via V5__adequacao_lgpd.sql)`
- `notificacao (via V4__criar_tabela_notificacao.sql)`
- `usuario (via V2__criar_tabelas_sistema_republica.sql)`
- `casa (via V2__criar_tabelas_sistema_republica.sql)`
- `morador (via V2__criar_tabelas_sistema_republica.sql)`
- `despesa (via V2__criar_tabelas_sistema_republica.sql)`
- `despesa_rateio (via V2__criar_tabelas_sistema_republica.sql)`
- `pagamento (via V2__criar_tabelas_sistema_republica.sql)`
- `tarefa (via V2__criar_tabelas_sistema_republica.sql)`

### Migrations (4 arquivos)

- `V2__criar_tabelas_sistema_republica.sql`
- `V3__adicionar_tipo_e_pix_despesa.sql`
- `V4__criar_tabela_notificacao.sql`
- `V5__adequacao_lgpd.sql`

---

## 3. Análise Arquitetural

| Aspecto | Status | Observação |
|---------|--------|-----------|
| Arquitetura em camadas | ✅ | controller=✅  service=✅  repository=✅ |
| Testes automatizados | ✅ | 9 arquivo(s) de teste |
| Migrations versionadas | ✅ | 4 migration(s) |
| Logging | ❌ | não detectado |
| Autenticação / Segurança | ✅ | Spring Security / JWT / decorator detectado |
| DTOs / Separação de dados | ✅ | classes *DTO / *Request / *Response detectadas |
| Tratamento global de exceções | ✅ | @ControllerAdvice / @ExceptionHandler detectado |
| Documentação de API (OpenAPI) | ❌ | não detectado |
| Variáveis de ambiente | ❌ | não detectado |
| Dockerfile / docker-compose | ❌ | não encontrado |

---

## 4. Contribuição por Usuário

### Resumo

| Usuário | Commits (main) | Commits (GitHub API) | Linhas adicionadas | Linhas no código atual | % código atual |
|---------|---------------|---------------------|-------------------|----------------------|----------------|
| @Ramonzin-dev | 1 | **36** ⚠️ | 15.461 | 10.368 | 100% |
| Ramon Alves Da Silva (@GimiliOgrande) | 0 | **8** ⚠️ | 0 | 0 | 0% |
| *(sem login GitHub)* | 1 | 50% | — | — | — |

> **⚠️ Divergência entre commits locais e GitHub API:**
> - **@Ramonzin-dev**: 1 commit(s) na branch `main` vs **36** registrados na API GitHub (commits em branches não mergeadas ou absorvidos via squash-merge sem preservação de autoria).
> - **@GimiliOgrande**: 0 commit(s) na branch `main` vs **8** registrados na API GitHub (commits em branches não mergeadas ou absorvidos via squash-merge sem preservação de autoria).
>

### Contribuição por Camada

| Camada | Total linhas | @Ramonzin-dev | Ramon Alves Da Silva (@GimiliOgrande) |
|--------|-------------|---------|---------|
| Controller | 2.048 | 100% | 0% |
| Frontend | 792 | 100% | 0% |
| Repository | 138 | 100% | 0% |
| Service | 2.782 | 100% | 0% |

---

## 5. Contribuição por Funcionalidade

Baseado em `git blame` nos arquivos de controller e service.

| Arquivo | Total linhas | @Ramonzin-dev | Ramon Alves Da Silva (@GimiliOgrande) |
|---------|-------------|---------|---------|
| `UsuarioServiceTest.java` | 369 | 100% | 0% |
| `DespesaService.java` | 293 | 100% | 0% |
| `UsuarioService.java` | 258 | 100% | 0% |
| `DespesaServiceTest.java` | 247 | 100% | 0% |
| `layout.html` | 240 | 100% | 0% |
| `republica_fragments.html` | 237 | 100% | 0% |
| `casa_detalhes.html` | 229 | 100% | 0% |
| `DespesaController.java` | 219 | 100% | 0% |
| `TarefaServiceTest.java` | 208 | 100% | 0% |
| `UploadStorageServiceTest.java` | 197 | 100% | 0% |
| `AuditoriaServiceTest.java` | 168 | 100% | 0% |
| `NotificacaoServiceTest.java` | 162 | 100% | 0% |
| `CasaServiceTest.java` | 154 | 100% | 0% |
| `CasaController.java` | 148 | 100% | 0% |
| `TarefaService.java` | 146 | 100% | 0% |
| `cadastro.html` | 135 | 100% | 0% |
| `login.html` | 130 | 100% | 0% |
| `dashboard.html` | 122 | 100% | 0% |
| `NotificacaoService.java` | 103 | 100% | 0% |
| `CasaService.java` | 95 | 100% | 0% |
| `NotificacaoController.java` | 94 | 100% | 0% |
| `UploadStorageService.java` | 87 | 100% | 0% |
| `AuditoriaService.java` | 86 | 100% | 0% |
| `TarefaController.java` | 86 | 100% | 0% |
| `V2__criar_tabelas_sistema_republica.sql` | 84 | 100% | 0% |
| `api.ts` | 82 | 100% | 0% |
| `DashboardController.java` | 56 | 100% | 0% |
| `LgpdController.java` | 53 | 100% | 0% |
| `RepublicaApplicationTests.java` | 52 | 100% | 0% |
| `CadastroController.java` | 45 | 100% | 0% |
| `PingControllerTest.java` | 45 | 100% | 0% |
| `RepublicaApplication.java` | 42 | 100% | 0% |
| `AuthController.java` | 41 | 100% | 0% |
| `CustomUserDetailsService.java` | 33 | 100% | 0% |
| `PingController.java` | 22 | 100% | 0% |
| `V5__adequacao_lgpd.sql` | 22 | 100% | 0% |
| `SpaController.java` | 18 | 100% | 0% |
| `V4__criar_tabela_notificacao.sql` | 13 | 100% | 0% |
| `HomeController.java` | 6 | 100% | 0% |
| `V3__adicionar_tipo_e_pix_despesa.sql` | 3 | 100% | 0% |

---

*Relatório gerado automaticamente em 2026-06-25.*
*Os dados de contribuição são baseados em `git log --numstat` (linhas adicionadas) e `git blame` (linhas no código atual), excluindo commits de merge.*