 package edu.umass.ciir.models;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 public class JaccardSimilarity implements LanguageModelSimilarity {
 
 	public SimilarityMeasure calculateSimilarity(LanguageModel lm1,	LanguageModel lm2, boolean useProbabilities) {
 		
	    
 		HashSet<TermEntry> union = new HashSet<TermEntry>();
 		union.addAll(lm1.getEntries());
 		union.addAll(lm2.getEntries());
 		
		if (union.size() == 0) {
		    return new SimilarityMeasure(0.0d, "Jaccard Coefficient");
		}
 		HashSet<String> intersection = new HashSet<String>();
 		Collection<TermEntry> vocabA = lm1.getEntries();
 		for (TermEntry entryA : vocabA) {
 			TermEntry entryB = lm2.getTermEntry(entryA.getTerm());
 			if (entryB != null) {
 				intersection.add(entryA.getTerm());
 			}
 		}
 		
 		double overlap = intersection.size() / (double) union.size();
 		return new SimilarityMeasure(overlap, "Jaccard Coefficient");
 		
 	}
 }
