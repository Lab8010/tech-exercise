#!/usr/bin/env sh
# db + api + GitLab をまとめて停止（名前付きボリュームは削除しない）
set -eu
cd "$(dirname "$0")"
export COMPOSE_PROFILES=gitlab

if command -v podman-compose >/dev/null 2>&1; then
  podman-compose down
else
  podman compose down
fi
