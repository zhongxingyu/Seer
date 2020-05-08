 package org.agora;
 
 import java.io.*;
 import java.net.*;
 import java.security.*;
 import java.security.cert.*;
 import java.util.*;
 import org.bouncycastle.ocsp.*;
 
 import java.util.Arrays;
 import java.util.Random;
 import java.applet.Applet;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.URLConnection;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.BoxLayout;
 import javax.swing.BorderFactory;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import java.awt.Frame;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.security.Key;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.Security;
 import java.security.Signature;
 import java.security.MessageDigest;
 import java.util.Enumeration;
 import java.security.cert.Certificate;
 import java.security.cert.X509Certificate;
 import javax.security.auth.x500.X500Principal;
 import org.bouncycastle.ocsp.*;
 
 import org.apache.commons.codec.binary.Base64;
 
 import verificatum.eio.ByteTree;
 import verificatum.eio.ByteTreeReader;
 import verificatum.arithm.ModPGroup;
 import verificatum.arithm.LargeInteger;
 import verificatum.arithm.PGroup;
 import verificatum.arithm.PGroupElement;
 import verificatum.arithm.PGroupElementArray;
 import verificatum.arithm.PPGroup;
 import verificatum.arithm.PPGroupElement;
 import verificatum.arithm.PRing;
 import verificatum.arithm.PRingElement;
 import verificatum.arithm.PRingElementArray;
 import verificatum.crypto.CryptoKeyGen;
 import verificatum.crypto.CryptoKeyGenCramerShoup;
 import verificatum.crypto.CryptoKeyPair;
 import verificatum.crypto.Hashfunction;
 import verificatum.crypto.HashfunctionHeuristic;
 import verificatum.crypto.PRG;
 import verificatum.crypto.PRGHeuristic;
 import verificatum.crypto.RandomSource;
 import verificatum.protocol.mixnet.MixNetElGamalInterface;
 
 /**
  * Class used in Agora to verify that a ballot is valid.
  */
 public class BallotVerifier {
     protected static final String interfaceName = "native";
     protected static final int certainty = 100;
 
     protected RandomSource mRandomSource = null;
 
     protected X509Certificate mCertificate = null;
     protected PGroupElement[] mEncryptedVotes = null;
 
     protected String mSubjectCIF = null;
     protected String mSubjectName = null;
     protected String mSubjectSurname1 = null;
     protected String mSubjectSurname2 = null;
 
     static String encode(byte[] bytes) throws Exception {
         byte[] encoded = Base64.encodeBase64(bytes);
         return new String(encoded, "ASCII");
     }
 
     static byte[] decode(String str) throws Exception {
         byte[] bytes = str.getBytes("ASCII");
         return Base64.decodeBase64(bytes);
     }
 
     /**
      * Verifies a ballot. Verifications:
      * 1. Checks that the vote is correctly encrpyted with the correct public keys
      * 2. Checks that the given dni-e certificate is valid and not revoked
      * 3. Check that the votes are signed with the given certificate
      * 4. Obtains user information from the certificate and returns it
      *
      * @return "CIF (with number),Name,Surname1,Surname2" if all verifications
      *         passed or "FAIL" otherwise. For example, a possible output is:
      *         "00000000F,Pepito,De Los Palotes,Marianos"
      */
     public String verify(String serializedCertificate, String signature,
         String[] votes, String[] propossalPublicKeys, String[] propossalIds,
         String[] aFactors, String[] dFactors, String[] uFactors) {
         try {
             init();
             if (votes.length < 1 || votes.length != propossalPublicKeys.length) {
                 throw new Exception("Invalid input data");
             }
             deserializeCertificate(serializedCertificate);
             checkVotesEncryption(votes, propossalPublicKeys, propossalIds,
                 aFactors, dFactors, uFactors);
             validateCertificate();
             checkSignature(signature, votes);
             return mSubjectCIF + "," + mSubjectName + "," + mSubjectSurname1
                    + "," + mSubjectSurname2;
         } catch (Exception e) {
             return "FAIL";
         }
     }
 
     /**
      * Reads a Certificate instance from a string
      */
     protected void deserializeCertificate(String serializedCertificate)
         throws Exception
     {
         CertificateFactory factory = CertificateFactory.getInstance("X.509");
         mCertificate = (X509Certificate)factory.generateCertificate(
             new ByteArrayInputStream(decode(serializedCertificate)));
         String subject = mCertificate.getSubjectX500Principal().toString();
 
         // Example of subject:
         // CN="DE LOS PALOTES MARIANOS, ANTONIO EDUARDO (FIRMA)", GIVENNAME=ANTONIO EDUARDO, SURNAME=DE LOS PALOTES, SERIALNUMBER=012345678D, C=ES
 
         Pattern pattern = Pattern.compile("^CN=\"([^,]+), [^(]+ \\(FIRMA\\)\", GIVENNAME=([^,]+), SURNAME=([^,]+), SERIALNUMBER=(\\d{8}[A-Z]), C=ES$");
         Matcher matcher = pattern.matcher(subject);
         if (!matcher.find()) {
             throw new Exception("Invalid Certificate subject: " +subject);
         }
 
         mSubjectCIF = matcher.group(4);
         mSubjectName = matcher.group(2);
         mSubjectSurname1 = matcher.group(3);
         mSubjectSurname2 = matcher.group(1).substring(mSubjectSurname1.length());
     }
 
     /**
      * Loads the vote string into PGroupElements. TODO: In the future,here we 
      * will verify if the vote is valid using a random oracle ZKP of knowledge
      * provided by the voter.
      */
     protected void checkVotesEncryption(String[] votes,
         String[] propossalPublicKeys, String[] propossalIds,
         String[] aFactors, String[] dFactors, String[] uFactors
     )
         throws Exception
     {
         mEncryptedVotes = new PGroupElement[votes.length];
         MixNetElGamalInterface mixnetInterface =
             MixNetElGamalInterface.getInterface(interfaceName);
         MessageDigest sha1 = MessageDigest.getInstance("SHA1");
         PRG prg = new PRGHeuristic();
 
         for (int i = 0; i < votes.length; i++) {
             // 1. Test that the ciphertext load correctly into memory
             PGroupElement fullPublicKey = MixNetElGamalInterface.stringToPublicKey(
                 interfaceName, propossalPublicKeys[i], mRandomSource, certainty);
             mEncryptedVotes[i] = mixnetInterface.stringToCiphertext(
                 fullPublicKey.getPGroup(), votes[i]);
 
             // 2. Test the proof of knowledge of the plaintext
             // Recover key from input
             PGroupElement basicPublicKey =
                 ((PPGroupElement)fullPublicKey).project(0);
             PGroupElement publicKey =
                 ((PPGroupElement)fullPublicKey).project(1);
 
             PGroup basicPublicKeyPGroup = basicPublicKey.getPGroup();
             PGroup publicKeyPGroup = publicKey.getPGroup();
             PRing randomizerPRing = basicPublicKeyPGroup.getPRing();
 
             // c = hash(prefix, g, u*v, a)
             String prefix =  propossalIds[i];
             String g = propossalPublicKeys[i];
             String uv = votes[i];
             String aStr = aFactors[i];
             String factorsStr = prefix + g + uv + aStr;
             byte[] cHash = sha1.digest(factorsStr.getBytes());
             prg.setSeed(cHash);
             PRingElement c =
                 randomizerPRing.randomElementArray(1, prg, 20).get(0);
 
             ByteTree bt = new ByteTree(decode(aFactors[i]), null);
             ByteTreeReader btr = bt.getByteTreeReader();
            PGroupElement aFactor = basicPublicKey.getPGroup().toElement(btr);
 
             bt = new ByteTree(decode(uFactors[i]), null);
             btr = bt.getByteTreeReader();
            PGroupElement uFactor = basicPublicKey.getPGroup().toElement(btr);
 
            bt = new ByteTree(decode(dFactors[i]), null);
            PRingElement dFactor = randomizerPRing.toElement(bt.getByteTreeReader());
 
             // check that u^c * a = g^d
             if (!uFactor.exp(c).mul(aFactor).equals(basicPublicKey.exp(dFactor))) {
                 throw new Exception("Invalid proof of knowledge for the plaintext for vote i = " + i);
             }
         }
 
         
     }
 
     /**
      * Checks that the given certificate is valid.
      * 1. Check that it is valid (not expired etc).
      * 2. Check that the autority that signed is one of the three valid i.e.
      *    recognized authorities.
      * 3. Check via OCSP that the certificate is not revoked.
      */
     protected void validateCertificate() throws Exception {
         // throws an exception if certificate is invalid (expired or not yet valid)
         mCertificate.checkValidity();
 
         Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
         OCSPReqGenerator ocspReqGen = new OCSPReqGenerator();
         
         String issuerCN = mCertificate.getIssuerX500Principal().getName("CANONICAL");
         CertificateFactory cfIssuer = CertificateFactory.getInstance("X.509");
         X509Certificate certCA = null;
         ClassLoader classLoader = getClass().getClassLoader();
         if (issuerCN.contains("cn=ac dnie 001")) {
             certCA = (X509Certificate) cfIssuer.generateCertificate(
                 new VirtualFileInputStream(classLoader.getResourceAsStream(
                     "certs/ACDNIE001-SHA1.crt")));
         } else if (issuerCN.contains("cn=ac dnie 002")) {
             certCA = (X509Certificate) cfIssuer.generateCertificate(
                 new VirtualFileInputStream(classLoader.getResourceAsStream(
                     "certs/ACDNIE002-SHA1.crt")));
         } else if (issuerCN.contains("cn=ac dnie 003")) {
             certCA = (X509Certificate) cfIssuer.generateCertificate(
                 new VirtualFileInputStream(classLoader.getResourceAsStream(
                     "certs/ACDNIE003-SHA1.crt")));
         } else {
             throw new Exception("Invalid certCA");
         }
 
         CertificateID certid = new CertificateID(CertificateID.HASH_SHA1, certCA, mCertificate.getSerialNumber());
         ocspReqGen.addRequest(certid);
         OCSPReq ocspReq = ocspReqGen.generate();
 
         URL url = new URL("http://ocsp.dnie.es");
         HttpURLConnection con = (HttpURLConnection)url.openConnection();
         con.setRequestProperty("Content-Type", "application/ocsp-request");
         con.setRequestProperty("Accept", "application/ocsp-response");
         con.setDoOutput(true);
 
         OutputStream out = con.getOutputStream();
         DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));
         dataOut.write(ocspReq.getEncoded());
         dataOut.flush();
         dataOut.close();
 
         InputStream in = con.getInputStream();
         BasicOCSPResp basicResp = (BasicOCSPResp)new OCSPResp(in).getResponseObject();
         con.disconnect();
         out.close();
         in.close();
 
         SingleResp singResp = basicResp.getResponses()[0];
         Object status = singResp.getCertStatus();
 
         // status being null indicates it's good!
         if (status != null) {
             throw new Exception("invalid certificate, status = " + status);
         }
     }
 
     protected void checkSignature(String signature, String[] votes) throws Exception {
         Signature sig = Signature.getInstance("SHA1withRSA");
         sig.initVerify(mCertificate.getPublicKey());
         ByteArrayOutputStream concatenatedVotes = new ByteArrayOutputStream();
         for (String vote : votes) {
             concatenatedVotes.write(vote.getBytes());
         }
         sig.update(concatenatedVotes.toByteArray());
         if(!sig.verify(decode(signature))) {
             throw new Exception("Signature mismatch");
         }
     }
 
     protected void init() throws Exception {
         mRandomSource = new PRGHeuristic();
     }
 
     /**
      * Call for testing. args = cert sig vote1 pk1 votingid1 afactor1 dfactor1 ufactor1
      * [voten pkn votingidn afactorn dfactorn ufactorn...]
      */
     public static void main(String[] args) {
         BallotVerifier verifier = new BallotVerifier();
 
         // Check arguments
         if (args.length < 8 || args.length % 6 != 2) {
            System.out.println("FAIL");
             System.exit(1);
             return;
         }
 
         // Parse arguments
         int numVotes = (args.length - 2) / 6;
         String serializedCertificate = args[0];
         String signature = args[1];
         String[] votes = new String[numVotes];
         String[] propossalPublicKeys = new String[numVotes];
         String[] propossalIds = new String[numVotes];
         String[] aFactors = new String[numVotes];
         String[] dFactors = new String[numVotes];
         String[] uFactors = new String[numVotes];
 
         for (int i = 0; i < numVotes; i++) {
             votes[i] = args[2 + i*6];
         }
 
         for (int i = 0; i < numVotes; i++) {
             propossalPublicKeys[i] = args[3 + i*6];
         }
 
         for (int i = 0; i < numVotes; i++) {
             propossalIds[i] = args[4 + i*6];
         }
 
         for (int i = 0; i < numVotes; i++) {
             aFactors[i] = args[5 + i*6];
         }
 
         for (int i = 0; i < numVotes; i++) {
             dFactors[i] = args[6 + i*6];
         }
 
         for (int i = 0; i < numVotes; i++) {
             uFactors[i] = args[7 + i*6];
         }
 
         // Verify
         String result = verifier.verify(serializedCertificate, signature, votes,
             propossalPublicKeys, propossalIds, aFactors, dFactors, uFactors);
         System.out.println(result);
         System.exit(result != "FAIL" ? 0 : 1);
         return;
     }
 }
