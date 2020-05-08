 /* Copyright 2007 kallasoft
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.kallasoft.smugmug.api.util;
 
 import java.io.InputStream;
 
import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.kallasoft.smugmug.api.json.Method;
 
 /**
  * Class used to provide generally useful methods to the API regardless of
  * version.
  * 
  * @author Riyad Kalla
  */
 public class APIUtils {
 	private static final Logger logger = LoggerFactory
 			.getLogger(APIUtils.class);
 
 	/**
 	 * Convenience method used to test if a <code>String</code> is empty. A
 	 * <code>String</code> is considered empty if it is <code>null</code>,
 	 * of length 0, or once trimmed is of length 0.
 	 * <p>
 	 * This implementation is provided to remove the dependency on the Commons
 	 * Lang library.
 	 * 
 	 * @param text
 	 *            The <code>String</code> that will be tested to see if it's
 	 *            empty.
 	 * 
 	 * @return <code>true</code> if <code>text</code> is <code>null</code>,
 	 *         of length 0 or trimmed then of length 0, otherwise returns
 	 *         <code>false</code>.
 	 */
 	public static boolean isEmpty(String text) {
 		return (text == null || text.length() == 0 || text.trim().length() == 0);
 	}
 
 	/**
 	 * Convenience method used by method implementations to convert optional
 	 * values to a <code>String</code> while returning a <code>null</code>
 	 * value when the value passed in is <code>null</code> as opposed to
 	 * throwing a NPE.
 	 * <p>
 	 * This makes writing the wrapper/convenience methods that eventually make a
 	 * call to {@link Method#execute(String, String[])} easier to implement.
 	 * 
 	 * @param object
 	 *            The object that needs to be converted to a <code>String</code>.
 	 * 
 	 * @return the <code>String</code> representation of the passed in
 	 *         <code>Object</code> (as represented by
 	 *         <code>object#toString()</code>) or <code>null</code> if the
 	 *         argument was <code>null</code>.
 	 */
 	public static String toString(Object object) {
 		return (object == null ? null : object.toString());
 	}
 
 	/**
 	 * Convenience method, similar to {@link #toString(Object)} but customized
 	 * for converting <code>Boolean</code> values to <code>0</code> or
 	 * <code>1</code> for <code>false</code> or <code>true</code>
 	 * respectively. SmugMug's API only accepts <code>0</code> or
 	 * <code>1</code> for boolean values up until API version 1.2.2, when
 	 * true/false were allowed.
 	 * <p>
 	 * However in order to maintain compatability across 1.2.0/1.2.1 and beyond,
 	 * this conversion method is still employed to normalize all the values.
 	 * 
 	 * @param bool
 	 *            The <code>Boolean</code> whose value will be converted to
 	 *            <code>0</code> or <code>1</code>.
 	 * 
 	 * @return if <code>bool</code> is <code>null</code>, returns
 	 *         <code>null</code> otherwise if <code>bool</code> is
 	 *         <code>false</code> returns <code>0</code> and if
 	 *         <code>bool</code> is <code>true</code> returns <code>1</code>.
 	 */
 	public static String toString(Boolean bool) {
 		return (bool == null ? null : (bool.booleanValue() ? "1" : "0"));
 	}
 
 	/**
 	 * Used to read the data from the given stream into a <code>byte[]</code>.
 	 * <p>
 	 * This method does not close the given stream when reading is done; the
 	 * caller should do that.
 	 * 
 	 * @param inputStream
 	 *            The stream whose bytes will be returned as an array.
 	 * 
 	 * @return a byte array that contains the data from the given stream.
 	 * 
 	 * @throws RuntimeException
 	 *             if an error occurs while trying to read the bytes from the
 	 *             stream (e.g. stream is already closed, no bytes, etc.).
 	 */
 	public static byte[] readStream(InputStream inputStream)
 			throws RuntimeException {
 		byte[] data = null;
 		logger.debug("Attempting to read in image data from given stream...");
 
 		try {
 			data = IOUtils.toByteArray(inputStream);
 			logger.debug("Successfully loaded stream data, length: {}", Integer
 					.toString(data.length));
 		} catch (Exception e) {
 			RuntimeException re = new RuntimeException(e);
 			logger
 					.error(
 							"An error occured while trying to load the stream data",
 							re);
 			throw re;
 		}
 
 		return data;
 	}
 
 	/**
 	 * Used to Base64-encode a byte array.
 	 * 
 	 * @param data
 	 *            An array of bytes that will be Base64-encoded.
 	 * 
 	 * @return a <code>String</code> that represents the result of
 	 *         Base64-encoding the original array.
 	 * 
 	 * @throws RuntimeException
 	 *             if an error occurs while trying to Base64-encode the byte
 	 *             array.
 	 */
 	public static String base64Encode(byte[] data) throws RuntimeException {
 		String encodedData = null;
 		logger.debug("Attempting to Base-64 encode the given image data...");
 
 		try {
			encodedData = Base64.encodeBase64String(data);
 			logger.debug(
 					"Successfully Base64-encoded the byte array, length: {}",
 					Integer.toString(encodedData.length()));
 		} catch (Exception e) {
 			RuntimeException re = new RuntimeException(e);
 			logger.error("Unable to Base64 encode the byte array", e);
 			throw re;
 		}
 
 		return encodedData;
 	}
 
 	/**
 	 * Used to calculate the MD5 Sum for the given array of bytes.
 	 * 
 	 * @param data
 	 *            The byte array who's MD5 Sum will be calculated.
 	 * 
 	 * @return a <code>String</code> representing the MD5 Sum for the contents
 	 *         of the given byte array.
 	 * 
 	 * @throws RuntimeException
 	 *             if an error occurs while trying to calculate the MD5 Sum for
 	 *             the byte array.
 	 */
 	public static String calculateMD5Sum(byte[] data) throws RuntimeException {
 		String md5Sum = null;
 		logger
 				.debug("Attempting to calculate an MD5 Sum for the given image data...");
 
 		try {
 			md5Sum = DigestUtils.md5Hex(data);
 			logger.debug("Successfully calculated the image MD5 Sum: {}",
 					md5Sum);
 		} catch (Exception e) {
 			RuntimeException re = new RuntimeException(e);
 			logger
 					.warn(
 							"An error occured while trying to calculate the MD5 Sum for the image data loaded from the given inputStream. This argument is optional to SmugMug so execution will continue.",
 							re);
 			throw re;
 		}
 
 		return md5Sum;
 	}
 }
