 package org.jboss.tools.bpel.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 
 import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.PackageExplorer;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.api.Table;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.table.DefaultTable;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.tools.bpel.reddeer.perspective.BPELPerspective;
 import org.jboss.tools.bpel.reddeer.wizard.NewProjectWizard;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * 
  * @author apodhrad
  * 
  */
 @CleanWorkspace
 @OpenPerspective(BPELPerspective.class)
 @RunWith(RedDeerSuite.class)
 @Server(type = ServerReqType.ANY, state = ServerReqState.PRESENT)
 public class AssociateRuntimeTest {
 
 	@InjectRequirement
 	private ServerRequirement serverRequirement;
 	
 	@Test
 	public void testModeling() throws Exception {
 		new NewProjectWizard("runtimeTest").execute();
 
 		new PackageExplorer().getProject("runtimeTest").select();
 
 		new ContextMenu("Properties").select();
 
 		new DefaultTreeItem("Targeted Runtimes").select();
 
 		String serverName = serverRequirement.getConfig().getName();
 		assertTrue(containsItem(new DefaultTable(), serverName));
 
 		new PushButton("OK").click();
 	}
 
 	private static boolean containsItem(Table table, String item) {
 		int count = table.rowCount();
 		for (int i = 0; i < count; i++) {
 			if (table.getItem(i).getText(0).equals(item)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
