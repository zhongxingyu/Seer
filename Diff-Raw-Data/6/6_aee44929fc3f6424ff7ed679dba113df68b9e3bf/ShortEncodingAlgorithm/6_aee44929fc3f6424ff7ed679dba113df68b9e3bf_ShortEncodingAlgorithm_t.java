 /*
  * Fast Infoset ver. 0.1 software ("Software")
  *
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
  *
  * Software is licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at:
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations.
  *
  *    Sun supports and benefits from the global community of open source
  * developers, and thanks the community for its important contributions and
  * open standards-based technology, which Sun has adopted into many of its
  * products.
  *
  *    Please note that portions of Software may be provided with notices and
  * open source licenses from such communities and third parties that govern the
  * use of those portions, and any licenses granted hereunder do not alter any
  * rights and obligations you may have under such open source licenses,
  * however, the disclaimer of warranty and limitation of liability provisions
  * in this License will apply to all Software in this distribution.
  *
  *    You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any nuclear
  * facility.
  *
  * Apache License
  * Version 2.0, January 2004
  * http://www.apache.org/licenses/
  *
  */
 
 
 package com.sun.xml.fastinfoset.algorithm;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import org.jvnet.fastinfoset.EncodingAlgorithmException;
 
 
 /**
  * An encoder for handling Short values.  Suppports the builtin SHORT encoder.
  *
  * @author Alan Hudson
  * @version
  */
 public class ShortEncodingAlgorithm extends IntegerEncodingAlgorithm {
 
     public final int getPrimtiveLengthFromOctetLength(int octetLength) throws EncodingAlgorithmException {
         if (octetLength % SHORT_SIZE != 0) {
             throw new EncodingAlgorithmException("'length' is not a multiple of " +
                     SHORT_SIZE +
                     " bytes correspond to the size of the 'short' primitive type");
         }
 
         return octetLength / SHORT_SIZE;
     }
 
     public int getOctetLengthFromPrimitiveLength(int primitiveLength) {
         return primitiveLength * SHORT_SIZE;
     }
 
     public final Object decodeFromBytes(byte[] b, int start, int length) throws EncodingAlgorithmException {
         short[] data = new short[getPrimtiveLengthFromOctetLength(length)];
         decodeFromBytesToShortArray(data, 0, b, start, length);
 
         return data;
     }
 
     public final Object decodeFromInputStream(InputStream s) throws IOException {
         return decodeFromInputStreamToShortArray(s);
     }
 
 
     public void encodeToOutputStream(Object data, OutputStream s) throws IOException {
         if (!(data instanceof short[])) {
             throw new IllegalArgumentException("'data' not an instance of short[]");
         }
 
         final short[] idata = (short[])data;
 
         encodeToOutputStreamFromShortArray(idata, s);
     }
 
 
     public final Object convertFromCharacters(char[] ch, int start, int length) {
         final CharBuffer cb = CharBuffer.wrap(ch, start, length);
         final List shortList = new ArrayList();
 
         matchWhiteSpaceDelimnatedWords(cb,
                 new WordListener() {
             public void word(int start, int end) {
                 String iStringValue = cb.subSequence(start, end).toString();
                 shortList.add(Short.valueOf(iStringValue));
             }
         }
         );
 
         return generateArrayFromList(shortList);
     }
 
     public final void convertToCharacters(Object data, StringBuffer s) {
         if (!(data instanceof short[])) {
             throw new IllegalArgumentException("'data' not an instance of short[]");
         }
 
         final short[] idata = (short[])data;
 
         convertToCharactersFromShortArray(idata, s);
     }
 
 
     public final void decodeFromBytesToShortArray(short[] sdata, int istart, byte[] b, int start, int length) {
         final int size = length / SHORT_SIZE;
         for (int i = 0; i < size; i++) {
             sdata[istart++] = (short) (((b[start++] & 0xFF) << 8) |
                     (b[start++] & 0xFF));
         }
     }
 
     public final short[] decodeFromInputStreamToShortArray(InputStream s) throws IOException {
         final List shortList = new ArrayList();
         final byte[] b = new byte[SHORT_SIZE];
 
         while (true) {
             int n = s.read(b);
             if (n != 2) {
                 if (n == -1) {
                     break;
                 }
 
                 while(n != 2) {
                     final int m = s.read(b, n, SHORT_SIZE - n);
                     if (m == -1) {
                         throw new EOFException();
                     }
                     n += m;
                 }
             }
 
            final int i = ((b[0] & 0xFF) << 8) |
                    (b[1] & 0xFF);
            shortList.add(new Short((short)i));
         }
 
         return generateArrayFromList(shortList);
     }
 
 
     public final void encodeToOutputStreamFromShortArray(short[] idata, OutputStream s) throws IOException {
         for (int i = 0; i < idata.length; i++) {
             final int bits = idata[i];
             s.write((bits >>> 8));
             s.write(bits & 0xFF);
         }
     }
 
     public final void encodeToBytes(Object array, int astart, int alength, byte[] b, int start) {
         encodeToBytesFromShortArray((short[])array, astart, alength, b, start);
     }
 
     public final void encodeToBytesFromShortArray(short[] sdata, int istart, int ilength, byte[] b, int start) {
         final int iend = istart + ilength;
         for (int i = istart; i < iend; i++) {
             final short bits = sdata[i];
             b[start++] = (byte)((bits >>>  8));
             b[start++] = (byte)(bits & 0xFF);
         }
     }
 
 
     public final void convertToCharactersFromShortArray(short[] sdata, StringBuffer s) {
         for (int i = 0; i < sdata.length; i++) {
             s.append(Short.toString(sdata[i]));
             if (i != sdata.length) {
                 s.append(' ');
             }
         }
     }
 
 
     public final short[] generateArrayFromList(List array) {
         short[] sdata = new short[array.size()];
         for (int i = 0; i < sdata.length; i++) {
             sdata[i] = ((Short)array.get(i)).shortValue();
         }
 
         return sdata;
     }
 }
