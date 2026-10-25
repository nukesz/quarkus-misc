{
  description = "Java project development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux"; # Adjust for your system
      pkgs = import nixpkgs { inherit system; };
    in {
      devShells.${system}.default = pkgs.mkShell {
        packages = with pkgs; [
          jdk25
          maven
	  quarkus
        ];

        shellHook = ''
          echo "Java development environment loaded"
          java -version
        '';
      };
    };
}

