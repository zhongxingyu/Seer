 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.performance.graph;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import junit.framework.AssertionFailedError;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.DB;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.SummaryEntry;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 
 /**
  * @author SDimitrov
  */
 public class Grapher {
 
     private static final int GRAPH_HEIGHT= 300;
     private static final int GRAPH_WIDTH= 800;
 
     String testResultDirectory;
     String outputDirectory;
     String referenceBuildId;
     String buildTypeFilter;
     Scenario [] scenarios;
     
     public Grapher() {
         super();
     }
     public Grapher(Scenario [] scenarios,String output,String reference) {
     	this.scenarios=scenarios;
     	outputDirectory=output;
     	referenceBuildId=reference;
     	run();
     }
 
     public void run() {
 
         Display display= Display.getDefault();
         Color black= display.getSystemColor(SWT.COLOR_BLACK);
         Color green= display.getSystemColor(SWT.COLOR_DARK_GREEN);
 
         new File(outputDirectory).mkdirs();
 
          for (int s= 0; s < scenarios.length; s++) {
             Scenario t= scenarios[s];
             String scenarioName= t.getScenarioName();
             Dim[] dimensions= t.getDimensions();
             for (int i= 0; i < dimensions.length; i++) {
                 Dim dim= dimensions[i];
                 String dimensionName= dim.getName();
                 LineGraph graph= new LineGraph(scenarioName + ": " + dimensionName, dim);
                 TimeSeries ts=null;
                 try{
                 ts= t.getTimeSeries(dim);
                 int n= ts.getLength();
                 
                 if (n > 0) {
 	                for (int j= 0; j < n; j++) {
 	                    String buildID= ts.getLabel(j);
 	                    double value= ts.getValue(j);
 	                    Color c= buildID.indexOf(referenceBuildId)  >= 0 ? green : black;
 	                    int underscoreIndex=buildID.indexOf('_');
 	                    buildID=(buildID.indexOf('_')==-1)?buildID:buildID.substring(0,underscoreIndex);
 	                    if (c == green)
 	                    	graph.addItem(buildID, dim.getDisplayValue(value), value, c, true);
	                    else
	                    	graph.addItem(buildID, dim.getDisplayValue(value), value, c, (n-2<j) );	
 	                }
 	                
 	                drawGraph(graph, outputDirectory + "/" + scenarioName.replace('#', '.').replace(':','_').replace('\\','_') + "_" + dimensionName);
 	                }
                 } catch (AssertionFailedError e){
                 	//System.err.println("Unable to get result for: "+t.getScenarioName()+" "+ts.toString());
                 }
             }
         }
     }
 
     public void drawGraph(LineGraph p, String output) {
 
         Image image= new Image(Display.getDefault(), GRAPH_WIDTH, GRAPH_HEIGHT);
 
         p.paint(image);
 
         ImageLoader il= new ImageLoader();
         il.data= new ImageData[] { image.getImageData()};
 
         OutputStream out= null;
         try {
             out= new BufferedOutputStream(new FileOutputStream(output + ".jpeg"));
             //System.out.println("writing: " + output);
             il.save(out, SWT.IMAGE_JPEG);
             
             String areas= p.getAreas();
             String scenarioName=output.substring(output.lastIndexOf('/')+1);
             if (areas != null) {
     	        try {
     	            PrintStream os= new PrintStream(new FileOutputStream(output + ".html"));
      	            os.println("<html><body>");
     	            os.println("<script language=\"JavaScript\">");
     	            os.println("if (!document.layers&&!document.getElementById)");
     	            os.println("event=\"test\"");
     	            os.println("function showtip(current,e,text){");
     	            os.println("if (document.getElementById){");
     	            os.println("thetitle=text.split('<br>')");
     	            os.println("if (thetitle.length>1){");
     	            os.println("thetitles=''");
     	            os.println("for (i=0;i<thetitle.length;i++)");
     	            os.println("thetitles+=thetitle[i]");
     	            os.println("current.title=thetitles}");
     	            os.println("else");
     	            os.println("current.title=text}");
     	            os.println("else if (document.layers){");
     	            os.println("document.tooltip.document.write('<layer bgColor=\"white\" style=\"border:1px solid black;font-size:12px;\">'+text+'</layer>')");
     	            os.println("document.tooltip.document.close()");
     	            os.println("document.tooltip.left=e.pageX+5");
     	            os.println("document.tooltip.top=e.pageY+5");
     	            os.println("document.tooltip.visibility=\"show\"}}");
     	            os.println("function hidetip(){");
     	            os.println("if (document.layers)");
     	            os.println("document.tooltip.visibility=\"hidden\"}");
     	            os.println("</script>");
     	            os.println("<div id=\"tooltip\" style=\"position:absolute;visibility:hidden\"></div>");
 
     	            
     	            os.println("<img src=\"" + scenarioName + ".jpeg\" usemap=\"#" + scenarioName + "\">");
     	            os.println("<map name=\"" + scenarioName + "\">");
     	            os.println(areas);
     	            os.println("</map>");
     	            os.println("</body></html>");
     	            os.close();
     	        } catch (FileNotFoundException e) {
     	            // TODO Auto-generated catch block
     	            e.printStackTrace();
     	        }
             }
             
             
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } finally {
             image.dispose();
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e1) {
                     // silently ignored
                 }
             }
         }
     }
 }
