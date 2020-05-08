 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Andrei Sobolev)
  *******************************************************************************/
 package org.eclipse.dltk.core.internal.rse;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileInfo;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.internal.rse.perfomance.RSEPerfomanceStatistics;
 import org.eclipse.dltk.core.internal.rse.ssh.RSESshManager;
 import org.eclipse.dltk.ssh.core.ISshConnection;
 import org.eclipse.dltk.ssh.core.ISshFileHandle;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.internal.efs.RSEFileSystem;
 
 public class RSEFileHandle implements IFileHandle {
 
 	private static Map<String, Long> timestamps = new HashMap<String, Long>();
 	private static Map<String, Long> lastaccess = new HashMap<String, Long>();
 
 	private IFileStore file;
 	private IEnvironment environment;
 	private ISshFileHandle sshFile;
 
 	/**
 	 * @since 2.0
 	 */
 	public RSEFileHandle(IEnvironment env, IFileStore file) {
 		this.environment = env;
 		this.file = file;
 	}
 
 	private void fetchSshFile() {
 		if (sshFile != null) {
 			return;
 		}
 		if (environment instanceof RSEEnvironment) {
 			RSEEnvironment rseEnv = (RSEEnvironment) environment;
 			IHost host = rseEnv.getHost();
 			ISshConnection connection = RSESshManager.getConnection(host);
 			if (connection != null) { // This is ssh connection, and it's alive.
 				try {
 					sshFile = connection.getHandle(new Path(file.toURI()
 							.getPath()));
 				} catch (Exception e) {
 					DLTKRSEPlugin.log("Failed to locate direct ssh connection",
 							e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public RSEFileHandle(IEnvironment env, IFileStore file,
 			ISshFileHandle sshFile) {
 		this.environment = env;
 		this.file = file;
 		this.sshFile = sshFile;
 	}
 
 	public RSEFileHandle(IEnvironment env, URI locationURI) {
 		this(env, RSEFileSystem.getInstance().getStore(locationURI));
 	}
 
 	public boolean exists() {
 		fetchSshFile();
 		if (sshFile != null) {
 			return sshFile.exists();
 		}
 		try {
 			return file.fetchInfo().exists();
 		} catch (RuntimeException e) {
 			return false;
 		}
 	}
 
 	public String toOSString() {
 		return this.environment.convertPathToString(getPath());
 	}
 
 	public String getCanonicalPath() {
 		return this.environment.getCanonicalPath(getPath());
 	}
 
 	public IFileHandle getChild(final String childname) {
 		fetchSshFile();
 		IFileStore childStore = file.getChild(new Path(childname));
 		if (sshFile != null) {
 			return new RSEFileHandle(environment, childStore, sshFile
 					.getChild(childname));
 		}
 		return new RSEFileHandle(environment, childStore);
 	}
 
 	public IFileHandle[] getChildren() {
 		fetchSshFile();
 		if (sshFile != null) {
 			try {
 				ISshFileHandle[] children = sshFile
 						.getChildren(new NullProgressMonitor());
 				IFileHandle rseChildren[] = new IFileHandle[children.length];
 				for (int i = 0; i < children.length; i++) {
 					IFileStore childStore = file.getChild(new Path(children[i]
 							.getName()));
 					rseChildren[i] = new RSEFileHandle(environment, childStore,
 							children[i]);
 				}
 				return rseChildren;
 			} catch (CoreException e) {
 				DLTKRSEPlugin.log(e);
 			}
 		}
 		try {
 			IFileStore[] files = file.childStores(EFS.NONE, null);
 			IFileHandle[] children = new IFileHandle[files.length];
 			for (int i = 0; i < files.length; i++)
 				children[i] = new RSEFileHandle(environment, files[i]);
 			return children;
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 			return null;
 		}
 	}
 
 	public IEnvironment getEnvironment() {
 		return environment;
 	}
 
 	public URI toURI() {
 		return file.toURI();
 	}
 
 	public String getName() {
 		return file.getName();
 	}
 
 	public IFileHandle getParent() {
 		IFileStore parent = file.getParent();
 		if (parent == null)
 			return null;
 		return new RSEFileHandle(environment, parent);
 	}
 
 	public IPath getPath() {
 		return new Path(file.toURI().getPath());
 	}
 
 	public boolean isDirectory() {
 		fetchSshFile();
 		if (sshFile != null) {
 			return sshFile.isDirectory();
 		}
 		return file.fetchInfo().isDirectory();
 	}
 
 	public boolean isFile() {
 		fetchSshFile();
 		if (sshFile != null) {
 			return sshFile.exists() && !sshFile.isDirectory();
 		}
 		final IFileInfo info = file.fetchInfo();
 		return info.exists() && !info.isDirectory();
 	}
 
 	public boolean isSymlink() {
 		fetchSshFile();
 		if (sshFile != null) {
 			return sshFile.isSymlink();
 		}
 		return file.fetchInfo().getAttribute(EFS.ATTRIBUTE_SYMLINK);
 	}
 
 	private InputStream internalOpenInputStream(IProgressMonitor monitor)
 			throws IOException {
 		fetchSshFile();
 		if (sshFile != null) {
 			try {
 				return sshFile.getInputStream(monitor);
 			} catch (CoreException e) {
				throw new IOException(e);
 			}
 		}
 		try {
 			return file.openInputStream(EFS.NONE, monitor);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 			throw new IOException(e.getLocalizedMessage());
 		}
 	}
 
 	public InputStream openInputStream(IProgressMonitor monitor)
 			throws IOException {
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			return new CountStream(this.internalOpenInputStream(monitor));
 		}
 		return this.internalOpenInputStream(monitor);
 	}
 
 	public OutputStream openOutputStream(IProgressMonitor monitor)
 			throws IOException {
 		fetchSshFile();
 		if (sshFile != null) {
 			try {
 				return sshFile.getOutputStream(monitor);
 			} catch (CoreException e) {
				throw new IOException(e);
 			}
 		}
 		try {
 			return file.openOutputStream(EFS.NONE, monitor);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 			throw new IOException(e.getLocalizedMessage());
 		}
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof RSEFileHandle) {
 			RSEFileHandle anotherFile = (RSEFileHandle) obj;
 			return this.file.equals(anotherFile.file);
 		}
 		return false;
 	}
 
 	public int hashCode() {
 		return file.hashCode();
 	}
 
 	public String toString() {
 		return toOSString();
 	}
 
 	public long lastModified() {
 		fetchSshFile();
 		String n = toString();
 		long c = 0;
 		boolean flag = !environment.isLocal();
 		if (flag) {
 			if (timestamps.containsKey(n)) {
 				c = System.currentTimeMillis();
 				Long last = lastaccess.get(n);
 				if (last != null
 						&& (c - last.longValue()) < 1000 * 60 * 60 * 24) {
 					return timestamps.get(n);
 				}
 			}
 		}
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 		long lm = 0;
 		if (sshFile != null) {
 			lm = sshFile.lastModificationTime();
 		} else {
 			lm = file.fetchInfo().getLastModified();
 		}
 		if (flag) {
 			timestamps.put(n, lm);
 			if (c == 0) {
 				c = System.currentTimeMillis();
 			}
 			lastaccess.put(n, c);
 		}
 		p.done("#", "Return file timestamp", 0);
 		return lm;
 
 	}
 
 	public long length() {
 		fetchSshFile();
 		if (sshFile != null) {
 			return sshFile.getSize();
 		}
 		return file.fetchInfo().getLength();
 	}
 
 	public IPath getFullPath() {
 		return EnvironmentPathUtils.getFullPath(environment, getPath());
 	}
 
 	public String getEnvironmentId() {
 		return environment.getId();
 	}
 
 	private final class CountStream extends BufferedInputStream {
 		private InputStream stream;
 
 		public CountStream(InputStream stream) {
 			super(stream);
 		}
 
 		public int read() throws IOException {
 			int read = stream.read();
 			if (read != -1) {
 				RSEPerfomanceStatistics
 						.inc(RSEPerfomanceStatistics.TOTAL_BYTES_RECEIVED);
 			}
 			return read;
 		}
 
 		public int read(byte[] b, int off, int len) throws IOException {
 			int read = this.stream.read(b, off, len);
 			if (read != -1) {
 				RSEPerfomanceStatistics.inc(
 						RSEPerfomanceStatistics.TOTAL_BYTES_RECEIVED, read);
 			}
 			return read;
 		}
 
 		public int read(byte[] b) throws IOException {
 			int read = this.stream.read(b);
 			if (read != -1) {
 				RSEPerfomanceStatistics.inc(
 						RSEPerfomanceStatistics.TOTAL_BYTES_RECEIVED, read);
 			}
 			return read;
 		}
 	}
 }
