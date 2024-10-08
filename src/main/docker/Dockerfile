FROM ubuntu:20.04

ARG OPENTTD_VERSION="14.1"
ARG OPENGFX_VERSION="0.7.1"

RUN apt-get update
RUN apt-get install dos2unix
RUN apt-get install -y wget
RUN apt-get install libgomp1

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

COPY src/main/docker/cleanup.sh /tmp/cleanup.sh
COPY src/main/docker/buildconfig /tmp/buildconfig
COPY --chown=1000:1000 src/main/docker/openttd.sh /openttd.sh

RUN dos2unix /tmp/cleanup.sh
RUN dos2unix /tmp/buildconfig
RUN dos2unix /openttd.sh

RUN chmod +x /tmp/cleanup.sh /openttd.sh

RUN apt update -qq
RUN apt install -yqq --no-install-recommends -o DPkg::Options::=--force-confold -o DPkg::Options::=--force-confdef dumb-init wget unzip ca-certificates libfontconfig1 libfreetype6 libfluidsynth2 libicu-dev libpng16-16 liblzma-dev liblzo2-2 libsdl1.2debian libsdl2-2.0-0 xz-utils > /dev/null 2>&1


## Create user
RUN adduser --uid 1000 --shell /bin/bash --gecos "" openttd
RUN addgroup openttd users
RUN passwd -d openttd

USER openttd
WORKDIR /home/openttd
RUN wget https://cdn.openttd.org/openttd-releases/14.1/openttd-14.1-linux-generic-amd64.tar.xz
RUN wget https://cdn.openttd.org/opengfx-releases/7.1/opengfx-7.1-all.zip

RUN tar -xf openttd-14.1-linux-generic-amd64.tar.xz

RUN mkdir openttd-14
RUN cp -a openttd-14.1-linux-generic-amd64/. openttd-14

RUN unzip opengfx-7.1-all.zip
RUN tar -xf opengfx-7.1.tar -C openttd-14/baseset/

RUN rm -rf opengfx-*.tar opengfx-*.zip openttd-14.1*
    ## Set entrypoint script to right user
RUN chmod +x /openttd.sh



USER root
RUN /tmp/cleanup.sh

RUN mkdir /home/openttd/server
RUN chown 1000:1000 /home/openttd/server

VOLUME /home/openttd

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
