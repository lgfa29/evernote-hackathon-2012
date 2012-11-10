/**
 * AudioFingerprinter.java
 * EchoprintLib
 * 
 * Created by Alex Restrepo on 1/22/12.
 * Copyright (C) 2012 Grand Valley State University (http://masl.cis.gvsu.edu/)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.gvsu.masl.echoprint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Main fingerprinting class<br>
 * This class will record audio from the microphone, generate the fingerprint code using a native library and query the data server for a match
 * 
 * @author Alex Restrepo (MASL)
 *
 */
public class AudioFingerprinter implements Runnable 
{
	public final static String META_SCORE_KEY = "meta_score";
	public final static String SCORE_KEY = "score";
	public final static String ALBUM_KEY = "release";
	public final static String TITLE_KEY = "title";
	public final static String TRACK_ID_KEY = "id";
	public final static String ARTIST_KEY = "artist_name";
	
//	private final String SERVER_URL = "<your server address here>/query?fp_code=";
	private final String SERVER_URL = "http://developer.echonest.com/api/v4/song/identify?api_key=FBJYI4PLNIO72ENS0&version=4.12&code=";
	
	private final int FREQUENCY = 11025;
	private final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
	private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;	
	
	private Thread thread;
	private volatile boolean isRunning = false;
	AudioRecord mRecordInstance = null;
	
	private short audioData[];
	private int bufferSize;	
	private int secondsToRecord;
	private volatile boolean continuous;
	
	private AudioFingerprinterListener listener;
	
	/**
	 * Constructor for the class
	 * 
	 * @param listener is the AudioFingerprinterListener that will receive the callbacks
	 */
	public AudioFingerprinter(AudioFingerprinterListener listener)
	{
		this.listener = listener;
	}
	
	/**
	 * Starts the listening / fingerprinting process using the default parameters:<br>
	 * A single listening pass of 20 seconds 
	 */
	public void fingerprint()
	{
		// set dafault listening time to 20 seconds
		this.fingerprint(20);
	}
	
	/**
	 * Starts a single listening / fingerprinting pass
	 * 
	 * @param seconds the seconds of audio to record.
	 */
	public void fingerprint(int seconds)
	{
		// no continuous listening
		this.fingerprint(seconds, false);
	}
	
