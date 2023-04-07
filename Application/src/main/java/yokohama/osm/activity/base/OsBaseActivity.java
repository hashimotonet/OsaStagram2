package yokohama.osm.activity.base;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class OsBaseActivity extends Activity {

    protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                Log.i("IMPORTANT", "(mIcon11 != null) : " + (mIcon11 != null));
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            Log.i("IMPORTANT", "(bmImage != null) : " + (bmImage != null));
            Log.i("IMPORTANT", "(result != null)  : " + (result != null));
            Log.i("IMPORTANT", "DownloadImageTask#onPostExecute() finished.");
        }
    }

}