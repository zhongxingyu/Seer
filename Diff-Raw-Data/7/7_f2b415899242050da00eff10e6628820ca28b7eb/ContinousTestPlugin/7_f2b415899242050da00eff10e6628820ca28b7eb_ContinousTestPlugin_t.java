 package com.marimon.maven.continous.test.plugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel.MapMode;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.surefire.SurefirePlugin;
 import org.codehaus.plexus.util.Base64;
 
 /**
  * Maven plugin to continously run tests. Developed following (mostly) the guide
  * at: http://maven.apache.org/guides/plugin/guide-java-plugin-development.html
  * 
  * @goal retest
  * @phase test
  * @requiresDependencyResolution test
  */
 public class ContinousTestPlugin extends DecoratorTestPlugin {
 
     public static final long DELAY = 200;
 
     private String _previousHash = "";
 
     public ContinousTestPlugin() {
         super();
     }
 
     @Override
     protected void setPlugin(final SurefirePlugin surefirePlugin) {
         super.setPlugin(surefirePlugin);
     }
 
     /**
      * Main execute method any Mojo must implement
      * 
      * @throws MojoExecutionException
      *             to cause a BUILD ERROR. TODO never thrown yet, must be
      *             implemented
      * @throws MojoFailureException
      *             to cause a BUILD FAILURE. TODO never thrown yet, must be
      *             implemented
      */
     public void execute()
             throws MojoExecutionException, MojoFailureException {
         getLog().info("Continous Test Plugin started...");
         int runs = 0;
         while (!completed(runs)) {
            rebootInnerInstance();
             getLog().info((runs + 1) + ". Checking files...");
 
             while (!detectedChange()) {
                 try {
                     Thread.sleep(DELAY);
                 } catch (InterruptedException e) {
                 }
             }
 
             getLog().info("Files checked.");
             runs++;
         }
         getLog().info("Continous Test Plugin completed.");
     }
 
     private boolean completed(final int runs) {
         return runs >= 10;
     }
 
     @SuppressWarnings("unchecked")
     private void rebootInnerInstance()
             throws MojoExecutionException, MojoFailureException {
         @SuppressWarnings("rawtypes")
         ConcurrentHashMap ctx = new ConcurrentHashMap();
         ctx.putAll(getPluginContext());
         getPlugin().setPluginContext(ctx);
        getPlugin().execute();
     }
 
     private boolean detectedChange() {
         String currentHash =
             new String(Base64.encodeBase64(getCurrentHash()));
         boolean equals = _previousHash.equals(currentHash);
         if (!equals) {
             _previousHash = currentHash;
         }
         return !equals;
     }
 
     private byte[] getCurrentHash() {
         MessageDigest instance;
         try {
             instance = MessageDigest.getInstance("MD5");
             updateHash(getBasedir(), instance);
             updateHash(getTestSourceDirectory(), instance);
             return instance.digest();
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException();
         }
     }
 
     private void updateHash(final File file, final MessageDigest instance) {
         if (file != null) {
             instance.update(file.getName().getBytes());
             if (file.isDirectory()) {
                 File[] listFiles = file.listFiles();
                 for (File f : listFiles) {
                     updateHash(f, instance);
                 }
             } else {
                 updateHashFile(instance, file);
             }
         } else {
             instance.update((byte) System.currentTimeMillis());
         }
 
     }
 
     private void updateHashFile(final MessageDigest instance, final File f) {
         FileInputStream input = null;
         ByteBuffer bb;
         try {
             input = new FileInputStream(f);
             try {
                 bb =
                     input.getChannel().map(MapMode.READ_ONLY, 0,
                         f.length());
                 instance.update(bb);
             } finally {
                 if (input != null) {
                     input.close();
                 }
             }
         } catch (FileNotFoundException e) {
         } catch (IOException e) {
         }
 
     }
 
     @Override
     public Map getPluginContext() {
         return super.getPluginContext();
     }
 
     @Override
     public void setPluginContext(final Map pluginContext) {
         super.setPluginContext(pluginContext);
     }
 
 }
