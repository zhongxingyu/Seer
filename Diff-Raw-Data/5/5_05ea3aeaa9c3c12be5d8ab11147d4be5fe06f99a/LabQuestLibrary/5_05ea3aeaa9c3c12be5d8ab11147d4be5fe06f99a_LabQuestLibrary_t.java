 package org.concord.sensor.labquest.jna;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.sun.jna.FunctionMapper;
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 import com.sun.jna.Platform;
 import com.sun.jna.Pointer;
 import com.sun.jna.Structure;
 import com.sun.jna.Native.DeleteNativeLibrary;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.ShortByReference;
 
 public class LabQuestLibrary 
 {
 	private NGIOLibrary ngio;
 	private Pointer hLibrary;
 
 	public void init()
 	{
 		File nativeLibFile = getNativeLibraryFromJar();
 		String nativeLibPath = nativeLibFile.getAbsolutePath();
 		
 		FunctionMapper functMapper = new FunctionMapper(){
 
 			public String getFunctionName(NativeLibrary library, Method method) {
 				return "NGIO_" + method.getName().substring(0,1).toUpperCase() + 
 				method.getName().substring(1);
 			}
 			
 		};
 		
 		Map options = new HashMap();
 		options.put(Library.OPTION_FUNCTION_MAPPER, functMapper);
 		options.put(Library.OPTION_STRUCTURE_ALIGNMENT, Structure.ALIGN_NONE);
 		ngio = (NGIOLibrary) Native.loadLibrary(nativeLibPath, 
 				NGIOLibrary.class, options);
 		
 		hLibrary = ngio.init();
 
 		int lqDebugLevel = Integer.getInteger("labquest.debug.level", -1);
 		if(lqDebugLevel != -1){
 			ngio.diags_SetDebugTraceThreshold(hLibrary, lqDebugLevel);
 		}
 
 	}
 
 	public void cleanup()
 	{
 		System.err.println("LabQuestLibrary: cleaning up");
 		if(ngio == null){
 			System.err.println("  ngio null");
 			return;
 		}
 		
 		if(hLibrary == null){
 			System.err.println("  hLibrary null");
 			return;
 		}
 
 		int ret = ngio.uninit(hLibrary);
 		if(ret != 0){
 			System.err.println("  uninit failed");
 		}
 		hLibrary = null;
 		ngio = null;
 	}
 	
 	public short [] getDLLVersion() throws LabQuestException
 	{
 		ShortByReference majorVersion = new ShortByReference();
 		ShortByReference minorVersion = new ShortByReference();
 		int ret = ngio.getDLLVersion(hLibrary, majorVersion, minorVersion);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 		short [] version = new short[2];
 		version[0] = majorVersion.getValue();
 		version[1] = minorVersion.getValue();
 		
 		return version;
 	}
 
 	public void searchForDevices() throws LabQuestException 
 	{
 		IntByReference listSig = new IntByReference();
 		int ret = ngio.searchForDevices(hLibrary, NGIOLibrary.DEVTYPE_LABPRO2, 
 				NGIOLibrary.COMM_TRANSPORT_USB, null, listSig);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 	}
 	
 	public void printListOfDevices() 
 	{
 		IntByReference listSig = new IntByReference();
 		IntByReference numDevices = new IntByReference();
 		Pointer openDeviceListSnapshotHandle = ngio.openDeviceListSnapshot(hLibrary, 
 				NGIOLibrary.DEVTYPE_LABPRO2, 
 				numDevices, listSig);
		if(openDeviceListSnapshotHandle == null){
			System.err.println("got a null snapshot handle");
			return;
		}
		
 		System.out.println("labpro2 num devices: " + numDevices.getValue() + 
 				" list sig: " + listSig.getValue());
 	
 		byte [] devNameBuf = new byte[NGIOLibrary.MAX_SIZE_DEVICE_NAME];
 		IntByReference deviceStatusMask = new IntByReference();
 		for(int i=0; i<numDevices.getValue(); i++){
 			
 			ngio.deviceListSnapshot_GetNthEntry(openDeviceListSnapshotHandle, i, 
 					devNameBuf, devNameBuf.length, deviceStatusMask);
 			String devName = Native.toString(devNameBuf);
 			System.out.println("dev name: " + devName);
 		}
 		ngio.closeDeviceListSnapshot(openDeviceListSnapshotHandle);
 	}
 
 	public String getFirstDeviceName() throws LabQuestException 
 	{
 		searchForDevices();
 		IntByReference listSig = new IntByReference();
 		IntByReference numDevices = new IntByReference();
 		Pointer openDeviceListSnapshotHandle = 
 			ngio.openDeviceListSnapshot(hLibrary, NGIOLibrary.DEVTYPE_LABPRO2, 
 				numDevices, listSig);
 		
 		int num = numDevices.getValue();
 		if(num <= 0){
 			return null;
 		}
 		
 		byte [] devNameBuf = new byte[NGIOLibrary.MAX_SIZE_DEVICE_NAME];
 		IntByReference deviceStatusMask = new IntByReference();
 		int ret = ngio.deviceListSnapshot_GetNthEntry(openDeviceListSnapshotHandle, 0, 
 				devNameBuf, devNameBuf.length, deviceStatusMask);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 		
 		ret = ngio.closeDeviceListSnapshot(openDeviceListSnapshotHandle);
 		if(ret != 0){
 			throw new LabQuestException();
 		}
 
 		return Native.toString(devNameBuf);
 	}
 
 	public LabQuest openDevice(String deviceName) throws LabQuestException
 	{
 		LabQuest labQuest = new LabQuestImpl(ngio);
 
 		SingleThreadDelegator<LabQuest> singleThreadDelegator = 
 			new SingleThreadDelegator<LabQuest>();
 		singleThreadDelegator.setDaemon(true);
 		singleThreadDelegator.start();
 		
 		Method closeMethod = null;
 		try {
 			closeMethod = LabQuest.class.getMethod("close");
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		LabQuest labQuest2 = singleThreadDelegator.instanciate(labQuest, LabQuest.class, 
 				LabQuestException.class, closeMethod);
 
 		labQuest2.open(deviceName, hLibrary);
 		
 		return labQuest2;		
 	}
 	
     private static File getNativeLibraryFromJar() {
         String libname = getNativeLibraryName();
         String resourceName = getNativeLibraryResourcePath() + "/" + libname;
         URL url = LabQuestLibrary.class.getResource(resourceName);
                 
         if (url == null) {
             throw new UnsatisfiedLinkError("NGIO (" + resourceName 
                                            + ") not found in resource path");
         }
     
         File lib = null;
         if (url.getProtocol().toLowerCase().equals("file")) {
             // NOTE: use older API for 1.3 compatibility
             lib = new File(URLDecoder.decode(url.getPath()));
         }
         else {
             InputStream is = Native.class.getResourceAsStream(resourceName);
             if (is == null) {
                 throw new Error("Can't obtain jnidispatch InputStream");
             }
             
             FileOutputStream fos = null;
             try {
                 // Suffix is required on windows, or library fails to load
                 // Let Java pick the suffix
                 lib = File.createTempFile("jna", null);
                 lib.deleteOnExit();
                 if (Platform.deleteNativeLibraryAfterVMExit()) {
                     Runtime.getRuntime().addShutdownHook(new DeleteNativeLibrary(lib));
                 }
                 fos = new FileOutputStream(lib);
                 int count;
                 byte[] buf = new byte[1024];
                 while ((count = is.read(buf, 0, buf.length)) > 0) {
                     fos.write(buf, 0, count);
                 }
             }
             catch(IOException e) {
                 throw new Error("Failed to create temporary file for jnidispatch library: " + e);
             }
             finally {
                 try { is.close(); } catch(IOException e) { }
                 if (fos != null) {
                     try { fos.close(); } catch(IOException e) { }
                 }
             }
         }
         return lib;
     }
 
     private static String getNativeLibraryName() {
         if (Platform.isWindows()) {
         	return "NGIO_lib.dll";
         }
         else if (Platform.isMac()) {
         	return "libNGIO.dylib";
         }
         return null;
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
         return "/org/concord/sensor/labquest/jna/" + osPrefix;
     }
 
 }
