package com.tivo.hackathon.listview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoViewActivity extends AppCompatActivity {
    VideoView vidView;
    Button mPlayButton, downloadButton;
    int startTime, endTime;
    String vidAddress, path, name, url;
    File apkStorage;

    String urlDownload = MainActivity.masterUrl + "/getOffline?path=";//192.168.43.222:9000
    File myExternalFile;
     String filepath = "MyFileStorage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        // "http://10.177.64.205:9000/video?path=/Bones/BonesS6E5.mp4";
        path = intent.getStringExtra("path");
        name = intent.getStringExtra("name");
        startTime = Integer.parseInt(intent.getStringExtra("startTime"))*1000;
        endTime = Integer.parseInt(intent.getStringExtra("endTime"))*1000;
        vidAddress = url + path + "/" + name;

        vidView = (VideoView)findViewById(R.id.myVideo);
        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);

        Uri vidUri;
        if(isDownloaded()) {
            System.out.println("Video available offline. Playing from there");
            vidUri =Uri.parse(Environment.getExternalStorageDirectory().toString() + path + "/" + name);
        } else {
            System.out.println("Video taking from online streaming");
            vidUri =Uri.parse(vidAddress);
        }
        vidView.setVideoURI(vidUri);

        mPlayButton = (Button) findViewById(R.id.skip);
        downloadButton = (Button) findViewById(R.id.download);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly() || isDownloaded()) {
            downloadButton.setEnabled(false);
            downloadButton.setText("Available Offline");
            downloadButton.invalidate();
        }

        mPlayButton.setVisibility(View.GONE);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(1000);
                        int currentPosition = vidView.getCurrentPosition();
                        if (currentPosition >= startTime && currentPosition<=endTime) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayButton.setVisibility(View.VISIBLE);
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayButton.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }
    public void skip(View view){
        vidView.seekTo(endTime);
    }

    public void download(View view) {
        new DownloadingTask().execute();
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private boolean isDownloaded() {
            File outputFile = getDownloadFile();
            if (outputFile.exists()) {
                return true;
            }
        return false;
    }

    private File getDownloadFile() {

            File apkStorage = new File(
                    Environment.getExternalStorageDirectory().toString() + path);
            if (!apkStorage.exists()) {
                apkStorage.mkdir();
                System.out.println("Directory Created.");
            }
            File outputFile = new File(apkStorage, name);
            return outputFile;
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

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
        protected Void doInBackground(Void... arg0) {
            //vidView.get
            try {
                if (ContextCompat.checkSelfPermission(VideoViewActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(VideoViewActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    } else {
                        ActivityCompat.requestPermissions(VideoViewActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                        );
                    }
                }

                String downloadUrl = urlDownload + path + "/" + name;
                URL url = new URL(downloadUrl);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                File apkStorage = new File(
                        Environment.getExternalStorageDirectory().toString() + path);//"/" + "tivo" + path
                System.out.println("Directory path: "+ apkStorage.getAbsolutePath()); //Environment.getExternalStorageDirectory()
                if (!apkStorage.exists()) {
                    apkStorage.mkdir();
                    System.out.println("Directory Created.");
                }
                outputFile = new File(apkStorage, name);
                if (!outputFile.exists()) {
                    int permissionCheck = ContextCompat.checkSelfPermission(VideoViewActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    System.out.println("Permission: " + permissionCheck + "  " + PackageManager.PERMISSION_GRANTED);
                    //if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                        outputFile.createNewFile();
                    System.out.println("File Created");
                }
                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {

                    fos.write(buffer, 0, len1);//Write new file
                    System.out.println("Download done");
                }
                System.out.println("Download done"+outputFile.getAbsolutePath());
                fos.close();
                is.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Download done"+outputFile.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();
                        downloadButton.setEnabled(false);
                        downloadButton.setText("Available Offline");
                        downloadButton.invalidate();
                    }
                });

                //dd();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }




}
