 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.engine.check;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.compare.diff.EMFCompareDiffMessages;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchPackage;
 import org.eclipse.emf.compare.match.metamodel.UnmatchElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMap;
 
 /**
  * This provides a base implementation for the different checks that clients can need to call in specific
  * differencing engines implementations.
  * 
  * @author Laurent Goubet <a href="mailto:laurent.goubet@obeo.fr">laurent.goubet@obeo.fr</a>
  * @since 1.0
  */
 public abstract class AbstractCheck {
 	/** Allows retrieval of the ancestor matched object. */
 	protected static final int ANCESTOR_OBJECT = 0;
 
 	/** Allows retrieval of the left matched object. */
 	protected static final int LEFT_OBJECT = 1;
 
 	/** Allows retrieval of the right matched object. */
 	protected static final int RIGHT_OBJECT = 2;
 
 	/**
 	 * If we're currently doing a resourceSet differencing, this will have been initialized with the whole
 	 * MatchResourceSet.
 	 */
 	protected final EcoreUtil.CrossReferencer crossReferencer;
 
 	/**
 	 * Instantiates the checker given the current crossreferencing members of the diff engine.
 	 * 
 	 * @param referencer
 	 *            This cross referencer has been initialized with the whole MatchResourceSet and can be used
 	 *            to retrieve matched EObjects towards other resources.
 	 */
 	public AbstractCheck(EcoreUtil.CrossReferencer referencer) {
 		crossReferencer = referencer;
 	}
 
 	/**
 	 * This will return a list containing only EObjects. This is mainly aimed at FeatureMap.Entry values.
 	 * 
 	 * @param input
 	 *            List that is to be converted.
 	 * @return A list containing only EObjects.
 	 * @since 1.0
 	 */
 	protected final List<Object> convertFeatureMapList(List<?> input) {
 		final List<Object> result = new ArrayList<Object>();
 		for (final Object inputValue : input) {
 			result.add(internalFindActualEObject(inputValue));
 		}
 		return result;
 	}
 
 	/**
 	 * Return the left or right matched EObject from the one given. More specifically, this will return the
 	 * left matched element if the given {@link EObject} is the right one, or the right matched element if the
 	 * given {@link EObject} is either the left or the origin one.
 	 * 
 	 * @param from
 	 *            The original {@link EObject}.
 	 * @return The matched {@link EObject}.
 	 */
 	protected final EObject getMatchedEObject(EObject from) {
 		EObject matchedEObject = null;
 		if (crossReferencer != null && from != null && crossReferencer.get(from) != null) {
 			for (final org.eclipse.emf.ecore.EStructuralFeature.Setting setting : crossReferencer.get(from)) {
 				if (setting.getEObject() instanceof Match2Elements) {
 					if (setting.getEStructuralFeature().getFeatureID() == MatchPackage.MATCH2_ELEMENTS__LEFT_ELEMENT) {
 						matchedEObject = ((Match2Elements)setting.getEObject()).getRightElement();
 					} else if (setting.getEStructuralFeature().getFeatureID() == MatchPackage.MATCH2_ELEMENTS__RIGHT_ELEMENT) {
 						matchedEObject = ((Match2Elements)setting.getEObject()).getLeftElement();
 					}
 				}
 			}
 		}
 		return matchedEObject;
 	}
 
 	/**
 	 * Return the specified matched {@link EObject} from the one given.
 	 * 
 	 * @param from
 	 *            The original {@link EObject}.
 	 * @param side
 	 *            side of the object we seek. Must be one of
 	 *            <ul>
 	 *            <li>{@link #ANCESTOR_OBJECT}</li>
 	 *            <li>{@link #LEFT_OBJECT}</li>
 	 *            <li>{@link #RIGHT_OBJECT}</li>
 	 *            </ul>
 	 *            .
 	 * @return The matched EObject.
 	 * @throws IllegalArgumentException
 	 *             Thrown if <code>side</code> is invalid.
 	 */
 	protected final EObject getMatchedEObject(EObject from, int side) throws IllegalArgumentException {
 		if (side != LEFT_OBJECT && side != RIGHT_OBJECT && side != ANCESTOR_OBJECT) {
 			throw new IllegalArgumentException(EMFCompareDiffMessages
 					.getString("GenericDiffEngine.IllegalSide")); //$NON-NLS-1$
 		}
 		EObject matchedEObject = null;
 		if (crossReferencer != null) {
 			final Collection<EStructuralFeature.Setting> settings = crossReferencer.get(from);
 			if (settings == null)
 				return null;
 			for (final org.eclipse.emf.ecore.EStructuralFeature.Setting setting : settings) {
 				if (setting.getEObject() instanceof Match2Elements) {
 					if (side == LEFT_OBJECT) {
 						matchedEObject = ((Match2Elements)setting.getEObject()).getLeftElement();
 					} else if (side == RIGHT_OBJECT) {
 						matchedEObject = ((Match2Elements)setting.getEObject()).getRightElement();
 					} else if (setting.getEObject() instanceof Match3Elements) {
 						matchedEObject = ((Match3Elements)setting.getEObject()).getOriginElement();
 					}
 				}
 			}
 		}
 		return matchedEObject;
 	}
 
 	/**
 	 * Returns <code>true</code> if the given element corresponds to an UnmatchedElement, <code>false</code>
 	 * otherwise.
 	 * 
 	 * @param element
 	 *            The element for which we need to know whether it is unmatched.
 	 * @return <code>true</code> if the given element corresponds to an UnmatchedElement, <code>false</code>
 	 *         otherwise.
 	 */
 	protected final boolean isUnmatched(EObject element) {
		if (crossReferencer != null && crossReferencer.get(element) != null) {
			final Iterator<EStructuralFeature.Setting> it = crossReferencer.get(element).iterator();
			if (it.hasNext() && it.next().getEObject() instanceof UnmatchElement) {
				return true;
			}
 		}
 		return false;
 	}
 
 	/**
 	 * This will return the first value of <tt>data</tt> that is not an instance of {@link Entry}.
 	 * 
 	 * @param data
 	 *            The object we need a valued of.
 	 * @return The first value of <tt>data</tt> that is not an instance of FeatureMapEntry.
 	 */
 	private Object internalFindActualEObject(Object data) {
 		if (data instanceof FeatureMap.Entry)
 			return internalFindActualEObject(((FeatureMap.Entry)data).getValue());
 		return data;
 	}
 }
