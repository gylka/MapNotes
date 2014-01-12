package net.gylka.mapnotes;

import com.google.android.gms.maps.model.LatLng;

public class MapNote {

    public static final String LATITUDE_KEY = "Latitude";
    public static final String LONGTITUDE_KEY = "Longtitude";
    public static final String TITLE_KEY = "Title";
    public static final String NOTE_KEY = "Note";

    private LatLng mLatLng;
    private String mTitle;
    private String mNote;

    public MapNote() {
        mLatLng = new LatLng(0,0);
        mTitle = "";
        mNote = "";
    }

    public LatLng getLatLng() { return mLatLng; }

    public void setLatLng(LatLng latLng) { this.mLatLng = latLng; }

    public String getTitle() { return mTitle; }

    public void setTitle(String title) { this.mTitle = title; }

    public String getNote() { return mNote; }

    public void setNote(String note) { this.mNote = note; }

}
