 package com.akavrt.csp.solver.sequential;
 
 import com.akavrt.csp.core.*;
 import com.akavrt.csp.core.metadata.SolutionMetadata;
 import com.akavrt.csp.solver.Algorithm;
 import com.akavrt.csp.solver.ExecutionContext;
 import com.akavrt.csp.solver.pattern.PatternGenerator;
 import com.akavrt.csp.utils.ParameterSet;
 import com.akavrt.csp.utils.Utils;
 import com.google.common.collect.Lists;
 import org.apache.logging.log4j.Logger;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * <p>Immediate base class for all implemented versions of sequential heuristic procedures.</p>
  */
 public abstract class SequentialProcedure implements Algorithm {
     private final PatternGenerator patternGenerator;
     protected ExecutionContext context;
     protected RollManager rollManager;
     protected OrderManager orderManager;
 
     /**
      * <p>Concrete implementation of the randomized pattern generation procedure must be provided
      * during creation.</p>
      *
      * @param generator Concrete implementation of pattern generator.
      */
     public SequentialProcedure(PatternGenerator generator) {
         this.patternGenerator = generator;
     }
 
     /**
      * <p>Short name of the method used to mark solutions.</p>
      *
      * @return Short name of the method.
      */
     protected abstract String getShortMethodName();
 
     /**
      * <p>Parameters specific to sequential heuristic procedures only.</p>
      *
      * @return Parameter set used by the method.
      */
     protected abstract SequentialProcedureParameters getMethodParameters();
 
     /**
      * <p>Implement main loop where solution is constructed by iteratively generating and adding
      * new patterns to it.</p>
      *
      * @return Solution constructed by the method.
      */
     protected abstract Solution search();
 
     /**
      * <p>Provide an instance of Logger used to print out patterns generated using randomized
      * procedure.</p>
      */
     protected abstract Logger getLogger();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Solution> execute(ExecutionContext context) {
         if (patternGenerator == null || context.getProblem() == null) {
             return null;
         }
 
         this.context = context;
 
         Problem problem = context.getProblem();
         patternGenerator.initialize(problem);
 
         rollManager = new RollManager(problem.getRolls());
         orderManager = new OrderManager(problem.getOrders());
 
         Solution solution = search();
 
         // prepare metadata
         solution.setMetadata(prepareMetadata());
 
         return Lists.newArrayList(solution);
     }
 
     private SolutionMetadata prepareMetadata() {
         SolutionMetadata metadata = new SolutionMetadata();
         metadata.setDescription("Solution obtained with " + getShortMethodName() + ".");
         metadata.setDate(new Date());
         metadata.setParameters(getParameters());
 
         return metadata;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ParameterSet> getParameters() {
         List<ParameterSet> parameters = Lists.newArrayList();
         parameters.add(getMethodParameters());
         parameters.add(patternGenerator.getParameters());
 
         return parameters;
     }
 
     /**
      * <p>Calculate maximum possible pattern usage level based on volume of ordered strip still to
      * be scheduled and characteristics of rolls left in stock.</p>
      *
      * @param allowedTrimRatio Fractional ratio defining maximum trim loss level allowed for
      *                         pattern not to be rejected.
     * @return Maximum possible pattern usage level.
      */
     protected int evaluatePatternUsage(double allowedTrimRatio) {
         // find roll with minimal area
         double minRollArea = rollManager.getMinRollArea();
 
         // calculate total area of ordered strip yet to be produced
         double ordersArea = orderManager.getUnfulfilledOrdersArea();
 
         // let's evaluate maximum possible pattern usage
         // sometimes we need to restrict pattern usage,
         // this is done using corresponding parameter defined as fractional ratio:
         // for ratio = 1.0 schedule all ordered strip,
         // for ratio = 0.5 schedule only one half of the ordered strip, etc.
         double upperBoundRatio = getMethodParameters().getPatternUsageUpperBound();
         int patternUsage = (int) Math.ceil(upperBoundRatio * ordersArea / minRollArea);
 
         // search for a largest group of rolls
         // group clustering is decided based on width
         int maxGroupSize = rollManager.getLargestGroupSize(allowedTrimRatio);
 
         // adjust expectations
         if (patternUsage > maxGroupSize) {
             patternUsage = maxGroupSize;
         }
 
         return patternUsage;
     }
 
     /**
      * <p>Traverse stock and generate pattern using randomized procedure.</p>
      *
      * @param allowedTrimRatio Fractional ratio defining maximum trim loss level allowed for
      *                         pattern not to be rejected.
      * @param patternUsage     Minimal number of rolls to be cutted with pattern.
      * @return Pattern and group of rolls if search was successful or null otherwise.
      */
     protected BuildingBlock rollGroupStep(double allowedTrimRatio, int patternUsage) {
         BuildingBlock block = null;
 
         int anchorIndex = 0;
         while (!context.isCancelled() && anchorIndex < rollManager.size() && block == null) {
             // check whether we can find group of sufficient size
             if (rollManager.getGroupSize(anchorIndex, allowedTrimRatio) >= patternUsage) {
                 // if suitable group exists, try to generate pattern
                 block = patternGeneration(allowedTrimRatio, patternUsage, anchorIndex);
             }
 
             anchorIndex++;
         }
 
         return block;
     }
 
     /**
      * <p>Generate pattern using randomized procedure.</p>
      *
      * @param allowedTrimRatio Fractional ratio defining maximum trim loss level allowed for
      *                         pattern not to be rejected.
      * @param patternUsage     Minimal number of rolls to be cutted with pattern.
      * @param anchorIndex      Index of the roll used as a base roll when searching for a group of
      *                         rolls with suitable characteristics.
      * @return Pattern and group of rolls if search was successful or null otherwise.
      */
     protected BuildingBlock patternGeneration(double allowedTrimRatio,
                                               int patternUsage,
                                               int anchorIndex) {
         BuildingBlock block = null;
 
         // let's process current group and try to generate suitable pattern
         List<Roll> group = rollManager.getGroup(anchorIndex, allowedTrimRatio, patternUsage);
         int[] demand = orderManager.calcGroupDemand(group);
 
         // rolls are sorted in ascending order of width
         // the narrowest roll is always the first roll in the group
         double baseRollWidth = group.get(0).getWidth();
 
         int[] pattern = patternGenerator.generate(baseRollWidth, demand, allowedTrimRatio);
         if (pattern != null
                 && orderManager.getPatternTrimRatio(group, pattern) <= allowedTrimRatio) {
             // pattern search succeeded
             block = new BuildingBlock(pattern, group);
 
             if (getLogger().isDebugEnabled()) {
                 debugPatternGeneration(anchorIndex, demand, block);
             }
         }
 
         return block;
     }
 
     /**
      * <p>Prepare fully-fledged Pattern equivalent of the pattern defined by an array of integer
      * multipliers.</p>
      *
      * @param pattern Pattern defined by an array of integer multipliers.
      * @return Equivalent of the pattern converted to an instance of Pattern.
      */
     protected Pattern prepareSolutionPattern(int[] pattern) {
         Pattern solutionPattern = new Pattern(context.getProblem());
         List<Order> orders = context.getProblem().getOrders();
         for (int i = 0; i < orders.size(); i++) {
             solutionPattern.setCut(orders.get(i), pattern[i]);
         }
 
         return solutionPattern;
     }
 
     private void debugPatternGeneration(int anchorIndex, int[] demand, BuildingBlock block) {
         Roll anchorRoll = block.group.get(0);
         double trimRatio = orderManager.getPatternTrimRatio(block.group, block.pattern);
 
         getLogger().debug("      ANC_IDX: %d -> base roll: '%s', #%d",
                           anchorIndex, anchorRoll.getId(), anchorRoll.getInternalId());
         getLogger().debug(Utils.convertIntArrayToString("       demand", demand));
         getLogger().debug(Utils.convertIntArrayToString("      pattern", block.pattern));
         getLogger().debug("      TL ratio = %.4f", trimRatio);
     }
 
     protected static class BuildingBlock {
         public final int[] pattern;
         public final List<Roll> group;
 
         public BuildingBlock(int[] pattern, List<Roll> group) {
             this.pattern = pattern;
             this.group = group;
         }
     }
 
 }
