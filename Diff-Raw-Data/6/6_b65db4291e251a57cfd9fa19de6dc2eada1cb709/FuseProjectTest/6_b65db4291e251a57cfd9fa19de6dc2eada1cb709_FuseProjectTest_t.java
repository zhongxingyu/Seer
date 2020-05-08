 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.jboss.reddeer.common.logging.Logger;
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.eclipse.ui.problems.ProblemsView;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.api.TreeItem;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.swt.impl.tree.DefaultTree;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * 
  * @author apodhrad
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
public class FuseProjectTest extends DefaultTest {
 
 	protected Logger log = Logger.getLogger(FuseProjectTest.class);
 
 	@Test
 	public void camelCxfCodeTest() {
 
 		ProjectFactory.createProject("test", "camel-cxf-code-first-archetype");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelCxfContractTest() {
 
 		ProjectFactory.createProject("test", "camel-cxf-contract-first-archetype");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelDroolsTest() {
 
 		ProjectFactory.createProject("test", "camel-drools-archetype");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelWebServiceTest() {
 
 		ProjectFactory.createProject("test", "camel-webservice-archetype");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelActiveMQTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-activemq");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelAPIComponentTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-api-component");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelBlueprintTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-blueprint");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelComponentTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-component");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelCxfCodeFirstBlueprint() {
 
 		ProjectFactory.createProject("test", "camel-archetype-cxf-code-first-blueprint");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelCxfContractFirstBlueprint() {
 
 		ProjectFactory.createProject("test", "camel-archetype-cxf-contract-first-blueprint");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelDataFormatTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-dataformat");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelJavaTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-java");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelSCRTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-scr");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelSpringTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-spring");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelSpringDMTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-spring-dm");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelWARTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-war");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelWebTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-web");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelWebConsoleTest() {
 
 		ProjectFactory.createProject("test", "camel-archetype-webconsole");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelCxfJaxRSTest() {
 
 		ProjectFactory.createProject("test", "cxf-jaxrs-service");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	@Test
 	public void camelCxfJaxWSTest() {
 
 		ProjectFactory.createProject("test", "cxf-jaxws-javafirst");
 		assertTrue(isPresent());
 		assertFalse(hasErrors());
 	}
 
 	private boolean hasErrors() {
 
 		new ProblemsView().open();
 		for (TreeItem item : new DefaultTree().getItems()) {
 			if (item.getText().toLowerCase().contains("error")) return true;
 		}
 		return false;
 	}
 
 	private boolean isPresent() {
 
 		return new ProjectExplorer().containsProject("test");
 	}
 
 	@After
 	public void clean() throws Exception {
 
 		new WorkbenchShell();
 		new ProjectExplorer().deleteAllProjects();
 		new WorkbenchShell();
 	}
 }
