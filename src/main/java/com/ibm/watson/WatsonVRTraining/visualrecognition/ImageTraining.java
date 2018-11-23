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
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.watson.WatsonVRTraining.ImageTrainingApp;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.TrainingAppException;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Classifier;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.CreateClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DeleteClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.GetClassifierOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ListClassifiersOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.UpdateClassifierOptions;

public class ImageTraining {
	Logger LOGGER = Logger.getLogger(ImageTraining.class.getName());
	
	String class_name=null,positiveZipPath=null,negativeZipPath=null;
	public ImageTraining(String class_name,String positiveZipPath,String negativeZipPath){
		this.class_name = class_name;
		this.positiveZipPath = positiveZipPath;
		this.negativeZipPath = negativeZipPath;
	}
	
	public void createClassifier() throws TrainingAppException
	{	
	    CreateClassifierOptions classifierOptions=null;
		try {
			classifierOptions = new CreateClassifierOptions.Builder()
					.name(AppConstants.vr_classifier_name)
					.addClass(class_name, new File(positiveZipPath))
					.negativeExamples(new File(negativeZipPath))
					.build();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
	    
	    boolean create_classifier_flag = true, update_classifier_flage=false,exit_loop=false;
	    String classifier_id=null;
	    IamOptions opts = new IamOptions.Builder()
				.apiKey(AppConstants.vr_APIKey)
				.build();
		VisualRecognition vr_svc = new VisualRecognition(AppConstants.vr_version,opts);
		 List<Classifier> clasifiers= vr_svc.listClassifiers(new ListClassifiersOptions.Builder().verbose(true).build()).execute().getClassifiers();
		 Iterator<Classifier> classifiers_it=clasifiers.iterator();
	    
	    LOGGER.log(Level.INFO,"existing classifiers count  "+clasifiers.size());
	    
	    while(classifiers_it.hasNext()){
	    	Classifier classifier = classifiers_it.next();
	    	LOGGER.log(Level.INFO,"classifier varification");
	    	LOGGER.log(Level.INFO,"checking for "+classifier.getName());
	    	if(classifier.getName().equalsIgnoreCase(AppConstants.vr_classifier_name))
	    	{
	    	create_classifier_flag = false;
	    	classifier_id = classifier.getClassifierId();
	    	LOGGER.log(Level.INFO,"found pre-existing "+classifier.getName()+":"+classifier_id);
	    	int cnt=0;
	    	Iterator<com.ibm.watson.developer_cloud.visual_recognition.v3.model.Class> it = classifier.getClasses().iterator();
		    while(it.hasNext())
		    	{
		    	com.ibm.watson.developer_cloud.visual_recognition.v3.model.Class claz = it.next();
		    		cnt++;
		    		LOGGER.log(Level.INFO," checking for "+classifier_id+"."+claz.getClassName());
		    		if(claz.getClassName().equalsIgnoreCase(class_name)){
		    			LOGGER.log(Level.INFO," found preexisting "+classifier_id+"."+claz.getClassName());
		    			ImageTrainingApp.getInstance().tts.playTextToSpeech("looks like given class name already exists. Please try giving different class name.");
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
	    	LOGGER.log(Level.INFO,"creating new classifier "+classifierOptions.name());
	    	ImageTrainingApp.getInstance().tts.playTextToSpeech("looks like you are training very first time. Let me train the model for you.");
	    	Classifier vc = vr_svc.createClassifier(classifierOptions).execute();
	    	int count=0;
			while(true){
				count++;
				String res = null;
				try {
					Thread.sleep(6000);
					try{
					GetClassifierOptions get_classifier = new GetClassifierOptions.Builder()
							.classifierId(vc.getClassifierId())
							.build();
					res = vr_svc.getClassifier(get_classifier).execute().getStatus().toString();
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
						DeleteClassifierOptions del_classifier = new DeleteClassifierOptions.Builder()
								.classifierId(vc.getClassifierId())
								.build();
						vr_svc.deleteClassifier(del_classifier);
						//System.exit(0);
		    			throw new TrainingAppException();
					}
					
					if(count == 3){
						ImageTrainingApp.getInstance().tts.playTextToSpeech("classifier is being trained. please try using it after some time.");
						throw new TrainingAppException();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }

	    if(update_classifier_flage && classifier_id!=null){
	    	LOGGER.log(Level.INFO,"updating classifier "+classifierOptions.name());
	    	ImageTrainingApp.getInstance().tts.playTextToSpeech("Let me update classifier for you.");
	    	UpdateClassifierOptions update_classifier=null;
			try {
				update_classifier = new UpdateClassifierOptions.Builder()
						.classifierId(classifier_id)
						.addClass(class_name, new File(positiveZipPath))
						.negativeExamples(new File(negativeZipPath))
						.build();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
	    	Classifier vc = vr_svc.updateClassifier(update_classifier).execute();
			int count=0;
	    	while(true){
	    		count++;
				String res = null;
				try {
					Thread.sleep(6000);
					try{
						GetClassifierOptions get_classifier = new GetClassifierOptions.Builder()
								.classifierId(vc.getClassifierId())
								.build();
					res = vr_svc.getClassifier(get_classifier).execute().getStatus().toString();
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
						DeleteClassifierOptions del_classifier = new DeleteClassifierOptions.Builder()
								.classifierId(vc.getClassifierId())
								.build();
						vr_svc.deleteClassifier(del_classifier);
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
