package sg.edu.rp.c346.id19008424.p10_gettingmylocations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    TextView latText,longText;
    Button btnStart,btnStop,btnCheckRecords;
    private GoogleMap map;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    String folderLocation;
    ToggleButton btnMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latText = findViewById(R.id.latText);
        longText = findViewById(R.id.longText);
        btnCheckRecords = findViewById(R.id.btnCheckRecord);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnMusic = findViewById(R.id.btnMusic);
        mLocationRequest = new  LocationRequest();
        mLocationCallback = new LocationCallback();
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)
                fm.findFragmentById(R.id.map);
        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFolder";

        File folder = new File(folderLocation);
        if (folder.exists() == false){
            boolean result = folder.mkdir();
            if(result == true){
                Log.d("File Read/Write", "Folder Created");
            }
        }
        checkPermission();
        btnMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    startService(new Intent(MainActivity.this,MyService.class));
                }
                else{
                    stopService(new Intent(MainActivity.this,MyService.class));
                }
            }
        });
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                checkPermission();
                Task<Location> task = client.getLastLocation();
                task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null){
                            String msg = "Lat :" + location.getLatitude() + " Lng : " + location.getLongitude();
                            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                            LatLng poi_LastKnown  = new LatLng(location.getLatitude(),location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi_LastKnown,
                                    10));
                            Marker cpEast = map.addMarker(new
                                    MarkerOptions()
                                    .position(poi_LastKnown)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                        else{
                            String msg = "No last known location found";
                            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                        }

                        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION);

                        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                            map.setMyLocationEnabled(true);
                        } else {
                            Log.e("GMap - Permission", "GPS access has not been granted");
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                        }
                    }
                });

            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                startService(new Intent(MainActivity.this,LocationService.class));
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this,LocationService.class));
            }
        });
        btnCheckRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,RecordActivity.class);
                startActivity(i);
            }
        });

    }
    private boolean checkPermission(){
        int permissionCheck_Write = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck_Read = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED ) {
            return true;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);


            return false;
        }
    }
}