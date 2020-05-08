 package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 
 public class DeduplicateKeytermUpdater extends AbstractKeytermUpdater {
 
   private Map<String, Integer> priority;
 
   @Override
   public void initialize(UimaContext context) throws ResourceInitializationException {
     super.initialize(context);
     priority = new HashMap<String, Integer>();
     priority.put("MESH", 1);
     priority.put("GENE", 2);
     priority.put("DISE", 2);
   }
   
   private int getPriority(Keyterm keyterm) {
     Integer p = priority.get(keyterm.getComponentId());
     if(p == null) return 0;
     return p;
   }
   
   @Override
   protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
     HashMap<String, Keyterm> keytermMap = new HashMap<String, Keyterm>();
     for (Keyterm keyterm : keyterms) {
      String key = keyterm.getText().replace(" ", "");
       Keyterm existing = keytermMap.get(key);
       if(existing == null || getPriority(keyterm) > getPriority(existing))
       keytermMap.put(key, keyterm);
     }
     return new ArrayList<Keyterm>(keytermMap.values());
   }
 }
