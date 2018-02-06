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
 * Util class to encode and decode the IMG.jpg file to Base-64.
 */
package com.ibm.watson.WatsonVRTraining.util.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

public class Base64EncoderDecoder {
	public String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = Base64.getEncoder().encodeToString(bytes);
            fileInputStreamReader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println(encodedfile);
        return encodedfile;
    }
	
	public File decodeFileToIMG(String base64encodedString)
	{
		byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64encodedString);
		BufferedImage img;
		File capturedImage = null;
		try {
			img = ImageIO.read(new ByteArrayInputStream(imageBytes));
		capturedImage = File.createTempFile("test",".jpg");
		ImageIO.write(img,"JPG", capturedImage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return capturedImage;
	}
	
	/*public static void main(String[] args) throws IOException
	{
		File f = new File("/Users/arpitrastogi/Downloads/2.jpg");
		System.out.println(f.getAbsolutePath());
		
		System.out.println(new Base64EncoderDecoder().encodeFileToBase64Binary(f));
		
		byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(new Base64EncoderDecoder().encodeFileToBase64Binary(f));
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
		File capturedImage = File.createTempFile("test",".jpg");
		ImageIO.write(img,"JPG", capturedImage);
		System.out.println(capturedImage.getAbsolutePath());
	}*/
}
