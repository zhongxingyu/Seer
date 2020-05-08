 package lab2.document;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import lab2.indexing.MyAnalyser;
 import lab2.prefixtree.Node;
import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Terms;
 import org.apache.lucene.index.TermsEnum;
 import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
 
 /**
  *
  * @author Gang
  */
 public class DocumentStatistics {
 
     private ArrayList<ArrayList<WordCount>> documentStatistics;
     private ArrayList<WordCount> collectionStatistics;
     IndexReader indexReader;
     Node documentPrefixTree;
     Node collectionPrefixTree;
 
     public DocumentStatistics(String indexPath) {
         documentStatistics = new ArrayList<>();
         collectionStatistics = new ArrayList<>();
         collectionPrefixTree = new Node();
         try {
             indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
         } catch (IOException ex) {
             Logger.getLogger(DocumentStatistics.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public ArrayList<ArrayList<WordCount>> getDocumentStatistics() {
         return documentStatistics;
     }
 
     
     
     public ArrayList<WordCount> getCollectionStatistics() {
         return collectionStatistics;
     }
 
     public void classifyFiles() {
         try {
             int maxDocs = indexReader.maxDoc();
             for (int i = 0; i < maxDocs; ++i) {
                 Terms termVector = indexReader.getTermVector(i, "contents");
                 TermsEnum termsEnum = termVector.iterator(TermsEnum.EMPTY);
 
                 documentPrefixTree = new Node();
 
                 while (termsEnum.next() != null) {
                     //System.out.println(termsEnum.term().utf8ToString());
                     documentPrefixTree.addWord(termsEnum.term().utf8ToString());
                     collectionPrefixTree.addWord(termsEnum.term().utf8ToString());
                 }
                 ArrayList<WordCount> documentWordInfo = documentPrefixTree.getWordCounts();
                 // wordCounts.get(0).showInfo();
                 //  wordCounts.get(0).showInfo();
                 documentStatistics.add(documentWordInfo);
                 //  System.out.println("another doc");
             }
             collectionStatistics = collectionPrefixTree.getWordCounts();
 
         } catch (IOException ex) {
             Logger.getLogger(DocumentStatistics.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
