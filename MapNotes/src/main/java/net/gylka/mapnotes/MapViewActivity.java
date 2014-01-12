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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapViewActivity extends BaseActivity {

    private MapNote mCurrentMapNote;
    private Marker mCurrentMarker;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

/*        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/

        FragmentManager fragmentManager = getSupportFragmentManager();
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
        for (MapNote mapNote : mapNotes) {
            googleMap.addMarker(new MarkerOptions().position(mapNote.getLatLng()));
        }

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                if (! mMapNotesDao.isMapNoteAlreadyInTable(latLng)) {
                    mCurrentMapNote = new MapNote();
                    mCurrentMapNote.setLatLng(latLng);
                    mCurrentMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    MenuItem actionNext = mMenu.findItem(R.id.action_next);
                    actionNext.setVisible(true);
                    actionNext.setEnabled(true);
                }
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_next : {
                Intent intent = new Intent(this, MarkerEditActivity.class);
                intent.putExtra(this.PACKAGE_NAME + MapNote.LATITUDE_KEY, mCurrentMapNote.getLatLng().latitude);
                intent.putExtra(this.PACKAGE_NAME + MapNote.LONGTITUDE_KEY, mCurrentMapNote.getLatLng().longitude);
                intent.putExtra(this.PACKAGE_NAME + MarkerEditActivity.REQUEST_KEY, MarkerEditActivity.REQUEST_ADD_MARKER);
                startActivityForResult(intent, MarkerEditActivity.REQUEST_ADD_MARKER);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MarkerEditActivity.REQUEST_ADD_MARKER : {
                if (resultCode == MarkerEditActivity.RESULT_CANCELED) {
                    mCurrentMarker.remove();
                }
                if (resultCode == MarkerEditActivity.RESULT_MARKER_ADDED) {
                    mCurrentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            }
            //TODO add case for editing marker
        }
        MenuItem actionNext = mMenu.findItem(R.id.action_next);
        actionNext.setVisible(false);
        actionNext.setEnabled(false);


    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
            return rootView;
        }
    }

}
