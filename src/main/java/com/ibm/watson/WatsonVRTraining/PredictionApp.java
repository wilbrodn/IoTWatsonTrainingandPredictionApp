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
 * Main class to launch the Scavenger hunt game.
 */


package com.ibm.watson.WatsonVRTraining;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.ibm.watson.WatsonVRTraining.CloudantNoSQLDB.DBCommunicator;
import com.ibm.watson.WatsonVRTraining.iot.util.IoTUtil;
import com.ibm.watson.WatsonVRTraining.speechToText.SpeechToTextWebSocketMain;
import com.ibm.watson.WatsonVRTraining.textToSpeech.TTSMain;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.camera.JavaImageCapture;
import com.ibm.watson.WatsonVRTraining.util.images.PhotoCaptureFrame;
import com.ibm.watson.WatsonVRTraining.util.images.WatchDir;

public class PredictionApp 
{
	Logger LOG = Logger.getLogger(PredictionApp.class.getName());

	
    public static void main( String[] args ) throws Exception
    {
    	    	PredictionApp.getInstance().startGame();
    }    
    
    static PredictionApp obj = null;
    public static PredictionApp getInstance(){
    	if(obj == null){
    		obj=new PredictionApp();
    	}
    	return obj;
    }
    
    public TTSMain tts = null;
    public SpeechToTextWebSocketMain stt = null;
    public DBCommunicator dbsvc = null;
    public IoTUtil iotObj = null;
    public int unique_app_id = 0;
    public String random_img_obj_str = null;
    
    public void loadServices() throws MalformedURLException
    {
    	/* startup all Below watson services:
    	 * 
    	 * a. IBM Watson Text to Speech
    	 * b. IBM Watson Speech to Text
    	 * c. IBM Watson Cloudant No SQL DB service
    	 * d. IBM Watson IoT connect
    	 */
		tts = new TTSMain(AppConstants.TTS_api_key,AppConstants.TTS_url);
		//stt = new SpeechToTextWebSocketMain(AppConstants.STT_api_key,AppConstants.STT_url);
		dbsvc = new DBCommunicator(AppConstants.cloudant_uname,AppConstants.cloudant_pass,AppConstants.cloudant_url,AppConstants.cloudant_dbName);
		iotObj = new IoTUtil();
    }
    

    void startGame()
    {
    	try{
    		loadServices();
		tts.playTextToSpeech("welcome to IBM Cloud platform.");
		
		Thread hearingThread = new Thread() {
			
			public void run() {
				//stt.startSTT();
				JavaImageCapture startCap = new JavaImageCapture(AppConstants.vr_process_img_dir,"tmp",PredictionApp.getInstance(),AppConstants.time_frame);
				PhotoCaptureFrame.getPhotoesJFrame().setVisible(true);
				SwingUtilities.invokeLater(startCap);
			}
		};
		
		//start the IBM Watson STT service thread		
		hearingThread.start();
		
		final Path dir = Paths.get(AppConstants.tmp_image_dir.toURI());

		Thread dirWatchThread = new Thread(){

		@Override
		public void run() {
			try{
	        new WatchDir(dir, true).processEvents();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		};
				
		//start the Watch thread to look after IMG file creation/updation in tmp_image_dir_path directory path. 
		dirWatchThread.start();
		
		
    	}catch(RuntimeException e)
    	{
    		LOG.log(Level.SEVERE,"looks like internet/service connectivity issue");
    		e.printStackTrace();
    	} catch (MalformedURLException e1) {
    		LOG.log(Level.SEVERE,"looks like some connectivity issue with DB service please check it.");
			e1.printStackTrace();
		}
    }
}