	/**
	 * Starts the listening / fingerprinting process
	 * 
	 * @param seconds the number of seconds to record per pass
	 * @param continuous if true, the class will start a new fingerprinting pass after each pass
	 */
	public void fingerprint(int seconds, boolean continuous)
	{
		if(this.isRunning)
			return;
				
		this.continuous = continuous;
		
		// cap to 30 seconds max, 10 seconds min.
		this.secondsToRecord = Math.max(Math.min(seconds, 30), 10);
		
		// start the recording thread
		thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * stops the listening / fingerprinting process if there's one in process
	 */
	public void stop() 
	{
		this.continuous = false;
		if(mRecordInstance != null)
			mRecordInstance.stop();
	}
	
	/**
	 * The main thread<br>
	 * Records audio and generates the audio fingerprint, then it queries the server for a match and forwards the results to the listener.
	 */
	public void run() 
	{
		this.isRunning = true;
		try 
		{			
			// create the audio buffer
			// get the minimum buffer size
			int minBufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);
			
			// and the actual buffer size for the audio to record
			// frequency * seconds to record.
			bufferSize = Math.max(minBufferSize, this.FREQUENCY * this.secondsToRecord);
						
			audioData = new short[bufferSize];
						
			// start recorder
			mRecordInstance = new AudioRecord(
								MediaRecorder.AudioSource.MIC,
								FREQUENCY, CHANNEL, 
								ENCODING, minBufferSize);
						
			willStartListening();
			
			mRecordInstance.startRecording();
			boolean firstRun = true;
			do 
			{		
				try
				{
					willStartListeningPass();
					
					long time = System.currentTimeMillis();
					// fill audio buffer with mic data.
					int samplesIn = 0;
					do 
					{					
						samplesIn += mRecordInstance.read(audioData, samplesIn, bufferSize - samplesIn);
						
						if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
							break;
					} 
					while (samplesIn < bufferSize);				
					Log.d("Fingerprinter", "Audio recorded: " + (System.currentTimeMillis() - time) + " millis");
										
					// see if the process was stopped.
					if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED || (!firstRun && !this.continuous))
						break;
					
					// create an echoprint codegen wrapper and get the code
					time = System.currentTimeMillis();
					Codegen codegen = new Codegen();
	    			String code = codegen.generate(audioData, samplesIn);
	    			Log.d("Fingerprinter", "Codegen created in: " + (System.currentTimeMillis() - time) + " millis");
	    			
	    			if(code.length() == 0)
	    			{
	    				// no code?
	    				// not enough audio data?
						continue;
	    			}
	    			
	    			didGenerateFingerprintCode(code);
	    			
	    			// fetch data from echonest
	    			time = System.currentTimeMillis();
	    			
	    			//TODO: REMOVE THIS HARDCODED CODE! :)
	    			//code = "eJzFV1uuXCkQ2xIFFBTLqef-lzA-HWn6TkuhP6JoFMnShdM8XLaLtNZotAt0v8GcN-C8gdQN9Aq5L0CDLvDlvvcbLb-B0wWo-Q36usGQC3y5EbcbBF2AWtygnxusvIGOC_S2LvBHil10A5UbXOvbwi5AI27what-gxgX6GNf4I-Y5HmD027w95jkdYMvXPEF_mJOfknC_0mTdybdL9CfvX8LrdkNvjA5bvAnTNoVPC7whaurf79w9Re7c9xgrxtcuaLVb3DkBmY3CP4t7KTplZRjj2ZM21TS_RzJw1o9WkpPKg_rc_XjyaMrLbZI2nXG6cM6nggKZtNW0-yx-9JGqCLmNgmeF1hyu0-nQ7tL6DBClQ0jox9xy0Vgd1Gv3scZW8Z2kaI9bWCbOaRH1pyrxqDUWrOCcoK3cus5f4Azut3H2A84beXJWdN3UVDnEkXZl-6sbLZwDO-efkKTc28fyV13o40fuWhkm9yLXNZzgm0I7iUbx8ix5oggOdqGHtxkzVZokD1GnwEOjjcfnua8D1hYucmKCb_WwT4UX3A_iXfCNHuDUzX7GHtD2gjZ3p-mt-ZS1A5LeONda2XUKojeuOqhsLGc0Yfiy9alqVVPjVM4Cio3EHAjl7L3jENRNZnSm45uyTFYW8bYfdvyMQv3DURECN4XbGO2KBnQjs_G2EX2WFxazpI0shhcDga_eyZRocZ4tGTo6RFi9QZu_PPPD9hMeIPR5jaDw3XvwRVrS9Qp2am81i6c5czaO0Nyj96l_KziRwTDZbdDyyfY08DQFj3MxqPkvFbOUwqR4uLTWAQ87iV8bFqXZW2YzIRS5uyQCSdFMztnByb7Oovn8Ca4rE_CP3wrGCwoy5_VYCw3Y_T0jcovYpRCIcZddRy1iFZbt2yQBP4R0ZZz7mg4vZ2-cUB4YbE_j3pR3CrASfcDkZTKqjcsCbjov2NvIAS4OGcfBHUsETO44tBga4oaJjaXOE1O9IW_BtmMR_gSueLgJQTz7m5zqqbhlhvCQQctkRRYGqYupb6ToTKIPs6IguHo7FNIhtEblHDcaZoveA6W65AOnM9mCyfRgt4QFIXD0GNPmscZrHM89Xx2LaL9fLFar2M0yjqKCx_X6fgZbGfLTqxyNNnJiBjrbAuiVfTcHwDnOn2MvcFzggi80aF3KsoWPmMK2XHYpyOG5muWUHp9zRLCTF-zR8mzpy_9NXtsK8JmM4D5tfKaB26isekUTjfH0IDgIEMTmORZzxUbOuzd5cCUMP4kHoHcGPvwa6Pt9AT4PBBN-ILMVGs_2eagR1FUGksmlu5bVYxXCaIK1-i0BZbuEGWqKpUVGgFqZNuhW0udhg7w2A1XMDOIBxYv45CzTbjyDWdYzY-xN8AfHWp97tgdmw40jYCIjCAp2KPFCLR4SFMnao2wjRynikTc19LhgYU2SoIsh5YM_ehAHk0E1QdTjmSBQ-BWRnMqcNOQYSdYA5vhJk-Yklsr84Ow9nx0iXjuqM1Q1MZOxZO72U73DhJgCUdubOEW3JF0T0dTeDyaF7VEOf0MhrQED8jAt0_PRHHHCdiYxjy4h-YyWeho6w1709wfY29AbBhUgxYNAZFgYyfL7NAv2tDHbGcoCa3ozI1wjtes9McvASeb4_8uGn32hphXe0KKTXDdQkI-ZxYTQr4m2jPaupv2Xy1f8TpY6GoOiSGIUBxOqzbBDQwTOFPy0oUuNyYhSZ2wE8Oz--HrzAUa8slmHPjh4nnfzQyofi7oHKUbC63djiKx0XYW3IAY11RHAeUH0Ir1OfYv_AOHwF5P";
	    			
					String urlstr = SERVER_URL + code;		
					System.out.println(">>>>>" + urlstr);
					HttpClient client = new DefaultHttpClient();
	    			HttpGet get = new HttpGet(urlstr);
	    			
	    			// get response
	    			HttpResponse response = client.execute(get);                
	    			// Examine the response status
	    	        Log.d("Fingerprinter",response.getStatusLine().toString());
	
	    	        // Get hold of the response entity
	    	        HttpEntity entity = response.getEntity();
	    	        // If the response does not enclose an entity, there is no need
	    	        // to worry about connection release
	
	    	        String result = "";
	    	        if (entity != null) 
	    	        {
	    	            // A Simple JSON Response Read
	    	            InputStream instream = entity.getContent();
	    	            result= convertStreamToString(instream);
	    	            // now you have the string representation of the HTML request
	    	            instream.close();
	    	        }
	     			Log.d("Fingerprinter", "Results fetched in: " + (System.currentTimeMillis() - time) + " millis");
	    			
	     			
	    			// parse JSON
		    		JSONObject jobj = new JSONObject(result);
		    		
		    		System.out.println(">>RESULTADO:"+result);
		    		if(jobj.has("response")){
		    			JSONObject status = jobj.getJSONObject("response").getJSONObject("status");
		    			JSONArray songs = jobj.getJSONObject("response").getJSONArray("songs");
		    			
		    			if(jobj.has("code"))
			    			Log.d("Fingerprinter", "Response code:" + jobj.getInt("code") + " (" + this.messageForCode(jobj.getInt("code")) + ")");
			    		
		    			
		    			if(songs.length() == 0){
		    				didNotFindMatchForCode(code);
		    				System.out.println("songs is empty");
		    			}else{
		    				JSONObject song = songs.getJSONObject(0);
		    				Hashtable<String, String> match = new Hashtable<String, String>();
		    				match.put(SCORE_KEY, song.getDouble(SCORE_KEY) + "");
		    				match.put(TRACK_ID_KEY, song.getString(TRACK_ID_KEY));
		    				if(song.has(TITLE_KEY)) match.put(TITLE_KEY, song.getString(TITLE_KEY));
		    				if(song.has(ARTIST_KEY)) match.put(ARTIST_KEY, song.getString(ARTIST_KEY));
		    				if(song.has(ALBUM_KEY)) match.put(ALBUM_KEY, song.getString(ALBUM_KEY));
		    				
		    				
		    				didFindMatchForCode(match, code);
		    			}
		    			
		    		}
		    		else{
			    		didFailWithException(new Exception("No response."));
			    	}
//		    		if(jobj.has("match"))
//		    		{
//		    			if(jobj.getBoolean("match"))
//		    			{
//		    				Hashtable<String, String> match = new Hashtable<String, String>();
//		    				match.put(SCORE_KEY, jobj.getDouble(SCORE_KEY) + "");
//		    				match.put(TRACK_ID_KEY, jobj.getString(TRACK_ID_KEY));
//		    				
//		    				// the metadata dictionary IS NOT included by default in the API demo server
//		    				// replace line 66/67 in API.py with:
//		    				// return json.dumps({"ok":True,"message":response.message(), "match":response.match(), "score":response.score, \
//	                        // "qtime":response.qtime, "track_id":response.TRID, "total_time":response.total_time, "metadata":response.metadata})
//		    				if(jobj.has("metadata"))
//		    				{
//		    					JSONObject metadata = jobj.getJSONObject("metadata");
//			    						    				
//			    				if(metadata.has(SCORE_KEY)) match.put(META_SCORE_KEY, metadata.getDouble(SCORE_KEY) + "");
//			    				if(metadata.has(TITLE_KEY)) match.put(TITLE_KEY, metadata.getString(TITLE_KEY));
//			    				if(metadata.has(ARTIST_KEY)) match.put(ARTIST_KEY, metadata.getString(ARTIST_KEY));
//			    				if(metadata.has(ALBUM_KEY)) match.put(ALBUM_KEY, metadata.getString(ALBUM_KEY));
//		    				}
//		    				
//		    				didFindMatchForCode(match, code);
//		    			}
//	    				else
//	    					didNotFindMatchForCode(code);	    			
//		    		}	    		
//		    		else
//		    		{
//		    			didFailWithException(new Exception("Unknown error"));
//		    		}
		    		
		    		firstRun = false;
				
		    		didFinishListeningPass();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Log.e("Fingerprinter", e.getLocalizedMessage());
					
					didFailWithException(e);
				}
			}
			while (this.continuous);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			Log.e("Fingerprinter", e.getLocalizedMessage());
			
			didFailWithException(e);
		}
		
