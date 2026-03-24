#!/usr/bin/env sh
# ローカルで演習環境の一部を確認する（GitLab イメージ取得・初回起動は別途時間がかかります）
set -eu
cd "$(dirname "$0")/.."

compose() {
  if command -v podman-compose >/dev/null 2>&1; then
    podman-compose "$@"
  else
    podman compose "$@"
  fi
}

echo "=== 1. Compose ファイル（profile gitlab）"
compose --profile gitlab config >/dev/null
echo "OK"

echo "=== 2. アプリ API（db+api が起動していれば）"
if curl -sfS "http://localhost:8080/cats" >/dev/null 2>&1; then
  echo "OK  http://localhost:8080/cats"
else
  echo "SKIP（未起動） ./start_petbattle.sh または start_exercise_lab.sh で起動"
fi

echo "=== 3. GitLab Web（GitLab コンテナが起動し準備できていれば）"
if curl -sfS "http://localhost:8929/users/sign_in" >/dev/null 2>&1; then
  echo "OK  http://localhost:8929/"
else
  echo "SKIP（未起動または初回起動待ち） COMPOSE_PROFILES=gitlab で up 後、数分〜十数分待つ"
fi

echo ""
echo "フル検証: cp .env.example .env を編集後"
echo "  ./start_exercise_lab.sh"
echo "（GitLab イメージ初回取得にディスク・時間が大きくかかります）"
