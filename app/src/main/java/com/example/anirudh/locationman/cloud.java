package com.example.anirudh.locationman;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;


import com.opencsv.CSVReader;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import is.arontibo.library.ElasticDownloadView;

public class cloud extends AppCompatActivity {
    Uri uri;
    volatile static String m_Text="";
    volatile int referencecount=0;
    volatile String reference_multiple_pressure="";
    volatile String reference_multiple_latitude="";
    volatile String reference_multiple_longitude="";
    volatile String reference_pressure;
    volatile String reference_latitude;
    volatile String reference_longitude;
    volatile String reference_cell;



    volatile String serverurl;
    volatile int countref = 0;
    volatile static String realtimevalue="\n";
    volatile static int countreference;
    volatile static  int count=0;
    volatile static  int refcount=0;
    ProgressBar progressBar;
    @BindView(R.id.elastic_download_view) ElasticDownloadView mElasticDownloadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        Resources res = getResources();
        showFileChooser();
        ButterKnife.bind(this);



        serverurl=res.getString(R.string.url);
 //progressBar=findViewById(R.id.progressBar2);
//progressBar.setVisibility(View.INVISIBLE);
Button select=findViewById(R.id.selectfile);
Button upload=findViewById(R.id.upload);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the name of the file");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cloud.m_Text=input.getText().toString();
                checkfile();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(cloud.this,Home.class);
                startActivity(intent);
            }
        });
builder.show();

upload.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
       // progressBar.setVisibility(View.VISIBLE);
        addreference();
        //progressBar.setVisibility(View.INVISIBLE);
    }
});


    }


    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    uri = data.getData();
                    //Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path

                    TextView display=findViewById(R.id.textView2);
                    display.setText(uri.getLastPathSegment().substring(5));
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    void addreference() {

        Button upload=findViewById(R.id.upload);
        upload.setVisibility(View.INVISIBLE);



        Toast.makeText(cloud.this, "STARTED", Toast.LENGTH_LONG).show();
        count = 0;



        mElasticDownloadView.startIntro();


        Thread thread = new Thread() {
            @Override
            public void run() {

                // Do your task

                try {

                    CSVReader reader = new CSVReader(new FileReader(uri.getLastPathSegment().substring(5)));
                    String[] nextLine;
                    referencecount=0;
                    TextView display=findViewById(R.id.textView2);
                    display.setText("UPLOAD IN PROGRESS");

                    while ((nextLine = reader.readNext()) != null && count!=2) {

                        // nextLine[] is an array of values from the line
                        reference_pressure=nextLine[4];
                        reference_latitude=nextLine[2];
                        reference_longitude=nextLine[3];
                        reference_cell=nextLine[5];





                            OutputStream os = null;
                            InputStream is = null;
                            HttpURLConnection conn = null;
                            try {
                                //constants
                                URL url = new URL(serverurl + "/postreference");
                                JSONObject jsonObject = new JSONObject();
                                Log.w("warning", reference_multiple_pressure );
                                jsonObject.put("filename", m_Text);
                                jsonObject.put("reference", reference_pressure);
                                jsonObject.put("latitude", reference_latitude);
                                jsonObject.put("longitude", reference_longitude);
                                jsonObject.put("cell", reference_cell);
                                Log.w("lol", reference_multiple_pressure);

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
                                //conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                                //  is.close();

                                //open
                                conn.connect();

                                //setup send
                                os = new BufferedOutputStream(conn.getOutputStream());
                                os.write(message.getBytes());
                                // Toast.makeText(DTWjava.this,message,Toast.LENGTH_LONG).show();
                                //Log.w("MESSAGE", message);
                                //clean up
                                os.flush();

                                //do somehting with response
                                is = conn.getInputStream();
                                StringBuffer sb = new StringBuffer();
                                try {
                                    int chr;
                                    while ((chr = is.read()) != -1) {
                                        sb.append((char) chr);
                                    }
                                    final String reply = sb.toString();


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try
                                            {
                                                BufferedReader bufferedReader = new BufferedReader(new FileReader(uri.getLastPathSegment().substring(5)));
                                                String input;
                                                refcount=0;
                                                while((input = bufferedReader.readLine()) != null)
                                                {
                                                    refcount++;

                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                Log.w("warning", e.toString());
                                            }
                                            Log.w("warning", Integer.toString(refcount));


                                            double per=(Integer.parseInt(reply.trim()))*(100);
                                           double percent=per/(refcount);
                                            Log.w("RESULYQWE", reply+","+Integer.toString(refcount));
                                            Log.w("lol", Double.toString(per)+","+Double.toString(percent));
                                            mElasticDownloadView.setProgress((int)percent);
                                        }
                                    });


                                } finally {
                                    is.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.w("warning", e.toString());

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.w("warning", e.toString());
                            } finally {
                                if (conn != null) {
                                    conn.disconnect();
                                }
                            }

                        }






                    display.setText("UPLOAD COMPLETED SUCCESSFULLY");
                    Intent intent=new Intent(cloud.this,Main2Activity.class);
                    startActivity(intent);
                }
                catch (IOException e)
                {

                }


            }
        };

        thread.start();



    }
    void checkfile() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.w("lol", cloud.m_Text );

                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl+"/checkfile");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("filename", m_Text);


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
