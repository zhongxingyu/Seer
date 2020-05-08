 package org.geworkbench.bison.datastructure.bioobjects;
 
 import java.io.Serializable;
 
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 
 /**
  * Edge data structure of IDEA Analysis.
  * 
  * @author zm2165 $Id$
  */
 public class IdeaEdge implements Serializable, Comparable<IdeaEdge> {
 	// Edge is the gene pair which is expanded on probe Ids. For each edge, the
 	// geneNo1 and geneNo2 which is entrez gene number
 	// may be the same, but probe Ids which is expRowNoG1 and expRowNoG2 are
 	// unique.
 
 	private static final long serialVersionUID = -1626720585421975791L;
 
 	private int geneNo1; // No1 of Entrez gene number used in network.txt
 	private int geneNo2; // No2 of Entrez gene pairs
 	private int expRowNoG1; // Row number of gene1 in gene expression file
 	private int expRowNoG2; // Row number of gene2 in gene expression file
 	private String probeId1;
 	private String probeId2;
 	private DSGeneMarker marker1 = null;
 	private DSGeneMarker marker2 = null;
 
 	public enum InteractionType {
 		PROTEIN_PROTEIN, PROTEIN_DNA
 	};
 
 	private InteractionType ppi;
 	private double[] nullDeltaCorr;
 
 	private double MI; // mutual information of the edge
 	private double deltaCorr;
 	private double normCorr;
 	private double zDeltaCorr;
 	private double tempNorm; // should be removed after test
 	private boolean loc;
 	private boolean goc;
 
 	public IdeaEdge(int geneNo1, int geneNo2, DSGeneMarker marker1,
 			DSGeneMarker marker2, int expRowNoG1, int expRowNoG2,
 			String probeId1, String probeId2, InteractionType ppi) {
 		this.geneNo1 = geneNo1;
 		this.geneNo2 = geneNo2;
 		this.expRowNoG1 = expRowNoG1;
 		this.expRowNoG2 = expRowNoG2;
 		this.probeId1 = probeId1;
 		this.probeId2 = probeId2;
 		this.ppi = ppi;
 		this.marker1 = marker1;
 		this.marker2 = marker2;
 	}
 
 	public int getGeneNo1() {
 		return geneNo1;
 	}
 
 	public int getGeneNo2() {
 		return geneNo2;
 	}
 
 	public int getExpRowNoG1() {
 		return expRowNoG1;
 	}
 
 	public int getExpRowNoG2() {
 		return expRowNoG2;
 	}
 
 	public void setMI(double MI) {
 		this.MI = MI;
 	}
 
 	public double getMI() {
 		return MI;
 	}
 
 	public void setDeltaCorr(double deltaCorr) {
 		this.deltaCorr = deltaCorr;
 	}
 
 	public double getDeltaCorr() {
 		return deltaCorr;
 	}
 
 	public void setNullData(double[] nullData) {
 		nullDeltaCorr = nullData;
 	}
 
 	public double[] getNullData() {
 		return nullDeltaCorr;
 	}
 
 	public void setNormCorr(double normCorr) {
 		this.normCorr = normCorr;
 	}
 
 	public double getNormCorr() {
 		return normCorr;
 	}
 
 	public double getTempNorm() {
 		return tempNorm;
 	}
 
 	public void setLoc(boolean loc) {
 		this.loc = loc;
 	}
 
 	public boolean isLoc() {
 		return loc;
 	}
 
 	public void setGoc(boolean goc) {
 		this.goc = goc;
 	}
 
 	public boolean isGoc() {
 		return goc;
 	}
 
 	public int compareTo(IdeaEdge otherEdge) {
 		double d = zDeltaCorr - otherEdge.getzDeltaCorr();
 		if(d<0) return -1;
 		else if (d>0) return 1;
 		else return 0;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		IdeaEdge o=(IdeaEdge) obj;
 		String m1=this.getMarker1().getLabel();
 		String m2=this.getMarker2().getLabel();
 		String o1=o.getMarker1().getLabel();
 		String o2=o.getMarker2().getLabel();
 
 		if((m1.equals(o1)&&m2.equals(o2))||((m1.equals(o2))&&(m2.equals(o1))))
 			return true;
 		else
			return false;
		
		
 	}
 	
 
 	public void setzDeltaCorr(double zDeltaCorr) {
 		this.zDeltaCorr = zDeltaCorr;
 	}
 
 	public double getzDeltaCorr() {
 		return zDeltaCorr;
 	}
 
 	public void setProbeId1(String probeId1) {
 		this.probeId1 = probeId1;
 	}
 
 	public String getProbeId1() {
 		return probeId1;
 	}
 
 	public void setProbeId2(String probeId2) {
 		this.probeId2 = probeId2;
 	}
 
 	public String getProbeId2() {
 		return probeId2;
 	}
 
 	public InteractionType getPpi() {
 		return ppi;
 	}
 
 	public void setMarker1(DSGeneMarker marker1) {
 		this.marker1 = marker1;
 	}
 
 	public DSGeneMarker getMarker1() {
 		return marker1;
 	}
 
 	public void setMarker2(DSGeneMarker marker2) {
 		this.marker2 = marker2;
 	}
 
 	public DSGeneMarker getMarker2() {
 		return marker2;
 	}
 
 }
