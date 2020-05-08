 package de.unihamburg.zbh.fishoracle.client.data;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class Gen implements IsSerializable{
 
 	
 	private String accessionID = null;
 	private String bioType = null;
 	private String description = null;
 	private String genName = null;
 	private String chr = null;
 	private int start;
 	private int end;
 	private int length;
 	private String strand = null;
 	
 	public Gen() {
 		
 	}
 
 	public Gen(String genName, String chr, int start, int end, String strand) {
 		this.genName = genName;
 		this.chr = chr;
 		this.start = start;
 		this.end = end;
 		if(strand.equals("-1") || strand.equals("1") || strand.equals("0")){
 			
 			this.strand = this.convertEnsembl2GFF3Strand(strand);
 			
 		} else {
 			
 			this.strand = strand;
 		}
 	}
 
 	public String getGenName() {
 		return genName;
 	}
 
 
 	public void setGenName(String genName) {
 		this.genName = genName;
 	}
 
 	
 	public String getChr() {
 		return chr;
 	}
 
 
 	public void setChr(String chr) {
 		this.chr = chr;
 	}
 
 
 	public int getStart() {
 		return start;
 	}
 
 
 	public void setStart(int start) {
 		this.start = start;
 	}
 
 
 	public int getEnd() {
 		return end;
 	}
 
 
 	public void setEnd(int end) {
 		this.end = end;
 	}
 
 
 	public String getStrand() {
 		return strand;
 	}
 
 
 	public void setStrand(String strand) {
 		if(strand.equals("-1") || strand.equals("1") || strand.equals("0")){
 			
 			this.strand = this.convertEnsembl2GFF3Strand(strand);
 			
 		} else {
 			
 			this.strand = strand;
 		}
 		
 	}
 	
 	
 	public String getAccessionID() {
 		return accessionID;
 	}
 
 	public void setAccessionID(String accessionID) {
 		this.accessionID = accessionID;
 	}
 
 	public String getBioType() {
 		return bioType;
 	}
 
 	public void setBioType(String bioType) {
 		this.bioType = bioType;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public void setLength(int length) {
 		this.length = length;
 	}
 
	public String convertEnsembl2GFF3Strand(String strand2){
 		String ret = null;
		if(strand2.equals("1")){
 			ret = "+";
		} else if(strand2.equals("-1")) {
 			ret = "-";
		} else if (strand2.equals("0")){
 			ret = ".";
 		}
 		return ret;
 	}
 
 	public int convertGFF32EnsemblStrand(String strand){
 		int ret = 0;
 		if(strand.equals("+")){
 			ret = 1;
 		} else if(strand.equals("-")) {
 			ret = -1;
 		} else if(strand.equals(".")){
 			ret = 0;
 		}
 		return ret;
 	}
 }
