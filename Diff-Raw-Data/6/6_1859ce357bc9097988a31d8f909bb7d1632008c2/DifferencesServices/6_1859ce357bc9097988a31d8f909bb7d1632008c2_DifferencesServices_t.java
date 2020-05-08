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
 package org.eclipse.emf.compare.match.statistic;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.internal.runtime.CompareProgressMonitor;
 import org.eclipse.emf.compare.match.Messages;
 import org.eclipse.emf.compare.match.api.MatchEngine;
 import org.eclipse.emf.compare.match.api.MatchOptions;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Element;
 import org.eclipse.emf.compare.match.metamodel.MatchFactory;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.RemoteUnMatchElement;
 import org.eclipse.emf.compare.match.metamodel.UnMatchElement;
 import org.eclipse.emf.compare.match.statistic.similarity.NameSimilarity;
 import org.eclipse.emf.compare.match.statistic.similarity.StructureSimilarity;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.compare.util.ETools;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 
 /**
  * These services are useful when one wants to compare models more precisely using the method modelDiff.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class DifferencesServices implements MatchEngine {
 	/**
 	 * Used while computing similarity, this defines the general threshold.
 	 */
 	private static final double GENERAL_THRESHOLD = 0.96d;
 
 	/** Minimal number of attributes an element must have for content comparison. */
 	private static final int MIN_ATTRIBUTES_COUNT = 5;
 
 	/** This constant is used as key for the buffering of name similarity. */
 	private static final char NAME_SIMILARITY = 'n';
 
 	/** This constant is used as key for the buffering of relations similarity. */
 	private static final char RELATION_SIMILARITY = 'r';
 
 	/** Containmnent reference for {@link MatchElement}s' submatches. */
 	private static final String SUBMATCH_ELEMENT_NAME = "subMatchElements"; //$NON-NLS-1$
 
 	/** This constant is used as key for the buffering of type similarity. */
 	private static final char TYPE_SIMILARITY = 't';
 
 	/** Containmnent reference for the {@link MatchModel}'s unmatched elements. */
 	private static final String UNMATCH_ELEMENT_NAME = "unMatchedElements"; //$NON-NLS-1$
 
 	/** This constant is used as key for the buffering of value similarity. */
 	private static final char VALUE_SIMILARITY = 'v';
 
 	/**
 	 * {@link MetamodelFilter} used for filtering unused features of the objects we're computing the
 	 * similarity for.
 	 */
 	protected final MetamodelFilter filter = new MetamodelFilter();
 
 	/** Contains the options given to the match procedure. */
 	protected final Map<String, Object> options = loadDefaultOptionMap();
 
 	/**
 	 * This map allows us memorize the {@link EObject} we've been able to match thanks to their XMI ID.
 	 */
 	private final Map<String, EObject> matchedByID = new EMFCompareMap<String, EObject>();
 
 	/**
 	 * This map is used to cache the comparison results Pair(Element1, Element2) => [nameSimilarity,
 	 * valueSimilarity, relationSimilarity, TypeSimilarity].
 	 */
 	private final Map<String, Double> metricsCache = new EMFCompareMap<String, Double>();
 
 	/**
 	 * This list allows us to memorize the unMatched elements for a three-way comparison.<br/>
 	 * <p>
 	 * More specifically, we will populate this list with the {@link UnMatchElement}s created by the
 	 * comparison between the left and the ancestor model, followed by the {@link UnMatchElement} created by
 	 * the comparison between the right and the ancestor model.<br/> Those {@link UnMatchElement} will then
 	 * be filtered to retain only those that actually cannot be matched.
 	 * </p>
 	 */
 	private final Set<EObject> remainingUnMatchedElements = new HashSet<EObject>();
 
 	/**
 	 * This list will be intensively used while matching elements to keep track of the unmatched ones from the
 	 * left model.
 	 */
 	private final List<EObject> stillToFindFromModel1 = new ArrayList<EObject>();
 
 	/**
 	 * This list will be intensively used while matching elements to keep track of the unmatched ones from the
 	 * right model.
 	 */
 	private final List<EObject> stillToFindFromModel2 = new ArrayList<EObject>();
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.api.MatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, org.eclipse.core.runtime.IProgressMonitor)
 	 * @deprecated Use {@link #modelMatch(EObject, EObject, IProgressMonitor, Map)} instead. This will be
 	 *             deleted before 1.0.
 	 */
 	@Deprecated
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, EObject ancestor,
 			IProgressMonitor monitor) throws InterruptedException {
 		return modelMatch(leftRoot, rightRoot, ancestor, new CompareProgressMonitor(monitor), Collections
 				.<String, Object> emptyMap());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.api.MatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.core.runtime.CompareProgressMonitor, java.util.Map)
 	 */
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, EObject ancestor,
 			IProgressMonitor monitor, Map<String, Object> optionMap) throws InterruptedException {
 		return modelMatch(leftRoot, rightRoot, ancestor, new CompareProgressMonitor(monitor), optionMap);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.api.MatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)
 	 */
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, EObject ancestor,
 			Map<String, Object> optionMap) {
 		MatchModel result = null;
 		try {
 			result = modelMatch(leftRoot, rightRoot, ancestor, new CompareProgressMonitor(), optionMap);
 		} catch (InterruptedException e) {
 			// cannot be thrown
 			assert false;
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a mapping model between the two other models. Basically the difference is computed this way :
 	 * <ul>
 	 * <li>Both models are browsed and compared, Mappings are created when two nodes are considered as
 	 * similar</li>
 	 * <li>Nodes which haven't been mapped are compared with each other in order to map them.</li>
 	 * <li>The mapping tree is browsed in order to determine the modification log.</li>
 	 * <li>The modification log (an EMF model) is then returned.</li>
 	 * </ul>
 	 * 
 	 * @param leftRoot
 	 *            Left model of the comparison.
 	 * @param rightRoot
 	 *            Right model of the comparison.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor Progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @return Mapping model of the two given models.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 * @see MatchEngine#modelMatch(EObject, EObject, IProgressMonitor)
 	 * @deprecated Use {@link #modelMatch(EObject, EObject, IProgressMonitor, Map)} instead. This will be
 	 *             deleted before 1.0.
 	 */
 	@Deprecated
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, IProgressMonitor monitor)
 			throws InterruptedException {
 		return modelMatch(leftRoot, rightRoot, new CompareProgressMonitor(monitor), Collections
 				.<String, Object> emptyMap());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.api.MatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, org.eclipse.core.runtime.CompareProgressMonitor, java.util.Map)
 	 */
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, IProgressMonitor monitor,
 			Map<String, Object> optionMap) throws InterruptedException {
 		return modelMatch(leftRoot, rightRoot, new CompareProgressMonitor(monitor), optionMap);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.api.MatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, java.util.Map)
 	 */
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, Map<String, Object> optionMap) {
 		MatchModel result = null;
 		try {
 			result = modelMatch(leftRoot, rightRoot, new CompareProgressMonitor(), optionMap);
 		} catch (InterruptedException e) {
 			// cannot be thrown
 			assert false;
 		}
 		return result;
 	}
 
 	/**
 	 * This will compute the similarity between two {@link EObject}s' contents.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @return <code>double</code> representing the similarity between the two {@link EObject}s' contents.
 	 *         0 &lt; value &lt; 1.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the {@link EObject}s' contents similarity metrics.
 	 * @see NameSimilarity#contentValue(EObject, MetamodelFilter)
 	 */
 	protected double contentSimilarity(EObject obj1, EObject obj2) throws FactoryException {
 		double similarity = 0d;
 		final Double value = getSimilarityFromCache(obj1, obj2, VALUE_SIMILARITY);
 		if (value != null) {
 			similarity = value;
 		} else {
 			similarity = NameSimilarity.nameSimilarityMetric(NameSimilarity.contentValue(obj1, filter),
 					NameSimilarity.contentValue(obj2, filter));
 			setSimilarityInCache(obj1, obj2, VALUE_SIMILARITY, similarity);
 		}
 		return similarity;
 	}
 
 	/**
 	 * This will return the value associated to the given key in the options map.
 	 * <p>
 	 * NOTE : Misuses of this method will easily throw {@link ClassCastException}s.
 	 * </p>
 	 * 
 	 * @param <T>
 	 *            Expected type of the value associated to <code>key</code>.
 	 * @param key
 	 *            Key of the value to retrieve.
 	 * @return Value associated to the given key in the options map.
 	 * @throws ClassCastException
 	 *             If the value isn't assignment compatible with the expected type.
 	 */
 	@SuppressWarnings("unchecked")
 	protected <T> T getOption(String key) throws ClassCastException {
 		return (T)options.get(key);
 	}
 
 	/**
 	 * This will compare two objects to see if they have ID and in that case, if these IDs are distinct.
 	 * 
 	 * @param left
 	 *            Left of the two objects to compare.
 	 * @param right
 	 *            right of the two objects to compare.
 	 * @return <code>True</code> if only one of the two objects has an ID or the two are distinct,
 	 *         <code>False</code> otherwise.
 	 */
 	protected boolean haveDistinctXMIID(EObject left, EObject right) {
 		boolean result = false;
 		String item1ID = null;
 		String item2ID = null;
 		if (left.eResource() != null && left.eResource() instanceof XMIResource)
 			item1ID = ((XMIResource)left.eResource()).getID(left);
 		if (right.eResource() != null && right.eResource() instanceof XMIResource)
 			item2ID = ((XMIResource)right.eResource()).getID(right);
 		if (item1ID != null) {
 			result = !item1ID.equals(item2ID);
 		} else {
 			result = item2ID != null;
 		}
 		/*
 		 * Now checking the Ecore ID's
 		 */
 		if (left.eClass().getEIDAttribute() != null && right.eClass().getEIDAttribute() != null) {
 			final Object leftID = left.eGet(left.eClass().getEIDAttribute());
 			final Object rightID = right.eGet(right.eClass().getEIDAttribute());
 			if (leftID != null && rightID != null && leftID.equals(rightID))
 				result = false;
 			else
 				result = true;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns <code>True</code> if the 2 given {@link EObject}s are considered similar.
 	 * 
 	 * @param obj1
 	 *            The first {@link EObject} to compare.
 	 * @param obj2
 	 *            Second of the {@link EObject}s to compare.
 	 * @return <code>True</code> if both elements have the same serialization ID, <code>False</code>
 	 *         otherwise.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute one of the needed similarity.
 	 */
 	protected boolean isSimilar(EObject obj1, EObject obj2) throws FactoryException {
 		boolean similar = false;
 
 		// Defines threshold constants to assume objects' similarity
 		final double fewerAttributesNameThreshold = 0.8d;
 		final double relationsThreshold = 0.9d;
 		final double nameThreshold = 0.2d;
 		final double contentThreshold = 0.9d;
 		final double triWayThreshold = 0.9d;
 		final double generalThreshold = GENERAL_THRESHOLD;
 
 		final double nameSimilarity = nameSimilarity(obj1, obj2);
 		final boolean hasSameUri = hasSameUri(obj1, obj2);
 		if (!this.<Boolean> getOption(MatchOptions.OPTION_DISTINCT_METAMODELS)
 				&& obj1.eClass() != obj2.eClass()) {
 			similar = false;
 		} else if (!this.<Boolean> getOption(MatchOptions.OPTION_IGNORE_XMI_ID)
 				&& haveDistinctXMIID(obj1, obj2)) {
 			similar = false;
 		} else if (nameSimilarity == 1 && hasSameUri) {
 			similar = true;
 			// softer test if we don't have enough attributes to compare the objects
 		} else if (nameSimilarity > fewerAttributesNameThreshold
 				&& nonNullFeaturesCount(obj1) <= MIN_ATTRIBUTES_COUNT
 				&& nonNullFeaturesCount(obj2) <= MIN_ATTRIBUTES_COUNT
 				&& typeSimilarity(obj1, obj2) > generalThreshold) {
 			similar = true;
 		} else {
 			final double contentSimilarity = contentSimilarity(obj1, obj2);
 			final double relationsSimilarity = relationsSimilarity(obj1, obj2);
 
 			if (relationsSimilarity == 1 && hasSameUri && nameSimilarity > nameThreshold) {
 				similar = true;
 			} else if (contentSimilarity == 1 && relationsSimilarity == 1) {
 				similar = true;
 			} else if (contentSimilarity > generalThreshold && relationsSimilarity > relationsThreshold
 					&& nameSimilarity > nameThreshold) {
 				similar = true;
 			} else if (relationsSimilarity > generalThreshold && contentSimilarity > contentThreshold) {
 				similar = true;
 			} else if (contentSimilarity > triWayThreshold && nameSimilarity > triWayThreshold
 					&& relationsSimilarity > triWayThreshold) {
 				similar = true;
 			} else if (contentSimilarity > generalThreshold && nameSimilarity > generalThreshold
 					&& typeSimilarity(obj1, obj2) > generalThreshold) {
 				similar = true;
 			}
 		}
 		return similar;
 	}
 
 	/**
 	 * This will compute the similarity between two {@link EObject}s' names.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @return <code>double</code> representing the similarity between the two {@link EObject}s' names. 0
 	 *         &lt; value &lt; 1.
 	 * @see NameSimilarity#nameSimilarityMetric(String, String)
 	 */
 	protected double nameSimilarity(EObject obj1, EObject obj2) {
 		double similarity = 0d;
 		try {
 			final Double value = getSimilarityFromCache(obj1, obj2, NAME_SIMILARITY);
 			if (value != null) {
 				similarity = value;
 			} else {
 				similarity = NameSimilarity.nameSimilarityMetric(NameSimilarity.findName(obj1),
 						NameSimilarity.findName(obj2));
 				setSimilarityInCache(obj1, obj2, NAME_SIMILARITY, similarity);
 			}
 		} catch (FactoryException e) {
 			// fails silently, will return 0d
 		}
 		return similarity;
 	}
 
 	/**
 	 * Returns an absolute comparison metric between the two given {@link EObject}s.
 	 * 
 	 * @param obj1
 	 *            The first {@link EObject} to compare.
 	 * @param obj2
 	 *            Second of the {@link EObject}s to compare.
 	 * @return An absolute comparison metric. 0 &lt; value &lt; 1.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the content similarity.
 	 */
 	private double absoluteMetric(EObject obj1, EObject obj2) throws FactoryException {
 		final double nameSimilarity = nameSimilarity(obj1, obj2);
 		final double relationsSimilarity = relationsSimilarity(obj1, obj2);
 		double sameUri = 0d;
 		if (hasSameUri(obj1, obj2))
 			sameUri = 1;
 		final double positionSimilarity = relationsSimilarity / 2d + sameUri / 2d;
 		final double contentSimilarity = contentSimilarity(obj1, obj2);
 		// Computing type similarity really is time expensive
 		// double typeSimilarity = typeSimilarity(obj1, obj2);
 
 		final double contentWeight = 0.5d;
 		final double nameWeight = 0.4d;
 		final double positionWeight = 0.4d;
 
 		return (contentSimilarity * contentWeight + nameSimilarity * nameWeight + positionSimilarity
 				* positionWeight)
 				/ (contentWeight + nameWeight + positionWeight);
 	}
 
 	/**
 	 * Returns an absolute comparison metric between the three given {@link EObject}s.
 	 * 
 	 * @param obj1
 	 *            The first {@link EObject} to compare.
 	 * @param obj2
 	 *            Second of the {@link EObject}s to compare.
 	 * @param obj3
 	 *            Second of the {@link EObject}s to compare.
 	 * @return An absolute comparison metric
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the content similarity.
 	 */
 	private double absoluteMetric(EObject obj1, EObject obj2, EObject obj3) throws FactoryException {
 		final double metric1 = absoluteMetric(obj1, obj2);
 		final double metric2 = absoluteMetric(obj1, obj3);
 		final double metric3 = absoluteMetric(obj2, obj3);
 
 		return (metric1 + metric2 + metric3) / 3;
 	}
 
 	/**
 	 * This will recursively create three-way submatches and add them under the given {@link MatchModel}. The
 	 * two {@link Match2Elements} we consider as parameters are the result of the two-way comparisons between :
 	 * <ul>
 	 * <li>The left and origin model.</li>
 	 * <li>The right and origin model.</li>
 	 * </ul>
 	 * <br/><br/>We can then consider that a {@link Match3Element} would be :
 	 * 
 	 * <pre>
 	 * match.leftElement = left.getLeftElement();
 	 * match.originElement = left.getRightElement() = right.getRightElement();
 	 * match.rightElement = right.getLeftElement();
 	 * </pre>
 	 * 
 	 * @param root
 	 *            {@link MatchModel} under which to add our {@link Match3Element}s.
 	 * @param matchElementRoot
 	 *            Root of the {@link Match3Element}s' hierarchy for the current element to be created.
 	 * @param left
 	 *            Left {@link Match2Elements} to consider.
 	 * @param right
 	 *            Right {@link Match2Elements} to consider.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the
 	 *             {@link #absoluteMetric(EObject, EObject, EObject) absolute metric} between the three
 	 *             elements or if we cannot add a {@link Match3Element} under the given
 	 *             <code>matchElementRoot</code>.
 	 */
 	@SuppressWarnings("unchecked")
 	private void createSub3Match(MatchModel root, Match3Element matchElementRoot, Match2Elements left,
 			Match2Elements right) throws FactoryException {
 		final List<Match2Elements> leftSubMatches = left.getSubMatchElements();
 		final List<Match2Elements> rightSubMatches = right.getSubMatchElements();
 		final List<Match2Elements> leftNotFound = new ArrayList<Match2Elements>(leftSubMatches);
 		final List<Match2Elements> rightNotFound = new ArrayList<Match2Elements>(rightSubMatches);
 
 		for (Match2Elements nextLeftMatch : leftSubMatches) {
 			Match2Elements correspondingMatch = null;
 
 			for (Match2Elements nextRightMatch : rightNotFound) {
 				if (nextRightMatch.getRightElement().equals(nextLeftMatch.getRightElement())) {
 					correspondingMatch = nextRightMatch;
 					break;
 				}
 			}
 
 			if (correspondingMatch != null) {
 				final Match3Element match = MatchFactory.eINSTANCE.createMatch3Element();
 				match.setSimilarity(absoluteMetric(nextLeftMatch.getLeftElement(), correspondingMatch
 						.getLeftElement(), correspondingMatch.getRightElement()));
 				match.setLeftElement(nextLeftMatch.getLeftElement());
 				match.setRightElement(correspondingMatch.getLeftElement());
 				match.setOriginElement(correspondingMatch.getRightElement());
 				redirectedAdd(matchElementRoot, SUBMATCH_ELEMENT_NAME, match);
 				createSub3Match(root, matchElementRoot, nextLeftMatch, correspondingMatch);
 				leftNotFound.remove(nextLeftMatch);
 				rightNotFound.remove(correspondingMatch);
 			}
 		}
 
 		for (Match2Elements nextLeftNotFound : leftNotFound) {
 			stillToFindFromModel1.add(nextLeftNotFound);
 		}
 		for (Match2Elements nextRightNotFound : rightNotFound) {
 			stillToFindFromModel2.add(nextRightNotFound);
 		}
 	}
 
 	/**
 	 * Creates the {@link Match2Elements submatch elements} corresponding to the mapping of objects from the
 	 * two given {@link List}s.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel} where to insert all these mappings.
 	 * @param list1
 	 *            First of the lists used to compute mapping.
 	 * @param list2
 	 *            Second of the lists used to compute mapping.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @throws FactoryException
 	 *             Thrown if we cannot match the elements of the two lists or add submatch elements to
 	 *             <code>root</code>.
 	 * @throws InterruptedException
 	 *             Thrown if the operation is cancelled or fails somehow.
 	 */
 	private void createSubMatchElements(EObject root, List<EObject> list1, List<EObject> list2,
 			CompareProgressMonitor monitor) throws FactoryException, InterruptedException {
 		stillToFindFromModel1.clear();
 		stillToFindFromModel2.clear();
 		final List<Match2Elements> mappings = mapLists(list1, list2, this
 				.<Integer> getOption(MatchOptions.OPTION_SEARCH_WINDOW), monitor);
 
 		final Iterator<Match2Elements> it = mappings.iterator();
 		while (it.hasNext()) {
 			final Match2Elements map = it.next();
 			final Match2Elements match = recursiveMappings(map.getLeftElement(), map.getRightElement(),
 					monitor);
 			redirectedAdd(root, SUBMATCH_ELEMENT_NAME, match);
 		}
 	}
 
 	/**
 	 * Creates {@link UnMatchElement}s and {@link RemoteUnMatchElement}s wrapped around all the elements of
 	 * the given {@link List}.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel} under which to insert all these elements.
 	 * @param unMatchedElements
 	 *            {@link List} containing all the elements we haven't been able to match.
 	 * @throws FactoryException
 	 *             Thrown if we cannot add elements under the given {@link MatchModel root}.
 	 */
 	private void createThreeWayUnMatchElements(MatchModel root, Map<EObject, Boolean> unMatchedElements)
 			throws FactoryException {
 		for (EObject element : unMatchedElements.keySet()) {
 			// We will only consider the highest level of an unmatched element
 			// hierarchy
 			if (!unMatchedElements.containsKey(element.eContainer())) {
 				final UnMatchElement unMap;
 				if (unMatchedElements.get(element))
 					unMap = MatchFactory.eINSTANCE.createRemoteUnMatchElement();
 				else
 					unMap = MatchFactory.eINSTANCE.createUnMatchElement();
 				unMap.setElement(element);
 				redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMap);
 			}
 		}
 		unMatchedElements.clear();
 	}
 
 	/**
 	 * Creates {@link UnMatchElement}s wrapped around all the elements of the given {@link List}.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel} under which to insert all these {@link UnMatchElement}s.
 	 * @param unMatchedElements
 	 *            {@link Set} containing all the elements we haven't been able to match.
 	 * @throws FactoryException
 	 *             Thrown if we cannot add elements under the given {@link MatchModel root}.
 	 */
 	private void createUnMatchElements(MatchModel root, Set<EObject> unMatchedElements)
 			throws FactoryException {
 		for (EObject element : unMatchedElements) {
 			final UnMatchElement unMap = MatchFactory.eINSTANCE.createUnMatchElement();
 			unMap.setElement(element);
 			redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMap);
 		}
 		unMatchedElements.clear();
 	}
 
 	/**
 	 * This will iterate through the given {@link List} and return its element which is most similar (as given
 	 * by {@link #absoluteMetric(EObject, EObject)}) to the given {@link EObject}.
 	 * 
 	 * @param eObj
 	 *            {@link EObject} we're searching a similar item for in the list.
 	 * @param list
 	 *            {@link List} in which we are to find an object similar to <code>eObj</code>.
 	 * @return The element from <code>list</code> which is the most similar to <code>eObj</code>.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the {@link #absoluteMetric(EObject, EObject) absolute metric}
 	 *             between <code>eObj</code> and one of the list's objects.
 	 */
 	private EObject findMostSimilar(EObject eObj, List list) throws FactoryException {
 		double max = 0d;
 		EObject resultObject = null;
 		final Iterator it = list.iterator();
 		while (it.hasNext()) {
 			final EObject next = (EObject)it.next();
 			final double similarity = absoluteMetric(eObj, next);
 			if (similarity > max) {
 				max = similarity;
 				resultObject = next;
 			}
 		}
 		return resultObject;
 	}
 
 	/**
 	 * Returns whether we should ignore the XMI IDs or compare with them.
 	 * 
 	 * @return <code>True</code> if we should ignore XMI ID, <code>False</code> otherwise.
 	 */
 	private boolean getDefaultIgnoreXMIID() {
 		// This will allow the match engine to work without eclipse
 		// TODO clean this up
 		try {
 			Class.forName("org.osgi.framework.BundleActivator"); //$NON-NLS-1$
 			if (EMFComparePlugin.getDefault() != null)
 				return EMFComparePlugin.getDefault().getBoolean("emfcompare.ignore.XMIID"); //$NON-NLS-1$
 		} catch (ClassNotFoundException e) {
 			// No action taken, we ARE outside of eclipse
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the search window corresponding to the number of siblings to consider while matching. Reducing
 	 * this number (on the preferences page) considerably improve performances while reducing precision.
 	 * 
 	 * @return An <code>int</code> representing the number of siblings to consider for matching.
 	 */
 	private int getDefaultSearchWindow() {
 		int searchWindow = MatchOptions.DEFAULT_SEARCH_WINDOW;
 		// This will allow the match engine to work without eclipse
 		// TODO clean this up
 		try {
 			Class.forName("org.osgi.framework.BundleActivator"); //$NON-NLS-1$
 			if (EMFComparePlugin.getDefault() != null
 					&& EMFComparePlugin.getDefault().getInt("emfcompare.search.window") > 0) //$NON-NLS-1$
 				searchWindow = EMFComparePlugin.getDefault().getInt("emfcompare.search.window"); //$NON-NLS-1$
 		} catch (ClassNotFoundException e) {
 			// No action taken, we ARE outside of eclipse
 		}
 		return searchWindow;
 	}
 
 	/**
 	 * Returns the given similarity between the two given {@link EObject}s as it is stored in cache.<br/>
 	 * <p>
 	 * <code>similarityKind</code> must be one of
 	 * <ul>
 	 * <li>{@link #NAME_SIMILARITY}</li>
 	 * <li>{@link #TYPE_SIMILARITY}</li>
 	 * <li>{@link #VALUE_SIMILARITY}</li>
 	 * <li>{@link #RELATION_SIMILARITY}</li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s we seek the similarity for.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s we seek the similarity for.
 	 * @param similarityKind
 	 *            Kind of similarity to get.
 	 * @return The similarity as described by <code>similarityKind</code> as it is stored in cache for the
 	 *         two given {@link EObject}s.
 	 */
 	private Double getSimilarityFromCache(EObject obj1, EObject obj2, char similarityKind) {
 		return metricsCache.get(pairHashCode(obj1, obj2, similarityKind));
 	}
 
 	/**
 	 * Checks wether the two given {@link EObject} have the same URI.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject} we're comparing.
 	 * @param obj2
 	 *            Second {@link EObject} we're comparing.
 	 * @return <code>True</code> if the {@link EObject}s have the same URI, <code>False</code> otherwise.
 	 */
 	private boolean hasSameUri(EObject obj1, EObject obj2) {
 		return ETools.getURI(obj1).equals(ETools.getURI(obj2));
 	}
 
 	/**
 	 * Sizes the given {@link CompareProgressMonitor monitor} and launches its main task for model comparison.
 	 * 
 	 * @param monitor
 	 *            Progress monitor to display while the operation lasts.
 	 * @param root
 	 *            Root of the first model on which the comparison will be launched.
 	 */
 	private void launchMonitor(CompareProgressMonitor monitor, EObject root) {
 		int size = 1;
 		final Iterator sizeit = root.eAllContents();
 		while (sizeit.hasNext()) {
 			sizeit.next();
 			size++;
 		}
 
 		monitor.beginTask(Messages.getString("DifferencesServices.monitor.task"), size); //$NON-NLS-1$
 		monitor.subTask(Messages.getString("DifferencesServices.monitor.browsing")); //$NON-NLS-1$
 	}
 
 	/**
 	 * This will load all the needed options with their default values.
 	 * 
 	 * @return Map containing all the needed options with their default values.
 	 */
 	private Map<String, Object> loadDefaultOptionMap() {
 		final Map<String, Object> optionMap = new EMFCompareMap<String, Object>(17);
 		optionMap.put(MatchOptions.OPTION_SEARCH_WINDOW, getDefaultSearchWindow());
 		optionMap.put(MatchOptions.OPTION_IGNORE_XMI_ID, getDefaultIgnoreXMIID());
 		optionMap.put(MatchOptions.OPTION_DISTINCT_METAMODELS, true);
 		return optionMap;
 	}
 
 	/**
 	 * This replaces the contents of the defaults options map with the options overriden by the given map.
 	 * 
 	 * @param map
 	 *            Map containing the option given to the match procedure. cannot be <code>null</code>.
 	 */
 	private void loadOptionMap(Map<String, Object> map) {
 		options.putAll(map);
 	}
 
 	/**
 	 * Returns a list containing mappings of the nodes of both given {@link List}s.
 	 * 
 	 * @param list1
 	 *            First of the lists from which we need to map the elements
 	 * @param list2
 	 *            Second list to map the elements from.
 	 * @param window
 	 *            Number of siblings to consider for the matching.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor Progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @return A {@link List} containing mappings of the nodes of both given {@link List}s.
 	 * @throws FactoryException
 	 *             Thrown if the metrics cannot be computed.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 */
 	private List<Match2Elements> mapLists(List<EObject> list1, List<EObject> list2, int window,
 			CompareProgressMonitor monitor) throws FactoryException, InterruptedException {
 		final List<Match2Elements> result = new ArrayList<Match2Elements>();
 		int curIndex = 0 - window / 2;
 		final List<EObject> notFoundList1 = new ArrayList<EObject>(list1);
 		final List<EObject> notFoundList2 = new ArrayList<EObject>(list2);
 
 		final Iterator it1 = list1.iterator();
 		// then iterate over the 2 lists and compare the elements
 		while (it1.hasNext() && notFoundList2.size() > 0) {
 			final EObject obj1 = (EObject)it1.next();
 
 			final StringBuilder obj1Key = new StringBuilder();
 			obj1Key.append(NameSimilarity.findName(obj1));
 			obj1Key.append(obj1.hashCode());
 			EObject obj2 = matchedByID.get(obj1Key.toString());
 
 			if (obj2 == null) {
 				// subtracts the difference between the notfound and the
 				// original list to avoid ArrayOutOfBounds
 				final int end = Math.min(curIndex + window - (list2.size() - notFoundList2.size()),
 						notFoundList2.size());
 				final int index = Math
 						.min(Math.max(curIndex - (list2.size() - notFoundList2.size()), 0), end);
 
 				obj2 = findMostSimilar(obj1, notFoundList2.subList(index, end));
 				if (obj2 != null) {
 					// checks if the most similar to obj2 is obj1
 					final EObject obj1Check = findMostSimilar(obj2, notFoundList1);
 					if (obj1Check != obj1 && isSimilar(obj1Check, obj2)) {
 						continue;
 					}
 				}
 			}
 
 			if (notFoundList1.contains(obj1) && notFoundList2.contains(obj2) && isSimilar(obj1, obj2)) {
 				final Match2Elements mapping = MatchFactory.eINSTANCE.createMatch2Elements();
 				final double metric = absoluteMetric(obj1, obj2);
 
 				mapping.setLeftElement(obj1);
 				mapping.setRightElement(obj2);
 				mapping.setSimilarity(metric);
 				result.add(mapping);
 				notFoundList2.remove(obj2);
 				notFoundList1.remove(obj1);
 			}
 			curIndex += 1;
 			monitor.worked(1);
 			if (monitor.isCanceled())
 				throw new InterruptedException();
 		}
 
 		// now putting the not found elements aside for later
 		stillToFindFromModel2.addAll(notFoundList2);
 		stillToFindFromModel1.addAll(notFoundList1);
 		return result;
 	}
 
 	/**
 	 * Iterates through both of the given {@link XMIResource resources} to find all the elements that can be
 	 * matched by their XMI ID, then populates {@link #matchedByID} with those mappings.
 	 * 
 	 * @param left
 	 *            First of the two {@link XMIResource resources} to visit.
 	 * @param right
 	 *            Second of the {@link XMIResource resources} to visit.
 	 * @throws FactoryException
 	 *             Thrown if we couldn't compute a key to store the items in cache.
 	 */
 	private void matchByID(XMIResource left, XMIResource right) throws FactoryException {
 		matchedByID.clear();
 		final Iterator leftIterator = left.getAllContents();
 
 		while (leftIterator.hasNext()) {
 			final EObject item1 = (EObject)leftIterator.next();
 			final String item1ID = left.getID(item1);
 			if (item1ID != null) {
 				final EObject item2 = right.getEObject(item1ID);
 				if (item2 != null) {
 					final StringBuilder item1Key = new StringBuilder();
					item1Key.append(NameSimilarity.findName(item1));
 					item1Key.append(item1.hashCode());
 					matchedByID.put(item1Key.toString(), item2);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method handles the creation and returning of a two way model match.
 	 * 
 	 * @param leftRoot
 	 *            Left model for the comparison.
 	 * @param rightRoot
 	 *            Right model for the comparison.
 	 * @param monitor
 	 *            Progress monitor to display while the comparison lasts.
 	 * @param optionMap
 	 *            Options to tweak the matching procedure. <code>null</code> or
 	 *            {@link Collections#EMPTY_MAP} will result in the default options to be used.
 	 * @return The corresponding {@link MatchModel}.
 	 * @throws InterruptedException
 	 *             Thrown if the comparison is interrupted somehow.
 	 */
 	private MatchModel modelMatch(EObject leftRoot, EObject rightRoot, CompareProgressMonitor monitor,
 			Map<String, Object> optionMap) throws InterruptedException {
 		if (optionMap != null && optionMap.size() > 0)
 			loadOptionMap(optionMap);
 
 		final MatchModel root = MatchFactory.eINSTANCE.createMatchModel();
 		setModelURIs(root, leftRoot, rightRoot);
 		launchMonitor(monitor, leftRoot);
 
 		// filtering unused features
 		filter.analyseModel(leftRoot);
 		filter.analyseModel(rightRoot);
 		// end of filtering
 
 		// navigate through both models at the same time and realize mappings..
 		try {
 			if (!this.<Boolean> getOption(MatchOptions.OPTION_IGNORE_XMI_ID)) {
 				final Resource leftResource = leftRoot.eResource();
 				final Resource rightResource = rightRoot.eResource();
 				if (leftResource instanceof XMIResource && rightResource instanceof XMIResource)
 					matchByID((XMIResource)leftResource, (XMIResource)rightResource);
 			}
 
 			monitor.subTask(Messages.getString("DifferencesServices.monitor.roots")); //$NON-NLS-1$
 			final List<Match2Elements> matchedRoots = mapLists(leftRoot.eResource().getContents(), rightRoot
 					.eResource().getContents(), this.<Integer> getOption(MatchOptions.OPTION_SEARCH_WINDOW),
 					monitor);
 			stillToFindFromModel1.clear();
 			stillToFindFromModel2.clear();
 			final List<EObject> unMatchedLeftRoots = new ArrayList<EObject>(leftRoot.eResource()
 					.getContents());
 			final List<EObject> unMatchedRightRoots = new ArrayList<EObject>(rightRoot.eResource()
 					.getContents());
 			// These sets will help us in keeping track of the yet to be found
 			// elements
 			final Set<EObject> still1 = new HashSet<EObject>();
 			final Set<EObject> still2 = new HashSet<EObject>();
 
 			Match2Elements matchModelRoot = MatchFactory.eINSTANCE.createMatch2Elements();
 			// We haven't found any similar roots, we then consider the firsts
 			// to be similar
 			if (matchedRoots.size() == 0) {
 				final Match2Elements rootMapping = MatchFactory.eINSTANCE.createMatch2Elements();
 				rootMapping.setLeftElement(leftRoot);
 				rootMapping.setRightElement(findMostSimilar(leftRoot, unMatchedRightRoots));
 				matchedRoots.add(rootMapping);
 			}
 			monitor.subTask(Messages.getString("DifferencesServices.monitor.rootsContents")); //$NON-NLS-1$
 			for (Match2Elements matchedRoot : matchedRoots) {
 				final Match2Elements rootMapping = recursiveMappings(matchedRoot.getLeftElement(),
 						matchedRoot.getRightElement(), monitor);
 				// this is the first passage
 				if (matchModelRoot.getLeftElement() == null) {
 					matchModelRoot = rootMapping;
 					redirectedAdd(root, "matchedElements", matchModelRoot); //$NON-NLS-1$
 				} else {
 					redirectedAdd(matchModelRoot, SUBMATCH_ELEMENT_NAME, rootMapping);
 				}
 
 				// Synchronizes the two lists to avoid multiple elements
 				still1.removeAll(stillToFindFromModel1);
 				still2.removeAll(stillToFindFromModel2);
 				// checks for matches within the yet to found elements lists
 				createSubMatchElements(rootMapping, new ArrayList<EObject>(stillToFindFromModel1),
 						new ArrayList<EObject>(stillToFindFromModel2), monitor);
 				// Adds all unfound elements to the sets
 				still1.addAll(stillToFindFromModel1);
 				still2.addAll(stillToFindFromModel2);
 
 				unMatchedLeftRoots.remove(matchedRoot.getLeftElement());
 				unMatchedRightRoots.remove(matchedRoot.getRightElement());
 			}
 			// We'll iterate through the unMatchedRoots all contents
 			monitor.subTask(Messages.getString("DifferencesServices.monitor.unmatchedRoots")); //$NON-NLS-1$
 			createSubMatchElements(matchModelRoot, unMatchedLeftRoots, unMatchedRightRoots, monitor);
 
 			// Now takes care of remaining unfound elements
 			still1.addAll(stillToFindFromModel1);
 			still2.addAll(stillToFindFromModel2);
 			createUnMatchElements(root, still1);
 			createUnMatchElements(root, still2);
 		} catch (FactoryException e) {
 			EMFComparePlugin.log(e, false);
 		}
 		return root;
 	}
 
 	/**
 	 * This method handles the creation and returning of a three way model match.
 	 * 
 	 * @param leftRoot
 	 *            Left model for the comparison.
 	 * @param rightRoot
 	 *            Right model for the comparison.
 	 * @param ancestor
 	 *            Common ancestor of the right and left models.
 	 * @param monitor
 	 *            Progress monitor to display while the comparison lasts.
 	 * @param optionMap
 	 *            Options to tweak the matching procedure. <code>null</code> or
 	 *            {@link Collections#EMPTY_MAP} will result in the default options to be used.
 	 * @return The corresponding {@link MatchModel}.
 	 * @throws InterruptedException
 	 *             Thrown if the comparison is interrupted somehow.
 	 */
 	@SuppressWarnings("unchecked")
 	private MatchModel modelMatch(EObject leftRoot, EObject rightRoot, EObject ancestor,
 			CompareProgressMonitor monitor, Map<String, Object> optionMap) throws InterruptedException {
 		final MatchModel root = MatchFactory.eINSTANCE.createMatchModel();
 		setModelURIs(root, leftRoot, rightRoot, ancestor);
 		final MatchModel root1AncestorMatch = modelMatch(leftRoot, ancestor, monitor, optionMap);
 		final MatchModel root2AncestorMatch = modelMatch(rightRoot, ancestor, monitor, optionMap);
 
 		final List<Match2Elements> root1MatchedElements = new ArrayList<Match2Elements>(root1AncestorMatch
 				.getMatchedElements());
 		final List<Match2Elements> root2MatchedElements = new ArrayList<Match2Elements>(root2AncestorMatch
 				.getMatchedElements());
 
 		// populates the unmatched elements list for later use
 		for (Object unMatch : root1AncestorMatch.getUnMatchedElements())
 			remainingUnMatchedElements.add(((UnMatchElement)unMatch).getElement());
 		for (Object unMatch : root2AncestorMatch.getUnMatchedElements())
 			remainingUnMatchedElements.add(((UnMatchElement)unMatch).getElement());
 
 		try {
 			final Match2Elements root1Match = root1MatchedElements.get(0);
 			final Match2Elements root2Match = root2MatchedElements.get(0);
 			final Match3Element subMatchRoot = MatchFactory.eINSTANCE.createMatch3Element();
 
 			subMatchRoot.setSimilarity(absoluteMetric(root1Match.getLeftElement(), root2Match
 					.getLeftElement(), root2Match.getRightElement()));
 			subMatchRoot.setLeftElement(root1Match.getLeftElement());
 			subMatchRoot.setRightElement(root2Match.getLeftElement());
 			subMatchRoot.setOriginElement(root2Match.getRightElement());
 			redirectedAdd(root, "matchedElements", subMatchRoot); //$NON-NLS-1$
 			createSub3Match(root, subMatchRoot, root1Match, root2Match);
 
 			// We will now check through the unmatched object for matches. This
 			// will allow for a more accurate detection
 			// for models with multiple roots.
 			processUnmatchedElements(root, subMatchRoot);
 
 			// #createSub3Match(MatchModel, Match3Element, Match2Elements,
 			// Match2Elements) will have updated "remainingUnMatchedElements"
 			final Set<EObject> remainingLeft = new HashSet<EObject>();
 			final Set<EObject> remainingRight = new HashSet<EObject>();
 			for (EObject unMatched : remainingUnMatchedElements) {
 				if (unMatched.eResource() == leftRoot.eResource()) {
 					remainingLeft.add(unMatched);
 					for (final TreeIterator<EObject> iterator = unMatched.eAllContents(); iterator.hasNext(); )
 						remainingLeft.add(iterator.next());
 				} else if (unMatched.eResource() == rightRoot.eResource()) {
 					remainingRight.add(unMatched);
 					for (final TreeIterator<EObject> iterator = unMatched.eAllContents(); iterator.hasNext(); )
 						remainingRight.add(iterator.next());
 				}
 			}
 			stillToFindFromModel1.clear();
 			stillToFindFromModel2.clear();
 			final List<Match2Elements> mappings = mapLists(new ArrayList<EObject>(remainingLeft),
 					new ArrayList<EObject>(remainingRight), this
 							.<Integer> getOption(MatchOptions.OPTION_SEARCH_WINDOW), monitor);
 			for (Match2Elements map : mappings) {
 				final Match3Element subMatch = MatchFactory.eINSTANCE.createMatch3Element();
 				subMatch.setLeftElement(map.getLeftElement());
 				subMatch.setRightElement(map.getRightElement());
 				redirectedAdd(subMatchRoot, SUBMATCH_ELEMENT_NAME, subMatch);
 			}
 			final Map<EObject, Boolean> unMatchedElements = new EMFCompareMap<EObject, Boolean>();
 			for (EObject remoteUnMatch : stillToFindFromModel1) {
 				unMatchedElements.put(remoteUnMatch, true);
 			}
 			for (EObject unMatch : stillToFindFromModel2) {
 				unMatchedElements.put(unMatch, false);
 			}
 			createThreeWayUnMatchElements(root, unMatchedElements);
 		} catch (FactoryException e) {
 			EMFComparePlugin.log(e, false);
 		}
 
 		return root;
 	}
 
 	/**
 	 * Counts all the {@link EStructuralFeature features} of the given {@link EObject} that are
 	 * <code>null</code> or initialized to the empty {@link String} &quot;&quot;.
 	 * 
 	 * @param eobj
 	 *            {@link EObject} we need to count the empty features of.
 	 * @return The number of features initialized to <code>null</code> or the empty String.
 	 */
 	@SuppressWarnings("unchecked")
 	private int nonNullFeaturesCount(EObject eobj) {
 		int nonNullFeatures = 0;
 		final Iterator<EStructuralFeature> features = eobj.eClass().getEAllStructuralFeatures().iterator();
 		while (features.hasNext()) {
 			final EStructuralFeature feature = features.next();
 			if (eobj.eGet(feature) != null && !eobj.eGet(feature).toString().equals("")) //$NON-NLS-1$
 				nonNullFeatures++;
 		}
 		return nonNullFeatures;
 	}
 
 	/**
 	 * Computes an unique key between to {@link EObject}s to store their similarity in cache.
 	 * <p>
 	 * <code>similarityKind</code> must be one of
 	 * <ul>
 	 * <li>{@link #NAME_SIMILARITY}</li>
 	 * <li>{@link #TYPE_SIMILARITY}</li>
 	 * <li>{@link #VALUE_SIMILARITY}</li>
 	 * <li>{@link #RELATION_SIMILARITY}</li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @param similarityKind
 	 *            Kind of similarity this key will represent in cache.
 	 * @return Unique key for the similarity cache.
 	 */
 	private String pairHashCode(EObject obj1, EObject obj2, char similarityKind) {
 		if (similarityKind == NAME_SIMILARITY || similarityKind == TYPE_SIMILARITY
 				|| similarityKind == VALUE_SIMILARITY || similarityKind == RELATION_SIMILARITY) {
 			final StringBuilder hash = new StringBuilder();
 			hash.append(similarityKind).append(obj1.hashCode()).append(obj2.hashCode());
 			return hash.toString();
 		}
 		throw new IllegalArgumentException(Messages.getString(
 				"DifferencesServices.illegalSimilarityKind", similarityKind)); //$NON-NLS-1$
 	}
 
 	/**
 	 * Allows for a more accurate modifications detection for three way comparison with multiple roots models.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel}.
 	 * @param subMatchRoot
 	 *            Root of the {@link Match3Element}s' hierarchy for the current element to be created.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute {@link EObject}s similarity or if adding elements to either
 	 *             <code>root</code> or <code>subMatchRoot</code> fails somehow.
 	 */
 	private void processUnmatchedElements(MatchModel root, Match3Element subMatchRoot)
 			throws FactoryException {
 		for (EObject obj1 : new ArrayList<EObject>(stillToFindFromModel1)) {
 			boolean matchFound = false;
 			if (obj1 instanceof Match2Elements) {
 				final Match2Elements match1 = (Match2Elements)obj1;
 				for (EObject obj2 : new ArrayList<EObject>(stillToFindFromModel2)) {
 					if (obj2 instanceof Match2Elements) {
 						final Match2Elements match2 = (Match2Elements)obj2;
 
 						if (match1.getRightElement() == match2.getRightElement()) {
 							matchFound = true;
 							final Match3Element match = MatchFactory.eINSTANCE.createMatch3Element();
 							match.setSimilarity(absoluteMetric(match1.getLeftElement(), match2
 									.getLeftElement(), match2.getRightElement()));
 							match.setLeftElement(match1.getLeftElement());
 							match.setRightElement(match2.getLeftElement());
 							match.setOriginElement(match2.getRightElement());
 							redirectedAdd(subMatchRoot, SUBMATCH_ELEMENT_NAME, match);
 							createSub3Match(root, subMatchRoot, match1, match2);
 							stillToFindFromModel1.remove(match1);
 							stillToFindFromModel2.remove(match2);
 						}
 					}
 				}
 				if (!matchFound) {
 					remainingUnMatchedElements.add(match1.getLeftElement());
 				}
 			}
 		}
 
 		for (EObject eObj : new ArrayList<EObject>(stillToFindFromModel1)) {
 			if (eObj instanceof Match2Elements) {
 				final Match2Elements nextLeftNotFound = (Match2Elements)eObj;
 				final UnMatchElement unMatch = MatchFactory.eINSTANCE.createUnMatchElement();
 				unMatch.setElement(nextLeftNotFound.getLeftElement());
 				remainingUnMatchedElements.remove(nextLeftNotFound.getLeftElement());
 				remainingUnMatchedElements.remove(nextLeftNotFound.getRightElement());
 				redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMatch);
 			}
 		}
 		for (EObject eObj : new ArrayList<EObject>(stillToFindFromModel2)) {
 			if (eObj instanceof Match2Elements) {
 				final Match2Elements nextRightNotFound = (Match2Elements)eObj;
 				final RemoteUnMatchElement unMatch = MatchFactory.eINSTANCE.createRemoteUnMatchElement();
 				unMatch.setElement(nextRightNotFound.getLeftElement());
 				remainingUnMatchedElements.remove(nextRightNotFound.getLeftElement());
 				remainingUnMatchedElements.remove(nextRightNotFound.getRightElement());
 				redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMatch);
 			}
 		}
 	}
 
 	/**
 	 * We consider here <code>current1</code> and <code>current2</code> are similar. This method creates
 	 * the mapping for the objects <code>current1</code> and <code>current2</code>, Then submappings for
 	 * these two elements' contents.
 	 * 
 	 * @param current1
 	 *            First element of the two elements mapping.
 	 * @param current2
 	 *            Second of the two elements mapping.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor Progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @return The mapping for <code>current1</code> and <code>current2</code> and their content.
 	 * @throws FactoryException
 	 *             Thrown when the metrics cannot be computed for <code>current1</code> and
 	 *             <code>current2</code>.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 */
 	@SuppressWarnings("unchecked")
 	private Match2Elements recursiveMappings(EObject current1, EObject current2,
 			CompareProgressMonitor monitor) throws FactoryException, InterruptedException {
 		Match2Elements mapping = null;
 		mapping = MatchFactory.eINSTANCE.createMatch2Elements();
 		mapping.setLeftElement(current1);
 		mapping.setRightElement(current2);
 		mapping.setSimilarity(absoluteMetric(current1, current2));
 		final List<Match2Elements> mapList = mapLists(current1.eContents(), current2.eContents(), this
 				.<Integer> getOption(MatchOptions.OPTION_SEARCH_WINDOW), monitor);
 		// We can map other elements with mapLists; we iterate through them.
 		final Iterator<Match2Elements> it = mapList.iterator();
 		while (it.hasNext()) {
 			final Match2Elements subMapping = it.next();
 			// As we know source and target are similars, we call recursive
 			// mappings onto these objects
 			EFactory.eAdd(mapping, SUBMATCH_ELEMENT_NAME, recursiveMappings(subMapping.getLeftElement(),
 					subMapping.getRightElement(), monitor));
 		}
 		return mapping;
 	}
 
 	/**
 	 * This method is an indirection for adding Mappings in the current MappingGroup.
 	 * 
 	 * @param object
 	 *            {@link EObject} to add a feature value to.
 	 * @param name
 	 *            Name of the feature to consider.
 	 * @param value
 	 *            Value to add to the feature <code>name</code> of <code>object</code>.
 	 * @throws FactoryException
 	 *             Thrown if the value's affectation fails.
 	 */
 	private void redirectedAdd(EObject object, String name, Object value) throws FactoryException {
 		EFactory.eAdd(object, name, value);
 	}
 
 	/**
 	 * This will compute the similarity between two {@link EObject}s' relations.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @return <code>double</code> representing the similarity between the two {@link EObject}s' relations.
 	 *         0 &lt; value &lt; 1.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the relations' similarity metrics.
 	 * @see StructureSimilarity#relationsSimilarityMetric(EObject, EObject, MetamodelFilter)
 	 */
 	private double relationsSimilarity(EObject obj1, EObject obj2) throws FactoryException {
 		double similarity = 0d;
 		final Double value = getSimilarityFromCache(obj1, obj2, RELATION_SIMILARITY);
 		if (value != null) {
 			similarity = value;
 		} else {
 			similarity = StructureSimilarity.relationsSimilarityMetric(obj1, obj2, filter);
 			setSimilarityInCache(obj1, obj2, RELATION_SIMILARITY, similarity);
 		}
 		return similarity;
 	}
 
 	/**
 	 * Sets the values of the {@link MatchModel}'s left and right models.
 	 * 
 	 * @param modelRoot
 	 *            Root of the {@link MatchModel}.
 	 * @param left
 	 *            Element from which to resolve the left model URI.
 	 * @param right
 	 *            Element from which to resolve the right model URI.
 	 */
 	private void setModelURIs(MatchModel modelRoot, EObject left, EObject right) {
 		setModelURIs(modelRoot, left, right, null);
 	}
 
 	/**
 	 * Sets the values of the {@link MatchModel}'s left, right and ancestor models.
 	 * 
 	 * @param modelRoot
 	 *            Root of the {@link MatchModel}.
 	 * @param left
 	 *            Element from which to resolve the left model URI.
 	 * @param right
 	 *            Element from which to resolve the right model URI.
 	 * @param ancestor
 	 *            Element from which to resolve the ancestor model URI. Can be <code>null</code>.
 	 */
 	private void setModelURIs(MatchModel modelRoot, EObject left, EObject right, EObject ancestor) {
 		// Sets values of left, right and ancestor model URIs
 		final Resource leftResource = left.eResource();
 		final Resource rightResource = right.eResource();
 		Resource ancestorResource = null;
 		if (ancestor != null)
 			ancestorResource = ancestor.eResource();
 
 		if (leftResource != null && leftResource.getURI() != null)
 			modelRoot.setLeftModel(leftResource.getURI().path());
 		if (rightResource != null && rightResource.getURI() != null)
 			modelRoot.setRightModel(rightResource.getURI().path());
 		if (ancestorResource != null && ancestorResource.getURI() != null)
 			modelRoot.setOriginModel(ancestorResource.getURI().path());
 	}
 
 	/**
 	 * Stores in cache the given similarity between the two given {@link EObject}s.<br/>
 	 * <p>
 	 * <code>similarityKind</code> must be one of
 	 * <ul>
 	 * <li>{@link #NAME_SIMILARITY}</li>
 	 * <li>{@link #TYPE_SIMILARITY}</li>
 	 * <li>{@link #VALUE_SIMILARITY}</li>
 	 * <li>{@link #RELATION_SIMILARITY}</li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s we're setting the similarity for.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s we're setting the similarity for.
 	 * @param similarityKind
 	 *            Kind of similarity to set.
 	 * @param similarity
 	 *            Value of the similarity between the two {@link EObject}s.
 	 */
 	private void setSimilarityInCache(EObject obj1, EObject obj2, char similarityKind, double similarity) {
 		metricsCache.put(pairHashCode(obj1, obj2, similarityKind), new Double(similarity));
 	}
 
 	/**
 	 * This will compute the similarity between two {@link EObject}s' types.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @return <code>double</code> representing the similarity between the two {@link EObject}s' types. 0
 	 *         &lt; value &lt; 1.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the type similarity metrics.
 	 * @see StructureSimilarity#typeSimilarityMetric(EObject, EObject)
 	 */
 	private double typeSimilarity(EObject obj1, EObject obj2) throws FactoryException {
 		double similarity = 0d;
 		final Double value = getSimilarityFromCache(obj1, obj2, TYPE_SIMILARITY);
 		if (value != null) {
 			similarity = value;
 		} else {
 			similarity = StructureSimilarity.typeSimilarityMetric(obj1, obj2);
 			setSimilarityInCache(obj1, obj2, TYPE_SIMILARITY, similarity);
 		}
 		return similarity;
 	}
 }
