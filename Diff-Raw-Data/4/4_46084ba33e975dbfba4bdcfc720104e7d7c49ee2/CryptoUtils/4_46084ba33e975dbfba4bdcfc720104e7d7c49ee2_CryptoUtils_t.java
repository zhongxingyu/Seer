 package nl.nikhef.jgridstart.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.cert.CertStore;
 import java.security.cert.CollectionCertStoreParameters;
 import java.security.cert.X509Certificate;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
 import org.bouncycastle.cms.CMSException;
 import org.bouncycastle.cms.CMSProcessableByteArray;
 import org.bouncycastle.cms.CMSSignedData;
 import org.bouncycastle.cms.CMSSignedDataGenerator;
 import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.mail.smime.SMIMEException;
 import org.bouncycastle.operator.ContentSigner;
 import org.bouncycastle.operator.OperatorException;
 import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
 import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
 import org.bouncycastle.util.encoders.Base64;
 
 /** Cryptographic utilities */
 public class CryptoUtils {
 
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
     
     /** Return a certificate's subject hash.
      * <p>
      * This is equal to the value computed by {@code openssl x509 -hash -noout -in cert.pem}.
      * @throws NoSuchAlgorithmException 
      * @throws IOException 
      */
     public static String getSubjectHash(X509Certificate cert) throws NoSuchAlgorithmException, IOException {
 	return String.format("%08x", getX509NameHash(cert.getSubjectX500Principal()));
     }
     /** Return a certificate signing request's subject hash.
      * <p>
      * Same as {@link #getSubjectHash} but for a CSR. No openssl equivalent.
      * @throws NoSuchAlgorithmException 
      * @throws NoSuchAlgorithmException 
      * @throws IOException 
      */
     public static String getSubjectHash(PKCS10CertificationRequest req) throws NoSuchAlgorithmException, IOException {
 	return String.format("%08x", getX509NameHash(req.getCertificationRequestInfo().getSubject()));
     }
     /** Return a certificate's issuer hash.
      * <p>
      * This is equal to the value computed by {@code openssl x509 -issuer_hash -noout -in cert.pem}.
      * @throws NoSuchAlgorithmException 
      * @throws IOException 
      */
     public static String getIssuerHash(X509Certificate cert) throws NoSuchAlgorithmException, IOException {
 	return String.format("%08x", getX509NameHash(cert.getIssuerX500Principal()));
     }
 
     
     /** Return the hash of an {@linkplain X509Name}.
      * <p>
      * Same as openssl's {@code X509_NAME_hash()}.
      * 
      * @throws NoSuchAlgorithmException 
      * @throws IOException 
      */
     public static long getX509NameHash(X509Name p) throws NoSuchAlgorithmException, IOException {
 	byte[] hash = MessageDigest.getInstance("MD5").digest(p.getEncoded());
 	long a = 0;
 	a |=  (long)hash[0]&0xffL;
 	a |= ((long)hash[1]&0xffL)<<8L;
 	a |= ((long)hash[2]&0xffL)<<16L;
 	a |= ((long)hash[3]&0xffL)<<24L;
 	return a;
     }
     /** Return the hash of an {@linkplain X509Name}.
      * @see #getX509NameHash
      * @throws NoSuchAlgorithmException
      * @throws IOException 
      */
     public static long getX509NameHash(X500Principal p) throws NoSuchAlgorithmException, IOException {
 	return getX509NameHash(new X509Principal(p.getEncoded()));
     }
 
     /** Return number of millseconds that certificate is still valid */
     public static long getX509MillisecondsValid(X509Certificate cert){
 	Calendar now = Calendar.getInstance();
 	Calendar end = Calendar.getInstance();
 	end.setTime(cert.getNotAfter());
 	return end.getTimeInMillis() - now.getTimeInMillis(); 
     }
     /** Return number of days that certificate is still valid */
     public static long getX509DaysValid(X509Certificate cert) {
 	return getX509MillisecondsValid(cert) / (1000*60*60*24);
     }
 
     /**
      * S/MIME sign a string.
      * <p>
      * The output is a MIME message consisting of two parts. The first is the
      * MIME-encoded string supplied as input, the second is the message signed
      * by the key with certificate embedded.
      * <p>
      * Example output:
      * <code>
      * MIME-Version: 1.0
      * Content-Type: multipart/signed; protocol="application/pkcs7-signature"; micalg=sha1; 
      * 	boundary="----=_Part_1_190331520.1253198584335"
      * 
      * ------=_Part_1_190331520.1253198584335
      * Content-Type: text/plain; charset=us-ascii
      * Content-Transfer-Encoding: 7bit
      * 
      * This is my important piece of text.
      * 
      * ------=_Part_1_190331520.1253198584335
      * Content-Type: application/pkcs7-signature; name=smime.p7s; smime-type=signed-data
      * Content-Transfer-Encoding: base64
      * Content-Disposition: attachment; filename="smime.p7s"
      * Content-Description: S/MIME Cryptographic Signature
      * 
      * OTAzMDMwMDAwMDBaFw0xMDAzMDMxMDQ5MDFaMFAxEjAQBgNVBAoTCWR1dGNoZ3JpZDEOMAwGA1UE
      * ChMFdXNlcnMxDzANBgNVBAoTBm5pa2hlZjEZMBcGA1UEAxMQV2lsbGVtIHZhbiBFb.......etc.
      * yAAAAAAAAA==
      * ------=_Part_1_190331520.1253198584335--
      * </code>
      */
     public static String SignSMIME(String msg, PrivateKey key, X509Certificate cert) throws GeneralSecurityException, SMIMEException, IOException {
 	try {
 	    ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(key);
 	    JcaX509CertificateHolder certHolder = new JcaX509CertificateHolder(cert);
	    
	    // BC 1.46 appears to require "\r\n" line-endings
	    msg = msg.replaceAll("(?<!\r)\n", "\r\n");
	    msg = msg.replaceAll("\r(?!\n)", "\r\n");
 	
 	    String tosign = 
 		"Content-Type: text/plain; charset=us-ascii\r\n" +
 		"Content-Transfer-Encoding: 7bit\r\n" +
 		"\r\n" +
 		msg;
 
 	    CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
 	    gen.addSignerInfoGenerator(new SignerInfoGeneratorBuilder(
 		new BcDigestCalculatorProvider()).build(sigGen, certHolder));
 	    CertStore certStore = CertStore.getInstance("Collection",
 		new CollectionCertStoreParameters(Arrays.asList(cert)), "BC");
 	    gen.addCertificatesAndCRLs(certStore);
 	
 	    CMSSignedData signed = gen.generate(new CMSProcessableByteArray(tosign.getBytes()));
 
 	    // TODO make sure not in body or signature	
 	    String mimeboundary = "------------mime_boundary_12345";
 	
 	    return
 		"MIME-Version: 1.0\r\n" +
 		"Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1; boundary=\""+mimeboundary+"\"\r\n" +
 		"\r\n" +
 		"--" + mimeboundary + "\r\n" +
 		tosign + "\r\n" +
 		"--" + mimeboundary + "\r\n" +
 		"Content-Type: application/x-pkcs7-signature; name=\"smime.p7s\"; smime-type=signed-data\r\n" +
 		"Content-Transfer-Encoding: base64\r\n" +
 		"\r\n" +
 		base64encode(signed.getEncoded()) + "\r\n" +
 		"--" + mimeboundary + "--\r\n";
 	} catch (CMSException e) {
 		throw new SMIMEException(e.getMessage(), e);
 	} catch (OperatorException e) {
 		throw new SMIMEException(e.getMessage(), e);
 	}
     }
     
     protected static String base64encode(byte[] data) {
 	return base64encode(data, 64);
     }
     protected static String base64encode(byte[] data, int len) {
 	byte[] base64 = Base64.encode(data);
 	StringBuffer out = new StringBuffer();
 	for (int i=0; i<base64.length; i++) {
 	    if (i>0 && i%len == 0)
 		out.append("\r\n");
 	    out.append((char)base64[i]);
 	}
 	return out.toString();
     }    
 }
