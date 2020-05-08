 /**
  * 
  */
 package ecologylab.xml.library.kml.geometry;
 
 import ecologylab.serialization.Hint;
 import ecologylab.serialization.simpl_inherit;
 import ecologylab.xml.library.kml.KmlObject;
 
 /**
  * @author Zach
  *
  */
 @simpl_inherit
 public abstract class Geometry extends KmlObject
 {
 	@simpl_scalar @simpl_hints(Hint.XML_LEAF) protected Coordinates	coordinates	= new Coordinates();
 
 	/**
 	 * 
 	 */
 	public Geometry()
 	{
 	}
 	
 	public Geometry(Coordinates coordinates)
 	{
 		this.setCoordinates(coordinates);
 	}
 
	@Override protected void deserializationPostHook()
 	{
		super.deserializationPostHook();
 		
		this.coordinates.deserializationPostHook();
 	}
 
	@Override protected void serializationPreHook()
 	{
		super.serializationPreHook();
 		
		this.coordinates.serializationPreHook();
 	}
 	
 	public void setCoordinates(Coordinates coordinates)
 	{
 		this.coordinates = coordinates;
 	}
 
 	public Coordinates getCoordinates()
 	{
 		return this.coordinates;
 	}
 }
