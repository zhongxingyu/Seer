 package org.padacore.test_plugin;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.junit.Test;
 import org.padacore.AdaProjectNature;
 import org.padacore.wizards.NewAdaProject;
 
 public class NewAdaProjectTest {
 
 	@Test
 	public void testCreateProjectWithDefaultLocation() {
 
 		NewAdaProject sut = new NewAdaProject("TestProject", true, null);
 
 		IProject createdProject = sut.Create();
 
 		assertNotNull("Project shall be not null", createdProject);
 		assertTrue("Project shall be open", createdProject.isOpen());
 
 		try {
 			IProjectDescription desc = createdProject.getDescription();
 
 			assertEquals("Project shall contain one nature", 1, desc.getNatureIds().length);
 
 			assertTrue("Project natures shall contain adaProjectNature",
 					desc.hasNature(AdaProjectNature.NATURE_ID));
 
 			assertEquals("Project location shall be in workspace", ResourcesPlugin.getWorkspace()
 					.getRoot().getLocationURI().getPath()
					+ "/" + "TestProject", createdProject.getLocationURI().getPath());
 
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
