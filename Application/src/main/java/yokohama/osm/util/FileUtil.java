package yokohama.osm.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import yokohama.osm.activity.UploadActivity;
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
     * @param bitmap ビットマップ
     * @return ファイル
     */
    public static File saveTemporaryFile(Bitmap bitmap) {

        File retFile = null;

        try {
            // sdcardフォルダを指定
            //File root = Environment.getExternalStorageDirectory();
            File root = CameraActivity.getInstance().getFilesDir();

            // 日付でファイル名を作成　
            Date mDate = new Date();
            SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd_HHmmss");

            // 保存処理開始
            FileOutputStream fos = null;
            retFile = new File(root, fileName.format(mDate) + ".jpg");
            retFile.createNewFile();
            retFile.deleteOnExit();
            fos = new FileOutputStream(retFile);

            // jpegで保存
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // 保存処理終了
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retFile;
    }

}
