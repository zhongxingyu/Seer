 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.services;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.util.ImportManager;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 
 /**
  * This class is used to implement some behaviors regarding GenModels that cannot be properly implemented with
  * acceleo alone.
  * 
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class ImportService {
 
 	/**
 	 * Initializing EMF importManager.
 	 * 
 	 * @since 1.1
 	 */
 	public void initializeImportManager(EObject caller, String packageName, String className) {
 		ImportManager importManager = new ImportManager(packageName, className);
 		getGenModel(caller).setImportManager(importManager);
 	}
 	
 	/**
 	 * Initializing EMF importManager on a genmodel.
	 * To fix call of import manager in a genmodel which is not the genmodel used by the default import manager
 	 */
	public void initializeImportManagerForGenmodel(EObject caller, GenModel model, String packageName, String className) {
 		ImportManager importManager = new ImportManager(packageName, className);
 		model.setImportManager(importManager);
 	}
 
 	/**
 	 * Adds an import.
 	 */
 	public void addImport(EObject caller, String qualifiedName) {
 		GenModel genModel = getGenModel(caller);
 		genModel.addImport(qualifiedName);
 	}
 
 	/**
 	 * Generate sorted imports.
 	 * 
 	 * @since 1.1
 	 */
 	public String genSortedImports(EObject caller) {
 		return getGenModel(caller).getImportManager().computeSortedImports();
 	}
 
 	/**
 	 * Retrieves the genModel from any EObject from the components model.
 	 * 
 	 * @param eo
 	 *            any EObject from the components model.
 	 * @return the EMF genModel (intended to hold the ImportManager)
 	 */
 	public GenModel getGenModel(EObject eo) {
 		if (eo.eResource() != null && eo.eResource().getResourceSet() != null) {
 			for (Resource resource : eo.eResource().getResourceSet().getResources()) {
 				if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof GenModel) {
 					return (GenModel)resource.getContents().get(0);
 				}
 			}
 		}
 		throw new RuntimeException("Unable to retrieve gen model from " + eo);
 	}
 	
 	/**
 	 * @deprecated. EEF is using EMF importmanager directy.
 	 * @param cur
 	 *            the current Eobject
 	 * @return the importmarker string
 	 */
 	public String getImports(EObject cur) {
 		return "";
 	}
 }
