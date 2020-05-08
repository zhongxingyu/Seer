 package nl.nikhef.jgridstart.ca;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 
 import nl.nikhef.jgridstart.osutils.ConnectionUtils;
 
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.X509Principal;
 
 import nl.nikhef.jgridstart.util.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 
 /**
  * This class is used to download a certificate from the Test CA
  * 
  * @author wvengen
  * 
  */
 public class TestCA implements CA {
     
     static final protected Logger logger = Logger.getLogger(TestCA.class.getName());
     
     /** Base URL of certificate authority */
     protected String base = System.getProperty("jgridstart.ca.base");
     /** URL of CA cert */
     protected String baseCaCert = System.getProperty("jgridstart.ca.base.cacert");
     /** CA certificate (cached) */
     protected static X509Certificate cacert = null;
     /** CA DN */
     protected String caDN = System.getProperty("jgridstart.ca.dn");
 
     /** Create new TestCA */
     public TestCA() throws NoSuchAlgorithmException, KeyManagementException {
 	if (base==null) {
 	    base = "http://www.nikhef.nl/~wvengen/testca/";
 	    System.setProperty("jgridstart.ca.base", base);
 	}
 	if (baseCaCert==null) {
 	    baseCaCert = base + "?action=retrieve_ca_cert&install=true";
 	    System.setProperty("jgridstart.ca.base.cacert", baseCaCert);
 	}
 	if (caDN==null) {
 	    caDN = "C=NL, ST=NH, L=Amsterdam, O=ChangeMeOrg, CN=changeme, emailAddress=mail@example.org";
 	    System.setProperty("jgridstart.ca.dn", caDN);
 	}
     }
     
     /** Just returns the PEM encoded version of the request. */
     public String encodeCertificationRequest(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	StringWriter out = new StringWriter();
 	PEMWriter writer = new PEMWriter(out);
 	writer.writeObject(req);
 	writer.close();
 	return out.toString();
     }
     
     /** TestCA does not handle renewals differently than ordinary requests. */
     public String signCertificationRequest(
 	    PKCS10CertificationRequest req, Properties info,
 	    PrivateKey oldKey, X509Certificate oldCert) throws IOException {
 	return encodeCertificationRequest(req, info);
     }
     
     /** Uploads a user certificate signing request onto the Test CA
      * 
      * @param req {@inheritDoc} request returned by encode/sign
      * @param info {@inheritDoc}; {@code email} and {@code fullname} are used here
      */
     public void uploadCertificationRequest(String req, Properties info) throws IOException {
 	String[] postdata = {
 		"action", "submit",
 		"fullname", info.getProperty("request.fullname"),
 		"email", info.getProperty("email"),
 		"request", req,
 		"submit", "Submit request"
 	};
 
 	URL url = new URL(base);
 	String answer = ConnectionUtils.pageContents(url, postdata, true);
 
 	String serial = null;
 
 	// TODO dodgy but I don't know how else to do it.
 	try {
 	    String matchstr = "Saving request as";
 	    int index = answer.indexOf(matchstr);
 	    if (index == -1 || answer.indexOf("error:") != -1 || answer.indexOf("Error:") != -1) {
 		// means: not successful
 		logger.severe("Could not upload certification request.");
 		throw new /*UnableToUploadCertificationRequestException*/IOException(answer);
 	    }
 	    int index_end = answer.indexOf(".", index);
 	    serial = answer.substring(index+matchstr.length(), index_end).trim();
 	} catch (RuntimeException e) {
 	    logger.severe(e.getMessage());
 	    throw e;
 	}
 
 	logger.info("Uploaded certificate signing request with serial "+serial);
 
 	info.setProperty("request.serial", serial);
     }
     
     public boolean isCertificationRequestProcessed(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	return downloadCertificate(req, info) != null;
     }
 
     /** Download a certificate from the Test CA
      * 
      * @param req {@inheritDoc} (not used by TestCA)
      * @return {@inheritDoc}
      */
     public X509Certificate downloadCertificate(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	
 	String reqserial = info.getProperty("request.serial");
 	if (reqserial==null || reqserial.equals(""))
 	    throw new IOException("Cannot download certificate without request serial number");
 
 	// return certificate by serial
 	URL url = new URL(base);
 	String[] pre = new String[] {
 		"action", "retrieve_cert",
 		"serial", reqserial
 	};
 	String scert = ConnectionUtils.pageContents(url, pre, false);
 	StringReader reader = new StringReader(scert);
 	PEMReader r = new PEMReader(reader);
 	X509Certificate cert = (X509Certificate)r.readObject();
 	r.close();
 	if (cert==null)
 	    throw new IOException("Certificate could not be retrieved: "+scert);
 	return cert;
     }
     
     /** {@inheritDoc}
      * <p>
      * Test CA certificate is downloaded once each program run.
      */
     public X509Certificate getCACertificate() throws IOException {
 	if (cacert==null) {
 	    // download when not present
 	    String scert = ConnectionUtils.pageContents(new URL(baseCaCert));
 	    StringReader reader = new StringReader(scert);
 	    PEMReader r = new PEMReader(reader);
 	    cacert = (X509Certificate)r.readObject();
 	    r.close();
 	    if (cacert==null)
 		throw new IOException("CA certificate could not be retrieved: "+scert);
 	}
 	return cacert;
     }
 
     /** {@inheritDoc} */
     public boolean isIssuer(X509Certificate cert) {
 	// TODO cache X500Principal
 	String dn = caDN;
 	if (dn.trim().startsWith("/"))
 	    dn = dn.substring(1).replace('/', ',');
 	return cert.getIssuerDN().equals(new X509Principal(dn));
     }
 }
