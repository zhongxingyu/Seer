 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.engine;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.diff.EMFCompareDiffMessages;
 import org.eclipse.emf.compare.diff.api.IDiffEngine;
 import org.eclipse.emf.compare.diff.metamodel.AbstractDiffExtension;
 import org.eclipse.emf.compare.diff.metamodel.AddAttribute;
 import org.eclipse.emf.compare.diff.metamodel.AddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.AddReferenceValue;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.MoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoteAddAttribute;
 import org.eclipse.emf.compare.diff.metamodel.RemoteAddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoteAddReferenceValue;
 import org.eclipse.emf.compare.diff.metamodel.RemoteMoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoteRemoveAttribute;
 import org.eclipse.emf.compare.diff.metamodel.RemoteRemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoteRemoveReferenceValue;
 import org.eclipse.emf.compare.diff.metamodel.RemoveAttribute;
 import org.eclipse.emf.compare.diff.metamodel.RemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoveReferenceValue;
 import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
 import org.eclipse.emf.compare.diff.metamodel.UpdateUniqueReferenceValue;
 import org.eclipse.emf.compare.diff.service.DiffService;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Element;
 import org.eclipse.emf.compare.match.metamodel.MatchElement;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.RemoteUnMatchElement;
 import org.eclipse.emf.compare.match.metamodel.UnMatchElement;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * This class is useful when one wants to determine a diff from a matching model.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class GenericDiffEngine implements IDiffEngine {
 	/** Allows retrieval of the ancestor matched object. */
 	protected static final int ANCESTOR_OBJECT = 0;
 
 	/** Allows retrieval of the left matched object. */
 	protected static final int LEFT_OBJECT = 1;
 
 	/** Allows retrieval of the right matched object. */
 	protected static final int RIGHT_OBJECT = 2;
 
 	/**
 	 * This map will keep track of the top level unmatched elements, as well as whether they are conflicting.
 	 */
 	protected final Map<UnMatchElement, Boolean> unMatchedElements = new EMFCompareMap<UnMatchElement, Boolean>();
 
 	/** This map is useful to find the Match from any EObject instance. */
 	private final Map<EObject, Match2Elements> eObjectToMatch = new EMFCompareMap<EObject, Match2Elements>();
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.api.IDiffEngine#doDiff(org.eclipse.emf.compare.match.metamodel.MatchModel)
 	 */
 	public DiffModel doDiff(MatchModel match) {
 		return doDiff(match, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.api.IDiffEngine#doDiff(org.eclipse.emf.compare.match.metamodel.MatchModel,
 	 *      boolean)
 	 */
 	public DiffModel doDiff(MatchModel match, boolean threeWay) {
 		updateEObjectToMatch(match, threeWay);
 		final DiffModel result = DiffFactory.eINSTANCE.createDiffModel();
 		result.setLeft(match.getLeftModel());
 		result.setRight(match.getRightModel());
 		result.setOrigin(match.getOriginModel());
 
 		DiffGroup diffRoot = null;
 		if (threeWay)
 			diffRoot = doDiffThreeWay(match);
 		else
 			diffRoot = doDiffTwoWay(match);
 
 		result.getOwnedElements().add(diffRoot);
 
 		final Iterator<EObject> it = match.eAllContents();
 		boolean found = false;
 		EObject leftRoot = null;
 		EObject rightRoot = null;
 		while (it.hasNext() && !found) {
 			final EObject itMatch = it.next();
 			if (itMatch instanceof Match2Elements) {
 				leftRoot = ((Match2Elements)itMatch).getLeftElement();
 				rightRoot = ((Match2Elements)itMatch).getRightElement();
 				found = true;
 			}
 		}
 		/*
 		 * First get the file extension..
 		 */
 		String extension = "ecore"; //$NON-NLS-1$
 		if (leftRoot != null && leftRoot.eResource() != null)
 			extension = leftRoot.eResource().getURI().fileExtension();
 		if (extension == null && rightRoot != null && rightRoot.eResource() != null)
 			extension = rightRoot.eResource().getURI().fileExtension();
 		final Collection<AbstractDiffExtension> extensions = DiffService
 				.getCorrespondingDiffExtensions(extension);
 		for (AbstractDiffExtension ext : extensions) {
 			// TODOCBR can this really be null?
 			if (ext != null)
 				ext.visit(result);
 		}
 		return result;
 	}
 
 	/**
 	 * Looks for an already created {@link DiffGroup diff group} in order to add the operation, if none
 	 * exists, create one where the operation belongs to.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffModel}.
 	 * @param operation
 	 *            Operation to add to the {@link DiffModel}.
 	 * @param targetParent
 	 *            Parent {@link EObject} for the operation.
 	 */
 	protected void addInContainerPackage(DiffGroup root, DiffElement operation, EObject targetParent) {
 		if (targetParent == null) {
 			root.getSubDiffElements().add(operation);
 			return;
 		}
 		DiffGroup targetGroup = findExistingGroup(root, targetParent);
 		if (targetGroup == null) {
 			// Searches for a DiffGroup with the matched parent
 			targetGroup = findExistingGroup(root, getMatchedEObject(targetParent));
 			if (targetGroup == null) {
 				// we have to create the group
 				targetGroup = buildHierarchyGroup(targetParent, root);
 			}
 		}
 		targetGroup.getSubDiffElements().add(operation);
 	}
 
 	/**
 	 * This will iterate through all the attributes of the <code>mapping</code>'s two elements to check if
 	 * any of them has been modified.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if one of the attributes has
 	 *            actually been changed.
 	 * @param mapping
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails.
 	 */
 	protected void checkAttributesUpdates(DiffGroup root, Match2Elements mapping) throws FactoryException {
 		final EClass eClass = mapping.getLeftElement().eClass();
 
 		final List<EAttribute> eclassAttributes = eClass.getEAllAttributes();
 		// for each feature, compare the value
 		final Iterator<EAttribute> it = eclassAttributes.iterator();
 		while (it.hasNext()) {
 			final EAttribute next = it.next();
 			if (!shouldBeIgnored(next)) {
 				final String attributeName = next.getName();
 				final Object leftValue = EFactory.eGet(mapping.getLeftElement(), attributeName);
 				final Object rightValue = EFactory.eGet(mapping.getRightElement(), attributeName);
 
 				if (leftValue instanceof EEnumLiteral && rightValue instanceof EEnumLiteral) {
 					final StringBuilder value1 = new StringBuilder();
 					value1.append(((EEnumLiteral)leftValue).getLiteral()).append(
 							((EEnumLiteral)leftValue).getValue());
 					final StringBuilder value2 = new StringBuilder();
 					value2.append(((EEnumLiteral)rightValue).getLiteral()).append(
 							((EEnumLiteral)rightValue).getValue());
 					if (!value1.toString().equals(value2.toString()))
 						createNonConflictingAttributeChange(root, next, mapping.getLeftElement(), mapping
 								.getRightElement());
 				} else if ((leftValue != null && !leftValue.equals(rightValue))
 						|| (leftValue == null && leftValue != rightValue)) {
 					createNonConflictingAttributeChange(root, next, mapping.getLeftElement(), mapping
 							.getRightElement());
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will iterate through all the attributes of the <code>mapping</code>'s three elements to check
 	 * if any of them has been modified.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if one of the attribute has
 	 *            actually been changed.
 	 * @param mapping
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails.
 	 */
 	protected void checkAttributesUpdates(DiffGroup root, Match3Element mapping) throws FactoryException {
 		// Ignores matchElements when they don't have origin (no updates on
 		// these)
 		if (mapping.getOriginElement() == null)
 			return;
 		final EClass eClass = mapping.getOriginElement().eClass();
 
 		final List<EAttribute> eclassAttributes = eClass.getEAllAttributes();
 		// for each feature, compare the value
 		final Iterator<EAttribute> it = eclassAttributes.iterator();
 		while (it.hasNext()) {
 			final EAttribute next = it.next();
 			if (!shouldBeIgnored(next)) {
 				final String attributeName = next.getName();
 				final Object leftValue = EFactory.eGet(mapping.getLeftElement(), attributeName);
 				final Object rightValue = EFactory.eGet(mapping.getRightElement(), attributeName);
 				final Object ancestorValue = EFactory.eGet(mapping.getOriginElement(), attributeName);
 
 				final boolean rightDistinctFromOrigin = rightValue != ancestorValue && rightValue != null
 						&& !rightValue.equals(ancestorValue);
 				final boolean rightDistinctFromLeft = rightValue != leftValue && rightValue != null
 						&& !rightValue.equals(leftValue);
 				final boolean leftDistinctFromOrigin = leftValue != ancestorValue && leftValue != null
 						&& !leftValue.equals(ancestorValue);
 
 				// There's a change if one of the above is true
 				if (rightDistinctFromOrigin || rightDistinctFromLeft || leftDistinctFromOrigin) {
 					// non conflicting change
 					if (rightDistinctFromOrigin && !leftDistinctFromOrigin) {
 						createNonConflictingAttributeChange(root, next, mapping.getLeftElement(), mapping
 								.getRightElement());
 						// only latest from head (left) has changed
 					} else if (leftDistinctFromOrigin && !rightDistinctFromOrigin) {
 						createRemoteAttributeChange(root, next, mapping);
 						// conflicting
 					} else if (!rightDistinctFromOrigin || rightDistinctFromLeft) {
 						checkConflictingAttributesUpdate(root, next, mapping);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will call all the different checks we need to call for when computing the diff. Clients can
 	 * override this to alter the checks or add others.
 	 * 
 	 * @param current
 	 *            current {@link DiffGroup} under which the new differences will be added.
 	 * @param match
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails somehow.
 	 */
 	protected void checkForDiffs(DiffGroup current, Match2Elements match) throws FactoryException {
 		checkAttributesUpdates(current, match);
 		checkReferencesUpdates(current, match);
 		checkMoves(current, match);
 	}
 
 	/**
 	 * This will check if the elements matched by a given {@link Match2Elements} have been moved..
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if the elements have actually
 	 *            been moved.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 */
 	protected void checkMoves(DiffGroup root, Match2Elements matchElement) {
 		if (matchElement.getLeftElement().eContainer() != null
 				&& matchElement.getRightElement().eContainer() != null
 				&& getMatchedEObject(matchElement.getLeftElement().eContainer()) != matchElement
 						.getRightElement().eContainer()) {
 			createMoveOperation(root, matchElement.getLeftElement(), matchElement.getRightElement());
 		}
 	}
 
 	/**
 	 * This will check if the elements matched by a given {@link Match3Element} have been moved since the
 	 * models common ancestor.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if the elements have actually
 	 *            been moved.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 */
 	protected void checkMoves(DiffGroup root, Match3Element matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		final EObject originElement = matchElement.getOriginElement();
 
 		final boolean leftMoved = originElement != null
 				&& leftElement.eContainer() != null
 				&& !getMatchedEObject(leftElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 		final boolean rightMoved = originElement != null
 				&& rightElement.eContainer() != null
 				&& !getMatchedEObject(rightElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 
 		// conflicting change
 		if (leftMoved && rightMoved
 				&& !getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())) {
 			final MoveModelElement operation = DiffFactory.eINSTANCE.createMoveModelElement();
 			operation.setRightElement(rightElement);
 			operation.setLeftElement(leftElement);
 			operation.setRightTarget(getMatchedEObject(leftElement.eContainer()));
 			operation.setLeftTarget(getMatchedEObject(rightElement.eContainer()));
 
 			final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 					.createConflictingDiffElement();
 			conflictingDiff.setLeftParent(leftElement);
 			conflictingDiff.setRightParent(rightElement);
 			conflictingDiff.setOriginElement(originElement);
 			conflictingDiff.getSubDiffElements().add(operation);
 			root.getSubDiffElements().add(conflictingDiff);
 			// non conflicting change
 		} else if (rightMoved
 				&& !getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())) {
 			createMoveOperation(root, leftElement, rightElement);
 		} else if (leftMoved
 				&& !getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())) {
 			createRemoteMoveOperation(root, leftElement, rightElement);
 		}
 	}
 
 	/**
 	 * Checks if there's been references updates in the model.<br/>
 	 * <p>
 	 * A reference is considered updated if its value(s) has been changed (either removal or addition of an
 	 * element if the reference is multi-valued or update of a single-valued reference) between the left and
 	 * the right model.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param mapping
 	 *            Contains informations about the left and right model elements we have to compare.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the references' values.
 	 */
 	protected void checkReferencesUpdates(DiffGroup root, Match2Elements mapping) throws FactoryException {
 		final EClass eClass = mapping.getLeftElement().eClass();
 		final List<EReference> eclassReferences = eClass.getEAllReferences();
 
 		final Iterator<EReference> it = eclassReferences.iterator();
 		while (it.hasNext()) {
 			final EReference next = it.next();
 			if (!shouldBeIgnored(next)) {
 				createNonConflictingReferencesUpdate(root, next, mapping.getLeftElement(), mapping
 						.getRightElement());
 			}
 		}
 	}
 
 	/**
 	 * Checks if there's been references updates in the model.<br/>
 	 * <p>
 	 * A reference is considered updated if its value(s) has been changed (either removal or addition of an
 	 * element if the reference is multi-valued or update of a single-valued reference) between the left and
 	 * the ancestor model, the right and the ancestor or between the left and the right model.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param mapping
 	 *            Contains informations about the left, right and origin model elements we have to compare.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the references' values.
 	 */
 	protected void checkReferencesUpdates(DiffGroup root, Match3Element mapping) throws FactoryException {
 		// Ignores matchElements when they don't have origin (no updates on
 		// these)
 		if (mapping.getOriginElement() == null)
 			return;
 		final EClass eClass = mapping.getOriginElement().eClass();
 		final List<EReference> eclassReferences = eClass.getEAllReferences();
 
 		final Iterator<EReference> it = eclassReferences.iterator();
 		while (it.hasNext()) {
 			final EReference next = it.next();
 			if (!shouldBeIgnored(next)) {
 				final String referenceName = next.getName();
 				final List<?> leftReferences = EFactory.eGetAsList(mapping.getLeftElement(), referenceName);
 				final List<?> rightReferences = EFactory.eGetAsList(mapping.getRightElement(), referenceName);
 				final List<?> ancestorReferences = EFactory.eGetAsList(mapping.getOriginElement(),
 						referenceName);
 
 				// Checks if there're conflicts
 				if (isConflictual(next, leftReferences, rightReferences, ancestorReferences)) {
 					createConflictingReferenceUpdate(root, next, mapping);
 					// We know there aren't any conflicting changes
 				} else {
 					final List<EObject> remoteDeletedReferences = new ArrayList<EObject>();
 					final List<EObject> remoteAddedReferences = new ArrayList<EObject>();
 					final List<EObject> deletedReferences = new ArrayList<EObject>();
 					final List<EObject> addedReferences = new ArrayList<EObject>();
 
 					populateThreeWayReferencesChanges(mapping, next, addedReferences, deletedReferences,
 							remoteAddedReferences, remoteDeletedReferences);
 					createRemoteReferencesUpdate(root, next, mapping, remoteAddedReferences,
 							remoteDeletedReferences);
 
 					boolean isUniqueReferenceUpdate = false;
 					if (!next.isMany() && addedReferences.size() > 0 && deletedReferences.size() > 0) {
 						isUniqueReferenceUpdate = !addedReferences.get(0).eIsProxy()
 								|| !deletedReferences.get(0).eIsProxy()
 								|| !EcoreUtil.getURI(addedReferences.get(0)).equals(
 										EcoreUtil.getURI(deletedReferences.get(0)));
 					}
 
 					if (isUniqueReferenceUpdate) {
 						root.getSubDiffElements().add(
 								createUpdatedReferencesOperation(mapping.getLeftElement(), mapping
 										.getRightElement(), next, addedReferences, deletedReferences));
 					} else if (addedReferences.size() > 0) {
 						// REFERENCES ADD
 						createNewReferencesOperation(root, mapping.getLeftElement(), mapping
 								.getRightElement(), next, addedReferences);
 					} else if (deletedReferences.size() > 0) {
 						// REFERENCES DEL
 						createRemovedReferencesOperation(root, mapping.getLeftElement(), mapping
 								.getRightElement(), next, deletedReferences);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * The diff computing for three way comparisons is handled here. We'll compute the diff model from the
 	 * given match model.
 	 * 
 	 * @param match
 	 *            {@link MatchModel match model} we'll be using to compute the differences.
 	 * @return {@link DiffGroup root} of the {@link DiffModel} computed from the given {@link MatchModel}.
 	 */
 	protected DiffGroup doDiffThreeWay(MatchModel match) {
 		final DiffGroup diffRoot = DiffFactory.eINSTANCE.createDiffGroup();
 		
 		// It is a possibility that no elements where matched
 		if (match.getMatchedElements().size() > 0) {
 			// we have to browse the model and create the corresponding operations
 			final Match3Element matchRoot = (Match3Element)match.getMatchedElements().get(0);
 	
 			doDiffDelegate(diffRoot, matchRoot);
 		}
 
 		unMatchedElements.clear();
 		final Iterator<UnMatchElement> unMatched = match.getUnMatchedElements().iterator();
 		while (unMatched.hasNext()) {
 			final UnMatchElement unMatchElement = unMatched.next();
 			boolean isChild = false;
 			boolean isAncestor = false;
 			for (Object object : match.getUnMatchedElements()) {
 				if (unMatchElement != ((UnMatchElement)object)) {
 					if (EcoreUtil.isAncestor(unMatchElement.getElement(), ((UnMatchElement)object)
 							.getElement())) {
 						isAncestor = true;
 					}
 					if (EcoreUtil.isAncestor(((UnMatchElement)object).getElement(), unMatchElement
 							.getElement())) {
 						isChild = true;
 					}
 				}
 				if (isChild || isAncestor)
 					break;
 			}
 			if (!isChild)
 				unMatchedElements.put(unMatchElement, isAncestor);
 		}
 		if (unMatchedElements.size() > 0) {
 			// seeks left resource
 			Resource leftModel = null;
 			for (UnMatchElement element : unMatchedElements.keySet()) {
 				if (element.getElement().eResource().getURI().toString().equals(match.getLeftModel())) {
 					leftModel = element.eResource();
 					break;
 				}
 			}
 			processUnMatchedElements(diffRoot, leftModel, unMatchedElements);
 		}
 		return diffRoot;
 	}
 
 	/**
 	 * The diff computing for two way comparisons is handled here. We'll compute the diff model from the given
 	 * match model.
 	 * 
 	 * @param match
 	 *            {@link MatchModel match model} we'll be using to compute the differences.
 	 * @return {@link DiffGroup root} of the {@link DiffModel} computed from the given {@link MatchModel}.
 	 */
 	protected DiffGroup doDiffTwoWay(MatchModel match) {
 		final DiffGroup diffRoot = DiffFactory.eINSTANCE.createDiffGroup();
 
 		// It is a possibility that no elements where matched
 		if (match.getMatchedElements().size() > 0) {
 			// we have to browse the model and create the corresponding operations
 			final Match2Elements matchRoot = (Match2Elements)match.getMatchedElements().get(0);
 
 			// browsing the match model
 			doDiffDelegate(diffRoot, matchRoot);
 		}
 		// iterate over the unmatched elements end determine if they have been
 		// added or removed.
 		final List<UnMatchElement> unMatched = new ArrayList<UnMatchElement>();
 		for (Object anUnMatched : match.getUnMatchedElements())
 			unMatched.add((UnMatchElement)anUnMatched);
 		// seeks left resource
 		Resource leftModel = null;
 		for (UnMatchElement element : unMatched) {
			if (element.getElement().eResource().getURI().toString().equals(match.getLeftModel())) {
				leftModel = element.eResource();
 				break;
 			}
 		}
 		processUnMatchedElements(diffRoot, leftModel, unMatched);
 		return diffRoot;
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
 	protected EObject getMatchedEObject(EObject from) {
 		EObject matchedEObject = null;
 		final Match2Elements matchElem = eObjectToMatch.get(from);
 		if (matchElem != null && from.equals(matchElem.getRightElement()))
 			matchedEObject = matchElem.getLeftElement();
 		else if (matchElem != null)
 			matchedEObject = matchElem.getRightElement();
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
 	 *            </ul>.
 	 * @return The matched EObject.
 	 * @throws IllegalArgumentException
 	 *             Thrown if <code>side</code> is invalid.
 	 */
 	protected EObject getMatchedEObject(EObject from, int side) throws IllegalArgumentException {
 		if (side != LEFT_OBJECT && side != RIGHT_OBJECT && side != ANCESTOR_OBJECT)
 			throw new IllegalArgumentException(EMFCompareDiffMessages.getString("DiffMaker.IllegalSide")); //$NON-NLS-1$
 		EObject matchedEObject = null;
 		final Match2Elements matchElem = eObjectToMatch.get(from);
 		if (matchElem != null) {
 			if (side == LEFT_OBJECT)
 				matchedEObject = matchElem.getLeftElement();
 			else if (side == RIGHT_OBJECT)
 				matchedEObject = matchElem.getRightElement();
 			else if (side == ANCESTOR_OBJECT && matchElem instanceof Match3Element)
 				matchedEObject = ((Match3Element)matchElem).getOriginElement();
 		}
 		return matchedEObject;
 	}
 
 	/**
 	 * This will process the {@link #unMatchedElements unmatched elements} list and create the appropriate
 	 * {@link DiffElement}s.
 	 * <p>
 	 * This is called for two-way comparison. Clients can override this to alter the checks or add their own.
 	 * </p>
 	 * 
 	 * @param diffRoot
 	 *            {@link DiffGroup} under which to create the {@link DiffElement}s.
 	 * @param leftModel
 	 *            {@link Resource} representing the left model.
 	 * @param unMatched
 	 *            The MatchModel's {@link UnMatchElement}s.
 	 */
 	protected void processUnMatchedElements(DiffGroup diffRoot, Resource leftModel,
 			List<UnMatchElement> unMatched) {
 		for (UnMatchElement unMatchElement : unMatched) {
 			final EObject element = unMatchElement.getElement();
 			if (element.eResource() == leftModel) {
 				// add RemoveModelElement
 				final RemoveModelElement operation = DiffFactory.eINSTANCE.createRemoveModelElement();
 				operation.setLeftElement(element);
 				// Container will be null if we're adding a root
 				if (element.eContainer() != null)
 					operation.setRightParent(getMatchedEObject(element.eContainer()));
 				addInContainerPackage(diffRoot, operation, element.eContainer());
 			} else {
 				// add AddModelElement
 				final AddModelElement operation = DiffFactory.eINSTANCE.createAddModelElement();
 				operation.setRightElement(element);
 				// Container will be null if we're adding a root
 				if (element.eContainer() != null) {
 					operation.setLeftParent(getMatchedEObject(element.eContainer()));
 					addInContainerPackage(diffRoot, operation, getMatchedEObject(element.eContainer()));
 				} else {
 					addInContainerPackage(diffRoot, operation, element.eContainer());
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will process the {@link #unMatchedElements unmatched elements} list and create the appropriate
 	 * {@link DiffElement}s.
 	 * <p>
 	 * This is called for three-way comparison. Clients can override this to alter the checks or add their
 	 * own.
 	 * </p>
 	 * 
 	 * @param diffRoot
 	 *            {@link DiffGroup} under which to create the {@link DiffElement}s.
 	 * @param leftModel
 	 *            {@link Resource} representing the left model.
 	 * @param unMatched
 	 *            The MatchModel's {@link UnMatchElement}s.
 	 */
 	protected void processUnMatchedElements(DiffGroup diffRoot, Resource leftModel,
 			Map<UnMatchElement, Boolean> unMatched) {
 		for (UnMatchElement unMatchElement : unMatched.keySet()) {
 			final boolean isConflicting = unMatched.get(unMatchElement);
 
 			final EObject element = unMatchElement.getElement();
 			final EObject matchedParent = getMatchedEObject(element.eContainer());
 			final EObject matchedAncestor = getMatchedEObject(element, ANCESTOR_OBJECT);
 
 			if (unMatchElement instanceof RemoteUnMatchElement && element.eResource() == leftModel) {
 				final RemoteAddModelElement addOperation = DiffFactory.eINSTANCE
 						.createRemoteAddModelElement();
 				addOperation.setLeftElement(element);
 				addOperation.setRightParent(matchedParent);
 				addInContainerPackage(diffRoot, addOperation, element.eContainer());
 			} else if (unMatchElement instanceof RemoteUnMatchElement) {
 				final DiffElement operation;
 				if (isConflicting) {
 					operation = DiffFactory.eINSTANCE.createConflictingDiffElement();
 					((ConflictingDiffElement)operation).setLeftParent(matchedParent);
 					((ConflictingDiffElement)operation).setRightParent(element);
 					((ConflictingDiffElement)operation).setOriginElement(matchedAncestor);
 
 					final RemoteRemoveModelElement removeOperation = DiffFactory.eINSTANCE
 							.createRemoteRemoveModelElement();
 					removeOperation.setRightElement(element);
 					removeOperation.setLeftParent(matchedParent);
 
 					operation.getSubDiffElements().add(removeOperation);
 				} else {
 					operation = DiffFactory.eINSTANCE.createRemoteRemoveModelElement();
 					((RemoteRemoveModelElement)operation).setRightElement(element);
 					((RemoteRemoveModelElement)operation).setLeftParent(matchedParent);
 				}
 
 				addInContainerPackage(diffRoot, operation, matchedParent);
 			} else if (element.eResource() == leftModel) {
 				final DiffElement operation;
 				if (isConflicting) {
 					operation = DiffFactory.eINSTANCE.createConflictingDiffElement();
 					((ConflictingDiffElement)operation).setLeftParent(element);
 					((ConflictingDiffElement)operation).setRightParent(matchedParent);
 					((ConflictingDiffElement)operation).setOriginElement(matchedAncestor);
 
 					final RemoveModelElement removeOperation = DiffFactory.eINSTANCE
 							.createRemoveModelElement();
 					removeOperation.setLeftElement(element);
 					removeOperation.setRightParent(matchedParent);
 
 					operation.getSubDiffElements().add(removeOperation);
 				} else {
 					operation = DiffFactory.eINSTANCE.createRemoveModelElement();
 					((RemoveModelElement)operation).setLeftElement(element);
 					((RemoveModelElement)operation).setRightParent(matchedParent);
 				}
 
 				addInContainerPackage(diffRoot, operation, matchedParent);
 			} else {
 				final AddModelElement addOperation = DiffFactory.eINSTANCE.createAddModelElement();
 				addOperation.setRightElement(element);
 				addOperation.setLeftParent(matchedParent);
 				addInContainerPackage(diffRoot, addOperation, matchedParent);
 			}
 		}
 	}
 
 	/**
 	 * Determines if we should ignore an attribute for diff detection.
 	 * <p>
 	 * Default is to ignore attributes marked either
 	 * <ul>
 	 * <li>Transient</li>
 	 * <li>Derived</li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * Clients should override this if they wish to ignore other attributes.
 	 * </p>
 	 * 
 	 * @param attribute
 	 *            Attribute to determine whether it should be ignored.
 	 * @return <code>True</code> if attribute has to be ignored, <code>False</code> otherwise.
 	 */
 	protected boolean shouldBeIgnored(EAttribute attribute) {
 		boolean ignore = attribute.isTransient();
 		ignore |= attribute.isDerived();
 		return ignore;
 	}
 
 	/**
 	 * Determines if we should ignore a reference for diff detection.
 	 * <p>
 	 * Default is to ignore references marked either
 	 * <ul>
 	 * <li>Containment</li>
 	 * <li>Container</li>
 	 * <li>Transient</li>
 	 * <li>Derived</li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * Clients should override this if they wish to ignore other references.
 	 * </p>
 	 * 
 	 * @param reference
 	 *            Reference to determine whether it should be ignored.
 	 * @return <code>True</code> if reference has to be ignored, <code>False</code> otherwise.
 	 */
 	protected boolean shouldBeIgnored(EReference reference) {
 		boolean ignore = reference.isContainment();
 		ignore |= reference.isDerived();
 		ignore |= reference.isTransient();
 		ignore |= reference.isContainer();
 		return ignore;
 	}
 
 	/**
 	 * Builds a {@link DiffGroup} for the <code>targetParent</code> with its full hierarchy.
 	 * 
 	 * @param targetParent
 	 *            Parent of the operation we're building a {@link DiffGroup} for.
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffModel}.
 	 * @return {@link DiffGroup} containing the full hierarchy needed for the <code>targetParent</code>.
 	 */
 	private DiffGroup buildHierarchyGroup(EObject targetParent, DiffGroup root) {
 		// if targetElement has a parent, we call buildgroup on it, else we add
 		// the current group to the root
 		DiffGroup curGroup = DiffFactory.eINSTANCE.createDiffGroup();
 		curGroup.setLeftParent(targetParent);
 		final DiffGroup targetGroup = findExistingGroup(root, targetParent);
 		if (targetGroup != null)
 			curGroup = targetGroup;
 		if (targetParent.eContainer() == null) {
 			root.getSubDiffElements().add(curGroup);
 			return curGroup;
 		}
 		buildHierarchyGroup(targetParent.eContainer(), root).getSubDiffElements().add(curGroup);
 		return curGroup;
 	}
 
 	/**
 	 * Checks if there are conflictual changes between the values of the given {@link EAttribute}.<br/>
 	 * <p>
 	 * An attribute update is considered &quot;conflictual&quot; if it isn't multi-valued and its left (latest
 	 * from head) value differs from the right (working copy) value.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if there actually are
 	 *            conflictual changes in the mapped elements <code>attribute</code> values.
 	 * @param attribute
 	 *            Target {@link EAttribute} to check.
 	 * @param mapping
 	 *            Contains the three (ancestor, left, right) elements' mapping.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch <code>attribute</code>'s values for either one of the mapped
 	 *             elements.
 	 */
 	private void checkConflictingAttributesUpdate(DiffGroup root, EAttribute attribute, Match3Element mapping)
 			throws FactoryException {
 		if (!attribute.isMany()) {
 			createConflictingAttributeChange(root, attribute, mapping);
 		} else {
 			final List<?> leftValue = EFactory.eGetAsList(mapping.getLeftElement(), attribute.getName());
 			final List<?> rightValue = EFactory.eGetAsList(mapping.getRightElement(), attribute.getName());
 			final List<?> ancestorValue = EFactory
 					.eGetAsList(mapping.getOriginElement(), attribute.getName());
 
 			for (Object aValue : leftValue) {
 				// If an object from the left is neither in the right nor in the
 				// origin, it's a remotely added
 				// attribute
 				if (!rightValue.contains(aValue) && !ancestorValue.contains(aValue)) {
 					final RemoteAddAttribute operation = DiffFactory.eINSTANCE.createRemoteAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setLeftTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 					// If the object from the left is not in the right values,
 					// it's been removed since last
 					// checkout
 				} else if (!rightValue.contains(aValue)) {
 					final RemoveAttribute operation = DiffFactory.eINSTANCE.createRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setLeftTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 			for (Object aValue : rightValue) {
 				// if an object from the right is neither in the left nor in the
 				// origin, it's been added since
 				// last checkout
 				if (!leftValue.contains(aValue) && !ancestorValue.contains(aValue)) {
 					final AddAttribute operation = DiffFactory.eINSTANCE.createAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 					// if the object from the right is not in the left values
 					// yet present in the origin, it's
 					// been removed remotely
 				} else if (!leftValue.contains(aValue)) {
 					final RemoteRemoveAttribute operation = DiffFactory.eINSTANCE
 							.createRemoteRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will create and populate a {@link List} with all the references from the
 	 * <code>rightReferences</code> {@link List} that cannot be matched in the <code>leftReferences</code>
 	 * {@link List}.
 	 * 
 	 * @param leftReferences
 	 *            List of the left element reference values.
 	 * @param rightReferences
 	 *            List of the right element reference values.
 	 * @return {@link List} of all the references that have been added in the right element since the left
 	 *         element.
 	 */
 	private List<EObject> computeAddedReferences(List<EObject> leftReferences, List<EObject> rightReferences) {
 		final List<EObject> deletedReferences = new ArrayList<EObject>();
 		final List<EObject> addedReferences = new ArrayList<EObject>();
 
 		if (leftReferences != null)
 			deletedReferences.addAll(leftReferences);
 		if (rightReferences != null)
 			addedReferences.addAll(rightReferences);
 		final List<EObject> matchedOldReferences = getMatchedReferences(deletedReferences);
 
 		// "Added" references are the references from the left element that
 		// can't be mapped
 		addedReferences.removeAll(matchedOldReferences);
 
 		// Double check for objects defined in a different model and thus not
 		// matched
 		// We'll use a new list to keep track of theses elements !avoid
 		// concurrent modification!
 		final List<EObject> remoteMatchedElements = new ArrayList<EObject>();
 		for (EObject deleted : deletedReferences) {
 			if (addedReferences.contains(deleted)) {
 				remoteMatchedElements.add(deleted);
 			}
 		}
 		addedReferences.removeAll(remoteMatchedElements);
 
 		return addedReferences;
 	}
 
 	/**
 	 * This will create and populate a {@link List} with all the references from the
 	 * <code>leftReferences</code> {@link List} that cannot be matched in the <code>rightReferences</code>
 	 * {@link List}.
 	 * 
 	 * @param leftReferences
 	 *            List of the left element reference values.
 	 * @param rightReferences
 	 *            List of the right element reference values.
 	 * @return {@link List} of all the references that have been deleted from the right element since the left
 	 *         element.
 	 */
 	private List<EObject> computeDeletedReferences(List<EObject> leftReferences, List<EObject> rightReferences) {
 		final List<EObject> deletedReferences = new ArrayList<EObject>();
 		final List<EObject> addedReferences = new ArrayList<EObject>();
 
 		if (leftReferences != null)
 			deletedReferences.addAll(leftReferences);
 		if (rightReferences != null)
 			addedReferences.addAll(rightReferences);
 		final List<EObject> matchedNewReferences = getMatchedReferences(addedReferences);
 
 		// "deleted" references are the references from the right element that
 		// can't be mapped
 		deletedReferences.removeAll(matchedNewReferences);
 
 		// Double check for objects defined in a different model and thus not
 		// matched
 		// We'll use a new list to keep track of theses elements !avoid
 		// concurrent modification!
 		final List<EObject> remoteMatchedElements = new ArrayList<EObject>();
 		for (EObject deleted : deletedReferences) {
 			if (addedReferences.contains(deleted)) {
 				remoteMatchedElements.add(deleted);
 			}
 		}
 		deletedReferences.removeAll(remoteMatchedElements);
 
 		return deletedReferences;
 	}
 
 	/**
 	 * This will create the {@link ConflictingDiffGroup} and its children for a conflictual
 	 * {@link AttributeChange}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param attribute
 	 *            Attribute which has been changed to conflictual values.
 	 * @param mapping
 	 *            Contains informations about the left, right and origin element.
 	 * @throws FactoryException
 	 *             Thrown if we cannot create the {@link ConflictingDiffGroup}'s children.
 	 */
 	private void createConflictingAttributeChange(DiffGroup root, EAttribute attribute, Match3Element mapping)
 			throws FactoryException {
 		// We'll use this diffGroup to make use of
 		// #createNonConflictingAttributeChange(DiffGroup, EAttribute, EObject,
 		// EObject)
 		final DiffGroup dummyGroup = DiffFactory.eINSTANCE.createDiffGroup();
 		createNonConflictingAttributeChange(dummyGroup, attribute, mapping.getLeftElement(), mapping
 				.getRightElement());
 
 		if (dummyGroup.getSubDiffElements().size() > 0) {
 			final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 					.createConflictingDiffElement();
 			conflictingDiff.setLeftParent(mapping.getLeftElement());
 			conflictingDiff.setRightParent(mapping.getRightElement());
 			conflictingDiff.setOriginElement(mapping.getOriginElement());
 			// Dummy DiffGroup should have a single change
 			conflictingDiff.getSubDiffElements().add(dummyGroup.getSubDiffElements().get(0));
 			root.getSubDiffElements().add(conflictingDiff);
 		}
 	}
 
 	/**
 	 * This will create the {@link ConflictingDiffGroup} and its children for a conflictual
 	 * {@link ReferenceChange}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement} to create.
 	 * @param reference
 	 *            Target {@link EReference} of the modification.
 	 * @param mapping
 	 *            Contains informations about the left, right and origin element where the given reference has
 	 *            changed.
 	 * @throws FactoryException
 	 *             Thrown if we cannot create the underlying {@link ReferenceChange}s.
 	 */
 	private void createConflictingReferenceUpdate(DiffGroup root, EReference reference, Match3Element mapping)
 			throws FactoryException {
 		// We'll use this diffGroup to make use of
 		// #createNonConflictingAttributeChange(DiffGroup, EAttribute, EObject,
 		// EObject)
 		final DiffGroup dummyGroup = DiffFactory.eINSTANCE.createDiffGroup();
 		createNonConflictingReferencesUpdate(dummyGroup, reference, mapping.getLeftElement(), mapping
 				.getRightElement());
 
 		if (dummyGroup.getSubDiffElements().size() > 0) {
 			final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 					.createConflictingDiffElement();
 			conflictingDiff.setLeftParent(mapping.getLeftElement());
 			conflictingDiff.setRightParent(mapping.getRightElement());
 			conflictingDiff.setOriginElement(mapping.getOriginElement());
 			// Dummy DiffGroup should have a single change
 			conflictingDiff.getSubDiffElements().add(dummyGroup.getSubDiffElements().get(0));
 			root.getSubDiffElements().add(conflictingDiff);
 		}
 	}
 
 	/**
 	 * This will create the {@link MoveModelElement} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element of the left model corresponding to the right one.
 	 * @param right
 	 *            Element that has been moved since the last (ancestor for three-way comparison, left for
 	 *            two-way comparison) version.
 	 */
 	private void createMoveOperation(DiffGroup root, EObject left, EObject right) {
 		final MoveModelElement operation = DiffFactory.eINSTANCE.createMoveModelElement();
 		operation.setRightElement(right);
 		operation.setLeftElement(left);
 		operation.setRightTarget(getMatchedEObject(left.eContainer()));
 		operation.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(operation);
 	}
 
 	/**
 	 * Creates the {@link DiffGroup} corresponding to a reference's value addition under the given
 	 * {@link DiffGroup}.<br/>The parameters include the list of added references which can be computed
 	 * using {@link #computeAddedReferences(List, List)}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement}s to create.
 	 * @param left
 	 *            Left element of the reference change.
 	 * @param right
 	 *            Right element of the reference change.
 	 * @param reference
 	 *            {@link EReference} target of the operation.
 	 * @param addedReferences
 	 *            {@link List} of reference values that have been added in the <code>right</code> element
 	 *            since the <code>left</code> element.
 	 */
 	private void createNewReferencesOperation(DiffGroup root, EObject left, EObject right,
 			EReference reference, List<EObject> addedReferences) {
 		for (final Iterator<EObject> addedReferenceIterator = addedReferences.iterator(); addedReferenceIterator
 				.hasNext(); ) {
 			final EObject eobj = addedReferenceIterator.next();
 			final AddReferenceValue addOperation = DiffFactory.eINSTANCE.createAddReferenceValue();
 			addOperation.setRightElement(right);
 			addOperation.setLeftElement(left);
 			addOperation.setReference(reference);
 			addOperation.setRightAddedTarget(eobj);
 			if (getMatchedEObject(eobj) != null)
 				addOperation.setLeftAddedTarget(getMatchedEObject(eobj));
 			root.getSubDiffElements().add(addOperation);
 		}
 	}
 
 	/**
 	 * Creates and add the {@link DiffGroup} corresponding to an {@link AttributeChange} operation to the
 	 * given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param attribute
 	 *            Attribute which value has been changed.
 	 * @param leftElement
 	 *            Left element of the attribute change.
 	 * @param rightElement
 	 *            Right element of the attribute change.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the attribute's value for either one of the elements.
 	 */
 	private void createNonConflictingAttributeChange(DiffGroup root, EAttribute attribute,
 			EObject leftElement, EObject rightElement) throws FactoryException {
 		if (attribute.isMany()) {
 			final List<?> leftValue = EFactory.eGetAsList(leftElement, attribute.getName());
 			final List<?> rightValue = EFactory.eGetAsList(rightElement, attribute.getName());
 			for (Object aValue : leftValue) {
 				if (!rightValue.contains(aValue) && aValue instanceof EObject) {
 					final RemoveAttribute operation = DiffFactory.eINSTANCE.createRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(rightElement);
 					operation.setLeftElement(leftElement);
 					operation.setLeftTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 			for (Object aValue : rightValue) {
 				if (!leftValue.contains(aValue) && aValue instanceof EObject) {
 					final AddAttribute operation = DiffFactory.eINSTANCE.createAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(rightElement);
 					operation.setLeftElement(leftElement);
 					operation.setRightTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 		} else {
 			final UpdateAttribute operation = DiffFactory.eINSTANCE.createUpdateAttribute();
 			operation.setRightElement(rightElement);
 			operation.setLeftElement(leftElement);
 			operation.setAttribute(attribute);
 			root.getSubDiffElements().add(operation);
 		}
 	}
 
 	/**
 	 * This will check the given <code>reference</code> for modification between <code>leftElement</code>
 	 * and <code>rightElement</code> and create the corresponding {@link DiffElement}s under the given
 	 * {@link DiffGroup}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement}s to create.
 	 * @param reference
 	 *            {@link EReference} to check for modifications.
 	 * @param leftElement
 	 *            Element corresponding to the initial value for the given reference.
 	 * @param rightElement
 	 *            Element corresponding to the final value for the given reference.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch <code>reference</code>'s values for either the left or the
 	 *             right element.
 	 */
 	@SuppressWarnings("unchecked")
 	private void createNonConflictingReferencesUpdate(DiffGroup root, EReference reference,
 			EObject leftElement, EObject rightElement) throws FactoryException {
 		final List<EObject> leftElementReferences = (List<EObject>)EFactory.eGetAsList(leftElement, reference
 				.getName());
 		final List<EObject> rightElementReferences = (List<EObject>)EFactory.eGetAsList(rightElement,
 				reference.getName());
 
 		final List<EObject> deletedReferences = computeDeletedReferences(leftElementReferences,
 				rightElementReferences);
 		final List<EObject> addedReferences = computeAddedReferences(leftElementReferences,
 				rightElementReferences);
 
 		// REFERENCES UPDATES
 		if (!reference.isMany() && addedReferences.size() > 0 && deletedReferences.size() > 0) {
 			/*
 			 * If neither the left nor the right target are proxies, or if their target URIs are distinct,
 			 * this is a reference update. Otherwise, we are here because we haven't been able to resolve the
 			 * proxy.
 			 */
 			if (!addedReferences.get(0).eIsProxy()
 					|| !deletedReferences.get(0).eIsProxy()
 					|| !EcoreUtil.getURI(addedReferences.get(0)).equals(
 							EcoreUtil.getURI(deletedReferences.get(0)))) {
 				root.getSubDiffElements().add(
 						createUpdatedReferencesOperation(leftElement, rightElement, reference,
 								addedReferences, deletedReferences));
 			}
 		} else {
 			// REFERENCES ADD
 			if (addedReferences.size() > 0) {
 				createNewReferencesOperation(root, leftElement, rightElement, reference, addedReferences);
 			}
 			// REFERENCES DEL
 			if (deletedReferences.size() > 0) {
 				createRemovedReferencesOperation(root, leftElement, rightElement, reference,
 						deletedReferences);
 			}
 		}
 	}
 
 	/**
 	 * This will create the needed remote attribute change {@link DiffElement} under the given
 	 * {@link DiffGroup root}.<br/>An attribute is &quot;remotely changed&quot; if it has been added,
 	 * updated or deleted in the left (latest from head) version but it has kept its former value in the right
 	 * (working copy) version.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param attribute
 	 *            Target {@link EAttribute} of the update.
 	 * @param mapping
 	 *            Contains the three (ancestor, left, right) elements' mapping.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch <code>attribute</code>'s left and right values.
 	 */
 	private void createRemoteAttributeChange(DiffGroup root, EAttribute attribute, Match3Element mapping)
 			throws FactoryException {
 		if (attribute.isMany()) {
 			final List<?> leftValue = EFactory.eGetAsList(mapping.getLeftElement(), attribute.getName());
 			final List<?> rightValue = EFactory.eGetAsList(mapping.getRightElement(), attribute.getName());
 			for (Object aValue : leftValue) {
 				// if the value is present in the left (latest) but not in the
 				// right (working copy), it's been
 				// added remotely
 				if (!rightValue.contains(aValue)) {
 					final RemoteAddAttribute operation = DiffFactory.eINSTANCE.createRemoteAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setLeftTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 			for (Object aValue : rightValue) {
 				// if the value is present in the right (working copy) but not
 				// in the left (latest), it's been
 				// removed remotely
 				if (!leftValue.contains(aValue)) {
 					final RemoteRemoveAttribute operation = DiffFactory.eINSTANCE
 							.createRemoteRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightTarget((EObject)aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 		} else {
 			final UpdateAttribute operation = DiffFactory.eINSTANCE.createRemoteUpdateAttribute();
 			operation.setRightElement(mapping.getRightElement());
 			operation.setLeftElement(mapping.getLeftElement());
 			operation.setAttribute(attribute);
 			root.getSubDiffElements().add(operation);
 		}
 	}
 
 	/**
 	 * This will create the {@link RemoteMoveModelElement} under the given {@link DiffGroup root}.<br/>A
 	 * {@link RemoteMoveModelElement} represents the fact that an element has been remotely moved since the
 	 * ancestor model, but the right model has kept the element in its former place.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element that has been moved in the left model.
 	 * @param right
 	 *            Element of the right model corresponding to the left one.
 	 */
 	private void createRemoteMoveOperation(DiffGroup root, EObject left, EObject right) {
 		final RemoteMoveModelElement operation = DiffFactory.eINSTANCE.createRemoteMoveModelElement();
 		operation.setRightElement(right);
 		operation.setLeftElement(left);
 		operation.setRightTarget(getMatchedEObject(left.eContainer()));
 		operation.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(operation);
 	}
 
 	/**
 	 * This will check for remote {@link ReferenceChange} operations and create the corresponding
 	 * {@link DiffElement}s.<br/>
 	 * <p>
 	 * A reference is considered &quot;remotely changed&quot; if its values differ between the left (latest
 	 * from HEAD) and origin (common ancestor) model, but its values haven't changed between the right
 	 * (working copy) and the origin model.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement}s to create.
 	 * @param reference
 	 *            {@link EReference} to check for {@link ReferenceChange}s.
 	 * @param mapping
 	 *            Contains informations about the left, right and original model elements.
 	 * @param remotelyAdded
 	 *            {@link List} of reference values that have been added in the left model since the origin.
 	 * @param remotelyDeleted
 	 *            {@link List} of reference values that have been removed from the left model since the
 	 *            origin.
 	 */
 	private void createRemoteReferencesUpdate(DiffGroup root, EReference reference, Match3Element mapping,
 			List<EObject> remotelyAdded, List<EObject> remotelyDeleted) {
 		if (!reference.isMany() && remotelyAdded.size() > 0 && remotelyDeleted.size() > 0) {
 			final UpdateUniqueReferenceValue operation = DiffFactory.eINSTANCE
 					.createRemoteUpdateUniqueReferenceValue();
 			operation.setLeftElement(mapping.getLeftElement());
 			operation.setRightElement(mapping.getRightElement());
 			operation.setReference(reference);
 
 			EObject leftTarget = getMatchedEObject(remotelyAdded.get(0));
 			EObject rightTarget = getMatchedEObject(remotelyDeleted.get(0));
 			// checks if target are defined remotely
 			if (leftTarget == null)
 				leftTarget = remotelyAdded.get(0);
 			if (rightTarget == null)
 				rightTarget = remotelyDeleted.get(0);
 
 			operation.setLeftTarget(leftTarget);
 			operation.setRightTarget(rightTarget);
 
 			root.getSubDiffElements().add(operation);
 		} else {
 			for (final Iterator<EObject> addedReferenceIterator = remotelyAdded.iterator(); addedReferenceIterator
 					.hasNext(); ) {
 				final EObject eobj = addedReferenceIterator.next();
 				final RemoteAddReferenceValue addOperation = DiffFactory.eINSTANCE
 						.createRemoteAddReferenceValue();
 				addOperation.setRightElement(mapping.getRightElement());
 				addOperation.setLeftElement(mapping.getLeftElement());
 				addOperation.setReference(reference);
 				addOperation.setLeftRemovedTarget(eobj);
 				if ((getMatchedEObject(eobj)) != null)
 					addOperation.setRightRemovedTarget(getMatchedEObject(eobj));
 				root.getSubDiffElements().add(addOperation);
 			}
 			for (final Iterator<EObject> deletedReferenceIterator = remotelyDeleted.iterator(); deletedReferenceIterator
 					.hasNext(); ) {
 				final EObject eobj = deletedReferenceIterator.next();
 				final RemoteRemoveReferenceValue delOperation = DiffFactory.eINSTANCE
 						.createRemoteRemoveReferenceValue();
 				delOperation.setRightElement(mapping.getRightElement());
 				delOperation.setLeftElement(mapping.getLeftElement());
 				delOperation.setReference(reference);
 				delOperation.setRightAddedTarget(eobj);
 				if ((getMatchedEObject(eobj)) != null)
 					delOperation.setLeftAddedTarget(getMatchedEObject(eobj));
 				root.getSubDiffElements().add(delOperation);
 			}
 		}
 	}
 
 	/**
 	 * Creates the {@link DiffGroup} corresponding to a reference's value removal under the given
 	 * {@link DiffGroup}.<br/>The parameters include the list of removed references which can be computed
 	 * using {@link #computeDeletedReferences(List, List)}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement}s to create.
 	 * @param left
 	 *            Left element of the reference change.
 	 * @param right
 	 *            Right element of the reference change.
 	 * @param reference
 	 *            {@link EReference} target of the operation.
 	 * @param deletedReferences
 	 *            {@link List} of reference values that have been removed in the <code>right</code> element
 	 *            since the <code>left</code> element.
 	 */
 	private void createRemovedReferencesOperation(DiffGroup root, EObject left, EObject right,
 			EReference reference, List<EObject> deletedReferences) {
 		for (final Iterator<EObject> deletedReferenceIterator = deletedReferences.iterator(); deletedReferenceIterator
 				.hasNext(); ) {
 			final EObject eobj = deletedReferenceIterator.next();
 			final RemoveReferenceValue delOperation = DiffFactory.eINSTANCE.createRemoveReferenceValue();
 			delOperation.setRightElement(right);
 			delOperation.setLeftElement(left);
 			delOperation.setReference(reference);
 			delOperation.setLeftRemovedTarget(eobj);
 			if (getMatchedEObject(eobj) != null)
 				delOperation.setRightRemovedTarget(getMatchedEObject(eobj));
 			root.getSubDiffElements().add(delOperation);
 		}
 	}
 
 	/**
 	 * Creates the {@link DiffElement} corresponding to an unique reference's value update.<br/>The
 	 * parameters include the lists of added and removed references, these can be computed using
 	 * {@link #computeAddedReferences(List, List)} and {@link #computeDeletedReferences(List, List)}.
 	 * 
 	 * @param left
 	 *            Left element of the reference change.
 	 * @param right
 	 *            Right element of the reference change.
 	 * @param reference
 	 *            {@link EReference} target of the operation.
 	 * @param deletedReferences
 	 *            {@link List} of reference values that have been removed in the <code>right</code> element
 	 *            since the <code>left</code> element.
 	 * @param addedReferences
 	 *            {@link List} of reference values that have been added in the <code>right</code> element
 	 *            since the <code>left</code> element.
 	 * @return The {@link DiffElement} corresponding to an unique reference's value update
 	 */
 	private UpdateUniqueReferenceValue createUpdatedReferencesOperation(EObject left, EObject right,
 			EReference reference, List<EObject> addedReferences, List<EObject> deletedReferences) {
 		final UpdateUniqueReferenceValue operation = DiffFactory.eINSTANCE.createUpdateUniqueReferenceValue();
 		operation.setLeftElement(left);
 		operation.setRightElement(right);
 		operation.setReference(reference);
 
 		EObject leftTarget = getMatchedEObject(addedReferences.get(0));
 		EObject rightTarget = getMatchedEObject(deletedReferences.get(0));
 		// checks if target are defined remotely
 		if (leftTarget == null)
 			leftTarget = addedReferences.get(0);
 		if (rightTarget == null)
 			rightTarget = deletedReferences.get(0);
 
 		operation.setLeftTarget(leftTarget);
 		operation.setRightTarget(rightTarget);
 
 		return operation;
 	}
 
 	/**
 	 * This is the core of the diff computing for two way comparison. This will call for checks on attributes,
 	 * references and model elements to check for updates/changes.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffModel} to create.
 	 * @param match
 	 *            {@link Match3Element root} of the {@link MatchModel} to analyze.
 	 */
 	private void doDiffDelegate(DiffGroup root, Match2Elements match) {
 		DiffGroup current = DiffFactory.eINSTANCE.createDiffGroup();
 		current.setLeftParent(match.getLeftElement());
 		try {
 			checkForDiffs(current, match);
 		} catch (FactoryException e) {
 			EMFComparePlugin.log(e, false);
 		}
 		// we need to build this list to avoid concurrent modifications
 		final List<DiffElement> shouldAddToList = new ArrayList<DiffElement>();
 		// we really have changes
 		if (current.getSubDiffElements().size() > 0) {
 			final Iterator<DiffElement> it2 = current.getSubDiffElements().iterator();
 			while (it2.hasNext()) {
 				final DiffElement diff = it2.next();
 				if (!(diff instanceof DiffGroup)) {
 					shouldAddToList.add(diff);
 				}
 			}
 			for (DiffElement diff : shouldAddToList) {
 				addInContainerPackage(root, diff, current.getLeftParent());
 			}
 		} else {
 			current = root;
 		}
 		// taking care of our childs
 		final Iterator<MatchElement> it = match.getSubMatchElements().iterator();
 		while (it.hasNext()) {
 			final Match2Elements element = (Match2Elements)it.next();
 			doDiffDelegate(root, element);
 		}
 	}
 
 	/**
 	 * Searches for an existing {@link DiffGroup} under <code>root</code> to add the operation which parent
 	 * is <code>targetParent</code>.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffModel}.
 	 * @param targetParent
 	 *            Parent of the operation we're seeking a {@link DiffGroup} for.
 	 * @return {@link DiffGroup} for the <code>targetParent</code>.
 	 */
 	private DiffGroup findExistingGroup(DiffGroup root, EObject targetParent) {
 		final TreeIterator<EObject> it = root.eAllContents();
 		while (it.hasNext()) {
 			final EObject obj = it.next();
 			if (obj instanceof DiffGroup) {
 				if (((DiffGroup)obj).getLeftParent() == targetParent) {
 					return (DiffGroup)obj;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the list of references from the given list that can be matched on either right or left
 	 * {@link EObject}s.
 	 * 
 	 * @param references
 	 *            {@link List} of the references to match.
 	 * @return The list of references from the given list that can be matched on either right or left
 	 *         {@link EObject}s.
 	 */
 	private List<EObject> getMatchedReferences(List<EObject> references) {
 		final List<EObject> matchedReferences = new ArrayList<EObject>();
 		for (final Iterator<EObject> refIterator = references.iterator(); refIterator.hasNext(); ) {
 			final Object currentReference = refIterator.next();
 			if (currentReference != null) {
 				final EObject currentMapped = getMatchedEObject((EObject)currentReference);
 				if (currentMapped != null)
 					matchedReferences.add(currentMapped);
 			}
 		}
 		return matchedReferences;
 	}
 
 	/**
 	 * Checks if the values of a given reference have been changed both on the left (latest from head) and
 	 * right (working copy) to distinct values since the origin.
 	 * 
 	 * @param reference
 	 *            Reference we're checking for conflictual changes.
 	 * @param leftReferences
 	 *            {@link List} of values from the left (latest from head) model for <code>reference</code>.
 	 * @param rightReferences
 	 *            {@link List} of values from the right (working copy) model for <code>reference</code>.
 	 * @param ancestorReferences
 	 *            {@link List} of values from the origin (common ancestor) model for <code>reference</code>.
 	 * @return <code>True</code> if there's been a conflictual change for the given {@link EReference},
 	 *         <code>False</code> otherwise.
 	 */
 	private boolean isConflictual(EReference reference, List<?> leftReferences, List<?> rightReferences,
 			List<?> ancestorReferences) {
 		boolean isConflictual = false;
 		// There CAN be a conflict ONLY if the reference is unique
 		if (!reference.isMany()) {
 			// If both left and right number of values have changed since origin...
 			if (leftReferences.size() != ancestorReferences.size()
 					&& rightReferences.size() != ancestorReferences.size()) {
 				// ... There is a conflict if the value hasn't been erased AND
 				// the left value is different than the right one
 				if (leftReferences.size() > 0
 						&& !leftReferences.get(0).equals(getMatchedEObject((EObject)rightReferences.get(0)))) {
 					isConflictual = true;
 				}
 				// If the number of values hasn't changed since origin, there
 				// cannot be a conflict if there are no values
 			} else if (leftReferences.size() > 0 && rightReferences.size() > 0) {
 				// There's a conflict in the values are all distinct
 				if (!leftReferences.get(0).equals(
 						getMatchedEObject((EObject)ancestorReferences.get(0), LEFT_OBJECT))
 						&& !rightReferences.get(0).equals(
 								getMatchedEObject((EObject)ancestorReferences.get(0), RIGHT_OBJECT))
 						&& !rightReferences.get(0).equals(getMatchedEObject((EObject)leftReferences.get(0)))) {
 					isConflictual = true;
 				}
 			}
 		}
 		return isConflictual;
 	}
 
 	/**
 	 * Checks a given {@link EReference reference} for changes related to a given <code>mapping</code> and
 	 * populates the given {@link List}s with the reference values belonging to them.
 	 * 
 	 * @param mapping
 	 *            Contains informations about the left, right and origin elements.<br/>
 	 *            <ul>
 	 *            <li>&quot;Added&quot; values are the values that have been added in the right element since
 	 *            the origin and that haven't been added in the left element.</li>
 	 *            <li>&quot;Deleted&quot; values are the values that have been removed from the right element
 	 *            since the origin but are still present in the left element.</li>
 	 *            <li>&quot;Remotely added&quot; values are the values that have been added in the left
 	 *            element since the origin but haven't been added in the right element.</li>
 	 *            <li>&quot;Remotely deleted&quot; values are the values that have been removed from the left
 	 *            element since the origin but are still present in the right element.</li>
 	 *            </ul>
 	 * @param reference
 	 *            {@link EReference} we're checking for changes.
 	 * @param addedReferences
 	 *            {@link List} that will be populated with the values that have been added in the right
 	 *            element since the origin.
 	 * @param deletedReferences
 	 *            {@link List} that will be populated with the values that have been removed from the right
 	 *            element since the origin.
 	 * @param remoteAddedReferences
 	 *            {@link List} that will be populated with the values that have been added in the left element
 	 *            since the origin.
 	 * @param remoteDeletedReferences
 	 *            {@link List} that will be populated with the values that have been removed from the left
 	 *            element since the origin.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the reference's values in either the left, right or origin
 	 *             element.
 	 */
 	private void populateThreeWayReferencesChanges(Match3Element mapping, EReference reference,
 			List<EObject> addedReferences, List<EObject> deletedReferences,
 			List<EObject> remoteAddedReferences, List<EObject> remoteDeletedReferences)
 			throws FactoryException {
 		final String referenceName = reference.getName();
 		final List<?> leftReferences = EFactory.eGetAsList(mapping.getLeftElement(), referenceName);
 		final List<?> rightReferences = EFactory.eGetAsList(mapping.getRightElement(), referenceName);
 		final List<?> ancestorReferences = EFactory.eGetAsList(mapping.getOriginElement(), referenceName);
 
 		// populates remotely added references list
 		for (Object left : leftReferences) {
 			if (left instanceof EObject
 					&& !ancestorReferences.contains(getMatchedEObject((EObject)left, ANCESTOR_OBJECT))
 					&& !rightReferences.contains(getMatchedEObject((EObject)left))) {
 				remoteAddedReferences.add((EObject)left);
 			}
 		}
 		// populates localy added list
 		for (Object right : rightReferences) {
 			if (right instanceof EObject
 					&& !ancestorReferences.contains(getMatchedEObject((EObject)right, ANCESTOR_OBJECT))
 					&& !leftReferences.contains(getMatchedEObject((EObject)right))) {
 				addedReferences.add((EObject)right);
 			}
 		}
 		// populates remotely deleted and localy added lists
 		for (Object origin : ancestorReferences) {
 			if (origin instanceof EObject
 					&& !leftReferences.contains(getMatchedEObject((EObject)origin, LEFT_OBJECT))
 					&& rightReferences.contains(getMatchedEObject((EObject)origin, RIGHT_OBJECT))) {
 				remoteDeletedReferences.add((EObject)origin);
 			} else if (origin instanceof EObject
 					&& !rightReferences.contains(getMatchedEObject((EObject)origin, RIGHT_OBJECT))
 					&& leftReferences.contains(getMatchedEObject((EObject)origin, LEFT_OBJECT))) {
 				deletedReferences.add((EObject)origin);
 			}
 		}
 	}
 
 	/**
 	 * Fill the <code>eObjectToMatch</code> map to retrieve matchings from left, right or origin
 	 * {@link EObject}.
 	 * 
 	 * @param match
 	 *            {@link MatchModel} to extract the {@link MatchElement}s from.
 	 * @param threeWay
 	 *            <code>True</code> if we need to retrieve the informations from the origin model too.
 	 */
 	private void updateEObjectToMatch(MatchModel match, boolean threeWay) {
 		final Iterator<MatchElement> rootElemIt = match.getMatchedElements().iterator();
 		while (rootElemIt.hasNext()) {
 			final Match2Elements matchRoot = (Match2Elements)rootElemIt.next();
 			eObjectToMatch.put(matchRoot.getLeftElement(), matchRoot);
 			eObjectToMatch.put(matchRoot.getRightElement(), matchRoot);
 			if (threeWay)
 				eObjectToMatch.put(((Match3Element)matchRoot).getOriginElement(), matchRoot);
 			final TreeIterator<EObject> matchElemIt = matchRoot.eAllContents();
 			while (matchElemIt.hasNext()) {
 				final Match2Elements matchElem = (Match2Elements)matchElemIt.next();
 				eObjectToMatch.put(matchElem.getLeftElement(), matchElem);
 				eObjectToMatch.put(matchElem.getRightElement(), matchElem);
 				if (threeWay && ((Match3Element)matchElem).getOriginElement() != null)
 					eObjectToMatch.put(((Match3Element)matchElem).getOriginElement(), matchElem);
 			}
 		}
 	}
 }
