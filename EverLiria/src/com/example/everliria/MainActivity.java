package com.example.everliria;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// culpa da mayra
	// Names of Evernote-specific Intent actions and extras
	public static final String ACTION_NEW_NOTE             = "com.evernote.action.CREATE_NEW_NOTE";
	
	//Layouts
	EditText inputName;
	Button startListening;
	Button stopListening;
	ListView musicList;
	
	MusicRecognizer mR = new MusicRecognizer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
    			  "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
    			  "Linux", "OS/2" };
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputName = (EditText)findViewById(R.id.input_list_name);
        startListening = (Button)findViewById(R.id.start);
        stopListening = (Button)findViewById(R.id.stop);
        musicList = (ListView)findViewById(R.id.music_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        		  android.R.layout.simple_list_item_1, android.R.id.text1, values);
        musicList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void start(View view) {
    	mR.startListening();
    	startListening.setVisibility(View.GONE);
    	stopListening.setVisibility(View.VISIBLE);
    	
//    	ImageView img = (ImageView)findViewById(R.id.elephant_loading);
//    	img.setBackgroundResource(R.drawable.ever_loading);
//
//    	// Get the background, which has been compiled to an AnimationDrawable object.
//    	AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
//
//    	// Start the animation (looped playback by default).
//    	frameAnimation.start();
    }
    
    /**
     * Bring up an empty "New Not" activity in Evernote for Android.
     */
    public void newList(View view) {
    	mR.stopListening();
    	startListening.setVisibility(View.VISIBLE);
    	stopListening.setVisibility(View.GONE);
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
