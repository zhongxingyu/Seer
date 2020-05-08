 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.bind.JAXBContext;
 
 import org.alex73.android.Context;
 import org.alex73.android.IDecoder;
 import org.alex73.android.IEncoder;
 import org.alex73.android.StAXDecoder;
 import org.alex73.android.StAXEncoder;
 import org.alex73.android.StyledString;
 import org.alex73.android.arsc.ChunkReader;
 import org.alex73.android.arsc.ChunkWriter;
 import org.alex73.android.arsc.ManifestInfo;
 import org.alex73.android.arsc.Resources;
 import org.alex73.android.arsc.StringTable;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 
 import android.control.App;
 import android.control.Translation;
 
 /**
  * Imports binary resources. arg0 is binaries .zips dir
  */
 public class UnpackBinaryResources {
     static String projectPath = "../../Android.OmegaT/Android/";
     static File[] zips;
 
     static List<BuildAll.FileInfo> fileNames = new ArrayList<BuildAll.FileInfo>();
 
     static Translation translationInfo;
 
     public static void main(String[] args) throws Exception {
         readTranslationInfo();
 
         // process binary zips
         zips = new File(args[0]).listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith(".zip");
             }
         });
         for (File zipFile : zips) {
             System.out.println(zipFile);
             ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
 
             ZipEntry ze;
             while ((ze = in.getNextEntry()) != null) {
                 if (ze.getName().contains("GameHub") && zipFile.getName().contains("s2")) {
                     // invalid entry size (expected 2255224840 but got 50345
                     // bytes)
                     continue;
                 }
                 if (ze.getName().endsWith(".apk")) {
                     byte[] apk = IOUtils.toByteArray(in);
                     byte[] manifest = BuildAll.extractFile(apk, "AndroidManifest.xml");
                     ManifestInfo mi = new ManifestInfo(manifest);
                     System.out.println("  " + ze.getName() + "  p:" + mi.getPackageName() + " v:" + mi.getVersion());
                     Context.setByManifest(mi);
                     String dirName = getDirName(mi.getPackageName(), mi.getVersion());
                     if (dirName != null) {
                         byte[] arsc = BuildAll.extractFile(apk, "resources.arsc");
                        processARSC(arsc, dirName, createSuffix(mi));
                     }
                 }
                 System.gc();
             }
         }
     }
 
     protected static void processARSC(byte[] arsc, String dirName, String suffixVersion) throws Exception {
         ChunkReader rsReader = new ChunkReader(arsc);
         Resources rs = new Resources(rsReader);
 
         File out = new File(projectPath + "/source/" + dirName);
         out.mkdirs();
 
         checkAllStrings(rs.getStringTable());
 
         // checks
         ChunkWriter wr = rs.getStringTable().write();
         byte[] readed = rs.getStringTable().getOriginalBytes();
         byte[] written = wr.getBytes();
 
         if (!Arrays.equals(readed, written)) {
             FileUtils.writeByteArrayToFile(new File("/tmp/st-orig"), readed);
             FileUtils.writeByteArrayToFile(new File("/tmp/st-new"), written);
             throw new Exception("StringTables are differ: /tmp/st-orig, /tmp/st-new");
         }
 
         System.out.println("    Store resources in " + out.getAbsolutePath() + " version=" + suffixVersion);
         new StAXEncoder().dump(rs, out, suffixVersion);
 
         // зыходныя рэсурсы захаваныя наноў - для параўняньня
         ChunkWriter rsWriterOriginal = rs.write();
 
         Assert.assertArrayEquals(arsc, rsWriterOriginal.getBytes());
     }
 
     protected static void checkAllStrings(final StringTable table) throws Exception {
         IEncoder encoder = new StAXEncoder();
         IDecoder decoder = new StAXDecoder();
 
         for (int i = 0; i < table.getStringCount(); i++) {
             StyledString ss1 = table.getStyledString(i);
             if (ss1.hasInvalidChars()) {
                 continue;
             }
 
             String x = encoder.marshall(ss1);
             StyledString ss2 = decoder.unmarshall(x);
 
             Assert.assertEquals(ss1.raw, ss2.raw);
             Assert.assertArrayEquals(ss1.tags, ss2.tags);
         }
     }
 
     static final String ALLOWED_VER = "0123456789.";
 
     static String createSuffix(ManifestInfo mi) {
         return mi.getVersion();
     }
 
     static void readTranslationInfo() throws Exception {
         JAXBContext ctx = JAXBContext.newInstance(Translation.class);
         translationInfo = (Translation) ctx.createUnmarshaller()
                 .unmarshal(new File(projectPath + "../translation.xml"));
     }
 
     static String getDirName(String packageName, String versionName) {
         String dirName = null;
         for (App app : translationInfo.getApp()) {
             if (!packageName.equals(app.getPackageName())) {
                 continue;
             }
             if (dirName != null) {
                 throw new RuntimeException("Duplicate dir for package " + packageName);
             }
             dirName = app.getDirName();
         }
         return dirName;
     }
 }
