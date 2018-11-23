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

/**
 * This is entry point main method to start any of below application <br>
 * 1. train --> to create and train the image classifier<br>
 * 2. scavenger --> to play with game<br>
 * It uses the Java Client Library for <br>
 * a. IBM Watson IoT Platform<br>
 * b. IBM Watson Text to Speech<br>
 * c. IBM Watson Speech to Text<br>
 * d. IBM Watson Visual Recognition<br>
 * 
 * This sample code should be executed in a JRE running on the device
 * 
 * and can be launched from command prompt in conventional way:<br>
 * example:
 * 
 * java -cp scavengerHunt-0.0.1-SNAPSHOT-jar-with-dependencies.jar:scavengerHunt-0.0.1-SNAPSHOT.jar com.ibm.watson.scavenger.LaunchApp sample-properties.properties [scavenger|train]
 * 
 */

package com.ibm.watson.WatsonVRTraining;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

public class LaunchApp {

	private static Properties loadedProperties = new Properties();
	public static boolean isTraining=true;

	public static void main(String[] args) 
	{
		if(args.length <= 1){
			System.out.println("Usage\n com.ibm.watson.WatsonVRTraining.LaunchApp [properties file path] ['trainApp' | 'predictionApp']");
			System.exit(0);
		}
			System.out.println(args[0]+" and "+args[1]);
	if(args[0].endsWith(".properties")){
		loadProperties(args[0].trim());
		
	} else {
		JOptionPane.showMessageDialog(null,"Properties file path might not given properly","ApplicationError",JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	
	if(args[1].toLowerCase().trim().equals("trainapp")){
		ImageTrainingApp.getInstance().startTrainingApp();
	}else if(args[1].toLowerCase().trim().equals("predictionapp")){
		isTraining=false;
		PredictionApp.getInstance().startGame();
	}else{
		JOptionPane.showMessageDialog(null,"app name can be one of 'trainApp' or 'predictionApp'","ApplicationError",JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	}
	}
	
	public static Properties getLoadedProperties(){
		return loadedProperties;
	}
	
	private static void loadProperties(String propertiesFilePath){
    	InputStream input = null;
    	try {
    		input = new FileInputStream(propertiesFilePath);
    		loadedProperties.load(input);
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	} finally {
    		if (input != null) {
    			try {
    				input.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
	}
}
