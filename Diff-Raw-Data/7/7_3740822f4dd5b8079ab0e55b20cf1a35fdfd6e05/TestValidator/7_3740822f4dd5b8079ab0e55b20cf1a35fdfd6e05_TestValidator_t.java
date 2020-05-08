 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.kb.test.validation;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
import org.jboss.tools.jst.web.kb.internal.KbBuilder;
 import org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper;
 import org.jboss.tools.jst.web.kb.internal.validation.SimpleValidatingProjectTree;
 import org.jboss.tools.jst.web.kb.internal.validation.ValidatingProjectSet;
 import org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager;
 import org.jboss.tools.jst.web.kb.validation.IProjectValidationContext;
 import org.jboss.tools.jst.web.kb.validation.IValidatingProjectSet;
 import org.jboss.tools.jst.web.kb.validation.IValidatingProjectTree;
 import org.jboss.tools.jst.web.kb.validation.IValidator;
 
 /**
  * @author Alexey Kazakov
  */
 public class TestValidator implements IValidator {
 
 	public static boolean validated = false;
 
 	protected IStatus OK_STATUS = new Status(IStatus.OK,
 			"org.eclipse.wst.validation", 0, "OK", null); //$NON-NLS-1$ //$NON-NLS-2$
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#getId()
 	 */
 	public String getId() {
 		return "org.jboss.tools.jst.web.kb.test.TestValidator";
 	}
 
	public String getBuilderId() {
		//for the sake of test
		return KbBuilder.BUILDER_ID;
	}

 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#getValidatingProjects(org.eclipse.core.resources.IProject)
 	 */
 	public IValidatingProjectTree getValidatingProjects(IProject project) {
 		Set<IProject> projects = new HashSet<IProject>();
 		projects.add(project);
 		IKbProject kbProject = KbProjectFactory.getKbProject(project, false);
 		if(kbProject!=null) {
 			IProjectValidationContext rootContext = kbProject.getValidationContext();
 			IValidatingProjectSet projectSet = new ValidatingProjectSet(project, projects, rootContext);
 			return new SimpleValidatingProjectTree(projectSet);
 		}
 		return new SimpleValidatingProjectTree(project);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#shouldValidate(org.eclipse.core.resources.IProject)
 	 */
 	public boolean shouldValidate(IProject project) {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#isEnabled(org.eclipse.core.resources.IProject)
 	 */
 	public boolean isEnabled(IProject project) {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#validate(java.util.Set, org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public IStatus validate(Set<IFile> changedFiles, IProject project,
 			ContextValidationHelper validationHelper,
 			IProjectValidationContext validationContext,
 			ValidatorManager manager, IReporter reporter)
 			throws ValidationException {
 		return OK_STATUS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#validateAll(org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public IStatus validateAll(IProject project,
 			ContextValidationHelper validationHelper,
 			IProjectValidationContext validationContext,
 			ValidatorManager manager, IReporter reporter)
 			throws ValidationException {
 		validated = true;
 		return OK_STATUS;
 	}
 }
