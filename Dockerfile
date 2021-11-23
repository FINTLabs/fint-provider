FROM gradle:4.10.3-jdk8-alpine as builder
USER root
COPY . .
ARG buildFlags=""
RUN gradle --no-daemon ${buildFlags} build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-provider-*.jar /data/fint-provider.jar
CMD ["/data/fint-provider.jar"]