		if(mRecordInstance != null)
		{
			mRecordInstance.stop();
			mRecordInstance.release();
			mRecordInstance = null;
		}
		this.isRunning = false;
		
		didFinishListening();
	}
	
	private static String convertStreamToString(InputStream is) 
	{
	    /*
	     * To convert the InputStream to String we use the BufferedReader.readLine()
	     * method. We iterate until the BufferedReader return null which means
	     * there's no more data to read. Each line will appended to a StringBuilder
	     * and returned as String.
	     */
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
		
	private String messageForCode(int code)
	{
		try{
			String codes[] = {
					"NOT_ENOUGH_CODE", "CANNOT_DECODE", "SINGLE_BAD_MATCH", 
					"SINGLE_GOOD_MATCH", "NO_RESULTS", "MULTIPLE_GOOD_MATCH_HISTOGRAM_INCREASED",
					"MULTIPLE_GOOD_MATCH_HISTOGRAM_DECREASED", "MULTIPLE_BAD_HISTOGRAM_MATCH", "MULTIPLE_GOOD_MATCH"
					}; 
	
			return codes[code];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return "UNKNOWN";
		}
	}
	
	private void didFinishListening()
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFinishListening();
				}
			});
		}
		else
			listener.didFinishListening();
	}
	
	private void didFinishListeningPass()
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFinishListeningPass();
				}
			});
		}
		else
			listener.didFinishListeningPass();
	}
	
	private void willStartListening()
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.willStartListening();
				}
			});
		}
		else	
			listener.willStartListening();
	}
	
	private void willStartListeningPass()
	{
		if(listener == null)
			return;
			
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.willStartListeningPass();
				}
			});
		}
		else
			listener.willStartListeningPass();
	}
	
	private void didGenerateFingerprintCode(final String code)
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didGenerateFingerprintCode(code);
				}
			});
		}
		else
			listener.didGenerateFingerprintCode(code);
	}
	
	private void didFindMatchForCode(final Hashtable<String, String> table, final String code)
	{
		if(listener == null)
			return;
			
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFindMatchForCode(table, code);
				}
			});
		}
		else
			listener.didFindMatchForCode(table, code);
	}
	
	private void didNotFindMatchForCode(final String code)
	{
		if(listener == null)
			return;
		
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didNotFindMatchForCode(code);
				}
			});
		}
		else
			listener.didNotFindMatchForCode(code);
	}
	
	private void didFailWithException(final Exception e)
	{
		if(listener == null)
			return;
			
		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() 
			{		
				public void run() 
				{
					listener.didFailWithException(e);
				}
			});
		}
		else
			listener.didFailWithException(e);
	}
		
	/**
	 * Interface for the fingerprinter listener<br>
	 * Contains the different delegate methods for the fingerprinting process
	 * @author Alex Restrepo
	 *
	 */
	public interface AudioFingerprinterListener
	{		
		/**
		 * Called when the fingerprinter process loop has finished
		 */
		public void didFinishListening();
		
		/**
		 * Called when a single fingerprinter pass has finished
		 */
		public void didFinishListeningPass();
		
		/**
		 * Called when the fingerprinter is about to start
		 */
		public void willStartListening();
		
		/**
		 * Called when a single listening pass is about to start
		 */
		public void willStartListeningPass();
		
		/**
		 * Called when the codegen libary generates a fingerprint code
		 * @param code the generated fingerprint as a zcompressed, base64 string
		 */
		public void didGenerateFingerprintCode(String code);
		
		/**
		 * Called if the server finds a match for the submitted fingerprint code 
		 * @param table a hashtable with the metadata returned from the server
		 * @param code the submited fingerprint code
		 */
		public void didFindMatchForCode(Hashtable<String, String> table, String code);
		
		/**
		 * Called if the server DOES NOT find a match for the submitted fingerprint code
		 * @param code the submited fingerprint code
		 */
		public void didNotFindMatchForCode(String code);
		
		/**
		 * Called if there is an error / exception in the fingerprinting process
		 * @param e an exception with the error
		 */
		public void didFailWithException(Exception e);
	}
}
