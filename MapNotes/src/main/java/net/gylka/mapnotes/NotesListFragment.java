package net.gylka.mapnotes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends Fragment implements OnMapNoteManipulationListener {

    private ListView mNotesListView;
    private MapNotesListAdapter mNotesListAdapter;
    private OnMarkerProcessIntentListener mMarkerProcessIntentListener;
    private NotesListManipulationListenerAdapter mNotesListManipulationListenerAdapter;

    public static NotesListFragment newInstance() {
        NotesListFragment fragment = new NotesListFragment();
        return fragment;
    }
    public NotesListFragment() {
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("NotesListFragment", "OnAtach");
        try {
            mMarkerProcessIntentListener = (OnMarkerProcessIntentListener) activity;
            mMarkerProcessIntentListener.AddOnMapNoteManipulationListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMarkerProcessIntentListener");
        }
        if (activity instanceof NotesListManipulationListenerAdapter) {
            mNotesListManipulationListenerAdapter = (NotesListManipulationListenerAdapter)activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("NotesListFragment", "OnDetach");
        mMarkerProcessIntentListener.RemoveOnMapNoteManipulationListener(this);
        mMarkerProcessIntentListener = null;
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
//        mNotesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MapNote mapNote = mNotesListAdapter.getItem(position);
                for (OnNotesListManipulationListener listener : mNotesListManipulationListenerAdapter.getOnNotesListManipulationListeners()) {
                    listener.onMapNoteListItemSelected(mapNote.getId());
                }
            }
        });
        return view;
    }

    private ArrayList<MapNote> getAllMapNotes() {
        MapNotesDao mapNotesDao = new MapNotesDaoImpl(getActivity());
        return mapNotesDao.getAllNotes();
    }

    @Override
    public void onMapNoteAdded(MapNote mapNote) {
        mNotesListAdapter.add(mapNote);
        mNotesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMapNoteEdited(MapNote mapNote) {
        int mapNotesListIndex = mNotesListAdapter.getItemIndexByMapNoteId(mapNote.getId());
        mNotesListAdapter.getItem(mapNotesListIndex).updateMapNoteSavingId(mapNote);
        mNotesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMapNoteDeleted(MapNote mapNote) {
        mNotesListAdapter.removeItemByMapNoteId(mapNote.getId());
        mNotesListAdapter.notifyDataSetChanged();
    }

    /** ********************************************************************************************
     */
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

        private int getItemIndexByMapNoteId(long mapNoteId) {
            for (int i=0; i < getCount(); i++) {
                if (getItem(i).getId() == mapNoteId) {
                    return i;
                }
            }
            return -1;
        }

        private void removeItemByMapNoteId(long mapNoteId) {
            for (int i=0; i < getCount(); i++) {
                if (getItem(i).getId() == mapNoteId) {
                    remove(getItem(i));
                }
            }
        }

    }

    /** ********************************************************************************************
     */
    public interface OnNotesListManipulationListener {

        void onMapNoteListItemSelected(long mapNoteId);

    }

}
