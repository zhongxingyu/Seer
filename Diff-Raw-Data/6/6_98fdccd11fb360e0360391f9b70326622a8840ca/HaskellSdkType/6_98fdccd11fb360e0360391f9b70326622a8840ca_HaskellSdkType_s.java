 package ideah.compiler;
 
 import com.intellij.openapi.projectRoots.*;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.util.io.FileUtil;
 import com.intellij.util.containers.HashMap;
 import ideah.util.ProcessLauncher;
 import org.jdom.Element;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.util.*;
 
 // todo: config page - do not include classpath/sourcepath/etc
 public final class HaskellSdkType extends SdkType {
 
     public static final HaskellSdkType INSTANCE = new HaskellSdkType();
     private static final Icon GHC_ICON = IconLoader.getIcon("/ideah/haskell_16x16.png"); // todo: another icon?
 
     public HaskellSdkType() {
         super("GHC");
     }
 
     public String suggestHomePath() {
         File haskellProgDir = null;
         String[] ghcDirs = null;
         if (SystemInfo.isLinux) {
             haskellProgDir = new File("/usr/lib");
             if (!haskellProgDir.exists())
                 return null;
             ghcDirs = haskellProgDir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.toLowerCase().startsWith("ghc") && new File(dir, name).isDirectory();
                 }
             });
         } else if (SystemInfo.isWindows) {
             String progFiles = System.getenv("ProgramFiles(x86)");
             if (progFiles == null) {
                 progFiles = System.getenv("ProgramFiles");
             }
             haskellProgDir = new File(progFiles, "Haskell Platform");
             if (!haskellProgDir.exists())
                 return progFiles;
             ghcDirs = haskellProgDir.list();
         }
        return haskellProgDir == null
             ? null
            : new File(haskellProgDir, getLatestVersion(ghcDirs)).getAbsolutePath();
     }
 
     private static String getLatestVersion(String[] names) {
         int length = names.length;
         if (length == 0)
             return null;
         if (length == 1)
             return names[0];
         List<GHCDir> ghcDirs = new ArrayList<GHCDir>();
         for (String name : names) {
             ghcDirs.add(new GHCDir(name));
         }
         Collections.sort(ghcDirs, new Comparator<GHCDir>() {
             public int compare(GHCDir d1, GHCDir d2) {
                 Integer[] version1 = d1.version;
                 Integer[] version2 = d2.version;
                 int minSize = Math.min(version1.length, version2.length);
                 for (int i = 0; i < minSize; i++) {
                     int compare = version1[i].compareTo(version2[i]);
                     if (compare != 0)
                         return compare;
                 }
                 return version1.length - version2.length;
             }
         });
         return ghcDirs.get(ghcDirs.size() - 1).name;
     }
 
     public static boolean checkForGhc(File path) {
         File bin = new File(path, "bin");
         if (!bin.exists())
             return false;
         File[] children = bin.listFiles(new FileFilter() {
             public boolean accept(File f) {
                 if (f.isDirectory())
                     return false;
                 return "ghc".equalsIgnoreCase(FileUtil.getNameWithoutExtension(f));
             }
         });
         return children != null && children.length >= 1;
     }
 
     public boolean isValidSdkHome(String path) {
         return checkForGhc(new File(path));
     }
 
     public String suggestSdkName(String currentSdkName, String sdkHome) {
         String suggestedName;
         if (currentSdkName != null && currentSdkName.length() > 0) {
             suggestedName = currentSdkName;
         } else {
             String versionString = getVersionString(sdkHome);
             if (versionString != null) {
                 suggestedName = "GHC " + versionString;
             } else {
                 suggestedName = "Unknown";
             }
         }
         return suggestedName;
     }
 
     private final Map<String, String> cachedVersionStrings = new HashMap<String, String>();
 
     public String getVersionString(String sdkHome) {
         if (cachedVersionStrings.containsKey(sdkHome)) {
             return cachedVersionStrings.get(sdkHome);
         }
         String versionString = getGhcVersion(sdkHome);
         if (versionString != null && versionString.length() == 0) {
             versionString = null;
         }
 
         if (versionString != null) {
             cachedVersionStrings.put(sdkHome, versionString);
         }
 
         return versionString;
     }
 
     @Nullable
     public static String getGhcVersion(String homePath) {
         if (homePath == null || !new File(homePath).exists()) {
             return null;
         }
         try {
             String output = new ProcessLauncher(
                 false, null,
                 homePath + File.separator + "bin" + File.separator + "ghc",
                 "--numeric-version"
             ).getStdOut();
             return output.trim();
         } catch (Exception ex) {
             // ignore
         }
         return null;
     }
 
     public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
         return null;
     }
 
     public void saveAdditionalData(SdkAdditionalData additionalData, Element additional) {
     }
 
     public String getPresentableName() {
         return "GHC";
     }
 
     @Override
     public Icon getIcon() {
         return GHC_ICON;
     }
 
     @Override
     public Icon getIconForAddAction() {
         return getIcon();
     }
 
     @Override
     public String adjustSelectedSdkHome(String homePath) {
         return super.adjustSelectedSdkHome(homePath); // todo: if 'bin' or 'ghc' selected, choose parent folder
     }
 
     @Override
     public void setupSdkPaths(Sdk sdk) {
         // todo: ???
     }
 }
