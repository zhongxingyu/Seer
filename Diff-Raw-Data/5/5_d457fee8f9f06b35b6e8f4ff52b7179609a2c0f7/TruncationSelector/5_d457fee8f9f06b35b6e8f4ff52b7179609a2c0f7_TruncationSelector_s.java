 package SigmaEC.select;
 
 import SigmaEC.evaluate.ObjectiveFunction;
 import SigmaEC.represent.Individual;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Truncation selection strategy.
  * 
  * @author Eric 'Siggy' Scott
  */
 public class TruncationSelector<T extends Individual> extends Selector<T>
 {
     private ObjectiveFunction<T> objective;
 
     public ObjectiveFunction<T> getObjective()
     {
         return objective;
     }
     
     public TruncationSelector(ObjectiveFunction objective)
     {
         super();
         if (objective == null)
             throw new IllegalArgumentException("TruncationSelector(): objective is null.");
         this.objective = objective;
     }
     
     /**
      *  Loops through the population to find the highest fitness individual.
      *  In the case that this individual is not unique, the *latest* individual
      *  with the highest fitness value is chosen.
      */
     @Override
     public T selectIndividual(List<T> population) throws NullPointerException
     {
         if (population.isEmpty())
             throw new IllegalArgumentException("TruncationSelector.selectMultipleIndividuals(): population is empty.");
         
        double bestFitness = 0.0;
         T best = null;
         for (T ind : population)
         {
             double fitness = objective.fitness(ind);
             if (fitness >= bestFitness)
             {
                 bestFitness = fitness;
                 best = ind;
             }
         }
        
         return best;
     }
     
     /**
      * Sort the population (nondestructively) by fitness and select the top
      * [count] individuals.
      */
     @Override
     public List<T> selectMultipleIndividuals(List<T> population, int numToSelect) throws IllegalArgumentException, NullPointerException
     {
         if (numToSelect < 1)
             throw new IllegalArgumentException("TruncationSelector.selectMultipleIndividuals(): numToSelect is zero.");
         else if (population.isEmpty())
             throw new IllegalArgumentException("TruncationSelector.selectMultipleIndividuals(): population is empty.");
         else if (numToSelect > population.size())
             throw new IllegalArgumentException("TruncationSelector.selectMultipleIndividuals(): numToSelect is greater than population size.");
         
         List<T> sortedPop = new ArrayList(population);
         Collections.sort(sortedPop, new fitnessComparator());
         List<T> topIndividuals = new ArrayList(numToSelect);
         for (int i = 0; i < numToSelect; i++)
             topIndividuals.add(sortedPop.get(sortedPop.size() - 1 - i));
         return topIndividuals;
     }
     
     public boolean repOK()
     {
         return (objective != null);
     }
     
     private class fitnessComparator implements Comparator<T>
     {
         @Override
         public int compare(T ind1, T ind2)
         {
             double x = objective.fitness(ind1);
             double y = objective.fitness(ind2);
             
             if (x > y)
                 return 1;
             if (x < y)
                 return -1;
             return 0;
         }
         
     }
 }
