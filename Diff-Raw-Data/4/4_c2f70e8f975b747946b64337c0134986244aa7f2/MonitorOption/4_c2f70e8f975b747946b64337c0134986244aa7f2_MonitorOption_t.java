 package de.ptb.epics.eve.data.scandescription;
 
 import javax.xml.bind.annotation.XmlEnumValue;
 
 /**
  * <code>MonitorOption</code> represents the available types of monitor options in 
  * the scan.
  * 
  * @author Hartmut Scherr
  */
 public enum MonitorOption {
 	
 	/**
 	 * All Options of the devices which are used in the scan.
 	 * with the setting monitor="true"
 	 */
 	@XmlEnumValue("used in scan")
 	USED_IN_SCAN,
 
 	/**
 	 * No Options are monitored.
 	 */
 	@XmlEnumValue("none")
 	NONE,
 
 	/**
 	 * The Options are editable.
 	 */
 	@XmlEnumValue("custom")
 	CUSTOM,
 
 	/**
 	 * All Options of the measurement.xml File will be monitored
 	 * with the setting monitor="true"
 	 */
 	@XmlEnumValue("as in device definition")
 	AS_IN_DEVICE_DEFINITION;
 
 	/**
 	 * Converts a <code>MonitorOption</code> to a {@link java.lang.String}.
 	 * 
 	 * @param monitorOption the monitor Option that should be converted
 	 * @return the <code>String</code> corresponding to the monitor Option
 	 */
 	public static String typeToString(final MonitorOption monitorOption) {
 		switch(monitorOption) {
 			case AS_IN_DEVICE_DEFINITION:
 				return "as in device definition";
 			case CUSTOM:
 				return "custom";
 			case NONE:
 				return "none";
 			case USED_IN_SCAN:
 				return "used in scan";
 		}
 		return null;
 	}
 	
 	/**
 	 * Converts a {@link java.lang.String} to its corresponding 
 	 * {@link de.ptb.epics.eve.data.scandescription.MonitorOption}
 	 * 
 	 * @param name the {@link java.lang.String} that should be converted
 	 * @return The corresponding
 	 * 		   {@link de.ptb.epics.eve.data.scandescription.MonitorOption}
 	 * @throws IllegalArgumentException if the argument is <code>null</code> 
 	 */
 	public static MonitorOption stringToType(final String name) {
 		if(name == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'name' must not be null!");
 		}
 		
 		if( name.equals("as in device definition")) {
 			return MonitorOption.AS_IN_DEVICE_DEFINITION;
 		}
 		if( name.equals("custom")) {
 			return MonitorOption.CUSTOM;
 		}
 		if( name.equals("none")) {
 			return MonitorOption.NONE;
 		}
 		if( name.equals("used in scan")) {
 			return MonitorOption.USED_IN_SCAN;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns all available monitor options.
 	 * 
 	 * @return all available monitor options
 	 */
 	public static String[] getPossibleMonitorOptions() {
		return new String[] { "none", "used in scan",
				"as in device definition", "custom" };
 	}
 }
