 package zemberek3.lexicon;
 
import java.util.*;

 public class TurkishSuffix {
 
     public final String id;
    List<SuffixFormSet> formSets = new ArrayList<SuffixFormSet>();
    Map<String, SuffixFormSet> nodeMap = new HashMap<String, SuffixFormSet>();
 
     public TurkishSuffix(String id) {
         this.id = id;
     }
 
    public TurkishSuffix addNode(SuffixFormSet... sets) {
        this.formSets.addAll(Arrays.asList(sets));
        for (SuffixFormSet set : sets) {
            nodeMap.put(set.getId(), set);
        }
        return this;
    }

     @Override
     public String toString() {
         return id;
     }
 }
