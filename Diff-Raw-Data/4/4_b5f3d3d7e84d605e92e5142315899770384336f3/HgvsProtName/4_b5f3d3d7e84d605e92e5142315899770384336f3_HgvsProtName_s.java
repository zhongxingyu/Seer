 package org.cchmc.bmi.snpomics.annotation.interactive;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cchmc.bmi.snpomics.annotation.reference.TranscriptAnnotation;
 import org.cchmc.bmi.snpomics.translation.AminoAcid;
 
 public class HgvsProtName {
 
 	public HgvsProtName(TranscriptAnnotation tx) {
 		this.tx = tx;
 		name = null;
 		startCoord = 0;
 		ref = null;
 		alt = null;
 		extension = null;
 		isFrameshift = false;
 	}
 
 	public String getName() {
 		if (name == null)
 			buildName();
 		return name;
 	}
 	
 	/**
 	 * The coordinate of the first amino acid specified to setRef().  The initiating Met has coordinate
 	 * 1, and then you count up.  This is much easier than cDNA :)
 	 * @param startCoord
 	 */
 	public void setStartCoord(int startCoord) {
 		this.startCoord = startCoord;
 	}
 
 	/**
 	 * The reference allele (in amino acids), including the AA before and after the change.
 	 * Do not extend beyond the coding sequence - so if the variation affects Met1, don't include
 	 * the preceding AA.
 	 */
 	public void setRef(List<AminoAcid> ref) {
 		this.ref = ref;
 	}
 
 	/**
 	 * The alternate allele corresponding to the same region as the ref allele.  If the mutation
 	 * is a frameshift, the alt allele should include translated sequence as far as possible (ie, 
 	 * to the end of transcription).  The same is true if the mutation destroys a stop codon
 	 * @param alt
 	 */
 	public void setAlt(List<AminoAcid> alt) {
 		this.alt = alt;
 	}
 
 	/**
 	 * The protein sequence (for the reference) starting just after the allele set in setRef().  If
 	 * setFrameshift(true) has been called, the allele specified in setAlt() should also include this
 	 * DNA sequence (though it will of course be a different protein sequence due to the frameshift)
 	 * @param ext
 	 */
 	public void setExtension(List<AminoAcid> ext) {
 		this.extension = ext;
 	}
 	
 	public void setFrameshift(boolean isFrameshift) {
 		this.isFrameshift = isFrameshift;
 	}
 
 	private void buildName() {
 		if (startCoord == 0) {
 			name = "";
 			return;
 		}
 		/*
 		 * Challenging cases:
 		 * 
 		 * gCATga => gga
 		 * A* => G
 		 * p.Ala#_*#delinsGlyext*#  (need to catch the readthrough and calculate the ext)
 		 * 
 		 * caacgatcg => caTTGacgatcg
 		 * QRS => H*RS
 		 * p.Gln#delinsH*
 		 * 
 		 * atgcgg => atCATgcgg
 		 * MR => ?MR
 		 * p.= (The insertion extends the UTR, but doesn't change the protein product)
 		 */
 		StringBuilder sb = new StringBuilder();
 		String protein = tx.getProtID();
 		if (protein != null && !protein.isEmpty()) {
 			sb.append(protein);
 			sb.append(":");
 		}
 		sb.append("p.(");
 		
 		
 		if (ref.equals(alt))
 			//Synonymous
 			sb.append("=");
 		else {
 			if (extension != null) {
 				ref.addAll(extension);
 				if (!isFrameshift) alt.addAll(extension);
 			}
 			//Find the region that actually changed
 			int leftFlank = 0;
 			int rightFlank = 0;
 			boolean isPotentialDuplicate = false;
 			while (leftFlank < ref.size() &&
 					leftFlank < alt.size() &&
 					ref.get(leftFlank) == alt.get(leftFlank)) {
 				leftFlank++;
 			}
 			while (rightFlank < ref.size() && 
 					rightFlank < alt.size() && 
 					ref.get(ref.size()-rightFlank-1) == alt.get(alt.size()-rightFlank-1)) {
 				rightFlank++;
 			}
 			//If a duplicating insertion is made (or a deletion from a rpt region),
 			//both leftFlank and rightFlank will go through the repeat region
 			//Ex:
 			//ABXXCD => ABXXXCD
 			//leftFlank and rightFlank will both be 4
 			if (leftFlank+rightFlank > ref.size() || leftFlank+rightFlank > alt.size()) {
 				rightFlank = Math.min(ref.size(), alt.size())-leftFlank;
 				if (alt.size() > ref.size()) isPotentialDuplicate = true;
 			}
 
 			//Now make a new list containing just the changes
 			//All of these manipulations are necessary because we need to know the flanking
 			//AAs for an insertion - otherwise, we could have just started with the change
 			List<AminoAcid> refAllele = new ArrayList<AminoAcid>(ref.subList(leftFlank, ref.size()-rightFlank));
 			List<AminoAcid> altAllele = new ArrayList<AminoAcid>(alt.subList(leftFlank, alt.size()-rightFlank));
 			String extType = "";
 			int extension = -1;
 			//Some of these special conditions can be caused by indels or substitutions,
 			//and we're worried about effect here, not cause.
 			//So identify some of the overarching conditions, and caress the extraneous data
 			//accordingly
 			if (!isFrameshift && refAllele.contains(AminoAcid.STOP)) {
 				//readthrough - everything after the old stop is unimportant, except that 
 				//we need to find the new stop
 				if (altAllele.size() > 1) {
 					//We'll only report the first new amino acid (altAllele might be empty here, so the if is important!)
 					altAllele.clear();
 					altAllele.add(alt.get(leftFlank));
 				}
 				extType = "ext";
 				extension = alt.subList(leftFlank, alt.size()).indexOf(AminoAcid.STOP);
 				if (extension > 0) extension++; //If we found it, make it one-based
 			} else if (!altAllele.isEmpty() && altAllele.get(0) == AminoAcid.STOP) {
 				//nonsense - this is reported simply as a substitution, so just make sure
 				//that it looks like a subst when we get to it later.  Importantly, if the first 
 				//AA modified by frameshift is a stop, don't report it as a fs
 				if (refAllele.size() > 1) {
 					//Analogous to the modification to altAllele in the readthrough case
 					refAllele.clear();
 					refAllele.add(ref.get(leftFlank));
 				}
 			} else if (!refAllele.isEmpty() && refAllele.get(0) == AminoAcid.MET && startCoord+leftFlank == 1) {
 				//start-loss
 				altAllele.clear();
 				if (refAllele.size() > 1)
 					refAllele.subList(1, refAllele.size()).clear();
 			} else if (isFrameshift) {
 				if (refAllele.size() > 1) {
 					refAllele.clear();
 					refAllele.add(ref.get(leftFlank));
 				}
 				if (altAllele.size() > 1) {
 					altAllele.clear();
 					altAllele.add(alt.get(leftFlank));
 				}
 				extType = "fs";
 				extension = alt.subList(leftFlank, alt.size()).indexOf(AminoAcid.STOP);
 				if (extension > 0) extension++; //If we found it, make it one-based
 			}
 			
 			//Now that the messy conditions have been cleaned up, it's pretty easy to describe the changes
 			if (refAllele.size() == 1 && altAllele.size() == 1) {
 				//substitution
 				sb.append(refAllele.get(0).abbrev());
 				sb.append(startCoord+leftFlank);
 				sb.append(altAllele.get(0).abbrev());
 			} else if (refAllele.isEmpty()) {
 				//straight insertion - special because the site is identified by flanks
 				if (startCoord+leftFlank == 1) {
 					//Special case: Insertions before the initial Met are not real
 					name = "";
 					return;
 				} else if (isPotentialDuplicate && 
 						ref.subList(leftFlank-altAllele.size(), leftFlank).equals(altAllele)) {
 					int dupLen = altAllele.size();
 					if (leftFlank <= dupLen)
 						sb.append("Unk");
 					else
 						sb.append(ref.get(leftFlank-dupLen).abbrev());
 					sb.append(startCoord+leftFlank-dupLen);
 					if (dupLen > 1) {
 						sb.append("_");
 						if (leftFlank == 0)
 							sb.append("Unk");
 						else
 							sb.append(ref.get(leftFlank-1).abbrev());
 						sb.append(startCoord+leftFlank-1);
 					}
 					sb.append("dup");
 				} else {
 					if (leftFlank == 0)
 						sb.append("Unk");
 					else
 						sb.append(ref.get(leftFlank-1).abbrev());
 					sb.append(startCoord+leftFlank-1);
 					sb.append("_");
 					if (rightFlank == 0)
 						sb.append("Unk");
 					else
 						sb.append(ref.get(ref.size()-rightFlank).abbrev());
 					sb.append(startCoord+leftFlank);
 					sb.append("ins");
 					for (AminoAcid aa : altAllele)
 						sb.append(aa.abbrev());
 				}
 			} else {
 				//Straight deletion or indel
 				if (startCoord+leftFlank == 1 && 
 						altAllele.isEmpty() && 
 						refAllele.get(0) == AminoAcid.MET) {
 					//Special case: Start loss (normalized to this form above)
 					sb.append("Met1?");
 				} else {
 					sb.append(refAllele.get(0).abbrev());
 					sb.append(startCoord+leftFlank);
 					if (refAllele.size() > 1) {
 						sb.append("_");
 						sb.append(refAllele.get(refAllele.size()-1).abbrev());
 						sb.append(startCoord+leftFlank+refAllele.size()-1);
 					}
 					sb.append("del");
 					if (!altAllele.isEmpty()) {
 						sb.append("ins");
 						for (AminoAcid aa : altAllele)
 							sb.append(aa.abbrev());						
 					}
 				}
 			}
 			if (!extType.equals("")) {
 				sb.append(extType);
 				sb.append("*");
 				if (extension > 0)
 					sb.append(extension);
 				else
 					sb.append("?");
 			}
 		}
 		
 		sb.append(")");
 		name = sb.toString();
 	}
 
 	private TranscriptAnnotation tx;
 	private String name;
 	private int startCoord;
 	private List<AminoAcid> ref;
 	private List<AminoAcid> alt;
 	private List<AminoAcid> extension;
 	private boolean isFrameshift;
 }
