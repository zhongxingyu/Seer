 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 import java.lang.reflect.Array;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 // TODO this probably doesn't handle feature maps. Test with an XSD-based metamodel
 // TODO does not handle proxies yet (see fixmes)
 // TODO does not handle ordering yet (fixme placed at every needed location)
 /**
  * The diff engine is in charge of actually computing the differences between the objects mapped by a
  * {@link Match} object.
  * <p>
  * This default implementation aims at being generic enough to be used for any model, whatever the metamodel.
  * However, specific differences, refinements of differences or even higher level differences might be
  * necessary.
  * </p>
  * TODO document available extension possibilities.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class DefaultDiffEngine implements IDiffEngine {
 	/**
 	 * We'll use this "placeholder" to differentiate the unmatched elements from the "null" values that
 	 * attributes can legitimately use.
 	 */
 	protected static final Object UNMATCHED_VALUE = new Object();
 
 	/**
 	 * The comparison for which we are detecting differences. Should only be accessed through
 	 * {@link #getComparison()}.
 	 */
 	private Comparison currentComparison;
 
 	/**
 	 * The diff processor that will be used by this engine. Should only be instantiated through
 	 * {@link #createDiffProcessor()} and accessed by {@link #getDiffProcessor()}.
 	 */
 	private IDiffProcessor diffProcessor;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.IDiffEngine#diff(org.eclipse.emf.compare.Comparison)
 	 */
 	public void diff(Comparison comparison) {
 		this.currentComparison = comparison;
 		diffProcessor = createDiffProcessor();
 
 		for (Match rootMatch : comparison.getMatches()) {
 			checkForDifferences(rootMatch);
 		}
 	}
 
 	/**
 	 * Checks the given {@link Match}'s sides for potential differences. Will recursively check for
 	 * differences on submatches.
 	 * 
 	 * @param match
 	 *            The match that is to be checked.
 	 */
 	protected void checkForDifferences(Match match) {
 		final FeatureFilter featureFilter = createFeatureFilter();
 
 		final Iterator<EReference> references = featureFilter.getReferencesToCheck(match);
 		while (references.hasNext()) {
 			final EReference reference = references.next();
 			final boolean considerOrdering = featureFilter.checkForOrderingChanges(reference);
 			if (reference.isContainment()) {
 				computeContainmentDifference(match, reference, considerOrdering);
 			} else {
 				computeDifferences(match, reference, considerOrdering);
 			}
 		}
 
 		final Iterator<EAttribute> attributes = featureFilter.getAttributesToCheck(match);
 		while (attributes.hasNext()) {
 			final EAttribute attribute = attributes.next();
 			final boolean considerOrdering = featureFilter.checkForOrderingChanges(attribute);
 			computeDifferences(match, attribute, considerOrdering);
 		}
 
 		for (Match submatch : match.getSubmatches()) {
 			checkForDifferences(submatch);
 		}
 	}
 
 	/**
 	 * Computes the difference between the sides of the given <code>match</code> for the given
 	 * <code>attribute</code>.
 	 * 
 	 * @param match
 	 *            The match which sides we need to check for potential differences.
 	 * @param attribute
 	 *            The attribute which values are to be checked.
 	 * @param checkOrdering
 	 *            <code>true</code> if we should consider ordering changes on this attribute,
 	 *            <code>false</code> otherwise.
 	 */
 	protected void computeDifferences(Match match, EAttribute attribute, boolean checkOrdering) {
 		// This default implementation does not care about "attribute" changes on unmatched elements
 		if (match.getOrigin() == null && (match.getLeft() == null || match.getRight() == null)) {
 			return;
 		}
 
 		if (!attribute.isMany()) {
 			computeSingleValuedAttributeDifference(match, attribute);
 		} else {
 			computeMultiValuedAttributeDifferences(match, attribute, checkOrdering);
 		}
 	}
 
 	/**
 	 * Computes the difference between the sides of the given <code>match</code> for the given single-valued
 	 * <code>attribute</code>.
 	 * 
 	 * @param match
 	 *            The match which sides we need to check for potential differences.
 	 * @param attribute
 	 *            The attribute which values are to be checked.
 	 */
 	protected void computeSingleValuedAttributeDifference(Match match, EAttribute attribute) {
 		Object leftValue = UNMATCHED_VALUE;
 		if (match.getLeft() != null) {
 			leftValue = match.getLeft().eGet(attribute);
 		}
 		Object rightValue = UNMATCHED_VALUE;
 		if (match.getRight() != null) {
 			rightValue = match.getRight().eGet(attribute);
 		}
 
 		if (matchingValues(leftValue, rightValue)) {
 			return;
 		}
 
 		if (match.getOrigin() != null) {
 			final Object originValue = match.getOrigin().eGet(attribute);
 
 			if (matchingValues(leftValue, originValue)) {
 				if (rightValue != UNMATCHED_VALUE) {
 					// Value is in left and origin, but not in the right
 					getDiffProcessor().attributeChange(match, attribute, rightValue, DifferenceKind.CHANGE,
 							DifferenceSource.RIGHT);
 				} else {
 					// Right is unmatched, left is the same as in the origin. No diff here : the diff is on
 					// the match itself, not on one of its attributes.
 				}
 			} else if (matchingValues(rightValue, originValue)) {
 				if (leftValue != UNMATCHED_VALUE) {
 					// Value is in right and origin, but not in left
 					getDiffProcessor().attributeChange(match, attribute, leftValue, DifferenceKind.CHANGE,
 							DifferenceSource.LEFT);
 				} else {
 					// Left is unmatched, right is the same as in the origin. No diff here : the diff is on
 					// the match itself, not on one of its attributes.
 				}
 			} else {
 				/*
 				 * Left and right are different. None match what's in the origin. Those of the two that are
 				 * not unmatched are thus a "change" difference, with a possible conflict.
 				 */
 				if (leftValue != UNMATCHED_VALUE) {
 					getDiffProcessor().attributeChange(match, attribute, leftValue, DifferenceKind.CHANGE,
 							DifferenceSource.LEFT);
 				}
 				if (rightValue != UNMATCHED_VALUE) {
 					getDiffProcessor().attributeChange(match, attribute, rightValue, DifferenceKind.CHANGE,
 							DifferenceSource.RIGHT);
 				}
 			}
 		} else {
 			// None of left and right are unmatched; they are different, and we have no origin
 			getDiffProcessor().attributeChange(match, attribute, leftValue, DifferenceKind.CHANGE,
 					DifferenceSource.LEFT);
 		}
 	}
 
 	/**
 	 * Computes the difference between the sides of the given <code>match</code> for the given multi-valued
 	 * <code>attribute</code>.
 	 * 
 	 * @param match
 	 *            The match which sides we need to check for potential differences.
 	 * @param attribute
 	 *            The attribute which values are to be checked.
 	 * @param checkOrdering
 	 *            <code>true</code> if we should consider ordering changes on this reference,
 	 *            <code>false</code> otherwise.
 	 */
 	protected void computeMultiValuedAttributeDifferences(Match match, EAttribute attribute,
 			boolean checkOrdering) {
 		// This will only be used for iteration, once. Use the original list
 		final Iterable<Object> leftValues = getValue(match.getLeft(), attribute);
 
 		/*
 		 * We might iterate a lot over these two. Since we'll remove objects from them in order to keep the
 		 * length of iterations as short as possible, we'll use two linked sets.
 		 */
 		final Set<Object> rightValues = Sets.newLinkedHashSet(getValue(match.getRight(), attribute));
 		final Set<Object> originValues = Sets.newLinkedHashSet(getValue(match.getOrigin(), attribute));
 
 		for (Object left : leftValues) {
 			final Object rightMatch = findMatch(left, rightValues);
 
 			if (rightMatch == UNMATCHED_VALUE) {
 				final Object originMatch = findMatch(left, originValues);
 				// no need to check if three way for these
 				if (originMatch != UNMATCHED_VALUE) {
 					// The value is in the left and origin, but not in the right.
 					getDiffProcessor().attributeChange(match, attribute, left, DifferenceKind.DELETE,
 							DifferenceSource.RIGHT);
 					// FIXME There could also be an ordering change between left and origin
 					originValues.remove(originMatch);
 				} else {
 					// Value is in left, but not in either right or origin
 					getDiffProcessor().attributeChange(match, attribute, left, DifferenceKind.ADD,
 							DifferenceSource.LEFT);
 				}
 			} else {
 				// Value is present in both left and right sides. We can only have a diff on ordering
 				// FIXME check ordering
 				rightValues.remove(rightMatch);
 				originValues.remove(findMatch(left, originValues));
 			}
 		}
 
 		// We've updated the right list as we matched objects. The remaining are diffs.
 		for (Object right : rightValues) {
 			final Object originMatch = findMatch(right, originValues);
 			// Even with no match in the origin, source is the left side if this is not a three way
 			// comparison
 			if (originMatch != UNMATCHED_VALUE || !getComparison().isThreeWay()) {
 				getDiffProcessor().attributeChange(match, attribute, right, DifferenceKind.DELETE,
 						DifferenceSource.LEFT);
 				// FIXME if originMatch != UNMATCHED_VALUE then check ordering between right and origin
 			} else {
 				getDiffProcessor().attributeChange(match, attribute, right, DifferenceKind.ADD,
 						DifferenceSource.RIGHT);
 			}
 		}
 	}
 
 	/**
 	 * Computes the differences between the sides of the given <code>match</code> for the given
 	 * <code>reference</code>.
 	 * <p>
 	 * Note that once here, we know that <code>reference</code> is not a containment reference.
 	 * </p>
 	 * 
 	 * @param match
 	 *            The match which sides we need to check for potential differences.
 	 * @param reference
 	 *            The reference which values are to be checked.
 	 * @param checkOrdering
 	 *            <code>true</code> if we should consider ordering changes on this reference,
 	 *            <code>false</code> otherwise.
 	 */
 	protected void computeDifferences(Match match, EReference reference, boolean checkOrdering) {
 		// This will only be used for iteration, once. use the original list
 		final Iterable<EObject> leftValues = Iterables.filter(getValue(match.getLeft(), reference),
 				EObject.class);
 		/*
 		 * These two will be used mainly for lookup, we'll transform them to Sets here. Linked set for the
 		 * rightValues since we'll iterate over its values once, a plain hash set for the origin as it will
 		 * only be used for lookup purposes.
 		 */
 		// NOTE : Do not use the original list for rightValues : we will remove objects from it.
 		final Set<EObject> rightValues = Sets.newLinkedHashSet(Iterables.filter(getValue(match.getRight(),
 				reference), EObject.class));
 		final Set<EObject> originValues = Sets.newHashSet(Iterables.filter(getValue(match.getOrigin(),
 				reference), EObject.class));
 
 		for (EObject value : leftValues) {
 			final Match valueMatch = getComparison().getMatch(value);
 
 			if (valueMatch != null) {
 				// Is this value present in the right side?
 				final EObject rightMatch = valueMatch.getRight();
 
 				if (rightMatch == null || !rightValues.contains(rightMatch)) {
 					final boolean originHasMatch = originValues.contains(valueMatch.getOrigin());
 					final DifferenceKind kind;
 					if (!reference.isMany()) {
 						kind = DifferenceKind.CHANGE;
 					} else if (originHasMatch) {
 						kind = DifferenceKind.DELETE;
 					} else {
 						kind = DifferenceKind.ADD;
 					}
 					// no need to check if three way for these
 					if (originHasMatch) {
 						// Value is in left and origin, though not in right.
 						getDiffProcessor().referenceChange(match, reference, value, kind,
 								DifferenceSource.RIGHT);
 						// FIXME if reference.isMany() then check ordering between left and origin
 					} else {
 						getDiffProcessor().referenceChange(match, reference, value, kind,
 								DifferenceSource.LEFT);
 					}
 				} else {
 					// Value is present in both left and right lists. We can only have a diff on ordering.
 					rightValues.remove(rightMatch);
 					// FIXME if reference.isMany() then check ordering between left and right
 				}
 			} else {
 				// this value is out of the comparison scope
 				// FIXME or could be a proxy : compare through URI
 			}
 		}
 
 		// We've updated the right list as we matched objects. The remaining are diffs.
 		for (EObject value : rightValues) {
 			final Match valueMatch = getComparison().getMatch(value);
 
 			if (valueMatch != null) {
 				final boolean originHasMatch = originValues.contains(valueMatch.getOrigin());
 				if (!reference.isMany()) {
					if (match.getLeft() == null) {
						getDiffProcessor().referenceChange(match, reference, value, DifferenceKind.CHANGE,
								DifferenceSource.LEFT);
					} else if (getComparison().isThreeWay()) {
						getDiffProcessor().referenceChange(match, reference, value, DifferenceKind.CHANGE,
								DifferenceSource.RIGHT);
					}
 				} else if (originHasMatch || !getComparison().isThreeWay()) {
 					// Even with no match in the origin, source is left side if not in a three way comparison
 					getDiffProcessor().referenceChange(match, reference, value, DifferenceKind.DELETE,
 							DifferenceSource.LEFT);
 				} else {
 					getDiffProcessor().referenceChange(match, reference, value, DifferenceKind.ADD,
 							DifferenceSource.RIGHT);
 				}
 			} else {
 				// this value is out of the comparison scope
 				// FIXME or could be a proxy : compare through URI
 			}
 		}
 	}
 
 	/**
 	 * Containment differences will be treated differently than plain references since the corresponding Match
 	 * elements themselves contain all information we might need for difference detection on these, ordering
 	 * excepted.
 	 * 
 	 * @param match
 	 *            The match which sides we need to check for potential differences.
 	 * @param reference
 	 *            The reference which values are to be checked.
 	 * @param checkOrdering
 	 *            <code>true</code> if we should consider ordering changes on this reference,
 	 *            <code>false</code> otherwise.
 	 */
 	protected void computeContainmentDifference(Match match, EReference reference, boolean checkOrdering) {
 		// This will only be used for iteration, once. use the original list
 		final Iterable<EObject> leftValues = Iterables.filter(getValue(match.getLeft(), reference),
 				EObject.class);
 
 		for (EObject leftValue : leftValues) {
 			final Match valueMatch = getComparison().getMatch(leftValue);
 
 			if (valueMatch != null) {
 				computeContainmentDiffForLeftValue(match, reference, valueMatch, checkOrdering);
 			} else {
 				// this value is out of the comparison scope
 				// FIXME or could be a proxy : compare through URI
 			}
 		}
 
 		// This will only be used for iteration, once. use the original list
 		final Iterable<EObject> rightValues = Iterables.filter(getValue(match.getRight(), reference),
 				EObject.class);
 
 		for (EObject rightValue : rightValues) {
 			final Match valueMatch = getComparison().getMatch(rightValue);
 
 			if (valueMatch == null || valueMatch.getLeft() != null
 					&& isContainedBy(match.getLeft(), reference, valueMatch.getLeft())) {
 				// Either out of scope or handled by the iteration on the left side
 				// FIXME or could be a proxy : compare through URI
 			} else {
 				computeContainmentDiffForRightValue(match, reference, valueMatch, checkOrdering);
 			}
 		}
 	}
 
 	/**
 	 * This will be called by the diff engine to compute containment differences on the given
 	 * <code>value</code>, which {@link Match#getLeft() left side} is known to be in the
 	 * <code>parent.reference</code>'s content list.
 	 * <p>
 	 * The necessary sanity checks have been made to know that <code>value.getLeft()</code> is not
 	 * <code>null</code> and part of <code>parent.eGet(reference)</code>. We also know that reference is a
 	 * containment reference.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            The Match which containment references we are currently checking for differences.
 	 * @param reference
 	 *            The reference of which content <code>value</code> is known to be a part.
 	 * @param value
 	 *            The value of the given containment reference which is to be checked here.
 	 * @param checkOrdering
 	 *            <code>true</code> if we should consider ordering changes on this reference,
 	 *            <code>false</code> otherwise.
 	 */
 	protected void computeContainmentDiffForLeftValue(Match parent, EReference reference, Match value,
 			boolean checkOrdering) {
 		final EObject leftValue = value.getLeft();
 		final EObject rightValue = value.getRight();
 		final EObject originValue = value.getOrigin();
 
 		if (rightValue == null) {
 			if (originValue == null) {
 				// This value has been added in the left side
 				getDiffProcessor().referenceChange(parent, reference, leftValue, DifferenceKind.ADD,
 						DifferenceSource.LEFT);
 			} else if (isContainedBy(parent.getOrigin(), reference, originValue)) {
 				// This value is also in the origin, in the same container. It has thus been removed
 				// from the right side.
 				getDiffProcessor().referenceChange(parent, reference, leftValue, DifferenceKind.DELETE,
 						DifferenceSource.RIGHT);
 				// FIXME check ordering between left and origin
 			} else {
 				/*
 				 * This value is also in the origin, it has thus been deleted from the right side. But it was
 				 * not in the same container; so there is also a MOVE difference in the left side. The delete
 				 * of the right side, however, is in another match element. It will be detected later on.
 				 */
 				getDiffProcessor().referenceChange(parent, reference, leftValue, DifferenceKind.MOVE,
 						DifferenceSource.LEFT);
 			}
 		} else if (originValue == null) {
 			// This value is in both left and right sides, but not in the origin
 			if (isContainedBy(parent.getRight(), reference, rightValue)) {
 				// they are in the same container; so there's actually no diff here : the same
 				// modification has been made in both sides
 				// FIXME check ordering between right and left
 			} else {
 				/*
 				 * This is a conflict if a three way comparison : same object added in two distinct
 				 * containers. The addition in the right side, however, is in another match element and will
 				 * be detected later on.
 				 */
 				getDiffProcessor().referenceChange(parent, reference, leftValue, DifferenceKind.ADD,
 						DifferenceSource.LEFT);
 			}
 		} else {
 			// This value is on all three sides
 			if (isContainedBy(parent.getRight(), reference, rightValue)) {
 				/*
 				 * Value is in the same container on the right and left side. The container in the origin does
 				 * not matter in this case : the only diff possible is the ordering between left and right.
 				 */
 				// FIXME check ordering between left and right
 			} else if (!isContainedBy(parent.getOrigin(), reference, originValue)) {
 				/*
 				 * Value has changed container in left _and_ in right. The move on the right side will be
 				 * detected for another match.
 				 */
 				getDiffProcessor().referenceChange(parent, reference, leftValue, DifferenceKind.MOVE,
 						DifferenceSource.LEFT);
 			} else {
 				/*
 				 * Value is in the same container on left and origin. It has moved on the right side, but that
 				 * will be detected for another match.
 				 */
 				// FIXME check ordering between left and origin
 			}
 		}
 	}
 
 	/**
 	 * This will be called by the diff engine to compute containment differences on the given
 	 * <code>value</code>, which {@link Match#getRight() right side} is known to be in the
 	 * <code>parent.reference</code>'s content list.
 	 * <p>
 	 * The necessary sanity checks have been made to know that <code>value.getRight()</code> is not
 	 * <code>null</code> and part of <code>parent.getRight().eGet(reference)</code>. We also know that
 	 * <code>value.getLeft()</code> is <b>not</b> a part of <code>parent.getLeft().eGet(reference)</code>
 	 * since these have already been taken care of by
 	 * {@link #computeContainmentDiffForLeftValue(Match, EReference, Match, boolean)}.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            The Match which containment references we are currently checking for differences.
 	 * @param reference
 	 *            The reference of which content <code>value</code> is known to be a part.
 	 * @param value
 	 *            The value of the given containment reference which is to be checked here.
 	 * @param checkOrdering
 	 *            <code>true</code> if we should consider ordering changes on this reference,
 	 *            <code>false</code> otherwise.
 	 */
 	protected void computeContainmentDiffForRightValue(Match parent, EReference reference, Match value,
 			boolean checkOrdering) {
 		final EObject leftValue = value.getLeft();
 		final EObject rightValue = value.getRight();
 		final EObject originValue = value.getOrigin();
 
 		if (leftValue == null) {
 			if (originValue == null && getComparison().isThreeWay()) {
 				// This value has been added in the right side
 				getDiffProcessor().referenceChange(parent, reference, rightValue, DifferenceKind.ADD,
 						DifferenceSource.RIGHT);
 			} else if (originValue == null || isContainedBy(parent.getOrigin(), reference, originValue)) {
 				// Either we are doing a two way comparison or this value is also in the origin, in the same
 				// container. It has thus been removed from the left side.
 				getDiffProcessor().referenceChange(parent, reference, rightValue, DifferenceKind.DELETE,
 						DifferenceSource.LEFT);
 				// FIXME check ordering between right and origin
 			} else {
 				/*
 				 * This value is also in the origin, it has thus been deleted from the left side. But it was
 				 * not in the same container; so there is also a MOVE difference in the right side. The delete
 				 * of the left side, however, is in another match element. It will be detected later on.
 				 */
 				getDiffProcessor().referenceChange(parent, reference, rightValue, DifferenceKind.MOVE,
 						DifferenceSource.RIGHT);
 			}
 		} else if (originValue == null && getComparison().isThreeWay()) {
 			// This value is in both left and right sides, but not in the origin. We also know that they are
 			// not in the same container since we are on the path for the "right" match, which will only be
 			// called if right and left are in distinct containers.
 			/*
 			 * This is actually a conflict : same object added in two distinct containers. The addition in the
 			 * left side, however, will be detected for another match element.
 			 */
 			getDiffProcessor().referenceChange(parent, reference, rightValue, DifferenceKind.ADD,
 					DifferenceSource.RIGHT);
 		} else {
 			// This value is on all three sides, or we are doing a two way comparison
 			// Either way, the value is in distinct containers on left and right
 			if (getComparison().isThreeWay() && !isContainedBy(parent.getOrigin(), reference, originValue)) {
 				// value present on all sides, but its container has changed in the right side as
 				// compared to the origin
 				getDiffProcessor().referenceChange(parent, reference, rightValue, DifferenceKind.MOVE,
 						DifferenceSource.RIGHT);
 			} else {
 				// The value has been moved in the left side ... but that's in another Match element
 				// FIXME check ordering between right and origin
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the given <code>value</code> is contained within the given <code>container</code>,
 	 * through the given <code>containmentReference</code>.
 	 * 
 	 * @param container
 	 *            The expected container of <code>value</code>.
 	 * @param containmentReference
 	 *            The reference of <code>container</code> within which we expect to find <code>value</code>.
 	 * @param value
 	 *            The value which container we are checking.
 	 * @return <code>true</code> if the given value is contained within the expected reference of the expected
 	 *         container, <code>false</code> otherwise.
 	 */
 	protected static boolean isContainedBy(EObject container, EReference containmentReference, EObject value) {
 		return container == value.eContainer() && containmentReference == value.eContainmentFeature();
 	}
 
 	/**
 	 * This default implementation considers all values as lists. This utility simply allows us to retrieve
 	 * the value of a given value as an iterable.
 	 * 
 	 * @param object
 	 *            The object for which feature we need a value.
 	 * @param feature
 	 *            The actual feature of which we need the value.
 	 * @return The value of the given <code>feature</code> for the given <code>object</code> as a list. An
 	 *         empty list if this object has no value for that feature or if the object is <code>null</code>.
 	 */
 	@SuppressWarnings("unchecked")
 	protected static Iterable<Object> getValue(EObject object, EStructuralFeature feature) {
 		if (object != null) {
 			Object value = object.eGet(feature, false);
 			final Iterable<Object> asList;
 			if (value instanceof Iterable) {
 				asList = (Iterable<Object>)value;
 			} else {
 				asList = Collections.singleton(value);
 			}
 			return asList;
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * This will be used in order to create the diff processor that is to be used by this diff engine.
 	 * 
 	 * @return The diff processor of this diff engine. Will only be called once per call to
 	 *         {@link #diff(Comparison)}.
 	 */
 	protected IDiffProcessor createDiffProcessor() {
 		return new DiffBuilder();
 	}
 
 	/**
 	 * This will return the diff processor that has been created through {@link #createDiffProcessor()} for
 	 * this differencing process.
 	 * 
 	 * @return The diff processor to notify of difference detections.
 	 */
 	protected final IDiffProcessor getDiffProcessor() {
 		return diffProcessor;
 	}
 
 	/**
 	 * This will be used in order to create the {@link FeatureFilter} that should be used by this engine to
 	 * determine the structural features on which it is to try and detect differences.
 	 * 
 	 * @return The newly created feature filter.
 	 */
 	protected FeatureFilter createFeatureFilter() {
 		return new FeatureFilter();
 	}
 
 	/**
 	 * Returns the comparison for which we are currently detecting differences.
 	 * 
 	 * @return The comparison for which we are currently detecting differences.
 	 */
 	protected Comparison getComparison() {
 		return currentComparison;
 	}
 
 	/**
 	 * This will try and find a match to <code>reference</code> in the given list of <code>candidates</code>
 	 * by using the semantics of {@link #matchingValues(Object, Object)}.
 	 * 
 	 * @param reference
 	 *            The object we need to find in <code>candidates</code>.
 	 * @param candidates
 	 *            Potential matches for <code>reference</code>.
 	 * @return The match if we found any, {@link #UNMATCHED_VALUE} otherwise.
 	 */
 	protected Object findMatch(Object reference, Iterable<Object> candidates) {
 		for (Object candidate : candidates) {
 			if (matchingValues(reference, candidate)) {
 				return candidate;
 			}
 		}
 		return UNMATCHED_VALUE;
 	}
 
 	/**
 	 * Check that the two given values are "equal", considering the specifics of EMF.
 	 * 
 	 * @param object1
 	 *            First of the two objects to compare here.
 	 * @param object2
 	 *            Second of the two objects to compare here.
 	 * @return <code>true</code> if both objects are to be considered equal, <code>false</code> otherwise.
 	 */
 	protected boolean matchingValues(Object object1, Object object2) {
 		final boolean equal;
 		if (object1 == object2) {
 			equal = true;
 		} else if (object1 instanceof EEnumLiteral && object2 instanceof EEnumLiteral) {
 			final EEnumLiteral literal1 = (EEnumLiteral)object1;
 			final EEnumLiteral literal2 = (EEnumLiteral)object2;
 			final String value1 = literal1.getLiteral() + literal1.getValue();
 			final String value2 = literal2.getLiteral() + literal2.getValue();
 			equal = value1.equals(value2);
 		} else if (object1 instanceof EObject && object2 instanceof EObject) {
 			// [248442] This will handle FeatureMapEntries detection
 			final Match match = getComparison().getMatch((EObject)object1);
 			equal = match.getLeft() == object2 || match.getRight() == object2 || match.getOrigin() == object2;
 		} else if (object1 != null && object1.getClass().isArray() && object2 != null
 				&& object2.getClass().isArray()) {
 			// [299641] compare arrays by their content instead of instance equality
 			equal = matchingArrays(object1, object2);
 		} else {
 			equal = object1 != null && object1.equals(object2);
 		}
 		return equal;
 	}
 
 	/**
 	 * Compares two values as arrays, checking that their length and content match each other.
 	 * 
 	 * @param object1
 	 *            First of the two objects to compare here.
 	 * @param object2
 	 *            Second of the two objects to compare here.
 	 * @return <code>true</code> if these two arrays are to be considered equal, <code>false</code> otherwise.
 	 */
 	private boolean matchingArrays(Object object1, Object object2) {
 		boolean equal = true;
 		final int length1 = Array.getLength(object1);
 		if (length1 != Array.getLength(object2)) {
 			equal = true;
 		} else {
 			for (int i = 0; i < length1 && equal; i++) {
 				final Object element1 = Array.get(object1, i);
 				final Object element2 = Array.get(object2, i);
 				equal = matchingValues(element1, element2);
 			}
 		}
 		return equal;
 	}
 }
