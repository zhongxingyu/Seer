 package elr.gui.utilities;
 
 import elr.core.Stack;
 import elr.core.modules.Directory;
 import elr.core.modules.Downloader;
 import elr.core.modules.ExceptionControl;
 import elr.core.modules.IO;
 import elr.core.modules.compressor.Compressor;
 import java.io.File;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 
 /**
  * Helper of Versions class.
  * @author Infernage
  */
 public class VersionsClass{
     
     /**
      * Private class used to download minecraft or ModPack.
      */
     private class Download extends Thread{
         private File destinyPath;
         private JProgressBar bar;
         private JLabel state;
         private String[] files = { "minecraft.jar", "jinput.jar", "lwjgl_util.jar", "lwjgl.jar", 
             Stack.config.getOS() + "_natives" };
         private Downloader download;
         private String url;
         private boolean isModPack;
         private int total = 0;
         private boolean exitedWithError = false;
         
         /**
          * Initializes the object.
          * @param url The URL.
          * @param jbar The progressbar to be displayed.
          * @param jstate The label to be displayed.
          * @param dst The place where will be allocated the download.
          * @param isMod {@code true} if is a ModPack, {@code false} if is Minecraft.
          */
         public Download(String url, JProgressBar jbar, JLabel jstate, File dst, boolean isMod){
             this.url = url;
             destinyPath = dst;
             bar = jbar;
             state = jstate;
             isModPack = isMod;
         }
         
         /**
          * Creates connections to calculate the total length.
          * @return An array of all connections.
          * @throws MalformedURLException
          * @throws IOException 
          */
         private HttpURLConnection[] createMinecraftConnections() throws MalformedURLException, 
                 IOException{
             HttpURLConnection[] connections = new HttpURLConnection[files.length];
             connections[0] = (HttpURLConnection) new URL(url).openConnection();
             total = connections[0].getContentLength();
             Properties xml = new Properties();
             xml.loadFromXML(new URL(Stack.getXMLFiles()).openStream());
             for (int i = 1; i < files.length; i++) {
                 connections[i] = (HttpURLConnection) new URL(xml.getProperty(files[i])).openConnection();
                 total += connections[i].getContentLength();
             }
             return connections;
         }
         
         /**
          * Gets the URLs.
          * @param con The array of connections.
          * @return The URLs.
          */
         private URL[] getURLs(HttpURLConnection[] con){
             URL[] urls = new URL[con.length];
             for (int i = 0; i < con.length; i++) {
                 urls[i] = con[i].getURL();
             }
             return urls;
         }
         
         /**
          * Decrypts all files.
          * @param files The array of crypted files.
          * @return A list with all files decrypted.
          * @throws IOException 
          */
         private List<File> decryptFiles(File[] files) throws Exception{
             List<File> list = new ArrayList<>();
             for (File file : files) {
                list.add(Compressor.secureDecompression(file, file.getParentFile(), Stack.crypter));
                 file.delete();
             }
             return list;
         }
         
         /**
          * Allocates all files into an instance.
          * @param dec The decrypted list.
          * @param ins The instance to be allocated.
          * @throws IOException 
          */
         private void installFiles(List<File> dec, String ins) throws IOException{
             File inst = new File(Directory.instances() + File.separator + ins + File.separator
                     + "Minecraft" + File.separator + "bin");
             inst.mkdirs();
             state.setText("Allocating files...");
             for (File file : dec) {
                 IO.copy(file, new File(inst.getAbsolutePath(), file.getName()));
             }
             new File(inst.getAbsolutePath() + File.separator + dec.get(0).getName())
                     .renameTo(new File(inst, files[0]));
             state.setText("Extracting files...");
             File natives = new File(inst.getAbsolutePath(), dec.get(dec.size() - 1)
                     .getName());
             try {
                 ZipFile zip = new ZipFile(natives);
                 zip.extractAll(inst.getAbsolutePath());
             } catch (Exception e) {
                 ExceptionControl.showException(2, e, "Extract failed!");
             }
             new File(inst.getAbsolutePath(), Stack.config.getOS())
                     .renameTo(new File(inst.getAbsolutePath(), "natives"));
             natives.delete();
         }
         
         /**
          * Downloads Minecraft.
          */
         private void downloadMinecraft(){
             HttpURLConnection[] connections = null;
             String instance = null;
             try {
                 connections = createMinecraftConnections();
                 state.setText("Downloading jars");
                 URL[] tmp = getURLs(connections);
                 download = new Downloader(tmp);
                 download.load(destinyPath.getAbsolutePath());
                 download.loadBar(bar);
                 download.setSize(total);
                 download.start();
                 instance = JOptionPane.showInputDialog("Please, input an instance name:");
                 if (instance == null) throw new RuntimeException("Cancelled by user");
                 else if (instance.equals("")) instance = "Default";
                 instance = Stack.frame.checkInstance(instance);
                 while(!download.isDone()) Thread.yield();
                 state.setText("Decrypting files...");
                 List<File> decrypted = decryptFiles(download.destinyFiles());
                 installFiles(decrypted, instance);
                 Stack.versions.finalized(getClone());
             } catch(Exception ex){
                 ExceptionControl.showException(3, ex, "Failed downloading");
                 exitedWithError = true;
             } finally{
                 if (exitedWithError && download != null){
                     download.breakByError();
                 }
                 if (connections != null){
                     for (HttpURLConnection con : connections) {
                         con.disconnect();
                     }
                 }
                 reset();
                 new Thread(){
                     @Override
                     public void run(){
                         while(!download.isDone()) Thread.yield();
                         IO.deleteDirectory(destinyPath);
                         destinyPath.delete();
                     }
                 }.start();
                 if (instance != null && exitedWithError){
                     File tmp = new File(Directory.instances(), instance);
                     IO.deleteDirectory(tmp);
                     tmp.delete();
                 }
             }
         }
         
         /**
          * Decrypts a ModPack.
          * @param modPack The ModPack to decrypt.
          * @return The file decrypted.
          * @throws IOException
          * @throws ZipException 
          */
         private File decodeModPack(File modPack) throws IOException, ZipException{
            return Compressor.secureDecompression(modPack, modPack.getParentFile(), Stack.crypter);
         }
         
         /**
          * Downloads a ModPack.
          */
         private void downloadModPack(){
             HttpURLConnection connection = null;
             String instance = null;
             try {
                 state.setText("Downloading ModPack");
                 connection = (HttpURLConnection) new URL(url).openConnection();
                 total = connection.getContentLength();
                 download = new Downloader(url);
                 download.load(destinyPath.getAbsolutePath());
                 download.loadBar(bar);
                 download.setSize(total);
                 download.start();
                 instance = JOptionPane.showInputDialog("Please, input an instance name:");
                 if (instance == null) throw new RuntimeException("Cancelled by user");
                 else if (instance.equals("")) instance = "Default";
                 instance = Stack.frame.checkInstance(instance);
                 while(!download.isDone()) Thread.yield();
                 state.setText("Decrypting files...");
                 File decoded = decodeModPack(download.destinyFiles()[0]);
                 File tmp = new File(decoded.getParent(), Directory.MINECRAFT);
                 decoded.renameTo(tmp);
                 state.setText("Installing ModPack");
                 File mineInstanced = new File(Directory.instances() +File.separator + instance + 
                         File.separator + Directory.MINECRAFT);
                 IO.copyDirectory(tmp, mineInstanced);
                 File instMods = new File(mineInstanced.getParentFile(), "instMods");
                 instMods.mkdirs();
                 Stack.versions.finalized(getClone());
             } catch (Exception e) {
                 ExceptionControl.showException(3, e, "Failed downloading");
                 exitedWithError = true;
             } finally{
                 if (exitedWithError && download != null){
                     download.breakByError();
                 }
                 if (connection != null){
                     connection.disconnect();
                 }
                 reset();
                 new Thread(){
                     @Override
                     public void run(){
                         while(!download.isDone()) Thread.yield();
                         IO.deleteDirectory(destinyPath);
                         destinyPath.delete();
                     }
                 }.start();
                 if (instance != null && exitedWithError){
                     final File tmp = new File(Directory.instances(), instance);
                     new Thread(){
                         @Override
                         public void run(){
                             while(!download.isDone()) Thread.yield();
                             IO.deleteDirectory(tmp);
                             tmp.delete();
                         }
                     }.start();
                 }
             }
         }
         
         @Override
         public void run(){
             if (!isModPack){
                 downloadMinecraft();
             } else{
                 downloadModPack();
             }
         }
     }
     
     /**
      * Used in Download class.
      * @return The object of VersionsClass.
      */
     private VersionsClass getClone(){ return this; }
     
     /**
      * Reinitializes the GUI.
      */
     private void reset(){
         Stack.versions.reload(getClone());
     }
     
     /**
      * Initializes the Download class.
      * @param url The URL.
      * @param bar The progressbar to be displayed.
      * @param state The label to be displayed.
      * @param isModPack {@code true} if it will download a ModPack, {@code false} if it will download Minecraft.
      * @throws Exception 
      */
     public void download(String url, JProgressBar bar, JLabel state, boolean isModPack) throws Exception{
         bar.setVisible(true);
         bar.setEnabled(true);
         File tmp = new File(Directory.root(), "Files");
         tmp.mkdirs();
         Download download = new Download(url, bar, state, tmp, isModPack);
         download.start();
     }
     
     /**
      * Closes the GUI.
      */
     public void close(){
         Stack.versions.setVisible(false);
         Stack.versions.reload(this);
         Stack.frame.setVisible(true);
         Stack.frame.locateInstances();
     }
     
     /**
      * Initializes a DefaultListModel from a map.
      * @param map The map of all Minecrafts and ModPacks.
      * @return The DefaultListModel initialized.
      */
     public DefaultListModel getModel(SortedMap<String, String> map){
         DefaultListModel model = new DefaultListModel();
         List<String> list = new ArrayList<>();
         for (String string : map.keySet()) {
             list.add(string);
         }
         Collections.reverse(list);
         for (String string : list) {
             model.addElement(string);
         }
         return model;
     }
     
     /**
      * Gets the recent version of Minecraft.
      * @param versions The map of Minecraft versions.
      * @return The recent version.
      */
     public String obtainRecentVersion(SortedMap<String, String> versions){
         SortedMap<String, String> tmp = new TreeMap<>();
         for (Map.Entry<String, String> entry : versions.entrySet()) {
             String string = entry.getKey();
             String string1 = entry.getValue();
             tmp.put(string, string1);
         }
         String actualRecent = tmp.lastKey().substring(0, tmp.lastKey().lastIndexOf(".")), temp = actualRecent;
         while(actualRecent.equals(temp)){
             tmp.remove(tmp.lastKey());
             temp = tmp.lastKey().substring(0, tmp.lastKey().lastIndexOf("."));
         }
         return temp;
     }
     
     /**
      * Creates a map where will be the Minecraft versions.
      * @return The map.
      */
     public TreeMap checkVersions(){
         TreeMap<String, String> map = new TreeMap<>();
         try {
             URL url = new URL(Stack.getXMLMinecraftVersions());
             Properties prop = new Properties();
             prop.loadFromXML(url.openStream());
             for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                 Object object = entry.getKey();
                 Object object1 = entry.getValue();
                 object = "Minecraft version " + object;
                 System.out.println("Adding " + object);
                 map.put((String) object, (String) object1);
             }
         } catch (Exception e) {
             ExceptionControl.showException(2, e, "Failed to check versions of minecraft");
             map.clear();
             map.put("No versions founded", null);
         }
         return map;
     }
     
     /**
      * Creates a map where will be the ModPacks.
      * @return The map.
      */
     public TreeMap checkModPacks(){
         TreeMap<String, String> map = new TreeMap<>();
         try {
             URL url = new URL(Stack.getXMLModPacks());
             Properties prop = new Properties();
             prop.loadFromXML(url.openStream());
             for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                 Object object = entry.getKey();
                 Object object1 = entry.getValue();
                 System.out.println("Adding modPack" + object);
                 map.put((String) object, (String) object1);
             }
         } catch (Exception e) {
             Stack.console.printError("Failed to check ModPacks", 3, this.getClass());
             map.clear();
             map.put("No ModPacks founded", null);
         }
         return map;
     }
 }
