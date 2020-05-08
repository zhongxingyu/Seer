 package core.copop;
 
 import core.ExecutionContextFactory;
 import core.ga.DefaultEvaluator;
 import core.ga.Rule;
 import core.ga.RulePrinter;
 import core.ga.ops.ec.FitnessEval;
 import core.ga.ops.ec.FitnessEvaluatorFactory;
 import core.ga.RulePopulation;
import core.ga.ops.ec.ExecutionEnv;
 import org.junit.Test;
 
 /**
  *
  * @author gmatoga
  */
 public class PittsIndividualTest {
 
    private ExecutionEnv ec;
     RulePopulation rp;
     PittsIndividual pi;
 
     public PittsIndividualTest() {
         FitnessEval fit = FitnessEvaluatorFactory.EVAL_FMEASURE;
         ec = ExecutionContextFactory.MONK(3, true, 100, fit);
         rp = new RulePopulation(ec);
         rp.decode();
         rp.repair();
         rp.evaluate();
         pi = new PittsIndividual(ec, rp);
     }
 
     @Test
     public void testEvaluate() {
         System.out.println("evaluate");
         final DefaultEvaluator eval = new DefaultEvaluator();
         pi.evaluate(ec.data(), eval);
         System.out.println(pi.getCm());
     }
 
     @Test
     public void testGetRS() {
         System.out.println("getRS");
         RuleSet result = pi.getRS();
         final RulePrinter printer = ec.getBundle().getPrinter();
         for (Rule rule : result.rules) {
             System.out.println(printer.prettyPrint(rule));
         }
         System.out.println("ELSE Class=" + result.defaultClass);
         System.out.println(" ");
         System.out.println();
     }
 
     @Test
     public void testAddOrRemoveWith() {
         System.out.println("mutateAt");
         System.out.println(pi);
         for (int i = 0; i < 100; i++) {
             pi.addOrRemoveWith(0.5);
             System.out.println(pi);
         }
     }
 }
