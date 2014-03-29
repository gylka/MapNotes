package net.gylka.mapnotes;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapViewFragment extends SupportMapFragment implements OnMapNoteManipulationListener,
        NotesListFragment.OnNotesListManipulationListener {

    public static final int CAMERA_ANIMATION_DURATION = 1000;

    public static final String SELECTED_MARKER_LATLNG_KEY = "SelectedMarkerLatLngKey";
    public static final String MARKER_LATLNG_WITH_INFO_WINDOW_SHOWN = "MarkerWithInfoWindowShown";

    private Marker mSelectedMarker;
    private Marker mMarkerWithInfoWindowShown;

    private List<MapNote> mMapNotes;
    private MapNotesDao mMapNotesDao;
    private Map<Marker, Long> mMapMarkers;

    private Menu mMenu;
    private LayoutInflater mInflater;
    private OnMarkerProcessIntentListener mOnMarkerProcessIntentListener;
    private NotesListManipulationListenerAdapter mNotesListManipulationListenerAdapter;

    public static MapViewFragment newInstance() {
        MapViewFragment mapViewFragment = new MapViewFragment();
        return mapViewFragment;
    }

    public MapViewFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnMarkerProcessIntentListener = (OnMarkerProcessIntentListener) activity;
            mOnMarkerProcessIntentListener.AddOnMapNoteManipulationListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMarkerProcessIntentListener");
        }
        if (activity instanceof NotesListManipulationListenerAdapter) {
            mNotesListManipulationListenerAdapter = (NotesListManipulationListenerAdapter) activity;
            mNotesListManipulationListenerAdapter.addOnNotesListManipulationListener(this);
        }
    }

    @Override
    public void onDetach() {
        Log.d("MapViewFragment", "onDetach");
        if (mOnMarkerProcessIntentListener != null) {
            mOnMarkerProcessIntentListener.RemoveOnMapNoteManipulationListener(this);
            mOnMarkerProcessIntentListener = null;
        }
        if (mNotesListManipulationListenerAdapter != null) {
            mNotesListManipulationListenerAdapter.removeOnNotesListManipulationListener(this);
            mNotesListManipulationListenerAdapter = null;
        }
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mMapNotesDao = new MapNotesDaoImpl(getActivity());
        mMapNotes = mMapNotesDao.getAllNotes();
        mMapMarkers = new HashMap<Marker, Long>();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        GoogleMap googleMap = getMap();
        if ((savedInstanceState != null) && (googleMap != null)) {
            LatLng selectedMarkerLatLng = savedInstanceState.getParcelable(SELECTED_MARKER_LATLNG_KEY);
            if (selectedMarkerLatLng != null) {
                // Checking if selected Marker was new. getMarkerByLatLng() returns null is it was new
                Marker selectedMarker = getMarkerByLatLng(selectedMarkerLatLng);
                if (selectedMarker == null) {
                    selectedMarker = googleMap.addMarker(new MarkerOptions().position(selectedMarkerLatLng));
                }
                selectMarker(selectedMarker);

                LatLng markerWithInfoWindowShownLatLng = savedInstanceState.getParcelable(MARKER_LATLNG_WITH_INFO_WINDOW_SHOWN);
                if (selectedMarkerLatLng.equals(markerWithInfoWindowShownLatLng)) {
                    mSelectedMarker.showInfoWindow();
                    mMarkerWithInfoWindowShown = mSelectedMarker;
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mInflater = inflater;
        final GoogleMap googleMap = getMap();
        if (googleMap != null) {
            for (MapNote mapNote : mMapNotes) {
                Marker marker = googleMap.addMarker(new MarkerOptions().position(mapNote.getLatLng()));
                mMapMarkers.put(marker, mapNote.getId());
            }

            NoteInfoWindowAdapter infoWindowAdapter = new NoteInfoWindowAdapter();
            googleMap.setInfoWindowAdapter(infoWindowAdapter);
            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));
                    selectMarker(marker);
                }
            });

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Log.d("mSelectedMarker address", "" + mSelectedMarker);
                    if (mSelectedMarker != null) {
                        Log.d("mSelectedMarker.getId()", "" + mSelectedMarker.getId());
                    }
                    Log.d("marker address", "" + marker);
                    if (marker != null) {
                        Log.d("marker.getId()", "" + marker.getId());
                    }
                    Log.d("mMarkerWithInfoWindowShown address", "" + mMarkerWithInfoWindowShown);
                    if (mMarkerWithInfoWindowShown != null) {
                        Log.d("mMarkerWithInfoWindowShown.getId()", "" + mMarkerWithInfoWindowShown.getId());
                    }

                    Log.d("mSelected -> Marker Equals", ""+marker.equals(mSelectedMarker));
                    Log.d("Marker IsNew", "" + isMarkerNew(marker));
                    Log.d("Marker -> mMarkerWithInfoWindowShown", ""+marker.equals(mMarkerWithInfoWindowShown));

                    // if changed selection - hide InfoWindow on previous marker;
                    if ( ! marker.equals(mSelectedMarker) ) {
                        if (mMarkerWithInfoWindowShown != null) {
                            mMarkerWithInfoWindowShown.hideInfoWindow();
                            mMarkerWithInfoWindowShown = null;
                        }
                        selectMarker(marker);
                    } else {
                        if( ! isMarkerNew(marker)) {
                            if (marker.equals(mMarkerWithInfoWindowShown)) {
                                Log.d("Hide InfoWindow Procedure", "");
                                mMarkerWithInfoWindowShown.hideInfoWindow();
                                mMarkerWithInfoWindowShown = null;
                            } else {
                                Log.d("Show InfoWindow Procedure", "");
                                mMarkerWithInfoWindowShown = marker;
                                mMarkerWithInfoWindowShown.showInfoWindow();
                                setCameraOnMapNote(mMapMarkers.get(marker));
                            }
                        }
                    }

                    return true;
                }
            });

            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    String provider = locationManager.getBestProvider(new Criteria(), false);
                    Location currentLocation = locationManager.getLastKnownLocation(provider);
                    LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    setCameraOnLatLng(currentLatLng);
                    return true;
                }
            });

        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedMarker != null) {
            outState.putParcelable(SELECTED_MARKER_LATLNG_KEY, mSelectedMarker.getPosition());
        }
        if (mMarkerWithInfoWindowShown != null) {
            outState.putParcelable(MARKER_LATLNG_WITH_INFO_WINDOW_SHOWN, mMarkerWithInfoWindowShown.getPosition());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_view_fragment, menu);
        mMenu = menu;
        if (mSelectedMarker != null) {
            if (isMarkerNew(mSelectedMarker)) {
                mMenu.findItem(R.id.action_create_mapnote).setVisible(true).setEnabled(true);
                mMenu.findItem(R.id.action_edit).setVisible(false).setEnabled(false);
                mMenu.findItem(R.id.action_delete).setVisible(false).setEnabled(false);
                mMenu.findItem(R.id.action_cancel).setVisible(true).setEnabled(true);
            } else {
                mMenu.findItem(R.id.action_create_mapnote).setVisible(false).setEnabled(false);
                mMenu.findItem(R.id.action_edit).setVisible(true).setEnabled(true);
                mMenu.findItem(R.id.action_delete).setVisible(true).setEnabled(true);
                mMenu.findItem(R.id.action_cancel).setVisible(false).setEnabled(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_mapnote : {
                if (mOnMarkerProcessIntentListener != null) {
                    mOnMarkerProcessIntentListener.onMarkerAddingIntent(mSelectedMarker.getPosition());
                }
                break;
            }
            case R.id.action_edit : {
                if (mOnMarkerProcessIntentListener != null) {
                    mOnMarkerProcessIntentListener.onMarkerEditingIntent(mMapMarkers.get(mSelectedMarker));
                }
                break;
            }
            case R.id.action_delete : {
                if (mOnMarkerProcessIntentListener != null) {
                    mOnMarkerProcessIntentListener.onMarkerDeletingIntent(mMapMarkers.get(mSelectedMarker));
                }
                break;
            }
            case R.id.action_cancel : {
                deselectMarkers();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isMarkerNew(Marker marker) {
        if (mMapMarkers.containsKey(marker)) {
            return false;
        }
        return true;
    }

    private Marker getMarkerByLatLng (LatLng latLng) {
        for (Marker marker : mMapMarkers.keySet()) {
            if (marker.getPosition().equals(latLng)){
                return marker;
            }
        }
        return null;
    }

    private Marker getMarkerByMapNoteId (long mapNoteId) {
        for (Map.Entry<Marker,Long> entry : mMapMarkers.entrySet()) {
            if (entry.getValue() == mapNoteId) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void selectMarker (Marker marker) {
        if ( ! marker.equals(mSelectedMarker) ) {
            deselectMarkers();
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            mSelectedMarker = marker;
        }
        if (mMenu != null) {
            if (isMarkerNew(mSelectedMarker)) {
                mMenu.findItem(R.id.action_create_mapnote).setVisible(true).setEnabled(true);
                mMenu.findItem(R.id.action_edit).setVisible(false).setEnabled(false);
                mMenu.findItem(R.id.action_delete).setVisible(false).setEnabled(false);
                mMenu.findItem(R.id.action_cancel).setVisible(true).setEnabled(true);
            } else {
                mMenu.findItem(R.id.action_create_mapnote).setVisible(false).setEnabled(false);
                mMenu.findItem(R.id.action_edit).setVisible(true).setEnabled(true);
                mMenu.findItem(R.id.action_delete).setVisible(true).setEnabled(true);
                mMenu.findItem(R.id.action_cancel).setVisible(false).setEnabled(false);
            }
        }

    }

    private void deselectMarkers() {
        if ((mSelectedMarker != null) && ! isMarkerNew(mSelectedMarker)) {
            mSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else {
            if ((mSelectedMarker != null) && isMarkerNew(mSelectedMarker)) {
                mSelectedMarker.remove();
            }
        }
        if (mMenu != null) {
            mMenu.findItem(R.id.action_create_mapnote).setVisible(false).setEnabled(false);
            mMenu.findItem(R.id.action_edit).setVisible(false).setEnabled(false);
            mMenu.findItem(R.id.action_delete).setVisible(false).setEnabled(false);
            mMenu.findItem(R.id.action_cancel).setVisible(false).setEnabled(false);
        }
        mSelectedMarker = null;
    }

    @Override
    public void onMapNoteAdded(MapNote mapNote) {
        LatLng selectedLatLng = mSelectedMarker.getPosition();
        if(selectedLatLng.equals(mapNote.getLatLng())) {
            mMapMarkers.put(mSelectedMarker, mapNote.getId());
            selectMarker(mSelectedMarker);
        }
    }

    @Override
    public void onMapNoteEdited(MapNote mapNote) {
        selectMarker(mSelectedMarker);
    }

    @Override
    public void onMapNoteDeleted(MapNote mapNote) {
        long mapNoteId = mapNote.getId();
        for (Map.Entry<Marker,Long> entry : mMapMarkers.entrySet ()) {
            if (entry.getValue() == mapNoteId) {
                Marker marker = entry.getKey();
                marker.remove();
                mMapMarkers.remove(entry.getKey());
                break;
            }
        }
    }

    private void setCameraOnLatLng (LatLng location) {
        GoogleMap googleMap = getMap();
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(location), CAMERA_ANIMATION_DURATION, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });
        }

    }

    private void setCameraOnMapNote(long mapNoteId) {
        if (getMap() != null) {
            Marker marker = getMarkerByMapNoteId(mapNoteId);
            setCameraOnLatLng(marker.getPosition());
        }
    }

    @Override
    public void onMapNoteListItemSelected(long mapNoteId) {
        selectMarker(getMarkerByMapNoteId(mapNoteId));
        setCameraOnMapNote(mapNoteId);
    }

    /**********************************************************************************************/

    public class NoteInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            View view = mInflater.inflate(R.layout.info_window_mapview, null, false);
            MapNote mapNote = mMapNotesDao.getMapNote(mMapMarkers.get(marker));
            TextView txtInfoWindowTitle = (TextView)view.findViewById(R.id.txtInfoWindowTitle);
            txtInfoWindowTitle.setText(mapNote.getTitle());
            TextView txtInfoWindowNote = (TextView)view.findViewById(R.id.txtInfoWindowNote);
            txtInfoWindowNote.setText(mapNote.getNote());
            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

    }
}