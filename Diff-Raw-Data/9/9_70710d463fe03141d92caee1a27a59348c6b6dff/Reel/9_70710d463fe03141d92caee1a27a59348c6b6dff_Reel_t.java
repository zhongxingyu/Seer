 package me.DDoS.MCCasino.slotmachine;
 
import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.inventory.ItemStack;
 /**
  *
  * @author DDos
  */
 public class Reel {
     
    private static final Random random = new SecureRandom();
    //
     private final List<ReelValue> values;
     
     public Reel(List<ReelValue> values) {
         
         this.values = values;
         
     }
     
     public ItemStack getRandomItem() {
         
         final List<Integer> weightedNumbers = new ArrayList<Integer>();
         
         for (ReelValue value : values) {
             
            weightedNumbers.add((random.nextInt(50000 * value.getProb()) + 1));
             
         }
         
         int highestNumber = Integer.MIN_VALUE;
         int winnerPos = 0;
         int i2 = 0;
         
         for (int weightedNumber : weightedNumbers) {
             
             if (weightedNumber > highestNumber) {
                 
                 highestNumber = weightedNumber;
                 winnerPos = i2;
                 
             }
             
             i2++;
             
         }
         
         return values.get(winnerPos).getItem();
         
     }
 }
