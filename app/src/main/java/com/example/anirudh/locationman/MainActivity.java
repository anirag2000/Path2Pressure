package com.example.anirudh.locationman;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.OrientationFusedKalman;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationFusion;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.sensor.acceleration.KalmanLinearAccelerationSensor;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements FSensor,SensorEventListener {


    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private boolean hasRotation = false;
    private boolean hasMagnetic = false;

    private float[] magnetic = new float[3];
    private float[] rawAcceleration = new float[3];
    private float[] rotation = new float[3];
    private float[] acceleration = new float[3];
    private float[] output = new float[4];

    private LinearAcceleration linearAccelerationFilterKalman;
    private OrientationFusedKalman orientationFusionKalman;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    // private  final String tag = GaugeAcceleration.class.getSimpleName();

    // holds the cached static part
    private Bitmap background;

    private Paint backgroundPaint;
    private Paint pointPaint;
    private Paint rimPaint;
    private Paint rimShadowPaint;

    private RectF faceRect;
    private RectF rimRect;
    private RectF rimOuterRect;
    private RectF innerRim;
    private RectF innerFace;
    private RectF innerMostDot;

    private float x;
    private float y;

    private float scaleX;
    private float scaleY;

    private int color = Color.parseColor("#2196F3");
    static  String m_Text;
    double velocity;
    double latitude = 0.0;
    double longitude = 0.0;
    Handler handler;
    char path=97;
    double ax, ay, az;
    double mx = 0.0, my = 0.0, mz = 0.0;
    double gx = 0.0, gy = 0.0, gz = 0.0;
    double pressure;
    File myFile;
    FileWriter fw;
    double lat1, lat2, long1, long2;
    int countloc = 0;
    double distance1;

    SimpleDateFormat mdformat;
    Calendar calendar;
    Instant start;
    Instant end;
    int poll;
    GaugeAcceleration gaugeAcceleration;
    double Ax,Ay,Az;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         gaugeAcceleration=new GaugeAcceleration(getApplicationContext());
        SensorManager sensorManager;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }


        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }


        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the name of the file");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.m_Text=input.getText().toString();
            }

                });
          builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   Intent intent=new Intent(MainActivity.this,Home.class);
                   startActivity(intent);
                }
            });

        builder.show();


        Intent intent = getIntent();
        this.sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        this.listener = new SimpleSensorListener();
        this.publishSubject = PublishSubject.create();
        initializeFSensorFusions();


        poll = Integer.parseInt(intent.getStringExtra("poll"));

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {


            Button start = findViewById(R.id.button3);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onstartclick();
                    startTime = 0;
                    count = 0;

                }
            });
            Button stop = findViewById(R.id.button2);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "PRESS START TO RECORD DATA", Toast.LENGTH_LONG).show();
                }
            });


        } else {
            Toast.makeText(MainActivity.this, "PLEASE GRANT PERMISSSION", Toast.LENGTH_LONG).show();
        }
        return;
    }


    void onstartclick() {
        Button button = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fw.close();
                    unregisterSensors();

                    Toast.makeText(MainActivity.this, "STOPPED,check download folder for csv file", Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    Log.w("ERROR", e.toString());

                }
            }
        });



                Toast.makeText(MainActivity.this, m_Text, Toast.LENGTH_LONG).show();

                String filePath = "/storage/emulated/0/Download/" + m_Text + ".csv";
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
                    fw.append("Gx");
                    fw.append(',');

                    fw.append("Gy");
                    fw.append(',');

                    fw.append("Gz");
                    fw.append(',');
                    fw.append("Path");
                    fw.append(',');


                    fw.append("lat");
                    fw.append(',');

                    fw.append("long");
                    fw.append(',');


                    fw.append("Pressure");
                    fw.append(',');
                    fw.append('\n');
                    startTime = 0;
                    count = 0;
                    Toast.makeText(MainActivity.this, "STARTED RECORDING DATA", Toast.LENGTH_LONG).show();


                } catch (IOException e) {
                    Log.w("ERROR", e.toString());

                }










        final int k = poll;

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                getLocation();
                Button pathchange=findViewById(R.id.path_change);
                pathchange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int k=(int)path;
                       k=k+1;
                       path=(char)k;

                    }
                });
                exportTheDB();

                handler.postDelayed(this, k);
            }
        };
        handler.postDelayed(r, k);


        Button start = findViewById(R.id.button3);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "PRESS STOP", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressure = event.values[0];


        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx= event.values[0];
            gy= event.values[1];
            gz= event.values[2];


        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax= event.values[0];
            ay= event.values[1];
            az= event.values[2];


        }



        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

            float gravityx= event.values[0];
            float gravityy = event.values[1];
            gaugeAcceleration.updatePoint(gravityx,gravityy);



        }

        DecimalFormat formatter = new DecimalFormat("#0.00000");

        //    "P" + Double.toString(pressure));

    }


    void getLocation() {


        calendar = Calendar.getInstance();
        mdformat = new SimpleDateFormat("HH:mm:ss");

        SmartLocation.with(getApplicationContext()).location().continuous()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {


                        if (location.getLatitude() != 0.0) {
                            if (countloc == 0) {
                                lat1 = location.getLatitude();
                                long1 = location.getLongitude();
                                start = Instant.now();


                            }
                            countloc = countloc + 1;
                            long2 = location.getLongitude();
                            lat2 = location.getLatitude();
                            end = Instant.now();


                            if (lat1 != long1 || lat2 != long2) {
                                double dist = distance(lat1, long1, lat2, long2);
                                distance1 = distance1 + dist;
                                Duration timeElapsed = Duration.between(start, end);

                                double time = ((timeElapsed.toMillis()) / 1000);

                                if (dist != 0.0) {
                                    velocity = dist / time;
                                }
                                start = end;
                                //TextView tv = findViewById(R.id.textView3);
                                if (time != 0.0) {
                                    //  tv.setText("Distance:" + distance1 + "\n" + "Time:" + time + "\n" + "vel" + velocity);


                                }
                                lat1 = lat2;
                                long1 = long2;


                            }


                        }


                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        // TextView tv = findViewById(R.id.textView);
                        //tv.setText(Double.toString(latitude) + "," + Double.toString(longitude));


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
            fw.append(Double.toString(gx));
            fw.append(',');

            fw.append(Double.toString(gy));
            fw.append(',');

            fw.append(Double.toString(gz));
            fw.append(',');
            fw.append(Character.toString(path));
            fw.append(',');

            fw.append(Double.toString(latitude));
            fw.append(',');

            fw.append(Double.toString(longitude));
            fw.append(',');


            fw.append(Double.toString(pressure));
            fw.append(',');

            //Toast.makeText(MainActivity.this,"ERITITNT", Toast.LENGTH_LONG).show();

            fw.append('\n');
        } catch (Exception e) {
            //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            Log.w("ERROR", e.toString());
        }


    }

    double distance(double lat1, double lon1, double lat2, double lon2) {
        double p = 0.017453292519943295;    // Math.PI / 180

        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return 12742000 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
    }

    double shortest(double error_range,double mlat,double mlong,double glat[],double glong[],double segments[]){



        return 1;

    }
    @Override
    public PublishSubject<float[]> getPublishSubject() {
        return publishSubject;
    }


    public void setSensorFrequency(int sensorFrequency) {
        this.sensorFrequency = sensorFrequency;
    }

    public void reset() {
        onStop();
        magnetic = new float[3];
        acceleration = new float[3];
        rotation = new float[3];
        output = new float[4];
        hasRotation = false;
        hasMagnetic = false;
        onStart();
    }

    private float calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.

        return (count++ / ((timestamp - startTime) / 1000000000.0f));
    }

    private void initializeFSensorFusions() {
        orientationFusionKalman = new OrientationFusedKalman();
        linearAccelerationFilterKalman = new LinearAccelerationFusion(orientationFusionKalman);
    }

    private void processRawAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.rawAcceleration, 0, this.rawAcceleration.length);
    }

    private void processAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void processMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, this.magnetic.length);
    }

    private void processRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
    }

    private void registerSensors(int sensorDelay) {

        orientationFusionKalman.reset();

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
                sensorDelay);

    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    private void setOutput(float[] value) {
        System.arraycopy(value, 0, output, 0, value.length);
        output[3] = calculateSensorFrequency();
        publishSubject.onNext(output);
        ax = output[0];
        ay = output[1];
        az = output[2];

    }

    private class SimpleSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                 processRawAcceleration(event.values);
                if (!orientationFusionKalman.isBaseOrientationSet()) {
                  if (hasRotation && hasMagnetic) {
                   orientationFusionKalman.setBaseOrientation(RotationUtil.getOrientationVectorFromAccelerationMagnetic(rawAcceleration, magnetic));
                }
                } else {
                  orientationFusionKalman.calculateFusedOrientation(rotation, event.timestamp, rawAcceleration, magnetic);
                processAcceleration(linearAccelerationFilterKalman.filter(rawAcceleration));
Toast.makeText(MainActivity.this,"workimg", (Toast.LENGTH_LONG)).show();
                acceleration = event.values;
                Ax = acceleration[0];
                Ay = acceleration[1];
                Az = acceleration[2];



                 }
                } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    processMagnetic(event.values);
                  hasMagnetic = true;
                } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                   processRotation(event.values);
                hasRotation = true;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}