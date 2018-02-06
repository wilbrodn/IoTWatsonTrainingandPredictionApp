/**
 *****************************************************************************
 * Copyright (c) 2017 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Arpit Rastogi - Initial Contribution
 *****************************************************************************
 */
/*
 * Main Text to Speech util class to connect it to IBM Cloud TTS service and playing audio using IoT device.
 */

package com.ibm.watson.WatsonVRTraining.textToSpeech;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.sound.JavaSoundPlayer;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

public class TTSMain {

	/*public static void main(String[] args) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		
		
		TTSMain obj = new TTSMain(ScavengerContants.TTS_uname,ScavengerContants.TTS_pass);
		obj.playTextToSpeech("Hello ! I am IBM watson artificially intelligent assisstence. "
				+ "To start the application you can say the keyword like game or scavenger hunt game or hunt game");
	}*/
	
	Logger LOG = Logger.getLogger(TTSMain.class.getName());
	
	TextToSpeech service = null;
	
	public TTSMain(String uname,String upass)
	{
		service = new TextToSpeech();
		service.setUsernameAndPassword(uname,upass);
	}
	
	public TTSMain(){
		
	}
	
	/*public void playTextToSpeechUsingWhisk(String txt){
    	String tmpStr = new CommandsUtils().executeCommand("/usr/local/bin/wsk","action", "invoke", "WatsonTTS", "--param", "message", txt);
    	String activationID = tmpStr.split(" ")[tmpStr.split(" ").length-1];
    	//System.out.println(activationID);
    	JsonObject res_payload = new JsonParser().parse(new CommandsUtils().executeCommand("/usr/local/bin/wsk","activation", "result",activationID)).getAsJsonObject();
    	System.out.println(res_payload.getAsJsonPrimitive("payload").getAsString().getBytes());
    	byte[] decoded = null;
			String encoded = res_payload.getAsJsonPrimitive("payload").getAsString();
			decoded = Base64.getDecoder().decode(encoded);
			try
		    {
		        File tmpWAV = File.createTempFile("tmp",".wav");
		        tmpWAV.deleteOnExit();
		        System.out.println(tmpWAV.getPath());
		        FileOutputStream os = new FileOutputStream(tmpWAV, true);
		        os.write(decoded);
		        os.close();
		        new JavaSoundPlayer().playWAVFile(tmpWAV);
		    }
		    catch (Exception e)
		    {
		        e.printStackTrace();
		    }

	}*/
	
	public void playTextToSpeech(String txt){
		
		
        InputStream stream = service.synthesize(txt, new Voice(AppConstants.TTS_name,AppConstants.TTS_gender,AppConstants.TTS_language),
        		new com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat("audio/wav")).execute();
        InputStream in=null;
        OutputStream out=null;
		try {
			in = WaveUtils.reWriteWaveHeader(stream);
        
        File tmpWAV = File.createTempFile("tmpTTS",".wav");
        LOG.log(Level.INFO,"audio tmp file is "+tmpWAV.toURI().toURL());
        tmpWAV.deleteOnExit();
        out = new FileOutputStream(tmpWAV);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
          out.write(buffer, 0, length);
        }
        
        new JavaSoundPlayer().playWAVFile(tmpWAV);
        
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        try {
				out.close();
	        in.close();
	        stream.close();	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
