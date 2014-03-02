package net.gylka.mapnotes;

import java.util.List;

public interface NotesListManipulationListenerAdapter extends NotesListFragment.OnNotesListManipulationListener {

    void addOnNotesListManipulationListener (NotesListFragment.OnNotesListManipulationListener listener);

    void removeOnNotesListManipulationListener (NotesListFragment.OnNotesListManipulationListener listener);

    List<NotesListFragment.OnNotesListManipulationListener> getOnNotesListManipulationListeners();

}
