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
 * Main class to launch the App to create custom classifier.
 */
package com.ibm.watson.WatsonVRTraining;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.ibm.watson.WatsonVRTraining.textToSpeech.TTSMain;
import com.ibm.watson.WatsonVRTraining.util.CommandsUtils;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.ThreadMessage_hook;
import com.ibm.watson.WatsonVRTraining.util.camera.JavaImageCapture;
import com.ibm.watson.WatsonVRTraining.visualrecognition.VRMain;

public class ImageTrainingApp {

	public static void main(String[] args) {
    ImageTrainingApp.getInstance().startTrainingApp();
	}
	
	static ImageTrainingApp obj = null;
	public static ImageTrainingApp getInstance(){
		if(obj == null){
			obj = new ImageTrainingApp();
		}
		return obj;
	}
	
    public TTSMain tts = null;
    public VRMain vr_svc = null;

	public void startTrainingApp()
	{
		/*
		 * start all required below IBM Watson services:
		 * a. Text To Speech
		 */
		tts = new TTSMain(AppConstants.TTS_uname,AppConstants.TTS_pass);
		vr_svc = new VRMain(AppConstants.vr_version,AppConstants.vr_APIKey);
		
		/*announce the welcome message*/
		tts.playTextToSpeech("to train the model you need to give at least twenty or more images for each classifier. the more clear images"
        		+ "you give. will increase the prediction accuracy of image. ");
		
		int i=0;
		while(i<=AppConstants.classifier_count){
       	/*get the custom classifier name which we are going to create*/
       	String class_name = null,negative_zip=null;
		final String default_negative_zip = AppConstants.vr_negative_example_zip;
       	int chances = 1;
       	do { 
       		
       		/*
       		 * swing UI starts here
       		 */
    		JTextField classifier_name = new JTextField();
    		final JLabel zip_varification_label = new JLabel();
    	      final JTextField negative_zip_path = new JTextField("default is "+default_negative_zip);
    	      negative_zip_path.addFocusListener(new FocusListener() {
    			
    			public void focusLost(FocusEvent e) {
    				if(!negative_zip_path.getText().equals("default is "+default_negative_zip) && 
    						!negative_zip_path.getText().trim().equals("") &&
    						!negative_zip_path.getText().equals("null") &&
    						negative_zip_path.getText() != null &&
    						negative_zip_path.getText().toLowerCase().endsWith(".zip"))
    				
    				{
    					if(new CommandsUtils().executeCommand("bash","-c","ls "+negative_zip_path.getText().trim()).trim().equals("")){
    						zip_varification_label.setText("*invalid path/zip default considered");
    						negative_zip_path.setText("default is "+default_negative_zip);
    					}
    					else {
        					negative_zip_path.setText(negative_zip_path.getText());
    						zip_varification_label.setText("");
    					}
    				}
    				else{
    					negative_zip_path.setText("default is "+default_negative_zip);
    				}
    			}
    			
    			public void focusGained(FocusEvent e) {
    				if(negative_zip_path.getText().equals("default is "+default_negative_zip)){
    					negative_zip_path.setText("");
    				}
    			}
    		});

    	      JPanel myPanel = new JPanel();
    	      myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.PAGE_AXIS));
    	      myPanel.add(new JLabel("class name(at least 5 chars):"));
    	      myPanel.add(classifier_name);
    	      myPanel.add(Box.createVerticalBox()); // a spacer
    	      myPanel.add(new JLabel("negative Imgs zip path:"));
    	      myPanel.add(negative_zip_path);
    	      myPanel.add(zip_varification_label);

    	      int result = JOptionPane.showConfirmDialog(null, myPanel, 
    	               "Please Enter Below Values", JOptionPane.OK_CANCEL_OPTION);
    	      if (result == JOptionPane.OK_OPTION) {
    	       		class_name = classifier_name.getText().trim();
    	       		if (negative_zip_path.getText().equals("default is "+default_negative_zip))
    	       			negative_zip = default_negative_zip;
    	       		else
    	       			negative_zip = negative_zip_path.getText().trim();
       	         System.out.println("classifier_name value: " + class_name);
       	         System.out.println("negative_zip_path value: " + negative_zip);
    	      }	
    	      
    	      /*
    	       * swing UI ends here
    	       */

       		if(chances++ > 1)
       		{
       			tts.playTextToSpeech("you have tried maximum number of attempts to enter valid classifier name. system will exit now. Please try again later.");
       			System.exit(0);
       		}
       	}while(class_name == null || class_name.trim().equals("") || class_name.equals(null) || class_name.length()<5);
        	JavaImageCapture startCap = new JavaImageCapture(AppConstants.vr_train_img_dir,class_name,ImageTrainingApp.getInstance(),negative_zip);
			synchronized(ThreadMessage_hook.classifier_shutDown_hook_obj){
        	try {
                /* start the camera capture window thread to capture the image*/
				SwingUtilities.invokeAndWait(startCap);
				if(ThreadMessage_hook.classifier_shutDown_hook_obj.getMsg().equals("create classifier")){
					ThreadMessage_hook.classifier_shutDown_hook_obj.wait();
					System.out.println(ThreadMessage_hook.classifier_shutDown_hook_obj.getMsg());
					ThreadMessage_hook.classifier_shutDown_hook_obj.setMsg("create classifier");
				}
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}	
			i++;
			if(i == AppConstants.classifier_count){
				tts.playTextToSpeech("Thanks for using this application. Hoping to see you soon on IBM Cloud.");
				System.exit(0);
			}
		}
	}
}