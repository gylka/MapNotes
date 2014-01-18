package net.gylka.mapnotes;

import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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
import java.util.Locale;
import java.util.Map;


public class MapViewActivity extends BaseActivity implements ActionBar.TabListener, NotesListFragment.OnFragmentInteractionListener {

    private MapNote mCurrentMapNote;
    private Marker mCurrentMarker;
    private Menu mMenu;
    private Map<Marker, Long> mMapMarkers;

    private MapViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private SupportMapFragment mMapFragment;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPagerAdapter = new MapViewPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i=0; i < mViewPagerAdapter.getCount() ; i++) {
            mActionBar.addTab(mActionBar.newTab().setText(mViewPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

/*
        mMapFragment = (SupportMapFragment) mViewPagerAdapter.getItem(MapViewPagerAdapter.MAP_VIEW_FRAGMENT_INDEX);
        final GoogleMap googleMap = mMapFragment.getMap();
        ArrayList<MapNote> mapNotes = mMapNotesDao.getAllNotes();
        mMapMarkers = new HashMap<Marker, Long>();
        for (MapNote mapNote : mapNotes) {
            Marker marker = googleMap.addMarker(new MarkerOptions().position(mapNote.getLatLng()));
            mMapMarkers.put(marker, mapNote.getId());
        }
*/

/*
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
*/


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
                break;
            }
            case R.id.action_edit : {
                Intent intent = new Intent(this, MarkerEditActivity.class);
                intent.putExtra(this.PACKAGE_NAME + MapNote.ID_KEY, mCurrentMapNote.getId());
                intent.putExtra(this.PACKAGE_NAME + MarkerEditActivity.REQUEST_KEY, MarkerEditActivity.REQUEST_EDIT_MARKER);
                startActivityForResult(intent, MarkerEditActivity.REQUEST_EDIT_MARKER);
                break;
            }
            case R.id.action_copy_db : {
                NotesDbOpenHelper.copyDatabaseToExtSDCardDownloads(getApplicationContext());
                Toast.makeText(getApplicationContext(), "DB copied to Downloads directory", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public class MapViewPagerAdapter extends FragmentPagerAdapter {

        public static final int MAP_VIEW_FRAGMENT_INDEX = 0;
        public static final int NOTES_LIST_FRAGMENT_INDEX = 1;

        public static final int NUMBER_OF_PAGES = 2;

        private SupportMapFragment mMapFragment;
        private NotesListFragment mNotesListFragment;

        public MapViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.mMapFragment = SupportMapFragment.newInstance();
            GoogleMap googleMap = mMapFragment.getMap();
            googleMap.addMarker((new MarkerOptions()).position(new LatLng(0,0)));
            mNotesListFragment = NotesListFragment.newInstance("aa","bb");
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case MAP_VIEW_FRAGMENT_INDEX : {
                    return mMapFragment;
                }
                case NOTES_LIST_FRAGMENT_INDEX : {
                    return mNotesListFragment;
                }
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case MAP_VIEW_FRAGMENT_INDEX :
                    return getString(R.string.tab_map_view).toUpperCase(Locale.getDefault());
                case NOTES_LIST_FRAGMENT_INDEX :
                    return getString(R.string.tab_notes_list).toUpperCase(Locale.getDefault());
            }
            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }
    }

}
