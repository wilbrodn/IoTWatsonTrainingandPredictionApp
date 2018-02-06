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
 * Camera capture (Prediction window) class to display the VR results with image as icon.
 * also calls the VR service for each image.
 */

package com.ibm.watson.WatsonVRTraining.util.images;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.commons.io.FilenameUtils;

import com.ibm.watson.WatsonVRTraining.util.CommandsUtils;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;

public class PhotoCaptureFrame extends JFrame {
	JPanel jp = null,headerPanel=null;
	JFrame f = null;
	JLabel ImageRemainingProcessingLabel=null,ImagebeingProcessedLabel=null,appIDLabel=null;
	Logger log = Logger.getLogger(PhotoCaptureFrame.class.getName());
	private static PhotoCaptureFrame obj = null;
	PhotoCaptureFrame(){
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(jp);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        scrollPane.setPreferredSize(new Dimension(dim.width/2-40,dim.height-117));
        
    	JButton btn = new JButton("Upload Image");
    	
    	btn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(System.getenv("user.home"));
		        fc.setFileFilter(new JPEGImageFileFilter());
		        int res = fc.showOpenDialog(null);
		        // We have an image!
		        try {
		            if (res == JFileChooser.APPROVE_OPTION) {
		                File file = fc.getSelectedFile();
		                //SharedResources.sharedCache.getCapturedImageList().add(file);
		                File tmpf_name = File.createTempFile("tmp", "."+FilenameUtils.getExtension(file.getName()));
		                System.out.println("cp "+file.getPath()+" "+AppConstants.vr_process_img_dir_path+File.separator+tmpf_name.getName());
		                new CommandsUtils().executeCommand("bash","-c","cp "+file.getPath()+" "+AppConstants.vr_process_img_dir_path+File.separator+tmpf_name.getName());
		            }
		        }catch (Exception iOException) {
		        }
				
			}
		});
        
    	ImageRemainingProcessingLabel = new JLabel("REMAINIG IMAGES:0");
    	ImageRemainingProcessingLabel.setHorizontalAlignment(SwingConstants.LEFT);
    	ImageRemainingProcessingLabel.setFont(new Font("Arial",Font.BOLD,13));
    	
    	ImagebeingProcessedLabel = new JLabel(" PROCESSING IMAGES:0");
    	ImagebeingProcessedLabel.setHorizontalAlignment(SwingConstants.LEFT);
    	ImagebeingProcessedLabel.setFont(new Font("Arial",Font.BOLD,13));

    	appIDLabel = new JLabel("APP-ID:"+AppConstants.unique_app_id);
    	appIDLabel.setHorizontalAlignment(SwingConstants.LEFT);
    	appIDLabel.setFont(new Font("Arial",Font.BOLD,13));

    	headerPanel = new JPanel(new FlowLayout());
    	headerPanel.add(ImageRemainingProcessingLabel);
    	headerPanel.add(ImagebeingProcessedLabel);
    	headerPanel.add(btn);
    	headerPanel.add(appIDLabel);
    	headerPanel.setSize(new Dimension(getWidth(),10));
    	
    	
        JPanel contentPane = new JPanel();
        contentPane.add(headerPanel);
        contentPane.add(scrollPane);
        f = new JFrame("IBM Watson Visual Prediction Window");
        f.setContentPane(contentPane);
        f.setSize(dim.width/2-30,dim.height-40);
        f.setLocation(dim.width/2,0);
        f.setResizable(false);
        f.setPreferredSize(new Dimension(dim.width/2-30,dim.height-60));
        f.setVisible(true);
	}
		
	public static JLabel getImageRemainingProcessingLabel()
	{
		if(obj == null){
			obj = new PhotoCaptureFrame();
		}
		return obj.ImageRemainingProcessingLabel;
	}
	
	public static JLabel getImagebeingProcessedLabel()
	{
		if(obj == null){
			obj = new PhotoCaptureFrame();
		}
		return obj.ImagebeingProcessedLabel;
	}

	public static JPanel getPhotoesJPanel()
	{
		if(obj == null){
			obj = new PhotoCaptureFrame();
		}
		return obj.jp;
	}

	public static JFrame getPhotoesJFrame()
	{
		if(obj == null){
			obj = new PhotoCaptureFrame();
		}
		return obj.f;
	}
	
	public static void updateCaptureFrame(File capturedImgFile,String img_result_html)
	{
				
		Photo photo=null;
		try {
			photo = new Photo("IBM Watson predictions for below image",capturedImgFile.toURI().toURL(),img_result_html);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		PhotoCaptureFrame.getPhotoesJPanel().add(photo);
		PhotoCaptureFrame.getPhotoesJFrame().repaint();
		PhotoCaptureFrame.getPhotoesJFrame().setVisible(true);
	}
}