 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.model.impl;
 
 import java.util.HashMap;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 
 /**
  * A copier class for copying projects.
  * 
  * @author emueller
  */
 public class IdEObjectCollectionCopier extends Copier {
 
 	private static final long serialVersionUID = 1L;
 	private Project orgProject;
 	private ProjectImpl copiedProject;
 	private HashMap<EObject, String> eObjectToIdMap;
 	private HashMap<String, EObject> idToEObjectMap;
 
 	/**
 	 * Default constructor.
 	 */
 	public IdEObjectCollectionCopier() {
 		eObjectToIdMap = new HashMap<EObject, String>();
 		idToEObjectMap = new HashMap<String, EObject>();
 	}
 
 	@Override
 	public EObject copy(EObject eObject) {
 		if (eObject instanceof Project) {
 			orgProject = (Project) eObject;
 		}
 		EObject copiedEObject = super.copy(eObject);
 
 		if (copiedEObject instanceof Project) {
 			// TODO: PlainEObjectMode, make sure that project is really returned as the last element
 			copiedProject = (ProjectImpl) copiedEObject;
 			copiedProject.initCaches(eObjectToIdMap, idToEObjectMap);
 			return copiedProject;
 		}
 
		if (eObject.eContainingFeature() != null && eObject.eContainingFeature().isTransient()) {
			return copiedEObject;
		}

 		ModelElementId eObjectId = orgProject.getModelElementId(eObject);
 		eObjectToIdMap.put(copiedEObject, eObjectId.getId());
 		idToEObjectMap.put(eObjectId.getId(), copiedEObject);
 
 		return copiedEObject;
 	}
 }
