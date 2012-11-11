package com.example.everliria;

import java.util.Hashtable;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import edu.gvsu.masl.echoprint.AudioFingerprinter;
import edu.gvsu.masl.echoprint.AudioFingerprinter.AudioFingerprinterListener;

public class MusicRecognizer extends IntentService implements AudioFingerprinterListener{
	
	private static final String THREAD_NAME = "musicRecognizer";
	
	private static final int RECORD_TIME = 20;
	private static final int INTERVAL_TIME = 40;

	private boolean recording, resolved;
	private AudioFingerprinter fingerprinter;
	private String currentSongId;
	
	
	public MusicRecognizer() {
		super(THREAD_NAME);
	}
	
	public MusicRecognizer(String name) {
		super(THREAD_NAME);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		recording = false;
		resolved = false;
		currentSongId = "";
		
		if (fingerprinter == null)
			fingerprinter = new AudioFingerprinter(this);
		
		Log.d(Constants.LOG_TAG, "Chamou o intent!");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		if (fingerprinter != null)
			fingerprinter.stop();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(Constants.LOG_TAG, "Chegou um intent!");
		
		if (recording) {
			recording = false;
			if (fingerprinter != null)
				fingerprinter.stop();
			
		} else {
			recording = true;
			
			if (fingerprinter == null)
				fingerprinter = new AudioFingerprinter(this);
			
			while(recording) {
				try {
					fingerprinter.fingerprint(RECORD_TIME);
					Thread.sleep(INTERVAL_TIME*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void didFinishListening() {
		if(!resolved)
			Log.d(Constants.LOG_TAG, "Idle...");
	}

	public void willStartListening() {
		Log.d(Constants.LOG_TAG, "Listening...");
		resolved = false;
	}

	public void didGenerateFingerprintCode(String code) {
		Log.d(Constants.LOG_TAG, "Will fetch info for code starting:\n" + code.substring(0, Math.min(50, code.length())));
	}

	public void didFindMatchForCode(final Hashtable<String, String> table, String code) {
		resolved = true;
		String song = table.get("artist_name") + " - " + table.get("title");
		String id = table.get("id");
		
		Log.d(Constants.LOG_TAG, "[ID] " + id);
		Log.d(Constants.LOG_TAG, "[CURRENT SONG] " + currentSongId);
		
		if (!id.equals(currentSongId)){
			currentSongId = id;
			sendIntent(Constants.INTENT_ACTION_MUSIC_FOUND, song);
		}
		
	}

	public void didNotFindMatchForCode(String code) {
		resolved = true;
		
		Log.d(Constants.LOG_TAG,"No match for code starting with: \n" + code.substring(0, Math.min(50, code.length())));
		sendIntent(Constants.INTENT_ACTION_MUSIC_NOT_FOUND, "");
	}

	public void didFailWithException(Exception e) {
		resolved = true;
		
		e.printStackTrace();
		Log.e(Constants.LOG_TAG, "Error");
		sendIntent(Constants.INTENT_ACTION_ERROR, e.getMessage());
	}
	
	public void didFinishListeningPass() {}
	public void willStartListeningPass() {}
	
	private void sendIntent(String tag, String message) {
		Intent responseIntent = new Intent(Constants.INTENT_FILTER_TAG);
		responseIntent.putExtra(tag, message);
		getApplicationContext().sendBroadcast(responseIntent);
	}

}
