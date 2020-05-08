 package org.fao.fi.vme.geobatch;
 
 import it.geosolutions.geobatch.actions.ds2ds.Ds2dsConfiguration;
 
 /**
  * 
  *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
  *        emmanuel.blondel@fao.org
  *
  */
 public class VMEZonalStatsConfiguration extends Ds2dsConfiguration{
 	
 
 	private String coverage;
 	private String geoIdentifier;
 
 	private String geoserverURL;
 	private String workspace;
 
 	/**
 	 * @param id
 	 * @param name
 	 * @param description
 	 */
 	public VMEZonalStatsConfiguration(String id, String name,
 			String description) {
 		super(id, name, description);
 	}
 	
 	/**
 	 * Set coverage
 	 * 
 	 * @param coverage
 	 */
 	public void setCoverage(String coverage){
 		this.coverage = coverage;
 	}
 	
 	/**
 	 * Get coverage
 	 * 
 	 * @return
 	 */
 	public String getCoverage(){
 		return this.coverage;
 	}
 	
 	/**
 	 * Set the geoIdentifier (attribute that maps the feature collection
 	 * and the zonal statistics)
 	 * 
 	 * @param geoIdentifier
 	 */
 	public void setGeoIdentifier(String geoIdentifier){
 		this.geoIdentifier = geoIdentifier;
 	}
 
 	/**
 	 * Get the geoIdentifier
 	 * 
 	 * @return 
 	 */
 	public String getGeoIdentifier(){
 		return this.geoIdentifier;
 	}
 	
 	public String getGeoserverURL(){
 		return this.geoserverURL;
 	}
 
 	public void setGeoserverURL(String geoserverURL){
 		this.geoserverURL = geoserverURL;
 	}
 
 	public String getWorkspace(){
 		return this.workspace;
 	}
 
 	public void setWorkspace(String workspace){
 		this.workspace = workspace;
 	}
 
 	
 	@Override
     public VMEZonalStatsConfiguration clone() { 
         final VMEZonalStatsConfiguration configuration = (VMEZonalStatsConfiguration) super
                 .clone();
        
        configuration.setGeoserverURL(geoserverURL);
        configuration.setWorkspace(workspace);
         configuration.setCoverage(coverage);
         configuration.setGeoIdentifier(geoIdentifier);
         return configuration;
     }
 	
 	
 
 }
