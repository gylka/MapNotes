package net.gylka.mapnotes;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public static final int RESULT_EDITED_SUCCESSFULLY = 202;
    public static final int RESULT_MARKER_DELETED = 203;
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
        private MapNotesDao mMapNotesDao;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_marker_edit, container, false);

            mCurrentMapNote = new MapNote();
            mMapNotesDao = new MapNotesDaoImpl(getActivity().getApplicationContext());
            Intent intent = getActivity().getIntent();
            mRequestCode = intent.getIntExtra(REQUEST_KEY, 0);
            switch (mRequestCode) {
                case REQUEST_ADD_MARKER: {
                    mCurrentMapNote.setLatLng((LatLng) intent.getParcelableExtra(MapNote.LAT_LNG_KEY));
                    break;
                }
                case REQUEST_EDIT_MARKER: {
                    mCurrentMapNote = mMapNotesDao.getMapNote(intent.getLongExtra(MapNote.ID_KEY, -1));
                    break;
                }
            }

            ((TextView)rootView.findViewById(R.id.lblLatitude)).setText(Location.convert(mCurrentMapNote.getLatLng().latitude, Location.FORMAT_SECONDS));
            ((TextView)rootView.findViewById(R.id.lblLongtitude)).setText(Location.convert(mCurrentMapNote.getLatLng().longitude, Location.FORMAT_SECONDS));
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

            final Button btnMarkerEditCreate = (Button)rootView.findViewById(R.id.btnMakerEditCreate);
            if (mRequestCode == REQUEST_ADD_MARKER) {
                btnMarkerEditCreate.setText(R.string.label_create_map_note);
            }
            if (mRequestCode == REQUEST_EDIT_MARKER) {
                btnMarkerEditCreate.setText(R.string.label_edit_map_note);
            }
            // Checking if Marker's Title is empty. If empty - disable "Create/Edit" button, else - enable
            // Additionaly listening Title's EditText changing
            if (edtMarkerTitle.getText().length() > 0) {
                btnMarkerEditCreate.setEnabled(true);
            } else {
                btnMarkerEditCreate.setEnabled(false);
            }
            edtMarkerTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        btnMarkerEditCreate.setEnabled(true);
                    } else {
                        btnMarkerEditCreate.setEnabled(false);
                    }

                }
            });

            btnMarkerEditCreate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mCurrentMapNote.setTitle(edtMarkerTitle.getText().toString());
                    mCurrentMapNote.setNote(edtMarkerNote.getText().toString());
                    switch (mRequestCode) {
                        case REQUEST_ADD_MARKER: {
                            mCurrentMapNote.setId(mMapNotesDao.addNote(mCurrentMapNote));
                            Intent intent = new Intent();
                            intent.putExtra(MapNote.MAP_NOTE_KEY, mCurrentMapNote);
                            getActivity().setResult(RESULT_MARKER_ADDED, intent);
                            getActivity().finish();
                            break;
                        }
                        case REQUEST_EDIT_MARKER: {
                            if (mMapNotesDao.updateMapNote(mCurrentMapNote.getId(), mCurrentMapNote)) {
                                Intent intent = new Intent();
                                intent.putExtra(MapNote.MAP_NOTE_KEY, mCurrentMapNote);
                                getActivity().setResult(RESULT_EDITED_SUCCESSFULLY, intent);
                            } else {
                                getActivity().setResult(RESULT_ERROR);
                            }
                            getActivity().finish();
                            break;
                        }
                    }
                }
            });

            return rootView;
        }
    }

}
