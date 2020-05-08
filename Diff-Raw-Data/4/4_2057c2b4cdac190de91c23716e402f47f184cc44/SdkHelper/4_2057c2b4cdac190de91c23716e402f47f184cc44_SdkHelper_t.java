 package com.atlassian.sdk.accept;
 
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FileOutputStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Enumeration;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 
 import junit.framework.Assert;
import static com.atlassian.maven.plugins.amps.util.FileUtils.file;
 
 public class SdkHelper
 {
     public static File setupSdk(File baseDir) throws IOException
     {
         File sdkZip = new File(System.getProperty("sdk.zip"));
 
         baseDir.mkdirs();
         unzip(sdkZip, baseDir);
         return new File(baseDir, sdkZip.getName().substring(0, sdkZip.getName().length() - ".zip".length()));
     }
 
     public static void runSdkScript(File sdkHome, File baseDir, String scriptName, String... args)
             throws IOException, InterruptedException
     {
         String extension = isWindows() ? ".bat" : "";
         File bin = new File(sdkHome, "bin");
 
         ExecRunner runner = new ExecRunner();
         File command = new File(bin, scriptName + extension);
 
         if (!isWindows())
         {
             runner.run(baseDir, Arrays.asList(
                     "/bin/chmod",
                     "755",
                     sdkHome.getAbsolutePath() + "/apache-maven/bin/mvn",
                     command.getAbsolutePath()), Collections.<String, String>emptyMap());
         }
         List<String> cmdlist = new ArrayList<String>(Arrays.asList(args));
         cmdlist.add(0, command.getAbsolutePath());
        cmdlist.add("-s");
        cmdlist.add(file(sdkHome, "apache-maven", "conf", "settings.xml").getPath());
 
         Assert.assertEquals(0, runner.run(baseDir, cmdlist, new HashMap<String, String>()
         {{
             put("MAVEN_OPTS", "-Xmx256m");
             put("JAVA_HOME", System.getProperty("java.home"));
             put("PATH", System.getenv("PATH"));
         }}));
     }
 
     public static boolean isWindows()
     {
         String myos = System.getProperty("os.name");
         return (myos.toLowerCase(Locale.ENGLISH).indexOf("windows") > -1);
     }
 
     private static void unzip(File zipfile, File baseDir) throws IOException
     {
         ZipFile zip = new ZipFile(zipfile);
         for (Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries(); entries.hasMoreElements();)
         {
             ZipEntry entry = entries.nextElement();
             File target = new File(baseDir, entry.getName());
             if (entry.isDirectory())
             {
                 target.mkdirs();
             }
             else
             {
                 FileOutputStream fout = new FileOutputStream(target);
                 IOUtils.copy(zip.getInputStream(entry), fout);
                 IOUtils.closeQuietly(fout);
             }
         }
     }
 
 }
