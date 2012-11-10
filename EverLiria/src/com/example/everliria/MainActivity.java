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
	public static final String INTENT_EXTRA_TITLE 		   = "android.intent.extra.TITLE";
	public static final String INTENT_EXTRA_TEXT 		   = "android.intent.extra.TEXT";
	
	//Layouts
	EditText inputName;

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
    	intent.putExtra(INTENT_EXTRA_TITLE, inputName.getText().toString());
    	intent.putExtra(INTENT_EXTRA_TEXT, "musica1 \nmusica2 \nmusica3");
    	try {
    		startActivity(intent);
    	} catch (android.content.ActivityNotFoundException ex) {
    		Toast.makeText(this, R.string.err_activity_not_found, Toast.LENGTH_SHORT).show();
	  } 
    }

    
}
