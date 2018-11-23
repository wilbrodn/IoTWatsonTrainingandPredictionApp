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
 * Main class for speech to text using Web Socket Mecahnism
 * used with default configuration configured for EN-US
 *  
 */

package com.ibm.watson.WatsonVRTraining.speechToText;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.ibm.watson.WatsonVRTraining.PredictionApp;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.camera.JavaImageCapture;
import com.ibm.watson.WatsonVRTraining.util.images.PhotoCaptureFrame;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;

public class SpeechToTextWebSocketMain {
	private static Logger LOGGER = Logger.getLogger(SpeechToTextWebSocketMain.class.getName());
	SpeechToText sttsvc = null;
	public SpeechToTextWebSocketMain(String apiKey, String url)
	{
		IamOptions options = new IamOptions.Builder().apiKey(apiKey).build();
		sttsvc = new SpeechToText(options);
		sttsvc.setEndPoint(url);
	}
		
	public void startSTT()
	{
		try{
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, getAudioFormat());

            if (!AudioSystem.isLineSupported(info)) {
            	LOGGER.log(Level.SEVERE,"Line not supported");
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(getAudioFormat());
            line.start();

            AudioInputStream audio = new AudioInputStream(line);
            
		sttsvc.recognizeUsingWebSocket(getRecognizeOptions(audio),new MicrophoneRecognizeDelegate());
		//LOGGER.log(Level.FINE,"callBack --------------- "+callback.toString());
		}catch (LineUnavailableException e) {
			LOGGER.log(Level.SEVERE,"Line not available");
			e.printStackTrace();
		}
	}
	
    private RecognizeOptions getRecognizeOptions(AudioInputStream audio) {
        return new RecognizeOptions.Builder()
                //.continuous(true)
        		.audio(audio)
                .contentType(HttpMediaType.AUDIO_RAW+"; rate=16000; channels=1")
                .interimResults(true)
                .inactivityTimeout(-1)
                .build();
    }
    
    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }


    private class MicrophoneRecognizeDelegate implements RecognizeCallback {

		public void onConnected() {
			LOGGER.log(Level.INFO, "connected........please speak now");

		}

		public void onDisconnected() {
			LOGGER.log(Level.INFO, "disconnected........");	
		}

		public void onError(Exception arg0) {
			LOGGER.log(Level.SEVERE, "error........"+arg0.getMessage());
			JOptionPane.showMessageDialog(null,"some error in STT svc initialization "+arg0.getMessage(),"STT error",JOptionPane.ERROR_MESSAGE);
			arg0.printStackTrace();
			System.exit(0);
		}

		public void onTranscription(SpeechRecognitionResults speechResults) {
			//LOGGER.log(Level.INFO, "in Transcription now "+speechResults);
            if(speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                LOGGER.log(Level.INFO,text);
                if(text.toLowerCase().contains("prediction") || text.toLowerCase().contains("result"))
                {
                	/*
                	 * get random objects for which imgs has to be captured for
                	 */
                	JavaImageCapture startCap = new JavaImageCapture(AppConstants.vr_process_img_dir,"tmp",PredictionApp.getInstance(),AppConstants.time_frame);
					PhotoCaptureFrame.getPhotoesJFrame().setVisible(true);
					SwingUtilities.invokeLater(startCap);
                }
                if(text.toLowerCase().contains("i am done") || text.toLowerCase().contains("exit") || text.toLowerCase().contains("i'm done") || 
                		text.toLowerCase().contains("close"))
                {
                	PredictionApp.getInstance().tts.playTextToSpeech("Thanks for using this application. Hoping to see you soon on IBM Watson cloud platform.");
                	PredictionApp.getInstance().iotObj.closeIOTConnection();
                	System.exit(0);
                }
            }
		}

		public void onInactivityTimeout(RuntimeException arg0) {
			LOGGER.log(Level.SEVERE, "InactivityTimeout");
			arg0.printStackTrace();
		}

		public void onListening() {
			LOGGER.log(Level.INFO, "now listening");
		}

		public void onTranscriptionComplete() {
			// TODO Auto-generated method stub
			
		}
    }
}
