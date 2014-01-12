package net.gylka.mapnotes;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapViewActivity extends BaseActivity {

    private MapNote mCurrentMapNote;
    private Marker mCurrentMarker;
    private Menu mMenu;
    private Map<Marker, Long> mMapMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        SupportMapFragment supportMapFragment = (SupportMapFragment) fragment;
        final GoogleMap googleMap = supportMapFragment.getMap();

 /*       Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "sadkjhgfkjdhfg", Toast.LENGTH_SHORT).show();
                NotesDbOpenHelper.copyDatabaseToExtSDCardDownloads(getApplicationContext());

            }
        });
*/
        ArrayList<MapNote> mapNotes = mMapNotesDao.getAllNotes();
        mMapMarkers = new HashMap<Marker, Long>();
        for (MapNote mapNote : mapNotes) {
            Marker marker = googleMap.addMarker(new MarkerOptions().position(mapNote.getLatLng()));
            mMapMarkers.put(marker, mapNote.getId());
        }

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                updateMapMarkers();

                mCurrentMapNote = new MapNote();
                mCurrentMapNote.setLatLng(latLng);
                mCurrentMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                MenuItem actionCreate = mMenu.findItem(R.id.action_create_mapnote);
                actionCreate.setVisible(true);
                actionCreate.setEnabled(true);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                updateMapMarkers();
                MapNote mapNote = mMapNotesDao.getMapNote(mMapMarkers.get(marker));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                mCurrentMarker = marker;
                mCurrentMapNote = mapNote;

                MenuItem actionEdit = mMenu.findItem(R.id.action_edit);
                actionEdit.setVisible(true);
                actionEdit.setEnabled(true);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_view, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_mapnote : {
                Intent intent = new Intent(this, MarkerEditActivity.class);
                intent.putExtra(this.PACKAGE_NAME + MapNote.LATITUDE_KEY, mCurrentMapNote.getLatLng().latitude);
                intent.putExtra(this.PACKAGE_NAME + MapNote.LONGTITUDE_KEY, mCurrentMapNote.getLatLng().longitude);
                intent.putExtra(this.PACKAGE_NAME + MarkerEditActivity.REQUEST_KEY, MarkerEditActivity.REQUEST_ADD_MARKER);
                startActivityForResult(intent, MarkerEditActivity.REQUEST_ADD_MARKER);
                return true;
            }
            case R.id.action_edit : {
                Intent intent = new Intent(this, MarkerEditActivity.class);
                intent.putExtra(this.PACKAGE_NAME + MapNote.ID_KEY, mCurrentMapNote.getId());
                intent.putExtra(this.PACKAGE_NAME + MarkerEditActivity.REQUEST_KEY, MarkerEditActivity.REQUEST_EDIT_MARKER);
                startActivityForResult(intent, MarkerEditActivity.REQUEST_EDIT_MARKER);
                return true;
            }


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MarkerEditActivity.RESULT_ERROR) {
            Toast.makeText(getApplicationContext(), "Some error happened during creating/editing operation", Toast.LENGTH_SHORT).show();
        }
        switch (requestCode) {
            case MarkerEditActivity.REQUEST_ADD_MARKER : {
                if (resultCode == MarkerEditActivity.RESULT_CANCELED) {
                }
                if (resultCode == MarkerEditActivity.RESULT_MARKER_ADDED) {
                    mMapMarkers.put(mCurrentMarker, data.getLongExtra(MapNote.ID_KEY, -1));
                }
                break;
            }
            case MarkerEditActivity.REQUEST_EDIT_MARKER : {
                break;
            }
        }
        updateMapMarkers();
    }

    private void updateMapMarkers() {
        if ((mCurrentMarker != null) && mMapMarkers.containsKey(mCurrentMarker)) {
            mCurrentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        if ((mCurrentMarker != null) && ! mMapMarkers.containsKey(mCurrentMarker)) {
            mCurrentMarker.remove();
        }
        MenuItem actionCreate = mMenu.findItem(R.id.action_create_mapnote);
        actionCreate.setVisible(false);
        actionCreate.setEnabled(false);
        MenuItem actionEdit = mMenu.findItem(R.id.action_edit);
        actionEdit.setVisible(false);
        actionEdit.setEnabled(false);

    }

}
