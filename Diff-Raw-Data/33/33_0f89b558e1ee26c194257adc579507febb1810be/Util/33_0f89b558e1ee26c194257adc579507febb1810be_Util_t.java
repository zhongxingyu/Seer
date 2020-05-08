 package org.jbundle.thin.base.util;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.rmi.RemoteException;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.model.Task;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.remote.RemoteTable;
 import org.jbundle.thin.base.util.osgi.bootstrap.ClassServiceBootstrap;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * A Application encompasses all of a single user's apps.
  * For client apps, there is only one Application class, for server apps there
  * will be an Application class for each client.
  * For example, a standalone app, an applet, an ongoing or stateless HTML Application,
  * a user's server Application, or a user's EJB app server Application.
  * <p/>
  * Some of the params that an Application uses are:
  * <pre>
  * user=nameOrID
  * remoteapp=tourapp (don't set this)
  * server=www.tourapp.com (You will need to do this for standalone apps).
  * codebase=classes (shouldn't need this)
  * resource=MyResources
  * lanugage=es
  * </pre>
  */
 public class Util extends Object
 {
     /**
      * A new line.
      */
     public static final char NEW_LINE = '\n';
 
     /**
      * Add this param and data to this URL.
      * @param strOldURL The original URL to add this param to.
      * @param strParam The parameter to add.
      * @param strData The data this parameter is set to.
      * @return The new URL string.
      */
     public static String addURLParam(String strOldURL, String strParam, String strData)
     {
         return Util.addURLParam(strOldURL, strParam, strData, true);
     }
     /**
      * Add this param and data to this URL.
      * @param strOldURL The original URL to add this param to.
      * @param strParam The parameter to add.
      * @param strData The data this parameter is set to.
      * @param bAddIfNull Add an empty param if the data is null?
      * @return The new URL string.
      */
     public static String addURLParam(String strOldURL, String strParam, String strData, boolean bAddIfNull)
     {
         String strURL = strOldURL;
         if ((strOldURL == null) || (strOldURL.length() == 0))
             strURL = "?";
         else if (strOldURL.indexOf('?') == -1)
             strURL += "?";
         else
             strURL += "&";
         if (strData == null)
         {
             if (!bAddIfNull)
                 return strOldURL; // Don't add a null param.
             strData = Constants.BLANK;
         }
         try {
             strURL += URLEncoder.encode(strParam, Constants.URL_ENCODING) + '=' + URLEncoder.encode(strData, Constants.URL_ENCODING);
         } catch (java.io.UnsupportedEncodingException ex) {
             ex.printStackTrace();
         }
         return strURL;
     }
     /**
      * Parse this URL formatted string into properties.
      * @properties The properties object to add the params to.
      * @args The arguments to parse (each formatted as key=value).
      */
     public static void parseArgs(Map<String,Object> properties, String[] args)
     {
         if (args == null)
             return;
         for (int i = 0; i < args.length; i++)
             Util.addParam(properties, args[i], false);
     }
     /**
      * Parse this URL formatted string into properties.
      * @properties The properties object to add the params to.
      * @args The URL to parse (formatted as: XYZ?key1=value1&key2=value2).
      */
     public static void parseArgs(Map<String,Object> properties, String strURL)
     {
         int iIndex = 0;
         int iStartIndex = strURL.indexOf('?') + 1;  // Start of first param (0 if no ?)
         while ((iIndex = strURL.indexOf('=', iIndex)) != -1)
         {
             int iEndIndex = strURL.indexOf('&', iIndex);
             if (iEndIndex == -1)
                 iEndIndex = strURL.length();
             if (iStartIndex < iEndIndex)
                 Util.addParam(properties, strURL.substring(iStartIndex, iEndIndex), true);
             iStartIndex = iEndIndex + 1;
             iIndex++;
         }
     }
     /**
      * Parse the param line and add it to this properties object.
      * (ie., key=value).
      * @properties The properties object to add this params to.
      * @param strParam param line in the format param=value
      */
     public static void addParam(Map<String,Object> properties, String strParams, boolean bDecodeString)
     {
         int iIndex = strParams.indexOf('=');
         int iEndIndex = strParams.length();
         if (iIndex != -1)
         {
             String strParam = strParams.substring(0, iIndex);
             String strValue = strParams.substring(iIndex + 1, iEndIndex);
             if (bDecodeString)
             {
                 try {
                     strParam = URLDecoder.decode(strParam, Constants.URL_ENCODING);
                     strValue = URLDecoder.decode(strValue, Constants.URL_ENCODING);
                 } catch (java.io.UnsupportedEncodingException ex) {
                     ex.printStackTrace();
                 }
             }
             properties.put(strParam, strValue);
         }
     }
     /**
      * Add this param to this path.
      * @param strOldURL The original URL to add this param to.
      * @param strParam The parameter to add.
      * @param strData The data this parameter is set to.
      * @param bAddIfNull Add an empty param if the data is null?
      * @return The new URL string.
      */
     public static String addToPath(String basePath, String fileName)
     {
         String strFileSeparator = System.getProperty("file.separator");
         char chFileSeparator = '/';
         if ((strFileSeparator != null) && (strFileSeparator.length() == 1))
             chFileSeparator = strFileSeparator.charAt(0);
         StringBuilder sb = new StringBuilder();
         if (basePath != null)
             sb.append(basePath);
         if (sb.length() > 0)
         	if (sb.charAt(sb.length() - 1) != chFileSeparator)
         		sb.append(chFileSeparator);
         if (fileName != null)
         {
             if ((fileName.length() > 0) && (fileName.charAt(0) == chFileSeparator) && (sb.length() > 0))
                 sb.append(fileName.substring(1));
             else
                 sb.append(fileName);
         }
         return sb.toString();
     }
     /**
      * Is this string a valid number?
      * @param string The string to check.
      * @return true if this string is a valid number (all characters numeric).
      */
     public static boolean isNumeric(String string)
     {
     	return Util.isNumeric(string, false);
     }
     /**
      * Is this string a valid number?
      * @param string The string to check.
      * @return true if this string is a valid number (all characters numeric).
      */
     public static boolean isNumeric(String string, boolean bAllowFormatting)
     {
         if ((string == null) || (string.length() == 0))
             return false;
         boolean bIsNumeric = true;
         for (int i = 0; i < string.length(); i++)
         {
             if (!Character.isDigit(string.charAt(i)))
             {
             	if ((!bAllowFormatting)
             		|| (Character.isLetter(string.charAt(i))))
             			bIsNumeric = false;
             }
         }
         return bIsNumeric;
     }
     /**
      * Set the message data as a XML String.
      * @return
      */
     public static Document convertXMLToDOM(String strXML)
     {
         DocumentBuilder db = Util.getDocumentBuilder();
         // Parse the input file
         Document doc = null;
         try {
             synchronized (db)
             {   // db is not thread safe
                 doc = db.parse(new InputSource(new StringReader(strXML)));
             }
         } catch (SAXException se) {
             System.out.println(se.getMessage());
             return null;
         } catch (IOException ioe) {
             System.out.println(ioe);
             return null;
         } catch (Exception ioe) {
             System.out.println(ioe);
             return null;
         }
         return doc;
     }
     /**
      * Send this document as text to this output stream.
      * @param doc
      * @return The converted XML
      */
     public static String convertDOMToXML(Node doc)
     {
         StringWriter out = new StringWriter();
         Util.convertDOMToXML(doc, out);
         return out.toString();
     }
     /**
      * Send this document as text to this output stream.
      * @param doc
      */
     public static void convertDOMToXML(Node doc, Writer out)
     {
         try {
             // Use a Transformer for output
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
 
             DOMSource source = new DOMSource(doc);
 
             StreamResult result = new StreamResult(out);
             transformer.transform(source, result);
         } catch (TransformerConfigurationException tce) {
             // Error generated by the parser
             tce.printStackTrace();
         } catch (TransformerException te) {
             // Error generated by the parser
             te.printStackTrace();
         }
     }
     /**
      * Get the document builder.
      * The document builder is the parsing XML engine.
      * NOTE: Since the document builder can't be considered thread safe, you MUST synchronize on db before doing any document commands
      * @return The document builder.
      */
     private static final ThreadLocal < DocumentBuilder > m_dbs = 
         new ThreadLocal < DocumentBuilder > () {
             @Override protected DocumentBuilder initialValue() {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
             // Optional: set various configuration options
             dbf.setValidating(false);
             dbf.setNamespaceAware(true);
             dbf.setIgnoringElementContentWhitespace(false);
             
             DocumentBuilder db = null;
             try {
                 db = dbf.newDocumentBuilder();
             } catch (ParserConfigurationException ex) {
                 ex.printStackTrace();
                 System.exit(1);
             }
             // Set an ErrorHandler before parsing
             db.setErrorHandler(new MyErrorHandler(null));
 
             return db;
         }
     };
 
     public static DocumentBuilder getDocumentBuilder()
     {
         return m_dbs.get();
     }
     /**
      * MyErrorHandler receives error notifications from the DocumentBuilder.
      */
     static class MyErrorHandler extends Object
         implements ErrorHandler
     {
         /**
          * Constructor.
          */
         public MyErrorHandler(Object obj)
         {
         }
         /**
           * Receive notification of a recoverable error.
           */
         public void error(SAXParseException exception)
         {
             System.out.println(exception.getMessage());
         }
         /**
           * Receive notification of a non-recoverable error.
           */
         public void fatalError(SAXParseException exception)
         {
             System.out.println(exception.getMessage());
         }
         /**
           * Receive notification of a recoverable warning.
           */
         public void warning(SAXParseException exception)
         {
             System.out.println(exception.getMessage());
         }
     }
     public static StringBuffer addStartTag(StringBuffer sbXML, String strTag)
     {
         return sbXML.append('<').append(strTag).append('>');
     }
     public static StringBuffer addEndTag(StringBuffer sbXML, String strTag)
     {
         return sbXML.append("</").append(strTag).append('>');
     }
    /**
     * Add this map as an xml string.
     */
    public static StringBuffer addXMLMap(StringBuffer sbXML, Map<String,Object> mapMessage)
    {
        if (sbXML == null)
            sbXML = new StringBuffer();
        if (mapMessage != null)
        {
            for (String strParam : mapMessage.keySet())
            {
                Object objValue = mapMessage.get(strParam);
                if (objValue != null)
                    Util.getXML(sbXML, strParam, objValue);
            }
        }
        return sbXML;
    }
    /**
     * Convert this value to a XML string.
     * @param This value's tag.
     * @param objValue The raw data value.
     * @return The XML string for this value.
     */
    public static StringBuffer getXML(StringBuffer sbXML, String strParam, Object objValue)
    {
        Util.addStartTag(sbXML, strParam);
        if (objValue instanceof Map)
        {
            Util.addXMLMap(sbXML, (Map)objValue);
        }
        else
        {
            String strValue = Constants.BLANK;
            if (objValue != null)
                strValue = objValue.toString();
            if (Util.isCData(strValue))
                strValue = CDATA_START + strValue + CDATA_END;
            sbXML.append(strValue);
        }
        return Util.addEndTag(sbXML, strParam).append(NEW_LINE);
    }
    public static final String CDATA_START = "![CDATA[";
    public static final String CDATA_END = "]]";
    /**
     * Is this a CData string?
     * @param string The string to analyze.
     * @return true if this string must be a CDATA area (if it has carriage-returns).
     */
    public static boolean isCData(String string)
    {
        if (string != null)
        {
            for (int i = 0; i < string.length(); i++)
            {
                char chChar = string.charAt(i);
                if ((chChar == 0x9)
                    || (chChar == 0xD)
                    || (chChar == 0xA))
                        return true;        // Carriage return/Line feed/newline
            }
        }
        return false;
    }
    /**
     * Fix this string so it can be a DOM element name.
     */
    public static String fixDOMElementName(String string)
    {
        for (int i = 0; i < string.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(string.charAt(i)))
                string = string.substring(0, i) + "." + string.substring(i + 1);
        }
        return string;
    }
    /**
     * Get the remote table reference.
     * If you want the remote table session, call this method with java.rmi.server.RemoteStub.class.
     * @classType The base class I'm looking for (If null, return the next table on the chain) 
     * @return The remote table reference.
     */
    public static RemoteTable getRemoteTableType(RemoteTable tableRemote, Class<?> classType)
    {
        if ((classType == null) || (tableRemote == null) || (classType.isAssignableFrom(tableRemote.getClass())))
            return tableRemote;
        RemoteTable remoteTable = null;
        if (!(tableRemote instanceof java.rmi.server.RemoteStub)) // No need to actually do the remote call
            if (!(tableRemote instanceof java.rmi.server.UnicastRemoteObject))
                if (!(tableRemote instanceof java.lang.reflect.Proxy)) // No need to actually do the remote call
        {
            try {
                remoteTable = tableRemote.getRemoteTableType(classType);
            } catch (RemoteException ex)    {
                // Never.
            }
        }
        if (classType == java.rmi.server.RemoteStub.class)  // Yeah I know this has already been done, but it is possible the EJB server may be forcing me to use a proxy
            if (remoteTable == null)
                return tableRemote;    // If you're asking for the last in the chain, that's me
        return remoteTable;
    }
    /**
     * Fake disable a control.
     */
    public static void setEnabled(Component component, boolean bEnabled)
    {
        if (component instanceof JPanel)
        {
            for (int i = 0; i < ((JPanel)component).getComponentCount(); i++)
            {
                JComponent componentSub = (JComponent)((JPanel)component).getComponent(i);
                Util.setEnabled(componentSub, bEnabled);
            }
        }
        else if (component instanceof JScrollPane)
        {
            JComponent componentSub = (JComponent)((JScrollPane)component).getViewport().getView();
            Util.setEnabled(componentSub, bEnabled);
        }
        else
            component.setEnabled(bEnabled);
    }
    /**
     * A utility method to get an Input stream from a filename or URL string.
     * @param strFilename The filename or url to open as an Input Stream.
     * @return The imput stream (or null if there was an error).
     */
    public static InputStream getInputStream(String strFilename, Application app)
    {
        InputStream streamIn = null;
        if ((strFilename != null) && (strFilename.length() > 0))
        {
            try   {
                URL url = null;
                if (strFilename.indexOf(':') != -1)
                    url = new URL(strFilename);
                if (url == null)
                    if (app != null)
                        url = app.getResourceURL(strFilename, null);
                if (url != null)
                    streamIn = url.openStream();
            } catch (Exception ex)  {
                streamIn = null;
            }
            if (streamIn == null)
            {
                File file = new File(strFilename);
                try   {
                    streamIn = new FileInputStream(file);
                } catch (FileNotFoundException ex)  {
                    streamIn = null;
                }
            }
        }
        return streamIn;
    }
    /**
     * Get this URL minus the nav bars
     * @param strURL
     * @param bHelp Add help param
     * @param bNoNav Add no nav bars params
     * @param bLanguage Add language param
     * @return
     */
    public static String fixDisplayURL(String strURL, boolean bHelp, boolean bNoNav, boolean bLanguage, PropertyOwner propertyOwner)
    {
        if ((strURL == null) || (strURL.length() == 0))
     	   return strURL;
        if ((strURL != null)
                && (strURL.length() > 0)
                && (strURL.charAt(0) != '?'))
     	   strURL = '?' + strURL;
        if (bHelp)
     	   strURL = Util.addURLParam(strURL, Params.HELP, Constants.BLANK);
        if (bNoNav)
        {
            strURL = Util.addURLParam(strURL, Params.MENUBARS, "No");
            strURL = Util.addURLParam(strURL, Params.NAVMENUS, "No");
            strURL = Util.addURLParam(strURL, Params.LOGOS, "No");
            strURL = Util.addURLParam(strURL, Params.TRAILERS, "No");  // Don't need outside frame stuff in a window
        }
        if (bLanguage)
        {
     	   String strLanguage = null;
     	   if (propertyOwner != null)
     		   strLanguage = propertyOwner.getProperty("helplanguage");
     	   if ((strLanguage == null) || (strLanguage.length() == 0))
     		   if (propertyOwner != null)
     			   strLanguage = propertyOwner.getProperty(Params.LANGUAGE);
     	   if ((strLanguage != null) && (strLanguage.length() > 0))
     		   strURL = Util.addURLParam(strURL, Params.LANGUAGE, strLanguage);
        }
        return strURL;
    }
 
    public static final Border GRAY_BORDER = new LineBorder(Color.LIGHT_GRAY);
    public static final Border BLACK_BORDER = new LineBorder(Color.BLACK);
 
     /**
      *
      */
     public static String getFullClassName(String strClassName) {
         return Util.getFullClassName(null, strClassName);
     }
     /**
      *
      */
     public static String getFullClassName(String strPackage, String strClass) {
         if (strPackage != null)
             if (strPackage.length() > 0) {
                 if (strPackage.charAt(strPackage.length() - 1) != '.')
                     strPackage = strPackage + '.';
             }
         if (strClass != null)
             if (strClass.length() > 0) {
                 if (strClass.indexOf('.') == -1)
                     if (strPackage != null)
                         strClass = strPackage + strClass;
                 if (strClass.charAt(0) == '.')
                     strClass = Constants.ROOT_PACKAGE + strClass.substring(1);
             }
         return strClass;
     }
    /**
     * Convert this class name by inserting this package after the domain.
     * ie., com.xyz.abc.ClassName -> com.xyz.newpackage.abc.ClassName.
     * @param className
     * @param stringToInsert
     * @return Converted string
     */
    public static String convertClassName(String className, String stringToInsert)
    {
        int iStartSeq = className.indexOf('.');	// Constants.ROOT_PACKAGE.length();
        if (iStartSeq != -1)
     	   iStartSeq = className.indexOf('.', iStartSeq + 1);
        int domainSeq = iStartSeq;
        if (className.indexOf(Constants.THIN_SUBPACKAGE, iStartSeq) == iStartSeq + 1)
            iStartSeq = iStartSeq + Constants.THIN_SUBPACKAGE.length();
        className = className.substring(0, domainSeq + 1) + stringToInsert + className.substring(iStartSeq + 1);
        return className;
    }
    /**
     * Get the Osgi class service.
     * NOTE: Don't import this package as the ClassService class may not be available until this service is started.
     * @return
     */
    public static org.jbundle.thin.base.util.osgi.ClassService getClassService()
    {
 	   return (org.jbundle.thin.base.util.osgi.ClassService)ClassServiceBootstrap.getClassService();
    }
    /**
     * Create this object given the class name.
     * @param className
     * @return
     */
    public static Object makeObjectFromClassName(String interfaceName, String className)
    {
 	   return Util.makeObjectFromClassName(interfaceName, className, null, true);
    }
    /**
     * Create this object given the class name.
     * @param className
     * @return
     */
    public static Object makeObjectFromClassName(String interfaceName, String className, Task task, boolean bErrorIfNotFound)
    {
 	   if (className == null)
 		   return null;
 	   className = Util.getFullClassName(className);
       
	   Class<?> clazz = null;
        try {
			clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
     	   try {
     		   Class.forName("org.osgi.framework.BundleActivator");	// This tests to see if osgi exists
			   if (ClassServiceBootstrap.getClassService() != null)
				   clazz = Util.getClassService().findClassBundle(interfaceName, className);	// Try to find this class in the obr repos
            } catch (Exception ex) {
         	   //Ignore this - just means osgi is not installed
            }
    	   if (clazz == null)
     	       Util.handleClassException(e, className, task, bErrorIfNotFound);
        }
        
 	   Object object = null;
        try {
    	   if (clazz != null)
    		   object = clazz.newInstance();
 	   } catch (InstantiationException e)   {
 	       Util.handleClassException(e, className, task, bErrorIfNotFound);
 	   } catch (IllegalAccessException e)   {
 	       Util.handleClassException(e, className, task, bErrorIfNotFound);
 	   } catch (Exception e) {
 	       Util.handleClassException(e, className, task, bErrorIfNotFound);
        }
        return object;
    }
    /**
     * Create this object given the class name.
     * @param className
     * @return
     */
    public static URL getResourceFromPathName(String className, Task task, boolean bErrorIfNotFound, ClassLoader classLoader)
    {
 	   if (className == null)
 		   return null;
       
 	   URL url = classLoader.getResource(className);
        if (url == null)
        {
 		   try {
 			   Class.forName("org.osgi.framework.BundleActivator");	// This tests to see if osgi exists
			   if (ClassServiceBootstrap.getClassService() != null)
 				   url = Util.getClassService().findBundleResource(className);	// Try to find this class in the obr repos
 	       } catch (Exception ex) {
 	    	   //Ignore this - just means osgi is not installed
 	       }
 		   if (url == null)
 		       Util.handleClassException(null, className, task, bErrorIfNotFound);
        }
 	   return url;
    }
    /**
     * Gets a resource bundle using the specified base name and locale,
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale the locale for which a resource bundle is desired
     * @exception NullPointerException if <code>baseName</code> or <code>locale</code> is <code>null</code>
     * @exception MissingResourceException if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and locale
     */
    public static final ResourceBundle getResourceBundle(String className, Locale locale, Task task, boolean bErrorIfNotFound, ClassLoader classLoader)
    {
 	   MissingResourceException ex = null;
 	   ResourceBundle resourceBundle = null;
 	   try {
 		   resourceBundle = ResourceBundle.getBundle(className, locale);
 	   } catch (MissingResourceException e) {
 		   ex = e;
 	   }
 	   
 	   if (resourceBundle == null)
        {
 		   try {
 			   Class.forName("org.osgi.framework.BundleActivator");	// This tests to see if osgi exists
			   if (ClassServiceBootstrap.getClassService() != null)
 				   resourceBundle = Util.getClassService().findResourceBundle(className, locale);	// Try to find this class in the obr repos
 		   } catch (MissingResourceException e) {
 			   ex = e;
 	       } catch (Exception e) {
 	    	   //Ignore this - just means osgi is not installed
 	       }
 		   if (resourceBundle == null)
 		       Util.handleClassException(null, className, task, bErrorIfNotFound);	// May throw MissingResourceException
        }
 	   
 	   if (resourceBundle == null)
 		   if (ex != null)
 			   throw ex;
 	   
        return resourceBundle;
    }
    /**
     * 
     * @param ex
     * @param className
     * @param task
     * @param bErrorIfNotFound
     */
    public static void handleClassException(Exception ex, String className, Task task, boolean bErrorIfNotFound)
    {
        if (bErrorIfNotFound)
        {
            Util.getLogger().warning("Error on create class: " + className);
            if (ex != null)
         	   ex.printStackTrace();
            if (task != null)
                task.setLastError("Error on create class: " + className);
        }
        
    }
    /**
     * Get the package name of this class name
     * @param className
     * @return
     */
    public static String getPackageName(String className)
    {
        String packageName = null;
        if (className != null)
            if (className.lastIndexOf('.') != -1)
                packageName = className.substring(0, className.lastIndexOf('.'));
        return packageName;
    }
    /**
     * Get the logger.
     * @return The logger.
     */
    public static Logger getLogger()
    {
        if (m_logger == null)
        {
            m_logger = Logger.getLogger(Constants.ROOT_PACKAGE.substring(0, Constants.ROOT_PACKAGE.length() - 1));
            try {
                LogManager logManager = LogManager.getLogManager();
                logManager.reset();//removeAllGlobalHandlers();
                ConsoleHandler fh = new ConsoleHandler();
                fh.setFormatter(new SimpleFormatter());
                // Send logger output to our FileHandler.
                m_logger.addHandler(fh);
            } catch (java.security.AccessControlException ex)    {
                // Access error if running in an applet.
         	   m_logger = Logger.getAnonymousLogger();
            } finally {
                try {
 	               // Request that every detail gets logged.
 //                 m_logger.setLevel(Level.OFF);
 //                 m_logger.setLevel(Level.ALL);
 	               m_logger.setLevel(Level.WARNING);        	   
                } catch (java.security.AccessControlException ex)    {
             	   ex.printStackTrace();	// Never
                }
            }
        }
        return m_logger;
    }
    /**
     * 
     */
    public static void setLogger(Logger logger)
    {
        m_logger = logger;
 //     m_logger.setLevel(Level.ALL);
        if (m_logger != null)
     	   m_logger.setLevel(Level.WARNING);
    }
    private static Logger m_logger = null;
 }
