 package to.richard.tsp;
 
 /**
  * Author: Richard To
  * Date: 2/10/13
  */
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Implements simple windowing for now.
  *
  * Windowing is used for avoiding stagnation.
  *
  * This implementation just uses the lowest fitness.
  *
  * 1. Find the lowest fitness
  * 2. Subtract that fitness value from population.
 *
 * Note that windowing only works when high fitness is considered best.
 * If working with lowest fitness, then FPSMinimization may need to be used
 * first.
  */
 public class FPSWindowing implements IFPSTransform {
 
    private FitnessMaximizationComparator _comparator;
 
    public FPSWindowing() {
        _comparator = new FitnessMaximizationComparator();
     }
 
     @Override
     public List<Pair<Double, Genotype>> transform(List<Pair<Double, Genotype>> genotypeFitnessList) {
         Pair<Double, Genotype> leastFitGenotype = genotypeFitnessList.get(0);
         ArrayList<Pair<Double, Genotype>> newList = new ArrayList<Pair<Double, Genotype>>();
 
         for (Pair<Double, Genotype> pair : genotypeFitnessList) {
             if(_comparator.compare(leastFitGenotype, pair) > -1) {
                 leastFitGenotype = pair;
             }
         }
 
         for (Pair<Double, Genotype> pair : genotypeFitnessList) {
             newList.add(new Pair<Double, Genotype>(
                     pair.getFirstValue() - leastFitGenotype.getFirstValue(), pair.getSecondValue()));
         }
 
         return newList;
     }
 }
