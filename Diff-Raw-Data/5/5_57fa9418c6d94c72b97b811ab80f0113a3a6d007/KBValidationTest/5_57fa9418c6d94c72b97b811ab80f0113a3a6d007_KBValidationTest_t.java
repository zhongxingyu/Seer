 package org.jboss.tools.jst.web.kb.test.validation;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.jboss.tools.common.base.test.validation.TestUtil;
 import org.jboss.tools.common.validation.ContextValidationHelper;
 import org.jboss.tools.common.validation.IValidatingProjectTree;
 import org.jboss.tools.common.validation.IValidationContextManager;
 import org.jboss.tools.common.validation.IValidator;
 import org.jboss.tools.common.validation.ValidationResourceRegister;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.ResourcesUtils;
 import org.osgi.framework.Bundle;
 
 /**
  * @author Alexey Kazakov
  */
 public class KBValidationTest extends TestCase {
 
 	protected static String PLUGIN_ID = "org.jboss.tools.jst.web.kb.test";
 	protected static String PROJECT_NAME = "KBValidationTest";
 	protected static String PROJECT_PATH = "/projects/KBValidationTest";
 
 	private IProject importProject() throws IOException, CoreException, InvocationTargetException, InterruptedException {
 		Bundle b = Platform.getBundle(PLUGIN_ID);
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		if(project==null || !project.exists()) {
 			project = ResourcesUtils.importProject(b, PROJECT_PATH);
 		}
 		TestUtil._waitForValidation(project);
 		return project;
 	}
 
 	private void deleteProject() throws CoreException {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
 		if(project!=null && project.exists()) {
 			project.delete(true, new NullProgressMonitor());
 		}
 		JobUtils.waitForIdle();
 	}
 
 	public void testValidationContextCleanedUpIntegration() throws IOException, CoreException, InvocationTargetException, InterruptedException {
 		try {
 			TestValidator.validated = false;
 			importProject();
 			assertTrue(TestValidator.validated);
 			deleteProject();
 			TestValidator.validated = false;
 			importProject();
 			assertTrue(TestValidator.validated);
 		} finally {
 			deleteProject();
 		}
 	}
 
 	public void testValidationContextCleanedUpUnit() {
 		ContextValidationHelper helper = new ContextValidationHelper();
 		helper.setValidationContextManager(new IValidationContextManager() {
 			public IValidatingProjectTree getValidatingProjectTree(
 					IValidator validator) {
 				return null;
 			}
 			public void addProject(IProject project) {
 			}
 			public void clearRegisteredFiles() {
 			}
			public void clearAllResourceLinks(Set<IProject> projects) {
 			}
 			public Set<IFile> getRemovedFiles() {
 				return null;
 			}
 			public void addRemovedFile(IFile file) {
 			}
 			public Set<IFile> getRegisteredFiles() {
 				return null;
 			}
 			public void registerFile(IFile file) {
 			}
 			public List<IValidator> getValidators() {
 				return null;
 			}
 			public Set<IProject> getRootProjects() {
 				return null;
 			}
 			public void addValidatedProject(IValidator validator,
 					IProject project) {
 			}
 			public boolean projectHasBeenValidated(IValidator validator,
 					IProject project) {
 				return false;
 			}
 			public void clearValidatedProjectsList() {
 			}
 			public void setValidationResourceRegister(
 					ValidationResourceRegister validationResourceRegister) {
 			}
 			public void init(IProject project) {
 			}
 			public boolean isObsolete() {
 				return false;
 			}
 		});
 		helper.cleanup();
 		assertNull(helper.getValidationContextManager(false));
 	}
 }
