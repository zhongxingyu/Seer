 package amber.os;
 
 import java.io.*;
 import java.net.URL;
 
 /**
  *
  * @author Tudor
  */
 public final class Natives {
 
     private static final boolean DEV = false;
     private static final String REV = "5";
     private static final String NATIVE_DIR = "native/";
     private static final String WIN_DIR = NATIVE_DIR + "windows/";
     private static final String NIX_DIR = NATIVE_DIR + "linux/";
     private static final String MAC_DIR = NATIVE_DIR + "mac/";
     private static final String CACHE_DIR = System.getProperty("java.io.tmpdir") + File.separatorChar
             + "ambercache_1.0.0_" + (DEV ? System.currentTimeMillis() : REV);
 
     public static void unpack() {
         System.setProperty("org.lwjgl.librarypath", CACHE_DIR);
         String amberOs = "";
 
         boolean is64Bit = OS.is64Bit();
         switch (OS.getPlatform()) {
             case WINDOWS:
                 if (is64Bit) {
                     unpack(WIN_DIR + "OpenAL64.dll");
                     unpack(WIN_DIR + "lwjgl64.dll");
                 } else {
                     unpack(WIN_DIR + "OpenAL32.dll");
                     unpack(WIN_DIR + "lwjgl.dll");
                     unpack(WIN_DIR + (amberOs = "amberos32.dll"));
                 }
                 break;
             case LINUX:
                 if (is64Bit) {
                     unpack(NIX_DIR + "libopenal64.so");
                     unpack(NIX_DIR + "liblwjgl64.so");
                 } else {
                     unpack(NIX_DIR + "libopenal.so");
                     unpack(NIX_DIR + "liblwjgl.so");
                 }
                 break;
             case MAC:
                 unpack(MAC_DIR + "liblwjgl.jnilib");
                 unpack(MAC_DIR + "libopenal.dylib");
                 break;
             default:
                 throw new UnsupportedOperationException("unsupported platform");
         }
        if (amberOs.length() > 0)
            System.setProperty("amber.os.librarypath", CACHE_DIR + File.separator + amberOs);
        else
            System.setProperty("amber.os.librarypath", null);
     }
 
     private static void unpack(String path) {
         try {
             URL url = ClassLoader.getSystemResource(path);
 
             File pathDir = new File(CACHE_DIR);
             pathDir.mkdirs();
             File libfile = new File(pathDir, path.substring(path.lastIndexOf("/"), path.length()));
 
             if (!libfile.exists()) {
                 libfile.deleteOnExit();
                 InputStream in = url.openStream();
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(libfile));
 
                 int len;
                 byte[] buffer = new byte[8192];
                 while ((len = in.read(buffer)) > -1) {
                     out.write(buffer, 0, len);
                 }
                 out.flush();
                 out.close();
                 in.close();
             }
         } catch (IOException x) {
             throw new RuntimeException("could not unpack binaries", x);
         }
     }
 }
