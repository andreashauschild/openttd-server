{
  description = "OpenTTD Server development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        # Node.js 24.12.0 to match pom.xml
        nodejs-24_12_0 = pkgs.stdenv.mkDerivation rec {
          pname = "nodejs";
          version = "24.12.0";

          src = pkgs.fetchurl {
            url = "https://nodejs.org/dist/v${version}/node-v${version}-linux-x64.tar.xz";
            sha256 = "sha256-vevuJ25Y0O9USPPVrBLGfaqWPdXgqbtiGlPRzvvIUv0=";
          };

          nativeBuildInputs = [ pkgs.autoPatchelfHook ];
          buildInputs = [ pkgs.stdenv.cc.cc.lib ];

          installPhase = ''
            mkdir -p $out
            cp -r * $out/
          '';
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            # Java 17 (matches Dockerfile)
            jdk17
            maven

            # Node.js 24.12.0 for frontend build (matches pom.xml)
            nodejs-24_12_0

            # Docker for building and running containers
            docker
            docker-compose

            # Useful utilities
            curl
            wget
          ];

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk17}"

            # Setup node symlinks for frontend-maven-plugin
            mkdir -p ui/node/node_modules
            ln -sf ${nodejs-24_12_0}/bin/node ui/node/node
            ln -sf ${nodejs-24_12_0}/lib/node_modules/npm ui/node/node_modules/npm
            ln -sf ${nodejs-24_12_0}/lib/node_modules/npm/bin/npm-cli.js ui/node/npm

            echo "OpenTTD Server dev environment loaded"
            echo "Java: $(java -version 2>&1 | head -1)"
            echo "Maven: $(mvn -version 2>&1 | head -1)"
            echo "Node: $(node --version)"
          '';
        };
      }
    );
}
