package com.example.anirudh.locationman;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main2Activity extends AppCompatActivity {


    volatile String serverurl;
   volatile String [] filenames;
    volatile String [] latlong;
    volatile Spinner s;
    volatile  String name;
    Button submit;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main2);
        listView=findViewById(R.id.list_view);

        Resources res = getResources();
        serverurl=res.getString(R.string.url);
        submit=findViewById(R.id.button6);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getlatlong();
            }
        });

        listfile();


    }
    int listfile() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {


                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl+"/getfiles");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("filename", "dummy");

                        Log.w("lol", " " + "lol");

                        String message = jsonObject.toString();

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000 /*milliseconds*/);
                        conn.setConnectTimeout(15000 /* milliseconds */);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setFixedLengthStreamingMode(message.getBytes().length);

                        //make some HTTP header nicety
                        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                        //open
                        conn.connect();

                        //setup send
                        os = new BufferedOutputStream(conn.getOutputStream());
                        os.write(message.getBytes());
                        // Toast.makeText(DTWjava.this,message,Toast.LENGTH_LONG).show();
                        //Log.w("MESSAGE", message);
                        //clean up
                        os.flush();
                        is = conn.getInputStream();
                        StringBuffer sb = new StringBuffer();
                        try {
                            int chr;
                            while ((chr = is.read()) != -1) {
                                sb.append((char) chr);
                            }
                            String reply = sb.toString();
                            filenames=reply.split(",");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    listView=findViewById(R.id.list_view);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_list_item_single_choice,filenames);



                                    listView.setAdapter(adapter);
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            name=filenames[position];
                                        }
                                    });
                                    listView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            return false;
                                        }
                                    });

                                }
                            });



                        } finally {
                            is.close();
                        }

                        //do somehting with response

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("warningfile", " " + e.toString());
                    } finally {

                        conn.disconnect();
                    }
                } catch (Exception e1) {
                    Log.w("warningfile", " " + e1.toString());

                }


            }
        }).start();
        return 0;
    }



    void getlatlong() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {


                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl+"/getlatlonglist");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("filename", name);

                        Log.w("lol", name);

                        String message = jsonObject.toString();

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000 /*milliseconds*/);
                        conn.setConnectTimeout(15000 /* milliseconds */);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setFixedLengthStreamingMode(message.getBytes().length);

                        //make some HTTP header nicety
                        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                        //open
                        conn.connect();

                        //setup send
                        os = new BufferedOutputStream(conn.getOutputStream());
                        os.write(message.getBytes());
                        // Toast.makeText(DTWjava.this,message,Toast.LENGTH_LONG).show();
                        //Log.w("MESSAGE", message);
                        //clean up
                        os.flush();
                        is = conn.getInputStream();
                        StringBuffer sb = new StringBuffer();
                        try {
                            int chr;
                            while ((chr = is.read()) != -1) {
                                sb.append((char) chr);
                            }
                            String reply = sb.toString();
                            Log.w("lol", reply );
                          Intent intent=new Intent(Main2Activity.this,RealtimeMap.class);
                          intent.putExtra("latlong",reply);
                            intent.putExtra("filename",name);
                          startActivity(intent);



                        } finally {
                            is.close();
                        }

                        //do somehting with response

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("warningfile", " " + e.toString());
                    } finally {

                        conn.disconnect();
                    }
                } catch (Exception e1) {
                    Log.w("warningfile", " " + e1.toString());

                }


            }
        }).start();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cloud) {
           Intent intent=new Intent(Main2Activity.this,cloud.class);
           startActivity(intent);
        }
        else if (id == R.id.add) {
            Intent intent=new Intent(Main2Activity.this,MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }









}
