package net.gylka.mapnotes;

public interface OnMapNoteManipulationListener {

    void onMapNoteAdded(MapNote mapNote);

    void onMapNoteEdited(MapNote mapNote);

    void onMapNoteDeleted(MapNote mapNote);

}
