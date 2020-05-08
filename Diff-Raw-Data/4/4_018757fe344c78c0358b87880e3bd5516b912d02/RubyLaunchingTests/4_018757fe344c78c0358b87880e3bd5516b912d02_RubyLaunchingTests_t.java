 package org.eclipse.dltk.ruby.debug.tests.launching;
 
 import junit.framework.Test;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.core.tests.launching.ScriptLaunchingTests;
 import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.debug.RubyDebugConstants;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 import org.eclipse.dltk.ruby.launching.RubyLaunchConfigurationDelegate;
 
 public class RubyLaunchingTests extends ScriptLaunchingTests {
 
 	public RubyLaunchingTests(String name) {
 		super("org.eclipse.dltk.ruby.core.tests", name);
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
 
 	public void testDebug() throws Exception {
 		RubyDebugPlugin.getDefault().getPluginPreferences().setValue(
 				RubyDebugConstants.DEBUGGING_ENGINE_ID_KEY,
 				"org.eclipse.dltk.ruby.basicdebugger");
 
 		super.testDebug();
 	}

	protected String getScriptFileName() {
		return "/launching/src/test.rb";
	}
 }
