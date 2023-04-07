package yokohama.osm.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import yokohama.osm.R;

import static yokohama.osm.util.ImageUtil.convertImage2Base64;
import static yokohama.osm.util.ImageUtil.convertRotatedImage2Base64;
import static yokohama.osm.util.ImageUtil.uri2File;

public class UploadActivity extends Activity
                                implements View.OnClickListener{

    /**
     * 画像イメージのURI
     */
    private Uri _uri;

    /**
     * 利用者ID
     */
    private String id;

    /**
     * デフォルトコンストラクタ
     */
    public UploadActivity() {
        super();
    }

    /**
     * アクティビティ起動時に実行されるメソッド。
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i("IMPORTANT", "UploadActivity#onCreate() called.");

        super.onCreate(savedInstanceState);

        //
        // レイアウトファイルの取り込み。
        setContentView(R.layout.upload);

        //
        // アップロード処理ボタンのオンクリックリスナ登録
        findViewById(R.id.upload).setOnClickListener(this);

        // 一覧画面表示処理ボタンのオンクリックリスナ登録
        findViewById(R.id.listImages).setOnClickListener(this);

        // キャンセル処理ボタンのオンクリックリスナ登録
        findViewById(R.id.cancel).setOnClickListener(this);

        // ネットワーク利用可能可否の判定
        if(isNetworkAvailable() == false) {

            // 「ネットワークが利用できません」
            onNetworkIsNotAvailable();

        }

        // 前画面からの情報取得。
        Intent intent = getIntent();
        String extra = intent.getStringExtra("Uri");
        this.id =     intent.getStringExtra("id");
        Log.i("IMPORTANT", "extra = " + extra);
        Log.w("IMPORTANT", "id = " + this.id);


        _uri = Uri.parse(extra);
        File file = uri2File(_uri);
        if (file != null) {
            Log.i("IMPORTANT", "file.exists() = " + file.exists());
        } else {
            Log.w("IMPORTANT", "File is null.....");
        }

        ImageView ivPhoto = findViewById(R.id.uploadImageView);
        ivPhoto.setImageResource(R.drawable.tile);

        Log.i("IMPORTANT", "(ivPhoto != null) : " + (ivPhoto != null));

        try {
            // 画像表示の非同期クラス起動。
            DownloadImageTask async = new DownloadImageTask(ivPhoto);

            // 非同期処理起動。
            // 前画面から渡されたURIを基に、画像表示を行う。
            async.execute(extra);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("IMPORTANT","uri = " + _uri.toString());

    }

    /**
     * アップロード処理完了後は、画像は内部ストレージに残さず
     * クラウドネットワークから取得するので、内部ストレージに
     * 保存したファイルは削除処理を行う。
     */
    @Override
    public void onDestroy() {
        if (_uri != null) {
            File file = new File(_uri.getPath());
            if (file != null) {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        super.onDestroy();
    }

    /**
     * URIからBitmapを取得する。
     * 現在未使用。
     *
     * @param url
     * @return
     */
    private Bitmap getImageBitmap(String url)  {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMPORTANT", "Error getting bitmap", e);
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * [Upload] / [Cancel] 二つのボタンに対応したイベントハンドラ。
     */
    @Override
    public void onClick(View view) {

        // 引数のビューよりIDを取得し、
        // 『どのボタンがタップされたか？』判定する。
        switch (view.getId()) {

            // アップロードボタンである場合
            case R.id.upload: {

                //
                // 画像アップロード処理
                //
                String base64 = null;

                // 画像URIより、Base64文字列を生成する。
                try {
//                    base64 = convertImage2Base64(getApplicationContext(),this._uri);
                    // 画像の縦横の変換を行う。
                    base64 = convertRotatedImage2Base64(getApplicationContext(),this._uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // アップロード非同期クラスをコールする。
                Upload upload = new Upload();

                // 非同期アップロード処理実行。
                // サーバへのアップロードと、データベースへの書き込みを行う。
                upload.execute(this.id, base64);

                // 処理を抜ける。
                break;
            }

            // 一覧表示ボタンである場合
            case R.id.listImages: {
                // 一覧画面表示アクティビティを起動
                startListImagesActivity();

                // 当画面の処理を終える
                super.finish();

                // switchを抜ける。
                break;
            }

            // キャンセルボタンが
            case R.id.cancel: {
                //
                // アップロードキャンセル処理
                //
                super.finish();

                // switch を抜ける。
                break;
            }
        }
    }

    /**
     * 一覧画面アクティビティの起動を行う。
     */
    private void startListImagesActivity() {
        Log.i("IMPORTANT","UploadActivity#startListImagesActivity() called.");

        // 一覧表示画面生成のためにインテントを生成する。
        Intent intent = new Intent(this, ListImagesActivity.class);

        // インテントの処理に必要な引数をセットする。
            // 現在、IDは"hashimoto"固定である。
            // 次フェーズ以降では、IDの取得はログインから取得することとする。
        intent.putExtra("id", this.id);
        intent.putExtra("status","uploaded");

        // 一覧画面アクティビティの起動を行う。
        startActivity(intent);
        Log.i("IMPORTANT","New Activity started.");
    }

    /**
     * インターネット上の画像表示を行うインナークラス。
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        /**
         * 表示のためのイメージビュー
         */
        ImageView bmImage;

        /**
         * コンストラクタ。
         * 引数はImageView型である。
         *
         * @param bmImage ImageView Androidの描画形式であるイメージビューオブジェクト
         */
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        /**
         * 当クラスメインの非同期処理。
         * String型の可変長引数を受け取れる形式だが、一つの引数にのみ対応しており、
         * 2つ目以降の引数には対応していない。
         * Bitmap型を返却する。
         *
         * @param urls URL群
         * @return Bitmap 当メソッドで取得されたビットマップ
         */
        protected Bitmap doInBackground(String... urls) {

            // 可変長引数より、最初の引数であるURLを取得。
            String urldisplay = urls[0];

            // 返却値であるBitmap。
            Bitmap mIcon11 = null;

            // try 節に入る。
            try {
                // URL文字列より入力ストリームを取得する。
                InputStream in = new java.net.URL(urldisplay).openStream();

                // BitmapFactoryのdecodeStreamメソッドで、
                // 入力ストリームよりビットマップを取得する。
                mIcon11 = BitmapFactory.decodeStream(in);
                Log.i("IMPORTANT", "(mIcon11 != null) : " + (mIcon11 != null));
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            // 取得したビットマップを返却する。
            return mIcon11;
        }

        /**
         * doInTheBackground() メソッドの後に呼び出される処理。
         *
         * @param result Bitmap doInTheBackGround() メソッドの返却値。
         */
        protected void onPostExecute(Bitmap result) {

            // インスタンス変数のイメージビューに、ビットマップ画像をセットする。
            bmImage.setImageBitmap(result);
            Log.i("IMPORTANT","(bmImage != null) : " + (bmImage != null) );
            Log.i("IMPORTANT","(result != null)  : " + (result != null));
            Log.i("IMPORTANT", "DownloadImageTask#onPostExecute() finished.");
        }
    }


    /**
     * インターネット接続インナークラス
     */
    private class Upload extends AsyncTask<String, Void, String> {

        @Override
        public String doInBackground(String... params) {
            //可変長引数でPOSTパラメータ書式生成
            String queryString = createQueryString(params);
            //IDと画像データを使って接続URL文字列を作成。
            String urlStr = "http://52.68.110.102:8080/PhotoGallery/Upload";
            urlStr = "https://192.168.11.10:8080/PhotoGallery/Upload";
            //要求受信結果である応答を格納。
            String result = "";

            //http接続を行うHttpURLConnectionオブジェクトを宣言。finallyで確実に解放するためにtry外で宣言。
            HttpURLConnection con = null;

            //http接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
            InputStream is = null;

            try {
                //URLオブジェクトを生成。
                URL url = new URL(urlStr);

                //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                con = (HttpURLConnection) url.openConnection();

                //http接続メソッドを設定。
                con.setRequestMethod("POST");

                // no Redirects
                //con.setInstanceFollowRedirects(false);

                // データを書き込む
                con.setDoOutput(true);

                // 応答取得
                con.setDoInput(true);

                // ヘッダ設定
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                con.setUseCaches(false);

                con.setFixedLengthStreamingMode(queryString.getBytes().length);

                //接続。
                con.connect();

                // POSTデータ送信処理
               OutputStream out = null;

                try {
                    out = con.getOutputStream();
                    out.write( queryString.getBytes("UTF-8"));
                    out.flush();
                    Log.d("IMPORTANT","flush");
                } catch (IOException e) {
                    // POST送信エラー
                    e.printStackTrace();
                    result="POST送信エラー";
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }

                    // 接続ステータスコード取得
                //
                int status = con.getResponseCode();

                Log.d("IMPORTANT","status = " + status);

                //HttpURLConnectionオブジェクトからレスポンスデータを取得。
                is = con.getInputStream();

                //レスポンスデータであるInputStreamオブジェクトを文字列に変換。
                result = convertInputStreamToString(is);
            }
            catch(MalformedURLException ex) {
                ex.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch(IOException ex) {
                ex.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
            } finally {
                //HttpURLConnectionオブジェクトがnullでないなら解放。
                if(con != null) {
                    con.disconnect();
                }
                //InputStreamオブジェクトがnullでないなら解放。
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch(IOException ex) {
                    }
                }
            }

            Log.d("IMPORTANT", result);

            //サーバ応答文字列を返す。
            return result;
        }

        @Override
        public void onPostExecute(String result) {
            String success = result;
            String message = "";

            Log.d("IMPORTANT", "result = " + result);

            if (success.startsWith("success")) {
                message = "アップロードが完了しました！";
            } else {
                message = "アップロードが失敗しました。";
            }

            Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();

        }

        /**
         * POSTのクエリ文字列を生成します。
         *
         * @param params
         * @return POSTクエリ文字列
         */
        private String createQueryString(String[] params) {
            StringBuilder sb = new StringBuilder();
            int index = 0;

            for(String param : params) {
                if(index == 0) {
                    try {
                        sb.append("id=" + URLEncoder.encode(params[index], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (index == 1) {
                    sb.append("&base64=" + params[index]);
                }
                index++;
            }

            Log.d("IMPORTANT", sb.toString());

            return sb.toString();
        }

        /**
         * InputStreamオブジェクトを文字列に変換するメソッド。変換文字コードはUTF-8。
         *
         * @param is 変換対象のInputStreamオブジェクト。
         * @return 変換された文字列。
         * @throws IOException 変換に失敗した時に発生。
         * @deprecated
         */
        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while(0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
                Log.d("IMPORTANT", "sb.toString() = " + sb.toString());
            }
            return sb.toString();
        }

        /**
         * 入力ストリームから文字列への変換を行います。
         *
         * @param is InputStream 入力ストリーム
         * @return String 文字列
         * @throws IOException
         */
        public String convertInputStreamToString(InputStream is) throws IOException {
            InputStreamReader reader = new InputStreamReader(is);
            StringBuilder builder = new StringBuilder();
            char[] buf = new char[1024];
            int numRead;
            while (0 <= (numRead = reader.read(buf))) {
                builder.append(buf, 0, numRead);
            }
            return builder.toString();
        }
    }


    /**
     * 現在未使用
     *
     * @param bitmap
     */
    protected void saveBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("hogehoge", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("key", bitmapStr);
        editor.apply();
    }

    /**
     * 現在未使用
     *
     * @return
     */
    protected Bitmap loadSavedBitmap() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("hogehoge", Context.MODE_PRIVATE);
        String s = pref.getString("key", "");
        if (!s.equals("")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            byte[] b = Base64.decode(s, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length).copy(Bitmap.Config.ARGB_8888, true);

            return bitmap;
        }

        return null;
    }

    /**
     * 現在未使用
     *
     * @return
     */
    protected String loadImageBase64() {
        Context context = getApplicationContext();
        SharedPreferences pref = context.getSharedPreferences("hogehoge", Context.MODE_PRIVATE);
        String base64 = pref.getString("key", "");
        if (!base64.equals("")) {

            Log.i("IMPORTANT","base64 = " + base64);

            return base64;
        } else Log.w("IMPORTANT", "There is NOTHING to upload!");

        return null;
    }

    /**
     * ネットワークが利用可能か判定し、判定結果を返却します。
     *
     * @return
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * ネットワーク利用不可の際に画面にメッセージを表示する。
     */
    private void onNetworkIsNotAvailable() {
        String message = "ネットワークが利用できません。";
        Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();
    }
}
