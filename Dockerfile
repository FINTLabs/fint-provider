# This branch keeps the code of release v2.4.2, which production runs.
# The v2.4.2 sources cannot be built any more: fint-events 2.3.1 and
# fint-springfox-extension 0.0.2 came from jcenter, which is shut down.
# Thus this Dockerfile takes the v2.4.2 jars from the released image
# and puts them on the eclipse-temurin 8 runtime.
FROM fintlabsacr.azurecr.io/provider:2.4.2 AS release

FROM eclipse-temurin:8-jre
ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError"
COPY --from=release /data/ /data/
ENTRYPOINT ["java", "-jar", "/data/fint-provider.jar"]
