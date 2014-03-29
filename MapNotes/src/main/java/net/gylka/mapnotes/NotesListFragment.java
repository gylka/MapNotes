package net.gylka.mapnotes;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class NotesListFragment extends Fragment implements OnMapNoteManipulationListener {

    private MapNotesListAdapter mNotesListAdapter;
    private OnMarkerProcessIntentListener mMarkerProcessIntentListener;
    private NotesListManipulationListenerAdapter mNotesListManipulationListenerAdapter;

    public static NotesListFragment newInstance() {
        return new NotesListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_list, container, false);
        ListView notesListView = (ListView) view.findViewById(R.id.listMapNotes);
        mNotesListAdapter = new MapNotesListAdapter(getActivity().getApplicationContext(), R.layout.list_mapnote, getAllMapNotes());
        notesListView.setAdapter(mNotesListAdapter);
        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            NotesListItemViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_mapnote, parent, false);
                viewHolder = new NotesListItemViewHolder();
                viewHolder.txtMapNoteTitle = (TextView) convertView.findViewById(R.id.txtMapNoteTitle);
                viewHolder.txtMapNoteDescription = (TextView) convertView.findViewById(R.id.txtMapNoteDescription);
                viewHolder.txtMapNoteLatitude = (TextView) convertView.findViewById(R.id.txtMapNoteLatitude);
                viewHolder.txtMapNoteLongtitude = (TextView) convertView.findViewById(R.id.txtMapNoteLongtitude);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (NotesListItemViewHolder) convertView.getTag();
            }
            viewHolder.txtMapNoteTitle.setText(getItem(position).getTitle());
            viewHolder.txtMapNoteDescription.setText(getItem(position).getNote());
            viewHolder.txtMapNoteLatitude.setText(Location.convert(getItem(position).getLatLng().latitude, Location.FORMAT_SECONDS));
            viewHolder.txtMapNoteLongtitude.setText(Location.convert(getItem(position).getLatLng().longitude, Location.FORMAT_SECONDS));
            return convertView;
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

        /** ********************************************************************************************
         */

        private static class NotesListItemViewHolder {
            TextView txtMapNoteTitle;
            TextView txtMapNoteDescription;
            TextView txtMapNoteLatitude;
            TextView txtMapNoteLongtitude;
        }
    }

    /** ********************************************************************************************
     */
    public interface OnNotesListManipulationListener {

        void onMapNoteListItemSelected(long mapNoteId);

    }

}
