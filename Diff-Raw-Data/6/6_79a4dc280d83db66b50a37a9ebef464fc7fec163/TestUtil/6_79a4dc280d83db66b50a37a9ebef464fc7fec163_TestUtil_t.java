 package org.jboss.tools.jst.jsp.test;
 
 import org.eclipse.core.runtime.CoreException;
 import org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager;
 import org.jboss.tools.test.util.JobUtils;
 
 public class TestUtil {
 
 	/**
 	 * Wait for validation to be completed.
 	 * Usage:
 	 *       ValidatorManager.setStatus("Any status but ValidatorManager.SLEEPING");
 	 *       ... // do some work here which will make Eclipse build the project
 	 *       TestUtil.waitForValidation(project);
 	 * @throws CoreException
 	 */
	public static void waitForValidation() throws CoreException{
 		JobUtils.waitForIdle();
 		for (int i = 0; i < 50; i++) {
 			if(ValidatorManager.getStatus().equals(ValidatorManager.SLEEPING)) {
 				break;
 			}
 			JobUtils.delay(100);
 			JobUtils.waitForIdle();
 		}
 	}
 }
