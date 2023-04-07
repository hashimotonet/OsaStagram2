package yokohama.osm.activity;

import static android.Manifest.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import yokohama.osm.R;
import yokohama.osm.activity.ui.login.LoginActivity;
import yokohama.osm.util.CameraPermissionUtil;

public class WebViewActivity extends AppCompatActivity {

    //static final String URL = "http://52.68.126.14:8080/PhotoGallery/photo.html";
    //static final String URL = "https://192.168.11.15:8443/PhotoGallery/photo.html";
    static final String URL = "https://192.168.11.15:8443/PhotoGallery/photoOsaStagram.html";
    private static final int REQUEST_SELECT_FILE_CODE = 1;

    private WebView webView;

    /**
     * 利用者ID
     */
    public static String id;

    private static final String TAG = "WebViewActivity";

    // URIリスト(テンポラリ)
    private ArrayList<Uri> UriList = new ArrayList<>();
    // INIファイルのアクセス用
    private SharedPreferences.Editor editor;

    // カメラ & 画像選択ダイアログ用
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private ValueCallback<Uri[]> filePathCallback;

    // パーミッションダイアログ
    public boolean CheckPermission(Activity actibity, String permission, int requestCode){
        // 権限の確認
        if (ActivityCompat.checkSelfPermission(actibity, permission) !=
                PackageManager.PERMISSION_GRANTED) {

            // 権限の許可を求めるダイアログを表示する
            ActivityCompat.requestPermissions(actibity, new String[]{permission},requestCode);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // ストレージの権限確認
        CheckPermission(WebViewActivity.this,
                permission.WRITE_EXTERNAL_STORAGE,
                1000);

        Button listViewButton = (Button)findViewById(R.id.go2List);

        webView = (WebView)findViewById(R.id.photo);
        webView.setWebViewClient(new CustomWebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        // キャッシュクリア
        // ※開発時のみ有効にする
        webView.clearCache(true);

        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    /// パーミッションを許可
                    String[] PERMISSIONS = {
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE
                    };
                    request.grant(request.getResources());
                    request.grant(PERMISSIONS);
                }
            }

            /**
             * 「ファイルを選択」ボタンが押された時
             * For Android > 5.0
             * @param webView
             * @param filePathCallback
             * @param fileChooserParams
             * @return
             */
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                showFileChooser(filePathCallback, fileChooserParams);
                return true;
            }
        });

        // <input type="file">で「カメラ、画像の選択」を表示する
        // ※元ソースはGoogleです。参考URLを参照してください。
        // ※そのままでは動作しないので改変しています。
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                mCameraPhotoPath = null;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(WebViewActivity.this.getPackageManager()) != null) {
                    String filename = Environment.getExternalStorageDirectory() + "/dummy.jpg";
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, filename);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                    Uri uri = getContentResolver().
                            insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    UriList.add(uri);
                    // INIファイルにURIリストの情報を書き込む
                    for (int i = 0; i < UriList.size(); i++) {
                        editor.putString("uri" + i, UriList.get(i).toString());
                    }
                    editor.putInt("count", UriList.size());
                    editor.apply();

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    mCameraPhotoPath = uri.toString();
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");

                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "選択");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                return true;
            }
        });

        webView.loadUrl(URL);

        listViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListImagesActivity listImages = new ListImagesActivity();
//                String id = mEmail;
                Intent intent;
                intent = new Intent(WebViewActivity.this, ListImagesActivity.class);
                intent.putExtra("id", "hashimoto.osamu@gmail.com");
                startActivity(intent);
                WebViewActivity.this.finish();
            }

        });

        //setContentView(webView);
    }

    static final int REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが必要な処理
                    ActivityCompat.requestPermissions(this, new String[]{
                            permission.CAMERA
                    }, REQUEST_CODE);
                } else {
                    // パーミッションが得られなかった時
                    // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
                    Toast.makeText(this, "パーミッションが得られなかった", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        // 完了していない処理があれば完了する
        if (this.filePathCallback != null) {
            this.filePathCallback.onReceiveValue(null);
        }
        this.filePathCallback = filePathCallback;

        // 権限がないときは、権限を要求する
        if (!CameraPermissionUtil.checkAndRequestPermissions(this, PackageManager.PERMISSION_GRANTED)) {
            this.filePathCallback.onReceiveValue(null);
            this.filePathCallback = null;
            return;
        }

        // カメラとファイルのインテントを作成する
        Intent chooserIntent = Intent.createChooser(fileChooserParams.createIntent(), "写真の選択");
        Uri mImageUri;
        try {
            mImageUri = createImageFile();
            Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{imageCaptureIntent});
        } catch (IOException ex) {
            ex.printStackTrace();
            mImageUri = null;
        }
        startActivityForResult(chooserIntent, REQUEST_SELECT_FILE_CODE);
    }

    private Uri createImageFile() throws IOException {
        File folder = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = String.format("MyApp_%s.jpg", date);
        File cameraFile = new File(folder, fileName);

        return FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                cameraFile);
    }

    // <input type="file">で「カメラ、画像の選択」を表示する
    // ※元ソースはGoogleです。参考URLを参照してください。
    // ※そのままでは動作しないので改変しています。
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                if (mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};

                    // このコードがないとAndroid8ではカメラ写真を取れない
                } else {
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                }
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
        return;
    }
 }

class CustomWebViewClient extends WebViewClient {
    public CustomWebViewClient() {
        super();
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }
}
