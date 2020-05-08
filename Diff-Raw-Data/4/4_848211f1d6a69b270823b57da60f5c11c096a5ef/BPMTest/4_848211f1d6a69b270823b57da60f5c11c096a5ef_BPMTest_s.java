 package org.jboss.tools.switchyard.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.ProjectItem;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ShellMenu;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.reddeer.swt.test.RedDeerTest;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.workbench.view.impl.WorkbenchView;
import org.jboss.tools.bpmn2.reddeer.editor.BPMN2Editor;
import org.jboss.tools.bpmn2.reddeer.editor.BPMN2PropertiesView;
 import org.jboss.tools.bpmn2.reddeer.editor.ConstructType;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.FromDataOutput;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.FromVariable;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.InputParameterMapping;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.OutputParameterMapping;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ToDataInput;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ToVariable;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.endevents.TerminateEndEvent;
 import org.jboss.tools.bpmn2.reddeer.editor.jbpm.startevents.StartEvent;
 import org.jboss.tools.bpmn2.reddeer.editor.switchyard.activities.SwitchYardServiceTask;
 import org.jboss.tools.switchyard.reddeer.component.BPM;
 import org.jboss.tools.switchyard.reddeer.component.Bean;
 import org.jboss.tools.switchyard.reddeer.component.Component;
 import org.jboss.tools.switchyard.reddeer.component.Service;
 import org.jboss.tools.switchyard.reddeer.editor.SwitchYardEditor;
 import org.jboss.tools.switchyard.reddeer.editor.TextEditor;
 import org.jboss.tools.switchyard.reddeer.view.JUnitView;
 import org.jboss.tools.switchyard.reddeer.widget.ProjectItemExt;
 import org.jboss.tools.switchyard.reddeer.wizard.ReferenceWizard;
 import org.jboss.tools.switchyard.reddeer.wizard.SwitchYardProjectWizard;
 import org.jboss.tools.switchyard.ui.bot.test.suite.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.tools.switchyard.ui.bot.test.suite.PerspectiveRequirement.Perspective;
 import org.jboss.tools.switchyard.ui.bot.test.suite.SwitchyardSuite;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Create simple BPMN process with a switchyard service task, run JUnit test.
  * @author lfabriko
  *
  */
 @CleanWorkspace
 @Perspective(name = "Java EE")
 @RunWith(SwitchyardSuite.class)
 public class BPMTest extends RedDeerTest {
 
 	private static final String PROJECT = "switchyard-bpm-processgreet";
 	private static final String PACKAGE = "org.switchyard.quickstarts.bpm.service";
 	private static final String GROUP_ID = "org.switchyard.quickstarts.bpm.service";
 	private static final String PROCESS_GREET = "ProcessGreet";
 	private static final String BPMN_FILE_NAME = "ProcessGreet.bpmn";
 	private static final String PACKAGE_MAIN_JAVA = "src/main/java";
 	private static final String PACKAGE_MAIN_RESOURCES = "src/main/resources";
 	private static final Integer[] BPM_COORDS = { 50, 200 };
 	private static final Integer[] BEAN_COORDS = { 250, 200 };
 	private static final String EVAL_GREET = "EvalGreet";
 	private static final String PROCESS_GREET_DECL = "public boolean checkGreetIsPolite(String greet);";
 	private static final String EVAL_GREET_DECL = "public boolean checkGreet(String greet);";
 	private static final String EVAL_GREET_BEAN = EVAL_GREET + "Bean";
 	
 	@Before @After
 	public void closeSwitchyardFile() {
 		try {
 			new SwitchYardEditor().saveAndClose();
 		} catch (Exception ex) {
 			// it is ok, we just try to close switchyard.xml if it is open
 		}
 	}
 	
 	@Test
 	public void bpmCreationTest() {
 		new WorkbenchShell().maximize();
 		// Create new Switchyard project, Add support for Bean, BPM
 		new SwitchYardProjectWizard(PROJECT).impl("Bean", "BPM (jBPM)")
 				.groupId(GROUP_ID).packageName(PACKAGE).create();
 		openFile(PROJECT, PACKAGE_MAIN_RESOURCES, "META-INF", "switchyard.xml");
 		new BPM().setService(PROCESS_GREET).setBpmnFileName(BPMN_FILE_NAME)
 				.create(BPM_COORDS);
 		AbstractWait.sleep(1000);
 		new Bean().setService(EVAL_GREET).create(BEAN_COORDS);
 		AbstractWait.sleep(1000);
 		// reference to bean
 		new Component(PROCESS_GREET).contextButton("Reference").click();
 		new ReferenceWizard().selectJavaInterface(EVAL_GREET).finish();
 		AbstractWait.sleep(2 * 1000);
 
 		// declare ProcessGreet interface
 		new Service(PROCESS_GREET, 1).openTextEditor();
 		new TextEditor(PROCESS_GREET + ".java").typeAfter("interface",
 				PROCESS_GREET_DECL);
 		// declare EvalGreet interface
 		openFile(PROJECT, PACKAGE_MAIN_JAVA, PACKAGE, EVAL_GREET + ".java");
 		new TextEditor(EVAL_GREET + ".java").typeAfter("interface",
 				EVAL_GREET_DECL);
 		// implement EvalGreetBean
 		openFile(PROJECT, PACKAGE_MAIN_JAVA, PACKAGE, EVAL_GREET_BEAN + ".java");
 		new TextEditor(EVAL_GREET_BEAN + ".java")
 				.typeAfter("implements", "@Override")
 				.newLine()
 				.type("public boolean checkGreet(String greet){")
 				.newLine()
 				.type("return (greet.equals(\"Good evening\")) ? true : false;")
 				.newLine().type("}");
 
 		// BPM Process and its properties
 		openFile(PROJECT, PACKAGE_MAIN_RESOURCES, BPMN_FILE_NAME);
 		BPMN2Editor editor = new BPMN2Editor();
 		editor.click(1, 1);
 		new BPMN2PropertiesView().selectTab("Process");
 		new LabeledText("Id").setText(PROCESS_GREET);
 		editor.setFocus();
 
 		new TerminateEndEvent("EndProcess").delete();
 		new StartEvent("StartProcess").append("EvalGreet",
 				ConstructType.SWITCHYARD_SERVICE_TASK);
 		SwitchYardServiceTask task = new SwitchYardServiceTask("EvalGreet");
 		task.setTaskAttribute("Operation Name", "checkGreet");
 		task.setTaskAttribute("Service Name", EVAL_GREET);
 		task.addParameterMapping(new InputParameterMapping(new FromVariable(
 				PROCESS_GREET + "/Parameter"), new ToDataInput("Parameter")));
 		task.addParameterMapping(new OutputParameterMapping(new FromDataOutput(
 				"Result"), new ToVariable(PROCESS_GREET + "/Result")));
 		task.append("EndProcess", ConstructType.TERMINATE_END_EVENT);
 
 		openFile(PROJECT, PACKAGE_MAIN_RESOURCES, "META-INF", "switchyard.xml");
 
 		// Junit
 		new Service(PROCESS_GREET, 1).newServiceTestClass();
 		new TextEditor(PROCESS_GREET + "Test.java")
 				.deleteLineWith("null")
 				.deleteLineWith("assertTrue")
 				.typeBefore("boolean", "String message = \"Good evening\";")
 				.newLine()
 				.typeAfter("getContent", "Assert.assertTrue(result);")
 				.newLine()
 				.type("Assert.assertFalse(service.operation(\"checkGreetIsPolite\").sendInOut(\"hi\").getContent(Boolean.class));");
 		new ShellMenu("File", "Save All").select();
 		AbstractWait.sleep(1000);
 		new PushButton("No").click();// BPMN nature
 
 		ProjectItem item = new ProjectExplorer().getProject(PROJECT)
 				.getProjectItem("src/test/java", PACKAGE,
 						PROCESS_GREET + "Test.java");
 		new ProjectItemExt(item).runAsJUnitTest();
 		assertEquals("1/1", new JUnitView().getRunStatus());
 		assertEquals(0, new JUnitView().getNumberOfErrors());
 		assertEquals(0, new JUnitView().getNumberOfFailures());
 	}
 
 	private void openFile(String... file) {
 		// focus on project explorer
 		new WorkbenchView("General", "Project Explorer").open();
 		new DefaultTreeItem(0, file).doubleClick();
 	}
 }
