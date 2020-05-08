 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2010, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.InvalidClassException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OptionalDataException;
 import java.io.StreamCorruptedException;
 
 import android.util.Log;
 
 public final class IOUtils {
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
 	private static final String TAG = IOUtils.class.getSimpleName();
 	
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
 	/**
 	 * Converts an Object to a byte array.  Will not throw an exception if an error occurs, rather
 	 * it will return null.
 	 * 
 	 * @param obj
 	 * 		The object to be converted to byte array.
 	 * 
 	 * @return
 	 * 		The object as a byte array, null if the specified object was null.
 	 */
 	public static byte[] objectToBytes(Object obj) {
 		try {
 			return objectToBytes(obj, false);
 		} catch (IOException e) {
 			// will never happen because we're sending 'false', but need for compilation
 		}
         throw new RuntimeException("sanity failure");
 	}
 	
 	/**
 	 * Converts an Object to a byte array.
 	 * 
 	 * @param obj
 	 * 		The object to be converted to byte array.
 	 * 
 	 * @param shouldThrowOnError
 	 *		Flag indicating whether or not the user wants to handle exceptions that are thrown
 	 *		during this operation. 
 	 * 
 	 * @return
 	 * 		The object as a byte array, null if the specified object was null.
 	 *
 	 * @throws IOException
 	 * 		If the user passed <code>true</code> for 'shouldThrowOnError' and an IOException has
 	 * 		occurred.
 	 */
 	public static byte[] objectToBytes(Object obj, boolean shouldThrowOnError) throws IOException {
 		if (obj != null) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutputStream oos;
 			try {
 				oos = new ObjectOutputStream(baos);
 				oos.writeObject(obj);
 				oos.close();
 				return baos.toByteArray();
 			} catch (IOException e) {
 				Log.e(TAG, "[objectToBytes] IOException encountered", e);
 				if (shouldThrowOnError) {
 					throw e;
 				}
 			}
 		}
         //this is a good byte[] representation of null that won't cause the file writer to crash
 		return new byte[0];
 	}
 	
 	/**
 	 * Converts a byte array to an Object.  Will not throw an exception if an error occurs, rather
 	 * it will return null.
 	 * 
 	 * @param bytes
 	 * 		The byte array to be converted to Object form.
 	 * 
 	 * @return
 	 * 		The Object if successful, null if the specified object was null or the conversion
 	 * 		failed.
 	 */
 	public static Object bytesToObject(byte[] bytes) {
 		Object retval = null;
 		try {
 			retval = bytesToObject(bytes, false);
 		} catch (Exception ignore) {
 			// will never happen because we're sending 'false', but need for compilation
 		}
 		return retval;
 	}
 
 	/**
 	 * Converts a byte array to an Object.  Will not throw an exception if an error occurs, rather
 	 * it will return null.
 	 * 
 	 * @param bytes
 	 * 		The byte array to be converted to Object form.
 	 * 
 	 * @param shouldThrowOnError
 	 *		Flag indicating whether or not the user wants to handle exceptions that are thrown
 	 *		during this operation. 
 	 * 
 	 * @return
 	 * 		The Object if successful, null if the specified object was null.
 	 * 
 	 * @throws StreamCorruptedException
 	 * 		If the user passed <code>true</code> for 'shouldThrowOnError' and a
 	 * 		StreamCorruptedException has occurred.
 	 * 
 	 * @throws OptionalDataException
 	 * 		If the user passed <code>true</code> for 'shouldThrowOnError' and an 
 	 * 		OptionalDataException has occurred.
 	 * 
 	 * @throws IOException
 	 * 		If the user passed <code>true</code> for 'shouldThrowOnError' and an IOException has
 	 * 		occurred.
 	 * 
 	 * @throws ClassNotFoundException
 	 * 		If the user passed <code>true</code> for 'shouldThrowOnError' and a
 	 * 		ClassNotFoundException has occurred.
 	 */
 	public static Object bytesToObject(byte[] bytes, boolean shouldThrowOnError) throws 
 			StreamCorruptedException, OptionalDataException, 
 			IOException, ClassNotFoundException {
 		Object retval = null;
 		if (bytes != null) {
 			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
 			ObjectInputStream ois = null;
 			try {
 				ois = new ObjectInputStream(bais);
 				retval = ois.readObject();
 				ois.close();
			} catch (InvalidClassException e) {
				Log.e(TAG, "[bytesToObject] InvalidClassException encountered", e);
				if (shouldThrowOnError) throw e;
 			} catch (StreamCorruptedException e) {
 				Log.e(TAG, "[bytesToObject] StreamCorruptedException encountered", e);
 				if (shouldThrowOnError) throw e;
 			} catch (OptionalDataException e) {
 				Log.e(TAG, "[bytesToObject] OptionalDataException encountered", e);
 				if (shouldThrowOnError) throw e;
 			} catch (IOException e) {
 				Log.e(TAG, "[bytesToObject] IOException encountered", e);
 				if (shouldThrowOnError) throw e;
 			} catch (ClassNotFoundException e) {
 				Log.e(TAG, "[bytesToObject] ClassNotFoundException encountered", e);
 				if (shouldThrowOnError) throw e;
 			}
 		}
 		return retval;
 	}
 	
 	/**
 	 * Reads the entire contents of the specified stream to a byte array.
 	 * 
 	 * @param in
 	 * 		The input stream to read the contents of.
 	 * 
 	 * @return
 	 * 		A byte array representing the full contents of the stream, null if stream is null or
 	 * 		operation failed.
 	 */
 	public static byte[] readFromStream(InputStream in) {
 		byte[] retval = null;
 		try {
 			retval = readFromStream(in, false);
 		} catch (IOException ignore) {
 			// will never happen because we're sending 'false', but need for compilation
 		}
 		return retval;
 	}
 	
 	/**
 	 * Reads the entire contents of the specified stream to a byte array.
 	 * 
 	 * @param in
 	 * 		The input stream to read the contents of.
 	 * 
 	 * @param shouldThrowOnError
 	 *		Flag indicating whether or not the user wants to handle exceptions that are thrown
 	 *		during this operation. 
 	 * 
 	 * @return
 	 * 		A byte array representing the full contents of the stream, null if stream is null.
 	 * 
 	 * @throws IOException
 	 * 		If the user passed <code>true</code> for 'shouldThrowOnError' and an IOException has
 	 * 		occurred.
 	 */
 	public static byte[] readFromStream(InputStream in, boolean shouldThrowOnError) 
 			throws IOException {
         ///todo XXX audit this code.
 		byte[] retval = null;
 		if (in != null) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			try {
 				byte[] buffer = new byte[1024];
 				int len;
 				while ((len = in.read(buffer)) != -1) {
 					baos.write(buffer, 0, len);
 				}
 				retval = baos.toByteArray();
 			} catch (IOException e) {
 				Log.e(TAG, "[readFromStream] problem reading from input stream.", e);
 				if (shouldThrowOnError) throw e;
 			} finally {
 				if (baos != null) {
 					try {
 						baos.close();
 					} catch (IOException ignore) {
 					}
 				}
 			}
 		}
 		return retval;
 	}
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
 	/**
 	 * Private default constructor -- Utility class, no instance.
 	 */
 	private IOUtils() {
 		// no instance
 	}
 }
