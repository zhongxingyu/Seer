 /* Copyright 2012 The Tor Project
  * See LICENSE for licensing information */
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.StringReader;
 import java.security.Security;
 import java.security.interfaces.RSAPublicKey;
 import java.util.Iterator;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.Hex;
 import org.bouncycastle.asn1.ASN1OutputStream;
 import org.bouncycastle.crypto.digests.SHA1Digest;
 import org.bouncycastle.crypto.encodings.PKCS1Encoding;
 import org.bouncycastle.crypto.engines.RSAEngine;
 import org.bouncycastle.crypto.params.RSAKeyParameters;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.PEMReader;
 import org.torproject.descriptor.Descriptor;
 import org.torproject.descriptor.DescriptorFile;
 import org.torproject.descriptor.DescriptorReader;
 import org.torproject.descriptor.DescriptorSourceFactory;
 import org.torproject.descriptor.ServerDescriptor;
 
 /*
  * Verify server descriptors using the contained signing key.  Verify that
  * 1) the contained fingerprint is actually a hash of the signing key and
  * 2) the router signature was created using the signing key.
  *
  * Usage:
  * - Extract server descriptors to in/.
  * - Clone metrics-lib, run `ant jar`, and copy descriptor.jar to this
  *   directory.
  * - Download Apache Commons Codec and Compress jar files
  *   commons-codec-1.4.jar and commons-compress-1.3.jar and put them in
  *   this directory.
  * - Download BouncyCastle 1.47 jar files bcprov-jdk15on-147.jar and
  *   bcpkix-jdk15on-147.jar and put them in this directory.
  * - Compile and run this class: ./run.sh.
  */
 public class VerifyServerDescriptors {
   public static void main(String[] args) throws Exception {
     System.out.println("Verifying descriptors...");
     if (Security.getProvider("BC") == null) {
       Security.addProvider(new BouncyCastleProvider());
     }
     File inputDirectory = new File("in/");
     DescriptorReader reader = DescriptorSourceFactory
         .createDescriptorReader();
     reader.addDirectory(inputDirectory);
     Iterator<DescriptorFile> descriptorFiles = reader.readDescriptors();
     int processedDescriptors = 0, verifiedDescriptors = 0;
     while (descriptorFiles.hasNext()) {
       DescriptorFile descriptorFile = descriptorFiles.next();
       if (descriptorFile.getDescriptors() == null) {
         continue;
       }
       for (Descriptor descriptor : descriptorFile.getDescriptors()) {
         if (!(descriptor instanceof ServerDescriptor)) {
           continue;
         }
         ServerDescriptor serverDescriptor = (ServerDescriptor) descriptor;
         boolean isVerified = true;
 
         /* Verify that the contained fingerprint is a hash of the signing
          * key. */
         String signingKeyString = serverDescriptor.getSigningKey();
         String fingerprintString =
             serverDescriptor.getFingerprint().toLowerCase();
         PEMReader pemReader = new PEMReader(new StringReader(
             signingKeyString));
         RSAPublicKey signingKey = (RSAPublicKey) pemReader.readObject();
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         new ASN1OutputStream(baos).writeObject(
             new org.bouncycastle.asn1.pkcs.RSAPublicKey(
             signingKey.getModulus(),
             signingKey.getPublicExponent()).toASN1Primitive());
         byte[] pkcs = baos.toByteArray();
         byte[] signingKeyHashBytes = new byte[20];
         SHA1Digest sha1 = new SHA1Digest();
         sha1.update(pkcs, 0, pkcs.length);
         sha1.doFinal(signingKeyHashBytes, 0);
         String signingKeyHashString = Hex.encodeHexString(
             signingKeyHashBytes);
         if (!signingKeyHashString.equals(fingerprintString)) {
           System.out.println("In " + descriptorFile.getFile()
               + ", server descriptor "
               + serverDescriptor.getServerDescriptorDigest()
               + ", the calculated signing key hash "
               + signingKeyHashString
               + " does not match the contained fingerprint "
               + fingerprintString + "!");
           isVerified = false;
         }
 
         /* Verify that the router signature was created using the signing
          * key. */
         String serverDescriptorDigestString = serverDescriptor
             .getServerDescriptorDigest().toLowerCase();
         String routerSignatureString = serverDescriptor
             .getRouterSignature();
         byte[] routerSignature = Base64
             .decodeBase64(routerSignatureString.substring(
                 0 + "-----BEGIN SIGNATURE-----\n".length(),
                 routerSignatureString.length()
                     - "-----END SIGNATURE-----\n".length()).replaceAll(
                 "\n", ""));
         RSAKeyParameters rsakp = new RSAKeyParameters(false,
             signingKey.getModulus(), signingKey.getPublicExponent());
         PKCS1Encoding pe = new PKCS1Encoding(new RSAEngine());
         pe.init(false, rsakp);
         byte[] decryptedSignature = pe.processBlock(routerSignature, 0,
             routerSignature.length);
         String decryptedSignatureString =
             Hex.encodeHexString(decryptedSignature);
         if (!decryptedSignatureString.equals(
             serverDescriptorDigestString)) {
           System.out.println("In " + descriptorFile.getFile()
               + ", server descriptor "
               + serverDescriptor.getServerDescriptorDigest()
               + ", the decrypted signature "
               + decryptedSignatureString
               + " does not match the descriptor digest "
               + serverDescriptorDigestString + "!");
           isVerified = false;
         }
 
         processedDescriptors++;
         if (isVerified) {
           verifiedDescriptors++;
         }
       }
     }
     System.out.println("Verified " + verifiedDescriptors + "/"
         + processedDescriptors + " server descriptors.");
   }
 }
 
