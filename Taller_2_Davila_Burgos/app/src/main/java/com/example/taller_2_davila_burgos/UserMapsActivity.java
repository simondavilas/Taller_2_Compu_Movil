package com.example.taller_2_davila_burgos;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String userSearchName;
    private LatLng user;
    private LatLng userSearch;
    private String otherUserId;
    private Double distance = 0.0;
    public double lat;
    public double lon;
    public double lat2;
    public double lon2;

    private Marker myMarker;
    private Marker markerotherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);

        mAuth = FirebaseAuth.getInstance();

        markerotherUser = null;
        myMarker = null;
        Bundle extras = getIntent().getExtras();
        otherUserId = getIntent().getExtras().getString("otherUserID");
        if(otherUserId == null) {
            otherUserId = extras.getString("otherUID");
            Log.d("USPRUEBA", extras.getString("otherUID"));
        }
        userSearchName = getIntent().getExtras().getString("nombre");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapUser);
        mapFragment.getMapAsync(this);
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
        setMyMarker();
        setOhterUserMarker();
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
    private void setMyMarker(){
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lat = snapshot.child("latitude").getValue(Double.class);
                lon  = snapshot.child("longitude").getValue(Double.class);
                user = new LatLng(lat,lon);
                Log.d("USPRUEBA", "My "+mAuth.getUid() +", latitude:"+lat+", longitude"+lon);
                if(myMarker != null){
                    myMarker.remove();
                }
                myMarker = mMap.addMarker(new MarkerOptions().position(user).title("Tu ubicación Actual")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setOhterUserMarker(){
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lat2 = snapshot.child("latitude").getValue(Double.class);
                lon2 = snapshot.child("longitude").getValue(Double.class);
                userSearchName = snapshot.child("name").getValue(String.class) + " " + snapshot.child("lastName").getValue(String.class);
                userSearch = new LatLng(lat2,lon2);
                Log.d("USPRUEBA", "Other "+otherUserId +", latitude:"+lat2+", longitude"+lon2);
                if(markerotherUser != null){
                    markerotherUser.remove();
                }

                markerotherUser = mMap.addMarker(new MarkerOptions().position(userSearch).title("Ubicación de "+userSearchName));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userSearch));

                Toast.makeText(getBaseContext(), "La distancia entre los dos puntos es de: "+ distancia(lat,lon,lat2,lon2)+ "Km", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void calcularDistancia(){

    }
    public double distancia(double lat1, double long1, double lat2, double long2) {

        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = 6371 * c;
        return Math.round(result*100.0)/100.0;
    }
}