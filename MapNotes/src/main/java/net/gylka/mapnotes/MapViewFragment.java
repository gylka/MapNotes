package net.gylka.mapnotes;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    private Marker mSelectedMarker;

    private List<MapNote> mMapNotes;
    private MapNotesDao mMapNotesDao;
    private Map<Marker, Long> mMapMarkers;

    private Menu mMenu;
    private OnMarkerProcessIntentListener mMarkerProcessIntentListener;
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
        Log.d("MapViewFragment", "onAtach");
        try {
            mMarkerProcessIntentListener = (OnMarkerProcessIntentListener) activity;
            mMarkerProcessIntentListener.AddOnMapNoteManipulationListener(this);
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
        super.onDetach();
        Log.d("MapViewFragment", "onDetach");
        if (mMarkerProcessIntentListener != null) {
            mMarkerProcessIntentListener.RemoveOnMapNoteManipulationListener(this);
            mMarkerProcessIntentListener = null;
        }
        if (mNotesListManipulationListenerAdapter != null) {
            mNotesListManipulationListenerAdapter.removeOnNotesListManipulationListener(this);
            mNotesListManipulationListenerAdapter = null;
        }

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
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        final GoogleMap googleMap = getMap();
        if (googleMap != null) {
            for (MapNote mapNote : mMapNotes) {
                Marker marker = googleMap.addMarker(new MarkerOptions().position(mapNote.getLatLng()));
                mMapMarkers.put(marker, mapNote.getId());
            }

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
                    selectMarker(marker);
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
                if (mMarkerProcessIntentListener != null) {
                    mMarkerProcessIntentListener.onMarkerAddingIntent(mSelectedMarker.getPosition());
                }
                break;
            }
            case R.id.action_edit : {
                if (mMarkerProcessIntentListener != null) {
                    mMarkerProcessIntentListener.onMarkerEditingIntent(mMapMarkers.get(mSelectedMarker));
                }
                break;
            }
            case R.id.action_delete : {
                if (mMarkerProcessIntentListener != null) {
                    mMarkerProcessIntentListener.onMarkerDeletingIntent(mMapMarkers.get(mSelectedMarker));
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
        if (marker != mSelectedMarker) {
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

    private void setCameraOnMapNote(long mapNoteId) {
        GoogleMap googleMap = getMap();
        if (googleMap != null) {
            Marker marker = getMarkerByMapNoteId(mapNoteId);
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), CAMERA_ANIMATION_DURATION, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });

        }
    }

    @Override
    public void onMapNoteListItemSelected(long mapNoteId) {
        selectMarker(getMarkerByMapNoteId(mapNoteId));
        setCameraOnMapNote(mapNoteId);
    }

}