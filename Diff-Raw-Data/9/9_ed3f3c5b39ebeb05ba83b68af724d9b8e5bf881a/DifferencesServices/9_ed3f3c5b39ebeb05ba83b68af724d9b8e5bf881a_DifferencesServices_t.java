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
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.match.api.MatchEngine;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchFactory;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.UnMatchElement;
 import org.eclipse.emf.compare.match.metamodel.impl.MatchFactoryImpl;
 import org.eclipse.emf.compare.match.statistic.similarity.NameSimilarity;
 import org.eclipse.emf.compare.match.statistic.similarity.StructureSimilarity;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.ETools;
 import org.eclipse.emf.compare.util.FactoryException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 
 /**
  * These services are useful when one wants to compare models more precisely using the method modelDiff. Two
  * distinct strategy can be considered for the matching :
  * <ul>
  * <li>{@link #STRONG_STRATEGY Strong} will only consider Add, Remove or Change operations.</li>
  * <li>{@link #SOFT_STRATEGY Soft} will consider Add, Remove, Change, Move and Rename operations.</li>
  * </ul>
  * <p>
  * Known bugs and limitations :
  * <ul>
  * <li>Model diff only works if the two roots given are similar.</li>
  * </ul>
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class DifferencesServices implements MatchEngine {
 	/** Strong strategy considers only ADD, REMOVE or CHANGE operations. */
 	public static final int STRONG_STRATEGY = 0;
 
 	/** Soft strategy considers ADD, REMOVE, CHANGE, MOVE and RENAME operations. */
 	public static final int SOFT_STRATEGY = 1;
 
 	private static final double THRESHOLD = 0.96d;
 
 	private static final double STRONGER_THRESHOLD = 0.96d;
 
 	private static final String NAME_SIMILARITY = "n"; //$NON-NLS-1$
 
 	private static final String TYPE_SIMILARITY = "t"; //$NON-NLS-1$
 
 	private static final String VALUE_SIMILARITY = "v"; //$NON-NLS-1$
 
 	private static final String RELATION_SIMILARITY = "r"; //$NON-NLS-1$
 
 	protected MetamodelFilter filter;
 
 	private int currentStrategy = STRONG_STRATEGY;
 
 	// this list is used in order to keep track of all the mappings it allows a better comparison for the 2nd
 	// pass
 	private List<Match2Elements> mappingList = new LinkedList<Match2Elements>();
 
 	private List<EObject> stillToFindFromModel1 = new ArrayList<EObject>();
 
 	private List<EObject> stillToFindFromModel2 = new ArrayList<EObject>();
 
 	/**
 	 * This map is used to cache the comparison results Pair(Element1, Element2) => [nameSimilarity,
 	 * valueSimilarity, relationSimilarity, TypeSimilarity].
 	 */
 	private Map<String, Double> metricsCache = new WeakHashMap<String, Double>();
 
 	/**
 	 * Setting this to <code>True</code> will enable you to have a look at the mapping created between
 	 * EObjects (useful for debug purpose).
 	 */
 	private boolean saveMapping = true;
 
 	private MatchFactory matchFactory = new MatchFactoryImpl();
 
 	/**
 	 * Set a different kind of strategy, must be one of {@link #STRONG_STRATEGY} or {@link #SOFT_STRATEGY}.
 	 * Default is SOFT_STRATEGY.
 	 * 
 	 * @param strategyId
 	 *            Matching strategy to consider for the matching.
 	 */
 	public void setStrategy(int strategyId) {
 		currentStrategy = strategyId;
 	}
 
 	/* Start of cache management */
 	private String pairHashCode(EObject obj1, EObject obj2, String similarityKind) {
 		return similarityKind + obj1.hashCode() + obj2.hashCode();
 	}
 
 	private Double getSimilarityFromCache(EObject obj1, EObject obj2, String similarityKind) {
 		return metricsCache.get(pairHashCode(obj1, obj2, similarityKind));
 	}
 
 	private void setSimilarityInCache(EObject obj1, EObject obj2, String similarityKind, double similarity) {
 		metricsCache.put(pairHashCode(obj1, obj2, similarityKind), new Double(similarity));
 	}
 
 	/* End of cache management */
 	private double nameSimilarity(EObject obj1, EObject obj2) {
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
 
 	private double contentSimilarity(EObject obj1, EObject obj2) throws FactoryException {
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
 	 * Returns an absolute comparaison metric between the two given {@link EObject}s.
 	 * 
 	 * @param obj1
 	 *            The first {@link EObject} to compare.
 	 * @param obj2
 	 *            Second of the {@link EObject}s to compare.
 	 * @return An absolute comparison metric
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the content similarity.
 	 */
 	public double absoluteMetric(EObject obj1, EObject obj2) throws FactoryException {
 		final double nameSimilarity = nameSimilarity(obj1, obj2);
 		final double relationsSimilarity = relationsSimilarity(obj1, obj2);
 		double sameUri = 0d;
 		if (hasSameUri(obj1, obj2))
 			sameUri = 1;
 		final double positionSimilarity = relationsSimilarity / 2d + sameUri / 2d;
 		final double contentSimilarity = contentSimilarity(obj1, obj2);
 		// Computing type similarity really is time expensive
 		// double typeSimilarity = typeSimilarity(obj1, obj2);
 
 		final double contentWeight = 0.4d;
 		final double nameWeight = 0.2d;
 		final double positionWeight = 0.4d;
 
 		return contentSimilarity * contentWeight + nameSimilarity * nameWeight + positionSimilarity
 				* positionWeight;
 	}
 
 	/**
 	 * Returns <code>True</code> if both elements have the same serialization ID.
 	 * 
 	 * @param left
 	 *            Element from the left model to compare.
 	 * @param right
 	 *            Element from the right model.
 	 * @return <code>True</code> if both elements have the same serialization ID, <code>False</code>
 	 *         otherwise.
 	 */
 	private boolean haveSameXmiId(EObject left, EObject right) {
 		if (left.eResource() instanceof XMLResource && right.eResource() instanceof XMLResource) {
 			final String leftId = ((XMLResource)left.eResource()).getID(left);
 			final String rightId = ((XMLResource)right.eResource()).getID(right);
 			if (leftId != null && rightId != null && !leftId.equals("") //$NON-NLS-1$
 					&& !rightId.equals("")) //$NON-NLS-1$
 				return leftId.equals(rightId);
 		}
 		return false;
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
 	private boolean isSimilar(EObject obj1, EObject obj2) throws FactoryException {
 		boolean similar = false;
 		
 		final double relationsThreshold = 0.9d;
 		final double nameThreshold = 0.2d;
 		final double softContentThreshold = 0.59d;
 		final double strongContentThreshold = 0.9d;
 		final double softTriWayThreshold = 0.8d;
 		final double strongTriWayThreshold = 0.9d;
 		
 		double contentThreshold = softContentThreshold;
 		double triWayThreshold = softTriWayThreshold;
 		double generalThreshold = THRESHOLD;
 		if (currentStrategy == STRONG_STRATEGY) {
 			contentThreshold = strongContentThreshold;
 			triWayThreshold = strongTriWayThreshold;
 			generalThreshold = STRONGER_THRESHOLD;
 		}
 		
 		if (haveSameXmiId(obj1, obj2)) {
 			similar = true;
 		} else {
 			final double nameSimilarity = nameSimilarity(obj1, obj2);
 			final boolean hasSameUri = hasSameUri(obj1, obj2);
 
 			if (nameSimilarity == 1 && hasSameUri) {
 				similar = true;
 			} else {
 				final double contentSimilarity = contentSimilarity(obj1, obj2);
 				final double relationsSimilarity = relationsSimilarity(obj1, obj2);
 	
 				if (nameSimilarity > generalThreshold && relationsSimilarity > relationsThreshold && nameSimilarity > nameThreshold) {
 					similar = true;
 				} else if (relationsSimilarity == 1 && hasSameUri) {
 					similar = true;
 				} else if (relationsSimilarity > generalThreshold && contentSimilarity > contentThreshold) {
 					similar = true;
 				} else if (contentSimilarity > triWayThreshold && nameSimilarity > triWayThreshold && relationsSimilarity > triWayThreshold) {
 					similar = true;
 				} else if (contentSimilarity > generalThreshold && nameSimilarity > generalThreshold && typeSimilarity(obj1, obj2) > generalThreshold) {
 					similar = true;
 				}
 			}
 		}
 		return similar;
 	}
 
 	private boolean hasSameUri(EObject obj1, EObject obj2) {
 		return ETools.getURI(obj1).equals(ETools.getURI(obj2));
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
 		if (saveMapping)
 			EFactory.eAdd(object, name, value);
 	}
 
 	/**
 	 * Returns THe search window corresponding to the number of siblings to consider while matching. Reducing
 	 * this number (on the preferences page) considerably improve performances while reducing precision.
 	 * 
 	 * @return An <code>int</code> representing the number of siblings to consider for matching.
 	 */
 	private int getDefaultSearchWindow() {
 		return EMFComparePlugin.getDefault().getPluginPreferences().getInt("emfcompare.search.window"); //$NON-NLS-1$
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
 	 * Known bugs and limitations :
 	 * <ul>
 	 * <li>Model diff only works if the two roots given are similar</li>
 	 * </ul>
 	 * 
 	 * @param root1
 	 *            Left model of the comparison.
 	 * @param root2
 	 *            Right model of the comparison.
 	 * @param monitor
 	 *            {@link IProgressMonitor Progress monitor} to display while the comparison lasts.
 	 * @return Mapping model of the two given models.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 * @see MatchEngine#modelMatch(EObject, EObject, IProgressMonitor)
 	 */
 	public MatchModel modelMatch(EObject root1, EObject root2, IProgressMonitor monitor)
 			throws InterruptedException {
 		final MatchModel root = matchFactory.createMatchModel();
 		
 		launchMonitor(monitor, root1);
 		
 		// filtering unused features
 		filter = new MetamodelFilter();
 		filter.analyseModel(root1);
 		filter.analyseModel(root2);
 		// end of filtering
 
 		// first navigate through both models at the same time and realize mappings..
 		try {
 			/*
 			 * TODO uncomment this if/else and fix the "else". If the roots aren't similar, we need to find the
 			 * matching root and do something with the elements we haven't matched.
 			 */
 			// if (isSimilar(root1, root2)) {
 			final Match2Elements rootMapping = recursiveMappings(root1, root2, monitor);
 
 			redirectedAdd(root, "matchedElements", rootMapping); //$NON-NLS-1$
 			// Keep current lists in a corner and init the objects list we still have to map
 			final List<EObject> still1 = new ArrayList<EObject>();
 			final List<EObject> still2 = new ArrayList<EObject>();
 			still1.addAll(stillToFindFromModel1);
 			still2.addAll(stillToFindFromModel2);
 			stillToFindFromModel1.clear();
 			stillToFindFromModel2.clear();
 			// now try to map not yet mapped elements...
 			monitor.subTask("Matching remaining elements"); //$NON-NLS-1$
 			// magic number to avoid too big complexity
 			final Collection mappings = mapLists(still1, still2, getDefaultSearchWindow(), monitor);
 			Iterator it = mappings.iterator();
 			while (it.hasNext()) {
 				final Match2Elements map = (Match2Elements)it.next();
 				redirectedAdd(rootMapping, "subMatchElements", map); //$NON-NLS-1$
 				// if it has not been mapped while browsing the trees at the same time it probably is a moved
 				// element
 			}
 
 			// now the other elements won't be mapped, keep them in the model
 			it = stillToFindFromModel1.iterator();
 			while (it.hasNext()) {
 				final EObject element = (EObject)it.next();
 				final UnMatchElement unMap = matchFactory.createUnMatchElement();
 				unMap.setElement(element);
 				redirectedAdd(root, "unMatchedElements", unMap); //$NON-NLS-1$
 			}
 			it = stillToFindFromModel2.iterator();
 			while (it.hasNext()) {
 				final EObject element = (EObject)it.next();
 				final UnMatchElement unMap = matchFactory.createUnMatchElement();
 				unMap.setElement(element);
 				redirectedAdd(root, "unMatchedElements", unMap); //$NON-NLS-1$
 			}
 			stillToFindFromModel1.clear();
 			stillToFindFromModel2.clear();
 			// } else {
 			/*
 			 * TODO remove the true from this else's matching if and fix the limitation (see javadoc for this
 			 * method/class).
 			 */
 			// }
 		} catch (FactoryException e) {
 			EMFComparePlugin.getDefault().log(e, false);
 		}
 		return root;
 	}
 	
 	private void launchMonitor(IProgressMonitor monitor, EObject root) {
 		int size = 1;
 		final Iterator sizeit = root.eAllContents();
 		while (sizeit.hasNext()) {
 			sizeit.next();
 			size++;
 		}
 
 		monitor.beginTask("Comparing model", size); //$NON-NLS-1$
 		monitor.subTask("Browsing model"); //$NON-NLS-1$
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
 	 *            {@link IProgressMonitor Progress monitor} to display while the comparison lasts.
 	 * @return The mapping for <code>current1</code> and <code>current2</code> and their content.
 	 * @throws FactoryException
 	 *             Thrown when the metrics cannot be computed for <code>current1</code> and
 	 *             <code>current2</code>.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 */
 	@SuppressWarnings("unchecked")
 	private Match2Elements recursiveMappings(EObject current1, EObject current2, IProgressMonitor monitor)
 			throws FactoryException, InterruptedException {
 		Match2Elements mapping = null;
 		mapping = matchFactory.createMatch2Elements();
 		mapping.setLeftElement(current1);
 		mapping.setRightElement(current2);
 		mappingList.add(mapping);
 		mapping.setSimilarity(absoluteMetric(current1, current2));
 		final Collection mapList = mapLists(current1.eContents(), current2.eContents(),
 				getDefaultSearchWindow(), monitor);
 		// We can map other elements with mapLists; we iterate through them.
 		final Iterator it = mapList.iterator();
 		while (it.hasNext()) {
 			final Match2Elements subMapping = (Match2Elements)it.next();
 			// As we know source and target are similars, we call recursive mappings onto these objects
 			EFactory.eAdd(mapping, "subMatchElements", recursiveMappings(subMapping.getLeftElement(), //$NON-NLS-1$
 					subMapping.getRightElement(), monitor));
 		}
 		return mapping;
 	}
 
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
 	 * Returns a list containing mappings of the nodes of both given {@link List}s.
 	 * 
 	 * @param list1
 	 *            First of the lists from which we need to map the elements
 	 * @param list2
 	 *            Second list to map the elements from.
 	 * @param window
 	 *            Number of siblings to consider for the matching.
 	 * @param monitor
 	 *            {@link IProgressMonitor Progress monitor} to display while the comparison lasts.
 	 * @return A {@link List} containing mappings of the nodes of both given {@link List}s.
 	 * @throws FactoryException
 	 *             Thrown if the metrics cannot be computed.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 */
 	private List<Match2Elements> mapLists(List<EObject> list1, List<EObject> list2, int window, IProgressMonitor monitor)
 			throws FactoryException, InterruptedException {
 		final List<Match2Elements> result = new ArrayList<Match2Elements>();
 		int curIndex = 0 - window / 2;
 		final List<EObject> notFoundList1 = new ArrayList<EObject>();
 		final List<EObject> notFoundList2 = new ArrayList<EObject>();
 		// first init the not found list with all contents (we have found nothing yet)
 		notFoundList1.addAll(list1);
 		notFoundList2.addAll(list2);
 
 		final Iterator it1 = list1.iterator();
 		// then iterate over the 2 lists and compare the elements
 		while (it1.hasNext() && list2.size() > 0) {
 			final EObject obj1 = (EObject)it1.next();
 			final int end = Math.min(curIndex + window, list2.size());
 			final int index = Math.min(Math.max(curIndex, 0), end);
 			
 			final EObject obj2 = findMostSimilar(obj1, list2.subList(index, end));
 			if (isSimilar(obj1, obj2)) {
 				final Match2Elements mapping = matchFactory.createMatch2Elements();
 				double metric = 1d;
 				if (saveMapping)
 					metric = absoluteMetric(obj1, obj2);
 				
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
 }
