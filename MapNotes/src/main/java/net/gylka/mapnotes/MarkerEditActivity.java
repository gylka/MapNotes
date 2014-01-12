package net.gylka.mapnotes;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class MarkerEditActivity extends BaseActivity {

    public static final String REQUEST_KEY = "RequestKey";

    public static final int REQUEST_ADD_MARKER = 100;
    public static final int REQUEST_EDIT_MARKER = 101;

    public static final int RESULT_MARKER_ADDED = 200;
    public static final int RESULT_CANCELED = 201;
    public static final int RESULT_ERROR = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_edit);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private MapNote mCurrentMapNote;
        private int mRequestCode;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_marker_edit, container, false);

            mCurrentMapNote = new MapNote();
            Intent intent = getActivity().getIntent();
            mCurrentMapNote.setLatLng(new LatLng(intent.getDoubleExtra(PACKAGE_NAME + MapNote.LATITUDE_KEY, 0), intent.getDoubleExtra(PACKAGE_NAME + MapNote.LONGTITUDE_KEY, 0)));
            mCurrentMapNote.setTitle(intent.getStringExtra(PACKAGE_NAME + MapNote.TITLE_KEY));
            mCurrentMapNote.setNote(intent.getStringExtra(PACKAGE_NAME + MapNote.NOTE_KEY));
            mRequestCode = intent.getIntExtra(PACKAGE_NAME + REQUEST_KEY, 0);

            ((TextView)rootView.findViewById(R.id.lblLatitude)).setText(new Double(mCurrentMapNote.getLatLng().latitude).toString());
            ((TextView)rootView.findViewById(R.id.lblLongtitude)).setText(new Double(mCurrentMapNote.getLatLng().longitude).toString());
            final EditText edtMarkerTitle = (EditText)rootView.findViewById(R.id.edtMarkerTitle);
            edtMarkerTitle.setText(mCurrentMapNote.getTitle());
            final EditText edtMarkerNote = (EditText)rootView.findViewById(R.id.edtMarkerNote);
            edtMarkerNote.setText(mCurrentMapNote.getNote());

            Button btnMarkerEditCancel = (Button)rootView.findViewById(R.id.btnMarkerEditCancel);
            btnMarkerEditCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    getActivity().setResult(RESULT_CANCELED, new Intent());
                    getActivity().finish();
                }
            });

            Button btnMarkerEditCreate = (Button)rootView.findViewById(R.id.btnMakerEditCreate);
            btnMarkerEditCreate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    MapNotesDao mapNotesDao = new MapNotesDaoImpl(getActivity().getApplicationContext());
                    mCurrentMapNote.setTitle(edtMarkerTitle.getText().toString());
                    mCurrentMapNote.setNote(edtMarkerNote.getText().toString());
                    if (mRequestCode == REQUEST_ADD_MARKER) {
                        if (mapNotesDao.addNote(mCurrentMapNote)) {
                            Intent intent = new Intent();
                            intent.putExtra(PACKAGE_NAME + MapNote.LATITUDE_KEY, mCurrentMapNote.getLatLng().latitude);
                            intent.putExtra(PACKAGE_NAME + MapNote.LONGTITUDE_KEY, mCurrentMapNote.getLatLng().longitude);
                            intent.putExtra(PACKAGE_NAME + MapNote.TITLE_KEY, mCurrentMapNote.getTitle());
                            intent.putExtra(PACKAGE_NAME + MapNote.NOTE_KEY, mCurrentMapNote.getNote());
                            getActivity().setResult(RESULT_MARKER_ADDED, intent);
                        } else {
                            getActivity().setResult(RESULT_ERROR);
                        }
                        getActivity().finish();
                    } else {
                        //TODO update mapNote
                    }
                }
            });

            return rootView;
        }
    }

}
