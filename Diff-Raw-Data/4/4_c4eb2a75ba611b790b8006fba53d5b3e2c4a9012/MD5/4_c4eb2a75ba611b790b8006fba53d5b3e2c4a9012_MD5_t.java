 package com.dgrid.util;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class MD5 {
 	private static final char kHexChars[] = { '0', '1', '2', '3', '4', '5',
 			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 
 	public static String md5(String text) {
 		return md5(text.getBytes());
 	}
 
 	public static String md5(byte[] bytes) {
 		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(bytes);
 			return MD5.bufferToHex(md.digest());
 		} catch (NoSuchAlgorithmException e) {
 			throw (new RuntimeException(e));
 		}
 	}
 
 	private static String bufferToHex(byte buffer[]) {
 		return MD5.bufferToHex(buffer, 0, buffer.length);
 	}
 
 	private static String bufferToHex(byte buffer[], int startOffset, int length) {
 		StringBuffer hexString = new StringBuffer(2 * length);
 		int endOffset = startOffset + length;
 
 		for (int i = startOffset; i < endOffset; i++)
 			MD5.appendHexPair(buffer[i], hexString);
 
 		return hexString.toString();
 	}
 
 	private static void appendHexPair(byte b, StringBuffer hexString) {
 		char highNibble = kHexChars[(b & 0xF0) >> 4];
 		char lowNibble = kHexChars[b & 0x0F];
 
 		hexString.append(highNibble);
 		hexString.append(lowNibble);
 	}
 }
