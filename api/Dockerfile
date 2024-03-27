
FROM maven:3.8.4-openjdk-17 AS build

# Defina o diretório de trabalho dentro do container temporário de build
WORKDIR /build

# Ajuste os caminhos no comando COPY para corresponder à estrutura do seu projeto
# Aqui assumimos que `api` é um subdiretório do contexto de build
COPY src/ /build/src/
COPY pom.xml /build/

# Executa o build do projeto Maven, pulando os testes
RUN mvn clean package -Dmaven.test.skip

# Segunda etapa: Construção da imagem final baseada no OpenJDK
FROM openjdk:17

# Expor a porta em que a aplicação vai rodar
EXPOSE 8080

# Copie o artefato JAR gerado na primeira etapa para o container final
COPY --from=build /build/target/*.jar /app.jar

# Defina o comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "/app.jar"]