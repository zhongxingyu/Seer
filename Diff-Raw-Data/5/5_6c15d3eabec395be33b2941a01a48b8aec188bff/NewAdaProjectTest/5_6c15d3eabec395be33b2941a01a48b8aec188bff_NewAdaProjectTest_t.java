 package org.padacore.ui.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.junit.Test;
 import org.padacore.core.AdaProjectNature;
 import org.padacore.ui.wizards.NewAdaProject;
 
 public class NewAdaProjectTest {
 	
 	private NewAdaProject sut;
 		
 	private static void checkProjectIsNotNull(IProject project) {
 		assertNotNull("Project shall be not null", project);
 	}
 
 	private static void checkProjectIsOpen(IProject project) {
 		assertTrue("Project shall be open", project.isOpen());
 	}
 
 	private static void checkProjectLocation(IProject project, String expectedPath) {
 		assertEquals("Project location", expectedPath, project.getLocationURI().getPath());
 	}
 
 	private static void checkProjectContainsAdaNature(IProject project) {
 		try {
 			IProjectDescription desc = project.getDescription();
 			assertEquals("Project shall contain one nature", 1, desc.getNatureIds().length);
 			assertTrue("Project natures shall contain adaProjectNature",
 					desc.hasNature(AdaProjectNature.NATURE_ID));
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void checkGprExists(IProject project) {
 		assertTrue("GPR project file shall exist",
 				new File(TestUtils.getFileAbsolutePath(project, project.getName() + ".gpr"))
 						.exists());
 	}
 
 	@Test
 	public void testCreateProjectWithDefaultLocation() {
		
 		sut = new NewAdaProject("TestProject", null);
 		
 		IProject createdProject = sut.create(true);
 
 		checkProjectIsNotNull(createdProject);
 		checkProjectIsOpen(createdProject);
 		checkProjectContainsAdaNature(createdProject);
 		checkProjectLocation(createdProject,
				TestUtils.getWorkspaceAbsolutePath() + "/"
 						+ createdProject.getName());
 		checkGprExists(createdProject);
 	}
 
 }
