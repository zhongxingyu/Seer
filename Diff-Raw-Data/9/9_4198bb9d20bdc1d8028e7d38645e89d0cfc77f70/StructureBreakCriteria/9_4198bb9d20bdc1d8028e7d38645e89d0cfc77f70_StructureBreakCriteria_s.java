 package geneticalgorithm.breakcriteria;
 
 import geneticalgorithm.Chromosome;
 import geneticalgorithm.Configuration;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Decide terminar cuando una parte relevante de la poblacin no cambia de
  * generacin en generacin.
  */
 public class StructureBreakCriteria extends BreakCriteria {
 
 	private static final float p = 0.6f;
 	private Set<Chromosome> diff = new HashSet<Chromosome>();
 	
 	public StructureBreakCriteria(Configuration config) {
 		super(config);
 	}
 	
 	@Override
 	public boolean isFinished() {
 		int repeated = countRepeatedCandidates(config);
 		float equalsPer = repeated / (float) config.population.length;
 		return equalsPer >= p;
 	}
 
 	private int countRepeatedCandidates(Configuration config) {
 		diff.clear();
 		Chromosome[] population = config.population;
 		for (Chromosome c: population) {
 			diff.add(c);
 		}
		return diff.size();
 	}
 
 }
