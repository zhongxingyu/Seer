 package genlab.algog.gui.examples.examples;
 
 import genlab.algog.algos.meta.DoubleGeneAlgo;
 import genlab.algog.algos.meta.GenomeAlgo;
 import genlab.algog.algos.meta.GoalAlgo;
 import genlab.algog.algos.meta.IntegerGeneAlgo;
 import genlab.algog.algos.meta.NSGA2GeneticExplorationAlgo;
 import genlab.algog.algos.meta.VerificationFunctionsAlgo;
 import genlab.algog.gui.jfreechart.algos.AlgoGPlotAlgo;
 import genlab.core.model.instance.IAlgoContainerInstance;
 import genlab.core.model.instance.IAlgoInstance;
 import genlab.core.model.instance.IGenlabWorkflowInstance;
 import genlab.core.model.meta.AlgoCategory;
 import genlab.core.model.meta.ExistingAlgoCategories;
 import genlab.core.model.meta.IFlowType;
 import genlab.core.model.meta.basics.algos.ConstantValueDouble;
 import genlab.gui.examples.contributors.GenlabExampleDifficulty;
 import genlab.gui.examples.contributors.IGenlabExample;
 import genlab.gui.jfreechart.algos.ScatterPlotAlgo;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 
 public class ExampleTestFunctionChakongHaimes implements IGenlabExample {
 
 	public ExampleTestFunctionChakongHaimes() {
 	}
 
 	@Override
 	public void fillInstance(IGenlabWorkflowInstance workflow) {
 
 		// genetic algo host
 		final NSGA2GeneticExplorationAlgo nsgaAlgo = new NSGA2GeneticExplorationAlgo();
 		final IAlgoContainerInstance nsgaInstance = (IAlgoContainerInstance)nsgaAlgo.createInstance(workflow);
 		workflow.addAlgoInstance(nsgaInstance);
 		
 		// genome
 		final GenomeAlgo genomeAlgo = new GenomeAlgo();
 		final IAlgoInstance genomeInstance = genomeAlgo.createInstance(workflow);
 		genomeInstance.setName("Chakong_Haimes");
 		nsgaInstance.addChildren(genomeInstance);
 		genomeInstance.setContainer(nsgaInstance);
 		workflow.addAlgoInstance(genomeInstance);
 				
 		// genes
 		final DoubleGeneAlgo doubleGeneAlgo = new DoubleGeneAlgo();
 		
 		final IAlgoInstance geneXInstance = doubleGeneAlgo.createInstance(workflow);
 		geneXInstance.setName("x");
 		nsgaInstance.addChildren(geneXInstance);
 		geneXInstance.setContainer(nsgaInstance);
 		workflow.addAlgoInstance(geneXInstance);
 		geneXInstance.setValueForParameter(DoubleGeneAlgo.PARAM_MINIMUM, -20d);
		geneXInstance.setValueForParameter(DoubleGeneAlgo.PARAM_MAXIMUM, 100d);
 		
 		workflow.connect(
 				genomeInstance, 
 				GenomeAlgo.OUTPUT_GENOME,
 				geneXInstance,
 				IntegerGeneAlgo.INPUT_GENOME
 				);
 		
 		final IAlgoInstance geneYInstance = doubleGeneAlgo.createInstance(workflow);
 		geneYInstance.setName("y");
 		nsgaInstance.addChildren(geneYInstance);
 		geneYInstance.setContainer(nsgaInstance);
 		workflow.addAlgoInstance(geneYInstance);
		geneYInstance.setValueForParameter(DoubleGeneAlgo.PARAM_MINIMUM, -100d);
 		geneYInstance.setValueForParameter(DoubleGeneAlgo.PARAM_MAXIMUM, 20d);
 		
 		workflow.connect(
 				genomeInstance, 
 				GenomeAlgo.OUTPUT_GENOME,
 				geneYInstance,
 				IntegerGeneAlgo.INPUT_GENOME
 				);
 		
 		// test function
 		final VerificationFunctionsAlgo verificationFunctionAlgo = new VerificationFunctionsAlgo();
 		final IAlgoInstance verificationFunctionInstance = verificationFunctionAlgo.createInstance(workflow);
 
 		verificationFunctionInstance.setName("verification function");
 		verificationFunctionInstance.setContainer(nsgaInstance);
 		verificationFunctionInstance.setValueForParameter(
 				VerificationFunctionsAlgo.PARAM_FUNCTION, 
 				VerificationFunctionsAlgo.EAvailableFunctions.CHAKONG_HAIMES.ordinal()
 				);
 		
 		nsgaInstance.addChildren(verificationFunctionInstance);
 		workflow.addAlgoInstance(verificationFunctionInstance);
 		
 		workflow.connect(
 				geneXInstance, 
 				DoubleGeneAlgo.OUTPUT_VALUE, 
 				verificationFunctionInstance, 
 				VerificationFunctionsAlgo.INPUT_X
 				);
 		workflow.connect(
 				geneYInstance, 
 				DoubleGeneAlgo.OUTPUT_VALUE, 
 				verificationFunctionInstance, 
 				VerificationFunctionsAlgo.INPUT_Y
 				);
 		
 		final GoalAlgo goalAlgo = new GoalAlgo();
 		final ConstantValueDouble constantDoubleAlgo = new ConstantValueDouble();
 
 
 		final IAlgoInstance constantDoubleZero = constantDoubleAlgo.createInstance(workflow);
 		constantDoubleZero.setContainer(nsgaInstance);
 		nsgaInstance.addChildren(constantDoubleZero);
 		workflow.addAlgoInstance(constantDoubleZero);
 		constantDoubleZero.setValueForParameter(constantDoubleAlgo.getConstantParameter(), -10000.0);
 
 		// set goal 1: f1
 		{
 			final IAlgoInstance goalF1 = goalAlgo.createInstance(workflow);
 			goalF1.setName("f1");
 			goalF1.setContainer(nsgaInstance);
 			nsgaInstance.addChildren(goalF1);
 			workflow.addAlgoInstance(goalF1);
 
 			workflow.connect(
 					verificationFunctionInstance, VerificationFunctionsAlgo.OUTPUT_F1,
 					goalF1, GoalAlgo.INPUT_VALUE
 					);
 			
 			workflow.connect(
 					constantDoubleZero, ConstantValueDouble.OUTPUT,
 					goalF1, GoalAlgo.INPUT_TARGET
 					);
 		}
 		
 		// set goal density
 		{
 			final IAlgoInstance goalF2 = goalAlgo.createInstance(workflow);
 			goalF2.setName("f2");
 			goalF2.setContainer(nsgaInstance);
 			nsgaInstance.addChildren(goalF2);
 			workflow.addAlgoInstance(goalF2);
 			
 			workflow.connect(
 					verificationFunctionInstance, VerificationFunctionsAlgo.OUTPUT_F2,
 					goalF2, GoalAlgo.INPUT_VALUE
 					);
 			
 			workflow.connect(
 					constantDoubleZero, ConstantValueDouble.OUTPUT,
 					goalF2, GoalAlgo.INPUT_TARGET
 					);
 		}
 		
 		// add displays
 		{
 			final AlgoGPlotAlgo algogPlotAlgo = new AlgoGPlotAlgo();
 			final IAlgoInstance algogPlotInstance = algogPlotAlgo.createInstance(workflow);
 			workflow.addAlgoInstance(algogPlotInstance);
 			
 
 			algogPlotInstance.setName("exploration of Pareto");
 			
 			workflow.connect(
 					nsgaInstance, 
 					NSGA2GeneticExplorationAlgo.OUTPUT_TABLE_PARETO, 
 					algogPlotInstance,
 					AlgoGPlotAlgo.INPUT_TABLE
 					);
 		}
 		
 		{
 			final ScatterPlotAlgo scatterPlotAlgo = new ScatterPlotAlgo();
 			final IAlgoInstance algoScatterPlotInstance = scatterPlotAlgo.createInstance(workflow);
 			workflow.addAlgoInstance(algoScatterPlotInstance);
 			
 			algoScatterPlotInstance.setName("Pareto fronts");
 			algoScatterPlotInstance.setValueForParameter(ScatterPlotAlgo.PARAM_COLUMN_X, 6);
 			algoScatterPlotInstance.setValueForParameter(ScatterPlotAlgo.PARAM_COLUMN_Y, 3);
 			workflow.connect(
 					nsgaInstance, 
 					NSGA2GeneticExplorationAlgo.OUTPUT_TABLE_PARETO, 
 					algoScatterPlotInstance,
 					ScatterPlotAlgo.INPUT_TABLE
 					);
 		}
 		
 		
 	}
 
 	@Override
 	public String getFileName() {
 		return "geneticalgo_multigoal_testfunction_chakong_haimes";
 	}
 
 	@Override
 	public String getName() {
 		return "test function Chakong Haimes for multi-objective genetic algorithms";
 	}
 
 	@Override
 	public String getDescription() {
 		return "applies NSGA2 on the Chakong Haimes test function with a display of the Pareto front; enables the checking of the NSGA 2 behaviour";
 	}
 
 	@Override
 	public void createFiles(File resourcesDirectory) {
 	
 	}
 
 	@Override
 	public GenlabExampleDifficulty getDifficulty() {
 		return GenlabExampleDifficulty.MEDIUM;
 	}
 
 	@Override
 	public Collection<IFlowType<?>> getIllustratedFlowTypes() {
 		return Collections.EMPTY_LIST;
 	}
 
 	@Override
 	public Collection<AlgoCategory> getIllustratedAlgoCategories() {
 		return new LinkedList<AlgoCategory>() {{ add(ExistingAlgoCategories.EXPLORATION_GENETIC_ALGOS); }};
 	}
 
 }
