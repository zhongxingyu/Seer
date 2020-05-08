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
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.wst.common.modulecore.ArtifactEdit;
 import org.eclipse.wst.common.modulecore.ArtifactEditModel;
 
 /**
  * <p>
  * EnterpriseArtifactEdit utilizes the facade function of ArtifactEdit {@see ArtifactEdit}to obtain
 * J2EE specific data from a resource (@see Resource), i.e deployment descriptor resource. The
 * deployment descriptor resource is retrieved from the ArtifactEditModel {@see ArtifactEditModel}
 * using a client defined URI. Defined methods extract data from the resource.
  * </p>
  * 
  * <p>
  * This class is an abstract class, and clients are intended to subclass and own their
  * implementation.
  * </p>
  */
 public abstract class EnterpriseArtifactEdit extends ArtifactEdit {
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 	public EnterpriseArtifactEdit(ArtifactEditModel model) {
 		super(model);
 	}
 
 	/***********************************************************************************************
 	 * <p>
 	 * Retrieves J2EE version information from deployment descriptor resource.
 	 * </p>
 	 * 
 	 * @return an interger representation of a J2EE Spec version
 	 *  
 	 */
 	public abstract int getJ2EEVersion();
 
 	/***********************************************************************************************
 	 * <p>
 	 * Retrieves a deployment descriptor resource from ArtifactEditModel (@see ArtifactEditModel)
 	 * using a defined URI.
 	 * </p>
 	 * 
 	 * @return deployment descriptor resource
 	 *  
 	 */
 	public abstract Resource getDeploymentDescriptorResource();
 
 	/***********************************************************************************************
 	 * <p>
 	 * Obtains the root object from a deployment descriptor resource, the root object contains all
 	 * other resource defined objects. Examples of a deployment descriptor root include: WebAPP
 	 * (@see WebApp), Applicaiton(@see Application), EJBJar((@see EJBJar)
 	 * </p>
 	 * <p>
 	 * Subclasses may extend this method to perform their own deployment descriptor creataion/
 	 * retrieval.
 	 * </p>
 	 * 
 	 * @return a J2EE specific root object
 	 *  
 	 */
 
 	public EObject getDeploymentDescriptorRoot() {
 		Resource res = getDeploymentDescriptorResource();
 		return (EObject) res.getContents().get(0);
 	}
 }
