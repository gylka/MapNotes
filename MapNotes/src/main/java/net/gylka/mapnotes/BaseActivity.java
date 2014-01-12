package net.gylka.mapnotes;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class BaseActivity extends ActionBarActivity {

    public static String PACKAGE_NAME = "net.gylka.mapnotes";

    protected MapNotesDaoImpl mMapNotesDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapNotesDao = new MapNotesDaoImpl(getApplicationContext());
    }

}
