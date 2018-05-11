package com.tivo.hackathon.listview;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

public class ListViewActivity extends AppCompatActivity {

    JSONObject finalResponse = null;
    String[] mobileArray;
    String url = MainActivity.masterUrl + "/video?path=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listlayout);

        Intent intent = getIntent();
        String response = intent.getStringExtra("response");
        try {
        finalResponse = new JSONObject(response);
        System.out.println(finalResponse.toString());
        } catch (JSONException e) {
        e.printStackTrace();
        }

        List<String> list = new ArrayList<String>();
        try {
            String path = finalResponse.getString("path");
            JSONArray array = finalResponse.getJSONArray("media");
            mobileArray = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String name = jsonObject.getString("name");
                mobileArray[i] = name;
                JSONObject metadata = jsonObject.getJSONObject("metaData");
                System.out.println("Video name: "+ name);
                System.out.println("Video metadata: "+ metadata);
                System.out.println("Video path: "+ path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.listv, mobileArray);


        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.listview_header,listView,false);
        TextView tv = (TextView) header.findViewById(R.id.tv);
        try {
            tv.setText(finalResponse.getString("path"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        header.invalidate();  // for refreshment
        listView.addHeaderView(header);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //String value=adapter.getItem(position);
                System.out.println("Position in ListViewActivity:" + position);
                String item = mobileArray[position-1];
                try {
                JSONArray array = finalResponse.getJSONArray("media");
                JSONObject jsonObject =  array.getJSONObject(position-1);
                    JSONObject metadata = jsonObject.getJSONObject("metaData");
                    JSONObject skipIntro = metadata.getJSONObject("skipIntro");
                    String startTime = skipIntro.getString("startTime");
                    String endTime = skipIntro.getString("endTime");
                    Intent myIntent = new Intent(ListViewActivity.this,
                            VideoViewActivity.class);
                    myIntent.putExtra("url",url);
                    myIntent.putExtra("path",finalResponse.getString("path"));
                    myIntent.putExtra("name",jsonObject.getString("name"));
                    myIntent.putExtra("startTime",startTime);
                    myIntent.putExtra("endTime",endTime);
                    startActivity(myIntent);
                } catch (JSONException e) {
                e.printStackTrace();
                }
            }
        });


    }
}
