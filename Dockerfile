# Builds the React frontend and the Java backend into one image.
# The Java server (WebServer) serves the API under /api and the
# built website from frontend/dist, so one container runs everything.

# ---------- 1. Build the website ----------
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------- 2. Build the Java jar ----------
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- 3. Run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend /app/target/personal-lifestyle-coach.jar ./app.jar
COPY --from=frontend /app/frontend/dist ./frontend/dist

# Render sends traffic to port 10000 by default.
# GEMINI_API_KEY and USDA_API_KEY must be set in Render's environment settings.
ENV WEB_PORT=10000
EXPOSE 10000

CMD ["java", "-jar", "app.jar"]
