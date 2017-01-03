package com.cs496.secondproject01;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by q on 2017-01-03.
 */

public class AlbumView extends Activity {
    JSONObject album;
    private RecyclerView mRecyclerView;
    private PhotoViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Integer> bgs =
            new ArrayList<Integer>(Arrays.asList(R.drawable.pic0, R.drawable.pic1, R.drawable.pic2));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_view);

        LinearLayout bg = (LinearLayout) findViewById(R.id.album_view);
        Random rand = new Random();
        bg.setBackgroundResource(bgs.get(rand.nextInt(3)));

        //Get album to View
        Intent intent = getIntent();
        String albumstr = intent.getExtras().getString("album");
        try {
            album = new JSONObject(albumstr);
            TextView a_name = (TextView) findViewById(R.id.a_name);
            TextView a_with = (TextView) findViewById(R.id.a_with);
            TextView a_photos = (TextView) findViewById(R.id.a_photos);
            TextView a_activity = (TextView) findViewById(R.id.a_activity);
            TextView a_date = (TextView) findViewById(R.id.a_date);
            TextView b_activity = (TextView) findViewById(R.id.b_activity);
            TextView b_with = (TextView) findViewById(R.id.b_with);
            a_name.setTypeface(App.myFont);
            a_with.setTypeface(App.myFont);
            a_photos.setTypeface(App.myFont);
            a_activity.setTypeface(App.myFont);
            a_date.setTypeface(App.myFont);
            b_activity.setTypeface(App.myFont);
            b_with.setTypeface(App.myFont);

            String friends = "";
            String activities = "";
            JSONArray friend_list = album.getJSONArray("friend_list");
            for (int i = 0; i < friend_list.length(); i++) {
                friends += friend_list.getJSONObject(i).getString("name");
                if (i < friend_list.length() - 1)
                    friends += ",";
            }
            JSONObject act_list = album.getJSONObject("activity");
            if (act_list.getString("work").contains("1")) { activities += "work ";}
            if (act_list.getString("study").contains("1")) { activities += "study ";}
            if (act_list.getString("food").contains("1")) { activities += "food ";}
            if (act_list.getString("cafe").contains("1")) { activities += "cafe ";}
            if (act_list.getString("sports").contains("1")) { activities += "sports ";}
            if (act_list.getString("movie").contains("1")) { activities += "movie ";}
            if (act_list.getString("game").contains("1")) { activities += "game ";}
            if (act_list.getString("travel").contains("1")) { activities += "travel ";}
            activities = activities.trim().replace(" ",",");
            a_name.setText(album.getString("album_name"));
            b_with.setText(friends);
            b_activity.setText(activities);
            a_date.setText(album.getString("date"));

            //Setup recycler view
            mRecyclerView = (RecyclerView) findViewById(R.id.album_recycler);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.scrollToPosition(0);
            mAdapter = new PhotoViewAdapter(this,album.getJSONArray("img_url_list"));
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        } catch (JSONException e) {
            e.printStackTrace();
        }







    }

}
