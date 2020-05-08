 package to.richard.tsp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Author: Richard To
  * Date: 2/11/13
  */
 
 /**
  * Implementation of replace worst (GENITOR) survivor selection
  *
  * Basically move the genotypes with the n-highest fitness values.
  *
  * n is the population size.
  */
 public class ReplaceWorstSurvivorSelection implements ISurvivorSelector {
 
     private FitnessEvaluator _fitnessEvaluator;
     private Comparator<Pair<Double, Genotype>> _comparator;
 
     public ReplaceWorstSurvivorSelection(
             FitnessEvaluator fitnessEvaluator, Comparator<Pair<Double, Genotype>> comparator) {
         _fitnessEvaluator = fitnessEvaluator;
         _comparator = comparator;
     }
 
     @Override
     public List<Genotype> replace(List<Genotype> parents, List<Genotype> offspring) {
 
         int populationSize = parents.size();
         int totalPopulation = populationSize * 2;
 
         ArrayList<Pair<Double, Genotype>> fitnessGenotypes = new ArrayList<Pair<Double, Genotype>>();
         ArrayList<Genotype> nextGeneration = new ArrayList<Genotype>();
 
         for (Genotype genotype : parents) {
             fitnessGenotypes.add(_fitnessEvaluator.evaluateAsPair(genotype));
         }
 
        for (Genotype genotype : offspring) {
             fitnessGenotypes.add(_fitnessEvaluator.evaluateAsPair(genotype));
         }
 
         Collections.sort(fitnessGenotypes, _comparator);
        for (int i = totalPopulation - 1; i >= populationSize; i--) {
             nextGeneration.add(fitnessGenotypes.get(i).getSecondValue());
         }
 
         return nextGeneration;
     }
 }
