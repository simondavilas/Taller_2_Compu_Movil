package com.example.taller_2_davila_burgos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class PuntosMapaActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION = 410;
    private GoogleMap mMap;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Bundle datos;
    double Longitudes[];
    double latitudes[];
    String nombres[];
    DatabaseReference DBase;
    String[] location_permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private double userLat;
    private double userLong;

    private FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puntos_mapa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean dispo = snapshot.child("disponible").getValue(Boolean.class);
                Log.d("USPRUEBA", String.valueOf(dispo));
                if (dispo == true) {
                    Toast.makeText(getBaseContext(), "Disponibilidad activada", Toast.LENGTH_SHORT).show();
                    startLocationService();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
        getLocation();
    }





    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        //DBase = FirebaseDatabase.getInstance().getReference().child("users").child();

        leerJson();
        LatLng centro = new LatLng(4.670863, -74.090360);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(centro));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        int tam = Longitudes.length;
        for(int i = 0;i<tam;i++){
            LatLng punto = new LatLng(latitudes[i], Longitudes[i]);
            mMap.addMarker(new MarkerOptions().position(punto).title(nombres[i]));
        }



    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisssions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permisssions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                userLat = location.getLatitude();
                                userLong = location.getLongitude();
                            } else {
                                //Active Location
                            }
                        }
                    });
                }else {
                    Toast.makeText(this, "Location services were denied by the user", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemClicked = item.getItemId();
        if(itemClicked == R.id.menuEstablecerDisponible){
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean dispo = snapshot.child("disponible").getValue(Boolean.class);
                    Log.d("USPRUEBA", String.valueOf(dispo));
                    if (dispo == true) {
                        Toast.makeText(getBaseContext(), "Disponibilidad desactivada", Toast.LENGTH_SHORT).show();
                        stopLocationService();
                        mDatabase.child("disponible").setValue(false);
                    } else {
                        Toast.makeText(getBaseContext(), "Disponibilidad activada", Toast.LENGTH_SHORT).show();
                        startLocationService();
                        mDatabase.child("disponible").setValue(true);

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mDatabase.addListenerForSingleValueEvent(postListener);

            //Change state in DataBase
        }else if(itemClicked == R.id.menuUsuariosDisponibles){
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());
            final boolean[] aux = new boolean[1];
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean dispo = snapshot.child("disponible").getValue(Boolean.class);
                    Log.d("USPRUEBA", String.valueOf(dispo));
                    if (dispo == true) {
                        Intent userList = new Intent(getApplicationContext(), UsersListActivity.class);
                        startActivity(userList);
                    } else {
                        Toast.makeText(getBaseContext(), "Para ver a otros usuarios debe establecerse como disponible", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mDatabase.addListenerForSingleValueEvent(postListener);

        }else if (itemClicked == R.id.menuCerrarSesion){
            stopLocationService();
            FirebaseAuth.getInstance().signOut();
            Intent start = new Intent(this, StartActivity.class);
            startActivity(start);
        }
        return super.onOptionsItemSelected(item);
    }

    public void getLocation(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        userLat = location.getLatitude();
                        userLong = location.getLongitude();
                        LatLng ubiActual = new LatLng(userLat, userLong);
                        mMap.addMarker(new MarkerOptions().position(ubiActual).title("Ubicación Actual")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }else{
                        Log.i("Location", "Location is null");
                        //Active location
                    }
                }
            });
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permiso requerido")
                    .setMessage("Este permiso es requerido para el acceso a su ubicación")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    PuntosMapaActivity.this, new String[]{
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                    }, REQUEST_LOCATION);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else{
            ActivityCompat.requestPermissions(this, location_permissions, REQUEST_LOCATION);
        }
    }

    private void leerJson(){
        JSONObject objeto1 = null;
        try{
            objeto1 = new JSONObject(convertir());
            JSONArray listaPuntos = objeto1.getJSONArray("locationsArray");
            int tamano = listaPuntos.length();
            Longitudes = new double[tamano];
            latitudes = new double[tamano];
            nombres = new String[tamano];

            for (int i = 0; i<tamano;i++){
                JSONObject objeto2 = listaPuntos.getJSONObject(i);
                Longitudes[i] = objeto2.getDouble("longitude");
                latitudes[i] = objeto2.getDouble("latitude");
                nombres[i] = objeto2.getString("name");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String convertir(){
        String json = null;
        try {
            InputStream entrada = this.getAssets().open("locations.json");
            int tam = entrada.available();
            byte[] buffer = new byte[tam];
            entrada.read(buffer);
            entrada.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private boolean isLocationServiceRunning(){
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(service.service.getClassName())){
                    if(service.foreground)
                        return true;
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stated", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }

}