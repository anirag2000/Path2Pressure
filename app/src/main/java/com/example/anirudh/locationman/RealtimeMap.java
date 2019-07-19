package com.example.anirudh.locationman;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.opencsv.CSVReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.apache.commons.lang3.StringUtils.split;

public class RealtimeMap extends AppCompatActivity implements OnMapReadyCallback,SensorEventListener {
public static volatile GoogleMap mmap;
public  volatile double latitude;
public  volatile double longitude;
public  volatile double latitude_cell;
public  volatile double longitude_cell;
    String latlong;
    List<LatLng> gps;
   MarkerOptions options ;
    String [] list;
    int len;

    SensorManager sensorManager;
    volatile  String  reference;

    volatile int status=0;
    volatile static double pressure;


    volatile String serverurl;
    volatile int postcount=1;
    volatile String dtwmp;
    volatile static String m_Text="";
    volatile  static  int countreference;
    volatile static String realtimevalue="\n";

    volatile static  int counter=0;
    volatile int referencecount=0;
    volatile String reference_multiple="";
    volatile String inputref;
    File myFile;
    FileWriter fw;
    volatile int count = 0;
    public volatile Marker ma;
    Button stop;
    Button start;
    public volatile TelephonyManager telephonyManager;
    public volatile String cidcomb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_map);
        getSupportActionBar().hide();
        Intent intent=getIntent();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        start=findViewById(R.id.start);
       stop=findViewById(R.id.stop);
       stop.setVisibility(View.INVISIBLE);


        latlong=intent.getStringExtra("latlong");
        m_Text=intent.getStringExtra("filename");
        String filePath = "/storage/emulated/0/Download/" + m_Text + "_real.csv";
        try {
            myFile = new File(filePath);
            fw = new FileWriter(filePath);
        }
        catch (Exception e)
        {

        }
        Resources res = getResources();
        serverurl=res.getString(R.string.url);

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(RealtimeMap.this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);


        options = new MarkerOptions();

        Log.w("filename", m_Text );
gps=new ArrayList<>();
         list=latlong.split("&");
         len=list.length;
        Log.w("lol", list[0].split(",")[0]);
         if(len%2!=0)
         {
             len=len-1;
         }

        for(int i=0;i<list.length;i++)
        {
            Log.w("lol", Integer.toString(i) );
            gps.add(new LatLng(Double.parseDouble(list[i].split(",")[0]),Double.parseDouble(list[i].split(",")[1])));


        }

        SupportMapFragment supportMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(RealtimeMap.this);
