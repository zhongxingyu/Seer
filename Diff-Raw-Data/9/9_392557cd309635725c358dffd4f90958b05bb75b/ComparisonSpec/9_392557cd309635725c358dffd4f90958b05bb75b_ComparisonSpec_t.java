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
 package org.eclipse.emf.compare.internal.spec;
 
 import static com.google.common.collect.Iterables.filter;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.AbstractEList;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.impl.ComparisonImpl;
 import org.eclipse.emf.compare.internal.DiffCrossReferencer;
 import org.eclipse.emf.compare.internal.MatchCrossReferencer;
 import org.eclipse.emf.compare.match.DefaultMatchEngine;
 import org.eclipse.emf.compare.utils.EqualityHelper;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * This specialization of the {@link ComparisonImpl} class allows us to define the derived features and
  * operations implementations.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ComparisonSpec extends ComparisonImpl {
 	/** Keeps a reference to our match cross referencer. */
 	private MatchCrossReferencer matchCrossReferencer;
 
 	/** Keeps a reference to our diff cross referencer. */
 	private DiffCrossReferencer diffCrossReferencer;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.impl.ComparisonImpl#getDifferences()
 	 */
 	@Override
 	public EList<Diff> getDifferences() {
 		final Iterator<Diff> diffIterator = Iterators.filter(eAllContents(), Diff.class);
 
 		final EList<Diff> allDifferences = new BasicEList<Diff>();
 		while (diffIterator.hasNext()) {
 			((AbstractEList<Diff>)allDifferences).addUnique(diffIterator.next());
 		}
 
 		return allDifferences;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.impl.ComparisonImpl#getDifferences(org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public EList<Diff> getDifferences(EObject element) {
 		if (element == null) {
 			return new BasicEList<Diff>();
 		}
 
 		if (diffCrossReferencer == null) {
 			diffCrossReferencer = new DiffCrossReferencer();
 			eAdapters().add(diffCrossReferencer);
 		}
 
		Iterable<Diff> diffOnElement = filter(getInverse(element, diffCrossReferencer), Diff.class);
 		final Match match = getMatch(element);
 		if (match != null) {
 			Iterable<Diff> left = ImmutableList.of();
 			Iterable<Diff> right = ImmutableList.of();
 			Iterable<Diff> origin = ImmutableList.of();
 			if (match.getLeft() != null) {
 				left = filter(getInverse(match.getLeft(), diffCrossReferencer), Diff.class);
 			}
 			if (match.getRight() != null) {
 				right = filter(getInverse(match.getRight(), diffCrossReferencer), Diff.class);
 			}
 			if (match.getOrigin() != null) {
 				origin = filter(getInverse(match.getOrigin(), diffCrossReferencer), Diff.class);
 			}
			Set<Diff> crossRefs = ImmutableSet.copyOf(Iterables.concat(diffOnElement, left, right, origin));
 			return new BasicEList<Diff>(crossRefs);
		} else {
			return new BasicEList<Diff>(ImmutableSet.copyOf(diffOnElement));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.impl.ComparisonImpl#getMatch(org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public Match getMatch(EObject element) {
 		if (element == null) {
 			return null;
 		}
 
 		if (matchCrossReferencer == null) {
 			matchCrossReferencer = new MatchCrossReferencer();
 			eAdapters().add(matchCrossReferencer);
 		}
 		Iterable<Match> crossRefs = filter(getInverse(element, matchCrossReferencer), Match.class);
 
 		return Iterables.getFirst(crossRefs, null);
 	}
 
 	/**
 	 * Returns an {@link Iterable} of EObject being inverse references of the given {@code element} stored by
 	 * the {@code adapter}.
 	 * 
 	 * @param element
 	 *            the target of the search cross references.
 	 * @param adapter
 	 *            the {@link ECrossReferenceAdapter} to use to look for inverse references.
 	 * @return a possibly empty {@link Iterable} of inverse references.
 	 */
 	private Iterable<EObject> getInverse(EObject element, ECrossReferenceAdapter adapter) {
 		final Collection<EStructuralFeature.Setting> settings = adapter.getInverseReferences(element, false);
 		final List<EObject> eObjects = Lists.newArrayList();
 		for (EStructuralFeature.Setting setting : settings) {
 			eObjects.add(setting.getEObject());
 		}
 		return eObjects;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.impl.ComparisonImpl#getEqualityHelper()
 	 */
 	@Override
 	public IEqualityHelper getEqualityHelper() {
 		IEqualityHelper ret = (IEqualityHelper)EcoreUtil.getExistingAdapter(this, IEqualityHelper.class);
 		if (ret == null) {
 			ret = new EqualityHelper(EqualityHelper.createDefaultCache(CacheBuilder.newBuilder().maximumSize(
 					DefaultMatchEngine.DEFAULT_EOBJECT_URI_CACHE_MAX_SIZE)));
 			this.eAdapters().add(ret);
 			ret.setTarget(this);
 		}
 		return ret;
 	}
 }
