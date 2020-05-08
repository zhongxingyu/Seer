 package eu.europeana.uim.repoxclient.utils;
 
 /**
  * Repox DataSet Types (Provider Attributes)
  * 
  * @author Georgios Markakis
  */
 
 public enum DataSetType {
 	MUSEUM("Museum/Gallery"),
 	ARCHIVE("Archive"),
 	LIBRARY("Library"),
 	AUDIO_VISUAL_ARCHIVE("Audio Visual"),
 	RESEARCH_EDUCATIONAL("Research and Educational"),
 	CROSS_SECTOR("Cross-sector portal"),
 	PUBLISHER("Publisher"),
 	PRIVATE("Private"),
 	AGGREGATOR("Aggregator"),
	UNKNOWN("Unknown");
 	
 	private String sugarName;
 	
 	DataSetType(String sugarName){
 	   this.sugarName = sugarName;
 	}
 
 	/**
 	 * @param sugarName the sugarName to set
 	 */
 	public void setSugarName(String sugarName) {
 		this.sugarName = sugarName;
 	}
 
 	/**
 	 * @return the sugarName
 	 */
 	public String getSugarName() {
 		return sugarName;
 	}
 	
 
 }
