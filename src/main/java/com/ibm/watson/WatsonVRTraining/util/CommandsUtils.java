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
 * Utility class to provide capability to execute shell commands through java.
 */

package com.ibm.watson.WatsonVRTraining.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandsUtils {
	Logger LOG = Logger.getLogger(CommandsUtils.class.getName());
	
    public String executeCommand(String... command) {

		StringBuffer output = new StringBuffer();

		/*Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			//ProcessBuilder pcss = new ProcessBuilder(command);
			//p = pcss.start();
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}*/

    	Process process;
		try {
			process = new ProcessBuilder(command).start();
    	InputStream is = process.getInputStream();
    	InputStreamReader isr = new InputStreamReader(is);
    	BufferedReader br = new BufferedReader(isr);
    	String line="";

    	LOG.log(Level.INFO, "retreiving result for \""+command[2].toString()+"\" : ");

    	while ((line = br.readLine()) != null) {
    	  output.append(line);
    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	LOG.log(Level.INFO, "Output of running "+output.toString());
		return output.toString();
	}
}
