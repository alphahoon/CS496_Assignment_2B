package com.cs496.secondproject01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by q on 2017-01-03.
 */

public class PhotoViewAdapter extends RecyclerView.Adapter<PhotoViewAdapter.ViewHolder> {
    private JSONArray photo_list;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public CardView card;

        public ViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.photo);
            card = (CardView) view.findViewById(R.id.cardview);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PhotoViewAdapter(Context activity, JSONArray list) {
        this.context = activity;
        this.photo_list = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PhotoViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        try {
            final String url = photo_list.getString(position);
            Bitmap img = new loadPhoto().execute(url).get();
            holder.mImageView.setImageBitmap(img);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent photoIntent = new Intent(context, FullScreenPop.class);
                    photoIntent.putExtra("url", url);
                    context.startActivity(photoIntent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (photo_list == null) {return 0;}
        return photo_list.length();
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
    //============================================================================================//


    // AsyncTask to send JSON to our MongoDB
    private class sendJSON extends AsyncTask<Void, Void, JSONObject> {
        String urlstr;
        String data;
        String contentType;

        public sendJSON(String url, String data, String contentType) {
            this.urlstr = url;
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HttpURLConnection conn;
            OutputStream os;
            InputStream is;
            BufferedReader reader;
            JSONObject json = new JSONObject();

            try {
                URL url = new URL(urlstr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);

                // If sending to our DB
                if (urlstr.contains("52.78.200.87")) {
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", contentType);
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));

                    os = new BufferedOutputStream(conn.getOutputStream());
                    os.write(data.getBytes());
                    os.flush();
                    os.close();
                }

                int statusCode = conn.getResponseCode();
                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append("\n");
                }
                reader.close();
                App.response = response.toString();
                json = new JSONObject(response.toString());

                conn.disconnect();


            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                return null;
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }

            return json;
        }

    }
    //============================================================================================//
}




