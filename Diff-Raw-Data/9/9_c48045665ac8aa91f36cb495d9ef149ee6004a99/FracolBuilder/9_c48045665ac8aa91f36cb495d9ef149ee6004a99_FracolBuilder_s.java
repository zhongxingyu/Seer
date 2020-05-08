 /*******************************************************************************
 * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.coconut.fracol.resource.fracol.mopp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.reuseware.coconut.fracol.FragmentCollaboration;
 import org.reuseware.coconut.fracol.resource.fracol.FracolEProblemType;
 import org.reuseware.coconut.resource.ReuseResources;
 
 /**
  * A builder that automatically syncs the ID defined inside a *.fracol
  * file with the file's ID in the Sokan repository.
  */
 public class FracolBuilder implements org.reuseware.coconut.fracol.resource.fracol.IFracolBuilder {
 	
 	/**
 	 * @param uri URI of file to build
 	 * 
 	 * @return true
 	 */
 	public boolean isBuildingNeeded(org.eclipse.emf.common.util.URI uri) {
 		return true;
 	}
 	
 	/**
 	 * Corrects the fracol ID or warns if the fracol is not located in a
 	 * Sokan repository.
 	 * 
 	 * @param resource the fracol resource to build
 	 * @param monitor a progress monitor
 	 * 
 	 * @return <code>org.eclipse.core.runtime.Status.OK_STATUS;</code>
 	 */
 	public IStatus build(FracolResource resource, IProgressMonitor monitor) {
 		String warning = validateAndCorrect(resource);
 		if (warning != null) {
 			resource.addWarning(warning, FracolEProblemType.BUILDER_ERROR, resource.getContents().get(0));
 		}
 		return org.eclipse.core.runtime.Status.OK_STATUS;
 	}
 	
 	private String validateAndCorrect(Resource resource) {
 		if (resource.getContents().isEmpty()) {
 			return null;
 		}
 		EObject root = resource.getContents().get(0);
 		if (!(root instanceof FragmentCollaboration)) {
 			return null;
 		}
 		FragmentCollaboration element = (FragmentCollaboration) root;
 
 		List<String> idOutsideModel = ReuseResources.INSTANCE.getID(resource.getURI());
 		if (idOutsideModel == null) {	
 			return "Place this specification into a fragment store to activate it";
 		}
 		
 		String warningMessage = null;
 		
 		List<String> idInsideModel = new ArrayList<String>(element.getFracolNamespace());
 		idInsideModel.add(element.getFracolName());
 		
 		if (!idOutsideModel.equals(idInsideModel)) {
 			//correct!
 			String name = idOutsideModel.remove(idOutsideModel.size() - 1);
 			
 			element.setFracolName(name);
 			element.getFracolNamespace().clear();
 			element.getFracolNamespace().addAll(idOutsideModel);
 			
 			try {
 				ResourceSet rs = resource.getResourceSet();
 				Map<Object, Object> options = null;
 				if (rs != null) {
 					options = rs.getLoadOptions();
 				}
 				resource.save(options);
 			} catch (IOException e) {
 				FracolPlugin.logError("Error during ID correction", e);
 			}
 		}
 
 		return warningMessage;
 	}
 	
 }
