 package edu.cmu.lti.f13.hw4.hw4_wfeely.casconsumers;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.collection.CasConsumer_ImplBase;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSList;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.util.ProcessTrace;
 
 import edu.cmu.lti.f13.hw4.hw4_wfeely.typesystems.Document;
 import edu.cmu.lti.f13.hw4.hw4_wfeely.typesystems.Token;
 import edu.cmu.lti.f13.hw4.hw4_wfeely.utils.Utils;
 
 public class RetrievalEvaluator extends CasConsumer_ImplBase {
 
   /** query id number **/
   public ArrayList<Integer> qIdList;
 
   /** query and text relevant values **/
   public ArrayList<Integer> relList;
 
   /** global vocabulary **/
   public HashMap<String, Integer> vocab;
 
   /** document array **/
   public ArrayList<Doc> docs;
 
   /** max query ID **/
   public int maxQID;
 
   public void initialize() throws ResourceInitializationException {
 
     qIdList = new ArrayList<Integer>();
 
     relList = new ArrayList<Integer>();
 
     vocab = new HashMap<String, Integer>();
 
     docs = new ArrayList<Doc>();
 
     maxQID = 0;
   }
 
   /**
    * DONE: 1. construct the global word dictionary 2. keep the word frequency for each sentence
    */
   @Override
   public void processCas(CAS aCas) throws ResourceProcessException {
 
     JCas jcas;
     try {
       jcas = aCas.getJCas();
     } catch (CASException e) {
       throw new ResourceProcessException(e);
     }
 
     FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
 
     if (it.hasNext()) {
       // set up document
       Document doc = (Document) it.next();
       Doc myDoc = new Doc();
       myDoc.text = doc.getText();
       myDoc.queryID = doc.getQueryID();
       myDoc.relevanceValue = doc.getRelevanceValue();
 
       // check queryID against max queryID
       if (myDoc.queryID > maxQID)
         maxQID = myDoc.queryID;
 
       // set up tokenList for this document
       FSList fsTokenList = doc.getTokenList();
       ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
 
       // set up word frequency vector for this document
       myDoc.f = new HashMap<String, Integer>();
 
       // add QueryID and RelevanceValue to qId list and rel list
       qIdList.add(doc.getQueryID());
       relList.add(doc.getRelevanceValue());
 
       // DEBUG: Print document
       // System.out.println(myDoc.text);
 
       // update global vocabulary with tokens from this document
       for (Token tok : tokenList) {
         String word = tok.getText();
         Integer freq = tok.getFrequency();
         // put each token into global vocabulary, updating global frequencies
         if (vocab.containsKey(word))
           vocab.put(word, freq + 1);
         else
           vocab.put(word, freq);
         // put each token into document word frequency vector
         myDoc.f.put(word, freq);
       }
       // DEBUG: print all elements in vocab
       /*
        * if (vocab != null) { for (String word : vocab.keySet()) { System.out.println(word + "," +
        * vocab.get(word)); } }
        */
 
       // add myDoc to documents array
       docs.add(myDoc);
 
     }
 
   }
 
   /**
    * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
    */
   @Override
   public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
           IOException {
 
     super.collectionProcessComplete(arg0);
 
     // set up query, result sets
     ArrayList<QuerySet> querySets = new ArrayList<QuerySet>();
     for (int i = 0; i < maxQID; i++) {
       // make a new query set
       QuerySet myQuerySet = new QuerySet();
       myQuerySet.qID = i;
       myQuerySet.docSet = new ArrayList<Doc>();
       // loop through docs
       for (Doc myDoc : docs) {
         // add each document in this query set to the list
         if (myDoc.queryID == i) {
           // set query if this doc's relValue is 99
           if (myDoc.relevanceValue == 99)
             myQuerySet.query = myDoc;
           // otherwise, add to docSet
           else
             myQuerySet.docSet.add(myDoc);
         }
       }
       // add this query set to list of querySets
       querySets.add(myQuerySet);
     }
 
     // TODO :: compute the cosine similarity measure
     for (QuerySet myQuerySet : querySets) {
       ArrayList<Double> cosSim = new ArrayList<Double>();
       for (Doc myDoc : myQuerySet.docSet) {
         cosSim.add(computeCosineSimilarity(myQuerySet.query.f, myDoc.f));
       }
     }
     // TODO :: compute the rank of retrieved sentences
 
     // TODO :: compute the metric:: mean reciprocal rank
     double metric_mrr = compute_mrr();
     System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
   }
 
   /**
    * 
    * @return cosine_similarity
    */
   private double computeCosineSimilarity(Map<String, Integer> queryVector,
           Map<String, Integer> docVector) {
     double cosine_similarity = 0.0;
 
     // TODO :: compute cosine similarity between two sentences
 
     return cosine_similarity;
   }
 
   /**
    * 
    * @return mrr
    */
   private double compute_mrr() {
     double metric_mrr = 0.0;
 
     // TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
 
     return metric_mrr;
   }
 
   /**
    * Doc class. Includes document text, word frequency vector, and tf-idf vector for each document.
    * 
    * @author Weston Feely
    */
   private class Doc {
 
     /** document text **/
     public String text;
 
     /** document query ID **/
     public int queryID;
 
     /** document relevance value **/
     public int relevanceValue;
 
     /** document word frequency vector **/
     public HashMap<String, Integer> f;
 
     /** document tf-idf **/
     public HashMap<String, Double> tfidf;
   }
 
   /**
    * QuerySet class. Includes set of documents belonging to this query set, and the query document
    * itself.
    * 
    * @author Weston Feely
    */
   private class QuerySet {
 
     /** Query ID for this query set **/
    int qID;
 
     /** Set of documents belonging to this query set **/
    ArrayList<Doc> docSet;
 
     /** Query document for this query set **/
    Doc query;
   }
 }
