 package nl.nikhef.jgridstart;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
 import nl.nikhef.jgridstart.util.FileUtils;
 
 /** A {@link CertificateStore} which has the notion of a default certificate.
  * <p>
  * The default {@code ~/.globus} directory generally has key and certificate
  * files that are used Globus and related tools. They do not support multiple
  * certificates. To give a user a possibility to use multiple certificates
  * anyway, the {@link CertificateStore} stores all certificates as
  * subdirectories. This class adds the notion that one of these can be
  * the default certificate, which means that they are copied/linked in
  * {@code ~/.globus}.
  * <p>
  * Only if the user only has one certificate, we rather don't bother creating
  * subdirectories, as it would only confuse the user when seeing non-standard
  * stuff in {@code ~/.globus}.
  * <p>
  * This class handles this transparently for the developer, so that there is
  * just a {@link CertificateStore} of which one can be the default.
  * 
  * <a name="default_certificate"></a>
  * <h3>The default certificate</h3>
  * While this store looks at subdirectories for its children, the standard
  * globus location for key and certificate is {@code ~/.globus/userkey.pem}
  * and {@code ~/.globus/usercert.pem}. This is an entry in this store, and
  * is called the <em>default certificate</em>. This class was designed to
  * allow multiple certificates to be present, and to be able to select one
  * of them as the default, and switch between them easily.
  * <p>
  * A new item is added according to the following rules:
  * <ol>
  *   <li>If the store is empty, the default location is used (no subdir)</li>
  *   <li>If the store has a default certificate and a renewal of that is added,
  *     the old certificate is moved to a subdir and the new one is imported
  *     on the default location (no subdir).</li>
  *   <li>If the store has a certificate and a new one is added, it is added
  *     as a subdir.</li>
  * </ol>
  * Please see {@link CertificateStoreWithDefault#setDefault} for the behaviour
  * when the default certificate is changed, this is directly related to this.
  * <p>
  * This behaviour satisfies the following situations:
  * <ul>
  *  <li><em>Empty</em>: the user has no {@code ~/.globus}, or it is empty.</li>
  *  <li><em>Single</em>: the user has imported a certificate, or created
  *    a new request. This is all done in the default certificate location, no
  *    subdirectory is needed.</li>
  *  <li><em>Single renewal</em>: one certificate is present, and a renewal
  *    is created. This renewal request is made in a subdirectory of the store, while
  *    the old certificate remains in place. When the renewal certificate is obtained,
  *    the old certificate is archived into a subdirectory, and the renewed certificate
  *    is set as the default certificate.</li>
  *  <li><em>Dual</em>: one certificate is present, and another one is
  *    imported. The default certificate remains such, and the new one is imported into
  *    a new subdirectory.</li>
  *  <li>With multiple certificates present, the user may want to change the default
  *    certificate (e.g. when working for a different organisation). So the current default
  *    certificate needs to be stored in a subdirectory, and the new default certificate
  *    needs to be linked/copied to the default certificate location. To be able to use
  *    select a specific certificate in scripts (if the user would want such), any
  *    certificate that has been put in a subdirectory before, remains there; if it is
  *    to become a default certificate again, a link or copy is made, and it is not
  *    counted as a separate entry in the store.</li>
  * </ul>
  * 
  * @author wvengen
  *
  */
 public class CertificateStoreWithDefault extends CertificateStore {
 
     /** new empty certificate store */
     public CertificateStoreWithDefault() {
 	super();
     }
 
     /** new certificate store and load from path */
     public CertificateStoreWithDefault(String path) {
 	super(path);
     }
 
     /** new certificate store and load from path as File */
     public CertificateStoreWithDefault(File path) {
 	super(path);
     }
 
     /** Return the default certificate.
      * <p>
      * The default certificate is the one in {@code ~/.globus}
      * (or the platform's equivalent).
      * <p>
      * This can be called pretty often, so the result is cached. Updates outside
      * of this program will not be picked up as a result while running.
      * 
      * @return Default {@linkplain CertificatePair}, or {@code null} if not present
      */
     public CertificatePair getDefault() throws IOException {
 	if (defaultCert==null) {
 	    // find certificate with same path as store
 	    for (Iterator<CertificatePair> it = iterator(); it.hasNext(); ) {
 		CertificatePair c = it.next();
 		if (c.path == path) {
 		    defaultCert = c;
 		    break;
 		}
 	    }
 	    // or else try to find certificate with same contents
 	    defaultCert = findDefaultCertificate();
 	}
 	return defaultCert;
     }
     
     /** Make a {@link CertificatePair} the default certificate.
      * <p>
      * This copies the files from a {@linkplain CertificatePair}'s subdirectory
      * to the store's path ({@code ~/.globus} by default, so that Globus tools
      * use that one).
      * <p>
      * If the default certificate (the one before it is updated) is not present
      * in a subdirectory of this store, its files are moved to a subdirectory.
      * This includes all ordinary files present in the store directory, but
      * not subdirectories.
      * <p>
      * When the supplied {@linkplain CertificatePair} is already the default,
      * nothing happens.
      * <p>
      * See also section <a href="#default_certificate">The default certificate</a>.
      */
     public void setDefault(CertificatePair c) throws IOException {
 	// don't do anything if this is already the default
 	CertificatePair oldDefault = getDefault();
 	if (oldDefault==c) return;
 	assert(oldDefault==null || (oldDefault.getPath() != c.getPath()));
 	
 	// first make sure the current default is present as a subdirectory
 	if (oldDefault!=null && oldDefault.getPath().equals(path)) {
 	    // nope, move files out of the way in new dir
 	    oldDefault.store();
 	    File newPath = newItem();
 	    try {
 		FileUtils.MoveFiles(FileUtils.listFilesOnly(path), newPath);
 		oldDefault.load(newPath);
 	    } catch (IOException e) {
 		deletePath(newPath);
 		throw e;
 	    }
 	    
 	// or else remove all files present in subdirectory present in dfl certificate too 
 	} else if (oldDefault!=null) {
 	    // safety check: make sure contents of files is ok
 	    if (!compareDefaultCertificate(oldDefault))
 		throw new IOException(
 			"Default certificate has changed unexpectedly, \n" +
 			"will not change the default certificate for safety.\n" +
 			"You may try to restart the program and try again.\n");
 	    // then delete them
 	    File[] dflfiles = FileUtils.listFilesOnly(oldDefault.getPath());
 	    for (int i=0; i<dflfiles.length; i++)
 		dflfiles[i].delete();
 	}
 	
 	// then copy files from specified CertificatePair to default location
 	try {
 	    // Make sure there are no files that we may accidentally overwrite
 	    // this happens if no default certificate was detected but there are
 	    // still some files we might overwrite.
 	    File[] files = FileUtils.listFilesOnly(c.getPath());
 	    String fmsg = "";
 	    for (int i=0; i<files.length; i++) {
 		File f = new File(path, files[i].getName());
 		if (f.exists())
 		    fmsg += "\n" + f.getPath();
 	    }
 	    if (fmsg!="") {
 		fmsg = "Setting the default certificate would overwrite the following files.\n" +
 		       "For your safety, you have to manually remove them before the default\n" +
 		       "certificate can be set.\n" +
 		       fmsg;
 		throw new IOException(fmsg);
 	    }
 	    FileUtils.CopyFiles(files, path);
 	} catch (IOException e) {
 	    // TODO rollback move of old default to new subdir
 	    throw e;
 	}
 	
 	// and update status
 	defaultCert = c;
 	notifyChanged(indexOf(defaultCert));
     }
     
     /** Find the {@link CertificatePair} that is equal to the default certificate, if any.
      * <p>
      * The files present in the default certificate location must be present in the
      * store location, and they must be equal. So the store location may contain more
      * files than the default certificate location, but not less.
      * <p>
      * In addition to this, a valid key must be present in the certificate location
      * that is checked.
      * 
      * @return {@linkplain CertificatePair} in this store that is equal to the default,
      *         or {@code null} if not found
      */
     protected CertificatePair findDefaultCertificate() throws IOException {
 	// now compare with other certificates in store
 	for (Iterator<CertificatePair> it = iterator(); it.hasNext(); ) {
 	    CertificatePair cert = it.next();
 	    if (compareDefaultCertificate(cert))
 		return cert;
 	}
 	// not found
 	return null;
     }
     
     /** Return whether the supplied {@linkplain CertificatePair} is equal to the default certificate.
      * 
      * @see #findDefaultCertificate
      */
     protected boolean compareDefaultCertificate(CertificatePair other) throws IOException {
 	if (other==null) return false;
 	if (!other.getKeyFile().exists()) return false;
 	File[] otherFiles = FileUtils.listFilesOnly(other.getPath());
 	for (int i=0; i<otherFiles.length; i++) {
 	    File otherFile = otherFiles[i];
 	    File dflFile = new File(path, otherFile.getName());
 	    // file need not exist in default store, except the private key (!)
 	    if (!dflFile.exists()) {
 		if (otherFile.getCanonicalPath() == other.getKeyFile().getCanonicalPath())
 		    return false;
 		continue;
 	    }
 	    // contents must be equal
 	    String otherTxt = FileUtils.readFile(otherFile);
 	    String dflTxt = FileUtils.readFile(dflFile);
 	    if (otherTxt!=null && !otherTxt.equals(dflTxt))
 		return false;
 	}
 	// no
 	return true;
     }
 }
