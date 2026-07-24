# =============================================================================
# Multi-stage Dockerfile — landscaping-api
# Builds with Maven, runs on a slim JRE, non-root user.
# Build context is the repo root; this file lives at api/Dockerfile.
# =============================================================================

# ---- build stage ------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the POM first so the dependency layer caches independently of source.
# A source-only change then skips the (slow) dependency download entirely.
COPY api/pom.xml .
RUN mvn -B -e -ntp dependency:go-offline

COPY api/src ./src
RUN mvn -B -e -ntp clean package -DskipTests

# ---- runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# wget backs the HEALTHCHECK below; alpine's busybox wget is already present,
# but curl is friendlier for debugging inside a running container.
RUN apk add --no-cache curl \
 && addgroup -S spring \
 && adduser -S spring -G spring

COPY --from=build /build/target/*.jar /app/app.jar

USER spring:spring

EXPOSE 8080

# MaxRAMPercentage rather than -Xmx: the JVM sizes itself against the
# container's cgroup limit, so changing the App Platform instance size needs
# no Dockerfile edit.
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Exec form so SIGTERM reaches the JVM and Spring shuts down gracefully
# rather than being killed after App Platform's drain window.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health/liveness || exit 1
