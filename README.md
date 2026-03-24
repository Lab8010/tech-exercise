# tech-exercise — CI/CD 演習用（Podman 1 台完結）

CentOS Stream などのノート PC と **Podman** で完結する、**Quarkus 3.x（Java 17）** の「猫の投票 API」演習用リポジトリです。以前の OpenShift / Tekton / Helm 向け資産は `old_assets/` に退避しています。

## 初めて使う方へ（起動から使い方まで）

次の手順で、ローカルに API とブラウザ画面が立ち上がります。

### 1. 必要なもの

| もの | 説明 |
|------|------|
| **Git** | リポジトリの取得用 |
| **Java 17** | 例: Eclipse Temurin 17 |
| **Apache Maven 3.9+** | `mvn` でビルド・テストする場合（コンテナビルドのみなら省略可） |
| **Podman** | コンテナ実行用 |
| **Compose** | `podman-compose` パッケージ、または `podman compose`（Docker Compose 互換） |

`podman-compose` が無い環境では、リポジトリの `start_petbattle.sh` が **`podman compose`** にフォールバックします。

### 2. リポジトリを手元に取る

```shell
git clone https://github.com/Lab8010/tech-exercise.git
cd tech-exercise
```

（フォークや別のリモートを使う場合は、その URL に読み替えてください。）

### 3. アプリを起動する（推奨: スクリプト）

リポジトリのルートで次を実行します。

```shell
./start_petbattle.sh
```

初回は PostgreSQL と API 用イメージの取得・ビルドに時間がかかることがあります。エラーなく終わったら、バックグラウンドで **DB** と **API** の 2 コンテナが動いています。

### 4. 動いているか確認する

```shell
podman ps
```

`api` と `db` に相当するコンテナが `Up` になっていればよいです。

### 5. ブラウザで使う

1. ブラウザで **http://localhost:8080/** を開く  
2. 猫のカードが **得票数の多い順**（同票のときは ID 昇順）で並びます  
3. **＋票 / −票** で投票（1 ずつ増減）  
4. **猫を追加** で名前と画像を送ると、同じ一覧に新しい猫が表示されます  

アップロードした画像はホストの **`./uploads`** に保存されます（`docker-compose.yml` のボリューム設定）。

### 6. API だけ試す（任意）

```shell
curl -s http://localhost:8080/cats
curl -s -X POST http://localhost:8080/cats/1/vote
```

`1` は猫の ID です。ブラウザの一覧や `GET /cats` の JSON で ID を確認してください。

### 7. 止める

リポジトリのルートで:

```shell
./stop_petbattle.sh
```

### 8. ビルドだけ先に確認したい場合（任意）

Java と Maven がある環境では、ホスト上で次も実行できます。

```shell
mvn clean package
```

テスト通過と JAR 生成の確認用です。コンテナ起動は引き続き `start_petbattle.sh` で問題ありません。

---

## 構成

- **API**: Quarkus REST + Hibernate ORM Panache + PostgreSQL
- **ブラウザ UI**: トップ `http://localhost:8080/` で猫の画像・投票（±1 票）・画像追加（ファイル選択）
- **一覧の並び**: 得票数 **降順**（同票は ID 昇順）
- **アップロード保存先**: コンテナでは `./uploads` を `/deployments/uploads` にマウント（`docker-compose.yml`）
- **コンテナ**: `Dockerfile`（マルチステージ）、`docker-compose.yml`（`db` + `api`）
- **起動**: `start_petbattle.sh` / `stop_petbattle.sh`（`podman-compose` または `podman compose`）
- **パイプライン例**: ルートの `Jenkinsfile`

## 演習のヒント

`src/main/java/com/example/petbattle/CatResource.java` の `voteStep`（既定は 1）や並び順の `Sort` を書き換え、Jenkins から `Jenkinsfile` のパイプラインを実行すると、デプロイ後の挙動変化を確認できます。

## 旧コンテンツ

講義用ドキュメントは引き続き `docs/` にあります。Kubernetes / OpenShift 向けの Tekton・Helm・quick-starts 等は `old_assets/` を参照してください。
