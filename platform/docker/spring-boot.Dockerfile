# syntax=docker/dockerfile:1
# Shared image for the Spring Boot services. Build context = the service directory, after ./gradlew bootJar:
#   docker build -f platform/docker/spring-boot.Dockerfile -t lab/order-service:dev reference-service/order-service
#
# Layered jar: dependencies change rarely and the application layer changes often, so rebuilds and pulls
# only move the small top layer. The runtime is explicit (no buildpack memory calculator), so the JVM memory
# flags below behave exactly as documented, which WP-09's memory-budget labs rely on.
#
# RUNTIME_IMAGE defaults to the full JDK so jcmd/jfr work inside the pod (WP-02/07/09 diagnostics).
# Production-style image (~150 MB smaller, no diagnostic tools):
#   --build-arg RUNTIME_IMAGE=eclipse-temurin:25-jre-noble
ARG RUNTIME_IMAGE=eclipse-temurin:25-jdk-noble

FROM eclipse-temurin:25-jre-noble AS extract
WORKDIR /workspace
COPY build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM ${RUNTIME_IMAGE}
RUN groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=extract /workspace/extracted/dependencies/ ./
COPY --from=extract /workspace/extracted/spring-boot-loader/ ./
COPY --from=extract /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extract /workspace/extracted/application/ ./
USER 10001:10001

# Heap as a share of the container memory limit, leaving room for metaspace, thread stacks, code cache and
# direct memory. Override per environment (k8s JAVA_TOOL_OPTIONS) from the WP-09 memory budget worksheet.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError" \
    SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
