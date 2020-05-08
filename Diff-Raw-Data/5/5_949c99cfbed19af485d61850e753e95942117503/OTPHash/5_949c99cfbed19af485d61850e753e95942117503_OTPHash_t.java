 /*
  * OTPHash.java
  * Copyright (C) 2002, Klaus Rennecke.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package net.sourceforge.fraglets.codec;
 
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.io.NotSerializableException;
 import java.io.ObjectOutputStream;
 import java.io.ObjectStreamClass;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Random;
 
 /**
  * This hashing library uses a mechanism analogous to OTP cryptography.
  * Hash function is a folded cryptogram of the key.
  * 
  * The OTP for encryption is taken from a PRNG, with transition mapping
  * to de-skew bit probabilities. Cryptogram is the (n + k)th random number
  * from the PRNG for byte n with value k.
  * 
  * Folding is done by rolling the previous value right by one bit and
  * XORing that with the next value. This is similar to the folding in
  * BUZHash from Robert Uzgalis. However, rolling is reversed and the
  * hash is folded from the rear, starting at the last byte.
  * 
  * Since the OTP is grown for larger keys, there is no limitation on the
  * length of the input key. Rolling prevents against nearby similarities,
  * and the extension of the OTP for key length prevents similarities with
  * greater distances.
  * 
  * @author marion@users.sourceforge.net
  * @version $Revision$
  */
 public class OTPHash {
 
     /**
      * Hash a byte array.
      * 
      * @param key the array to hash
      * @return the hash code for key
      */
     public static int hash(byte key[]) {
         if (key == null) return 0;
         return hash(key, 0, key.length);
     }
     
     /**
      * Hash a byte array, using only a portion of the array.
      * 
      * @param key the array to hash
      * @param off where to start in key
      * @param len length of portion of key to use.
      * @return the hash code for key
      */
     public static int hash(byte key[], int off, int len) {
         if (key == null) return 0;
         int step = 0;
         int scan = off + len;
         int part = len;
         int bits[] = getOTP(scan + 255);
        while (--scan >= off) {
             step = roll(step) ^ bits[--part + (key[scan] & 0xff)];
         }
         return step;
     }
 
     /**
      * Hash a character array.
      * 
      * @param key the array to hash
      * @return the hash code for key
      */
     public static int hash(char key[]) {
         if (key == null) return 0;
         return hash(key, 0, key.length);
     }
     
     /**
      * Hash a character array, using only a portion of the array.
      * 
      * @param key the array to hash
      * @param off where to start in key
      * @param len length of portion of key to use.
      * @return the hash code for key
      */
     public static int hash(char key[], int off, int len) {
         if (key == null) return 0;
         validateBufferIndex(key.length, off, len);
         int step = 0, fold;
         int scan = off + len;
         int part = len * 2;
         int bits[] = getOTP(part + 255);
        while (--scan >= off) {
             fold = (int) key[scan];
             step = roll(step) ^ bits[--part + (fold & 0xff)];
             step = roll(step) ^ bits[--part + (fold >>> 8)];
         }
         return step;
     }
 
     /**
      * Hash a string.
      * 
      * @param key the string to hash
      * @return the hash code for key
      */
     public static int hash(String key) {
         if (key == null)
             return 0;
         int step = 0, fold;
         int scan = key.length();
         int part = scan * 2;
         int bits[] = getOTP(part + 255);
         while (--scan >= 0) {
             fold = (int) key.charAt(scan);
             step = roll(step) ^ bits[--part + (fold & 0xff)];
             step = roll(step) ^ bits[--part + (fold >>> 8)];
         }
         return step;
     }
 
     /**
      * Hash a string buffer.
      * 
      * @param key the string buffer to hash
      * @return the hash code for key
      */
     public static int hash(StringBuffer key) {
         if (key == null)
             return 0;
         int step = 0, fold;
         int scan = key.length();
         int part = scan * 2;
         int bits[] = getOTP(part + 255);
         while (--scan >= 0) {
             fold = (int) key.charAt(scan);
             step = roll(step) ^ bits[--part + (fold & 0xff)];
             step = roll(step) ^ bits[--part + (fold >>> 8)];
         }
         return step;
     }
 
     /**
      * Hash an integer array.
      * 
      * @param key the array to hash
      * @return the hash code for key
      */
     public static int hash(int key[]) {
         if (key == null) return 0;
         return hash(key, 0, key.length);
     }
 
     /**
      * Hash an integer array, using only a portion of the array.
      * 
      * @param key the array to hash
      * @param off where to start in key
      * @param len length of portion of key to use.
      * @return the hash code for key
      */
     public static int hash(int key[], int off, int len) {
         if (key == null) return 0;
         validateBufferIndex(key.length, off, len);
         int step = 0, fold;
         int scan = off + len;
         int part = len * 4;
         int bits[] = getOTP(part + 255);
         while (--scan >= off) {
             fold = key[scan];
             step = roll(step) ^ bits[--part + (fold & 0xff)];
             step = roll(step) ^ bits[--part + ((fold >>>= 8) & 0xff)];
             step = roll(step) ^ bits[--part + ((fold >>>= 8) & 0xff)];
             step = roll(step) ^ bits[--part + (fold >>> 8)];
         }
         return step;
     }
 
     /**
      * Hash an arbitrary serializable object. Note that the hash codes generated
      * by this method are <em>not identical</em> with the hash codes from the
      * type specific methods. This is due to the fact that object stream protocol
      * data is being hashed as well as hashing being done from the front of the
      * input.
      * 
      * @param key the object to hash
      * @return the hash code for key
      * @throws InvalidClassException Something is wrong with a class used by
      * serialization.
      * @throws NotSerializableException Some object to be serialized does not
      * implement the java.io.Serializable interface.
      */
     public static int hash(Serializable key)
         throws InvalidClassException, NotSerializableException {
         if (key == null)
             return 0;
         try {
             Output buffer = new Output();
             ObjectOutputStream out = new ObjectOutputStream(buffer) {
                     // avoid stream header and class descriptor
                 protected void writeStreamHeader() {
                 }
                 protected void writeClassDescriptor(ObjectStreamClass desc) {
                 }
                 public void writeUTF(String str) throws IOException {
                     writeChars(str);
                 }
             };
             out.writeObject(key);
             out.close();
             return buffer.getHash();
         } catch (InvalidClassException ex) {
             throw ex;
         } catch (NotSerializableException ex) {
             throw ex;
         } catch (IOException ex) {
             throw new InvalidClassException(
                 "unexpected exception serializing "
                     + key.getClass() + ": " + ex);
         }
     }
 
     /**
      * Chain two hash values into a new hash value. This is done by hashing
      * the concatenation of the two hash values.
      * 
      * @param h1 first hash value
      * @param h2 second hash value
      * @return combined result
      */
     public static int chain(int h1, int h2) {
         int step = 0;
         int part = 8; // ldc quicker?
         int bits[] = getOTP(part + 255);
         step = roll(step) ^ bits[--part + (h2 & 0xff)];
         step = roll(step) ^ bits[--part + ((h2 >>>= 8) & 0xff)];
         step = roll(step) ^ bits[--part + ((h2 >>>= 8) & 0xff)];
         step = roll(step) ^ bits[--part + (h2 >>> 8)];
         step = roll(step) ^ bits[--part + (h1 & 0xff)];
         step = roll(step) ^ bits[--part + ((h1 >>>= 8) & 0xff)];
         step = roll(step) ^ bits[--part + ((h1 >>>= 8) & 0xff)];
         step = roll(step) ^ bits[--part + (h1 >>> 8)];
         return step;
     }
 
     /**
      * Roll an integer value one bit to the right, moving the least
      * significant bit to the most significant position.
      * 
      * @param step value to roll
      * @return rolled result
      */
     public static int roll(int step) {
         return (step << 31) | (step >>> 1);
     }
 
     /**
      * Copy the OTP into the given <var>buffer</var>. If <var>buffer</var>
      * is <code>null</code>, a suitable buffer will be allocated, and offset
      * will be ignored.
      * 
      * @param buffer
      * @param off
      * @param len
      * @return int[]
      * @return copy of the OTP
      */
     public int[] getOTP(int buffer[], int off, int len) {
         if (buffer == null) {
             buffer = new int[len];
             off = 0;
         } else {
             validateBufferIndex(buffer.length, off, len);
         }
         System.arraycopy(getOTP(len), 0, buffer, off, len);
         return buffer;
     }
 
     /**
      * Get the current size of the OTP.
      * 
      * @return the current size
      */
     public static int getOTPSize() {
         return bits == null ? 0 : bits.length;
     }
 
     /**
      * Reset the cached OTP, reclaming memory and resetting the
      * pseudo-random generator. This is usually only useful for tests
      * or when you know that you hashed some enormous keys and do not
      * want to retain the OTP cache required for the generation.
      */
     public static synchronized void reset() {
         bits = null;
         seed = SEED_INITIAL;
         have = 0;
     }
 
     /**
      * Validate buffer and indices for common ([],off,len) parameters. When
      * used as <code>validateBufferIndex(buffer.length, off, len)</code> it
      * will conveniently throw NullPointerException when the buffer is null.
      * 
      * @param size the buffer size
      * @param off the offset given
      * @param len the length given
      * @throws IndexOutOfBoundsException when off or len index somewhere
      * outside the buffer
      */
     public static void validateBufferIndex(int size, int off, int len)
         throws IndexOutOfBoundsException {
         if (off < 0 || off > size) {
             throw new IndexOutOfBoundsException(
                 "buffer offset: " + size + "<>" + off);
         } else if (len < 0 || off + len > size) {
             throw new IndexOutOfBoundsException(
                 "buffer length: " + size + "<>" + off + "+" + len);
         }
     }
 
     /** Streaming interfacing class to hash. */
     public static class Output extends OutputStream {
         private int step = 0;
         private int scan = 0;
 
         public int getHash() {
             return step;
         }
 
         /**
          * @see java.io.ObjectOutputStream#reset()
          */
         public void reset() throws IOException {
             step = 0;
         }
 
         /**
          * @see java.io.OutputStream#write(byte[], int, int)
          */
         public void write(byte[] b, int off, int len) throws IOException {
             validateBufferIndex(b.length, off, len);
             int bits[] = getOTP(scan + len + 255);
             int end = off + len;
             while (off < end) {
                 System.out.println("byte: 0x" + Integer.toHexString(b[off]));
                 step = roll(step) ^ bits[scan++ + (b[off++] & 0xff)];
             }
         }
 
         /**
          * @see java.io.OutputStream#write(int)
          */
         public void write(int b) {
             int bits[] = getOTP(scan + 255);
             step = roll(step) ^ bits[scan++ + (b & 0xff)];
         }
 
     }
 
     /**
      * Get the OTP with the given <var>minimum size</var>. This method
      * must be kept private because it would expose a reference to the
      * shared OTP array.
      * 
      * @param size minimum size of OTP
      * @return the OTP
      */
     private static int[] getOTP(int size) {
         if (getOTPSize() < size) {
             synchronized (OTPHash.class) {
                 // compute length again in case we lost the
                 // monitor race
                 int n = getOTPSize();
                 if (n < size) {
                     int more = Math.max(size, n > 1 ? n + n / 2 : 12);
                     int grow[] = new int[more];
                     if (n > 0) {
                         // copy old values
                         System.arraycopy(bits, 0, grow, 0, n);
                     }
                     while (n < grow.length) {
                         // create new values
                         grow[n++] = createBits();
                     }
                     bits = grow;
                 }
             }
         }
         return bits;
     }
 
     /**
      * Create some pseudo-random bits. This method must be called
      * synchronized on this class because it uses the internal LCR
      * pseudo-random generator and sequence on calls to that must
      * always be identical or we would risk creating a different OTP.
      * Synchronizing createBits would not help here.
      * 
      * @return the next pseudo-random integer
      */
     private static int createBits() {
         int need = 32;
         int result = 0;
         while (need > 0) {
             if (have == 0) {
                 have = next();
             }
 
             // transition map have into result, see RFC1750.
             int pair = have & 3;
             have >>>= 2;
             switch (pair) {
                 case 0 : // 00 -> discard
                 case 3 : // 11 -> discard
                     // discard
                     continue;
                 case 2 : // 10 -> 1
                     result |= 1;
                     //				case 1: // 01 -> 0
             }
             result <<= 1;
             need -= 1;
         }
         return result;
     }
 
     /**
      * Method next.
      * @return int
      */
     private static int next() {
         seed = (seed * SEED_MULTIPLIER + 11) & 0xffffffffffffL;
         return (int) (seed >>> 16);
     }
 
     /** The multiplier used in the RNG. */
     public static final long SEED_MULTIPLIER = 0x5DEECE66DL;
 
     /** The 48 bit mask for RNG. */
     public static final long SEED_MASK = 0xFFFFFFFFFFFFL;
 
     /** The initial seed used to start the RNG. */
     public static final long SEED_INITIAL =
         (0xBAFFEL ^ SEED_MULTIPLIER) & SEED_MASK;
 
     /** The current state of the RNG. */
     private static long seed = SEED_INITIAL;
 
     /** Remaining bits from the previous random generation. */
     private static int have;
 
     /** Shared OTP of random numbers. */
     private static int bits[];
 }
