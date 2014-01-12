package net.gylka.mapnotes;

import com.google.android.gms.maps.model.LatLng;

public class MapNote {

    public static final String ID_KEY = "Id";
    public static final String LATITUDE_KEY = "Latitude";
    public static final String LONGTITUDE_KEY = "Longtitude";
    public static final String TITLE_KEY = "Title";
    public static final String NOTE_KEY = "Note";

    private long mId;
    private LatLng mLatLng;
    private String mTitle;
    private String mNote;

    public MapNote() {
        mId = -1;
        mLatLng = new LatLng(0,0);
        mTitle = "";
        mNote = "";
    }

    public long getId() { return this.mId; }

    public void setId(long id) { this.mId = id; }

    public LatLng getLatLng() { return mLatLng; }

    public void setLatLng(LatLng latLng) { this.mLatLng = latLng; }

    public String getTitle() { return mTitle; }

    public void setTitle(String title) { this.mTitle = title; }

    public String getNote() { return mNote; }

    public void setNote(String note) { this.mNote = note; }

}
