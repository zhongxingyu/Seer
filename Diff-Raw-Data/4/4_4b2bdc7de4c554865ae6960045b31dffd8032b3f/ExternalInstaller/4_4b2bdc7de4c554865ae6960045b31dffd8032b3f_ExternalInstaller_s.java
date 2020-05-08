 package com.yoursway.autoupdater.installer.external;
 
 import static com.google.common.collect.Lists.newLinkedList;
 import static com.yoursway.utils.YsFileUtils.createTempFolder;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import com.yoursway.autoupdater.auxiliary.ComponentStopper;
 import com.yoursway.autoupdater.installer.Installation;
 import com.yoursway.autoupdater.installer.Installer;
 import com.yoursway.autoupdater.installer.InstallerException;
 import com.yoursway.utils.log.Log;
 
 public class ExternalInstaller implements Installer {
     
     public static final int PORT = 32123;
     
     private File folder;
     private boolean prepared;
     
     private final boolean gui;
     
     private static InstallerClient client;
     
     public ExternalInstaller() {
         this(false);
     }
     
     public ExternalInstaller(boolean gui) {
         this.gui = gui;
     }
     
     public void install(Installation installation, ComponentStopper stopper) throws InstallerException {
         try {
             setFolder();
         } catch (IOException e) {
             throw new InstallerException("Cannot set temprary folder for external installer", e);
         }
         
         prepare(installation);
         Log.write("External installer prepared in " + folder + ".");
         
         start();
         
         try {
             client().receive(InstallerClient.READY);
             client().send(InstallerClient.STOPPING);
         } catch (Exception e) {
             throw new InstallerException("Cannot communicate with the external installer", e);
         }
         
         boolean stopped = stopper.stop();
         if (!stopped)
             throw new InstallerException("Cannot stop the application");
         
     }
     
     private void setFolder() throws IOException {
         folder = createTempFolder("com.yoursway.autoupdater.installer", null); //!
         
         if (folder.list().length != 0)
             throw new AssertionError("An external installer folder must be empty.");
     }
     
     private void prepare(Installation installation) throws InstallerException {
         
         try {
             installation.setupExternalInstaller(folder);
         } catch (Exception e) {
             throw new InstallerException("Cannot setup external installer", e);
         }
         
         try {
             OutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(folder,
                     "installation")));
             
             installation.toMemento().writeTo(stream);
             
             stream.close();
         } catch (IOException e) {
             throw new InstallerException("Cannot write data for external installer", e);
         }
         
         prepared = true;
     }
     
     private void start() throws InstallerException {
         if (!prepared)
             throw new IllegalStateException("ExternalInstaller should be prepared before starting");
         
         String javaHome = System.getProperty("java.home");
         File java = new File(javaHome, "bin/java"); //! check at windows
         
         //File installer = new File(folder, "com.yoursway.autoupdater.installer_1.0.0.jar"); //!
         
         ProcessBuilder pb = new ProcessBuilder();
         pb.directory(folder);
         
         List<String> cmd;
         try {
             cmd = newLinkedList();
             cmd.add(java.getCanonicalPath());
             cmd.add("-XstartOnFirstThread");
             cmd.add("-cp");
             
             StringBuilder sb = new StringBuilder();
             getJars(sb, folder);
             cmd.add(sb.substring(1));
             
             cmd.add("com.yoursway.autoupdater.installer.InstallerMain");
             cmd.add("gui");
         } catch (Exception e) {
             throw new InstallerException("Cannot build java cmd", e); //!
         }
         pb.command(cmd);
         
         Log.write(cmd.toString());
         
         /*if (gui)
             pb.command(java.getAbsolutePath(), "-XstartOnFirstThread", "-jar", installer.getAbsolutePath(),
                     "gui");
         else
             pb.command(java.getAbsolutePath(), "-jar", installer.getAbsolutePath());*/
 
         try {
             pb.start();
         } catch (IOException e) {
             throw new InstallerException("Cannot start the external installer", e);
         }
     }
     
     private void getJars(StringBuilder sb, File folder) throws IOException {
         for (File file : folder.listFiles()) {
             if (file.getName().endsWith(".jar"))
                 sb.append(":" + file.getCanonicalPath());
             else if (file.isDirectory())
                 getJars(sb, file);
         }
         
     }
     
     public static InstallerClient client() {
         if (client == null)
             client = new InstallerClient();
         return client;
     }
     
     public static void afterInstall() throws InstallerException {
         try {
            client.receive("OK");
            client.send("OK");
         } catch (Throwable e) {
             throw new InstallerException("Cannot communicate with external installer", e);
         }
     }
     
 }
