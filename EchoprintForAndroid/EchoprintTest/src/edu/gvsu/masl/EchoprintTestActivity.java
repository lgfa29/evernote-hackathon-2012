/**
 * EchoprintTestActivity.java
 * EchoprintTest
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

package edu.gvsu.masl;

import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.gvsu.masl.echoprint.AudioFingerprinter;
import edu.gvsu.masl.echoprint.AudioFingerprinter.AudioFingerprinterListener;


public class EchoprintTestActivity extends Activity implements AudioFingerprinterListener 
{	
	boolean recording, resolved;
	AudioFingerprinter fingerprinter;
	TextView status;
	TextView song_list;
	Button btn;
	String currentsong;
	AudioFingerprinterListener afListner;
	
    @Override       
    public void onCreate(Bundle savedInstanceState) 
    {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        afListner = this;
        
        btn = (Button) findViewById(R.id.recordButton);
        
        status = (TextView) findViewById(R.id.status);
        song_list = (TextView) findViewById(R.id.song_list);
        btn.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
            	System.out.println("Recording: "+recording);
                // Perform action on click
            	if(recording)
            	{            	
            		if (fingerprinter != null)
            			fingerprinter.stop();
        			recording = false;
            	}
            	else
            	{   
            		recording = true;
            		btn.setText("Stop");
            		
            		new Thread(new Runnable() {
            		    public void run() 
            		    {
            		    	System.out.println("LETS RECORD!");
            		    	while(recording){
            		    		System.out.println("OK!");
    	            			if(fingerprinter == null)
    		            			fingerprinter = new AudioFingerprinter(afListner);
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

            	           		
	            		
	            		//Now waits for 40 seconds
//	                    try {
//	                        synchronized(this){
//	                            wait(40000);
//	                        }
//	                    }
//	                    catch(InterruptedException ex){                    
//	                    }
            		
            	}
            }
        });
    }

	public void didFinishListening() 
	{					
		btn.setText("Start");
		
		if(!resolved)
			status.setText("Idle...");
		
		//recording = false;
	}
	
	public void didFinishListeningPass()
	{}

	public void willStartListening() 
	{
		status.setText("Listening...");
		btn.setText("Stop");
		//recording = true;
		resolved = false;
	}

	public void willStartListeningPass() 
	{}

	public void didGenerateFingerprintCode(String code) 
	{
		status.setText("Will fetch info for code starting:\n" + code.substring(0, Math.min(50, code.length())));
	}

	public void didFindMatchForCode(final Hashtable<String, String> table,
			String code) 
	{
		resolved = true;
		status.setText("Match: \n" + table);
		String song = table.get("artist_name") + " - " + table.get("title");
		String id = table.get("id");
		System.out.println("[ID] "+id);
		System.out.println("[CURRENT SONG] "+currentsong);
		if (!id.equals(currentsong)){
			currentsong = id;
			song_list.setText(song_list.getText() + "\n" + song);
		}
		
	}

	public void didNotFindMatchForCode(String code) 
	{
		resolved = true;
		status.setText("No match for code starting with: \n" + code.substring(0, Math.min(50, code.length())));
	}

	public void didFailWithException(Exception e) 
	{
		resolved = true;
		status.setText("Error: " + e);
	}
}