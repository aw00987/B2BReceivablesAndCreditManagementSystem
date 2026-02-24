# API仕様書 (API Specification)

## 1. 概要
本ドキュメントは、B2B売掛金・債権管理システム（RCMS）のバックエンドAPIエンドポイントを定義する。
ベースURL: `/api/v1`

---

## 2. 認証 (Authentication)
### 2.1 ログイン
- **URL**: `/auth/login`
- **Method**: `POST`
- **説明**: ユーザー名とパスワードで認証し、JWTトークンを取得する。
- **Request Body**:
  - `username` (String): ユーザー名
  - `password` (String): パスワード
- **Response**: `LoginResponseDto` (Token, Username, Role)

---

## 3. ユーザー管理 (User Management)
### 3.1 ユーザー作成
- **URL**: `/users`
- **Method**: `POST`
- **説明**: 新規ユーザーを登録する（管理者権限が必要）。
- **Response**: `User` オブジェクト

### 3.2 ユーザー一覧取得
- **URL**: `/users`
- **Method**: `GET`
- **説明**: 有効なユーザーの一覧をページング形式で取得する。
- **Query Params**: `page`, `size`, `sort`

### 3.3 ユーザー無効化
- **URL**: `/users/{username}`
- **Method**: `DELETE`
- **説明**: 指定したユーザーを無効化（論理削除）する。

---

## 4. 取引先管理 (Company Management)
### 4.1 取引先登録
- **URL**: `/companies`
- **Method**: `POST`
- **説明**: 新規取引先企業を登録する。

### 4.2 取引先一覧取得
- **URL**: `/companies`
- **Method**: `GET`
- **説明**: 取引先企業の一覧をページング形式で取得する。

### 4.3 取引先検索
- **URL**: `/companies/search`
- **Method**: `GET`
- **説明**: 企業名による部分一致検索を行う。
- **Query Params**: `userInput` (String)

---

## 5. 請求管理 (Invoice Management)
### 5.1 請求書作成
- **URL**: `/invoices`
- **Method**: `POST`
- **説明**: 新規請求書を発行する。

### 5.2 請求書一覧取得
- **URL**: `/invoices`
- **Method**: `GET`
- **説明**: 請求書の一覧をページング形式で取得する。

### 5.3 請求書詳細取得
- **URL**: `/invoices/{invoiceNo}`
- **Method**: `GET`
- **説明**: 指定した請求書番号の詳細情報を取得する。

### 5.4 督促ステータス更新
- **URL**: `/invoices/{invoiceNo}/dunning`
- **Method**: `PUT`
- **説明**: 請求書を「督促中」ステータスに変更し、遅延損害金の計算対象とする。
- **Query Params**: `note` (String, Optional)

### 5.5 法的措置ステータス更新
- **URL**: `/invoices/{invoiceNo}/litigation`
- **Method**: `PUT`
- **説明**: 請求書を「法的措置」ステータスに変更する。
- **Query Params**: `note` (String, Optional)

### 5.6 ダッシュボード統計取得
- **URL**: `/invoices/statistics`
- **Method**: `GET`
- **説明**: 売掛金総額、延滞金額、回収率などの統計情報を取得する。

---

## 6. 入金消込 (Reconciliation)
### 6.1 自動消込実行
- **URL**: `/reconciliations/auto`
- **Method**: `POST`
- **説明**: 銀行取引明細CSVファイルをアップロードし、請求書との自動マッチング・消込を行う。
- **Request Params**: `file` (MultipartFile)
- **Response**: マッチ件数、合計金額を含む統計データ
