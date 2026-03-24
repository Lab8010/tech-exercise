#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if command -v podman-compose >/dev/null 2>&1; then
  podman-compose down
  podman-compose up --build -d
else
  podman compose down
  podman compose up --build -d
fi
