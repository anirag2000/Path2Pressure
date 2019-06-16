package com.example.anirudh.locationman;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.util.LogWriter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


//private FirebaseFirestore db;
String  m_Text;
    double velocity;
    double latitude=0.0;
    double longitude=0.0;
    Handler handler;
    double ax, ay, az;
    double mx, my, mz;
    double pressure;
    File myFile;
    FileWriter fw;
    double lat1,lat2,long1,long2;
    int count=0;
    double distance1;

    SimpleDateFormat mdformat;
    Calendar calendar;
    Instant start;
    Instant end;
    int poll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //db=FirebaseFirestore.getInstance();
       Intent intent=getIntent();
        poll =Integer.parseInt(intent.getStringExtra("poll"));

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {


            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant


            Button start = findViewById(R.id.button3);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onstartclick();
                }
            });
            Button stop = findViewById(R.id.button2);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "PRESS START TO RECORD DATA", Toast.LENGTH_LONG).show();
                }
            });


        }
        else {
        Toast.makeText(MainActivity.this,"PLEASE GRANT PERMISSSION",Toast.LENGTH_LONG).show();
        }
        return;
    }


    void onstartclick()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the name of the file");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);



// Set up the buttons
        builder.setPositiveButton("START", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();

                Toast.makeText(MainActivity.this,m_Text,Toast.LENGTH_LONG).show();

                String filePath = "/storage/emulated/0/Download/"+m_Text+".csv";
                try {
                    myFile = new File(filePath);
                    fw = new FileWriter(filePath);

                    fw.append("TIME");
                    fw.append(',');

                    fw.append("Ax");
                    fw.append(',');

                    fw.append("Ay");
                    fw.append(',');

                    fw.append("Az");
                    fw.append(',');

                    fw.append("lat");
                    fw.append(',');

                    fw.append("long");
                    fw.append(',');

                    fw.append("mx");
                    fw.append(',');

                    fw.append("my");
                    fw.append(',');

                    fw.append("mz");
                    fw.append(',');
                    fw.append("Pressure");
                    fw.append(',');
                    fw.append('\n');
                    Toast.makeText(MainActivity.this,"STARTED RECORDING DATA",Toast.LENGTH_LONG).show();



                }
                catch (IOException e)
                {

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


        SensorManager sensorManager;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Button button = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fw.close();
                    Toast.makeText(MainActivity.this,"STOPPED,check download folder for csv file",Toast.LENGTH_LONG).show();
                }
                catch (IOException e)
                {

                }
            }
        });
        final int k=poll;

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                getLocation();
                exportTheDB();

                handler.postDelayed(this, k);
            }
        };
        handler.postDelayed(r, k);




        Button start=findViewById(R.id.button3);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"PRESS STOP",Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];


        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mx = event.values[0];
            my = event.values[1];
            mz = event.values[2];


        }

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressure = event.values[0];


        }
        DecimalFormat formatter = new DecimalFormat("#0.00000");
        TextView textView = findViewById(R.id.textView2);
        textView.setText("Ax:" + formatter.format(ax) + "Ay:" + formatter.format(ay) + "Az:" + formatter.format(az) + "\n\n" + "Mx"
                + formatter.format(mx) + "My:" + formatter.format(my) + "Mz" + formatter.format(mz) + "\n\n" +
                "P" + Double.toString(pressure));

    }

    void getLocation() {


        calendar = Calendar.getInstance();
         mdformat = new SimpleDateFormat("HH:mm:ss");

        SmartLocation.with(getApplicationContext()).location().continuous()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {


                        if(location.getLatitude()!=0.0) {
                            if(count==0) {
                                lat1 = location.getLatitude();
                                long1 = location.getLongitude();
                                start = Instant.now();


                            }
                            count=count+1;
                            long2=location.getLongitude();
                            lat2=location.getLatitude();
                           end = Instant.now();


                            if(lat1!=long1|| lat2!=long2)
                            {
                                double dist =distance(lat1,long1,lat2,long2);
                                distance1=distance1+dist;
                                Duration timeElapsed = Duration.between(start, end);

                               double time =((timeElapsed.toMillis())/1000);

                               if(dist!=0.0) {
                                   velocity = dist / time;
                               }
                                start=end;
                                TextView tv=findViewById(R.id.textView3);
                                if(time!=0.0) {
                                    tv.setText("Distance:" + distance1 + "\n" + "Time:" + time + "\n" + "vel" + velocity);


                                }
                                lat1=lat2;
                                long1=long2;


                            }


                        }


                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        TextView tv = findViewById(R.id.textView);
                        tv.setText(Double.toString(latitude) + "," + Double.toString(longitude));


                    }
                });
    }

    private void exportTheDB() {


        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss:SSS");
            String strDate = mdformat.format(calendar.getTime());



            fw.append(strDate);
            fw.append(',');

            fw.append(Double.toString(ax));
            fw.append(',');

            fw.append(Double.toString(ay));
            fw.append(',');

            fw.append(Double.toString(az));
            fw.append(',');

            fw.append(Double.toString(latitude));
            fw.append(',');

            fw.append(Double.toString(longitude));
            fw.append(',');

            fw.append(Double.toString(mx));
            fw.append(',');

            fw.append(Double.toString(my));
            fw.append(',');

            fw.append(Double.toString(mz));
            fw.append(',');
            fw.append(Double.toString(pressure));
            fw.append(',');

           // Toast.makeText(MainActivity.this,"ERITITNT", Toast.LENGTH_LONG).show();

            fw.append('\n');
        } catch (Exception e) {
            //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }


    }
    double distance(double lat1, double lon1, double lat2, double lon2) {
        double p = 0.017453292519943295;    // Math.PI / 180

        double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p))/2;

        return 12742000 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
    }
}