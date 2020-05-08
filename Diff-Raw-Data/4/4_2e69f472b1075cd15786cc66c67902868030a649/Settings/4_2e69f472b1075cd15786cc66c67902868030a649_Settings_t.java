 /*
  * movie-renamer-core
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.settings;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import com.sun.jna.NativeLibrary;
 import com.sun.jna.Platform;
 
 import fr.free.movierenamer.exception.SettingsSaveFailedException;
 import fr.free.movierenamer.mediainfo.MediaInfoLibrary;
 import fr.free.movierenamer.scrapper.MovieScrapper;
 import fr.free.movierenamer.scrapper.SubtitleScrapper;
 import fr.free.movierenamer.scrapper.TvShowScrapper;
 import fr.free.movierenamer.scrapper.impl.IMDbScrapper;
 import fr.free.movierenamer.scrapper.impl.OpenSubtitlesScrapper;
 import fr.free.movierenamer.scrapper.impl.TheTVDBScrapper;
 import fr.free.movierenamer.utils.ClassUtils;
 import fr.free.movierenamer.utils.FileUtils;
 import fr.free.movierenamer.utils.LocaleUtils;
 import fr.free.movierenamer.utils.NumberUtils;
 import fr.free.movierenamer.utils.StringUtils;
 import fr.free.movierenamer.utils.WebRequest;
 import fr.free.movierenamer.utils.XPathUtils;
 
 /**
  * Class Settings , Movie Renamer settings <br>
  * Only public and non static attributes are written in conf file !
  * 
  * @author Nicolas Magré
  * @author Simon QUÉMÉNEUR
  */
 public final class Settings {
 
   static {
     APPNAME = getApplicationProperty("application.name");
     VERSION = getApplicationProperty("application.version");
     appName_nospace = getApplicationProperty("application.name").replace(' ', '_');
     appFolder = getApplicationFolder();
   }
 
   public static final String APPNAME;
   public static final String VERSION;
   public static final File appFolder;
   private static final String appName_nospace;
 
   // XML
   private static final String arrayEscapeChar = "/_";
   private static final String sZero = "0";
 
   // files
  private static final String configFile = appName_nospace + ".conf";
  private static final String logFile = appName_nospace + ".log";
 
   // Misc
   private static boolean xmlError = false;
   private static String xmlVersion = "";
   
   // Logger
   public static final Logger LOGGER = Logger.getLogger(appName_nospace + " Logger");
 
   // Settings instance
   private static Settings instance;
 
   //lang
   public Locale locale = Locale.getDefault();
   
   // Rename movie filename
   public String movieFilenameFormat = "<t> (<y>)";
   public String movieFilenameSeparator = ", ";
   public int movieFilenameLimit = 3;
   public StringUtils.CaseConversionType movieFilenameCase = StringUtils.CaseConversionType.FIRSTLA;
   public boolean movieFilenameTrim = true;
   public boolean movieFilenameRmDupSpace = true;
   public boolean movieFilenameCreateDirectory = false;
   
   // Renamer movie folder
   public String movieFolderFormat = "<t> (<y>)";
   public String movieFolderSeparator = ", ";
   public int movieFolderLimit = 3;
   public int movieFolderCase = 1;
   public boolean movieFolderTrim = true;
   public boolean movieFolderRmDupSpace = true;
   
   // Rename Tv show filename
   public String tvShowFilenameFormat = "<st> S<s>E<e> <et>";
   public String tvShowFilenameSeparator = ", ";
   public int tvShowFilenameLimit = 3;
   public int tvShowFilenameCase = 1;
   public boolean tvShowFilenameTrim = true;
   public boolean tvShowFilenameRmDupSpace = true;
   
   // Cache
   public boolean clearCache = false;
   
   // Search
   public Class<? extends MovieScrapper> movieScrapper = IMDbScrapper.class;
   public Class<? extends TvShowScrapper> tvshowScrapper = TheTVDBScrapper.class;
   public Class<? extends SubtitleScrapper> subtitleScrapper = OpenSubtitlesScrapper.class;
   public Locale scrapperLang = Locale.ENGLISH;
   public boolean sortBySimiYear = true;
   public int nbResult = 2;
   public boolean displayApproximateResult = false;
   
   // Proxy
   public boolean useProxy = true;
   public String proxyUrl = "10.2.1.10";
   public int proxyPort = 3128;
   
   //http param
   public int requestTimeOut = 30;
   public String customUserAgent = "Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2";
   
   /**
    * Private build for singleton fix
    * 
    * @return
    */
   private static synchronized Settings newInstance() {
     if (instance == null) {
       instance = new Settings();
       // if new, just load values
       try {
         instance.loadSetting();
       } catch (SettingsSaveFailedException e) {
         LOGGER.log(Level.WARNING, e.toString(), e);
         instance = e.getDefaultSettings();
       }
     }
     return instance;
   }
 
   /**
    * Access to the Settings instance
    * 
    * @return The only instance of MR Settings
    */
   public static synchronized Settings getInstance() {
     if (instance == null) {
       instance = newInstance();
     }
     return instance;
   }
 
   /**
    * Constructor
    */
   private Settings() {
     // Log init
     try {
       File logsRoot = new File(Settings.appFolder, "logs");
       if (!logsRoot.isDirectory() && !logsRoot.mkdirs()) {
         throw new IOException("Failed to create logs dir: " + logsRoot);
       }
       FileHandler fh = new FileHandler(logsRoot.getAbsolutePath() + File.separator + logFile);
       LOGGER.addHandler(fh);
     } catch (SecurityException e) {
       LOGGER.log(Level.SEVERE, e.getMessage());
     } catch (IOException e) {
       LOGGER.log(Level.SEVERE, e.getMessage());
     }
   }
 
   /**
    * Load Movie Renamer settings
    * 
    * @return Movie Renamer settings
    */
   private Settings loadSetting() throws SettingsSaveFailedException {
     LOGGER.log(Level.INFO, "Load configuration from {0}", configFile);
     boolean saved;
     Settings config = new Settings();
     File confRoot = new File(Settings.appFolder, "conf");
     File file = new File(confRoot, configFile);
 
     if (!file.exists()) {
       try {
         saved = config.saveSetting();
       } catch (IOException e) {
         saved = false;
       }
       if (!saved) {
         // Set locale
         Locale.setDefault((config.locale.equals(Locale.FRENCH) ? Locale.FRENCH : Locale.ENGLISH));
         throw new SettingsSaveFailedException(config, LocaleUtils.i18n("settingsSaveFailed") + " " + appFolder.getAbsolutePath());
       }
       return loadSetting();
     }
 
     saved = false;
     try {
       // Parse Movie Renamer Settings
       Document xml = WebRequest.getXmlDocument(file.toURI());
       List<Node> nodes = XPathUtils.selectChildren(appName_nospace + "/setting", xml);
       for (Node node : nodes) {
         setValue(node.getNodeName(), XPathUtils.getTextContent(node));
       }
 
       // Define locale on first run
       if (config.locale.equals("")) {
         if (!Locale.getDefault().equals(Locale.FRENCH)) {
           config.locale = Locale.ENGLISH;
         } else {
           config.locale = Locale.FRENCH;
         }
         xmlVersion = VERSION;// Ensures that the settings file is
         // written once only
       }
 
       // Set locale
       Locale.setDefault((config.locale.equals(Locale.FRENCH) ? Locale.FRENCH : Locale.ENGLISH));
       if (VERSION.equals(xmlVersion) && !xmlError) {
         saved = true;
       }
 
     } catch (SAXException ex) {
       LOGGER.log(Level.SEVERE, ClassUtils.getStackTrace("SAXException", ex.getStackTrace()));
     } catch (IOException ex) {
       LOGGER.log(Level.SEVERE, ClassUtils.getStackTrace("IOException : " + ex.getMessage(), ex.getStackTrace()));
     } finally {
       if (!saved) {
         // FIXME No swing in settings (for cli)
         // JOptionPane.showMessageDialog(null,
         // Utils.i18n("lostSettings"), Utils.i18n("Information"),
         // JOptionPane.INFORMATION_MESSAGE);
         try {
           saved = config.saveSetting();
         } catch (IOException e) {
           saved = false;
         }
       }
     }
 
     if (!saved) {
       throw new SettingsSaveFailedException(config, LocaleUtils.i18n("saveSettingsFailed") + " " + appFolder.getAbsolutePath());
     }
 
     return config;
   }
 
   /**
    * Save setting
    * 
    * @return True if setting was saved, False otherwise
    * @throws IOException
    */
   public boolean saveSetting() throws IOException {
     LOGGER.log(Level.INFO, "Save configuration to {0}", configFile);
     File confRoot = new File(Settings.appFolder, "conf");
     if (!confRoot.isDirectory() && !confRoot.mkdirs()) {
       throw new IOException("Failed to create conf dir: " + confRoot);
     }
     try {
       DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
       
       // root elements
       Document doc = docBuilder.newDocument();
       Element rootElement = doc.createElement(appName_nospace);
       doc.appendChild(rootElement);
       
       Attr version = doc.createAttribute("Version");
       version.setValue(VERSION);
       rootElement.setAttributeNode(version);
 
       // setting elements
       Element setting = doc.createElement("setting");
       rootElement.appendChild(setting);
       
       // Variables
       for (Field field : getSettingsFields()) {
           Object value = field.get(this);
           if (value == null) {
             // set blank value if null
             value = "";
           } else {
             if (field.getType().getName().equalsIgnoreCase(Boolean.class.getSimpleName())) {
               // to string for boolean field
             value = ((Boolean) value) ? sZero : "1";
             } else if (field.getType().isArray()) {
               // to string for array fields
               value = StringUtils.arrayToString((Object[]) value, arrayEscapeChar, 0);
             } else if (Collection.class.isAssignableFrom(field.getType())) {
               // to string for Collection fields
               value = StringUtils.arrayToString((List<?>) value, arrayEscapeChar, 0);
             } else if (Class.class.isAssignableFrom(field.getType())) {
               // to string for Class fields
               value = ((Class<?>) value).getName();
             }
           }
         // param elements
         Element param = doc.createElement(field.getName());
         param.appendChild(doc.createTextNode(value.toString()));
         setting.appendChild(param);
       }
 
       // write it to file
       File confFile = new File(confRoot, configFile);
       FileUtils.writeXmlFile(doc, confFile);
 
     } catch (Exception e) {
       LOGGER.log(Level.SEVERE, e.getMessage());
       return false;
     }
     return true;
   }
 
   /**
    * Get the user settings fields
    * 
    * @return
    */
   private Collection<Field> getSettingsFields() {
     Collection<Field> results = new ArrayList<Field>();
     for (Field field : this.getClass().getDeclaredFields()) {
       int mod = field.getModifiers();
       if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
         results.add(field);
       }
     }
     return results;
   }
 
   /**
    * Set a value using field name
    * 
    * @param fieldName
    * @param configValue
    */
   public void setValue(String fieldName, String configValue) {
     try {
       Field field = this.getClass().getField(fieldName);
       Object value = null;
       if (field.getType().getName().equalsIgnoreCase(Boolean.class.getSimpleName())) {
         // Boolean field
         value = sZero.equals(configValue);
       } else if (field.getType().isArray()) {
         // Array field
         value = configValue.split(arrayEscapeChar);
       } else if (Collection.class.isAssignableFrom(field.getType())) {
         // Collection field
         value = StringUtils.stringToArray(configValue, arrayEscapeChar);
       } else if (field.getType().isEnum()) {
         // Enum field
         try {
           @SuppressWarnings("unchecked")
           Enum<?> en = Enum.valueOf(field.getType().asSubclass(Enum.class), configValue);
           value = en;
         } catch (IllegalArgumentException ex) {// Wrong xml setting
           LOGGER.log(Level.SEVERE, " No enum const named {0}", configValue);
           xmlError = true;
         }
       } else if (NumberUtils.isNumeric(field.getType())) {
         @SuppressWarnings("unchecked")
         Class<Number> type = (Class<Number>) field.getType();
         value = NumberUtils.convertToNumber(type, configValue);// Integer.valueOf(configValue);
       } else if (field.getType() == Locale.class) {
         value = new Locale(configValue);
       } else if (field.getType() == Class.class) {
         value = Class.forName(configValue);
       } else {
         // other parsing
         value = StringUtils.unEscapeXML(configValue, "UTF-8");
       }
       if (value != null) {
         field.set(this, value);
       }
     } catch (SecurityException e) {
       LOGGER.log(Level.WARNING, e.getMessage());
     } catch (NoSuchFieldException e) {
       LOGGER.log(Level.CONFIG, "Configuration field no longer exists", e);
     } catch (IllegalArgumentException e) {
       LOGGER.log(Level.WARNING, "Configuration value is not in the good format !", e);
     } catch (IllegalAccessException e) {
       LOGGER.log(Level.WARNING, e.getMessage());
     } catch (ClassNotFoundException e) {
       LOGGER.log(Level.WARNING, "Configuration value is not in the good format !", e);
     }
   }
 
   public String getVersion() {
     return VERSION;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj == null) {
       return false;
     }
 
     if (!(obj instanceof Settings)) {
       return false;
     }
 
     Settings older = (Settings) obj;
     Collection<Field> olderFields = older.getSettingsFields();
     Collection<Field> currentFields = this.getSettingsFields();
     if (currentFields.size() != olderFields.size()) {
       return false;
     }
 
     Iterator<Field> targetIt = currentFields.iterator();
     for (Field field : olderFields) {
       try {
         if (!field.get(older).equals(targetIt.next().get(this))) {
           return false;
         }
       } catch (IllegalArgumentException ex) {
         LOGGER.log(Level.SEVERE, null, ex);
       } catch (IllegalAccessException ex) {
         LOGGER.log(Level.SEVERE, null, ex);
       }
     }
 
     return true;
   }
 
   @Override
   public int hashCode() {
     int hash = 7;
     Collection<Field> fields = this.getSettingsFields();
     for (Field field : fields) {
       try {
         if (field.getType().getName().equalsIgnoreCase(Boolean.class.getSimpleName())) {
           // Boolean field
           hash = 29 * hash + (((Boolean) field.get(this)) ? 1 : 0);
         } else if (field.getType().isArray()) {
           // Array field
           hash = 29 * hash + Arrays.deepHashCode((Object[]) field.get(this));
         } else if (Collection.class.isAssignableFrom(field.getType())) {
           // Collection field
           hash = 29 * hash + ((Collection<?>) field.get(this)).hashCode();
         } else if (field.getType().isEnum()) {
           // Enum field
           hash = 29 * hash + ((Enum<?>) field.get(this)).hashCode();
         } else if (NumberUtils.isNumeric(field.getType())) {
           @SuppressWarnings("unchecked")
           Class<Number> type = (Class<Number>) field.getType();
           hash = 29 * hash + NumberUtils.convertToNumber(type, (String) field.get(this)).hashCode();
         }
       } catch (SecurityException e) {
         LOGGER.log(Level.WARNING, e.getMessage());
       } catch (IllegalArgumentException e) {
         LOGGER.log(Level.WARNING, "Configuration value is not in the goot format !", e);
       } catch (IllegalAccessException e) {
         LOGGER.log(Level.WARNING, e.getMessage());
       }
     }
 
     return hash;
   }
 
   @Override
   public Settings clone() throws CloneNotSupportedException {
     return (Settings) super.clone();
   }
 
   public static String decodeApkKey(String apkkey) {
     return new String(DatatypeConverter.parseBase64Binary(StringUtils.rot13(apkkey)));
   }
 
   public static String getApplicationProperty(String key) {
     return ResourceBundle.getBundle(Settings.class.getName(), Locale.ROOT).getString(key);
   }
 
   private static File getApplicationFolder() {
     String applicationDirPath = System.getProperty("application.dir");
     String userHome = System.getProperty("user.home");
     String userDir = System.getProperty("user.dir");
     File applicationFolder = null;
 
     if (applicationDirPath != null && applicationDirPath.length() > 0) {
       // use given path
       applicationFolder = new File(applicationDirPath);
     } else if (userHome != null) {
       // create folder in user home
       applicationFolder = new File(userHome, Platform.isWindows() ? appName_nospace : "." + appName_nospace);
     } else {
       // use working directory
       applicationFolder = new File(userDir);
     }
 
     // create folder if necessary
     if (!applicationFolder.exists()) {
       applicationFolder.mkdirs();
     }
 
     return applicationFolder;
   }
 
   private static boolean libzen = false;
   private static Boolean mediainfo = null;
 
   /**
    * Check if lib media info is installed
    * 
    * @return True if lib media info is installed, otherwhise false
    */
   public static boolean libMediaInfo() {
     if (mediainfo != null) {
       return mediainfo;
     }
 
     boolean linux = Platform.isLinux();
     if (linux) {
       try {
         NativeLibrary.getInstance("zen");
         libzen = true;
       } catch (LinkageError e) {
         Settings.LOGGER.log(Level.WARNING, "Failed to preload libzen");
       }
     }
     if ((linux && libzen) || !linux) {
       try {
         MediaInfoLibrary.INSTANCE.New();
         mediainfo = Boolean.TRUE;
       } catch (LinkageError e) {
         mediainfo = Boolean.FALSE;
       }
     }
     return mediainfo.equals(Boolean.TRUE);
   }
 
 }
