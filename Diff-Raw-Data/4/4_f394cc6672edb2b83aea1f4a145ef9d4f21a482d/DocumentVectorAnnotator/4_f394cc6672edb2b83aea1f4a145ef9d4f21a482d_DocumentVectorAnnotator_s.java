 package edu.cmu.lti.f13.hw4.hw4_nluevisa.annotators;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSList;
 import org.apache.uima.jcas.cas.IntegerArray;
 import org.apache.uima.jcas.cas.StringArray;
 import org.apache.uima.jcas.tcas.Annotation;
 
 import edu.cmu.lti.f13.hw4.hw4_nluevisa.typesystems.Document;
 import edu.cmu.lti.f13.hw4.hw4_nluevisa.typesystems.Token;
 import edu.cmu.lti.f13.hw4.hw4_nluevisa.utils.Stemmer;
 import edu.cmu.lti.f13.hw4.hw4_nluevisa.utils.Utils;
 import edu.cmu.lti.f13.hw4.hw4_nluevisa.utils.*;
 /**
  * Class to annotate the document and create token list for Document
  */
 public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {
 
   /**
    * A Set containing stop words
    * 
    */
   Set<String> stopWordSet;
 	@Override
 	public void process(JCas jcas) throws AnalysisEngineProcessException {
 	  
 	  /**
 	   * Read stop word string from files and add them to stopWordSet 
 	   * 
 	   */
 	  stopWordSet = new TreeSet<String>();
	  BufferedReader br = null;
	  try {
 	      br = new BufferedReader(new FileReader("src/main/resources/stopwords.txt"));
         String line = br.readLine();
         
         while (line != null) {
           stopWordSet.add(line);
           line = br.readLine();
         }
        
     } catch (FileNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } finally {
         try {
           br.close();
         } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
     }
 	  /*****************************************/
 	  
 		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
 		if (iter.isValid()) {
 			iter.moveToNext();
 			Document doc = (Document) iter.get();
 			createTermFreqVector(jcas, doc);
 		}
 
 	}
 	/**
 	 * Create the token list for the document and add it to document itself.
 	 * @param jcas
 	 * @param doc - Document in which we want to create term frequency
 	 */
 
 	private void createTermFreqVector(JCas jcas, Document doc) {
 	  
 		String docText = doc.getText();
 		//TO DO: construct a vector of tokens and update the tokenList in CAS
 		Stemmer s = new Stemmer();
 		
 		ArrayList<Token> arrTok = new ArrayList<Token>();
 		
 		// iterate through sentence and set term frequency
 		Map<String,Integer> termFreq = new HashMap<String, Integer>();
 		String[] terms = docText.split(" ");
 		for(String term : terms){
 		  
 		  term = term.toLowerCase();
 		  s.add(term.toCharArray(), term.length());
 		  s.stem();
 		  term = s.toString();
 		  
 		  /* Experiment with removing stop words */
 //		  if(stopWordSet.contains(term))
 //		  {
 //		    continue;
 //		  }
 		  
 		  if(termFreq.containsKey(term))
 		  {
 		    int freq = termFreq.get(term) + 1;
 		    termFreq.put(term, freq);
 		  }
 		  else
 		  {
 		    termFreq.put(term, 1);
 		  }
 		}
 		
 		//create tokenList
 		Iterator it = termFreq.entrySet().iterator();
     while (it.hasNext()) {
         Map.Entry pairs = (Map.Entry)it.next();
         String term = (String)pairs.getKey();
         int freq = (Integer)pairs.getValue();
         Token tok = new Token(jcas);
         tok.setText(term);
         tok.setFrequency(freq);
         arrTok.add(tok);
     }
     
     //convert collection of token to FSList
     FSList tokenList = Utils.fromCollectionToFSList(jcas,arrTok);
     
     //add it to document
     doc.setTokenList(tokenList);
 	}
 
 }
