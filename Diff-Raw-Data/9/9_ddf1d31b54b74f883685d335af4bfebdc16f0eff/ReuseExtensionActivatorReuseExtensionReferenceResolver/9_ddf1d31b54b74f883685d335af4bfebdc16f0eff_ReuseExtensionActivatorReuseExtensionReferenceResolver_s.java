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
 package org.reuseware.coconut.reuseextensionactivator.resource.rexactivator.analysis;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EReference;
 import org.reuseware.coconut.resource.util.ReuseResourcesUtil;
 import org.reuseware.coconut.reuseextension.ComponentModelSpecification;
 import org.reuseware.coconut.reuseextension.CompositionLanguageSpecification;
 import org.reuseware.coconut.reuseextension.CompositionLanguageSyntaxSpecification;
 import org.reuseware.coconut.reuseextension.ReuseExtension;
 import org.reuseware.coconut.reuseextensionactivator.ComponentModelSpecificationActivator;
 import org.reuseware.coconut.reuseextensionactivator.CompositionLanguageSpecificationActivator;
 import org.reuseware.coconut.reuseextensionactivator.CompositionLanguageSyntaxSpecificationActivator;
 import org.reuseware.coconut.reuseextensionactivator.ReuseExtensionActivator;
 import org.reuseware.coconut.reuseextensionactivator.resource.rexactivator.IRex_activatorReferenceResolver;
 
 /**
  * Finds the reuse extension to be activated (if one of the correct type exists).
  */
 public class ReuseExtensionActivatorReuseExtensionReferenceResolver
 		implements IRex_activatorReferenceResolver<ReuseExtensionActivator, ReuseExtension> {
 
 	protected static final String FILE_EXTENSION_REX = "." + "rex";
 	protected static final String NAMESPACE_SEPARATOR = ".";
 
 	/**
 	 * Finds the activated reuse extension in the repository.
 	 * 
 	 * @param identifier The identifier for the reference.
 	 * @param container The object that contains the reference.
 	 * @param reference The reference that points to the target of the reference.
 	 * @param position The index of the reference (if it has an upper bound greater than 1).
 	 * @param resolveFuzzy return objects that do not match exactly
 	 * @param result an object that can be sued to store the result of the resolve operation.
 	 */
 	public void resolve(
 			java.lang.String identifier,
 			org.reuseware.coconut.reuseextensionactivator.ReuseExtensionActivator container,
 			org.eclipse.emf.ecore.EReference reference,
 			int position,
 			boolean resolveFuzzy,
 			final org.reuseware.coconut.reuseextensionactivator.resource.rexactivator.IRex_activatorReferenceResolveResult<org.reuseware.coconut.reuseextension.ReuseExtension> result) {
 		List<String> rexID = new ArrayList<String>();
 		rexID.addAll(container.getRexNamespace());
 		rexID.add(identifier + FILE_EXTENSION_REX);
 
 		ReuseExtension target = ReuseResourcesUtil.getReuseExtension(rexID,
 				container.eResource().getResourceSet());
 
 		if (target != null) {
 			if (target instanceof ComponentModelSpecification
 					&& container instanceof ComponentModelSpecificationActivator) {
 				result.addMapping(identifier, target);
 			} else if (target instanceof CompositionLanguageSpecification
 					&& container instanceof CompositionLanguageSpecificationActivator) {
 				result.addMapping(identifier, target);
 			} else if (target instanceof CompositionLanguageSyntaxSpecification
 					&& container instanceof CompositionLanguageSyntaxSpecificationActivator) {
 				result.addMapping(identifier, target);
 			} 
 		}
 
 		result.setErrorMessage("Reuse extension '" + rexID.toString()
				+ "' not defind or registered");
 	}
 	
 	/**
 	 * Returns the the name of the reuse extension and prepends the
 	 * namespace to the name if it is not already defined in the activator.
 	 * 
 	 * @param element The referenced model element.
 	 * @param container The object referencing the element.
 	 * @param reference The reference that holds the element.
 	 * 
 	 * @return The identification string for the reference
 	 */
 	public String deResolve(ReuseExtension element,
 			ReuseExtensionActivator container, EReference reference) {
 		if (null == element.getRexName()) {
 			return "undefined";
 		}
 
 		String value = element.getRexName();
 		if (value.endsWith(FILE_EXTENSION_REX)) {
 			value = value.substring(0, value.indexOf(FILE_EXTENSION_REX));
 		}
 		if (container.getRexNamespace().isEmpty()) {
 			String namespace = "";
 			for (String nsSegment : element.getRexNamespace()) {
 				namespace = namespace + nsSegment + NAMESPACE_SEPARATOR;
 			}
 			value = namespace + value;
 		}
 		return value;
 	}
 
 	/**
 	 * @param options not supported
 	 */
 	public void setOptions(java.util.Map<?, ?> options) { }
 	
 }
