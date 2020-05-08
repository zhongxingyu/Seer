 /*
  * This file is part of JSTUN.
  * 
  * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
  * reserved.
  * 
  * This software is licensed under either the GNU Public License (GPL),
  * or the Apache 2.0 license. Copies of both license agreements are
  * included in this distribution.
  */
 
 package de.javawi.jstun.util;
 
 public class Utility {
 
 	public static final byte integerToOneByte(int value)
 			throws UtilityException {
 		if ((value > Math.pow(2, 15)) || (value < 0)) { // TODO shouldn't it be
 			// 31 ?
 			throw new UtilityException("Integer value " + value
 					+ " is larger than 2^15");
 		}
 		return (byte) (value & 0xFF);
 	}
 
 	// converts to Big-Endian
 	public static final byte[] integerToTwoBytes(int value)
 			throws UtilityException {
 		byte[] result = new byte[2];
 		if ((value > Math.pow(2, 31)) || (value < 0)) {
 			throw new UtilityException("Integer value " + value
 					+ " is larger than 2^31");
 		}
 		result[0] = (byte) ((value >>> 8) & 0xFF);
 		result[1] = (byte) value;
 		return result;
 	}
 
 	// converts to Big-Endian
 	public static final byte[] integerToFourBytes(int value)
 			throws UtilityException {
 		byte[] result = new byte[4];
 		if ((value > Math.pow(2, 63)) || (value < 0)) {
 			throw new UtilityException("Integer value " + value
 					+ " is larger than 2^63");
 		}
 		result[0] = (byte) ((value >>> 24) & 0xFF);
 		result[1] = (byte) ((value >>> 16) & 0xFF);
 		result[2] = (byte) ((value >>> 8) & 0xFF);
 		result[3] = (byte) (value & 0xFF);
 		return result;
 	}
 
 	public static final int oneByteToInteger(byte value)
 			throws UtilityException {
 		return (int) value & 0xFF;
 	}
 
 	public static final int twoBytesToInteger(byte[] value)
 			throws UtilityException {
 		if (value.length < 2) {
 			throw new UtilityException("Byte array too short!");
 		}
 		int temp0 = value[0] & 0xFF;
 		int temp1 = value[1] & 0xFF;
 		return ((temp0 << 8) + temp1);
 	}
 
 	public static final long fourBytesToLong(byte[] value)
 			throws UtilityException { // TODO should be int?
 		if (value.length < 4) {
 			throw new UtilityException("Byte array too short!");
 		}
 		int temp0 = value[0] & 0xFF;
 		int temp1 = value[1] & 0xFF;
 		int temp2 = value[2] & 0xFF;
 		int temp3 = value[3] & 0xFF;
 		return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
 	}
 
 	public static final int fourBytesToInt(byte[] value)
 			throws UtilityException {
 		if (value.length < 4) {
 			throw new UtilityException("Byte array too short!");
 		}
 		
		int temp0, temp1, temp2, temp3;

 		// value[i] & 0xFF in order to convert to unsigned integer
		temp0 = (value[0] & 0xFF) << 24;
		temp1 = (value[1] & 0xFF) << 16;
		temp2 = (value[2] & 0xFF) << 8;
		temp3 = (value[3] & 0xFF);
		return temp0 | temp1 | temp2 | temp3;
 	}
 
	// TODO it's not so deprecated after all
 	/**
 	 * <p>
 	 * Returns 2 if the first 2 bits of typeArray are 0's, as it's a RFC5389
 	 * (stun2) header. <br>
 	 * Otherwise, it's a RFC3489 (stun1) header, and it returns 1.
 	 * 
 	 * @param typeArray
 	 *            the byte array containing the first 2 bytes of the header
 	 * @return 1 or 2
 	 * @throws UtilityException
 	 *             When typeArray size is != 2
 	 * @deprecated It's not necessarily true that stun1 headers don't have 0b00
 	 *             as the first 2 bytes.
 	 */
 	@Deprecated
 	public static int stunVersion(byte[] typeArray) throws UtilityException {
 		if (typeArray.length != 2)
 			throw new UtilityException("Wrong type header length");
 		else {
 			// TODO unmagic
 			if ((typeArray[0] & 0xC0) == 1) // it's not a stun2 header
 				return 1;
 			else
 				return 2;
 		}
 	}
 }
