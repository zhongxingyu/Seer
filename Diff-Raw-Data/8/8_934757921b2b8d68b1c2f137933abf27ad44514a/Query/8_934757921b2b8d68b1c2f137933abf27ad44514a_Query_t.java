 package edu.tuhh.summarizer.lsa;
 
 import edu.ucla.sspace.common.DocumentVectorBuilder;
 import edu.ucla.sspace.common.SemanticSpace;
 import edu.ucla.sspace.common.Similarity;
 import edu.ucla.sspace.common.WordComparator;
 import edu.ucla.sspace.lsa.LatentSemanticAnalysis;
 import edu.ucla.sspace.matrix.Matrix;
 import edu.ucla.sspace.util.MultiMap;
 import edu.ucla.sspace.vector.DenseVector;
 import edu.ucla.sspace.vector.DoubleVector;
 
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.util.*;
 
 /**
  * @author avelinsk
  */
 public class Query {
   private SemanticSpace sspace;
   private DoubleVector queryVector;
   private DocumentVectorBuilder docBuilder;
   private WordComparator wordCompare;
   private Matrix docMatrix;
 
   public Query() {
     sspace = LSAUtils.getTermsSpace();
     docBuilder = new DocumentVectorBuilder(sspace);
     wordCompare = new WordComparator();
     queryVector = new DenseVector(sspace.getVectorLength());
     docMatrix = LSAUtils.getDocsMatrix();
   }
 
 
   /**
    * @param query
    * @return
    */
   protected DoubleVector getQueryAsVector(String query) {
     queryVector = docBuilder.buildVector(new BufferedReader(new StringReader(query)), queryVector);
     return queryVector;
   }
 
   public List<SearchResult> searchDocSpace(String query) {
     DoubleVector queryVector = getQueryAsVector(query);
     final Map<Integer, Double> similarityMap =
             new HashMap<Integer, Double>();
     for (int i = 0; i < docMatrix.rows(); i++) {
       double sim = SimilarityUtil.getCosineSimilarity(docMatrix.getRowVector(i), queryVector);
       if (sim > 0.0D) {
         similarityMap.put(i, sim);
       }
     }
     return sortByScore(similarityMap);
   }
 
   public List<SearchResult> searchTermSpace(String query, int maxResult) {
     DoubleVector queryVector = getQueryAsVector(query);
 
     MultiMap<Double, String> similarityMap = wordCompare.getMostSimilarToVector(queryVector, sspace,
             maxResult, Similarity.SimType.COSINE);
 
     List<SearchResult> results = new ArrayList<SearchResult>();
    Iterator<Map.Entry<Double, String>> iter = similarityMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = iter.next();
      double score = (Double) entry.getKey();
      if (score < 0.001D) {
         continue;
       }
       results.add(new SearchResult(0, (String) entry.getValue(), score));
     }
 
     return results;
   }
 
 
   /**
    * Computes the cosine similarity between the query
    * and the document vectors in the document space
    *
    * @param query
    * @param sspace      - the document semantic space
    * @param noOfResults
    * @return
    */
   public double computeCosineSimilarity(DoubleVector query, SemanticSpace sspace, int noOfResults) {
     LatentSemanticAnalysis lsa_space = (LatentSemanticAnalysis) sspace;
     double cosineSimilarity = 0.0;
     int i = sspace.getVectorLength();
     for (int j = 0; j < i; j++) {
       DoubleVector doc = lsa_space.getDocumentVector(j);
       cosineSimilarity = Similarity.cosineSimilarity(doc, query);
     }
     return cosineSimilarity;
   }
 
 
   protected MultiMap getSimilarWords(SemanticSpace sspace, String word, int maxResult) {
     MultiMap results = wordCompare.getMostSimilar(word, sspace, maxResult, Similarity.SimType.COSINE);
     return results;
   }
 
   protected DoubleVector getDocument(SemanticSpace sspace, int idx) {
     return ((LatentSemanticAnalysis) sspace).getDocumentVector(idx);
   }
 
   protected DoubleVector getWord(SemanticSpace space, String word) {
     return (DoubleVector) ((LatentSemanticAnalysis) sspace).getVector(word);
   }
 
   private List<SearchResult> sortByScore(
           final Map<Integer, Double> similarityMap) {
     List<SearchResult> results = new ArrayList<SearchResult>();
     List<Integer> docIndexes = new ArrayList<Integer>();
     docIndexes.addAll(similarityMap.keySet());
     Collections.sort(docIndexes, new Comparator<Integer>() {
       public int compare(Integer s1, Integer s2) {
         return similarityMap.get(s2).compareTo(similarityMap.get(s1));
       }
     });
     for (Integer index : docIndexes) {
       double score = similarityMap.get(index);
       if (score < 0.001D) {
         continue;
       }
       results.add(new SearchResult(index, "", score));
     }
     return results;
   }
 
 
   public class SearchResult {
     public int index;
     public String word;
     public double score;
 
     public SearchResult(Integer index, String word, double score) {
       this.index = index;
       this.word = word;
       this.score = score;
     }
   }
 }
