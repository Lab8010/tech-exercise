#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if command -v podman-compose >/dev/null 2>&1; then
  podman-compose down
else
  podman compose down
fi
