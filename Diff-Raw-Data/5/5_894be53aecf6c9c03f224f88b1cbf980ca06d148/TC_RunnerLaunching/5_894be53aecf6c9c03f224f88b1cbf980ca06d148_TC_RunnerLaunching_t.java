 package org.rubypeople.rdt.internal.launching;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.debug.core.model.IProcess;
 import org.rubypeople.eclipse.shams.debug.core.ShamLaunchConfigurationType;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.tests.ModifyingResourceTest;
 import org.rubypeople.rdt.internal.debug.core.model.RubyDebugTarget;
 import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
 import org.rubypeople.rdt.launching.IVMInstall;
 import org.rubypeople.rdt.launching.IVMInstallType;
 import org.rubypeople.rdt.launching.RubyRuntime;
 import org.rubypeople.rdt.launching.VMStandin;
 
 public class TC_RunnerLaunching extends ModifyingResourceTest {
 
 	private final static String PROJECT_NAME = "Simple Project";
 	private final static String RUBY_LIB_DIR = "someRubyDir"; // dir inside
 	// project
 	private final static String RUBY_FILE_NAME = "rubyFile.rb";
 	private final static String INTERPRETER_ARGUMENTS = "interpreter Arguments";
 	private final static String PROGRAM_ARGUMENTS = "programArguments";
 
 	private static final String VM_TYPE_ID = "org.rubypeople.rdt.launching.TestVMType";
 	private IVMInstallType vmType;
 	private IVMInstall interpreter;
 	private IRubyProject project;
 
 	public TC_RunnerLaunching(String name) {
 		super(name);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		project = createRubyProject('/' + PROJECT_NAME);
 		IFolder location = createFolder('/' + PROJECT_NAME + "/interpreterOne");
 		createFolder('/' + PROJECT_NAME + "/interpreterOne/lib");
 		createFolder('/' + PROJECT_NAME + "/interpreterOne/bin");
 		createFile('/' + PROJECT_NAME + "/interpreterOne/bin/ruby", "");
 
 		vmType = RubyRuntime.getVMInstallType(VM_TYPE_ID);
 		VMStandin standin = new VMStandin(vmType, "fake");
 		standin.setName("fake");
 		standin.setInstallLocation(location.getLocation().toFile());
 		interpreter = standin.convertToRealVM();
 		RubyRuntime.setDefaultVMInstall(interpreter, null, true);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		deleteProject('/' + PROJECT_NAME);
 		vmType.disposeVMInstall(interpreter.getId());
 	}
 
 	protected ILaunchManager getLaunchManager() {
 		return DebugPlugin.getDefault().getLaunchManager();
 	}
 
 	protected String getCommandLine(IProject project, String debugFile, boolean debug) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(" \"");
 		buffer.append(new Path(interpreter.getInstallLocation().getAbsolutePath()).append("bin").append("ruby").toOSString());
 		buffer.append("\" ");
 		buffer.append(INTERPRETER_ARGUMENTS);
 		if (debug) {
 			buffer.append(" -I ");
 			if (Platform.getOS().equals(Platform.OS_WIN32))
 				buffer.append("\"");
 			buffer.append(new Path(StandardVMDebugger.getDirectoryOfRubyDebuggerFile()).toOSString());
 			if (Platform.getOS().equals(Platform.OS_WIN32))
 				buffer.append("\"");
 		}
 		addSyncArgs(buffer);
 		if (debug) {
 			buffer.append(" -r");
 			buffer.append(debugFile);
 			buffer.append(" -rclassic-debug");
 		}
 		buffer.append(" -- ");
 		buffer.append(RUBY_LIB_DIR);
 		buffer.append(File.separator);
 		buffer.append(RUBY_FILE_NAME);
 		buffer.append(' ');
 		buffer.append(PROGRAM_ARGUMENTS);
 		return buffer.toString();
 	}
 
 	private void addSyncArgs(StringBuffer buffer) {
 		buffer.append(" -I ");
 		if (Platform.getOS().equals(Platform.OS_WIN32))
 			buffer.append("\"");
		buffer.append(LaunchingPlugin.getFileInPlugin(new Path("ruby").append(StandardVMRunner.STREAM_FLUSH_SCRIPT)).getParent());
 		if (Platform.getOS().equals(Platform.OS_WIN32))
 			buffer.append("\"");
		buffer.append(" -r" + StandardVMRunner.STREAM_FLUSH_SCRIPT);
 	}
 
 	public void testDebugEnabled() throws Exception {
 		// check if debugging is enabled in plugin.xml
 		ILaunchConfigurationType launchConfigurationType = getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
 		assertEquals("Ruby Application", launchConfigurationType.getName());
 		assertTrue("LaunchConfiguration supports debug", launchConfigurationType.supportsMode(ILaunchManager.DEBUG_MODE));
 	}
 
 	public void launch(boolean debug) throws Exception {
 		ILaunchConfiguration configuration = new ShamLaunchConfiguration();
 		ILaunch launch = new Launch(configuration, debug ? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE, null);
 		ILaunchConfigurationType launchConfigurationType = getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
 		launchConfigurationType.getDelegate(debug ? "debug" : "run").launch(configuration, debug ? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE, launch, null);
 
 		RubyDebugTarget debugTarget = (RubyDebugTarget) launch.getDebugTarget();
 		String debugFile = "";
 		if (debug) {
 			debugFile = debugTarget.getDebugParameterFile().getAbsolutePath();
 		}
 
 		assertEquals("Only one process should have been spawned", 1, launch.getProcesses().length);
 		IProcess process = launch.getProcesses()[0];
 		String expected = getCommandLine(project.getProject(), debugFile, debug);
 		assertEquals(expected, process.getAttribute(IProcess.ATTR_CMDLINE));
 	}
 
 	public void testRunInDebugMode() throws Exception {
 		launch(true);
 	}
 
 	public void testRunInRunMode() throws Exception {
 		launch(false);
 	}
 
 	public class ShamLaunchConfiguration implements ILaunchConfiguration {
 		public boolean contentsEqual(ILaunchConfiguration configuration) {
 			return false;
 		}
 
 		public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
 			return null;
 		}
 
 		public void delete() throws CoreException {}
 
 		public boolean exists() {
 			return true;
 		}
 
 		public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
 			return defaultValue;
 		}
 
 		public int getAttribute(String attributeName, int defaultValue) throws CoreException {
 			return defaultValue;
 		}
 
 		public List getAttribute(String attributeName, List defaultValue) throws CoreException {
 			return defaultValue;
 		}
 
 		public Map getAttribute(String attributeName, Map defaultValue) throws CoreException {
 			return defaultValue;
 		}
 
 		public String getAttribute(String attributeName, String defaultValue) throws CoreException {
 			if (attributeName.equals(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME)) {
 				return PROJECT_NAME;
 			} else if (attributeName.equals(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME)) {
 				return RUBY_LIB_DIR + File.separator + RUBY_FILE_NAME;
 			} else if (attributeName.equals(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY)) {
 				return '/' + PROJECT_NAME;
 			} else if (attributeName.equals(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS)) {
 				return PROGRAM_ARGUMENTS;
 			} else if (attributeName.equals(IRubyLaunchConfigurationConstants.ATTR_VM_ARGUMENTS)) {
 				return INTERPRETER_ARGUMENTS;
 			}
 
 			return defaultValue;
 		}
 
 		public IFile getFile() {
 			return null;
 		}
 
 		public IPath getLocation() {
 			return null;
 		}
 
 		public String getMemento() throws CoreException {
 			return null;
 		}
 
 		public String getName() {
 			return null;
 		}
 
 		public ILaunchConfigurationType getType() throws CoreException {
 			return new ShamLaunchConfigurationType();
 		}
 
 		public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
 			return null;
 		}
 
 		public boolean isLocal() {
 			return false;
 		}
 
 		public boolean isWorkingCopy() {
 			return false;
 		}
 
 		public ILaunch launch(String mode, IProgressMonitor monitor) throws CoreException {
 			return null;
 		}
 
 		public boolean supportsMode(String mode) throws CoreException {
 			return false;
 		}
 
 		public Object getAdapter(Class adapter) {
 			return null;
 		}
 
 		public String getCategory() throws CoreException {
 			return null;
 		}
 
 		public Map getAttributes() throws CoreException {
 			return null;
 		}
 
 		public ILaunch launch(String mode, IProgressMonitor monitor, boolean build) throws CoreException {
 			return null;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String,
 		 *      org.eclipse.core.runtime.IProgressMonitor, boolean, boolean)
 		 */
 		public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource[] getMappedResources() throws CoreException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean isMigrationCandidate() throws CoreException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void migrate() throws CoreException {
 		// TODO Auto-generated method stub
 
 		}
 	}
 
 }
