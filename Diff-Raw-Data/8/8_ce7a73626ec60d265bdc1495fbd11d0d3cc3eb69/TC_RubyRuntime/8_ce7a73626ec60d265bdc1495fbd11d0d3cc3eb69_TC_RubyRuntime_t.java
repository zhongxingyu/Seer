 package org.rubypeople.rdt.internal.launching;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileState;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourceAttributes;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.rubypeople.rdt.launching.IInterpreter;
 
 public class TC_RubyRuntime extends TestCase {
 	protected StringWriter runtimeConfigurationWriter = new StringWriter();
 
 	public TC_RubyRuntime(String name) {
 		super(name);
 	}
 	public void testGetInstalledInterpreters() {
 		ShamRubyRuntime runtime = new ShamRubyRuntime();
 		
 		IInterpreter interpreterOne = new RubyInterpreter("InterpreterOne", new File("C:/RubyInstallRootOne"));
 		IInterpreter interpreterTwo = new RubyInterpreter("InterpreterTwo", new File("C:/RubyInstallRootTwo"));
 		
 		assertTrue("Runtime should contain all interpreters.", runtime.getInstalledInterpreters().containsAll(Arrays.asList(new Object[] { interpreterOne, interpreterTwo })));
 		assertTrue("interpreterTwo should be selected interpreter.", runtime.getSelectedInterpreter().equals(interpreterTwo));
 	}
 	public void testSetInstalledInterpreters() {
 		
 		ShamRubyRuntime runtime = new ShamRubyRuntime();
 		
 		IInterpreter interpreterOne = new RubyInterpreter("InterpreterOne", new File("C:/RubyInstallRootOne"));
 		runtime.setInstalledInterpreters(Arrays.asList(new IInterpreter[] { interpreterOne }));
		assertEquals("XML should indicate only one interpreter with it being the selected.", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><runtimeconfig><interpreter name=\"InterpreterOne\" path=\"C:\\RubyInstallRootOne\" selected=\"true\"/></runtimeconfig>", runtimeConfigurationWriter.toString());
 		
 		RubyInterpreter interpreterTwo = new RubyInterpreter("InterpreterTwo", new File("C:/RubyInstallRootTwo"));
 		runtime.setInstalledInterpreters(Arrays.asList(new IInterpreter[] { interpreterOne, interpreterTwo }));
		assertEquals("XML should indicate both interpreters with the first one being selected.", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><runtimeconfig><interpreter name=\"InterpreterOne\" path=\"C:\\RubyInstallRootOne\" selected=\"true\"/><interpreter name=\"InterpreterTwo\" path=\"C:\\RubyInstallRootTwo\"/></runtimeconfig>", runtimeConfigurationWriter.toString());
 		
 		runtime.setSelectedInterpreter(interpreterTwo);
		assertEquals("XML should indicate selected interpreter change.", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><runtimeconfig><interpreter name=\"InterpreterOne\" path=\"C:\\RubyInstallRootOne\"/><interpreter name=\"InterpreterTwo\" path=\"C:\\RubyInstallRootTwo\" selected=\"true\"/></runtimeconfig>", runtimeConfigurationWriter.toString());
 	}
 	protected class ShamRubyRuntime extends RubyRuntime {
 		protected ShamRubyRuntime() {
 			super();
 		}
 		protected Reader getRuntimeConfigurationReader() {
 			return new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><runtimeconfig><interpreter name=\"InterpreterOne\" path=\"C:/RubyInstallRootOne\"/><interpreter name=\"InterpreterTwo\" path=\"C:/RubyInstallRootTwo\" selected=\"true\"/></runtimeconfig>");
 		}
 
 		protected Writer getRuntimeConfigurationWriter() {
 			return runtimeConfigurationWriter;
 		}
 		public void setInstalledInterpreters(List<IInterpreter> newInstalledInterpreters) {
 			super.setInstalledInterpreters(newInstalledInterpreters);
 		}
 		
 		public void saveRuntimeConfiguration() {
 			runtimeConfigurationWriter = new StringWriter();
 			super.saveRuntimeConfiguration() ;
 		}				
 	}
 	protected class RuntimeConfigurationFile implements IFile {
 		protected RuntimeConfigurationFile() {}
 
 		public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
 		}
 		public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}
 
 		public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {}
 
 		public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}
 
 		public InputStream getContents() throws CoreException {
 			return new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><runtimeconfig><interpreter name=\"InterpreterOne\" path=\"C:/RubyInstallRootOne\"/><interpreter name=\"InterpreterTwo\" path=\"C:/RubyInstallRootTwo\" selected=\"true\"/></runtimeconfig>".getBytes());
 		}
 
 		public InputStream getContents(boolean force) throws CoreException {
 			return getContents();
 		}
 
 		public IPath getFullPath() {
 			return null;
 		}
 
 		public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
 			return null;
 		}
 
 		public String getName() {
 			return null;
 		}
 
 		public boolean isReadOnly() {
 			return false;
 		}
 
 		public String getCharset() throws CoreException {
 			return null;
 		}
 		
 		public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}
 
 		public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}
 
 		public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}
 
 		public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {}
 
 		public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {}
 
 		public void accept(IResourceVisitor visitor) throws CoreException {}
 
 		public void clearHistory(IProgressMonitor monitor) throws CoreException {}
 
 		public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {}
 
 		public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {}
 
 		public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public IMarker createMarker(String type) throws CoreException {
 			return null;
 		}
 
 		public void delete(boolean force, IProgressMonitor monitor) throws CoreException {}
 
 		public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {}
 
 		public boolean exists() {
 			return false;
 		}
 
 		public IMarker findMarker(long id) throws CoreException {
 			return null;
 		}
 
 		public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
 			return null;
 		}
 
 		public String getFileExtension() {
 			return null;
 		}
 
 		public IPath getLocation() {
 			return null;
 		}
 
 		public IMarker getMarker(long id) {
 			return null;
 		}
 
 		public long getModificationStamp() {
 			return 0;
 		}
 
 		public IContainer getParent() {
 			return null;
 		}
 
 		public String getPersistentProperty(QualifiedName key) throws CoreException {
 			return null;
 		}
 
 		public IProject getProject() {
 			return null;
 		}
 
 		public IPath getProjectRelativePath() {
 			return null;
 		}
 
 		public Object getSessionProperty(QualifiedName key) throws CoreException {
 			return null;
 		}
 
 		public int getType() {
 			return 0;
 		}
 
 		public IWorkspace getWorkspace() {
 			return null;
 		}
 
 		public boolean isAccessible() {
 			return false;
 		}
 
 		public boolean isDerived() {
 			return false;
 		}
 
 		public boolean isLocal(int depth) {
 			return false;
 		}
 
 		public boolean isPhantom() {
 			return false;
 		}
 
 		public boolean isTeamPrivateMember() {
 			return false;
 		}
 
 		public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {}
 
 		public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {}
 
 		public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {}
 
 		public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {}
 
 		public void setDerived(boolean isDerived) throws CoreException {}
 
 		public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {}
 
 		public void setPersistentProperty(QualifiedName key, String value) throws CoreException {}
 
 		public void setReadOnly(boolean readOnly) {}
 
 		public void setSessionProperty(QualifiedName key, Object value) throws CoreException {}
 
 		public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {}
 
 		public void touch(IProgressMonitor monitor) throws CoreException {}
 
 		public Object getAdapter(Class adapter) {
 			return null;
 		}
 
 		public boolean isSynchronized(int depth) {
 			return false;
 		}
 
 		public int getEncoding() throws CoreException {
 			return 0;
 		}
 
 		public void createLink(
 			IPath localLocation,
 			int updateFlags,
 			IProgressMonitor monitor)
 			throws CoreException {
 		}
 
 		public IPath getRawLocation() {
 			return null;
 		}
 
 		public boolean isLinked() {
 			return false;
 		}
 
 
 		public void accept(IResourceProxyVisitor arg0, int arg1) throws CoreException {
 		}
 
 		public long getLocalTimeStamp() {
 		
 			return 0;
 		}
 
 		public long setLocalTimeStamp(long value) throws CoreException {
 
 			return 0;
 		}
 
 		public boolean contains(ISchedulingRule rule) {
 
 			return false;
 		}
 
 		public boolean isConflicting(ISchedulingRule rule) {
 
 			return false;
 		}
 		public void setCharset(String newCharset) throws CoreException {
 		}
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.resources.IFile#getCharset(boolean)
 		 */
 		public String getCharset(boolean checkImplicit) throws CoreException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.resources.IFile#getContentDescription()
 		 */
 		public IContentDescription getContentDescription() throws CoreException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public String getCharsetFor(Reader reader) throws CoreException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public ResourceAttributes getResourceAttributes() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public void revertModificationStamp(long value) throws CoreException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public URI getLocationURI() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
         public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
             // TODO Auto-generated method stub
             
         }
 
         public URI getRawLocationURI() {
             // TODO Auto-generated method stub
             return null;
         }
 
         public boolean isLinked(int options) {
             // TODO Auto-generated method stub
             return false;
         }
 
 		public IResourceProxy createProxy() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 
 	}
 }
