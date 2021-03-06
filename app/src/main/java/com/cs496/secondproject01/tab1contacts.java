package com.cs496.secondproject01;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Bundle;
import com.facebook.FacebookSdk;

import com.cs496.secondproject01.dummy.DummyContent;
import com.cs496.secondproject01.dummy.DummyContent.DummyItem;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static android.R.attr.name;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.cs496.secondproject01.R.id.container;


public class tab1contacts extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab1contacts, container, false);

        //LoginManager.getInstance().logOut();
/*
        FloatingActionButton fb = (FloatingActionButton) view.findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent popupIntent = new Intent(getActivity(), LoginPop.class);
                startActivity(popupIntent);
            }
        });
*/

        if (!isLoggedIn()) {
        //if (true) {
            //LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
            //loginButton.setVisibility(View.INVISIBLE);
            //App.firstAccess = false;
            Intent popupIntent = new Intent(getActivity(), LoginPop.class);
            startActivity(popupIntent);
        } else {
            SharedPreferences settings = getActivity().getSharedPreferences("DB",MODE_PRIVATE);
            App.db_user_id = settings.getString("db_id", "no info");
            if (App.friends == null) {
                try {
                    JSONObject obj = new JSONObject();
                    JSONObject result = new JSONObject();
                    obj = new JSONObject();
                    obj.put("type", "GET_CONTACTS");
                    obj.put("user_id", App.db_user_id);
                    result = new sendJSON("http://52.78.200.87:3000",
                            obj.toString(), "application/json").execute().get();

                    //Update Information in App variables
                    App.friends = result.getJSONArray("contacts");
                    for (int i = 0; i < App.friends.length(); i++) {
                        JSONObject f = App.friends.getJSONObject(i);
                        App.names[i] = f.getString("name");
                        //Hashmap
                        App.friend_map.put(f.getString("name"),
                                f.getString("friend_id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            Log.v("db_user_id", App.db_user_id);
        }


        return view;
    }

    public void onResume() {
        super.onResume();
        JSONArray contactsjson = new JSONArray();
        //친구 가져오기
        try {
            if (App.friends == null) {
                contactsjson.put(new JSONObject("{\"name\" : \"Me\"}"));
            } else {
                contactsjson = App.friends;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Handle ListView
        ListView conList = (ListView) getView().findViewById(R.id.contact_list);
        ContactViewAdapter adapter = new ContactViewAdapter(getActivity(),
                R.layout.contact_item, contactsjson);
        conList.setAdapter(adapter);
    }

    JSONArray JSONgetContacts (InputStream raw) {
        String json = null;
        try {
            int size = raw.available();
            byte[] buffer = new byte[size];
            raw.read(buffer);
            raw.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            JSONArray contactsjson = new JSONArray(json);
            return contactsjson;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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



    //public void onResume() {
    //    super.onResume();
    //}

    public boolean isLoggedIn() {
        return getActivity().getSharedPreferences("DB",Context.MODE_PRIVATE).getString("db_id", "") != "";
        //AccessToken accessToken = AccessToken.getCurrentAccessToken();
        //return accessToken != null;
    }




}



