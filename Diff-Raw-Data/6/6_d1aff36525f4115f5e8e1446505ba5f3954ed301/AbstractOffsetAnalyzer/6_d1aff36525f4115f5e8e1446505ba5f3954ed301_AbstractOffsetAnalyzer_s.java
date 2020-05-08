 package org.clafer.ast.analysis;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.clafer.ast.AstAbstractClafer;
 import org.clafer.ast.AstClafer;
 import org.clafer.ast.Card;
 
 /**
  *
  * @author jimmy
  */
 public class AbstractOffsetAnalyzer implements Analyzer {
 
     @Override
     public Analysis analyze(final Analysis analysis) {
         Map<AstAbstractClafer, Offsets> offsetsMap = new HashMap<>();
         for (AstAbstractClafer abstractClafer : analysis.getAbstractClafers()) {
             Map<AstClafer, Integer> offsets = new HashMap<>();
             List<AstClafer> reverseOffsets = new ArrayList<>();
             int offset = 0;
             List<AstClafer> subs = new ArrayList<>(abstractClafer.getSubs());
             /*
              * What is this optimization?
              *
              * This optimization is to put more "stable" Clafers near the
              * beginning, to reduce the number of backtracking for LowGroups.
              */
             Collections.sort(subs, new Comparator<AstClafer>() {
                 @Override
                 public int compare(AstClafer o1, AstClafer o2) {
                     Card card1 = analysis.getGlobalCard(o1);
                     Card card2 = analysis.getGlobalCard(o2);
                    double ratio1 = ((double) card1.getLow()) / ((double) card1.getHigh());
                    double ratio2 = ((double) card2.getLow()) / ((double) card2.getHigh());
                    return Double.compare(ratio1, ratio2);
                 }
             });
             for (AstClafer sub : subs) {
                 offsets.put(sub, offset);
                 int skip = analysis.getScope(sub);
                 for (int i = 0; i < skip; i++) {
                     reverseOffsets.add(sub);
                 }
                 offset += skip;
             }
             offsetsMap.put(abstractClafer, new Offsets(abstractClafer, offsets, reverseOffsets.toArray(new AstClafer[reverseOffsets.size()])));
         }
         return analysis.setOffsetMap(offsetsMap);
     }
 }
