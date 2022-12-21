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

COPY prepare.sh /tmp/prepare.sh
COPY cleanup.sh /tmp/cleanup.sh
COPY buildconfig /tmp/buildconfig
COPY --chown=1000:1000 openttd.sh /openttd.sh

RUN dos2unix /tmp/prepare.sh
RUN dos2unix /tmp/cleanup.sh
RUN dos2unix /tmp/buildconfig
RUN dos2unix /openttd.sh

RUN chmod +x /tmp/prepare.sh /tmp/cleanup.sh /openttd.sh
RUN /tmp/prepare.sh
RUN /tmp/cleanup.sh

VOLUME /home/openttd/.openttd

EXPOSE 3979/tcp
EXPOSE 3979/udp


ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'


# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=1000 server/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1000 server/target/quarkus-app/*.jar /deployments/
COPY --chown=1000 server/target/quarkus-app/app/ /deployments/app/
COPY --chown=1000 server/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
EXPOSE 5005
USER 1000
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENV start-server.command="/openttd.sh"


#STOPSIGNAL 3
#ENTRYPOINT [ "/usr/bin/dumb-init", "--rewrite", "15:3", "--rewrite", "9:3", "--" ]
#CMD [ "/openttd.sh" ]
ENV openttd.save.dir=/tmp/openttd/save
ENV openttd.config.dir=/tmp/openttd/config
ENV server.config.dir=/tmp/openttd

CMD ["java", "-jar", "/deployments/quarkus-run.jar" ]
