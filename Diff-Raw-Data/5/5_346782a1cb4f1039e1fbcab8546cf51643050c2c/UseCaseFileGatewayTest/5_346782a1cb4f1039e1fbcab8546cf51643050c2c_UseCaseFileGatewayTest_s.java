 package org.jboss.tools.switchyard.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileWriter;
 
 import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.jboss.tools.switchyard.reddeer.binding.FileBindingPage;
 import org.jboss.tools.switchyard.reddeer.component.Service;
 import org.jboss.tools.switchyard.reddeer.component.SwitchYardComponent;
 import org.jboss.tools.switchyard.reddeer.editor.SwitchYardEditor;
 import org.jboss.tools.switchyard.reddeer.editor.TextEditor;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement.SwitchYard;
 import org.jboss.tools.switchyard.reddeer.server.ServerDeployment;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * File gateway test
  * 
  * @author apodhrad
  * 
  */
 @SwitchYard(server = @Server(type = ServerReqType.ANY, state = ServerReqState.RUNNING))
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
 public class UseCaseFileGatewayTest {
 
 	public static final String PROJECT = "file_project";
 	public static final String PACKAGE = "com.example.switchyard.file_project";
 
 	@InjectRequirement
 	private SwitchYardRequirement switchyardRequirement;
 	
 	@Before @After
 	public void closeSwitchyardFile() {
 		try {
 			new SwitchYardEditor().saveAndClose();
 		} catch (Exception ex) {
 			// it is ok, we just try to close switchyard.xml if it is open
 		}
 	}
 
 	@Test
 	public void fileGatewayTest() throws Exception {
 		switchyardRequirement.project(PROJECT).impl("Bean").binding("File").create();
 
 		// Create new service and interface
 		new SwitchYardEditor().addBeanImplementation().createNewInterface("Info").finish();
 
 		// Edit the interface
 		new SwitchYardComponent("Info").doubleClick();
 		new TextEditor("Info.java").typeAfter("interface", "void printInfo(String body);")
 				.saveAndClose();
 
 		// Edit the bean
 		new SwitchYardComponent("InfoBean").doubleClick();
 		new TextEditor("InfoBean.java").typeAfter("public class", "@Override").newLine()
 				.type("public void printInfo(String body) {").newLine()
 				.type("System.out.println(\"Body: \" + body);}").saveAndClose();
 
 		new SwitchYardEditor().save();
 
 		// Promote info service
 		new Service("Info").promoteService().activate().setServiceName("InfoService").finish();
 
 		// Add File binding
 		new Service("InfoService").addBinding("File");
 		FileBindingPage wizard = new FileBindingPage();
 		File input = new File("target/input");
 		input.mkdirs();
 		File output = new File("target/processed");
 		output.mkdirs();
 		wizard.setName("file-binding");
		wizard.getDirectory().setText(input.getAbsolutePath());
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(true);
		wizard.getMove().setText(output.getAbsolutePath());
 		wizard.finish();
 		new SwitchYardEditor().save();
 
 		// Deploy and test the project
 		new ServerDeployment(switchyardRequirement.getConfig().getName()).deployProject(PROJECT);
 		FileWriter out = new FileWriter(new File(input, "test.txt"));
 		out.write("Hello File Gateway");
 		out.flush();
 		out.close();
 
 		new WaitUntil(new ConsoleHasText("Body: Hello File Gateway"));
 
 		File file = new File(output, "test.txt");
 		assertTrue("File 'test.txt' wasn't processed", file.exists());
 	}
 }
