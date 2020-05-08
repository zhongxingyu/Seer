 package org.eclipse.dltk.ruby.debug.tests.launching;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Test;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.core.tests.launching.ScriptLaunchingTests;
 import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.debug.RubyDebugConstants;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 import org.eclipse.dltk.ruby.launching.RubyLaunchConfigurationDelegate;
 
 public class RubyLaunchingTests extends ScriptLaunchingTests {
 
 	public RubyLaunchingTests(String name) {
 		super("org.eclipse.dltk.ruby.debug.tests", name);
 	}
 
 	public RubyLaunchingTests(String testProjectName, String name) {
 		super(testProjectName, name);
 	}
 
 	public static Test suite() {
 		return new Suite(RubyLaunchingTests.class);
 	}
 
 	protected String getProjectName() {
 		return "launching";
 	}
 
 	protected String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	protected String getDebugModelId() {
 		return RubyDebugConstants.DEBUG_MODEL_ID;
 	}
 
 	protected ILaunchConfiguration createLaunchConfiguration(String arguments) {
 		return createTestLaunchConfiguration(getNatureId(), getProjectName(),
 				"src/test.rb", arguments);
 	}
 
 	protected void startLaunch(ILaunch launch) throws CoreException {
 		final AbstractScriptLaunchConfigurationDelegate delegate = new RubyLaunchConfigurationDelegate();
 		delegate.launch(launch.getLaunchConfiguration(),
 				launch.getLaunchMode(), launch, null);
 	}
 
 	public void testDebugRuby() throws Exception {
 		RubyDebugPlugin.getDefault().getPluginPreferences().setValue(
 				RubyDebugConstants.DEBUGGING_ENGINE_ID_KEY,
 				"org.eclipse.dltk.ruby.basicdebugger");
 
 		DebugEventStats stats = this.internalTestDebug("ruby");
 		int suspendCount = stats.getSuspendCount();
 		assertEquals(1, suspendCount);
 
		assertEquals(3, stats.getResumeCount());
 
 		// Checking extended events count
 		assertEquals(1, stats.getBeforeVmStarted());
 		assertEquals(1, stats.getBeforeCodeLoaded());
 		assertEquals(2, stats.getBeforeResumeCount());
 		assertEquals(1, stats.getBeforeSuspendCount());
 	}
 	public void testFastDebugRuby() throws Exception {
 		RubyDebugPlugin.getDefault().getPluginPreferences().setValue(
 				RubyDebugConstants.DEBUGGING_ENGINE_ID_KEY,
 				"org.eclipse.dltk.ruby.fastdebugger");
 		DebugEventStats stats = this.internalTestDebug("ruby");
 		int suspendCount = stats.getSuspendCount();
 		assertEquals(1, suspendCount);
 
		assertEquals(3, stats.getResumeCount());
 
 		// Checking extended events count
 		assertEquals(1, stats.getBeforeVmStarted());
 		assertEquals(1, stats.getBeforeCodeLoaded());
 		assertEquals(2, stats.getBeforeResumeCount());
 		assertEquals(1, stats.getBeforeSuspendCount());
 	}
 
 
 	public void testDebugJRuby() throws Exception {
 		RubyDebugPlugin.getDefault().getPluginPreferences().setValue(
 				RubyDebugConstants.DEBUGGING_ENGINE_ID_KEY,
 				"org.eclipse.dltk.ruby.basicdebugger");
 
 		DebugEventStats stats = this.internalTestDebug("jruby");
 		int suspendCount = stats.getSuspendCount();
 		assertEquals(1, suspendCount);
 
 		assertEquals(3, stats.getResumeCount());
 
 		// Checking extended events count
 		assertEquals(1, stats.getBeforeVmStarted());
 		assertEquals(1, stats.getBeforeCodeLoaded());
 		assertEquals(2, stats.getBeforeResumeCount());
 		assertEquals(1, stats.getBeforeSuspendCount());
 	}
 
 	public void testRuby() throws Exception {
 		String NAME = "ruby";
 		this.internalTestRequiredInterpreterAvailable(NAME);
 		this.internalTestRun(NAME);
 	}
 
 	public void testJRuby() throws Exception {
 		String NAME = "jruby";
 		this.internalTestRequiredInterpreterAvailable(NAME);
 		this.internalTestRun(NAME);
 	}
 
 	protected IInterpreterInstall[] getPredefinedInterpreterInstalls() {
 		IInterpreterInstallType[] installTypes = ScriptRuntime
 				.getInterpreterInstallTypes(RubyNature.NATURE_ID);
 		int id = 0;
 		List installs = new ArrayList();
 		for (int i = 0; i < installTypes.length; i++) {
 			String installId = getNatureId() + "_";
 			createAddInstall(installs, "/usr/bin/ruby", installId
 					+ Integer.toString(++id), installTypes[i]);
 			createAddInstall(installs, "/home/dltk/apps/jruby/bin/jruby",
 					installId + Integer.toString(++id), installTypes[i]);
 		}
 		if (installs.size() > 0) {
 			return (IInterpreterInstall[]) installs
 					.toArray(new IInterpreterInstall[installs.size()]);
 		}
 		return searchInstalls(RubyNature.NATURE_ID);
 	}
 
 	protected boolean hasPredefinedInterpreters() {
 		return true;
 	}
 }
