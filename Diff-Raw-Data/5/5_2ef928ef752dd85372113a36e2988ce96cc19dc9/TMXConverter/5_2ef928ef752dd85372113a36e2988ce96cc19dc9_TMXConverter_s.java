 package domderrien.build;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.xpath.XPathAPI;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * TMX to Resource Bundle converter.
  * JDK 1.5 is expected.
  *
  * Notes: <ul>
  *   <li>Input for this converter and output of this converter are UTF-8 encoding.</li>
  *   <li>A generated properties file needs to be converted to ASCII with <code>native2ascii</code>.<br/>
  *    Ex. <code>native2ascii -encoding UTF-8 &lt;base_name&gt;.properties-utf8 &lt;base_name&gt;.properties</code></li>
  *   <li>Running with ant, specify <code>-Dfile.encoding=UTF-8</code> as a JVM argument.</li>
  */
 public class TMXConverter {
 
     /**
      * TMX Converter entry point: extract the parameters and process the retrieved TMX files.
      * @param args Usual arguments of a program
      */
     public static void main(String[] args) {
         TMXConverter converter = new TMXConverter(true);
         converter.setContext(args);
         converter.processContext();
         converter.processTMX();
     }
 
     /**
      * Default constructor
      * @param isStandalone Specifies that any error should invoke "System.exit(1)" to report a
      *                     an application error code to the instanciator environment
      */
     public TMXConverter(boolean isStandalone) {
         runStandalone = isStandalone;
     }
 
     protected final static String TMX_FILENAME_BASE_ARG = "-tmxFilenameBase";
     protected final static String SOURCE_PATH_ARG = "-sourcePath";
     protected final static String JS_DEST_PATH_ARG = "-jsDestPath";
     protected final static String JAVA_DEST_PATH_ARG = "-javaDestPath";
     protected final static String LANGUAGE_FILENAME_BASE_ARG = "-languageFilenameBase";
     protected final static String BUILD_STAMP_ARG = "-buildStamp";
 
     /**
      * Set the class instance variables with data extracted from the command line arguments
      * @param args Usual command line argument list
      * @throws NullPointerException if the given parameter is <code>null</code>
      */
     public void setContext(String[] args) {
         if (args.length % 2 != 0 ||
             args.length < 12)
             {
                 displayUsage();
                 return;
             }
 
         List<String> arguments = Arrays.asList(args);
         int tmxFilenameBaseIdx = arguments.indexOf(TMX_FILENAME_BASE_ARG);
         int sourcePathIdx = arguments.indexOf(SOURCE_PATH_ARG);
         int jsDestPathIdx = arguments.indexOf(JS_DEST_PATH_ARG);
         int javaDestPathIdx = arguments.indexOf(JAVA_DEST_PATH_ARG);
         int languageFilenameBaseIdx = arguments.indexOf(LANGUAGE_FILENAME_BASE_ARG);
         int buildStampIdx = arguments.indexOf(BUILD_STAMP_ARG);
 
         if (tmxFilenameBaseIdx == -1 ||
             sourcePathIdx == -1 ||
             jsDestPathIdx == -1 ||
             javaDestPathIdx == -1 ||
             languageFilenameBaseIdx == -1 ||
             buildStampIdx == -1)
         {
             displayUsage();
             return;
         }
 
         try {
             tmxFilenameBase = arguments.get(tmxFilenameBaseIdx + 1);
             sourcePath = arguments.get(sourcePathIdx + 1);
             jsDestPath = arguments.get(jsDestPathIdx + 1);
             javaDestPath = arguments.get(javaDestPathIdx + 1);
             languageFilenameBase = arguments.get(languageFilenameBaseIdx + 1);
             buildStamp = arguments.get(buildStampIdx + 1);
         }
         catch(ArrayIndexOutOfBoundsException ex) {
             displayUsage();
             return;
         }
 
         if (tmxFilenameBase.length() == 0 ||
             sourcePath.length() == 0 ||
             jsDestPath.length() == 0 ||
             javaDestPath.length() == 0 ||
             languageFilenameBase.length() == 0 ||
             buildStamp.length() == 0)
         {
             displayUsage();
             return;
         }
     }
 
     /**
      * Just display how the class should be invoked from the command line
      */
     protected void displayUsage() {
         if (runStandalone)
         {
             System.out.println("TMX Converter usage:");
             System.out.print("TMXConverter: ");
             System.out.print(TMX_FILENAME_BASE_ARG + " <filename> ");
             System.out.print(SOURCE_PATH_ARG + " <path> ");
             System.out.print(JS_DEST_PATH_ARG + " <path> ");
             System.out.print(JAVA_DEST_PATH_ARG + " <path> ");
             System.out.print(LANGUAGE_FILENAME_BASE_ARG + " <filename>");
             System.out.print(BUILD_STAMP_ARG + " <build stamp>");
             System.out.println(".");
         }
         stopProcess();
     }
 
     /**
      * Report an error code to the application instaniator if it works
      * in standalone mode.
      *
      */
     protected void stopProcess() {
         processStopped = true;
         if (runStandalone) {
             // To make the standalone program
             // failing the build process
             System.exit(1);
         }
     }
 
     /**
      * Report an error message.
      *
      */
     protected void reportError(String message) {
         errorReported = true;
         if (runStandalone) {
             System.err.println(message);
         }
     }
 
     private boolean runStandalone = false;
     private boolean processStopped = false;
     private boolean errorReported = false;
 
     /** Accessor */
     protected boolean getProcessStopped() {
         return processStopped;
     }
 
     /** Accessor */
     protected boolean isErrorReported() {
         return errorReported;
     }
 
     private String tmxFilenameBase = null;
     private String sourcePath = null;
     private String jsDestPath = null;
     private String javaDestPath = null;
     private String languageFilenameBase = null;
     private String buildStamp = null;
 
     private Map<String, Long> sourceFileDates = new HashMap<String, Long>();
     private Map<String, Long> jsDestFileDates = new HashMap<String, Long>();
     private Map<String, Long> javaDestFileDates = new HashMap<String, Long>();
     private Map<String, String> processedLanguages = new HashMap<String, String>();
 
     /** Accessor for unit test */
     protected void setTMXFilenameBase(String name) {
         tmxFilenameBase = name;
     }
 
     /** Accessor for unit test */
     protected void setSourceMap(Map<String, Long> map) {
         sourceFileDates = map;
     }
 
     /** Accessor for unit test */
     protected void setJSMap(Map<String, Long> map) {
         jsDestFileDates = map;
     }
 
     /** Accessor for unit test */
     protected void setJavaMap(Map<String, Long> map) {
         javaDestFileDates = map;
     }
 
     /** Accessor for unit test */
     protected void setLanguageMap(Map<String, String> map) {
         processedLanguages = map;
     }
 
     /** Accessor for unit test */
     protected Map<String, String> getLanguageMap() {
         return processedLanguages;
     }
 
     /** Accessor for unit test */
     protected void setLanguageFilenameBase(String name) {
         languageFilenameBase = name;
     }
 
     /** Accessor for unit test */
     protected void setBuildStamp(String name) {
         buildStamp = name;
     }
 
     private ResourceBundle documentedLanguages;
 
     protected ResourceBundle getDocumentedLanguages() throws MissingResourceException {
         if (documentedLanguages == null) {
             documentedLanguages = ResourceBundle.getBundle(languageFilenameBase);
         }
         return documentedLanguages;
     }
 
     /**
      * Process the contextual information
      */
     public void processContext() {
         getSourceFileDates(tmxFilenameBase, sourcePath, sourceFileDates);
         getJSFileDates(sourceFileDates, jsDestPath, jsDestFileDates);
         getJavaFileDates(sourceFileDates, javaDestPath, javaDestFileDates);
 
         try {
             getDocumentedLanguages();
         }
         catch (MissingResourceException ex) {
             reportError("No previous language definitions found.");
             // Ignore execption because the file content will be built from the list of processed TMX
         }
     }
 
     /**
      * Extract the name of the files contained in the given directory and their
      * last-modified date.
      *
      * @param filenameBase Base name of the TMX files
      * @param directory File instance to study (can be relative or absolute).
      * @param fileDates List where extracted information are stored.
      */
     protected void getFileDates(String filenameBase, File directory, Map<String, Long> fileDates) {
         if (directory.isDirectory()) {
             File[] file = directory.listFiles();
             for (int i = 0; i <file.length; i++) {
                 File containedFile = file[i];
                 if (containedFile.isFile()) {
                     String filename = containedFile.getName();
                     if (filename.startsWith(filenameBase)) {
                         int dotIndex = filename.indexOf('.');
                         String localizedFilenameBase = dotIndex == -1 ? filename : filename.substring(0, filename.indexOf('.'));
                         fileDates.put(localizedFilenameBase, containedFile.lastModified());
                     }
                 }
             }
         }
     }
 
     protected File getFile(String filename) {
         return new File(filename);
     }
 
     /**
      * Extract the name of the files contained in the specified directory and their
      * last-modified date.
      *
      * Note: this method decouples the File object instanciation from the directory
      * content parsing just to ease the unit testing.
      *
      * @param filenameBase Beginning of the filename to consider
      * @param directoryName Name of the directory to study (can be relative or absolute).
      * @param fileDates List where extracted information are stored.
      */
     protected void getSourceFileDates(String filenameBase, String directoryName, Map<String, Long> fileDates) {
         File directory = getFile(directoryName);
         getFileDates(filenameBase, directory, fileDates);
     }
 
     /**
      * Using the list of identified source files, extract the date of the corresponding
      * JavaScript map files.
      *
      * Note: this method decouples the File object instanciation from the directory
      * content parsing just to ease the unit testing.
      *
      * @param sourceFileDates List where extracted information are stored.
      * @param directoryName Name of the directory to study (can be relative or absolute).
      * @param fileDates List where extracted information are stored.
      */
     protected void getJSFileDates(Map<String, Long> sourceFileDates, String directoryName, Map<String, Long> fileDates) {
         String filenameBase = null;
         for (String filename : sourceFileDates.keySet()) {
             File file = null;
             int separatorIdx = filename.indexOf('_');
             if (separatorIdx == -1) {
                 file = getFile(directoryName + File.separator + filename + ".js");
             }
             else {
                 if (filenameBase == null) {
                     filenameBase = filename.substring(0, separatorIdx);
                 }
                 String locale = filename.substring(separatorIdx + 1);
                 file = getFile(directoryName + File.separator + locale + File.separator + filenameBase + ".js");
             }
             if (file.isFile()) {
                 fileDates.put(filename, file.lastModified());
             }
         }
     }
 
     /**
      * Using the list of identified source files, extract the date of the corresponding
      * Java map files.
      *
      * Note: this method decouples the File object instanciation from the directory
      * content parsing just to ease the unit testing.
      *
      * @param sourceFileDates List where extracted information are stored.
      * @param directoryName Name of the directory to study (can be relative or absolute).
      * @param fileDates List where extracted information are stored.
      */
     protected void getJavaFileDates(Map<String, Long> sourceFileDates, String directoryName, Map<String, Long> fileDates) {
         for (String filename : sourceFileDates.keySet()) {
             File file = getFile(directoryName + File.separator + filename + ".properties-utf8");
             if (file.isFile()) {
                 fileDates.put(filename, file.lastModified());
             }
         }
     }
 
     /**
      * Business method processing all TMX file matching the given
      * <code>filenameBase</code> argument value. According to the TMX
      * file content, it produces:<ul>
      *   <li>One JavaScript file with <code>"key":"value",</code> in a map;</li>
      *   <li>One Java property file with <code>key=value</code>;</li></ul>
      * When all TMX files have been processed, one Java property file is
      * generated with the list of supported locales and their corresponding
      * localized language name.
      */
     public void processTMX() {
         for (String filename : sourceFileDates.keySet()) {
             Long sourceDate = sourceFileDates.get(filename);
             Long jsDestDate = jsDestFileDates.get(filename);
             Long javaDestDate = javaDestFileDates.get(filename);
 
             boolean processRequired = jsDestDate == null || javaDestDate == null;
             processRequired = processRequired || jsDestDate < sourceDate;
             processRequired = processRequired || javaDestDate < sourceDate;
 
             if(processRequired) {
                 try {
                     long start = System.currentTimeMillis();
                     System.out.print("running: TmxConverter process " + filename); //$NON-NLS-1$
                     convert(filename);
                     long end = System.currentTimeMillis();
                     System.out.print("' in " + (end - start) + " ms\n"); //$NON-NLS-1$ //$NON-NLS-2$
                 }
                 catch (FileNotFoundException e) {
                     // Just reported the non processed filename
                     reportError(": " + filename + ".tmx: not processed because not found.");
                 }
                 catch (IOException ex) {
                     // Returning non zero should stop the build
                     reportError(": [Exception: " + ex + "] ");
                     stopProcess();
                     return;
                 }
             }
         }
 
         if (0 < processedLanguages.size()) {
             try {
                 long start = System.currentTimeMillis();
                 System.out.print("running: TmxConverter process " + languageFilenameBase); //$NON-NLS-1$
                 saveSupportedLanguages();
                 long end = System.currentTimeMillis();
                 System.out.print("' in " + (end - start) + " ms\n"); //$NON-NLS-1$ //$NON-NLS-2$
             }
             catch (IOException ex) {
                 // Returning non zero should stop the build
                 reportError(": [Exception: " + ex + "] ");
                 stopProcess();
                 return;
             }
         }
     }
 
     public static final String EMPTY = ""; //$NON-NLS-1$
     public static final String NL = "\r\n"; //$NON-NLS-1$
 
     public static final String JS_SWITCH = "-js"; //$NON-NLS-1$
     public static final String PROP_SWITCH = "-prop"; //$NON-NLS-1$
     public static final String LANG_SWITCH = "-lang"; //$NON-NLS-1$
 
     public static final String JS_FILE_START = "({"; //$NON-NLS-1$
     public static final String JS_LINE_START = ""; //$NON-NLS-1$
     public static final String JS_LINE_MIDDLE = ":\""; //$NON-NLS-1$
     public static final String JS_LINE_END = "\","; //$NON-NLS-1$
     public static final String JS_FILE_END = "})"; //$NON-NLS-1$
 
     public static final String JAVA_LINE_START = ""; //$NON-NLS-1$
     public static final String JAVA_LINE_MIDDLE = "="; //$NON-NLS-1$
     public static final String JAVA_LINE_END = ""; //$NON-NLS-1$
 
     public static final String DOJO_TK = "dojotk"; //$NON-NLS-1$
     public static final String JAVA_RB = "javarb"; //$NON-NLS-1$
 
     public static final String LANGUAGE_ID = "bundle_language"; //$NON-NLS-1$
     public static final String BUILD_STAMP_ID = "x_timeStamp"; //$NON-NLS-1$
 
     protected InputStream getInputStream(String filename) throws FileNotFoundException {
         return new BufferedInputStream(
             new FileInputStream(filename)
         );
     }
 
     protected OutputStream getOutputStream(String filename) throws FileNotFoundException {
         return  new BufferedOutputStream(
             new FileOutputStream(filename, false)
         );
     }
 
     /**
      * Convert TMX file to resource bundles.
      *
      * Note: this method decouples the File object instanciation from the directory
      * content parsing just to ease the unit testing.
      *
      * @param filename Name of the TMX file to process
      */
     protected void convert(String filename) throws IOException {
         InputStream sourceBIF = getInputStream(sourcePath + File.separator + filename + ".tmx");
 
         int underscoreIndex = filename.indexOf('_');
         String locale = tmxFilenameBase.length() < filename.length() && 0 < underscoreIndex
             ? filename.substring(underscoreIndex + 1)
             : null;
         OutputStream jsBOF = null;
         if (locale == null) {
             jsBOF = getOutputStream(jsDestPath + File.separator + tmxFilenameBase + ".js");
         }
         else {
             String nestedDirectory = jsDestPath + File.separator + locale.replace('_', '-').toLowerCase();
             boolean directoryExists = (getFile(nestedDirectory)).exists();
             if (!directoryExists) {
                 boolean directoryCreated = (getFile(nestedDirectory)).mkdir();
                 if (!directoryCreated) {
                     throw new IOException("Cannot create nested directory: " + nestedDirectory);
                 }
             }
             jsBOF = getOutputStream(nestedDirectory + File.separator + tmxFilenameBase + ".js");
         }
 
         OutputStream javaBOF = getOutputStream(javaDestPath + File.separator + filename + ".properties-utf8");
 
         convert(locale, sourceBIF, jsBOF, javaBOF);
 
         sourceBIF.close();
         jsBOF.close();
         javaBOF.close();
     }
 
     /**
      * Nop entity resolver to avoid accessing an external DTD.
      * Without this, you need to be online when you run the converter.
      */
     protected EntityResolver getEntityResolver() {
         return new EntityResolver() {
             public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                 return new InputSource(new StringReader(EMPTY));
             }
         };
     }
 
     private DocumentBuilder parser = null;
 
     protected DocumentBuilderFactory getFactory() {
         return DocumentBuilderFactory.newInstance();
     }
 
     protected DocumentBuilder getParser() {
         if (parser == null) {
             DocumentBuilderFactory factory = getFactory();
             factory.setValidating(false);
 
             parser = null;
             try {
                 parser = factory.newDocumentBuilder();
             }
             catch (ParserConfigurationException ex) {
                 reportError("Cannot instanciate the DOM parser [Exception: " + ex + "]");
                 stopProcess();
                 return null;
             }
             parser.setEntityResolver(getEntityResolver());
         }
 
         return parser;
     }
 
     protected Document getDocument(InputStream sourceIS) {
         try {
             return getParser().parse(sourceIS);
         }
         catch (SAXException ex) {
             reportError(" [Exception: " + ex + "] ");
             stopProcess();
         }
         catch (IOException ex) {
             reportError(" [Exception: " + ex + "] ");
             stopProcess();
         }
         return null;
     }
 
     protected NodeList getNodeList(Node elem, String path) {
         try {
             return XPathAPI.selectNodeList(elem, path); //$NON-NLS-1$
         }
         catch (TransformerException ex) {
             reportError(" [Exception: " + ex + "] ");
             stopProcess();
         }
         return null;
     }
 
     /**
      * Convert TMX file to resource bundle.
      *
      * @param locale Identifier of the current locale
      * @param sourceIS Input stream with the TMX entries
      * @param jsOS Output stream storing the entries in a JavaScript map
      * @param javaOS Output stream storing the entries in a Java property file
      */
     protected void convert(String locale, InputStream sourceIS, OutputStream jsOS, OutputStream javaOS) throws IOException {
         Document doc = getDocument(sourceIS);
 
         NodeList nl = getNodeList(doc, "/tmx/body/tu"); //$NON-NLS-1$
 
         jsOS.write(JS_FILE_START.getBytes());
 
         Pattern bracesPattern = Pattern.compile("(\\{[\\d]+\\})"); //$NON-NLS-1$
         boolean languageIdSearched = true;
 
         for (int i = 0; i < nl.getLength(); i++) {
             Element tu = (Element)nl.item(i);
             String id = tu.getAttribute("tuid"); //$NON-NLS-1$
             if (id.length() == 0) {
                 reportError("TMXConverter: Cannot find tuid."); //$NON-NLS-1$
                 return;
             }
 
             NodeList props = getNodeList(tu, "prop"); //$NON-NLS-1$
 
             boolean saveToJS = false;
             boolean saveToJava = false;
             for (int j = 0; j < props.getLength(); j++) {
                 Element prop = (Element)props.item(j);
                 String type = prop.getAttribute("type"); //$NON-NLS-1$
                 if ("x-tier".equals(type)) { //$NON-NLS-1$
                     String nodeValue = prop.getFirstChild().getNodeValue();
                     boolean b0 = saveToJS;
                     boolean b1 = DOJO_TK.equals(nodeValue);
                     saveToJS = b0 || b1;
                     boolean b2 = saveToJava;
                     boolean b3 = JAVA_RB.equals(nodeValue);
                     saveToJava = b2 || b3;
                 }
             }
             if (!saveToJS && !saveToJava) {
                 reportError("TMXConverter: Cannot find x-tier information for entry \"" + id + "\"."); //$NON-NLS-1$
                 return;
             }
             props = null;
 
             String text = null;
             NodeList l = getNodeList(tu, "tuv/seg/text()"); //$NON-NLS-1$
             Node n = l.item(0);
             if (n != null) {
                 text = n.getNodeValue();
             }
             l = null;
             if (text == null) {
                 reportError("TMXConverter: Empty text node of <seg> for entry \"" + id + "\"."); //$NON-NLS-1$
                 return;
             }
 
             if (saveToJS) {
                // js.append("'" + id + "':\"").append(text.replaceAll("(\\{[\\d]+\\})", "%$1")).append("\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                 String updatedText = text;
                 if (text.indexOf('{') != -1) {
                     // Use the replacement with regular expression only if needed
                    updatedText = bracesPattern.matcher(text).replaceAll("%$1"); //$NON-NLS-1$
 
                 }
                 jsOS.write((JS_LINE_START + id + JS_LINE_MIDDLE + updatedText + JS_LINE_END + NL).getBytes()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
             }
             if (saveToJava) {
                 javaOS.write((id + JAVA_LINE_MIDDLE + text + NL).getBytes()); //$NON-NLS-1$
             }
 
             if(languageIdSearched) {
                 if (LANGUAGE_ID.equals(id)) {
                     processedLanguages.put(locale == null ? "en" : locale, text);
                     languageIdSearched = false;
                 }
             }
         }
         nl = null;
 
         jsOS.write((JS_LINE_START + BUILD_STAMP_ID + JS_LINE_MIDDLE + buildStamp + "\"" + JS_FILE_END).getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
         javaOS.write((BUILD_STAMP_ID + JAVA_LINE_MIDDLE + buildStamp).getBytes()); //$NON-NLS-1$
 
         jsOS.flush();
         javaOS.flush();
     }
 
     protected int getMinimumJSSize() {
         int buildStampLength = buildStamp == null ? "null".length() : buildStamp.length();
         return JS_FILE_START.length() + JS_LINE_START.length() + BUILD_STAMP_ID.length() + JS_LINE_MIDDLE.length() + buildStampLength + (JS_LINE_END.length() - 1) + JS_FILE_END.length();
     }
 
     protected int getMinimumJavaSize() {
         int buildStampLength = buildStamp == null ? "null".length() : buildStamp.length();
         return BUILD_STAMP_ID.length() + JAVA_LINE_MIDDLE.length() + buildStampLength;
     }
 
     /**
      * Convert TMX file to resource bundles.
      *
      * Note: this method decouples the File object instanciation from the directory
      * content parsing just to ease the unit testing.
      */
     protected void saveSupportedLanguages() throws IOException {
         OutputStream langBOF = getOutputStream(
             javaDestPath + File.separator + languageFilenameBase + ".properties-utf8"
         );
 
         saveSupportedLanguages(langBOF);
 
         langBOF.close();
     }
 
     /**
      * Convert TMX file to resource bundles.
      *
      * Note: this method decouples the File object instanciation from the directory
      * content parsing just to ease the unit testing.
      *
      * @param file Stream of the file where the language list is going to be saved
      */
     protected void saveSupportedLanguages(OutputStream file) throws IOException {
         ResourceBundle documentedLanguages = null;
         try {
             documentedLanguages = getDocumentedLanguages();
         }
         catch (MissingResourceException ex) {
             reportError("No previous language definitions found.");
             // Ignore execption because the file content will be built from the list of processed TMX
         }
         if (documentedLanguages != null) {
             Enumeration<String> localeEnum = documentedLanguages.getKeys();
             while (localeEnum.hasMoreElements()) {
                 String locale = localeEnum.nextElement();
                 String transcript = documentedLanguages.getString(locale);
                 if (processedLanguages.get(locale) != null) {
                     transcript = processedLanguages.get(locale);
                     processedLanguages.remove(locale);
                 }
                 file.write((locale + JAVA_LINE_MIDDLE + transcript + NL).getBytes());
             }
         }
         for (String locale: processedLanguages.keySet()) {
             file.write((locale + JAVA_LINE_MIDDLE + processedLanguages.get(locale) + NL).getBytes());
         }
     }
 }
