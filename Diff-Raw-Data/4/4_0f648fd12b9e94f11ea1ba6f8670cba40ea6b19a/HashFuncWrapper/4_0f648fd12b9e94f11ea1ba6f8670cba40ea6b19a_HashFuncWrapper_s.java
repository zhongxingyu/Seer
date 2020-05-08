 package de.tr0llhoehle.buschtrommel;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Formatter;
 
 /**
  * This class wraps the hash function that is used in buschtrommel.
  * @author Tobias Sturm
  *
  */
 public class HashFuncWrapper {
 
 	private static final String HASH_FUNCTION = "SHA-1";
 
 	/**
 	 * Checks if the hash-provider is available on this machine.
 	 * 
 	 * @return true if available.
 	 */
 	public static boolean check() {
 		try { // forgiveme for my sins!
 			MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
 			return true;
 		} catch (NoSuchAlgorithmException ex) {
 			return false;
 		}
 	}
 
 	/**
 	 * Creates the uppercase hash string for the given byte-array.
 	 * 
 	 * @param d
 	 *            byte array
 	 * @return uppercase hash or "" if hash provider is not available on this machine. Use
 	 *         check-method before!
 	 */
 	public static String hash(byte[] d) {
 		try {
 			MessageDigest md = MessageDigest.getInstance(HASH_FUNCTION);
 			return byteArray2Hex(md.digest(d));
 		} catch (NoSuchAlgorithmException ex) {
 			LoggerWrapper.logError(HASH_FUNCTION + "is not available on this machine!");
 			throw new IllegalStateException(HASH_FUNCTION + "is not available on this machine!");
 		}
 		
 	}
 
 	/**
 	 * Generates the hash of the content of a file. 
 	 * @param path path to the file
 	 * @return the hash as uppercase string
 	 * @throws IOException 
 	 */
 	public static String hash(String path) throws IOException {
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance(HASH_FUNCTION);
 			FileInputStream fis = new FileInputStream(path);
 			byte[] dataBytes = new byte[1024];
 
 			int nread = 0;
 
 			while ((nread = fis.read(dataBytes)) != -1) {
 				md.update(dataBytes, 0, nread);
 			};
 
 			return byteArray2Hex(md.digest());
 		} catch (NoSuchAlgorithmException e) {
 			LoggerWrapper.logError(HASH_FUNCTION + "is not available on this machine!");
 			throw new IllegalStateException(HASH_FUNCTION + "is not available on this machine!");
 		}
 		
 	}
 
 	private static String byteArray2Hex(final byte[] hash) {
 		Formatter formatter = new Formatter();
 		for (byte b : hash) {
 			formatter.format("%02x", b);
 		}
 		formatter.close();
		return formatter.toString().toUpperCase();
 	}
 }
