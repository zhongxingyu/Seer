 package eu.scapeproject;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class PosixStorage {
 
     private final File xmlDirectory;
     private final File datastreamDirectory;
     private final Pattern versionPattern = Pattern.compile("version\\-\\d*\\.xml");
     
 
     public PosixStorage(String directory) {
         File parent = new File(directory);
         if (!parent.exists()) {
             parent.mkdir();
         }
         if (!parent.canExecute() || !parent.canRead() || !parent.canWrite()) {
             throw new RuntimeException("Unable to access directory " + parent.getAbsolutePath());
         }
 
         xmlDirectory = new File(parent, "foxml");
         if (!xmlDirectory.exists()) {
             xmlDirectory.mkdir();
         }
         if (!xmlDirectory.canExecute() || !xmlDirectory.canRead() || !xmlDirectory.canWrite()) {
             throw new RuntimeException("Unable to access directory " + xmlDirectory.getAbsolutePath());
         }
 
         datastreamDirectory = new File(parent, "datastreams");
         if (!datastreamDirectory.exists()) {
             datastreamDirectory.mkdir();
         }
         if (!datastreamDirectory.canExecute() || !datastreamDirectory.canRead() || !datastreamDirectory.canWrite()) {
             throw new RuntimeException("Unable to access directory " + datastreamDirectory.getAbsolutePath());
         }
     }
 
     public boolean exists(String id, Integer versionNumber) throws IOException{
         if (versionNumber == null){
             versionNumber=getLatestVersionNumber(id);
         }
         try {
             return new File(getEntityDir(id), "version-" + versionNumber + ".xml").exists();
         } catch (IOException e) {
             return false;
         }
     }
 
     private File getEntityDir(String id) throws IOException {
         File f = new File(xmlDirectory, id);
         if (f.exists() && !f.isDirectory()) {
             throw new IOException("not a directory " + f.getAbsolutePath());
         }
         if (!f.exists()) {
             f.mkdir();
         }
         return f;
     }
 
     public int getLatestVersionNumber(String id) throws IOException {
         int version = 1;
         File dir = getEntityDir(id);
         for (String name : dir.list()) {
             Matcher m = versionPattern.matcher(name);
             if (m.find()) {
                 version = Math.max(version, Integer.parseInt(name.substring(m.start() + 8, m.end() - 4)));
             }
         }
         return version;
     }
 
     public int getNewVersionNumber(String id) throws IOException {
         return getLatestVersionNumber(id) + 1;
     }
 
     public List<String> getVersionList(String id) throws IOException {
         List<String> versionList = new ArrayList<String>();
         File dir = getEntityDir(id);
         for (String name : dir.list()) {
             Matcher m = versionPattern.matcher(name);
             if (m.find()) {
                 versionList.add(name.substring(m.start() + 8, m.end() - 4));
             }
         }
         return versionList;
     }
 
     public byte[] getXML(String id) throws Exception {
         return getXML(id,null);
     }
 
     public byte[] getXML(String id, Integer version) throws Exception {
         if (version == null){
             version=getLatestVersionNumber(id);
         }
         final File entityDir = new File(xmlDirectory, id);
         if (!entityDir.exists() || !entityDir.canRead() || !entityDir.isDirectory()) {
             throw new FileNotFoundException("Unable to open dir " + entityDir.getAbsolutePath());
         }
         final File f = new File(entityDir, "version-" + version + ".xml");
         if (!f.exists() || !f.canRead()) {
             throw new FileNotFoundException("Unable to open file " + f.getAbsolutePath());
         }
         return IOUtils.toByteArray(new FileInputStream(f));
     }
 
     public void purge() throws Exception {
         FileUtils.deleteDirectory(xmlDirectory);
         FileUtils.deleteDirectory(datastreamDirectory);
     }
 
     public void saveXML(byte[] blob, String name, int version, boolean overwrite) throws Exception {
         File entityDir = new File(xmlDirectory, name);
         if (entityDir.exists() && (entityDir.isFile() || !entityDir.canWrite())) {
             throw new IOException("Unable to write to " + entityDir.getAbsolutePath());
         }
         if (!entityDir.exists()) {
             entityDir.mkdir();
         }
         File f = new File(entityDir, "version-" + version + ".xml");
         if (f.exists() && !overwrite) {
             throw new IOException("File " + f.getAbsolutePath() + " exists already!");
         }
         OutputStream out = null;
         try {
             out = new FileOutputStream(f);
             IOUtils.write(blob, out);
         } finally {
             IOUtils.closeQuietly(out);
         }
     }
 }
