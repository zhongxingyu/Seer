 package net.jodreport.ooAddon;
 
 import com.sun.star.awt.MessageBoxButtons;
 import com.sun.star.awt.Rectangle;
 import com.sun.star.awt.XMessageBox;
 import com.sun.star.awt.XMessageBoxFactory;
 import com.sun.star.awt.XWindowPeer;
 import net.jodreport.ooAddon.ui.report.Setting;
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.frame.XComponentLoader;
 import com.sun.star.frame.XDesktop;
 import com.sun.star.frame.XModel;
 import com.sun.star.frame.XStorable;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XMultiServiceFactory;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import com.sun.star.lib.uno.helper.Factory;
 import com.sun.star.lang.XSingleComponentFactory;
 import com.sun.star.registry.XRegistryKey;
 import com.sun.star.lib.uno.helper.WeakBase;
 import com.sun.star.text.XDependentTextField;
 import com.sun.star.text.XTextDocument;
 import com.sun.star.text.XTextViewCursorSupplier;
 import freemarker.core.ParseException;
 import freemarker.ext.dom.NodeModel;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 import javax.xml.parsers.ParserConfigurationException;
 import net.jodreport.ooAddon.ui.script.Script;
 import net.jodreport.ooAddon.ui.script.ScriptDialog;
 import net.jodreport.ooAddon.ui.report.SettingDialog;
 import net.sf.jooreports.templates.DocumentTemplate;
 import net.sf.jooreports.templates.DocumentTemplate.ContentWrapper;
 import net.sf.jooreports.templates.DocumentTemplateException;
 import net.sf.jooreports.templates.DocumentTemplateFactory;
 import org.apache.commons.io.FilenameUtils;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author tedliang
  */
 
 public final class JodAddon extends WeakBase
         implements com.sun.star.lang.XInitialization,
         com.sun.star.frame.XDispatch,
         com.sun.star.frame.XDispatchProvider,
         com.sun.star.lang.XServiceInfo {
 
     private final XComponentContext m_xContext;
     private com.sun.star.frame.XFrame m_xFrame;
     private static final String m_implementationName = JodAddon.class.getName();
     private static final String[] m_serviceNames = {"com.sun.star.frame.ProtocolHandler"};
     private Setting setting = new Setting("");
 
     public JodAddon(XComponentContext context) {
         m_xContext = context;
     }
 
     public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {
         XSingleComponentFactory xFactory = null;
 
         if (sImplementationName.equals(m_implementationName)) {
             xFactory = Factory.createComponentFactory(JodAddon.class, m_serviceNames);
         }
         return xFactory;
     }
 
     public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
         return Factory.writeRegistryServiceInfo(m_implementationName,
                 m_serviceNames,
                 xRegistryKey);
     }
 
     // com.sun.star.lang.XInitialization:
     public void initialize(Object[] object)
             throws com.sun.star.uno.Exception {
         if (object.length > 0) {
             m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(
                     com.sun.star.frame.XFrame.class, object[0]);
         }
     }
 
     // com.sun.star.frame.XDispatch:
     public void dispatch(com.sun.star.util.URL aURL,
             com.sun.star.beans.PropertyValue[] aArguments) {
         if (aURL.Protocol.compareTo("net.jodreport.ooAddon.jodaddon:") == 0) {
             if (aURL.Path.compareTo("JODReport") == 0) {
                 jodReportOnClick();
             } else if (aURL.Path.compareTo("JOOScript") == 0) {
                 insertScriptOnClick();
             }
             return;
         }
     }
 
     private void jodReportOnClick() {
         String path = getCurrentDocumentPath();
 
         try {
             if (path != null && !path.equals("")) {
 
                 // sometimes we may have in path spaces, and sometimes
                 // we may have nice %20 in place of spaces....
                 if (path.indexOf(" ") != -1) {
                     path = path.replaceAll(" ", "%20");
                 }
                 File file = new File(new URL(path).toURI());
                 if (file.isFile()) {
                     SettingDialog sd = new SettingDialog(m_xContext, setting);
                     if (sd.createDialog()) {
                         if (setting.isDataFileExist()) {
                             XStorable storable = UnoRuntime.queryInterface(
                                     XStorable.class, getXModel());
                             storable.store();
 
                             String outputFilePath = file.getAbsolutePath();
                             int idx = outputFilePath.lastIndexOf(".");
                             outputFilePath = outputFilePath.substring(0, idx) + "-report" + outputFilePath.substring(idx);
                             createReport(file, new File(setting.getDataFilePath()), new File(outputFilePath));
                             openReport( outputFilePath);
                         } else {
                             displayMessage("errorbox", "ERROR", "No data file found!");
                         }
                     }
                 } else {
                     displayMessage("warningbox", "WARNING", "You must first save your file on hard disk!");
                 }
 
             } else {
                 displayMessage("warningbox", "WARNING", "You must first save your file on hard disk!");
             }
         } catch (ParseException ex) {
             displayMessage("errorbox", "ERROR", "Script syntax error. Possible reason: Unclosed directive (list or if-else)!\nDetails: "+ex.toString());
         } catch (Exception ex) {
             displayMessage("errorbox", "ERROR", ex.toString());
         }
     }
 
     private void insertScriptOnClick() {
         try {
             ScriptDialog sd = new ScriptDialog(m_xContext);
             Script script = sd.createDialog();
             if (script != null && script.getValue() != null && !script.getValue().equals("")) {
 
                 String type;
                 String propertyName;
 
                 if (script.isVisiable()) {
                     type = "Input";
                     propertyName = "Hint";
                 } else {
                     type = "Script";
                     propertyName = "ScriptType";
                 }
 
                // query its XTextDocument interface to get the text
                 XTextDocument mxDoc = UnoRuntime.queryInterface(
                         XTextDocument.class, getXModel());
 
                 XMultiServiceFactory loXMSFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, mxDoc);
                 XDependentTextField loXDependentTextField = UnoRuntime.queryInterface(XDependentTextField.class,
                         loXMSFactory.createInstance("com.sun.star.text.textfield." + type));
 
                 XPropertySet loXPropertySet = UnoRuntime.queryInterface(XPropertySet.class, loXDependentTextField);
 
                 // And now set properties
                 loXPropertySet.setPropertyValue(propertyName, "jooscript");
                 loXPropertySet.setPropertyValue("Content", script.getValue());
 
                 // the controller gives us the TextViewCursor
                XTextViewCursorSupplier xViewCursorSupplier = UnoRuntime.queryInterface(
                        XTextViewCursorSupplier.class, m_xFrame.getController());
 
                mxDoc.getText().insertTextContent(xViewCursorSupplier.getViewCursor(), loXDependentTextField, true);
 
             }
 
         } catch (Exception ex) {
             displayMessage("errorbox", "ERROR", ex.toString());
             ex.printStackTrace();
         }
     }
 
     private void createReport(File templateFile, File dataFile, File outputFile)
             throws IOException, SAXException, ParserConfigurationException, DocumentTemplateException {
 
         DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
         documentTemplateFactory.getFreemarkerConfiguration().setTemplateExceptionHandler(
                 new FreemarkerTemplateExceptionHandler());
         DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
         template.setContentWrapper(new ContentWrapper(){
             public String wrapContent(String string) {
                 return string;
             }
         });
 
         Object model = null;
         String dataFileExtension = FilenameUtils.getExtension(dataFile.getName());
         if (dataFileExtension.equals("xml")) {
             model = NodeModel.parse(dataFile);
         } else if (dataFileExtension.equals("properties")) {
             Properties properties = new Properties();
             properties.load(new FileInputStream(dataFile));
             model = properties;
         } else {
             throw new IllegalArgumentException("data file must be 'xml' or 'properties'; unsupported type: " + dataFileExtension);
         }
 
         template.createDocument(model, new FileOutputStream(outputFile));
 
     }
 
     private void openReport(String file) throws com.sun.star.uno.Exception {
         // get the remote office service manager
         XMultiComponentFactory xMCF = m_xContext.getServiceManager();
 
         // get a new instance of the desktop
         XDesktop xDesktop = UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class,
                 xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext));
 
         XComponentLoader xCompLoader = UnoRuntime.queryInterface(
                 com.sun.star.frame.XComponentLoader.class, xDesktop);
 
         xCompLoader.loadComponentFromURL("file:///" + file, "_blank", 0, new PropertyValue[0]);
     }
 
     private String getCurrentDocumentPath() {
         XModel xDoc = (XModel) UnoRuntime.queryInterface(
                 XModel.class, getXModel());
         return xDoc.getURL();
     }
 
     private XModel getXModel(){
         return m_xFrame.getController().getModel();
     }
 
     private void displayMessage(String type, String title, String msg){
         XMessageBoxFactory factory;
         try {
             factory = UnoRuntime.queryInterface(XMessageBoxFactory.class,
                     m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.awt.Toolkit", m_xContext));
 
             XWindowPeer parent = UnoRuntime.queryInterface(
                     XWindowPeer.class, m_xFrame.getContainerWindow());
             XMessageBox box = factory.createMessageBox(parent,new Rectangle(),type,MessageBoxButtons.BUTTONS_OK,title,msg);
 
             box.execute();
         } catch (com.sun.star.uno.Exception ex) {
             ex.printStackTrace();
         }
 
     }
 
     public void addStatusListener(com.sun.star.frame.XStatusListener xControl,
             com.sun.star.util.URL aURL) {
         // add your own code here
     }
 
     public void removeStatusListener(com.sun.star.frame.XStatusListener xControl,
             com.sun.star.util.URL aURL) {
         // add your own code here
     }
 
     // com.sun.star.frame.XDispatchProvider:
     public com.sun.star.frame.XDispatch queryDispatch(com.sun.star.util.URL aURL,
             String sTargetFrameName,
             int iSearchFlags) {
         if (aURL.Protocol.compareTo("net.jodreport.ooAddon.jodaddon:") == 0) {
             return this;
         }
         return null;
     }
 
     // com.sun.star.frame.XDispatchProvider:
     public com.sun.star.frame.XDispatch[] queryDispatches(
             com.sun.star.frame.DispatchDescriptor[] seqDescriptors) {
         int nCount = seqDescriptors.length;
         com.sun.star.frame.XDispatch[] seqDispatcher =
                 new com.sun.star.frame.XDispatch[seqDescriptors.length];
 
         for (int i = 0; i < nCount; ++i) {
             seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                     seqDescriptors[i].FrameName,
                     seqDescriptors[i].SearchFlags);
         }
         return seqDispatcher;
     }
 
     // com.sun.star.lang.XServiceInfo:
     public String getImplementationName() {
         return m_implementationName;
     }
 
     public boolean supportsService(String sService) {
         int len = m_serviceNames.length;
 
         for (int i = 0; i < len; i++) {
             if (sService.equals(m_serviceNames[i])) {
                 return true;
             }
         }
         return false;
     }
 
     public String[] getSupportedServiceNames() {
         return m_serviceNames;
     }
 }
