 package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import edu.cmu.lti.f12.hw2.hw2_team01.retrieval.MeSHQueryExpander;
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.util.CoreMap;
 
 public class MeSHKeytermUpdater extends AbstractKeytermUpdater {
 
   private MeSHQueryExpander meshExpander;
 
   @Override
   public void initialize(UimaContext context) throws ResourceInitializationException {
     super.initialize(context);
     String meshDatabasePath = (String) context.getConfigParameterValue("MeSH-database");
     log("Loading MeSH query expander with database: "+meshDatabasePath);
     meshExpander = new MeSHQueryExpander(meshDatabasePath);
   }
 
   @Override
   protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
     // 1. Collect noun phrases (rule-base chunking)
     StringBuffer buf = new StringBuffer();
 
     List<String> candidates = new ArrayList<String>();
     for (CoreMap sentence : StanfordPOSTagger.getInstance().annotate(question)) {
       for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
         String pos = token.get(PartOfSpeechAnnotation.class);
         if (pos.startsWith("N") || pos.equals("POS") || pos.startsWith("J")) {
           String word = token.get(TextAnnotation.class);
           buf.append(word+" ");
         }
         else {
           if(buf.length() > 0) {
             candidates.add(buf.toString().trim());
             buf.delete(0, buf.length());
           }
         }
       }
     }
     if(buf.length() > 0)
       candidates.add(buf.toString().trim());
 
     // 2. Check NP presence in database
     for(String candidate: candidates) {
       if(candidate.split(" ").length < 2) continue; // at least two words
       if(meshExpander.getSynomyms(candidate).size() > 0) {
        Keyterm keyterm = new Keyterm(candidate);
         keyterm.setComponentId("MESH");
         keyterms.add(keyterm);
       }
     }
     return keyterms;
   }
 }
