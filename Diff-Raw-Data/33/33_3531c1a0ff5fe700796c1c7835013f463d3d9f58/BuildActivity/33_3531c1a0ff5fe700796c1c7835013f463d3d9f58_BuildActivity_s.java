 package org.jessies.dalvikexplorer;
 
 import android.app.*;
 import android.content.pm.*;
 import android.os.*;
 import android.util.*;
 import android.view.*;
 import android.widget.*;
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 
 public class BuildActivity extends TextViewActivity {
     protected CharSequence title(String unused) {
         return "Build/Device Details";
     }
     
     protected CharSequence content(String unused) {
         return getBuildDetailsAsString(this, getWindowManager());
     }
     
    // sysconf _SC_PROCESSOR_CONF and _SC_PROCESSOR_ONLN were broken in bionic
    // until mid-2010 (probably Honeycomb?), which broke Runtime.availableProcessors.
    private static int countCores(boolean active) {
         int count = 0;
         BufferedReader in = null;
         try {
            in = new BufferedReader(new FileReader(active ? "/proc/stat" : "/proc/cpuinfo"));
             String line;
             while ((line = in.readLine()) != null) {
                if (!active && line.startsWith("processor")) {
                    ++count;
                } else if (active && line.startsWith("cpu") && !line.startsWith("cpu ")) {
                     ++count;
                 }
             }
             return count;
         } catch (IOException ex) {
             return -1;
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ignored) {
                 }
             }
         }
     }
     
     static String getBuildDetailsAsString(Activity context, WindowManager wm) {
         final Build build = new Build();
         
         // These two fields were added to Build at API level 4 (Android 1.6).
         // On 2011-02-02 about 4% of devices were still running 1.5, so we use reflection.
         // http://developer.android.com/resources/dashboard/platform-versions.html
         final String cpuAbi = getFieldReflectively(build, "CPU_ABI");
         final String manufacturer = getFieldReflectively(build, "MANUFACTURER");
         
         final StringBuilder result = new StringBuilder();
         result.append("Manufacturer: " + manufacturer + "\n"); // "Motorola"
         result.append("Model: " + build.MODEL + "\n"); // "Droid"
         result.append('\n');
         result.append("CPU ABI: " + cpuAbi + "\n"); // "armeabi-v7a"
        int hardwareCoreCount = countCores(false);
        int enabledCoreCount = countCores(true);
         String cores = Integer.toString(hardwareCoreCount);
         if (enabledCoreCount != hardwareCoreCount) {
             cores += " (enabled: " + enabledCoreCount + ")";
         }
         result.append("Cores: " +  cores + "\n"); // 1
         result.append('\n');
         result.append("Brand: " + build.BRAND + "\n"); // "verizon"
         result.append("Board: " + build.BOARD + "\n"); // "sholes"
         result.append("Device: " + build.DEVICE + "\n"); // "sholes"
         result.append('\n');
         result.append("Build Fingerprint: " + build.FINGERPRINT + "\n"); // "verizon/voles/sholes/sholes:2.1/ERD76/22321:userdebug/test-keys"
         result.append('\n');
         result.append("DalvikVM Heap Size: " + Runtime.getRuntime().maxMemory() / (1024*1024) + " MiB\n"); // "24 MiB"
         result.append('\n');
         
         DisplayMetrics metrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(metrics);
         result.append("Screen Density: " + metrics.densityDpi + "dpi (" + metrics.density + "x DIP)\n");
         result.append("Screen Size: " + metrics.widthPixels + " x " + metrics.heightPixels + " pixels\n");
         result.append("Exact DPI: " + metrics.xdpi + " x " + metrics.ydpi + "\n");
         double widthInches = metrics.widthPixels/metrics.xdpi;
         double heightInches = metrics.heightPixels/metrics.ydpi;
         double diagonalInches = Math.sqrt(widthInches*widthInches + heightInches*heightInches);
         result.append(String.format("Approximate Dimensions: %.1f\" x %.1f\" (%.1f\" diagonal)\n", widthInches, heightInches, diagonalInches));
         result.append('\n');
         
         result.append("Features:\n");
         String openGlEsVersion = null;
         for (FeatureInfo feature : context.getPackageManager().getSystemAvailableFeatures()) {
             if (feature.name != null) {
                 result.append("  " + feature.name + "\n");
             } else {
                 openGlEsVersion = feature.getGlEsVersion();
             }
         }
         result.append('\n');
         
         if (openGlEsVersion != null) {
             result.append("OpenGL ES version: " + openGlEsVersion + "\n");
             result.append('\n');
         }
         
         try {
             Class<?> vmDebugClass = Class.forName("dalvik.system.VMDebug");
             Method getVmFeatureListMethod = vmDebugClass.getDeclaredMethod("getVmFeatureList");
             String[] features = (String[]) getVmFeatureListMethod.invoke(null);
             result.append("DalvikVM features:\n");
             for (String feature : features) {
                 result.append("  " + feature + "\n");
             }
             result.append('\n');
         } catch (Throwable ignored) {
         }
         
         result.append("Shared Java libraries:\n");
         for (String library : context.getPackageManager().getSystemSharedLibraryNames()) {
             result.append("  " + library + "\n");
         }
         
         return result.toString();
     }
     
     private static String getFieldReflectively(Build build, String fieldName) {
         try {
             final Field field = Build.class.getField(fieldName);
             return field.get(build).toString();
         } catch (Exception ex) {
             return "unknown";
         }
     }
 }
