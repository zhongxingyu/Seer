 package org.eclipse.gmf.internal.xpand.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.nio.charset.Charset;
 
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.expression.SyntaxConstants;
 
 /**
  * Node: no support for relative paths (i.e. '..::templates::SomeTemplate.xpt')
  * @author artem
  */
 public class BundleResourceManager extends ResourceManagerImpl {
 	private final URL[] paths;
 
 	public BundleResourceManager(URL... paths) {
 		assert paths != null && paths.length > 0; 
 		this.paths = new URL[paths.length];
 		for (int i = 0; i < paths.length; i++) {
 			this.paths[i] = fixTrailingSlash(paths[i]);
 		}
 	}
 
 	/**
 	 * new URL("base:url/path1/withoutTrailingSlash", "path2/noLeadingSlash")
 	 * results in "base:url/path/path2/noLeadingSlash" - note lost "withoutTrailingSlash" part
 	 * XXX Perhaps, would be better for clients do this 'normalization'?
 	 */
 	private static URL fixTrailingSlash(URL u) {
 		try {
 			if (u.getPath() != null && !u.getPath().endsWith("/")) {
 				return new URL(u, u.getPath() + '/');
 			}
 		} catch (MalformedURLException ex) {
 			/*IGNORE*/
 		}
 		return u;
 	}
 
 	@Override
 	protected Reader resolve(String fullyQualifiedName, String extension) throws IOException {
 		final String urlPath = fullyQualifiedName.replaceAll(SyntaxConstants.NS_DELIM, "/") + '.' + extension;
 		for (int i = 0; i < paths.length; i++) {
 			try {
 				URL u = new URL(paths[i], urlPath);
 				InputStream is = u.openStream();
 				// XXX here we ignore the fact paths[i] may point to workspace location
 				// and hence charset can be derived from IFile
				return new InputStreamReader(is, Charset.forName("ISO-8859-1"));
 			} catch (MalformedURLException ex) {
 				/*IGNORE*/
 			} catch (IOException ex) {
 				// XXX perhaps, conditionally turn logging on to debug template loading issues?
 				/*IGNORE*/
 			} catch (Exception ex) {
 				// just in case
 				Activator.logError(ex);
 			}
 		}
 		return null;
 	}
 }
