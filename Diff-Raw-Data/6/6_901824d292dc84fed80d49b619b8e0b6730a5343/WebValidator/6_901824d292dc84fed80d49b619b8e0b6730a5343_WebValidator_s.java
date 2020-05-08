  /*******************************************************************************
   * Copyright (c) 2011 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.kb.internal.validation;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.validation.ContextValidationHelper;
 import org.jboss.tools.common.validation.IProjectValidationContext;
 import org.jboss.tools.jst.web.WebUtils;
 
 /**
  * @author Alexey Kazakov
  */
 abstract public class WebValidator extends KBValidator {
 
 	private static final String JAVA_EXT = "java"; //$NON-NLS-1$
 
 	protected IContainer[] webRootFolders;
 	protected IProject currentProject;
 	protected IResource[] currentSources;
 
 	private boolean enabled = true;
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#init(org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.eclipse.wst.validation.internal.provisional.core.IValidator, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	@Override
 	public void init(IProject project, ContextValidationHelper validationHelper, IProjectValidationContext context, org.eclipse.wst.validation.internal.provisional.core.IValidator manager, IReporter reporter) {
 		super.init(project, validationHelper, context, manager, reporter);
 		webRootFolders = null;
 		currentProject = null;
 	}
 
 	abstract protected boolean shouldValidateJavaSources();
 
 	protected boolean shouldFileBeValidated(IFile file) {
 		if(!file.isAccessible()) {
 			return false;
 		}
 		IProject project = file.getProject();
 		if(!file.isSynchronized(IResource.DEPTH_ZERO)) {
 			// The resource is out of sync with the file system
 			// Just ignore this resource.
 			return false;
 		}
 		if(!project.equals(currentProject)) {
 			currentProject = project;
 			enabled = isEnabled(project);	
 			if(!enabled) {
 				return false;
 			}
 			if(webRootFolders!=null && webRootFolders.length>0 && !project.equals(webRootFolders[0].getProject())) {
 				webRootFolders = null;
 			}
 			if(webRootFolders==null) {
 				webRootFolders = WebUtils.getWebRootFolders(project);
 			}
 			if(shouldValidateJavaSources()) {
 				currentSources = EclipseResourceUtil.getJavaSourceRoots(project);
 			}
 		}
 		if(!enabled) {
 			return false;
 		}
 		// Validate all files from java source folders, if we should.
 		if(shouldValidateJavaSources()) {
 			for (int i = 0; currentSources!=null && i < currentSources.length; i++) {
 				if(currentSources[i].getLocation().isPrefixOf(file.getLocation())) {
 					return true;
 				}
 			}
 			// If *.java is out of Java Source path then ignore it.
 			if(JAVA_EXT.equalsIgnoreCase(file.getFileExtension())) {
 				return false;
 			}
 		}
 		// Otherwise validate only files from Web-Content (in case of WTP project)
 		if(webRootFolders!=null) {
 			for (IContainer webRootFolder : webRootFolders) {
 				if(webRootFolder.getLocation().isPrefixOf(file.getLocation())) {
 					return true;
 				}
 			}
 			if(webRootFolders.length>1) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
