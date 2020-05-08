 package info.freelibrary.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ResourceBundle;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PairtreeObject extends File {
 
 	/**
 	 * The <code>serialVersionUID</code> for a <code>PairtreeFile</code>.
 	 */
 	private static final long serialVersionUID = 5022790117870380626L;
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(PairtreeObject.class);
 
 	private static final XMLResourceBundle BUNDLE = (XMLResourceBundle) ResourceBundle
 			.getBundle("Messages", new XMLBundleControl());
 
 	/**
 	 * Creates a new Pairtree object for the supplied name in the supplied
 	 * Pairtree structure.
 	 * 
 	 * @param aRoot An existing Pairtree structure
 	 * @param aName The name of the Pairtree object
 	 * @throws IOException If there is a problem creating the Pairtree structure
 	 */
 	PairtreeObject(PairtreeRoot aRoot, String aName) throws IOException {
 		super(aRoot, PairtreeUtils.mapToPPath(aName));
 
 		if (!exists() && !mkdirs()) {
 			throw new IOException(BUNDLE.get("pt.cant_mkdirs", this));
 		}
 
 		if (LOGGER.isInfoEnabled()) {
			LOGGER.debug(BUNDLE.get("pt.object_created1", new String[] {
					getAbsolutePath(), aName }));
 		}
 	}
 
 	/**
 	 * Creates a new Pairtree object for the supplied name in the supplied
 	 * Pairtree structure.
 	 * 
 	 * @param aRoot The root of the Pairtree structure
 	 * @param aPrefix A Pairtree prefix to use when creating the directory
 	 *        structure
 	 * @param aName The name of the Pairtree object to create
 	 * @throws IOException If there is a problem creating the object
 	 */
 	PairtreeObject(PairtreeRoot aRoot, String aPrefix, String aName)
 			throws IOException {
 		super(aRoot, PairtreeUtils.mapToPPath(aPrefix, aName));
 
 		if (!exists() && !mkdirs()) {
 			throw new IOException(BUNDLE.get("pt.cant_mkdirs", this));
 		}
 
 		if (LOGGER.isInfoEnabled()) {
			LOGGER.debug(BUNDLE.get("pt.object_created2", new String[] {
					getAbsolutePath(), aPrefix, aName }));
 		}
 	}
 
 	/**
 	 * Returns a string representation of the Pairtree object.
 	 */
 	public String toString() {
 		return getAbsolutePath();
 	}
 }
