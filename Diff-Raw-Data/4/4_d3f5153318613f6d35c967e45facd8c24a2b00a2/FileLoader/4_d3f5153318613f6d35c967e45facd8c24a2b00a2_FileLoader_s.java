 package com.ijg.darklight.sdk.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /*
  * Copyright (C) 2013  Isaac Grant
  * 
  * This file is part of the Darklight Nova Core.
  *  
  * Darklight Nova Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Darklight Nova Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the Darklight Nova Core.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Utilities for loading files, especially in tokenized form
  * @author Isaac Grant
  */
 public class FileLoader {
 	
 	/**
 	 * Loads a file as tokens using a scanner, tokens are converted to
 	 * byte arrays using the String.getBytes() method
 	 * @param file The file to load
 	 * @return An ArrayList of the loaded tokens, as byte arrays
 	 * @throws FileNotFoundException
 	 */
 	public static ArrayList<byte[]> tokenizedLoad(File file) throws FileNotFoundException {
 		ArrayList<byte[]> tokens = new ArrayList<byte[]>();
 		Scanner scanner = new Scanner(file);
 		
 		while (scanner.hasNext())
 			tokens.add(scanner.next().getBytes());
 		scanner.close();
 		
 		ArrayList<Integer> indexesToRemove = new ArrayList<Integer>();
 		for (int i = 0; i < tokens.size(); ++i) {
 			try {
 				byte[] fixedBytes = removeZeroBytesAndCompact(tokens.get(i));
 				tokens.set(i, fixedBytes);
 			} catch (InvalidParameterException e) {
 				indexesToRemove.add(i);
 			}
 		}
		for (int index : indexesToRemove) {
			tokens.remove(index);
 		}
 		
 		return tokens;
 	}
 	
 	/**
 	 * Load data from an InputStream and return as a String
 	 * @param is InputStream to retrieve data from
 	 * @return The loaded data compiled into a single string
 	 * @throws IOException
 	 */
 	public static String loadFromInputStream(InputStream is) throws IOException {
 		String data = "";
 		byte[] buffer = new byte[4096];
 		int length = buffer.length;
 		while ((length = is.read(buffer, 0, length)) > 0) {
 			data += new String(removeZeroBytesAndCompact(buffer));
 		}
 		return data;
 	}
 	
 	/**
 	 * Removes bytes from a byte array with an integer value of 0, and compacts
 	 * the array so there are no empty slots
 	 * @param bytes The byte array to work with
 	 * @return The byte array without any 0's, and compacted
 	 * @throws InvalidParameterException If the byte array only contains 0's
 	 */
 	public static byte[] removeZeroBytesAndCompact(byte[] bytes) throws InvalidParameterException {
 		byte[] compactBytes = null;
 		byte[] fixedBytes = new byte[bytes.length];
 		int index = 0;
 		for (int i = 0; i < bytes.length; ++i) {
 			if (bytes[i] != 0)
 				fixedBytes[index++] = bytes[i];
 		}
 		if (index > 0) {
 			compactBytes = new byte[index];
 			for (int i = 0; i < compactBytes.length; ++i)
 				compactBytes[i] = fixedBytes[i];
 		} else {
 			throw new InvalidParameterException("Byte array only contains zeroes");
 		}
 		
 		return compactBytes;
 	}
 }
