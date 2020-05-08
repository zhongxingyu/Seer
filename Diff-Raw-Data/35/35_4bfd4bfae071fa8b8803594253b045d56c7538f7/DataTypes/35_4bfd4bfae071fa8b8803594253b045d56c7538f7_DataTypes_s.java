 package de.ptb.epics.eve.data;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.annotation.XmlEnumValue;
 
 /**
  * Defines some special data types, used inside of an EPICS environment. It also
  * provides some static methods which determine whether a value is allowed as a
  * given data type.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @version 1.4
  * @author Hartmut Scherr
  */
 public enum DataTypes {
 
 	// TODO correct Datatypes onoff and openclose
 	/**
 	 * Used to display On or Off for data of type integer.
 	 */
 	ONOFF,
 	/**
 	 * Used to display Open or Close for data of type integer.
 	 */
 	OPENCLOSE,
 	/**
 	 * Normal integer data type.
 	 */
 	@XmlEnumValue("int")
 	INT,
 	/**
 	 * Normal double data type.
 	 */
 	@XmlEnumValue("double")
 	DOUBLE,
 	/**
 	 * Normal String data type.
 	 */
 	STRING,
 
 	/**
 	 * DateTime may be an absolute date/time spec: yyyy-mm-dd hh:mm:ss.sss or an
 	 * absolute time without date hh:mm:ss.sss which assumes today as date or an
 	 * relative time hh:mm:ss.sss or an relative time ss.sss Examples:
 	 * 2009-10-01 17:09:20.000 (abs) valid absolute <code>DATETIME</code>
 	 * 2009-10-01 17:09:20.000 (rel) invalid relative <code>DATETIME</code>
 	 * 17:09:20.000 (abs) valid absolute <code>DATETIME</code> assuming date
 	 * today 17:09:20.000 (rel) valid relative time (duration of 1580 secs)
 	 */
 	@XmlEnumValue("datetime")
 	DATETIME;
 
 	/**
 	 * Determines whether a value is allowed for a given data type.
 	 * 
 	 * @param type
 	 *            the data type the value will be checked with
 	 * @param value
 	 *            the value to be checked
 	 * @return <code>true</code> if the value is valid, <code>false</code>
 	 *         otherwise
 	 * @throws IllegalArgumentException
 	 *             if at least one argument is <code>null</code>
 	 */
 	public static boolean isValuePossible(final DataTypes type,
 			final String value) {
 
 		if (type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		if (value == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'value' must not be null!");
 		}
 
 		switch (type) {
 
 		// TODO : ONOFF and OPENCLOSE always true ?
 		// TODO : Delete Comments ?
 		// case ONOFF: return ( value.toUpperCase().equals( "ON" ) ||
 		// value.toUpperCase().equals( "OFF" ) );
 		case ONOFF:
 			return true;
 
 			// case OPENCLOSE: return ( value.toUpperCase().equals( "OPEN" ) ||
 			// value.toUpperCase().equals( "CLOSE" ) );
 		case OPENCLOSE:
 			return true;
 
 		case INT:
 			try {
 				Integer.parseInt(value);
 			} catch (final NumberFormatException e) {
 				return false;
 			}
 			return true;
 
 		case DOUBLE:
 			try {
 				Double.parseDouble(value);
 			} catch (final NumberFormatException e) {
 				return false;
 			}
 			return true;
 
 		case STRING:
 			return true;
 		case DATETIME:
 			if (value
 					.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}([.]\\d{1,3})?$")) {
 				DateFormat formatDate = new SimpleDateFormat(
 						"yyyy-MM-dd HH:mm:ss.SSS");
 				try {
 					formatDate.setLenient(false);
 					formatDate.parse(value);
 					return true;
 				} catch (final ParseException e) {
 					return false;
 				}
 			} else if (value.matches("\\d+:\\d+:\\d+([.]\\d{1,3})?$")) {
 				DateFormat formatTime = DateFormat.getTimeInstance();
 				try {
 					formatTime.setLenient(false);
 					formatTime.parse(value);
 					return true;
 				} catch (final ParseException e) {
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	// TODO describe following function in more detail !
 	/**
 	 * Returns a string formatted to the corresponding DataTypes.
 	 * 
 	 * @param type
 	 *            ?????
 	 * @param value
 	 *            the <code>String</code> that should be formatted
 	 * @return a formatted <code>String</code> or <code>null</code>
 	 * @throws IllegalArgumentException
 	 *             if the argument is <code>null</code>
 	 */
 	public static String formatValue(final DataTypes type, final String value) {
 
 		if (value == null) {
 			throw new IllegalArgumentException(
 					"The parameter value must not be null");
 		}
 
 		if (!DataTypes.isValuePossible(type, value)) {
 			return null;
 		}
 
 		String returnString = null;
 
 		// TODO: first 3 if statements can be converted to one with ||
 		if (type == DataTypes.STRING) {
 			returnString = value;
 		} else if (type == DataTypes.ONOFF) {
 			returnString = value;
 		} else if (type == DataTypes.OPENCLOSE) {
 			returnString = value;
 		} else if (type == DataTypes.INT) {
 			Integer intval = Integer.parseInt(value.trim());
 			returnString = intval.toString();
 		} else if (type == DataTypes.DOUBLE) {
 			Double intval = Double.parseDouble(value.trim());
 			returnString = intval.toString();
 		} else if (type == DataTypes.DATETIME) {
 			if (value.contains("-")) {
 				// date included
 				if (value
 						.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}([.]\\d{1,3})?$")) {
 					returnString = value;
 				}
 			} else if (value.contains(":")) {
 				// no date included
 				if (value.matches("\\d+:\\d+:\\d+([.]\\d{1,3})?$")) {
 					returnString = value;
 				}
 				// TODO don't compile on every call
 				Pattern p = Pattern
 						.compile("(\\d+):(\\d+):(\\d+)([.]\\d{1,3})?$");
 				Matcher m = p.matcher(value);
 				m.find();
 				Integer hours = Integer.parseInt(m.group(1));
 				Integer minutes = Integer.parseInt(m.group(2));
 				Integer seconds = Integer.parseInt(m.group(3));
 				Double fractalSeconds = 0.0;
 				if (m.group(4) != null) {
 					fractalSeconds = Double.parseDouble(m.group(4));
 				}
 				if (seconds > 59) {
 					minutes += seconds / 60;
 					seconds %= 60;
 				}
 				if (minutes > 59) {
 					hours += minutes / 60;
 					minutes %= 60;
 				}
 				if (hours > 23) {
 					hours %= 24;
 				}
 				if (fractalSeconds > 0.0) {
 					fractalSeconds += seconds.doubleValue();
 					returnString = String.format(new Locale("us"),
 							"%1$02d:%2$02d:%3$06.3f", hours, minutes,
 							fractalSeconds);
 				} else {
 					returnString = String.format(new Locale("us"),
 							"%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
 				}
 			} else {
 				// we assume a double value as seconds
 				try {
 					Double dseconds = Double.parseDouble(value.trim());
 					Integer secs = dseconds.intValue();
 					int hours = secs / 3600;
 					int minutes = secs / 60 - hours * 60;
					dseconds -= new Integer(hours * 3600 + minutes * 60)
 							.doubleValue();
 					secs %= 60;
 					if (dseconds > secs.doubleValue()) {
 						returnString = String.format(new Locale("us"),
 								"%1$02d:%2$02d:%3$06.3f", hours, minutes,
 								dseconds);
 					} else {
 						returnString = String.format(new Locale("us"),
 								"%1$02d:%2$02d:%3$02d", hours, minutes, secs);
 					}
 				} catch (final NumberFormatException e) {
 					returnString = null;
 				}
 			}
 		}
 		return returnString;
 	}
 
 	// TODO describe following function in more detail !
 	/**
 	 * Convenience function returns a formatted <code>String</code> or default
 	 * value.
 	 * 
 	 * @param value
 	 *            the <code>String</code> that should be formatted
 	 * @param type
 	 *            the data type of value
 	 * @return a formatted <code>String</code> or <code>null</code>
 	 */
 	public static String formatValueDefault(final DataTypes type,
 			final String value) {
 
 		String returnString = formatValue(type, value);
 		if (returnString != null) {
 			return returnString;
 		} else {
 			return getDefaultValue(type);
 		}
 	}
 
 	/**
 	 * Returns a well-formatted <code>String</code> with a valid default value.
 	 * 
 	 * @param type
 	 *            the data type of which a default value is returned
 	 * @return a default value
 	 */
 	public static String getDefaultValue(final DataTypes type) {
 
 		switch (type) {
 
 		case ONOFF:
 			return "OFF";
 		case OPENCLOSE:
 			return "CLOSE";
 		case INT:
			return new Integer(0).toString();
 		case DOUBLE:
			return new Double(0.0).toString();
 		case STRING:
 			return "<unknown>";
 		case DATETIME:
 			return "00:00:00";
 		}
 		return "";
 	}
 
 	/**
 	 * Converts a name (<code>String</code>) of a data type into its
 	 * corresponding type (<code>DataTypes</codes>).
 	 * 
 	 * @param name
 	 *            the <code>String</code> that should be converted<br>
 	 *            <b>Precondition:</b> name is element of {"OpenClose", "Off",
 	 *            "int", "double", "string"}
 	 * @return the corresponding data type
 	 * @throws IllegalArgumentException
 	 *             if the argument is <code>null</code>
 	 */
 	public static DataTypes stringToType(final String name) {
 		if (name == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'name' must not be null!");
 		}
 
 		if (name.equals("OnOff")) {
 			return DataTypes.ONOFF;
 		} else if (name.equals("OpenClose")) {
 			return DataTypes.OPENCLOSE;
 		} else if (name.equals("int")) {
 			return DataTypes.INT;
 		} else if (name.equals("double")) {
 			return DataTypes.DOUBLE;
 		} else if (name.equals("string")) {
 			return DataTypes.STRING;
 		} else if (name.equals("datetime")) {
 			return DataTypes.DATETIME;
 		}
 		return null;
 	}
 
 	/**
 	 * Converts a type (<code>DataTypes</code>) into a <code>String<code>.
 	 * 
 	 * @param type
 	 *            the type, that should be converted.
 	 * @return the corresponding <code>String</code>
 	 * @exception IllegalArgumentException
 	 *                if the argument is <code>null</code>
 	 */
 	public static String typeToString(final DataTypes type) {
 
 		if (type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		switch (type) {
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
 		case DATETIME:
 			return "datetime";
 		}
 		return null;
 	}
 
 	/**
 	 * Returns an array of <code>ComparisonTypes</code> that are allowed for a
 	 * given data type.
 	 * 
 	 * @see de.ptb.epics.eve.data.ComparisonTypes
 	 * @param type
 	 *            the data type of which you want to get the allowed
 	 *            <code>ComparisonTypes</code>.
 	 * @return an array containing the allowed <code>comparisonTypes</code>.
 	 * @throws IllegalArgumentException
 	 *             if the argument is <code>null</code>
 	 */
 	public static ComparisonTypes[] getPossibleComparisonTypes(
 			final DataTypes type) {
 		if (type == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'type' must not be null!");
 		}
 		switch (type) {
 		case ONOFF:
 		case OPENCLOSE:
 		case STRING:
 			return new ComparisonTypes[] { ComparisonTypes.EQ,
 					ComparisonTypes.NE };
 		case DATETIME:
 		case INT:
 		case DOUBLE:
 			return new ComparisonTypes[] { ComparisonTypes.EQ,
 					ComparisonTypes.NE, ComparisonTypes.GT, ComparisonTypes.LT };
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if a comparison type is allowed for a given data type. E.g. EQ and
 	 * NE are working for a string but GT and LT are not.
 	 * 
 	 * @param dataType
 	 *            the data type to be checked
 	 * @param comparisonType
 	 *            the comparison type that should be checked.
 	 * @return <code>true</code> if the type is allowed, <code>false</code>
 	 *         otherwise
 	 */
 	public static boolean isComparisonTypePossible(final DataTypes dataType,
 			final ComparisonTypes comparisonType) {
 		ComparisonTypes[] comparisonTypes = DataTypes
 				.getPossibleComparisonTypes(dataType);
 		for (int i = 0; i < comparisonTypes.length; ++i) {
 			if (comparisonTypes[i] == comparisonType) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
