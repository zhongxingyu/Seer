 /*
  * Applied Science Associates, Inc.
  * Copyright 2009. All Rights Reserved.
  *
  * SosData.java
  *
  * Created on Aug 25, 2009 @ 11:51:16 AM
  */
 package com.asascience.edc.sos.parsers;
 
 import java.beans.PropertyChangeEvent;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import cern.colt.Timer;
 import com.asascience.edc.sos.requests.GenericRequest;
 import com.asascience.edc.sos.requests.ResponseFormat;
 import com.asascience.edc.sos.requests.custom.DifToCSV;
 import com.asascience.edc.sos.requests.custom.DifToNetCDF;
 import com.asascience.edc.sos.requests.custom.SweToCSV;
 import com.asascience.edc.sos.requests.custom.SweToNetCDF;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Date;
 
 //import com.sun.xml.internal.fastinfoset.util.StringArray;
 /**
  * 
  * @author DAS <dstuebe@asascience.com>
  */
 public class SosServer implements PropertyChangeListener {
 
   private GenericParser sosParser;
   private GenericRequest sosRequest;
   
   private String myUrl;
   private float requestTime;
   private float parseTime;
   private PropertyChangeSupport pcs;
   protected Date startTime;
   protected Date endTime;
   protected String sosURL;
   protected String homeDir;
   
   protected Document getCapDoc;
   protected String type;
   protected double timeInterval;
   protected int numTimeSteps;
   protected ResponseFormat responseFormat;
 
   public SosServer(String url) {
     if (url.contains("?")) {
       url = url.substring(0, url.indexOf('?'));
     }
     myUrl = url;
     pcs = new PropertyChangeSupport(this);
     sosRequest = new GenericRequest(url);
     sosRequest.addPropertyChangeListener(this);
   }
 
   public boolean parseSosGetCapabilities() {
     Timer stopwatch = new Timer();
 
     pcs.firePropertyChange("progress", null, 1);
     pcs.firePropertyChange("message", null, "Building Request");
     String capRequest = "?request=GetCapabilities&service=SOS&version=1.0.0";
     pcs.firePropertyChange("message", null, "Reading SOS URL: " + myUrl + capRequest);
 
     // Start Timing
     stopwatch.start();
 
     SAXBuilder getCapBuilder = new SAXBuilder(); // parameters control
     // validation, etc
     Document getCapDoc = null;
     try {
       getCapDoc = getCapBuilder.build(myUrl + capRequest);
     } catch (JDOMException e) {
       pcs.firePropertyChange("message", null, "SOS at: " + myUrl + "; is not well-formed");
       return false;
     } catch (IOException e) {
       pcs.firePropertyChange("message", null, "SOS at: " + myUrl + "; is inaccessible");
       return false;
     } catch (NullPointerException e) {
       pcs.firePropertyChange("message", null, "Error parsing SOS server output");
       return false;
     }
 
     stopwatch.stop();
     requestTime = stopwatch.seconds();
     pcs.firePropertyChange("message", null, "Fetched GetCapabilities in " + requestTime + " seconds");
 
     try {
       XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
       Writer writer = new StringWriter(4 * 10 ^ 7); // 40 mb
       outputter.output(getCapDoc, writer);
       float mysize = writer.toString().length();
       pcs.firePropertyChange("message", null, "Size of SOS capabilities request (mB)= ~" + mysize / 1000000.0);
     } catch (IOException e) {
       pcs.firePropertyChange("message", null, "Could not get the size of the xml document");
       return false;
     }
 
     // If there are more types of parsers in the future,
     // call them here instead of the GenericParser
     sosParser = new GenericParser(getCapDoc);
     sosParser.addPropertyChangeListener(this);
     sosParser.process();
     
     return true;
   }
 
   public static double[] parseBnds(String value) {
     double[] bnds = null;
     String[] values = value.trim().split(" ");
     if (values != null && values.length >= 2) {
       bnds = new double[2];
       try {
         bnds[0] = Double.parseDouble(values[0]);
         bnds[1] = Double.parseDouble(values[1]);
       } catch (NumberFormatException e) {
         bnds = null;
       }
     }
     return bnds;
   }
 
   public void setHomeDir(String home) {
     homeDir = home;
   }
 
   public void setRequestTime(float requestTime) {
     this.requestTime = requestTime;
   }
 
   public float getRequestTime() {
     return requestTime;
   }
 
   public void setParseTime(float parseTime) {
     this.parseTime = parseTime;
   }
 
   public GenericParser getParser() {
     return sosParser;
   }
 
   public float getParseTime() {
     return parseTime;
   }
   
   public void setResponseFormat(ResponseFormat format) {
     responseFormat = format;
   }
 
   public GenericRequest getRequest() {
     return sosRequest;
   }
 
   public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
     pcs.addPropertyChangeListener(l);
   }
 
   public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
     pcs.removePropertyChangeListener(l);
   }
 
   public void getObservations() {
     if (responseFormat.isPostProcess()) {
       String name = responseFormat.getClassName();
       if (name.equalsIgnoreCase("DifToCSV")) {
         sosRequest = new DifToCSV(sosRequest);
       } else if (name.equalsIgnoreCase("DifToNetCDF")) {
         sosRequest = new DifToNetCDF(sosRequest);
       } else if (name.equalsIgnoreCase("SweToCSV")) {
         sosRequest = new SweToCSV(sosRequest);
       } else if (name.equalsIgnoreCase("SweToNetCDF")) {
         sosRequest = new SweToNetCDF(sosRequest);
       }
      sosRequest.addPropertyChangeListener(this);
     }
     sosRequest.setFileSuffix(responseFormat.getFileSuffix());
     sosRequest.setResponseFormatValue(responseFormat.getValue());
     sosRequest.getObservations();
   }
 
   public void propertyChange(PropertyChangeEvent evt) {
     pcs.firePropertyChange(evt);
   }
 }
