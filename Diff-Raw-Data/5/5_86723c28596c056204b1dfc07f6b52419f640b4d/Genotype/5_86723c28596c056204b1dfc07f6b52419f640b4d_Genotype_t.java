 package to.richard.tsp;
 
 /**
  * Author: Richard To
  * Date: 2/5/13
  */
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Immutable Genotype.
  *
  * Representation of genes is permutations.
  *
  * Alleles must be distinct. This is not checked by Genotype.
  * Use GenotypeValidator to validate Genotype (length, valid values, distinctness).
  * Fitness is evaluated using a FitnessEvaluator.
  *
  * In hindsight, the fitness should have been added to this class.
  * Current solution seems kind of inefficient.
  */
 public class Genotype implements Iterable<Allele> {
 
     /**
      * Tentatively mark an unset gene position (null allele) to use
      * the an underscore in toString output.
      *
      * Not the best idea. An underscore could potentially be used
      * as an allele value? Doesn't matter for TSP problem, so just
      * going with this for now. Running out of time!
      */
     public static String NULL_ALLELE = "_";
 
     protected Allele[] _genes;
     protected String _genotypeString;
    protected double _fitness;
     /**
      * Protected constructor. Only used to make inheritance work.
      */
     protected Genotype() {}
 
     /**
      * Construct a genotype with allele values for genes.
      */
     public Genotype(Allele[] alleles) {
         _genes =  Arrays.copyOf(alleles, alleles.length);
         _genotypeString = null;
     }
 
     /**
      * Copy constructor. Mainly needed to MutableGenotype copy method.
      * Otherwise kind of pointless.
      */
     public Genotype(Genotype genotype) {
         _genes =  Arrays.copyOf(genotype._genes, genotype._genes.length);
         _genotypeString = null;
     }
 
     /**
      * Helper to build a string representation of genotype. This is basically
      * a sequence of alleles in string format instead of array format.
      */
     protected String buildGenotypeString(Allele[] genes) {
         StringBuilder genotypeStringBuilder = new StringBuilder();
         for (Allele allele : genes) {
             if (allele == null) {
                 genotypeStringBuilder.append(NULL_ALLELE);
             } else {
                 genotypeStringBuilder.append(allele.getValue());
             }
         }
         return genotypeStringBuilder.toString();
     }
 
     /**
      * Gets allele value by gene index.
      * Index starts at 0. Out of bounds exception is thrown if
      * index is out bounds.
      */
     public Allele getAllele(int geneIndex) {
         return _genes[geneIndex];
     }
 
     /**
      * Finds the allele and returns the gene position.
      * If allele is not found. Throw an error.
      */
     public int findAllele(Allele allele) {
         Integer position = null;
         Allele currentAllele = null;
         for (int i = 0; i < _genes.length; i++) {
             currentAllele = _genes[i];
             if (currentAllele != null && currentAllele.equals(allele)) {
                 position = i;
                 break;
             }
         }
 
         if (position == null) {
             throw new Errors.AlleleNotFound();
         }
 
         return position;
     }
 
     /**
      * Gets length of genes.
      */
     public int length() {
         return _genes.length;
     }
 
     /**
      * Gets the gene sequence in string format. Returns a copy of the string.
      */
     @Override
     public String toString() {
 
         if (_genotypeString == null) {
            _genotypeString = buildGenotypeString(_genes);
         }
         return new String(_genotypeString);
     }
 
     @Override
     public int hashCode() {
         return toString().hashCode();
     }
 
     /**
      * Copies genotype and returns mutable version.
      */
     public MutableGenotype copyMutable() {
         return new MutableGenotype(this);
     }
 
     @Override
     public Iterator<Allele> iterator() {
         Iterator<Allele> geneIterator = new Iterator<Allele>() {
 
             private int currentIndex = 0;
 
             @Override
             public boolean hasNext() {
                 return currentIndex < length();
             }
 
             @Override
             public Allele next() {
                 return getAllele(currentIndex++);
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
         return geneIterator;
     }
 }
