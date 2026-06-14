# =============================================================================
# Dockerfile de PRODUÇÃO — Multi-stage build
# =============================================================================
# Estágio 1 (builder): compila o projeto com Maven e gera o JAR.
# Estágio 2 (runtime): imagem mínima com apenas o JRE, sem ferramentas de build.
#
# Vantagens do multi-stage:
#   - Imagem final muito menor (sem Maven, sem fontes Java, sem classes temp.)
#   - Superfície de ataque reduzida (menos binários = menos vulnerabilidades)
#   - Separação entre ambiente de build e runtime
#
# Como construir:
#   docker build -f docker/Dockerfile -t republica:latest .
# =============================================================================

# ---- Estágio 1: Build do Frontend React --------------------------------------
FROM node:20-alpine AS frontend-builder

WORKDIR /frontend

# Instalar dependências e compilar
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ---- Estágio 2: Build do Backend Java ----------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS backend-builder

WORKDIR /build

# Copiar pom.xml para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copiar código-fonte
COPY src ./src

# Copiar o build do React para a pasta de arquivos estáticos do Spring Boot
# Assim, o Spring Boot servirá o React de forma integrada na porta 8080
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static

# Compilar e gerar o JAR integrado
RUN mvn clean package -DskipTests -B -q

# ---- Estágio 3: Imagem de Runtime JRE ---------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Criar usuário não-root por segurança
RUN groupadd --gid 1001 republica && \
    useradd --uid 1001 --gid republica --shell /bin/false republica

WORKDIR /app

# Copiar apenas o JAR gerado no estágio do backend
COPY --from=backend-builder /build/target/*.jar app.jar

# Criar diretório de uploads e definir permissões recursivas para o usuário republica
RUN mkdir -p /app/uploads/comprovantes && \
    chown -R republica:republica /app

# Trocar para usuário não-root
USER republica

EXPOSE 8080

# Health check: Actuator expõe /actuator/health
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Opções JVM para produção
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]

