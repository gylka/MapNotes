package net.gylka.mapnotes;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class BaseActivity extends ActionBarActivity {

    protected MapNotesDaoImpl mMapNotesDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapNotesDao = new MapNotesDaoImpl(getApplicationContext());
    }


}
