 package net.thegreshams.openstates4j.model;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.annotate.JsonAnySetter;
 
 /**
  * Base
  * 
  * @author Brandon Gresham <brandon@thegreshams.net>
  */
 public abstract class Base implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	protected static final Logger LOGGER = Logger.getRootLogger();
 	
 
 	/**
 	 * Optional Properties
 	 */
 	public final Map<String, Object> optionalProperties = new HashMap<String, Object>();
 
 	
 	
 	/**
 	 * Catch-all method Jackson calls whenever it finds a property it doesn't
 	 * know how to map.  OpenStates marks their optional-properties with "+",
 	 * this strips off the "+" and pushes the key/value into the map.  Also,
 	 * any property that gets caught here (whether it starts with a "+" or not)
 	 * will be pushed into the map.
 	 * 
 	 * If the key is null or empty, it is discarded.
 	 * If the value is null, it is discarded.
 	 * If the key already exists, it is overwritten with the new value.
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	@JsonAnySetter 
 	protected void anySetter( String key, Object value ) {
 
 		// sanity-checks
 		if( key == null || key.trim().isEmpty() ) {
			LOGGER.warn( "key was null/empty; discarding" );
 			return;
 		}
 		key = key.trim();
 		
 		// sanity-checks
 		if( value == null ) {
			LOGGER.warn( "value was null for key (" + key + "); discarding" );
 			return;
 		}
 		
 		// optional property
 		if( key.startsWith( "+" ) ) {	// OpenStates.org API states that optional-properties are prepended with '+'
 			key = key.replaceFirst( "\\+", "" );
 			if( key.trim().isEmpty() ) {
				LOGGER.warn( "trimmed '+' from key but now it's empty; discarding" );
 				return;
 			}
 		}
 		
 		// double-check whether the key already exists or not
 		if( this.optionalProperties.containsKey( key ) ) {
			LOGGER.warn( "replacing value in optional-properties for key (" + key + ")" );
 		}
 		
 		this.optionalProperties.put( key, value );
 	}
 	
 }
