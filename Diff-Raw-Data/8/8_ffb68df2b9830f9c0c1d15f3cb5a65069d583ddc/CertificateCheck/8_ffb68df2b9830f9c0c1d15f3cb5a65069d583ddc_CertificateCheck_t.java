 package nl.nikhef.jgridstart;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigInteger;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.interfaces.DSAParams;
 import java.security.interfaces.DSAPrivateKey;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.RSAPrivateKey;
 import java.security.interfaces.RSAPublicKey;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import nl.nikhef.jgridstart.util.PEMReader;
 import nl.nikhef.jgridstart.util.PasswordCache;
 import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
 
 /** Security checks for a certificate directory.
  * <p>
  * All checks have a void return type and throw an (TODO x) Exception on failure.
  */
 public class CertificateCheck {
     
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");
 
 
     /** CertificatePair under investivation */
     protected CertificatePair cert = null;
 
     /** Create a new instance. */
     public CertificateCheck(CertificatePair c) {
 	cert = c;
     }
     
     /** Run the default checks.
      * <p>
      * Exceptions work such that only one exception is shown at once.
      * So the order in which the checks appear <em>is</em> important.
      */
     public void check() throws CertificateCheckException {
 	checkAccessPath();
 	checkPrivateKey();
 	checkCSR();
 	checkCertificate();
     }
     
     /** Run the private key checks.
      * <p>
      * This is a separate method from {@linkplain #check} because it
      * requires the private key's password so either make sure the
      * user is expecting a password dialog, or the password is already
      * present in the {@link PasswordCache}. 
      */
     public void checkPrivate() throws CertificateCheckException {
 	checkPrivateKeyDecryptValid();
 	if (cert.getCertFile().exists()) checkPrivateKeyMatchesCertificate();
     }
     
     /** Check access to certificate directory. Must exist. */
     protected void checkAccessPath() throws CertificateCheckException {
 	File f = cert.getPath();
 	if (!f.exists())
 	    fail("Certificate directory not found", f);
 	if (!f.isDirectory())
 	    fail("Certificate directory is not a directory", f);
 	if (!f.canRead())
 	    fail("Certificate directory cannot be read", f);
     }
 
     /** Check that the private key is valid.
      * <p>
      * It only checks that a private key file is present and has a
      * valid format, not if it can be decrypted since we don't want
      * to use the password.
      */
     protected void checkPrivateKey() throws CertificateCheckException {
 	File f = cert.getKeyFile();
 	if (!f.exists())
 	    fail("Private key not found", f);
 	if (!f.isFile())
 	    fail("Private key is not a file", f);
 	if (!f.canRead())
 	    fail("Private key cannot be read", f);
 	// TODO check that others cannot read this key!
 	try {
 	    Object o = PEMReader.readObject(f, KeyPair.class);
 	    if (o==null)
 		fail("Private key file contains no private key", f);
 	} catch (PasswordCancelledException e) {
 	    // is ok :)
 	} catch (IOException e) {
 	    throw new CertificateCheckException(e);
 	}
     }
 
     /** Check the certificate.
      * <p>
      * This is only checked if the certificate really
      * exists, because it is optional (e.g. when the certificate signing
      * request was made but the certificate not received from the CA).
      */
     protected void checkCertificate() throws CertificateCheckException {
 	File f = cert.getCertFile();
 	// certificate request must exist when no certificate is present
 	if (!f.exists())
 	    return;
 	if (!f.isFile())
 	    fail("Certificate is not a file", f);
 	if (!f.canRead())
 	    fail("Certificate cannot be read", f);
 	// check if certificate was loaded or can be loaded
 	try {
 	    if (cert.getCertificate() == null)
 	        fail("Certificate file contains no certificate", f);
 	} catch (IOException e) {
 	    throw new CertificateCheckException(e);
 	}
     }
     
     /** Check the certificate signing request (CSR).
      * <p>
      * This is only checked if the certificate does not exist, since
      * only then is the CSR relevant.
      */
     protected void checkCSR() throws CertificateCheckException {
 	File f = cert.getCSRFile();
	// certificate request only really should exist when no certificate is present
 	try {
 	    if (cert.getCertificate()!=null) 
 	    	return;
 	} catch(IOException e) { /* need to check CSR as well */ }
 
 	if (!f.exists())
 	    fail("Certificate nor certificate signing request found", f);
 	if (!f.isFile())
 	    fail("Certificate signing request is not a file", f);
 	if (!f.canRead())
 	    fail("Certificate signing request cannot be read", f);
 	// check if certificate was loaded or can be loaded
 	try {
 	    if (cert.getCSR() == null)
 	        fail("Certificate signing request file contains no request", f);
 	} catch (IOException e) {
 	    throw new CertificateCheckException(e);
 	}
     }
     
     /** Check if the decrypted private key is valid.
      * <p>
      * This requires the private key to be decrypted so a password
      * may be asked.
      */
     protected void checkPrivateKeyDecryptValid() throws CertificateCheckException {
 	try {
 	    cert.getPrivateKey();
 	} catch (PasswordCancelledException e) {
 	} catch (IOException e) {
 	    throw new CertificateCheckException(e);
 	}
     }
     
     /** Check if the private key and certificate belong together.
      * <p>
      * This requires the private key to be decrypted so a password
      * may be asked.
      * <p>
      * TODO don't use IOException
      */
     protected void checkPrivateKeyMatchesCertificate() throws CertificateCheckException {
 	try {
 	    PublicKey pub = cert.getCertificate().getPublicKey();
 	    PrivateKey priv = cert.getPrivateKey();
 	    
 	    if (!pub.getAlgorithm().equals(priv.getAlgorithm()))
 		    fail("Private key doesn't belong to certificate (algorithm)");
 	    
 	    if (pub.getAlgorithm().equals("DSA")) {
 		DSAPublicKey dpub = (DSAPublicKey)pub;
 		DSAPrivateKey dpriv = (DSAPrivateKey)priv;
 		DSAParams dpubp = dpub.getParams();
 		DSAParams dprivp = dpriv.getParams();
 		// private key has no getY() interface :(
 		BigInteger dprivY = dprivp.getG().modPow(dpriv.getX(), dprivp.getP());
 		if ( !dpubp.getG().equals(dprivp.getG()) ||
 		     !dpubp.getP().equals(dprivp.getP()) ||
 		     !dpubp.getQ().equals(dprivp.getQ()) ||
 		     !dpub.getY().equals(dprivY))
 		    fail("Private key doesn't belong to certificate (DSA params)");
 		    
 	    } else if (pub.getAlgorithm().equals("RSA")) {
 		RSAPublicKey rpub = (RSAPublicKey)pub;
 		RSAPrivateKey rpriv = (RSAPrivateKey)priv;
 		if (!rpub.getModulus().equals(rpriv.getModulus()))
 			fail("Private key doesn't belong to certificate (modulus)");
 		
 	    } else 
 		fail("Unsupported key format");
 	} catch (PasswordCancelledException e) {
 	} catch (IOException e) {
 	    throw new CertificateCheckException(e);
 	}
     }
     
     /** Called when a check fails, throws an Exception. */
     protected void fail(String msg) throws CertificateCheckException {
 	_fail(msg+": "+cert);
     }
     /** Called when a check fails, throws an Exception. */
     protected void fail(String msg, File f) throws CertificateCheckException {
 	_fail(msg+": "+f);
     }
     /** Called when a check fails, throws an Exception (low level, don't use) */
     private void _fail(String msg) throws CertificateCheckException {
 	logger.warning("Check failed: "+msg);
 	throw new CertificateCheckException(msg);
     }
     
     /** Exception thrown when a test fails */
     public class CertificateCheckException extends Exception {
 	protected CertificateCheckException(String reason) {
 	    super(reason);
 	}
 	protected CertificateCheckException(Exception e) {
 	    super(e);
 	}
     }
     
     //
     // Command-line test tool; called from jgridstart.sh
     //
     
     /** Test program */
     public static void main(String[] args) throws Exception {
 	boolean checkPrivate = false;
 	String dir = null;
 	try {
 	    final Options opts = new Options();
 
 	    opts.addOption(new Option("h", "help", false, "show help message"));
 	    opts.addOption(new Option("p", "private", false, "decrypt private key (password required)"));
 
 	    CommandLineParser parser = new GnuParser();
 	    CommandLine line = parser.parse(opts, args);
 
 	    if (line.getArgs().length==0 || line.hasOption("help")) {
 		actionHelp(line, opts);
 		System.exit(0);
 	    }
 	    dir = line.getArgs()[0];
 	    if (line.hasOption("private"))
 		checkPrivate = true;
 	} catch (ParseException e) {
 	    System.err.println(e.getLocalizedMessage());
 	}
 	// now check the supplied path; CertificatePair checks for itself, btw
 	try {
 	    CertificatePair cert = new CertificatePair(new File(dir));
 	    if (checkPrivate)
 		new CertificateCheck(cert).checkPrivate();
 	    
 	    System.out.println("ok");
 	} catch (IOException e) {
 	    System.err.println(e.getLocalizedMessage());
 	    System.exit(1);
 	}
 	
 	System.exit(0);
     }
 
     /** Show help message */
     protected static void actionHelp(CommandLine line, Options opts) {
 	// figure out executable name; shellscript sets variable to aid in this
 	String prog = System.getenv("INVOKED_PROGRAM");
 	if (prog==null) prog = "java "+CertificateCheck.class.getName();
 	// print help
 	HelpFormatter fmt = new HelpFormatter();
 	if (System.getenv("COLUMNS")!=null)
 	    fmt.setWidth(Integer.valueOf(System.getenv("COLUMNS")));
 	PrintWriter out = new PrintWriter(System.out, true);
 	out.println("usage: "+prog+" [-p] <globus certificate dir>");
 	fmt.printOptions(out, fmt.getWidth(), opts, fmt.getLeftPadding(), fmt.getDescPadding());
 	System.exit(0);
     }	
 }
