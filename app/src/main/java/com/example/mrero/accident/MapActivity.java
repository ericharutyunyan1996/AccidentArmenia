
package com.example.mrero.accident;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    TextView addressText;
    Button submitBtnMap;
    Geocoder geocoder;
    List<Address> addresses;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager lm;
    private double mLat;
    private double mLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        addressText = (TextView) findViewById(R.id.addressText);
        submitBtnMap = (Button) findViewById(R.id.submitBtnMap);

        // lm = (LocationManager) MapActivity.this.getSystemService(Context.LOCATION_SERVICE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // mapFragment = SupportMapFragment.newInstance(options);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.getDefault());

        submitBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("key", 1);
                intent.putExtra("address", addressText.getText().toString());
                intent.putExtra("lat", mLat);
                intent.putExtra("lng", mLng);

                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationPermission();
        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLat = mLastLocation.getLatitude();
            mLng = mLastLocation.getLongitude();

            LatLng latLng = new LatLng(mLat, mLng);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(17)
                    .bearing(90)
                    .tilt(30)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // MarkerOptions markerOptions = new MarkerOptions().position(latLng).draggable(true);
            // mMap.addMarker(markerOptions);

            try {
                addresses = geocoder.getFromLocation(mLat, mLng, 1);
                // Here 1 represent max location result to returned, by documents it recommended 1
                // to 5
                String address = addresses.get(0).getAddressLine(0);
                // If any additional address line present than only, check with
                // max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                addressText.setText(address + ", " + city + ", " + country);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        mMap = googleMap;

        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                try {

                    mLat = cameraPosition.target.latitude;
                    mLng = cameraPosition.target.longitude;

                    addresses = geocoder.getFromLocation(mLat, mLng, 1);
                    // Here 1 represent max location result to returned, by documents it recommended
                    // 1 to 5

                    if (addresses.size() == 0)
                        return;
                    // TODO

                    String address = addresses.get(0).getAddressLine(0);
                    // If any additional address line present than only, check with
                    // max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                    addressText.setText(address + ", " + city + ", " + country);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
        //
        // @Override
        // public void onMarkerDragStart(Marker marker) {
        // }
        //
        // @Override
        // public void onMarkerDragEnd(Marker marker) {
        //// Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
        // marker.setSnippet(String.valueOf(marker.getPosition().latitude));
        // mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        //
        // try {
        // addresses = geocoder.getFromLocation(marker.getPosition().latitude,
        // marker.getPosition().longitude, 1);
        //
        // // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        // String address = addresses.get(0).getAddressLine(0);
        // // If any additional address line present than only, check with
        // // max available address lines by getMaxAddressLineIndex()
        // String city = addresses.get(0).getLocality();
        // String state = addresses.get(0).getAdminArea();
        // String country = addresses.get(0).getCountryName();
        // String postalCode = addresses.get(0).getPostalCode();
        // String knownName = addresses.get(0).getFeatureName();
        //
        // addressText.setText(address + ", " + city + ", " + country);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        //
        // @Override
        // public void onMarkerDrag(Marker marker) {
        // }
        //
        // });
    }

    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(MapActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}
