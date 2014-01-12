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
    public long addNote(MapNote mapNote) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (! isMapNoteAlreadyInTable(db, mapNote.getId())) {
            ContentValues values = new ContentValues();
            values.put(NotesEntry.C_LAT, mapNote.getLatLng().latitude );
            values.put(NotesEntry.C_LNG, mapNote.getLatLng().longitude);
            values.put(NotesEntry.C_TITLE, mapNote.getTitle());
            values.put(NotesEntry.C_NOTE, mapNote.getNote());
            return db.insert(NotesEntry.TABLE_NAME, null, values);
        }
        return -2;
    }

    @Override
    public MapNote getMapNote(long id) {
        if (id < 0) {
            return null;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + NotesEntry.TABLE_NAME
                + " WHERE " + NotesEntry._ID + " = ? ",
                new String[]{new Long(id).toString()});
        if (cur.getCount() == 0 ) {
            return null;
        }
        cur.moveToFirst();
        MapNote mapNote = new MapNote();
        mapNote.setId(id);
        mapNote.setLatLng(new LatLng(cur.getDouble(cur.getColumnIndex(NotesEntry.C_LAT)), cur.getDouble(cur.getColumnIndex(NotesEntry.C_LNG))));
        mapNote.setTitle(cur.getString(cur.getColumnIndex(NotesEntry.C_TITLE)));
        mapNote.setNote(cur.getString(cur.getColumnIndex(NotesEntry.C_NOTE)));
        return mapNote;
    }

    @Override
    public ArrayList<MapNote> getAllNotes() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ArrayList<MapNote> mapNotes = new ArrayList<MapNote>();
        Cursor cur = db.rawQuery("SELECT * FROM " + NotesEntry.TABLE_NAME, null);
        while (cur.moveToNext()) {
            MapNote mapNote = new MapNote();
            mapNote.setId(cur.getInt(cur.getColumnIndex(NotesEntry._ID)));
            mapNote.setLatLng(new LatLng(cur.getDouble(cur.getColumnIndex(NotesEntry.C_LAT)), cur.getDouble(cur.getColumnIndex(NotesEntry.C_LNG))));
            mapNote.setTitle(cur.getString(cur.getColumnIndex(NotesEntry.C_TITLE)));
            mapNote.setNote(cur.getString(cur.getColumnIndex(NotesEntry.C_NOTE)));
            mapNotes.add(mapNote);
        }
        cur.close();
        return mapNotes;
    }

    @Override
    public boolean updateMapNote(long mapNoteId, MapNote mapNote) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (! isMapNoteAlreadyInTable(mapNoteId)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(NotesEntry.C_LAT, mapNote.getLatLng().latitude );
        values.put(NotesEntry.C_LNG, mapNote.getLatLng().longitude);
        values.put(NotesEntry.C_TITLE, mapNote.getTitle());
        values.put(NotesEntry.C_NOTE, mapNote.getNote());
        db.update(NotesEntry.TABLE_NAME, values, NotesEntry._ID + " = ?", new String[] {new Long(mapNote.getId()).toString()});
        return true;
    }

    @Override
    public boolean isMapNoteAlreadyInTable(MapNote mapNote) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return isMapNoteAlreadyInTable(db, mapNote.getId());
    }

    @Override
    public boolean isMapNoteAlreadyInTable(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return isMapNoteAlreadyInTable(db, id);
   }

    private boolean isMapNoteAlreadyInTable(SQLiteDatabase db, long id) {
        Cursor cur = db.rawQuery("SELECT 1 FROM " + NotesEntry.TABLE_NAME +
                " WHERE " + NotesEntry._ID + " = ?",
                new String[]{new Long(id).toString()});
        if (cur.getCount() > 0) {
            cur.close();
            return true;
        }
        cur.close();
        return false;
    }

}
