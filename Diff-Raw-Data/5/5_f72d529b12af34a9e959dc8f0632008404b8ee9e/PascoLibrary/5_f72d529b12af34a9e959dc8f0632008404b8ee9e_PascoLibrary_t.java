 package org.concord.sensor.pasco.jna;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.Platform;
 import com.sun.jna.Structure;
 import com.sun.jna.Native.DeleteNativeLibrary;
 
 public class PascoLibrary {
 	private static PascoLibrary instance;
 	private PascoJNALibrary jnaLib;
 	private int handle = 0;
 
 	// BUG because the native code doesn't handle multiple PasInit we need to only create one version of this class
 	public static PascoLibrary getInstance() {
 		if(instance != null){
 			return instance;
 		}
 		instance = new PascoLibrary();
 		return instance;
 	}
 	
 	public void initLibrary() throws IOException, InterruptedException
 	{
 		File nativeLibFile = getNativeLibraryFromJar();
 		String nativeLibPath = nativeLibFile.getAbsolutePath();
 
 		// Not sure if we need this
 		Map<String, Object> options = new HashMap<String, Object>();
 		options.put(Library.OPTION_STRUCTURE_ALIGNMENT, Structure.ALIGN_NONE);
 		jnaLib = (PascoJNALibrary) Native.loadLibrary(nativeLibPath, 
 				PascoJNALibrary.class, options);
 	}
 
 	public void init()
 	{
 		// TODO see if this returns negative or some form of error message
 		// BUG currently native library doesn't handle PasDelete correctly so only the first init should
 		// be called.
 		if(handle == 0){
 			handle = jnaLib.PasInit();
 		}
 	}
 	
     public void start()
     {
 		// TODO see if this returns negative or some form of error message
     	int ret = jnaLib.PasStart(handle);
     }
 
     public void stop()
     {
 		// TODO see if this returns negative or some form of error message
     	int ret = jnaLib.PasStop(handle);
     }
     
     public void delete()
     {
 		// TODO see if this returns negative or some form of error message
     	int ret = jnaLib.PasDelete(handle);
     }
     
 	public PascoDevice [] getDevices() throws PascoException
 	{
 		// the 100 should be way more than would ever be attached
 		int [] devHandles = new int[100];
 		int count = jnaLib.PasGetDevices(handle, devHandles, 100);
 		if(count < 0){
 			throw new PascoException("No devices found.  Error code: " + count);
 		}
 		
 		PascoDevice [] devices = new PascoDevice[count];
 		for (int i=0; i < count; i++) {
 			devices[i] = new PascoDevice(devHandles[i], this);
 		}
 		
 		return devices;
 	}
     
     
     static File getNativeLibraryFromJar() throws IOException, InterruptedException {
     	if(Platform.isWindows()){
             File directory = createTmpDirectory();
             extractResource("QtCore4.dll", directory);
             return extractResource("pasco_api.dll", directory);
     	} else if (Platform.isMac()){
     		File directory = createTmpDirectory();
     		extractResource("libqtcore4.dylib", directory);
     		return extractResource("pasco-sdk.dylib", directory);
     	} else {
     		return null;
     	}
     }
     
 	static File extractResource(String resourceName, File directory)
 	throws Error, FileNotFoundException {
 		URL url = PascoLibrary.class.getResource(getNativeLibraryResourcePath() + "/" + resourceName);
 
 		if (url == null) {
 			throw new FileNotFoundException(resourceName + " not found in resource path");
 		}
 
 		File resourceFile = null;
 		if (url.getProtocol().toLowerCase().equals("file")) {
 			// NOTE: use older API for 1.3 compatibility
 			resourceFile = new File(URLDecoder.decode(url.getPath()));
 		}
 		else {
			InputStream is = PascoLibrary.class.getResourceAsStream(resourceName);
 			if (is == null) {
				throw new Error("Can't obtain resource InputStream, resource: " + resourceName);
 			}
 
 			FileOutputStream fos = null;
 			try {
 				String fileName = resourceName.substring(resourceName.lastIndexOf('/')+1);
 				resourceFile = new File(directory, fileName);
 				resourceFile.deleteOnExit();
 				if (Platform.deleteNativeLibraryAfterVMExit()) {
 					Runtime.getRuntime().addShutdownHook(new DeleteNativeLibrary(resourceFile));
 				}
 				fos = new FileOutputStream(resourceFile);
 				int count;
 				byte[] buf = new byte[1024];
 				while ((count = is.read(buf, 0, buf.length)) > 0) {
 					fos.write(buf, 0, count);
 				}
 			}
 			catch(IOException e) {
 				throw new Error("Failed to create temporary file: " + e);
 			}
 			finally {
 				try { is.close(); } catch(IOException e) { }
 				if (fos != null) {
 					try { fos.close(); } catch(IOException e) { }
 				}
 			}
 		}
 		return resourceFile;
 	}
 
     private static String getNativeLibraryResourcePath() {
         String arch = System.getProperty("os.arch").toLowerCase();
         String osPrefix;
         if (Platform.isWindows()) {
             osPrefix = "win32_" + arch;
         }
         else if (Platform.isMac()) {
             osPrefix = "darwin";
         }
         else if (Platform.isLinux()) {
             if ("x86".equals(arch)) {
                 arch = "i386";
             }
             else if ("x86_64".equals(arch)) {
                 arch = "amd64";
             }
             osPrefix = "linux_" + arch;
         }
         else if (Platform.isSolaris()) {
             osPrefix = "sunos_" + arch;
         }
         else {
             osPrefix = System.getProperty("os.name").toLowerCase();
             int space = osPrefix.indexOf(" ");
             if (space != -1) {
                 osPrefix = osPrefix.substring(0, space);
             }
             osPrefix += "-" + arch;
         }
         return "/org/concord/sensor/pasco/jna/" + osPrefix;
     }
 
     static File createTmpDirectory() throws IOException {
 		File directory = File.createTempFile("jna", "");
 		directory.delete();
 		directory.mkdir();
 		return directory;
     }
 
 	PascoJNALibrary getJNALibrary() {
 		return jnaLib;
 	}
 
 	int getHandle() {
 		return handle;
 	}
 }
