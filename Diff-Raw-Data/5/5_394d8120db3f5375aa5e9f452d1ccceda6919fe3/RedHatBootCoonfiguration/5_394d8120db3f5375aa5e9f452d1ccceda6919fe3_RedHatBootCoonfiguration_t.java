 package hudson.plugins.pxe;
 
 import hudson.Extension;
 import hudson.Util;
 import static hudson.util.FormValidation.error;
 import org.jvnet.hudson.tftpd.Data;
 import org.kohsuke.loopy.FileEntry;
 import org.kohsuke.loopy.iso9660.ISO9660FileSystem;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerResponse;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Arrays;
 
 /**
  * RedHat/Fedora boot configuration.
  *
  * @author Kohsuke Kawaguchi
  */
 public class RedHatBootCoonfiguration extends LinuxBootConfiguration {
     public final String additionalPackages;
     public final String password;
 
     @DataBoundConstructor
     public RedHatBootCoonfiguration(File iso, String password, String additionalPackages) {
         super(iso);
 
         if(Util.fixEmptyAndTrim(password)==null)    password="hudson";
         if(!password.startsWith("$1$"))
             password = Crypt.cryptMD5("abcdefgh",password);
         this.password = password;
 
        this.additionalPackages = Util.fixEmptyAndTrim(additionalPackages);
     }
 
     protected String getIdSeed() {
         return getRelease().replaceAll("[ ()]","");
     }
 
     protected FileEntry getTftpIsoMountDir(ISO9660FileSystem fs) throws IOException {
         return fs.get("/images/pxeboot");
     }
 
     /**
      * Serves menu.txt by replacing variables.
      */
     public Data tftp(String fileName) throws IOException {
         if(fileName.equals("splash.jpg")) {
             ISO9660FileSystem fs = new ISO9660FileSystem(iso,false);
             return new FileEntryData(fs.grab("/isolinux/splash.jpg"));
         }
 
         return super.tftp(fileName);
     }
 
     /**
      * Serves the kickstart file
      */
     public void doKickstart(StaplerResponse rsp) throws IOException {
         serveMacroExpandedResource(rsp,"kickstart.txt");
     }
 
     /**
      * Package list formatted in the kickstart format.
      */
    public String getPackageList() {
         if(additionalPackages==null)    return "";
         return Util.join(Arrays.asList(additionalPackages.split(" +")),"\n");
     }
 
     @Extension
     public static class DescriptorImpl extends IsoBasedBootConfigurationDescriptor {
         public String getDisplayName() {
             return "RedHat/Fedora";
         }
 
         /**
          * This returns string like "Fedora 10"
          *
          * TODO: where can we get the architecture information?
          */
         protected String getReleaseInfo(File iso) throws IOException {
             ISO9660FileSystem fs=null;
             try {
                 try {
                     fs = new ISO9660FileSystem(iso,false);
                 } catch (IOException e) {
                     LOGGER.log(Level.INFO,iso+" isn't an ISO file?",e);
                     throw error(iso+" doesn't look like an ISO file");
                 }
 
                 FileEntry info = fs.get("/.treeinfo");
                 if(info==null)
                     throw error(iso+" doesn't look like a RedHat/Fedora CD/DVD image");
 
                 /* On Fedora 10 DVD, this file contains:
                     [general]
                     family = Fedora
                     timestamp = 1227142151.33
                     variant = Fedora
                     totaldiscs = 1
                     version = 10
                     discnum = 1
                     packagedir =
                     arch = i386
 
                    On CentOS, this entry was:
                     [general]
                     family = CentOS
                     timestamp = 1237646605.22
                     totaldiscs = 1
                     version = 5.3
                     discnum = 1
                     packagedir = CentOS
                     arch = i386
                  */
                 Map<String,String> section = new TreeMap<String,String>();
                 BufferedReader r = new BufferedReader(new InputStreamReader(info.read()));
                 try {
                     String line;
                     while((line=r.readLine())!=null) {
                         if(line.contains(" = ")) {
                             String[] tokens = line.split("=");
                             section.put(tokens[0].trim(),tokens[1].trim());
                         }
                     }
                     if(!section.containsKey("family") || !section.containsKey("version") || !section.containsKey("arch"))
                         throw error(iso+" doesn't contain the name entry in media.repo");
                     // should be something like "CentOS 5.3 (i386)"
                     return Util.join(Arrays.asList(
                             section.get("family"),section.get("version"),"("+section.get("arch")+")")," ");
                 } finally {
                     r.close();
                 }
             } finally {
                 if(fs!=null)
                     fs.close();
             }
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(RedHatBootCoonfiguration.class.getName());
 }
