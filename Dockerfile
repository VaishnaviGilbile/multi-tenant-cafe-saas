# Dockerfile — at project root
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/cafesaas-*.jar app.jar

# Render injects PORT dynamically — your app must listen on it
ENV PORT=8082
EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]