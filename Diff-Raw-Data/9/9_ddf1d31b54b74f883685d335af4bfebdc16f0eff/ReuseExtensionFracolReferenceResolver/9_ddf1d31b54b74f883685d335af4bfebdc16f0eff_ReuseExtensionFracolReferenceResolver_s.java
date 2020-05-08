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
 package org.reuseware.coconut.reuseextension.resource.rex.analysis;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EReference;
 import org.reuseware.coconut.fracol.FragmentCollaboration;
 import org.reuseware.coconut.resource.util.ReuseResourcesUtil;
 import org.reuseware.coconut.reuseextension.ReuseExtension;
 import org.reuseware.coconut.reuseextension.resource.rex.IRexReferenceResolveResult;
 import org.reuseware.coconut.reuseextension.resource.rex.IRexReferenceResolver;
 
 /**
  * Finds the Fracol implemented by a reuse extension.
  */
 public class ReuseExtensionFracolReferenceResolver implements 
 			IRexReferenceResolver<ReuseExtension, FragmentCollaboration> {
 
 	protected static final String FILE_EXTENSION_FRACOL = 
 		"." + "fracol";
 	protected static final String NAMESPACE_SEPARATOR = 
 		".";
 	
 	/**
 	 * Searches for the Fracol with the given name and the defined namespace in
 	 * the Sokan repository.
 	 * 
 	 * @param identifier The identifier for the reference.
 	 * @param container The object that contains the reference.
 	 * @param reference The reference that points to the target of the reference.
 	 * @param position The index of the reference (if it has an upper bound greater than 1).
 	 * @param resolveFuzzy return objects that do not match exactly
 	 * @param result an object that can be sued to store the result of the resolve operation.
 	 */
 	public void resolve(String identifier, ReuseExtension container, EReference reference, int position, boolean resolveFuzzy, IRexReferenceResolveResult<FragmentCollaboration> result) {
 		List<String> fracolID = new ArrayList<String>();
 		fracolID.addAll(container.getFracolNamespace());
 		fracolID.add(identifier + FILE_EXTENSION_FRACOL);
 
 		FragmentCollaboration target = ReuseResourcesUtil.getFragmentCollaboration(
 				fracolID, container.eResource().getResourceSet());
 		if (target != null) {
 			result.addMapping(identifier, target);
 		}
 			
		result.setErrorMessage("Fracol '" + fracolID + "' not defind or registered");
 	}
 	
 	/**
 	 * Returns the name of the Fracol. If the Fracol's namespace is not 
 	 * defined in the reuse extension, it is prepended to the name.
 	 * 
 	 * @param element The referenced model element.
 	 * @param container The object referencing the element.
 	 * @param reference The reference that holds the element.
 	 * 
 	 * @return The identification string for the reference
 	 */
 	public String deResolve(FragmentCollaboration element, ReuseExtension container, EReference reference) {
 		if (null == element.getFracolName()) {
 			return "undefined";
 		}
 		
 		String value = element.getFracolName();
 		if (value.endsWith(FILE_EXTENSION_FRACOL)) {
 			value = value.substring(0, value.indexOf(FILE_EXTENSION_FRACOL));
 		}
 		if (container.getFracolNamespace().isEmpty()) {
 			String namespace = "";
 			for (String nsSegment : element.getFracolNamespace()) {
 				namespace = namespace + nsSegment + NAMESPACE_SEPARATOR;
 			}
 			value = namespace + value;
 		}
 		return value;
 	}
 	
 	/**
 	 * @param options not supported
 	 */
 	public void setOptions(Map<?, ?> options) { }
 }
