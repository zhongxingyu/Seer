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
 package org.eclipse.releng;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 public class GenerateExcludeListTask extends Task {
 
 	private ArrayList a = new ArrayList();
 	private String mapFile;
 	private String outputFile;
 			
 	public GenerateExcludeListTask() {
 		super();
 	}
 	
 	public static void main(String[] args) {		
 		GenerateExcludeListTask parser = new GenerateExcludeListTask();
 		parser.setMapFile("c:\\temp\\orbit.map");
 		parser.setOutputFile("c:\\temp\\orbit.map.new");
 		parser.execute();			
 	}
 	
 	public String getOutputFile() {
 		return outputFile;
 	}
 
 	public void setOutputFile(String outputFile) {
 		this.outputFile = outputFile;
 	}
 
 	public String getMapFile() {
 		return mapFile;
 	}
 
 	public void setMapFile(String mapFile) {
 		this.mapFile = mapFile;
 	}
 
 	public void execute() throws BuildException {		
 		readMap();
 		writeProp();
 	}
 
 	private void readMap() {
 		try {
 			BufferedReader r = new BufferedReader(new FileReader(
 					mapFile));
 			String line;
 			while ((line = r.readLine()) != null) {
 				int start = line.lastIndexOf('/');
 				int lastcomma = line.lastIndexOf(',');
 				int end = line.length();
 				if (lastcomma > start) {
 					end = lastcomma;
 				}
 				int lastzip = line.lastIndexOf(".zip");
				if (lastzip > start) {
					end = lastzip;
 				}
 				if ((start < end) && (start > 0)) {
 					String substr = line.substring(start + 1, end);
 					a.add(substr);
 				}
 			}
 			r.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeProp() {
 
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(
 					outputFile));			
 			for (Iterator iterator = a.iterator(); iterator.hasNext();) {				
 				String s = iterator.next().toString();
 				if (iterator.hasNext()) {
 				out.write("eclipse/plugins/" + s + ",");
 				} else {
 					out.write("eclipse/plugins/" + s);
 				}
 			}
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 
 }
