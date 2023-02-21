package com.rifqi.dude2;

import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class RetrieveMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String longitude;
    private String latitude;
    private double mlongitude;
    private double mlatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        longitude = getIntent().getExtras().get("longitude").toString();
        latitude = getIntent().getExtras().get("latitude").toString();
        mlongitude = Double.parseDouble(longitude);
        mlatitude = Double.parseDouble(latitude);


        LatLng location = new LatLng(mlatitude,mlongitude);

        mMap.addMarker(new MarkerOptions().position(location).title(getCompleteAddress(mlatitude,mlongitude)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,14F));
    }

    private String getCompleteAddress(double Latitude, double Longtitude){

        String address = "";

        Geocoder geocoder = new Geocoder(RetrieveMapActivity.this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(Latitude,Longtitude,1);

            if (address != null){
                Address returnAddress = addresses.get(0);
                StringBuilder stringBuilderReturnAddress = new StringBuilder("");

                for (int i=0; i<=returnAddress.getMaxAddressLineIndex(); i++){
                    stringBuilderReturnAddress.append(returnAddress.getAddressLine(i)).append("\n");
                }

                address = stringBuilderReturnAddress.toString();
            }

            else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

        return address;
    }
}