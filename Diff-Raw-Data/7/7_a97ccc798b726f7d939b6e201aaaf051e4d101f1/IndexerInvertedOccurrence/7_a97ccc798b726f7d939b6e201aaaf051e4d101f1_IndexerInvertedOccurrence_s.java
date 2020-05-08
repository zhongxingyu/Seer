 package edu.nyu.cs.cs2580;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.File;
 import edu.nyu.cs.cs2580.SearchEngine.Options;
 
 /**
  * @CS2580: Implement this class for HW2.
  */
 public class IndexerInvertedOccurrence extends IndexerInverted implements Serializable{
 
 
 private class DocOccPair implements Serializable{
     private static final long serialVersionUID = 1027111905740085030L;
     private int _did;
     private ArrayList<Integer> _occs = new ArrayList<Integer>();
     public DocOccPair (int did, int occ) {
         _did = did;
         _occs.add(occ);
     }
     public int getDid() {
         return _did;
     }
     public void setDid(int did) {
         _did = did;
     }
     public void InsertOcc(int occs) {
         _occs.add(occs);
     }
     public int Frequency() {
         return _occs.size();
     }
 }
 
 
   private static final long serialVersionUID = 1057111905740085030L;
     //  private Map<Integer, Integer> _termDocFrequency = new HashMap<Integer,Integer>();
   private Map<Integer, ArrayList<DocOccPair> > _termToOccus =
         new HashMap<Integer, ArrayList<DocOccPair> > ();
 
 
   public IndexerInvertedOccurrence(Options options) {
     super(options);
     System.out.println("Using Indexer: " + this.getClass().getSimpleName());
   }
 
   @Override
   public String getIndexFilePath() {
       return _options._indexPrefix + "/corpus_invertedOccurrence.idx";
   }
   @Override
   public void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques,
                                   int did, int offset) {
     Integer token;
     for (int i = 0; i<tokens.size();i++) {
         uniques.add(tokens.get(i));
         token = tokens.get(i);
         ArrayList<DocOccPair> dop = _termToOccus.get(token);
         if (dop.size()==0 || dop.get(dop.size()-1).getDid()!=did) {
             dop.add(new DocOccPair(did,offset+i));
         }
         else {
             dop.get(dop.size()-1).InsertOcc(offset+i);
         }
         ++_totalTermFrequency;
     }
   }
   @Override
   public void updateUniqueTerms (Set<Integer> uniqueTerms,int did) {
       //    for (Integer idx : uniqueTerms) {
       //        _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
       //    }
   }
   @Override
   public void addToken(String token, Vector<Integer> tokens) {
       int idx = -1;
       if (_dictionary.containsKey(token)) {
         idx = _dictionary.get(token);
       } else {
         idx = _terms.size();
         _terms.add(token);
         _dictionary.put(token, idx);
         _termToOccus.put(idx,new ArrayList<DocOccPair>());
         //        _termDocFrequency.put(idx, 0);
       }
       tokens.add(idx);
   }
 
   public void loadIndex() {
       try {
       String indexFile = _options._indexPrefix + "/corpus_invertedOccurrence.idx";
       System.out.println("Load index from: " + indexFile);
 
       ObjectInputStream reader =
           new ObjectInputStream(new FileInputStream(indexFile));
       IndexerInvertedOccurrence loaded = null;
       try {
           loaded = (IndexerInvertedOccurrence) reader.readObject();
       } catch (ClassNotFoundException e) {
       }
 
       this._documents = loaded._documents;
       // Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
       this._numDocs = _documents.size();
       for (ArrayList<DocOccPair> freq: loaded._termToOccus.values()) {
           this._totalTermFrequency+= freq.size();
       }
       this._dictionary = loaded._dictionary;
       this._terms = loaded._terms;
       this._termToOccus = loaded._termToOccus;
       //      this._termDocFrequency = loaded._termDocFrequency;
       reader.close();
 
       System.out.println(Integer.toString(_numDocs) + " documents loaded " +
                          "with " + Long.toString(_totalTermFrequency) + " terms!");
       } catch (IOException e) {
       }
       //      output();
   }
 
   @Override
   public Document getDoc(int docid) {
     SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
     return null;
   }
 
   /**
    * In HW2, you should be using {@link DocumentIndexed}.
    */
  @Override
   public Document nextDoc(Query query, int docid) {
     return null;
   }
 
  @Override
   public int corpusDocFrequencyByTerm(String term) {
       if (!_dictionary.containsKey(term))
           return 0;
       Integer idx = _dictionary.get(term);
       return _termToOccus.get(idx).size();
   }
 
  @Override
   public int corpusTermFrequency(String term) {
       if (!_dictionary.containsKey(term))
           return 0;
       Integer idx = _dictionary.get(term);
       ArrayList<DocOccPair> dops = _termToOccus.get(idx);
       int ret = 0;
       for (DocOccPair dop : dops)
           ret+=dop.Frequency();
       return ret;
   }
 
  @Override
   public int documentTermFrequency(String term, String url) {
     SearchEngine.Check(false, "Not implemented!");
     return 0;
   }
   public void output() {
       System.out.println("_numDocs="+Integer.toString(_numDocs));
       System.out.println("_totalTermFrequency="+Long.toString(_totalTermFrequency));
       for (int i = 0; i<_terms.size();i++) {
           System.out.println(_terms.get(i)+
                            ":"+Integer.toString(corpusTermFrequency(_terms.get(i)))+
                            ":"+Integer.toString(corpusDocFrequencyByTerm(_terms.get(i))));
           /*          Vector<DocOccPair> dop = _termToOccus.get(i);
           for (int j = 0;j<dop.size();j++) {
               System.out.println(Integer.toString(i)+" "+Integer.toString(dop.get(j).getOcc())+" "+Integer.toString(dop.get(j).getDid()));
           }
           System.out.println("===");*/
       }
   }
 }
