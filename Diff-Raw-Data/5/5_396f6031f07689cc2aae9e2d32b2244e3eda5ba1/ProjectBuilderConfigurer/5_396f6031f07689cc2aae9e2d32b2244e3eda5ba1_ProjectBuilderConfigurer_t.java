 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.core.configuration;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.runtime.CoreException;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 
 /**
  * Class to configure (add/remove) the JAX-RS MediaTypeCapabilitiesBuilder on a project.
  * 
  * @author xcoulon
  *
  */
 public class ProjectBuilderConfigurer implements IProjectNature {
 
 	/** the selected project. */
 	private IProject project = null;
 
 	@Override
 	public final void configure() throws CoreException {
 		if (project == null) {
 			return;
 		}
 		// project nature installation triggers the project builder installation, by configuration/association in the plugin.xml file.
 		if (ProjectBuilderUtils.installProjectBuilder(project, ProjectBuilderUtils.JAXRS_BUILDER_ID)) {
			Logger.info("JAX-RS Builder is now installed.");
 		} else {
			Logger.info("JAX-RS Builder was already installed.");
 		}
 	}
 
 	@Override
 	public final void deconfigure() throws CoreException {
 		if (project == null) {
 			return;
 		}
 		if (ProjectBuilderUtils.uninstallProjectBuilder(project, ProjectBuilderUtils.JAXRS_BUILDER_ID)) {
 			Logger.info("JAX-RS MediaTypeCapabilitiesBuilder is now uninstalled.");
 		} else {
 			Logger.info("JAX-RS MediaTypeCapabilitiesBuilder was not installed.");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final IProject getProject() {
 		return project;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final void setProject(final IProject p) {
 		this.project = p;
 	}
 
 }
