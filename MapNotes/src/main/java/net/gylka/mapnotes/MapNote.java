package net.gylka.mapnotes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class MapNote implements Parcelable {

    public static final String MAP_NOTE_KEY = "MapNote";

    public static final String ID_KEY = "Id";
    public static final String LAT_LNG_KEY = "LatLng";
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

    public MapNote(Parcel in) {
        mId = in.readLong();
        double latitude = in.readDouble();
        double longtitude = in.readDouble();
        mLatLng = new LatLng(latitude, longtitude);
        mTitle = in.readString();
        mNote = in.readString();
    }

    public long getId() { return this.mId; }

    public void setId(long id) { this.mId = id; }

    public LatLng getLatLng() { return mLatLng; }

    public void setLatLng(LatLng latLng) { this.mLatLng = latLng; }

    public String getTitle() { return mTitle; }

    public void setTitle(String title) { this.mTitle = title; }

    public String getNote() { return mNote; }

    public void setNote(String note) { this.mNote = note; }

    public void updateMapNoteSavingId (MapNote mapNote) {
        mLatLng = mapNote.mLatLng;
        mTitle = mapNote.mTitle;
        mNote = mapNote.mNote;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeDouble(mLatLng.latitude);
        dest.writeDouble(mLatLng.longitude);
        dest.writeString(mTitle);
        dest.writeString(mNote);
    }

    public static final Parcelable.Creator<MapNote> CREATOR = new Parcelable.Creator<MapNote>() {

        @Override
        public MapNote createFromParcel(Parcel source) {
            return new MapNote(source);
        }

        @Override
        public MapNote[] newArray(int size) {
            return new MapNote[size];
        }
    };
}
