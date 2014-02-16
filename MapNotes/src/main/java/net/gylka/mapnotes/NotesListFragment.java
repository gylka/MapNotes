package net.gylka.mapnotes;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotesListFragment extends Fragment implements MapNotesFragmentRefresher {

    private ListView mNotesListView;
    private MapNotesListAdapter mNotesListAdapter;

    public static NotesListFragment newInstance() {
        NotesListFragment fragment = new NotesListFragment();
        return fragment;
    }
    public NotesListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_list, container, false);
        mNotesListView = (ListView) view.findViewById(R.id.listMapNotes);
        mNotesListAdapter = new MapNotesListAdapter(getActivity().getApplicationContext(), R.layout.list_mapnote, getAllMapNotes());
        mNotesListView.setAdapter(mNotesListAdapter);
        return view;
    }

    @Override
    public void refreshFragment() {
        Log.d("refreshList", "Before refresh = " + mNotesListAdapter.getCount());
        mNotesListAdapter.clear();
        ArrayList<MapNote> mapNotes = getAllMapNotes();
        mNotesListAdapter.addAll(mapNotes);
        Log.d("refreshList", "After refresh = " + mNotesListAdapter.getCount());
        mNotesListAdapter.notifyDataSetChanged();
    }

    private ArrayList<MapNote> getAllMapNotes() {
        MapNotesDao mapNotesDao = new MapNotesDaoImpl(getActivity());
        return mapNotesDao.getAllNotes();
    }

    public static class MapNotesListAdapter extends ArrayAdapter<MapNote> {

        private Context mContext;

        public MapNotesListAdapter(Context context, int resource, ArrayList<MapNote> objects) {
            super(context, resource, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_mapnote, parent, false);

            TextView txtMapNoteId = (TextView) view.findViewById(R.id.txtMapNoteId);
            txtMapNoteId.setText(Long.toString(getItem(position).getId()));
            TextView txtMapNoteTitle = (TextView) view.findViewById(R.id.txtMapNoteTitle);
            txtMapNoteTitle.setText(getItem(position).getTitle());
            TextView txtMapNoteDescription = (TextView) view.findViewById(R.id.txtMapNoteDescription);
            txtMapNoteDescription.setText(getItem(position).getNote());
            TextView txtMapNoteLatitude = (TextView) view.findViewById(R.id.txtMapNoteLatitude);
            txtMapNoteLatitude.setText(Double.toString(getItem(position).getLatLng().latitude));
            TextView txtMapNoteLongtitude = (TextView) view.findViewById(R.id.txtMapNoteLongtitude);
            txtMapNoteLongtitude.setText(Double.toString(getItem(position).getLatLng().longitude));

            return view;
        }

    }
}
