 /*
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  * 3. The names "Graz University of Technology" and "IAIK of Graz University of
  *    Technology" must not be used to endorse or promote products derived from
  *    this software without prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
  *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
  *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
  *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *  POSSIBILITY  OF SUCH DAMAGE.
  */
 
 package org.iaik.net.utils;
 
 import java.util.StringTokenizer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Contains a collection of useful functions for manipulating different data
  * types and network specific formats.
  * 
  * @author <a href="mailto:stefan.kraxberger@iaik.tugraz.at">Stefan
  *         Kraxberger</a>
  * @date Dec 6, 2006
  * @version $Rev: 930 $ $Date: 2007/02/12 17:56:29 $
  */
 public class NetUtils {
 
 	private static Log log_ = LogFactory.getLog(NetUtils.class);
 
 	/**
 	 * Converts an IP address of the format [xx]x.[xx]x.[xx]x.[xx]x to an
 	 * integer. Throws an exception if the format of the String is faulty.
 	 * 
 	 * @param ipAddr
 	 *            The IP address String
 	 * @return The IP address as an int
 	 */
 	public static int ipStringToInt(String ipAddr) {
 		byte dots = 0;
 		short ipOctet = 0;
 		int ipInt = 0;
 		for (int i = 0; i <= ipAddr.length(); i++) {
 			if (i == ipAddr.length() || ipAddr.charAt(i) == '.') {
 				if (i < ipAddr.length() && ++dots == 4) {
 					log_.debug("Too many dots in " + ipAddr);
 					return 0;
 				}
 				if (ipOctet < 0 || ipOctet > 255) {
 					log_.debug("Wrong IP values in " + ipAddr);
 					return 0;
 				}
 				ipInt = (ipInt << 8) | (ipOctet & 0xFF);
 				ipOctet = 0;
 			} else if (ipAddr.charAt(i) >= '0' && ipAddr.charAt(i) <= '9')
 				ipOctet = (short) (ipOctet * 10 + (ipAddr.charAt(i) - '0'));
 			else {
 				log_.debug("Wrong char in IP address " + ipAddr);
 				return 0;
 			}
 		}
 
 		if (dots != 3) {
 			log_.debug("IP address too short " + ipAddr);
 			return 0;
 		}
 		return ipInt;
 	}
 
 	/**
 	 * Converts the <code>Integer</code> representation of an IP address into a
 	 * <code>String</code> representation.
 	 * 
 	 * @param ipAddr
 	 *            The IP address as <code>Integer</code>.
 	 * @return The <code>String</code> representation of an IP address
 	 */
 	public static String ipIntToString(int ipAddr) {
 		return "" + ((ipAddr >>> 24) & 0xFF) + "." + ((ipAddr >>> 16) & 0xFF) + "." + ((ipAddr >>> 8) & 0xFF) + "." + ((ipAddr) & 0xFF);
 	}
 
 	/**
 	 * Converts the provided byte array values into an IPv4 address in
 	 * <code>String</code> representation (X[XX].X[XX].X[XX].X[XX]).
 	 * 
 	 * @param address
 	 *            The byte array containing the IP address values.
 	 * @param offset
 	 *            The starting point of the address in the provided byte array.
 	 *            From this index 4 bytes are taken for conversion into the IP
 	 *            address.
 	 * @return The <code>String</code> representation of this IP address.
 	 */
 
 	public static String toIPAddress(byte[] address, boolean IPv4) {
 		StringBuffer ip = new StringBuffer();
 		if (IPv4 && address.length == 4) {
 			ip.append(toDecString(address[0]) + ".");
 			ip.append(toDecString(address[1]) + ".");
 			ip.append(toDecString(address[2]) + ".");
 			ip.append(toDecString(address[3]));
 		} else if (!IPv4 && address.length == 16) {
 			// TODO: must be corrected is not working now.
 			ip.append(address[0] + "" + address[1] + ":");
 			ip.append(address[2] + "" + address[3] + ":");
 			ip.append(address[4] + "" + address[5] + ":");
 			ip.append(address[6] + "" + address[7] + ":");
 			ip.append(address[8] + "" + address[9] + ":");
 			ip.append(address[10] + "" + address[11] + ":");
 			ip.append(address[12] + "" + address[13] + ":");
 			ip.append(address[14] + "" + address[15] + ":");
 		}
 		return ip.toString();
 	}
 
 	/**
 	 * Converts the provided byte array values into an IPv4 address in
 	 * <code>String</code> representation (X[XX].X[XX].X[XX].X[XX]).
 	 * 
 	 * @param address
 	 *            The byte array containing the IP address values.
 	 * @param offset
 	 *            The starting point of the address in the provided byte array.
 	 *            From this index 4 bytes are taken for conversion into the IP
 	 *            address.
 	 * @return The <code>String</code> representation of this IP address.
 	 */
 	public static String toIPv4Address(byte[] address, int offset) {
 		return toIPAddress(getFromByteArray(address, offset, 4), true);
 	}
 
 	/**
 	 * Converts the provided byte array values into an IPv6 address in
 	 * <code>String</code> representation
 	 * (XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX).
 	 * 
 	 * @param address
 	 *            The byte array containing the IP address values.
 	 * @param offset
 	 *            The starting point of the address in the provided byte array.
 	 *            From this index 4 bytes are taken for conversion into the IP
 	 *            address.
 	 * @return The <code>String</code> representation of this IP address.
 	 */
 	public static String toIPv6Address(byte[] address, int offset) {
 		return toIPAddress(getFromByteArray(address, offset, 16), false);
 	}
 
 	/**
 	 * Converts a signed byte value into a usinged decimal string
 	 * representation.
 	 * 
 	 * @param b
 	 *            The signed byte value which should be converted.
 	 * @return The unsigned <code>String</code> representative of the provided
 	 *         signed byte value.
 	 */
 	public static String toDecString(byte b) {
 		return String.valueOf(toInt(b));
 	}
 
 	public static String toASCIIString(byte b) {
 		return new Character((char) b).toString();
 	}
 
 	public static String toASCIIString(byte[] byteArray) {
 		return toASCIIString(byteArray, 0, byteArray.length, 0);
 	}
 
 	public static String toASCIIString(byte[] byteArray, int off, int len, int linebreak) {
 
 		if (byteArray.length < off + len)
 			len = byteArray.length - off;
 
 		StringBuffer sb = new StringBuffer(2 * len);
 
 		for (int i = 0; i < len; i++) {
 			if (linebreak > 0 && (i % linebreak) == 0)
 				sb.append("\n");
 
 			sb.append(toASCIIString(byteArray[off + i]));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Converts a byte into a hexadecimal string. s *
 	 * 
 	 * @param b
 	 *            the byte to convert
 	 * @return the byte as hex value
 	 */
 	public static String toHexString(byte b) {
 
 		StringBuffer sb = new StringBuffer(2);
 
 		int h = (b & 0xf0) >> 4;
 		int l = (b & 0x0f);
 		sb.append(new Character((char) ((h > 9) ? 'A' + h - 10 : '0' + h)));
 		sb.append(new Character((char) ((l > 9) ? 'A' + l - 10 : '0' + l)));
 
 		return sb.toString();
 	}
 
 	/**
 	 * Converts the specified subsequence of the given byte array into a
 	 * hexadecimal string of the format: 01:23:34:56... .
 	 * 
 	 * @param byteArray
 	 *            the byte array containing the data
 	 * @param off
 	 *            the offset indicating the start position within the given byte
 	 *            array; the following <code>len</code> bytes will be converted
 	 *            into a hexadecimal string
 	 * @param len
 	 *            how many bytes shall be written
 	 * @return the string representation
 	 */
 	public static String toHexString(byte[] byteArray, int off, int len) {
 		return toHexString(byteArray, off, len, ":", -1);
 	}
 
 	/**
 	 * Converts the specified subsequence of the given byte array into a
 	 * hexadecimal string of the format:
 	 * 01'delimiter'23'delimiter'34'delimiter'56... . Whereas for the delimiter
 	 * any arbitrary <code>String</code> can be used which separates the entries
 	 * of the byte array.
 	 * 
 	 * @param byteArray
 	 *            the byte array containing the data
 	 * @param off
 	 *            the offset indicating the start position within the given byte
 	 *            array; the following <code>len</code> bytes will be converted
 	 *            into a hexadecimal string
 	 * @param len
 	 *            how many bytes shall be written
 	 * @return the string representation
 	 */
 	public static String toHexString(byte[] byteArray, int off, int len, String delimiter) {
 		return toHexString(byteArray, off, len, delimiter, -1);
 	}
 
 	/**
 	 * Converts the specified subsequence of the given byte array into a
 	 * hexadecimal string of the format:
 	 * 01'delimiter'23'delimiter'34'delimiter'56... . Whereas for the delimiter
 	 * any arbitrary <code>String</code> can be used which separates the entries
 	 * of the byte array.
 	 * 
 	 * @param byteArray
 	 *            the byte array containing the data
 	 * @param off
 	 *            the offset indicating the start position within the given byte
 	 *            array; the following <code>len</code> bytes will be converted
 	 *            into a hexadecimal string
 	 * @param len
 	 *            how many bytes shall be written
 	 * @param delimiter
 	 *            the delimiter to be used for separating the hex values, e.g.
 	 *            ":" (specify "" for not using a delimiter)
 	 * @param linebreak
 	 *            Specifies the amount of bytes should be printed in one line.
 	 *            After the specified amount of bytes a linebreak is inserted.
 	 * @return the string representation
 	 */
 	public static String toHexString(byte[] byteArray, int off, int len, String delimiter, int linebreak) {
 
 		if (byteArray.length < off + len)
 			len = byteArray.length - off;
 
 		StringBuffer sb = new StringBuffer(2 * len);
 		boolean first = true;
 
 		for (int i = 0; i < len; i++) {
 			if (first)
 				first = false;
 			else if (linebreak > 0 && (i % linebreak) == 0)
 				sb.append("\n");
 			else
 				sb.append(delimiter);
 
 			sb.append(toHexString(byteArray[off + i]));
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Converts a byte array into a hexadecimal string of the format:
 	 * 01:23:34:56... .
 	 * 
 	 * @param byteArray
 	 *            the byte array containing the data
 	 * 
 	 * @return the string representation
 	 */
 	public static String toHexString(byte byteArray[]) {
 		return toHexString(byteArray, 0, byteArray.length);
 	}
 
 	/**
 	 * Converts a byte array into a hexadecimal string of the format:
 	 * 01:23:34:56...
 	 * 
 	 * @param byteArray
 	 *            the byte array containing the data
 	 * 
 	 * @return the string representation
 	 */
 	public static String toHexString(byte byteArray[], int linebreak) {
 		return toHexString(byteArray, 0, byteArray.length, " ", linebreak);
 	}
 
 	/**
 	 * Converts a byte array into a hexadecimal string of the format:
 	 * 01'delimiter'23'delimiter'34'delimiter'56... . Whereas for the delimiter
 	 * any arbitrary <code>String</code> can be used which separates the entries
 	 * of the byte array.
 	 * 
 	 * @param byteArray
 	 *            the byte array containing the data
 	 * @param delimiter
 	 *            the delimiter to be used for separating the hex values, e.g.
 	 *            ":" (specify "" for not using a delimiter)
 	 * 
 	 * @return the string representation
 	 */
 	public static String toHexString(byte byteArray[], String delimiter) {
 		return toHexString(byteArray, 0, byteArray.length, delimiter, -1);
 	}
 
 	/**
 	 * Converts an int into a hexadecimal string of the format: 01:23:34:56... .
 	 * 
 	 * @param i
 	 *            the int to convert
 	 * @return the int as hexadecimal string representation
 	 */
 	public static String toHexString(int i) {
 		return toHexString(i, "");
 	}
 
 	/**
 	 * Converts an int into a hexadecimal string. After each byte of the integer
 	 * the provided delimiter is inserted. Therefore an integer can be converted
 	 * into either an MAC address of format (XX:XX:XX:XX:XX:XX) or into an IP
 	 * address of format (X[XX].X[XX].X[XX].X[XX]).
 	 * 
 	 * @param i
 	 *            The integer which should be converted into an hexadecimal
 	 *            string.
 	 * @param delimiter
 	 *            The character which should be used as delimiter.
 	 * @return The String representation of the provided integer.
 	 */
 	public static String toHexString(int i, String delimiter) {
 		byte[] array = new byte[4];
 		array[0] = (byte) (i >> 24);
 		array[1] = (byte) (i >> 16);
 		array[2] = (byte) (i >> 8);
 		array[3] = (byte) (i);
 
 		return toHexString(array, delimiter);
 	}
 
 	/**
 	 * Converts an long into a hexadecimal string of the format
 	 * XX:XX:XX:XX:XX:XX.
 	 */
 
 	/**
 	 * Converts a long into a hexadecimal string of the format: XX:XX:.. . Where
 	 * X can be 0 - 9 or A - F.s Uses big endian byte order.
 	 * 
 	 * @param l
 	 *            the long to convert
 	 * @return the int as hexadecimal string representation
 	 */
 	public static String toHexString(long l) {
 		return toHexString(l, "");
 	}
 
 	/**
 	 * Converts a long into a hexadecimal string of the format:
 	 * XX[delimiter]XX[delimiter].. . Where X can be 0 - 9 or A - F.s Uses big
 	 * endian byte order.
 	 * 
 	 * @param l
 	 *            the long to convert
 	 * @param delimiter
 	 *            the character or String which separates the octets.
 	 * 
 	 * @return the int as hexadecimal string representation
 	 */
 	public static String toHexString(long l, String delimiter) {
 		return toHexString((int) (l >> 32)) + delimiter + toHexString((int) l);
 	}
 
 	/**
 	 * Converts the provided short value into hexadecimal values represented
 	 * as String.
 	 * 
 	 * @param s The short value which should be transformed.
 	 * @return The hexadecimal representation as String. 
 	 */
 	public static String toHexString(short s) {
 		return toHexString(s, "");
 	}
 
 	/**
 	 * Returns the HEX representation of the provided short value as String. The
 	 * provided delimiter is used between each hextet
 	 * 
 	 * @param s
 	 * @param delimiter
 	 * @return
 	 */
 	public static String toHexString(short s, String delimiter) {
 		byte[] array = new byte[2];
 
 		array[0] = (byte) (s >> 8);
 		array[1] = (byte) (s);
 
 		return toHexString(array, delimiter);
 	}
 
 	/**
 	 * Returns a new byte array containing the data from the provided byte array
 	 * starting from the provided <code>start</code> position using
 	 * <code>len</code> bytes.
 	 * 
 	 * @param array
 	 *            The original byte array.
 	 * @param start
 	 *            The start point from where in the original byte array the data
 	 *            should be taken.
 	 * @param len
 	 *            The amount of bytes which should be taken from the original
 	 *            byte array.
 	 * @return The resulting byte array.
 	 */
 	public static byte[] getFromByteArray(byte[] array, int start, int len) {
 		return getFromByteArray(array, start, len, false);
 	}
 
 	/**
 	 * Returns a new byte array containing the data from the provided byte array
 	 * starting from the provided <code>start</code> position using
 	 * <code>len</code> bytes. If there are not enough bytes remaining in the
 	 * byte array to create the new byte array but the <code>getRest</code>
 	 * switch is enabled the available bytes are used instead.s
 	 * 
 	 * @param array
 	 *            The original byte array.
 	 * @param start
 	 *            The start point from where in the original byte array the data
 	 *            should be taken.
 	 * @param len
 	 *            The amount of bytes which should be taken from the original
 	 *            byte array.
 	 * @param getRest
 	 *            Switch which indicates if we should use the data from the byte
 	 *            array even if there aren't enough bytes as specified by
 	 *            <code>len</code>
 	 * @return The resulting byte array.
 	 */
 	public static byte[] getFromByteArray(byte[] array, int start, int len, boolean getRest) {
 		byte[] newArray = null;
 
 		if (array.length >= start + len) {
 			newArray = new byte[len];
 			for (int index = 0; index < len; index++) {
 				newArray[index] = array[start + index];
 			}
 		} else if (array.length < start + len && getRest) {
 			newArray = new byte[len];
 			for (int index = 0; index < (array.length - start); index++)
 				newArray[index] = array[start + index];
 		}
 
 		return newArray;
 	}
 
 	public static int bytesToInt(byte[] intBytes, boolean fill) {
 
 		int value = 0;
 
 		if (fill) {
 			if (intBytes.length < 4) {
 				int i = 0;
 				byte[] temp = new byte[4];
 				for (; i < intBytes.length; i++)
 					temp[i] = intBytes[i];
 				intBytes = temp;
 			}
 		}
 
 		value = toInt(intBytes[0]);
 		value = (value << 8);
 		value = value + toInt(intBytes[1]);
 		value = (value << 8);
 		value = value + toInt(intBytes[2]);
 		value = (value << 8);
 		value = value + toInt(intBytes[3]);
 		return value;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param intBytes
 	 * @param offset
 	 * @param fill
 	 * @return
 	 */
 	public static int bytesToInt(byte[] intBytes, int offset, boolean fill) {
 		return bytesToInt(getFromByteArray(intBytes, offset, 4, fill), fill);
 	}
 
 	/**
 	 * Convertes the provided byte array to an integer value. The 4 bytes needed
 	 * for the integer transformation are taken from the byte array starting
 	 * from the offset position.
 	 * 
 	 * @param intBytes
 	 *            The amoun
 	 * @param offset
 	 * @return
 	 */
 	public static int bytesToInt(byte[] intBytes, int offset) {
 		return bytesToInt(getFromByteArray(intBytes, offset, 4), false);
 	}
 
 	/**
 	 * Converts a short value into the appropriate sequence of byte values. The
 	 * result is returned in a byte array containing two values.
 	 * 
 	 * @param shortValue
 	 *            The short value which should be converted.
 	 * @return The converted byte array.
 	 */
 	public static byte[] shortToBytes(short shortValue) {
 
 		byte[] value = new byte[2];
 
 		value[0] = (byte) ((shortValue & 0xFF00) >>> 8);
 		value[1] = (byte) ((shortValue & 0x00FF));
 
 		return value;
 	}
 
 	/**
 	 * Converts a integer value into the appropriate sequence of byte values.
 	 * The result is returned in a byte array containing four values.
 	 * 
 	 * @param intValue
 	 *            The integer value which should be converted.
 	 * @return The converted byte array.
 	 */
 	public static byte[] intToBytes(int intValue) {
 		byte[] value = new byte[4];
 		// intValue = intValue & 0xFFFF;
 		value[0] = (byte) ((intValue & 0xFF000000) >>> 24);
 		value[1] = (byte) ((intValue & 0x00FF0000) >>> 16);
 		value[2] = (byte) ((intValue & 0x0000FF00) >>> 8);
 		value[3] = (byte) ((intValue & 0x000000FF));
 
 		return value;
 	}
 
 	/**
 	 * Converts a value stored in two bytes into a short (2 x 8 = 16 bit).
 	 * 
 	 * @param shortBytes
 	 *            The value stored in a byte array of length 2.
 	 * @return The value as primitive type <code>short</code>.
 	 */
 	public static short bytesToShort(byte[] shortBytes) {
 
 		int value = 0;
 
 		value = (int) shortBytes[0] & 0xFF;
 
 		value = value << 8 | (shortBytes[1] & 0xFF);
 
 		return (short) value;
 	}
 
 	public static short bytesToShort(byte[] shortBytes, int offset) {
 		return bytesToShort(getFromByteArray(shortBytes, offset, 2));
 	}
 
 	/**
 	 * Primitive function which converts a signed byte into an unsigned integer.
 	 * Therefore, converts the singed value into an unsigned value. This can
 	 * also be casted into a short value without lost of precision.
 	 * 
 	 * @param b
 	 *            The byte value (-128 - +127).
 	 * @return The unsigned integer value (0 - 255).
 	 */
 	public static int toInt(byte b) {
 		int value = 0;
 
 		value = (b & 0xFF);
 
 		return value;
 	}
 
 	public static int toInt(short b) {
 		int value = 0;
 
 		value = (b & 0xFFFF);
 
 		return value;
 	}
 
 	/**
 	 * Converts an MAC (XX:XX:XX:XX:XX:XX) or IP address
 	 * (X[XX].X[XX].X[XX].X[XX]) into a corresponding byte array. The returned
 	 * byte array is therefore either 6 (MAC) or 4 (IP) bytes long.
 	 * 
 	 * @param s
 	 *            The MAC or IP address as <code>String</code>.
 	 * @param delimiter
 	 *            The delimiter character, either <code>:</code> or
 	 *            <code>.</code>.
 	 * @return The byte array containing the address in bytes.
 	 */
 	public static byte[] addressToBytes(String s, String delimiter) {
 		int index = 0;
 		int offset = 0;
 
 		if (delimiter.equals(":"))
 			offset = 16;
 		else
 			offset = 10;
 
 		StringTokenizer st = new StringTokenizer(s, delimiter);
 
 		byte[] b = new byte[st.countTokens()];
 
 		while (st.hasMoreTokens()) {
 			b[index++] = (byte) Integer.parseInt(st.nextToken(), offset);
 		}
 
 		return b;
 	}
 
 	/**
 	 * Checks if the provided <code>String</code> is in HEX format. This is a
 	 * very simple and failure prone check, since this function only searches
 	 * the <code>String</code> for occurences of possible HEX characters. It
 	 * does not verify if it can be converted into a decimal number.
 	 * 
 	 * @param s
 	 *            The <code>String</code> which should be checked.
 	 * @return True if the <code>String</code> contains HEX characters.
 	 */
 	public static boolean isHEX(String s) {
 		char[] sequence = s.toCharArray();
 
 		for (int i = 0; i < sequence.length; i++) {
 			if (!(sequence[i] >= '0' && sequence[i] <= '9') && !(sequence[i] >= 'A' && sequence[i] <= 'F')
 					&& !(sequence[i] >= 'a' && sequence[i] <= 'f') && !(sequence[i] == 'x'))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Concatenates two byte arrays and returns the newly created byte array
 	 * containing the content of both. The content of the byte array data1 is on
 	 * the lower indexes and the content of the byte array data2 is on the
 	 * higher indexes of the returned byte array.
 	 * 
 	 * @param data1
 	 *            Byte array which should come first in the new combined byte
 	 *            array.
 	 * @param data2
 	 *            Byte array which should come first in the new combined byte
 	 *            array.
 	 * @return The newly created combined byte array.
 	 */
 	public static byte[] concatData(byte[] data1, byte[] data2) {
 		byte[] data = null;
 		int data_1_length = 0;
 		int data_2_length = 0;
 		if (data1 instanceof byte[] && data2 instanceof byte[]) {
 			data = new byte[data1.length + data2.length];
 			data_1_length = data1.length;
 			data_2_length = data2.length;
 		} else if (data1 instanceof byte[] && !(data2 instanceof byte[])) {
 			data = new byte[data1.length];
 			data_1_length = data1.length;
 		} else if (!(data1 instanceof byte[]) && data2 instanceof byte[]) {
 			data = new byte[data2.length];
 			data_2_length = data2.length;
 		} else
 			return data;
 
 		int index = 0;
 
 		for (; index < data_1_length; index++) {
 			data[index] = data1[index];
 		}
 		for (; index < (data1.length + data_2_length); index++) {
 			data[index] = data2[index - data1.length];
 		}
 		return data;
 	}
 
 	/**
 	 * Inserts the content of the byte array <code>data2</code> at the specified
 	 * offset position into the byte array <code>data</code>. This function only
 	 * is executed if the provided content of the byte array <code>data2</code>
 	 * fits into the byte array <code>data</code>.
 	 * 
 	 * @param data
 	 *            The byte array into which the content should be inserted.
 	 * @param data2
 	 *            The content which should be inserted.
 	 * @param offset
 	 *            The position at which the content should be inserted.
 	 * @return The byte array containing the inserted content.
 	 */
 	public static byte[] insertData(byte[] data, byte[] data2, int offset) {
 		if ((offset + data2.length) <= data.length) {
 			for (int index = offset; (index - offset) < data2.length; index++) {
 				data[index] = data2[(index - offset)];
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Inserts the content of the byte array <code>data2</code> at the specified
 	 * offset position into the byte array <code>data</code>. This function only
 	 * is executed if the provided content of the byte array <code>data2</code>
 	 * fits into the byte array <code>data</code>.
 	 * 
 	 * @param data
 	 *            The byte array into which the content should be inserted.
 	 * @param data2
 	 *            The content which should be inserted.
 	 * @param offset
 	 *            The position at which the content should be inserted.
 	 * @param count
 	 *            Specifies the amount of bytes which should be copied.
 	 * @return The byte array containing the inserted content.
 	 */
 	public static byte[] insertData(byte[] data, byte[] data2, int offset, int count) {
 		if ((offset + count) <= data.length) {
 			for (int index = offset; (index - offset) < count; index++) {
 				data[index] = data2[(index - offset)];
 			}
 		}
 		return data;
 	}
 	
 	public static short calcIPChecksum(byte[] data, int offset, int length)
 	{
 		int sum = 0;
 
 		
		for(int i = offset; i < length-1 && i < data.length-1; i+=2)
 		{
			int word = (int)(toInt(data[i])<<8 | toInt(data[i+1]));
 			
 			sum += word;
 		}
 		
 		while(sum>>16 != 0)
 		{
 			sum = (int)(sum & 0xFFFF) + ((int)sum>>16);
 		}
 		
 		return (short)((~sum) & 0xFFFF);
 	}
 }
