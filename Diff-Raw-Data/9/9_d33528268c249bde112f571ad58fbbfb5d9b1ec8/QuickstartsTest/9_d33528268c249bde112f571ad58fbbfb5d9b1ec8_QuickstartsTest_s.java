 package org.jboss.tools.switchyard.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.List;
 
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.Project;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.eclipse.ui.problems.ProblemsView;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.api.TreeItem;
 import org.jboss.reddeer.swt.condition.JobIsRunning;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement.SwitchYard;
 import org.jboss.tools.switchyard.reddeer.wizard.ImportMavenWizard;
 import org.junit.After;
 import org.junit.runner.RunWith;
 
 /**
  * Abstract test class for importing quickstarts
  * 
  * @author apodhrad
  * 
  */
 @SwitchYard(server = @Server(type = ServerReqType.ANY, state = ServerReqState.PRESENT))
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
 public abstract class QuickstartsTest {
 
 	protected String quickstartPath;
 
 	@InjectRequirement
	private ServerRequirement serverRequirement;
 	
 	public QuickstartsTest(String quickstartPath) {
 		this.quickstartPath = quickstartPath;
 	}
 
 	protected void testQuickstart(String path) {
		String fullPath = serverRequirement.getConfig().getServerBase().getName() + "/" + quickstartPath + "/" + path;
 		assertTrue("Path '" + fullPath + "' doesn exist!", new File(fullPath).exists());
 
 		new ImportMavenWizard().importProject(fullPath);
 
 		checkErrors(path);
 	}
 
 	protected void checkErrors(String path) {
 		AbstractWait.sleep(TimePeriod.NORMAL);
 		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
 
 		ProblemsView problemsView = new ProblemsView();
 		problemsView.open();
 		List<TreeItem> errors = problemsView.getAllErrors();
 		assertTrue("After importing the quickstart '" + path
 				+ "' there are the following errors:\n" + toString(errors), errors.isEmpty());
 	}
 
 	@After
 	public void deleteAllProjects() {
 		new SWTWorkbenchBot().closeAllShells();
 		ProjectExplorer projectExplorer = new ProjectExplorer();
 		for (Project project : projectExplorer.getProjects()) {
 			project.delete(false);
 		}
 	}
 
 	protected String toString(List<TreeItem> items) {
 		StringBuffer result = new StringBuffer();
 		for (TreeItem item : items) {
 			result.append(item.getText());
 			result.append("\n");
 		}
 		return result.toString();
 	}
 
 }
