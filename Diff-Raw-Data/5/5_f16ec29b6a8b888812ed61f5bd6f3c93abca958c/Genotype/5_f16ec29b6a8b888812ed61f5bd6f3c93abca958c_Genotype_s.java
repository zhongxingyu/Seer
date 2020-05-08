 package genotype;
 
 import java.io.Serializable;
 import java.util.Vector;
 
 
 // Important!  constructor of Genotype is PRIVATE
 //   USE static methods for getting Genotype or Id of Genotype
 public class Genotype implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 //==============================| Class Genotype: |===
 	private Genome[] genomes;
 	private boolean[] clonalities;
 	
 	private Genotype(Genome[] genomes, boolean[] clonalities){
 		this.genomes = genomes;
 		this.clonalities = clonalities;
 	}
 	
 	// only for 2 Genomes in Genotype!!!
 	public byte getGender(){
 		if(genomes[0].gender == Genome.X  &&  genomes[1].gender == Genome.X)
 			return Genome.X;
 		if(genomes[0].gender == Genome.X  &&  genomes[1].gender == Genome.Y
 		|| genomes[0].gender == Genome.Y  &&  genomes[1].gender == Genome.X)
 			return Genome.Y;
 		return Genome.UNDEF;
 	}
 	
 	public Genome[] getGenomes() {
 		return genomes;
 	}
 
 	public boolean[] getClonalities() {
 		return clonalities;
 	}
 	
 	public String toString() {
 		// TODO
 		return null;
 	}
 	
 	
 	
 //==============================| GLOBAL: |===========
 
 	static private Vector<Genotype> genotypes = new Vector<Genotype>();
 	final static public byte UNDEF = -1;
 	
 	/* It serves as a constructor
 	 *
 	 */
 	static public Genotype getGenotype(Genome[] genomes, boolean[] clonalities){
 		boolean mustContinue;
 		for (int i=0; i<genotypes.size(); i++){
 			mustContinue=false;
 			if (genomes.length != genotypes.get(i).genomes.length)	continue;
 			for (int j=0; j<clonalities.length; j++)
				if (clonalities[j] != genotypes.get(j).clonalities[j]){
 					mustContinue = true;
 					break;
 				}
 			if (mustContinue)	continue;
 			for (int j=0; j<genomes.length; j++)
				if (!genomes[i].equals(genotypes.get(j).genomes[i])){
 					mustContinue = true;
 					break;
 				}
 			if (mustContinue)	continue;
 			return genotypes.get(i);
 		}
 		Genotype genotype = new Genotype(genomes, clonalities);
 		genotypes.add(genotype);
 		return genotype;
 	}
 	
 	/* It serves as a constructor by String as "xRyL", or "xRxR", or "(yL)xL" ...
 	 *
 	 */
 	static public Genotype getGenotype(String str){
 		int shift = 0;
 		Genome[] genomes = new Genome[2];
 		boolean[] clonalities = new boolean[2];
 		if (str.charAt(0)=='('){
 			if (str.charAt(3)!=')')
 				return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 			clonalities[0] = true;
 			if ((genomes[0] = parseGenome(str.charAt(1), str.charAt(2))) == null)
 				return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 			shift = 2;
 		}
 		else{
 			if ((genomes[0] = parseGenome(str.charAt(0), str.charAt(1))) == null)
 				return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 		}
 		if (str.charAt(2+shift)=='('){
 			if (str.charAt(5+shift)!=')')
 				return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 			clonalities[1] = true;
 			if ((genomes[1] = parseGenome(str.charAt(3+shift), str.charAt(4+shift))) == null)
 				return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 		}
 		else{
 			if ((genomes[1] = parseGenome(str.charAt(2+shift), str.charAt(3+shift))) == null)
 				return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 		}
 		return getGenotype(genomes, clonalities);
 	}
 	
 		// only for 2 Genomes in Genotype!!!
 		private static Genome parseGenome(char gender, char name){
 			byte genderByte;
 			Genome.GenomeName nameByte;
 			if (gender == 'x' || gender == 'X')
 				genderByte = Genome.X;
 			else if (gender == 'y' || gender == 'Y')
 				genderByte = Genome.Y;
 			else return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 			if (name == 'l' || name == 'L')
 				nameByte = Genome.GenomeName.L;
 			else if (name == 'r' || name == 'R')
 				nameByte = Genome.GenomeName.R;
 			else return null;						// "return null;" is bad, throws new ...Exception(..) will be better
 			return new Genome(genderByte, nameByte);
 		}
 	
 	/* It serves as a constructor
 	 * but return ID
 	 */
 	static public int getGenotypeId(Genome[] genomes, boolean[] clonalities){
 		boolean mustContinue;
 		for (int i=0; i<genotypes.size(); i++){
 			mustContinue=false;
 			if (genomes.length != genotypes.get(i).genomes.length)	continue;
 			for (int j=0; j<clonalities.length; j++)
 				if (clonalities[j] != genotypes.get(j).clonalities[j]){
 					mustContinue = true;
 					break;
 				}
 			if (mustContinue)	continue;
 			for (int j=0; j<genomes.length; j++)
 				if (genomes[i].equals(genotypes.get(j).genomes[i])){
 					mustContinue = true;
 					break;
 				}
 			if (!mustContinue)	return i;
 		}
 		Genotype genotype = new Genotype(genomes, clonalities);
 		genotypes.add(genotype);
 		return genotypes.size()-1;
 	}
 	
 	/* It serves as a constructor by String as "xRyL", or "xRxR", or "(yL)xL" ...
 	 * but return ID
 	 */
 	static public int getGenotypeId(String str){
 		int shift = 0;
 		Genome[] genomes = new Genome[2];
 		boolean[] clonalities = new boolean[2];
 		if (str.charAt(0)=='('){
 			if (str.charAt(3)!=')')
 				return UNDEF;						// "return UNDEF;" is bad, throws new ...Exception(..) will be better
 			clonalities[0] = true;
 			if ((genomes[0] = parseGenome(str.charAt(1), str.charAt(2))) == null)
 				return UNDEF;						// "return UNDEF;" is bad, throws new ...Exception(..) will be better
 			shift = 2;
 		}
 		if (str.charAt(2+shift)=='('){
 			if (str.charAt(5+shift)!=')')
 				return UNDEF;						// "return UNDEF;" is bad, throws new ...Exception(..) will be better
 			clonalities[1] = true;
 			if ((genomes[1] = parseGenome(str.charAt(3+shift), str.charAt(4+shift))) == null)
 				return UNDEF;						// "return UNDEF;" is bad, throws new ...Exception(..) will be better
 		}
 		return getGenotypeId(genomes, clonalities);
 	}
 	
 	// get existed genotype
 	static public Genotype getGenotypeById(int id){
 		return genotypes.get(id);
 	}
 	
 	// get existed ID
 	static public int getIdOf(Genotype genotype){
 		return genotypes.indexOf(genotype);
 	}
 }
