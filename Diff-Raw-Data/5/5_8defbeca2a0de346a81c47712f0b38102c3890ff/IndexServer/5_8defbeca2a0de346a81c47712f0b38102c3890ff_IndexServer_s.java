 package edu.umich.eecs485.pa4;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Iterator;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.json.simple.parser.ContainerFactory;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import edu.umich.eecs485.pa4.utils.QueryHit;
 import edu.umich.eecs485.pa4.utils.GenericIndexServer;
 
 /*******************************************************
  * The <code>IndexServer</code> loads an inverted index and processes
  * user queries.  It returns Hit objects that are then returned to the
  * PHP server over the network.
  *
  * Its superclass is GenericIndexServer, which provides basic network
  * and serialization functionality.
  *******************************************************/
 public class IndexServer extends GenericIndexServer {
     
     
    static HashMap<String, List<DocItem>> map;
     
   /**
    * Creates a new <code>IndexServer</code> instance.
    *
    * The superclass needs a port to listen on.
    * We store fname in a member variable for later use.
    */
   public IndexServer(int port, File fname) throws IOException {
     super(port, fname);
   }
 
   /**
    * This method is called once when the server is first started.
    * Inside this method you should load the inverted index from disk.
    *
    * Fill in this method to do something useful!
    */
   public void initServer(File fname) {
     // Do something!
     System.err.println("Init server with fname " + fname);
     
     // here we will load the serialized map object back into mem.
   
     map = new HashMap<String, List<DocItem>>();
     
     try {
         BufferedReader read = new BufferedReader(new FileReader(fname));
         
         String s = read.readLine();
         JSONParser parser = new JSONParser();
         
         ContainerFactory containerFactory = new ContainerFactory(){
             public List creatArrayContainer() {
                 return new ArrayList();
             }
 
             public Map createObjectContainer() {
                 return new HashMap();
             }
         };
 
         Map json = (Map)parser.parse(s, containerFactory);
         Iterator iter = json.entrySet().iterator();
         DocItem item;
         Map temp;
         List<DocItem> listDocItem;
         Map.Entry entry;
         String word;
         List docList;
         Map<String, Long> tf;
         
         while(iter.hasNext()){
             entry = (Map.Entry) iter.next();
             
             word = (String) entry.getKey();
             docList = (List) entry.getValue();
             
             listDocItem = new ArrayList<DocItem>();
             
             for (int j=0; j<docList.size(); j++) {
                temp = (Map) docList.get(j);
                tf = (Map<String, Long>) parser.parse( (String) temp.get("tf"), containerFactory);
                
                item = new DocItem( 
                        (String) temp.get("id"), 
                        ((Double) temp.get("score")).doubleValue(), 
                        (String) temp.get("url"), 
                        (String) temp.get("caption"), 
                        (HashMap<String, Long>) tf
                     );
                listDocItem.add(item);
             }
             
             map.put(word, listDocItem);
         }
         
     } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
   }
   
 
   /**
    * The <code>processQuery</code> method takes a user query and
    * returns a relevance-ranked and scored list of document hits.
    * If the list is empty, then there are zero hits for the query. 
    *
    * This method should never return null.
    *
    * Fill in this method to do something useful!
    */
   
   public List<QueryHit> processQuery(String query) {
       System.out.println("Processing query '" + query + "'");
       ArrayList<QueryHit> result = new ArrayList<QueryHit>();
       
       // Split query String into words
      String [] words = query.split("\\s*[^0-9a-zA-Z']+\\s*"); 
       String word;
       int totalWords = words.length;
       
       
       HashSet<DocItem> union = new HashSet<DocItem>(); 
       
       for (int i=0; i<totalWords; i++) {
           word = words[i];
           
           if (map.get(word) != null) {
               union.addAll(map.get(word));
           }
       }
       
       for (DocItem item: union) {
           result.add( new QueryHit(item.getIdentifier(), calScore(words, item)) );
       }
       
       // this will sort doc item in descending order
       Collections.sort( result, new DocItemComparator());
       
       return result;
   }
   
   public static class DocItemComparator implements Comparator<QueryHit>{
       // this will sort doc item in descending order
       
       @Override
       public int compare(QueryHit o1, QueryHit o2) {
           if (o1.getScore() > o2.getScore() ) {
               return -1;
           } else if (o1.getScore() == o2.getScore() ) {
               return 0;
           } else {
               return 1;
           }
       }
   }
   
   
   
   public List<QueryHit> processQuery2(String query) {
       
       System.out.println("Processing query '" + query + "'");
       ArrayList<QueryHit> result = new ArrayList<QueryHit>();
       
       // Split query String into words
       String [] words = query.split(" "); 
       int totalWords = words.length;
       
       ArrayList<String> queryWords = new ArrayList<String>();
       HashMap<String, Integer> searchMap = new HashMap<String, Integer>();
       
       for (int i=0; i < totalWords; i++) {
           if (map.get(words[i]) == null) {
               return result;
           }
           queryWords.add(words[i]);
           searchMap.put(words[i], new Integer(0));
       }
       
       // start from first word 
       String nextWord = queryWords.get(0);
       int min = map.get(nextWord).get(0).getIntId();
       
       if (totalWords == 1 && map.get(nextWord) != null) {
           // single word query
           System.out.println("single word");
           for (DocItem item: map.get(nextWord)) {
               result.add( new QueryHit(item.getIdentifier(), calScore(words, item)) );
           }
       } else { 
           // multiple word query
           System.out.println("multi word");
           boolean foundFlag;
           while ( searchEnd(searchMap) ) {
               foundFlag = false;
 
               String word, preWord;
               int pos, prePos;
               DocItem foundDocItem;
               Integer temp;
               
               // 1. check if we found a result
               for (int i=1; i<totalWords; i++) {
                   word = queryWords.get(i);
                   preWord = queryWords.get(i-1); 
                   pos = searchMap.get(word).intValue();
                   prePos = searchMap.get(preWord).intValue();
                   if (map.get(word).get(pos).getIntId() != map.get(preWord).get(prePos).getIntId()) {
                       System.out.println("not found incre pointer");
                       // if we didn't find a word
                       // find the word where the pointer have smallest doc id
                       // get smallest doc id among all words
                       for (String curWord : queryWords) {
                           pos = searchMap.get(curWord).intValue();
                           if (map.get(curWord).get(pos).getIntId() < min) {
                               min = map.get(curWord).get(pos).getIntId();
                               nextWord = curWord;
                           }
                       }
 
                       temp = searchMap.get(nextWord);
                       searchMap.put(nextWord, new Integer(temp.intValue() + 1));
                   }
 
               }
               
               // found one word
               word = queryWords.get(0);
               pos = searchMap.get(word).intValue();
               foundDocItem = map.get(word).get(pos);
               
               result.add(new QueryHit(foundDocItem.getIdentifier(), calScore(words, foundDocItem)) );
 
               // increament pointer for each word in query words
               for (String eachWord : queryWords) {
                   searchMap.put(eachWord, new Integer(searchMap.get(eachWord).intValue() + 1));
               }
 
               System.out.println("found one word");
               
               for (String testWord : queryWords) {
                   System.out.println(testWord + " " + searchMap.get(testWord));
               }
           }
       }
       System.out.println(result.size());
       
       return result;
   }
   
   public static boolean searchEnd(HashMap<String, Integer> searchMap) {
       for (Map.Entry<String, Integer> entry : searchMap.entrySet() ) {
           // entry.getValue(): index of DocItemList,  length - 1
           System.out.println("cur pos " + entry.getValue());
           System.out.println("max siz " + map.get(entry.getKey()).size());
           if (entry.getValue() == map.get(entry.getKey()).size() - 1) {
               System.out.println("false");
               return false;
           }
       }
       System.out.println("true");
       return true;
   }
 
   private double calScore(String [] words, DocItem docItem){
   
       HashMap<String, Double> queryTf = new HashMap<String, Double>();
       HashMap<String, Double> idf = new HashMap<String, Double>();
 
       double de1 = 0;
       double de2 = 0;
       double nu = 0;
 
       String word;
       for (int i=0; i< words.length; i++) {
          word = words[i];
 					
           if ( !queryTf.containsKey(word) ) {
               queryTf.put(word, new Double(1)); 
           } else {
               queryTf.put(word, Double.valueOf( queryTf.get(word) + 1)); 
           }
           // TODO make "totalDocument" a global double variable, for PA4, it should be 200
           double totalDocument = 200.0;
           if( !idf.containsKey(word)) {
               double temptemp = 0;
               if( map.containsKey(word)) {
                   temptemp =(double) map.get(word).size();
               } else {
                   temptemp = 0;
               }
 	          idf.put(word, Math.log10(((totalDocument + 1.0)/(temptemp + 1.0) )) );
           }
       }
 
       double result = 0, temp1 = 0, temp2 = 0;
       for (int i=0; i< words.length; i++) {
           word = words[i];
           temp1 = queryTf.get(word) * idf.get(word);
           if(docItem.tf.containsKey(word)) {
               temp2 = docItem.tf.get(word) * idf.get(word);
           } else {
               temp2 = 0;
           }
           nu += temp1 * temp2;
           de1 += temp1 * temp1;
           de2 += temp2 * temp2;
       }
 
       
       if(de2 == 0) {
           return 0;
       }
       
       result = nu / (Math.sqrt(de1) * Math.sqrt(de2));
 
       return result;
   }
   
   /**
    * Parse the command-line args.  Then start up the server.
    */
   public static void main(String argv[]) throws IOException {
     if (argv.length < 2) {
       System.err.println("Usage: IndexServer <portnum> <inverted-index-filename>");
       return;
     }
 
     // Parse args
     int i = 0;
     int portnum = -1;
     try {
       portnum = Integer.parseInt(argv[i++]);
     } catch (NumberFormatException nfe) {
       System.err.println("Cannot parse port number: " + argv[i-1]);
       return;
     }
     File fname = new File(argv[i++]).getCanonicalFile();
 
     // Run server.  Note that because server.serve() creates a new
     // thread, the process will not terminate even though serve() returns.
     IndexServer server = new IndexServer(portnum, fname);
     server.serve();
   }
 }
