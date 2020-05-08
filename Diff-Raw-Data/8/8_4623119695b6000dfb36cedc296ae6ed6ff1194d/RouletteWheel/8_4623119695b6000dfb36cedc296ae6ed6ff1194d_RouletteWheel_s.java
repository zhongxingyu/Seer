 package to.richard.tsp;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Author: Richard To
  * Date: 2/6/13
  */
 
 /**
  * Roulette Wheel algorithm implementation.
  *
  * 1. Take a list of elements and sum up all values.
  * 2. Divide each value by the sum
  * 3. Add each value to the roulette wheel starting with position 0.
  *    - If the current position is .25 and the next value is .3, the new
  *      position would be .55, etc.
  * 4. Randomly select a value between 0 and 1.
  * 5. Pick the element that the number falls between.
  * 6. Repeat until the desired number of samples have been selected.
  */
 public class RouletteWheel<E> implements ISampler<E> {
 
     protected IRandom _random;
 
     protected RouletteWheel() {}
 
     /**
      * Constructs a roulette wheel to randomly select objects, based on their weight.
      */
     public RouletteWheel(IRandom random) {
         _random = random;
     }
 
     /**
      * Picks one random element from distribution.
      *
      * Object is returned by reference.
      */
     public E sampleOne(List<Pair<Double, E>> valueMap) {
         TreeMap<Double, E> wheel = buildRouletteWheel(valueMap);
         double probability = _random.nextDouble();
         Map.Entry<Double, E> entry = wheel.higherEntry(probability);
         return entry.getValue();
     }
 
     /**
      * Samples n elements from roulette wheel.
      *
      * Object is returned by reference.
      */
     public ArrayList<E> sample(List<Pair<Double, E>> valueMap, int n) {
         TreeMap<Double, E> wheel = buildRouletteWheel(valueMap);
         ArrayList<E> sampledObjects = new ArrayList<E>();
         Map.Entry<Double, E> entry;
         double probability;
         for (int i = 0; i < n; i++) {
             probability = _random.nextDouble();
             entry = wheel.higherEntry(probability);
             sampledObjects.add(entry.getValue());
         }
         return sampledObjects;
     }
 
     public TreeMap<Double, E> buildRouletteWheel(List<Pair<Double, E>> valueMap) {
         TreeMap<Double, E> wheel = new TreeMap<Double, E>();
         double totalProbability = 0.0;
         double probability = 0.0;
         double totalWeight = 0;
 
         for (Pair<Double, E> pair : valueMap) {
             totalWeight += pair.getFirstValue();
         }
 
         for (Pair<Double, E> pair : valueMap) {
            probability = pair.getFirstValue() / totalWeight;
            totalProbability += probability;
            wheel.put(totalProbability, pair.getSecondValue());
         }
         return wheel;
     }
 }
