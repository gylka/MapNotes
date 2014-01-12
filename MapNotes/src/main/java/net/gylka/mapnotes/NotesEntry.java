package net.gylka.mapnotes;

import android.provider.BaseColumns;

public class NotesEntry implements BaseColumns {

    public static final String TABLE_NAME = "markers";
    public static final String C_LAT = "latitude";
    public static final String C_LNG = "longtitude";
    public static final String C_TITLE = "title";
    public static final String C_NOTE = "note";

    protected static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                    C_LAT + " REAL, " +
                    C_LNG + " REAL, " +
                    C_TITLE + " TEXT , " +
                    C_NOTE + " TEXT " +
                    " )";

    protected static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}