# Avaliação — EQ17 (DSC)

**Data:** 2026-07-01  
**Avaliador:** Prof. Rodrigo  
**Método:** verificação automática cruzando o que o `README.md` declara com evidências no código-fonte (leitura de `origin/main`).

> Esta é uma avaliação automática preliminar. O que não estiver documentado no README e commitado no repositório é considerado não atendido.

---

## 1. Log de Auditoria

✅ **Atendido** — documentado no README e com 149 evidência(s) no código.

---

## 2. Integração com Serviço Externo

- ✅ **Google OAuth2** — declarado no README e comprovado no código (3 ocorrência(s)).
  - Evidência: `src/main/java/br/ufpb/dsc/republica/config/SecurityConfig.java:84:                .oauth2Login(oauth2 -> oauth2`

---

## 3. Cobertura de Testes (≥ 85%)

✅ **Atendido** — backend linhas 94.3% (instruções 91.4% · ramos 73.3%) [JaCoCo]; frontend linhas 100% (JS) (relatório em `cobertura/`, 50 arquivo(s)).

> Critério: **cobertura de linhas** ≥ 85% (conforme a orientação). As demais métricas (instruções/ramos) são informativas.

> Observação: a cobertura é lida do relatório commitado pela equipe; não é recalculada nesta avaliação.

---

*Avaliação gerada automaticamente em 2026-07-01. Consulte `ORIENTACOES-AVALIACAO-2026-06-29.md` para os critérios.*