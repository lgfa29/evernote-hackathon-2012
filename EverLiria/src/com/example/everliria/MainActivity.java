package com.example.everliria;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// culpa da mayra
	// Names of Evernote-specific Intent actions and extras
	public static final String ACTION_NEW_NOTE             = "com.evernote.action.CREATE_NEW_NOTE";
	
	EditText inputName;
	Button startListening;
	Button stopListening;
	ListView musicList;
	AnimationDrawable frameAnimation;
	View loading;
	List<String> musics;
	CustomAdapter adapter;
	
	MusicRecognizer mR = new MusicRecognizer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputName = (EditText)findViewById(R.id.input_list_name);
        startListening = (Button)findViewById(R.id.start);
        stopListening = (Button)findViewById(R.id.stop);
        loading = (View)findViewById(R.id.loading);
        		
        musics = new ArrayList<String>();
        		
        musicList = (ListView)findViewById(R.id.music_list);
        adapter = new CustomAdapter();
        musicList.setAdapter(adapter);
        
        loading.setBackgroundResource(R.drawable.ever_loading);
    	frameAnimation = (AnimationDrawable) loading.getBackground();
    	frameAnimation.setAlpha(50);
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
    	loading.setVisibility(View.VISIBLE);
    	frameAnimation.start();
    	new Thread(new Runnable(){

			public void run() {
				for (int i=0; i < 10; i++) {
					musics = mR.getMusicList();
					adapter.notifyDataSetChanged();
					synchronized(this){
						try {
							wait(10000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}});
    	
    }
    
    /**
     * Bring up an empty "New Not" activity in Evernote for Android.
     */
    public void newList(View view) {
    	mR.stopListening();
    	startListening.setVisibility(View.VISIBLE);
    	stopListening.setVisibility(View.GONE);
    	loading.setVisibility(View.GONE);
    	
    	Intent intent = new Intent();
    	intent.setAction(ACTION_NEW_NOTE);
    	intent.putExtra(Intent.EXTRA_TITLE, inputName.getText().toString());
    	intent.putExtra(Intent.EXTRA_TEXT, "badabum");
    	try {
    		startActivity(intent);
    	} catch (android.content.ActivityNotFoundException ex) {
    		ex.printStackTrace();
    		Toast.makeText(this, R.string.err_activity_not_found, Toast.LENGTH_SHORT).show();
	  } 
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
				convertView = getLayoutInflater().inflate(R.layout.item, parent);
			}
			TextView tv = (TextView)convertView.findViewById(R.id.music_item);
			tv.setText(musics.get(position));
			return convertView;
		}
    	
    }
    
}
