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
 * IoT utlility class to:<br>
 * a. connect to Watson IoT to send event to IoT configured device.<br> 
 * b. to receive commands from registered IoT Node-Red device.
 */

package com.ibm.watson.WatsonVRTraining.iot.util;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.watson.WatsonVRTraining.PredictionApp;
import com.ibm.watson.WatsonVRTraining.CloudantNoSQLDB.JSonDocumentTemplateClass;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
import com.ibm.watson.WatsonVRTraining.util.images.Base64EncoderDecoder;
import com.ibm.watson.WatsonVRTraining.util.images.PhotoCaptureFrame;
import com.ibm.watson.WatsonVRTraining.util.images.WatchDir;

public class IoTUtil {
	Properties options = new Properties();
	DeviceClient myClient = null;
	public IoTUtil()
	{
		//load all required configuration properties from properties file. 
		options.setProperty("Organization-ID", AppConstants.iot_Organization_ID);
		options.setProperty("type", AppConstants.iot_device_type);
		options.setProperty("id", AppConstants.iot_device_id);
		options.setProperty("Authentication-Token", AppConstants.iot_Authentication_Token);
		options.setProperty("Authentication-Method", AppConstants.iot_Authentication_Method);
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(options);
		
		//Connect to the IBM Watson IoT Platform
		myClient.connect();
		
		//start the call back thread to receive device-commands from IoT.
		MyNewCommandCallback callback = new MyNewCommandCallback();
		Thread t = new Thread(callback);
		t.start();
		myClient.setCommandCallback(callback);

		}catch(MqttException e){
			System.out.println("can not connect to IOT");
			JOptionPane.showMessageDialog(null,"can not connect to IoT !!","ApplicationError",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void publishEvent(JsonObject eventDataObj){
		myClient.publishEvent(AppConstants.iot_event_for_img_base64, eventDataObj, 0);
	}
	
	public void closeIOTConnection()
	{
		myClient.disconnect();
	}
}


/*
 * Utility class to receive device commands for registered device from IoT.
 */

class MyNewCommandCallback implements CommandCallback, Runnable {
	
	// A queue to hold & process the commands for smooth handling of MQTT messages
	private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
	
	/**
	 * This method is invoked by the library whenever there is command matching the subscription criteria
	 */
	public void processCommand(Command cmd) {
		try {
			queue.put(cmd);
			} catch (InterruptedException e) {
		}			
	}

	public void run() {
		boolean publish_flag = true;
		int score = 0;
		
				List<JSonDocumentTemplateClass> lst = PredictionApp.getInstance().dbsvc.getAllIMGsBase64(""+AppConstants.unique_app_id);
				if(!(lst.size() <= 0)){
					for(JSonDocumentTemplateClass obj:lst){
                	PhotoCaptureFrame.updateCaptureFrame(new Base64EncoderDecoder().decodeFileToIMG(obj.getImg_base64()),obj.getImg_result_html());
					PhotoCaptureFrame.getPhotoesJFrame().setVisible(true);
					PhotoCaptureFrame.getPhotoesJFrame().repaint();
					score = score + obj.getScore();
					}
				}

		while(true) {
			Command cmd = null;
			try {
				cmd = queue.take();
				System.out.println("COMMAND RECEIVED = '" + cmd.getCommand() + "'\t with data="+cmd.getData().toString());
				
				//refresh the Visual Recognition GUI with IBM Watson prediction results.
				if(cmd.getCommand().equals("refreshUI"))
				{
					JSonDocumentTemplateClass db_rec = PredictionApp.getInstance().dbsvc.getIMGBase64(cmd.getData().toString().trim());
                	PhotoCaptureFrame.updateCaptureFrame(new Base64EncoderDecoder().decodeFileToIMG(db_rec.getImg_base64()),db_rec.getImg_result_html());
					PhotoCaptureFrame.getPhotoesJFrame().setVisible(true);
					PhotoCaptureFrame.getPhotoesJFrame().repaint();
					publish_flag = true;
					PhotoCaptureFrame.getImagebeingProcessedLabel().setText("PROCESSING IMAGES:0");
					PhotoCaptureFrame.getImageRemainingProcessingLabel().setText("REMAINIG IMAGES:"+WatchDir.queue.size());
					score = score+db_rec.getScore();
				}
				
				if(cmd.getCommand().equals("checkForPublishIoT")){
					if(WatchDir.queue.size() > 0 && publish_flag)
					{
					try {
						publish_flag = false;
						URI uri = WatchDir.queue.take();
						System.out.println("posting to IoT:"+uri.toString());
						PhotoCaptureFrame.getImagebeingProcessedLabel().setText("PROCESSING IMAGES:1");
						PhotoCaptureFrame.getImageRemainingProcessingLabel().setText("REMAINIG IMAGES:"+WatchDir.queue.size());
						JsonObject event_payload = new JsonObject();
	            		event_payload.addProperty("img_base64",new Base64EncoderDecoder().encodeFileToBase64Binary(new File(uri)));
	            		event_payload.addProperty("img_id", new File(uri).getName());
	            		event_payload.addProperty("app_id",AppConstants.unique_app_id);
						PredictionApp.getInstance().iotObj.publishEvent(event_payload);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
				}
			} catch (InterruptedException e) {}
		}
	}
}
