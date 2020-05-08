 package com.orii.uima;
 
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 
 public class SentenceAnnotator extends JCasAnnotator_ImplBase {
 
   @Override
   public void process(JCas aJCas) throws AnalysisEngineProcessException {
     // get document text
     String docText = aJCas.getDocumentText();
     String[] lines = docText.split("\\r?\\n");
     
     for (String line : lines) {
       Sentence annotation = new Sentence(aJCas);
       
      int spaceIndex = line.indexOf(" ");
      String id = line.substring(0, spaceIndex);
      String string = line.substring(spaceIndex + 1);
       
       annotation.setId(id);
       annotation.setRawString(string);
       annotation.addToIndexes();
     }
   }
 
 }
