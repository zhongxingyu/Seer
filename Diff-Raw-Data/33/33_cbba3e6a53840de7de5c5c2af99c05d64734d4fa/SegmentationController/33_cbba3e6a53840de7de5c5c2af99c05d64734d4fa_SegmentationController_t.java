 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 
 package edu.stanford.cfuller.analysistoolsinterface;
 
 import edu.stanford.cfuller.imageanalysistools.frontend.AnalysisController;
 import edu.stanford.cfuller.imageanalysistools.frontend.DataSummary;
 import edu.stanford.cfuller.imageanalysistools.method.Method;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.prefs.Preferences;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author cfuller
  */
 public class SegmentationController extends TaskController implements OmeroListener{
 
     final static String METHOD_XML_FILENAME = "edu/stanford/cfuller/analysistoolsinterface/resources/methods.xml";
     final static String METHOD_XML_TAG = "method";
     final static String DISPLAY_ATTR_NAME = "displayname";
     final static String CLASS_ATTR_NAME= "class";
 
 
     final static String STATUS_PROCESSING = "Processing";
     final static String STATUS_READY = "Ready";
     final static String STATUS_SUMMARY = "Making data summary";
 	final static String STATUS_OMERO_ERR = "OMERO missing; ";
     
     SegmentationWindow sw;
 
     List<Long> omeroImageIds;
     OmeroBrowsingWindowController omeroBrowser;
 
 
     @Override
     public void startTask() {
 
         this.omeroBrowser = null;
 
         sw = new SegmentationWindow(this);
         sw.addWindowListener(this);
         initializeMethods();
 
         String imageFilename = Preferences.userNodeForPackage(this.getClass()).get("imageFile", "");
         String parameterFilename = Preferences.userNodeForPackage(this.getClass()).get("parameterFile", "");
         int selectedMethodIndex = Preferences.userNodeForPackage(this.getClass()).getInt("selectedIndex", 0);
 
         sw.setImageFilename(imageFilename);
         sw.setParameterFilename(parameterFilename);
         sw.setSelectedMethodIndex(selectedMethodIndex);
 
         LoggingUtilities.addHandler(sw.getLogHandler());
 
         this.sw.setStatus(STATUS_READY);
 
         sw.setVisible(true);
 
 
 
     }
 
     @Override
     public void imageIdsHaveBeenSelected(List<Long> ids) {
         this.omeroImageIds = ids;
         this.sw.setUseOmeroServer(true);
     }
 
     public void browseForParameterFile() {
         String path = this.sw.getParameterFilename();
         JFileChooser fc = null;
         if ((new File(path)).exists()) {
             fc = new JFileChooser(path);
         } else {
             fc = new JFileChooser();
         }
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.addChoosableFileFilter(new FileNameExtensionFilter("Parameter files (.xml, .rb)", "xml", "rb"));
         int retVal = fc.showOpenDialog(this.sw);
 
 
         if (retVal == JFileChooser.APPROVE_OPTION) {
 
             String selected = fc.getSelectedFile().getAbsolutePath();
 
 
             sw.setParameterFilename(selected);
         }
     }
 
     public void browseForImageFileOrDirectory() {
         String path = this.sw.getImageFilename();
         JFileChooser fc = null;
         if ((new File(path)).exists()) {
             fc = new JFileChooser(path);
         } else {
             fc = new JFileChooser();
         }
         fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         
         int retVal = fc.showOpenDialog(this.sw);
 
         if (retVal == JFileChooser.APPROVE_OPTION) {
 
             String selected = fc.getSelectedFile().getAbsolutePath();
 
 
             sw.setImageFilename(selected);
         }
     }
 
 
     public void onMethodSelect() {
         int index = this.sw.getSelectedMethodIndex();
 
         MethodInfo mi = (MethodInfo) this.sw.getMethodComboBoxModel().getElementAt(index);
 
         if (mi.getMethodClass() == null) {
             this.sw.setCustomMethodFieldEnabled(true);
         } else {
             this.sw.setCustomMethodFieldEnabled(false);
         }
 
     }
 
     public void useOmeroDataSource() {
 		try {
         	Class.forName("omero.api.GatewayPrx");
 		} catch (ClassNotFoundException e) {
 			LoggingUtilities.warning("Could not open OMERO data source; OMERO client plugin missing.");
 			this.sw.setStatus(STATUS_OMERO_ERR + STATUS_READY);
 			this.sw.disableOmero();
 			return;
 		}
 		this.omeroBrowser = new OmeroBrowsingWindowController();
     	omeroBrowser.openNewBrowsingWindow(this);
     }
 
     public void runButtonPressed() {
 
         if (! (this.sw.getStatus().equals(STATUS_READY) || this.sw.getStatus().equals(STATUS_OMERO_ERR + STATUS_READY))) return;
 
 
         final String parameterFilename = this.sw.getParameterFilename();
         final String imageFilename = this.sw.getImageFilename();
 
         Preferences.userNodeForPackage(this.getClass()).put("imageFile", imageFilename);
         Preferences.userNodeForPackage(this.getClass()).put("parameterFile", parameterFilename);
         Preferences.userNodeForPackage(this.getClass()).putInt("selectedIndex", this.sw.getSelectedMethodIndex());
 
 		this.sw.setStatus(STATUS_PROCESSING);
 
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			
 			public void run() {
 				
 				MethodInfo mi = (MethodInfo) sw.getMethodComboBoxModel().getElementAt(sw.getSelectedMethodIndex());
 
 		        Class c = mi.getMethodClass();
 				
				final AnalysisMetadata am = AnalysisMetadataParserFactory.createParserForFile(parameterFilename).parseFileToAnalysisMetadata(parameterFilename);        

				final ParameterDictionary pd = am.getInputParameters();
 
 		        if (c == null && !pd.hasKey("method_name")) {
 		            try {
 		                c = Class.forName(sw.getCustomClassName());
 		            } catch (ClassNotFoundException e) {
 		                LoggingUtilities.warning("Custom class not found with name: " + sw.getCustomClassName());
 						sw.setStatus(STATUS_READY);
 		                return;
 		            } 
 		        }
 
 		        pd.addIfNotSet("method_name", c.getName());
 		        if ((new File(imageFilename)).isDirectory()) {
 		            pd.addIfNotSet("local_directory", imageFilename);
 		        } else {
 		            File wholeFilename = new File(imageFilename);
 		            String name = wholeFilename.getName();
 		            String dir = wholeFilename.getParent();
 		            if (!pd.hasKey("common_filename_tag")) {pd.addIfNotSet("common_filename_tag", name);
 		            } else {
 		                pd.setValueForKey("image_extension", name);
 		            }
 		            pd.addIfNotSet("local_directory", dir);
 		        }
 
 		        pd.addIfNotSet("max_threads", Integer.toString(Runtime.getRuntime().availableProcessors()));
 
 		        if (sw.getUseOmeroServer()) {
 		            pd.setValueForKey("use_omero", "true");
 		            pd.addIfNotSet("omero_hostname", omeroBrowser.getHostname());
 		            pd.addIfNotSet("omero_username", omeroBrowser.getUsername());
 		            pd.addIfNotSet("omero_password", omeroBrowser.getPassword());
 		            String omeroImageIdString = null;
 		            for (long l : omeroImageIds) {
 		                if (omeroImageIdString == null) {
 		                    omeroImageIdString = "";
 		                } else {
 		                    omeroImageIdString += " ";
 		                }
 		                omeroImageIdString += Long.toString(l);
 		            }
 
 		            pd.addIfNotSet("omero_image_ids", omeroImageIdString);
 		        }
 
 
 				(new Thread(new Runnable() {
 
 
 					public void run() {
 		       			if (! sw.summarizeDataOnly()) {
 		                   AnalysisController ac = new AnalysisController();
 		                   ac.addAnalysisLoggingHandler(sw.getLogHandler());
		                   ac.runLocal(am);
 						}
 				
 						sw.setStatus(STATUS_SUMMARY);
 				
 						try {
 						    DataSummary.SummarizeData(pd.getValueForKey("local_directory")+File.separator+AnalysisController.DATA_OUTPUT_DIR, pd.getValueForKey("local_directory")+File.separator+AnalysisController.PARAMETER_OUTPUT_DIR);
 						} catch (IOException e) {
 						    LoggingUtilities.severe("Encountered error while summarizing data.");
 						}
 				
 						sw.setStatus(STATUS_READY);
 					}
 				})).start();
 	
 			}
 		});
                         
         
 
         
 
     }
 
 
 
 
     protected static class MethodInfo {
         String displayName;
         Class methodClass;
 
         public MethodInfo(String displayName, String className) {
             this.displayName = displayName;
             if (className != null) {
                 try {
                     this.methodClass =  Class.forName(className);
                 } catch (ClassNotFoundException e) {
                     LoggingUtilities.warning("Valid class not found for method with name: " + displayName);
                     this.methodClass = null;
                 } 
             } else {
                 this.methodClass = null;
             }
         }
 
         public String getDisplayName() {
             return this.displayName;
         }
 
         public Class getMethodClass() {
             return this.methodClass;
         }
 
         @Override
         public String toString() {
             return this.displayName;
         }
 
     }
 
     protected void initializeMethods() {
 	
         DefaultComboBoxModel model = sw.getMethodComboBoxModel();
 
         String methodResourceLocation = this.getClass().getClassLoader().getResource(METHOD_XML_FILENAME).toString();
 
         NodeList methodNodes = null;
 
         try {
             methodNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(methodResourceLocation).getElementsByTagName(METHOD_XML_TAG);
         } catch (SAXException e) {
             LoggingUtilities.severe("Encountered exception while parsing tasks xml file.");
             return;
         } catch (ParserConfigurationException e) {
             LoggingUtilities.severe("Incorrectly configured xml parser.");
             return;
         } catch (IOException e) {
             LoggingUtilities.severe("Exception while reading xml file.");
             return;
         }
 
         for (int i =0; i < methodNodes.getLength(); i++) {
             Node n = methodNodes.item(i);
 
             Node displayName = n.getAttributes().getNamedItem(DISPLAY_ATTR_NAME);
             Node className = n.getAttributes().getNamedItem(CLASS_ATTR_NAME);
 
             model.addElement(new MethodInfo(displayName != null? displayName.getNodeValue() : null, className != null? className.getNodeValue() : null));
         }
 
 
     }
 
 
 
 
 }
