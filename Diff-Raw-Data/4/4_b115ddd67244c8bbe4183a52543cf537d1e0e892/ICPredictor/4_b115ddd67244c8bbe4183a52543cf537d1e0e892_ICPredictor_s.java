 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package edu.toronto.cs.ontools.prediction;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.toronto.cs.ontools.annotation.AnnotationTerm;
 import edu.toronto.cs.ontools.annotation.SearchResult;
 import edu.toronto.cs.ontools.taxonomy.TaxonomyTerm;
 
 public class ICPredictor extends AbstractPredictor {
 	private static final boolean ENABLE_CUMMULATIVE_IC = false;
 	private final Map<String, Double> icCache = new HashMap<String, Double>();
 
 	public double getIC(String taxonomyTermID) {
 		return getIC(this.annotations.getTaxonomyNode(taxonomyTermID));
 	}
 
 	private double getCummulativeIC(List<AnnotationTerm> taxonomyTerms) {
 		double result = 0;
 
 		int firstIndex = 0;
 		AnnotationTerm first = null;
 		while (firstIndex < taxonomyTerms.size()
 				&& (first = taxonomyTerms.get(firstIndex++)) == null) {
 			;
 		}
 		if (first == null) {
 			return result;
 		}
 		Set<String> commonNeighbors = new HashSet<String>();
 		commonNeighbors.addAll(first.getNeighbors());
 		result = commonNeighbors.size();
 		for (int i = firstIndex; i < taxonomyTerms.size(); ++i) {
 			if (taxonomyTerms.get(i) != null) {
 				commonNeighbors.retainAll(taxonomyTerms.get(i).getNeighbors());
 				if (commonNeighbors.size() > 0) {
 					result = commonNeighbors.size();
 				} else {
 					break;
 				}
 			}
 		}
 		return -Math.log(result / this.annotations.getAnnotations().size());
 	}
 
 	private double getIC(AnnotationTerm taxonomyTerm) {
 		// return taxonomyTerm == null ? 0 : getCachedIC(taxonomyTerm);
 		return taxonomyTerm == null ? 0 : -Math.log((double) taxonomyTerm
 				.getNeighborsCount()
 				/ this.annotations.getAnnotations().size());
 	}
 
 	private double getCachedIC(AnnotationTerm taxonomyTerm) {
 		Double result = this.icCache.get(taxonomyTerm.getId());
 		if (result == null) {
 			result = -Math.log((double) taxonomyTerm.getNeighborsCount()
 					/ this.annotations.getAnnotations().size());
 			this.icCache.put(taxonomyTerm.getId(), result);
 		}
 		return result;
 	}
 
 	public TaxonomyTerm getMICA(String taxonomyTerm1, String taxonomyTerm2) {
 		String micaId = getMICAId(taxonomyTerm1, taxonomyTerm2);
 		if (micaId != null) {
 			return this.annotations.getTaxonomy().getTerm(micaId);
 		}
 		return null;
 	}
 
 	public String getMICAId(String taxonomyTerm1, String taxonomyTerm2) {
 		// TODO: implement more efficiently!
 		Set<String> intersection = new HashSet<String>();
 		intersection.addAll(this.annotations.getTaxonomy().getAncestors(
 				taxonomyTerm1));
 		intersection.retainAll(this.annotations.getTaxonomy().getAncestors(
 				taxonomyTerm2));
 		double max = -1;
 		String micaId = this.annotations.getTaxonomy().getRootId();
 		for (String a : intersection) {
 			double ic = this.getIC(a);
 			if (ic >= max) {
 				max = ic;
 				micaId = a;
 			}
 		}
 		return micaId;
 	}
 
 	public List<AnnotationTerm> getMICAIds(String taxonomyTerm1,
 			String taxonomyTerm2) {
 		// TODO: implement more efficiently!
 		List<AnnotationTerm> result = new ArrayList<AnnotationTerm>();
		int maxResults = 4;
 
 		Set<String> intersection = new HashSet<String>();
 		intersection.addAll(this.annotations.getTaxonomy().getAncestors(
 				taxonomyTerm1));
 		intersection.retainAll(this.annotations.getTaxonomy().getAncestors(
 				taxonomyTerm2));
 		double max = -1;
 		String micaId = this.annotations.getTaxonomy().getRootId();
 		while (intersection.size() > 0 && result.size() < maxResults) {
 			for (String a : intersection) {
 				double ic = this.getIC(a);
 				if (ic >= max) {
 					max = ic;
 					micaId = a;
 				}
 			}
 			result.add(this.annotations.getTaxonomyNode(micaId));
 			intersection.removeAll(this.annotations.getTaxonomy().getAncestors(
 					micaId));
 		}
 		return result;
 	}
 
 	public double asymmetricPhenotypeSimilarity(Collection<String> query,
 			Collection<String> reference) {
 		double result = 0.0;
 		for (String q : query) {
 			double bestMatchIC = 0;
 			for (String r : reference) {
 				double ic = ENABLE_CUMMULATIVE_IC ? this.getCummulativeIC(this
 						.getMICAIds(q, r)) : this.getIC(this.getMICAId(q, r));
 				if (ic > bestMatchIC) {
 					bestMatchIC = ic;
 				}
 			}
 			result += bestMatchIC;
 		}
 		return result / (query.size() > 0 ? query.size() : 1);
 	}
 
 	public double symmetricPhenotypeSimilarity(Collection<String> query,
 			Collection<String> reference) {
 		return .5 * asymmetricPhenotypeSimilarity(query, reference) + .5
 				* asymmetricPhenotypeSimilarity(reference, query);
 	}
 
 	public double getSimilarityScore(Collection<String> query,
 			Collection<String> reference) {
 		return this.asymmetricPhenotypeSimilarity(query, reference);
 	}
 
 	@Override
 	public List<SearchResult> getMatches(Collection<String> taxonomyTermIDs) {
 		List<SearchResult> result = new LinkedList<SearchResult>();
 		for (AnnotationTerm o : this.annotations.getAnnotations()) {
 			Set<String> annTaxonomyTerms = this.annotations
 					.getTaxonomyTermsWithAnnotation(o.getId()).keySet();
 			double matchScore = this.asymmetricPhenotypeSimilarity(
 					taxonomyTermIDs, annTaxonomyTerms);
 			if (matchScore > 0) {
 				result
 						.add(new SearchResult(o.getId(), o.getName(),
 								matchScore));
 			}
 		}
 		Collections.sort(result);
 		return result;
 	}
 
 	public double getMatchScore(Collection<String> taxonomyTermIDs,
 			String annotationID) {
 		List<String> annTaxonomyTerms = this.annotations.getAnnotationNode(
 				annotationID).getOriginalAnnotations();
 		return this.asymmetricPhenotypeSimilarity(taxonomyTermIDs,
 				annTaxonomyTerms)
 				/ this.asymmetricPhenotypeSimilarity(annTaxonomyTerms,
 						annTaxonomyTerms);
 	}
 
 	@Override
 	public double getSpecificity(String taxonomyTermID) {
 		return this.getIC(taxonomyTermID);
 	}
 }
