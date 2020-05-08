 package nl.nikhef.jgridstart;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.security.SecureRandom;
 
 import org.junit.Test;
 import junit.framework.TestCase;
 
 import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
 import nl.nikhef.jgridstart.util.FileUtils;
 import nl.nikhef.jgridstart.util.PasswordCache;
 
 public class CertificateCheckTest extends TestCase {
     
     /** Helper method: get {@linkplain File} for test number */
     protected File getResourceFile(String name) throws IOException {
 	try {
 	    return new File(getClass().getResource("CertificateCheck-tests/"+name).toURI());
 	} catch (URISyntaxException e) { throw new IOException(e.getLocalizedMessage()); }
     }
     /** Helper method: get {@link CertificatePair}; not load()ed !!! */
     protected CertificatePair getCert(File f) throws IOException {
 	// create CertificatePair
 	// use protected methods to workaround built-in CertificateCheck invocation
 	CertificatePair cert = new CertificatePair();
 	cert.path = f;
 	return cert;
     }
     protected CertificatePair getCert(String name) throws IOException {
 	return getCert(getResourceFile(name));
     }
     /** Helper method: test a dir that is loaded, optional private decryption (via {@link PasswordCache}).*/
     protected void test(File f, String passw) throws IOException, CertificateCheckException {
 	CertificatePair cert = getCert(f);
 	CertificateCheck check = new CertificateCheck(cert);
 	check.check();
 	// private key check if asked for
 	if (passw!=null) {
 	    PasswordCache.getInstance().set(cert.getKeyFile().getCanonicalPath(), passw.toCharArray());
 	    check.checkPrivate();
 	}
 	// private key check with random password should fail
 	SecureRandom random;
 	try {
 	    random = SecureRandom.getInstance("SHA1PRNG", "SUN");
 	} catch (Exception e) { throw new IOException(e.getLocalizedMessage()); }
 	char[] wrongpw = new char[12];
 	for (int i=0; i<wrongpw.length; i++) wrongpw[i] = (char)(random.nextInt(128-32)+32);
 	PasswordCache.getInstance().set(cert.getKeyFile().getCanonicalPath(), wrongpw);
 	try {
 	    check.checkPrivate();
 	    throw new IOException("Private key check succeeds with random password");
 	} catch(CertificateCheckException e) { /* ok */ }
     }
     /** Helper method: test and decrypt private key */
     protected void test(String name, String passw) throws IOException, CertificateCheckException {
 	test(getResourceFile(name), passw);
     }
     /** Helper method: test without decrypting private key */
     protected void test(String name) throws IOException, CertificateCheckException {
 	test(getResourceFile(name), null);
     }
     /** Helper method: test and assume that an CertificateCheckException occurs.*/
     protected void testE(String name, String passw) throws IOException {
 	try {
 	    test(name, passw);
 	} catch(CertificateCheckException e) {
 	    return;
 	}
 	throw new IOException("Expected a CertificateCheckException");
     }
     /** Helper method: test and assume that an CertificateCheckException occurs (no decrypt). */
     protected void testE(String name) throws IOException {
 	testE(name, null);
     }
 
     /** Ordinary, correct cert+key+request */
     @Test public void testO_01() throws Exception { test("testO-01", ""); }
     /** Ordinary, correct cert+key */
     @Test public void testO_02() throws Exception { test("testO-02",""); }
     /** Ordinary, correct key+request */
     @Test public void testO_03() throws Exception { test("testO-03",""); }
     /** Test ordinary cert+key with different password */
     @Test public void testO_05() throws Exception { test("testO-05","jjzlkxOIoi234jioOIj"); }
     /** Test ordinary cert+key with DSA algorithm */
     @Test public void testO_06() throws Exception { test("testO-06","abcd"); }
 
     /** Empty directory */
     @Test public void testE_01() throws Exception { testE("testE-01"); }
     /** Empty private key */
     @Test public void testE_02() throws Exception { testE("testE-02"); }
     /** Unreadable private key */
     @Test public void testE_03() throws Exception {
 	CertificatePair cert = getCert("testE-03");
 	try {
 	    FileUtils.chmod(cert.getKeyFile(), false, false, false, false);
 	    testE("testE-03");
 	} finally {
 	    FileUtils.chmod(cert.getKeyFile(), true, false, false, false);
 	}
     }
     /** Malformed private key; random chars replaced */
     @Test public void testE_04() throws Exception { testE("testE-04", ""); }
     /** Empty certificate */
     @Test public void testE_05() throws Exception { testE("testE-05"); }
     /** Unreadable certificate */
     @Test public void testE_06() throws Exception {
 	CertificatePair cert = getCert("testE-06");
 	try {
 	    FileUtils.chmod(cert.getCertFile(), false, false, false, false);
 	    testE("testE-06");
 	} finally {
 	    FileUtils.chmod(cert.getCertFile(), true, false, false, false);
 	}
     }
     /** Malformed certificate; random chars replaced */
     @Test public void testE_07() throws Exception { testE("testE-07", ""); }
     /** Key/certificate mismatch, both RSA */
     @Test public void testE_08() throws Exception { testE("testE-08", ""); }
     /** Key/certificate mismatch, key DSA, cert RSA */
     @Test public void testE_09() throws Exception { testE("testE-09", "abcd"); }
     /** Key/certificate mismatch, key RSA, cert DSA */
     @Test public void testE_10() throws Exception { testE("testE-10", "jjzlkxOIoi234jioOIj"); }
     /** Key/certificate mismatch, both DSA */
     @Test public void testE_11() throws Exception { testE("testE-11", "qqq123"); }
    /** Only key. */
    @Test public void testE_12() throws Exception { testE("testE-12",""); }
 }
