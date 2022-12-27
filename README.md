[![Build OpenTTD Server](https://github.com/andreashauschild/openttd-server/actions/workflows/build.yml/badge.svg)](https://github.com/andreashauschild/openttd-server/actions/workflows/build.yml)
[![Release OpenTTD Server](https://github.com/andreashauschild/openttd-server/actions/workflows/release.yml/badge.svg)](https://github.com/andreashauschild/openttd-server/actions/workflows/release.yml)  
[![Docker Pulls](https://badgen.net/docker/pulls/hauschi86/openttd-server?icon=docker&label=pulls)](https://hub.docker.com/r/hauschi86/openttd-server/)
[![Docker Stars](https://badgen.net/docker/stars/hauschi86/openttd-server?icon=docker&label=stars)](https://hub.docker.com/r/hauschi86/openttd-server/)
[![Docker Image Size](https://badgen.net/docker/size/hauschi86/openttd-server?icon=docker&label=image%20size)](https://hub.docker.com/r/hauschi86/openttd-server/)
![Github stars](https://badgen.net/github/stars/andreashauschild/openttd-server?icon=github&label=stars)
![Github forks](https://badgen.net/github/forks/andreashauschild/openttd-server?icon=github&label=forks)
![Github issues](https://img.shields.io/github/issues/andreashauschild/openttd-server)
![Github last-commit](https://img.shields.io/github/last-commit/andreashauschild/openttd-server)

# Welcome to  OpenTTD Server
This Docker container allows you to host multiple instances of an OpenTTD dedicated server in a single container, providing an efficient and convenient environment for hosting these servers.

It provides the following features:

- Login protected admin gui
- Managing multiple dedicated OpenTTD server instance. You just need to expose the needed ports on container startup.
- Upload/Download of save games and configuration files
- Password protection for dedicated servers
- Auto save of running servers
- Auto pause and unpause on inactive servers. If no player is playing the server is running but paused. Server unpauses if a player joins a company.
- Simple terminal to send commands directly to the dedicated server

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

### Admin Login
<a href="docs/images/admin-login.JPG">
<img src="docs/images/admin-login.JPG"/>
</a>

# Development

This section is for developers. This can be skipped if you just want to use that server as docker container.
`docker run -i --rm -v /tmp/openttd-server-volumne:/home/openttd/server -p 8080:8080 -p 3979-3999:3979-3999/tcp 3979-3999:3979-3999/udp openttd-server` 


# Usage Development Mode

- Open a terminal in the `root` directory
- `docker build -f src/main/docker/Dockerfile.dev . --progress=plain -t openttd-server`
- `docker run -i --rm -p 8080:8080 -p 5005:5005 -p 3979:3979/tcp -p 3979:3979/udp -e QUARKUS_LAUNCH_DEVMODE=true openttd-server`
- `openttd -D -b 8bpp-optimized`  Run with possibility to do screenshots (https://www.tt-forums.net/viewtopic.php?t=88943)
- https://quarkus.io/guides/maven-tooling#remote-development-mode
- https://blog.sebastian-daschner.com/entries/quarkus-remote-dev-in-containers-update

# Debug and develop in Quarkus Container:

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
    - `quarkus:remote-dev -Ddebug=false -Dquarkus.live-reload.url=http://localhost:8080`

### File locations ###

This image is supplied with a user named `openttd`.  
Openttd server is run as this user and subsequently its home folder will be `/home/openttd`.  
Openttd on linux uses `.openttd` in the users homefolder to store configurations, savefiles and other miscellaneous files.  
If you want to your local files accessible to openttd server inside the container you need to mount them inside with `-v` parameter (
see https://docs.docker.com/engine/reference/commandline/run/ for more details on -v)


### Networking ###

By default docker does not expose the containers on your network. This must be done manually with `-p` parameter (
see [here](https://docs.docker.com/engine/reference/commandline/run/) for more details on -p).
If your openttd config is set up to listen on port 3979 you need to map the container port to your machines network like so `-p 3979:3979` where the first reference is the host
machines port and the second the container port.

### Examples ###

Run Openttd and expose the default ports.

    docker run -d -p 3979:3979/tcp -p 3979:3979/udp bateau/openttd:latest

Run Openttd with random port assignment.

    docker run -d -P bateau/openttd:latest

## Honourable Mention ##

This dockerfile and linux setup was inspired by the openttd docker project: [bateau/openttd](https://github.com/bateau84/openttd). Thank you!

# Developer notes

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

