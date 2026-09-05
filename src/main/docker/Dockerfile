# syntax=docker/dockerfile:1
ARG OPENTTD_VERSION=15.3
ARG OPENGFX_VERSION=8.0

# --- Stage A: build the OpenTTD dedicated server from source (cdn.openttd.org has no linux arm64 binary) ---
FROM ubuntu:24.04 AS openttd-build
ARG OPENTTD_VERSION
ARG OPENGFX_VERSION
ARG OPENTTD_SHA256=5ea21eea7d59c78a42071924ac18c6bc0116088f2e96b14cfee9369175973be7
ARG OPENGFX_SHA256=43a0c1dabf39cb865394f3a6cc36d4da5c10ecfaaf55652043104806810903be
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
      build-essential cmake ninja-build pkg-config ca-certificates curl unzip xz-utils \
      zlib1g-dev liblzma-dev liblzo2-dev libpng-dev libcurl4-openssl-dev \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /build
RUN curl -fsSLO "https://cdn.openttd.org/openttd-releases/${OPENTTD_VERSION}/openttd-${OPENTTD_VERSION}-source.tar.xz" \
    && echo "${OPENTTD_SHA256}  openttd-${OPENTTD_VERSION}-source.tar.xz" | sha256sum -c - \
    && tar -xJf "openttd-${OPENTTD_VERSION}-source.tar.xz"
RUN cmake -S "openttd-${OPENTTD_VERSION}" -B build -G Ninja \
      -DCMAKE_BUILD_TYPE=Release -DOPTION_DEDICATED=ON -DOPTION_INSTALL_FHS=OFF \
      -DCMAKE_INSTALL_PREFIX=/opt/openttd \
    && cmake --build build -j"$(nproc)" \
    && cmake --install build
RUN curl -fsSLO "https://cdn.openttd.org/opengfx-releases/${OPENGFX_VERSION}/opengfx-${OPENGFX_VERSION}-all.zip" \
    && echo "${OPENGFX_SHA256}  opengfx-${OPENGFX_VERSION}-all.zip" | sha256sum -c - \
    && unzip -q "opengfx-${OPENGFX_VERSION}-all.zip" \
    && tar -xf "opengfx-${OPENGFX_VERSION}.tar" -C /opt/openttd/baseset/ \
    && mkdir /opt/openttd/newgrf \
    && ldd /opt/openttd/openttd

# --- Stage B: runtime ---
FROM eclipse-temurin:21-jre-noble
ARG DEBIAN_FRONTEND=noninteractive
# temurin noble already ships zlib1g liblzma5 libpng16-16t64 libcurl4t64 libstdc++6
RUN apt-get update && apt-get install -y --no-install-recommends tini liblzo2-2 \
    && rm -rf /var/lib/apt/lists/*
# noble has a default 'ubuntu' user on uid 1000 -> replace it with 'openttd' (uid/gid 1000, as before)
RUN userdel -r ubuntu \
    && groupadd --gid 1000 openttd \
    && useradd --uid 1000 --gid 1000 --create-home --shell /bin/bash openttd \
    && usermod -aG users openttd && passwd -d openttd \
    && mkdir -p /home/openttd/server && chown -R 1000:1000 /home/openttd
COPY --from=openttd-build --chown=1000:1000 /opt/openttd /home/openttd/openttd-15
COPY --chown=1000:1000 --chmod=755 src/main/docker/openttd.sh /openttd.sh
VOLUME /home/openttd

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=1000 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1000 target/quarkus-app/*.jar /deployments/
COPY --chown=1000 target/quarkus-app/app/ /deployments/app/
COPY --chown=1000 target/quarkus-app/quarkus/ /deployments/quarkus/

USER 1000
WORKDIR /home/openttd

# Explicit, so 'docker stop' gives the application the chance to save the running servers before it is killed.
# Give it enough time with 'docker stop --stop-timeout 60' or 'stop_grace_period: 60s' (see README).
STOPSIGNAL SIGTERM

ENV start-server.command="/openttd.sh" \
    openttd.save.dir=/home/openttd/server/save \
    openttd.config.dir=/home/openttd/server/config \
    server.config.dir=/home/openttd/server

ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["java", "-jar", "/deployments/quarkus-run.jar"]
