 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.asascience.edc.erddap;
 
 import cern.colt.Timer;
 import com.asascience.edc.Configuration;
 import com.asascience.edc.CsvProperties;
 import com.asascience.edc.utils.CsvFileUtils;
 import com.asascience.edc.utils.FileSaveUtils;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.swing.JFrame;
 
 /**
  *
  * @author kwilcox
  */
 public class ErddapDataRequest implements PropertyChangeListener {
 
     private String baseUrl;
     private String responseFormat = ".htmlTable";
     private String parameters;
     private PropertyChangeSupport pcs;
     private String homeDir;
     private JFrame parent;
     private File saveFile;
     private ErddapDataset erd;
     
     public ErddapDataRequest(String homeDir, ErddapDataset erd) {
       this.homeDir = homeDir;
       this.erd = erd;
       pcs = new PropertyChangeSupport(this);
     }
 
     public void setParent(JFrame parent) {
       this.parent = parent;
     }
 
     public JFrame getParent() {
       return parent;
     }
 
     public String getHomeDir() {
       return homeDir;
     }
 
     public String getBaseUrl() {
       return baseUrl;
     }
     
     public void setResponseFormat(String responseFormat) {
       this.responseFormat = responseFormat;
     }
 
     public void setParameters(String parameters) {
       this.parameters = parameters;
     }
 
     public File getComputedSaveFile() {
       saveFile = new File(FileSaveUtils.chooseFilename(new File(homeDir + File.separator + FileSaveUtils.getNameAndDateFromUrl(baseUrl) + File.separator), erd.getId() + responseFormat));
       return saveFile;
     }
     
     public File getUpdatedSaveFile() {
       return new File (FileSaveUtils.getFilePathNoSuffix(saveFile.getAbsolutePath()) + responseFormat);
     }
     
     public File getSaveFile() {
       try {
         return getUpdatedSaveFile();
       } catch(NullPointerException npe) {
         return null;
       }
     }
 
     public void setSaveFile(File saveFile) {
       this.saveFile = saveFile;
     }
     
     public String getFilename() {
       return saveFile.getName();
     }
     
     public void setBaseUrl(String baseUrl) {
       this.baseUrl = baseUrl;
     }
 
     public String getResponseFormat() {
       return responseFormat;
     }
     
     public String getRequestUrl() {
       return baseUrl + responseFormat + "?" + parameters;
     }
 
     public void propertyChange(PropertyChangeEvent evt) {
       pcs.firePropertyChange(evt);
     }
     
     public void addPropertyChangeListener(PropertyChangeListener l) {
       pcs.addPropertyChangeListener(l);
     }
 
     public void removePropertyChangeListener(PropertyChangeListener l) {
       pcs.removePropertyChangeListener(l);
     }
     
     public void getData() {
       boolean arc = false;
       CsvProperties properties;
 
       Timer stopwatch = new Timer();
       File f = null;
               
       stopwatch.reset();
 
       int written = 0;
       try {
         URL u = new URL(getRequestUrl());
         pcs.firePropertyChange("message", null, "- Making Request (" + getRequestUrl() + ")");
         HttpURLConnection ht = (HttpURLConnection) u.openConnection();
         ht.setDoInput(true);
         ht.setRequestMethod("GET");
         InputStream is = ht.getInputStream();
         // Increment the file
         File incrementFile = new File(FileSaveUtils.chooseFilename(getSaveFile().getParentFile(), getSaveFile().getName()));
         pcs.firePropertyChange("message", null, "- Streaming Results to File: " + incrementFile.getAbsolutePath());
         f = incrementFile;
         OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
         byte[] buffer = new byte[2048];
         int len = 0;
         written = 0;
         while (-1 != (len = is.read(buffer))) {
           output.write(buffer, 0, len);
           written += len;
         }
         is.close();
         output.flush();
         output.close();
         // Now write the properties file if we need to (ARC)
         if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {
          // We can assume a CSV file here, because we are in ESRI mode
          // We need the date and times to be NOT zulu (go figure).
          CsvFileUtils.convertTimestepsToEsri(f, erd.getTime().getName());
           properties = new CsvProperties();
           properties.setPath(f.getAbsolutePath());
           properties.setSuffix("csv");
           if (erd.hasZ()) {
             properties.setZHeader(erd.getZ().getName());
           }
           if (erd.hasCdmAxis()) {
             properties.setIdHeader(erd.getCdmAxis().getName());
           }
           if (erd.hasX()) {
             properties.setLonHeader(erd.getX().getName());
           }
           if (erd.hasY()) {
             properties.setLatHeader(erd.getY().getName());
           }
           if (erd.hasTime()) {
             properties.setTimeHeader(erd.getTime().getName());
           }
           if (erd.hasCdmDataType()) {
             properties.setCdmFeatureType(erd.getCdmDataType());
           }
           properties.setTimesteps(CsvFileUtils.getTimesteps(f, erd.getTime().getName()));
           properties.setVariableHeaders(CsvFileUtils.getVariables(f, properties.getHeaders()));
           properties.writeFile();
         }
       } catch (MalformedURLException e) {
         pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
       } catch (IOException io) {
         pcs.firePropertyChange("message", null, "- NO DATA available for selected range: " + io.getMessage());
       }
 
       pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
       pcs.firePropertyChange("progress", null, 100);
       if (f != null) {
         pcs.firePropertyChange("done", null, f.getAbsolutePath());
       }
     }
   }
