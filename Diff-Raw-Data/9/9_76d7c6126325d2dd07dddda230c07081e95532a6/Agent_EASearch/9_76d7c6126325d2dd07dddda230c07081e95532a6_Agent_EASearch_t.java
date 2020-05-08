 package pikater;
 
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 import java.util.Random;
 import pikater.evolution.MergingReplacement;
 import pikater.evolution.Population;
 import pikater.evolution.RandomNumberGenerator;
 import pikater.evolution.Replacement;
 import pikater.evolution.individuals.Individual;
 import pikater.evolution.individuals.SearchItemIndividual;
 import pikater.evolution.operators.OnePtXOver;
 import pikater.evolution.operators.Operator;
 import pikater.evolution.operators.SearchItemIndividualMutation;
 import pikater.evolution.selectors.Selector;
 import pikater.evolution.selectors.TournamentSelector;
 import pikater.evolution.surrogate.SearchItemIndividualArchive;
 import pikater.evolution.surrogate.SurrogateMutationOperator;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.SearchItem;
 import pikater.ontology.messages.SearchSolution;
 
 public class Agent_EASearch extends Agent_Search {
     /*
      * Implementation of Genetic algorithm search
      * Half uniform crossover, tournament selection
      * Options:
      * -E float
      * minimum error rate (default 0.1)
      * 
      * -M int 
      * maximal number of generations (default 10)
      * 
      * -I int
      * maximal number of evaluated configurations (default 100)
      * 
      * -T float
      * Mutation rate (default 0.2)
      * 
      * -F float
      * Mutation rate per field in individual (default 0.2)
      * 
      * -X float
      * Crossover probability (default 0.5)
      * 
      * -P int
      * population size (default 10)
      * 
      * -L float
      * The percentage of elite individuals (default 0.1)
      * 
      * 
      */
 
     //fitness is the error rate - the lower, the better!
     SearchItemIndividualArchive archive = new SearchItemIndividualArchive();
     Population parents;
     Population offspring;
     Population toEvaluate = new Population();
     Population evaluated = new Population();
     Replacement replacement = new MergingReplacement();
     java.util.ArrayList<Selector> environmentalSelectors;
     java.util.ArrayList<Selector> matingSelectors;
     java.util.ArrayList<Operator> operators;
     double eliteSize = 0.1;
     int popSize = 10;
     double mutProb = 0.0;
     double mutProbPerField = 0.0;
     double xOverProb = 0.0;
     private int genNumber = 0;
     private double bestError = Double.MAX_VALUE;
     private int maxGeneration = 5;
     private double goalError = 0.1;
     private int maxEval = 100;
     protected Random rng = RandomNumberGenerator.getInstance().getRandom();
     /**
      *
      */
     private static final long serialVersionUID = -387458001824777077L;
     
     @Override
     protected List generateNewSolutions(List solutions, float[][] evaluations) {
 
         offspring = new Population();
         offspring.setPopulationSize(popSize);
         
         genNumber++;
         
         if (evaluations == null) {
             //create new population
             
             matingSelectors = new java.util.ArrayList<Selector>();
             environmentalSelectors = new java.util.ArrayList<Selector>();
             operators = new java.util.ArrayList<Operator>();
             archive = new SearchItemIndividualArchive();
             
             environmentalSelectors.add(new TournamentSelector());
             operators.add(new OnePtXOver(xOverProb));
            operators.add(new SearchItemIndividualMutation(mutProb, mutProbPerField, 0.1));
            operators.add(new SurrogateMutationOperator(archive, 0.2));
             
             parents = new Population();
             parents.setPopulationSize(popSize);
             
             List schema = getSchema();
             
             SearchItemIndividual sampleIndividual = new SearchItemIndividual(schema.size());
             Iterator it = schema.iterator();
             for (int i = 0; it.hasNext(); i++) {
                 sampleIndividual.set(i, "");
                 sampleIndividual.setSchema(i, (SearchItem)it.next());
             }
             
             parents.setSampleIndividual(sampleIndividual);
             
             genNumber = 0;
             bestError = Double.MAX_VALUE;
             parents.createRandomInitialPopulation();
             
             return populationToList(parents);
         }
         
         Population matingPool = new Population();
 
         if (matingSelectors.size() > 0) {
             int mateSel = matingSelectors.size();
             int toSelect = parents.getPopulationSize()/mateSel;
             for (int i = 0; i < matingSelectors.size(); i++) {
                 Population sel = new Population();
                 matingSelectors.get(i).select(toSelect, parents, sel);
                 matingPool.addAll((Population)sel.clone());
             }
 
             int missing = parents.getPopulationSize() - matingPool.getPopulationSize();
             if (missing > 0) {
                 Population sel = new Population();
                 matingSelectors.get(matingSelectors.size()-1).select(toSelect, parents, sel);
                 matingPool.addAll((Population)sel.clone());
             }
         } else
         {
             matingPool = (Population)parents.clone();
         }
         
         offspring = null;
         for (Operator o : operators) {
             offspring = new Population();
             o.operate(matingPool, offspring);
             matingPool = offspring;
         }
         
         toEvaluate.clear();
         evaluated.clear();
         
         for (int i = 0; i < offspring.getPopulationSize(); i++) {
             if (archive.contains((SearchItemIndividual)offspring.get(i))) {
                 offspring.get(i).setFitnessValue(archive.getFitness((SearchItemIndividual)offspring.get(i)));
                 evaluated.add(offspring.get(i));
                 continue;
             }
             toEvaluate.add(offspring.get(i));
         }
         
         return populationToList(toEvaluate);
         
     }
 
     private List populationToList(Population pop) {
         
         List ret = new ArrayList();
         for (Individual i : pop.getSortedIndividuals()) {
             SearchItemIndividual si = (SearchItemIndividual)i;
             ArrayList vals = new ArrayList();
             
             for (int j = 0; j < si.length(); j++) {
                 vals.add(si.get(j).toString());
             }
             
             SearchSolution ss = new SearchSolution();
             ss.setValues(vals);
             ret.add(ss);
         }
         return ret;
     }
     
     @Override
     protected void updateFinished(float[][] evaluations) {
         
         //assign evaluations to the population as fitnesses		
         if (evaluations == null) {
             for (int i = 0; i < popSize; i++) {
                 offspring.get(i).setFitnessValue(1);
             }
             return;
         }
         
         //initial generation -- evaluate the random population
         if (genNumber == 0) {
             for (int i = 0; i < evaluations.length; i++) {
                 parents.get(i).setFitnessValue(-evaluations[i][0]);
                 if (evaluations[i][0] < bestError) {
                     bestError = evaluations[i][0];
                 }
                 archive.add((SearchItemIndividual)parents.get(i));
             }
             return;
         }
         
         for (int i = 0; i < evaluations.length; i++) {
             toEvaluate.get(i).setFitnessValue(-evaluations[i][0]);
             if (evaluations[i][0] < bestError) {
                 bestError = evaluations[i][0];
             }
             archive.add((SearchItemIndividual)toEvaluate.get(i));
         }
         
         offspring.clear();
         offspring.addAll(toEvaluate);
         offspring.addAll(evaluated);
         
         Population selected = new Population();
 
         java.util.ArrayList<Individual> sortedOld = parents.getSortedIndividuals();
         for (int i = 0; i < eliteSize*parents.getPopulationSize(); i++) {
             selected.add(sortedOld.get(i));
         }
         
         Population combined = replacement.replace(parents, offspring);
         
         System.err.println("COMBINED: " + combined.getPopulationSize());
         
         int envSel = environmentalSelectors.size();
         int toSelect = (parents.getPopulationSize() - selected.getPopulationSize())/envSel;
         for (int i = 0; i < environmentalSelectors.size(); i++) {
             Population sel = new Population();
             environmentalSelectors.get(i).select(toSelect, combined, sel);
             selected.addAll((Population)sel.clone());
         }
 
         int missing = parents.getPopulationSize() - selected.getPopulationSize();
         if (missing > 0) {
             Population sel = new Population();
             environmentalSelectors.get(environmentalSelectors.size() - 1).select(toSelect, combined, sel);
             selected.addAll((Population)sel.clone());
         }
 
         parents.clear();
         parents.addAll(selected);
         
         System.err.println("PARENTS: " + parents.getPopulationSize());
 
     }
 
     @Override
     protected boolean finished() {
         //number of generations, best error rate
         
         System.err.println("FINISHED_GEN: " + genNumber + "/" + maxGeneration);
         System.err.println("FINISHED_ERR: " + bestError + "/" + goalError);
         System.err.println("FINISHED_EVA: " + archive.size() + "/" + maxEval);
         
         if (genNumber >= maxGeneration) {
             return true;
         }
 
         if (bestError <= goalError) {
             return true;
         }
         
         if (archive.size() >= maxEval) 
             return true;
         
         return false;
     }
 
     @Override
     protected String getAgentType() {
         return "EASearch";
     }
 
     @Override
     protected void loadSearchOptions() {
         popSize = 10;
         mutProb = 0.2;
         xOverProb = 0.5;
         maxGeneration = 5;
         goalError = 0.02;
         List search_options = getSearch_options();
         // find maximum tries in Options
         Iterator itr = search_options.iterator();
         while (itr.hasNext()) {
             Option next = (Option) itr.next();
             if (next.getName().equals("E")) {
                 goalError = Float.parseFloat(next.getValue());
             }
             if (next.getName().equals("M")) {
                 maxGeneration = Integer.parseInt(next.getValue());
             }
             if (next.getName().equals("T")) {
                 mutProb = Float.parseFloat(next.getValue());
             }
             if (next.getName().equals("X")) {
                 xOverProb = Float.parseFloat(next.getValue());
             }
             if (next.getName().equals("P")) {
                 popSize = Integer.parseInt(next.getValue());
             }
             if (next.getName().equals("I")) {
                 maxEval = Integer.parseInt(next.getValue());
             }
             if (next.getName().equals("F")) {
                 mutProbPerField = Float.parseFloat(next.getValue());
             }
             if (next.getName().equals("L")) {
                 eliteSize = Float.parseFloat(next.getValue());
             }
         }
         query_block_size = popSize;
 
     }
 
 }
