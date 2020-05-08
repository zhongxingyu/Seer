 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.internal.modulecore.util;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.ApplicationResource;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveConstants;
 import org.eclipse.wst.common.modulecore.ArtifactEditModel;
 
 /**
  * <p>
  * EARArtifactEdit utilizes the facade function of ArtifactEdit {@see ArtifactEdit}to obtain
 * Application specific data from an ApplicationXMIResource (@see ApplicationXMIResource ). The
 * ApplicationXMIResource is retrieved from the ArtifactEditModel {@see ArtifactEditModel}using a
  * cached constant (@see ArchiveConstants.APPLICATION_DD_URI). Defined methods extract data from the
  * resource.
  * </p>
  */
 
 public class EARArtifactEdit extends EnterpriseArtifactEdit {
 
 
 	public static String TYPE_ID = "EAR_TYPE";
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 	public EARArtifactEdit(ArtifactEditModel model) {
 		super(model);
 	}
 
 	/***********************************************************************************************
 	 * <p>
 	 * Retrieves J2EE version information from ApplicationXMIResource.
 	 * </p>
 	 * 
 	 * @return an interger representation of a J2EE Spec version
 	 *  
 	 */
 	public int getJ2EEVersion() {
 		return getApplicationXmiResource().getJ2EEVersionID();
 	}
 
 	/***********************************************************************************************
 	 * 
 	 * @return ApplicationResource from (@link getDeploymentDescriptorResource())
 	 *  
 	 */
 
 	public ApplicationResource getApplicationXmiResource() {
 		return (ApplicationResource) getDeploymentDescriptorResource();
 	}
 
 	/***********************************************************************************************
 	 * <p>
 	 * Obtains the Application (@see Application) root object from the ApplicationResource, the root
 	 * object contains all other resource defined objects.
 	 * </p>
 	 * 
 	 * @return Application
 	 *  
 	 */
 
 	public Application getApplication() {
 		return (Application) getDeploymentDescriptorRoot();
 	}
 
 	/***********************************************************************************************
 	 * <p>
 	 * Retrieves the resource from the ArtifactEditModel using defined URI.
 	 * </p>
 	 * 
 	 * @return Resource
 	 *  
 	 */
 
 	public Resource getDeploymentDescriptorResource() {
 		return getArtifactEditModel().getResource(URI.createURI(ArchiveConstants.APPLICATION_DD_URI));
 	}
 }
