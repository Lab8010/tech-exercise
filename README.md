# tech-exercise — CI/CD 演習用（Podman 1 台完結）

CentOS Stream などのノート PC と **Podman** で完結する、**Quarkus 3.x（Java 17）** の「猫の投票 API」演習用リポジトリです。以前の OpenShift / Tekton / Helm 向け資産は `old_assets/` に退避しています。

**CI/CD は GitLab CI（`.gitlab-ci.yml`）** を使います。次のどちらでも演習できます。

1. **同梱の GitLab CE コンテナ**（`docker-compose.yml` の `profile: gitlab`）… 外部 Git ホスティング不要で閉域に近い構成  
2. **別途構築した自前 GitLab**（Omnibus 等）… 既にサーバがある場合

---

## 演習の流れ（推奨: GitLab コンテナ同梱）

受講者は **この GitLab 上の `root/tech-exercise` リポジトリ**を `git clone` して作業し、**push で `.gitlab-ci.yml` のパイプライン**を体験します。

1. **講師（または受講者の最初の 1 台）**  
   - 教材ツリー（本リポジトリ）を手元に用意する（社内 Git・USB・ZIP など、組織の配布方法に合わせる）。  
   - `.env` を用意して **フルラボ起動**する（後述）。  
   - 起動スクリプトが **GitLab 起動待ちのあと**、作業ツリーの内容を **`root/tech-exercise` に初回 push** する。  

2. **受講者**  
   - ブラウザで GitLab（例: `http://localhost:8929`）にログインするか、**clone 用 URL** で直接取得する。  
   - 取得したリポジトリでコードを変更し **`git push`** → **CI/CD パイプライン**が動く。  

**注意**: シード用に固定の PAT（`.env.example` の `GITLAB_SEED_PAT`）を使います。**閉域ラボ専用**です。本番や共有環境では必ず変更してください。

---

## フル演習環境の起動（PostgreSQL + API + GitLab CE）

### 前提

| もの | 説明 |
|------|------|
| **Podman** | 必須 |
| **Compose** | **`podman compose`（推奨）**。`docker-compose.yml` の **profiles** を使うため、古い **podman-compose** では動かない場合があります |
| **curl** | ヘルス待ちに使用 |
| **git** | シード処理に使用 |
| **rsync** | シード処理に使用（無い場合は `dnf install rsync` 等） |
| **メモリ** | GitLab CE は **合計 8GB 以上の RAM** を推奨（公式より緩めだが、それ未満は失敗しやすい） |

### 手順

```shell
cd tech-exercise
cp .env.example .env
# .env 内の GITLAB_ROOT_PASSWORD を変更（シングルクォート `'` は使わないこと）
chmod +x start_exercise_lab.sh stop_exercise_lab.sh scripts/*.sh
./start_exercise_lab.sh
```

`start_exercise_lab.sh` は次を行います。

1. `COMPOSE_PROFILES=gitlab` で **db・api・gitlab** を起動（api は従来どおり `./uploads` をマウント）  
2. `scripts/wait_gitlab_ready.sh` で **GitLab Web（`/users/sign_in`）** が応答するまで待機（初回は **数分〜十数分**かかることがあります。GitLab 17 系では未認証の `/api/v4/version` は 401 になるため、待機条件は UI ベースです）  
3. `scripts/seed_gitlab_project.sh` で **root 用 PAT の作成**（有効期限 1 年）・**プロジェクト `root/tech-exercise` の作成**・**現在の作業ツリー（`.git` / `target` / `uploads` 実体 / `.env` を除く）の初回コミット＋ push**  

### 止める（GitLab 含むすべて）

```shell
./stop_exercise_lab.sh
```

名前付きボリューム（`gitlab_config` / `gitlab_logs` / `gitlab_data`）は **削除されません**。GitLab のデータを捨てて最初からやり直す場合は:

```shell
COMPOSE_PROFILES=gitlab podman compose down -v
```

（**GitLab の全データが消えます**。）

### 動作確認（軽量）

Compose の文法と、起動済みなら API / GitLab の応答を確認します。

```shell
chmod +x scripts/verify_lab.sh
./scripts/verify_lab.sh
```

### 受講者向け clone 例

`.env` の `GITLAB_SEED_PAT`（既定では `exercise-gitlab-seed-token`）を使います。

```shell
git clone "http://oauth2:exercise-gitlab-seed-token@localhost:8929/root/tech-exercise.git"
cd tech-exercise
```

Web UI: **http://localhost:8929/**（ユーザー `root` / `.env` の `GITLAB_ROOT_PASSWORD`）

猫アプリ UI: **http://localhost:8080/**（従来どおり）

### GitLab Runner の登録（CI を回すマシン）

- GitLab の **Admin → CI/CD → Runners** などからトークンを取得し、**Shell executor** などで Runner を登録します。  
- **Git URL** は **`http://localhost:8929/`**（または演習マシンから見た GitLab のホスト名）。  
- **deploy_local** ジョブ用にタグ **`petbattle-deploy`** の Runner を、**`start_petbattle.sh` が動くマシン**（Podman + compose 利用可）に置く想定です（`.gitlab-ci.yml` 参照）。  

---

## アプリだけ起動する（GitLab なし・軽量）

GitLab を動かさず API だけ試す場合は従来どおりです。

```shell
./start_petbattle.sh
./stop_petbattle.sh
```

`stop_petbattle.sh` は **db + api** を止めます。**`start_exercise_lab.sh` で起動した GitLab は止まりません**。GitLab まで止める場合は **`./stop_exercise_lab.sh`** を使ってください。

### ビルドだけ先に確認したい場合（任意）

```shell
mvn clean package
```

---

## GitLab CI/CD（`.gitlab-ci.yml`）

| ステージ | 内容 |
|----------|------|
| **build** | `mvn clean verify` |
| **image** | `docker build`（DinD。**privileged Runner** が一般的に必要） |
| **deploy** | `./start_petbattle.sh`（手動・タグ `petbattle-deploy`） |

同一 GitLab で Container Registry を有効にしている場合、ビルド後イメージを push します。変数・タグの詳細は `.gitlab-ci.yml` 先頭のコメントを参照してください。

**同梱 GitLab** を使う場合も、上記と同じ `.gitlab-ci.yml` が **`root/tech-exercise` に push された時点で**その GitLab 上のパイプラインとして動きます（Runner の登録が別途必要）。

### Jenkins との関係

ルートの **`Jenkinsfile`** は参考用です。GitLab CI を主軸にする場合は **`.gitlab-ci.yml` のみ**で問題ありません。

---

## 構成

- **API**: Quarkus REST + Hibernate ORM Panache + PostgreSQL  
- **ブラウザ UI**: `http://localhost:8080/`（投票 ±1、画像追加、得票降順）  
- **GitLab CE（任意）**: `docker-compose.yml` の **profile `gitlab`**、`gitlab_config` / `gitlab_logs` / `gitlab_data` **永続ボリューム**  
- **起動**: `start_exercise_lab.sh`（フル） / `start_petbattle.sh`（アプリのみ）  
- **停止**: `stop_exercise_lab.sh`（フル） / `stop_petbattle.sh`（アプリのみ）  
- **CI/CD（主）**: `.gitlab-ci.yml`  

## 演習のヒント

`CatResource.java` の `voteStep` や `Sort` を変更し、**GitLab 上の `tech-exercise` に push** してパイプラインを再実行すると挙動の変化を確認できます。

## 旧コンテンツ

講義用ドキュメントは `docs/` にあります。Kubernetes / OpenShift 向けの資産は `old_assets/` を参照してください。
