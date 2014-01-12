package net.gylka.mapnotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class NotesDbOpenHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mapnotes.db";

    private static NotesDbOpenHelper mInstance;

    public static NotesDbOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotesDbOpenHelper(context);
        }
        return mInstance;
    }

    private NotesDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(NotesEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(NotesEntry.SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }

    public static void copyDatabaseToExtSDCardDownloads (Context context) {
        File backupDbDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (backupDbDirectory.canWrite()) {
            String currentDbPath = NotesDbOpenHelper.getInstance(context).getReadableDatabase().getPath();
            File currentDb = new File(currentDbPath);
            File backupDb = new File(backupDbDirectory, DATABASE_NAME);
            if (currentDb.exists()) {
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(currentDb);
                    fos = new FileOutputStream(backupDb);
                } catch (FileNotFoundException e) {
                    Toast.makeText(context, "Can't find currend DB file or can't open backup file for writing", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                FileChannel source = fis.getChannel();
                FileChannel destination = fos.getChannel();
                try {
                    destination.transferFrom(source, 0, source.size());
                } catch (IOException e) {
                    Toast.makeText(context, "Some I/O error happened during DB backup", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                try {
                    source.close();
                    destination.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(context, "Can't write to Download directory", Toast.LENGTH_SHORT).show();
        }
    }


}
