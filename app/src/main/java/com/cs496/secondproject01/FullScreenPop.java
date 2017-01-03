package com.cs496.secondproject01;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by q on 2017-01-04.
 */

public class FullScreenPop extends Activity {
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen);
        Intent intent = getIntent();
        url = intent.getExtras().getString("url");

        try {
            ImageView img = (ImageView) findViewById(R.id.img);
            Bitmap bitmap = new loadPhoto().execute(url).get();
            img.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class loadPhoto extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public loadPhoto () { }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

    }


}
