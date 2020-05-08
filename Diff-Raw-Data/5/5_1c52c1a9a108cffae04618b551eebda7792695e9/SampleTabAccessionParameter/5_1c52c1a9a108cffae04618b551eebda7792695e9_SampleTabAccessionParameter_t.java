 package uk.ac.ebi.fgpt.conan.process.biosd;
 
 import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;
 
 public class SampleTabAccessionParameter extends AbstractConanParameter {
 
 	private String accession;
 	
 	protected SampleTabAccessionParameter() {
         super("SampleTab Accession");
         this.accession = null;
     }
 	
 	public void setAccession(String accession){
 		//TODO validate
 		this.accession = accession;
 	}
 	
 	public String getAccession(){
 		return this.accession;
 	}
 
	public boolean testIfMageTabAccession() {
 		String regex = "GAE-[A-Z]+-[0-9]+";
 		if (getAccession().matches(regex)){
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String getMageTabAccession() {
		if (!testIfMageTabAccession()){
 			return null;
 		} else {
 			return getAccession().substring(2);
 		}
 	}
 }
