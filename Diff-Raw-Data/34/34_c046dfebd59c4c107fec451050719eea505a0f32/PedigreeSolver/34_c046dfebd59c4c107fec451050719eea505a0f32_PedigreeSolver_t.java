 package edu.umb.jsPedigrees.client.PE;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Vector;
 
 import edu.umb.jsPedigrees.client.Pelican.PelicanPerson;
 
 public class PedigreeSolver {
 
 	private static String[][] RTODS = {
 		{"A A", "A a"},
 		{"B B", "B b"},
 		{"XD XD", "XD Xd"},
 		{"XE XE", "XE Xe"}
 	};
 
 	private static String[] REPLACEMENT_GENOS = {"A _", "B _", "XD X_", "XE X_"};
	private static String[] GENOS_TO_REPLACE = {"A A", "B B", "XD XD", "XE XE"};
 
 	private PelicanPerson[] people;
 	private PelicanPerson[] pedigree;
 	private GenotypeSet[] successfulGenotypeSets; 	//index is model number
 
 	public PedigreeSolver(PelicanPerson[] pedigree, HashSet<String> matingList) {
 		successfulGenotypeSets = new GenotypeSet[4];
 		successfulGenotypeSets[0] = new GenotypeSet();
 		successfulGenotypeSets[1] = new GenotypeSet();
 		successfulGenotypeSets[2] = new GenotypeSet();
 		successfulGenotypeSets[3] = new GenotypeSet();
 
 		this.pedigree = pedigree;
 
 		/*
 		 * make array of individuals by id
 		 *   note that first person is 1
 		 */
 		people = new PelicanPerson[pedigree.length + 1];
 		for (int i = 0; i < pedigree.length; i++) {
 			people[pedigree[i].id] = pedigree[i];
 		}
 	}
 
 	public PedigreeSolution solve() {
 
 		// step thru the four models
 		for (int modelNumber = 0; modelNumber < 4; modelNumber++) {
 			resetGenotypes();
 			tryGenotypes(modelNumber, 1);
 		}
 		return new PedigreeSolution(successfulGenotypeSets);
 	}
 
 	private void tryGenotypes(int modelNumber, int personId) {
 		//	debug
 		//		if (personId >= people.length) {
 		//			System.out.println("Finished checking all people");
 		//		} else {
 		//			System.out.println("Checking person " + personId);
 		//		}
 
 		/*
 		 * first, see if you're done - if you've finished with last person
 		 *   if so, log the complete set and return
 		 */
 		if (personId >= people.length) {
 			logSuccessfulGenotypeSet(modelNumber);
 			return;
 		}
 
 		// if not, recursively set and check genos
 		PelicanPerson currentPerson = people[personId];
 //		System.out.println("\tGetting genos for person " + personId);
 		String[][] possibleGenos = getPossibleGenotypes(modelNumber, currentPerson);
 		for (int i = 0; i < possibleGenos.length; i++) {
 			/*
 			 * need to make new strings, otherwise strange things happen
 			 * because of various pointers
 			 */
 			currentPerson.genotype[0] = new String(possibleGenos[i][0]);
 			currentPerson.genotype[1] = new String(possibleGenos[i][1]);
 //			System.out.println("\tTrying genotype: " + currentPerson.getGenotypeAsString());
 			if (isPossible(currentPerson)) {
 //				System.out.println("\t\tPossible; continuing to next person");
 				tryGenotypes(modelNumber, personId + 1);
 			} else {
 //				System.out.println("\t\tNot possible; trying new geno");
 			}
 		}
 		// all genos tried
 //		System.out.println("\tAll genos tried");
 		currentPerson.genotype[0] = "?";
 		currentPerson.genotype[1] = "?";
 	}
 
 	private void resetGenotypes() {
 		for (int i = 1; i < people.length; i++) {
 			people[i].genotype[0] = "?";
 			people[i].genotype[1] = "?";
 		}
 	}
 
 	/*
 	 * check to see if the person could have come 
 	 * from his/her parents
 	 */
 	private boolean isPossible(PelicanPerson p) {
 		/*
 		 * if person has no parents in pedigree, then any genotype
 		 * (that is consistent with his/her phenotype) is automatically OK
 		 * this also does not require sanity checks as they check the parents
 		 * (and will therefore throw an error if we don't check this first)
 		 */
 		if ((p.mother == null) && (p.father == null)) return true;
 
 
 		// some sanity checks to catch egregious errors that should never happen
 		if ((p.mother == null) && (p.father != null)) {
 			//			JOptionPane.showMessageDialog(null, 
 			//					"Error: Person #" + p.id + " has father but no mother!");
 			return false;
 		}
 
 		if ((p.mother != null) && (p.father == null)) {
 			//			JOptionPane.showMessageDialog(null, 
 			//					"Error: Person #" + p.id + " has mother but no father!");
 			return false;
 		}
 
 		if ((p.mother.genotype[0].equals("? ?")) || (p.father.genotype[0].equals("? ?"))) {
 			//			JOptionPane.showMessageDialog(null, 
 			//					"Error: Person #" + p.id + " parent(s) genotypes have not been set!");
 			return false;
 		}
 
 		// OK to proceed		
 		/*
 		 * if we get here, then person has parents who have been assigned
 		 * genotypes, so we can see if he/she could have come from them
 		 */
 		HashSet<String> possibleGenotypes = new HashSet<String>();
 		possibleGenotypes.add(p.father.genotype[0] + " " + p.mother.genotype[0]);
 		possibleGenotypes.add(p.father.genotype[0] + " " + p.mother.genotype[1]);
 		possibleGenotypes.add(p.father.genotype[1] + " " + p.mother.genotype[0]);
 		possibleGenotypes.add(p.father.genotype[1] + " " + p.mother.genotype[1]);
 		return ((possibleGenotypes.contains(p.genotype[0] + " " + p.genotype[1])) ||
 				(possibleGenotypes.contains(p.genotype[1] + " " + p.genotype[0])));
 	}
 
 
 	private void logSuccessfulGenotypeSet(int modelNumber) {
 		StringBuffer b = new StringBuffer();
 		String[] genos = new String[pedigree.length];
 		for (int i = 0; i < pedigree.length; i++) {
 			genos[i] = people[i + 1].getGenotypeAsString();
 			b.append((i + 1) + ":" + genos[i].toString() + ";");
 		}
 		b.deleteCharAt(b.length() - 1);
 		//		System.out.println(b.toString());
 		successfulGenotypeSets[modelNumber].add(genos);
 	}
 
 
 
 	private String[][] getPossibleGenotypes(int modelNumber, PelicanPerson person) {
 
 		switch (modelNumber) {
 
 		case 0:		// autosomal recessive
 			if (person.affection == PelicanPerson.affected) {	
 				return Genetics.AR_A;
 			} else {						
 				return Genetics.AR_U;				
 			}
 
 		case 1:		// autosomal dominant
 			if (person.affection == PelicanPerson.affected) {	
 				return Genetics.AD_A;				
 			} else {						
 				return Genetics.AD_U;
 			}
 
 		case 2:		// sex-linked recessive
 			if (person.affection == PelicanPerson.affected) {	
 				if (person.sex == PelicanPerson.male) {		
 					return Genetics.SLR_M_A;
 				} else {					
 					return Genetics.SLR_F_A;
 				}
 			} else {
 				if (person.sex == PelicanPerson.male) {		
 					return Genetics.SLR_M_U;
 				} else {					
 					return Genetics.SLR_F_U;
 				}	
 			}
 
 		case 3:		// sex-linked dominant
 			if (person.affection == PelicanPerson.affected) {	
 				if (person.sex == PelicanPerson.male) {		
 					return Genetics.SLD_M_A;
 				} else {					
 					return Genetics.SLD_F_A;
 				}
 			} else {
 				if (person.sex == PelicanPerson.male) {		
 					return Genetics.SLD_M_U;
 				} else {					
 					return Genetics.SLD_F_U;
 				}	
 			}
 		}
 		return null;
 	}
 
 
 	/*
 	 * consolidate: 
 	 *   when you find two solutions that differ at only one place
 	 *   and the diff is "A A" vs "A a" (etc) -> convert it to "A _"
 	 *   (this kind of difference is "the Right Type Of Difference" RTOD)
 	 *   do this repeatedly until no more changes
 	 */
 	public PedigreeSolution consolidate(PedigreeSolution ps) {
 		// make copy for modification
 		GenotypeSet[] consolidatedSets = new GenotypeSet[4];
 		for (int i = 0; i < 4; i++) {
 			consolidatedSets[i] = ps.getResults()[i];
 		}
 
 		// go model-by-model
 		for (int modelNum = 0; modelNum < 4; modelNum++) {
 			// only consolidate if there's something to consolidate
 			ArrayList<String[]> currentSet = consolidatedSets[modelNum].getAll();
 			if (currentSet.size() != 0) {
 
 				// loop over the set members looking for ones to consolidate
 				for (int i = 0; i < currentSet.size(); i++) {
 					for (int j = i; j < currentSet.size(); j++) {
 
 						int numDiffs = 0;
 						int numRTOD = 0;
 						int RTODindex = 0;
 						String[] setI = currentSet.get(i);
 						String[] setJ = currentSet.get(j);
 
 						for (int x = 0; x < setI.length; x++) {
 							if (!setI[x].equals(setJ[x])) {
 								numDiffs++;
 								if (isRTOD(modelNum, setI[x], setJ[x])) {
 									numRTOD++;
 									RTODindex = x;
 								}
 							}
 						}
 						// if you've found a pair that can be consolidated, do so
 						if ((numDiffs == 1) && (numRTOD == 1)) {
 							// remove higher numbered set
 							currentSet.remove(j);
 							String[] workingSet = currentSet.get(i);
 							workingSet[RTODindex] = REPLACEMENT_GENOS[modelNum];
 							currentSet.remove(i);
 							currentSet.add(i, workingSet);
 							consolidatedSets[modelNum].setAll(currentSet);
 
 							// start loops over
 							i = 0;
 							j = 0;
 						}
 					}
 				}
 			}
 		}
		
		/*
		 * because of 8/29/13 problem
		 * replace all AA's with A_'s (and similar for other models)
		 */
		for (int modelNum = 0; modelNum < 4; modelNum++) {
			ArrayList<String[]> currentSet = consolidatedSets[modelNum].getAll();
			if (currentSet.size() != 0) {
				Iterator<String[]> sIt = currentSet.iterator();
				while (sIt.hasNext()) {
					String[] workingSet = sIt.next();
					for (int i = 0; i < workingSet.length; i++) {
						if (workingSet[i].equals(GENOS_TO_REPLACE[modelNum])) {
							workingSet[i] = REPLACEMENT_GENOS[modelNum];
						}
					}
				}
			}
		}
 		return new PedigreeSolution(consolidatedSets);
 	}
 
 	private boolean isRTOD(int modelNum, String memberI, String memberJ) {
 		return ((memberI.equals(RTODS[modelNum][0]) && memberJ.equals(RTODS[modelNum][1])) ||
 				(memberI.equals(RTODS[modelNum][1]) && memberJ.equals(RTODS[modelNum][0])));
 	}
 }
