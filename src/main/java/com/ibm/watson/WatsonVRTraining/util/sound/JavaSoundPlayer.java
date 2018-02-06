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
 * utility class to play audio wav file received from TTS bluemix service
 */
package com.ibm.watson.WatsonVRTraining.util.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

public class JavaSoundPlayer {
	Logger LOG = Logger.getLogger(JavaSoundPlayer.class.getName());
	
	public void playWAVFile(File file) throws FileNotFoundException
	{
        InputStream stream = new FileInputStream(file);
        File tmpWAV = null;
        InputStream in=null;
        OutputStream out=null;
		try {
			in = WaveUtils.reWriteWaveHeader(stream);
        
        tmpWAV = File.createTempFile("tmpTTS",".wav");
        LOG.log(Level.INFO,"audio tmp file is "+tmpWAV.toURI().toURL());
        tmpWAV.deleteOnExit();
        out = new FileOutputStream(tmpWAV);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
          out.write(buffer, 0, length);
        }
        
        /*AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tmpWAV);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = tmpWAV.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));
        LOG.log(Level.INFO,"audio tmp file play approx duration is "+durationInSeconds);
        long sleeptime = (long) durationInSeconds*1010;*/
        AudioInputStream ais = AudioSystem.getAudioInputStream(tmpWAV.toURI().toURL());
        Clip clip=null;
		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
        try {
            LOG.log(Level.INFO,"playing audio file "+tmpWAV.toURI().toURL());
			clip.open(ais);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
        //clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();

        final CountDownLatch latch = new CountDownLatch(1);
        clip.addLineListener(new LineListener() {
            
            public void update(LineEvent event) {
                if (event.getType().equals(LineEvent.Type.STOP)) {
                    event.getLine().close();
                    latch.countDown();
                }
            }
        });
        latch.await();
        
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
	        try {
				out.close();
	        in.close();
	        stream.close();
            LOG.log(Level.INFO,"finished audio playing "+tmpWAV.toURI().toURL());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
