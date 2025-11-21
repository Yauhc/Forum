# MyForum

軽量な掲示板システム。Spring Boot 3 / MyBatis-Plus / MySQL / Vue をベースに、JWT と CSRF トークンで保護された API を提供します。

## 機能概要
- ユーザー登録 / ログイン / トークンリフレッシュ
- JWT + リフレッシュトークンによる stateless 認証（既存セッション互換）
- 投稿一覧 / 投稿作成（CSRF 対策 + 入力検証）
- 共通レスポンス `R`・エラーコード体系・グローバル例外ハンドリング
- 静的ページ + Vite/Vue クライアントの下準備
- 単体テスト（AuthService）

## 技術スタック
- Java 17, Spring Boot 3.3.4, Spring Security
- MyBatis-Plus 3.5.7, MySQL 8+
- JWT: `io.jsonwebtoken 0.12.5`
- フロント: 素の HTML/CSS/JS（`src/main/resources/static`）、Vite + Vue（`forum-frontend`）

## セットアップ
1. **環境変数**（`.env` などで管理）  
   | 変数 | 説明 | 既定値 |
   | --- | --- | --- |
   | `DB_URL` | JDBC 接続 URL | `jdbc:mysql://localhost:3306/forum?...` |
   | `DB_USERNAME` / `DB_PASSWORD` | DB 認証情報 | `root` / `123456` |
   | `JWT_SECRET` | 32byte 以上のシークレット | `change-me-...` |
   | `JWT_ACCESS_TTL` | アクセストークン寿命(秒) | `900` |
   | `JWT_REFRESH_TTL` | リフレッシュトークン寿命(秒) | `604800` |

2. **DB 準備**  
   `docs/db/schema.sql` を参考にテーブルを作成するか、`spring.jpa.hibernate.ddl-auto=update` で自動生成を許可します。

3. **依存解決 & 起動**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **フロントエンド (任意)**  
   ```bash
   cd forum-frontend
   npm install
   npm run dev
   ```

## 認証フロー
1. `/users/login` でアクセストークン + リフレッシュトークンを取得（JSON or form）  
2. フロントは `localStorage` にトークン保存、`Authorization: Bearer` で送信  
3. 有効期限切れ時は `/users/refresh-token` で再発行  
4. セキュリティ: `CookieCsrfTokenRepository` + `X-XSRF-TOKEN` ヘッダ、機密情報はログに出さない

## 主要 API
| メソッド | パス | 説明 |
| --- | --- | --- |
| `POST /users/register` | ユーザー登録（JSON Body） |
| `POST /users/login` | ログイン（JSON / Form） |
| `POST /users/refresh-token` | リフレッシュトークンで再発行 |
| `GET /users/me` | 現在ユーザーを取得（要 Bearer Token） |
| `POST /myforum/createPost` | 投稿作成（要認証 + CSRF） |
| `GET /myforum/listPosts` | 投稿一覧（ページング） |
| `GET /csrf-token` | CSRF トークン取得（初期化用） |

`R` レスポンス例:
```json
{
  "code": 200,
  "message": "success",
  "user": {
    "id": 1,
    "username": "alice"
  }
}
```

## テスト
```bash
mvn test
```

## 今後のTODO（抜粋）
- コメント・いいね・お気に入り・通報などのドメイン拡張
- 投稿検索・タグ・版管理・Markdown/富テキスト対応
- グローバル監査ログ / 操作ログ
- Vue クライアント実装と API クライアント共通化
- Flyway / Liquibase によるスキーマ管理

