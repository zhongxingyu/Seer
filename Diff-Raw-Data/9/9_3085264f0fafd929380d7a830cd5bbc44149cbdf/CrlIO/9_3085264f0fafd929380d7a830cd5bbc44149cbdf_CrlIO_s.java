 package ajeetmurty.reference.java.crypt;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509CRL;
 import java.security.cert.X509CRLEntry;
 import java.util.Set;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CrlIO {
 	private final Logger logp = LoggerFactory.getLogger(this.getClass().getName());
 	// CRL file download: http://www.certificates-australia.com.au/general/cert_download.shtml
	private final String inputCrlFilePath = "src/main/resources/VerizonAU_SecureNet-Limited-Root-CA.crl";
 
 	public static void main(String[] args) {
 		new CrlIO();
 	}
 
 	public CrlIO() {
 		logp.info("start");
 		readCrlFile();
 		logp.info("stop");
 	}
 
 	private void readCrlFile() {
 		try {
 			logp.info("input crl file path: " + inputCrlFilePath);
 			File crlFile = new File(inputCrlFilePath);
 			if (crlFile.isFile() && crlFile.canRead()) {
 				InputStream inStream = new FileInputStream(crlFile);
 				CertificateFactory cf = CertificateFactory.getInstance("X.509");
 				X509CRL crl = (X509CRL) cf.generateCRL(inStream);
 				inStream.close();
 
 				logp.info(String.format("crl info: thisUpdate|nextUpdate|issuerDN : %1$s|%2$s|%3$s.", crl.getThisUpdate().toString(), crl.getNextUpdate().toString(), crl.getIssuerX500Principal().getName()));
 
 				Set<? extends X509CRLEntry> revokedCerts = crl.getRevokedCertificates();
 				for (X509CRLEntry crlEntry : revokedCerts) {
 					logp.info("revoked cert serial: " + crlEntry.getSerialNumber());
 				}
 			} else {
 				logp.error("input crl file not found/not readable");
 			}
 		} catch (Exception e) {
 			logp.error(e.getMessage(), e);
 		}
 	}
 }
