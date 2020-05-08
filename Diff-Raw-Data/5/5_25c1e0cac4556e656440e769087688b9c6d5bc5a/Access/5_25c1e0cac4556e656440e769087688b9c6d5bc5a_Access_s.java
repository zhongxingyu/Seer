 /*******************************************************************************
  * Copyright (c) 2001, 2007 Physikalisch Technische Bundesanstalt.
  * All rights reserved.
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package de.ptb.epics.eve.data.measuringstation;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.MethodTypes;
 import de.ptb.epics.eve.data.TransportTypes;
 
 /**
  * This class representes a access description through a mediated layer like EPICS.
  *
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @version 1.2
  */
 public class Access {
 	
 	/**
 	 * The method type of this access.
 	 */
 	private MethodTypes method;
 	
 	/**
 	 * The datatype of this access.
 	 */
 	private DataTypes type;
 	
 	/**
 	 * If this access leads to a array type of data, the array size is in this attribute.
 	 */
 	private int count;
 	
 	/**
 	 * The variable id of this access.
 	 */
 	private String variableID;
 	
 	/**
 	 * The transport type.
 	 */
 	private TransportTypes transport;
 	
 	/**
 	 * The timeout of the access.
 	 */
 	private double timeout;
 	
 	/**
 	 * may be monitored.
 	 */
 	private boolean monitor;
 	
 	/**
 	 * This constructor constructs an new Access object with the given method type.
 	 * 
 	 * @see de.trustedcode.scanmoduleditor.data.MethodTypes
 	 * @param method A value of MethodTypes. Must not be null.
 	 */
 	public Access( final MethodTypes method ) {
 		this( "", null, 0, method, null, 0.0 );
 	}
 	
 	/**
 	 * This constructor construct a new Access with very specific value.
 	 * 
 	 * @see de.trustedcode.scanmoduleditor.data.MethodTypes
 	 * @see de.trustedcode.scanmoduleditor.data.DataTypes 
 	 * @param variableID
 	 * @param type
 	 * @param count
 	 * @param method
 	 */
 	public Access( final String variableID, final DataTypes type, final int count, final MethodTypes method, final TransportTypes transport, final double timeout ) {
 		if( variableID == null ) {
 			throw new IllegalArgumentException( "The parameter 'variableID' must not be null!" );
 		}
 		if( method == null ) {
 			throw new IllegalArgumentException( "The parameter 'method' must not be null!" );
 		}
 		this.variableID = variableID;
 		this.type = type;
 		this.count = count;
 		this.method = method;
 		this.transport = transport;
 		this.timeout = timeout;
 	}
 	
 	/**
 	 * Gives back the method type of this access.
 	 * 
 	 * @return A value of MethodTypes. Never returns null!
 	 */
 	public MethodTypes getMethod() {
 		return this.method;
 	}
 	
 	/**
 	 * Gives back the data type of this access.
 	 * 
 	 * @return A value of DataTypes or null.
 	 */
 	public DataTypes getType() {
 		return this.type;
 	}
 	
 	/**
 	 * Gives back the count of the access.
 	 * 
 	 * @return 0 or a positive integer.
 	 */
 	public int getCount() {
 		return this.count;
 	}
 	
 	/**
 	 * Gives back the id of the access.
 	 * 
 	 * @return A String object containing the variable id. Never returns null.
 	 */
 	public String getVariableID() {
 		return this.variableID;
 	}
 	
 	/**
 	 * Sets the count of this acces.
 	 * 
 	 * @param count The new count for this access.
 	 */
 	public void setCount( final int count ) {
 		this.count = count;
 	}
 
 	/**
 	 * Sets the access method of this Access.
 	 * 
 	 * @param method The the method type of this access.
 	 */
 	public void setMethod( final MethodTypes method ) {
 		if( method == null ) {
 			throw new IllegalArgumentException( "The parameter 'method' must not be null!" );
 		}
 		this.method = method;
 	}
 
 	/**
 	 * Sets the data type of this Access.
 	 * 
 	 * @param type The new datatype for this Access.
 	 */
 	public void setType( final DataTypes type ) {
 		this.type = type;
 	}
 
 	/**
 	 * Sets the ID of this Access.
 	 * 
 	 * This means the id in the transport system that is used to access it.
 	 * 
 	 * @param variableID The ID of this acccess. 
 	 */
 	public void setVariableID( final String variableID ) {
 		if( variableID == null ) {
 			throw new IllegalArgumentException( "The parameter 'variableID' must not be null!" );
 		}
 		this.variableID = variableID;
 	}
 
 	
 	/**
 	 * This method gives back the timeout of this Access.
 	 * 
 	 * @return The Timeout of this Access.
 	 */
 	public double getTimeout() {
 		return this.timeout;
 	}
 
 	/**
 	 * This method sets the timeout of this access.
 	 * 
 	 * @param timeout The timeout of this access.
 	 */
 	public void setTimeout( final double timeout ) {
 		this.timeout = timeout;
 	}
 	
 	/**
 	 * 
 	 * @return monitor flag
 	 */
 	public boolean getMonitor() {
 		return this.monitor;
 	}
 	/**
 	 * 
 	 * @param hasMonitor set/unset the monitor flag
 	 */
 	public void setMonitor( boolean hasMonitor ) {
 		this.monitor = hasMonitor;
 	}
 
 
 	/**
 	 * This method gives back the transport system of this access. 
 	 *
 	 * @return The transport type of this access.
 	 */
 	public TransportTypes getTransport() {
 		return this.transport;
 	}
 
 	/**
 	 * This method sets the Transport Type of this Access.
 	 * 
 	 * @param transport The new transport Type of this Acccess.
 	 */
 	public void setTransport( final TransportTypes transport ) {
 		this.transport = transport;
 	}
 
 	/**
 	 * This method gives back if a value is possible for this acces.
 	 * 
 	 * @param value The value that should be checked.
 	 * @return Gives back 'true' if the value is possible for this Access and false if not.
 	 */
 	public boolean isValuePossible( final String value ) {
 		return DataTypes.isValuePossible( this.type, value );
 	}
 
 	/**
 	 * Return a well-formatted string with a valid value for the datatype.
 	 * If value can not be converted, return a default value
 	 * 
 	 * @param value The value that will be formatted.
 	 * @return a well-formatted string with a valid value
 	 */
 	public String formatValueDefault( final String value ) {
 		return DataTypes.formatValueDefault( this.type, value );
 	}
 	
 	/**
 	 * Return a well-formatted string with a valid value for the datatype.
 	 * If value can not be converted, return null
 	 * 
 	 * @param value The value that will be formatted.
 	 * @return a well-formatted string or null
 	 */
 	public String formatValue( final String value ) {
 		return DataTypes.formatValue( this.type, value );
 	}
 
 	/**
 	 * Return a well-formatted default value for the datatype.
 	 * 
 	 * @return a well-formatted string with a default value
 	 */
 	public String getDefaultValue() {
 		return DataTypes.getDefaultValue(this.type);
 	}
 
 	public boolean isReadOnly() {
 		switch (method) {
 		case GETPUT:
 		case GETPUTCB:
 		case PUT:
 		case PUTCB:
 			return false;
 		default:
 			return true;
 		}
 	}
 }
