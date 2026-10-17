# Stage 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar configuracao Maven e codigo fonte
COPY backend/pom.xml .
COPY backend/src ./src

# Compilar o jar de producao
RUN mvn clean package -DskipTests

# Stage 2: Imagem final de execucao
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar jar compilado
COPY --from=builder /app/target/*.jar app.jar

# Copiar storage inicial (catalogo, capas e roms)
COPY backend/storage ./storage

# Variaveis de ambiente padrao
ENV JAVA_OPTS="-Xmx400m -Xms128m -XX:MaxMetaspaceSize=128m -Xss512k"

# Porta da API Spring Boot
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
