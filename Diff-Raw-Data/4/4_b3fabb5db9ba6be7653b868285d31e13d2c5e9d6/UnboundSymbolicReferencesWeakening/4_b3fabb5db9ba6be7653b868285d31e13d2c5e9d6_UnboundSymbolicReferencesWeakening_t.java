 /*******************************************************************************
  * Copyright (c) 2010 Technical University of Denmark.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors:
  *    Patrick Koenemann, DTU Informatics - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.mpatch.transform.impl;
 
 import org.eclipse.emf.compare.mpatch.IElementReference;
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.MPatchPackage;
 import org.eclipse.emf.compare.mpatch.ModelDescriptorReference;
 import org.eclipse.emf.compare.mpatch.common.util.MPatchConstants;
 import org.eclipse.emf.compare.mpatch.descriptor.DescriptorPackage;
 import org.eclipse.emf.compare.mpatch.extension.IMPatchTransformation;
 import org.eclipse.emf.compare.mpatch.symrefs.ElementSetReference;
 import org.eclipse.emf.ecore.EReference;
 
 /**
  * This Unbound Symbolic References generalization changes the upper bound to unlimited of all symbolic references of
  * type {@link ElementSetReference}. In other words, all changes are not only applicable once but might be applied
  * several times. In order to make it work, the symbolic references themselves should also be weakened, e.g. by the
  * {@link ScopeExpansion} transformation.
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  */
 public class UnboundSymbolicReferencesWeakening implements IMPatchTransformation {
 
 	/** Label. */
 	public static final String LABEL = "Unbound Symbolic References";
 
 	/** Description for this transformation. */
 	private static final String DESCRIPTION = "This transformation removes the bounds of "
 			+ MPatchConstants.SYMBOLIC_REFERENCES_NAME
 			+ ". This is an optional transformation and might change the result of "
 			+ MPatchConstants.MPATCH_SHORT_NAME
 			+ " application!\n\n"
 			+ "It allow changes to be applicable not only to one but to many model elements. "
 			+ "When applying differences to another model, by default, each change must be applied to exactly one model element. "
 			+ "However, when changing the bounds to [1..*], changes are applicable to more than one just one model element. "
 			+ "For instance, a change describing a class moved to another package might be applied not to a single but to a set of classes.";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getLabel() {
 		return LABEL;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getDescription() {
 		return DESCRIPTION;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getPriority() {
 		return 10;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isOptional() {
 		return true;
 	}
 
 	/**
 	 * This weakening generalization changes the upper bound to unlimited of all symbolic references of type
 	 * {@link ElementSetReference}.
 	 */
 	public int transform(MPatchModel mpatch) {
 		return weakenBounds(mpatch);
 	}
 
 	/**
 	 * Set {@link IElementReference#setUpperBound(int)} to <code>-1</code> for all weakenable symbolic references.
 	 * {@link WeakeningHelper} is used to determine those references.
 	 * 
 	 * @return The number of symbolic references whose upper bounds were updated.
 	 */
 	public static int weakenBounds(MPatchModel mpatch) {
 		int counter = 0;
 		for (IElementReference ref : WeakeningHelper.getWeakenableSymbolicReferences(mpatch)) {
 
 			// not all symbolic references allow unbound resolutions
 			if (isWeakenable((IElementReference) ref))
 				if (ref.setUpperBound(-1))
 					counter++;
 		}
 		return counter;
 	}
 
 	/**
 	 * @param ref
 	 *            A symbolic reference.
 	 * @return <code>true</code>, if <code>ref</code> is capable of an unbound cardinality. <code>false</code>
 	 *         otherwise.
 	 */
 	private static boolean isWeakenable(IElementReference ref) {
 		// only these types of symrefs are weakenable:
 		if (ref instanceof ElementSetReference || ref instanceof ModelDescriptorReference) {
 
 			if (ref.eContainer() == null)
 				throw new IllegalArgumentException(MPatchConstants.SYMBOLIC_REFERENCE_NAME
 						+ " must be contained somewhere: " + ref);
 			final EReference feature = ref.eContainmentFeature();
 
 			// all corresponding elements are weakenable
 			if (MPatchPackage.Literals.INDEP_CHANGE__CORRESPONDING_ELEMENT.equals(feature))
 				return true;
 
			// all resulting elements are weakenable (relevant for reversed mpatches)
			if (MPatchPackage.Literals.INDEP_CHANGE__RESULTING_ELEMENT.equals(feature))
				return true;

 			// changed references
 			if (MPatchPackage.Literals.INDEP_ADD_REM_REFERENCE_CHANGE__CHANGED_REFERENCE.equals(feature))
 				return true;
 
 			// cross references in model descriptors
 			if (DescriptorPackage.Literals.EREFERENCE_TO_ELEMENT_REFERENCE_MAP__VALUE.equals(feature))
 				return true;
 		}
 
 		return false;
 	}
 }
