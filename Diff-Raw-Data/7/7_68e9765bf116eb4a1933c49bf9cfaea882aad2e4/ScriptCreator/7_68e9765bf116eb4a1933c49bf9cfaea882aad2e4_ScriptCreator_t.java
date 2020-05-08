 package org.grails.plugin.i18njs;
 
 import grails.util.BuildSettingsHolder;
 import org.apache.log4j.Logger;
 import org.grails.plugin.i18njs.locale.PropertiesExtractStrategy;
 import org.grails.plugin.i18njs.locale.PropertiesExtractStrategyResolver;
 import org.grails.plugin.i18njs.util.RegexUtil;
 
 import javax.servlet.ServletContext;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.FileChannel;
 import java.util.*;
 
 /**
  * Creates a javascript which holds the data from a message bundle(s) for each i18n file.
  * The script is such that properties can be accessed as functions (i.e.:
  * alert(com.mycompany.mymessage()); ).
  * <p/>
  * Edit for using in grails, Alex Sanin. Remain not that much from old class. Take diff for exact changes.
  *
  * @author Jordi Hernández Sellés
  * @author ibrahim Chaehoi
  * @author Alex Sanin
  */
 class ScriptCreator {
 
     public static final String SCRIPT_NAME_PREFIX = "msg_";
     private static final Logger LOGGER = Logger.getLogger(ScriptCreator.class);
     private static final String PROPERTIES_DIR = "/grails-app/i18n/";
     private static final String PROPERTIES_EXT = "properties";
 
     private ServletContext servletContext;
     private List<String> filterList;
     private Properties config = new Properties();
 
     ScriptCreator(Properties properties, ServletContext servletContext) {
         //TODO add params for input, output encoding
         //config.putAll(properties);
         config = properties;
         initFilter(properties.getProperty("filter"));
         this.servletContext = servletContext;
         if (LOGGER.isDebugEnabled())
             LOGGER.debug("i18njs script creator initiated succesfully with config:" + config);
     }
 
     /**
      * init filterList
      *
      * @param filter
      */
     private void initFilter(String filter) {
         if (null != filter) {
             StringTokenizer tk = new StringTokenizer(filter, "\\|");
             filterList = new ArrayList<String>();
             while (tk.hasMoreTokens())
                 filterList.add(tk.nextToken());
         }
     }
 
     /**
      * Create javascript for each i18n file and stores it in outputDir
      *
      * @param outputDir where to store
      * @throws IOException
      */
     public void createScripts(String outputDir) throws IOException {
         String scriptOutPrefix = outputDir + "/" + SCRIPT_NAME_PREFIX;
         Map<Locale, Properties> localeProperties = getLocaleProperties(getI18Dir());
         filterLocaleProperties(localeProperties);
         for (Locale locale : localeProperties.keySet()) {
            //saveToFile(doCreateScript(localeProperties.get(locale)), scriptOutPrefix + locale.getLanguage() + ".js");
            saveToFileNio(doCreateScript(localeProperties.get(locale)), scriptOutPrefix + locale.getLanguage() + ".js");
         }
     }
 
     /**
      * Save data from reader to the file. Using standard IO
      *
      * @param reader   from
      * @param filename to
      * @throws IOException
      */
     private void saveToFile(Reader reader, String filename) throws IOException {
         String tmp;
         BufferedReader scriptIn = new BufferedReader(reader);
         PrintWriter scriptOut = new PrintWriter(filename);
         try {
             while ((tmp = scriptIn.readLine()) != null)
                 scriptOut.println(tmp);
         } finally {
             scriptIn.close();
             scriptOut.close();
         }
 
     }
 
     /**
      * Save data from reader to the file. Using NIO
      *
      * @param reader   from
      * @param filename to
      * @throws IOException
      */
     private void saveToFileNio(Reader reader, String filename) throws IOException {
         FileChannel fc = new FileOutputStream(filename).getChannel();
         try {
             ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (reader.read(buffer.asCharBuffer()) != -1) {
                 buffer.flip();
                 fc.write(buffer);
                 buffer.clear();
             }
         } finally {
             fc.close();
             reader.close();
         }
     }
 
     /**
      * @return File object for directory which contains i18n files
      */
     private File getI18Dir() {
         File i18dir;
         // Determine wether this is run-app or run-war style of runtime.
         if ((Boolean) config.get("isWarDeployed")) {
             i18dir = new File(servletContext.getRealPath("/WEB-INF" + PROPERTIES_DIR));
         } else {
             i18dir = new File("./" + PROPERTIES_DIR);
             if (!i18dir.exists())
                 i18dir = new File(BuildSettingsHolder.getSettings().getResourcesDir().getAbsolutePath() + PROPERTIES_DIR);
         }
         if (LOGGER.isDebugEnabled())
             LOGGER.debug("i18 directory:" + i18dir.getAbsolutePath());
         if (i18dir.isDirectory()) {
             return i18dir;
         } else
             throw new RuntimeException("no i18 directory");
     }
 
     /**
      * @param fromDir directory from
      * @return Map representation for all i18n files in fromDir
      * @throws IOException
      */
     private Map<Locale, Properties> getLocaleProperties(File fromDir) throws IOException {
         Map<Locale, Properties> result = new LinkedHashMap<Locale, Properties>();
         PropertiesExtractStrategyResolver resolver = new PropertiesExtractStrategyResolver(config.getProperty("basename"), PROPERTIES_EXT);
         String[] files = fromDir.list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.matches("^" + config.getProperty("basename") + PropertiesExtractStrategy.PROPERTIES_HOLDER + "\\." + PROPERTIES_EXT + "$");
             }
         });
         if (LOGGER.isDebugEnabled())
             LOGGER.debug("found next properties files: " + Arrays.toString(files));
         for (String filename : files) {
             Properties properties = new Properties();
             properties.load(new InputStreamReader(new FileInputStream(fromDir.getAbsolutePath() + "/" + filename), "UTF-8"));
             //TODO replace on result.put(resolver.resolve(filename), properties); after tests
             Locale locale = resolver.resolve(filename);
             if (locale != null)
                 result.put(locale, properties);
         }
         return result;
 
     }
 
     /**
      * Filter localeProperties agaisnt @see filterList
      *
      * @param localeProperties
      */
     private void filterLocaleProperties(Map<Locale, Properties> localeProperties) {
         for (Properties props : localeProperties.values()) {
             for (Object key : props.keySet()) {
                 if (!matchesFilter((String) key))
                     props.remove(key);
             }
         }
     }
 
     /**
      * @param props properties for creating script
      * @return Reader which contains generated script
      */
     private Reader doCreateScript(Properties props) {
         BundleStringJsonifier bsj = new BundleStringJsonifier(props);
         String script = config.getProperty("template");
         String messages = bsj.serializeBundles().toString();
         script = script.replaceFirst("@namespace", RegexUtil.adaptReplacementToMatcher(config.getProperty("namespace")));
         script = script.replaceFirst("@messages", RegexUtil.adaptReplacementToMatcher(messages));
         return new StringReader(script);
     }
 
     /**
      * @param key - i18n message key
      * @return wether a key matches any of the set filters.
      */
     private boolean matchesFilter(String key) {
         boolean rets = (null == filterList);
         if (!rets) {
             for (Iterator<String> it = filterList.iterator(); it.hasNext() && !rets; )
                 rets = key.startsWith(it.next());
         }
         if (LOGGER.isDebugEnabled())
             LOGGER.debug("filter key:[" + key + "];match:" + rets);
         return rets;
     }
 
 }
