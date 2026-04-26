# --- Etapa 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copiar pom y descargar dependencias (Aprovechar caché de capas)
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Run ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Por seguridad: no correr como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el jar generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto del servicio
EXPOSE 8083

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
