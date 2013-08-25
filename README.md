ライフログ - Play framework諸々検証
===================================

プロジェクト概要
================
Play frameworkをどう使っていくかを、実際にありそうな (かつ、複雑でない)
アプリケーションを作りながら、「興味の赴くままに」検証しています。

検証課題
========
*   複数プロジェクト構成
    *   公開系Web、管理系Web、バッチの3つのプロジェクトが、1つの共通プ
        ロジェクトを参照する。
    *   共通プロジェクトに依存ライブラリを指定すると、参照元のプロジェ
        クトにも引き継がれること。
    *   eclipseコマンドを実行してEclipseにインポートした時にプロジェク
        トの依存関係をEclipseが認識すること。
*   認証でアクセス制御 (アクションコンポジション)
*   画面に共通的に表示するデータ (ログインユーザ) の渡し方
*   画面遷移の実装パターン
*   ページネーション
*   Formの構成方法の整理
    *   Formインスタンスをどこに定義するか
    *   Formで参照するパラメタ (定数) をどこに定義するか
    *   Formのバインド相手: クラス、タプル
*   入力項目のフィールドテンプレートをカスタマイズ
*   表示時の書式整形をどこに定義するか
*   flashメッセージ表示の場合分けの仕方
*   データキャッシュ (Cache)
*   プラグイン: 初期管理者パスワードの設定を題材として
*   フィルター: 操作ログを題材として
*   実行時のパラメタ切替え
    *   DB接続先URL = db.default.url
    *   HTTPポート = http.port
*   ログ出力設定の変更
    *   設定ファイル: application-logger.xml
    *   ローテーション、世代削除
*   非同期処理 (Akka actor)
    *   ルータで振分け: ラウンドロビン、(アクタ数の) リサイザ
    *   時間のかかる処理を、アクションで受付けて、アクタで主たる処理。
        *   エクスポート
        *   インポート
        *   DB接続はアクションではなく、アクタ側で張る必要がある。
*   エクスポート
    *   RDBMSのカーソル相当の制御 (Stream[Row])。
    *   遅延評価。Stream[Row] で「条件合致の全レコード」を表すが、実際
        に取得するのは参照した時。
*   インポート
    *   CSVから読込んで一レコードずつ処理。
    *   Seq[String]からMap[String, String]へ変換し、Formにバインドして
        一レコードずつバリデーション。
*   バッチフレームワーク
    *   バッチもWebと同じく、WithApplicationでテストできるように。
*   テストデータ (WithApplication拡張)
*   Eclipseの「importの_を展開しない」の対象の検討


ライセンス
==========
>  Copyright 2013 agwlvssainokuni
>
>  Licensed under the Apache License, Version 2.0 (the "License");
>  you may not use this file except in compliance with the License.
>  You may obtain a copy of the License at
>
>      http://www.apache.org/licenses/LICENSE-2.0
>
>  Unless required by applicable law or agreed to in writing, software
>  distributed under the License is distributed on an "AS IS" BASIS,
>  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>  See the License for the specific language governing permissions and
>  limitations under the License.
