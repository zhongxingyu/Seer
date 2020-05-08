 package elr.core;
 
 import elr.Starter;
 import elr.gui.Debug;
 import elr.core.modules.configuration.Configuration;
 import elr.core.modules.ExceptionControl;
 import elr.core.modules.AES;
 import elr.core.modules.Directory;
 import elr.core.modules.compressor.Compressor;
 import elr.core.modules.compressor.Compressor.CompressionLevel;
 import elr.gui.Splash;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 /**
  * Initial class which starts the launcher.
  * @author Infernage
  */
 public class Booter extends Starter.StaticForms{
     private static Hook shutdownHook; //Thread executed at the end of JVM.
     
     private Booter(){} //Don't let anyone to instance this.
     
     /**
      * Puts the internal property {@code ConfigVar.elr_selectedInstance} to the selected instance, or 
      * {@code null} in otherwise.
      * @param instanceName The instance name.
      * @return {@code true} if the map was succesfully updated with the new value, {@code false} if the
      * value given is null.
      */
     public static boolean setSelectedInstance(String instanceName){
         if (Directory.instances() != null){
             File instance = new File(Directory.instances(), instanceName);
             if (instance.exists()){
                 Stack.config.setInternalProperty(Configuration.ConfigVar.elr_selectedInstance.name(), 
                         instanceName);
                 return true;
             } else{
                 Stack.config.resetInternalProperty(new Booter(), Configuration.ConfigVar
                         .elr_selectedInstance.name());
                 return false;
             }
         }
         return false;
     }
     
     /**
      * Saves all configs into the file.
      */
     public static void saveConfig(){
         shutdownHook.saveConfigs();
     }
     
     /**
      * Main boot method to start.
      */
     public static void startBoot(){
         try {
             if (shutdownHook != null) throw new RuntimeException("Hook already created");
             Splash.displayMsg("Loading configuration and parameters...");
             Splash.set(10);
             Stack.crypter = new AES(password);
             String OS = setOS();
             String currentPath = getStaticCurrentJar();
             String configPath = getConfigurationPath(OS);
             System.out.println("Current file: " + currentPath + "\nConfiguration path: " + configPath);
             File root = new File(configPath);
             File config = new File(root, "ELR.cfg");
             if (config.exists()) config.delete();
             File[] files = root.listFiles();
             for (File file : files) {
                 if (file.getName().contains(";ELR_cfg-File_.cxz;")){
                     System.out.println("Decoding launcher state");
                     Splash.displayMsg("Decoding configuration...");
                     Splash.set(15);
                     try {
                        File decrypted = Compressor.cryptedDecompression(file, file.getParentFile(), Stack.crypter);
                        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(decrypted))){
                             Stack.config = (Configuration) input.readObject();
                             Stack.config.setInternalProperty(Configuration.ConfigVar.elr_currentJar.name(), 
                                     currentPath);
                             Stack.config.setInternalProperty(Configuration.ConfigVar.elr_currentPath.name(), 
                                     new File(currentPath).getParent());
                             if (Stack.config.getValueInternalConfig(Configuration.ConfigVar
                                     .elr_selectedInstance.name()) != null){
                                 File selectedInstance = new File(Directory.instances(), Stack.config
                                         .getValueInternalConfig(Configuration.ConfigVar.elr_selectedInstance
                                         .name()));
                                 if (!selectedInstance.exists() || selectedInstance.list().length < 2){
                                     Stack.config.resetInternalProperty(new Booter(), Configuration.ConfigVar
                                             .elr_selectedInstance.name());
                                 }
                             }
                             shutdownHook = new Hook(file, decrypted);
                             Runtime.getRuntime().addShutdownHook(shutdownHook);
                         } catch (IOException | ClassNotFoundException e) {
                             if (shutdownHook != null) shutdownHook.release();
                             if (decrypted != null) decrypted.delete();
                             file.delete();
                             System.out.println("Reset done");
                             e.printStackTrace();
                             throw new RuntimeException(e);
                         }
                     } catch (Exception e) {
                         file.delete();
                         e.printStackTrace();
                         throw new RuntimeException(e);
                     }
                     ModuleLoader.startLoad();
                     return;
                 }
             }
             if (!config.exists()){
                 System.out.println("Encoding new config file");
                 Configuration.createNewConfig(OS, currentPath, root);
                 Splash.displayMsg("Encoding new configuration...");
                 Splash.set(15);
                 File temp = null;
                 try {
                     config.createNewFile();
                    temp = Compressor.cryptedCompression(config, config.getParentFile(), 
                            CompressionLevel.FAST, Stack.crypter);
                     shutdownHook = new Hook(temp, config);
                     Runtime.getRuntime().addShutdownHook(shutdownHook);
                 } catch (Exception e) {
                     if (shutdownHook != null) shutdownHook.release();
                     if (temp != null) temp.delete();
                     config.delete();
                     System.out.println("Reset done");
                     e.printStackTrace();
                     throw new RuntimeException(e);
                 }
                 ModuleLoader.startLoad();
             }
         } catch (IOException | RuntimeException e) {
             e.printStackTrace();
             ExceptionControl.severeExceptionWOStream(1, e, "Failed at boot of the launcher");
         }
     }
     
     /**
      * Gets OS in a static form.
      * @return The operative system.
      */
     public static String getStaticOS(){
         String OS = System.getProperty("os.name").toLowerCase();
         if (OS.contains("win")){
             OS = "windows";
         } else if (OS.contains("lin") || OS.contains("uni")){
             OS = "linux";
         } else if (OS.contains("mac")){
             OS = "macosx";
         } else{
             OS = null;
         }
         return OS;
     }
     
     /**
      * Assigns OS name.
      */
     private static String setOS(){
         String OS = getStaticOS();
         if (OS == null){
             System.out.println("OPERATIVE SYSTEM NOT SUPPORTED!\nExiting with "
                     + "exit code: 9");
             System.err.println("[ERROR]Found operative system not supported! "
                     + "Exiting system!");
             JOptionPane.showMessageDialog(null, "Your operative system is not "
                     + "supported! Only are supported"
                     + " Windows and Linux.\nOperative system in this computer: "
                     + System.getProperty("os.name") + "\nIf you are running one "
                     + "of these operative systems, contact with Infernage");
             Debug de = new Debug(null, true);
             de.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
             de.setLocationRelativeTo(null);
             de.setVisible(true);
             System.exit(9);
         }
         System.out.println("OS: " + OS);
         return OS;
     }
     
     /**
      * Class used to create a thread which will be executed at the end of JVM.
      * @author Infernage
      */
     private static class Hook extends Thread{
         private File decrypted, crypted;
         private FileChannel channel;
         private FileLock locker;
         private ObjectOutputStream output;
         
         public Hook(File cConfig, File dConfig) throws FileNotFoundException, IOException{
             decrypted = dConfig;
             crypted = cConfig;
             FileOutputStream fileOut = new FileOutputStream(decrypted);
             output = new ObjectOutputStream(fileOut);
             channel = fileOut.getChannel();
             locker = channel.lock();
         }
         
         /**
          * Saves all configuration.
          */
         public void saveConfigs(){
             try {
                 output.writeObject(Stack.config);
                 output.flush();
             } catch (Exception e) {
                 try {
                     ExceptionControl.showExceptionWOStream(e, e.toString(), 0);
                 } catch (Exception ex) {
                     //The question is... How the heck that produced an Exception?
                 }
             }
         }
         
         /**
          * Release decrypted file
          */
         public void release() throws IOException{
             if (locker != null) locker.release();
             if (channel != null) channel.close();
             if (output != null) output.close();
         }
         
         @Override
         public void run(){
             try {
                 saveConfigs();
                 release();
                 crypted.delete();
                Compressor.cryptedCompression(decrypted, decrypted.getParentFile(), CompressionLevel.FAST,
                        Stack.crypter);
             } catch (Exception e) {
                 try {
                     ExceptionControl.showExceptionWOStream(e, "Released failed!", 0);
                 } catch (Exception ex) {
                     //The question is... How the heck that produced an Exception?
                 }
             } finally{
                 decrypted.delete();
             }
         }
     }
 }
