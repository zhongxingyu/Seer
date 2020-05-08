 package org.fao.fi.vme.msaccess.component;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.security.MessageDigest;
 
 import javax.enterprise.inject.Alternative;
 
 @Alternative
 public class EmbeddedMsAccessConnectionProvider extends MsAccessConnectionProvider {
 	final static private String DEFAULT_MS_ACCESS_DB_RESOURCE = "VME_DB_production.accdb";
 
 	private String _resource = DEFAULT_MS_ACCESS_DB_RESOURCE;
 
 	public EmbeddedMsAccessConnectionProvider() {
 		super();
 	}
 
 	public EmbeddedMsAccessConnectionProvider(String resource) {
 		super();
 
 		this._resource = resource;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.fao.fi.vme.msaccess.component.AbstractMsAccessConnectionProvider#
 	 * getDBLocation()
 	 */
 	@Override
 	protected String getDBLocation() {
 		InputStream is = null;
 		FileOutputStream fos = null;
 
 		try {
 			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(this._resource);
 
 			if (is == null)
 				throw new FileNotFoundException("Resource " + this._resource + " is not available in current classpath");
 
 			File temp = new File(new File(System.getProperty("java.io.tmpdir")), this._resource);
 
 			boolean copy = true;
 
 			if (temp.exists()) {
 				String sum = this.getMD5Checksum(this.createChecksum(new FileInputStream(temp)));
 				String resourceSum = this.getMD5Checksum(this.createChecksum(is));
 
 				copy = !resourceSum.equals(sum);
 			}
 
 			if (copy) {
 				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(this._resource);
 
 				LOG.info("Copying embedded resource {} to {}...", this._resource, temp.getAbsolutePath());
 
 				fos = new FileOutputStream(temp);
 
 				byte[] buffer = new byte[65536];
 
 				int copied = 0;
 				int len = -1;
 
 				while ((len = is.read(buffer)) != -1) {
 					fos.write(buffer, 0, len);
 
 					copied += len;
 
 					LOG.info("{} bytes copied so far...", copied);
 
 					fos.flush();
 				}
 
 				LOG.info("{} total bytes copied...", copied);
 
 				fos.flush();
 			} else {
 				LOG.info("Embedded resource was already found in the temp dir as {}", temp.getAbsolutePath());
 			}
 
 			return temp.getAbsolutePath();
 		} catch (FileNotFoundException FNFe) {
 			throw new RuntimeException(FNFe);
		} catch (Exception t) {
 			throw new RuntimeException("Unable to copy embedded VME access file " + DEFAULT_MS_ACCESS_DB_RESOURCE
 					+ " to temp file: " + t.getClass().getSimpleName() + " [ " + t.getMessage() + " ]", t);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
				} catch (Exception tt) {
 					LOG.warn("Unable to close input stream", tt);
 				}
 			}
 
 			if (fos != null) {
 				try {
 					fos.close();
				} catch (Exception tt) {
 					LOG.warn("Unable to close file output stream", tt);
 				}
 			}
 		}
 	}
 
 	private byte[] createChecksum(InputStream inputStream) throws Exception {
 		byte[] buffer = new byte[1024];
 
 		MessageDigest complete = MessageDigest.getInstance("MD5");
 
 		int numRead;
 
 		do {
 			numRead = inputStream.read(buffer);
 			if (numRead > 0) {
 				complete.update(buffer, 0, numRead);
 			}
 		} while (numRead != -1);
 
 		inputStream.close();
 
 		return complete.digest();
 	}
 
 	private String getMD5Checksum(byte[] digest) throws Exception {
 		String result = "";
 
 		for (int i = 0; i < digest.length; i++) {
 			result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
 		}
 		return result;
 	}
 }
