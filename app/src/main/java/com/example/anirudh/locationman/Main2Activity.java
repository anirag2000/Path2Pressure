package com.example.anirudh.locationman;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main2Activity extends AppCompatActivity {
    volatile String serverurl="https://path2pressure-244816.appspot.com";
   volatile String [] filenames;
    volatile String [] latlong;
    volatile Spinner s;
    volatile  String name;
    Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        submit=findViewById(R.id.button6);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getlatlong();
            }
        });
        Spinner s = (Spinner) findViewById(R.id.spinner);
        listfile();

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                name=filenames[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
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
                                    Spinner s = findViewById(R.id.spinner);


                                    ArrayAdapter<String> adp1 = new ArrayAdapter<>(Main2Activity.this,
                                            android.R.layout.simple_list_item_1, filenames);
                                    adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    s.setAdapter(adp1);
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
                            Log.w("lol", reply );
                          Intent intent=new Intent(Main2Activity.this,DTWjava.class);
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









}
