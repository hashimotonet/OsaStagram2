package yokohama.osm.camera;

import static androidx.activity.result.contract.ActivityResultContracts.GetContent;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import yokohama.osm.R;
import yokohama.osm.activity.UploadActivity;

public class CameraAlignmentActivity extends AppCompatActivity {

    private MyLifecycleObserver mObserver;

    private ImageView imageView;
    private View imageCaptureButton;
    private View getContentButton;
    private View submitButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private final int REQUEST_TAKE_PHOTO = 1;

    private String currentPhotoPath;

    /**
     * 利用者ID
     */
    public static String id = "hashimoto";

    /*
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        PackageManager pm = getApplicationContext().getPackageManager();
        List<PackageInfo> appInfoList = pm.getInstalledPackages(0);
        for(int i =0;i<appInfoList.size();i++){
            Log.d("CameraAlignmentActivity",appInfoList.get(i).applicationInfo.loadLabel(pm).toString());
        }
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    } */

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            android.os.Bundle extras = data.getExtras();
            android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");

            // カメラ画像イメージをバイト配列に変換
            ByteBuffer byteBuffer = ByteBuffer.allocate(imageBitmap.getByteCount());
            imageBitmap.copyPixelsToBuffer(byteBuffer);
            byte[] bmparr = byteBuffer.array();

            java.io.File file = null;
            try {
                file = createImageFile();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            java.io.ByteArrayOutputStream bytesOutStream = null;
                java.io.FileOutputStream fileOutputStream  = null;
            try {
                fileOutputStream  = new java.io.FileOutputStream(file);
                imageBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.close();

                // イメージを反転
                imageBitmap = rotateImageIfRequired(imageBitmap, this.getBaseContext(), android.net.Uri.fromFile(file));
            } catch (java.io.IOException e) {
                // エラーメッセージ表示
                android.widget.Toast.makeText(this, e.getLocalizedMessage(), android.widget.Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            imageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_alignment);

        imageView = (ImageView)findViewById(R.id.image_view);
        imageCaptureButton = (View)findViewById(R.id.camera_button);
        getContentButton = (View) findViewById(R.id.get_content_button);
        submitButton = (View)findViewById(R.id.both_button);

        enableNormalFunction();

        CheckPermission(CameraAlignmentActivity.this,
                Manifest.permission.CAMERA,
                PackageManager.PERMISSION_GRANTED);

        mObserver = new MyLifecycleObserver(CameraAlignmentActivity.this.getActivityResultRegistry());
        getLifecycle().addObserver(mObserver);
    }

    private void nextPage(Bitmap bmp) {
        Intent intent = new Intent(this, UploadActivity.class);
        intent.putExtra("id", CameraAlignmentActivity.id);
        intent.putExtra("data", byteArray2Base64String(bmpBlob2byteArray(bmp)));
        intent.putExtra("height", imageView.getHeight());
        intent.putExtra("width", imageView.getWidth());
        intent.putExtra("layoutWidth", bmp.getScaledHeight(imageView.getHeight()));
        intent.putExtra("layoutHeight", bmp.getScaledWidth(imageView.getWidth()));
        startActivity(intent);
    }

    /**
     * 引数のビットマップをバイト配列に変換する。
     *
     * @param bitmap
     * @return
     */
    public byte[] bmp2byteArray(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bmparr = byteBuffer.array();
        return bmparr;
    }

    /**
     * 引数のバイト配列をBase64形式にエンコードする。
     *
     * @param array
     * @return
     */
    public String byteArray2Base64String(byte[] array) {
        String encImage = Base64.encodeToString(array, Base64.DEFAULT);
        return encImage;
    }

    @Deprecated
    public byte[] bmpBlob2byteArray(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /* Ignored for PNGs */, blob);
        byte[] bitmapdata = blob.toByteArray();
        return bitmapdata;
    }

    private void enableNormalFunction() {
        // 送信ボタンにイベントリスナ設定
        if (null != submitButton) {
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("log","test");
                    //mObserver.selectImage();
                    // 次画面であるアップロード画面にdataオブジェクトを渡す処理。
                    Bitmap bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    if (null != bmp) {
                        nextPage(bmp);
                    } else {
                        System.out.println("画像データがありません。");
                    }
                }

            });
        }

        // イメージ選択ボタンにイベントリスナ設定
        if (null != getContentButton) {
            getContentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("log","test");
                    mObserver.selectImage();
                }
            });
        }

        // カメラボタンにイベントリスナ設定t
        if ( null != imageCaptureButton) {
            imageCaptureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("log",v.getTransitionName() + " clicked.");
                    dispatchTakePictureIntent();
                }
            });
        }
    }

    // パーミッションダイアログ
    public boolean CheckPermission(Activity activity, String permission, int requestCode){
        // 権限の確認
        if (ActivityCompat.checkSelfPermission(activity, permission) !=
                PackageManager.PERMISSION_GRANTED) {

            // 権限の許可を求めるダイアログを表示する
            ActivityCompat.requestPermissions(activity, new String[]{permission},requestCode);
            return false;
        }
        return true;
    }

    /**
     * Rotate an image if required.
     * https://www.samieltamawy.com/how-to-fix-the-camera-intent-rotated-image-in-android/
     *
     * @param bitmap The image bitmap
     * @param context
     * @param uri    Image URI
     * @return The resulted Bitmap after manipulation
     */
    public Bitmap rotateImageIfRequired(Bitmap bitmap, android.content.Context context, Uri uri) throws IOException {
        android.os.ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        java.io.FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        Log.i("info", "fileDescriptor = " + String.valueOf(fileDescriptor));
        Log.i("info", "uri.getPath()  = " + uri.getPath());

        androidx.exifinterface.media.ExifInterface ei = new androidx.exifinterface.media.ExifInterface(uri.getPath());
        int orientation = ei.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);

        parcelFileDescriptor.close();

        android.widget.Toast.makeText(CameraAlignmentActivity.this, "orientation = " + orientation, android.widget.Toast.LENGTH_SHORT).show();

        int amplication = 270;

        switch (orientation) {
            case 0:
                return rotateImage(bitmap, amplication);
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90 + amplication);
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180 + amplication);
            case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270 + amplication);
            default:
                return bitmap;
        }
    }

    /**
     * rotate image
     *
     * @param bitmap
     * @param degree
     * @return
     */
    private Bitmap rotateImage(Bitmap bitmap, int degree) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotatedImg;
    }
//    private void enableCameraFunction() {
//        ActivityResultLauncher takePicturePreview = registerForActivityResult(new TakePicturePreview(), this::onPicture);
//        imageCaptureButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Log.v("log", "imageCaptureButton.");
//                takePicturePreview.launch(new TakePicture());
//            }
//        });
//    }

//    private void showPicture(File imgFile) {
//        if(imgFile.exists()){
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//            this.imageView.setImageBitmap(myBitmap);
//        }
//    }

//    private void onPicture(Bitmap bitmap) {
//        imageView.setImageBitmap(bitmap);
//    }

    class MyLifecycleObserver implements DefaultLifecycleObserver {
        private final ActivityResultRegistry mRegistry;
        private ActivityResultLauncher<String> mGetContent;

        MyLifecycleObserver(@NonNull ActivityResultRegistry registry) {
            mRegistry = registry;
        }

        public void onCreate(@NonNull LifecycleOwner owner) {
            // ...

            mGetContent = mRegistry.register("key", owner, new GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            // Handle the returned Uri
                        }
                    });
        }

        public void selectImage() {
            // Open the activity to select an image
            mGetContent.launch("image/*");
        }

    }
}