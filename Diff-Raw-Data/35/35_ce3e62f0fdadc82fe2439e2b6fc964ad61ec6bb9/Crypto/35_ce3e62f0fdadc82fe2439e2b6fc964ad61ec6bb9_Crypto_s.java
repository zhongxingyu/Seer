 package com.ringlord.crypto;
 
 import java.io.CharArrayWriter;
 import java.io.UnsupportedEncodingException;
 
 import java.security.MessageDigest;
 import java.security.Key;
 import java.security.NoSuchAlgorithmException;
 import java.security.InvalidKeyException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.NoSuchAlgorithmException;
 import java.security.GeneralSecurityException;
 import java.security.spec.InvalidKeySpecException;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.SecretKey;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Mac;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import java.util.Map;
 
 import com.ringlord.mime.Base64;
 
 // ======================================================================
 // This file is part of the Ringlord Technologies Java ODF Library,
 // which provides access to the contents of OASIS ODF containers,
 // including encrypted contents.
 //
 // Copyright (C) 2012 K. Udo Schuermann
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>.
 // ======================================================================
 
 /**
  * <p>Encrypt/decrypt data using a password. Although this class is
  * intended to be flexible, it is presently limited to use the PBKDF2
  * password-based key derivative function for key strengthening and
  * is not prepared to adjust to other algorithms.</p>
  *
  * <p>This limitation is entirely due to the fact that the Java crypto
  * libraries require a password-based keys to be built from UTF-8
  * encoded passwords rather than binary and we had to supply a
  * binary-capable implementation of our own; although it could be
  * ascribed to laziness, we lack the requisite knowledge to integrate
  * this class into the Java crypto framework.</p>
  *
  * <p>NOTE: Although I have spent a fair amount of time working with
  * and researching these topics and <em>believe that I understand the
  * concepts sufficiently</em>, I am no cryptographer and there may be
  * inaccuracies in the various explanations given in this class, even
  * blatant ones that could mislead your own understanding of the
  * concepts. I urge you not to rely blindly on this class to build
  * your own understanding of cryptographical concepts, but do your own
  * reseach. It's really brilliant fun once you realize how it all fits
  * together!</p>
  *
  * @author K. Udo Schuermann
  **/
 public class Crypto
 {
   // ======================================================================
   // Constructors
   // ======================================================================
 
   /**
    * <p>Constructs a Crypto object using four groups of key/value
    * pairs based directly on the XML element names and attributes
    * found in the OASIS Open Document Format specification v1.2's
    * "META-INF/manifest.xml" file inside the ODF container.</p>
    *
    * <p>This form of the constructor requires virtually no
    * pre-processing from the data found in the container's
    * manifest.</p>
    *
    * <p>Note that none of the parameters can be null, but the older
    * OASIS ODF specification v1.0/v1.1 does not name some of the
    * values that the v1.2 specification does. Missing values will,
    * under specific circumstances, be given assumed defaults (such as
    * the implied use of "SHA1" to start key strengthening).
    *
    * @param encryptionData Originating from a
    * "manifest:encryption-data" element (inside "manifest:file-entry")
    * with "manifest:checksum-type", and a Base64-encoded value for the
    * "manifest:checksum" key. Note that if the checksum and/or the
    * checksum-type are null, verification of the decryption process
    * will not be performed, which prevents detection of the wrong
    * password.
    *
    * @param keyDerivationData Originating from a
    * "manifest:key-derivation" element (found inside the XML element
    * "manifest:encryption-data", see previous) with
    * "manifest:key-derivation-name", "manifest:key-size",
    * "manifest:iteration-count, and a Base64-encoded value for the
    * "manifest:salt".
    *
    * @param algorithmData Originating from a "manifest:algorithm"
    * element (found inside the XML element "manifest:encryption-data")
    * with "manifest:algorithm-name" and a Base64-encoded value for the
    * "manifest:initialisation-vector".
    *
    * @param startKeyGenerationData Originating from a
    * "manifest:start-key-generation" element (found inside the XML
    * element "manifest:encryption-data") with
    * "manifst:start-key-generation-name", and "manifest:key-size".
    *
    * @throws NoSuchAlgorithmException The indicated encryption
    * algorithm is not supported.
    *
   * @param NoSuchPaddingException The indicated algorithm does not
    * support the requested padding.
    **/
   public Crypto( final Map<String,String> encryptionData,
                  final Map<String,String> keyDerivationData,
                  final Map<String,String> algorithmData,
                  final Map<String,String> startKeyGenerationData )
     throws NoSuchAlgorithmException,
            NoSuchPaddingException
   {
     super();
 
     this.checksumType  = encryptionData.get( "manifest:checksum-type" );
     this.checksum      = toByteArray( encryptionData.get("manifest:checksum") );
 
     this.algorithmName = algorithmData.get( "manifest:algorithm-name" );
     this.initVector    = toByteArray( algorithmData.get("manifest:initialisation-vector") );
 
 
     this.keyDerivName  = keyDerivationData.get( "manifest:key-derivation-name" );
     if( keyDerivationData.get("manifest:key-size") == null )
       {
         this.keyDerivSize  = 16;
       }
     else
       {
         this.keyDerivSize  = toInt( keyDerivationData.get("manifest:key-size") );
       }
     this.keyDerivIter  = toInt( keyDerivationData.get("manifest:iteration-count") );
     this.salt          = toByteArray( keyDerivationData.get("manifest:salt") );
 
     if( startKeyGenerationData == null )
       {
         this.startKeyGen   = "SHA1";
         this.startKeySize  = 20;
       }
     else
       {
         this.startKeyGen   = startKeyGenerationData.get( "manifest:start-key-generation-name" );
         this.startKeySize  = toInt( startKeyGenerationData.get("manifest:key-size") );
       }
 
     init();
   }
 
   /**
    * <p>Constructs a Crypto object using direct parameters that have
    * been somewhat pre-processed (compared to the {@linkplain
    * #Crypto(Map,Map,Map,Map) other constructor}).</p>
    *
    * <p>All parameter values are given in essentially the form found
    * in the OASIS Open Document Format v1.2 container's
    * META-INF/manifest.xml file (with Base64-encoded binary values
    * pre-transformed into their original binary, and numeric values
    * pre-parsed into actual integers).</p>
    *
    * @param checksumType The name of the checksum algorithm. The
    * checksum is usually an SHA variant covering the first 1024 (1K)
    * bytes of the encrypted document. If checksum matches then the
    * correct key/password were supplied; with the wrong key/password
    * the result would be garbage. Note that if the checksumType is
    * null, verification of the decryption process will not be
    * possible.
    *
    * @param checksum The checksum bytes as produced by a {@link
    * MessageDigest}. Note that if the checksum is null, verification
    * of the decryption process will not be possible.
    *
    * @param algorithmName The algorithm to be used for decryption.  In
    * the v1.0/v1.1 specification this was 128-bit "Blowfish&nbsp;CFB"
    * but in the v1.2 specification this has changed to a URL-based
    * name that references the 256-bit AES algorithm with CBC. As a
    * convenience the given name will be transformed, if recognized as
    * one used by the OASIS ODF specification, to one that maps to the
    * Java Crypto libraries, but you may provide them directly as
    * "Blowfish/CFB/NoPadding" or "AES/CBC/NoPadding", or any other
    * form that may be needed to support future changes to the ODF
    * specification.
    *
    * @param initVector The encryption algorithm's Initialisation
    * Vector. Its purpose is to pre-seed the encryption algorithm to
    * increase dramatically the size of pre-computed attack
    * dictionaries.
    *
    * @param keyDerivationAlgorithmName The name of the encryption key
    * derivation algorithm. At this time it is "PBKDF2" ("Password
    * Based Key Derivation Function v2"), whose purpose is to munge a
    * cryptographically rather weak human-readable password in such a
    * way that the final result is not merely limited to about 40 or 50
    * common symbols (letters, digits, and a few specials) but forms a
    * good spread across binary characters not even found on keyboards.
    * The algorithm "strengthens" a password to limit the effectiveness
    * of pattern detections in the encrypted data. PBKDF2, for example,
    * uses a certain number of iterations of SHA-1. The actual value
    * that "PBKDF2" actually translates to is "PBKDF2WithHmacSHA1" and
    * you could supply that full name (or another as appropriate)
    * directly.
    *
    * @param keyDerivationSize The size (in bytes) of the derived key.
    *
    * @param keyDerivationIterationCount How many times the password
    * based key derivation algorithms (such as PBKDF2) should iterate.
    *
    * @param salt The salt is used to pre-seed the cryptographic key
    * data to reduce dictionary-based attacks on the key itself.
    *
    * @param startKeyGenerationName The algorithm used to pre-munge the
    * password before it is given to the password based key
    * strengthening algorithm (such as PBKDF2). This tends to be SHA-1
    * or SHA-256, but any other digest-based algorithm could serve this
    * purpose, at least in theory.
    *
    * @param startKeySize The size of the initial key; this value is
    * not used directly but should reflect the size of the digest
    * produced by the algorithm given by the 'startKeyGenerationName'
    * parameter.
    *
    * @throws NoSuchAlgorithmException The indicated encryption
    * algorithm is not supported.
    *
   * @param NoSuchPaddingException The indicated algorithm does not
    * support the requested padding.
    **/
   public Crypto( final String checksumType,
                  final byte[] checksum,
                  final String algorithmName,
                  final byte[] initVector,
                  final String keyDerivationAlgorithmName,
                  final int keyDerivationSize,
                  final int keyDerivationIterationCount,
                  final byte[] salt,
                  final String startKeyGenerationName,
                  final int startKeySize )
     throws NoSuchAlgorithmException,
            NoSuchPaddingException
   {
     super();
 
     this.checksumType  = checksumType;
     this.checksum      = checksum;
 
     this.algorithmName = algorithmName;
     this.initVector    = initVector;
 
     this.keyDerivName  = keyDerivationAlgorithmName;
     this.keyDerivSize  = keyDerivationSize;
     this.keyDerivIter  = keyDerivationIterationCount;
     this.salt          = salt;
 
     this.startKeyGen   = startKeyGenerationName;
     this.startKeySize  = startKeySize;
 
     init();
   }
 
   // ======================================================================
   // Public Methods
   // ======================================================================
 
   /**
    * <p>Encrypts the given plain text (the stuff you want to hide)
    * using a String-based password (likely entered by a human) which
    * is first strengthened to build a real cryptographic key.</p>
    *
    * @param plainText The data that is to be encrypted; even though it
    * is referred to as plain <em>text</em> it is not necessarily mere
    * text, but can be any kind of (binary) data (like audio, pictures,
    * video, etc.) If this is intended to represent readable text, be
    * sure to use a reliable encoding, such as UTF-8 to produce the
    * plainText data.
    *
    * @param password The password to serve as a basis for the
    * cryptographic key. This is strengthened first because plain text
    * rarely makes for good cryptographic text.
    *
    * @return The cipher text (despite the word <em>text</em> this is
    * binary data). Passing this to {@link #decrypt(byte[],String)}
    * with the same password should reproduce the original data.
    *
    * @see #encrypt(byte[],Key)
    **/
   public byte[] encrypt( final byte[] plainText,
                          final String password )
     throws InvalidKeyException,
            InvalidKeySpecException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException
   {
     final Key key = makeKey( password );
     return encrypt( plainText, key );
   }
 
   /**
    * <p>Encrypts the given plain text (the stuff you want to hide)
    * using a given cryptographic key.</p>
    *
    * @param plainText The data that is to be encrypted; even though it
    * is referred to as plain <em>text</em> it is not necessarily mere
    * text, but can be any kind of (binary) data (like audio, pictures,
    * video, etc.) If this is intended to represent readable text, be
    * sure to use a reliable encoding, such as UTF-8 to produce the
    * plainText data.
    *
    * @param key The cryptographic key to be used.
    *
    * @return The cipher text (despite the word <em>text</em> this is
    * binary data). Passing this to {@link #decrypt(byte[],String)}
    * with the same password should reproduce the original data.
    *
    * @see #encrypt(byte[],String)
    **/
   public byte[] encrypt( final byte[] plainText,
                          final Key key )
     throws InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException
   {
     final IvParameterSpec iv = new IvParameterSpec( initVector );
     cipher.init( Cipher.ENCRYPT_MODE, key, iv );
     return cipher.doFinal( plainText );
   }
 
   /**
    * <p>Decrypts the given cipher text using a String-based password
    * (likely entered by a human) which is first strengthened to build
    * a real cryptographic key.</p>
    *
    * @param cipherText The encrypted data (despite the word
    * <em>text</em> this is binary data).
    *
    * @param password The password to serve as a basis for the
    * cryptographic key. This is strengthened first because plain text
    * rarely makes for good cryptographic text.
    *
    * @return The plain text (despite the word <em>text</em> this may
    * well be binary data) representing the original data.
    *
   * @see #decrypt(byte[],Key,byte[])
    **/
   public byte[] decrypt( final byte[] cipherText,
                          final String password )
     throws InvalidKeyException,
            InvalidKeySpecException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException
   {
     final Key key = makeKey( password );
     return decrypt( cipherText, key );
   }
 
   /**
    * <p>Decrypts the given cipher text using a given cryptographic
    * key.</p>
    *
    * @param cipherText The encrypted data (despite the word
    * <em>text</em> this is binary data).
    *
    * @param key The cryptographic key to be used.
    *
    * @return The plain text (despite the word <em>text</em> this may
    * well be binary data) representing the original data.
    *
    * @throws IllegalArgumentException The given key has failed to
    * decrypt the cipherText (the checksum has failed).
    *
   * @see #decrypt(byte[],Key,String)
    **/
   public byte[] decrypt( final byte[] cipherText,
                          final Key key )
     throws InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException,
            NoSuchAlgorithmException
   {
     final IvParameterSpec iv = new IvParameterSpec( initVector );
     cipher.init( Cipher.DECRYPT_MODE, key, iv );
     return verify( cipher.doFinal(cipherText) );
   }
 
   private byte[] verify( final byte[] result )
     throws NoSuchAlgorithmException
   {
     if( (checksumType != null) &&
         (checksum != null) )
       {
         final MessageDigest digest;
         if( "SHA1/1K".equals(checksumType) )
           {
             digest = MessageDigest.getInstance( "SHA1" );
             digest.update( result, 0, Math.min(1024,result.length) );
           }
         else if( "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0#sha256-1k".equals(checksumType) )
           {
             digest = MessageDigest.getInstance( "SHA-256" );
             digest.update( result, 0, Math.min(1024,result.length) );
           }
         else
           {
             // no checksum available, cannot verify, assume that it's fine
             return result;
           }
 
         final byte[] test = digest.digest();
         for( int i=0; i<checksum.length; i++ )
           {
             if( test[i] != checksum[i] )
               {
                 throw new IllegalArgumentException( "Checksum mismatch (wrong key/password?)" );
               }
           }
       }
 
     return result;
   }
 
   private void showInfo()
   {
     System.err.println( "\tchecksumType   = "+checksumType );
     System.err.println( "\tchecksum.len   = "+checksum.length );
     System.err.println( "\talgorithm      = "+algorithmName );
     System.err.println( "\tinitVector.len = "+initVector.length );
     System.err.println( "\tkeyDrivation   = "+keyDerivName );
     System.err.println( "\tkeyDerivSize   = "+keyDerivSize );
     System.err.println( "\tkeyDerivIter   = "+keyDerivIter );
     System.err.println( "\tsalt.len       = "+salt.length );
     System.err.println( "\tkeyStart       = "+startKeyGen );
     System.err.println( "\tkeyStartSize   = "+startKeySize );
   }
 
   /**
    * <p>Strengthens the given password using an algorithm designed for
    * this purpose. A popular algorithm is often referred to as
    * "PBKDF2" (Password Based Key Derivation Function v2) which is
    * identified in the Java Crypto libraries as
    * "PBKDF2WithHmacSHA1".</p>
    *
    * <p>The idea of strengthening a password is based on the fact that
    * passwords (or pass phrases) make lousy cryptographic keys, not
    * just because they can be far too short, but the total possible
    * spread of symbols employed in each character of a password falls
    * far short of the 256 possibilities embodied in a byte. Password
    * strenghtening like PBKDF2 rely on message digests to generate a
    * digital hash from the password. Running this same algorithm
    * through a number of iterations (like 1024 times) will not just
    * obfuscate the original password, but virtually ensure that the
    * resulting data appears like thoroughly random binary (it's not,
    * but is probably far more suitable for cryptography than the
    * original plain old password).
    *
    * @param password The password, which can be any length. Of couse,
    * the longer it is, the more reasonably secure the resulting key
    * will be.
    *
   * @param The cryptographic (secret) key derived from the password
    * after the password has been strengthened.
    **/
   public Key makeKey( final String password )
     throws InvalidKeyException,
            InvalidKeySpecException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException
   {
     final byte[] sha;
     if( "http://www.w3.org/2000/09/xmldsig#sha256".equals(startKeyGen) )
       {
         sha = toSHA256( password );
       }
     else
       {
         sha = toSHA1( password );
       }
     final byte[] derivedKey = PBKDF2.deriveKey( sha,
                                                 salt,
                                                 keyDerivIter,
                                                 keyDerivSize );
     return new SecretKeySpec( derivedKey,
                               keyAlgorithm );
   }
 
   // ======================================================================
   // Private Methods
   // ======================================================================
 
   /**
    * <p>Translates initial parameters (set in the constructors) so
    * that identifiers like "PBKDF2" becomes "PBKDF2WithHmacSHA1", and
    * "Blowfish&nbsp;CFB" becomes "Blowfish/CFB/NoPadding"; this allows
    * for identifiers from the OASIS ODF container's manifest to be
    * used directly, without the caller being required to perform this
    * translation.</p>
    **/
   private void init()
     throws NoSuchAlgorithmException,
            NoSuchPaddingException
   {
     // ----------------------------------------------------------------------
     // Translate the cipher algorithm from the version used by the ODF
     // container to the one recognized by the Java crypto libraries:
     String algorithmID;
     if( algorithmName.equals("Blowfish CFB") )
       {
         algorithmID = "Blowfish/CFB/NoPadding";
       }
     else if( algorithmName.equals("http://www.w3.org/2001/04/xmlenc#aes256-cbc") )
       {
         algorithmID = "AES/CBC/NoPadding";
       }
     else // not recognized, unsupported, most likely won't work
       {
         algorithmID = algorithmName;
       }
     this.cipher = Cipher.getInstance( algorithmID );
 
     // ----------------------------------------------------------------------
     // Translate the public key derivation algorithm from the version
     // used by the ODF container to version recognized by the Java
     // crypto libraries:
     String keyDerivationID;
     if( keyDerivName.equals("PBKDF2") )
       {
         keyDerivationID = "PBKDF2WithHmacSHA1";
       }
     else // not recognized, unsupported, most likely won't work
       {
         keyDerivationID = keyDerivName;
       }
     this.keyFactory = SecretKeyFactory.getInstance( keyDerivationID );
 
     // ----------------------------------------------------------------------
     // A cipher specification is either "Name" or "Name/Mode/Padding",
     // so we'll look for the first '/' symbol and, if present, take
     // the name that precedes it or the whole thing if there is no '/'
     // for the name of the secret key algorithm:
     final int slashPos = algorithmID.indexOf("/");
     this.keyAlgorithm = (slashPos < 0
                          ? algorithmID
                          : algorithmID.substring(0,slashPos));
   }
 
   /**
    * Parses Base64-encoded data back into its binary form.
    **/
   private static byte[] toByteArray( final String base64Data )
   {
     return Base64.decode( base64Data.getBytes() );
   }
   /**
    * Parses a textual representation of a number into an integer.
    **/
   private static int toInt( final String text )
   {
     return Integer.valueOf( text );
   }
   /**
    * Translates the given password into a UTF8-based set of bytes,
    * then produces from these the digest bytes based on SHA-256.
    **/
   private static byte[] toSHA256( final String password )
     throws NoSuchAlgorithmException,
            UnsupportedEncodingException
   {
     final byte[] passwordCharacters = password.getBytes( "UTF-8" );
 
     final MessageDigest sha256Digester = MessageDigest.getInstance( "SHA-256" );
     return sha256Digester.digest( passwordCharacters );
   }
   /**
    * Translates the given password into a UTF8-based set of bytes,
    * then produces from these the digest bytes based on SHA-1.
    **/
   private static byte[] toSHA1( final String password )
     throws NoSuchAlgorithmException,
            UnsupportedEncodingException
   {
     final byte[] passwordCharacters = password.getBytes( "UTF-8" );
 
     final MessageDigest sha1Digester = MessageDigest.getInstance( "SHA1" );
     return sha1Digester.digest( passwordCharacters );
   }
 
   // ======================================================================
   // Fields and Constants
   // ======================================================================
 
   private Cipher cipher;
   private SecretKeyFactory keyFactory;
   private String keyAlgorithm; // based on cipherType
   //
   private final String checksumType;
   private final byte[] checksum;
   private final String algorithmName;
   private final byte[] initVector;
   private final String keyDerivName;
   private final int keyDerivSize;
   private final int keyDerivIter;
   private final byte[] salt;
   private final String startKeyGen;
   private final int startKeySize;
 }
