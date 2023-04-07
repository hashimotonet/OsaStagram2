package yokohama.osm.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import yokohama.osm.R;
import yokohama.osm.activity.base.OsBaseActivity;

public class SpreadImageActivity extends OsBaseActivity {

    public SpreadImageActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.spread_image);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Uri uri = getImageURI(url);
        ImageView ivImage = findViewById(R.id.spreadImageView);

        DownloadImageTask imageTask = new DownloadImageTask(ivImage);
        imageTask.execute(url);

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
              callFinish();
            }
        });
    }

    public void callFinish() {
        super.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.spread_image);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Uri uri = getImageURI(url);
        ImageView ivImage = findViewById(R.id.spreadImageView);

        DownloadImageTask imageTask = new DownloadImageTask(ivImage);
        imageTask.execute(url);
    }

    private Uri getImageURI(String url) {
        Uri uri = Uri.parse(url);
        return uri;
    }

}
