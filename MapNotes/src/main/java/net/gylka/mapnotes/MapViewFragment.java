package net.gylka.mapnotes;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapViewFragment extends SupportMapFragment {

    private ArrayList<MarkerOptions> mMarkers;

    public static MapViewFragment newInstance(GoogleMapOptions googleMapOptions, ArrayList<MarkerOptions> markers) {
        MapViewFragment mapViewFragment = new MapViewFragment();
        mapViewFragment.mMarkers = markers;
        return mapViewFragment;
    }

    public MapViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
/*
        GoogleMap googleMap = super.getMap();
        if (googleMap != null) {
            for (MarkerOptions marker : mMarkers) {
                googleMap.addMarker(marker);
            }
        }
*/
    }
}