package net.gylka.mapnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MapNotesDaoImpl implements MapNotesDao {

    private NotesDbOpenHelper mDbHelper;

    public MapNotesDaoImpl(Context context) {
        mDbHelper = NotesDbOpenHelper.getInstance(context);
    }

    @Override
    public boolean addNote(MapNote mapNote) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (! isMapNoteAlreadyInTable(db, mapNote.getLatLng())) {
            ContentValues values = new ContentValues();
            values.put(NotesEntry.C_LAT, mapNote.getLatLng().latitude );
            values.put(NotesEntry.C_LNG, mapNote.getLatLng().longitude);
            values.put(NotesEntry.C_TITLE, mapNote.getTitle());
            values.put(NotesEntry.C_NOTE, mapNote.getNote());
            if (db.insert(NotesEntry.TABLE_NAME, null, values) != -1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ArrayList<MapNote> getAllNotes() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ArrayList<MapNote> mapNotes = new ArrayList<MapNote>();
        Cursor cur = db.rawQuery("SELECT * FROM " + NotesEntry.TABLE_NAME, null);
        while (cur.moveToNext()) {
            MapNote mapNote = new MapNote();
            mapNote.setLatLng(new LatLng(cur.getDouble(cur.getColumnIndex(NotesEntry.C_LAT)), cur.getDouble(cur.getColumnIndex(NotesEntry.C_LNG))));
            mapNote.setTitle(cur.getString(cur.getColumnIndex(NotesEntry.C_TITLE)));
            mapNote.setNote(cur.getString(cur.getColumnIndex(NotesEntry.C_NOTE)));
            mapNotes.add(mapNote);
        }
        cur.close();
        return mapNotes;
    }

    @Override
    public boolean isMapNoteAlreadyInTable(MapNote mapNote) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return isMapNoteAlreadyInTable(db, mapNote.getLatLng());
    }

    @Override
    public boolean isMapNoteAlreadyInTable(LatLng latLng) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return isMapNoteAlreadyInTable(db, latLng);
   }

    private boolean isMapNoteAlreadyInTable(SQLiteDatabase db, LatLng latLng) {
        Cursor cur = db.rawQuery("SELECT 1 FROM " + NotesEntry.TABLE_NAME +
                " WHERE " + NotesEntry.C_LAT + " = ? AND " + NotesEntry.C_LNG + " = ?"
                , new String[]{new Double(latLng.latitude).toString(), new Double(latLng.longitude).toString() });
        if (cur.getCount() > 0) {
            cur.close();
            return true;
        }
        cur.close();
        return false;
    }

}
