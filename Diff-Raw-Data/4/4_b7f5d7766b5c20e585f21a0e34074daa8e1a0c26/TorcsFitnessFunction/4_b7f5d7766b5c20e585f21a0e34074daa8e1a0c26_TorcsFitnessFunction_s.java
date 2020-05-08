 package fuzzyClient;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import net.sourceforge.jFuzzyLogic.FIS;
 import net.sourceforge.jFuzzyLogic.FunctionBlock;
 import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravity;
 import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravitySingletons;
 import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
 import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
 import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionSingleton;
 import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTrapetzoidal;
 import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
 import net.sourceforge.jFuzzyLogic.membership.Value;
 import net.sourceforge.jFuzzyLogic.plot.JDialogFis;
 import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
 import net.sourceforge.jFuzzyLogic.rule.Rule;
 import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
 import net.sourceforge.jFuzzyLogic.rule.RuleExpression;
 import net.sourceforge.jFuzzyLogic.rule.RuleTerm;
 import net.sourceforge.jFuzzyLogic.rule.Variable;
 import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod;
 import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethodMax;
 import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethodSum;
 import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethodMin;
 import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodAndMin;
 import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodOrMax;
 
 import org.jgap.*;
 import org.jgap.impl.DoubleGene;
 
 
 public class TorcsFitnessFunction extends FitnessFunction implements ChromosomeDefinition{
 	
 	private final static String HUMAN_DATA_FILE = "humanPlayerData.csv";
 	private static LinkedList<Double[]> humanData = null;
 	
 	private boolean accel = false;
 	
 	public TorcsFitnessFunction(boolean accel) {
 		this.accel = accel;
 	}
 	
 	@Override
 	protected double evaluate(IChromosome a_subject) {
 
 		// Reconstruct a fcl from the chromosome
 		FIS fis = constructFCL(a_subject);
 
 		// Calculate the fitness of this solution
 		// 1.0 is the worst fitness and then greater is better!
 		return calculateFitness(fis);
 	}
 
 	// Decode the chromosome in order to construct a FCL
 	public static FIS constructFCL(IChromosome a_potentialSolution) {
 		
 		Gene[] genes = a_potentialSolution.getGenes();
 		
 		// Create the FCL
 		FIS fis = new FIS();
 		
 		// FUNCTION_BLOCK fuzzyDriver
 		FunctionBlock functionBlock = new FunctionBlock(fis);
 		fis.addFunctionBlock("EvoDriver", functionBlock);
 
 		//		VAR_INPUT              
 		//		   input0  : REAL;
 		//		   input1  : REAL
 		//         input2  : REAL
 		//         input3  : REAL
 		//		   ...
 		//         input20 : REAL
 		//		END_VAR
 
 		Variable[] inputs = new Variable[NB_INPUT];
 		for(int i = 0; i < NB_INPUT; i++){
 			inputs[i] = new Variable("input" + i);
 			functionBlock.setVariable(inputs[i].getName(), inputs[i]);
 		}
 		
 		//		VAR_OUTPUT
 		//		   output0 : REAL;
 		//		   output1 : REAL;
 		//		   ...
 		//		   outputn : REAL;
 		//		END_VAR
 
 		Variable[] outputs = new Variable[NB_OUTPUT];
 		for(int i = 0; i < NB_OUTPUT; i++){
 			outputs[i] = new Variable("output" + i);
 			functionBlock.setVariable(outputs[i].getName(), outputs[i]);
 		}
 		
 		// Decode the chromosome for the input
 		MembershipFunction memFunc;
 		for(int i = 0;i < NB_INPUT;i++){
 			//		FUZZIFY inputi
 			//		   TERM in_i_0 := (INPUT_MIN, 0) (INPUT_MIN, 1) (j, 1) (j+1, 0) ;
 			//		   TERM in_i_1 := (j-1, 0) (j,1) (j+1,0);
 			//		   TERM in_i_2 := (j-1, 0) (j, 1) (INPUT_MAX, 1) (INPUT_MAX, 0);
 			//		END_FUZZIFY
 			
 			// Sort the values
 			double[] sortedVal = new double[NB_FA_IN]; 
 			for(int j = 0;j < NB_FA_IN;j++)
 				sortedVal[j] = (Double)genes[j+i*NB_FA_IN+NB_DEFAULT].getAllele();
 			Arrays.sort(sortedVal);
 			
 			for(int j = 0;j < NB_FA_IN;j++){
 				
 				// Define the membership function
 				if(j==0)
 					memFunc = new MembershipFunctionTrapetzoidal(
 									new Value(INPUT_MIN), 
 									new Value(INPUT_MIN), 
 									new Value(sortedVal[j]), 
 									new Value(sortedVal[j+1]));
 				else if(j == NB_FA_IN - 1)
 					memFunc = new MembershipFunctionTrapetzoidal(
 									new Value(sortedVal[j-1]), 
 									new Value(sortedVal[j]),
 									new Value(INPUT_MAX), 
 									new Value(INPUT_MAX));
 				else
 					memFunc = new MembershipFunctionTriangular(
 									new Value(sortedVal[j-1]),
 									new Value(sortedVal[j]),
 									new Value(sortedVal[j+1]));
 				
 				// Add a label and add it to the input variable
 				inputs[i].add("in_"+(j+i*NB_FA_IN), memFunc);
 			}
 		}
 		
 		// Decode the chromosome for the output
     	//      DEFUZZIFY outputi
 		//		   TERM out_i_0 := (OUTPUT_MIN, 0) (OUTPUT_MIN, 1) (j, 1) (j+1, 0) ;
 		//		   TERM out_i_1 := (j-1, 0) (j,1) (j+1,0);
 		//		   TERM out_i_2 := (j-1, 0) (j, 1) (OUTPUT_MAX, 1) (OUTPUT_MAX, 0);
 		//		   METHOD : COG;
 		//		   DEFAULT := defaultGene;
 		//		END_DEFUZZIFY
         for(int i = 0; i < NB_OUTPUT; i++){
               	
         	for(int j = 0; j < NB_FA_OUT; j++){
         		// Define the membership function
 				memFunc = new MembershipFunctionSingleton(
 								new Value((Double)genes[j+i*NB_FA_OUT+NB_INPUT*NB_FA_IN+NB_DEFAULT].getAllele()));
 				
 				// Add a label and add it to the output variable
 				outputs[i].add("out_"+(j+i*NB_FA_OUT), memFunc);	
         	}
 
         	// Set the default value
         	outputs[i].setDefaultValue((Double)genes[i].getAllele());
         	
         	// Set the gravity center for singletons
         	outputs[i].setDefuzzifier(new DefuzzifierCenterOfGravitySingletons(outputs[i]));
         }
 		
 		
 		//		RULEBLOCK No1
 		//		   ACCU : MAX;
 		//		   AND  : MIN;
 		//		   ACT  : MIN;
 		RuleBlock ruleBlock = new RuleBlock(functionBlock);
 		ruleBlock.setName("No1");
 		ruleBlock.setRuleAccumulationMethod(new RuleAccumulationMethodMax());
 		ruleBlock.setRuleActivationMethod(new RuleActivationMethodMin());
 		
 		// Decode the chromosome to add the rules to the FCL
 		Rule rule;
 		int pos;
 		for(int i = 0; i < NB_REGLE; i++){
 		//		   RULE 1 : IF cond1 IS x AND cond2 is y AND cond3 is z THEN output IS c;
 			rule = new Rule("Rule"+i, ruleBlock);
 			
 			// Create the terms of the expression
 			RuleExpression expression = new RuleExpression();
 			for(int j = 0; j < NB_R_IN; j++){
 				pos = (Integer)genes[j+i*NB_R_IN+NB_OUTPUT*NB_FA_OUT+NB_INPUT*NB_FA_IN+NB_DEFAULT].getAllele();
 				try{
 					expression.add(new RuleTerm(inputs[pos / NB_FA_IN], "in_" + pos, false));
 				}
 				catch(RuntimeException e){
 					expression = new RuleExpression(expression, new RuleTerm(inputs[pos / NB_FA_IN], "in_" + pos, false), new RuleConnectionMethodAndMin());
 				}
 			}
 			rule.setAntecedents(expression);
 			
 			// Add the consequent
 			for(int j = 0; j < NB_R_OUT; j++){
 				pos = (Integer)genes[j+NB_R_IN+i*(NB_R_IN + NB_R_OUT)+NB_OUTPUT*NB_FA_OUT+NB_INPUT*NB_FA_IN+NB_DEFAULT].getAllele();
 				rule.addConsequent(outputs[pos / NB_FA_OUT], "out_" + pos, false);
 			}
 			
 			ruleBlock.add(rule);
 		}
 		
 		//		END_RULEBLOCK
 		//
 		//		END_FUNCTION_BLOCK
 		HashMap<String, RuleBlock> ruleBlocksMap = new HashMap<String, RuleBlock>();
 		ruleBlocksMap.put(ruleBlock.getName(), ruleBlock);
 		functionBlock.setRuleBlocks(ruleBlocksMap);
 		
 		//---
 		// Show generated FIS (FCL) and show animation
 		//---
 		//System.out.println(fis);
 		
 		return fis;
 	}
 	
 	//The fitness function.
 	//In this example case we want obtain the int array [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
 	private double calculateFitness(FIS fis) {
 		
 		// The higher the score is, the best the sample is
 		double error = 1000000.;
 		
 		// The human data must be loaded if that's the first time
 		if(humanData == null)
 			humanData = loadHumanPlayerData(HUMAN_DATA_FILE);
 		
 		for(Double[] data : humanData){
 			// The two last column are the result
 			for(int i=0;i < NB_INPUT; i++)
 				fis.setVariable("input" + i, data[i]);
 			
 			// Evaluate the system
 			fis.evaluate();
 			
 			// Calcul the error
 			for(int i=0;i < NB_OUTPUT; i++)
 				if(accel && i == 0)
 					error -= Math.abs(fis.getVariable("output" + i).getValue() - data[NB_INPUT + i + 1]);
 				else
 					error -= Math.abs(fis.getVariable("output" + i).getValue() - data[NB_INPUT + i]);
 		}
 
 		return error;
 	}
 	
 	private LinkedList<Double[]> loadHumanPlayerData(String filename){
 		LinkedList<Double[]> result = new LinkedList<Double[]>();
 		try{
 			InputStream is = new FileInputStream(filename); 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			
 			String ligne;
 			// Ignore the first line with the headers
 			br.readLine();
 			
 			// Read the rest
 			while ((ligne=br.readLine()) != null){
 				result.add(processLine(ligne.split(";")));
 			}
 			br.close(); 
 		}		
 		catch (Exception e){
 			System.out.println(e.toString());
 		}
 		
 		return result;
 	}
 
 	private Double[] processLine(String[] split) {
 		// Ignore the first and the last column
		Double[] result = new Double[split.length - 2];
		for(int i = 1;i < split.length - 1; i++)
 			result[i-1] = Double.parseDouble(split[i]);
 		return result;
 	}
 }
