 package org.eclipse.stem.diseasemodels.experimental.impl;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.FileWriter;
 import java.io.IOException;
 
import org.eclipse.stem.diseasemodels.Activator;

  class LogDiseaseState {
 
     private static FileWriter fw;
 
     /**
      * Constructor
      * @param fileName
      */
 	public LogDiseaseState(String fileName) {
 		try {
 			fw = new FileWriter(fileName);
 	} catch (IOException e) {
				Activator.logInformation("Error creating file writer "+e.getMessage(),e);
 			    e.printStackTrace();
 			    System.exit(1);
 	}
 	}
 	
 
 /**
  * write string
  * @param str
  */
 public void write(String str) {
 	try {
 		 //System.out.print(str);
 	     fw.write(str); 
 	 } catch (IOException e) {
 			System.out.println("Error writing to file writer "+e.getMessage());
 		    e.printStackTrace();
 		    System.exit(1);
 	  }// try
 } // write
 
 /**
  * close
  */
 public static void close() {
 			
 	try {
 		  fw.flush();
 	      fw.close();
 	} catch (IOException e) {
 			System.out.println("Error closing file writer "+e.getMessage());
 		    e.printStackTrace();
 	}// try
 }
 	
 
 } // LogDiseaseState
