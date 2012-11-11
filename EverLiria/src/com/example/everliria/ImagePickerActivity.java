package com.example.everliria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.chute.android.multiimagepicker.intent.ChoosePhotosActivityIntentWrapper;
import com.chute.sdk.collections.GCLocalAssetCollection;
import com.chute.sdk.model.GCAccountStore;
import com.chute.sdk.model.GCLocalAssetModel;

public class ImagePickerActivity extends Activity implements
		OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_imagepicker);
		//Button selectPhotos = (Button) findViewById(R.id.btnSelectPhotos);
		//selectPhotos.setOnClickListener(this);
		GCAccountStore.setAppId(getApplicationContext(), "4f15d1f138ecef6af9000004");
		ChoosePhotosActivityIntentWrapper wrapper = new ChoosePhotosActivityIntentWrapper(
				this);
		wrapper.startActivityForResult(this,
				ChoosePhotosActivityIntentWrapper.ACTIVITY_FOR_RESULT_KEY);
	}

	@Override
	public void onClick(View v) {
		ChoosePhotosActivityIntentWrapper wrapper = new ChoosePhotosActivityIntentWrapper(
				this);
		wrapper.startActivityForResult(this,
				ChoosePhotosActivityIntentWrapper.ACTIVITY_FOR_RESULT_KEY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println(">>DATA "+data.getExtras());
		if (requestCode == ChoosePhotosActivityIntentWrapper.ACTIVITY_FOR_RESULT_KEY) {
			if (resultCode == RESULT_OK) {
				ChoosePhotosActivityIntentWrapper wrapper = new ChoosePhotosActivityIntentWrapper(
						data);
				GCLocalAssetCollection localAssetCollection = makeGCLocalAssetCollection(wrapper
						.getAssetPathList());
				
				int photosSelected = localAssetCollection.size();
				Toast.makeText(getApplicationContext(),
						photosSelected + " photos selected!",
						Toast.LENGTH_SHORT).show();
				
				  //Create a note
				  Intent intent = new Intent();
				  intent.setAction("com.evernote.action.CREATE_NEW_NOTE");
				  // Set the note's title and plaintext content
				  
				 SharedPreferences shared = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
				 String title = shared.getString(Constants.PREFS_TITLE, "");
				 Set<String> songs = shared.getStringSet(Constants.PREFS_SONGS, new HashSet<String>());
				 
				 intent.putExtra(Intent.EXTRA_TITLE, title);
				 intent.putExtra(Intent.EXTRA_TEXT, formatMusicList(songs));
				 
				  
				   // Add file(s) to be attached to the note
				   ArrayList<Uri> uriList = new ArrayList<Uri>();
				   for(String st : wrapper.getAssetPathList())
					   uriList.add(Uri.parse("file://"+st));
				   
				   intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM , uriList);
				   try {
				     startActivity(intent);
				   } catch (android.content.ActivityNotFoundException ex) {
				     Toast.makeText(this, R.string.err_activity_not_found, Toast.LENGTH_SHORT).show();
				   } 

				    
				    
			} else {
				Toast.makeText(getApplicationContext(), "No photos selected",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public GCLocalAssetCollection makeGCLocalAssetCollection(
			ArrayList<String> gridSelectedFilePath) {
		GCLocalAssetCollection collection = new GCLocalAssetCollection();
		GCLocalAssetModel model = new GCLocalAssetModel();
		for (String filePath : gridSelectedFilePath) {
			model.setFile(filePath);
			collection.add(model);
		}
		return collection;
	}
	
	 private String formatMusicList(Set<String> musics) {
    	StringBuilder musicList = new StringBuilder();
    	for (String music : musics)
    		musicList.append(music+"\n");
    			
    	return musicList.toString();
    }

}