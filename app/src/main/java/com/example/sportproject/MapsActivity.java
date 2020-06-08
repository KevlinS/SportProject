package com.example.sportproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private CameraPosition mCameraPosition;


    private Button btleft = null;
    private Button btright = null;
    private Button btmiddle = null;
    private TextView texttime = null;
    private TextView textlength = null;
    private TextView textpace = null;
    private TextView textcalories = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private Handler mHandler = null;
    /*DES DONNEES DE SORTIE*/
    private static int count = 0;
    private static double s = 0;
    private static double d = 0;
    private static double sum = 0;
    private static double latitude = 0;
    private static double longitude = 0;
    private static double vdraw = 0;
    private static int v = 0;
    private static int pace = 0;
    private static int calories = 0;

    private boolean isPause = false;
    private boolean isStop = true;
    private static int delay = 1000; //1s
    private static int period = 1000; //1s
    private static final int UPDATE_TEXTVIEW = 0;

    private static double EARTH_RADIUS = 6378.137;//rayon de la terre

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    float[] results = new float[3];
    // LATITUDE LONGITUDE
    List<Double> latList = new ArrayList<Double>();
    List<Double> lonList = new ArrayList<Double>();

    private Location previousLocation = null;
    private ArrayList<Polyline> runningRoute = new ArrayList<Polyline>();
    private ArrayList<Location> points = new ArrayList<Location>();

    // GESTION DE GOOGLE MAPS
    private boolean isDraw = false;
    private boolean showBound = false;

    String DB_NAME = "running_db.sqlite";
    RunningDAO runningdao;

    private String startTime = null;
    private String finishTime = null;

    // GESTION DU THEME
    public static final String PREFS_NAME = "prefs";
    public static final String PREF_DARK_THEME = "dark_theme";



    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Utilisér le thème choisi
        boolean useDarkTheme = isDarkThemeEnabled();

        if(useDarkTheme) {
            setTheme(R.style.AppThemeDark);
        }
        //
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
         //switch
        Switch toggle = findViewById(R.id.switch1);
        toggle.setChecked(useDarkTheme);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                toggleTheme(isChecked);
            }
        });

        //Déclaration des buttons et Textview
        btleft = (Button) findViewById(R.id.button_left);
        btright = (Button) findViewById(R.id.button_right);
        btmiddle = (Button) findViewById(R.id.button_middle);
        texttime = (TextView) findViewById(R.id.data_time);
        textlength = (TextView) findViewById(R.id.data_length);

        btleft.setOnClickListener(listener);
        btmiddle.setOnClickListener(listener);
        btright.setOnClickListener(listener);

        btleft.setEnabled(true);
        btright.setEnabled(false);
        btmiddle.setEnabled(false);
        // connexion de la base de données
        final File dbFile = this.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) {
            try {
                copyDatabaseFile(dbFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // query les données de la base de données
        queryDataFromDatabase();// query function

        // Gérer les Texview ( Time, Distance )
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TEXTVIEW:
                        updateTextView();
                        break;
                    default:
                        break;
                }
            }
        };


        // google map utilise un fragment par défaut
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //La fonction getMapAsync (initialiser le système de carte et la vue)
        mapFragment.getMapAsync(this);

        mFusedLocationClient = getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        //définir l'intervalle de mise à jour de l'emplacement (0,3 seconde)
        mLocationRequest.setInterval(300);
        //la demande d'intervalle de 0,01 seconde
        mLocationRequest.setFastestInterval(100);
        //demander un emplacement de haute précision
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        //vérifier l'autorisation de demande de localisation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1340);
        } else {
            requestLocationUpdates();
        }

    }

    // Dark Theme Méthode
    private boolean isDarkThemeEnabled() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(PREF_DARK_THEME, false);
    }

    //copier le fichier de base de données en local depuis APK
    private void copyDatabaseFile(String destinationPath) throws IOException {
        InputStream assetsDB = this.getAssets().open(DB_NAME);
        OutputStream dbOut = new FileOutputStream(destinationPath);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = assetsDB.read(buffer)) > 0) {
            dbOut.write(buffer, 0, length);
        }
        dbOut.flush();
        dbOut.close();
    }

    // query les données
    public void queryDataFromDatabase() {
        AppDatabase database = Room.databaseBuilder(this, AppDatabase.class, DB_NAME).allowMainThreadQueries().build();
        runningdao = database.getRunningdataDAO();
        List<Runningdata> runningdata_list = runningdao.getAllRuningdata();
        for (int i = 0; i < runningdata_list.size(); i++) {
            int oldId = runningdata_list.get(i).getId();
            String oldStarttime = runningdata_list.get(i).getStarttime();
            String oldFinishtime = runningdata_list.get(i).getFinishtime();
            double oldDistance = runningdata_list.get(i).getDistance();

        }

    }

    // insérer des données dans la base de données locale
    public void AddDataRecordtoDB(View view) {

        //obtenir la distance de course en textview
        TextView TV_distance = (TextView)findViewById(R.id.data_length);
        Double runningDistance = Double.parseDouble(TV_distance.getText().toString());
        System.out.println("La distance est:"+runningDistance);

        Runningdata NewRunningdata = new Runningdata();
        NewRunningdata.setDistance(runningDistance);
        NewRunningdata.setStarttime(startTime);
        NewRunningdata.setFinishtime(finishTime);

        //utiliser "dao" pour manipuler la base de données
        runningdao.insert(NewRunningdata);
        System.out.println("J'ai insérer les données");
    }


    @SuppressLint("NewApi")
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        // demander la permission de Google Map (location)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1340);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                isDarkThemeEnabled() ? R.raw.map_night_style : R.raw.map_day_style));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //désactiver le bouton de zoom car le niveau de zoom est fixe.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        //activer le bouton de positionnement
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        //désactivez cela car après la fenêtre contextuelle du marqueur POI, cet outil sera ajouté automatiquement
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }


    //cette méthode gère le résultat de l'autorisation
    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1340:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(this, "Location cannot be obtained due to " + "missing permission.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, null);
    }

    public void onLocationChanged(Location location) {
        if (location != null) {

            if(!showBound) {
                // le niveau de zoom est 17
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            }
            // dessiner l'itinéraire
            if (isDraw) {
                routeDrawing(location);
            }

        }
    }

    //dessiner une polyligne pendant la course.
    private void routeDrawing(Location location) {

        //obtenir la position exact
        if (previousLocation == null) {
            previousLocation = location;
        }
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        latitude = lat;
        longitude = lon;
        latList.add(latitude);
        lonList.add(longitude);

        PolylineOptions lineOptions = new PolylineOptions();
        vdraw = (GetDistance(previousLocation.getLatitude(), previousLocation.getLongitude(), location.getLatitude(), location.getLongitude()))/0.3;
        System.out.println("vdraw: "+vdraw);
        if (vdraw<0.01){

            lineOptions.add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    .color(getResources().getColor(R.color.slow))
                    .width(30);
            System.out.print("Je cours lentement");
        }
        if (vdraw>=0.008 && vdraw<=0.03){
            lineOptions.add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    .color(getResources().getColor(R.color.commen))
                    .width(30);
            System.out.print("Je cours normalement");
        }
        if (vdraw>0.03){
            lineOptions.add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()))
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    .color(getResources().getColor(R.color.fast))
                    .width(30);
            System.out.print("Je cours vite");
        }

        //ajouter la polyligne à la carte
        Polyline partOfRunningRoute = mMap.addPolyline(lineOptions);
        partOfRunningRoute.setZIndex(1000);
        runningRoute.add(partOfRunningRoute);
        points.add(location);
        previousLocation = location;
    }


    /*CHRONOMETRE*/
    private View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == btleft) {//Button démarrer

                previousLocation = null;
                //commence à dessiner
                isDraw = true;
                showBound = false;

                if(!runningRoute.isEmpty()) {
                    //supprimer toutes les polylignes de la carte
                    for (Polyline line : runningRoute) {
                        line.remove();
                    }
                    runningRoute.clear();
                }

                isStop = !isStop;
                btleft.setEnabled(false);
                btleft.setBackgroundColor(getResources().getColor(R.color.unabled));
                btright.setEnabled(true);
                btright.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btmiddle.setEnabled(true);
                startTimer();
                btmiddle.setBackgroundResource(R.drawable.pause_button);
                textlength.setText("0.00");


                //obtenir l'heure actuelle du système de périphérique.
                startTime = Calendar.getInstance().getTime().toString();
                System.out.println("Heure de début: " + startTime);
                //Sun Jun 07 13:12:49 GMT 2020

            }

            if (v == btmiddle) {
                btleft.setEnabled(false);
                btleft.setBackgroundColor(getResources().getColor(R.color.unabled));
                btright.setEnabled(true);
                btmiddle.setEnabled(true);
                pauseTimer();

                if (isPause) {//pause
                    btmiddle.setBackgroundResource(R.drawable.play_button);
                    //arreter de dessiner
                    isDraw = false;
                }else{//continuer
                    btmiddle.setBackgroundResource(R.drawable.pause_button);
                    previousLocation = null;
                    isDraw = true;
                }
            }

            if (v == btright){//arrêter
                stopButton();




                // ajouter les données à la base de données
                AddDataRecordtoDB(v);
            }
        }
    };

    private void stopButton() {
        if (isPause){
            isPause = !isPause;
        }
        stopTimer();
        sum = 0;
        btmiddle.setBackgroundResource(R.drawable.logo_round);
        btleft.setEnabled(true);
        btleft.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btmiddle.setEnabled(false);
        btright.setEnabled(false);
        btright.setBackgroundColor(getResources().getColor(R.color.unabled));
        isDraw = false;
        finishTime = Calendar.getInstance().getTime().toString();
        System.out.println("heure de fin de course: " + finishTime);

        showBound = true;
        showBounds();
        // Effacer les arrays
        points.clear();
    }

    // Obtenir bounds (Zoomer la map)
    private void showBounds(){

        LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();


        for (int i = 0; i < points.size(); i++) {
            LatLng b_position = new LatLng(points.get(i).getLatitude(),points.get(i).getLongitude());
            boundBuilder.include(b_position);
        }

        LatLngBounds bounds = boundBuilder.build();

        int deviceWidth = getResources().getDisplayMetrics().widthPixels;
        int deviceHeight = getResources().getDisplayMetrics().heightPixels;
        int devicePadding = (int) (deviceHeight * 0.20); //décalage des bords de la carte 10% de l'écran.

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, deviceWidth, deviceHeight, devicePadding);
        mMap.animateCamera(cu);

    }


    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {

                    sendMessage(UPDATE_TEXTVIEW);
                    do {
                        try {

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);
                    count++;/*mise à jour de temps*/
                }
            };
        }

        if (mTimer != null)
            mTimer.schedule(mTimerTask, delay, period);

    }

    private void pauseTimer(){
        isPause = !isPause;
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;

        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;

        }
        count = 0;
    }

    public void sendMessage(int id) {
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }

    /*CHRONOMETRE*/
    /*LES FORMATS DU TEMPS*/
    public static String getTime(int second) {
        if (second < 10) {
            return "00:00:0" + second;
        }
        if (second < 60) {
            return "00:00:" + second;
        }
        if (second < 3600) {
            int minute = second / 60;
            second = second - minute * 60;
            if (minute < 10) {
                if (second < 10) {
                    return "00:" + "0" + minute + ":0" + second;
                }
                return "00:" + "0" + minute + ":" + second;
            }
            if (second < 10) {
                return "00:" + minute + ":0" + second;
            }
            return "00:" + minute + ":" + second;
        }
        int hour = second / 3600;
        int minute = (second - hour * 3600) / 60;
        second = second - hour * 3600 - minute * 60;
        if (hour < 10) {
            if (minute < 10) {
                if (second < 10) {
                    return "0" + hour + ":0" + minute + ":0" + second;
                }
                return "0" + hour + ":0" + minute + ":" + second;
            }
            if (second < 10) {
                return "0" + hour + ":" + minute + ":0" + second;
            }
            return "0" + hour + ":" + minute + ":" + second;
        }
        if (minute < 10) {
            if (second < 10) {
                return hour + ":0" + minute + ":0" + second;
            }
            return hour + ":0" + minute + ":" + second;
        }
        if (second < 10) {
            return hour + ":" + minute + ":0" + second;
        }
        return hour + ":" + minute + ":" + second;


    }

    //Obtenir la distance
    public static double GetDistance(double lat1, double lon1, double lat2, double lon2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;//km
        return s;
    }

    public void updateTextView() {
        texttime.setText(getTime(count));
        for (int i = 1; i < latList.size(); i++) {
            s = GetDistance(latList.get(i - 1),
                    lonList.get(i - 1),
                    latList.get(i),
                    lonList.get(i));

        }
        for (int j=10; j < latList.size(); j=j+10 ){
            d = GetDistance(latList.get(j - 10),
                    lonList.get(j - 10),
                    latList.get(j),
                    lonList.get(j));

        }
        sum = sum + s;
        String Sum = String.format("%.2f",sum);
        textlength.setText(Sum);

    }

    //Toogle du thème
    private void toggleTheme(boolean darkTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_DARK_THEME, darkTheme);
        editor.apply();

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                darkTheme ? R.raw.map_night_style : R.raw.map_day_style));

        //puisque nous devons recréer l'activité lors du changement de thème
        //assurez-vous au moins d'arrêter le chronomètre et de stocker les données.
        if (btright.isEnabled()) {
            stopButton();
        }
        recreate();
    }
    public void onClickStartNewActivity(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}