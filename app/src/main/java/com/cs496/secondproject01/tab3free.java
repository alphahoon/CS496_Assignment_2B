package com.cs496.secondproject01;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class tab3free extends Fragment {
    public Button btnRankActivity;
    public Button btnRankFriends;
    public ListView listViewRank;
    public JSONArray rankList;

    public int mode;
    public static final int MODE_ACTIVITY = 0;
    public static final int MODE_FRIEND = 1;

    public static final String DEFAULT_IMG_URL = "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png";
    public static final String[] ACTIVITY_LIST = {
            "work", "study", "food", "cafe", "sports", "movie", "game", "travel"};
    public static final String[] ACTIVITY_URLS = {
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // work
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // study
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // food
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // cafe
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // sports
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // movie
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png",  // game
            "https://www.thesocialmediahat.com/sites/default/files/default_profile_4.png"   // travel
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3free, container, false);
        btnRankActivity = (Button) view.findViewById(R.id.btn_rank_activity);
        btnRankFriends = (Button) view.findViewById(R.id.btn_rank_friends);
        listViewRank = (ListView) view.findViewById(R.id.listView_rank_list);

        btnRankActivity.setOnClickListener(btnRankActivityOnClicked);
        btnRankFriends.setOnClickListener(btnRankFriendsOnClicked);

        mode = MODE_ACTIVITY;
        rankList = getRankList(mode);
        displayList(rankList);

        return view;
    }

    View.OnClickListener btnRankActivityOnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mode = MODE_ACTIVITY;
            rankList = getRankList(mode);
            displayList(rankList);
        }
    };

    View.OnClickListener btnRankFriendsOnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mode = MODE_FRIEND;
            rankList = getRankList(mode);
            displayList(rankList);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        rankList = getRankList(mode);
        displayList(rankList);
    }

    public void displayList(JSONArray rankList) {
        RankViewAdapter adapter = new RankViewAdapter(
                getActivity(),
                R.layout.rank_item,
                rankList
        );
        listViewRank.setAdapter(adapter);
    }

    public JSONArray getRankList (int mode) {
        try {
            // GENERATE REQUEST
            JSONObject req = new JSONObject();
            req.put("type", "GET_STATS");
            req.put("user_id", App.db_user_id);

            // GET RESPONSE
            JSONObject res = new sendJSON("http://52.78.200.87:3000", req.toString(), "application/json").execute().get();
            if (res == null) {
                Log.e("null response", "res = null");
                return null;
            } else if (res.get("result").equals("failed")) {
                Log.e("GET_STATS failed", res.toString());
                return null;
            }

            // PARSE RESPONSE TO GET RANKLIST
            JSONObject tmp;
            JSONArray rankList = new JSONArray();
            switch (mode)
            {
                // FOR ACTIVITY RANK TAB
                case MODE_ACTIVITY:
                    // GET ACTIVITY FIELD AND CHECK VALIDITY
                    JSONObject activity = res.getJSONObject("activity");
                    for (int i = 0; i < ACTIVITY_LIST.length; i++) {
                        if (!activity.has(ACTIVITY_LIST[i])) {
                            Log.e("activity incompleted", activity.toString());
                            return null;
                        }
                    }

                    // ADD TO RANK LIST
                    for (int i = 0; i < ACTIVITY_LIST.length; i++) {
                        tmp = new JSONObject();
                        if (activity.has(ACTIVITY_LIST[i])) {
                            tmp.put("pic", getActivityImageURL(ACTIVITY_LIST[i]));
                            tmp.put("name", ACTIVITY_LIST[i]);
                            tmp.put("count", activity.get(ACTIVITY_LIST[i]));
                        }
                        rankList.put(tmp);
                    }
                    break;

                // FOR FRIEND RANK TAB
                case MODE_FRIEND:
                    // GET FRIENDS FIELD AND CHECK VALIDITY
                    JSONArray friends = res.getJSONArray("friends");
                    JSONObject friend;
                    for (int i = 0; i < friends.length(); i++) {
                        friend = friends.getJSONObject(i);
                        if (!friend.has("name") || !friend.has("count")) {
                            Log.e("friends incompleted", friend.toString());
                            return null;
                        }
                    }

                    for (int i = 0; i < friends.length(); i++) {
                        tmp = new JSONObject();
                        friend = friends.getJSONObject(i);
                        if (friend.has("pic")) {
                            tmp.put("pic", friend.get("pic"));
                        } else {
                            tmp.put("pic", DEFAULT_IMG_URL);
                        }
                        tmp.put("name", friend.get("name"));
                        tmp.put("count", friend.get("count"));
                    }
                    break;
            }

            // SORT JSON ARRAY BASED ON THE NUMBER OF COUNT
            JSONArray sortedRankList = new JSONArray();
            List<JSONObject> jsonValues = new ArrayList<>();
            for (int i = 0; i < rankList.length(); i++) {
                jsonValues.add(rankList.getJSONObject(i));
            }
            Collections.sort( jsonValues, new Comparator<JSONObject>() {
                //You can change "Name" with "ID" if you want to sort by ID
                private static final String KEY_NAME = "count";

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();
                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });
            for (int i = 0; i < rankList.length(); i++)
                sortedRankList.put(jsonValues.get(i));

            // PUT RANK FIELD WITH VALUE START FROM 1
            for (int i = 0; i < sortedRankList.length(); i++) {
                tmp = sortedRankList.getJSONObject(i);
                tmp.put("rank", String.valueOf(i + 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.v("rankList", rankList.toString());
        return rankList;
    }

    public String getActivityImageURL(String activityName) {
        String url = DEFAULT_IMG_URL;
        for (int i = 0; i < ACTIVITY_LIST.length; i++) {
            if (activityName.equals(ACTIVITY_LIST[i]))
                url = ACTIVITY_URLS[i];
        }
        return url;
    }

    // AsyncTask to communicate with Facebook or our MongoDB
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
            JSONObject json;

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
}