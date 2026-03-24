#!/usr/bin/env sh
# ローカル作業ツリーを GitLab 上の root/tech-exercise に初回投入する（冪等なし push は毎回上書き可）
set -eu
cd "$(dirname "$0")/.."

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

: "${GITLAB_ROOT_PASSWORD:?Set GITLAB_ROOT_PASSWORD in .env (see .env.example)}"
: "${GITLAB_SEED_PAT:=exercise-gitlab-seed-token}"

URL="${GITLAB_EXTERNAL_URL:-http://localhost:8929}"
PROJECT_PATH="tech-exercise"
TOKEN="$GITLAB_SEED_PAT"

compose() {
  if command -v podman-compose >/dev/null 2>&1; then
    COMPOSE_PROFILES=gitlab podman-compose "$@"
  else
    COMPOSE_PROFILES=gitlab podman compose "$@"
  fi
}

echo "Ensuring GitLab Personal Access Token (via gitlab-rails runner)..."
compose exec -T gitlab gitlab-rails runner \
  "u = User.find_by_username('root'); u.personal_access_tokens.where(name: 'exercise-seed').delete_all; t = u.personal_access_tokens.build(name: 'exercise-seed', scopes: [:api, :write_repository], expires_at: 1.year.from_now); t.set_token('${TOKEN}'); t.save!" || {
  echo "rails runner failed — is GitLab fully started? Re-run after wait_gitlab_ready.sh" >&2
  exit 1
}

echo "Creating project root/${PROJECT_PATH} if missing..."
HTTP_CODE=$(curl -sS -o /tmp/gitlab_create_resp.json -w "%{http_code}" \
  -X POST "$URL/api/v4/projects" \
  -H "PRIVATE-TOKEN: $TOKEN" \
  -d "name=${PROJECT_PATH}" \
  -d "path=${PROJECT_PATH}" \
  -d "visibility=internal" \
  -d "default_branch=main" \
  -d "initialize_with_readme=false")

if [ "$HTTP_CODE" = "201" ]; then
  echo "Project created."
elif [ "$HTTP_CODE" = "400" ]; then
  if grep -q "already been taken" /tmp/gitlab_create_resp.json 2>/dev/null; then
    echo "Project already exists; continuing."
  else
    echo "Create project failed (HTTP $HTTP_CODE):" >&2
    cat /tmp/gitlab_create_resp.json >&2
    exit 1
  fi
else
  echo "Create project unexpected HTTP $HTTP_CODE:" >&2
  cat /tmp/gitlab_create_resp.json >&2
  exit 1
fi

REPO_ROOT=$(pwd)
WORK=$(mktemp -d)
cleanup() {
  rm -rf "$WORK"
}
trap cleanup EXIT

cd "$WORK"
rsync -a \
  --exclude='.git/' \
  --exclude='target/' \
  --exclude='uploads/' \
  --exclude='.env' \
  "$REPO_ROOT"/ .

mkdir -p uploads
touch uploads/.gitkeep

git init -b main
git config user.email "exercise@local.lab"
git config user.name "Exercise Import"
git add -A
git commit -m "chore: initial import for exercise environment" || {
  echo "Nothing to commit — tree empty?" >&2
  exit 1
}

REMOTE="http://oauth2:${TOKEN}@localhost:8929/root/${PROJECT_PATH}.git"
git remote add origin "$REMOTE"
git push -u -f origin main

echo ""
echo "Done. Clone for participants (PAT は .env の GITLAB_SEED_PAT):"
echo "  git clone http://oauth2:${TOKEN}@localhost:8929/root/${PROJECT_PATH}.git"
echo "Web UI: $URL  (root / GITLAB_ROOT_PASSWORD)"
