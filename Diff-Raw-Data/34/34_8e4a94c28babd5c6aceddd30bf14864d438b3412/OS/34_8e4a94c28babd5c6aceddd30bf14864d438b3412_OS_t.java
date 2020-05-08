 package amber.os;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import javax.swing.UIManager;
 
 /**
  *
  * A class to augment java.lang.System.
  *
  * @author Tudor
  */
 public class OS {
 
     private static boolean amberosLoaded;
 
     public static String[] argv() {
         return ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
     }
 
     public static void println(Object... args) {
         for (Object arg : args) {
             print(arg);
         }
         print("\n");
     }
 
     public static void print(Object... args) {
         for (Object arg : args) {
             System.out.print(arg);
         }
     }
 
     public static void sleep(long millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void printTrace() {
         new Throwable().printStackTrace(System.out);
     }
 
     public static void newInstance(Class main, String args) throws IOException, InterruptedException {
         String separator = System.getProperty("file.separator");
         String classpath = System.getProperty("java.class.path");
         String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
         ProcessBuilder processBuilder =
                 new ProcessBuilder(path, "-cp",
                 classpath,
                 main.getName(), args);
         Process process = processBuilder.start();
         process.waitFor();
     }
 
     public static Object[] deepcopy(Object[] arr) {
         Object[] cdata = new Object[arr.length];
         for (int i = 0; i != cdata.length; i++) {
             Object o = arr[i];
             if (o instanceof Object[]) {
                 cdata[i] = deepcopy((Object[]) o);
             } else if (o instanceof Cloneable) {
                 try {
                     cdata[i] = o.getClass().getDeclaredMethod("clone", null).invoke(o, null);
                     continue;
                 } catch (Exception ex) {
                 }
             }
             cdata[i] = o; // Shallow
         }
         return cdata;
     }
 
     public static String getSystemFontName() {
         if (amberosLoaded) {
             switch (getPlatform()) {
                 case WINDOWS:
                     return Win.getSystemFontName();
             }
         }
         return UIManager.getString("Label.font");
     }
 
     public static Platform getPlatform() {
         return Platform.getPlatform();
     }
 
     public static boolean is64Bit() {
         String osArch = System.getProperty("os.arch");
         return "amd64".equals(osArch) || "x86_64".equals(osArch);
     }
 
     public static void loadNativeLibraries() {
         Natives.unpack();
        loadAmberOS();
    }

    protected static void loadAmberOS() {
        String path = System.getProperty("amber.os.librarypath");
        if (path != null) {
            try {
                System.load(path);
                amberosLoaded = true;
            } catch (Exception e) {
                System.err.println("Can't load AmberOS, OS-specific utilities will not work");
                e.printStackTrace();
                amberosLoaded = false;
            }
        } else
             amberosLoaded = false;
     }
 
     public static boolean osLibrariesLoaded() {
         return amberosLoaded;
     }
 
     public static enum Platform {
 
         LINUX, WINDOWS, MAC;
 
         public static Platform getPlatform() {
             String os = System.getProperty("os.name").toLowerCase();
             if (os.indexOf("win") >= 0) {
                 return WINDOWS;
             }
             if ((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0) || (os.indexOf("aix") > 0)) {
                 return LINUX;
             }
             if (os.indexOf("mac") >= 0) {
                 return MAC;
             }
             return null;
         }
     }
 }
