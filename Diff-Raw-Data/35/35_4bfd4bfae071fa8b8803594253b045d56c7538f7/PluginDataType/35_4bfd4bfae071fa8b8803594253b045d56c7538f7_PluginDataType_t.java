 package de.ptb.epics.eve.data;
 
 /**
  * This enum represents the data types for plug in variables. They are similar 
  * to the data types that are defined in the enum DataTypes. 
  * Additionally this enum has the types AXISID, CHANNELID and DEVIDEID that 
  * indicate, that the plug in parameter is a channel, axis or device.
  * 
  * @author Stephan Rehfeld
  */
 public enum PluginDataType {
 	
 	/**
 	 * On/Off data type is used at some Devices to switch them on or off.
 	 */
 	ONOFF,
 	
 	/**
 	 * Open/Close data type is used to open or close a device 
 	 * e.g. a beam shutter.
 	 */
 	OPENCLOSE,
 	
 	/**
 	 * Normal integer data type.
 	 */
 	INT, 
 	
 	/**
 	 * Normal double data type.
 	 */
 	DOUBLE,
 	
 	/**
 	 * Normal String data type.
 	 */
 	STRING,
 	
 	/**
 	 * A String that represents an identifier of an axis.
 	 */
 	AXISID,
 	
 	/**
 	 * A String that represents an identifier of an channel.
 	 */
 	CHANNELID,
 	
 	/**
 	 * A String that represents an identifier of an device.
 	 */
 	DEVICEID;
 	
 	/**
 	 * Checks whether a value is valid as a given data type.
 	 * 
 	 * @param type the type the value should be checked with
 	 * @param value the value that should be checked
 	 * @return <code>true</code> if the value is valid, 
 	 * 			<code>false</code> otherwise
 	 * @throws IllegalArgumentException if at least one argument is 
 	 * 									<code>null</code>
 	 */
 	public static boolean isValuePossible(final PluginDataType type, 
 											final String value) {
 		
 		if(type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		if(value == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'value' must not be null!");
 		}	
 		
 		switch(type) {
 		
 		case ONOFF:		return (value.toUpperCase().equals("ON") || 
 								 value.toUpperCase().equals("OFF"));
 		
 		case OPENCLOSE:	return (value.toUpperCase().equals("OPEN") || 
 								 value.toUpperCase().equals("CLOSE"));
 		
 		case INT:		try {
 							Integer.parseInt(value);
 						} catch(final NumberFormatException e) {
 							return false;
 						}
 						return true;
 						
 		case DOUBLE:	try {
 							Double.parseDouble(value);
 						} catch(final NumberFormatException e) {
 							return false;
 						}
 						return true; 
 			
 		case STRING:
 		case AXISID:
 		case CHANNELID:
 		case DEVICEID:
 				return true;		
 		}		
 		return false;
 	}
 	
 	/**
 	 * Converts a <code>String</code> into its corresponding 
 	 * <code>PluginDataType</code>.
 	 * 
 	 * @param name the <code>String</code> that should be converted<br>
 	 * 				<b>Precondition:</b> name is element of {"OnOff", 
 	 * 									"OpenClose", "int", "double", "string", 
 	 * 									"axisid", "channelid", "deviceid"}
 	 * @return The corresponding <code>PluginDataType</code>.
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public static PluginDataType stringToType(final String name) {
 		if(name == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'name' must not be null!");
 		}
 		
 		if( name.equals( "OnOff" ) ) {
 			return PluginDataType.ONOFF;
 		} else if( name.equals( "OpenClose" ) ) {
 			return PluginDataType.OPENCLOSE;
 		} else if( name.equals( "int" ) ) {
 			return PluginDataType.INT;
 		} else if( name.equals( "double" ) ) {
 			return PluginDataType.DOUBLE;
 		} else if( name.equals( "string" ) ) {
 			return PluginDataType.STRING;
 		} else if( name.equals( "axisid" ) ) {
 			return PluginDataType.AXISID;
 		} else if( name.equals( "channelid" ) ) {
 			return PluginDataType.CHANNELID;
 		} else if( name.equals( "deviceid" ) ) {
 			return PluginDataType.DEVICEID;
 		} 
 		return null;	
 	}
 
 	/**
 	 * Converts a <code>PluginDataType</code> into a <code>String</code> 
 	 * with a valid default value.
 	 * 
 	 * @param type the data type that should be converted.
 	 * @return a <code>String</code> with a valid default value
 	 */
 	public static String getDefaultValue(final PluginDataType type){
 		
 		switch( type ) {
 		
 		case ONOFF:		return "OFF";
 		case OPENCLOSE:	return "CLOSE";		
		case INT:		return "0";
		case DOUBLE:	return "0.0";
 		case STRING:	return "<unknown>";
 		case AXISID:	return "";
 		case CHANNELID:	return "";
 		case DEVICEID:	return "";
 		}
 		return "";
 	}
 	
 	/**
 	 * Converts a <code>PluginDataType</code> into a <code>String</code>. 
 	 * 
 	 * @param type the type, that should be converted<br>
 	 * @return the corresponding <code>String</code>
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public static String typeToString(final PluginDataType type) {
 		
 		if(type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		switch( type ) {
 			case ONOFF:
 				return "OnOff";
 			case OPENCLOSE:
 				return "OpenClose";
 			case INT:
 				return "int";
 			case DOUBLE:
 				return "double";
 			case STRING:
 				return "string";
 			case AXISID:
 				return "axisid";
 			case CHANNELID:
 				return "channelid";
 			case DEVICEID:
 				return "deviceid";					
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns an array of <code>ComparisonTypes</code> that are valid for the 
 	 * given <code>PluginDataType</code>
 	 * 
 	 * @see de.ptb.epics.eve.data.ComparisonTypes
 	 * @param type the <code>PluginDataType</code> of interest
 	 * @return an array containing the valid <code>ComparisonTypes</code>
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public static ComparisonTypes[] getPossibleComparisonTypes(
 										final PluginDataType type) {
 		if(type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		switch(type) {
 			case ONOFF:
 			case OPENCLOSE:
 			case STRING:
 			case AXISID:
 			case CHANNELID:
 			case DEVICEID:
 				return new ComparisonTypes[]
 				                   {ComparisonTypes.EQ, ComparisonTypes.NE};
 			case INT:
 			case DOUBLE:
 				return new ComparisonTypes[]
 				                   {ComparisonTypes.EQ, ComparisonTypes.NE, 
 									ComparisonTypes.GT, ComparisonTypes.LT};
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if a comparison type (<code>ComparisonTypes</code>) is valid as a 
 	 * <code>PluginDataType</code>.
 	 * E.g. EQ and NE are working for a string but GT and LT are not.
 	 * 
 	 * @param dataType the <code>PluginDataType</code>
 	 * @param comparisonType the comparison type that should be checked.
 	 * @return <code>true</code> if type is valid, <code>false</code> otherwise
 	 */
 	public static boolean isComparisonTypePossible(
 								final PluginDataType dataType, 
 								final ComparisonTypes comparisonType) {
 		ComparisonTypes[] comparisonTypes = 
 				PluginDataType.getPossibleComparisonTypes(dataType);
 		
 		for(int i=0; i<comparisonTypes.length; ++i) {
 			if(comparisonTypes[i] == comparisonType) {
 				return true;
 			}
 		}
 		return false;
 	}	
 }