Button start=findViewById(R.id.start);
start.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        counter=1;
    }
});
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       RealtimeMap.mmap=googleMap;

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
               .addAll(gps)

                .width(8)
                .color(Color.RED));


        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
        TextView distancetext=findViewById(R.id.distance);
        TextView time=findViewById(R.id.time);

        double distance = SphericalUtil.computeLength(gps);
        DecimalFormat dec = new DecimalFormat("#0.00");
        double dis_km=Double.parseDouble(dec.format(distance/1000));
        int hour=((int)dis_km)/20;
        int min=(int)((((((dis_km/20)*100)%100))/100)*60);

        if(hour==0)
        {
            time.setText(Integer.toString(min)+" min");

        }
        else
        {
            time.setText(Integer.toString(hour)+" hr"+Integer.toString(min)+" min");
        }



        if(distance>999)
        {
            distancetext.setText("("+(dec.format(distance/1000)) + " km)");
        }
        else {

            distancetext.setText((dec.format(distance)) + "m");
        }
        Log.w("lol", Double.toString(distance) );
            googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(list[0].split(",")[0]),Double.parseDouble(list[0].split(",")[1]))).title("start"));
      mmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(new LatLng(Double.parseDouble(list[len-1].split(",")[0]),Double.parseDouble(list[len-1].split(",")[1]))).title("end"));
        mmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(list[0].split(",")[0]), Double.parseDouble(list[0].split(",")[1])), 19));
             // Set listeners for click events.


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addreference();
            }
        });
    }

    void addreference() {
        stop.setVisibility(View.VISIBLE);
        start.setVisibility(View.INVISIBLE);


     //  Marker ma= mmap.addMarker(new MarkerOptions().position(new LatLng(12.8983721,77.6179345)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

        // Marker icon


        // Add marker to map






        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                showCellinfo();

                // Do your task

                try {

                    OutputStream os = null;
                            InputStream is = null;
                            HttpURLConnection conn = null;
                            try {
                                //constants
                                URL url = new URL(serverurl+"/cal_latlong");
                                JSONObject jsonObject = new JSONObject();
///if you want cell info use cellcomb
                                jsonObject.put("filename", m_Text);
                                jsonObject.put("pressure", pressure);
                                Log.w("real", Double.toString(pressure));

                                fw.append(Double.toString(pressure));
                                fw.append('\n');

                                stop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            fw.close();
                                        }
                                        catch (Exception e)
                                        {
                                            Log.w("warning", "filenotfound " );
                                        }
                                        onstopbutton();
                                    }
                                });

                                String message = jsonObject.toString();

                                conn = (HttpURLConnection) url.openConnection();
                                conn.setReadTimeout(35000 );
                                conn.setConnectTimeout(25000 );
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
                                    String reply = sb.toString();

                                    if(mmap!=null) {
                                        if(ma!=null)
                                        {

                                        }
                                         latitude = Double.parseDouble(reply.split(",")[0]);
                                         longitude = Double.parseDouble(reply.split(",")[1]);

                                        runOnUiThread(new Runnable(){
                                            public void run() {
                                                // UI code goes here
                                                if(ma!=null)
                                                {
                                                    ma.remove();
                                                }
                                                Log.w("RESULY",Double.toString(latitude)+","+Double.toString(longitude));


                                                ma=RealtimeMap.mmap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));


                                            }
                                        });

                                    }
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
                catch (Exception e)
                {

                }

                cell_pre();
                /////////////////////////////////////////////////////////celll//////////////////////////////
                try {

                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl+"/celltower_real");
                        JSONObject jsonObject = new JSONObject();
