 /* 
  * Copyright (C) 2010-2011  David Gloriam <davidgloriam@googlemail.com> & Patrik Rydberg <patrik.rydberg@gmail.com>
  * 
  * Contact: smartcyp@farma.ku.dk
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package smartcyp;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.openscience.cdk.Atom;
 import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Molecule;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.graph.PathTools;
 import org.openscience.cdk.graph.invariant.EquivalentClassPartitioner;
 import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
 
public class MoleculeKU extends Molecule{
 
	
 	public enum SMARTCYP_PROPERTY {
 		SymmetryNumber,
 		IsSymmetric,
 		NrofSymmetricSites,
 		Score {
 			@Override
 			public String getLabel() {
 				return "S";
 			}
 		},
 		Score2D6 {
 			@Override
 			public String getLabel() {
 				return "S2D6";
 			}
 		},
 		Score2C9 {
 			@Override
 			public String getLabel() {
 				return "S2C9";
 			}
 		},
 		Ranking {
 			@Override
 			public String getLabel() {
 				return "R";
 			}
 		},
 		Ranking2D6 {
 			@Override
 			public String getLabel() {
 				return "R";
 			}
 		},
 		Ranking2C9 {
 			@Override
 			public String getLabel() {
 				return "R";
 			}
 		},
 		Energy {
 			@Override
 			public String getLabel() {
 				return "E";
 			}
 		},
 		Accessibility {
 			@Override
 			public String getLabel() {
 				return "A";
 			}
 		},
 		Span2End {
 			@Override
 			public String getLabel() {
 				return "S";
 			}
 		},
 		Dist2CarboxylicAcid {
 			@Override
 			public String getLabel() {
 				return "DC";
 			}
 		},
 		Dist2ProtAmine {
 			@Override
 			public String getLabel() {
 				return "DP";
 			}
 		};
 
 		public String  getLabel()  { return "";};
 
 		public void set(IAtom atom, Number value) {
 			atom.setProperty(toString(), value);
 		}
 
 		public Number get(IAtom atom) {
 			Object o = atom.getProperty(toString());
 			return (o==null)?null:(Number)o;
 		}
 
 		public String atomProperty2String(IAtom atom) {
 			return String.format("%s:%s",getLabel(),get(atom));
 		}
 
 	}
 	// Local variables
 	private static final long serialVersionUID = 1L;	
 	AtomComparator atomComparator = new AtomComparator();
 	private TreeSet<Atom> atomsSortedByEnA = new TreeSet<Atom>(atomComparator);
 	AtomComparator2D6 atomComparator2D6 = new AtomComparator2D6();
 	private TreeSet<Atom> atomsSortedByEnA2D6 = new TreeSet<Atom>(atomComparator2D6);
 	AtomComparator2C9 atomComparator2C9 = new AtomComparator2C9();
 	private TreeSet<Atom> atomsSortedByEnA2C9 = new TreeSet<Atom>(atomComparator2C9);
 	private int HighestSymmetryNumber = 0;
 
 
 	// Constructor
 	// This constructor also calls the methods that calculate MaxTopDist, Energies and sorts C, N, P and S atoms
 	// This constructor is the only way to create MoleculeKU and Atom objects, -there is no add() method
 	public MoleculeKU(IAtomContainer iAtomContainer, HashMap<String, Double> SMARTSnEnergiesTable) throws CloneNotSupportedException
 	{
 		// Calls the constructor in org.openscience.cdk.AtomContainer
 		// Atoms are stored in the array atoms[] and accessed by getAtom() and setAtom()
 		super(iAtomContainer);			
 		int number = 1;
 		for (int atomIndex=0; atomIndex < iAtomContainer.getAtomCount(); atomIndex++) {
 			iAtomContainer.getAtom(atomIndex).setID(String.valueOf(number));
 			number++;
 		}
 	}
 
 
 
 	public void assignAtomEnergies(HashMap<String, Double> SMARTSnEnergiesTable) throws CDKException {
 
 		// Variables
 		int numberOfSMARTSmatches = 0;															// Number of SMARTS matches = number of metabolic sites
 
 		// Iterate over the SMARTS in SMARTSnEnergiesTable
 		Set<String> keySetSMARTSnEnergies = (Set<String>) SMARTSnEnergiesTable.keySet();
 		Iterator<String> keySetIteratorSMARTSnEnergies = keySetSMARTSnEnergies.iterator();
 
 		String currentSMARTS = "C";
 		SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS);					// Creates the Query Tool
 
 
 		while(keySetIteratorSMARTSnEnergies.hasNext()){
 
 			try {
 				currentSMARTS = keySetIteratorSMARTSnEnergies.next();
 				querytool.setSmarts(currentSMARTS);
 
 				// Check if there are any SMARTS matches
 				boolean status = querytool.matches(this);
 				if (status) {
 
 
 					numberOfSMARTSmatches = querytool.countMatches();		// Count the number of matches				
 					List<List<Integer>> matchingAtomsIndicesList_1;				// List of List objects each containing the indices of the atoms in the target molecule
 					List<Integer> matchingAtomsIndicesList_2 = null;						// List of atom indices
 					double energy = SMARTSnEnergiesTable.get(currentSMARTS);		// Energy of currentSMARTS
 
 					//					System.out.println("\n The SMARTS " + currentSMARTS + " has " + numberOfSMARTSmatches + " matches in the molecule " + this.getID());
 
 					matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();													// This list contains the C, N, P and S atom indices
 
 					for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						
 
 						matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);							// Contains multiple atoms
 
 						// System.out.println("How many times numberOfSMARTSmatches: " + numberOfSMARTSmatches);							
 						// System.out.println("atomID " +this.getAtom(atomNr).getID()+ ", energy " + energy);
 
 
 						// Set the Energies of the atoms
 						int indexOfMatchingAtom;
 						Atom matchingAtom;
 						for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){								// Contains 1 atom
 							indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);
 
 							// An atom can be matched by several SMARTS and thus assigned several energies
 							// The if clause assures that atoms will get the lowest possible energy
 							matchingAtom = (Atom) this.getAtom(indexOfMatchingAtom);
 
 							if(SMARTCYP_PROPERTY.Energy.get(matchingAtom) == null 
 									|| energy < SMARTCYP_PROPERTY.Energy.get(matchingAtom).doubleValue())
 								SMARTCYP_PROPERTY.Energy.set(matchingAtom,energy);
 						}
 					}
 				}
 			}	
 			catch (CDKException e) {System.out.println("There is something fishy with the SMARTS: " + currentSMARTS); e.printStackTrace();}
 		}
 		//assign energy 999 to all atoms not matching a SMARTS
 		for (int testAtomNr=0; testAtomNr < this.getAtomCount(); testAtomNr++){
 			IAtom testAtom;
 			testAtom = this.getAtom(testAtomNr);
 			if(SMARTCYP_PROPERTY.Energy.get(testAtom) == null) {
 				SMARTCYP_PROPERTY.Energy.set(testAtom,999);
 			}
 		}
 	}
 
 	// Calculates the Accessibilities of all atoms
 	public void calculateAtomAccessabilities() throws CloneNotSupportedException{
 
 
 		int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);
 
 		// Calculate the maximum topology distance
 		// Takes an adjacency matrix and outputs and MaxTopDist matrix of the same size
 		int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
 
 
 		// Find the longest Path of all, "longestMaxTopDistInMolecule"
 		double longestMaxTopDistInMolecule = 0;
 		double currentMaxTopDist = 0;
 		for(int x = 0; x < this.getAtomCount(); x++){
 			for(int y = 0; y < this.getAtomCount(); y++){
 				currentMaxTopDist =  minTopDistMatrix[x][y];
 				if(currentMaxTopDist > longestMaxTopDistInMolecule) longestMaxTopDistInMolecule = currentMaxTopDist;
 			}
 		}
 
 
 		// Find the Accessibility value ("longest shortestPath") for each atom
 
 		// ITERATE REFERENCE ATOMS
 		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 
 			// ITERATE COMPARISON ATOMS
 			double highestMaxTopDistInMatrixRow = 0;
 			IAtom refAtom;
 			for (int compAtomNr = 0; compAtomNr < this.getAtomCount(); compAtomNr++){
 				if(highestMaxTopDistInMatrixRow < minTopDistMatrix[refAtomNr][compAtomNr]) highestMaxTopDistInMatrixRow = minTopDistMatrix[refAtomNr][compAtomNr];
 			}	
 
 			refAtom = this.getAtom(refAtomNr);
 			// Set the Accessibility of the Atom
 			SMARTCYP_PROPERTY.Accessibility.set(refAtom,(highestMaxTopDistInMatrixRow / longestMaxTopDistInMolecule));
 		}
 	}
 
 	// Compute the score of all atoms
 	public void calculateAtomScores() throws CloneNotSupportedException{
 
 		// ITERATE ATOMS
 		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 			IAtom refAtom;
 			refAtom = this.getAtom(refAtomNr);
 			// Calculate the Atom scores
 			if(SMARTCYP_PROPERTY.Accessibility.get(refAtom)!=null) {
 				if(SMARTCYP_PROPERTY.Energy.get(refAtom) != null){
 					double score = SMARTCYP_PROPERTY.Energy.get(refAtom).doubleValue() - 8 * SMARTCYP_PROPERTY.Accessibility.get(refAtom).doubleValue();
 					SMARTCYP_PROPERTY.Score.set(refAtom,score);
 				}
 			}
 		}
 	}
 
 	// Compute the 2D6 score of all atoms
 	public void calculate2D6AtomScores() throws CloneNotSupportedException{
 
 		// ITERATE ATOMS
 		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 			IAtom refAtom;
 			refAtom = this.getAtom(refAtomNr);
 			double CorrectionDist2ProtAmine;
 			double CorrectionSpan2End;
 			//double x;
 			CorrectionDist2ProtAmine = 0;
 			int span2end; 
 			int cutoff = 8;
 			int s2endcutoff = 4;
 			double constant = 6.7;
 			// Calculate the Atom scores
 			if(SMARTCYP_PROPERTY.Accessibility.get(refAtom)!=null) {
 				if(SMARTCYP_PROPERTY.Energy.get(refAtom) != null){
 					if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom) != null){
 						//x = SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).intValue();
 						//if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).intValue()>4 && SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).intValue()<10) Correction2D6 = 30 * Math.exp(-0.5 * (x - 7.5) * (x - 7.5)) + 15 * Math.exp(-0.05 * (x - 7.5) * (x - 7.5));
 						//x = Math.abs(SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).doubleValue() - 7.5);
 						double ProtAmineDist = SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).doubleValue();
 						CorrectionDist2ProtAmine = 0;
 						if(ProtAmineDist < cutoff) CorrectionDist2ProtAmine = constant*(cutoff - ProtAmineDist);
 					}
 					else CorrectionDist2ProtAmine = 0;
 					span2end = SMARTCYP_PROPERTY.Span2End.get(refAtom).intValue();
 					if(span2end < s2endcutoff){
 						CorrectionSpan2End = constant*span2end;
 					}
 					else CorrectionSpan2End = constant*s2endcutoff + 0.01*span2end;
 					double score = SMARTCYP_PROPERTY.Energy.get(refAtom).doubleValue() + CorrectionDist2ProtAmine + CorrectionSpan2End;
 					SMARTCYP_PROPERTY.Score2D6.set(refAtom,score);
 				}
 			}
 		}
 	}
 
 	// Compute the 2C9 score of all atoms
 	public void calculate2C9AtomScores() throws CloneNotSupportedException{
 
 		// ITERATE ATOMS
 		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 			IAtom refAtom;
 			refAtom = this.getAtom(refAtomNr);
 			double CorrectionDist2Carboxylicacid;
 			double CorrectionSpan2End;
 			//double x;
 			CorrectionDist2Carboxylicacid = 0;
 			int span2end; 
 			int cutoff = 8;
 			int s2endcutoff = 4;
 			double constant = 5.9;
 			// Calculate the Atom scores
 			if(SMARTCYP_PROPERTY.Accessibility.get(refAtom)!=null) {
 				if(SMARTCYP_PROPERTY.Energy.get(refAtom) != null){
 					if(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom) != null){
 						double CarboxylicAcidDist = SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom).doubleValue();
 						CorrectionDist2Carboxylicacid = 0;
 						if(CarboxylicAcidDist < cutoff) CorrectionDist2Carboxylicacid = constant*(cutoff - CarboxylicAcidDist);
 					}
 					else CorrectionDist2Carboxylicacid = 0;
 					span2end = SMARTCYP_PROPERTY.Span2End.get(refAtom).intValue();
 					if(span2end < s2endcutoff){
 						CorrectionSpan2End = constant*span2end;
 					}
 					else CorrectionSpan2End = constant*s2endcutoff + 0.01*span2end;
 					double score = SMARTCYP_PROPERTY.Energy.get(refAtom).doubleValue() + CorrectionDist2Carboxylicacid + CorrectionSpan2End;
 					SMARTCYP_PROPERTY.Score2C9.set(refAtom,score);
 				}
 			}
 		}
 	}
 	
 	// Calculates the Span to end of molecule
 	public void calculateSpan2End() throws CloneNotSupportedException{
 
 
 		int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);
 
 		// Calculate the maximum topology distance
 		// Takes an adjacency matrix and outputs and MaxTopDist matrix of the same size
 		int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
 
 
 		// Find the longest Path of all, "longestMaxTopDistInMolecule"
 		double longestMaxTopDistInMolecule = 0;
 		double currentMaxTopDist = 0;
 		for(int x = 0; x < this.getAtomCount(); x++){
 			for(int y = 0; y < this.getAtomCount(); y++){
 				currentMaxTopDist =  minTopDistMatrix[x][y];
 				if(currentMaxTopDist > longestMaxTopDistInMolecule) longestMaxTopDistInMolecule = currentMaxTopDist;
 			}
 		}
 
 
 		// Find the Span2End (maxtopdist - currenttopdist) for each atom
 
 		// ITERATE REFERENCE ATOMS
 		for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 
 			// ITERATE COMPARISON ATOMS
 			double highestMaxTopDistInMatrixRow = 0;
 			IAtom refAtom;
 			for (int compAtomNr = 0; compAtomNr < this.getAtomCount(); compAtomNr++){
 				if(highestMaxTopDistInMatrixRow < minTopDistMatrix[refAtomNr][compAtomNr]) highestMaxTopDistInMatrixRow = minTopDistMatrix[refAtomNr][compAtomNr];
 			}	
 
 			refAtom = this.getAtom(refAtomNr);
 			// Set the Accessibility of the Atom
 			SMARTCYP_PROPERTY.Span2End.set(refAtom,(longestMaxTopDistInMolecule - highestMaxTopDistInMatrixRow));
 
 		}
 	}
 
 	// Calculates the distance to the most distant possibly protonated amine / guanidine nitrogen
 	public void calculateDist2ProtAmine() throws CDKException{
 
 		//locate amine nitrogens which could be protonated
 		// Variables
 		int numberOfSMARTSmatches = 0;	// Number of SMARTS matches = number of protonated amine sites
 		
 		// new matching 2.2
 		String [] SMARTSstrings = {"[$([N][CX3](=[N])[N])" + //guanidine like fragment
 				                   ",$([N^3X3H0]([#6^3])([#6^3])[#6^3]),$([N^3X3H1]([#6^3])[#6^3]),$([N^3X3H2][#6^3])]"};   // primary, secondary, tertiary amines bound to only carbon and hydrogen atoms, not next to sp2 carbon
 				                
 		
 		/* old matching 2.1
 		String [] SMARTSstrings = {"[$([N][CX3](=[N])[N])" + //guanidine like fragment
 				                   ",$([N^3X3H0]([#6^3])([#6^3])[#6^3]),$([N^3X3H1]([#6^3])[#6^3]),$([N^3X3H2][#6^3])" +  // primary, secondary, tertiary amines bound to only carbon and hydrogen atoms, not next to sp2 carbon
 				                   ",$([nD2]1[c][nH][c][c]1)" +  //imidazole nitrogen 1
 								   ",$([nH]1[c][nD2][c][c]1)" +  //imidazole nitrogen 2
 		                           ",$([NX2]([#6,H])=[CX3]([#6,H])[#6])]"}; //imine nitrogen
 		*/ //end old matching 2.1
 		
 		/*start old matching 2.0
 		String [] SMARTSstrings = {"[$([N][CX3](=[N])[N]);!$([NX3][S](=[O])=[O])]", 
                 "[$([NX3]);!$([NX3][#6X3]);!$([NX3][N]=[O]);!$([NX3][S](=[O])=[O])]"}; 
 		*/ //end old matching 2.0
 		
 		for (String currentSMARTS : SMARTSstrings){
 			SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS);		// Creates the Query Tool
 	
 			querytool.setSmarts(currentSMARTS);
 	
 			// Check if there are any SMARTS matches
 			boolean status = querytool.matches(this);
 			if (status) {
 	
 				numberOfSMARTSmatches = querytool.countMatches();		// Count the number of matches				
 				List<List<Integer>> matchingAtomsIndicesList_1;			// List of List objects each containing the indices of the atoms in the target molecule
 				List<Integer> matchingAtomsIndicesList_2 = null;		// List of atom indices
 	
 				matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();	// This list contains the atom indices of protonated amine nitrogens
 	
 				for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						
 	
 					matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);	// Contains multiple atoms
 	
 					// Compute distance for all atoms to the matching atoms
 					int indexOfMatchingAtom;
 					for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){		// Contains 1 atom
 						indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);
 						//System.out.println("\n" + indexOfMatchingAtom);
 						int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);
 						int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
 						//iterate over all atoms
 						for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 							Atom refAtom;
 							refAtom = (Atom) this.getAtom(refAtomNr);
 							int thisdist2protamine;
 							thisdist2protamine = minTopDistMatrix[refAtomNr][indexOfMatchingAtom];
 							if(SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom) != null){
 								if(thisdist2protamine > SMARTCYP_PROPERTY.Dist2ProtAmine.get(refAtom).intValue()){
 									SMARTCYP_PROPERTY.Dist2ProtAmine.set(refAtom,thisdist2protamine);
 								}
 							}
 							else SMARTCYP_PROPERTY.Dist2ProtAmine.set(refAtom,thisdist2protamine);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	// Calculates the distance to the furthest carboxylic acid
 	public void calculateDist2CarboxylicAcid() throws CDKException{
 		
 		//locate carboxylic acid groups
 		// Variables
 		int numberOfSMARTSmatches = 0;	// Number of SMARTS matches = number of carboxylic acid sites
 					       
 		String [] SMARTSstrings = {"[$([O]=[C^2][OH1])" + // carboxylic acid oxygen 
 				   ",$([O]=[C^2][C^2]=[C^2][OH1]),$([O]=[C^2][c][c][OH1])" + // vinylogous carboxylic acids (e.g. ascorbic acid)
 				   ",$([n]1:[n]:[n]:[n]:[c]1)" + // tetrazole 1
 				   ",$([n]1:[n]:[n]:[c]:[n]1)" + // tetrazole 2
 				   ",$([O]=[C^2][N][OH1])" + // hydroxamic acid
 				   ",$([O]=[C^2]([N])[N])" + // urea
 				   ",$([O]=[S][OH1])" + // sulfinic and sulfonic acids 
 				   ",$([O]=[PD4][OH1])" + // phosphate esters and phosphoric acids
 				   ",$([O]=[S](=[O])(c)[C][C]=[O]),$([O]=[C][C][S](=[O])(=[O])[c])" + // sulfones next to phenyls with carbonyl two bonds away
 				   ",$([O]=[S](=[O])[NH1][C]=[O]),$([O]=[C][NH1][S](=[O])=[O])" + // sulfones bound to nitrogen with carbonyl next to it
 				   ",$([O]=[C^2][NH1][O]),$([O]=[C^2][NH1][C]#[N])" + // peptide with oxygen or cyano group next to nitrogen
 				   ",$([OH1][c]1[n][o,s][c,n][c]1),$([OH1][n]1[n][c,n][c][c]1),$([OH1][n]1[c][n][c][c]1)" + // alcohol on aromatic five membered ring
 				   ",$([O]=[C]1[N][C](=O)[O,S][C,N]1)" + // carbonyl oxygen on almost conjugated five membered ring
 				   ",$([O]=[C]1[NH1,O][N]=[N,C][N]1)" + // carbonyl oxygen on fully conjugated five membered ring
 				   ",$([nD2]1[nD2][c]([S]=[O])[nD2][c]1),$([nD2]1[c]([S]=[O])[nD2][c][nD2]1),$([nD2]1[c]([S]=[O])[nD2][nD2][c]1)" + // nitrogens in histidine-like 5-ring with sulfoxide/sulfone next to it
 			       ",$([O]=[SX4](=[O])[NX3])]"}; // sulfonamides
 		
 
 		for (String currentSMARTS : SMARTSstrings){
 			SMARTSQueryTool querytool = new SMARTSQueryTool(currentSMARTS);		// Creates the Query Tool
 			
 			querytool.setSmarts(currentSMARTS);
 			
 			// Check if there are any SMARTS matches
 			boolean status = querytool.matches(this);
 			if (status) {
 				
 				numberOfSMARTSmatches = querytool.countMatches();		// Count the number of matches				
 				List<List<Integer>> matchingAtomsIndicesList_1;			// List of List objects each containing the indices of the atoms in the target molecule
 				List<Integer> matchingAtomsIndicesList_2 = null;		// List of atom indices
 				
 				matchingAtomsIndicesList_1 = querytool.getMatchingAtoms();	// This list contains the atom indices of protonated amine nitrogens
 				
 				for(int listObjectIndex = 0; listObjectIndex < numberOfSMARTSmatches; listObjectIndex++){						
 				
 					matchingAtomsIndicesList_2 = matchingAtomsIndicesList_1.get(listObjectIndex);	// Contains multiple atoms
 				
 					// Compute distance for all atoms to the matching atoms
 					int indexOfMatchingAtom;
 					for (int atomNr = 0; atomNr < matchingAtomsIndicesList_2.size(); atomNr++){		// Contains 1 atom
 						indexOfMatchingAtom = matchingAtomsIndicesList_2.get(atomNr);
 						//System.out.println("\n" + indexOfMatchingAtom);
 						int[][] adjacencyMatrix = AdjacencyMatrix.getMatrix(this);
 						int[][] minTopDistMatrix = PathTools.computeFloydAPSP(adjacencyMatrix);
 						//iterate over all atoms
 						for (int refAtomNr=0; refAtomNr < this.getAtomCount(); refAtomNr++){
 							Atom refAtom;
 							refAtom = (Atom) this.getAtom(refAtomNr);
 							int thisdist2carboxylicacid;
 							thisdist2carboxylicacid = minTopDistMatrix[refAtomNr][indexOfMatchingAtom];
 							if(SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom) != null){
 								if(thisdist2carboxylicacid > SMARTCYP_PROPERTY.Dist2CarboxylicAcid.get(refAtom).intValue()){
 									SMARTCYP_PROPERTY.Dist2CarboxylicAcid.set(refAtom,thisdist2carboxylicacid);
 								}
 							}
 							else SMARTCYP_PROPERTY.Dist2CarboxylicAcid.set(refAtom,thisdist2carboxylicacid);
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 	//  This method makes atomsSortedByEnA
 	public void sortAtoms() throws CDKException{
 
 		Atom currentAtom;
 		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S
 
 		// The Symmetry Numbers are needed to compare the atoms (Atom class and the compareTo method) before adding them below
 		this.setSymmetryNumbers();
 		int[] AddedSymmetryNumbers = new int[this.HighestSymmetryNumber];
 
 		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
 
 			currentAtom = (Atom) this.getAtom(atomNr);
 			int currentSymmetryNumber = SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue();
 
 			// Match atom symbol
 			currentAtomType = currentAtom.getSymbol();
 			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
 				if (FindInArray(AddedSymmetryNumbers,currentSymmetryNumber) == 0) {
 					atomsSortedByEnA.add(currentAtom);
 					AddedSymmetryNumbers[currentSymmetryNumber - 1] = currentSymmetryNumber;
 				}
 			}
 		}
 	}
 	
 	public void sortAtoms2D6() throws CDKException{
 
 		Atom currentAtom;
 		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S
 
 		// The Symmetry Numbers are needed to compare the atoms (Atom class and the compareTo method) before adding them below
 		this.setSymmetryNumbers();
 		int[] AddedSymmetryNumbers = new int[this.HighestSymmetryNumber];
  
 		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
 
 			currentAtom = (Atom) this.getAtom(atomNr);
 			int currentSymmetryNumber = SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue();
 
 			// Match atom symbol
 			currentAtomType = currentAtom.getSymbol();
 			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
 				if (FindInArray(AddedSymmetryNumbers,currentSymmetryNumber) == 0) {
 					atomsSortedByEnA2D6.add(currentAtom);
 					AddedSymmetryNumbers[currentSymmetryNumber - 1] = currentSymmetryNumber;
 				}
 			}
 		}
 	}
 
 	public void sortAtoms2C9() throws CDKException{
 
 		Atom currentAtom;
 		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S
 
 		// The Symmetry Numbers are needed to compare the atoms (Atom class and the compareTo method) before adding them below
 		this.setSymmetryNumbers();
 		int[] AddedSymmetryNumbers = new int[this.HighestSymmetryNumber];
  
 		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
 
 			currentAtom = (Atom) this.getAtom(atomNr);
 			int currentSymmetryNumber = SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue();
 
 			// Match atom symbol
 			currentAtomType = currentAtom.getSymbol();
 			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {
 				if (FindInArray(AddedSymmetryNumbers,currentSymmetryNumber) == 0) {
 					atomsSortedByEnA2C9.add(currentAtom);
 					AddedSymmetryNumbers[currentSymmetryNumber - 1] = currentSymmetryNumber;
 				}
 			}
 		}
 	}
 
 	// Symmetric atoms have identical values in the array from getTopoEquivClassbyHuXu
 	public void setSymmetryNumbers() throws CDKException{
 		Atom atom;
 		//set charges so that they are not null
 		for(int atomIndex = 0; atomIndex < this.getAtomCount(); atomIndex++){
 			atom = (Atom) this.getAtom(atomIndex);
 			atom.setCharge((double) atom.getFormalCharge());
 		}
 		//compute symmetry
 		EquivalentClassPartitioner symmtest = new EquivalentClassPartitioner((AtomContainer) this);
 		int[] symmetryNumbersArray = symmtest.getTopoEquivClassbyHuXu((AtomContainer) this);
 		symmetryNumbersArray[0]=0;//so we can count the number of symmetric sites for each atom without double counting for the ones with the highest symmetrynumber
 		int symmsites;
 		for(int atomIndex = 0; atomIndex < this.getAtomCount(); atomIndex++){
 			symmsites = 0;
 			atom = (Atom) this.getAtom(atomIndex);
 			SMARTCYP_PROPERTY.SymmetryNumber.set(atom,symmetryNumbersArray[atomIndex+1]);
 			// Compute how many symmetric sites the atom has, 1=only itself
 			symmsites = FindInArray(symmetryNumbersArray,symmetryNumbersArray[atomIndex+1]);
 			SMARTCYP_PROPERTY.NrofSymmetricSites.set(atom,symmsites);
 
 			if (symmetryNumbersArray[atomIndex+1] > HighestSymmetryNumber) HighestSymmetryNumber = symmetryNumbersArray[atomIndex+1];
 		}
 	}
 
 
 
 	// This method makes the ranking
 	public void rankAtoms() throws CDKException{
 
 		// Iterate over the Atoms in this sortedAtomsTreeSet
 		int rankNr = 1;
 		int loopNr = 1;
 		Atom previousAtom = null;
 		Atom currentAtom;
 		Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA().iterator();
 		while(atomsSortedByEnAiterator.hasNext()){
 
 			currentAtom = atomsSortedByEnAiterator.next();
 
 			// First Atom
 			if(previousAtom == null){}				// Do nothing												
 
 			// Atoms have no score, compare Accessibility instead
 			else if(SMARTCYP_PROPERTY.Score.get(currentAtom) == null){
 				if(SMARTCYP_PROPERTY.Accessibility.get(currentAtom) != SMARTCYP_PROPERTY.Accessibility.get(previousAtom)) rankNr = loopNr;
 			} 
 
 			// Compare scores
 			else if(SMARTCYP_PROPERTY.Score.get(currentAtom).doubleValue() > SMARTCYP_PROPERTY.Score.get(previousAtom).doubleValue()) rankNr = loopNr;
 
 			// Else, Atoms have the same score
 			SMARTCYP_PROPERTY.Ranking.set(currentAtom,rankNr);
 			previousAtom = currentAtom;	
 			loopNr++;
 		}
 
 		this.rankSymmetricAtoms();
 	}
 
 	// This method makes the ranking
 	public void rankAtoms2D6() throws CDKException{
 
 		// Iterate over the Atoms in this sortedAtomsTreeSet
 		int rankNr = 1;
 		int loopNr = 1;
 		Atom previousAtom = null;
 		Atom currentAtom;
 		Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2D6().iterator();
 		while(atomsSortedByEnAiterator.hasNext()){
 
 			currentAtom = atomsSortedByEnAiterator.next();
 
 			// First Atom
 			if(previousAtom == null){}				// Do nothing												
 
 			// Atoms have no score, compare Accessibility instead
 			//else if(SMARTCYP_PROPERTY.Score2D6.get(currentAtom) == null){
 			//	if(SMARTCYP_PROPERTY.Accessibility.get(currentAtom) != SMARTCYP_PROPERTY.Accessibility.get(previousAtom)) rankNr = loopNr;
 			//} 
 
 			// Compare scores
 			else if(SMARTCYP_PROPERTY.Score2D6.get(currentAtom).doubleValue() > SMARTCYP_PROPERTY.Score2D6.get(previousAtom).doubleValue()) rankNr = loopNr;
 
 			// Else, Atoms have the same score
 			SMARTCYP_PROPERTY.Ranking2D6.set(currentAtom,rankNr);
 			previousAtom = currentAtom;	
 			loopNr++;
 		}
 
 		this.rankSymmetricAtoms2D6();
 	}
 
 	// This method makes the ranking
 	public void rankAtoms2C9() throws CDKException{
 
 		// Iterate over the Atoms in this sortedAtomsTreeSet
 		int rankNr = 1;
 		int loopNr = 1;
 		Atom previousAtom = null;
 		Atom currentAtom;
 		Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2C9().iterator();
 		while(atomsSortedByEnAiterator.hasNext()){
 
 			currentAtom = atomsSortedByEnAiterator.next();
 
 			// First Atom
 			if(previousAtom == null){}				// Do nothing												
 
 			// Atoms have no score, compare Accessibility instead
 			//else if(SMARTCYP_PROPERTY.Score2D6.get(currentAtom) == null){
 			//	if(SMARTCYP_PROPERTY.Accessibility.get(currentAtom) != SMARTCYP_PROPERTY.Accessibility.get(previousAtom)) rankNr = loopNr;
 			//} 
 
 			// Compare scores
 			else if(SMARTCYP_PROPERTY.Score2C9.get(currentAtom).doubleValue() > SMARTCYP_PROPERTY.Score2C9.get(previousAtom).doubleValue()) rankNr = loopNr;
 
 			// Else, Atoms have the same score
 			SMARTCYP_PROPERTY.Ranking2C9.set(currentAtom,rankNr);
 			previousAtom = currentAtom;	
 			loopNr++;
 		}
 
 		this.rankSymmetricAtoms2C9();
 	}
 
 	// This method makes the ranking of symmetric atoms
 	public void rankSymmetricAtoms() throws CDKException{
 
 		Atom currentAtom;
 		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S
 
 		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
 
 			currentAtom = (Atom) this.getAtom(atomNr);
 
 			// Match atom symbol
 			currentAtomType = currentAtom.getSymbol();
 			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {			
 
 				//This clause finds symmetric atoms which have not been assigned a ranking
 				if(SMARTCYP_PROPERTY.Ranking.get(currentAtom) == null){
 
 					// AtomsSortedByEnA contains the ranked atoms
 					// We just need to find the symmetric atom and use its ranking for the unranked symmetric atom
 					Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA().iterator();
 					Atom rankedAtom;
 					Number rankNr;
 					while(atomsSortedByEnAiterator.hasNext()){
 
 						rankedAtom = atomsSortedByEnAiterator.next();
 
 						if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(rankedAtom).intValue()){
 
 							rankNr = SMARTCYP_PROPERTY.Ranking.get(rankedAtom);
 							SMARTCYP_PROPERTY.Ranking.set(currentAtom,rankNr);
 							SMARTCYP_PROPERTY.IsSymmetric.set(currentAtom,1);
 
 						}
 					}
 
 				}
 
 			}
 		}
 	}
 
 	// This method makes the ranking of symmetric atoms
 	public void rankSymmetricAtoms2D6() throws CDKException{
 
 		Atom currentAtom;
 		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S
 
 		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
 
 			currentAtom = (Atom) this.getAtom(atomNr);
 
 			// Match atom symbol
 			currentAtomType = currentAtom.getSymbol();
 			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {			
 
 				//This clause finds symmetric atoms which have not been assigned a ranking
 				if(SMARTCYP_PROPERTY.Ranking2D6.get(currentAtom) == null){
 
 					// AtomsSortedByEnA contains the ranked atoms
 					// We just need to find the symmetric atom and use its ranking for the unranked symmetric atom
 					Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2D6().iterator();
 					Atom rankedAtom;
 					Number rankNr;
 					while(atomsSortedByEnAiterator.hasNext()){
 
 						rankedAtom = atomsSortedByEnAiterator.next();
 
 						if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(rankedAtom).intValue()){
 
 							rankNr = SMARTCYP_PROPERTY.Ranking2D6.get(rankedAtom);
 							SMARTCYP_PROPERTY.Ranking2D6.set(currentAtom,rankNr);
 							SMARTCYP_PROPERTY.IsSymmetric.set(currentAtom,1);
 
 						}
 					}
 
 				}
 
 			}
 		}
 	}	
 
 	// This method makes the ranking of symmetric atoms
 	public void rankSymmetricAtoms2C9() throws CDKException{
 
 		Atom currentAtom;
 		String currentAtomType;					// Atom symbol i.e. C, H, N, P or S
 
 		for (int atomNr = 0; atomNr < this.getAtomCount(); atomNr++){
 
 			currentAtom = (Atom) this.getAtom(atomNr);
 
 			// Match atom symbol
 			currentAtomType = currentAtom.getSymbol();
 			if(currentAtomType.equals("C") || currentAtomType.equals("N") || currentAtomType.equals("P") || currentAtomType.equals("S")) {			
 
 				//This clause finds symmetric atoms which have not been assigned a ranking
 				if(SMARTCYP_PROPERTY.Ranking2C9.get(currentAtom) == null){
 
 					// AtomsSortedByEnA contains the ranked atoms
 					// We just need to find the symmetric atom and use its ranking for the unranked symmetric atom
 					Iterator<Atom> atomsSortedByEnAiterator = this.getAtomsSortedByEnA2C9().iterator();
 					Atom rankedAtom;
 					Number rankNr;
 					while(atomsSortedByEnAiterator.hasNext()){
 
 						rankedAtom = atomsSortedByEnAiterator.next();
 
 						if(SMARTCYP_PROPERTY.SymmetryNumber.get(currentAtom).intValue() == SMARTCYP_PROPERTY.SymmetryNumber.get(rankedAtom).intValue()){
 
 							rankNr = SMARTCYP_PROPERTY.Ranking2C9.get(rankedAtom);
 							SMARTCYP_PROPERTY.Ranking2C9.set(currentAtom,rankNr);
 							SMARTCYP_PROPERTY.IsSymmetric.set(currentAtom,1);
 
 						}
 					}
 
 				}
 
 			}
 		}
 	}	
 
 	// Get the TreeSet containing the sorted C, N, P and S atoms
 	public TreeSet<Atom> getAtomsSortedByEnA(){
 		return this.atomsSortedByEnA;
 	}
 
 	public TreeSet<Atom> getAtomsSortedByEnA2D6(){
 		return this.atomsSortedByEnA2D6;
 	}
 
 	public TreeSet<Atom> getAtomsSortedByEnA2C9(){
 		return this.atomsSortedByEnA2C9;
 	}
 	
 	public void setID(String id){
 		super.setID(id);
 	}
 
 	public String toString(){
 		for(int atomNr=0; atomNr < this.getAtomCount(); atomNr++) System.out.println(this.getAtom(atomNr).toString());
 		return "MoleculeKU " + super.toString();
 	}
 
 	public static int FindInArray(int[] arr, int numToFind) {
 		int occurence=0;
 		for (int i = 0; i < arr.length; i++) { 
 			if (arr[i] == numToFind) occurence++;
 		}
 		return occurence;
 	}
 
 	public int[] convertStringArraytoIntArray(String[] sarray) throws Exception {
 		if (sarray != null) {
 			int intarray[] = new int[sarray.length];
 			for (int i = 0; i < sarray.length; i++) {
 				intarray[i] = Integer.parseInt(sarray[i]);
 			}
 			return intarray;
 		}
 		return null;
 	}
 
 	public static int [] concatAll(int[] first, int[]... rest) {
 		int totalLength = first.length;
 		for (int [] array : rest) {
 			totalLength += array.length;
 		}
 		int [] result = Arrays.copyOf(first, totalLength);
 		int offset = first.length;
 		for (int [] array : rest) {
 			System.arraycopy(array, 0, result, offset, array.length);
 			offset += array.length;
 		}
 		return result;
 	}
 
 }
 
 
 
 
 
