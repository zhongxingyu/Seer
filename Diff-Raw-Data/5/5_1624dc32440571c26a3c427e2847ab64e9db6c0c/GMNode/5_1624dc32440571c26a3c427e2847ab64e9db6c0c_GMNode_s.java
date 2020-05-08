 package cz.fjfi.guideme.core;
 import java.util.UUID;
 import java.util.List;
 
 /**
  * This class represents node in map of GuideMe app. It provides getters and 
  * setters for its attributes.
  * 
  * @author 
  * @version
  */
 public class GMNode
 {
 //== CLASS CONSTANTS ===========================================================
 //== CLASS VARIABLES ===========================================================
 //== INSTANCE VARIABLES ========================================================
 private UUID  guid;
 private String name;
 private List<Location> locations;
 private String description;
 private GMMap myMap;
 	
 //==============================================================================
 //== CONSTRUCTORS ==============================================================
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: constructor parameters and body
     public GMNode(UUID guid, String name, List<Location> locations, String description,  GMMap myMap)
 	{
 		this.guid = guid;
 		this.name = name;
 		this.locations = locations;
 		this.description = description;
 		this.myMap = myMap;
 	}
     
 //== GETTERS AND SETTERS =======================================================
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final UUID getGUID()
     {
     	return this.guid;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final String getName()
     {
     	return this.name;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final List<Location> getLocations()
     {
     	return this.locations;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final String getDescription()
     {
     	return this.description;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     protected final GMMap getMyMap()
     {
     	return this.myMap;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final void setGUID(UUID newGUID)
     {
     	this.guid = newGUID;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final void setName(String newName)
     {
     	this.name = newName;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final void setLocations(List<Location> newLocations)
     {
     	this.locations = newLocations;
     }
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: body
     public final void setDescription(String newDescription)
     {
     	this.description = newDescription;
     }
     
 //== OTHER METHODS ============================================================= 
     public String exportXML()
     {
     	String output = new String("<node guid=\"" + this.getGUID() + "\">\n"
    								+ "<name>" + this.getName() + "<\name>\n" 
     								+ "<locs>\n");
     	for(Location loc:this.locations)
     	{
     		output = output.concat("<loc guid=\"" + loc.getGUID() + "\" />\n");
     	}
     	output = output.concat("</locs>\n"
    							+ "<name>" + this.getName() + "<\name>\n"
     							+ "<desc>" + this.getDescription() + "</desc>\n" 
     							+ "</node>\n");
     	return output;
     }
 }
