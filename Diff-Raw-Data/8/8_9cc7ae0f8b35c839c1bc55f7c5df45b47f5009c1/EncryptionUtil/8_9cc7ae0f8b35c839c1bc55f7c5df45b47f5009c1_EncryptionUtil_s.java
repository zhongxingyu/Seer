 package com.geekcommune.identity;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.security.SignatureException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.bouncycastle.bcpg.ArmoredInputStream;
 import org.bouncycastle.bcpg.ArmoredOutputStream;
 import org.bouncycastle.bcpg.BCPGOutputStream;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openpgp.PGPCompressedData;
 import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
 import org.bouncycastle.openpgp.PGPEncryptedData;
 import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
 import org.bouncycastle.openpgp.PGPEncryptedDataList;
 import org.bouncycastle.openpgp.PGPException;
 import org.bouncycastle.openpgp.PGPKeyValidationException;
 import org.bouncycastle.openpgp.PGPLiteralData;
 import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
 import org.bouncycastle.openpgp.PGPObjectFactory;
 import org.bouncycastle.openpgp.PGPOnePassSignature;
 import org.bouncycastle.openpgp.PGPOnePassSignatureList;
 import org.bouncycastle.openpgp.PGPPBEEncryptedData;
 import org.bouncycastle.openpgp.PGPPrivateKey;
 import org.bouncycastle.openpgp.PGPPublicKey;
 import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
 import org.bouncycastle.openpgp.PGPPublicKeyRing;
 import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
 import org.bouncycastle.openpgp.PGPSecretKey;
 import org.bouncycastle.openpgp.PGPSecretKeyRing;
 import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
 import org.bouncycastle.openpgp.PGPSignature;
 import org.bouncycastle.openpgp.PGPSignatureGenerator;
 import org.bouncycastle.openpgp.PGPSignatureList;
 import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
 import org.bouncycastle.openpgp.PGPUtil;
 import org.bouncycastle.openpgp.PGPV3SignatureGenerator;
 
 import com.geekcommune.util.Pair;
 
 /**
  * Copyright (c) 2000-2011 The Legion Of The Bouncy Castle (http://www.bouncycastle.org)
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
  * and associated documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial
  * portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
  * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  * 
  * @author Legion of the Bouncy Castle, bobby
  *
  */
 
 public class EncryptionUtil {
     private static final Logger log = Logger.getLogger(EncryptionUtil.class);
 
     private static final boolean _verbose = false;
     private static EncryptionUtil instance = new EncryptionUtil();
 
     public static EncryptionUtil instance() {
         return instance;
     }
     
     private EncryptionUtil() {
         Security.addProvider(new BouncyCastleProvider());
         PGPUtil.setDefaultProvider("BC");
     }
 
     public PGPPublicKeyRingCollection readPublicKeyRing(String baseDir) throws FileNotFoundException, IOException,
             PGPException {
         PGPPublicKeyRingCollection pubRings = null;
         PGPPublicKeyRing pgpPub = null;
 
         // directory that contains all the .asc files
         File dir = new File(baseDir +
                 "/KeyRings/Public");
 
         // list all the files
         String[] children = dir.list();
         if (children == null) {
             // Either dir does not exist or is not a directory
         } else {
             for (int i = 0; i < children.length; i++) {
                 String filename = children[i];
                 log.info("File Name (.asc) " + "(" + i + ")"
                         + " = " + filename);
                 PGPPublicKeyRingCollection tmpKeyRingCollection = readPublicKeyRingCollection(new File(dir, filename));
 
                 if (pubRings == null) {
                     // read the first .asc file and create the
                     // PGPPublicKeyRingCollection to hold all the other key
                     // rings
                     pubRings = tmpKeyRingCollection;
                 } else {
                     PGPPublicKeyRingCollection otherKeyRings =
                             tmpKeyRingCollection;
 
                     @SuppressWarnings("unchecked")
                     Iterator<PGPPublicKeyRing> rIt =
                         otherKeyRings.getKeyRings();
                     while (rIt.hasNext()) {
                         pgpPub = rIt.next();
                     }
                     
                     //TODO bobby doesn't this belong inside the loop?
                     // copy the key ring to PGPPublicKeyCollection pubRings
                     pubRings = PGPPublicKeyRingCollection.
                             addPublicKeyRing(pubRings, pgpPub);
                 }
             }// end of for
 
             // size should equal the number of the .asc files
             log.debug("Collection size = "
                     + pubRings.size());
         }// end of else
         
         return pubRings;
     }
 
     public PGPPublicKeyRingCollection readPublicKeyRingCollection(
             File keyRingCollectionFile) throws IOException,
             FileNotFoundException, PGPException {
         FileInputStream keyRingCollectionIn = new FileInputStream(keyRingCollectionFile);
 
         PGPPublicKeyRingCollection retval = readPublicKeyRingCollection(keyRingCollectionIn);
         
         return retval;
     }
 
     public PGPPublicKeyRingCollection readPublicKeyRingCollection(
             InputStream keyRingCollectionIn) throws IOException,
             FileNotFoundException, PGPException {
         InputStream in = PGPUtil.getDecoderStream(keyRingCollectionIn);
 
         PGPPublicKeyRingCollection retval = new PGPPublicKeyRingCollection(in);
         
         in.close();
         keyRingCollectionIn.close();
         
         return retval;
     }
 
     public PGPSecretKey generateKey(String identity, char[] passPhrase) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
         KeyPairGenerator    kpg = KeyPairGenerator.getInstance("RSA", "BC");
         kpg.initialize(2048);
         KeyPair                    kp = kpg.generateKeyPair();
         PGPSecretKey    secretKey =
                 new PGPSecretKey(
                         PGPSignature.DEFAULT_CERTIFICATION,
                         PGPPublicKey.RSA_GENERAL,
                         kp.getPublic(),
                         kp.getPrivate(),
                         new Date(),
                         identity,
                         PGPEncryptedData.AES_256,
                         passPhrase,
                         null,
                         null,
                         new SecureRandom(),
                         "BC");
 
         //TODO sign key, associate email address, expiration?, comment?
         return secretKey;
     }
     //from here down is currently shamelessly stolen from GPG example code (PGPRampageEngine)
     
     /**
      * Encrypt and sign the specified input file
      * @param seed 
      * @throws PGPException 
      */
     public void encryptAndSignFile(String outputFilename, File inFile,
                                    InputStream publicRing, InputStream secretRing,
                                    String recipient, String signor, char[] passwd, byte[] seed) throws PGPException {
         encryptAndSignFile(
                 outputFilename,
                 inFile,
                 publicRing,
                 secretRing,
                 recipient,
                 signor,
                 passwd,
                 false,
                 false,
                 false,
                 seed);
     }
     
     /**
      * Encrypt and sign the specified input file.  If you pass in a seed, you
      * will get the same encrypted output for the same file + same seed + same signor.
      * 
      * DANGER!  If you use the same seed for multiple different messages, you are
      * making your key stream vulnerable to hacking, and your encryption is near
      * meaningless!  Make sure to use different seeds for different contents!
      *  
      * @param seed 
      */
     public void encryptAndSignFile(String outputFilename, File inFile,
                                    InputStream publicRing, InputStream secretRing,
                                    String recipient, String signor, char[] passwd,
                                    boolean armor, boolean withIntegrityCheck,
                                    boolean oldFormat, byte[] seed)
         throws PGPException
     {
         try
         {
             // Get the public keyring
             PGPPublicKeyRingCollection pubRing = new PGPPublicKeyRingCollection(
                                            PGPUtil.getDecoderStream(publicRing));
 
             PGPSecretKeyRingCollection secRing = readSecretKeyRingCollection(secretRing);
 
             // Find the recipient's key
             PGPPublicKey encKey = readPublicKey(pubRing, recipient, true);
             if (encKey.isRevoked()) {
                 String keyId = Long.toHexString(encKey.getKeyID()).substring(8);
                 throw new PGPException("Encryption key (0x"+keyId+") has been revoked");
             }
 
             // Find the signing key
             PGPPublicKey publicKey;
             PGPSecretKey secretKey;
             if (signor != null) {
                 publicKey = readPublicKey(pubRing, signor, false);
                 secretKey = findSecretKey(secRing, publicKey.getKeyID(), true);
             } else {
                 // Just look for the first signing key on the secret keyring (if any)
                 secretKey = findSigningKey(secRing);
                 publicKey = findPublicKey(pubRing, secretKey.getKeyID(), false);
             }
             if (publicKey.isRevoked()) {
                 String keyId = Long.toHexString(publicKey.getKeyID()).substring(8);
                 throw new PGPException("Signing key (0x"+keyId+") has been revoked");
             }
 
             PGPPrivateKey   privateKey = secretKey.extractPrivateKey(passwd, "BC");
 
             // Sign the data into an in-memory stream
             ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 
             if (oldFormat) {
                 signDataV3(inFile, bOut, publicKey, privateKey);
             } else {
                 signData(inFile, bOut, publicKey, privateKey);
             }
 
             SecureRandom secRand = makeSecureRandom(seed);
 
             PGPEncryptedDataGenerator cPk = oldFormat?
                new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, secRand, oldFormat, "BC"):
                new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, withIntegrityCheck, secRand, "BC");
 
             cPk.addMethod(encKey);
 
             byte[] bytes = bOut.toByteArray();
 
             OutputStream out  = new FileOutputStream(outputFilename);
             OutputStream aOut = armor? new ArmoredOutputStream(out): out;
             OutputStream cOut = cPk.open(aOut, bytes.length);
 
             cOut.write(bytes);
 
             cPk.close();
 
             if (armor) {
                 aOut.close();
             }
             out.close();
         } catch (PGPException e) {
             throw e;
         } catch (Exception e) {
             throw new PGPException("Error in encryption", e);
         }
     }
 
     /**
      * Encrypt the specified input file
      * @throws FileNotFoundException 
      */
     public void encryptFile(OutputStream out, File inFile, PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing,
                             String recipient, char[] passphrase, byte[] seed)
         throws PGPException, FileNotFoundException {
         long time = inFile.lastModified();
         try {
             encryptFile(out, new FileInputStream(inFile), inFile.getName(), inFile.length(), new Date(time), readPublicKey(pubRing, recipient, true),
                     false, false, false, passphrase, seed);
         } catch (IOException e) {
             throw new PGPException("Failed to read public key from keyring", e);
         }
     }
 
 
 
     /**
      * Encrypt the specified input file
      */
     public void encryptFile(OutputStream out, InputStream in, String inName, long inLength, Date inDate, PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing,
                             String recipient, char[] passphrase, byte[] seed)
         throws PGPException {
         try {
             encryptFile(out, in, inName, inLength, inDate, readPublicKey(pubRing, recipient, true),
                     false, false, false, passphrase, seed);
         } catch (IOException e) {
             throw new PGPException("Failed to read public key from keyring", e);
         }
     }
     
     enum Format {
         COMPRESSED, UNCOMPRESSED;
     }
     
     /**
      * Encrypt the specified input file
      * @param seed 
      */
     public void encryptFile(OutputStream out, InputStream in, String inName, long inLength, Date inDate, PGPPublicKey encKey,
                             boolean armor, boolean withIntegrityCheck,
                             boolean oldFormat, char[] passphrase, byte[] seed)
         throws PGPException {
         try {
             if (encKey.isRevoked()) {
                 String keyId = Long.toHexString(encKey.getKeyID()).substring(8);
                 throw new PGPException("Encryption key (0x"+keyId+") has been revoked");
             }
 
             // Compress the data into an in-memory stream
             ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 
             compressData(in, bOut, inName, inLength, inDate, oldFormat, Format.UNCOMPRESSED);
 
             // Now encrypt the result
             SecureRandom secRand = makeSecureRandom(seed);
 
             // Now encrypt the result
             PGPEncryptedDataGenerator cPk = oldFormat?
                new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, secRand, oldFormat, "BC"):
                new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, withIntegrityCheck, secRand, "BC");
 
             cPk.addMethod(encKey);
 
             byte[] bytes = bOut.toByteArray();
 
             OutputStream aOut = armor? new ArmoredOutputStream(out): out;
             OutputStream cOut = cPk.open(aOut, bytes.length);
 
             cOut.write(bytes);
 
             cPk.close();
 
             if (armor) {
                 aOut.close();
             }
             out.close();
         }
         catch (PGPException e) {
             throw e;
         }
         catch (Exception e) {
             throw new PGPException("Error in encryption", e);
         }
     }
 
 	public SecureRandom makeSecureRandom(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException {
 		SecureRandom secRand;
 		if( seed != null ) {
 		    secRand = SecureRandom.getInstance("SHA1PRNG");
 		    secRand.setSeed(seed);
 		} else {
 		    secRand = new SecureRandom();
 		}
 		return secRand;
 	}
 
     public PGPSecretKeyRingCollection readSecretKeyRingCollection(
             File secretRing) throws IOException, PGPException {
         InputStream secretRingStream = new FileInputStream(secretRing);
         PGPSecretKeyRingCollection secRing =
                 new PGPSecretKeyRingCollection(
                         PGPUtil.getDecoderStream(secretRingStream));
         secretRingStream.close();
         return secRing;
     }
 
     public PGPSecretKeyRingCollection readSecretKeyRingCollection(
             InputStream secretRing) throws IOException, PGPException {
         PGPSecretKeyRingCollection secRing =
                 new PGPSecretKeyRingCollection(
                         PGPUtil.getDecoderStream(secretRing));
         return secRing;
     }
 
 
 
     /**
      * Sign the specified file
      */
     public void signFile(String outputFilename,
                          File inFile,
                          InputStream publicRing,
                          InputStream secretRing,
                          String signor, char[] passwd)
         throws PGPException {
         signFile(outputFilename, inFile, publicRing, secretRing, signor, passwd, false, false);
     }
     
     /**
      * Sign the specified file
      */
     public void signFile(String outputFilename, File inFile,
                          InputStream publicRing,
                          InputStream secretRing,
                          String signor, char[] passwd,
                          boolean armor, boolean oldFormat)
         throws PGPException {
         try {
             PGPPublicKey publicKey;
             PGPSecretKey secretKey;
 
             // Get the public keyring
             PGPPublicKeyRingCollection pubRing = new PGPPublicKeyRingCollection(
                                           PGPUtil.getDecoderStream(publicRing));
 
             PGPSecretKeyRingCollection secRing = readSecretKeyRingCollection(secretRing);
 
             // Find the signing key
             if (signor != null) {
                 publicKey = readPublicKey(pubRing, signor, false);
                 secretKey = findSecretKey(secRing, publicKey.getKeyID(), true);
             } else {
                 // Just look for the first signing key on the secret keyring (if any)
                 secretKey = findSigningKey(secRing);
                 publicKey = findPublicKey(pubRing, secretKey.getKeyID(), false);
             }
             if (publicKey.isRevoked()) {
                 String keyId = Long.toHexString(publicKey.getKeyID()).substring(8);
                 throw new PGPException("Signing key (0x"+keyId+") has been revoked");
             }
 
             PGPPrivateKey   privateKey = secretKey.extractPrivateKey(passwd, "BC");
 
             OutputStream out  = new FileOutputStream(outputFilename);
             OutputStream aOut = armor? new ArmoredOutputStream(out): out;
 
             // Sign the data
             if (oldFormat) {
                 signDataV3(inFile, aOut, publicKey, privateKey);
             } else {
                 signData(inFile, aOut, publicKey, privateKey);
             }
 
             if (armor) {
                 // close() just finishes and flushes the stream but does not close it
                 aOut.close();
             }
             out.close();
         }
         catch (PGPException e) {
             throw e;
         }
         catch (Exception e) {
             throw new PGPException("Error in signing", e);
         }
     }
 
 
     /**
      * UNUSED IN FRIENDLY BACKUP
      * Sign the passed in message stream (version 3 signature)
      */
     private void signDataV3(File inFile, OutputStream aOut,
                             PGPPublicKey publicKey, PGPPrivateKey privateKey)
         throws PGPException
     {
         try {
             PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
             BCPGOutputStream           bOut = new BCPGOutputStream(cGen.open(aOut));
             PGPLiteralDataGenerator    lGen = new PGPLiteralDataGenerator(true);
 
             PGPV3SignatureGenerator    s3Gen =
                     new PGPV3SignatureGenerator(publicKey.getAlgorithm(), PGPUtil.SHA1, "BC");
 
             s3Gen.initSign(PGPSignature.BINARY_DOCUMENT, privateKey);
 
             s3Gen.generateOnePassVersion(false).encode(bOut);
 
             OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY, inFile);
 
             FileInputStream fIn = new FileInputStream(inFile);
 
             int ch;
             while ((ch = fIn.read()) >= 0) {
                 lOut.write(ch);
                 s3Gen.update((byte)ch);
             }
 
             fIn.close();
 
             // close() finishes the writing of the literal data and flushes the stream
             // It does not close bOut so this is ok here
             lGen.close();
 
             // Generate the signature
             s3Gen.generate().encode(bOut);
 
             // Must not close bOut here
             bOut.finish();
             bOut.flush();
 
             cGen.close();
         }
         catch (PGPException e) {
             throw e;
         }
         catch (Exception e) {
             throw new PGPException("Error in signing", e);
         }
     }
 
     /**
      * Sign the passed in message stream
      */
     private void signData(File inFile, OutputStream aOut,
                           PGPPublicKey publicKey, PGPPrivateKey privateKey)
         throws PGPException
     {
         try {
             PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
             BCPGOutputStream           bOut = new BCPGOutputStream(cGen.open(aOut));
             PGPLiteralDataGenerator    lGen = new PGPLiteralDataGenerator();
 
             PGPSignatureGenerator sGen = 
                     new PGPSignatureGenerator(publicKey.getAlgorithm(), PGPUtil.SHA1, "BC");
 
             sGen.initSign(PGPSignature.BINARY_DOCUMENT, privateKey);
 
             @SuppressWarnings("unchecked")
             Iterator<String> users = publicKey.getUserIDs();
             if (users.hasNext()) {
                 PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
                 spGen.setSignerUserID(false, users.next());
                 sGen.setHashedSubpackets(spGen.generate());
             }
 
             sGen.generateOnePassVersion(false).encode(bOut);
 
             OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY, inFile);
 
             FileInputStream fIn = new FileInputStream(inFile);
 
             int ch;
             while ((ch = fIn.read()) >= 0) {
                 lOut.write(ch);
                 sGen.update((byte)ch);
             }
 
             fIn.close();
 
             // close() finishes the writing of the literal data and flushes the stream
             // It does not close bOut so this is ok here
             lGen.close();
 
             // Generate the signature
             sGen.generate().encode(bOut);
 
             // Must not close bOut here
             bOut.finish();
             bOut.flush();
 
             cGen.close();
         }
         catch (PGPException e) {
             throw e;
         }
         catch (Exception e) {
             throw new PGPException("Error in signing", e);
         }
     }
 
 
     public PGPSignature makeSignature(byte[] input, PGPPublicKey publicKey, PGPPrivateKey privateKey) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
         PGPSignatureGenerator sGen = 
                 new PGPSignatureGenerator(publicKey.getAlgorithm(), PGPUtil.SHA1, "BC");
 
         sGen.initSign(PGPSignature.BINARY_DOCUMENT, privateKey);
 
         @SuppressWarnings("unchecked")
         Iterator<String> users = publicKey.getUserIDs();
         if (users.hasNext()) {
             PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
             spGen.setSignerUserID(false, users.next());
             sGen.setHashedSubpackets(spGen.generate());
         }
 
         for(byte b : input) {
             sGen.update(b);
         }
         
         return sGen.generate();
     }
     
     /**
      * Compress the data in the input stream
      * @throws FileNotFoundException 
      */
     public void compressData(File inFile, OutputStream bOut, boolean oldFormat, Format compress)
         throws PGPException, FileNotFoundException {
         long time = inFile.lastModified();
         compressData(new FileInputStream(inFile), bOut, inFile.getName(), inFile.length(), new Date(time), oldFormat, compress);
     }
     
 
     /**
      * Compress the data in the input stream
      */
     public void compressData(InputStream fIn, OutputStream bOut, String fileName, long dataLength, Date date, boolean oldFormat, Format compress)
         throws PGPException {
         try {
             if( compress == Format.COMPRESSED ) {
                 PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
                 bOut = comData.open(bOut);
             }
 
             PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator(oldFormat);
             OutputStream            pOut = lData.open(bOut, PGPLiteralData.BINARY,
                                                       fileName, dataLength,
                                                       date);
             byte[]                  bytes = new byte[4096];
             int                     len;
 
             while ((len = fIn.read(bytes)) > 0) {
                 pOut.write(bytes, 0, len);
             }
         
             fIn.close();
 
             lData.close();
             
             if( compress == Format.COMPRESSED ) {
                 bOut.close();
             }
         }
         catch (Exception e) {
             throw new PGPException("Error in encryption", e);
         }
     }
 
 
     /**
      * Decrypt the specified (PKE) input file
      * @throws IOException 
      */
     public byte[] decryptKeyBasedData(
             byte[] input,
             PGPSecretKey secKey,
             char[] passwd)
         throws PGPException, IOException {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
         ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
         decryptKeyBasedFile(encryptedOut, inputStream, secKey, passwd, false);
         inputStream.close();
         encryptedOut.close();
         return encryptedOut.toByteArray();
     }
 
     
     /**
      * Decrypt the specified (PKE) input file
      * @throws IOException 
      */
     public byte[] decryptKeyBasedData(
             byte[] input,
             PGPPublicKeyRingCollection pubRing,
             PGPSecretKeyRingCollection secRing,
             char[] passwd)
         throws PGPException, IOException {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
         ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
         decryptKeyBasedFile(encryptedOut, inputStream, pubRing, secRing, passwd, false);
         inputStream.close();
         encryptedOut.close();
         return encryptedOut.toByteArray();
     }
 
     
     /**
      * Decrypt the specified (PKE) input file
      */
     public void decryptKeyBasedFile(OutputStream out, InputStream inFile,
             PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing,
             PGPSecretKey pgpSecKey, PGPPublicKey encKey,
                                     char[] passwd)
         throws PGPException {
         decryptKeyBasedFile(out, inFile, pubRing, secRing, passwd, false);
     }
 
 
     /**
      * Decrypt the specified (PKE) input file.
      * 
      * @param out
      * @param inFile
      * @param pubRing
      * @param secRing
      * @param encKey
      * @param passwd
      * @param mdcRequired
      * @throws PGPException
      */
     public void decryptKeyBasedFile(OutputStream out, InputStream inFile,
             PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing,
                                     char[] passwd)
         throws PGPException
     {
         decryptKeyBasedFile(out, inFile, pubRing, secRing, null, passwd, false);
     }
 
 
     /**
      * Decrypt the specified (PKE) input file.
      * 
      * @param out
      * @param inFile
      * @param pubRing
      * @param secRing
      * @param encKey
      * @param passwd
      * @param mdcRequired
      * @throws PGPException
      */
     public void decryptKeyBasedFile(OutputStream out, InputStream inFile,
             PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing,
                                     char[] passwd, boolean mdcRequired)
         throws PGPException
     {
         decryptKeyBasedFile(out, inFile, pubRing, secRing, null, passwd, mdcRequired);
     }
 
 
     /**
      * Decrypt the specified (PKE) input file.
      * 
      * @param out
      * @param inFile
      * @param pgpSecKey, 
      * @param encKey
      * @param passwd
      * @param mdcRequired
      * @throws PGPException
      */
     public void decryptKeyBasedFile(
             OutputStream out,
             InputStream inFile,
             PGPSecretKey pgpSecKey,
             char[] passwd,
             boolean mdcRequired)
         throws PGPException
     {
         decryptKeyBasedFile(out, inFile, null, null, pgpSecKey, passwd, mdcRequired);
     }
 
 
     /**
      * Decrypt the specified (PKE) input file.
      * 
      * Either pubRing and secRing should be null, or pgpSecKey should be null, but not both.
      * 
      * @param out
      * @param inFile
      * @param pubRing
      * @param secRing
      * @param pgpSecKey
      * @param encKey
      * @param passwd
      * @param mdcRequired
      * @throws PGPException
      */
     private void decryptKeyBasedFile(OutputStream out, InputStream inFile,
             PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing,
             PGPSecretKey pgpSecKey, 
                                     char[] passwd, boolean mdcRequired)
         throws PGPException
     {
         try {
             InputStream fileToDecrypt = PGPUtil.getDecoderStream(inFile);
 
             PGPObjectFactory pgpFact = new PGPObjectFactory(fileToDecrypt);
 
             Object message = pgpFact.nextObject();
 
             PGPPublicKeyEncryptedData pked = null;
 //            PGPCompressedData cData;
 
             // Check for signed only
             if (!(message instanceof PGPCompressedData)) {
                 //
                 // Encrypted - the first object might be a PGP marker packet.
                 //
                 if (!(message instanceof PGPEncryptedDataList)) {
                     message = pgpFact.nextObject();
                     if (!(message instanceof PGPEncryptedDataList)) {
                         throw new PGPException("Unrecognised PGP message type: " + message.getClass());
                     }
                 }
 
                 PGPEncryptedDataList enc = (PGPEncryptedDataList) message;
 
                 int count = 0;
 
                 // find the secret key that is needed
                 while (count != enc.size()) {
                     if (enc.get(count) instanceof PGPPublicKeyEncryptedData) {
                         pked = (PGPPublicKeyEncryptedData) enc.get(count);
                         if( pgpSecKey == null ) {
                             pgpSecKey = secRing.getSecretKey(pked.getKeyID());
                             if (pgpSecKey != null) {
                                 break;
                             }
                         } else {
                             if( pgpSecKey.getKeyID() == pked.getKeyID() ) {
                                 break;
                             }
                         }
                     }
 
                     count++;
                 }
 
                 if (pgpSecKey == null) {
                     throw new PGPException("Corresponding secret key not found");
                 }
 
                 // Check for revoked key
                 PGPPublicKey encKey = pgpSecKey.getPublicKey();
 
                 if( encKey == null ) {
                     encKey = findPublicKey(pubRing, pgpSecKey.getKeyID(), true);
                 }
 
                 if (encKey.isRevoked()) {
                     String keyId = Long.toHexString(encKey.getKeyID()).substring(8);
                     System.out.println("Warning: Encryption key (0x"+keyId+") has been revoked");
                     // throw new PGPException("Encryption key (0x"+keyId+") has been revoked");
                 }
 
                 InputStream clear = pked.getDataStream(pgpSecKey.extractPrivateKey(passwd, "BC"), "BC");
    
                 PGPObjectFactory pgpClearFact = new PGPObjectFactory(clear);
                 
                 message = pgpClearFact.nextObject();
                 
                 if( message == null ) {
                     message = pgpFact.nextObject();
                 }
 //
 //                cData = (PGPCompressedData) pgpFact.nextObject();
 //            }
 //            else {
 //                cData = (PGPCompressedData) message;
             }
 
             if( message instanceof PGPCompressedData ) {
                 PGPCompressedData compressedData = (PGPCompressedData)message;
                 pgpFact = new PGPObjectFactory(compressedData.getDataStream());
 
                 message = pgpFact.nextObject();
             }
 
             // Plain file
             if (message instanceof PGPLiteralData) {
                 PGPLiteralData ld = (PGPLiteralData) message;
 
                 InputStream dataIn = ld.getInputStream();
 
                 int ch;
                 while ((ch = dataIn.read()) >= 0) {
                     out.write(ch);
                 }
                 out.close();
             } else if (message instanceof PGPOnePassSignatureList) {
                 // One-pass signature
                 if (!checkOnePassSignature(out,
                                            (PGPOnePassSignatureList) message,
                                            pgpFact,
                                            pubRing)) {
                     throw new PGPException("Signature verification failed");
                 }
 
                 System.out.println("Signature verified");
             } else if (message instanceof PGPSignatureList) {
                 // Signature list
                 if (!checkSignature(out,
                                     (PGPSignatureList) message,
                                     pgpFact,
                                     pubRing)) {
                     throw new PGPException("Signature verification failed");
                 }
 
                 System.out.println("Signature verified");
             } else {
                 // what?
                 // System.out.println("Unrecognised message type");
                 throw new PGPException("Unrecognised PGP message type: " + message.getClass());
             }
 
             if (pked != null) {
                 if (pked.isIntegrityProtected()) {
                     if (!pked.verify()) {
                         throw new PGPException("Message failed integrity check");
                     }
 
                     if (_verbose) {
                         System.out.println("Message integrity check passed");
                     }
                 }
                 else {
                     if (_verbose) {
                         System.out.println("No message integrity check");
                     }
 
                     if (mdcRequired) {
                         throw new PGPException("Missing required message integrity check");
                     }
                 }
             }
         }
         catch (PGPException e) {
             throw e;
         }
         catch (Exception e) {
             throw new PGPException("Error in decryption", e);
         }
     }
 
 
 
     /**
      * Decrypt the specified (PBE) input file
      */
     public void decryptPBEBasedFile(String outputFilename, InputStream in,
                                     char[] passPhrase, boolean mdcRequired)
         throws PGPException
     {
         try {
             //
             // we need to be able to reset the stream if we try a
             // wrong passphrase, we'll assume that all the mechanisms
             // appear in the first 10k for the moment...
             //
             int READ_LIMIT = 10 * 1024;
 
             in.mark(READ_LIMIT);
 
             PGPPBEEncryptedData pbe;
             InputStream clear;
             int count = 0;
 
             for (;;) {
                 InputStream dIn = PGPUtil.getDecoderStream(in);
 
                 PGPObjectFactory pgpF = new PGPObjectFactory(dIn);
                 PGPEncryptedDataList enc;
                 Object o = pgpF.nextObject();
 
                 //
                 // the first object might be a PGP marker packet.
                 //
                 if (o instanceof PGPEncryptedDataList) {
                     enc = (PGPEncryptedDataList) o;
                 } else {
                     enc = (PGPEncryptedDataList) pgpF.nextObject();
                 }
 
                 while (count < enc.size()) {
                     if (enc.get(count) instanceof PGPPBEEncryptedData) {
                         break;
                     }
 
                     count++;
                 }
 
                 if (count >= enc.size()) {
                     throw new PGPException("Passphrase invalid");
                 }
 
                 pbe = (PGPPBEEncryptedData) enc.get(count);
 
                 try {
                     clear = pbe.getDataStream(passPhrase, "BC");
                 } catch (PGPKeyValidationException e) {
                     in.reset();
                     continue;
                 }
 
                 break;
             }
 
             PGPObjectFactory pgpFact = new PGPObjectFactory(clear);
 
             PGPCompressedData cData = (PGPCompressedData) pgpFact.nextObject();
 
             pgpFact = new PGPObjectFactory(cData.getDataStream());
 
             PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();
 
             if (outputFilename == null) {
                 outputFilename = ld.getFileName();
             }
 
             FileOutputStream fOut = new FileOutputStream(outputFilename);
 
             InputStream unc = ld.getInputStream();
 
             int ch;
             while ((ch = unc.read()) >= 0) {
                 fOut.write(ch);
             }
 
             if (pbe.isIntegrityProtected()) {
                 if (!pbe.verify()) {
                     throw new PGPException("Message failed integrity check");
                 }
                 if (_verbose) {
                     System.out.println("Message integrity check passed");
                 }
             } else {
                 if (_verbose) {
                     System.out.println("No message integrity check");
                 }
 
                 if (mdcRequired) {
                     throw new PGPException("Missing required message integrity check");
                 }
             }
         } catch (PGPException e) {
             throw e;
         } catch (Exception e) {
             throw new PGPException("Error in decryption", e);
         }
     }
 
 
     /**
      * Verify the passed in file as being correctly signed.
      */
     public void verifyFile(OutputStream out, InputStream inFile, InputStream publicRing)
         throws PGPException
     {
         try {
             // Get the public keyring
             PGPPublicKeyRingCollection pubRing = new PGPPublicKeyRingCollection(
                                            PGPUtil.getDecoderStream(publicRing));
 
             InputStream in = PGPUtil.getDecoderStream(inFile);
 
             //
             // a clear signed file
             //
             if (in instanceof ArmoredInputStream && ((ArmoredInputStream) in).isClearText()) {
                 if (!checkClearsign(in, pubRing)) {
                     throw new PGPException("Signature verification failed.");
                 } 
                 
                 if (_verbose) {
                     System.out.println("Signature verified.");
                 }
             } else {
                 PGPObjectFactory pgpFact = new PGPObjectFactory(in);
 
                 PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
 
                 pgpFact = new PGPObjectFactory(c1.getDataStream());
 
                 Object message = pgpFact.nextObject();
 
                 if (message instanceof PGPOnePassSignatureList) {
                     // One-pass signature list
                     if (!checkOnePassSignature(out,
                                                (PGPOnePassSignatureList) message,
                                                pgpFact,
                                                pubRing)) {
                         throw new PGPException("Signature verification failed.");
                     }
                 } else if (message instanceof PGPSignatureList) {
                     // Signature list
                     if (!checkSignature(out,
                                         (PGPSignatureList) message,
                                         pgpFact,
                                         pubRing)) {
                         throw new PGPException("Signature verification failed.");
                     }
                 } else {
                     // what?
                     throw new PGPException("Unrecognised PGP message type");
                 }
             }
             if (_verbose) {
                 System.out.println("Signature verified.");
             }
         }
         catch (Exception e) {
             throw new PGPException("Error in verification", e);
         }
     }
 
 
     /**
      * Check the signature in clear-signed data
      */
     private boolean checkClearsign(InputStream in, PGPPublicKeyRingCollection pgpRings)
         throws PGPException {
         try {
             //
             // read the input, making sure we ingore the last newline.
             //
             ArmoredInputStream aIn = (ArmoredInputStream) in;
             boolean newLine = false;
             ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 
             int ch;
             while ((ch = aIn.read()) >= 0 && aIn.isClearText()) {
                 if (newLine) {
                     bOut.write((byte) '\n');
                     newLine = false;
                 }
 
                 if (ch == '\n') {
                     newLine = true;
                     continue;
                 }
 
                 bOut.write((byte) ch);
             }
 
             PGPObjectFactory pgpFact = new PGPObjectFactory(aIn);
             PGPSignatureList p3 = (PGPSignatureList) pgpFact.nextObject();
             PGPSignature sig = null;
             PGPPublicKey key = null;
 
             int count = 0;
             while (count < p3.size()) {
                 sig = (PGPSignature) p3.get(count);
                 key = pgpRings.getPublicKey(sig.getKeyID());
                 if (key != null) {
                     break;
                 }
 
                 count++;
             }
 
             if (key == null) {
                 throw new PGPException("Corresponding public key not found");
             }
             if (key.isRevoked()) {
                 String keyId = Long.toHexString(key.getKeyID()).substring(8);
                 System.out.println("Warning: Signing key (0x"+keyId+") has been revoked");
                 // throw new PGPException("Signing key (0x"+keyId+") has been revoked");
             }
 
             sig.initVerify(key, "BC");
 
             sig.update(bOut.toByteArray());
 
             return sig.verify();
         } catch (PGPException e) {
             throw e;
         } catch (Exception e) {
             throw new PGPException("Error in verification", e);
         }
     }
 
 
     /**
      * Check a one-pass signature
      */
     private boolean checkOnePassSignature(OutputStream out,
                                           PGPOnePassSignatureList p1,
                                           PGPObjectFactory pgpFact,
                                           PGPPublicKeyRingCollection pgpRing)
         throws PGPException {
         try {
             PGPOnePassSignature ops = null;
             PGPPublicKey key = null;
 
             int count = 0;
             while (count < p1.size()) {
                 ops = p1.get(count);
                 key = pgpRing.getPublicKey(ops.getKeyID());
                 if (key != null) {
                     break;
                 }
 
                 count++;
             }
 
             if (key == null) {
                 throw new PGPException("Corresponding public key not found");
             }
             
             if (key.isRevoked()) {
                 String keyId = Long.toHexString(key.getKeyID()).substring(8);
                 System.out.println("Warning: Signing key (0x"+keyId+") has been revoked");
                 // throw new PGPException("Signing key (0x"+keyId+") has been revoked");
             }
 
             PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();
 
 //            if (outputFilename == null) {
 //                outputFilename = ld.getFileName();
 //            }
 //
 //            FileOutputStream out = new FileOutputStream(outputFilename);
 
             InputStream dataIn = ld.getInputStream();
 
             ops.initVerify(key, "BC");
 
             int ch;
             while ((ch = dataIn.read()) >= 0) {
                 ops.update((byte) ch);
                 out.write(ch);
             }
 
             out.close();
 
             PGPSignatureList p3 = (PGPSignatureList) pgpFact.nextObject();
 
             return ops.verify(p3.get(0));
         } catch (PGPException e) {
             throw e;
         } catch (Exception e) {
             throw new PGPException("Error in verification", e);
         }
     }
 
 
     /**
      * Check a signature
      */
     private boolean checkSignature(OutputStream out,
                                    PGPSignatureList sigList,
                                    PGPObjectFactory pgpFact,
                                    PGPPublicKeyRingCollection pgpRing)
         throws PGPException
     {
         try {
             PGPSignature sig = null;
             PGPPublicKey key = null;
 
             int count = 0;
             while (count < sigList.size()) {
                 sig = sigList.get(count);
                 key = pgpRing.getPublicKey(sig.getKeyID());
                 if (key != null) {
                     break;
                 }
 
                 count++;
             }
 
             if (key == null) {
                 throw new PGPException("Corresponding public key not found");
             }
             
             if (key.isRevoked()) {
                 String keyId = Long.toHexString(key.getKeyID()).substring(8);
                 System.out.println("Warning: Signing key (0x"+keyId+") has been revoked");
                 // throw new PGPException("Signing key (0x"+keyId+") has been revoked");
             }
 
             PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();
 
 //            if (outputFilename == null) {
 //                outputFilename = ld.getFileName();
 //            }
 //
 //            FileOutputStream out = new FileOutputStream(outputFilename);
 
             InputStream dataIn = ld.getInputStream();
 
             sig.initVerify(key, "BC");
 
             int ch;
             while ((ch = dataIn.read()) >= 0) {
                 sig.update((byte) ch);
                 out.write(ch);
             }
             out.close();
 
             return sig.verify();
         } catch (PGPException e) {
             throw e;
         } catch (Exception e) {
             throw new PGPException("Error in verification", e);
         }
     }
 
 
 
     /**
      * Find the public key for the recipient
      */
     public PGPPublicKey readPublicKey(PGPPublicKeyRingCollection pubRing,
                                        String recipient, boolean encrypting)
         throws IOException, PGPException {
         //
         // we just loop through the collection till we find a key suitable for encryption, in the real
         // world you would probably want to be a bit smarter about this.
         //
         PGPPublicKey key = null;
 
         //
         // iterate through the key rings.
         //
         @SuppressWarnings("unchecked")
         Iterator<PGPPublicKeyRing> rIt = pubRing.getKeyRings();
 
         //System.out.println("processing public key ring, looking for : "+recipient);
         while (key == null && rIt.hasNext()) {
             PGPPublicKeyRing kRing = rIt.next();
             //System.out.println("Found a ring with keys ");
             @SuppressWarnings("unchecked")
             Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
 
             //TODO bobby make sure it's safe to reuse the name from the prior key!
             String name = "<not specified>";
             while (key == null && kIt.hasNext()) {
                 PGPPublicKey k = kIt.next();
                 @SuppressWarnings("unchecked")
                 Iterator<String> userIDs = k.getUserIDs();
 //                String name = "<not specified>";
                 if (userIDs.hasNext()) {
                     name = userIDs.next();
                 }
                 //System.out.println("found a key with name "+name);
 
                 if (name.indexOf(recipient) >= 0) {
                     if (!encrypting || k.isEncryptionKey()) {
                         //System.out.println("Found the key I'm looking for");
                         key = k;
                     }
                 }
             }
         }
 
         if (key == null) {
             if (encrypting) {
                 throw new PGPException("Can't find encryption key in key ring");
             } else {
                 throw new PGPException("Can't find signing key in key ring");
             }
         }
 
         return key;
     }
 
 
     public PGPPublicKey findFirstEncryptingKey(PGPPublicKeyRing keyRing) throws PGPException {
         @SuppressWarnings("unchecked")
         Iterator<PGPPublicKey> kIt = keyRing.getPublicKeys();
 
         PGPPublicKey retval = null;
         while (retval == null && kIt.hasNext()) {
             PGPPublicKey k = kIt.next();
 
             if (k.isEncryptionKey()) {
                 //System.out.println("Found the key I'm looking for");
                 retval = k;
             }
         }
         
         if( retval == null ) {
             throw new PGPException("No encrypting key found in keyring");
         }
         
         return retval;
     }
 
     /**
      * Finds the first key in secretKeyRings which is capable of signing and which corresponds with a key in keyRing.
      * @param keyRing
      * @param secretKeyRings
      * @return
      * @throws PGPException 
      */
     public PGPSecretKey findFirstSigningKey(PGPPublicKeyRing keyRing, PGPSecretKeyRingCollection secretKeyRings) throws PGPException {
         @SuppressWarnings("unchecked")
         Iterator<PGPPublicKey> kIt = keyRing.getPublicKeys();
 
         PGPSecretKey retval = null;
         while (retval == null && kIt.hasNext()) {
             PGPPublicKey k = kIt.next();
             PGPSecretKey sk = secretKeyRings.getSecretKey(k.getKeyID());
             if( sk.isSigningKey() ) {
                 retval = sk;
             }
         }
         
         if( retval == null ) {
             throw new PGPException("No signing key found");
         }
         
         return retval;
     }
     
     
     /**
      * Find the public keys for the recipient
      */
     public PGPPublicKeyRing findPublicKeyRing(PGPPublicKeyRingCollection pubRing,
                                        String recipient)
         throws IOException, PGPException {
         PGPPublicKeyRing retval = null;
         String retvalName = null;
 
         //
         // iterate through the key rings.
         //
         @SuppressWarnings("unchecked")
         Iterator<PGPPublicKeyRing> rIt = pubRing.getKeyRings();
 
         //System.out.println("processing public key ring, looking for : "+recipient);
         while (rIt.hasNext()) {
             PGPPublicKeyRing kRing = rIt.next();
             //System.out.println("Found a ring with keys ");
             @SuppressWarnings("unchecked")
             Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
 
             while (kIt.hasNext()) {
                 PGPPublicKey k = kIt.next();
 
                 String name = "<not specified>";
                 
                 @SuppressWarnings("unchecked")
                 Iterator<String> userIDs = k.getUserIDs();
                 if (userIDs.hasNext()) {
                     name = userIDs.next();
                 }
                 //System.out.println("found a key with name "+name);
 
                 if (name.indexOf(recipient) >= 0) {
                     if( retval == null || retval == kRing ) {
                         retval = kRing;
                         retvalName = name;
                     } else {
                         throw new PGPException("Ambiguous recipient name; matches both " + name + " and " + retvalName);
                     }
                 }
             }
         }
 
         if (retval == null) {
             throw new PGPException("Can't find keyring matching " + recipient);
         }
 
         return retval;
     }
 
 
 
     /**
      * Load a public key ring collection from keyIn and find the key corresponding to
      * keyID if it exists.
      *
      * @param keyIn      input stream representing a key ring collection.
      * @param keyID      keyID we want.
      * @param encrypting whether we are encrypting or not
      * @return
      * @throws IOException
      * @throws PGPException
      * @throws NoSuchProviderException
      */
     private static PGPPublicKey findPublicKey(PGPPublicKeyRingCollection pubRing,
                                               long keyID, boolean encrypting)
         throws IOException, PGPException, NoSuchProviderException {
         PGPPublicKey    pubKey = pubRing.getPublicKey(keyID);
 
         if (pubKey != null) {
             if (encrypting && !pubKey.isEncryptionKey()) {
                 throw new PGPException("Key is not an encryption key");
             }
         } else {
             throw new PGPException("Can't find public key in key ring");
         }
 
         return pubKey;
     }
 
 
 
     /**
      * Load a secret key ring collection from keyIn and find the secret key corresponding to
      * keyID if it exists.
      *
      * @param keyIn input stream representing a key ring collection.
      * @param keyID keyID we want.
      * @param signing indicates whether looking for a signing key.
      * @return
      * @throws IOException
      * @throws PGPException
      * @throws NoSuchProviderException
      */
     public PGPSecretKey findSecretKey(PGPSecretKeyRingCollection secRing, long keyID, boolean signing)
         throws IOException, PGPException, NoSuchProviderException {
         PGPSecretKey    pgpSecKey = secRing.getSecretKey(keyID);
 
         if (pgpSecKey != null) {
             if (signing && !pgpSecKey.isSigningKey()) {
                 throw new PGPException("Key is not a signing key");
             }
         } else {
             throw new PGPException("Can't find secret key in key ring");
         }
 
         return pgpSecKey;
     }
 
 
 
     /**
      * A simple routine that opens a key ring file and finds the first available
      * key suitable for signature generation.
      * 
      * @param in
      * @return
      * @throws IOException
      * @throws PGPException
      */
     private static PGPSecretKey findSigningKey(PGPSecretKeyRingCollection secRing)
         throws IOException, PGPException
     {
         //
         // We just loop through the collection till we find a key suitable for encryption.
         //
         PGPSecretKey    key = null;
 
         @SuppressWarnings("unchecked")
         Iterator<PGPSecretKeyRing> rIt = secRing.getKeyRings();
 
         while (key == null && rIt.hasNext()) {
             PGPSecretKeyRing kRing = rIt.next();    
             @SuppressWarnings("unchecked")
             Iterator<PGPSecretKey> kIt = kRing.getSecretKeys();
 
             while (key == null && kIt.hasNext()) {
                 PGPSecretKey k = (PGPSecretKey) kIt.next();
                 if (k.isSigningKey()) {
                     key = k;
                 }
             }
         }
         
         if (key == null) {
             throw new PGPException("Can't find a signing key in the key ring");
         }
         
         return key;
     }
 
     public byte[] encryptData(byte[] input, PGPPublicKey pubKey, char[] passwd, byte[] seed) throws PGPException, IOException {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
         ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
         encryptFile(encryptedOut, inputStream, "data", input.length, new Date(0), pubKey, false, false, false, passwd, seed);
         inputStream.close();
         encryptedOut.close();
         return encryptedOut.toByteArray();
     }
 
     public byte[] encryptData(byte[] input, PGPPublicKey pubKey, PGPPublicKeyRingCollection pubRing, PGPSecretKeyRingCollection secRing, String recip, char[] passwd, byte[] seed) throws PGPException, IOException {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
         ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
         encryptFile(encryptedOut, inputStream, "data", input.length, new Date(0), pubRing, secRing, recip, passwd, seed);
         inputStream.close();
         encryptedOut.close();
         return encryptedOut.toByteArray();
     }
 
     /**
      * Get the keyring pointed to by the public & secret files.  Creates the files
      * with a single key if they don't already exist; interrogates keyDataSource for the
      * info to create the key (typically it should pop up a gui).
      * 
      * TODO bobby: doesn't create usable keyrings yet, and doesn't save what it does create :-( 
      * @param publicKeyRingFile
      * @param secretKeyRingFile
      * @param keyDataSource
      * @return
      * @throws PGPException
      * @throws FileNotFoundException
      * @throws IOException
      * @throws NoSuchAlgorithmException
      * @throws NoSuchProviderException
      * @throws InvalidAlgorithmParameterException
      */
     public Pair<PGPPublicKeyRingCollection, PGPSecretKeyRingCollection>
         getOrCreateKeyring(
                 File publicKeyRingFile,
                 File secretKeyRingFile,
                 KeyDataSource keyDataSource)
                         throws PGPException, FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
         boolean pubRingFound = publicKeyRingFile.isFile();
         boolean secRingFound = secretKeyRingFile.isFile();
         
         if( pubRingFound != secRingFound ) {
             throw new PGPException("Expect both public & secret keyring, or neither: " + publicKeyRingFile + ", " + secretKeyRingFile);
         }
         
         Pair<PGPPublicKeyRingCollection, PGPSecretKeyRingCollection> retval =
                 new Pair<PGPPublicKeyRingCollection, PGPSecretKeyRingCollection>();
         
         if( pubRingFound ) {
             retval.setFirst(EncryptionUtil.instance().readPublicKeyRingCollection(publicKeyRingFile));
             retval.setSecond(EncryptionUtil.instance().readSecretKeyRingCollection(secretKeyRingFile));
         } else {
             if( publicKeyRingFile.exists() || secretKeyRingFile.exists() ) {
                 throw new PGPException("Either public or secret keyring not a normal file: " + publicKeyRingFile + ", " + secretKeyRingFile);
             }
             
             PGPSecretKey key =
                     generateKey(keyDataSource.getIdentity(), keyDataSource.getPassphrase());
             
             PGPPublicKeyRing publicKeyRing = new PGPPublicKeyRing(key.getPublicKey().getEncoded());
             Collection<PGPPublicKeyRing> collection = Collections.singletonList(publicKeyRing);
             retval.setFirst(new PGPPublicKeyRingCollection(collection));
             
             PGPSecretKeyRing secretKeyRing = new PGPSecretKeyRing(key.getEncoded());
             Collection<PGPSecretKeyRing> secretKeyRings = Collections.singletonList(secretKeyRing);
             retval.setSecond(new PGPSecretKeyRingCollection(secretKeyRings));
             
             //TODO bobby save keyrings to the files
         }
         
         return retval;
     }
 
     public void generateKey(
             String name,
             String email,
             char[] passphrase,
             File publicKeyringFile,
             File secretKeyringFile) throws IOException, InterruptedException {
         String gpgInput =
                 "# input file to generate GnuPG keys automatically\n" +
                 "#######################################\n" + 
                 "# parameters for the key \n" +
                 "Key-Type: DSA\n" +
                 "Key-Length: 1024\n" +
                 "Subkey-Type: ELG-E\n" +
                 "Subkey-Length: 2048\n" +
                 "Name-Real: " + name + "\n" +
                 "Name-Email: " + email + "\n" +
                 "Passphrase: " + new String(passphrase) + "\n" +
                 "Name-Comment: friendly backup key\n" + 
                 "Expire-Date: 0\n" +
                 "######################################\n" + 
                 "# the keyring files \n" +
                 "%pubring " + publicKeyringFile.getCanonicalPath() + "\n" +
                 "%secring " + secretKeyringFile.getCanonicalPath() + "\n" +
                 "# perform key generation\n" + 
                 "%commit\n";
         
         String gpgCommand = "gpg --homedir tmp --no-options --batch --gen-key -";
         
         Process proc = Runtime.getRuntime().exec(gpgCommand);
         PrintStream out = new PrintStream(proc.getOutputStream());
         out.print(gpgInput);
         out.close();
         
         proc.waitFor();
     }
 
 	public PGPPublicKey findFirstSigningKey(PGPPublicKeyRing keyRing) throws PGPException {
         @SuppressWarnings("unchecked")
         Iterator<PGPPublicKey> kIt = keyRing.getPublicKeys();
 
         PGPPublicKey retval = null;
         while (retval == null && kIt.hasNext()) {
             PGPPublicKey k = kIt.next();
 
             if ( isSigningAlgorithm(k.getAlgorithm()) ) {
             	retval = k;
             }
         }
         
         if( retval == null ) {
             throw new PGPException("No signing key found in keyring");
         }
         
         return retval;
 	}
 
 	public boolean isSigningAlgorithm(int algorithm) {
 		  return ((algorithm == PGPPublicKey.RSA_GENERAL) || (algorithm == PGPPublicKey.RSA_SIGN)
 		  || (algorithm == PGPPublicKey.DSA) || (algorithm == PGPPublicKey.ECDSA) || (algorithm == PGPPublicKey.ELGAMAL_GENERAL));
 	}
 }
