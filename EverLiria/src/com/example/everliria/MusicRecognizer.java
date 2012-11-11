package com.example.everliria;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;
import edu.gvsu.masl.echoprint.AudioFingerprinter;
import edu.gvsu.masl.echoprint.AudioFingerprinter.AudioFingerprinterListener;

public class MusicRecognizer extends Activity implements AudioFingerprinterListener{
	
	boolean recording, resolved;
	AudioFingerprinter fingerprinter;
	TextView status;
	List<String> song_list;
	Button btn;
	String currentsong;
	AudioFingerprinterListener afListener;
	
	public void startListening(){
		
		song_list = new ArrayList<String>();
		afListener = this;
		
		new Thread(new Runnable() {
		    public void run() 
		    {
		    	System.out.println("LETS RECORD!");
		    	while(recording){
        			if(fingerprinter == null)
            			fingerprinter = new AudioFingerprinter(afListener);
            		System.out.println(">>Fingerprint");
            		fingerprinter.fingerprint(20);
            		//Now waits for 40 seconds
                    try {
                        synchronized(this){
                            wait(40000);
                        }
                    }
                    catch(InterruptedException ex){                    
                    }
    			}
		    }
		}).start();
	}
	
	public void stopListening(){
		if (fingerprinter != null)
			fingerprinter.stop();
		recording = false;
	}
	
	public List<String> getMusicList(){
		return song_list;
	}
	
	public void didFinishListening() {					
		//btn.setText("Start");
		
		if(!resolved)
			System.out.println("Idle...");
			//status.setText("Idle...");
		
		//recording = false;
	}
	
	public void didFinishListeningPass()
	{}

	public void willStartListening() {
		//status.setText("Listening...");
		System.out.println("Listening...");
		//btn.setText("Stop");
		//recording = true;
		resolved = false;
	}

	public void willStartListeningPass() 
	{}

	public void didGenerateFingerprintCode(String code) {
		//status.setText("Will fetch info for code starting:\n" + code.substring(0, Math.min(50, code.length())));
	}

	public void didFindMatchForCode(final Hashtable<String, String> table,
			String code) {
		resolved = true;
		//status.setText("Match: \n" + table);
		String song = table.get("artist_name") + " - " + table.get("title");
		String id = table.get("id");
		System.out.println("[ID] "+id);
		System.out.println("[CURRENT SONG] "+currentsong);
		if (!id.equals(currentsong)){
			currentsong = id;
			song_list.add(song);
			//song_list.setText(song_list.getText() + "\n" + song);
		}
		
	}

	public void didNotFindMatchForCode(String code) {
		resolved = true;
		System.out.println("No match for code starting with: \n" + code.substring(0, Math.min(50, code.length())));
		//status.setText("No match for code starting with: \n" + code.substring(0, Math.min(50, code.length())));
	}

	public void didFailWithException(Exception e) {
		resolved = true;
		System.out.println("Error: " + e);
		//status.setText("Error: " + e);
	}

}
