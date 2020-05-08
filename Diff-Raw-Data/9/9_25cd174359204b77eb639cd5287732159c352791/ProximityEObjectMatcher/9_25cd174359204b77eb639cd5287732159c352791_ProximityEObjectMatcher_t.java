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
 package org.eclipse.emf.compare.match.eobject;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.common.util.Monitor;
 import org.eclipse.emf.compare.CompareFactory;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.match.eobject.EObjectIndex.Side;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * This matcher is using a distance function to match EObject. It guarantees that elements are matched with
  * the other EObject having the lowest distance. If two elements have the same distance regarding the other
  * EObject it will arbitrary pick one. (You should probably not rely on this and make sure your distance only
  * return 0 if both EObject have the very same content). The matcher will try to use the fact that it is a
  * distance to achieve a suitable scalability. It is also build on the following assumptions :
  * <ul>
  * <li>Most EObjects have no difference and have their corresponding EObject on the other sides of the model
  * (right and origins)</li>
  * <li>Two consecutive calls on the distance function with the same parameters will give the same distance.</li>
  * </ul>
  * The scalability you'll get will highly depend on the complexity of the distance function. The
  * implementation is not caching any distance result from two EObjects.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class ProximityEObjectMatcher implements IEObjectMatcher, ScopeQuery {
 
 	/**
 	 * The index which keep the EObjects.
 	 */
 	private EObjectIndex index;
 
 	/**
 	 * Keeps track of which side was the EObject from.
 	 */
 	private Map<EObject, Side> eObjectsToSide = Maps.newHashMap();
 
 	/**
 	 * Create the matcher using the given distance function.
 	 * 
 	 * @param meter
 	 *            a function to measure the distance between two {@link EObject}s.
 	 */
 	public ProximityEObjectMatcher(DistanceFunction meter) {
 		this.index = new ByTypeIndex(meter, this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 
 	public void createMatches(Comparison comparison, Iterator<? extends EObject> leftEObjects,
 			Iterator<? extends EObject> rightEObjects, Iterator<? extends EObject> originEObjects,
 			Monitor monitor) {
 
		// FIXME: how to create an EMF submonitor
		Monitor subMonitor = new BasicMonitor();
 		subMonitor.beginTask("indexing objects", 1);
 		int nbElements = 0;
 		/*
 		 * We are iterating through the three sides of the scope at the same time so that index might apply
 		 * pre-matching strategies elements if they wish.
 		 */
 		while (leftEObjects.hasNext() || rightEObjects.hasNext() || leftEObjects.hasNext()) {
 
 			if (leftEObjects.hasNext()) {
 				EObject next = leftEObjects.next();
 				nbElements++;
 				index.index(next, Side.LEFT);
 				eObjectsToSide.put(next, Side.LEFT);
 			}
 
 			if (rightEObjects.hasNext()) {
 				EObject next = rightEObjects.next();
 				nbElements++;
 				index.index(next, Side.RIGHT);
 				eObjectsToSide.put(next, Side.RIGHT);
 			}
 
 			if (originEObjects.hasNext()) {
 				EObject next = originEObjects.next();
 				nbElements++;
 				index.index(next, Side.ORIGIN);
 				eObjectsToSide.put(next, Side.ORIGIN);
 			}
 		}
 
 		subMonitor.worked(1);
 		subMonitor.done();
 
		// FIXME: how to create an EMF submonitor
		subMonitor = new BasicMonitor();
 		subMonitor.beginTask("matching objects", nbElements);
 
 		Iterator<EObject> todo = index.getValuesStillThere(Side.LEFT).iterator();
 		while (todo.hasNext()) {
 			Iterator<EObject> remainingResult = matchList(comparison, todo, subMonitor).iterator();
 			todo = remainingResult;
 
 		}
 		todo = index.getValuesStillThere(Side.RIGHT).iterator();
 		while (todo.hasNext()) {
 			Iterator<EObject> remainingResult = matchList(comparison, todo, subMonitor).iterator();
 			todo = remainingResult;
 		}
 
 		for (EObject notFound : index.getValuesStillThere(Side.RIGHT)) {
 			areMatching(comparison, null, notFound, null);
 		}
 		for (EObject notFound : index.getValuesStillThere(Side.LEFT)) {
 			areMatching(comparison, notFound, null, null);
 		}
 		for (EObject notFound : index.getValuesStillThere(Side.ORIGIN)) {
 			areMatching(comparison, null, null, notFound);
 		}
 
 		subMonitor.done();
 		restructureMatchModel(comparison);
 
 	}
 
 	/**
 	 * Process the list of objects matching them. This method might not be able to process all the EObjects if
 	 * - for instance, their container has not been matched already. Every object which could not be matched
 	 * is returned in the list.
 	 * 
 	 * @param comparison
 	 *            the comparison being built.
 	 * @param todo
 	 *            the list of objects to process.
 	 * @param monitor
 	 *            a monitor to track progress.
 	 * @return the list of
 	 */
 	private Iterable<EObject> matchList(Comparison comparison, Iterator<EObject> todo, Monitor monitor) {
 		List<EObject> remainingResult = Lists.newArrayList();
 		while (todo.hasNext()) {
 			EObject next = todo.next();
 			if (next.eContainer() == null || comparison.getMatch(next.eContainer()) != null
 					|| !isInScope(next.eContainer())) {
 				if (!tryToMatch(comparison, next)) {
 					remainingResult.add(next);
 				}
 				monitor.worked(1);
 			} else {
 				remainingResult.add(next);
 			}
 		}
 		return remainingResult;
 	}
 
 	/**
 	 * Try to create a Match. If the match got created, register it (having actual left/right/origin matches
 	 * or not), if not, then return false. Cases where it might not create the match : if some required data
 	 * has not been computed yet (for instance if the container of an object has not been matched and if the
 	 * distance need to know if it's match to find the children matches).
 	 * 
 	 * @param comparison
 	 *            the comparison under construction, it will be updated with the new match.
 	 * @param a
 	 *            object to match.
 	 * @return false if the conditions are not fulfilled to create the match, true otherwhise.
 	 */
 	private boolean tryToMatch(Comparison comparison, EObject a) {
 		Side aSide = eObjectsToSide.get(a);
 		assert aSide != null;
 		Side bSide = Side.LEFT;
 		Side cSide = Side.RIGHT;
 		if (aSide == Side.RIGHT) {
 			bSide = Side.LEFT;
 			cSide = Side.ORIGIN;
 		} else if (aSide == Side.LEFT) {
 			bSide = Side.RIGHT;
 			cSide = Side.ORIGIN;
 		} else if (aSide == Side.ORIGIN) {
 			bSide = Side.LEFT;
 			cSide = Side.RIGHT;
 		}
 		assert aSide != bSide;
 		assert bSide != cSide;
 		assert cSide != aSide;
 		Map<Side, EObject> closests = index.findClosests(comparison, a, aSide);
 		if (closests != null) {
 			EObject lObj = closests.get(bSide);
 			EObject aObj = closests.get(cSide);
 			areMatching(comparison, closests.get(Side.LEFT), closests.get(Side.RIGHT), closests
 					.get(Side.ORIGIN));
 			if (lObj != null) {
 				index.remove(lObj, Side.LEFT);
 			}
 			if (aObj != null) {
 				index.remove(aObj, Side.ORIGIN);
 			}
 			if (a != null) {
 				index.remove(a, Side.RIGHT);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Process all the matches of the given comparison and re-attach them to their parent if one is found.
 	 * 
 	 * @param comparison
 	 *            the comparison to restructure.
 	 */
 	private void restructureMatchModel(Comparison comparison) {
 		Iterator<Match> it = ImmutableList.copyOf(Iterators.filter(comparison.eAllContents(), Match.class))
 				.iterator();
 
 		while (it.hasNext()) {
 			Match cur = it.next();
 			EObject possibleContainer = null;
 			if (cur.getLeft() != null) {
 				possibleContainer = cur.getLeft().eContainer();
 			}
 			if (possibleContainer == null && cur.getRight() != null) {
 				possibleContainer = cur.getRight().eContainer();
 			}
 			if (possibleContainer == null && cur.getOrigin() != null) {
 				possibleContainer = cur.getOrigin().eContainer();
 			}
 			Match possibleContainerMatch = comparison.getMatch(possibleContainer);
 			if (possibleContainerMatch != null) {
 				((BasicEList<Match>)possibleContainerMatch.getSubmatches()).addUnique(cur);
 			}
 		}
 	}
 
 	/**
 	 * Register the given object as a match and add it in the comparison.
 	 * 
 	 * @param comparison
 	 *            container for the Match.
 	 * @param left
 	 *            left element.
 	 * @param right
 	 *            right element
 	 * @param origin
 	 *            origin element.
 	 * @return the created match.
 	 */
 	private Match areMatching(Comparison comparison, EObject left, EObject right, EObject origin) {
 		Match result = CompareFactory.eINSTANCE.createMatch();
 		result.setLeft(left);
 		result.setRight(right);
 		result.setOrigin(origin);
 		((BasicEList<Match>)comparison.getMatches()).addUnique(result);
 		if (left != null) {
 			index.remove(left, Side.LEFT);
 		}
 		if (right != null) {
 			index.remove(right, Side.RIGHT);
 		}
 		if (origin != null) {
 			index.remove(origin, Side.ORIGIN);
 		}
 		return result;
 	}
 
 	/**
 	 * This represent a distance function used by the {@link ProximityEObjectMatcher} to compare EObjects and
 	 * retrieve the closest EObject from one side to another. Axioms of the distance are supposed to be
 	 * respected more especially :
 	 * <ul>
 	 * <li>symetry : dist(a,b) == dist(b,a)</li>
 	 * <li>separation :dist(a,a) == 0</li>
 	 * </ul>
 	 * Triangular inequality is not leveraged with the current implementation but might be at some point to
 	 * speed up the indexing. <br/>
 	 * computing the distance between two EObjects should be a <b> fast operation</b> or the scalability of
 	 * the whole matching phase will be poor.
 	 * 
 	 * @author cedric brun <cedric.brun@obeo.fr>
 	 */
 	public interface DistanceFunction {
 		/**
 		 * Return the distance between two EObjects. When the two objects should considered as completely
 		 * different the implementation is expected to return Integer.MAX_VALUE.
 		 * 
 		 * @param inProgress
 		 *            the comparison being processed right now. This might be used for the distance to
 		 *            retrieve other matches for instance.
 		 * @param a
 		 *            first object.
 		 * @param b
 		 *            second object.
 		 * @return the distance between the two EObjects or Integer.MAX_VALUE when the objects are considered
 		 *         too different to be the same.
 		 */
 		double distance(Comparison inProgress, EObject a, EObject b);
 
 		/**
 		 * Check that two objects are equals from the distance function point of view (distance should be 0)
 		 * You should prefer this method when you just want to check objects are not equals enabling the
 		 * distance to stop sooner.
 		 * 
 		 * @param inProgress
 		 *            the comparison being processed right now. This might be used for the distance to
 		 *            retrieve other matches for instance.
 		 * @param a
 		 *            first object.
 		 * @param b
 		 *            second object.
 		 * @return true of the two objects are equals, false otherwise.
 		 */
 		boolean areIdentic(Comparison inProgress, EObject a, EObject b);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isInScope(EObject eContainer) {
 		return eObjectsToSide.get(eContainer) != null;
 	}
 }
