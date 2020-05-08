 /*
  * Copyright � 2011-2012 Brian Groenke
  * All rights reserved.
  * 
  *  This file is part of the 2DX Graphics Library.
  *
  *  2DX is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  2DX is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * 2DX home package containing generic utility classes and base API elements.
  */
 package bg.x2d;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import bg.x2d.utils.*;
 
 /**
  * A convenience class consisting entirely of static methods regarding system
  * information. Many of these methods simply mirror the
  * <code>System.getProperty</code> method in Java's standard package.
  * 
  * This class also deals with how 2DX loads and handles native libraries.  By default, 2DX loads
  * native code from the /lib directory in the classpath.  Inside of this driectory, each platform specific
  * directory must exist in order to be counted as supported.  Any platforms which do not have an existing directory
  * within the native library location will not be listed as supported by the 2DX-GL software distribution.
  * 
  * The native library location shouldn't change, but a setter method is provided in case it must be changed programmatically.
  * Changes to the default library location should be made to the default value of the 'nativeLib' member of this class.
  * 
  * @since 2DX 1.0 (1st Edition)
  */
 
 public abstract class Local {
 
 	public static final String 
 	/**
 	 * Field value="x64"
 	 */
 	JVM_X64 = "x64", 
 	/**
 	 * Field value="x86"
 	 */
 	JVM_X86 = "x86",
 	NATIVE_WIN32 = "/win32", NATIVE_WIN64 = "/win64", NATIVE_LINUX32 = "/linux32", NATIVE_LINUX64 = "/linux64",
 	NATIVE_BSD32 = "/freebsd32", NATIVE_BSD64 = "/freebsd64", NATIVE_MAC32 = "/mac32", NATIVE_MAC64 = "/mac64";
 
 	private static String[] nativeSupported;
 
 	private static String nativesJar = "natives.jar", nativeLib = "x2d_lib", current;
 	private static URLClassLoader nativeLoader;
 
 	private static final String nativeJarTmp = System.getProperty("java.io.tmpdir") + File.separator + "x2d_tmp_" + nativesJar;
 
 	static {
 		checkNativeSupport();
 		loadNative("Local");
 	}
 
 	/**
 	 * Uses a simple algorithm to convert Calendar time into standard 24-hour
 	 * time format.
 	 * 
 	 * @return a four digit (usually) integer representing the current time in
 	 *         24-hour format.
 	 */
 	public static int hourlyTime() {
 		Calendar cal = Calendar.getInstance();
 		int hour = cal.get(Calendar.HOUR_OF_DAY) * 100;
 		int minutes = cal.get(Calendar.MINUTE);
 		int time = hour + minutes;
 		return time;
 	}
 
 	@Deprecated
 	/**
 	 * This method has little if any significant benefit over the java.lang.System class method currentTimeMillis()
 	 */
 	public static long timeInMillis() {
 		long millis = System.currentTimeMillis();
 		return millis;
 	}
 
 	public static String getPlatform() {
 		return System.getProperty("os.name");
 	}
 
 	public static String getJavaVersion() {
 		return System.getProperty("java.version");
 	}
 
 	public static String getUserHome() {
 		return System.getProperty("user.home");
 	}
 
 	public static String getWorkingDir() {
 		return System.getProperty("user.dir");
 	}
 
 	public static String getJVM() {
 		return System.getProperty("java.vm.name");
 	}
 
 	public static String getOSArch() {
 		return System.getProperty("os.arch");
 	}
 
 	/**
 	 * Returns the architecture of the current VM (32 or 64 bit)
 	 * @return a String value representing the architecture: JVM_X64 constant if 64-bit, JVM_X86 constant otherwise.
 	 */
 	public static String getJavaArch() {
 		return (getJVM().toLowerCase().contains("64-bit")) ? JVM_X64:JVM_X86;
 	}
 
 	/**
 	 * Attempts to determine the human-readable identifier of the graphics device installed on this machine.
	 * Usually this method returns the name of either the video hardware or software depending on what information
	 * the underlying system provides.  On Windows, the returned value tends to be the former, on Linux, the latter.
	 * @return the human-readable name as a String or null if unable to be determined (i.e an error occurred or 2DX
	 * doesn't support native operations on this platform).
 	 */
 	public static native String getGraphicsDevice();
 
 	/**
 	 * Fetches the amount of RAM the system has available.  As opposed to the built in Java functions, this method
 	 * obtains the amount of free RAM system-wide, outside of the virtual machine.
 	 * @return free system RAM in bytes.
 	 */
 	public static native long getSystemAvailableRAM();
 
 	/**
 	 * Fetches the total amount of RAM installed on the system.  As opposed to the built in Java functions, this method
 	 * obtains the total amount of RAM installed on the hardware.  The accuracy of the result may vary depending on the
 	 * platform.
 	 * @return total system installed RAM in bytes.
 	 */
 	public static native long getSystemTotalRAM();
 
 	/**
 	 * 
 	 * @return the file suffix for shared libraries on the native platform (no '.') or null if unknown.
 	 */
 	public static String getNativeLibrarySuffix() {
 		String os = getPlatform().toLowerCase();
 		if(os.contains("windows"))
 			return "dll";
 		else if(os.contains("linux") || os.contains("bsd"))
 			return "so";
 		else if(os.contains("mac"))
 			return "jnilib";
 		else
 			return null;
 	}
 
 	/**
 	 * Sets the location from which native libraries should be loaded from.  This method
 	 * automatically calls checkNativeSupport after setting the new variable.
 	 * 
 	 * Two checks are performed before re-assigning the native library location:
 	 * <br/>
 	 * 1) The full JAR path points to a valid JAR file at the given location.
 	 * <br/>
 	 * 2) The URLClassLoader created with this JAR file recognizes the given package as a valid resource.
 	 * 
 	 * @param jar URL
 	 * @return true if successful, false otherwise
 	 */
 	public static boolean setNativeLibraryLocation(URL jar, String pkg) {
 		InputStream test = null;
 		try {
 			test = jar.openStream();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(test == null || !jar.toString().endsWith(".jar"))
 			return false;
 		Utils.closeStream(test);
 		URLClassLoader nloader = new URLClassLoader(new URL[]{jar}, ClassLoader.getSystemClassLoader());
 		if(nloader.getResource(pkg) == null)
 			return false;
 		
 		nativeLoader = nloader;
 		nativeLib = pkg;
 
 		checkNativeSupport();
 		return true;
 	}
 
 	public static boolean isNativeCodeSupported() {
 		return Arrays.binarySearch(nativeSupported, current) >= 0;
 	}
 
 	/**
 	 * Loads the specified library into the system.  Looks for the native library
 	 * in the /lib folder of the JVM classpath (/lib in JAR or whatever is set), writes it to a temporary
 	 * file location and loads it into the system.
 	 * @param native library name (no extension or path).
 	 * @return true if successful, false otherwise.
 	 */
 	public static boolean loadNative(String name) {
 		if(!isNativeCodeSupported())
 			return false;
 		name = name + '.' + getNativeLibrarySuffix();
 		try {
 			URL url = nativeLoader.getResource(nativeLib + current + "/" + name);
 			if(url == null)
 				throw(new UnsatisfiedLinkError("failed to locate native library"));
 			File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + name);
 			InputStream in = url.openStream();
 			FileOutputStream fos = new FileOutputStream(tmp);
 			byte[] buff = new byte[1024];
 			int len;
 			while((len=in.read(buff)) > 0)
 				fos.write(buff, 0, len);
 			fos.close();
 			System.load(tmp.getPath());
 			tmp.deleteOnExit();
 			return true;
 		} catch(UnsatisfiedLinkError ule) {
 			System.err.println("error loding library " + name + ": " + ule.getMessage());
 		} catch (IOException e) {
 			System.err.println("error loding library " + name + ": " + e.getMessage());
 		}
 
 		return false;
 	}
 
 	/**
 	 * Writes the natives.jar
 	 */
 	private static synchronized void checkNativeSupport() {
 
 		if(nativeLoader == null) {
 			if(ClassLoader.getSystemResource(nativesJar) == null) {
 				System.out.println("2DX: failed to locate natives.jar");
 				nativeSupported = new String[0];
 				return;
 			} else {
 				OutputStream out = null;
 				InputStream in = null;
 				boolean error = false;
 				try {
 					out = new FileOutputStream(nativeJarTmp);
 					in = ClassLoader.getSystemResourceAsStream(nativesJar);
 					byte[] bytes = new byte[8192];
 					int len = 0;
 					while((len=in.read(bytes, 0, bytes.length)) >= 0)
 						out.write(bytes, 0, len);
 				} catch (IOException e) {
 					error = true;
 				} finally {
 					Utils.closeStream(in);
 					Utils.closeStream(out);
 				}
 				if(error)
 					return;
 			}
 
 			File f = new File(nativeJarTmp);
 			f.deleteOnExit();
 			nativeLoader = new URLClassLoader(new URL[]{Utils.getFileURL(f)}, 
 					ClassLoader.getSystemClassLoader());
 		}
 
 		final int SYS_COUNT = 8; // this must be changed if platforms are added/removed.
 
 		HashSet<String> supported = new HashSet<String>();
 		for(int i = 0;i < SYS_COUNT;i++) {
 			String platform = null;
 			switch(i) {
 			case 0:
 				platform = NATIVE_WIN32;
 				if(getPlatform().toLowerCase().contains("windows") && getJavaArch().equals(JVM_X86))
 					current = platform;
 				break;
 			case 1:
 				platform = NATIVE_WIN64;
 				if(getPlatform().toLowerCase().contains("windows") && getJavaArch().equals(JVM_X64))
 					current = platform;
 				break;
 			case 2:
 				platform = NATIVE_LINUX32;
 				if(getPlatform().toLowerCase().contains("linux") && getJavaArch().equals(JVM_X86))
 					current = platform;
 				break;
 			case 3:
 				platform = NATIVE_LINUX64;
 				if(getPlatform().toLowerCase().contains("linux") && getJavaArch().equals(JVM_X64))
 					current = platform;
 				break;
 			case 4:
 				platform = NATIVE_BSD32;
 				if(getPlatform().toLowerCase().contains("bsd") && getJavaArch().equals(JVM_X86))
 					current = platform;
 				break;
 			case 5:
 				platform = NATIVE_BSD64;
 				if(getPlatform().toLowerCase().contains("bsd") && getJavaArch().equals(JVM_X64))
 					current = platform;
 				break;
 			case 6:
 				platform = NATIVE_MAC32;
 				if(getPlatform().toLowerCase().contains("mac") && getJavaArch().equals(JVM_X86))
 					current = platform;
 				break;
 			case 7:
 				platform = NATIVE_MAC64;
 				if(getPlatform().toLowerCase().contains("mac") && getJavaArch().equals(JVM_X64))
 					current = platform;
 			}
 
 			if(nativeLoader.getResource(nativeLib + platform  + "/") != null)
 				supported.add(platform);
 			nativeSupported = supported.toArray(new String[supported.size()]);
 		}
 
 	}
 }
