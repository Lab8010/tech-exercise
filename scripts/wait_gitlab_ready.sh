#!/usr/bin/env sh
# GitLab の HTTP が応答するまで待つ（初回起動は数分かかることがあります）
set -eu
cd "$(dirname "$0")/.."

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

URL="${GITLAB_EXTERNAL_URL:-http://localhost:8929}"
MAX="${GITLAB_WAIT_ATTEMPTS:-90}"
SLEEP="${GITLAB_WAIT_SLEEP:-10}"

echo "Waiting for GitLab at $URL (max ${MAX} attempts, ${SLEEP}s apart)..."
i=1
while [ "$i" -le "$MAX" ]; do
  # GitLab 17 系では /api/v4/version が未認証 401 になることがあるため、Web UI のサインインを待機条件にする
  if curl -sfS "$URL/users/sign_in" >/dev/null 2>&1; then
    echo "GitLab Web UI is up (sign-in page)."
    exit 0
  fi
  if curl -sfS "$URL/" >/dev/null 2>&1; then
    echo "GitLab root responded (still waiting for full readiness)..."
  fi
  printf "  attempt %s/%s\n" "$i" "$MAX"
  i=$((i + 1))
  sleep "$SLEEP"
done

echo "Timed out waiting for GitLab." >&2
exit 1
