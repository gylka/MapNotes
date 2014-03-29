package net.gylka.mapnotes;

import com.google.android.gms.maps.model.LatLng;

public interface OnMarkerProcessIntentListener {

    void onMarkerAddingIntent(LatLng latLng);

    void onMarkerEditingIntent(long mapNoteId);

    void onMarkerDeletingIntent(long mapNoteId);

    void onMarkerAdditionalInfoIntent(long mapNoteId);

    void AddOnMapNoteManipulationListener(OnMapNoteManipulationListener onMapNoteManipulationListener);

    void RemoveOnMapNoteManipulationListener(OnMapNoteManipulationListener onMapNoteManipulationListener);

}
