 import org.jgap.FitnessFunction;
 import org.jgap.IChromosome;
 
 public class ScoreFitnessFunction extends FitnessFunction
 {
     @Override
     public double evaluate(IChromosome chromosome)
     {
         Experimentalist experimentalist = new Experimentalist();
        Options chromosomeOptions = new Options(chromosome);
        System.out.println("Now evaluating fitness of chromosome:");
        chromosomeOptions.print();
        return experimentalist.runNormalTrials(chromosomeOptions);
     }
 }
