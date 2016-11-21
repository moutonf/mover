package com.mover.vehicle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements getRequest.AsyncResponse, OnMapReadyCallback {

    private int user; //userID
    private TextView tv1 = null;
    private float acc; //Acceleration magnitude value
    private float rawAcc;
    private double g;
    private int crashSize;
    private int crashIndex;
    private LinkedList<Float> crash;

    private float lat; //latitude
    private float lng; //longitude

    private PowerManager.WakeLock wl; //keeps app running in background

    //For writing acc values to file
    private FileOutputStream outputStream;
    private FileOutputStream crashStream;
    private String path;
    private long timer;
    private float maxAcc;
    private float maxRawAcc;
    private double maxG;

    //For location
    private GoogleMap googleMap;
    private Location myPosition;
    private String coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //buffer for crash data
        crashSize = 30;
        crashIndex = 0;
        crash = new LinkedList<Float>();

        //Keeps activity running through screen lock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();

        //Output file setup
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
        String date = new Date().toString();
        String packagename = this.getPackageName();
        path = directory + "/Android/data/" + packagename + "/files/";
        boolean exists = (new File(path)).exists();

        //Make output file directory if it doesn't exist
        if (!exists) {
            new File(path).mkdirs();
        }
        try {
            outputStream = new FileOutputStream(path + "/" + date);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Not in accident state
        Mover.setAccident(false);

        setContentView(com.mover.vehicle.R.layout.activity_main);

        //Get userID
        user = Mover.getUser();

        tv1 = (TextView) findViewById(com.mover.vehicle.R.id.sensorText);

        // Getting reference to the SupportMapFragment of activity_main.xml
        MapFragment mFragment = (MapFragment) getFragmentManager().findFragmentById(com.mover.vehicle.R.id.map);
        mFragment.getMapAsync(this);

        //App records maximum acceleration value of time window every 0.3 seconds
        timer = System.currentTimeMillis();

        //Raw acceleration, acceleration with gravity accounted for, and filtered acceleration
        maxAcc = 0;
        maxRawAcc = 0;
        g = 0;
        maxG = 0;

        //write initial 0 acceleration values to file
        try {
            outputStream.write((String.valueOf(maxAcc)).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Accelerometer
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Accelerometer
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                //Acceleration vector dimensions
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                //Get acceleration vector magnitude minus gravity, rounded to 2 decimal places
                rawAcc = (((float) Math.round(Math.abs(Math.sqrt(x * x + y * y + z * z)) * 100)) / 100);
                acc = (((float) Math.round(Math.abs(Math.sqrt(x * x + y * y + z * z)-9.8) * 100)) / 100);
                g = 0.9 * g + 0.1 * acc;

                //Location
                myPosition = getMyLocation();
                if(myPosition!=null) {
                    lat = ((float) Math.round(myPosition.getLatitude() * 100)) / 100;
                    lng = ((float) Math.round(myPosition.getLongitude() * 100)) / 100;
                }
                //If can't get location
                else {
                    lat = 0.f;
                    lng = 0.f;
                }
                coordinates = "lat: " + lat + "\nlong: " + lng;

                if(Mover.getAccident()){
                    tv1.setText("ACCIDENT!\nAcceleration: " + acc + "\nGPS coordinates:\n" + coordinates);
                }
                else {
                    tv1.setText("Acceleration: " + acc + "\nGPS coordinates:\n" + coordinates);
                }

                //Record max acceleration value every 0.3 seconds
                if (System.currentTimeMillis() > timer + 300) {
                    timer = System.currentTimeMillis();

                    if(crash.size() >= crashSize){ //Acceleration buffer is full
                        if(Mover.getAccident()){ //If we are in an accident state, write accident out to file
                            try {
                                String date = new Date().toString();
                                File crashFile = new File(path + "/crash" + crashIndex + " " + date);
                                crashStream = new FileOutputStream(crashFile);
                                crashStream.write((String.valueOf(crash.remove())).getBytes());
                                for(float crashAcc : crash){
                                    crashStream.write((String.valueOf(" " + crashAcc)).getBytes());
                                }
                                crashIndex++;
                                crashSize = 30;
                                while(crash.size()>crashSize){
                                    crash.remove();
                                }

                                Mover.setAccident(false);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                System.exit(0);
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        }
                        else { //If not in an accident state, just remove from acceleration buffer queue and add to it
                            crash.remove();
                            crash.add(maxAcc);
                        }
                    }
                    else { //If acceleration buffer not full, add to it
                        crash.add(maxAcc);
                    }

                    try { //write out acceleration value to file
                        outputStream.write((String.valueOf(" " + maxRawAcc + " " + maxAcc + " " + maxG)).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    maxAcc = acc;
                    maxRawAcc = rawAcc;
                    maxG = g;
                }
                else { //check if maximum acceleration for the current 300ms window has been exceeded
                    if(acc > maxAcc){
                        maxAcc = acc;
                    }
                    if(rawAcc > maxRawAcc){
                        maxRawAcc = rawAcc;
                    }
                    if(g > maxG) {
                        maxG = g;
                    }
                }

                //If accident detected, call accident aler method
                if (thresholdReached(acc)) {
                    crashSize = 60;
                    Mover.setAccident(true);
                    accidentAlert(lat, lng, maxAcc);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

        }, sensor, SensorManager.SENSOR_DELAY_FASTEST);


    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //Close output file
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Release wake lock
        wl.release();
    }

    //Accident detection threshold
    private boolean thresholdReached(double a) {
        if (a <= 20) {
            return false;
        } else if (Mover.getAccident()) {
            return false;
        } else {
            return true;
        }
    }

    //Send accident record to server
    public void accidentAlert(double lattitude, double longitude, float acc) {

        long unixTime = System.currentTimeMillis() / 1000L;
        CarAccident accident = new CarAccident(acc, lattitude, longitude);
        String request = "type=car&latitude=" + accident.getLat() + "&longitude=" + accident.getLng() + "&time-of-accident=" + unixTime + "&acceleration=" + accident.getAcceleration() + "&userId=" + user;

        //Start acident activity
        //Intent k = new Intent(this, accidentActivity.class);
        //startActivity(k);

        //Send accident record to server
        postRequest asyncTask = (postRequest) new postRequest(new postRequest.AsyncResponse() {

            @Override
            public void processFinish(String output) {
                Context context = getApplicationContext();

                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, output, duration);
                toast.show();
            }
        }, request).execute("http://139.162.178.79:4000/accident");

    }

    @Override
    public void processFinish(String output) {

    }

    //Display map
    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Dont have permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Ask for location permission if it isnt granted
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        googleMap.setMyLocationEnabled(true);
        myPosition = getMyLocation();
    }

    //Get location
    private Location getMyLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check if location permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Ask for location permission if it isnt granted
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            return null;
        }
        else {
            Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation == null) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = lm.getBestProvider(criteria, true);
                myLocation = lm.getLastKnownLocation(provider);
            }
            return myLocation;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    myPosition = getMyLocation();

                } else {
                    // permission denied. Location will be set to (0,0)
                }
            }

        }
    }

}
