 package nl.nikhef.jgridstart;
 
 import java.awt.ItemSelectable;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.GeneralSecurityException;
 import java.security.Key;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Security;
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
import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.TooManyListenersException;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
 import nl.nikhef.jgridstart.ca.CA;
 import nl.nikhef.jgridstart.ca.CAException;
 import nl.nikhef.jgridstart.ca.CAFactory;
 import nl.nikhef.jgridstart.util.CryptoUtils;
 import nl.nikhef.jgridstart.util.FileUtils;
 import nl.nikhef.jgridstart.util.PEMReader;
 import nl.nikhef.jgridstart.util.PEMWriter;
 import nl.nikhef.jgridstart.util.PKCS12KeyStoreUnlimited;
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
 import org.bouncycastle.openssl.PasswordFinder;
 
 /** Class containing everything related to a grid certificate.
  * <p>
  * Each instance is directly bound to a {@literal ~/.globus/xxx} directory
  * containing the relevant files (or another base directory, which is the
  * {@link CertificateStore}). This is at least a private key, and usually a
  * certificate. A log can be expected as well (this class logs all actions to
  * it) and a certificate signing request can be present too.
  * <p>
  * This class provides actions like request, revoke, import, export, etc.
  * <p>
  * This class is also a child of Properties. One can set and get any property
  * desired, but some are specifically reserved and queried directly from the
  * certificate and/or certificate signing request. Please see {@link #getProperty}.
  * When the object is loaded, properties found in the file indicated by
  * {@link #getPropertiesFile} are set. On destruction, the properties are written back
  * as to provide transparent presistency. When a property shouldn't be written,
  * one can set the property name with {@code .volatile} appended to {@code true}
  * to make the property not persistent, e.g. with
  * <pre><code>
  *   foo.html=&gt;b&lt;hi there&gt;/b&lt;
  *   foo.html.volatile=true
  * </code></pre>
  * the variable {@code foo.html} is not saved back to the properties file.
  * <p>
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
     protected CertificatePair() {
 	super();
 	Runtime.getRuntime().addShutdownHook(new Thread() {
 	    @Override
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
     public CertificatePair(File f) throws IOException, CertificateCheckException {
 	this();
 	load(f);
     }
     
     /** Return the value of a property.
      * <p>
      * Usually this just returns the
      * value set by {@link #setProperty}, but there are some cases where the
      * value is taken directly from the certificate or certificate
      * signing request.
      * <dl>
      * <dt>valid</dt>
      *     <dd>is {@code true} if the certificate is valid, {@code null} otherwise</dd>
      * <dt>valid.notafter, valid.notbefore</dt>
      *     <dd>localised validity interval</dd>
      * <dt>cert, request</dt>
      *     <dd>is {@code true} when certificate respectively CSR are present</dd>
      * <dt>subject, issuer</dt>
      *     <dd>dinstinguished name in certificate or else CSR, in slash-notation.
      *     Specific fields can be requested as well, e.g. {@code subject.o} for
      *     a comma-separated list of subject organisations; see
      *     {@link #getSubjectPrincipalValue}.</dd>
      * <dt>usage</dt>
      *     <dd>is {@code true} when child keys can be present</dd>
      * <dt>usage.any, usage.serverauth, usage.clientauth, usage.codesigning,
      *   <dd>usage.emailprotection, usage.timestamping, usage.ocspsigning</dt>
      *     are {@code true} when they are defined in the extended key usage</dd>
      * <dt>modulus, modulus.first20</dt>
      *     <dd>the public key's modulus, and its first 20 characters</dd>
      * <dt>org</dt>
      *     <dd>the certificate's (or request's) organisation; this is the last
      *     last organisation RDN, optionally with one or more organisation-unit
      *     RDNs appended (separated by a comma).</dd>
      * <dt>state.icon</dt>
      *     <dd>Icon for certificate state, one of {@code valid}, {@code warning},
      *     {@code renew} or {@code error}.</dd>
      * </dl>
      * 
      * You can postfix each property with {@code .html} to get an html representation.
      * If no html representation is present, it just returns the same as the
      * property without {@code .html}.
      * 
      * @param key property to get the value of
      * @return value of the property, or null if not found.
      */
     @Override
     public String getProperty(String key) {
 	// parse html
 	if (key.endsWith(".html")) {
 	    if (containsKey(key)) return super.getProperty(key);
 	    key = key.substring(0, key.length()-5);
 	    String r = getPropertyHtml(key);
 	    if (r!=null) return r;
 	}
 	try {
 	    // return generated property
 	    if (key.equals("path"))
 		if (getPath()==null) return null;
 		else return getPath().toString();
 	    if (key.equals("cert"))
 		if (getCertificate()==null) return null;
 		else return "true";
 	    if (key.equals("cert.serial"))
 		if (getCertificate()==null) return null;
 		else return getCertificate().getSerialNumber().toString();
 	    if (key.equals("request"))
 		if (!getCSRFile().exists()) return null;
 		else return "true";
 	    if (key.equals("subject"))
 		if (getCertificate()==null && getCSR()==null) return null;
 		else return getSubjectPrincipalValue("x-dn-slash");
 	    if (key.startsWith("subject."))
 		return getSubjectPrincipalValue(key.substring(8));
 	    if (key.equals("issuer"))
 		if (getCertificate()==null) return null;
 		else return getIssuerPrincipalValue("x-dn-slash");
 	    if (key.startsWith("issuer."))
 		return getIssuerPrincipalValue(key.substring(7));
 	    if (key.equals("org")) {
 		if (containsKey(key)) return super.getProperty(key);
 		if (getProperty("subject")==null) return null;
 		// parse organisation from subject
 		String org = Organisation.getFromCertificate(this).getProperty("x-full-rdn");
 		if (org!=null) super.setProperty("org", org);
 		return org;
 	    }
 	    if (key.equals("modulus"))
 		if (getCertificate()!=null) return ((RSAPublicKey)getCertificate().getPublicKey()).getModulus().toString(16);
 		else if (getCSR()!=null) return ((RSAPublicKey)getCSR().getPublicKey()).getModulus().toString(16);
 	    if (key.equals("modulus.first20")) {
 		if (getCertificate()!=null) return ((RSAPublicKey)getCertificate().getPublicKey()).getModulus().toString(16).substring(0,20);
 		else if (getCSR()!=null) return ((RSAPublicKey)getCSR().getPublicKey()).getModulus().toString(16).substring(0,20);
 		else return null;
 	    }
 	    if (key.equals("valid")) {
 		if (getCertificate()==null) return null;
 		try { getCertificate().checkValidity(); }
 		catch(CertificateExpiredException e) { return null; }
 		catch(CertificateNotYetValidException e) { return null; }
 		return "true";
 	    }
 	    if (key.equals("valid.notbefore")) {
 		if (getCertificate()==null) return null;
 		return DateFormat.getDateInstance().format(getCertificate().getNotBefore());
 	    }
 	    if (key.equals("valid.notafter")) {
 		if (getCertificate()==null) return null;
 		return DateFormat.getDateInstance().format(getCertificate().getNotAfter());
 	    }
 	    if (key.equals("state.icon")) {
 		if (Boolean.valueOf(getProperty("cert")) && !Boolean.valueOf(getProperty("valid")))
 		    return "expired";
 		if (!Boolean.valueOf(getProperty("cert")) && Boolean.valueOf(getProperty("request")))
 		    return "warning";
 		if (Boolean.valueOf(getProperty("valid")))
 		    return "valid";
 		// TODO if (Boolean.valueOf(getProperty(""))) return "renew";
 		return "error";
 	    }
 	    if (key.startsWith("usage")) {
 		if (getCertificate()==null) return null;
 		List<String> usage = getCertificate().getExtendedKeyUsage();
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
 	if (key.endsWith(".o")) {
 	    // hyperlink organisations
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
 	    return sorgs.length()>2 ? sorgs.substring(0, sorgs.length()-2).trim() : "";
 	    
 	} else if (key.endsWith(".ou")) {
 	    // hyperlink sub-units based on organisation
 	    // TODO allow multiple sub-units
 	    Organisation org = Organisation.getFromCertificate(this);
 	    if (org==null) return getProperty(key);
 	    try {
 		return org.getNameHTML();
 	    } catch (Exception e) { }
 	    	    
 	} else if (key.equals("state.icon")) {
 	    // convert icon name to html portion
 	    String state = getProperty(key);
 	    if (state == "valid")
 		return "<b color='green'>&#x2713;</b>";
 	    if (state == "expired")
 		return "<b color='gray'>&#x25cf;</b>";
 	    else if (state == "warning")
 		return "<font size='+1'><b color='#cc9900'>!</b></font>";
 	    else
 		return "<b color='red'>&#x2715;</b>";
 	}
 	// nothing by default
 	return null;
     }
     /** {@inheritDoc}
      * <p>
      * On setting a property, all {@link ItemListener}s are notified using
      * {@linkplain #notifyChanged} if the value is different from the old one.
      */
     @Override
     public Object setProperty(String name, String value) {
 	String old = getProperty(name);
 	Object o = super.setProperty(name, value);
 	if (!value.equals(old))
 	    notifyChanged();
 	return o;
     }
 
     /** Reset the contents to this object to the empty state */
     @Override
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
 	try {
 	    check(false);
 	} catch(CertificateCheckException e) {
 	    setProperty("state.message", e.getMessage());
 	    setProperty("state.message.volatile", "true");
 	}
 
 	// read additional properties, not fatal if not present
 	if (getPropertiesFile().exists()) {
 	    InputStream in = new FileInputStream(getPropertiesFile());
 	    try {
 		super.load(in);
 	    } finally {
 		in.close();
 	    }
 	}
 
 	notifyChanged();
     }
     
     /** Store the properties in the file indicated by {@link #getPropertiesFile}.
      * <p>
      * This file is written with permissions so that only the user can read
      * it, because it may contain personal information. 
      * 
      * @throws IOException 
      * @throws FileNotFoundException */
     public void store() throws FileNotFoundException, IOException {
 	logger.finest("Storing certificate properties: "+getPropertiesFile());
 	Properties p = (Properties)CertificatePair.super.clone();
 	// remove volatile properties
 	ArrayList<String> propsToRemove = new ArrayList<String>();
 	for (Enumeration<?> en=p.propertyNames(); en.hasMoreElements(); ) {
 	    String key = (String)en.nextElement();
 	    if (Boolean.valueOf(p.getProperty(key+".volatile")))
 		propsToRemove.add(key);
 	    // remove "*.volatile" entries, also stray ones without a property
 	    if (key.endsWith(".volatile"))
 		propsToRemove.add(key);
 	}
 	for (Iterator<String> it = propsToRemove.iterator(); it.hasNext(); ) {
 	    String key = it.next();
 	    p.remove(key);
 	}
 	// and store with OutputStream for Java 1.5 and below
 	if (p.size() > 0) {
 	    PrivateFileWriter writer = new PrivateFileWriter(getPropertiesFile());
 	    try {
 		p.store(writer.getOutputStream(),
 		    	"jGridstart certificate properties");
 	    } finally {
 		writer.close();
 	    }
 	} else {
 	    getPropertiesFile().delete();
 	}
     }
 
     /** Import a {@linkplain CertificatePair} from a keystore into a (new) directory.
      * 
      * @param src File to import from
      * @param dst Directory to import into. On success, this directory could be
      *            passed later to create a new CertificatePair which is equal to
      *            the one returned by this method.
      * @param dstpw password to use for private key storage, or {@code null} to use
      *            same password as import password
      * @return a new CertificatePair representing the newly imported pair.
      */
     static public CertificatePair importFrom(File src, File dst, char[] dstpw)
 	    throws IOException, PasswordCancelledException, CertificateCheckException, GeneralSecurityException {
 
 	if (src==null || src.getName()==null)
 	    throw new NullPointerException("Please supply a filename to import from");
 	if (!src.isFile() && !src.isDirectory())
 	    throw new IOException("Need file to import from: " + src);
 	if (!src.canRead())
 	    throw new IOException("Cannot read file to import from: " + src);
 
 	CertificatePair cert = new CertificatePair();
 	cert.path = dst;
 
 	String ext = src.getName().toLowerCase();
 	ext = ext.substring(ext.lastIndexOf('.')+1);
 
 	if (src.isDirectory()) {
 	    // TODO handle srcpw!=dstpw
 	    cert.importFromDirectory(src);
 	    cert.check(false); // private key is not decrypted here
 	} else 	if (ext.equals("p12") || ext.equals("pfx")) {
 	    cert.importFromPKCS(src, dstpw);
 	    cert.check(true); // needed password at import, so check privkey as well
 	} else if (ext.equals("pem")) {
 	    cert.importFromPEM(src, dstpw);
 	    cert.check(true); // as above
 	} else {
 	    throw new IOException("Cannot determine format to import from, unknown file extension: "+ext);
 	}
 	cert.notifyChanged();
 	
 	return cert;
     }
 
     /** Import a {@linkplain CertificatePair} from a keystore into a (new) directory.
      * 
      * @param src File to import from
      * @param dst Directory to import into. On success, this directory could be
      *            passed later to create a new CertificatePair which is equal to
      *            the one returned by this method.
      * @return a new CertificatePair representing the newly imported pair.
      */
     static public CertificatePair importFrom(File src, File dst)
     	     throws IOException, PasswordCancelledException, CertificateCheckException, GeneralSecurityException {
 	return importFrom(src,dst, null);
     }
     
     /** Import key and certificate from a PEM file.
      * <p>
      * This may possibly overwrite the current data.
      * 
      * @param src PEM file to import from
      */
     protected void importFromPEM(File src, char[] dstpw)
     		throws IOException, CertificateCheckException, NoSuchAlgorithmException, PasswordCancelledException {
 	int count = 0;
 	logger.finer("Trying to import certificate from PEM file: "+src);
 	// set destination password if given
 	if (dstpw!=null)
 	    PasswordCache.getInstance().set(getKeyFile().getCanonicalPath(), dstpw);
 	// process all items in the file
 	PEMReader r = new PEMReader(src, "private key in PEM file "+src.getName());
 	while (r.ready()) {
 	    Object o = r.readObject();
 	    if (o==null) break;
 	    count++;
 	    if (o instanceof KeyPair) {
 		// Extract and write private key
 		if (((KeyPair) o).getPrivate() != null) {
 		    PrivateKey privKey = ((KeyPair) o).getPrivate();
 		    // now import file private key password was got, so we can use that for export
 		    // when required
 		    String msg = "private key for "+getPath().getName();
 		    if (dstpw==null)
 			PasswordCache.getInstance().set(getKeyFile().getCanonicalPath(),
 				PasswordCache.getInstance().getForDecrypt(msg, src.getCanonicalPath()));
 		    // and write private key
 		    PEMWriter.writeObject(getKeyFile(), privKey, msg);
 		}
 	    } else if (o instanceof X509Certificate) {
 		// Extract and write certificate
 		PEMWriter.writeObject(getCertFile(), o);
 	    } else if (o instanceof PKCS10CertificationRequest) {
 		// Extract and write certificate signing request (CSR)
 		PEMWriter.writeObject(getCSRFile(), o);
 	    } else {
 		logger.fine("Discarding unrecognised object in PEM file "+src.getName()+": "+o);
 	    }
 	    // we only copied files, no load it
 	    load(path);
 	}
 
 	if (count == 0)
 	    throw new IOException("not a PEM file: " + src);
     }
 
     /** Import from PKCS.
      * <p>
      * If multiple key/certificate entries are found, only the first one is
      * imported.
      * 
      * @param src file to import from
      * @param dstpw password for new private keym or {@code null} to use same as import password
      */
     protected void importFromPKCS(File src, char[] dstpw)
     		throws IOException, PasswordCancelledException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
 	PasswordCache pwcache = PasswordCache.getInstance();
 	String storename = "PKCS#12 store " + src.getName();
 	KeyStore store = PKCS12KeyStoreUnlimited.getInstance();
 	// now try until success or cancel
 	boolean loaded = false;
 	for (int i=0; i<3; i++) {
 	    FileInputStream in = new FileInputStream(src);
 	    try {
 		char[] pw = pwcache.getForDecrypt(storename, src.getCanonicalPath());
 		store.load(in, pw);
 		loaded = true;
 		if (dstpw==null) dstpw = pw;
 	    } catch(IOException e) {
 		if (e.getLocalizedMessage().contains("wrong password")) {
 		    // if bad password, invalidate and ask again
 		    pwcache.invalidate(src.getCanonicalPath());
 		} else throw e;
 	    } finally {
 		in.close();
 	    }
 	}
 	if (!loaded)
 	    throw new IOException("Number of password attempts exceeded for PKCS#12 file: " + src);
 
 	if (!loaded || store.size() == 0)
 	    throw new IOException("Not a PKCS#12 file: " + src);
 	
 	logger.finer("Importing certificate from PKCS#12 file: "+src);
 
 	for (Enumeration<String> it = store.aliases(); it.hasMoreElements(); ) {
 	    String alias = it.nextElement();
 	    if (store.isKeyEntry(alias)) {
 		// TODO check it is a private key
 		/*
 		 * This could be done but passwords on entries aren't
 		 * very common and it would be a hassle to ask the user
 		 * again.
 		 */
 		Key key = store.getKey(alias, null);
 		if (dstpw!=null)
 		    PEMWriter.writeObject(getKeyFile(), key, dstpw);
 		else
 		    PEMWriter.writeObject(getKeyFile(), key, "imported private key");
 		
 		Certificate[] chain = store.getCertificateChain(alias);
 		if (chain != null) {
 		    // we want only the user certificate, which is the first one
 		    // TODO check it really is an X509Certificate
 		    cert = (X509Certificate) chain[0];
 		    PEMWriter.writeObject(getCertFile(), cert);
 		    // we're done!
 		    return;
 		}
 	    }
 	}
     }
     
     protected void importFromDirectory(File src) throws IOException, CertificateCheckException {
 	// import from directory: make sure private key exist and copy
 	File key = new File(src, "userkey.pem");
 	if (!key.canRead() || !key.isFile())
 	    throw new IOException("Need directory with readable userkey.pem to import from: " + src);
 	
 	logger.finer("Importing certificate from directory: "+src);
 
 	// copy all files
 	List<File> files = Arrays.asList(src.listFiles());
 	for (Iterator<File> i = files.iterator(); i.hasNext();) {
 	    File f = i.next();
 	    if (!f.isFile()) continue;
 	    FileUtils.CopyFile(f, new File(path, f.getName()));
 	}
 	// load from new location
 	load(path);
     }
 
     /** Export the certificate and private key to a file.
      * <p>
      * Type is detected from the filename.
      * 
      * @param dst destination to export {@link TooManyListenersException}
      * @param pw password to encrypt exported key with, or {@code null} to use private key password
      */
     public void exportTo(File dst, char[] pw) throws IOException, GeneralSecurityException {
 	if (dst==null || dst.getName()==null)
 	    throw new NullPointerException("Please supply a filename to export to");
 	String ext = dst.getName().toLowerCase();
 	ext = ext.substring(ext.lastIndexOf('.')+1);
 	
 	if (ext.equals("p12") || ext.equals("pfx")) {
 	    // need password
 	    if (pw==null) {
 		// ask for it using standard way
 		getPrivateKey();
 		// and retrieve
 		boolean oldAsk = PasswordCache.getInstance().setAlwaysAskForEncrypt(false);
 		try {
 		    pw = PasswordCache.getInstance().getForEncrypt("private key", getKeyFile().getCanonicalPath());
 		} finally {
 		    PasswordCache.getInstance().setAlwaysAskForEncrypt(oldAsk);
 		}
 	    }
 	    exportToPKCS(dst, pw);
 	} else if (ext.equals("pem")) {
 	    // password can be null
 	    exportToPEM(dst, pw);
 	} else {
 	    throw new IOException("Cannot determine format to export to, unknown file extension: "+ext);
 	}
     }
     
     /** Export the certificate and private key to a file using private key password.
      * <p>
      * Type is detected from the filename, password is taken from private key when
      * required.
      * 
      * @param dst destination to export {@link TooManyListenersException}
      */
     public void exportTo(File dst) throws PasswordCancelledException, IOException, GeneralSecurityException {
 	exportTo(dst, null);
     }
     
     /** Export the certificate and private key to a PKCS#12 file. */
     protected void exportToPKCS(File dst, char[] pw) throws IOException, GeneralSecurityException, PasswordCancelledException {
 	logger.finer("Exporting certificate '"+this+"' to PKCS#12: "+dst);
 
 	// Create certificate chain TODO do proper chain
 	X509Certificate[] certchain = {getCertificate()}; // TODO include chain?
 	
 	// Create PKCS12 keystore with password
 	KeyStore store = PKCS12KeyStoreUnlimited.getInstance();
 	store.load(null, null);
 	store.setKeyEntry("Grid certificate", getPrivateKey(), null, certchain); // TODO proper alias
 	
 	// write file with password
 	FileOutputStream out = new FileOutputStream(dst);
 	store.store(out, pw);
 	out.close();
     }
     
     /** Export the certificate and private key to a PEM file.
      * <p>
      * This is quite simple, since it just concatenates the existing
      * files from its {@code .globus} directory; no password is needed.
      */
     protected void exportToPEM(File dst, char[] pw) throws IOException {
 	logger.finer("Exporting certificate '"+this+"' to PEM: "+dst);
 	
 	PEMWriter out = new PEMWriter(dst);
 	try {
 	    String s = System.getProperty("line.separator");
 	    File[] files = new File[] {
 		    getKeyFile(),
 		    getCSRFile(),
 		    getCertFile()
 	    };
 	    // private must be decrypted and encrypted if password given
 	    if (pw!=null) {
 		files[0] = null;
 		out.writeObject(getPrivateKey());
 	    }
 	    // rest of the files can just be copied literally
 	    for (int i=0; i<files.length; i++) {
 		if (files[i]==null) continue;
 		if (!files[i].exists()) continue;
 		BufferedReader in = new BufferedReader(new FileReader(files[i]));
 		while (in.ready()) {
 		    out.write(in.readLine() + s);
 		}
 		in.close();
 	    }
 	} catch(IOException e) {
 	    throw e;
 	} finally {
 	    if (out!=null) out.close();
 	}
     }
 
     /** Generate a new private key+CSR pair with specified password.
      * <p>
      * Details are taken from properties as follows, based on
      * "<em>Grid Certificate Profile</em>"
      * revision 0.26,
      * <a href="http://www.ogf.org/documents/GFD.125.pdf">http://www.ogf.org/documents/GFD.125.pdf</a>.
      * <p>
      * Details of the certificate are specified by the supplied
      * {@linkplain Properties}. Currently only the property {@code subject}
      * is used, which dictates the subject (DN) to use.
      * 
      * @param dst Destination directory (subdir of a store)
      * @param p Properties according to which to generate request
      * @param pw Password to use, or {@code null} to ask from user via {@link PasswordCache}
      * @return newly created CertificatePair
      */
     static public CertificatePair generateRequest(File dst, Properties p, final char[] pw)
 	    throws IOException, GeneralSecurityException, PasswordCancelledException, CAException {
 	// functionally based on
 	// org.globus.tools.GridCertRequest.genCertificateRequest()
 	
 	String subject = p.getProperty("subject");
 	logger.finer("Generating certificate request for \""+subject+"\" to "+dst);
 
 	CertificatePair cert = new CertificatePair();
 	cert.path = dst;
 
 	String sigAlgName = System.getProperty("jgridstart.sigalgname");
 	if (sigAlgName==null) sigAlgName = "SHA1WithRSA";
 	String keyAlgName = System.getProperty("jgridstart.keyalgname");
 	if (keyAlgName==null) keyAlgName = "RSA";
 	
 	int keysize = 1024;
 	if (System.getProperty("jgridstart.keysize")!=null)
 	    keysize = Integer.valueOf(System.getProperty("jgridstart.keysize"));
 
 	// needs comma-notation, so convert if slash-notation
 	if (subject.trim().startsWith("/"))
 	    subject = subject.substring(1).replace('/', ',');
 	X509Name name = new X509Name(subject);
 
 	// Generate new key pair
 	KeyPairGenerator keygen = KeyPairGenerator.getInstance(keyAlgName);
 	keygen.initialize(keysize);
 	KeyPair keyPair = keygen.genKeyPair();
 	PrivateKey privKey = keyPair.getPrivate();
 	PublicKey pubKey = keyPair.getPublic();
 
 	// Generate certificate request
 	DERSet derSet = new DERSet();
 	cert.req = new PKCS10CertificationRequest(
 		sigAlgName, name, pubKey, derSet, privKey);
 
 	// Save certificate request
 	FileUtils.writeFile(cert.getCSRFile(),
 			cert.getCA().encodeCertificationRequest(cert.req, cert));
 
 	// Save private key; permissions are ok by default
 	if (pw==null)
 	    PEMWriter.writeObject(cert.getKeyFile(), privKey, "new certificate's private key");
 	else
 	    PEMWriter.writeObject(cert.getKeyFile(), privKey, pw);
 	
 	// TODO check
 	//cert.check(true);
 	
 	return cert;
     }
     /** Generate a new private key+CSR pair, request password.
      * @see #generateRequest(File, Properties) */
     static public CertificatePair generateRequest(File dst, Properties p) throws GeneralSecurityException, IOException, PasswordCancelledException, CAException {
 	return generateRequest(dst, p, null);
     }
     
     /** Upload the certificate signing request to its certificate authority */
     public void uploadRequest() throws GeneralSecurityException, IOException, CAException {
 	logger.finer("Uploading certificate request for: "+this);
 	try {
 	    if (getCertificate()!=null) throw new IOException();
 	} catch (IOException e) {
 	    logger.warning("Ignoring request to upload CSR since certificate is present: "+this);
 	    return;
 	}
 	String csrData = FileUtils.readFile(getCSRFile());
 	getCA().uploadCertificationRequest(csrData, this);
 	setProperty("request.submitted", "true");
 	if (getProperty("request.serial")!=null)
 	    logger.finer("Got certificate request serial "+getProperty("request.serial")+" for: "+this);
 	notifyChanged();
 	store();
     }
     
     /** See if the certificate can be downloaded from the certificate authority.
      * <p>
      * Also sets the property {@code request.processed} accordingly.
      */
     public boolean isCertificationRequestProcessed() throws GeneralSecurityException, CAException {
 	boolean isProcessed;
 	try {
 	    isProcessed = getCA().isCertificationRequestProcessed(getCSR(), this);
 	    setProperty("request.processed", Boolean.toString(isProcessed));
 	} catch (IOException e) {
 	    return false;
 	}
 	return isProcessed;
     }
     
     /** Download the certificate from the certificate authority  */
     public void downloadCertificate() throws IOException, CertificateCheckException, CAException, GeneralSecurityException {
 	if (getCertificate()!=null) {
 	    logger.warning("Ignoring request to download certificate when already present: "+this);
 	    return;
 	}
 	
 	logger.finer("Downloading certificate: "+this);
 	
 	cert = getCA().downloadCertificate(getCSR(), this);
 	if (cert!=null) {
 	    setProperty("request.processed", Boolean.toString(true));
 	    PEMWriter.writeObject(getCertFile(), cert);
 	    notifyChanged();
 	}
 	// TODO what when cert is null, throw Exception; can downloadCertificate() return null anyway?
 	check(false);
     }
     
     /** Return the correct CA for this CertificatePair. Currently this is
      * fixed, but eventually it should be possible to have certificates with
      * different CA's.
      */
     protected CA getCA() throws GeneralSecurityException, CAException {
 	return CAFactory.getDefault();
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
     
     /** Return the {@linkplain File} containing the additional properties.
      * <p>
      * If no certificate is loaded, {@code null} is returned. The file need not exist yet.
      */
     protected File getPropertiesFile() {
 	if (path == null)
 	    return null;
 	return new File(getPath(), "userinfo.properties");
     }
     
     /** Return the decrypted private key.
      * <p>
      * The decryption password is requested from the user when required
      * using {@link PasswordCache}. When the password is incorrect, the user is
      * asked the password up to three times, after which a exception is thrown. 
      * 
      * @throws IOException 
      * @throws PasswordCancelledException */
     protected PrivateKey getPrivateKey() throws IOException, PasswordCancelledException {
 	PrivateKey key = null;
 	final String srcmsg = "private key";
 	String msg = srcmsg;
 	
 	for (int i=0; i<3 && key==null; i++) {
 	    try {
 		key = ((KeyPair)PEMReader.readObject(getKeyFile(), KeyPair.class, msg)).getPrivate();
 	    } catch (IOException e) {
 		if (!PasswordCache.isPasswordWrongException(e))
 		    throw e;
 		// invalidate old entry if error is wrong password
 		PasswordCache.getInstance().invalidate(getKeyFile().getCanonicalPath());
 		msg = srcmsg + ", please try again.\n" +
 		      "(there is a small chance that the data is corrupted)";
 	    }
 	}
 
 	return key;
     }
     
     /** Return the certificate.
      * <p>
      * Returns or {@code null} if not present.
      */
     public X509Certificate getCertificate() throws IOException {
 	if (cert==null) {
 	    try {
 		cert = (X509Certificate)PEMReader.readObject(getCertFile(), X509Certificate.class);
 	    } catch (FileNotFoundException e) {
 		cert = null;
 	    }
 	}
 	return cert;
     }
     
     /** Return the certificate signing request (CSR).
      * <p>
      * Returns {@code null} if not present
      */
     public PKCS10CertificationRequest getCSR() throws IOException {
 	if (req==null) {
 	    try {
 		req = (PKCS10CertificationRequest)PEMReader.readObject(getCSRFile(), PKCS10CertificationRequest.class);
 	    } catch (FileNotFoundException e) {
 		req = null;
 	    }
 	}
 	return req;
     }
 
     /** Refresh an item from disk and update its status from online sources.
      * 
      * @return whether the refresh was successful or no
      */
     public boolean refresh() throws GeneralSecurityException, CAException {
 	// TODO should check if disk contents changed, update if so.
 	//      load(path) should not be done here, since that clears
 	//      the current state. E.g. at some point in the request-new
 	//      wizard, the selection is set to the newly created
 	//      CertificatePair. This calls refresh() and shouldn't throw
 	//      away volatile properties, for example.
 
 	// see if certificate can be downloaded
 	if (!Boolean.valueOf(getProperty("request.processed")))
 	    isCertificationRequestProcessed();
 	
 	return true;
     }
     
     /** Run certificate checks.
      * 
      * @param checkPriv True to check private key as well, requires private key password.
      *                  You can safely test this if the private key password is still
      *                  known to be in the {@link PasswordCache}
      * @throws CertificateCheckException 
      */
     protected void check(boolean checkPriv) throws CertificateCheckException {
 	CertificateCheck c = new CertificateCheck(this);
 	c.check();
 	if (checkPriv) c.checkPrivate();
     }
     
     /** Return a value of a principal of the certificate issuer/subject.
      * <p>
      * This is taken from the certificate when present. If that fails, the CSR is
      * attempted. If that fails as well, {@code null} is returned. The value returned is
      * meant for display purposes.
      * <p>
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
 	    if (getCertificate() != null) {
 		if (where) subject = PrincipalUtil.getSubjectX509Principal(getCertificate());
 		else       subject = PrincipalUtil.getIssuerX509Principal(getCertificate());
 	    } else {
 		if (getCSR() == null) return null;
 		if (!where) return null; // no issuer yet for CSR 
 		subject = getCSR().getCertificationRequestInfo().getSubject();
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
 	} catch (CertificateEncodingException e) { 
 	} catch (IOException e) {
 	}
 	return null;
     }
     public String getSubjectPrincipalValue(DERObjectIdentifier id) {
 	return getPrincipalValue(id, true);
     }
     public String getIssuerPrincipalValue(DERObjectIdentifier id) {
 	return getPrincipalValue(id, false);
     }
 
     /** Get a principal value from the certificate issuer/subject by string.
      * <p>
      * The string is matched with {@link X509Name#DefaultLookUp}, see
      * {@link #getSubjectPrincipalValue} for details.
      * <p>
      * Apart from this some special string ids are provided:
      * <dl>
      *   <dt>{@code x-email}</dt>
      *       <dd>email address, one of the several fields</dd>
      *   <dt>{@code x-dn-slash} or {@code x-dn}</dt>
      *       <dd>string of the whole subject or issuer distinguished name,
      *           parts separated by {@code '/'}</dd>
      *   <dt>{@code x-dn-comma}</dt>
      *       <dd>string of the whole subject or issuer distinguished name,
      *           parts separated by {@code ','}</dd>
      *   <dt>{@code x-hash}</dt>
      *       <dd>openssl hash of subject dn, see {@link CryptoUtils#getSubjectHash}</dd>
      * </dl>
      * 
      * @param id name as present in X509Name.DefaultLookup
      * @param where true for subject, false for issuer
      */
     protected String getPrincipalValue(String id, boolean where) {
 	String s;
 	// special cases
 	if (id.equals("x-email")) {
 	    if ((s=getPrincipalValue(X509Name.EmailAddress, where)) != null)
 		return s;
 	    return getPrincipalValue(X509Name.E, where);
 	} else if (id.equals("x-dn-slash")) {
 	    return getPrincipalValue((DERObjectIdentifier)null, where);
 	} else if (id.equals("x-dn-comma")) {
 	    return getPrincipalValue((DERObjectIdentifier)null, where).replace('/', ',').substring(1);
 	} else if (id.equals("x-hash")) {
 	    if (where) {
 		try {
 		    return CryptoUtils.getSubjectHash(getCertificate());
 		} catch (Exception e1) { try {
 		    return CryptoUtils.getSubjectHash(getCSR());
 		} catch (Exception e2) {
 		    return null;
 		} }
 	    } else {
 		try {
 		    return CryptoUtils.getIssuerHash(getCertificate());
 		} catch (Exception e) {
 		    return null;
 		}
 	    }
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
 
     @Override
     public String toString() {
 	try {
 	    if (getCertificate() == null && getCSR() == null)
 	        throw new IOException();
 	} catch (IOException e) {
 	    return "<empty " + this.getClass().getSimpleName() + ">";
 	}
 	String name = getSubjectPrincipalValue(X509Principal.CN);
 	String org = getSubjectPrincipalValue(X509Principal.O);
 	if (name == null && org == null)
 	    return "<unnamed " + this.getClass().getSimpleName() + ">";
 	return name + " (" + org + ")";
     }
 
     /** Test equality.
      * <p>
      * A {@linkplain CertificatePair} object only equals another object if
      * it is a {@linkplain CertificatePair} as well, and if the certificates
      * are equal. If no certificate is present, the CSR is compared instead.
      * If no CSR is present either, the path is compared.
      * <p>
      * Ideally the private key would be checked as well, but that requires a password.
      */
     @Override
     public boolean equals(Object other) {
 	if (!(other instanceof CertificatePair))
 	    return false;
 	try {
 	    if (getCertificate()!=null)
 	        return getCertificate().equals(((CertificatePair)other).getCertificate());
 	} catch (IOException e1) {
 	    return false;
 	}
 	try {
 	    if (getCSR()!=null)
 	        return getCSR().equals(((CertificatePair)other).getCSR());
 	} catch (IOException e) { }
 	return getPath().equals(((CertificatePair)other).getPath());
     }
 
     /*
      * ItemListener interface
      */
    private List<ItemListener> itemListeners = Collections.synchronizedList(new ArrayList<ItemListener>());
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
