 package nl.nikhef.jgridstart.ca;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 
 import nl.nikhef.jgridstart.util.ConnectionUtils;
 
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 
 /**
  * This class is used interface with the DutchGrid production CA
  * 
  * @author wvengen
  */
 public class DutchGridCA implements CA {
     
     static final protected Logger logger = Logger.getLogger(DutchGridCA.class.getName());
     
     /** CA entry point: submission */
     protected String baseSubmit = System.getProperty("jgridstart.ca.base.submit");
     /** CA entry point: query */
     protected String baseQuery = System.getProperty("jgridstart.ca.base.query");
     /** CA entry point: CA cert */
     protected String baseCaCert = System.getProperty("jgridstart.ca.base.cacert");
     
     /** CA certificate (cached) */
     protected static X509Certificate cacert = null;
 
    /** Create new DutchGridCA 
      * 
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException */
     public DutchGridCA() throws NoSuchAlgorithmException, KeyManagementException {
 	if (baseSubmit==null)
 	    baseSubmit = "http://ra.dutchgrid.nl/ra/public/submit";
 	if (baseQuery==null)
 	    baseQuery = "http://ca.dutchgrid.nl/medium/query/";
 	if (baseCaCert==null)
 	    baseCaCert = "https://ca.dutchgrid.nl/medium/cacert.pem";
     }
 
     /** Uploads a user certificate signing request onto the DutchGrid CA
      * 
      * @param req {@inheritDoc}
      * @param info {@inheritDoc}; only "email" is used here.
      * @return {@inheritDoc}
      */
     public String uploadCertificationRequest(
 	    PKCS10CertificationRequest req, Properties info) throws IOException {
 	
 	String name = req.getCertificationRequestInfo().getSubject().getValues(X509Principal.CN).get(0).toString();
 	
 	StringWriter reqWriter = new StringWriter();
 	PEMWriter w = new PEMWriter(reqWriter);
 	w.writeObject(req);
 	w.close();
 	
 	String[] postdata = {
 		"action", "submit",
 		"fullname", name,
 		"email_1", info.getProperty("email"),
 		"email_2", info.getProperty("email"),
 		"requesttext", reqWriter.toString(),
 		"Publish", "Upload Publishing Data",
		"robot", "yes"
 	};
 
 	URL url = new URL(baseSubmit);
 	String answer = ConnectionUtils.pageContents(url, postdata, true);
 
	// returns "200 ..." on success
	// TODO CAException instead of IOException
	if (!answer.startsWith("200"))
		throw new IOException("Certificate signing request upload failed:\n"+answer);
 
 	logger.info("Uploaded certificate signing request");
 
	// ok; serial is not used here
	return null;
     }
     
     public boolean isCertificationRequestProcessed(
 	    PKCS10CertificationRequest req, String reqserial) throws IOException {
 	return downloadCertificate(req, reqserial) != null;
     }
 
     /** Download a certificate from the DutchGrid CA
      * 
      * @param req {@inheritDoc}
      * @param reqserial {@inheritDoc} (not used by DutchGridCA)
      * @return {@inheritDoc}
      */
     public X509Certificate downloadCertificate(
 	    PKCS10CertificationRequest req, String reqserial) throws IOException {
 	
 	// return certificate by serial
 	URL url = new URL(baseQuery);
 	String[] pre;
 	try {
 	    pre = new String[] {
 	    	"id", ((RSAPublicKey)req.getPublicKey()).getModulus().toString(16),
 	    	"fmt", "single"
 	    };
 	} catch (Exception e) {
 	    throw new IOException(e.getLocalizedMessage());
 	}
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
     * CA certificate is downloaded once each program run.
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
 }
