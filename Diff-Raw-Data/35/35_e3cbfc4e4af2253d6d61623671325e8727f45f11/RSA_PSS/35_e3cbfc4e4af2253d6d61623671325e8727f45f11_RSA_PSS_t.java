 package com.cyanoryx.uni.crypto.rsa;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.zip.DataFormatException;
 
 import com.cyanoryx.uni.common.Bytes;
 
 /**
  * Java implementation of the EMSA-PSS digital signature standard
  * See <a href="http://tools.ietf.org/html/rfc3447">RFC3447</a> for more information
  * 
  * @author adammulligan
  *
  */
 public class RSA_PSS {
   /**
    * Exponent E (priv or pub), modulus N
    */
   private BigInteger E,N;
   
   private Key key;
   
   private MessageDigest md;
 
   private int emBits;
   private int emLen;
   
   public RSA_PSS(Key key) {
     this.key = key;
     
     this.E = this.key.getExponent();
     this.N = this.key.getN();
     
     this.emBits = this.N.bitLength() - 1;
     this.emLen = (this.emBits + 7) / 8;
     
     try {
       this.md = MessageDigest.getInstance("sha1");
     } catch (NoSuchAlgorithmException e1) {
       e1.printStackTrace();
     }
   }
   
   /**
    * RSASSA-PSS-SIGN
    * 
    * @param M Message to be signed
    * @return S Signature octet string
    * @throws DataFormatException
    */
   public byte[] sign(byte[] M) throws DataFormatException {
     /*
      *  1. EMSA-PSS encoding: Apply the EMSA-PSS encoding operation (Section
      *     9.1.1) to the message M to produce an encoded message EM of length
      *     \ceil ((modBits - 1)/8) octets such that the bit length of the
      *     integer OS2IP (EM) (see Section 4.2) is at most modBits - 1, where
      *     modBits is the length in bits of the RSA modulus n:
      *     
      *         EM = EMSA-PSS-ENCODE (M, modBits - 1).
      */
     byte[] EM = EMSA_PSS_ENCODE(M);
     
     /*
      * 2. RSA signature:
      *
      *     a. Convert the encoded message EM to an integer message
      *        representative m (see Section 4.2):
      *
      *           m = OS2IP (EM).
      */
     BigInteger m = new BigInteger(1,EM);
     
     /*
      * b. Apply the RSASP1 signature primitive (Section 5.2.1) to the RSA
      *    private key K and the message representative m to produce an
      *    integer signature representative s:
      *    
      *        s = RSASP1 (K, m).
      */
     BigInteger s = RSASP1(m);
     
     /*
      * c. Convert the signature representative s to a signature S of
      *    length k octets (see Section 4.1):
      *    
      *        S = I2OSP (s, k).
      */
     byte[] S = Bytes.toFixedLenByteArray(s,this.emBits);
     
     
     /*
      * 3. Output the signature S.
      */
     return S;
   }
   
   /**
    * RSASSA-PSS-VERIFY
    * 
    * @param M Message to be verified
    * @param S Signature of message to be verified
    * @return boolean If signature is valid or not
    * @throws DataFormatException
    */
   public boolean verify(byte[] M, byte[] S) throws DataFormatException {
     /*
      * 2. RSA verification:
      *
      *    a. Convert the signature S to an integer signature representative
      *       s (see Section 4.2):
      *       
      *          s = OS2IP (S).
      */
     BigInteger s = new BigInteger(1,S);
     
     /*
      * b. Apply the RSAVP1 verification primitive (Section 5.2.2) to the
      *    RSA public key (n, e) and the signature representative s to
      *    produce an integer message representative m:
      *    
      *        m = RSAVP1 ((n, e), s).
      */
     BigInteger m = RSAVP1(s);
     
     /*
      * c. Convert the message representative m to an encoded message EM
      *    of length emLen = \ceil ((modBits - 1)/8) octets, where modBits
      *    is the length in bits of the RSA modulus n (see Section 4.1):
      *    
      *        EM = I2OSP (m, emLen).
      */
     byte[] EM = Bytes.toFixedLenByteArray(m,this.emLen);
     
     /*
      * 3. EMSA-PSS verification: Apply the EMSA-PSS verification operation
      *    (Section 9.1.2) to the message M and the encoded message EM to
      *    determine whether they are consistent:
      *    
      *        Result = EMSA-PSS-VERIFY (M, EM, modBits - 1).
      */
     return EMSA_PSS_VERIFY(M,EM);
   }
   
   /**
    * EMSA-PSS-ENCODE
    * 
    * @param M Message octet string to encode
    * @return EM encoded octet string message
    * @throws DataFormatException
    */
   private byte[] EMSA_PSS_ENCODE(byte[] M) throws DataFormatException {
     /*
      * 1.  If the length of M is greater than the input limitation for the
      *     hash function (2^61 - 1 octets for SHA-1), output "message too
      *     long" and stop.
      */
     if (M.length > this.md.getDigestLength()) {
 //      System.out.println(M.length);
 //      System.out.println(this.md.getDigestLength());
       throw new DataFormatException("Message too long");
     }
     
     /*
      * 2.  Let mHash = Hash(M), an octet string of length hLen.
      */
     this.md.update(M);
     byte[] mHash = this.md.digest();
     
     /*
      * 3.  If emLen < hLen + sLen + 2, output "encoding error" and stop.
      */
     if (emLen < this.md.getDigestLength()*2 + 2) {
       throw new InternalError("Encoding error");
     }
     
     /*
      * 4.  Generate a random octet string salt of length sLen; if sLen = 0,
      *     then salt is the empty string.
      */
     byte[] salt = new byte[this.md.getDigestLength()];
     SecureRandom rng = new SecureRandom();
     rng.nextBytes(salt);
     
     /*
      * 5.  Let
      *       M' = (0x)00 00 00 00 00 00 00 00 || mHash || salt;
      *
      *    M' is an octet string of length 8 + hLen + sLen with eight
      *    initial zero octets.
      */
     this.md.update(new byte[8]); // Eight zero-octets
     this.md.update(mHash);
     
     /*
      * 6.  Let H = Hash(M'), an octet string of length hLen.
      */
     byte[] H = this.md.digest(salt);
     
     /*
      * 7.  Generate an octet string PS consisting of emLen - sLen - hLen - 2
      *     zero octets.  The length of PS may be 0.
      */
     byte[] PS = new byte[emLen - (this.md.getDigestLength()*2) - 2];
     
     /*
      * 8.  Let DB = PS || 0x01 || salt; DB is an octet string of length
      *     emLen - hLen - 1.
      */
     byte[] DB = Bytes.concat(PS, new byte[]{0x01}, salt);
     
     /*
      * 9.  Let dbMask = MGF(H, emLen - hLen - 1).
      */
     MGF1 mgf1 = new MGF1(this.md);
     byte[] dbMask = mgf1.generateMask(H, emLen - this.md.getDigestLength() - 1);
     
     /*
      * 10. Let maskedDB = DB \xor dbMask.
      */
     byte[] maskedDB = Bytes.xor(DB, dbMask);
     
     /*
      * 11. Set the leftmost 8emLen - emBits bits of the leftmost octet in
      *     maskedDB to zero.
      */
     byte[] MASK = {(byte)0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03, 0x01};
     int maskBits = 8*emLen - emBits;
     maskedDB[0] &= MASK[maskBits];
     
     /*
      * 12. Let EM = maskedDB || H || 0xbc.
      */
     byte[] EM = Bytes.concat(maskedDB,H,new byte[]{ (byte)0xbc });
     
     return EM;
   }
   
   /**
    * EMSA-PSS-VERIFY
    * 
    * @param M octet string message to verify
    * @param EM encoded octet string
    * @return boolean If signature matches message
    */
   private boolean EMSA_PSS_VERIFY(byte[] M, byte[] EM) {
     /*
      * 1.  If the length of M is greater than the input limitation for the
      *     hash function (2^61 - 1 octets for SHA-1), output "inconsistent"
      *     and stop.
      */
     if (M.length > this.md.getDigestLength()) {
       return false;
     }
 
     /*
      * 2.  Let mHash = Hash(M), an octet string of length hLen.
      */
     this.md.update(M);
     byte[] mHash = this.md.digest();
     
     /*
      * 3.  If emLen < hLen + sLen + 2, output "inconsistent" and stop.
      */
     if (emLen < this.md.getDigestLength()*2 + 2) {
       return false;
     }
     
     /*
      * 4.  If the rightmost octet of EM does not have hexadecimal value
      *     0xbc, output "inconsistent" and stop.
      */
     if (EM[EM.length-1] != (byte)0xbc) {
       return false;
     }
     
     /*
      * 5.  Let maskedDB be the leftmost emLen - hLen - 1 octets of EM, and
      *     let H be the next hLen octets.
      */
     byte[] maskedDB = new byte[emLen - this.md.getDigestLength() - 1];
     System.arraycopy(EM, 0, maskedDB, 0, emLen - this.md.getDigestLength() - 1);
     
     /*
      * 6.  If the leftmost 8emLen - emBits bits of the leftmost octet in
      *     maskedDB are not all equal to zero, output "inconsistent" and
      *     stop.
      */
     byte[] H = new byte[this.md.getDigestLength()];
     System.arraycopy(EM, maskedDB.length, H, 0, this.md.getDigestLength());
     
     byte[] MASK = {(byte)0xFF, 0x7F, 0x3F, 0x1F, 0x0F, 0x07, 0x03, 0x01};
     if ((maskedDB[0] & ~MASK[8*emLen - emBits]) != 0) {
       return false;
     }
     
     /*
      * 7.  Let dbMask = MGF(H, emLen - hLen - 1).
      */
     MGF1 mgf1 = new MGF1(this.md);
     byte[] dbMask = mgf1.generateMask(H,emLen - this.md.getDigestLength() - 1);
     
     /*
      * 8.  Let DB = maskedDB \xor dbMask.
      */
     byte[] DB = Bytes.xor(maskedDB, dbMask);
     
     /*
      * 9.  Set the leftmost 8emLen - emBits bits of the leftmost octet in DB
      *     to zero.
      */
     for (int i=0;i<(emLen - (this.md.getDigestLength()*2) - 2);i++) {
       if (DB[i] != 0) {
         return false;
       }
     }
     
     /*
      * 10. If the emLen - hLen - sLen - 2 leftmost octets of DB are not zero
      *     or if the octet at position emLen - hLen - sLen - 1 (the leftmost
      *     position is "position 1") does not have hexadecimal value 0x01,
      *     output "inconsistent" and stop.
      */
     
     if (DB[emLen - (this.md.getDigestLength()*2) - 2] != 0x1) {
       return false;
     }
     
     /*
      * 11.  Let salt be the last sLen octets of DB.
      */
     
     byte[] salt = new byte[this.md.getDigestLength()];
     System.arraycopy(DB, DB.length - this.md.getDigestLength(), salt, 0, this.md.getDigestLength());
     
     /*
      * 12.  Let
          *          M' = (0x)00 00 00 00 00 00 00 00 || mHash || salt ;
          *          
          *      M' is an octet string of length 8 + hLen + sLen with eight
          *      initial zero octets.
      */
     
     this.md.reset();
     this.md.update(new byte[8]);
     this.md.update(mHash);
     
     /*
      * 13. Let H' = Hash(M'), an octet string of length hLen.
      */
     
     byte[] Hdash = this.md.digest(salt);
     
     return Bytes.equals(H, Hdash);
   }
   
   /**
    * RSASP1
    * 
    * Output:
       *  s        signature representative, an integer between 0 and n - 1
       *   
      * Assumption: RSA private key K is valid
    * 
    * @param m Message representative
    * @return s Signature representative
    */
   private BigInteger RSASP1(BigInteger m) {
     // 2. Let s = m^d mod n.
     BigInteger s = m.modPow(E, N);
     return s;
   }
   
   /**
    * RSAVP1
    * 
    * Inverse of RSAVP1
    * 
    * Output:
      *  m        message representative
      *  
       * Assumption: RSA public key (n, e) is valid
    * 
    * @param s - Signature representative
    * @return m Message representative
    */
   private BigInteger RSAVP1(BigInteger s) {
     // 2. Let m = s^e mod n.
     return this.RSASP1(s);
   }
 }
