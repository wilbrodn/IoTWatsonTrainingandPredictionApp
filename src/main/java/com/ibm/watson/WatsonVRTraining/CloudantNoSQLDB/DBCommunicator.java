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
 * Cloudant DB connection utility class to retreive and store IMG-Base64 encoded string to DB.
 */

package com.ibm.watson.WatsonVRTraining.CloudantNoSQLDB;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

public class DBCommunicator {
	Logger logger = Logger.getLogger(DBCommunicator.class.getName());
	CloudantClient client = null;
	Database db = null;
	public DBCommunicator(String uname, String pass, String url,String db_name) throws MalformedURLException{
		client = ClientBuilder.account("Scavenger")
	            .username(uname)
	            .password(pass)
	            .url(new URL(url))
	            .build();
		
		db = client.database(db_name, true);
	}

	public void saveIMGBase64(JSonDocumentTemplateClass obj){
		logger.log(Level.INFO,"saving to DB");
		db.save(obj);
		logger.log(Level.INFO,"saved to DB");
	}
	
	public JSonDocumentTemplateClass getIMGBase64(String id){
		logger.log(Level.INFO,"fetching the result");
		return db.find(JSonDocumentTemplateClass.class,id);
	}
	
	public List<JSonDocumentTemplateClass> getAllIMGsBase64(String app_id){
		return db.findByIndex("{  \"selector\": {    \"_id\": {      \"$gt\": 0    },\"app_id\": {      \"$eq\": "+app_id+"    }  },  \"fields\": [    \"_id\",    \"img_base64\" ,\"random_img_obj_str\" ],  \"sort\": [    {      \"_id\": \"asc\"    }  ]}",JSonDocumentTemplateClass.class);
	}

	public List<JSonDocumentTemplateClass> getAllScores(String app_id){
		return db.findByIndex("{  \"selector\": {    \"_id\": {      \"$gt\": 0    },\"app_id\": {      \"$eq\": "+app_id+"    }, \"score\": {      \"$gt\": 0    }  },  \"fields\": [\"_id\",\"score\",\"random_img_obj_str\"],  \"sort\": [    {      \"_id\": \"asc\"    }  ]}",JSonDocumentTemplateClass.class);
	}
}
