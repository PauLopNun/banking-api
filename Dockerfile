# ─── FASE 1: BUILD ───────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la caché de Docker
# Si el pom no cambia, no re-descarga dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── FASE 2: RUN ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos solo el jar generado en la fase anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone la app
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
```

También crea `.dockerignore` en la raíz para que Docker no copie ficheros innecesarios:
```
target/
.git/
.gitignore
*.md
.idea/