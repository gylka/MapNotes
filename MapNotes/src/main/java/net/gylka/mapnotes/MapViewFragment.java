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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapViewFragment extends SupportMapFragment implements MapNotesFragmentRefresher {

    private MapNote mSelectedMapNote;
    private Marker mSelectedMarker;

    private List<MapNote> mMapNotes;
    private MapNotesDao mMapNotesDao;
    private Map<Marker, Long> mMapMarkers;

    private Menu mMenu;
    private OnMarkerEditListener mOnMarkerEditListener;

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
            mOnMarkerEditListener = (OnMarkerEditListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMarkerEditListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnMarkerEditListener = null;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_view_fragment, menu);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_mapnote : {
                if (mOnMarkerEditListener != null) {
                    mOnMarkerEditListener.onMarkerAddingIntent(mSelectedMapNote.getLatLng());
                }
                break;
            }
            case R.id.action_edit : {
                if (mOnMarkerEditListener != null) {
                    mOnMarkerEditListener.onMarkerEditingIntent(mSelectedMapNote.getId());
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        final GoogleMap googleMap = getMap();
        if (googleMap != null) {
            for (MapNote mapNote : mMapNotes) {
                Marker marker = googleMap.addMarker(new MarkerOptions().position(mapNote.getLatLng()));
                mMapMarkers.put(marker, mapNote.getId());
            }

            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    updateMapMarkers();
                    mSelectedMapNote = new MapNote();
                    mSelectedMapNote.setLatLng(latLng);
                    mSelectedMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    MenuItem actionCreate = mMenu.findItem(R.id.action_create_mapnote);
                    actionCreate.setVisible(true);
                    actionCreate.setEnabled(true);
                }
            });

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (! isMarkerIsNew(marker)) {
                        updateMapMarkers();
                        MapNote mapNote = mMapNotesDao.getMapNote(mMapMarkers.get(marker));
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                        mSelectedMarker = marker;
                        mSelectedMapNote = mapNote;

                        MenuItem actionEdit = mMenu.findItem(R.id.action_edit);
                        actionEdit.setVisible(true);
                        actionEdit.setEnabled(true);
                    }
                    return true;
                }
            });

        }
    }

    private void hideMapViewMenuItems() {
        if (mMenu != null) {
            MenuItem actionCreate = mMenu.findItem(R.id.action_create_mapnote);
            actionCreate.setVisible(false);
            actionCreate.setEnabled(false);
            MenuItem actionEdit = mMenu.findItem(R.id.action_edit);
            actionEdit.setVisible(false);
            actionEdit.setEnabled(false);
        }
    }

    private boolean isMarkerIsNew(Marker marker) {
        if (mMapMarkers.containsKey(marker)) {
            return false;
        }
        return true;
    }

    private void updateMapMarkers() {
        if ((mSelectedMarker != null) && ! isMarkerIsNew(mSelectedMarker)) {
            mSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        if ((mSelectedMarker != null) && isMarkerIsNew(mSelectedMarker)) {
            mSelectedMarker.remove();
        }
        hideMapViewMenuItems();
    }

    @Override
    public void refreshFragment() {
        updateMapMarkers();
    }

    public void onMarkerAdded(long mapNoteId) {
        mMapMarkers.put(mSelectedMarker, mapNoteId);
        updateMapMarkers();
    }

    public void onMarkerEdited() {
        updateMapMarkers();
    }

    public void onMarkerAddOrEditCanceled() {
        updateMapMarkers();
    }

    public interface OnMarkerEditListener {

        public void onMarkerAddingIntent(LatLng latLng);

        public void onMarkerEditingIntent(long mapNoteId);
    }

}