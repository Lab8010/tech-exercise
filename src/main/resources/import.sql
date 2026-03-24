-- 起動時に投入される初期データ（Hibernate がスキーマ生成後に読み込む）
-- import フェーズでは id を明示する（H2 / PostgreSQL 双方で整合しやすい）
INSERT INTO cat (id, name, count) VALUES (1, 'Mittens', 0);
INSERT INTO cat (id, name, count) VALUES (2, 'Whiskers', 0);
INSERT INTO cat (id, name, count) VALUES (3, 'Luna', 0);
