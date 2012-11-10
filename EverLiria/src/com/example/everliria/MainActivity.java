package com.example.everliria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	// Names of Evernote-specific Intent actions and extras
	public static final String ACTION_NEW_NOTE             = "com.evernote.action.CREATE_NEW_NOTE";
	
	//Layouts
	EditText inputName;
	
	MusicRecognizer mR = new MusicRecognizer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputName = (EditText)findViewById(R.id.input_list_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Bring up an empty "New Not" activity in Evernote for Android.
     */
    public void newList(View view) {
    	Intent intent = new Intent();
    	intent.setAction(ACTION_NEW_NOTE);
    	intent.putExtra(Intent.EXTRA_TITLE, inputName.getText().toString());
    	intent.putExtra(Intent.EXTRA_TEXT, mR.getMusicList());
    	try {
    		startActivity(intent);
    	} catch (android.content.ActivityNotFoundException ex) {
    		Toast.makeText(this, R.string.err_activity_not_found, Toast.LENGTH_SHORT).show();
	  } 
    }

    
}
