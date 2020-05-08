 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.merge;
 
 import static com.google.common.collect.Iterators.filter;
 import static org.eclipse.emf.compare.utils.ReferenceUtil.safeEIsSet;
 
 import com.google.common.collect.Iterators;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 
 /**
  * This specific implementation of {@link AbstractMerger} will be used to merge reference changes.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ReferenceChangeMerger extends AbstractMerger {
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.merge.IMerger#isMergerFor(org.eclipse.emf.compare.Diff)
 	 */
 	public boolean isMergerFor(Diff target) {
 		return target instanceof ReferenceChange;
 	}
 
 	/**
 	 * Merge the given difference rejecting it.
 	 * 
 	 * @param diff
 	 *            The difference to merge.
 	 * @param rightToLeft
 	 *            The direction of the merge.
 	 */
 	@Override
 	protected void reject(final Diff diff, boolean rightToLeft) {
 		ReferenceChange referenceChange = (ReferenceChange)diff;
 		DifferenceSource source = referenceChange.getSource();
 		switch (referenceChange.getKind()) {
 			case ADD:
 				// We have a ADD on left, thus nothing in right. We need to revert the addition
 				removeFromTarget(referenceChange, rightToLeft);
 				break;
 			case DELETE:
 				// DELETE in the left, thus an element in right. We need to re-create that element
 				addInTarget(referenceChange, rightToLeft);
 				break;
 			case MOVE:
 				moveElement(referenceChange, rightToLeft);
 				break;
 			case CHANGE:
 				EObject container = null;
 				if (source == DifferenceSource.LEFT) {
 					container = referenceChange.getMatch().getLeft();
 
 				} else {
 					container = referenceChange.getMatch().getRight();
 				}
 				// Is it an unset?
 				if (container != null) {
 					final EObject leftValue = (EObject)container.eGet(referenceChange.getReference(), false);
 					if (leftValue == null) {
 						// Value has been unset in the right, and we are merging towards right.
 						// We need to re-add this element
 						addInTarget(referenceChange, rightToLeft);
 					} else {
 						// We'll actually need to "reset" this reference to its original value
 						resetInTarget(referenceChange, rightToLeft);
 					}
 				} else {
 					// we have no left, and the source is on the left. Can only be an unset
 					addInTarget(referenceChange, rightToLeft);
 				}
 				break;
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * Merge the given difference accepting it.
 	 * 
 	 * @param diff
 	 *            The difference to merge.
 	 * @param rightToLeft
 	 *            The direction of the merge.
 	 */
 	@Override
 	protected void accept(final Diff diff, boolean rightToLeft) {
 		ReferenceChange referenceChange = (ReferenceChange)diff;
 		DifferenceSource source = diff.getSource();
 		switch (diff.getKind()) {
 			case ADD:
 				// Create the same element in right
 				addInTarget(referenceChange, rightToLeft);
 				break;
 			case DELETE:
 				// Delete that same element from right
 				removeFromTarget(referenceChange, rightToLeft);
 				break;
 			case MOVE:
 				moveElement(referenceChange, rightToLeft);
 				break;
 			case CHANGE:
 				EObject container = null;
 				if (source == DifferenceSource.LEFT) {
 					container = referenceChange.getMatch().getLeft();
 				} else {
 					container = referenceChange.getMatch().getRight();
 				}
 				// Is it an unset?
 				if (container != null) {
					final EObject leftValue = (EObject)container.eGet(referenceChange.getReference(), false);
 					if (leftValue == null) {
 						removeFromTarget(referenceChange, rightToLeft);
 					} else {
 						addInTarget(referenceChange, rightToLeft);
 					}
 				} else {
 					// we have no left, and the source is on the left. Can only be an unset
 					removeFromTarget(referenceChange, rightToLeft);
 				}
 				break;
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * This will be called when trying to copy a "MOVE" diff.
 	 * 
 	 * @param diff
 	 *            The diff we are currently merging.
 	 * @param rightToLeft
 	 *            Whether we should move the value in the left or right side.
 	 */
 	protected void moveElement(ReferenceChange diff, boolean rightToLeft) {
 		final Comparison comparison = diff.getMatch().getComparison();
 		final Match valueMatch = comparison.getMatch(diff.getValue());
 		final EReference reference = diff.getReference();
 
 		final EObject expectedContainer;
 		if (reference.isContainment()) {
 			/*
 			 * We cannot "trust" the holding match (getMatch) in this case. However, "valueMatch" cannot be
 			 * null : we cannot have detected a move if the moved element is not matched on both sides. Use
 			 * that information to retrieve the proper "target" container.
 			 */
 			final Match targetContainerMatch;
 			// If it exists, use the source side's container as reference
 			if (rightToLeft && valueMatch.getRight() != null) {
 				targetContainerMatch = comparison.getMatch(valueMatch.getRight().eContainer());
 			} else if (!rightToLeft && valueMatch.getLeft() != null) {
 				targetContainerMatch = comparison.getMatch(valueMatch.getLeft().eContainer());
 			} else {
 				// Otherwise, the value we're moving on one side has been removed from its source side.
 				targetContainerMatch = comparison.getMatch(valueMatch.getOrigin().eContainer());
 			}
 			if (rightToLeft) {
 				expectedContainer = targetContainerMatch.getLeft();
 			} else {
 				expectedContainer = targetContainerMatch.getRight();
 			}
 		} else if (rightToLeft) {
 			expectedContainer = diff.getMatch().getLeft();
 		} else {
 			expectedContainer = diff.getMatch().getRight();
 		}
 		if (expectedContainer == null) {
 			// FIXME throw exception? log? re-try to merge our requirements?
 			// one of the "required" diffs should have created our container.
 			return;
 		}
 
 		final EObject expectedValue;
 		if (valueMatch == null) {
 			// The value being moved is out of the scope
 			/*
 			 * Note : there should not be a way to end up with a "move" for an out of scope value : a move can
 			 * only be detected if the object is matched on both sides, otherwise all we can see is "add" and
 			 * "delete"... Is this "fallback" code even reachable? If so, how?
 			 */
 			// We need to look it up
 			if (reference.isMany()) {
 				@SuppressWarnings("unchecked")
 				final List<EObject> targetList = (List<EObject>)expectedContainer.eGet(reference);
 				expectedValue = findMatchIn(comparison, targetList, diff.getValue());
 			} else {
 				expectedValue = (EObject)expectedContainer.eGet(reference);
 			}
 		} else {
 			if (rightToLeft) {
 				expectedValue = valueMatch.getLeft();
 			} else {
 				expectedValue = valueMatch.getRight();
 			}
 		}
 		// We now know the target container, target reference and target value.
 		doMove(diff, comparison, expectedContainer, expectedValue, rightToLeft);
 	}
 
 	/**
 	 * This will do the actual work of moving the element into its reference. All sanity checks were made in
 	 * {@link #moveElement(boolean)} and no more verification will be made here.
 	 * 
 	 * @param diff
 	 *            The diff we are currently merging.
 	 * @param comparison
 	 *            Comparison holding this Diff.
 	 * @param expectedContainer
 	 *            The container in which we are reorganizing a reference.
 	 * @param expectedValue
 	 *            The value that is to be moved within its reference.
 	 * @param rightToLeft
 	 *            Whether we should move the value in the left or right side.
 	 */
 	@SuppressWarnings("unchecked")
 	protected void doMove(ReferenceChange diff, Comparison comparison, EObject expectedContainer,
 			EObject expectedValue, boolean rightToLeft) {
 		final EReference reference = diff.getReference();
 		if (reference.isMany()) {
 			// Element to move cannot be part of the LCS... or there would not be a MOVE diff
 			int insertionIndex = findInsertionIndex(comparison, diff, rightToLeft);
 
 			/*
 			 * However, it could still have been located "before" its new index, in which case we need to take
 			 * it into account.
 			 */
 			final List<EObject> targetList = (List<EObject>)expectedContainer.eGet(reference);
 			final int currentIndex = targetList.indexOf(expectedValue);
 			if (insertionIndex > currentIndex && currentIndex >= 0) {
 				insertionIndex--;
 			}
 
 			if (currentIndex == -1) {
 				// happens for container changes for example.
 				if (!reference.isContainment()) {
 					targetList.remove(expectedValue);
 				}
 				if (insertionIndex < 0 && insertionIndex > targetList.size()) {
 					targetList.add(expectedValue);
 				} else {
 					targetList.add(insertionIndex, expectedValue);
 				}
 			} else if (targetList instanceof EList<?>) {
 				if (insertionIndex < 0 && insertionIndex > targetList.size()) {
 					((EList<EObject>)targetList).move(targetList.size() - 1, expectedValue);
 				} else {
 					((EList<EObject>)targetList).move(insertionIndex, expectedValue);
 				}
 			} else {
 				targetList.remove(expectedValue);
 				if (insertionIndex < 0 && insertionIndex > targetList.size()) {
 					targetList.add(expectedValue);
 				} else {
 					targetList.add(insertionIndex, expectedValue);
 				}
 			}
 		} else {
 			expectedContainer.eSet(reference, expectedValue);
 		}
 	}
 
 	/**
 	 * This will be called when we need to create an element in the target side.
 	 * <p>
 	 * All necessary sanity checks have been made to ensure that the current operation is one that should
 	 * create an object in its side or add an objet to a reference. In other words, either :
 	 * <ul>
 	 * <li>We are copying from right to left and
 	 * <ul>
 	 * <li>we are copying an addition to the right side (we need to create the same object in the left), or</li>
 	 * <li>we are copying a deletion from the left side (we need to revert the deletion).</li>
 	 * </ul>
 	 * </li>
 	 * <li>We are copying from left to right and
 	 * <ul>
 	 * <li>we are copying a deletion from the right side (we need to revert the deletion), or</li>
 	 * <li>we are copying an addition to the left side (we need to create the same object in the right).</li>
 	 * </ul>
 	 * </li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param diff
 	 *            The diff we are currently merging.
 	 * @param rightToLeft
 	 *            Tells us whether we are to add an object on the left or right side.
 	 */
 	@SuppressWarnings("unchecked")
 	protected void addInTarget(ReferenceChange diff, boolean rightToLeft) {
 		final Match match = diff.getMatch();
 		final EObject expectedContainer;
 		if (rightToLeft) {
 			expectedContainer = match.getLeft();
 		} else {
 			expectedContainer = match.getRight();
 		}
 
 		if (expectedContainer == null) {
 			// FIXME throw exception? log? re-try to merge our requirements?
 			// one of the "required" diffs should have created our container.
 			return;
 		}
 
 		final Comparison comparison = match.getComparison();
 		final EReference reference = diff.getReference();
 		final EObject expectedValue;
 		final Match valueMatch = comparison.getMatch(diff.getValue());
 		if (valueMatch == null) {
 			// This is an out of scope value.
 			if (diff.getValue().eIsProxy()) {
 				// Copy the proxy
 				expectedValue = EcoreUtil.copy(diff.getValue());
 			} else {
 				// Use the same value.
 				expectedValue = diff.getValue();
 			}
 		} else if (rightToLeft) {
 			if (reference.isContainment()) {
 				expectedValue = createCopy(diff.getValue());
 				valueMatch.setLeft(expectedValue);
 			} else {
 				expectedValue = valueMatch.getLeft();
 			}
 		} else {
 			if (reference.isContainment()) {
 				expectedValue = createCopy(diff.getValue());
 				valueMatch.setRight(expectedValue);
 			} else {
 				expectedValue = valueMatch.getRight();
 			}
 		}
 
 		// We have the container, reference and value. We need to know the insertion index.
 		if (reference.isMany()) {
 			final int insertionIndex = findInsertionIndex(comparison, diff, rightToLeft);
 
 			final List<EObject> targetList = (List<EObject>)expectedContainer.eGet(reference);
 			addAt(targetList, expectedValue, insertionIndex);
 		} else {
 			expectedContainer.eSet(reference, expectedValue);
 		}
 
 		if (reference.isContainment()) {
 			// Copy XMI ID when applicable.
 			final Resource initialResource = diff.getValue().eResource();
 			final Resource targetResource = expectedValue.eResource();
 			if (initialResource instanceof XMIResource && targetResource instanceof XMIResource) {
 				((XMIResource)targetResource).setID(expectedValue, ((XMIResource)initialResource).getID(diff
 						.getValue()));
 			}
 		}
 
 		checkImpliedDiffsOrdering(diff, rightToLeft);
 	}
 
 	/**
 	 * This will be called when we need to remove an element from the target side.
 	 * <p>
 	 * All necessary sanity checks have been made to ensure that the current operation is one that should
 	 * delete an object. In other words, we are :
 	 * <ul>
 	 * <li>Copying from right to left and either
 	 * <ul>
 	 * <li>we are copying a deletion from the right side (we need to remove the same object in the left) or,</li>
 	 * <li>we are copying an addition to the left side (we need to revert the addition).</li>
 	 * </ul>
 	 * </li>
 	 * <li>Copying from left to right and either
 	 * <ul>
 	 * <li>we are copying an addition to the right side (we need to revert the addition), or.</li>
 	 * <li>we are copying a deletion from the left side (we need to remove the same object in the right).</li>
 	 * </ul>
 	 * </li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param diff
 	 *            The diff we are currently merging.
 	 * @param rightToLeft
 	 *            Tells us whether we are to add an object on the left or right side.
 	 */
 	@SuppressWarnings("unchecked")
 	protected void removeFromTarget(ReferenceChange diff, boolean rightToLeft) {
 		final Match match = diff.getMatch();
 		final EReference reference = diff.getReference();
 		final EObject currentContainer;
 		if (rightToLeft) {
 			currentContainer = match.getLeft();
 		} else {
 			currentContainer = match.getRight();
 		}
 		final Comparison comparison = match.getComparison();
 		final Match valueMatch = comparison.getMatch(diff.getValue());
 
 		if (currentContainer == null) {
 			// FIXME throw exception? log? re-try to merge our requirements?
 			// one of the "required" diffs should have created our container.
 			return;
 		}
 
 		final EObject expectedValue;
 		if (valueMatch == null) {
 			// value is out of the scope... we need to look it up
 			if (reference.isMany()) {
 				final List<EObject> targetList = (List<EObject>)currentContainer.eGet(reference);
 				expectedValue = findMatchIn(comparison, targetList, diff.getValue());
 			} else {
 				// the value will not be needed anyway
 				expectedValue = null;
 			}
 		} else if (rightToLeft) {
 			expectedValue = valueMatch.getLeft();
 		} else {
 			expectedValue = valueMatch.getRight();
 		}
 
 		// We have the container, reference and value to remove. Expected value can be null when the
 		// deletion was made on both side (i.e. a pseudo delete)
 		if (reference.isContainment() && expectedValue != null) {
 			EcoreUtil.remove(expectedValue);
 			if (rightToLeft && valueMatch != null) {
 				valueMatch.setLeft(null);
 			} else if (valueMatch != null) {
 				valueMatch.setRight(null);
 			}
 			// TODO remove dangling? remove empty Match?
 		} else if (reference.isMany()) {
 			/*
 			 * TODO if the same value appears twice, should we try and find the one that has actually been
 			 * deleted? Can it happen? For now, remove the first occurence we find.
 			 */
 			final List<EObject> targetList = (List<EObject>)currentContainer.eGet(reference);
 			targetList.remove(expectedValue);
 		} else {
 			currentContainer.eUnset(reference);
 		}
 	}
 
 	/**
 	 * This will be called by the merge operations in order to reset a reference to its original value, be
 	 * that the left or right side.
 	 * <p>
 	 * Should never be called on multi-valued references.
 	 * </p>
 	 * 
 	 * @param diff
 	 *            The diff we are currently merging.
 	 * @param rightToLeft
 	 *            Tells us the direction of this merge operation.
 	 */
 	protected void resetInTarget(ReferenceChange diff, boolean rightToLeft) {
 		final Match match = diff.getMatch();
 		final EReference reference = diff.getReference();
 		final EObject targetContainer;
 		if (rightToLeft) {
 			targetContainer = match.getLeft();
 		} else {
 			targetContainer = match.getRight();
 		}
 
 		final EObject originContainer;
 		if (match.getComparison().isThreeWay()) {
 			originContainer = match.getOrigin();
 		} else if (rightToLeft) {
 			originContainer = match.getRight();
 		} else {
 			originContainer = match.getLeft();
 		}
 
 		if (originContainer == null || !safeEIsSet(targetContainer, reference)
 				|| !safeEIsSet(originContainer, reference)) {
 			targetContainer.eUnset(reference);
 		} else {
			final EObject originalValue = (EObject)originContainer.eGet(reference);
 			final Match valueMatch = match.getComparison().getMatch(originalValue);
 			final EObject expectedValue;
 			if (valueMatch == null) {
 				// Value is out of the scope, use it as-is
 				expectedValue = originalValue;
 			} else if (rightToLeft) {
 				expectedValue = valueMatch.getLeft();
 			} else {
 				expectedValue = valueMatch.getRight();
 			}
 			targetContainer.eSet(reference, expectedValue);
 		}
 	}
 
 	/**
 	 * In the case of many-to-many eOpposite references, EMF will simply report the difference made on one
 	 * side of the equivalence to the other, without considering ordering in any way. In such cases, we'll
 	 * iterate over our equivalences after the merge, and double-check the ordering ourselves, fixing it as
 	 * needed.
 	 * <p>
 	 * Note that both implied and equivalent diffs will be double-checked from here.
 	 * </p>
 	 * 
 	 * @param diff
 	 *            The diff we are currently merging.
 	 * @param rightToLeft
 	 *            Direction of the merge.
 	 */
 	protected void checkImpliedDiffsOrdering(ReferenceChange diff, boolean rightToLeft) {
 		final EReference reference = diff.getReference();
 		final List<Diff> mergedImplications;
 		if (rightToLeft) {
 			if (diff.getSource() == DifferenceSource.LEFT) {
 				mergedImplications = diff.getImpliedBy();
 			} else {
 				mergedImplications = diff.getImplies();
 			}
 		} else {
 			if (diff.getSource() == DifferenceSource.LEFT) {
 				mergedImplications = diff.getImplies();
 			} else {
 				mergedImplications = diff.getImpliedBy();
 			}
 		}
 
 		Iterator<Diff> impliedDiffs = mergedImplications.iterator();
 		if (reference.isMany() && diff.getEquivalence() != null) {
 			impliedDiffs = Iterators.concat(impliedDiffs, diff.getEquivalence().getDifferences().iterator());
 		}
 		final Iterator<ReferenceChange> impliedReferenceChanges = filter(impliedDiffs, ReferenceChange.class);
 
 		while (impliedReferenceChanges.hasNext()) {
 			final ReferenceChange implied = impliedReferenceChanges.next();
 			if (implied != diff && implied.getState() == DifferenceState.MERGED) {
 				if (implied.getReference().isMany() && implied.getKind() != DifferenceKind.MOVE) {
 					internalCheckOrdering(implied, rightToLeft);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks a particular difference for the ordering of its target values. This will be used to double-check
 	 * that equivalent differences haven't been "broken" by EMF by not preserving their value order.
 	 * <p>
 	 * Should only be used on <u>merged</u> differences which target <u>many-valued</u> references.
 	 * </p>
 	 * 
 	 * @param diff
 	 *            The diff that is to be checked.
 	 * @param rightToLeft
 	 *            Direction of the merge that took place.
 	 */
 	private void internalCheckOrdering(ReferenceChange diff, boolean rightToLeft) {
 		final EStructuralFeature feature = diff.getReference();
 		final EObject value = diff.getValue();
 		final Match match = diff.getMatch();
 		final Comparison comparison = match.getComparison();
 		final Match valueMatch = comparison.getMatch(value);
 
 		final EObject sourceContainer;
 		final EObject targetContainer;
 		final EObject newValue;
 		if (rightToLeft) {
 			sourceContainer = match.getRight();
 			targetContainer = match.getLeft();
 			newValue = valueMatch.getLeft();
 		} else {
 			sourceContainer = match.getLeft();
 			targetContainer = match.getRight();
 			newValue = valueMatch.getRight();
 		}
 
 		final List<Object> sourceList = ReferenceUtil.getAsList(sourceContainer, feature);
 		final List<Object> targetList = ReferenceUtil.getAsList(targetContainer, feature);
 
 		final List<Object> lcs = DiffUtil.longestCommonSubsequence(comparison, sourceList, targetList);
 		if (lcs.contains(valueMatch.getLeft()) || lcs.contains(valueMatch.getRight())) {
 			// Ordering is correct on this one
 			return;
 		}
 
 		int insertionIndex = DiffUtil.findInsertionIndex(comparison, sourceList, targetList, value);
 		if (insertionIndex >= 0) {
 			/*
 			 * We've used unresolving views of the eobject lists since we didn't know whether there was
 			 * actually any work to do. Use the real list now.
 			 */
 			@SuppressWarnings("unchecked")
 			final List<EObject> changedList = (List<EObject>)targetContainer.eGet(feature);
 			if (changedList instanceof EList<?>) {
 				if (insertionIndex > changedList.size()) {
 					((EList<EObject>)changedList).move(changedList.size() - 1, newValue);
 				} else {
 					((EList<EObject>)changedList).move(insertionIndex, newValue);
 				}
 			} else {
 				changedList.remove(newValue);
 				if (insertionIndex > changedList.size()) {
 					changedList.add(newValue);
 				} else {
 					changedList.add(insertionIndex, newValue);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Seeks a match of the given {@code element} in the given list, using the equality helper to find it.
 	 * This is only used when moving or deleting proxies for now.
 	 * 
 	 * @param comparison
 	 *            The comparison which Diff we are currently merging.
 	 * @param list
 	 *            The list from which we seek a value.
 	 * @param element
 	 *            The value for which we need a match in {@code list}.
 	 * @return The match of {@code element} in {@code list}, {@code null} if none.
 	 */
 	protected EObject findMatchIn(Comparison comparison, List<EObject> list, EObject element) {
 		final IEqualityHelper helper = comparison.getEqualityHelper();
 		final Iterator<EObject> it = list.iterator();
 		while (it.hasNext()) {
 			final EObject next = it.next();
 			if (helper.matchingValues(next, element)) {
 				return next;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This will be used by the distinct merge actions in order to find the index at which a value should be
 	 * inserted in its target list. See {@link DiffUtil#findInsertionIndex(Comparison, Diff, boolean)} for
 	 * more on this.
 	 * <p>
 	 * Sub-classes can override this if the insertion order is irrelevant. A return value of {@code -1} will
 	 * be considered as "no index" and the value will be inserted at the end of its target list.
 	 * </p>
 	 * 
 	 * @param comparison
 	 *            This will be used in order to retrieve the Match for EObjects when comparing them.
 	 * @param diff
 	 *            The diff which merging will trigger the need for an insertion index in its target list.
 	 * @param rightToLeft
 	 *            {@code true} if the merging will be done into the left list, so that we should consider the
 	 *            right model as the source and the left as the target.
 	 * @return The index at which this {@code diff}'s value should be inserted into the 'target' list, as
 	 *         inferred from {@code rightToLeft}. {@code -1} if the value should be inserted at the end of its
 	 *         target list.
 	 * @see DiffUtil#findInsertionIndex(Comparison, Diff, boolean)
 	 */
 	protected int findInsertionIndex(Comparison comparison, Diff diff, boolean rightToLeft) {
 		return DiffUtil.findInsertionIndex(comparison, diff, rightToLeft);
 	}
 }
