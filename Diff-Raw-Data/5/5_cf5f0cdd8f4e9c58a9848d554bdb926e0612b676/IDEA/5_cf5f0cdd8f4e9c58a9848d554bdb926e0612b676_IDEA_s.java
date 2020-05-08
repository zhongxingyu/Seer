 /*
  * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
  * Studienarbeit am Institut für Theoretische Informatik der
  * Technischen Universität Braunschweig
  * 
  * Datei:        IDEA.java
  * Beschreibung: Dummy-Implementierung des International Data Encryption
  *               Algorithm (IDEA)
  * Erstellt:     30. März 2010
  * Autor:        Martin Klußmann
  */
 
 package task3;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.math.BigInteger;
 import java.nio.*;
 import java.util.*;
 
 import de.tubs.cs.iti.jcrypt.chiffre.BlockCipher;
 
 /**
  * Dummy-Klasse für den International Data Encryption Algorithm (IDEA).
  *
  * @author Martin Klußmann
  * @version 1.1 - Sat Apr 03 21:57:35 CEST 2010
  */
 public final class IDEA extends BlockCipher {
 
     /// a bigint with first 128 bits set, needed for some bitwise trickery in idea_subkeys
     protected final static BigInteger _128bits = BigInteger.valueOf(0L).setBit(128).subtract(BigInteger.ONE);
 
     int[] keys_enc, keys_dec;
 
     /** generate subkeys.
      *
      * The first eight sub-keys are extracted directly from the key, with
      * K1 from the first round being the lower sixteen bits; further groups
      * of eight keys are created by rotating the main key left 25 bits
      * between each group of eight.
      *
      * @param key 128 bit BigInteger
      * @return an array of 52 sequential subkeys
      *
      */
     public static int[] idea_subkeys(BigInteger bI) {
         // there should be no bits set after the 128th! this is basically key.length == 16
         assert(bI.and(_128bits).equals(bI));
 
         // allocate buffer space
         int[] encryptKeys = new int[52];
         byte[] key = bI.setBit(128).toByteArray();
 
         // Encryption keys.  The first 8 key values come from the 16
         // user-supplied key bytes.
         int k1;
 
         // Encryption keys.  The first 8 key values come from the 16
         // user-supplied key bytes.
         for ( k1 = 0; k1 < 8; ++k1 )
             encryptKeys[k1] =
                 ( ( key[2 * k1 +1] & 0xff ) << 8 ) | ( key[ 2 * k1 +2] & 0xff );
 
         // Subsequent key values are the previous values rotated to the
         // left by 25 bits.
 
         for ( ; k1 < 52; ++k1 )
             encryptKeys[k1] =
                 ( ( encryptKeys[k1 - 8] << 9 ) |
                   ( encryptKeys[k1 - 7] >>> 7 ) ) & 0xffff;
 
         return encryptKeys;
     }
 
     /** generate decryption keys.
      *
      * implementation concept: http://www.quadibloc.com/crypto/co040302.htm
      *
      * @param keys_enc 52 subkeys to generate decryption keys for
      * @return an array of 52 sequential decryption subkeys
      *
      */
     public static int[] idea_deckeys(int[] keys_enc) {
 
         BigInteger addMod = BigInteger.valueOf(65536L);
         BigInteger multMod = BigInteger.valueOf(65537L);
 
         // allocate buffer space
         IntBuffer buf = IntBuffer.allocate(52);
 
         /*
             The first four subkeys for decryption are:
 
             KD(1) = 1/K(49)
             KD(2) =  -K(50)
             KD(3) =  -K(51)
             KD(4) = 1/K(52)
         */
         buf.put(BigInteger.valueOf(keys_enc[48]).modInverse(multMod).intValue());
         buf.put(-keys_enc[49] & 0xffff);
         buf.put(-keys_enc[50] & 0xffff);
         buf.put(BigInteger.valueOf(keys_enc[51]).modInverse(multMod).intValue());
 
         /*
             The following is repeated eight times, adding 6 to every decryption key's index and subtracting 6 from every encryption key's index:
         */
         for(int i = 0; i < 48; i+=6) {
 
             /*
                 KD(5)  =   K(47)
                 KD(6)  =   K(48)
             */
             buf.put(keys_enc[46 -i]);
             buf.put(keys_enc[47 -i]);
 
             /*
                 KD(7)  = 1/K(43)
                 KD(8)  =  -K(45)
                 KD(9)  =  -K(44)
                 KD(10) = 1/K(46)
             */
             buf.put(BigInteger.valueOf(keys_enc[42 -i]).modInverse(multMod).intValue());
             buf.put(-keys_enc[44 -i] & 0xffff);
             buf.put(-keys_enc[43 -i] & 0xffff);
             buf.put(BigInteger.valueOf(keys_enc[47 -i]).modInverse(multMod).intValue());
 
         }
 
         // there should be no bytes left for writing!
         assert(buf.remaining() == 0);
 
         // ok, get back to beginning, and write first 104 bytes into an array of 52 ints
         int[] ret = new int[52];
         buf.flip();
         buf.get(ret, 0, 52);
 
         return ret;
 
     }
 
     public static void mainx(String[] args) {
         byte[] key = new byte[] { (byte) 0x42, (byte) 0x61, (byte) 0xce, (byte) 0xd1, (byte) 0xff, (byte) 0x55, (byte) 0xff, (byte) 0x1d,
                                   (byte) 0xf2, (byte) 0x12, (byte) 0xfc, (byte) 0xfa, (byte) 0xaa, (byte) 0xff, (byte) 0x91, (byte) 0xff };
 
         assert(args.length == 1 && args[0].length() == 16);
         int[] subkeys = IDEA.idea_subkeys(new BigInteger(key)); //args[0].getBytes()));
         int[] deckeys = IDEA.idea_deckeys(subkeys); //args[0].getBytes()));
 
         for(int i = 0; i < subkeys.length; i++) {
             System.out.println(String.format("%04x %04x", subkeys[i], deckeys[i]));
         }
 
     }
 
     /** One block of IDEA, consisting of 8.5 rounds of IDEA.
      *
      * @param in 64 bits of input to encrypt. may be altered!
      * @param out 64 bits of output
      * @param key 52 subkeys
      *
      */
     public static void idea_block(int[] in, int[] out, int[] subkeys) {
         assert(in.length == 4 && out.length == 4);
         assert(subkeys.length == 52);
 
         // 8 idea rounds
         for(int i = 0; i < 8; i+=2) {
             // swap around in/out. note that the "in" array is used for temp
             // values and thus destroyed in the process.
             idea_round(in, out, subkeys, (i+0)*6);
             idea_round(out, in, subkeys, (i+1)*6);
         }
 
         // 1 idea half-round
         idea_halfround(in, out, subkeys, 48);
 
     }
 
     /** One round of IDEA.
      * @param in 64 bits of input to encrypt. may be altered!
      * @param out 64 bits of output
      * @param key 96 bit key
      */
     public static void idea_round(int[] in, int[] out, int[] key, int key_offset) {
         assert(in.length == 4 && out.length == 4);
         assert(key.length >= key_offset +6);
 
         // first layer
         out[0] = in[0] * key[key_offset+0] & 0xffff;
         out[1] = in[1] + key[key_offset+1] & 0xffff;
         out[2] = in[2] + key[key_offset+2] & 0xffff;
         out[3] = in[3] * key[key_offset+3] & 0xffff;
 
         // intermediate values
         in[0] =  (out[0] ^ out[2]) * key[key_offset+4] & 0xffff;
         in[1] =  (out[1] ^ out[3]) + in[0] & 0xffff;
         in[2] = in[1] + key[key_offset+5] & 0xffff;
         in[3] = in[0] + in[2] & 0xffff;
 
         // bottom xor-layer
         out[0] = out[0] ^ in[2];
         out[1] = out[2] ^ in[2];
         out[2] = out[1] ^ in[3];
         out[3] = out[3] ^ in[3];
 
     }
 
     /** One half-round of IDEA.
      * @param in 64 bits of input to encrypt. may be altered!
      * @param out 64 bits of output
      * @param key 64 bit key
      */
     public static void idea_halfround(int[] in, int[] out, int[] key, int key_offset) {
         assert(in.length == 4 && out.length == 4);
         assert(key.length >= key_offset +4);
 
         // we use our time here to make sure with some assertions that the
         // down-casting does not shave off any of our precision
         out[0] = in[0] * key[key_offset+0] & 0xffff;
         assert(out[0] == (in[0] * key[key_offset+0] & 0xffff));
 
         out[1] = in[2] + key[key_offset+1] & 0xffff;
         assert(out[1] == (in[2] + key[key_offset+1] & 0xffff));
 
         out[2] = in[1] + key[key_offset+2] & 0xffff;
         assert(out[2] == (in[1] + key[key_offset+2] & 0xffff));
 
         out[3] = in[3] * key[key_offset+3] & 0xffff;
         assert(out[3] == (in[3] * key[key_offset+3] & 0xffff));
 
     }
 
     /**
      * Entschlüsselt den durch den FileInputStream <code>ciphertext</code>
      * gegebenen Chiffretext und schreibt den Klartext in den FileOutputStream
      * <code>cleartext</code>.
      *
      * @param ciphertext
      * Der FileInputStream, der den Chiffretext liefert.
      * @param cleartext
      * Der FileOutputStream, in den der Klartext geschrieben werden soll.
      */
     public void decipher(FileInputStream ciphertext, FileOutputStream cleartext) {
 
         try {
 
             // buffer for 8 bytes at a time
             byte[] block_byte = new byte[8];
             // current ciphertext block (to be encrypted)
             int[] block_int = new int[4];
             // lastCipherBlock for CBC, starting with IV
             int[] block_last = new int[4];
 
             // read IV from first block
             ciphertext.read(block_byte);
             convertByteArrayToShortIntArray(block_byte, block_last);
 
             int bytes_read;
 
             while ( (bytes_read = ciphertext.read(block_byte)) > 0) {
                 assert(bytes_read == 8);
 
                 // convert to ints of 16 bits each
                 convertByteArrayToShortIntArray(block_byte, block_int);
 
                 // CBC: xor with last block
                 for (int i = 0; i < 4; i++) {
                     block_int[i] ^= block_last[i];
                 }
 
                 // encrypt block with IDEA
                 idea_block(block_int, block_last, this.keys_dec);
 
                 convertShortIntArrayToByteArray(block_last, block_byte);
 
                 // write to output
                 cleartext.write(block_byte);
 
             }
         } catch (IOException e) {
             System.out
                     .println("Encipher failed, could not read cleartext or write ciphertext.");
             e.printStackTrace();
         }
     }
 
     
     private static void convertByteArrayToShortIntArray(byte[] in, int[] out) {
         assert(in.length % 2 == 0);
        assert(in.length / 2 == out.length);
         for (int i = 0; i < in.length; i+=2) {
             out[i/2] = (in[i] << 8) | in[i+1];
         }
     }
     
     private static void convertShortIntArrayToByteArray (int[] in, byte[] out) {
        assert(in.length == out.length / 2);
         for (int i = 0; i < in.length; i+=2) {
             out[i] = (byte) (in[i/2] >> 8);
             out[i+1] = (byte) (in[i/2]);
         }
     }
     
     /**
      * Verschlüsselt den durch den FileInputStream <code>cleartext</code>
      * gegebenen Klartext und schreibt den Chiffretext in den FileOutputStream
      * <code>ciphertext</code>.
      * 
      * @param cleartext
      * Der FileInputStream, der den Klartext liefert.
      * @param ciphertext
      * Der FileOutputStream, in den der Chiffretext geschrieben werden soll.
      */
     public void encipher(FileInputStream cleartext, FileOutputStream ciphertext) {
         try {
             byte[] initVectorBytes = new BigInteger(64, new Random())
                     .toByteArray();
             ciphertext.write(initVectorBytes); // write init vector as 0. block into ciphertext
 
             // buffer for 8 bytes at a time
             byte[] block_byte = new byte[8];
             // current ciphertext block (to be encrypted)
             int[] block_int = new int[4];
             // lastCipherBlock for CBC, starting with IV
             int[] block_last = new int[4];
             convertByteArrayToShortIntArray(initVectorBytes, block_last);
 
             int bytes_read;
 
             while ( (bytes_read = cleartext.read(block_byte)) > 0) {
                 // if there aren't enough bytes, fill with zeroes
                 if(bytes_read < 8)
                     Arrays.fill(block_byte, bytes_read, 8, (byte) 0);
 
                 // convert to ints of 16 bits each
                 convertByteArrayToShortIntArray(block_byte, block_int);
 
                 // CBC: xor with last block
                 for (int i = 0; i < 4; i++) {
                     block_int[i] ^= block_last[i];
                 }
 
                 // encrypt block with IDEA
                 idea_block(block_int, block_last, this.keys_enc);
 
                 convertShortIntArrayToByteArray(block_last, block_byte);
 
                 // write to output
                 ciphertext.write(block_byte);
 
             }
         } catch (IOException e) {
             System.out
                     .println("Encipher failed, could not read cleartext or write ciphertext.");
             e.printStackTrace();
         }
     }
 
     /**
      * Erzeugt einen neuen Schlüssel.
      * 
      * @see #readKey readKey
      * @see #writeKey writeKey
      */
     public void makeKey() {
 
         byte[] key = new byte[] { (byte) 0x42, (byte) 0x61, (byte) 0xce, (byte) 0xd1, (byte) 0xff, (byte) 0x55, (byte) 0xff, (byte) 0x1d,
                                   (byte) 0xf2, (byte) 0x12, (byte) 0xfc, (byte) 0xfa, (byte) 0xaa, (byte) 0xff, (byte) 0x91, (byte) 0xff };
 
         keys_enc = idea_subkeys(new BigInteger(key));
         keys_dec = idea_deckeys(keys_enc);
 
     }
 
     /**
      * Liest den Schlüssel mit dem Reader <code>key</code>.
      * 
      * @param key
      * Der Reader, der aus der Schlüsseldatei liest.
      * @see #makeKey makeKey
      * @see #writeKey writeKey
      */
     public void readKey(BufferedReader key) {
 
     }
 
     /**
      * Schreibt den Schlüssel mit dem Writer <code>key</code>.
      * 
      * @param key
      * Der Writer, der in die Schlüsseldatei schreibt.
      * @see #makeKey makeKey
      * @see #readKey readKey
      */
     public void writeKey(BufferedWriter key) {
 
     }
     
     public static void main(String[] args) throws IOException {
         FileInputStream input = new FileInputStream(args[1]);
         FileOutputStream output = new FileOutputStream(args[2]);
         IDEA v = new IDEA();
         v.makeKey();
 
         if (args[0].equals("encipher")) {
             v.encipher(input, output);
             return;
         }
         else if (args[0].equals("decipher")) {
             v.decipher(input, output);
             return;
         }
         else {
                 System.out.println("Usage: $0 encipher|decipher infile outfile");
         }
 
     }
 }