///if you want cell info use cellcomb
                        jsonObject.put("filename", m_Text);
                        jsonObject.put("Cell_tower", cidcomb);




                        String message = jsonObject.toString();

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(35000 );
                        conn.setConnectTimeout(25000 );
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
                            String reply = sb.toString();

                            if(mmap!=null) {
                                if(ma!=null)
                                {

                                }
                                latitude_cell = Double.parseDouble(reply.split(",")[0]);
                                longitude_cell = Double.parseDouble(reply.split(",")[1]);

                                runOnUiThread(new Runnable(){
                                    public void run() {
                                        // UI code goes here
                                        if(ma!=null)
                                        {
                                            ma.remove();
                                        }
                                        Log.w("RESULY",Double.toString(latitude_cell)+","+Double.toString(longitude_cell));


                                        ma=RealtimeMap.mmap.addMarker(new MarkerOptions().position(new LatLng(latitude_cell,longitude_cell)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                                    }
                                });

                            }
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
                catch (Exception e)
                {

                }

















            }
        }, 0, 200);





    }






    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            RealtimeMap.countreference += 1;

            Log.w("pressure", Double.toString(RealtimeMap.pressure));
            pressure = event.values[0];


        }
    }
    void onstopbutton()
    {

        Thread thread = new Thread() {
            @Override
            public void run() {

                // Do your task

                try {

                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl + "/reset");
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("filename", m_Text);


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
                            String reply = sb.toString();
                            Log.w("RESULYQWE", reply);


                        } finally {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.w("warning", e.toString());

                    }  finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }











                    Intent intent=new Intent(RealtimeMap.this,Home.class);
                    startActivity(intent);
                }
                catch (Exception e)
                {

                }


            }
        };

        thread.start();





    }
    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }



    public void showCellinfo() {
        TextView tv = findViewById(R.id.textView);
        List<CellInfo> cellInfoList = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            Log.w("cellinfo", "showCellinfo: " );

        } else if (cellInfoList.size() == 0) {

        } else {
            int cellNumber = cellInfoList.size();
            BaseStation main_BS = bindData(cellInfoList.get(0));
            tv.setText( main_BS.toString());

            for (CellInfo cellInfo : cellInfoList) {
                BaseStation bs = bindData(cellInfo);
                //Log.i(TAG, bs.toString());
                String cs=cellInfo.toString().substring(25,28);
                if(cs.equalsIgnoreCase("YES"))
                {
                    cidcomb=cidcomb+cellInfo.toString().substring(138,143)+".";
                }
                else if(cs.equalsIgnoreCase("NO "))
                {
                    cidcomb=cidcomb+cellInfo.toString().substring(133,138)+".";
                }

            }
            Log.w("cell", cidcomb );
        }

    }

    private BaseStation bindData(CellInfo cellInfo) {
        BaseStation baseStation = null;
        //基站有不同信号类型：2G，3G，4G
        if (cellInfo instanceof CellInfoWcdma) {
            //联通3G
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("WCDMA");
            baseStation.setCid(cellIdentityWcdma.getCid());
            baseStation.setLac(cellIdentityWcdma.getLac());
            baseStation.setMcc(cellIdentityWcdma.getMcc());
            baseStation.setMnc(cellIdentityWcdma.getMnc());
            baseStation.setBsic_psc_pci(cellIdentityWcdma.getPsc());
            if (cellInfoWcdma.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoWcdma.getCellSignalStrength().getAsuLevel()); //Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP.
                baseStation.setSignalLevel(cellInfoWcdma.getCellSignalStrength().getLevel()); //Get signal level as an int from 0..4
                baseStation.setDbm(cellInfoWcdma.getCellSignalStrength().getDbm()); //Get the signal strength as dBm
            }
        } else if (cellInfo instanceof CellInfoLte) {
            //4G
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("LTE");
            baseStation.setCid(cellIdentityLte.getCi());
            baseStation.setMnc(cellIdentityLte.getMnc());
            baseStation.setMcc(cellIdentityLte.getMcc());
            baseStation.setLac(cellIdentityLte.getTac());
            baseStation.setBsic_psc_pci(cellIdentityLte.getPci());
            if (cellInfoLte.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoLte.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoLte.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoLte.getCellSignalStrength().getDbm());
            }
        } else if (cellInfo instanceof CellInfoGsm) {
            //2G
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("GSM");
            baseStation.setCid(cellIdentityGsm.getCid());
            baseStation.setLac(cellIdentityGsm.getLac());
            baseStation.setMcc(cellIdentityGsm.getMcc());
            baseStation.setMnc(cellIdentityGsm.getMnc());
            baseStation.setBsic_psc_pci(cellIdentityGsm.getPsc());
            if (cellInfoGsm.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoGsm.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoGsm.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoGsm.getCellSignalStrength().getDbm());
            }
        } else {
            //电信2/3G
            //Log.e(TAG, "CDMA CellInfo................................................");
        }
        return baseStation;
    }

    void cell_pre()
    {
        try {

            OutputStream os = null;
            InputStream is = null;
            HttpURLConnection conn = null;
            try {
                //constants
                URL url = new URL(serverurl+"/celltower_pre");
                JSONObject jsonObject = new JSONObject();
///if you want cell info use cellcomb
                jsonObject.put("filename", m_Text);





                String message = jsonObject.toString();

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(35000 );
                conn.setConnectTimeout(25000 );
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
                    String reply = sb.toString();



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
        catch (Exception e)
        {

        }





    }






    }

