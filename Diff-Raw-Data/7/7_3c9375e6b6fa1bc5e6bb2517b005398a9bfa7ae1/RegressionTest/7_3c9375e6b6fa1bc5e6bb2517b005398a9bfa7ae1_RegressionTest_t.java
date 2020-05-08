 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Scanner;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.api.TreeItem;
 import org.jboss.reddeer.swt.condition.ShellWithTextIsAvailable;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.handler.ShellHandler;
 import org.jboss.reddeer.swt.impl.button.CheckBox;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.ctab.DefaultCTabItem;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.menu.ShellMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
 import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.reddeer.swt.matcher.RegexMatcher;
 import org.jboss.reddeer.swt.matcher.WithTooltipTextMatcher;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.fuse.reddeer.component.Log;
 import org.jboss.tools.fuse.reddeer.condition.FuseLogContainsText;
 import org.jboss.tools.fuse.reddeer.debug.BreakpointsView;
 import org.jboss.tools.fuse.reddeer.debug.IsRunning;
 import org.jboss.tools.fuse.reddeer.debug.ResumeButton;
 import org.jboss.tools.fuse.reddeer.editor.CamelEditor;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.preference.ServerRuntimePreferencePage;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.reddeer.server.ServerManipulator;
 import org.jboss.tools.fuse.reddeer.utils.ResourceHelper;
 import org.jboss.tools.fuse.reddeer.view.JMXNavigator;
 import org.jboss.tools.fuse.ui.bot.test.utils.EditorManipulator;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * Class contains test cases verifying resolved issues
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
 @Server(type = ServerReqType.Fuse, state = ServerReqState.PRESENT)
 public class RegressionTest {
 
 	@InjectRequirement
 	private ServerRequirement serverRequirement;
 
 	@After
 	public void clean() {
 
 		String server = serverRequirement.getConfig().getName();
 		if (ServerManipulator.isServerStarted(server)) {
 			ServerManipulator.stopServer(server);
 		}
 		new DefaultToolItem(new WorkbenchShell(), 0, new WithTooltipTextMatcher(new RegexMatcher("Save All.*"))).click();
 		new ProjectExplorer().deleteAllProjects();
 		ShellHandler.getInstance().closeAllNonWorbenchShells();
 		try {
 			new ConsoleView().terminateConsole();
 		} catch (Exception e) {
 		}
 	}
 
 	/**
 	 * GUI editor issue when using route scoped onException
 	 * https://issues.jboss.org/browse/FUSETOOLS-674
 	 * 
 	 * NOTE: not fixed yet - deferred to 8.0
 	 */
 	@Ignore
 	@Test
 	public void issue_674() throws ParserConfigurationException, SAXException, IOException {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
 		new DefaultCTabItem("Source").activate();
 
 		// copy sample of camel-context.xml
 		File testFile = new File(ResourceHelper.getResourceAbsolutePath(Activator.PLUGIN_ID,
 				"resources/camel-context.xml"));
 		DefaultStyledText editor = new DefaultStyledText();
 		Scanner scanner = new Scanner(testFile);
 		scanner.useDelimiter("\\Z");
 		editor.setText(scanner.next());
 		scanner.close();
 
 		new DefaultCTabItem("Design").activate();
 		new DefaultCTabItem("Source").activate();
 		new ShellMenu("File", "Save").select();
 
 		// check XML content
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		InputSource is = new InputSource(new StringReader(new DefaultStyledText().getText()));
 		Document doc = builder.parse(is);
 		int i = doc.getElementsByTagName("onException").item(0).getChildNodes().getLength();
 
 		assertEquals("'camel-context.xml' file was changed!", 11, i);
 	}
 
 	/**
 	 * Propose a DebugAs option to start CamelContext & debug java code used by beans from camel routes
 	 * https://issues.jboss.org/browse/FUSETOOLS-853
 	 */
 	@Test
 	public void issue_853() {
 
 		ProjectFactory.createProject("camel-blueprint", "camel-archetype-blueprint");
 		new ProjectExplorer().getProject("camel-blueprint").getProjectItem("src/main/resources", "OSGI-INF", "blueprint", "blueprint.xml").select();
 		try {
 			new ContextMenu("Debug As", "3 Local Camel Context (without tests)");
 		} catch (Exception e) {
 			fail("Context menu 'Debug As --> Local Camel Context' is missing");
 		}
 	}
 
 	/**
 	 * New Server Runtime Wizard - Cancel/Finish button error
 	 * https://issues.jboss.org/browse/FUSETOOLS-1067
 	 */
 	@Test
 	public void issue_1067() {
 
 		new ServerRuntimePreferencePage().open();
 
 		new PushButton("Add...").click();
 		new DefaultShell("New Server Runtime Environment").setFocus();
 		new DefaultTreeItem("JBoss Fuse").expand();
 
 		// tests the _Finish_ button
 		for (TreeItem item : new DefaultTreeItem("JBoss Fuse").getItems()) {
 			if (!item.getText().startsWith("JBoss"))
 				continue;
 			AbstractWait.sleep(TimePeriod.SHORT);
 			item.select();
 			try {
 
 				assertFalse(new PushButton("Finish").isEnabled());
 			} catch (AssertionError ex) {
 
 				new DefaultTreeItem("JBoss Fuse").select();
 				AbstractWait.sleep(TimePeriod.SHORT);
 				new PushButton("Cancel").click();
 				AbstractWait.sleep(TimePeriod.NORMAL);
 				new DefaultShell().close();
 				throw ex;
 			}
 		}
 
 		// tests the _Cancel_ button
 		AbstractWait.sleep(TimePeriod.SHORT);
 		new DefaultTreeItem("JBoss Fuse", serverRequirement.getConfig().getServerBase().getName()).select();
 		new PushButton("Cancel").click();
 		try {
 
 			assertTrue(new DefaultShell().getText().equals("Preferences"));
 		} catch (AssertionError ex) {
 
 			new DefaultShell().close();
 			new DefaultTreeItem("JBoss Fuse").select();
 			AbstractWait.sleep(TimePeriod.SHORT);
 			new PushButton("Cancel").click();
 			AbstractWait.sleep(TimePeriod.NORMAL);
 			new DefaultShell().close();
 			throw ex;
 		}
 	}
 
 	/**
 	 * New Server Runtime Wizard - Finish button error
 	 * https://issues.jboss.org/browse/FUSETOOLS-1076
 	 */
 	@Test
 	public void issue_1076() {
 
 		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
 		serverRuntime.open();
 		new PushButton("Add...").click();
 		new DefaultShell("New Server Runtime Environment").setFocus();
 		new DefaultTreeItem("JBoss Fuse", "JBoss Fuse 6.1").select();
 		if (new PushButton("Finish").isEnabled()) {
 			new PushButton("Cancel").click();
 			new DefaultShell("Preferences").close();
 			fail("'Finish' button should not be enabled!");
 		}
 	}
 
 	/**
 	 * camel context won't run without tests in eclipse kepler
 	 * https://issues.jboss.org/browse/FUSETOOLS-1077
 	 */
 	@Test
 	public void issue_1077() {
 
 		ProjectFactory.createProject("camel-web", "camel-archetype-web");
 		new CamelProject("camel-web").runApplicationContextWithoutTests("applicationContext.xml");
 		new WaitUntil(new ConsoleHasText("[INFO] Started Jetty Server\nHello Web Application, how are you?"), TimePeriod.LONG);
 	}
 
 	/**
 	 * An endpoint is lost after saving
 	 * https://issues.jboss.org/browse/FUSETOOLS-1085
 	 */
 	@Test
 	public void issue_1085() {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
 		CamelEditor editor = new CamelEditor("camel-context.xml");
 		editor.addCamelComponent(new Log());
 		new DefaultToolItem(new WorkbenchShell(), 0, new WithTooltipTextMatcher(new RegexMatcher("Save All.*"))).click();
 		new WaitUntil(new ShellWithTextIsAvailable("Please confirm..."));
 		new DefaultShell("Please confirm...");
 		new PushButton("No").click();
 		assertTrue(editor.isDirty());
 		new DefaultToolItem(new WorkbenchShell(), 0, new WithTooltipTextMatcher(new RegexMatcher("Save All.*"))).click();
 		new WaitUntil(new ShellWithTextIsAvailable("Please confirm..."));
 		new DefaultShell("Please confirm...");
 		new PushButton("Yes").click();
 		AbstractWait.sleep(TimePeriod.getCustom(5));
 		assertFalse(editor.isDirty());
 	}
 
 	/**
 	 * JMX Navigator - prevent from close Camel Context
 	 * https://issues.jboss.org/browse/FUSETOOLS-1115
 	 */
 	@Test
 	public void issue_1115() {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").runCamelContext("camel-context.xml");
 		new JMXNavigator().getNode("Local Camel Context", "Camel", "camel").select();
 
 		try {
 			new ContextMenu("Close Camel Context");
 		} catch (SWTLayerException ex) {
 			return;
 		} finally {
 			new ConsoleView().terminateConsole();
 		}
 
 		fail("Context menu item 'Close Camel Context' is available!");
 	}
 
 	/**
 	 * context id is removed on save
 	 * https://issues.jboss.org/browse/FUSETOOLS-1123
 	 */
 	@Test
 	public void issue_1123() {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
 		CamelEditor.switchTab("Source");
 		EditorManipulator.copyFileContentToCamelXMLEditor("resources/camel-context-routeContextId.xml");
 		CamelEditor.switchTab("Design");
 		CamelEditor.switchTab("Source");
 		String editorText = new DefaultStyledText().getText();
		assertTrue(editorText.contains("id=\"test\""));
 	}
 
 	/**
 	 * Karaf cannot be started in debug mode
 	 * https://issues.jboss.org/browse/FUSETOOLS-1132
 	 */
 	@Test
 	public void issue_1132() {
 
 		ProjectFactory.createProject("camel-blueprint", "camel-archetype-blueprint");
 		String server = serverRequirement.getConfig().getName();
 		new BreakpointsView().importBreakpoints(ResourceHelper.getResourceAbsolutePath(Activator.PLUGIN_ID, "resources/breakpoint.bkpt"));
 		ServerManipulator.debugServer(server);
 		ServerManipulator.addModule(server, "camel-blueprint");
 		try {	
 			new WaitUntil(new ShellWithTextIsAvailable("Confirm Perspective Switch"), TimePeriod.LONG);
 			new DefaultShell("Confirm Perspective Switch");
 			new CheckBox(0).toggle(true);
 			new PushButton("No").click();
 			AbstractWait.sleep(TimePeriod.NORMAL);
 		} catch (Exception e) {
 			fail();
 		} finally {
 			new WaitWhile(new IsRunning());
 			new BreakpointsView().removeAllBreakpoints();
 			new WorkbenchShell().setFocus();
 			ResumeButton resume = new ResumeButton();
 			if (resume.isEnabled()) {
 				resume.select();
 			}
 			ServerManipulator.removeAllModules(server);
 		}
 	}
 
 	/**
 	 * New Fuse Project - Finish button
 	 * https://issues.jboss.org/browse/FUSETOOLS-1149
 	 */
 	@Test
 	public void issue_1149() {
 
 		new ShellMenu("File", "New", "Fuse Project").select();
 		new DefaultShell("New Fuse Project");
 		if (new PushButton("Finish").isEnabled()) {
 			new DefaultShell().close();
 			fail("'Finish' button should not be enabled!");
 		}
 	}
 
 	/**
 	 * uninstall of bundles from servers broken
 	 * https://issues.jboss.org/browse/FUSETOOLS-1152
 	 */
 	@Test
 	public void issue_1152() {
 
 		ProjectFactory.createProject("camel-spring-dm", "camel-archetype-spring-dm");
 		String server = serverRequirement.getConfig().getName();
 		ServerManipulator.startServer(server);
 		ServerManipulator.addModule(server, "camel-spring-dm");
 		AbstractWait.sleep(TimePeriod.NORMAL);
 		ServerManipulator.removeAllModules(server);
 		new WaitUntil(new FuseLogContainsText("Application context succesfully closed (OsgiBundleXmlApplicationContext(bundle=camel-spring-dm"), TimePeriod.VERY_LONG);
 	}
 
 	/**
 	 * remove use of the customId attribute
 	 * https://issues.jboss.org/browse/FUSETOOLS-1172
 	 */
 	@Test
 	public void issue_1172() throws ParserConfigurationException, SAXException, IOException {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
 		CamelEditor.switchTab("Design");
 		CamelEditor editor = new CamelEditor("camel-context.xml");
 		editor.click(5, 5);
 		editor.setProperty("Id", "1");
 		editor.save();
 		CamelEditor.switchTab("Source");
 
 		// check XML content
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		InputSource is = new InputSource(new StringReader(new DefaultStyledText().getText()));
 		Document doc = builder.parse(is);
 		assertNull(doc.getElementsByTagName("route").item(0).getAttributes().getNamedItem("customId"));
 	}
 
 	/**
 	 * Problem occurred during restart JBoss Fuse
 	 * https://issues.jboss.org/browse/FUSETOOLS-1252
 	 */
 	@Test
 	public void issue_1252() {
 
 		ProjectFactory.createProject("camel-blueprint", "camel-archetype-blueprint");
 		String server = serverRequirement.getConfig().getName();
 		ServerManipulator.addModule(server, "camel-blueprint");
 		ServerManipulator.startServer(server);
 		new ServersView().getServer(server).restartInDebug();
 		try {
 			new WaitUntil(new ShellWithTextIsAvailable("Problem Occurred"));
 			new DefaultShell("Problem Occurred");
 			new PushButton("OK");
 		} catch (Exception e) {
 			// OK no shell "Problem Occurred" was found
 			return;
 		}
 		fail();
 	}
 }
