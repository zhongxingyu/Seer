 package edu.umb.jsPedigrees.client.PE;
 
 import java.util.Iterator;
 
 public class PedigreeSolution {
 	
 	private GenotypeSet[] result;
 	private static String[] MODEL_NAMES = {"Autosomal Recessive", 
 			"Autosomal Dominant",
 			"Sex-linked Recessive",
 			"Sex-linked Dominant"};
 	
 	public PedigreeSolution(GenotypeSet[] result) {
 		this.result = result;
 	}
 
 	public GenotypeSet[] getResults() {
 		return result;
 	}
 	
 	public String toString() {
 		StringBuffer b = new StringBuffer();
 		for (int i = 0; i < 4; i++) {
 			b.append(MODEL_NAMES[i] + "\n");
 			if (result[i].getAll().size() == 0) {
 				b.append("\tNot possible\n");
 			} else {
 				b.append("\tPossible; geno sets are:\n");
 				Iterator<String[]> it = result[i].getAll().iterator();
 				while (it.hasNext()) {
 					String[] s = it.next();
 					b.append("\t\t");
 					for (int x = 0; x < s.length; x++) {
						b.append((x + 1) + ":" + s[x] + "   ");
 					}
 					b.append("\n");
 				}
 			}
 		}
 		return b.toString();
 	}
 
 	
 }
