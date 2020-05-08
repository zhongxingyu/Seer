 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 
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
 import org.eclipse.emf.compare.match.Match2Elements;
 import org.eclipse.emf.compare.match.MatchFactory;
 import org.eclipse.emf.compare.match.MatchModel;
 import org.eclipse.emf.compare.match.UnMatchElement;
 import org.eclipse.emf.compare.match.api.MatchEngine;
 import org.eclipse.emf.compare.match.impl.MatchFactoryImpl;
 import org.eclipse.emf.compare.match.statistic.similarity.NameSimilarity;
 import org.eclipse.emf.compare.match.statistic.similarity.StructureSimilarity;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.ETools;
 import org.eclipse.emf.compare.util.FactoryException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 
 /**
  * These services are usefull when one want to compare models
  * 
  * more precisely using the method modelDiff.
  * 
  * Known bugs and limitation: 1- modeldiff only works if the two roots given are
  * similar
  * 
  * @author www.obeo.fr
  * 
  */
 public class DifferencesServices implements MatchEngine {
 	private static double THRESHOLD = 0.96;
 
 	private static double STRONGER_THRESHOLD = 0.96;
 
 	/**
 	 * Strong strategy considers only ADD, REMOVE or CHANGE operations
 	 */
 	public static int STRONG_STRATEGY = 0;
 
 	/**
 	 * Soft strategy considers ADD, REMOVE, CHANGE, MOVE and RENAME operations.
 	 */
 	public static int SOFT_STRATEGY = 1;
 
 	private int currentStrategy = STRONG_STRATEGY;
 
 	/**
 	 * This map is used to cache the comparison results Pair(Element1, Element2) => [
 	 * nameSimilarity, valueSimilarity, relationSimilarity, TypeSimilarity]
 	 */
 	private Map metricsCache = new WeakHashMap();
 
 	/**
 	 * If you put true here youl'll be able to have a look at the mapping
 	 * created between EObjects (usefull for debug purpose)
 	 */
 	private boolean saveMapping = true;
 
 	private MatchFactory matchFactory = new MatchFactoryImpl();
 
 	/**
 	 * Set a different kind of strategy. default is SOFT_STRATEGY.
 	 * 
 	 * @param strategyId
 	 */
 
 	public void setStrategy(int strategyId) {
 		currentStrategy = strategyId;
 	}
 
 	/* Start of cache management */
 	private String pairHashCode(EObject obj1, EObject obj2,
 			String similarityKind) {
 		return similarityKind + obj1.hashCode() + obj2.hashCode();
 	}
 
 	private String NAME_SIMILARITY = "n"; //$NON-NLS-1$
 
 	private String TYPE_SIMILARITY = "t"; //$NON-NLS-1$
 
 	private String VALUE_SIMILARITY = "v"; //$NON-NLS-1$
 
 	private String RELATION_SIMILARITY = "r"; //$NON-NLS-1$
 
	private double getSimilarityFromCache(EObject obj1, EObject obj2,
 			String similarityKind) {
 		return ((Double) metricsCache.get(pairHashCode(obj1, obj2,
				similarityKind))).doubleValue();
 	}
 
 	private void setSimilarityInCache(EObject obj1, EObject obj2,
 			String similarityKind, double similarity) {
 		metricsCache.put(pairHashCode(obj1, obj2, similarityKind), new Double(
 				similarity));
 
 	}
 
 	/* End of cache management */
 	private double nameSimilarity(EObject obj1, EObject obj2)
 			throws FactoryException {
 		try {
 			Double value = getSimilarityFromCache(obj1, obj2, NAME_SIMILARITY);
 			if (value != null) {
 				return value;
 			} else {
 				double similarity = NameSimilarity.nameSimilarityMetric(
 						NameSimilarity.findName(obj1), NameSimilarity
 								.findName(obj2));
 				setSimilarityInCache(obj1, obj2, NAME_SIMILARITY, similarity);
 				return similarity;
 			}
 		} catch (Exception e) {
 			return 0.0;
 		}
 	}
 
 	private double typeSimilarity(EObject obj1, EObject obj2)
 			throws FactoryException {
 		Double value = getSimilarityFromCache(obj1, obj2, TYPE_SIMILARITY);
 		if (value != null) {
 			return value;
 		} else {
 			double similarity = StructureSimilarity.typeSimilarityMetric(obj1,
 					obj2);
 			setSimilarityInCache(obj1, obj2, TYPE_SIMILARITY, similarity);
 			return similarity;
 		}
 
 	}
 
 	private double relationsSimilarity(EObject obj1, EObject obj2)
 			throws FactoryException {
 		Double value = getSimilarityFromCache(obj1, obj2, RELATION_SIMILARITY);
 		if (value != null) {
 			return value;
 		} else {
 			double similarity = StructureSimilarity.relationsSimilarityMetric(
 					obj1, obj2, filter);
 			setSimilarityInCache(obj1, obj2, RELATION_SIMILARITY, similarity);
 			return similarity;
 		}
 
 	}
 
 	private double contentSimilarity(EObject obj1, EObject obj2)
 			throws FactoryException {
 		Double value = getSimilarityFromCache(obj1, obj2, VALUE_SIMILARITY);
 		if (value != null) {
 			return value;
 		} else {
 			double similarity = NameSimilarity.nameSimilarityMetric(
 					NameSimilarity.contentValue(obj1, filter), NameSimilarity
 							.contentValue(obj2, filter));
 			setSimilarityInCache(obj1, obj2, VALUE_SIMILARITY, similarity);
 			return similarity;
 		}
 	}
 
 	/**
 	 * 
 	 * @param obj1
 	 * @param obj2
 	 * @return an absolute comparison metric
 	 * @throws FactoryException
 	 */
 	public double absoluteMetric(EObject obj1, EObject obj2)
 
 	throws FactoryException {
 		double nameSimilarity = nameSimilarity(obj1, obj2);
 		double relationsSimilarity = relationsSimilarity(obj1, obj2);
 		double sameUri = 0;
 		if (hasSameUri(obj1, obj2))
 			sameUri = 1;
 		double positionSimilarity = (0.5 * relationsSimilarity + 0.5 * sameUri);
 		double contentSimilarity = contentSimilarity(obj1, obj2);
 		// double typeSimilarity = typeSimilarity(obj1, obj2); // type
 		// similarity is really time expensive
 		return contentSimilarity * 0.4 + nameSimilarity * 0.2
 				+ positionSimilarity * 0.4;
 	}
 
 	/**
 	 * Return true if both elements have the same serialization ID, false
 	 * otherwise
 	 * 
 	 * @param left
 	 *            left model element
 	 * @param right
 	 *            right model element
 	 * @return true if both elements have the same serialization ID
 	 */
 	private boolean haveSameXmiId(EObject left, EObject right) {
 		if (left.eResource() instanceof XMLResource
 				&& right.eResource() instanceof XMLResource) {
 			String leftId = ((XMLResource) left.eResource()).getID(left);
 			String rightId = ((XMLResource) right.eResource()).getID(right);
 			if (leftId != null && rightId != null && !leftId.equals("") //$NON-NLS-1$
 					&& !rightId.equals("")) //$NON-NLS-1$
 				return leftId.equals(rightId);
 		}
 		return false;
 	}
 
 	/**
 	 * Return true if the 2 objects are considered as similars
 	 * 
 	 * @param obj1
 	 * @param obj2
 	 * @return true if both elements have the same serialization ID
 	 * @throws FactoryException
 	 * @throws ENodeCastException
 	 */
 	private boolean isSimilar(EObject obj1, EObject obj2)
 			throws FactoryException {
 		if (haveSameXmiId(obj1, obj2))
 			return true;
 		if (currentStrategy == SOFT_STRATEGY) {
 			// SOFT STRATEGY
 			double nameSimilarity = nameSimilarity(obj1, obj2);
 
 			boolean hasSameUri = hasSameUri(obj1, obj2);
 
 			if (nameSimilarity == 1 && hasSameUri)
 				return true;
 
 			double contentSimilarity = contentSimilarity(obj1, obj2);
 
 			double relationsSimilarity = relationsSimilarity(obj1, obj2);
 
 			if (nameSimilarity > THRESHOLD && relationsSimilarity > 0.9)
 				return true;
 
 			if (relationsSimilarity == 1 && hasSameUri)
 				return true;
 
 			if (relationsSimilarity > THRESHOLD && contentSimilarity > 0.59)
 				return true;
 
 			if (contentSimilarity > 0.8 && nameSimilarity > 0.8
 					&& relationsSimilarity > 0.8)
 				return true;
 
 			double typeSimilarity = typeSimilarity(obj1, obj2);
 			if (contentSimilarity > THRESHOLD && nameSimilarity > THRESHOLD
 					&& typeSimilarity > THRESHOLD) {
 				return true;
 			}
 
 			return false;
 		} else {// STRONG STRATEGY
 			double nameSimilarity = nameSimilarity(obj1, obj2);
 			boolean hasSameUri = hasSameUri(obj1, obj2);
 
 			if (nameSimilarity == 1 && hasSameUri)
 				return true;
 
 			double contentSimilarity = contentSimilarity(obj1, obj2);
 
 			double relationsSimilarity = relationsSimilarity(obj1, obj2);
 
 			if (nameSimilarity > STRONGER_THRESHOLD
 					&& relationsSimilarity > 0.9)
 				return true;
 			/*
 			 * Seems quite stupid if (relationsSimilarity == 1 && hasSameUri)
 			 * return true;
 			 */
 
 			if (relationsSimilarity > STRONGER_THRESHOLD
 					&& contentSimilarity > 0.9 && nameSimilarity > 0.2)
 				return true;
 
 			if (contentSimilarity > 0.9 && nameSimilarity > 0.9
 					&& relationsSimilarity > 0.9)
 				return true;
 
 			double typeSimilarity = typeSimilarity(obj1, obj2);
 			if (contentSimilarity > STRONGER_THRESHOLD
 					&& nameSimilarity > STRONGER_THRESHOLD
 					&& typeSimilarity > STRONGER_THRESHOLD) {
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private boolean hasSameUri(EObject obj1, EObject obj2) {
 		return ETools.getURI(obj1).equals(ETools.getURI(obj2));
 	}
 
 	private Collection stillToFindFromModel1 = new ArrayList();
 
 	private Collection stillToFindFromModel2 = new ArrayList();
 
 	/**
 	 * This method is an indirection for adding Mappings in the current
 	 * MappingGroup.
 	 * 
 	 * @param object
 	 * @param name
 	 * @param value
 	 * @throws FactoryException
 	 */
 	private void redirectedAdd(EObject object, String name, Object value)
 			throws FactoryException {
 		if (saveMapping)
 			EFactory.eAdd(object, name, value);
 	}
 
 	/**
 	 * a Filter
 	 */
 	protected MetamodelFilter filter;
 
 	/**
 	 * 
 	 * @return an int meaning the number of siblings elements I will look in
 	 */
 	private int getDefaultSearchWindow() {
 		return 100;
 	}
 
 	/**
 	 * Return a mapping model between the two other models..
 	 * 
 	 * Basically the difference is calculated this way : - both models are
 	 * browsed and compared, Mappings are created when two nodes are considered
 	 * as similar - Nodes wich has not been mapped are compared in order to map
 	 * them - The mapping tree is browsed in order to determine the modification
 	 * log - the modification log (an EMF model) is returned
 	 * 
 	 * Known bugs and limitation: 1°- modeldiff only works if the two roots
 	 * given are similar
 	 * 
 	 * @param root1
 	 *            the first root element
 	 * @param root2
 	 *            the second root element
 	 * @return a mapping model between the two other models..
 	 * @throws InterruptedException
 	 * @throws FactoryException
 	 * @throws ENodeCastException
 	 */
 	public MatchModel modelMatch(EObject root1, EObject root2,
 			IProgressMonitor monitor) throws InterruptedException {
 		// ModificationLog diffResult;
 		MatchModel root = matchFactory.createMatchModel();
 		int size = 1;
 		Iterator sizeit = root1.eAllContents();
 		while (sizeit.hasNext()) {
 			sizeit.next();
 			size++;
 		}
 		// filtering unused features
 		filter = new MetamodelFilter();
 		filter.analyseModel(root1);
 		filter.analyseModel(root2);
 		// end of filtering
 
 		monitor.beginTask("Comparing model", size);
 		monitor.subTask("Browsing model");
 
 		// Match2Elements associations = matchFactory.createMatch2Elements();
 		// EFactory.eAdd(root, "matchedElements", associations);
 
 		// first navigate through both models at the same time and realize
 		// mappings..
 		try {
 			if (true && isSimilar(root1, root2)) {
 				Match2Elements rootMapping = recursiveMappings(root1, root2,
 						monitor);
 
 				redirectedAdd(root, "matchedElements", rootMapping);
 				// Keep current lists in a corner and init the objects list we
 				// still
 				// have to map
 				List still1 = new ArrayList();
 				List still2 = new ArrayList();
 				still1.addAll(stillToFindFromModel1);
 				still2.addAll(stillToFindFromModel2);
 				stillToFindFromModel1 = new ArrayList();
 				stillToFindFromModel2 = new ArrayList();
 				// now try to map not yet mapped elements...
 				monitor.subTask("Matching remaining elements");
 				// magic number to avoid too big complexity
 				Collection mappings = mapLists(still1, still2,
 						getDefaultSearchWindow(), monitor);
 				Iterator it = mappings.iterator();
 				while (it.hasNext()) {
 					Match2Elements map = (Match2Elements) it.next();
 					redirectedAdd(rootMapping, "subMatchElements", map);
 					// if it has not been mapped while browsing the trees at
 					// the
 					// same time it probably is a moved element
 				}
 
 				// now the other elements won't be mapped, keep them in the
 				// model
 				it = stillToFindFromModel1.iterator();
 				while (it.hasNext()) {
 					EObject element = (EObject) it.next();
 					UnMatchElement unMap = matchFactory.createUnMatchElement();
 					unMap.setElement(element);
 					redirectedAdd(root, "unMatchedElements", unMap);
 
 				}
 				it = stillToFindFromModel2.iterator();
 				while (it.hasNext()) {
 					EObject element = (EObject) it.next();
 					UnMatchElement unMap = matchFactory.createUnMatchElement();
 					unMap.setElement(element);
 					redirectedAdd(root, "unMatchedElements", unMap);
 
 				}
 				stillToFindFromModel1 = new ArrayList();
 				stillToFindFromModel2 = new ArrayList();
 
 			} else {
 				// FIX here for known bug and limitation number 1
 			}
 		} catch (FactoryException e) {
 			EMFComparePlugin.getDefault().log(e, false);
 		}
 		return root;
 	}
 
 	// this list is used in order to keep track of all the mappings
 	// it allows a better comparaison for the 2nd pass
 	private Collection MappingList = new LinkedList();
 
 	/**
 	 * we considers here current1 and current2 are similar ! this method create
 	 * the mapping for the objects current1 and current2. Then it create
 	 * submappings for current1 and current2 contents
 	 * 
 	 * @param current1
 	 * @param current2
 	 * @return the mapping for current1 and current2
 	 * @throws FactoryException
 	 * @throws InterruptedException
 	 * @throws ENodeCastException
 	 */
 	private Match2Elements recursiveMappings(EObject current1,
 			EObject current2, IProgressMonitor monitor)
 			throws FactoryException, InterruptedException {
 		Match2Elements mapping = null;
 		mapping = matchFactory.createMatch2Elements();
 		mapping.setLeftElement(current1);
 		mapping.setRightElement(current2);
 		MappingList.add(mapping);
 		mapping.setSimilarity(absoluteMetric(current1, current2));
 		Collection mapList = mapLists(current1.eContents(), current2
 				.eContents(), getDefaultSearchWindow(), monitor);
 		// // in maplist we get other mappings
 		Iterator it = mapList.iterator();
 		while (it.hasNext()) {
 			Match2Elements subMapping = (Match2Elements) it.next();
 			// here we now source and target are similars, then we should launch
 			// recursive mappings onto these objects
 			EFactory.eAdd(mapping, "subMatchElements", recursiveMappings(
 					subMapping.getLeftElement(), subMapping.getRightElement(),
 					monitor));
 		}
 		return mapping;
 
 	}
 
 	private EObject findMostSimilar(EObject eObj, List list)
 			throws FactoryException {
 		double max = 0;
 		EObject resultObject = null;
 		Iterator it = list.iterator();
 		while (it.hasNext()) {
 			EObject next = (EObject) it.next();
 			double similarity = absoluteMetric(eObj, next);
 			if (similarity > max) {
 				max = similarity;
 				resultObject = next;
 			}
 		}
 		return resultObject;
 	}
 
 	/**
 	 * Return a list containing mappings of the nodes of both lists
 	 * 
 	 * @param list1
 	 * @param list2
 	 * @return a list containing mappings of the nodes of both lists
 	 * @throws FactoryException
 	 * @throws InterruptedException
 	 * @throws ENodeCastException
 	 */
 	private Collection mapLists(List list1, List list2, int window,
 			IProgressMonitor monitor) throws FactoryException,
 			InterruptedException {
 		Collection result = new ArrayList();
 		// System.err.println("mapping to lists: " + list1.size() +","+
 		// list2.size() + " first elem : " + list1.get(0) );
 		int curIndex = 0 - window / 2;
 		Collection notFoundList1 = new ArrayList();
 		Collection notFoundList2 = new ArrayList();
 		// first init the not found list with all contents (we have found
 		// nothing yet)
 		notFoundList1.addAll(list1);
 		notFoundList2.addAll(list2);
 
 		Iterator it1 = list1.iterator();
 		Iterator it2 = null;
 		// then iterate over the 2 lists and compare the elements
 		while (it1.hasNext()) {
 			EObject obj1 = (EObject) it1.next();
 			it2 = list2.iterator();
 			int index = curIndex < 0 ? 0 : curIndex;
 			int end = curIndex + window > list2.size() ? list2.size()
 					: curIndex + window;
 			if (index > end)
 				index = end;
 			EObject obj2 = findMostSimilar(obj1, list2.subList(index, end));
 			if (notFoundList1.contains(obj1) && notFoundList2.contains(obj2)
 					&& isSimilar(obj1, obj2)) {
 				Match2Elements mapping = matchFactory.createMatch2Elements();
 				double metric = 1.0;
 				if (saveMapping) {
 					metric = absoluteMetric(obj1, obj2);
 				}
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
 	 * Return a list containing mappings of the nodes of both lists
 	 * 
 	 * @param list1
 	 * @param list2
 	 * @return a list containing mappings of the nodes of both lists
 	 * @throws FactoryException
 	 * @throws InterruptedException
 	 * @throws ENodeCastException
 	 */
 	/*
 	 * private Collection mapListsSure(Collection list1, Collection list2,
 	 * IProgressMonitor monitor) throws FactoryException, InterruptedException {
 	 * Collection result = new ArrayList(); Collection notFoundList1 = new
 	 * ArrayList(); Collection notFoundList2 = new ArrayList(); // first init
 	 * the not found list with all contents (we have found // nothing yet)
 	 * notFoundList1.addAll(list1); notFoundList2.addAll(list2);
 	 * 
 	 * Iterator it1 = list1.iterator(); Iterator it2 = null; // then iterate
 	 * over the 2 lists and compare the elements while (it1.hasNext()) { EObject
 	 * obj1 = (EObject) it1.next(); it2 = list2.iterator(); EObject obj2 =
 	 * findMostSimilar(obj1, list2); if (notFoundList1.contains(obj1) &&
 	 * notFoundList2.contains(obj2) && isSimilar(obj1, obj2)) { Match2Elements
 	 * mapping = matchFactory .createMatch2Elements(); double metric = 1.0; if
 	 * (saveMapping) { metric = absoluteMetric(obj1, obj2); }
 	 * mapping.setLeftElement(obj1); mapping.setRightElement(obj2);
 	 * EObjectToMapping.put(obj1, mapping); EObjectToMapping.put(obj2, mapping);
 	 * mapping.setSimilarity(metric); result.add(mapping);
 	 * notFoundList2.remove(obj2); notFoundList1.remove(obj1); }
 	 * monitor.worked(1); if (monitor.isCanceled()) throw new
 	 * InterruptedException(); } // now putting the not found elements aside for
 	 * later stillToFindFromModel2.addAll(notFoundList2);
 	 * stillToFindFromModel1.addAll(notFoundList1); return result; }
 	 */
 
 	/**
 	 * Return a list containing mappings of the nodes of both lists
 	 * 
 	 * @param list1
 	 * @param list2
 	 * @return a list containing mappings of the nodes of both lists
 	 * @throws FactoryException
 	 * @throws InterruptedException
 	 * @throws ENodeCastException
 	 */
 	/*
 	 * private Collection mapListsQuick(Collection list1, Collection list2,
 	 * IProgressMonitor monitor) throws FactoryException, InterruptedException {
 	 * Collection result = new ArrayList(); Collection notFoundList1 = new
 	 * ArrayList(); Collection notFoundList2 = new ArrayList(); // first init
 	 * the not found list with all contents (we have found // nothing yet)
 	 * notFoundList1.addAll(list1); notFoundList2.addAll(list2);
 	 * 
 	 * Iterator it1 = list1.iterator(); Iterator it2 = null; // then iterate
 	 * over the 2 lists and compare the elements while (it1.hasNext()) { EObject
 	 * obj1 = (EObject) it1.next(); it2 = list2.iterator(); EObject obj2 =
 	 * findMostSimilar(obj1, list2); if (notFoundList1.contains(obj1) &&
 	 * notFoundList2.contains(obj2) && isSimilar(obj1, obj2)) { Match2Elements
 	 * mapping = matchFactory.createMatch2Elements(); double metric = 1.0; if
 	 * (saveMapping) { metric = absoluteMetric(obj1, obj2); }
 	 * mapping.setLeftElement(obj1); mapping.setRightElement(obj2);
 	 * EObjectToMapping.put(obj1, mapping); EObjectToMapping.put(obj2, mapping);
 	 * mapping.setSimilarity(metric); result.add(mapping);
 	 * notFoundList2.remove(obj2); notFoundList1.remove(obj1); }
 	 * monitor.worked(1); if (monitor.isCanceled()) throw new
 	 * InterruptedException(); } stillToFindFromModel2.addAll(notFoundList2);
 	 * stillToFindFromModel1.addAll(notFoundList1); return result; }
 	 */
 
 }
