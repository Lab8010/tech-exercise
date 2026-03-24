# tech-exercise — CI/CD 演習用（Podman 1 台完結）

CentOS Stream などのノート PC と **Podman** で完結する、**Quarkus 3.x（Java 17）** の「猫の投票 API」演習用リポジトリです。以前の OpenShift / Tekton / Helm 向け資産は `old_assets/` に退避しています。

## 構成

- **API**: Quarkus REST + Hibernate ORM Panache + PostgreSQL
- **ブラウザ UI**: トップ `http://localhost:8080/` で猫の画像と投票（`＋票` / `−票`）
- **コンテナ**: `Dockerfile`（マルチステージ）、`docker-compose.yml`（`db` + `api`）
- **起動**: `podman-compose` 用 `start_petbattle.sh` / `stop_petbattle.sh`
- **パイプライン例**: ルートの `Jenkinsfile`

## 手元での動かし方

前提: Maven 3.9+（Java 17）、Podman、`podman-compose`。

```shell
mvn clean package          # ビルド・テスト
./start_petbattle.sh       # DB + API をバックグラウンド起動
# ブラウザで http://localhost:8080/ を開く（画像＋投票）
curl -s http://localhost:8080/cats
curl -s -X POST http://localhost:8080/cats/1/vote
```

`podman ps` で `api` と `db` の 2 コンテナが動いていることを確認してください。

## 演習のヒント

`src/main/java/com/example/petbattle/CatResource.java` の投票増分（既定 +10 / `down=true` で -10）を書き換え、Jenkins から `Jenkinsfile` のパイプラインを実行すると、デプロイ後の挙動変化を確認できます。

## 旧コンテンツ

講義用ドキュメントは引き続き `docs/` にあります。Kubernetes / OpenShift 向けの Tekton・Helm・quick-starts 等は `old_assets/` を参照してください。
