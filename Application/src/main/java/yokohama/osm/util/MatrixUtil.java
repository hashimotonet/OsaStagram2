package yokohama.osm.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MatrixUtil {

    /**
     * プライベートコンストラクタ
     */
    private MatrixUtil() {
        super();
    }

    /**
     * 画像の回転後のマトリクスを取得
     *
     * @param file 入力画像
     * @param matrix 元のマトリクス
     * @return matrix 回転後のマトリクス
     */
    public static Matrix getRotatedMatrix(File file, Matrix matrix){
        ExifInterface exifInterface = null;

        if (file == null ) return null;

        try {
            exifInterface = new ExifInterface(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            return matrix;
        }

        // 画像の向きを取得
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        // 画像を回転させる処理をマトリクスに追加
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                // 水平方向にリフレクト
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                // 180度回転
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                // 垂直方向にリフレクト
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                // 反時計回り90度回転
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                // 時計回り90度回転し、垂直方向にリフレクト
                matrix.postRotate(-90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                // 反時計回り90度回転し、垂直方向にリフレクト
                matrix.postRotate(90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                // 反時計回りに270度回転（時計回りに90度回転）
                matrix.postRotate(-90f);
                break;
        }
        return matrix;
    }

    /**
     * リサイズ・回転後の画像を取得
     * 一時ファイルを作成する
     *
     * @param file オリジナル画像
     * @param matrix 回転・縮小を設定したマトリクス
     * @return file 一時保存先のファイル
     */
    public static File getTemporaryFile(File file, Matrix matrix) {

        Log.w("IMPORTANT", "file = " + file);

        Log.w("IMPORTANT","file.exists() : " + file.exists());

//        if (file == null) {
//            return null;
//        } else if (file.exists() == false) {
//            return null;
//        }

        // 元画像の取得
        Bitmap originalPicture = BitmapFactory.decodeFile(file.getAbsolutePath());
//        Bitmap originalPicture = BitmapFactory.decodeFile(file.getPath());

        Log.w("IMPORTANT","originalPicture = " + originalPicture);

        if (originalPicture == null ) return file;

        int height = originalPicture.getHeight();
        int width = originalPicture.getWidth();

        // マトリクスをつけることで縮小、向きを反映した画像を生成
        Bitmap resizedPicture = Bitmap.createBitmap(originalPicture, 0, 0, width, height, matrix, true);

        // 一時ファイルの保存
        File destination = null;
        try {
            destination = new File(file.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(destination);
            resizedPicture.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // ファイルのインスタンスをリサイズ後のものに変更
            file = destination;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //return file;
        return destination;
    }
}
