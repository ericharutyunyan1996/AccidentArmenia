
package com.example.mrero.accident;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mMap;
    Accident accident;
    DatabaseReference ref;
    private Calendar now;
    private long currentTimeInMillis;
    private SupportMapFragment mSupportMapFragment;
    private DatabaseReference mDatabase;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        now = Calendar.getInstance();
        currentTimeInMillis = now.getTimeInMillis();

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapWhere);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mSupportMapFragment = SupportMapFragment.newInstance();
        fragmentTransaction.replace(R.id.mapWhere, mSupportMapFragment).commit();

        mSupportMapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Accident");
        mDatabase.keepSynced(true);

        ref = FirebaseDatabase.getInstance().getReference().child("Accident");

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }

        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            Accident marker = userSnapshot.getValue(Accident.class);
                            long accidentTime = marker.getDate();
                            long subTime = currentTimeInMillis - accidentTime;

                            Date date = new Date(accidentTime);
                            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                            String dateFormatted = formatter.format(date);

                            if (subTime < 24 * 3600 * 1000) {

                                Double lat = marker.getLat();
                                Double log = marker.getLng();
                                LatLng latLng = new LatLng(lat, log);

                                mMap.addMarker(new MarkerOptions().position(latLng).icon(
                                        BitmapDescriptorFactory.fromResource(R.drawable.car)).title(dateFormatted));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // handle databaseError
                    }
                });

    }

    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}
