 package model;
 
 import java.util.List;
 
 /**
  * Utility class for getting statistics on multiple Genes.
  * 
  * @author Mitchell Rosen
  * @version 19-Apr-2013
  */
 public class GeneUtils {
    public static double avgGeneSize(List<Gene> genes) {
       int size = 0;
       for (Gene gene : genes)
          size += gene.size();
       return (double) size / genes.size();
    }
    
    public static double avgCdsSize(List<Gene> genes) {
       int size = 0;
       for (Gene gene : genes)
          size += gene.cdsSize();
       return (double) size / genes.size();
    }
    
    public static double avgExonSize(List<Gene> genes) {
       int size = 0, numExons = 0;
       for (Gene gene : genes) {
          size += gene.exonSize();
          numExons += gene.numExons();
       }
       return (double) size / numExons;
    }
    
    public static double avgIntronSize(List<Gene> genes) {
       int size = 0, numIntrons = 0;
       for (Gene gene : genes) {
          size += gene.intronSize();
          numIntrons += gene.numIntrons();
       }
       return (double) size / numIntrons;
    }
    
    public static double avgIntergenicRegionSize(List<Gene> genes) {
       int size = 0;
       for (int i = 0; i < genes.size()-1; ++i)
          size += genes.get(i+1).getStart() - genes.get(i).getStop();
      return size;
    }
    
    /**
     * @requires genes.size > 0
     * @requires gene.sequence is the same for all in |genes|
     */
    public static double cdsDensity(List<Gene> genes) {
       return avgCdsSize(genes) / genes.get(0).getSequence().size();
    }
    
    /**
     * @requires genes.size > 0
     * @requires gene.sequence is the same for all in |genes|
     */
    public static double genesPerKilobase(List<Gene> genes) {
       int numNucleotides = genes.get(0).getSequence().size();
       return (double) 10000 * genes.size() / numNucleotides;
    }
   
    /**
     * @requires genes.size > 0
     * @requires gene.sequence is the same for all in |genes|
     */
    public static double kilobasesPerGene(List<Gene> genes) {
       return 1 / genesPerKilobase(genes);
    }
 }
