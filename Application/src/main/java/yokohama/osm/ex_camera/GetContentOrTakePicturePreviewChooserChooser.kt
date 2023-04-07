package yokohama.osm.ex_camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class GetContentOrTakePicturePreviewChooserChooser(private val context: Context, private val chooserTitle: String) : ActivityResultContract<Void?, Bitmap>() {
    private val getContentIntent get() = ActivityResultContracts.GetContent().createIntent(context, "image/*")
    private val takePicturePreviewIntent get() = ActivityResultContracts.TakePicturePreview().createIntent(context, null)
    private val hasCameraFeature get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent.createChooser(getContentIntent, chooserTitle).apply {
            if (hasCameraFeature) {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePicturePreviewIntent))
            }
        }
    }

    override fun getSynchronousResult(context: Context, input: Void?): SynchronousResult<Bitmap>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap {
        if (intent == null || resultCode != Activity.RESULT_OK) {
            throw java.lang.Exception();
        }

        val bitmap: Bitmap? = intent.getParcelableExtra<Bitmap>("data")
        val data: Uri? = intent.data

        return when {
            bitmap != null -> bitmap
            data != null   -> MediaStore.Images.Media.getBitmap(context.contentResolver, data)
            else           -> throw java.lang.Exception();
        }
    }
}