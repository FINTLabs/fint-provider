FROM eclipse-temurin:8-jdk AS builder
WORKDIR /workspace
COPY . .
ARG buildFlags=""
RUN ./gradlew --no-daemon ${buildFlags} build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
COPY --from=builder /workspace/build/deps/external/*.jar /data/
COPY --from=builder /workspace/build/deps/fint/*.jar /data/
COPY --from=builder /workspace/build/libs/fint-provider-*.jar /data/fint-provider.jar
CMD ["/data/fint-provider.jar"]
