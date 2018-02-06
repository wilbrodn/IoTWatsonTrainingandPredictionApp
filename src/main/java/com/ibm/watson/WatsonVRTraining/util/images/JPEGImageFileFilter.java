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
 * Filter class to select supported IMG file types.
 */
package com.ibm.watson.WatsonVRTraining.util.images;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class JPEGImageFileFilter extends FileFilter implements java.io.FileFilter
{
public boolean accept(File f)
  {
  if (f.getName().toLowerCase().endsWith(".jpeg")) return true;
  if (f.getName().toLowerCase().endsWith(".jpg")) return true;
  if (f.getName().toLowerCase().endsWith(".png")) return true;
  if(f.isDirectory())return true;
  return false;
 }
public String getDescription()
  {
  return "JPEG/PNG files";
  }

} 

