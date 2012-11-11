package com.example.everliria;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.chute.android.multiimagepicker.intent.ChoosePhotosActivityIntentWrapper;
import com.chute.sdk.api.GCHttpCallback;
import com.chute.sdk.api.asset.GCAssets;
import com.chute.sdk.collections.GCLocalAssetCollection;
import com.chute.sdk.model.GCAccountStore;
import com.chute.sdk.model.GCHttpRequestParameters;
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

}