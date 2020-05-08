 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.InvalidPluginException;
 import org.bukkit.plugin.UnknownDependencyException;
 
 /*
  * ##################################
  * ### DISCLAMER - Do not remove! ###
  * ##################################
  * 
  * This work is created by QuarterCode/LoadingByte.
  * 
  * This work is licensed under the Creative Commons Attribution 3.0 Unported License.
  * To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
  */
 
 /**
  * This class is for integrating QuarterBukkit into a plugin.
  */
 public class QuarterBukkitIntegration {
 
     private static final String TITLE_TAG = "title";
     private static final String LINK_TAG  = "link";
     private static final String ITEM_TAG  = "item";
 
     private static URL          feedUrl;
 
     static {
         try {
             feedUrl = new URL("http://dev.bukkit.org/server-mods/quarterbukkit/files.rss");
         }
         catch (final MalformedURLException e) {
             Bukkit.getLogger().severe("Error while initalizing URL (" + e + ")");
         }
     }
 
     /**
      * Call this method in onLoad() for integrating QuarterBukkit into your plugin. It simply installs QuarterBukkit if it isn't.
      */
     public static boolean integrate() {
 
         final File file = new File("plugins", "QuarterBukkit.jar");
 
         try {
             if (!Bukkit.getPluginManager().isPluginEnabled("QuarterBukkit")) {
                 install(file);
             }

            return true;
         }
         catch (final UnknownHostException e) {
             Bukkit.getLogger().warning("Can't connect to dev.bukkit.org!");
         }
         catch (final Exception e) {
             Bukkit.getLogger().severe("An error occurred while installing QuarterBukkit (" + e + ")");
             e.printStackTrace();
         }
 
         return false;
     }
 
     private static void install(final File file) throws IOException, XMLStreamException, UnknownDependencyException, InvalidPluginException, InvalidDescriptionException {
 
         Bukkit.getLogger().info("Installing QuarterBukkit ...");
 
         final File zipFile = new File(file.getParentFile(), "QuarterBukkit_download.zip");
         final URL url = new URL(getFileURL(getFeedData().get("link")));
         final InputStream inputStream = url.openStream();
         final OutputStream outputStream = new FileOutputStream(zipFile);
         outputStream.flush();
 
         final byte[] tempBuffer = new byte[4096];
         int counter;
         while ( (counter = inputStream.read(tempBuffer)) > 0) {
             outputStream.write(tempBuffer, 0, counter);
             outputStream.flush();
         }
 
         inputStream.close();
         outputStream.close();
 
         final File unzipDir = new File(file.getParentFile(), "QuarterBukkit_extract");
         unzipDir.mkdirs();
         unzip(zipFile, unzipDir);
         copy(new File(unzipDir, file.getName()), file);
         zipFile.delete();
         deleteRecursive(unzipDir);
 
         Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(file));
     }
 
     private static void unzip(final File zip, final File destination) throws ZipException, IOException {
 
         final ZipFile zipFile = new ZipFile(zip);
 
         for (final ZipEntry zipEntry : Collections.list(zipFile.entries())) {
             final File file = new File(destination, zipEntry.getName());
             final byte[] BUFFER = new byte[0xFFFF];
 
             if (zipEntry.isDirectory()) {
                 file.mkdirs();
             } else {
                 new File(file.getParent()).mkdirs();
 
                 final InputStream inputStream = zipFile.getInputStream(zipEntry);
                 final OutputStream outputStream = new FileOutputStream(file);
 
                 for (int lenght; (lenght = inputStream.read(BUFFER)) != -1;) {
                     outputStream.write(BUFFER, 0, lenght);
                 }
                 if (outputStream != null) {
                     outputStream.close();
                 }
                 if (inputStream != null) {
                     inputStream.close();
                 }
             }
         }
 
         zipFile.close();
     }
 
     private static void copy(final File source, final File destination) throws FileNotFoundException, IOException {
 
         if (source.isDirectory()) {
             destination.mkdirs();
 
             for (final File entry : source.listFiles()) {
                 copy(new File(source, entry.getName()), new File(destination, entry.getName()));
             }
         } else {
             final byte[] buffer = new byte[32768];
 
             final InputStream inputStream = new FileInputStream(source);
             final OutputStream outputStream = new FileOutputStream(destination);
 
             int numberOfBytes;
             while ( (numberOfBytes = inputStream.read(buffer)) > 0) {
                 outputStream.write(buffer, 0, numberOfBytes);
             }
 
             inputStream.close();
             outputStream.close();
         }
     }
 
     private static void deleteRecursive(final File file) {
 
         if (file.isDirectory()) {
             for (final File entry : file.listFiles()) {
                 deleteRecursive(entry);
             }
         }
 
         file.delete();
     }
 
     private static String getFileURL(final String link) throws IOException {
 
         final URL url = new URL(link);
         URLConnection connection = url.openConnection();
         final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
         String line;
         while ( (line = reader.readLine()) != null) {
             if (line.contains("<li class=\"user-action user-action-download\">")) {
                 return line.split("<a href=\"")[1].split("\">Download</a>")[0];
             }
         }
         connection = null;
         reader.close();
 
         return null;
     }
 
     private static Map<String, String> getFeedData() throws IOException, XMLStreamException {
 
         final Map<String, String> returnMap = new HashMap<String, String>();
         String title = null;
         String link = null;
 
         final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         final InputStream inputStream = feedUrl.openStream();
         final XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);
 
         while (eventReader.hasNext()) {
             XMLEvent event = eventReader.nextEvent();
             if (event.isStartElement()) {
                 if (event.asStartElement().getName().getLocalPart().equals(TITLE_TAG)) {
                     event = eventReader.nextEvent();
                     title = event.asCharacters().getData();
                     continue;
                 }
                 if (event.asStartElement().getName().getLocalPart().equals(LINK_TAG)) {
                     event = eventReader.nextEvent();
                     link = event.asCharacters().getData();
                     continue;
                 }
             } else if (event.isEndElement()) {
                 if (event.asEndElement().getName().getLocalPart().equals(ITEM_TAG)) {
                     returnMap.put("title", title);
                     returnMap.put("link", link);
                     return returnMap;
                 }
             }
         }
 
         return returnMap;
     }
 
     private QuarterBukkitIntegration() {
 
     }
 
 }
