 /*******************************************************************************
  * Copyright 2011 Andrew Jenkins
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.ajj.robodtn.sdnvlib;
 
 import java.math.BigInteger;
 
 public class Sdnv {
 	public Sdnv() {
 		value = 0;
 		bytesInt = new BigInteger(0, zeroArray);
 		bytes = zeroArray;
 	}
 	
 	public Sdnv(long value) {
 		setByValue(value);
 	}
 	
 	public Sdnv(byte [] bytes) {
 		setByBytes(bytes);
 	}
 	
 	public Sdnv(byte [] bytes, int index) {
 		setByBytes(bytes, index);
 	}
 	
 	public Sdnv(String bytesString, int radix) {
 		setByBytesString(bytesString, radix);
 	}
 	
 	public long getValue() {
 		return value;
 	}
 	
 	public void setByValue(long value) {
 		if (value < 0) {
 			throw new NumberFormatException("SDNVs must be non-negative");
 		}
 		this.value = value;
 		this.valueToBytes();
 	}
 	
 	public byte [] getBytes() {
 		return bytes;
 	}
 	
 	public String getBytesAsHexString() {
 		return bytesInt.toString(16);
 	}
 	
 	public String getBytesAsString() {
 		return bytesInt.toString();
 	}
 	
 	public void setByBytes(byte [] bytes, int index) {
 		long v = 0;
 		BigInteger b = new BigInteger(0, zeroArray);
 		int i;
 		
 		for(i = index; i < bytes.length; i++)
 		{
 			if (v > Long.MAX_VALUE>>7) {
 				throw new NumberFormatException("SDNV is bigger than " + Long.MAX_VALUE);
 			}
 			
 			/* Store lower 7 bits to v */
 			v <<=7;
 			v |= bytes[i] & 0x7F;
 			
 			/* Store all 8 bits to b */
 			b = b.shiftLeft(8).or(new BigInteger(1, new byte [] { bytes[i] }));
 			
 			/* See if we must keep going. */
 			if ((bytes[i] & 0x80) == 0) break;
 		}
 		
 		value = v;
 		this.bytesInt = b;
 		bytesIntToBytes();
 	}
 	
 	public void setByBytes(byte [] bytes) {
 		setByBytes(bytes, 0);
 	}
 	
 	public void setByBytesString(String bytes, int radix) {
 		BigInteger b = new BigInteger(bytes, radix);
 		byte [] bAsBytes = b.toByteArray();
 		if (bAsBytes[0] == 0x00) {
 			setByBytes(bAsBytes, 1);
 		} else {
 			setByBytes(bAsBytes, 0);
 		}
 	}
 	
 	public boolean equals(Sdnv rhs) {
 		if(value == rhs.value) return true;
 		return false;
 	}
 	
 	private void valueToBytes() {
 		// No SDNV can be bigger than 10 bytes encoded.
 		final int maxSdnvByteArray = 10;
 		byte [] myBytes = new byte[maxSdnvByteArray];
 		
 		int i = 0;
 		long v = value;
 		
 		// Encode value into myBytes scratch space.
 		do {
 			myBytes[maxSdnvByteArray-1-i] = (byte) ((0x7F & v) | (i > 0 ? 0x80 : 0x00));
 			v >>= 7;
 			i++;
 		} while (v > 0);
 		
 		// Allocate a new correctly-sized array for bytes and copy into it.
 		byte [] sizedBytes = new byte[i];
 		System.arraycopy(myBytes, maxSdnvByteArray-i, sizedBytes, 0, i);
 		bytesInt = new BigInteger(1, sizedBytes);
 		bytesIntToBytes();
 	}
 	
 	private void bytesIntToBytes() {
 		byte [] ba = bytesInt.toByteArray();
 		
		/* If there is one byte and it is 0, then we're done. */
		if (ba.length == 1 && ba[0] == 0x00) {
			bytes = ba;
			return;
		}
		
 		/* Otherwise, strip leading zeros from the byte array. */
 		int numZeros;
 		for(numZeros = 0; ba[numZeros] == 0x00; numZeros++);
 		
 		bytes = new byte [ba.length - numZeros];
 		System.arraycopy(ba, numZeros, bytes, 0, ba.length - numZeros);
 	}
 	
 	private byte [] bytes;
 	private BigInteger bytesInt;
 	private long value;
 	
 	private static final byte [] zeroArray = new byte [] { 0x00 };
 }
