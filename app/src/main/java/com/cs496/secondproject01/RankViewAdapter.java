package com.cs496.secondproject01;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

public class RankViewAdapter extends BaseAdapter {
    public Context context;
    public int resource;
    private JSONArray rankList;
    private Bitmap pic;
    private String rank, name, count;

    public RankViewAdapter(Context context, int resource, JSONArray rankList) {
        this.context = context;
        this.resource = resource;
        this.rankList = rankList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rank_item, parent, false);
        }

        // GET VIEWS
        TextView rankNoView = (TextView) convertView.findViewById(R.id.textView_rank_no);
        ImageView rankThumbnailView = (ImageView) convertView.findViewById(R.id.imageView_rank_thumnail);
        TextView rankNameView = (TextView) convertView.findViewById(R.id.textView_rank_name);
        TextView rankCountView = (TextView) convertView.findViewById(R.id.textView_rank_count);

        try {
            // GET VALUES
            JSONObject jObject = rankList.getJSONObject(position);
            if (jObject.has("rank")) {
                rank = jObject.getString("rank");
            }
            if (jObject.has("pic")) {
                try {
                    pic = new loadImage().execute(jObject.getString("pic")).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                rankThumbnailView.setImageResource(R.drawable.default_profile);
            }
            if (jObject.has("name")) {
                name = jObject.getString("name");
            }
            if (jObject.has("count")) {
                count = jObject.getString("count");
            }

            // SET VIEWS
            rankNoView.setText(rank);
            rankNoView.setTypeface(App.myFont);
            rankThumbnailView.setImageBitmap(pic);
            rankNameView.setText(name);
            rankNameView.setTypeface(App.myFont);
            rankCountView.setText(count);
            rankCountView.setTypeface(App.myFont);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        if (rankList == null)
            return 0;
        else
            return rankList.length();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    private class loadImage extends AsyncTask<String, Void, Bitmap> {
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