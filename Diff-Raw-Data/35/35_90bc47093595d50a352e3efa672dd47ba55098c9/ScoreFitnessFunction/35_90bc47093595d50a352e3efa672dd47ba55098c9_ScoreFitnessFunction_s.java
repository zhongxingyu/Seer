 import org.jgap.FitnessFunction;
 import org.jgap.IChromosome;
 
 public class ScoreFitnessFunction extends FitnessFunction
 {
     @Override
     public double evaluate(IChromosome chromosome)
     {
         Experimentalist experimentalist = new Experimentalist();
        return experimentalist.runNormalTrials(new Options(chromosome));
     }
 }
