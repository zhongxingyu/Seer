 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 
 public class QueryHandler {
 
     /** Remembers the documents IDs used to create the index.**/
     private DataSet dataSet;
     private Logger logger = Logger.getLogger(QueryHandler.class.getName());
 
     public QueryHandler(DataSet dataSet){
         this.dataSet = dataSet;
     }
 
     public ArrayList<String> retrieveDocumentsForQuery(Query query) {
         switch(query.getType()){
         case NOT:
             return handleNOTQuery(query);
         case AND:
             return handleANDQuery(query);
         case OR:
             return handleORQuery(query);
         case PHRASE:
             return handlePhraseQuery(query);
         case PROXIMITY:
             return handleProximityQuery(query);
         }
 
         logger.log(Config.LOG_LEVEL, "Query type " + query.getType() + " not recognized!");
 
         return null;
     }
 
 
     /**
      * It searches from every term given in the query and for each matched document
      * and term it creates a  map from DocIds to another Map<Term, Positions>. 
      * 
      * @param query The query object
      * @return a Map from DocIds to another map of terms to positions. 
      */
     private HashMap<String, HashMap<String, TreeSet<Integer>>>
     getDocIdsMatchingPhrase(Query query) {
         ArrayList<String> terms = query.getTerms();
         HashMap<String, HashMap<String, TreeSet<Integer>>> documents =
             new HashMap<String, HashMap<String, TreeSet<Integer>>>();
 
         for(String term : terms) {
             TreeSet<DocIdEntry> docIDs = dataSet.getDocIdEntrySet(term);
             if(docIDs == null)
                 return  documents;
             Iterator<DocIdEntry> iterator = docIDs.iterator();
 
             while(iterator.hasNext()){
                 DocIdEntry currEntry = iterator.next();
 
                 HashMap<String, TreeSet<Integer>> termToPos = documents.get(currEntry.getDocId());
                 if(termToPos == null) {
                     termToPos = new HashMap<String, TreeSet<Integer>>();
                     documents.put(currEntry.getDocId(), termToPos);
                 }
 
                 termToPos.put(term, currEntry.getPositions());
             }
         }
 
         // Ensure all the documents have all the terms. Removes the invalid documents.
         HashMap<String, HashMap<String, TreeSet<Integer>>> result =
             new HashMap<String, HashMap<String, TreeSet<Integer>>>();
 
         for (String documentId : documents.keySet()) {
             if (documents.get(documentId).keySet().size() == query.getTerms().size()) {
                 result.put(documentId, documents.get(documentId));
             }
         }
 
         return result;
     }
 
 
 
     private boolean checkProximity(HashMap<String, ArrayList<Integer>> positions, Query query, int dist){
         ArrayList<String> terms = query.getTerms();
 
         HashMap<String, Integer> lastSeen = new HashMap<String, Integer>();
         HashMap<String, Integer> lastIndexSeen = new HashMap<String, Integer>();
         HashMap<String, Integer> nrSameTerm = new HashMap<String, Integer>();
         HashMap<String, Iterator<Integer>> mapIt = new HashMap<String, Iterator<Integer>>();
 
         for(String term : terms){
             if(positions.containsKey(term) == false)
                 return false;    
             if(nrSameTerm.containsKey(term)){
                 int nr = nrSameTerm.get(term) + 1;
                 nrSameTerm.put(term, nr);
             }else{
                 nrSameTerm.put(term, 1);
             }
 
             Iterator<Integer> it = positions.get(term).iterator();
             mapIt.put(term, it);
         }
 
 
         for(String term: mapIt.keySet()){
             Iterator<Integer> it = mapIt.get(term);
             if(it.hasNext()){
                 lastSeen.put(term, it.next());
                 lastIndexSeen.put(term, 0);
             }
         }
 
         boolean flag = true;
         while(flag){
             int min = Integer.MAX_VALUE;
             int max = Integer.MIN_VALUE;
             String minTerm = null;
             boolean allTermsMatched = true;
 
             for(String term: lastSeen.keySet()){
 
                 int nrOcc = nrSameTerm.get(term);
                 if(nrOcc > 1 && lastIndexSeen.get(term) >= nrOcc - 1){
                     ArrayList<Integer> currPos = positions.get(term);
                     for(int i = lastIndexSeen.get(term); i >= 0; i-- ){
                         if(min >currPos.get(i)){
                             min = currPos.get(i);
                             minTerm = term;  
                         }
                         if(max < currPos.get(i)){
                             max = currPos.get(i);
                         }
                     }
                 }
 
                 if(nrOcc > 1 && lastIndexSeen.get(term) < nrOcc -1) 
                     allTermsMatched = false;
                 if(nrOcc == 1){   
                     int index = lastSeen.get(term);
                     if(min > index){
                         min = index;
                         minTerm = term;
                     }
 
                     if(max  < index){
                         max = index;
                     }
                 }   
             }
 
             if(allTermsMatched == true){
                 if(max - min <= dist){
                     if(Config.orderedProximity){
                         int last = -1;
                         boolean flagOrdered = true;
                         for(String ordTerms: terms){
                             if(last < lastSeen.get(ordTerms)){
                                 last = lastSeen.get(ordTerms);
                             }else{
                                 //System.out.println(ordTerms + " " + lastSeen.get(ordTerms));   
                                 flagOrdered = false;
                             }
                         }
 
                         if(flagOrdered)
                             return true;
                         else{
                             Iterator<Integer> it = mapIt.get(minTerm);
                             if(it.hasNext() == false)
                                 return false;
                             int nextIndx = it.next();
                             int last2 = lastIndexSeen.get(minTerm);
                             last2 ++;
                             lastIndexSeen.put(minTerm, last2);
                             lastSeen.put(minTerm, nextIndx);
                         }
                     } else
                         return true;
                 }
                 else{
                     Iterator<Integer> it = mapIt.get(minTerm);
                     if(it.hasNext() == false)
                         return false;
                     int nextIndx = it.next();
                     int last = lastIndexSeen.get(minTerm);
                     last ++;
                     lastIndexSeen.put(minTerm, last);
                     lastSeen.put(minTerm, nextIndx);
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Answers proximity queries
      *
      * @param query
      * @return A list of documents that map the query
      */
     private ArrayList<String> handleProximityQuery(Query query) {
         int distance = query.getProximityWindow();    
         HashMap<String, HashMap<String, TreeSet<Integer>>> intersect =
             getDocIdsMatchingPhrase(query);
         TreeSet<String> matchingDocs = new TreeSet<String>();
 
         // Filters the documents that contain the whole phrase.
         for (String documentId : intersect.keySet()) {
             HashMap<String, TreeSet<Integer>> termsPos = intersect.get(documentId);
             HashMap<String, ArrayList<Integer>> termsPosList = 
                 new HashMap<String, ArrayList<Integer>>();
 
 //            System.out.println("Document: " + documentId);
             for(String term: termsPos.keySet()){
                 termsPosList.put(term, new ArrayList<Integer>(termsPos.get(term)));
                 //for(Integer p : termsPos.get(term)){
                  //   System.out.println("Term: " + term + " " + p);
                 //}
             }
             if(checkProximity(termsPosList, query, distance)){
                 matchingDocs.add(documentId);
             }
         }            
 
 
         return new ArrayList<String>(matchingDocs);
     }
 
     /**
      * Answers phrase queries
      *
      * @param query The PHRASE query that needs to be answered.
      * @return The list of documents that correspond to the query.
      */
     private ArrayList<String> handlePhraseQuery(Query query) {
         HashMap<String, HashMap<String, TreeSet<Integer>>> commonDocumentEntries =
             getDocIdsMatchingPhrase(query);
         ArrayList<String> matchingDocs = new ArrayList<String>();
 
         // Filters the documents that contain the whole phrase.
         for (String documentId : commonDocumentEntries.keySet()) {
             TreeSet<Integer> positions = 
                 commonDocumentEntries.get(documentId).get(query.getTerm(0));
 
             for (int i = 1; i < query.getTerms().size(); i++) {
                 ArrayList<Integer> incrementedPositions = new ArrayList<Integer>();
 
                 // Increment the positions from the previous term.
                 for (Iterator<Integer> it = positions.iterator(); it.hasNext(); ) {
                     incrementedPositions.add(it.next() + 1);
                 }
                 positions = commonDocumentEntries.get(documentId).get(query.getTerm(i));
                 positions.retainAll(incrementedPositions);
             }
 
             if (!positions.isEmpty()) {
                 matchingDocs.add(documentId);
             }
         }
 
         return matchingDocs;
     }
 
     /**
      * It answers queries that contain only NOT operators
      *
      * Queries can be of form: A NOT B. The result returned
      * is of all documents containing A and that do not contain B.
      * In case the query is: NOT A. The method returns
      * all documents that do not contain A.
      *
      * @param query The NOT Query that needs to be answered
      * @return A list of documents that map the query
      */
     public ArrayList<String> handleNOTQuery(Query query){
 
         TreeSet<String> matchingDocs = new TreeSet<String>();
 
         ArrayList<String> terms = query.getTerms();
         for(String term:terms){
             TreeSet<String> matchedDocs = dataSet.getDocIdSet(term);
             matchingDocs.addAll(matchedDocs);
         }
 
         if(matchingDocs.isEmpty()){
             matchingDocs = dataSet.getDocSet();
         }
 
         ArrayList<String> notTerms = query.getNotTerms();
         for(String notTerm : notTerms){
             matchingDocs.removeAll(dataSet.getDocIdSet(notTerm));
         }
 
 
         return new ArrayList<String>(matchingDocs);
     }
 
     /**
      * Answers queries that contain only AND operators
      *
      * @param query
      * @return A list of documents that map the query
      */
     public ArrayList<String> handleANDQuery(Query query) {
         TreeSet<String> matchingDocs =
             new TreeSet<String>(dataSet.getDocIdSet(query.getTerm(0)));
 
         for (String term: query.getTerms()) {
             matchingDocs.retainAll(dataSet.getDocIdSet(term));
         }
 
         return new ArrayList<String>(matchingDocs);
     }
 
     /**
      * Answers queries that contain only OR operators
      *
      * @param query
      * @return A list of documents that map the query
      */
     public ArrayList<String> handleORQuery(Query query) {
         TreeSet<String> matchingDocs = new TreeSet<String>();
 
         for (String term : query.getTerms()) {
             matchingDocs.addAll(dataSet.getDocIdSet(term));
         }
 
         return new ArrayList<String>(matchingDocs);
     }
 }
