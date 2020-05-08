 package pl.edu.agh.to1.dice.logic.computing;
 
 import pl.edu.agh.to1.dice.logic.DiceBucket;
 import pl.edu.agh.to1.dice.logic.Dice;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Piotr Turek
  */
 public class SequenceComputingStrategy extends AbstractPredicateComputingStrategy implements ComputingStrategy {
     private final int minCount;
     private final List<Boolean> values = new ArrayList<Boolean>(6);
 
     public SequenceComputingStrategy(int reward, int minCount) {
         super(reward);
         this.minCount = minCount;
         for (int i = 0; i < 6; i++) values.add(false);
     }
 
     @Override
     public boolean apply(DiceBucket diceBucket) {
         for (int i = 0; i < values.size(); i++) values.set(i, false);
         for (Dice dice : diceBucket) {
             values.set(dice.getState() - 1, true);
         }
 
         int seqLen = 0, longest = 0;
         for (int i = 0; i < values.size(); i++) {
             if (values.get(i)) seqLen++;
            else if (seqLen > longest) longest = seqLen;
         }
         if (seqLen > longest) longest = seqLen;
         if (longest >= minCount) return true;
         return false;
     }
 }
