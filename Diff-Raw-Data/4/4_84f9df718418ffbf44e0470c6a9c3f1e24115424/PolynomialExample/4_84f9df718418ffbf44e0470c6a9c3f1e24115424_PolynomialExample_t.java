 package org.neurus.samples.symbolicregression;
 
 import org.neurus.evolution.Evolution;
 import org.neurus.evolution.EvolutionBuilder;
 import org.neurus.evolution.Individual;
 import org.neurus.fitness.Fitness;
 import org.neurus.fitness.FitnessFunction;
 import org.neurus.instruction.ConstantRegisters;
 import org.neurus.instruction.Machine;
 import org.neurus.instruction.MachineBuilder;
 import org.neurus.instruction.MathInstructions;
 import org.neurus.instruction.ProgramRunner;
 
 public class PolynomialExample {
 
   private void run() {
     double[][] values = createPolynomialValues();
     Machine machine = new MachineBuilder()
         .withCalculationRegisters(10)
         .withConstantRegisters(new ConstantRegisters(0, 9, 1))
         .withInstruction(MathInstructions.addition())
         .withInstruction(MathInstructions.substraction())
         .withInstruction(MathInstructions.multiplication())
         .withInstruction(MathInstructions.division())
         .build();
     Evolution evolution = new EvolutionBuilder().withMachine(machine)
         .withFitnessFunction(new MseFitnessFunction(values)).build();
     evolution.evolve();
   }
 
   private double[][] createPolynomialValues() {
     double[][] values = new double[21][2];
     double x = -1.0;
     for (int i = 0; i < values.length; i++) {
       values[i][0] = x;
       values[i][1] = polynomial(x);
       x += 0.1;
     }
     return values;
   }
 
   private double polynomial(double x) {
     return 5 * x * x - 9 * x + 3;
   }
 
   public static void main(String[] args) {
     PolynomialExample example = new PolynomialExample();
     example.run();
   }
 }
 
 class MseFitnessFunction implements FitnessFunction {
 
   private double[][] values;
   private double[] inputs;
 
   public MseFitnessFunction(double[][] values) {
     super();
     this.values = values;
     inputs = new double[values[0].length - 1];
   }
 
   @Override
   public Fitness evaluate(ProgramRunner programRunner, Individual individual) {
     programRunner.load(individual.getProgram());
     double error = 0;
     for (int i = 0; i < values.length; i++) {
       for (int j = 0; j < inputs.length; j++) {
         inputs[j] = values[i][j];
       }
       double[] output = programRunner.run(inputs);
       error += Math.pow(output[0] - values[i][inputs.length], 2);
     }
    error = error / values.length;
     return new Fitness(error);
   }
 }
