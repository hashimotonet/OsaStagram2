package yokohama.osm.camera;

import static androidx.activity.result.contract.ActivityResultContracts.*;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import yokohama.osm.R;

public class CameraAlignmentActivity extends AppCompatActivity {

    private MyLifecycleObserver mObserver;

    private ImageView imageView;
    private View imageCaptureButton;
    private View getContentButton;
    private View bothButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private final int REQUEST_TAKE_PHOTO = 1;

    private String currentPhotoPath;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
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
        bothButton = (View)findViewById(R.id.both_button);

        enableNormalFunction();
        CheckPermission(CameraAlignmentActivity.this,
                Manifest.permission.CAMERA,
                PackageManager.PERMISSION_GRANTED);
        mObserver = new MyLifecycleObserver(CameraAlignmentActivity.this.getActivityResultRegistry());
        getLifecycle().addObserver(mObserver);
    }

    private void enableNormalFunction() {
        if (null != getContentButton) {
            //permissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
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

    private void enableCameraFunction() {
        // 小さな画像をカメラから取得する。実機で試したところ 260*195 の画像だった。
        ActivityResultLauncher takePicturePreview = registerForActivityResult(new TakePicturePreview(), this::onPicture);
        imageCaptureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.v("log", "imageCaptureButton.");
                takePicturePreview.launch(new TakePicture());
            }
        });
    }

    private void onPicture(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

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