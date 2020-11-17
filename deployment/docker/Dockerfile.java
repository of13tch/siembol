FROM openjdk:8-jre-slim

ARG APP
ENV APP=$APP
ARG VERSION
ENV VERSION=$VERSION

RUN adduser --uid 101 --system --no-create-home --disabled-password --home /opt/$APP --shell /sbin/nologin --group $APP

EXPOSE 8080

WORKDIR /opt/$APP
COPY $APP /opt/$APP

USER $APP

CMD exec java $JAVA_OPTS -jar $APP-$VERSION.jar