 package nl.nikhef.jgridstart;
 
 import java.awt.ItemSelectable;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.KeyException;
 import java.security.KeyManagementException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Security;
 import java.security.SignatureException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.X509Certificate;
 import java.security.interfaces.RSAPublicKey;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import nl.nikhef.jgridstart.ca.*;
 import nl.nikhef.jgridstart.util.CryptoUtils;
 import nl.nikhef.jgridstart.util.PasswordCache;
 import nl.nikhef.jgridstart.util.PrivateFileWriter;
 import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
 
 import org.bouncycastle.asn1.DERSet;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.x509.KeyPurposeId;
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.PrincipalUtil;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 /**
  * Class containing everything related to a grid certificate. Each instance is
  * directly bound to a ~/.globus/xxx directory containing the relevant files (or
  * another base directory, which is the CertificateStore). This is at least a
  * private key, and usually a certificate. A log can be expected as well (this
  * class logs all actions to it) and a certificate signing request can be
  * present too.
  * 
  * This class provides actions like request, revoke, import, export, etc.
  * 
  * This class is also a child of Properties. One can set and get any property
  * desired, but some are specifically reserved and queried directly from the
  * certificate and/or certificate signing request. Please see getProperty().
  * When the object is loaded, properties found in the file indicated  by
  * getPropertiesFile() are set. On destruction, the properties are written
  * back as to provide transparent presistency. When a property shouldn't be
  * written, one can set the property name with ".volatile" appended to "true"
  * to make the property not persistent, e.g. with
  * <code>
  *   foo.html=&gt;b&lt;hi there&gt;/b&lt;
  *   foo.html.volatile=true
  * </code>
  * the variable "foo.html" is not saved back to the properties file.
  * 
  * 
  * After some more thinking this could actually involve a Java KeyStore as a
  * custom backend for ~/.globus containing just a private key and a certificate.
  * Then custom extensions can retrieve the CSR and other info. This would allow
  * one to use other types of 'definitive' storage as well, like using a PKCS#12
  * certificate instead of userkey.pem and certificate.pem for jGlobus, as
  * mentioned on a mailing-list. For now I'll keep it as it is.
  * 
  * @author wvengen
  */
 public class CertificatePair extends Properties implements ItemSelectable {
 
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart");
 
     /** The directory that represents this CertificatePair. */
     protected File path = null;
 
     /** The certificate. It is cached because it contains most important info. */
     protected X509Certificate cert = null;
     /** If no certificate exists we can get info from the CSR */
     protected PKCS10CertificationRequest req = null;
 
     // we need BouncyCastle as a provider for PKCS#12 keystore import/export
     static {
 	if (Security.getProvider("BC") == null)
 	    Security.addProvider(new BouncyCastleProvider());
     }
 
     /** Create new empty certificate pair */
     private CertificatePair() {
 	super();
 	Runtime.getRuntime().addShutdownHook(new Thread() {
 	   public void run() {
 	       if (path!=null) {
 		   try {
 		       store();
 		   } catch (IOException e) { }
 	       }
 	   }
 	});
     }
 
     /** New certificate pair of a directory */
     public CertificatePair(File f) throws IOException {
 	this();
 	load(f);
     }
     
     /** Return the value of a property. Usually this just returns the
      * value set by setProperty, but there are some cases where the
      * value is taken directly from the certificate or certificate
      * signing request. Please see getSubjectPrincipalValue() for more info.
      * 
      * - valid
      *     is "true" if the certificate is valid, null otherwise
      * - valid.notafter, valid.notbefore
      *     localised validity interval
      * - cert, request
      *     is "true" when certificate respectively CSR are present
      * - subject, issuer, usage
      *     is "true" when child keys can be present
      * - usage.any, usage.serverauth, usage.clientauth, usage.codesigning,
      *   usage.emailprotection, usage.timestamping, usage.ocspsigning
      *     are "true" when they are defined in the extended key usage
      * - modulus, modulus.first20
      *     the public key's modulus, and its first 20 characters
      * - org
      *     is returned when set, or guessed from subject if unset
      *     
      * You can postfix each property with ".html" to get an html representation.
      * If no html representation is present, it just returns the same as the
      * property without ".html".
      * 
      * @param key property to get the value of
      * @return value of the property, or null if not found.
      */
     public String getProperty(String key) {
 	// parse html
 	if (key.endsWith(".html")) {
 	    String r = getPropertyHtml(key.substring(0, key.length()-5));
 	    if (r!=null) return r;
 	}
 	// return generated property
 	if (key.equals("cert"))
 	    if (cert==null) return null;
 	    else return "true";
 	if (key.equals("request"))
 	    if (!getCSRFile().exists()) return null;
 	    else return "true";
 	if (key.equals("subject"))
 	    if (cert==null && req==null) return null;
 	    else return getSubjectPrincipalValue("x-full");;
 	if (key.startsWith("subject."))
 	    return getSubjectPrincipalValue(key.substring(8));
 	if (key.equals("issuer"))
 	    if (cert==null) return null;
 	    else return "true";
 	if (key.startsWith("issuer."))
 	    return getIssuerPrincipalValue(key.substring(7));
 	if (key.equals("org")) {
 	    if (containsKey(key)) return super.getProperty(key);
 	    if (getProperty("subject")==null) return null;
 	    // return last defined organisation
 	    // TODO doesn't work when organisation unit is involved; need to move to Organisation
 	    String[] orgs = getProperty("subject.o").split(",\\s*");
 	    return orgs[orgs.length-1];
 	}
 	try {
 	    if (key.equals("modulus"))
 		if (cert!=null) return ((RSAPublicKey)cert.getPublicKey()).getModulus().toString();
 		else if (req!=null) return ((RSAPublicKey)req.getPublicKey()).getModulus().toString();
 	    if (key.equals("modulus.first20"))
 		if (cert!=null) return ((RSAPublicKey)cert.getPublicKey()).getModulus().toString().substring(0,20);
 		else if (req!=null) return ((RSAPublicKey)req.getPublicKey()).getModulus().toString().substring(0,20);
 	    else return null;
 	    if (key.equals("valid")) {
 		if (cert==null) return null;
 		try { cert.checkValidity(); }
 		catch(CertificateExpiredException e) { return null; }
 		catch(CertificateNotYetValidException e) { return null; }
 		return "true";
 	    }
 	    if (key.equals("valid.notbefore")) {
 		if (cert==null) return null;
 		return DateFormat.getDateInstance().format(cert.getNotBefore());
 	    }
 	    if (key.equals("valid.notafter")) {
 		if (cert==null) return null;
 		return DateFormat.getDateInstance().format(cert.getNotAfter());
 	    }
 	    if (key.startsWith("usage")) {
 		if (cert==null) return null;
 		List<String> usage = cert.getExtendedKeyUsage();
 		if (usage==null) return null;
 		if ( key.equals("usage") ||
 		     (key.equals("usage.any")
 			&& usage.contains(KeyPurposeId.anyExtendedKeyUsage.toString())) ||
 		     (key.equals("usage.serverauth")
 			&& usage.contains(KeyPurposeId.id_kp_serverAuth.toString())) ||
 		     (key.equals("usage.clientauth")
 			 && usage.contains(KeyPurposeId.id_kp_clientAuth.toString())) ||
 		     (key.equals("usage.codesigning")
 			&& usage.contains(KeyPurposeId.id_kp_codeSigning.toString())) ||
 		     (key.equals("usage.emailprotection")
 			&& usage.contains(KeyPurposeId.id_kp_emailProtection.toString())) ||
 		     (key.equals("usage.timestamping")
 			&& usage.contains(KeyPurposeId.id_kp_timeStamping.toString())) ||
 		     (key.equals("usage.ocspsigning")
 			&& usage.contains(KeyPurposeId.id_kp_OCSPSigning.toString())) )
 		    return "true";
 		return null;
 	    }
 	} catch (Exception e) {
 	    return null;
 	}
 	// else return property set before with setProperty()
 	return super.getProperty(key);
     }
     /** Return a property in html format, or null if not defined. */
     protected String getPropertyHtml(String key) {
 	// hyperlink organisations
 	if (key.endsWith(".o")) {
 	    String sorgs = getProperty(key);
 	    if (sorgs==null) return null;
 	    String[] orgs = sorgs.split(",\\s*");
 	    sorgs = "";
 	    for (int i=0; i<orgs.length; i++) {
 		Organisation org = Organisation.get(orgs[i]);
 		try {
		    sorgs += org.getNameHTML() + ", ";
 		} catch (Exception e) { }
 	    }
	    return sorgs.substring(0, sorgs.length()-2).trim();
 	}
 	// nothing by default
 	return null;
     }
 
     /** reset the contents to this object to the empty state */
     public void clear() {
 	path = null;
 	cert = null;
 	req = null;
 	super.clear();
     }
 
     /** Load a certificate from a directory */
     protected void load(File f) throws IOException {
 	clear();
 	path = f;
 
 	// make sure it's ok
 	new SecurityChecks(this).checkAll();
 
 	// read certificate or else CSR, not fatal if they don't exist. 
 	if (getCertFile().exists()) {
 	    cert = (X509Certificate)CryptoUtils.readPEM(
 		    new FileReader(getCertFile()), null);
 	} else if (getCSRFile().exists()) {
 	    req = (PKCS10CertificationRequest) CryptoUtils.readPEM(
 		    new FileReader(getCSRFile()), null);
 	}
 	// read additional properties, not fatal if not present
 	if (getPropertiesFile().exists()) {
 	    super.load(new FileInputStream(getPropertiesFile()));
 	}
 	notifyChanged();
     }
     
     /** Store the properties in the file indicated by getPropertiesFile().
      * This file is written with permissions so that only the user can read
      * it, because it may contain personal information. 
      * 
      * @throws IOException 
      * @throws FileNotFoundException */
     public void store() throws FileNotFoundException, IOException {
 	Properties p = (Properties)CertificatePair.super.clone();
 	// remove volatile properties
 	ArrayList<String> propsToRemove = new ArrayList<String>();
 	for (Enumeration<Object> en=p.keys(); en.hasMoreElements(); ) {
 	    String key = (String)en.nextElement();
 	    if (Boolean.valueOf(p.getProperty(key+".volatile")))
 		propsToRemove.add(key);
 	}
 	for (Iterator<String> it = propsToRemove.iterator(); it.hasNext(); ) {
 	    String key = it.next();
 	    p.remove(key);
 	    p.remove(key+".volatile");
 	}
 	// and store with OutputStream for Java 1.5 and below
 	p.store(new PrivateFileWriter(getPropertiesFile()).getOutputStream(),
 		"jGridstart certificate properties");
     }
 
     /**
      * Import a CertificatePair from a keystore into a (new) directory.
      * 
      * @param src File to import from
      * @param dst Directory to import into. On success, this directory could be
      *            passed later to create a new CertificatePair which is equal to
      *            the one returned by this method.
      * @return a new CertificatePair representing the newly imported pair.
      * @throws IOException
      * @throws NoSuchAlgorithmException 
      * @throws PasswordCancelledException 
      * @throws CertificateException 
      * @throws NoSuchProviderException 
      * @throws KeyStoreException 
      * @throws UnrecoverableKeyException 
      */
     static public CertificatePair importFrom(File src, File dst)
 	    throws IOException, NoSuchAlgorithmException, PasswordCancelledException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, CertificateException {
 	if (!src.isFile())
 	    throw new IOException("Need file to import from: " + src);
 	if (!src.canRead())
 	    throw new IOException("Cannot read file to import from: " + src);
 
 	CertificatePair cert = new CertificatePair();
 	SecurityChecks checks = new SecurityChecks(cert);
 	cert.path = dst;
 	checks.checkAccessPath();
 
 	try {
 	    // try to read from PEM first
 	    cert.importFromPEM(src);
 	    cert.notifyChanged();
 	} catch (IOException e) {
 	    // try to read from PKCS#12
 	    cert.importFromPKCS(src);
 	    cert.notifyChanged();
 	}
 	
 	// run checks on imported certificate
 	checks.checkAll();
 
 	return cert;
     }
 
     /**
      * Import key and certificate from a PEM file, possibly overwriting the
      * current data.
      * 
      * @param src
      *            PEM file to import from
      * @throws IOException
      * @throws NoSuchAlgorithmException 
      * @throws PasswordCancelledException 
      */
     protected void importFromPEM(File src) throws IOException, NoSuchAlgorithmException, PasswordCancelledException {
 	Object o;
 	FileReader fr = new FileReader(src);
 	int count = 0;
 	// process all items in the file
 	while ((o = PasswordCache.getInstance().
 		readPEM(fr, src, "PEM certificate "+src.getName())) != null) {
 	    count++;
 	    if (o.getClass().isInstance(KeyPair.class)) {
 		// Extract and write private key
 		if (((KeyPair) o).getPrivate() != null) {
 		    PrivateKey privKey = ((KeyPair) o).getPrivate();
 		    PasswordCache.getInstance().writePEM(privKey, getKeyFile(),
 			    "private key for "+getPath().getName());
 		}
 	    } else if (o.getClass().isInstance(X509Certificate.class)) {
 		// Extract and write certificate
 		cert = (X509Certificate) o;
 		CryptoUtils.writePEM(cert, new FileWriter(getCertFile()));
 	    } else if (o.getClass()
 		    .isInstance(PKCS10CertificationRequest.class)) {
 		// Extract and write certificate signing request (CSR)
 		req = (PKCS10CertificationRequest) o;
 		CryptoUtils.writePEM(req, new FileWriter(getCSRFile()));
 	    }
 	}
 
 	if (count == 0)
 	    throw new IOException("not a PEM file: " + src);
     }
 
     protected void importFromPKCS(File src) throws IOException, PasswordCancelledException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
 	PasswordCache pwcache = PasswordCache.getInstance();
 	String storename = "PKCS#12 store " + src.getName();
 	KeyStore store = KeyStore.getInstance("PKCS12", "BC");
 	char[] pw = pwcache.getForDecrypt(storename, src.getCanonicalPath());
 	if (pw == null) {
 	    // user cancel
 	    throw new KeyStoreException("User cancelled password entry");
 	}
 	store.load(new FileInputStream(src), pw);
 
 	if (store.size() == 0)
 	    throw new IOException("Not a PKCS#12 file: " + src);
 
 	Certificate c = null;
 	int i = 1;
 	for (Enumeration<String> it = store.aliases(); it.hasMoreElements(); i++) {
 	    String alias = it.nextElement();
 	    if (store.isKeyEntry(alias)) {
 		// TODO warn against multiple keys
 		// TODO check it is a private key
 		/*
 		 * This could be done but passwords on entries aren't
 		 * very common and it would be a hassle to ask the user
 		 * again.
 		 * 
 		 * TODO more general framework that just tries all stored
 		 *      passwords if we have the keys in software but is
 		 *     careful for hardware devices
 		 */
 		Key key = store.getKey(alias, null);
 		CryptoUtils.writePEM(key, new FileWriter(getKeyFile()));
 	    }
 	    if ((c = store.getCertificate(alias)) != null) {
 		// TODO warn against multiple certificates
 		// TODO we want only lowest certificate for this key ...
 		// TODO check it really is an instance of X509Certificate
 		cert = (X509Certificate) c;
 		CryptoUtils.writePEM(cert, new FileWriter(getCertFile()));
 	    }
 	}
     }
 
     /** Export the certificate and private key to a file. Type is
      * detected from the filename.
      * 
      * @param src destination to export to
      * @throws CertificateException 
      * @throws NoSuchAlgorithmException 
      * @throws NoSuchProviderException 
      * @throws KeyStoreException 
      * @throws PasswordCancelledException 
      */
     public void exportTo(File dst) throws IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, PasswordCancelledException {
 	String ext = dst.getName().toLowerCase();
 	ext = ext.substring(ext.lastIndexOf('.')+1);
 	if (ext.equals("p12") || ext.equals("pfx")) {
 	    exportToPKCS(dst);
 	} else if (ext.equals("pem")) {
 	    // TODO
 	} else {
 	    throw new IOException("Cannot determine format to export to, unknown file extension: "+ext);
 	}
     }
     
     /** Export the certificate and private key to a PKCS#12 file 
      * @throws NoSuchProviderException 
      * @throws KeyStoreException 
      * @throws CertificateException 
      * @throws NoSuchAlgorithmException 
      * @throws PasswordCancelledException */
     protected void exportToPKCS(File dst) throws IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, PasswordCancelledException {
 
 	// Create certificate chain TODO do proper chain
 	X509Certificate[] certchain = {cert}; // TODO include chain?
 	
 	// Create PKCS12 keystore with password
 	KeyStore store = KeyStore.getInstance("PKCS12", "BC");
 	store.load(null, null);
 	store.setKeyEntry("Grid certificate", getPrivateKey(), null, certchain); // TODO proper alias
 	
 	// write file with password
 	PasswordCache pwcache = PasswordCache.getInstance();
 	String storename = "PKCS#12 store " + dst.getName();
 	char[] pw = pwcache.getForEncrypt(storename, dst.getCanonicalPath());
 	store.store(new FileOutputStream(dst), pw);
     }
 
     /** Generate a new private key+CSR pair. Details are taken from
      * properties as follows, based on "Grid Certificate Profile"
      * revision 0.26 ( http://www.ogf.org/documents/GFD.125.pdf ).
      * 
      *   subject
      *     certificate subject to use (DN) 
      * 
      * @param dst Destination directory (subdir of a store)
      * @param p Properties according to which to generate request
      * @return newly created CertificatePair
      * @throws IOException
      * @throws NoSuchAlgorithmException
      * @throws InvalidKeyException
      * @throws NoSuchProviderException
      * @throws SignatureException
      * @throws PasswordCancelledException 
      */
     static public CertificatePair generateRequest(File dst, Properties p)
 	    throws IOException, NoSuchAlgorithmException, InvalidKeyException,
 	    NoSuchProviderException, SignatureException, PasswordCancelledException {
 	// functionally based on
 	// org.globus.tools.GridCertRequest.genCertificateRequest()
 
 	CertificatePair cert = new CertificatePair();
 	SecurityChecks checks = new SecurityChecks(cert);
 	cert.path = dst;
 	checks.checkAccessPath();
 
 	String sigAlgName = "SHA1WithRSA";
 	String keyAlgName = "RSA";
 
 	X509Name name = new X509Name(p.getProperty("subject"));
 
 	// Generate new key pair
 	KeyPairGenerator keygen = KeyPairGenerator.getInstance(keyAlgName);
 	keygen.initialize(1024);
 	KeyPair keyPair = keygen.genKeyPair();
 	PrivateKey privKey = keyPair.getPrivate();
 	PublicKey pubKey = keyPair.getPublic();
 
 	// Generate certificate request
 	DERSet derSet = new DERSet();
 	cert.req = new PKCS10CertificationRequest(
 		sigAlgName, name, pubKey, derSet, privKey);
 
 	// Save certificate request
 	CryptoUtils.writePEM(cert.req, new FileWriter(cert.getCSRFile()));
 
 	// Save private key; permissions are ok by default
 	PasswordCache.getInstance().writePEM(privKey, cert.getKeyFile(), 
 		"new certificate's private key");
 	
 	// check
 	checks.checkAll();
 	
 	return cert;
     }
     
     /** Upload the certificate signing request to its certificate authority 
      * @throws SignatureException 
      * @throws NoSuchProviderException 
      * @throws IllegalStateException 
      * @throws NoSuchAlgorithmException 
      * @throws KeyException 
      * @throws CertificateException 
      * @throws IOException */
     public void uploadRequest() throws CertificateException, KeyException, NoSuchAlgorithmException, IllegalStateException, NoSuchProviderException, SignatureException, IOException {
 	if (cert!=null) {
 	    logger.warning("Ignoring request to upload CSR since certificate is present: "+this);
 	    return;
 	}
 	setProperty("request.serial", getCA().uploadCertificationRequest(req, this));
 	setProperty("request.submitted", "true");
 	notifyChanged();
 	store();
     }
     
     /** See if the certificate can be downloaded from the certificate authority. Also sets
      * the property "request.processed" accordingly.
      *  
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException */
     public boolean isCertificationRequestProcessed() throws KeyManagementException, NoSuchAlgorithmException {
 	boolean isProcessed;
 	try {
 	    isProcessed = getCA().isCertificationRequestProcessed(req, getProperty("request.serial"));
 	    boolean oldProcessed = Boolean.valueOf(getProperty("request.processed"));
 	    setProperty("request.processed", Boolean.toString(isProcessed));
 	    if (oldProcessed != isProcessed) notifyChanged();
 	} catch (IOException e) {
 	    return false;
 	}
 	return isProcessed;
     }
     
     /** Download the certificate from the certificate authority */
     public void downloadCertificate() throws IOException, KeyManagementException, NoSuchAlgorithmException {
 	if (cert!=null) {
 	    logger.warning("Ignoring request to download certificate when already present: "+this);
 	    return;
 	}
 	
 	cert = getCA().downloadCertificate(req, getProperty("request.serial"));
 	if (cert!=null) {
 	    setProperty("request.processed", Boolean.toString(true));
 	    CryptoUtils.writePEM(cert, new FileWriter(getCertFile()));
 	    notifyChanged();
 	}
 	// TODO what when cert is null, throw Exception; can downloadCertificate() return null anyway?
 	// TODO security check
     }
     
     /** Return the correct CA for this CertificatePair. Currently this is
      * fixed, but eventually it should be possible to have certificates with
      * different CA's. 
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException */
     protected CA getCA() throws KeyManagementException, NoSuchAlgorithmException {
 	final CA ca = new NikhefCA();
 	return ca;
     }
 
     /** get the source of this certificate, if any */
     public File getPath() {
 	return path;
     }
 
     /**
      * return the File containing the private key, or null if no certificate is
      * loaded
      */
     public File getKeyFile() {
 	if (path == null)
 	    return null;
 	return new File(getPath(), "userkey.pem");
     }
 
     /**
      * return the File containing the certificate request, or null if no
      * certificate is loaded. The file need not exist.
      */
     public File getCSRFile() {
 	if (path == null)
 	    return null;
 	File f = new File(getPath(), "userrequest.pem");
 	// fallback to Grix's default "usercert_request.pem" if it exists
 	if (!f.exists()) {
 	    File f2 = new File(getPath(), "usercert_request.pem");
 	    if (f2.exists())
 		return f2;
 	}
 	return f;
     }
 
     /**
      * return the File containing the certificate, or null if no certificate is
      * loaded. The file need not exist.
      */
     public File getCertFile() {
 	if (path == null)
 	    return null;
 	return new File(getPath(), "usercert.pem");
     }
     
     /**
      * 
      * return the File containing the additional properties, or null if no certificate
      * is loaded. The file need not exist.
      */
     protected File getPropertiesFile() {
 	if (path == null)
 	    return null;
 	return new File(getPath(), "userinfo.properties");
     }
     
     /** return the private key; decrypt password is requested from the user
      * when required. 
      * @throws IOException 
      * @throws PasswordCancelledException */
     protected PrivateKey getPrivateKey() throws IOException, PasswordCancelledException {
 	FileReader reader = new FileReader(getKeyFile());
 	PasswordCache pwcache = PasswordCache.getInstance();
 	PrivateKey privKey = ((KeyPair)pwcache.readPEM(reader, getKeyFile(), "private key")).getPrivate();
 	return privKey;
     }
     
     /** return the certificate, or null if not present. */
     public X509Certificate getCertificate() {
 	return cert;
     }
     
     /** return the certificate signing request, or null if not present */
     public PKCS10CertificationRequest getCSR() throws IOException {
 	if (req==null) {
 	    if (!getCSRFile().isFile() || !getCSRFile().canRead())
 		return null;
 	    req = (PKCS10CertificationRequest)
 	    	CryptoUtils.readPEM(new FileReader(getCSRFile()), null);
 	}
 	return req;
     }
 
     /**
      * refresh an item from disk and update its status from online sources.
      * 
      * @return whether the refresh was successful or no
      * @throws NoSuchAlgorithmException 
      * @throws KeyManagementException 
      */
     public boolean refresh() throws KeyManagementException, NoSuchAlgorithmException {
 	if (path == null)
 	    return false;
 	// reload from disk
 	try {
 	    load(path);
 	    // see if certificate can be downloaded
 	    if (!Boolean.valueOf(getProperty("request.processed")))
 		isCertificationRequestProcessed();
 	} catch (IOException e) {
 	    // TODO proper error reporting or don't catch
 	    return false;
 	}	
 	return true;
     }
 
     /**
      * return a value of a principal of the certificate issuer/subject. This is
      * taken from the certificate when present. If that fails, the CSR is
      * attempted. If that fails as well, null is returned. The value returned is
      * meant for display purposes.
      * 
      * TODO document behaviour when id==null
      * 
      * @param id one of X509Certificate.* (O, CN, ...)
      * @param where true for subject, false for issuer
      * @return string with value of requested principal, or null if not
      *         available. If multiple entries are present, these are
      *         concatenated using ', '.
      */
     protected String getPrincipalValue(DERObjectIdentifier id, boolean where) {
 	try {
 	    // determine source: certificate, else CSR, else return null.
 	    X509Name subject = null;
 	    if (cert != null) {
 		if (where) subject = PrincipalUtil.getSubjectX509Principal(cert);
 		else       subject = PrincipalUtil.getIssuerX509Principal(cert);
 	    } else {
 		if (req == null) return null;
 		if (!where) return null; // no issuer yet for CSR 
 		subject = req.getCertificationRequestInfo().getSubject();
 	    }
 	    // get info from it
 	    if (id!=null) {
 		// return comma-separated list of values
 		Vector<?> cn = subject.getValues(id);
 		if (cn.size() == 0) return null;
 		String val = "";
 		for (int i=0; i<cn.size(); i++) {
 		    val += ", "+(String)cn.get(i);
 		}
 		return val.substring(2);
 	    } else {
 		// return '/'-separated representation of all
 		Vector<?> oids = subject.getOIDs();
 		Vector<?> values = subject.getValues();
 		String ret = "";
 		for (int i=0; i<oids.size(); i++) {
 		    ret += "/"+X509Name.DefaultSymbols.get(oids.get(i))+"="+(String)values.get(i);
 		}
 		return ret;
 	    }
 	} catch (CertificateEncodingException e) { }
 	return null;
     }
     public String getSubjectPrincipalValue(DERObjectIdentifier id) {
 	return getPrincipalValue(id, true);
     }
     public String getIssuerPrincipalValue(DERObjectIdentifier id) {
 	return getPrincipalValue(id, false);
     }
 
     /** Get a principal value from the certificate issuer/subject by string.
      * The string is matched with X509Name.DefaultLookup, see
      * getSubjectPrincipalValue() for details. Apart from this some special
      * string ids are provided:
      * 
      *   x-email
      *       email address, one of the several fields
      *   x-full
      *       string of the whole subject or issuer, parts separated by '/'
      * 
      * @param id name as present in X509Name.DefaultLookup
      * @param where true for subject, false for issuer
      * @return
      */
     protected String getPrincipalValue(String id, boolean where) {
 	String s;
 	// special cases
 	if (id.equals("x-email")) {
 	    if ((s=getPrincipalValue(X509Name.EmailAddress, where)) != null)
 		return s;
 	    return getPrincipalValue(X509Name.E, where);
 	} else if (id.equals("x-full")) {
 	    return getPrincipalValue((DERObjectIdentifier)null, where);
 	}
 	// fallback to X509Name definition
 	return getPrincipalValue((DERObjectIdentifier)X509Name.DefaultLookUp.get(id), where);	
     }
     public String getSubjectPrincipalValue(String id) {
 	return getPrincipalValue(id, true);
     }
     public String getIssuerPrincipalValue(String id) {
 	return getPrincipalValue(id, false);
     }
 
 
     public String toString() {
 	if (cert == null && req == null)
 	    return "<empty " + this.getClass().getSimpleName() + ">";
 	String name = getSubjectPrincipalValue(X509Principal.CN);
 	String org = getSubjectPrincipalValue(X509Principal.O);
 	if (name == null && org == null)
 	    return "<unnamed " + this.getClass().getSimpleName() + ">";
 	return name + " (" + org + ")";
     }
 
     /**
      * Security checks for the certificate directory. All checks have a void
      * return type and throw an (TODO x) Exception on failure.
      */
     static protected class SecurityChecks {
 	protected CertificatePair cert = null;
 
 	public SecurityChecks(CertificatePair c) {
 	    cert = c;
 	}
 
 	/** Run all checks. */
 	public void checkAll() throws IOException {
 	    checkAccessPath();
 	    checkPrivateKey();
 	    checkCertificate();
 	}
 
 	/** Check access to certificate directory. Must exist. */
 	public void checkAccessPath() throws IOException {
 	    File f = cert.getPath();
 	    if (!f.exists())
 		throw new IOException("Certificate directory not found: " + f);
 	    if (!f.isDirectory())
 		throw new IOException(
 			"Certificate directory is not a directory: " + f);
 	    if (!f.canRead())
 		throw new IOException("Certificate directory cannot be read: "
 			+ f);
 	}
 
 	/**
 	 * Check that the private key is valid. It only checks that a private
 	 * key file is present and has a valid format, not if it can be
 	 * decrypted since we don't want to use the password.
 	 */
 	public void checkPrivateKey() throws IOException {
 	    File f = cert.getKeyFile();
 	    if (!f.exists())
 		throw new IOException("Private key not found: " + f);
 	    if (!f.isFile())
 		throw new IOException("Private key is not a file: " + f);
 	    if (!f.canRead())
 		throw new IOException("Private key cannot be read: " + f);
 	    // TODO check that others cannot read this key!
 	    try {
 		if (CryptoUtils.readPEM(new FileReader(f), null) == null)
 		    throw new IOException(
 			    "Private key file contains no private key: " + f);
 	    } catch (IOException e) {
 		// Since readPEM "throws IOException" the specific information
 		// that it might have been a PasswordException is lost :(
 		// So now I have to parse the message string ...
 		if (!e.getMessage().contains("org.bouncycastle.openssl.PasswordException")&&
 		    !e.getMessage().contains("wrong password"))
 		    throw e;
 	    }
 	}
 
 	/**
 	 * Check the certificate. This is only checked if the certificate really
 	 * exists, because it is optional (e.g. when the certificate signing
 	 * request was made but the certificate not received from the CA).
 	 */
 	public void checkCertificate() throws IOException {
 	    File f = cert.getCertFile();
 	    if (!f.exists())
 		return;
 	    if (!f.isFile())
 		throw new IOException("Certificate is not a file: " + f);
 	    if (!f.canRead())
 		throw new IOException("Certificate cannot be read: " + f);
 	    // check if certificate can be loaded
 	    // TODO cache certificate loading in cert?
 	    if (CryptoUtils.readPEM(new FileReader(f), null) == null)
 		throw new IOException(
 			"Certificate file contains no certificate: " + f);
 	}
     }
 
     /*
      * ItemListener interface
      */
     private ArrayList<ItemListener> itemListeners = new ArrayList<ItemListener>();
     /** @see java.awt.ItemSelectable#addItemListener(java.awt.event.ItemListener) */
     public void addItemListener(ItemListener l) {
 	itemListeners.add(l);
     }
     /** @see java.awt.ItemSelectable#getSelectedObjects() */
     public Object[] getSelectedObjects() {
 	return itemListeners.toArray();
     }
     /** @see java.awt.ItemSelectable#removeItemListener(java.awt.event.ItemListener) */
     public void removeItemListener(ItemListener l) {
 	itemListeners.remove(l);
     }
     /** notify itemlisteners that the item was changed */
     protected void notifyChanged() {
 	ItemEvent e = new ItemEvent(CertificatePair.this, ItemEvent.ITEM_STATE_CHANGED, CertificatePair.this, 0);
 	for (Iterator<ItemListener> it = itemListeners.iterator(); it.hasNext(); ) {
 	    it.next().itemStateChanged(e);
 	}
     }
 }
