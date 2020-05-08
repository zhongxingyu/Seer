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
  * Implementation of Elitism using Aged-based replacement.
  *
  * Replace fittest parent with worst offspring. If no worst offspring,
  * don't do replacement.
  */
 public class ElitismSurvivorSelection implements ISurvivorSelector {
 
     private FitnessEvaluator _fitnessEvaluator;
     private Comparator<Pair<Double, Genotype>> _comparator;
 
     public ElitismSurvivorSelection(
             FitnessEvaluator fitnessEvaluator, Comparator<Pair<Double, Genotype>> comparator) {
         _fitnessEvaluator = fitnessEvaluator;
         _comparator = comparator;
     }
 
     @Override
     public List<Genotype> replace(List<Genotype> parents, List<Genotype> offspring) {
         ArrayList<Pair<Double, Genotype>> fitnessParents = new ArrayList<Pair<Double, Genotype>>();
         ArrayList<Pair<Double, Genotype>> fitnessOffspring = new ArrayList<Pair<Double, Genotype>>();
         ArrayList<Genotype> nextGeneration = new ArrayList<Genotype>();
 
         Pair<Double, Genotype> fittestGenotypePair;
 
         for (Genotype genotype : parents) {
             fitnessParents.add(_fitnessEvaluator.evaluateAsPair(genotype));
         }
         Collections.sort(fitnessParents, _comparator);
 
        for (Genotype genotype : offspring) {
             fitnessOffspring.add(_fitnessEvaluator.evaluateAsPair(genotype));
         }
         Collections.sort(fitnessOffspring, _comparator);
 
         for (Pair<Double, Genotype> pair : fitnessOffspring) {
             nextGeneration.add(pair.getSecondValue());
         }
 
         fittestGenotypePair = fitnessParents.get(fitnessParents.size() - 1);
        if (_comparator.compare(fittestGenotypePair, fitnessOffspring.get(0)) > 0) {
             nextGeneration.set(0, fittestGenotypePair.getSecondValue());
         }
 
         return nextGeneration;
     }
 }
