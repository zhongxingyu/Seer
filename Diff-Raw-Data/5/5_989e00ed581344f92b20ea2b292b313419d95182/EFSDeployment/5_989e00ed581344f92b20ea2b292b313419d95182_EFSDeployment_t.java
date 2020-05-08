 package org.eclipse.dltk.internal.launching.execution;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.internal.environment.EFSFileHandle;
 import org.osgi.framework.Bundle;
 
 public class EFSDeployment implements IDeployment {
 	private IFileStore root;
 	private IEnvironment environment;
 
 	public EFSDeployment(IEnvironment env, URI rootURI) throws CoreException {
 		this.environment = env;
 		this.root = EFS.getStore(rootURI);
 		this.root.mkdir(0, null);
 	}
 
 	public IPath add(Bundle bundle, String bundlePath) throws IOException {
 		try {
 			final IFileStore dest = root.getChild(new Path(bundlePath));
 			final Enumeration paths = bundle.getEntryPaths(bundlePath);
 			if (paths != null) {
 				// result is a directory
 				dest.mkdir(EFS.NONE, null);
 				while (paths.hasMoreElements()) {
 					final String path = (String) paths.nextElement();
 					if (path.endsWith("/")) { //$NON-NLS-1$
 						add(bundle, path);
 					} else {
 						copy(bundle.getEntry(path), root.getChild(path));
 					}
 				}
 			} else {
 				final URL url = bundle.getEntry(bundlePath);
 				if (url == null)
 					throw new IOException(MessageFormat.format(Messages.EFSDeployment_failedToLocateEntryForPath, new Object[] { bundlePath }));
 				
 				IFileStore parent = dest.getParent();
 				if (parent != null) {
 					parent.mkdir(EFS.NONE, null);
 				}
 				copy(url, dest);
 			}
 		} catch (CoreException e) {
 			throw new IOException(e.getLocalizedMessage());
 		}
 		return new Path(bundlePath);
 	}
 
 	private static void copy(InputStream input, OutputStream output)
 			throws IOException {
 		int ch = -1;
 		while ((ch = input.read()) != -1) {
 			output.write(ch);
 		}
 	}
 
 	private static void copy(InputStream input, IFileStore file) throws IOException, CoreException {
 		OutputStream output = null;
 		try {
 			output = new BufferedOutputStream(file.openOutputStream(EFS.NONE, null));
 			copy(input, output);
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			if (output != null) {
 				output.close();
 			}
 		}
 	}
 
 	private static void copy(URL url, IFileStore file) throws IOException, CoreException {
 		InputStream input = null;
 		try {
 			input = url.openStream();
 			copy(input, file);
 		} finally {
 			if (input != null) {
 				input.close();
 			}
 		}
 	}
 
 	public void dispose() {
 		try {
 			root.delete(EFS.NONE, null);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public IPath getAbsolutePath() {
 		return new Path(root.toURI().getPath());
 	}
 
 	public IFileHandle getFile(IPath deploymentPath) {
 		return new EFSFileHandle(environment, root.getChild(deploymentPath));
 	}
 
 	public void mkdirs(IPath path) {
 		try {
 			root.getChild(path).mkdir(EFS.NONE, null);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
	public IPath add(InputStream input, String filename) throws IOException {
 		final IFileStore dest = root.getChild(filename);
 		try {
 			copy(input, dest);
 		} catch (CoreException e) {
 			throw new IOException(MessageFormat.format(Messages.EFSDeployment_failedToDeployStream, new Object[] { e.getMessage() }));
 		}
		return new Path( filename );
 	}
 }
