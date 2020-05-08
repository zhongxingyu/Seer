 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.io;
 
 import us.exultant.ahs.util.*;
 import java.io.*;
 import java.net.*;
 import java.nio.charset.*;
 import java.util.*;
 
 /**
  * <p>
  * IOForge largely just contains ghastly huge batches of convenience methods making
  * facades around java.io &mdash; getting rid of the dead-simple boilerplate code that
  * otherwise often shows up in numerous places in any sizable project. In particular,
  * these methods are oriented around the following axioms:
  * <ul>
  * <li>Fuctions don't do anything piecemeal, they just give you the answer you wanted and
  * don't make you worry about it.
  * <li>For local filesystem operations.
 * <li>Zero threading, nonblocking, or anything interesting &mdash just dead-simple stuff.
  * <li>Includes all the little edge cases that often throw a novice coder, such as closing
  * streams even if their creation threw exceptions (which if neglected can deplete the
  * range of file descriptors the OS is willing to allocate), etc.
  * <li>Charset always defaults to UTF-8 (instead of the "platform default").
  * <li>Operations are always sensibly buffered.
  * </ul>
  * </p>
  * 
  * <p>
  * If you're looking for higher-powered APIs that do network stuff or high-performance
  * nonblocking operations, you'll want to look elsewhere in the IO package. These
  * functions are mostly just intended to be great for rapid application development or use
  * by Java novices.
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public class IOForge {
 	/** This OutputStream is simply an effective /dev/null in portable java. */
 	public static final OutputStream silentOutputStream = new OutputStreamDiscard();
 	/** This PrintStream is simply an effective /dev/null in portable java. */
 	public static final PrintStream silentPrintStream = new PrintStream(new OutputStreamDiscard());
 	
 	
 	/** Read an entire file into an array as raw bytes. */
 	public static byte[] readFileRaw(File $file) throws FileNotFoundException, IOException {
 		return readRaw(new FileInputStream($file));
 	}
 	/** Read an entire file into an array as raw bytes. */
 	public static byte[] readFileRaw(String $filename) throws FileNotFoundException, IOException {
 		return readRaw(new FileInputStream($filename));
 	}
 	/** Read an entire file into an array as raw bytes. */
 	public static byte[] readResourceRaw(String $resource) throws FileNotFoundException, IOException {
 		return readRaw(getResourceAsStream($resource));
 	}
 	/** Read an entire file into a UTF-8 string. */
 	public static String readFileAsString(String $filename) throws FileNotFoundException, IOException {
 		return readString(new FileInputStream($filename));
 	}
 	/** Read an entire file into a string. */
 	public static String readFileAsString(String $filename, Charset $cs) throws FileNotFoundException, IOException {
 		return readString(new FileInputStream($filename), $cs);
 	}
 	/** Read an entire file into a UTF-8 string. */
 	public static String readFileAsString(File $file) throws FileNotFoundException, IOException {
 		return readString(new FileInputStream($file));
 	}
 	/** Read an entire file into a string. */
 	public static String readFileAsString(File $file, Charset $cs) throws FileNotFoundException, IOException {
 		return readString(new FileInputStream($file), $cs);
 	}
 	/** Read an entire file into a UTF-8 string. */
 	public static String readResourceAsString(String $resource) throws FileNotFoundException, IOException {
 		return readString(getResourceAsStream($resource));
 	}
 	/** Read an entire file into a string. */
 	public static String readResourceAsString(String $resource, Charset $cs) throws FileNotFoundException, IOException {
 		return readString(getResourceAsStream($resource), $cs);
 	}
 	/** Read an entire file into an array of UTF-8 strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.) */
 	public static String[] readFileAsStringLines(String $filename) throws FileNotFoundException, IOException {
 		return readStringLines(new FileInputStream($filename));
 	}
 	/** Read an entire file into an array of strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.) */
 	public static String[] readFileAsStringLines(String $filename, Charset $cs) throws FileNotFoundException, IOException {
 		return readStringLines(new FileInputStream($filename), $cs);
 	}
 	/** Read an entire file into an array of UTF-8 strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.) */
 	public static String[] readFileAsStringLines(File $file) throws FileNotFoundException, IOException {
 		return readStringLines(new FileInputStream($file));
 	}
 	/** Read an entire file into an array of strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.) */
 	public static String[] readFileAsStringLines(File $file, Charset $cs) throws FileNotFoundException, IOException {
 		return readStringLines(new FileInputStream($file), $cs);
 	}
 	/** Read an entire file into an array of UTF-8 strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.) */
 	public static String[] readResourceAsStringLines(String $resource) throws FileNotFoundException, IOException {
 		return readStringLines(getResourceAsStream($resource));
 	}
 	/** Read an entire file into an array of strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.) */
 	public static String[] readResourceAsStringLines(String $resource, Charset $cs) throws FileNotFoundException, IOException {
 		return readStringLines(getResourceAsStream($resource), $cs);
 	}
 	
 	
 	
 	/** Read an input stream into an array as raw bytes.  Closes the input stream when done, even if IOException. */
 	public static byte[] readRaw(InputStream $ins) throws IOException {
 		try {
 			byte[] $buf = new byte[2048];
 			int $k;
 			ByteArrayOutputStream $bs = new ByteArrayOutputStream();
 			while (($k = $ins.read($buf)) != -1) {
 				$bs.write($buf, 0, $k);
 			}
 			return $bs.toByteArray();
 		} finally {
 			$ins.close();
 		}
 	}
 	
	/** Read an entire input stream into an array of UTF-8 strings. Closes the input stream when done, even if IOException. */
 	public static String readString(InputStream $ins) throws IOException {
 		return readString($ins, Strings.UTF_8);
 	}
 	
	/** Read an entire input stream into an array of strings.  Closes the input stream when done, even if IOException. */
 	public static String readString(InputStream $ins, Charset $cs) throws IOException {
 		try {
 			char[] $buf = new char[2048];
 			int $k;
 			StringBuffer $sb = new StringBuffer();
 			InputStreamReader $isr = new InputStreamReader($ins, $cs);
 			while (($k = $isr.read($buf)) != -1) {
 				$sb.append($buf, 0, $k);
 			}
 			return $sb.toString();
 		} finally {
 			$ins.close();
 		}
 	}
 	
 	/** Read an entire file into an array of UTF-8 strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.)  Closes the input stream when done, even if IOException. */
 	public static String[] readStringLines(InputStream $ins) throws IOException {
 		return readStringLines($ins, Strings.UTF_8);
 	}
 	
 	/** Read an entire file into an array of strings, one array entry for each line in the file.  (A 'line' is defined exactly as per {@link BufferedReader#readLine()}.)  Closes the input stream when done, even if IOException. */
 	public static String[] readStringLines(InputStream $ins, Charset $cs) throws IOException {
 		try {
 			List<String> $lines = new ArrayList<String>();
 			BufferedReader $br = new BufferedReader(new InputStreamReader($ins, $cs));
 			for (String $next = $br.readLine(); $next != null; $next = $br.readLine())
 				$lines.add($next);
 			return $lines.toArray(Primitives.EMPTY_STRING);
 		} finally {
 			$ins.close();
 		}
 	}
 	
 	/** Returns an InputStream for the named resource (this is shorthand for accessing {@link ClassLoader#getResource(String)}).  If possible, the system classloader is used to resolve the resource; otherwise the classloader for the IOForge class is used. */
 	public static InputStream getResourceAsStream(String $resource) throws FileNotFoundException {
 		InputStream $ins = CL.getResourceAsStream($resource);
 		if ($ins == null) throw new FileNotFoundException();
 		return $ins;
 	}
 	
 	private static final ClassLoader CL;
 	static {
 		ClassLoader $cl;
 		try {
 			$cl = ClassLoader.getSystemClassLoader();
 		} catch (java.security.AccessControlException $e) {
 			$cl = null;
 		}
 		CL = ($cl == null) ? IOForge.class.getClassLoader() : $cl;
 	};
 	
 	/**
 	 * <p>
 	 * Convenience method for times when you don't care about code quality and just
 	 * want to make a damn PrintStream in a chained constructor call or when declaring
 	 * a static final variable without making seven lines of static initializer just
 	 * to catch an exception that you're going to throw up your hands in despair and
 	 * crash on anyway.
 	 * </p>
 	 * 
 	 * <p>
 	 * In other words, this throws an Error if it can't give you the stream.
 	 * </p>
 	 * 
 	 * @throws Error if FileNotFoundException.
 	 */
 	public static PrintStream makePrintStreamNoGuff(File $file) {
 		try {
 			return new PrintStream($file);
 		} catch (FileNotFoundException $e) {
 			throw new Error($e);
 		}
 	}
 	
 	/** Write an array of raw bytes to a file. */
 	public static void saveFile(byte[] $bah, File $dest) throws IOException {
 		writeFile($bah, $dest, false);
 	}
 	/** Write a string to a file in UTF-8 encoding. */
 	public static void saveFile(String $bah, File $dest) throws IOException {
 		writeFile($bah, $dest, false);
 	}
 	/** Write a string to a file. */
 	public static void saveFile(String $bah, Charset $cs, File $dest) throws IOException {
 		writeFile($bah, $cs, $dest, false);
 	}
 	/** Append an array of raw bytes to a file. */
 	public static void appendFile(byte[] $bah, File $dest) throws IOException {
 		writeFile($bah, $dest, true);
 	}
 	/** Append a string to a file in UTF-8 encoding. */
 	public static void appendFile(String $bah, File $dest) throws IOException {
 		writeFile($bah, $dest, true);
 	}
 	/** Append a string to a file. */
 	public static void appendFile(String $bah, Charset $cs, File $dest) throws IOException {
 		writeFile($bah, $cs, $dest, true);
 	}
 	private static void writeFile(byte[] $bah, File $dest, boolean $append) throws IOException {
 		OutputStream $os = null;
 		try {
 			$os = new BufferedOutputStream(new FileOutputStream($dest, $append));
 			$os.write($bah);
 		} finally {
 			if ($os != null) $os.close();
 		}
 	}
 	private static void writeFile(String $bah, File $dest, boolean $append) throws IOException {
 		writeFile($bah, Strings.UTF_8, $dest, $append);
 		
 	}
 	private static void writeFile(String $bah, Charset $cs, File $dest, boolean $append) throws IOException {
 		OutputStreamWriter $os = null;
 		try {
 			$os = new OutputStreamWriter(new FileOutputStream($dest, $append), $cs);
 			$os.write($bah);
 		} finally {
 			if ($os != null) $os.close();
 		}
 	}
 	
 	/**
 	 * <p>
 	 * Simple method to save information from http to the local filesystem.
 	 * </p>
 	 * 
 	 * <p>
 	 * There are almost always smarter ways to go about this, but if you're willing to
 	 * pay the price throwing a whole thread at doing nothing but wait for this for
 	 * the sake of simplicity and rapid application development, then this will do
 	 * just fine.
 	 * <p>
 	 * 
 	 * @param $request
 	 *                URL pointing to the http path to save.
 	 * @param $dest
 	 *                Local file to store to.
 	 * @throws IOException
 	 */
 	public static void saveFile(URL $request, File $dest) throws IOException {
 		InputStream $in = null;
 		OutputStream $out = null;
 		try {
 			URLConnection $conn = $request.openConnection();
 			$conn.connect();
 			$in = $conn.getInputStream();
 			if ($conn instanceof HttpURLConnection) {
 				int code = ((HttpURLConnection) $conn).getResponseCode();
 				if (code / 100 != 2) throw new IOException("Could not connect to '" + $request + "' via HTTP; gave error code " + code + ".");
 			}
 			
 			$in = new BufferedInputStream($in);
 			$out = new BufferedOutputStream(new FileOutputStream($dest));
 			byte[] $buf = new byte[2048];
 			int $k;
 			int $p = 0;
 			while (($k = $in.read($buf)) != -1) {
 				$out.write($buf,0,$k);
 				$p += $k;
 			}
 		} finally {
 			try {
 				if ($in != null) $in.close();
 			} finally {
 				if ($out != null) $out.close();
 			}
 		}
 	}
 }
