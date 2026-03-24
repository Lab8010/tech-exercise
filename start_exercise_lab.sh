#!/usr/bin/env sh
# アプリ（db+api）と GitLab CE を起動し、GitLab 準備後にリポジトリを root/tech-exercise へ投入する
set -eu
cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "Create .env from .env.example first:" >&2
  echo "  cp .env.example .env" >&2
  echo "Then edit GITLAB_ROOT_PASSWORD (no single quotes in password)." >&2
  exit 1
fi

export COMPOSE_PROFILES=gitlab

if command -v podman-compose >/dev/null 2>&1; then
  podman-compose up --build -d
else
  podman compose up --build -d
fi

chmod +x scripts/wait_gitlab_ready.sh scripts/seed_gitlab_project.sh 2>/dev/null || true
./scripts/wait_gitlab_ready.sh
./scripts/seed_gitlab_project.sh

echo ""
echo "GitLab: http://localhost:8929  |  App: http://localhost:8080/"
echo "Stop all: COMPOSE_PROFILES=gitlab ./stop_exercise_lab.sh"
