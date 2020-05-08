  /*******************************************************************************
   * Copyright (c) 2007 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributor:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.kb.internal.validation;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 import org.eclipse.wst.validation.internal.provisional.core.IValidatorJob;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 import org.jboss.tools.jst.web.kb.validation.IValidator;
 
 /**
  * This Manager invokes all dependent validators that should be invoked in one job.
  * We need this one because wst validation framework does not let us invoke
  * dependent validators in the same job.
  * @author Alexey Kazakov
  */
 public class ValidatorManager implements IValidatorJob {
 
 	private static Set<IProject> validatingProjects = new HashSet<IProject>(); 
 
 	public ValidatorManager() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.validation.internal.provisional.core.IValidatorJob#getSchedulingRule(org.eclipse.wst.validation.internal.provisional.core.IValidationContext)
 	 */
 	public ISchedulingRule getSchedulingRule(IValidationContext helper) {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.validation.internal.provisional.core.IValidatorJob#validateInJob(org.eclipse.wst.validation.internal.provisional.core.IValidationContext, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public IStatus validateInJob(IValidationContext helper, IReporter reporter)	throws ValidationException {
 		ContextValidationHelper validationHelper = (ContextValidationHelper)helper;
 		IProject project = validationHelper.getProject();
 		if(project==null) {
 			return OK_STATUS;
 		}
 		IProject rootProject = validationHelper.getValidationContext().getRootProject();
 		IStatus status = OK_STATUS;
 		synchronized (validatingProjects) {
 			if(validatingProjects.contains(rootProject)) {
 				return OK_STATUS;
 			}
 			validatingProjects.add(rootProject);
 		}
 		synchronized (validatingProjects) {
 			org.jboss.tools.jst.web.kb.validation.IValidationContext validationContext = null;
 			try {
 				validationContext = new ValidationContext(project);
 				validationHelper.setValidationContext(validationContext);
 
 				List<IValidator> validators = validationHelper.getValidationContext().getValidators();
 				Set<IFile> changedFiles = validationHelper.getChangedFiles();
 				if(!changedFiles.isEmpty()) {
 					status = validate(validators, changedFiles, rootProject, validationHelper, reporter);
 				} else if(!validationContext.getRegisteredFiles().isEmpty()) {
 					validationContext.clearAllResourceLinks();
 					status = validateAll(validators, rootProject, validationHelper, reporter);
 				}
 			} finally {
 				if(validationContext!=null) {
 					validationContext.clearRegisteredFiles();
 				}
 				validatingProjects.remove(rootProject);
 			}
 		}
 		return status;
 	}
 
 	private IStatus validate(List<IValidator> validators, Set<IFile> changedFiles, IProject rootProject, ContextValidationHelper validationHelper, IReporter reporter) throws ValidationException {
 		removeMarkers(changedFiles);
 		for (IValidator validator : validators) {
 			validator.validate(changedFiles, rootProject, validationHelper, this, reporter);
 		}
 		return OK_STATUS;
 	}
 
 	private void removeMarkers(Set<IFile> files) {
 		try {
 			for (IFile file : files) {
				if(file.isAccessible()) {
					file.deleteMarkers(IValidator.KB_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_ZERO);
				}
 			}
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 	}
 
 	private IStatus validateAll(List<IValidator> validators, IProject rootProject, ContextValidationHelper validationHelper, IReporter reporter) throws ValidationException {
 		removeMarkers(validationHelper.getProjectSetRegisteredFiles());
 		for (IValidator validator : validators) {
 			validator.validateAll(rootProject, validationHelper, this, reporter);
 		}
 		return OK_STATUS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.validation.internal.provisional.core.IValidator#cleanup(org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public void cleanup(IReporter reporter) {
 		reporter = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.wst.validation.internal.provisional.core.IValidator#validate(org.eclipse.wst.validation.internal.provisional.core.IValidationContext, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public void validate(IValidationContext helper, IReporter reporter)	throws ValidationException {
 		validateInJob(helper, reporter);
 	}
 }
