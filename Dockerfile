# ==============================================================================
# TASKVOICE — RENDER DOCKERFILE (MULTI-STAGE BUILD)
# Stage 1: Build WAR with Maven & OpenJDK 17
# Stage 2: Run Tomcat 10 (Jakarta EE 10 / Servlet 6.0)
# ==============================================================================

# Step 1: Build Phase
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY database ./database
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Runtime Phase
FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat

# Remove default Tomcat webapps
RUN rm -rf webapps/*

# Copy packaged WAR as ROOT.war so app runs at domain root (e.g., https://your-app.onrender.com/)
COPY --from=builder /app/target/taskvoice.war webapps/ROOT.war

# Expose Render PORT (default 8080 or PORT env var)
EXPOSE 8080

CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/g\" conf/server.xml && bin/catalina.sh run"]
