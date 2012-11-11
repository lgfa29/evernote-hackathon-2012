package com.example.everliria;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Names of Evernote-specific Intent actions and extras
	public static final String ACTION_NEW_NOTE = "com.evernote.action.CREATE_NEW_NOTE";
	
	EditText inputName;
	Button startListening;
	Button stopListening;
	ListView musicList;
	AnimationDrawable frameAnimation;
	View loading;
	View stopped;
	View create;
	List<String> musics;
	CustomAdapter adapter;
	BroadcastReceiver bcReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        inputName = (EditText)findViewById(R.id.input_list_name);
        
        startListening = (Button)findViewById(R.id.start);
        stopListening = (Button)findViewById(R.id.stop);
        
        stopped = (View)findViewById(R.id.stopped);
        stopped.getBackground().setAlpha(50);
        loading = (View)findViewById(R.id.loading);
        		
        musics = new ArrayList<String>();
        musicList = (ListView)findViewById(R.id.music_list);
        adapter = new CustomAdapter();
        
        View v = getLayoutInflater().inflate(R.layout.create_button, null);
        musicList.addFooterView(v);
        create = findViewById(R.id.create);
        musicList.setAdapter(adapter);
        
    	bcReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Bundle bundle = intent.getExtras();
				
				if (bundle.containsKey(Constants.INTENT_ACTION_MUSIC_NAME)) {
					String song = bundle.getString(Constants.INTENT_ACTION_MUSIC_NAME);
					if (!musics.contains(song)) {
						musics.add(song);
						adapter.notifyDataSetChanged();
						Toast.makeText(context, "New music added!", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(context, song+" is already in the list.", Toast.LENGTH_LONG).show();
					}
				} else if (bundle.containsKey(Constants.INTENT_ACTION_MUSIC_NOT_FOUND)) {
					Toast.makeText(context, "Music not found.", Toast.LENGTH_LONG).show();
				} else if (bundle.containsKey(Constants.INTENT_ACTION_ERROR)) {
					Toast.makeText(context, "Error while trying to identify music.", Toast.LENGTH_LONG).show();
				}
			}
		};
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	registerReceiver(bcReceiver, new IntentFilter(Constants.INTENT_FILTER_TAG));
    	adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	unregisterReceiver(bcReceiver);
    }
    
    public void start(View view) {
    	startListening.setVisibility(View.GONE);
    	stopListening.setVisibility(View.VISIBLE);
    	stopped.setVisibility(View.GONE);
    	loading.setVisibility(View.VISIBLE);
    	loading.setBackgroundResource(R.drawable.ever_loading);
    	frameAnimation = (AnimationDrawable) loading.getBackground();
    	frameAnimation.setAlpha(50);
    	frameAnimation.start();
		create.setVisibility(View.GONE);
    	
    	startService(new Intent(this, MusicRecognizer.class));
    }
    
    public void stop(View view){
    	startListening.setVisibility(View.VISIBLE);
    	stopListening.setVisibility(View.GONE);
    	stopped.setVisibility(View.VISIBLE);
    	loading.setVisibility(View.GONE);
    	create.setVisibility(View.VISIBLE);
    	startService(new Intent(this, MusicRecognizer.class));
    }
    
    /**
     * Bring up an empty "New Not" activity in Evernote for Android.
     */
    public void newList(View view) {
    	Intent intent = new Intent();
    	intent.setAction(ACTION_NEW_NOTE);
    	intent.putExtra(Intent.EXTRA_TITLE, inputName.getText().toString());
    	intent.putExtra(Intent.EXTRA_TEXT, formatMusicList(musics));
    	try {
    		startActivity(intent);
    	} catch (android.content.ActivityNotFoundException ex) {
    		ex.printStackTrace();
    		Toast.makeText(this, R.string.err_activity_not_found, Toast.LENGTH_SHORT).show();
	  } 
    }
    
    private String formatMusicList(List<String> musics) {
    	StringBuilder musicList = new StringBuilder();
    	for (String music : musics)
    		musicList.append(music+"\n");
    			
    	return musicList.toString();
    }

    public class CustomAdapter extends BaseAdapter {
    	
    	
		public int getCount() {
			return musics.size();
		}

		public Object getItem(int position) {
			return musics.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item, null, false);
			}
			TextView tv = (TextView)convertView.findViewById(R.id.music_item);
			tv.setText(musics.get(position));
			return convertView;
		}
    	
    }
    
}
