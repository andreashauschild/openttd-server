[![Build OpenTTD Server](https://github.com/andreashauschild/openttd-server/actions/workflows/build.yml/badge.svg)](https://github.com/andreashauschild/openttd-server/actions/workflows/build.yml)
[![Release OpenTTD Server](https://github.com/andreashauschild/openttd-server/actions/workflows/release.yml/badge.svg)](https://github.com/andreashauschild/openttd-server/actions/workflows/release.yml)  
[![Docker Pulls](https://badgen.net/docker/pulls/hauschi86/openttd-server?icon=docker&label=pulls)](https://hub.docker.com/r/hauschi86/openttd-server/)
[![Docker Stars](https://badgen.net/docker/stars/hauschi86/openttd-server?icon=docker&label=stars)](https://hub.docker.com/r/hauschi86/openttd-server/)
[![Docker Image Size](https://badgen.net/docker/size/hauschi86/openttd-server?icon=docker&label=image%20size)](https://hub.docker.com/r/hauschi86/openttd-server/)
![Github stars](https://badgen.net/github/stars/andreashauschild/openttd-server?icon=github&label=stars)
![Github forks](https://badgen.net/github/forks/andreashauschild/openttd-server?icon=github&label=forks)
![Github issues](https://img.shields.io/github/issues/andreashauschild/openttd-server)
![Github last-commit](https://img.shields.io/github/last-commit/andreashauschild/openttd-server)

# Welcome to OpenTTD Server
This Docker container allows you to host multiple instances of OpenTTD (https://www.openttd.org) dedicated servers in a single container, providing an efficient and convenient environment for hosting these servers.

This documentation expect a basic knowledge of docker (expose ports and volumes).

The current state of this Project is `BETA`. Hosting works and every function was tested but as you know a developer should not test his own software. Please create an issue if something needs to be fixed.

It provides the following features:

- Login protected admin gui
- Managing multiple dedicated OpenTTD server instance. You just need to expose the needed ports on container startup.
- Upload/Download of save games and configuration files
- Password protection for dedicated servers
- Auto save of running servers
- Auto pause and unpause on inactive servers. The game is paused while no player is connected; it unpauses as soon as a client connects, no matter whether that client joins a company or watches as a spectator.
- Simple terminal to send commands directly to the dedicated server

# Versions
The following table shows which docker image contains which OpenTTD version. For now, I will only support final versions (no beta).

Since `v15.3.1` (next release) every tag is a multi-arch image: `linux/amd64` and `linux/arm64`. Docker picks the right
one automatically; older tags are `linux/amd64` only.

| Container                        | OpenTTD |
|----------------------------------|--------|
| hauschi86/openttd-server:latest  | 15.3   |
| hauschi86/openttd-server:v15.3.0 | 15.3   |
| hauschi86/openttd-server:v15.2.0 | 15.2   |
| hauschi86/openttd-server:v15.1.1 | 15.1   |
| hauschi86/openttd-server:v15.1.0 | 15.1   |
| hauschi86/openttd-server:v15.0.2 | 15.0   |
| hauschi86/openttd-server:v15.0.0 | 15.0   |
| hauschi86/openttd-server:v14.1.1 | 14.1   |
| hauschi86/openttd-server:v14.1.0 | 14.1   |
| hauschi86/openttd-server:v14.0.0 | 14.0   |
| hauschi86/openttd-server:v13.4.0 | 13.4   |
| hauschi86/openttd-server:v13.0.0 | 13.0   |
| hauschi86/openttd-server:v12.2.2 | 12.2   |

Runs on Raspberry Pi 4/5 (64-bit OS), Oracle Cloud Ampere A1 and Apple silicon hosts without emulation. OpenTTD is
compiled from the official source release as a dedicated server (`-DOPTION_DEDICATED=ON`), because there is no official
Linux arm64 binary.


# Screenshots

### Server Management
<a href="docs/images/server-overview.JPG">
<img src="docs/images/server-overview.JPG"/>
</a>

### Dedicated Server Settings
<a href="docs/images/server_configuration.JPG">
<img src="docs/images/server_configuration.JPG"/>
</a>

### Terminal
<a href="docs/images/server-terminal.JPG">
<img src="docs/images/server-terminal.JPG"/>
</a>

### File Upload
<a href="docs/images/file_upload.JPG">
<img src="docs/images/file_upload.JPG"/>
</a>

### File Explorer for server customization like NewGRF etc.
<a href="docs/images/openttd-fileexplorer.gif">
<img src="docs/images/openttd-fileexplorer.gif"/>
</a>

### Admin Login
<a href="docs/images/admin-login.JPG">
<img src="docs/images/admin-login.JPG"/>
</a>

### File Explorer
The File Explorer allows you to customize your OpenTTD installation at runtime by uploading custom content directly through the web interface. This feature was added based on [Issue #4](https://github.com/andreashauschild/openttd-server/issues/4).

#### Use Cases
- Upload **NewGRF** files to add new graphics, vehicles, industries, or town names
- Add **AI scripts** and **Game Scripts** to enhance gameplay
- Upload **Base Graphics Sets** for custom visual styles
- Manage configuration files and save games

#### How It Works
The File Explorer provides access to the OpenTTD installation directory (`/home/openttd/openttd-<version>`). You can:
- **Browse** the complete directory structure
- **Upload** files to any directory (e.g., `newgrf/`, `ai/`, `game/`)
- **Create** new directories for organizing content
- **Delete** files and directories
- **Download** files or entire directories as ZIP archives
- **Move/Copy** files between directories
- **Rename** files and directories

#### Example: Adding a NewGRF
1. Download the NewGRF using one of these methods:
   - **Via OpenTTD App:** Use the in-game content downloader. Files are saved to your local `content_download/newgrf` folder. See [OpenTTD Wiki](https://wiki.openttd.org/en/Manual/NewGRF#manual-install) for directory locations on your OS.
   - **Manual Download:** Get `.grf` files from [BaNaNaS](https://bananas.openttd.org/) or [GRFCrawler](https://grfcrawler.tt-forums.net/)
2. Open the File Explorer in the web interface
3. Navigate to the `newgrf/` directory
4. Upload the `.grf` file
5. Configure your server's `openttd.cfg` to use the NewGRF or create a save game that includes it

**Note:** NewGRF files should be installed before starting a new game to ensure correct operation. Changes to NewGRF settings are baked into save games.

# Networking
By default, docker does not expose the containers on your network. This must be done manually with -p parameter (see here for more details on -p). 
For the container to work you need to expose at least 2 ports. The port `8080` for the web application and the port for your openttd dedicated server (default: `3979`)

# File Locations
All data and uploads within the container are saved in the `/home/openttd/server` directory.
OpenTTD is installed on `/home/openttd/openttd-<version>` directory.

# Setup
When you start the Docker container for the OpenTTD server for the first time, it will log the password for the admin login. See fragment below.
You can use the admin user to log in to the web app, which runs on http://localhost:8080 by default.
Once logged in, you can access the web app's settings to change the admin password.

## Upgrading from an older image (Portainer users especially)

Two things from a container created with an older image break the new one when the container is *recreated* instead
of created from scratch:

- **Stale environment variables.** Old containers carried `PATH` and `JAVA_HOME` pointing at `/usr/lib/jvm/java-17-openjdk-x64`.
  Portainer's *Duplicate/Edit* copies them into the new container, where they override the image and the start fails with
  `[FATAL tini (7)] exec java failed: No such file or directory`. Remove `PATH`, `JAVA_HOME`, `JDK_DOWNLOAD` and `JVM_DIR`
  from the container's environment (or create the container from scratch).
- **A volume mounted at `/home/openttd`.** Older images declared the whole home directory as a volume. Such a volume also
  shadows the OpenTTD installation in `/home/openttd/openttd-15`, so you keep running the old OpenTTD binary and never
  receive a version update. Mount your data at `/home/openttd/server` only; your existing data is in the `server/`
  sub folder of the old volume:

  ```
  docker volume create openttd-server-data
  docker run --rm -v <old-volume-name>:/old -v openttd-server-data:/new alpine sh -c 'cp -a /old/server/. /new/'
  ```

## Graceful shutdown

When the container is stopped, the application saves every running dedicated server (if auto save is enabled for it)
and then asks OpenTTD to `quit` before the process is terminated. Killing OpenTTD while it writes a save game leaves a
truncated, unreadable `.sav` behind, so the container needs enough time to finish.

`docker stop` sends `SIGKILL` after **10 seconds** by default, which is not enough. Give the container a longer grace
period:

```
docker stop --time 60 <container>
```

or configure it once when the container is created:

```
docker run -d --stop-timeout 60 ... hauschi86/openttd-server:latest
```

With Docker Compose use `stop_grace_period` (see the example below).

## Automatic restart of running servers

The application remembers which dedicated servers were started. After a host reboot, a container restart or an image
update, those servers are started again automatically. A server is only remembered while it is started; stopping it in
the web app clears the flag, so a server you stopped stays stopped.

Set the following environment variable if you prefer to start every server manually:

```
SERVER_AUTO_START_RUNNING_SERVERS=false
```

**First startup log fragment with password**
```
...
###########################################################################
### No initial password was set. A password for 'admin' will be generated.
### Copy it NOW, because it will never be shown again.
### Password: W!318Y-yBb
###########################################################################
...
```

# Examples
**Info:** If you have a specific version of the container that you prefer to use, you can replace the example version with your chosen version. 
This will ensure that you are using the version of the container that best meets your needs and preferences.

Run OpenTTD Server with 1 exposed port. In this case you can host only 1 server.

`docker run -d -p 8080:8080 -p 3979:3979/tcp -p 3979:3979/udp hauschi86/openttd-server:latest`

Run OpenTTD Server with 20 exposed port. In this case you can host 20 servers.

`docker run -d -p 8080:8080 -p 3979-3999:3979-3999/tcp -p 3979-3999:3979-3999/udp hauschi86/openttd-server:latest`

The container uses a simple file storage to store data. If you want to have persistent storage you should create a volume and bind it.

`docker run -d -v openttd-server-volume:/home/openttd/server -p 8080:8080 -p 3979-3999:3979-3999/tcp -p 3979-3999:3979-3999/udp hauschi86/openttd-server:latest`

## Docker Compose

The same setup as a `docker-compose.yml`. `stop_grace_period` gives the container time to save the running servers
before it is killed (see [Graceful shutdown](#graceful-shutdown)).

```yaml
services:
  openttd-server:
    image: hauschi86/openttd-server:latest
    container_name: openttd-server
    restart: unless-stopped
    stop_grace_period: 60s
    ports:
      - "8080:8080"
      - "3979-3999:3979-3999/tcp"
      - "3979-3999:3979-3999/udp"
    environment:
      # Optional: set the password of the 'admin' user on the very first start
      # SERVER_INITIAL_PASSWORD: "change-me"
      # Optional: do not start the previously running servers again
      # SERVER_AUTO_START_RUNNING_SERVERS: "false"
    volumes:
      - openttd-server-volume:/home/openttd/server

volumes:
  openttd-server-volume:
```

Start it with `docker compose up -d` and read the generated admin password from `docker compose logs openttd-server`.

# Reverse proxy / HTTPS

The web app can be served through a reverse proxy. Two things need attention:

1. **Session header.** The session id travels in the `X-Openttd-Server-Session-Id` HTTP header. Up to the previous
   release the header was named `X-OPENTTD_SERVER_SESSION_ID`; nginx drops headers containing underscores by default,
   which made every request behind a proxy answer with `401`. The header is hyphenated since this release. The backend
   still accepts and still sends the old name for one release, so an already loaded browser tab keeps working.
2. **WebSocket.** The live terminal and the dashboard use a WebSocket on `/data-stream`. The proxy must be configured
   to upgrade that connection. On an `https://` page the web app now uses `wss://` automatically. The proxy
   configuration below is unchanged, but the socket itself is authenticated by its first frame instead of by its
   handshake, which matters for anyone scripting against it:

   ```json
   {"type":"subscribe","sessionId":"<session id>","processId":"<process id or null>","fromOffset":null}
   ```

   A connection that does not send this within five seconds, or that sends an unknown session id, is closed with code
   `1008`. With a `processId` the server answers with one `OpenttdTerminalSnapshotEvent` (the console history, ending
   at `endOffset`) and then with the `OpenttdTerminalUpdateEvent`s of that process only; every event carries an
   absolute `offset`, so a client drops the updates below `endOffset` and appends the rest without a gap or a
   duplicate. `processId: null` subscribes to every process and gets no snapshot. A later frame switches the process,
   or asks for the output missed during a reconnect with `fromOffset`. Cross site websocket hijacking is prevented by
   the session id: a page on another origin cannot read it. An additional check of the `Origin` header can be enabled
   with `server.websocket.check-origin=true` and `server.websocket.allowed-origins` (comma separated); it is off by
   default, because the Angular dev server is served from another origin than the backend.

   While the terminal is open in the web app, the automatic commands (`server_info`, `pause`, `unpause`, autosave) are
   suppressed so that they do not pollute the console. `GET /processes` reports how many of them were skipped in
   `process.skippedCommands`. Passwords typed into the terminal (`server_pw`, `rcon_pw`, `rcon`, `company_pw`,
   `setting *password*`) are replaced with `********` in the console history, in that endpoint and on the socket.

An nginx example:

```nginx
server {
    listen 443 ssl;
    server_name openttd.example.com;

    # ssl_certificate / ssl_certificate_key ...

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;

        # Required for the /data-stream WebSocket
        proxy_set_header   Upgrade           $http_upgrade;
        proxy_set_header   Connection        "upgrade";
        proxy_read_timeout 3600s;
    }
}
```

Note that the OpenTTD dedicated servers themselves are plain TCP/UDP game servers. They are not served through the
HTTP proxy, their ports have to be reachable directly.


# Usage Development Mode

## Build
- `./mvnw -Pno-ui verify` - backend only, skips the Angular UI build (runs on JDK 17 and JDK 21)
- `./mvnw verify` - full build including the Angular UI (downloads Node and npm into the project on first run)
- `docker buildx build --platform linux/amd64,linux/arm64 -f src/main/docker/Dockerfile -t openttd-server .` - multi-arch
  image. Use a single platform plus `--load` to run it locally, e.g.
  `docker buildx build --platform linux/arm64 -f src/main/docker/Dockerfile --load -t openttd-server .`.
  `./mvnw verify` must run first, because the Dockerfile copies `target/quarkus-app`.
- The CI build uses JDK 17, so keep the code compatible with `maven.compiler.release=17`. The image itself runs on the
  Eclipse Temurin 21 JRE (Ubuntu 24.04).
- Windows contributors: `.gitattributes` normalizes everything to LF in the repository. Only `*.cmd`, `*.bat`
  and `*.ps1` keep CRLF in the working tree, so no `core.autocrlf` tweaking is needed.
- Ad-hoc image: Actions -> "Manual image build" -> Run workflow, enter a tag (e.g. `v15.3.1-rc1`) and pick the
  platforms. In CI arm64 is compiled natively on GitHub's `ubuntu-24.04-arm` runner and takes roughly 10 minutes; the
  ~2 h figure only applies to a local emulated (QEMU) cross build.
- If you pick a single platform there, the tag automatically gets a `-arm64` / `-amd64` suffix, so a partial build can
  never overwrite the multi-arch manifest of `latest` or `vX.Y.Z`.

## Quarkus remote docker container development
- Open a terminal in the `root` directory
- `docker buildx build --platform linux/amd64 -f src/main/docker/Dockerfile --load --progress=plain -t openttd-server .`
- `docker run -i --rm -p 8080:8080 -p 5005:5005 -p 3977:3977/tcp -p 3979:3979/tcp -p 3979:3979/udp -e QUARKUS_LAUNCH_DEVMODE=true openttd-server`
- `openttd -D -b 8bpp-optimized`  Run with possibility to do screenshots (https://www.tt-forums.net/viewtopic.php?t=88943)
- https://quarkus.io/guides/maven-tooling#remote-development-mode
- https://blog.sebastian-daschner.com/entries/quarkus-remote-dev-in-containers-update

## Debug and develop in Quarkus Container:
- Quarkus debug url: http://localhost:8080/q
- Add properties to `application.properties`

```
quarkus.package.type=mutable-jar
quarkus.live-reload.password=Password_1
quarkus.live-reload.url=http://localhost:8080
```

- Add env values to dockerfile expose debug port and add remote debug to startup:

```
EXPOSE 5005
ENV QUARKUS_LAUNCH_DEVMODE=true
ENV JAVA_ENABLE_DEBUG=true

CMD ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005", "-jar", "/deployments/quarkus-run.jar"]
```

- Start dev environment in remote debug mode (but disable local debug):
    - `mvn quarkus:remote-dev -Ddebug=false -Dquarkus.live-reload.url=http://localhost:8080`

## Helpful commands:

| Description                        | Command                                                                                   |
|------------------------------------|-------------------------------------------------------------------------------------------|
| Create Module with routing         | `npx ng g m HomeIndex --flat --routing`                                                   |
| Create Component and add to module | `npx ng g m LoginIndex --flat && npx ng g c LoginIndex --flat -m .\login-index.module.ts` |
|                                    |                                                                                           |
|                                    |                                                                                           |

## Structure Based on:

- https://github.com/joshuamorony/nx-angular-structure/tree/main/src/app
    - https://www.youtube.com/watch?v=7SDpTOLeqHE

