 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.bridge.genmodel;
 
 import java.util.Iterator;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 
 /**
  * @author artem
 *
  */
 public class GenModelMatcher {
 	private final GenModel myEMFGenModel;
 
 	public GenModelMatcher(GenModel genModel) {
 		assert genModel != null;
 		myEMFGenModel = genModel;
 	}
 
 	public GenModelMatcher(EPackage domainModel) {
 		myEMFGenModel = findGenModel(domainModel);
 		if (myEMFGenModel == null) {
 			throw new IllegalStateException();
 		}
 	}
 
 	public GenModel getGenModel() {
 		return myEMFGenModel;
 	}
 
 	public GenPackage findGenPackage(EPackage domainPackage) {
		return getGenModel().findGenPackage(domainPackage);
 	}
 
 	public GenClass findGenClass(EClass domainMetaClass) {
 		GenPackage gp = findGenPackage(domainMetaClass.getEPackage());
 		for (Iterator it = gp.getGenClasses().iterator(); it.hasNext();) {
 			GenClass genClass = (GenClass) it.next();
 			if (genClass.getEcoreClass().equals(domainMetaClass)) {
 				return genClass;
 			}
 		}
 		throw new IllegalStateException("Can't find genClass for class '" + domainMetaClass.getName() + "' in package " + gp.getPackageName());
 	}
 
 	public GenFeature findGenFeature(EStructuralFeature domainMetaFeature) {
 		GenClass genClass = findGenClass(domainMetaFeature.getEContainingClass());
 		for (Iterator it = genClass.getGenFeatures().iterator(); it.hasNext();) {
 			GenFeature next = (GenFeature) it.next();
 			if (next.getEcoreFeature().equals(domainMetaFeature)) {
 				return next;
 			}
 		}
 		throw new IllegalStateException("Can't find genFeature for feature '" + domainMetaFeature.getName() + "' in class " + genClass.getName());
 	}
 
 	public static GenModel findGenModel(EPackage model) {
 		URI genModelURI = (URI) EcorePlugin.getEPackageNsURIToGenModelLocationMap().get(model.getNsURI());
 		if (genModelURI == null) {
 			URI domainModelURI = model.eResource().getURI(); 
 			genModelURI = domainModelURI.trimFileExtension().appendFileExtension("genmodel");
 			if (genModelURI.equals(domainModelURI)) {
 				genModelURI = null; // don't even try, then
 			}
 		}
 		if (genModelURI == null) {
 			return null;
 		}
 		ResourceSet rs = model.eResource().getResourceSet();
 		// @see org.eclipse.emf.importer.ModelImporter.getExternalGenModels()
 		Resource genModelResource = rs.getResource(genModelURI, false);
 		if (genModelResource == null) {
 			genModelResource = rs.getResource(genModelURI, true);
 			if (genModelResource != null) {
 				return (GenModel) genModelResource.getContents().get(0);
 			}
 		}
 		return null;
 	}
 }
