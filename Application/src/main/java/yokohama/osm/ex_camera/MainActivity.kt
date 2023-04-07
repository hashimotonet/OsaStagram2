package yokohama.osm.ex_camera

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import yokohama.osm.R

class MainActivity : AppCompatActivity(R.layout.activity_camera_alignment) {
    private val imageView: ImageView by lazy { findViewById<ImageView>(R.id.image_view) }
    private val imageCaptureButton: View by lazy { findViewById<View>(R.id.camera_button) }
    private val getContentButton: View by lazy { findViewById<View>(R.id.get_content_button) }
    private val bothButton: View by lazy { findViewById<View>(R.id.both_button) }

    private val hasCameraFeature get() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableNormalFunction()
        when {
            hasCameraFeature -> enableCameraFunction()
            else             -> disableCameraFunction()
        }

        setContentView(R.layout.activity_camera_alignment)

    }

    private fun enableNormalFunction() {
        // 実機に保存されている content を mimeType 指定で取得する。
        val getContent = registerForActivityResult(ActivityResultContracts.GetContent(), this::onContent)
        getContentButton.setOnClickListener { getContent.launch("image/*") }

        // GetContent と TakePicturePreview と同等の機能を chooser で選択可能にした版。
        val both = registerForActivityResult(GetContentOrTakePicturePreviewChooserChooser(this, "画像を選択"), this::onPicture)
        bothButton.setOnClickListener { both.launch(null) }
    }

    private fun enableCameraFunction() {
        // 小さな画像をカメラから取得する。実機で試したところ 260*195 の画像だった。
        val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview(), this::onPicture)
        imageCaptureButton.setOnClickListener { takePicturePreview.launch(null) }
    }

    private fun disableCameraFunction() {
        imageCaptureButton.isEnabled = false
    }

    private fun onContent(uri: Uri?) {
        imageView.setImageURI(uri ?: return)
    }

    private fun onPicture(bitmap: Bitmap?) {
        imageView.setImageBitmap(bitmap ?: return)
    }
}
