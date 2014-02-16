package net.gylka.mapnotes;

import java.util.ArrayList;

public interface MapNotesDao {

    long addNote (MapNote mapNote);

    MapNote getMapNote(long id);

    ArrayList<MapNote> getAllNotes();

    boolean updateMapNote(long mapNoteId, MapNote mapNote);

    boolean isMapNoteAlreadyInTable (MapNote mapNote);

    boolean isMapNoteAlreadyInTable (long id);

    boolean deleteMapNote(long id);

    boolean deleteAllMapNotes();

}
