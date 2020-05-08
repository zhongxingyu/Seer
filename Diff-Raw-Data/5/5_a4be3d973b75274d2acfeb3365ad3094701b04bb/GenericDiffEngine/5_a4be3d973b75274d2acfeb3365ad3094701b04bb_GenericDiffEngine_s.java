 /*******************************************************************************
  * Copyright (c) 2006, 2007, 2008 Obeo.
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
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.diff.EMFCompareDiffMessages;
 import org.eclipse.emf.compare.diff.api.IDiffEngine;
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
 import org.eclipse.emf.compare.diff.metamodel.RemoteUpdateContainmentFeature;
 import org.eclipse.emf.compare.diff.metamodel.RemoveAttribute;
 import org.eclipse.emf.compare.diff.metamodel.RemoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.RemoveReferenceValue;
 import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
 import org.eclipse.emf.compare.diff.metamodel.UpdateContainmentFeature;
 import org.eclipse.emf.compare.diff.metamodel.UpdateUniqueReferenceValue;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchElement;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.RemoteUnmatchElement;
 import org.eclipse.emf.compare.match.metamodel.UnmatchElement;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMap.Entry;
 
 /**
  * This class is useful when one wants to determine a diff from a matching model.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
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
 	protected final Map<UnmatchElement, Boolean> unmatchedElements = new EMFCompareMap<UnmatchElement, Boolean>();
 
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
 		if (threeWay) {
 			diffRoot = doDiffThreeWay(match);
 		} else {
 			diffRoot = doDiffTwoWay(match);
 		}
 
 		result.getOwnedElements().add(diffRoot);
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.api.IDiffEngine#reset()
 	 */
 	public void reset() {
 		unmatchedElements.clear();
 		eObjectToMatch.clear();
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
 	 * This will iterate through all the attributes of the <code>mapping</code>'s two elements to check if any
 	 * of them has been modified.
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
 					if (!value1.toString().equals(value2.toString())) {
 						createNonConflictingAttributeChange(root, next, mapping.getLeftElement(), mapping
 								.getRightElement());
 					}
 				} else if ((leftValue != null && !leftValue.equals(rightValue))
 						|| (leftValue == null && leftValue != rightValue)) {
 					createNonConflictingAttributeChange(root, next, mapping.getLeftElement(), mapping
 							.getRightElement());
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will iterate through all the attributes of the <code>mapping</code>'s three elements to check if
 	 * any of them has been modified.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if one of the attribute has
 	 *            actually been changed.
 	 * @param mapping
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails.
 	 */
 	protected void checkAttributesUpdates(DiffGroup root, Match3Elements mapping) throws FactoryException {
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
 
 				// non conflicting change
 				if (leftDistinctFromOrigin && !rightDistinctFromOrigin) {
 					createNonConflictingAttributeChange(root, next, mapping.getLeftElement(), mapping
 							.getRightElement());
 					// only latest from head has changed
 				} else if (rightDistinctFromOrigin && !leftDistinctFromOrigin) {
 					createRemoteAttributeChange(root, next, mapping);
 					// conflicting
 				} else if ((rightDistinctFromOrigin && leftDistinctFromOrigin) || rightDistinctFromLeft) {
 					checkConflictingAttributesUpdate(root, next, mapping);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will check whether the left and right element are contained in the same containment reference and
 	 * create a difference if need be.
 	 * 
 	 * @param current
 	 *            {@link DiffGroup} under which the new differences will be added.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a containment
 	 *            reference update.
 	 */
 	protected void checkContainmentUpdate(DiffGroup current, Match2Elements matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		if (leftElement.eContainmentFeature() != null && rightElement.eContainmentFeature() != null) {
 			if (!leftElement.eContainmentFeature().getName().equals(
 					rightElement.eContainmentFeature().getName())) {
 				createUpdateContainmentOperation(current, leftElement, rightElement);
 			}
 		}
 	}
 
 	/**
 	 * This will check whether the left and right element are contained in the same containment reference and
 	 * create a difference if need be.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup} under which the new differences will be added.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a containment
 	 *            reference update.
 	 */
 	protected void checkContainmentUpdate(DiffGroup root, Match3Elements matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		final EObject originElement = matchElement.getOriginElement();
 		if (leftElement.eContainer() == null && rightElement.eContainer() == null
 				&& originElement.eContainer() == null)
 			return;
 
 		final boolean leftChangedContainment = originElement.eContainmentFeature() != null
 				&& leftElement.eContainmentFeature() != null
 				&& !leftElement.eContainmentFeature().getName().equals(
 						originElement.eContainmentFeature().getName())
 				&& getMatchedEObject(leftElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 		final boolean rightChangedContainment = originElement.eContainmentFeature() != null
 				&& rightElement.eContainmentFeature() != null
 				&& !rightElement.eContainmentFeature().getName().equals(
 						originElement.eContainmentFeature().getName())
 				&& getMatchedEObject(rightElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 
 		// effective change
 		if (getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())
 				&& !leftElement.eContainmentFeature().getName().equals(
 						rightElement.eContainmentFeature().getName())) {
 			// conflicting change
 			if (leftChangedContainment && rightChangedContainment) {
 				final UpdateContainmentFeature updateContainment = DiffFactory.eINSTANCE
 						.createUpdateContainmentFeature();
 				updateContainment.setLeftElement(leftElement);
 				updateContainment.setRightElement(rightElement);
 				updateContainment.setRightTarget(getMatchedEObject(leftElement.eContainer()));
 				updateContainment.setLeftTarget(getMatchedEObject(rightElement.eContainer()));
 
 				final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 						.createConflictingDiffElement();
 				conflictingDiff.setLeftParent(leftElement);
 				conflictingDiff.setRightParent(rightElement);
 				conflictingDiff.setOriginElement(originElement);
 				conflictingDiff.getSubDiffElements().add(updateContainment);
 				root.getSubDiffElements().add(conflictingDiff);
 			} else if (leftChangedContainment) {
 				createUpdateContainmentOperation(root, leftElement, rightElement);
 			} else if (rightChangedContainment) {
 				createRemoteUpdateContainmentOperation(root, leftElement, rightElement);
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
 		checkContainmentUpdate(current, match);
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
 	protected void checkForDiffs(DiffGroup current, Match3Elements match) throws FactoryException {
 		checkAttributesUpdates(current, match);
 		checkReferencesUpdates(current, match);
 		checkMoves(current, match);
 		checkContainmentUpdate(current, match);
 	}
 
 	/**
 	 * This will check if the elements matched by a given {@link Match2Elements} have been moved.
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
 	protected void checkMoves(DiffGroup root, Match3Elements matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		final EObject originElement = matchElement.getOriginElement();
 		if (leftElement.eContainer() == null && rightElement.eContainer() == null
 				&& originElement.eContainer() == null)
 			return;
 
 		final boolean leftMoved = originElement != null
 				&& leftElement.eContainer() != null
 				&& !getMatchedEObject(leftElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 		final boolean rightMoved = originElement != null
 				&& rightElement.eContainer() != null
 				&& !getMatchedEObject(rightElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 
 		// effective change
 		if (!getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())) {
 			// conflicting change
 			if (leftMoved && rightMoved) {
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
 			} else if (rightMoved) {
 				createRemoteMoveOperation(root, leftElement, rightElement);
 			} else if (leftMoved) {
 				createMoveOperation(root, leftElement, rightElement);
 			}
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
 	protected void checkReferencesUpdates(DiffGroup root, Match3Elements mapping) throws FactoryException {
 		// Ignores matchElements when they don't have origin (no updates on these)
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
 
 					if (!next.isMany()) {
 						EObject addedValue = null;
 						EObject deletedValue = null;
 						if (addedReferences.size() > 0) {
 							addedValue = addedReferences.get(0);
 						}
 						if (deletedReferences.size() > 0) {
 							deletedValue = deletedReferences.get(0);
 						}
 
 						// One of the two values is null, reference has been unset
 						if ((addedValue == null || deletedValue == null) && addedValue != deletedValue) {
 							root.getSubDiffElements().add(
 									createUpdatedReferenceOperation(mapping.getLeftElement(), mapping
 											.getRightElement(), next, addedValue, deletedValue));
 						} else if (addedValue != null && deletedValue != null
 								&& !EcoreUtil.getURI(addedValue).equals(EcoreUtil.getURI(deletedValue))) {
 							root.getSubDiffElements().add(
 									createUpdatedReferenceOperation(mapping.getLeftElement(), mapping
 											.getRightElement(), next, addedValue, deletedValue));
 						}
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
 		Resource leftModel = null;
 
 		// It is a possibility that no elements were matched
 		if (match.getMatchedElements().size() > 0) {
 			// we have to browse the model and create the corresponding operations
 			final Match3Elements matchRoot = (Match3Elements)match.getMatchedElements().get(0);
 			leftModel = matchRoot.getLeftElement().eResource();
 
 			doDiffDelegate(diffRoot, matchRoot);
 		}
 
 		unmatchedElements.clear();
 		final Iterator<UnmatchElement> unmatched = match.getUnmatchedElements().iterator();
 		while (unmatched.hasNext()) {
 			final UnmatchElement unmatchElement = unmatched.next();
 			boolean isChild = false;
 			boolean isAncestor = false;
 			for (final Object object : match.getUnmatchedElements()) {
 				if (unmatchElement != ((UnmatchElement)object)) {
 					if (EcoreUtil.isAncestor(unmatchElement.getElement(), ((UnmatchElement)object)
 							.getElement())) {
 						isAncestor = true;
 					}
 					if (EcoreUtil.isAncestor(((UnmatchElement)object).getElement(), unmatchElement
 							.getElement())) {
 						isChild = true;
 					}
 				}
 				if (isChild || isAncestor) {
 					break;
 				}
 			}
 			if (!isChild) {
 				unmatchedElements.put(unmatchElement, isAncestor);
 			}
 		}
 		if (unmatchedElements.size() > 0) {
 			if (leftModel == null) {
 				for (final UnmatchElement element : unmatchedElements.keySet()) {
 					if (element.getElement().eResource() == null) {
 						continue;
 					}
 					if (element.getElement().eResource().getURI().toString().equals(match.getLeftModel())) {
 						leftModel = element.getElement().eResource();
 						break;
 					}
 				}
 			}
 			processUnmatchedElements(diffRoot, leftModel, unmatchedElements);
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
 		Resource leftModel = null;
 
 		// It is a possibility that no elements were matched
 		if (match.getMatchedElements().size() > 0) {
 			// we have to browse the model and create the corresponding operations
 			final Match2Elements matchRoot = (Match2Elements)match.getMatchedElements().get(0);
 			leftModel = matchRoot.getLeftElement().eResource();
 
 			// browsing the match model
 			doDiffDelegate(diffRoot, matchRoot);
 		}
 		// iterate over the unmatched elements end determine if they have been
 		// added or removed.
 		if (leftModel == null) {
 			for (final UnmatchElement element : match.getUnmatchedElements()) {
 				if (element.getElement().eResource() == null) {
 					continue;
 				}
 				if (element.getElement().eResource().getURI().toString().equals(match.getLeftModel())) {
 					leftModel = element.getElement().eResource();
 					break;
 				}
 			}
 		}
 		processUnmatchedElements(diffRoot, leftModel, match.getUnmatchedElements());
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
 		if (matchElem != null && from.equals(matchElem.getRightElement())) {
 			matchedEObject = matchElem.getLeftElement();
 		} else if (matchElem != null) {
 			matchedEObject = matchElem.getRightElement();
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
 	protected EObject getMatchedEObject(EObject from, int side) throws IllegalArgumentException {
 		if (side != LEFT_OBJECT && side != RIGHT_OBJECT && side != ANCESTOR_OBJECT) {
 			throw new IllegalArgumentException(EMFCompareDiffMessages
 					.getString("GenericDiffEngine.IllegalSide")); //$NON-NLS-1$
 		}
 		EObject matchedEObject = null;
 		final Match2Elements matchElem = eObjectToMatch.get(from);
 		if (matchElem != null) {
 			if (side == LEFT_OBJECT) {
 				matchedEObject = matchElem.getLeftElement();
 			} else if (side == RIGHT_OBJECT) {
 				matchedEObject = matchElem.getRightElement();
 			} else if (side == ANCESTOR_OBJECT && matchElem instanceof Match3Elements) {
 				matchedEObject = ((Match3Elements)matchElem).getOriginElement();
 			}
 		}
 		return matchedEObject;
 	}
 
 	/**
 	 * This will process the {@link #unmatchedElements unmatched elements} list and create the appropriate
 	 * {@link DiffElement}s.
 	 * <p>
 	 * This is called for two-way comparison. Clients can override this to alter the checks or add their own.
 	 * </p>
 	 * 
 	 * @param diffRoot
 	 *            {@link DiffGroup} under which to create the {@link DiffElement}s.
 	 * @param leftModel
 	 *            {@link Resource} representing the left model.
 	 * @param unmatched
 	 *            The MatchModel's {@link UnmatchElement}s.
 	 */
 	protected void processUnmatchedElements(DiffGroup diffRoot, Resource leftModel,
 			List<UnmatchElement> unmatched) {
 		for (final UnmatchElement unmatchElement : unmatched) {
 			final EObject element = unmatchElement.getElement();
 			if (element.eResource() != leftModel) {
 				// add RemoveModelElement
 				final RemoveModelElement operation = DiffFactory.eINSTANCE.createRemoveModelElement();
 				operation.setRightElement(element);
 				// Container will be null if we're adding a root
 				if (element.eContainer() != null) {
 					operation.setLeftParent(getMatchedEObject(element.eContainer()));
 					addInContainerPackage(diffRoot, operation, getMatchedEObject(element.eContainer()));
 				} else {
 					operation.setLeftParent(element.eContainer());
 					addInContainerPackage(diffRoot, operation, element.eContainer());
 				}
 			} else {
 				// add AddModelElement
 				final AddModelElement operation = DiffFactory.eINSTANCE.createAddModelElement();
 				operation.setLeftElement(element);
 				// Container will be null if we're adding a root
 				if (element.eContainer() != null) {
 					operation.setRightParent(getMatchedEObject(element.eContainer()));
 					addInContainerPackage(diffRoot, operation, getMatchedEObject(element.eContainer()));
 				} else {
 					operation.setRightParent(element.eContainer());
 					addInContainerPackage(diffRoot, operation, element.eContainer());
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will process the {@link #unmatchedElements unmatched elements} list and create the appropriate
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
 	 * @param unmatched
 	 *            The MatchModel's {@link UnmatchElement}s.
 	 */
 	protected void processUnmatchedElements(DiffGroup diffRoot, Resource leftModel,
 			Map<UnmatchElement, Boolean> unmatched) {
 		for (final UnmatchElement unmatchElement : unmatched.keySet()) {
 
 			if (unmatched.get(unmatchElement)) {
 				processConflictingUnmatchedElement(diffRoot, leftModel, unmatchElement);
 			} else {
 				final EObject element = unmatchElement.getElement();
 				final EObject matchedParent = getMatchedEObject(element.eContainer());
 
 				if (unmatchElement instanceof RemoteUnmatchElement) {
 					if (element.eResource() != leftModel) {
 						final RemoteAddModelElement addOperation = DiffFactory.eINSTANCE
 								.createRemoteAddModelElement();
 						addOperation.setRightElement(element);
 						addOperation.setLeftParent(matchedParent);
 						addInContainerPackage(diffRoot, addOperation, matchedParent);
 					} else {
 						final RemoteRemoveModelElement removeOperation = DiffFactory.eINSTANCE
 								.createRemoteRemoveModelElement();
 						removeOperation.setLeftElement(element);
 						removeOperation.setRightParent(matchedParent);
 						addInContainerPackage(diffRoot, removeOperation, element.eContainer());
 					}
 				} else {
 					if (element.eResource() != leftModel) {
 						final RemoveModelElement removeOperation = DiffFactory.eINSTANCE
 								.createRemoveModelElement();
 						removeOperation.setRightElement(element);
 						removeOperation.setLeftParent(matchedParent);
 						addInContainerPackage(diffRoot, removeOperation, matchedParent);
 					} else {
 						final AddModelElement addOperation = DiffFactory.eINSTANCE.createAddModelElement();
 						addOperation.setLeftElement(element);
 						addOperation.setRightParent(matchedParent);
 						addInContainerPackage(diffRoot, addOperation, element.eContainer());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will process the given unmatched element as a conflicting difference.
 	 * 
 	 * @param diffRoot
 	 *            {@link DiffGroup} under which to create the {@link DiffElement}s.
 	 * @param leftModel
 	 *            {@link Resource} representing the left model.
 	 * @param unmatch
 	 *            The conflicting diff element that is to be created.
 	 */
 	private void processConflictingUnmatchedElement(DiffGroup diffRoot, Resource leftModel,
 			UnmatchElement unmatch) {
 		final DiffElement operation;
 
 		final EObject element = unmatch.getElement();
 		final EObject matchedParent = getMatchedEObject(element.eContainer());
 		final EObject matchedAncestor = getMatchedEObject(element, ANCESTOR_OBJECT);
 
 		if (unmatch instanceof RemoteUnmatchElement) {
 			if (element.eResource() != leftModel) {
 				operation = DiffFactory.eINSTANCE.createConflictingDiffElement();
 				((ConflictingDiffElement)operation).setLeftParent(matchedParent);
 				((ConflictingDiffElement)operation).setRightParent(element);
 				((ConflictingDiffElement)operation).setOriginElement(matchedAncestor);
 
 				final RemoteAddModelElement addOperation = DiffFactory.eINSTANCE
 						.createRemoteAddModelElement();
 				addOperation.setRightElement(element);
 				addOperation.setLeftParent(matchedParent);
 
 				operation.getSubDiffElements().add(addOperation);
 				addInContainerPackage(diffRoot, operation, matchedParent);
 			} else {
 				operation = DiffFactory.eINSTANCE.createConflictingDiffElement();
 				((ConflictingDiffElement)operation).setLeftParent(element);
 				((ConflictingDiffElement)operation).setRightParent(matchedParent);
 				((ConflictingDiffElement)operation).setOriginElement(matchedAncestor);
 
 				final RemoteRemoveModelElement removeOperation = DiffFactory.eINSTANCE
 						.createRemoteRemoveModelElement();
 				removeOperation.setLeftElement(element);
 				removeOperation.setRightParent(matchedParent);
 
 				operation.getSubDiffElements().add(removeOperation);
 				addInContainerPackage(diffRoot, operation, element.eContainer());
 			}
 		} else {
 			if (element.eResource() != leftModel) {
 				operation = DiffFactory.eINSTANCE.createConflictingDiffElement();
 				((ConflictingDiffElement)operation).setLeftParent(matchedParent);
 				((ConflictingDiffElement)operation).setRightParent(element);
 				((ConflictingDiffElement)operation).setOriginElement(matchedAncestor);
 
 				final AddModelElement addOperation = DiffFactory.eINSTANCE.createAddModelElement();
 				addOperation.setLeftElement(element);
 				addOperation.setRightParent(matchedParent);
 
 				operation.getSubDiffElements().add(addOperation);
 				addInContainerPackage(diffRoot, operation, element.eContainer());
 			} else {
 				operation = DiffFactory.eINSTANCE.createConflictingDiffElement();
 				((ConflictingDiffElement)operation).setLeftParent(element);
 				((ConflictingDiffElement)operation).setRightParent(matchedParent);
 				((ConflictingDiffElement)operation).setOriginElement(matchedAncestor);
 
 				final RemoveModelElement removeOperation = DiffFactory.eINSTANCE.createRemoveModelElement();
 				removeOperation.setRightElement(element);
 				removeOperation.setLeftParent(matchedParent);
 
 				operation.getSubDiffElements().add(removeOperation);
 				addInContainerPackage(diffRoot, operation, matchedParent);
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
 		curGroup.setRightParent(targetParent);
 		final DiffGroup targetGroup = findExistingGroup(root, targetParent);
 		if (targetGroup != null) {
 			curGroup = targetGroup;
 		}
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
 	 * An attribute update is considered &quot;conflictual&quot; if it isn't multi-valued and its left value
 	 * differs from the right value.
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
 	private void checkConflictingAttributesUpdate(DiffGroup root, EAttribute attribute, Match3Elements mapping)
 			throws FactoryException {
 		if (!attribute.isMany()) {
 			createConflictingAttributeChange(root, attribute, mapping);
 		} else {
 			final List<?> leftValue = internalConvertFeatureMapList(EFactory.eGetAsList(mapping
 					.getLeftElement(), attribute.getName()));
 			final List<?> rightValue = internalConvertFeatureMapList(EFactory.eGetAsList(mapping
 					.getRightElement(), attribute.getName()));
 			final List<?> ancestorValue = internalConvertFeatureMapList(EFactory.eGetAsList(mapping
 					.getOriginElement(), attribute.getName()));
 
 			for (final Object aValue : leftValue) {
 				final boolean rightHasValue = rightValue.contains(aValue);
 				if (!rightHasValue && !ancestorValue.contains(aValue)) {
 					// If an object from the left is neither in the right nor in the
 					// origin, it's been added since last checkout
 					final AddAttribute operation = DiffFactory.eINSTANCE.createAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setLeftTarget(aValue);
 					root.getSubDiffElements().add(operation);
 				} else if (!rightHasValue) {
 					// If the object from the left is not in the right values,
 					// it's been removed remotely.
 					final RemoteRemoveAttribute operation = DiffFactory.eINSTANCE
 							.createRemoteRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setLeftTarget(aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 			for (final Object aValue : rightValue) {
 				final boolean leftHasValue = leftValue.contains(aValue);
 				if (!leftHasValue && !ancestorValue.contains(aValue)) {
 					// If an object from the right is neither in the left nor in the
 					// origin, it's a remotely added attribute
 					final RemoteAddAttribute operation = DiffFactory.eINSTANCE.createRemoteAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightTarget(aValue);
 					root.getSubDiffElements().add(operation);
 				} else if (!leftHasValue) {
 					// If the object from the right is not in the left values
 					// yet present in the origin, it's been removed since last checkout
 					final RemoveAttribute operation = DiffFactory.eINSTANCE.createRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightTarget(aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 		}
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
 	 * @return {@link List} of all the references that have been added in the left (local) element since the
 	 *         right (distant) element.
 	 */
 	private List<EObject> computeAddedReferences(List<EObject> leftReferences, List<EObject> rightReferences) {
 		final List<EObject> deletedReferences = new ArrayList<EObject>();
 		final List<EObject> addedReferences = new ArrayList<EObject>();
 
 		if (leftReferences != null) {
 			addedReferences.addAll(leftReferences);
 		}
 		if (rightReferences != null) {
 			deletedReferences.addAll(rightReferences);
 		}
 		final List<EObject> matchedOldReferences = getMatchedReferences(deletedReferences);
 
 		// "Added" references are the references from the left element that
 		// have no matching "right" counterpart
 		addedReferences.removeAll(matchedOldReferences);
 
 		// Double check for objects defined in a different model and thus not
 		// matched but plain equal
 		for (final EObject deleted : deletedReferences) {
 			addedReferences.remove(deleted);
 		}
 
 		return addedReferences;
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
 	 * @return {@link List} of all the references that have been deleted from the left (local) element since
 	 *         the right (distant) element.
 	 */
 	private List<EObject> computeDeletedReferences(List<EObject> leftReferences, List<EObject> rightReferences) {
 		final List<EObject> deletedReferences = new ArrayList<EObject>();
 		final List<EObject> addedReferences = new ArrayList<EObject>();
 
 		if (leftReferences != null) {
 			addedReferences.addAll(leftReferences);
 		}
 		if (rightReferences != null) {
 			deletedReferences.addAll(rightReferences);
 		}
 		final List<EObject> matchedNewReferences = getMatchedReferences(addedReferences);
 
 		// "deleted" references are the references from the right element that
 		// have no counterpart in the left element
 		deletedReferences.removeAll(matchedNewReferences);
 
 		// Double check for objects defined in a different model and thus not
 		// matched
 		for (final EObject added : addedReferences) {
 			deletedReferences.remove(added);
 		}
 
 		return deletedReferences;
 	}
 
 	/**
 	 * This will create the {@link ConflictingDiffGroup} and its children for a conflictual AttributeChange.
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
 	private void createConflictingAttributeChange(DiffGroup root, EAttribute attribute, Match3Elements mapping)
 			throws FactoryException {
 		// We'll use this diffGroup to make use of #createNonConflictingAttributeChange(DiffGroup, EAttribute,
 		// EObject, EObject)
 		final DiffGroup dummyGroup = DiffFactory.eINSTANCE.createDiffGroup();
 		createNonConflictingAttributeChange(dummyGroup, attribute, mapping.getLeftElement(), mapping
 				.getRightElement());
 
 		if (dummyGroup.getSubDiffElements().size() > 0) {
 			final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 					.createConflictingDiffElement();
 			conflictingDiff.setLeftParent(mapping.getLeftElement());
 			conflictingDiff.setRightParent(mapping.getRightElement());
 			conflictingDiff.setOriginElement(mapping.getOriginElement());
			for (final DiffElement subDiff : dummyGroup.getSubDiffElements()) {
 				conflictingDiff.getSubDiffElements().add(subDiff);
 			}
 			root.getSubDiffElements().add(conflictingDiff);
 		}
 	}
 
 	/**
 	 * This will create the {@link ConflictingDiffGroup} and its children for a conflictual ReferenceChange.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement} to create.
 	 * @param reference
 	 *            Target {@link EReference} of the modification.
 	 * @param mapping
 	 *            Contains informations about the left, right and origin element where the given reference has
 	 *            changed.
 	 * @throws FactoryException
 	 *             Thrown if we cannot create the underlying ReferenceChanges.
 	 */
 	private void createConflictingReferenceUpdate(DiffGroup root, EReference reference, Match3Elements mapping)
 			throws FactoryException {
 		// We'll use this diffGroup to make use of #createNonConflictingAttributeChange(DiffGroup, EAttribute,
 		// EObject, EObject)
 		final DiffGroup dummyGroup = DiffFactory.eINSTANCE.createDiffGroup();
 		createNonConflictingReferencesUpdate(dummyGroup, reference, mapping.getLeftElement(), mapping
 				.getRightElement());
 
 		if (dummyGroup.getSubDiffElements().size() > 0) {
 			final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 					.createConflictingDiffElement();
 			conflictingDiff.setLeftParent(mapping.getLeftElement());
 			conflictingDiff.setRightParent(mapping.getRightElement());
 			conflictingDiff.setOriginElement(mapping.getOriginElement());
			for (final DiffElement subDiff : dummyGroup.getSubDiffElements()) {
 				conflictingDiff.getSubDiffElements().add(subDiff);
 			}
 			root.getSubDiffElements().add(conflictingDiff);
 		}
 	}
 
 	/**
 	 * This will create the {@link MoveModelElement} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element that has been moved in the left model.
 	 * @param right
 	 *            Corresponding element that has been moved in the right model.
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
 	 * {@link DiffGroup}.<br/>The parameters include the list of added references which can be computed using
 	 * {@link #computeAddedReferences(List, List)}.
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
 		final Iterator<EObject> addedReferenceIterator = addedReferences.iterator();
 		while (addedReferenceIterator.hasNext()) {
 			final EObject eobj = addedReferenceIterator.next();
 			final AddReferenceValue addOperation = DiffFactory.eINSTANCE.createAddReferenceValue();
 			addOperation.setRightElement(right);
 			addOperation.setLeftElement(left);
 			addOperation.setReference(reference);
 			addOperation.setLeftTarget(eobj);
 			if (getMatchedEObject(eobj) != null) {
 				addOperation.setRightTarget(getMatchedEObject(eobj));
 			}
 			root.getSubDiffElements().add(addOperation);
 		}
 	}
 
 	/**
 	 * Creates and add the {@link DiffGroup} corresponding to an AttributeChange operation to the given
 	 * {@link DiffGroup root}.
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
 			for (final Object aValue : leftValue) {
 				if (!rightValue.contains(aValue)) {
 					final AddAttribute operation = DiffFactory.eINSTANCE.createAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(rightElement);
 					operation.setLeftElement(leftElement);
 					operation.setLeftTarget(aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 			for (final Object aValue : rightValue) {
 				if (!leftValue.contains(aValue)) {
 					final RemoveAttribute operation = DiffFactory.eINSTANCE.createRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(rightElement);
 					operation.setLeftElement(leftElement);
 					operation.setRightTarget(aValue);
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
 	 * This will check the given <code>reference</code> for modification between <code>leftElement</code> and
 	 * <code>rightElement</code> and create the corresponding {@link DiffElement}s under the given
 	 * {@link DiffGroup}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement}s to create.
 	 * @param reference
 	 *            {@link EReference} to check for modifications.
 	 * @param leftElement
 	 *            Element corresponding to the final value for the given reference.
 	 * @param rightElement
 	 *            Element corresponding to the initial value for the given reference.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch <code>reference</code>'s values for either the left or the right
 	 *             element.
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
 		if (!reference.isMany()) {
 			EObject addedValue = null;
 			EObject deletedValue = null;
 
 			if (addedReferences.size() > 0) {
 				addedValue = addedReferences.get(0);
 			}
 			if (deletedReferences.size() > 0) {
 				deletedValue = deletedReferences.get(0);
 			}
 
 			// One of the two value is null, reference has been unset
 			if ((addedValue == null || deletedValue == null) && addedValue != deletedValue) {
 				root.getSubDiffElements().add(
 						createUpdatedReferenceOperation(leftElement, rightElement, reference, addedValue,
 								deletedValue));
 			} else if (addedValue != null && deletedValue != null
 					&& !EcoreUtil.getURI(addedValue).equals(EcoreUtil.getURI(deletedValue))) {
 				root.getSubDiffElements().add(
 						createUpdatedReferenceOperation(leftElement, rightElement, reference, addedValue,
 								deletedValue));
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
 	 * {@link DiffGroup root}.<br/>An attribute is &quot;remotely changed&quot; if it has been added, updated
 	 * or deleted in the right (latest from head) version but it has kept its former value in the left
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
 	private void createRemoteAttributeChange(DiffGroup root, EAttribute attribute, Match3Elements mapping)
 			throws FactoryException {
 		if (attribute.isMany()) {
 			final List<?> leftValue = EFactory.eGetAsList(mapping.getLeftElement(), attribute.getName());
 			final List<?> rightValue = EFactory.eGetAsList(mapping.getRightElement(), attribute.getName());
 			for (final Object aValue : leftValue) {
 				// if the value is present in the right (latest) but not in the
 				// left (working copy), it's been removed remotely
 				if (!rightValue.contains(aValue)) {
 					final RemoteRemoveAttribute operation = DiffFactory.eINSTANCE
 							.createRemoteRemoveAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setLeftTarget(aValue);
 					root.getSubDiffElements().add(operation);
 				}
 			}
 			for (final Object aValue : rightValue) {
 				// if the value is present in the left (working copy) but not
 				// in the right (latest), it's been added remotely
 				if (!leftValue.contains(aValue)) {
 					final RemoteAddAttribute operation = DiffFactory.eINSTANCE.createRemoteAddAttribute();
 					operation.setAttribute(attribute);
 					operation.setRightElement(mapping.getRightElement());
 					operation.setLeftElement(mapping.getLeftElement());
 					operation.setRightTarget(aValue);
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
 	 * This will check for remote ReferenceChange operations and create the corresponding {@link DiffElement}
 	 * s.<br/>
 	 * <p>
 	 * A reference is considered &quot;remotely changed&quot; if its values differ between the right (latest
 	 * from HEAD) and origin (common ancestor) model, but its values haven't changed between the left (working
 	 * copy) and the origin model.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement}s to create.
 	 * @param reference
 	 *            {@link EReference} to check for ReferenceChanges.
 	 * @param mapping
 	 *            Contains informations about the left, right and original model elements.
 	 * @param remotelyAdded
 	 *            {@link List} of reference values that have been added in the left model since the origin.
 	 * @param remotelyDeleted
 	 *            {@link List} of reference values that have been removed from the left model since the
 	 *            origin.
 	 */
 	private void createRemoteReferencesUpdate(DiffGroup root, EReference reference, Match3Elements mapping,
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
 			if (leftTarget == null) {
 				leftTarget = remotelyAdded.get(0);
 			}
 			if (rightTarget == null) {
 				rightTarget = remotelyDeleted.get(0);
 			}
 
 			operation.setLeftTarget(leftTarget);
 			operation.setRightTarget(rightTarget);
 
 			root.getSubDiffElements().add(operation);
 		} else if (reference.isMany()) {
 			final Iterator<EObject> addedReferenceIterator = remotelyAdded.iterator();
 			while (addedReferenceIterator.hasNext()) {
 				final EObject eobj = addedReferenceIterator.next();
 				final RemoteAddReferenceValue addOperation = DiffFactory.eINSTANCE
 						.createRemoteAddReferenceValue();
 				addOperation.setRightElement(mapping.getRightElement());
 				addOperation.setLeftElement(mapping.getLeftElement());
 				addOperation.setReference(reference);
 				addOperation.setRightTarget(eobj);
 				if ((getMatchedEObject(eobj)) != null) {
 					addOperation.setLeftTarget(getMatchedEObject(eobj));
 				}
 				root.getSubDiffElements().add(addOperation);
 			}
 			final Iterator<EObject> deletedReferenceIterator = remotelyDeleted.iterator();
 			while (deletedReferenceIterator.hasNext()) {
 				final EObject eobj = deletedReferenceIterator.next();
 				final RemoteRemoveReferenceValue delOperation = DiffFactory.eINSTANCE
 						.createRemoteRemoveReferenceValue();
 				delOperation.setRightElement(mapping.getRightElement());
 				delOperation.setLeftElement(mapping.getLeftElement());
 				delOperation.setReference(reference);
 				delOperation.setLeftTarget(eobj);
 				if ((getMatchedEObject(eobj)) != null) {
 					delOperation.setRightTarget(getMatchedEObject(eobj));
 				}
 				root.getSubDiffElements().add(delOperation);
 			}
 		}
 	}
 
 	/**
 	 * This will create a {@link UpdateContainmentFeature} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element of the left model corresponding to the right one.
 	 * @param right
 	 *            Element that has been moved since the last (ancestor for three-way comparison, left for
 	 *            two-way comparison) version.
 	 */
 	private void createRemoteUpdateContainmentOperation(DiffGroup root, EObject left, EObject right) {
 		final RemoteUpdateContainmentFeature updateContainment = DiffFactory.eINSTANCE
 				.createRemoteUpdateContainmentFeature();
 		updateContainment.setLeftElement(left);
 		updateContainment.setRightElement(right);
 		updateContainment.setRightTarget(getMatchedEObject(left.eContainer()));
 		updateContainment.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(updateContainment);
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
 		final Iterator<EObject> deletedReferenceIterator = deletedReferences.iterator();
 		while (deletedReferenceIterator.hasNext()) {
 			final EObject eobj = deletedReferenceIterator.next();
 			final RemoveReferenceValue delOperation = DiffFactory.eINSTANCE.createRemoveReferenceValue();
 			delOperation.setRightElement(right);
 			delOperation.setLeftElement(left);
 			delOperation.setReference(reference);
 			delOperation.setRightTarget(eobj);
 			if (getMatchedEObject(eobj) != null) {
 				delOperation.setLeftTarget(getMatchedEObject(eobj));
 			}
 			root.getSubDiffElements().add(delOperation);
 		}
 	}
 
 	/**
 	 * This will create a {@link UpdateContainmentFeature} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element of the left model corresponding to the right one.
 	 * @param right
 	 *            Element that has been moved since the last (ancestor for three-way comparison, right for
 	 *            two-way comparison) version.
 	 */
 	private void createUpdateContainmentOperation(DiffGroup root, EObject left, EObject right) {
 		final UpdateContainmentFeature updateContainment = DiffFactory.eINSTANCE
 				.createUpdateContainmentFeature();
 		updateContainment.setLeftElement(left);
 		updateContainment.setRightElement(right);
 		updateContainment.setRightTarget(getMatchedEObject(left.eContainer()));
 		updateContainment.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(updateContainment);
 	}
 
 	/**
 	 * Creates the {@link DiffElement} corresponding to an unique reference's value update.
 	 * 
 	 * @param left
 	 *            Left element of the reference change.
 	 * @param right
 	 *            Right element of the reference change.
 	 * @param reference
 	 *            {@link EReference} target of the operation.
 	 * @param addedValue
 	 *            Value which has been added for the reference.
 	 * @param deletedValue
 	 *            Value that has been deleted from the reference.
 	 * @return The {@link DiffElement} corresponding to an unique reference's value update
 	 */
 	private UpdateUniqueReferenceValue createUpdatedReferenceOperation(EObject left, EObject right,
 			EReference reference, EObject addedValue, EObject deletedValue) {
 		final UpdateUniqueReferenceValue operation = DiffFactory.eINSTANCE.createUpdateUniqueReferenceValue();
 		operation.setLeftElement(left);
 		operation.setRightElement(right);
 		operation.setReference(reference);
 
 		EObject leftTarget = getMatchedEObject(addedValue);
 		EObject rightTarget = getMatchedEObject(deletedValue);
 		// checks if target are defined remotely
 		if (leftTarget == null) {
 			leftTarget = addedValue;
 		}
 		if (rightTarget == null) {
 			rightTarget = deletedValue;
 		}
 
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
 	 *            {@link Match2Elements root} of the {@link MatchModel} to analyze.
 	 */
 	private void doDiffDelegate(DiffGroup root, Match2Elements match) {
 		DiffGroup current = DiffFactory.eINSTANCE.createDiffGroup();
 		current.setRightParent(match.getRightElement());
 		try {
 			checkForDiffs(current, match);
 		} catch (final FactoryException e) {
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
 			for (final DiffElement diff : shouldAddToList) {
 				addInContainerPackage(root, diff, current.getRightParent());
 			}
 		} else {
 			current = root;
 		}
 		// taking care of our children
 		final Iterator<MatchElement> it = match.getSubMatchElements().iterator();
 		while (it.hasNext()) {
 			final Match2Elements element = (Match2Elements)it.next();
 			doDiffDelegate(root, element);
 		}
 	}
 
 	/**
 	 * This is the core of the diff computing for three way comparison. This will call for checks on
 	 * attributes, references and model elements to check for updates/changes.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffModel} to create.
 	 * @param match
 	 *            {@link Match3Elements root} of the {@link MatchModel} to analyze.
 	 */
 	private void doDiffDelegate(DiffGroup root, Match3Elements match) {
 		DiffGroup current = DiffFactory.eINSTANCE.createDiffGroup();
 		current.setRightParent(match.getRightElement());
 		try {
 			checkForDiffs(current, match);
 		} catch (final FactoryException e) {
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
 			for (final DiffElement diff : shouldAddToList) {
 				addInContainerPackage(root, diff, current.getRightParent());
 			}
 		} else {
 			current = root;
 		}
 		// taking care of our children
 		final Iterator<MatchElement> it = match.getSubMatchElements().iterator();
 		while (it.hasNext()) {
 			final MatchElement element = it.next();
 			if (element instanceof Match3Elements) {
 				doDiffDelegate(root, (Match3Elements)element);
 			} else {
 				doDiffDelegate(root, (Match2Elements)element);
 			}
 		}
 	}
 
 	/**
 	 * Searches for an existing {@link DiffGroup} under <code>root</code> to add the operation which parent is
 	 * <code>targetParent</code>.
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
 				if (((DiffGroup)obj).getRightParent() == targetParent)
 					return (DiffGroup)obj;
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
 		final Iterator<EObject> refIterator = references.iterator();
 		while (refIterator.hasNext()) {
 			final Object currentReference = refIterator.next();
 			if (currentReference != null) {
 				final EObject currentMapped = getMatchedEObject((EObject)currentReference);
 				if (currentMapped != null) {
 					matchedReferences.add(currentMapped);
 				}
 			}
 		}
 		return matchedReferences;
 	}
 
 	/**
 	 * This will return a list containing only EObjects. This is mainly aimed at FeatureMap.Entry values.
 	 * 
 	 * @param input
 	 *            List that is to be converted.
 	 * @return A list containing only EObjects.
 	 */
 	private List<EObject> internalConvertFeatureMapList(List<?> input) {
 		final List<EObject> result = new ArrayList<EObject>();
 		for (final Object inputValue : input) {
 			result.add(internalFindActualEObject(inputValue));
 		}
 		return result;
 	}
 
 	/**
 	 * This will return the first value of <tt>data</tt> that is not an instance of {@link Entry}.
 	 * 
 	 * @param data
 	 *            The object we need a valued of.
 	 * @return The first value of <tt>data</tt> that is not an instance of FeatureMapEntry.
 	 */
 	private EObject internalFindActualEObject(Object data) {
 		if (data instanceof Entry)
 			return internalFindActualEObject(((Entry)data).getValue());
 		return (EObject)data;
 	}
 
 	/**
 	 * Checks if the values of a given reference have been changed both on the right (latest from head) and
 	 * left (working copy) to distinct values since the origin.
 	 * 
 	 * @param reference
 	 *            Reference we're checking for conflictual changes.
 	 * @param leftReferences
 	 *            {@link List} of values from the left (working copy) model for <code>reference</code>.
 	 * @param rightReferences
 	 *            {@link List} of values from the right (latest from head) model for <code>reference</code>.
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
 				// If the number of values hasn't changed since the origin, there
 				// cannot be a conflict if there are no values
 			} else if (leftReferences.size() > 0 && rightReferences.size() > 0) {
 				// There's a conflict if the values are distinct
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
 	 *            <li>&quot;Added&quot; values are the values that have been added in the left element since
 	 *            the origin and that haven't been added in the right element.</li>
 	 *            <li>&quot;Deleted&quot; values are the values that have been removed from the left element
 	 *            since the origin but are still present in the right element.</li>
 	 *            <li>&quot;Remotely added&quot; values are the values that have been added in the right
 	 *            element since the origin but haven't been added in the left element.</li>
 	 *            <li>&quot;Remotely deleted&quot; values are the values that have been removed from the right
 	 *            element since the origin but are still present in the left element.</li>
 	 *            </ul>
 	 * @param reference
 	 *            {@link EReference} we're checking for changes.
 	 * @param addedReferences
 	 *            {@link List} that will be populated with the values that have been added in the left element
 	 *            since the origin.
 	 * @param deletedReferences
 	 *            {@link List} that will be populated with the values that have been removed from the left
 	 *            element since the origin.
 	 * @param remoteAddedReferences
 	 *            {@link List} that will be populated with the values that have been added in the right
 	 *            element since the origin.
 	 * @param remoteDeletedReferences
 	 *            {@link List} that will be populated with the values that have been removed from the right
 	 *            element since the origin.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the reference's values in either the left, right or origin
 	 *             element.
 	 */
 	private void populateThreeWayReferencesChanges(Match3Elements mapping, EReference reference,
 			List<EObject> addedReferences, List<EObject> deletedReferences,
 			List<EObject> remoteAddedReferences, List<EObject> remoteDeletedReferences)
 			throws FactoryException {
 		final String referenceName = reference.getName();
 		final List<?> leftReferences = EFactory.eGetAsList(mapping.getLeftElement(), referenceName);
 		final List<?> rightReferences = EFactory.eGetAsList(mapping.getRightElement(), referenceName);
 		final List<?> ancestorReferences = EFactory.eGetAsList(mapping.getOriginElement(), referenceName);
 
 		// populates remotely added references list
 		for (final Object right : rightReferences) {
 			if (right instanceof EObject
 					&& !ancestorReferences.contains(getMatchedEObject((EObject)right, ANCESTOR_OBJECT))
 					&& !leftReferences.contains(getMatchedEObject((EObject)right))) {
 				remoteAddedReferences.add((EObject)right);
 			}
 		}
 		// populates localy added list
 		for (final Object left : leftReferences) {
 			if (left instanceof EObject
 					&& !ancestorReferences.contains(getMatchedEObject((EObject)left, ANCESTOR_OBJECT))
 					&& !rightReferences.contains(getMatchedEObject((EObject)left))) {
 				addedReferences.add((EObject)left);
 			}
 		}
 		// populates remotely deleted and localy added lists
 		for (final Object origin : ancestorReferences) {
 			if (origin instanceof EObject
 					&& !leftReferences.contains(getMatchedEObject((EObject)origin, LEFT_OBJECT))
 					&& rightReferences.contains(getMatchedEObject((EObject)origin, RIGHT_OBJECT))) {
 				deletedReferences.add((EObject)origin);
 			} else if (origin instanceof EObject
 					&& !rightReferences.contains(getMatchedEObject((EObject)origin, RIGHT_OBJECT))
 					&& leftReferences.contains(getMatchedEObject((EObject)origin, LEFT_OBJECT))) {
 				remoteDeletedReferences.add((EObject)origin);
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
 			if (threeWay) {
 				eObjectToMatch.put(((Match3Elements)matchRoot).getOriginElement(), matchRoot);
 			}
 			final TreeIterator<EObject> matchElemIt = matchRoot.eAllContents();
 			while (matchElemIt.hasNext()) {
 				final Match2Elements matchElem = (Match2Elements)matchElemIt.next();
 				eObjectToMatch.put(matchElem.getLeftElement(), matchElem);
 				eObjectToMatch.put(matchElem.getRightElement(), matchElem);
 				if (threeWay && ((Match3Elements)matchElem).getOriginElement() != null) {
 					eObjectToMatch.put(((Match3Elements)matchElem).getOriginElement(), matchElem);
 				}
 			}
 		}
 	}
 }
