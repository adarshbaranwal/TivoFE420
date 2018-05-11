package com.tivo.hackathon.listview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button button;

    public static String masterUrl = "http://192.168.43.222:9000"; // 10.177.64.205 //192.168.43.222

    String url;

    String path = "";

    String[] seriesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        seriesArray = new String[2];//{"Office","Bones"};
        seriesArray[0] = "Office";
        seriesArray[1] = "Bones";
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.listv, seriesArray);
        ListView listView = (ListView) findViewById(R.id.series_list);
        listView.setAdapter(adapter);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.listview_header,listView,false);
        TextView tv = (TextView) header.findViewById(R.id.tv);
        tv.setText("Series");
        header.invalidate();  // for refreshment
        listView.addHeaderView(header);
        // Locate the button in activity_main.xml

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                System.out.println("Position in MainActivity:" + position);
                path = seriesArray[position-1];
                url = masterUrl+"/list?path="+"/"+path;

                if(isDownloaded()) {
                    System.out.println("metadata downloaded");
                    File sdcard = new File(Environment.getExternalStorageDirectory().toString() + "/" +path);
                    File file = new File(sdcard,path);
                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                    }

                    Intent myIntent = new Intent(MainActivity.this,
                            ListViewActivity.class);
                    System.out.println("response.toString()");
                    myIntent.putExtra("response", text.toString());
                    startActivity(myIntent);
                } else {

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Start NewActivity.class
                            Intent myIntent = new Intent(MainActivity.this,
                                    ListViewActivity.class);
                            new DownloadingTask().execute(response.toString());
                            System.out.println("response.toString()");
                            myIntent.putExtra("response", response.toString());
                            startActivity(myIntent);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });

                    Volley.newRequestQueue(MainActivity.this).add(jsonRequest);
                }

            }
        });



        /*button = (Button) findViewById(R.id.MyButton);

        // Capture button clicks
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                url = masterUrl+"/list?path="+"/"+path;

                if(isDownloaded()) {
                    System.out.println("metadata downloaded");
                    File sdcard = new File(Environment.getExternalStorageDirectory().toString() + "/" +path);
                    File file = new File(sdcard,path);
                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                    }

                    Intent myIntent = new Intent(MainActivity.this,
                            ListViewActivity.class);
                    System.out.println("response.toString()");
                    myIntent.putExtra("response", text.toString());
                    startActivity(myIntent);
                } else {

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Start NewActivity.class
                            Intent myIntent = new Intent(MainActivity.this,
                                    ListViewActivity.class);
                            new DownloadingTask().execute(response.toString());
                            System.out.println("response.toString()");
                            myIntent.putExtra("response", response.toString());
                            startActivity(myIntent);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });

                    Volley.newRequestQueue(MainActivity.this).add(jsonRequest);
                }

            }
        });*/
    }

    private boolean isDownloaded() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                );
            }
        }
        File outputFile = getDownloadFile();
        if (outputFile.exists()) {
            return true;
        }
        return false;
    }

    private File getDownloadFile() {

        File apkStorage = new File(
                Environment.getExternalStorageDirectory().toString() + "/" +path);
        if (!apkStorage.exists()) {
            apkStorage.mkdir();
            System.out.println("Directory Created. apkStorage: "+ apkStorage);
        }
        File outputFile = new File(apkStorage, path);
        return outputFile;
    }

    private class DownloadingTask extends AsyncTask<String, Void, Void> {

        File apkStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(String... data) {
            try {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                        );
                    }
                }
                URL urls = new URL(url);
                HttpURLConnection c = (HttpURLConnection) urls.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                File apkStorage = new File(
                        Environment.getExternalStorageDirectory().toString() + "/" + path);//"/" + "tivo" + path
                System.out.println("Directory path: "+ apkStorage.getAbsolutePath()); //Environment.getExternalStorageDirectory()
                if (!apkStorage.exists()) {
                    apkStorage.mkdir();
                    System.out.println("Directory Created.");
                }
                outputFile = new File(apkStorage, path);
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    System.out.println("File Created");
                }
                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location
                fos.write(data[0].getBytes());
                /*InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {

                    fos.write(buffer, 0, len1);//Write new file
                    System.out.println("Download done");
                }
                System.out.println("Download done"+outputFile.getAbsolutePath());

                is.close();*/
                fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

                return null;
        }
    }
}
