package net.gylka.mapnotes;

import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapViewActivity extends BaseActivity implements ActionBar.TabListener, OnMarkerProcessIntentListener,
        NotesListManipulationListenerAdapter {

    private Menu mMenu;

    private ActionBar mActionBar;
    private MapNotesPagerAdapter mMapNotesPagerAdapter;
    private int mCurrentViewPagerItem;
    private List<OnMapNoteManipulationListener> mMapNoteManipulationListeners;
    private List<NotesListFragment.OnNotesListManipulationListener> mOnNotesListManipulationListeners;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MapNotesPagerAdapter.MAP_NOTES_PAGER_CURRENT_FRAGMENT_KEY, mCurrentViewPagerItem);
        Log.d("Putting outState: ", "" + mCurrentViewPagerItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mMapNoteManipulationListeners = new ArrayList<OnMapNoteManipulationListener>();
        mOnNotesListManipulationListeners = new ArrayList<NotesListFragment.OnNotesListManipulationListener>();
        mOnNotesListManipulationListeners.add(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setHomeButtonEnabled(false);
        mMapNotesPagerAdapter = new MapNotesPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        viewPager.setAdapter(mMapNotesPagerAdapter);

        // Removing gestures (swiping included) on ViewPager
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                mActionBar.setSelectedNavigationItem(i);
                mCurrentViewPagerItem = i;
            }
        });

        for (int i=0; i < mMapNotesPagerAdapter.getCount() ; i++) {
            mActionBar.addTab(mActionBar.newTab().setText(mMapNotesPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

        if (savedInstanceState != null) {
            mCurrentViewPagerItem = savedInstanceState.getInt(MapNotesPagerAdapter.MAP_NOTES_PAGER_CURRENT_FRAGMENT_KEY);
            viewPager.setCurrentItem(mCurrentViewPagerItem);
            mActionBar.setSelectedNavigationItem(mCurrentViewPagerItem);
        }
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
            case R.id.action_copy_db : {
                NotesDbOpenHelper.copyDatabaseToExtSDCardDownloads(getApplicationContext());
                //TODO : remove this comment
                //mMapNotesDao.deleteAllMapNotes();
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
                if (resultCode == MarkerEditActivity.RESULT_MARKER_ADDED) {
                    for(OnMapNoteManipulationListener mapNoteManipulationListener : mMapNoteManipulationListeners) {
                        MapNote mapNote = data.getParcelableExtra(MapNote.MAP_NOTE_KEY);
                        mapNoteManipulationListener.onMapNoteAdded(mapNote);
                    }
                }
                break;
            }
            case MarkerEditActivity.REQUEST_EDIT_MARKER : {
                if (resultCode == MarkerEditActivity.RESULT_EDITED_SUCCESSFULLY) {
                    for(OnMapNoteManipulationListener mapNoteManipulationListener : mMapNoteManipulationListeners) {
                        MapNote mapNote = data.getParcelableExtra(MapNote.MAP_NOTE_KEY);
                        mapNoteManipulationListener.onMapNoteEdited(mapNote);
                    }

                }
                break;
            }
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        int position = tab.getPosition();
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onMarkerAddingIntent(LatLng latLng) {
        Intent intent = new Intent(this, MarkerEditActivity.class);
        intent.putExtra(MapNote.LAT_LNG_KEY, latLng);
        intent.putExtra(MarkerEditActivity.REQUEST_KEY, MarkerEditActivity.REQUEST_ADD_MARKER);
        startActivityForResult(intent, MarkerEditActivity.REQUEST_ADD_MARKER);
    }

    @Override
    public void onMarkerEditingIntent(long mapNoteId) {
        Intent intent = new Intent(this, MarkerEditActivity.class);
        intent.putExtra(MapNote.ID_KEY, mapNoteId);
        intent.putExtra(MarkerEditActivity.REQUEST_KEY, MarkerEditActivity.REQUEST_EDIT_MARKER);
        startActivityForResult(intent, MarkerEditActivity.REQUEST_EDIT_MARKER);
    }

    @Override
    public void onMarkerDeletingIntent(long mapNoteId) {
        MapNote mapNote = mMapNotesDao.getMapNote(mapNoteId);
        if (mMapNotesDao.deleteMapNote(mapNoteId)) {
            for(OnMapNoteManipulationListener onMapNoteManipulationListener : mMapNoteManipulationListeners) {
                onMapNoteManipulationListener.onMapNoteDeleted(mapNote);
            }
        };
    }

    @Override
    public void onMarkerAdditionalInfoIntent(long mapNoteId) {

    }

    @Override
    public void AddOnMapNoteManipulationListener(OnMapNoteManipulationListener onMapNoteManipulationListener) {
        mMapNoteManipulationListeners.add(onMapNoteManipulationListener);
    }

    @Override
    public void RemoveOnMapNoteManipulationListener(OnMapNoteManipulationListener onMapNoteManipulationListener) {
        mMapNoteManipulationListeners.remove(onMapNoteManipulationListener);
    }

    @Override
    public void addOnNotesListManipulationListener(NotesListFragment.OnNotesListManipulationListener listener) {
        mOnNotesListManipulationListeners.add(listener);
    }

    @Override
    public void removeOnNotesListManipulationListener(NotesListFragment.OnNotesListManipulationListener listener) {
        mOnNotesListManipulationListeners.remove(listener);
    }

    @Override
    public List<NotesListFragment.OnNotesListManipulationListener> getOnNotesListManipulationListeners() {
        return mOnNotesListManipulationListeners;
    }

    @Override
    public void onMapNoteListItemSelected(long mapNoteId) {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        viewPager.setCurrentItem(MapNotesPagerAdapter.MAP_VIEW_FRAGMENT_INDEX);
    }

    /** ********************************************************************************************
     */
    public class MapNotesPagerAdapter extends FragmentPagerAdapter {

        public static final String MAP_NOTES_PAGER_CURRENT_FRAGMENT_KEY = "MapNotesPagerCurrentFragment";
        public static final int MAP_VIEW_FRAGMENT_INDEX = 0;
        public static final int NOTES_LIST_FRAGMENT_INDEX = 1;

        public static final int NUMBER_OF_PAGES = 2;

        private MapViewFragment mMapFragment;
        private NotesListFragment mNotesListFragment;

        public MapNotesPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case MAP_VIEW_FRAGMENT_INDEX : {
                    if (mMapFragment == null) {
                        mMapFragment = MapViewFragment.newInstance();
                    }
                    return mMapFragment;
                }
                case NOTES_LIST_FRAGMENT_INDEX : {
                    if (mNotesListFragment == null) {
                        mNotesListFragment = NotesListFragment.newInstance();
                    }
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


