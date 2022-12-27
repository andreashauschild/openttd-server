FROM ubuntu:20.04

ARG OPENTTD_VERSION="12.2"
ARG OPENGFX_VERSION="0.6.1"

RUN apt-get update
RUN apt-get install dos2unix
RUN apt-get install -y wget

# See: https://jdk.java.net/archive/
ENV JDK_DOWNLOAD https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz

# Install OpenJDK Java 17 SDK
ENV JVM_DIR /usr/lib/jvm
RUN mkdir -p "${JVM_DIR}"


RUN wget -q --no-cookies --no-check-certificate \
  -O "${DOWNLOAD_DIR}/openjdk-17.0.2_linux-x64_bin.tar.gz" "${JDK_DOWNLOAD}" \
  && cd "${JVM_DIR}" \
  && tar --no-same-owner -xzf "${DOWNLOAD_DIR}/openjdk-17.0.2_linux-x64_bin.tar.gz" \
  && rm -f "${DOWNLOAD_DIR}/openjdk-17.0.2_linux-x64_bin.tar.gz" \
  && mv "${JVM_DIR}/jdk-17.0.2" "${JVM_DIR}/java-17.0.2-openjdk-x64" \
  && ln -s "${JVM_DIR}/java-17.0.2-openjdk-x64" "${JVM_DIR}/java-17-openjdk-x64"

ENV JAVA_HOME ${JVM_DIR}/java-17-openjdk-x64

ENV PATH=$PATH:$HOME/bin:$JAVA_HOME/bin

COPY src/main/docker/prepare.sh /tmp/prepare.sh
COPY src/main/docker/cleanup.sh /tmp/cleanup.sh
COPY src/main/docker/buildconfig /tmp/buildconfig
COPY --chown=1000:1000 src/main/docker/openttd.sh /openttd.sh

RUN dos2unix /tmp/prepare.sh
RUN dos2unix /tmp/cleanup.sh
RUN dos2unix /tmp/buildconfig
RUN dos2unix /openttd.sh

RUN chmod +x /tmp/prepare.sh /tmp/cleanup.sh /openttd.sh
RUN /tmp/prepare.sh
RUN /tmp/cleanup.sh

RUN mkdir /home/openttd/server
RUN chown 1000:1000 /home/openttd/server

VOLUME /home/openttd/server

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=1000 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1000 target/quarkus-app/*.jar /deployments/
COPY --chown=1000 target/quarkus-app/app/ /deployments/app/
COPY --chown=1000 target/quarkus-app/quarkus/ /deployments/quarkus/

USER 1000

ENV start-server.command="/openttd.sh"

ENV openttd.save.dir=/home/openttd/server/save
ENV openttd.config.dir=/home/openttd/server/config
ENV server.config.dir=/home/openttd/server

CMD ["java", "-jar", "/deployments/quarkus-run.jar"]
