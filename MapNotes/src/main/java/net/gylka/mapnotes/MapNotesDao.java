package net.gylka.mapnotes;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;

public interface MapNotesDao {

    boolean addNote (MapNote mapNote);

    ArrayList<MapNote> getAllNotes();

    boolean isMapNoteAlreadyInTable (MapNote mapNote);

    boolean isMapNoteAlreadyInTable (LatLng latLng);

}
