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
 * template class for each Photo panel displayed on prediction window.
 */
package com.ibm.watson.WatsonVRTraining.util.images;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Photo extends JPanel{

    private static final long serialVersionUID = 1L;
    private String title;
    private ImageIcon image;
    private BufferedImage bimg;
    private String description;

    private JLabel titleLabel;
    private JLabel imageLabel;
    private JLabel descLabel;

    public Photo(String title, URL image, String description) {
        setLayout(new BorderLayout(5,5));
        //
        setBorder(BorderFactory.createEtchedBorder());
        setAlignmentX(CENTER_ALIGNMENT);
        //
        this.title = title; 
        this.titleLabel = new JLabel(title);
        Font f = this.titleLabel.getFont();
        f = f.deriveFont(Font.PLAIN);
        f = f.deriveFont(10f);
        this.titleLabel.setFont(f);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(titleLabel, BorderLayout.CENTER);
        //this.add(northPanel, BorderLayout.NORTH);
        
        this.image = new ImageIcon(image){
        	@Override
        	public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
//        		super.paintIcon(c, g, x, y);
        		g.drawImage(bimg, 0, 0, 200,200,null);
        	}
        	
        };
        
        try {
			this.bimg = ImageIO.read(image);
		} catch (IOException e) {
			e.printStackTrace();
		} 
        this.imageLabel = new JLabel(this.image){
        	@Override
        	protected void paintComponent(Graphics g) {
        		g.drawImage(bimg, 0,0,200,200,null);
        	}
        	@Override
        	public void repaint(Rectangle r) {
        		r.setBounds(0, 0, 200,200);
        	}
        };
        
        this.imageLabel.setPreferredSize(new Dimension(200,200));
        this.add(this.imageLabel, BorderLayout.CENTER);
        //
        this.description = description;
        this.descLabel = new JLabel(description);
        //this.descLabel.setPreferredSize(new Dimension(200,);
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(descLabel, BorderLayout.LINE_START);
        this.add(eastPanel, BorderLayout.LINE_END);
    }    

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    public ImageIcon getImage() {
        return image;
    }

    public void setImage(ImageIcon image) {
        this.image = image;
        repaint();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        repaint();
    }
}
