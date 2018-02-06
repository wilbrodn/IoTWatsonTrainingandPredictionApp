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
 * Utility class to create/train custom classifier by passing negative and positive images zip file.<br>
 * negative file has to be specified in properties file(vr_negative_example_zip)<br>
 * if not specified default is australianterrier.zip will be taken.
 */

package com.ibm.watson.WatsonVRTraining.visualrecognition;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.watson.WatsonVRTraining.ImageTrainingApp;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.TrainingAppException;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier.VisualClass;

public class ImageTraining {
	Logger LOGGER = Logger.getLogger(ImageTraining.class.getName());
	synchronized public void createClassifier(String class_name,String positiveZipPath,String negativeZipPath) throws TrainingAppException
	{
		
	    ClassifierOptions classifierOptions = new ClassifierOptions.Builder().classifierName(AppConstants.vr_classifier_name)
	    		.negativeExamples(new File(negativeZipPath))
	    		.addClass(class_name, new File(positiveZipPath))
	    		.build();
	    
	    boolean create_classifier_flag = true, update_classifier_flage=false,exit_loop=false;
	    String classifier_id=null;
	    Iterator<VisualClassifier> classifiers_it = ImageTrainingApp.getInstance().vr_svc.getVRInstance().getClassifiers().execute().iterator();
	    while(classifiers_it.hasNext()){
	    	VisualClassifier classifier = classifiers_it.next();
	    	LOGGER.log(Level.INFO,"classifier varification");
	    	LOGGER.log(Level.INFO,"checking for "+classifier.getName());
	    	if(classifier.getName().equalsIgnoreCase(AppConstants.vr_classifier_name))
	    	{
	    	create_classifier_flag = false;
	    	classifier_id = classifier.getId();
	    	LOGGER.log(Level.INFO,"found pre-existing "+classifier.getName()+":"+classifier_id);
	    	int cnt=0;
		    	for(VisualClass claz:classifier.getClasses())
		    	{
		    		cnt++;
		    		LOGGER.log(Level.INFO," checking for "+classifier_id+"."+claz.getName());
		    		if(claz.getName().equalsIgnoreCase(class_name)){
		    			LOGGER.log(Level.INFO," found preexisting "+classifier_id+"."+claz.getName());
		    			ImageTrainingApp.getInstance().tts.playTextToSpeech("looks like given class name already exists. Please try giving different class name.");
		    			//System.exit(0);
		    			throw new TrainingAppException();
		    		}
		    		else if(cnt==classifier.getClasses().size()){
		    			update_classifier_flage = true;
		    			exit_loop =true;
		    		}
		    		if(exit_loop) break;
		    	}
		    	if(exit_loop)
		    	break;
	    	}
	    	if(exit_loop) break;
	    }
	    
	    if(create_classifier_flag){
	    	LOGGER.log(Level.INFO,"creating new classifier "+classifierOptions.classifierName());
	    	ImageTrainingApp.getInstance().tts.playTextToSpeech("looks like you are training very first time. Let me train the model for you.");
	    	VisualClassifier vc = ImageTrainingApp.getInstance().vr_svc.getVRInstance().createClassifier(classifierOptions).execute();
	    	int count=0;
			while(true){
				count++;
				String res = null;
				try {
					Thread.sleep(6000);
					try{
					res = ImageTrainingApp.getInstance().vr_svc.getVRInstance().getClassifier(vc.getId()).execute().getStatus().toString();
					}catch(Exception e){
						e.printStackTrace();
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier is being trained. please try using it after some time");
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					if((count == 2 && res.toLowerCase().contains("training")) || res.toLowerCase().contains("training")){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("please wait classifier is being trained.");
					}
					else if((count == 2 && res.toLowerCase().contains("ready")) || res.toLowerCase().contains("ready")){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier has been trained now.");
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					else if((count == 2 && res.toLowerCase().contains("fail")) || res.toLowerCase().contains("fail")){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("there was some error while creating classifier. Please retry again later.");
						ImageTrainingApp.getInstance().vr_svc.getVRInstance().deleteClassifier(vc.getId());
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					
					if(count == 3){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier is being trained. please try using it after some time.");
						//System.exit(0);
		    			throw new TrainingAppException();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }

	    if(update_classifier_flage && classifier_id!=null){
	    	LOGGER.log(Level.INFO,"updating classifier "+classifierOptions.classifierName());
	    	ImageTrainingApp.getInstance().tts.playTextToSpeech("Let me update classifier for you.");
	    	VisualClassifier vc = ImageTrainingApp.getInstance().vr_svc.getVRInstance().updateClassifier(classifier_id, classifierOptions).execute();
			int count=0;
	    	while(true){
	    		count++;
				String res = null;
				try {
					Thread.sleep(6000);
					try{
					res = ImageTrainingApp.getInstance().vr_svc.getVRInstance().getClassifier(vc.getId()).execute().getStatus().toString();
					}catch(Exception e){
						e.printStackTrace();
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier is being trained. please try using it after some time");
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					if((count==2 && res.toLowerCase().contains("training")) || res.toLowerCase().contains("training")){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("please wait classifier is being trained.");
					}
					else if((count==2 && res.toLowerCase().contains("ready")) || res.toLowerCase().contains("ready")){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier has been trained now. To create another classifier you need to rerun this application.");
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					else if((count==2 && res.toLowerCase().contains("fail")) || res.toLowerCase().contains("fail")){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("there was some error while creating classifier. Please try again later.");
						ImageTrainingApp.getInstance().vr_svc.getVRInstance().deleteClassifier(vc.getId());
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					if(count == 3){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier is being trained. please try using it after some time.");
						//System.exit(0);
		    			throw new TrainingAppException();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
	}
}
