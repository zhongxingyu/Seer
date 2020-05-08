 package org.eclipse.dltk.core.internal.rse;
 
 import java.net.URI;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.internal.efs.RSEFileSystem;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 
 public class RSEEnvironment implements IEnvironment, IAdaptable {
 	private IRemoteFileSubSystem fs;
 	private IHost host;
 
 	public RSEEnvironment(IRemoteFileSubSystem fs) {
 		this.fs = fs;
 		this.host = fs.getConnectorService().getHost();
 	}
 
 	public IFileHandle getFile(IPath path) {
 		if (Path.EMPTY.equals(path)) {
 			throw new RuntimeException(
 					"Passing empty filename to RSEEnvironment.getFile() method...");
 		}
 		URI uri = RSEFileSystem.getURIFor(host.getHostName(), path.toString());
 		return new RSEFileHandle(this, uri);
 	}
 
 	public String getId() {
 		return RSEEnvironmentProvider.RSE_ENVIRONMENT_PREFIX
 				+ host.getAliasName();
 	}
 
 	public String getSeparator() {
 		return fs.getSeparator();
 	}
 
 	public char getSeparatorChar() {
 		return fs.getSeparatorChar();
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof RSEEnvironment) {
 			RSEEnvironment other = (RSEEnvironment) obj;
 			return getId().equals(other.getId());
 		}
 		return false;
 	}
 
 	public int hashCode() {
 		return getId().hashCode();
 	}
 
 	public String getName() {
 		return host.getName() + " (RSE)";
 	}
 
 	public IHost getHost() {
 		return host;
 	}
 
 	public Object getAdapter(Class adapter) {
 		return Platform.getAdapterManager()
 				.loadAdapter(this, adapter.getName());
 	}
 
 	public URI getURI(IPath location) {
 		return RSEFileSystem.getURIFor(host.getHostName(), location
 				.toPortableString());
 	}
 
 	public String convertPathToString(IPath path) {
 		if (host.getSystemType().isWindows()) {
 			return path.toString().replace('/', '\\');
 		} else {
 			return path.toString();
 		}
 	}
 
 	public IFileHandle getFile(URI locationURI) {
 		return new RSEFileHandle(this, locationURI);
 	}
 
 	public String getPathsSeparator() {
 		return Character.toString(getPathsSeparatorChar());
 	}
 
 	public char getPathsSeparatorChar() {
 		return host.getSystemType().isWindows() ? ';' : ':';
 	}

	public String getCanonicalPath(IPath path) {
		return convertPathToString(path);
	}
 }
