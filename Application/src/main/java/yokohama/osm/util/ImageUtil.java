package yokohama.osm.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;



public class ImageUtil {

    public static Uri file2Uri(File file) {
        Uri uri = null;
        if (file != null) {
            if (file.exists() == true) {
                uri = Uri.fromFile(file);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return uri;
    }

    public static File uri2File(Uri uri) {
        File file = null;
        file = new File(uri.getPath());
        return file;
    }

    /**
     * 画像縦横回転変換を行うファサードメソッド。
     *
     * @param context
     * @param uri
     * @return
     * @throws FileNotFoundException
     */
    public static String convertRotatedImage2Base64(Context context, Uri uri)
            throws FileNotFoundException {
        String base64 = null;

        Bitmap bitmap = ImageUtil.uri2Bitmap(context, uri);

        File tempFile = FileUtil.saveTemporaryFile(bitmap);

        ImageRotateUtil rotator = new ImageRotateUtil(tempFile.getPath());
        tempFile = rotator.rotateImage();

        bitmap = ImageUtil.file2Bitmap(tempFile);
        base64 = ImageUtil.convertImage2Base64(bitmap);

        return base64;
    }


    public static String  convertImage2Base64(Context context, Uri uri) throws FileNotFoundException {
        String base64 = null;
        InputStream stream = uri2InputStream(context,uri);
        Bitmap bitmap = inputStream2Bitmap(stream);
        byte[] bytes = bitmap2JpegByteArray(bitmap);
        base64 = byteArray2Base64String(bytes);

        return base64;
    }

    public static String convertImage2Base64(Bitmap bitmap) {
        String base64 = null;
        byte[] bytes = bitmap2JpegByteArray(bitmap);
        base64 = byteArray2Base64String(bytes);

        return base64;
    }

    public static Bitmap uri2Bitmap(Context context, Uri uri)
            throws FileNotFoundException {
        InputStream stream = uri2InputStream(context, uri);
        Bitmap image = inputStream2Bitmap(stream);
        return image;
    }

    /**
     * URIから入力ストリームへの変換を行います。
     * @param context
     * @param uri
     * @return
     * @throws FileNotFoundException
     */
    private static InputStream uri2InputStream(Context context, Uri uri)
            throws FileNotFoundException {
        InputStream stream = context.getContentResolver().openInputStream(uri);
        return stream;
    }

    /**
     * 入力ストリームからビットマップへの変換を行います。
     *
     * @param stream
     * @return
     */
    private static Bitmap inputStream2Bitmap(InputStream stream) {
        Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(stream));
        return bitmap;
    }

    /**
     * ビットマップからバイト配列への変換を行います。
     *
     * @param bmp
     * @return
     */
    private static byte[] bitmap2JpegByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();

        return bytes;
    }

    /**
     * バイト配列からBase64文字列に変換を行います。
     *
     * @param bytes
     * @return
     */
    private static String byteArray2Base64String(byte[] bytes) {
        String strBase64 =  Base64.encodeToString(bytes, Base64.DEFAULT);
        return strBase64;
    }

    /**
     *
     * @param base64Str
     * @return
     * @throws IllegalArgumentException
     * @deprecated
     */
    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(base64Str.substring(base64Str.indexOf(",")  + 1), Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     *
     * @param bitmap
     * @return
     * @deprecated
     */
    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }



    /**
     *
     * @param bitmap
     * @return
     * @deprecated
     */
    public static byte[] bitmap2byteArray(Bitmap bitmap) {
        // bitmap(Bitmap)に画像データが入っている前提
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bmparr = byteBuffer.array();

        return bmparr;
    }

    /**
     *
     * @param bytes
     * @return
     */
    public static String byteArray2Base64(byte[] bytes) {
        try {
            Log.i("IMPORTANT", "Base64.encodeToString(bytes, Base64.URL_SAFE) = " + Base64.encodeToString(bytes, Base64.URL_SAFE));
//            return org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
            return Base64.encodeToString(bytes, Base64.URL_SAFE);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param file
     * @return
     */
    public static Bitmap file2Bitmap(File file) {
        Log.d("IMPORTANT","file.getAbsolutePath() = " + file.getAbsolutePath());
        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        return bm;
    }

    public static byte[] bitmap2Jpeg(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] jpgarr = baos.toByteArray();

        if (result)
            return jpgarr;
        else
            return null;
    }

    public static byte[] loadBinaryImage(File file)
           throws IOException {

        try  {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buffer)) != -1) {
                bout.write(buffer, 0, len);
            }
            return bout.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] convertFile(File file) {
        try  {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buffer)) != -1) {
                bout.write(buffer, 0, len);
            }
            return bout.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
