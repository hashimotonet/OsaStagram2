package yokohama.osm.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import yokohama.osm.activity.UploadActivity;
import yokohama.osm.camera.CameraAlignmentActivity;
import yokohama.osm.camera2basic.CameraActivity;

import static java.nio.file.Files.readAllBytes;

public class FileUtil {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] convertFile(File file) throws IOException {
        return readAllBytes(file.toPath());
    }

    /**
     * ビットマップを引数に、ファイルを生成して返却します。
     *
     * @param bmp ビットマップ
     * @param context コンテキスト
     * @param resolver コンテントレゾルバ
     * @param imageView 画面描画領域
     * @return URI
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Uri saveTemporaryFile(Bitmap bmp, Context context, ContentResolver resolver, ImageView imageView) {

        Uri item = null;

        try {
            // 日付でファイル名を作成　
            Date mDate = new Date();
            SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd_HHmmssSS");

            // 保存処理開始
            if(isExternalStorageWritable()) {

                ContentValues values = new ContentValues();
                // コンテンツ クエリの列名
                // ファイル名
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName.format(mDate) + ".png");
                // MIMEの設定
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                // 書込み時にメディア ファイルに排他的にアクセスする
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                //ContentResolver resolver = context.getContentResolver();
                Uri collection = MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY);
                item = resolver.insert(collection, values);

                try (OutputStream outstream = resolver.openOutputStream(item)) {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                    imageView.setImageBitmap(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                values.clear();
                //　排他アクセスの解除
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(item, values, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    /**
     *　外部ストレージに書き込み（読み込み）が可能か検査して結果を返却する。
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }
}
