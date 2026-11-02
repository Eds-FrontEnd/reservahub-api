
# ============================================================================
# DOCKERFILE DA API RESERVAHUB
# ============================================================================

# ----------------------------------------------------------------------------
# ESTÁGIO 1 — BUILD
# ----------------------------------------------------------------------------

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia os arquivos de configuração do Maven primeiro para aproveitar o cache.
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Corrige quebras de linha do Windows e prepara o Maven Wrapper.
RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B --no-transfer-progress dependency:go-offline

# Copia o código-fonte.
COPY src ./src

# Compila e gera o JAR executável do Spring Boot.
RUN ./mvnw -B --no-transfer-progress -DskipTests package

# ----------------------------------------------------------------------------
# ESTÁGIO 2 — RUNTIME
# ----------------------------------------------------------------------------

FROM eclipse-temurin:21-jre-alpine

# Cria usuário e grupo sem privilégios administrativos.
RUN addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app

# Copia o JAR gerado pelo Maven.
COPY --from=builder --chown=spring:spring \
    /build/target/api-0.0.1-SNAPSHOT.jar app.jar

# Diretório de uploads configurável.
RUN mkdir -p /app/uploads/products \
    && chown -R spring:spring /app/uploads

# Copia as imagens dos produtos existentes para dentro do contêiner.
COPY --chown=spring:spring \
    uploads/products/ /app/uploads/products/

ENV APP_UPLOAD_DIR=/app/uploads

USER spring

EXPOSE 8080

# Limita a memória máxima da JVM em relação ao limite do contêiner.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
