 /*
  *                    BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence.  This should
  * be distributed with the code.  If you do not have a copy,
  * see:
  *
  *      http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors.  These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  *      http://www.biojava.org/
  *
  * Created on Jun 12, 2010
  * Author: Jianjiong Gao 
  *
  */
 
 package org.biojava3.protmod.structure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.biojava.bio.structure.Atom;
 import org.biojava.bio.structure.Chain;
 import org.biojava.bio.structure.Group;
 import org.biojava.bio.structure.GroupType;
 import org.biojava.bio.structure.PDBResidueNumber;
 import org.biojava.bio.structure.Structure;
 import org.biojava.bio.structure.StructureException;
 import org.biojava.bio.structure.StructureTools;
 //import org.biojava.bio.structure.io.mmcif.chem.ResidueType;
 //import org.biojava.bio.structure.io.mmcif.chem.PolymerType;
 //import org.biojava.bio.structure.io.mmcif.model.ChemComp;
 
 import org.biojava3.protmod.Component;
 import org.biojava3.protmod.ComponentType;
 import org.biojava3.protmod.ModificationCondition;
 import org.biojava3.protmod.ModificationLinkage;
 import org.biojava3.protmod.ProteinModification;
 
 /**
  * Identify attachment modification in a 3-D structure.
  * 
  * @author Jianjiong Gao
  * @since 3.0
  */
 public class ProteinModificationParser {
 	
 	private double bondLengthTolerance = 0.4;
 	private boolean recordUnidentifiableModifiedCompounds = false;
 	
 	private Set<ModifiedCompound> identifiedModifiedCompounds = null;
 	private Set<StructureAtomLinkage> unidentifiableAtomLinkages = null;
 	private Set<StructureGroup> unidentifiableModifiedResidues = null;
 	
 	/**
 	 * 
 	 * @param bondLengthTolerance tolerance of error (in Angstroms) of the
 	 *  covalent bond length, when calculating the atom distance threshold.
 	 */
 	public void setbondLengthTolerance(final double bondLengthTolerance) {
 		if (bondLengthTolerance<0) {
 			throw new IllegalArgumentException("bondLengthTolerance " +
 					"must be positive.");
 		}
 		this.bondLengthTolerance = bondLengthTolerance;
 	}
 	
 	/**
 	 * 
 	 * @param recordUnidentifiableAtomLinkages true if choosing to record unidentifiable
 	 *  atoms; false, otherwise.
 	 * @see #getUnidentifiableModifiedResidues
 	 * @see #getUnidentifiableAtomLinkages
 	 */
 	public void setRecordUnidentifiableCompounds(boolean recordUnidentifiableModifiedCompounds) {
 		this.recordUnidentifiableModifiedCompounds = recordUnidentifiableModifiedCompounds;
 	}
 	
 	/**
 	 * 
 	 * @return a set of identified {@link ModifiedCompound}s from
 	 *  the last parse result.
 	 * @see ModifiedCompound
 	 */
 	public Set<ModifiedCompound> getIdentifiedModifiedCompound() {
 		if (identifiedModifiedCompounds==null) {
 			throw new IllegalStateException("No result available. Please call parse() first.");
 		}
 		
 		return identifiedModifiedCompounds;
 	}
 	
 	/**
 	 * 
 	 * @return a set of atom linkages, which represent the 
 	 *  atom bonds that were not covered by the identified 
 	 *  {@link ModifiedCompound}s from the last parse result.
 	 *  Each element of the list is a array containing two atoms.
 	 * @see StructureAtomLinkage
 	 * @see #setRecordUnidentifiableCompounds
 	 */
 	public Set<StructureAtomLinkage> getUnidentifiableAtomLinkages() {
 		if (!recordUnidentifiableModifiedCompounds) {
 			throw new UnsupportedOperationException("Recording unidentified atom linkages" +
 					"is not supported. Please setRecordUnidentifiableCompounds(true) first.");
 		}
 		
 		if (identifiedModifiedCompounds==null) {
 			throw new IllegalStateException("No result available. Please call parse() first.");
 		}
 		
 		return unidentifiableAtomLinkages;
 	}
 	
 	/**
 	 * 
 	 * @return a set of modified residues that were not covered by
 	 *  the identified ModifiedCompounds from the last parse 
 	 *  result.
 	 *  @see StructureGroup
 	 *  @see #setRecordUnidentifiableCompounds
 	 *  @see #getIdentifiedModifiedCompound
 	 */
 	public Set<StructureGroup> getUnidentifiableModifiedResidues() {
 		if (!recordUnidentifiableModifiedCompounds) {
 			throw new UnsupportedOperationException("Recording unidentified atom linkages" +
 					"is not supported. Please setRecordUnidentifiableCompounds(true) first.");
 		}
 		
 		if (identifiedModifiedCompounds==null) {
 			throw new IllegalStateException("No result available. Please call parse() first.");
 		}
 		
 		return unidentifiableModifiedResidues;
 	}
 	
 	/**
 	 * Parse modifications in a structure.
 	 * @param structure query {@link Structure}.
 	 * @param potentialModifications query {@link ProteinModification}s.
 	 */
 	public void parse(final Structure structure, 
 			final Set<ProteinModification> potentialModifications) {
 		identifiedModifiedCompounds = new LinkedHashSet<ModifiedCompound>();
 		if (recordUnidentifiableModifiedCompounds) {
 			unidentifiableAtomLinkages = new LinkedHashSet<StructureAtomLinkage>();
 			unidentifiableModifiedResidues = new LinkedHashSet<StructureGroup>();
 		}
 		
 		if (structure==null) {
 			throw new IllegalArgumentException("Null structure.");
 		}
 		
 		if (potentialModifications==null) {
 			throw new IllegalArgumentException("Null potentialModifications.");
 		}
 		
 		if (potentialModifications.isEmpty()) {
 			return;
 		}
 		
 		List<Chain> chains = structure.getChains();
 		
 		for (Chain chain : chains) {
 			List<ModifiedCompound> modComps = new ArrayList<ModifiedCompound>();
 			
 			List<Group> residues = getAminoAcids(chain);
 			List<Group> ligands = chain.getAtomLigands();
 			
 			Map<Component, Set<Group>> mapCompGroups = 
 				getModificationGroups(potentialModifications, residues, ligands);
 			
 			for (ProteinModification mod : potentialModifications) {
 				ModificationCondition condition = mod.getCondition();
 				List<Component> components = condition.getComponents();
 				if (!mapCompGroups.keySet().containsAll(components)) {
 					// not all components exist for this mod.
 					continue;
 				}
 				
 				int sizeComps = components.size();
 				if (sizeComps==1) {
 					// modified residue
 					// TODO: is this the correct logic for CROSS_LINK_1?
 					Set<Group> modifiedResidues = mapCompGroups.get(components.get(0));
 					if (modifiedResidues != null) {
 						for (Group residue : modifiedResidues) {
 							PDBResidueNumber resNum = StructureTools.getPDBResidueNumber(residue);
 							StructureGroup strucGroup = new StructureGroup(resNum, residue.getPDBCode(), ComponentType.AMINOACID);
 							ModifiedCompound modRes = new ModifiedCompoundImpl(mod, strucGroup);
 							modComps.add(modRes);
 						}
 					}
 				} else {
 					// for multiple components
 					
 					// find linkages first
 					List<List<Atom[]>> matchedAtomsOfLinkages =
 							getMatchedAtomsOfLinkages(condition, mapCompGroups);
 					
 					if (matchedAtomsOfLinkages.size() != condition.getLinkages().size()) {
 						continue;
 					}
 					
 					assembleLinkages(matchedAtomsOfLinkages, mod, modComps);
 				}
 			}
 
 			// identify additional groups that are not directly attached to amino acids.
 			for (ModifiedCompound mc : modComps) {
 				identifyAdditionalAttachments(mc, ligands, chain);
 			}
 			
 			identifiedModifiedCompounds.addAll(modComps);
 			
 			// record unidentifiable linkage
 			if (recordUnidentifiableModifiedCompounds) {
 				recordUnidentifiableAtomLinkages(modComps, residues, ligands);
 				recordUnidentifiableModifiedResidues(modComps, residues);
 			}
 		}
 	}
 	
 	// TODO: this should be replaced when Andreas fix the getAtomGroups("amino");
 	/**
 	 * Get all amino acids in a chain.
 	 * @param chain
 	 * @return
 	 */
 	private List<Group> getAminoAcids(Chain chain) {
 //		List<Group> residues = new ArrayList<Group>();
 //		for (Group group : chain.getAtomGroups()) {
 //			ChemComp cc = group.getChemComp();
 //			if (ResidueType.lPeptideLinking.equals(cc.getResidueType()) ||
 //					PolymerType.PROTEIN_ONLY.contains(cc.getPolymerType())) {
 //				residues.add(group);
 //			}
 //		}
 		List<Group> residues = new ArrayList<Group>(chain.getSeqResGroups());
 		residues.retainAll(chain.getAtomGroups());
 		
 		return residues;
 	}
 	
 	/**
 	 * identify additional groups that are not directly attached to amino acids.
 	 * @param mc {@link ModifiedCompound}.
 	 * @param chain a {@link Chain}.
 	 * @return a list of added groups.
 	 */
 	private void identifyAdditionalAttachments(ModifiedCompound mc, 
 			List<Group> ligands, Chain chain) {
 		if (ligands.isEmpty()) {
 			return;
 		}
 		
 		// TODO: should the additional groups only be allowed to the identified 
 		// heta groups or both amino acids and heta groups?
 		// TODO: how about chain-chain links?
 		List<Group> identifiedGroups = new ArrayList<Group>();
 		for (StructureGroup num : mc.getGroups(ComponentType.LIGAND)) {
 			Group group;
 			try {
 				String numIns = "" + num.getResidueNumber();
 				if (num.getInsCode() != null) {
 					numIns += num.getInsCode();
 				}
 				group = chain.getGroupByPDB(numIns);
 			} catch (StructureException e) {
 				// should not happen
 				continue;
 			}
 			identifiedGroups.add(group);
 		}
 		
 		int start = 0;
 		
 		int n = identifiedGroups.size();
 		while (n > start) {
 			for (Group group1 : ligands) {
 				for (int i=start; i<n; i++) {
 					Group group2 = identifiedGroups.get(i);
 					if (!identifiedGroups.contains(group1)) {
 						List<Atom[]> linkedAtoms = StructureUtil.findNonNCAtomLinkages(
 								group1, false, group2, false, bondLengthTolerance);
 						if (!linkedAtoms.isEmpty()) {
 							for (Atom[] atoms : linkedAtoms) {
 								mc.addAtomLinkage(StructureUtil.getStructureAtomLinkage(atoms[0], 
 										ComponentType.LIGAND, atoms[1], ComponentType.LIGAND));
 							}
 							identifiedGroups.add(group1);
 							break;
 						}
 					}
 				}
 			}
 			
 			start = n;
 			n = identifiedGroups.size();
 		}
 	}
 	
 	/**
 	 * Record unidentifiable atom linkages in a chain. Only linkages between two
 	 * residues or one residue and one ligand will be recorded.
 	 */
 	private void recordUnidentifiableAtomLinkages(List<ModifiedCompound> modComps,
 			List<Group> residues, List<Group> ligands) {
 		
 		// first put identified linkages in a map for fast query
 		Set<StructureAtomLinkage> identifiedLinkages = new HashSet<StructureAtomLinkage>();
 		for (ModifiedCompound mc : modComps) {
 			identifiedLinkages.addAll(mc.getAtomLinkages());
 		}
 		
 		// record
 		// cross link
 		int nRes = residues.size();
 		for (int i=0; i<nRes-1; i++) {
 			Group group1 = residues.get(i);
 			for (int j=i+1; j<nRes; j++) {
 				Group group2 = residues.get(j);
 				List<Atom[]> linkages = StructureUtil.findNonNCAtomLinkages(
 						group1, true, group2, true, bondLengthTolerance);
 				for (Atom[] atoms : linkages) {
 					StructureAtomLinkage link = StructureUtil.getStructureAtomLinkage(atoms[0], 
							ComponentType.AMINOACID, atoms[1], ComponentType.AMINOACID);
 					if (!identifiedLinkages.contains(link)) {
 						unidentifiableAtomLinkages.add(link);
 					}
 				}
 			}
 		}
 		
 		// attachment
 		int nLig = ligands.size();
 		for (int i=0; i<nRes; i++) {
 			Group group1 = residues.get(i);
 			for (int j=0; j<nLig; j++) {
 				Group group2 = ligands.get(j);
 				if (group1==group2) { // overlap between residues and ligands
 					continue;
 				}
 				List<Atom[]> linkages = StructureUtil.findNonNCAtomLinkages(
 						group1, true, group2, false, bondLengthTolerance);
 				for (Atom[] atoms : linkages) {
 					StructureAtomLinkage link = StructureUtil.getStructureAtomLinkage(atoms[0], 
							ComponentType.AMINOACID, atoms[1], ComponentType.LIGAND);
 					if (!identifiedLinkages.contains(link)) {
 						unidentifiableAtomLinkages.add(link);
 					}
 				}
 			}
 		}
 	}
 	
 	private void recordUnidentifiableModifiedResidues(List<ModifiedCompound> modComps, List<Group> residues) {
 		Set<StructureGroup> identifiedComps = new HashSet<StructureGroup>();
 		for (ModifiedCompound mc : modComps) {
 			identifiedComps.addAll(mc.getGroups(ComponentType.AMINOACID));
 		}
 		
 		// TODO: use the ModifiedAminoAcid after Andreas add that.
 		for (Group group : residues) {
 			if (group.getType().equals(GroupType.HETATM)) {
 				StructureGroup strucGroup = StructureUtil.getStructureGroup(
 						group, ComponentType.AMINOACID);
 				if (!identifiedComps.contains(strucGroup)) {
 					unidentifiableModifiedResidues.add(strucGroup);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param modifications a set of {@link ProteinModification}s.
 	 * @param residues
 	 * @param ligands 
 	 * @return map from component to list of corresponding residues
 	 *  in the chain.
 	 */
 	private Map<Component, Set<Group>> getModificationGroups(
 			final Set<ProteinModification> modifications,
 			final List<Group> residues,
 			final List<Group> ligands) {
 		if (residues==null || ligands==null || modifications==null) {
 			throw new IllegalArgumentException("Null argument(s).");
 		}
 		
 		Set<Component> comps = new HashSet<Component>();
 		for (ProteinModification mod : modifications) {
 			ModificationCondition condition = mod.getCondition();
 			for (Component comp : condition.getComponents()) {
 				comps.add(comp);
 			}
 		}
 		
 		Map<Component, Set<Group>> mapCompRes = 
 			new HashMap<Component, Set<Group>>();
 		
 		{
 			// ligands
 			for (Group group : ligands) {
 				String pdbccId = group.getPDBName().trim();
 				Component comp = Component.of(pdbccId, ComponentType.LIGAND);
 				if (!comps.contains(comp)) {
 					continue;
 				}
 				Set<Group> gs = mapCompRes.get(comp);
 				if (gs==null) {
 					gs = new LinkedHashSet<Group>();
 					mapCompRes.put(comp, gs);
 				}
 				gs.add(group);
 			}
 		}
 		
 		{
 			// residues
 			if (residues.isEmpty()) {
 				return mapCompRes;
 			}
 			
 			// for all residues
 			for (Group group : residues) {
 				String pdbccId = group.getPDBName().trim();
 				Component comp = Component.of(pdbccId, ComponentType.AMINOACID);
 				if (!comps.contains(comp)) {
 					continue;
 				}
 				Set<Group> gs = mapCompRes.get(comp);
 				if (gs==null) {
 					gs = new LinkedHashSet<Group>();
 					mapCompRes.put(comp, gs);
 				}
 				gs.add(group);
 			}
 
 			// for N-terminal
 			int nRes = residues.size();
 			int iRes = 0;
 			Group res;
 			do {
 				// for all ligands on N terminal and the first residue
 				res = residues.get(iRes++);
 
 				Component comp = Component.of(res.getPDBName(), ComponentType.AMINOACID, true, false);
 				if (comps.contains(comp)) {
 					Set<Group> gs = mapCompRes.get(comp);
 					if (gs==null) {
 						gs = new LinkedHashSet<Group>();
 						mapCompRes.put(comp, gs);
 					}
 					gs.add(res);
 				}
 			} while (iRes<nRes && ligands.contains(res));
 			
 			// for C-terminal
 			iRes = residues.size()-1;
 			do {
 				// for all ligands on C terminal and the last residue
 				res = residues.get(iRes--);
 				
 				Component comp = Component.of(res.getPDBName(), ComponentType.AMINOACID, false, true);
 				if (comps.contains(comp)) {
 					Set<Group> gs = mapCompRes.get(comp);
 					if (gs==null) {
 						gs = new LinkedHashSet<Group>();
 						mapCompRes.put(comp, gs);
 					}
 					gs.add(res);
 				}
 			} while (iRes>=0 && ligands.contains(res));
 		}
 
 		return mapCompRes;
 	}
 	
 	/**
 	 * Get matched atoms for all linkages.	
 	 */
 	private List<List<Atom[]>> getMatchedAtomsOfLinkages(
 			ModificationCondition condition, Map<Component, Set<Group>> mapCompGroups) {
 		List<ModificationLinkage> linkages = condition.getLinkages();
 		int nLink = linkages.size();
 
 		List<List<Atom[]>> matchedAtomsOfLinkages = 
 				new ArrayList<List<Atom[]>>(nLink);
 		
 		for (int iLink=0; iLink<nLink; iLink++) {
 			ModificationLinkage linkage = linkages.get(iLink);
 			Component comp1 = linkage.getComponent1();
 			Component comp2 = linkage.getComponent2();
 
 			boolean isAA1 = comp1.getType()==ComponentType.AMINOACID;
 			boolean isAA2 = comp2.getType()==ComponentType.AMINOACID;
 			
 			Set<Group> groups1 = mapCompGroups.get(comp1);
 			Set<Group> groups2 = mapCompGroups.get(comp2);						
 			
 			List<Atom[]> list = new ArrayList<Atom[]>();
 
 			List<String> potentialNamesOfAtomOnGroup1 = linkage.getPDBNameOfPotentialAtomsOnComponent1();
 			List<String> potentialNamesOfAtomOnGroup2 = linkage.getPDBNameOfPotentialAtomsOnComponent2();
 
 			for (Group g1 : groups1) {
 				for (Group g2 : groups2) {
 					if (g1 == g2) {
 						continue;
 					}
 		
 					Atom[] atoms = StructureUtil.findNearestNonNCAtomLinkage(
 							g1, isAA1, g2, isAA2,
 							potentialNamesOfAtomOnGroup1,
 							potentialNamesOfAtomOnGroup2, 
 							bondLengthTolerance);
 					if (atoms!=null) {
 						list.add(atoms);
 					}
 				}
 			}
 				
 			if (list.isEmpty()) {
 				// broken linkage
 				break;
 			}
 	
 			matchedAtomsOfLinkages.add(list);
 		}
 		
 		return matchedAtomsOfLinkages;
 	}
 	
 	/**
 	 * Assembly the matched linkages.
 	 * @param matchedAtomsOfLinkages
 	 * @param mod
 	 * @param condition
 	 * @param ret ModifiedCompound will be stored here.
 	 */
 	private void assembleLinkages(List<List<Atom[]>> matchedAtomsOfLinkages,
 			ProteinModification mod, List<ModifiedCompound> ret) {
 		ModificationCondition condition = mod.getCondition();
 		List<ModificationLinkage> modLinks = condition.getLinkages();
 		
 		int nLink = matchedAtomsOfLinkages.size();
 		int[] indices = new int[nLink];
 		Set<ModifiedCompound> identifiedCompounds = new HashSet<ModifiedCompound>();
 		while (indices[0]<matchedAtomsOfLinkages.get(0).size()) {
 			List<Atom[]> atomLinkages = new ArrayList<Atom[]>(nLink);
 			for (int iLink=0; iLink<nLink; iLink++) {
 				Atom[] atoms = matchedAtomsOfLinkages.get(iLink).get(indices[iLink]);
 				atomLinkages.add(atoms);
 			}
 			if (matchLinkages(modLinks, atomLinkages)) {
 				// matched
 				
 				int n = atomLinkages.size();
 				List<StructureAtomLinkage> linkages = new ArrayList<StructureAtomLinkage>(n);
 				for (int i=0; i<n; i++) {
 					Atom[] linkage = atomLinkages.get(i);
 					StructureAtomLinkage link = StructureUtil.getStructureAtomLinkage(
 							linkage[0], modLinks.get(i).getComponent1().getType(), 
 							linkage[1], modLinks.get(i).getComponent2().getType());
 					linkages.add(link);
 				}
 				
 				ModifiedCompound mc = new ModifiedCompoundImpl(mod, linkages);
 				if (!identifiedCompounds.contains(mc)) {
 					ret.add(mc);
 					identifiedCompounds.add(mc);
 				}
 			}
 			
 			// indices++ (e.g. [0,0,1]=>[0,0,2]=>[1,2,0])
 			int i = nLink-1;
 			while (i>=0) {
 				if (i==0 || indices[i]<matchedAtomsOfLinkages.get(i).size()-1) {
 					indices[i]++;
 					break;
 				} else {
 					indices[i] = 0;
 					i--;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param linkages
 	 * @param atomLinkages
 	 * @return true if atomLinkages satisfy the condition; false, otherwise.
 	 */
 	private boolean matchLinkages(List<ModificationLinkage> linkages, 
 			List<Atom[]> atomLinkages) {
 		int nLink = linkages.size();
 		if (nLink != atomLinkages.size()) {
 			return false;
 		}
 		for (int i=0; i<nLink-1; i++) {
 			ModificationLinkage link1 = linkages.get(i);
 			Atom[] atoms1 = atomLinkages.get(i);
 			for (int j=i+1; j<nLink; j++) {
 				ModificationLinkage link2 = linkages.get(j);
 				Atom[] atoms2 = atomLinkages.get(j);
 				
 				// check components
 				if (((link1.getIndexOfComponent1()==link2.getIndexOfComponent1())
 							!= (atoms1[0].getParent()==atoms2[0].getParent()))
 					|| ((link1.getIndexOfComponent1()==link2.getIndexOfComponent2())
 							!= (atoms1[0].getParent()==atoms2[1].getParent()))
 					|| ((link1.getIndexOfComponent2()==link2.getIndexOfComponent1())
 							!= (atoms1[1].getParent()==atoms2[0].getParent()))
 					|| ((link1.getIndexOfComponent2()==link2.getIndexOfComponent2())
 							!= (atoms1[1].getParent()==atoms2[1].getParent()))) {
 					return false;
 				}
 				
 				// check atoms
 				String label11 = link1.getLabelOfAtomOnComponent1();
 				String label12 = link1.getLabelOfAtomOnComponent2();
 				String label21 = link2.getLabelOfAtomOnComponent1();
 				String label22 = link2.getLabelOfAtomOnComponent2();
 				if ((label11!=null && label21!=null && label11.equals(label21))
 							!= (atoms1[0]==atoms2[0])
 					 || (label11!=null && label22!=null && label11.equals(label22))
 							!= (atoms1[0]==atoms2[1])
 					 || (label12!=null && label21!=null && label12.equals(label21))
 							!= (atoms1[1]==atoms2[0])
 					 || (label12!=null && label22!=null && label12.equals(label22))
 							!= (atoms1[1]==atoms2[1])) {
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 }
