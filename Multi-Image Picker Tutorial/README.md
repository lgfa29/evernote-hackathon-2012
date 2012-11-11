Introduction
====

Multi-Image Picker Tutorial is a tutorial project that shows how to use the Multi-Image Picker component. It contains Chute SDK library as well as Multi-Image Picker library. 
This tutorial demonstrates how to make GCLocalAssetCollection of assets. It searches for images on the device, displays the images in a grid and creates GCLocalAssetCollection out of the selected images in the grid.

![image1](https://github.com/chute/chute-tutorials/raw/master/Android/Multi-Image%20Picker%20Tutorial/screenshots/1.png)![image2](https://github.com/chute/chute-tutorials/raw/master/Android/Multi-Image%20Picker%20Tutorial/screenshots/2.png)![image3](https://github.com/chute/chute-tutorials/raw/master/Android/Multi-Image%20Picker%20Tutorial/screenshots/3.png)

Setup
====

* Follow the ProjectSetup tutorial that can be found and downloaded at  
  [https://github.com/chute/chute-tutorials/tree/master/Android/Project%20Setup](https://github.com/chute/chute-tutorials/tree/master/Android/Project%20Setup) for a complete guide on how to setup the chute SDK.
  
* Add the Multi-Image Picker component to your project by either copying all the resources and source code or by adding it as an Android Library project.
  Multi-Image Picker component can be found and downloaded at [https://github.com/chute/chute-android-components/tree/master/Multi-Image%20Picker](https://github.com/chute/chute-android-components/tree/master/Multi-Image%20Picker).

* The next thing you need to do is register the activities and the application class into the AndroidManifest.xml file:

    ```
         <application
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:name=".app.LocalImagePickerTutorialApp"
        android:theme="@android:style/Theme.Light.NoTitleBar" >
        <activity
            android:label="@string/app_name"
            android:name=".app.LocalImagePickerTutorialActivity"
            android:theme="@android:style/Theme.Light.NoTitleBar"
            android:screenOrientation="portrait" >
            <intent-filter >
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name="com.chute.android.multiimagepicker.app.MultiImagePickerActivity"
            android:theme="@android:style/Theme.Light.NoTitleBar"
            android:screenOrientation="portrait" >
            </activity>
         </application>
    ```

Usage
====

##MultiImagePickerTutorialApp.java 
This class is the extended Application class. It is registered inside the "application" tag in the manifest and is used for initializing the utility classes used in the component.
MultiImagePickerTutorialApp can extend MultiImagePickerApp like shown in this tutorial:

<pre><code>
public class MultiImagePickerTutorialApp extends MultiImagePickerApp {

}
</code></pre>

This way the developer can use his own methods and code inside the Application class. 

If the developer decides to extend the Application class instead of MultiImagePickerApp he must copy the all the code below:

<pre><code>
public class MultiImagePickerTutorialApp extends Application {
    private static ImageLoader createImageLoader(Context context) {
		ImageLoader imageLoader = new ImageLoader(context, R.drawable.placeholder_image_small);
		imageLoader.setDefaultImageSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources()
						.getDisplayMetrics()));
		return imageLoader;
    }

    private ImageLoader mImageLoader;

    @Override
    public void onCreate() {
	super.onCreate();
	mImageLoader = createImageLoader(this);
    }

    @Override
    public Object getSystemService(String name) {
	if (ImageLoader.IMAGE_LOADER_SERVICE.equals(name)) {
	    return mImageLoader;
	} else {
	    return super.getSystemService(name);
	}
    }

}
</code></pre>

MultiImagePickerTutorialApp can also be neglected by registering MultiImagePickerApp into the manifest instead of MultiImagePickerTutoiralApp if the developer doesn't have the need for extending the Application class.
 
##MultiImagePickerTutorialActivity.java 
This Activity class contains a "Select Photos" button. When the button is clicked, ChoosePhotosActivityIntentWrapper starts MultiImagePickerActivity. ChoosePhotosActivityIntentWrapper is a wrapper class that wraps the parameters needed for the intent.

<pre><code>
@Override
	    public void onClick(View v) {
		ChoosePhotosActivityIntentWrapper wrapper = new ChoosePhotosActivityIntentWrapper(this);
		wrapper.startActivityForResult(this,
			ChoosePhotosActivityIntentWrapper.ACTIVITY_FOR_RESULT_KEY);
	    }
</code></pre>    

MultiImagePickerActivity contains GridView that displays device images. You can select one or multiple images. After selecting the desired images, when "Ok" button gets clicked, a result is returned to the activity that started the component where toast message appears showing the number of the selected images.

<pre><code>
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
			Toast.makeText(getApplicationContext(), photosSelected + " photos selected!",
				Toast.LENGTH_SHORT).show();
		    } else {
			Toast.makeText(getApplicationContext(), "No photos selected", Toast.LENGTH_SHORT)
				.show();
		    }
		}
	    }
</code></pre>

ChoosePhotosActivityIntentWrapper encapsulates a list of selected images paths, from which GCLocalAssetCollection can be created:

<pre><code>
 public GCLocalAssetCollection makeGCLocalAssetCollection(ArrayList<String> gridSelectedFilePath) {
		GCLocalAssetCollection collection = new GCLocalAssetCollection();
		GCLocalAssetModel model = new GCLocalAssetModel();
		for (String filePath : gridSelectedFilePath) {
		    model.setFile(filePath);
		    collection.add(model);
		}
		return collection;
	    }
</code></pre>	     	    
  