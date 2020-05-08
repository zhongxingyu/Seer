 /*******************************************************************************
  * Copyright (c) 2001, 2007 Physikalisch Technische Bundesanstalt.
  * All rights reserved.
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package de.ptb.epics.eve.data.measuringstation;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.ptb.epics.eve.data.measuringstation.exceptions.ParentNotAllowedException;
 
 /**
  * This abstract class is the base of all of the different devices inside of the
  * Scan Modul Editor model, that is describing the measuring station. That includes
  * motors, detectors, motor axis, detector channels and options.  
  * Please don't mix up the term of the AbstractDevices and a Device, which is actually
  * more behaving like an option. A Device in the term of the Scan Modul Editor data
  * Model is a specilatzation of an Abstract Device.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @version 1.5
  * 
  */
 public abstract class AbstractDevice {
 	
 	/**
 	 * The name of the device.
 	 */
 	private String name;
 	
 	/**
 	 * The id of the device. It only contains the own id, not the id of the parent.
 	 */
 	private String id;
 	
 	/**
 	 * The unit of the device.
 	 */
 	private Unit unit;
 	
 	/**
 	 * The parent of this device.
 	 */
 	private AbstractDevice parent;
 	
 	/**
 	 * A list, that is containing all options of the device.
 	 */
 	private final List<Option> options;
 	
 	/**
 	 * This constructor is used by inheriting classes to construct a blank AbstractDevice
 	 * with an empty name, empty id, null unit and empty list of options.
 	 *
 	 */
 	protected AbstractDevice() {
 		this( "", "", null, null, null );
 	}
 	
 	
 	private String fullIdentifier = null;
 	
 	/**
 	 * This constructor is used by inheriting classes to construct a new Abstract Device with
 	 * given name, id, unit and options. The list of options of the AbstractDevice will be a copy
 	 * of the given list. Note, that the list will be copied, not the elements inside! The parent
 	 * will not be setted directly, it is using the setParent Method. If you overriding this method,
 	 * you can control wich types are allowed an which not.
 	 * 
 	 * @param name The name of the device. Must not be null! Use a empty String if you want no name for the device.
 	 * @param id The id of the device. Must not be null! Use a empty String if you want no id for the device.
 	 * @param unit The unit of the device. May be null.
 	 * @param options A list of options of the Device. If it's null, a new empty list will be created.
 	 * @param parent The parent of this device.
 	 */
 	public AbstractDevice( final String name, final String id, final Unit unit, final List<Option> options, final AbstractDevice parent ) {
 		if( name == null ) {
 			throw new IllegalArgumentException( "The parameter 'name' must not be null!");
 		}
 		if( id == null ) {
 			throw new IllegalArgumentException( "The parameter 'id' must not be null!");
 		}
 		this.name = name;
 		this.id = id;
 		this.unit = unit;
 		if( options != null ) {
 			this.options = new ArrayList<Option>( options );
 		} else {
 			this.options = new ArrayList<Option>();
 		}
 		try {
 			this.setParent( parent );
 		} catch( ParentNotAllowedException e ) {
 			throw new IllegalArgumentException( "The parameter 'parent' had an illegal Type.", e );
 			
 		}
 	}
 	
 	/**
 	 * This method is very fundamental for the model of the hierarchy of all devices inside of the
 	 * measuring station description. Every device is cleary addressable by it's full identifier, that
 	 * is setted together by it's own name, and the name of it's parent device. If the device has no name,
 	 * the id is used to build the full identifier. So:<br />
 	 * <br />
 	 * The full identifier of a motor will be: Motor-Name  ( Motor-Id ).<br />
 	 * The full identifier of a motor-axis will be: Motor-Name Axis-Name  ( Axis-Id ).<br />
 	 * The full identifier of a detector will be: Detector-Name  ( Detector-Id ).<br />
 	 * The full identifier of a detector-channel will be: Detector-Name Channel-Name  ( Channel-Id ).<br />
 	 * The full identifier of a option of an motor will be: Motor-Name Option-Name  ( Option-Id ).<br />
 	 * The full identifier of a option of an motor-axis will be: Motor-Name Axis-Name Option-Name  ( Option-Id ).<br />
 	 * The full identifier of a option of an detector will be: Detector-Name Option-Name  ( Option-Id ).<br />
 	 * The full identifier of a option of an detector-channel will be: Detector-Name Channel-Name Option-Name  ( Option-Id ).<br />
 	 * The full identifier of a device will be: Device-Name  ( Device-Id ).<br />
 	 * <br />
 	 * 
 	 * @return A String object that contains the full identifier of the device.
 	 */
 	public String getFullIdentifyer() {
 		
 		if( this.fullIdentifier == null ) {
 			StringBuffer fullIdentifyer = new StringBuffer();
 		
 			if (this.parent!=null) {
 				if (this.parent.parent!=null) {
 					if (!this.parent.parent.getName().equals(""))
 						fullIdentifyer.append( this.parent.parent.getName() );
 				}
 				if (!this.parent.getName().equals("")){
 					if (!fullIdentifyer.toString().equals( "" )) {
 						fullIdentifyer.append( " " );
 						fullIdentifyer.append( this.parent.getName() );
 					}
 					else
 						fullIdentifyer.append( this.parent.getName() );
 				}
 			}
 			if (!this.getName().equals("")) {
 				if (fullIdentifyer.toString().equals( "" ) ) {
 					fullIdentifyer.append( " " );
 					fullIdentifyer.append( this.getName() );
 				}
 				else
 					fullIdentifyer.append( this.getName() );
 			}
 
 			if (!fullIdentifyer.equals( "" ))
 				fullIdentifyer.append( "  ( " );
 			else
 				fullIdentifyer.append( "( " );
 			
 			fullIdentifyer.append( this.getID() );
 			fullIdentifyer.append( " )" );
 			this.fullIdentifier = fullIdentifyer.toString();
 		}
 		return this.fullIdentifier;
 	}
 	
 	/**
 	 * Gives back the name of the device.
 	 * 
 	 * @return A String object that contains the name of the device. Never returns null.
 	 */
 	public String getName() {
 		return this.name;
 	}
 	
 	/**
 	 * Gives back the id of the device.
 	 * 
 	 * @return  A String object that contains the id of the device. Never returns null.
 	 */
 	public String getID() {
 		return this.id;
 	}
 	
 	/**
 	 * Gives back the unit of the device.
 	 * 
 	 * @see de.ptb.epics.eve.data.measuringstation.Unit
 	 * @return A Unit object or null if it's not set.
 	 */
 	public Unit getUnit() {
 		return this.unit;
 	}
 	
 	/**
 	 * Gives back the parent of this device.
 	 * 
 	 * @return The parent of the device. May be null if the device has no parent.
 	 */
 	public AbstractDevice getParent() {
 		return this.parent;
 	}
 	
 	/**
 	 * Gives back a copy of the internal list, that is holding the options.
 	 * 
 	 * @return A list of Option objects. Never returns null.
 	 */
 	public List<Option> getOptions() {
 		return new ArrayList<Option>( this.options );
 	}
 
 	/**
 	 * Gives back a iterator over the internal list of options.
 	 * 
 	 * @return A Iterator<Option> object over the internal list of options.
 	 */
 	public Iterator<Option> optionIterator() {
 		return this.options.iterator();
 	}
 	
 	/**
 	 * Sets the ID of the Device.
 	 * 
 	 * @param id An String object, that contains the id of the device. Must not be null.
 	 */
 	public void setId( final String id ) {
 		if( id == null ) {
 			throw new IllegalArgumentException( "The parameter 'id' must not be null!");
 		}
 		this.id = id;
 	}
 	
 	/**
 	 * Adds an Option to the device. Actually this method is setting the device as parent for the
 	 * Option (for builidng the full identifyer) and adding it to an internal list.
 	 * 
 	 * @param option The Option that should be added. Must not be null.
 	 * @return Returns 'true' if the Option has been added and 'false' if not. 
 	 */
 	public boolean add( final Option option ) {
 		if( option == null ) {
 			throw new IllegalArgumentException("The parameters 'option' must not be null!");
 		}
 		try {
 			option.setParent( this );
 		} catch( ParentNotAllowedException e ) {
 			throw new IllegalArgumentException( "Your option does not accept this kind of device as parent. Please check you implementation!", e );
 		}
 		return options.add( option );
 	}
 
 	/**
 	 * Removes an Option from the device. Actually this method is setting the parent of the Option
 	 * to null and remove it from an internal list. The parent will not be setted to null if the Option
 	 } else {* were never a part of this device.
 	 * 
 	 * @param option The that should be remove. Must not be null.
 	 * @return Returns 'true' if the Option has been removed and 'false' if not.
 	 */
 	public boolean remove( final Option option ) {
 		if( option == null ) {
 			throw new IllegalArgumentException("The parameters 'option' must not be null!");
 		}
 		final boolean result = options.remove( option );
 		if( result ) {
 			try {
 				option.setParent( null );
 			} catch (ParentNotAllowedException e) {
 				throw new IllegalArgumentException( "Your option has not accepted to set the parent to null. Please check you implementation!", e );
 			}
 		}
 		return result; 
 	}
 
 	/**
 	 * Sets the name of the device.
 	 * 
 	 * @param name A String object, that contains the name of the device. Must not be null.
 	 */
 	public void setName( final String name ) {
 		if( name == null ) {
 			throw new IllegalArgumentException("The parameters 'name' must not be null!");
 		}
 		this.name = name;
 	}
 
 	/**
 	 * Sets the unit of the devive.
 	 * 
 	 * @param unit A Unit object.
 	 */
 	public void setUnit( final Unit unit ) {
 		this.unit = unit;
 	}
 
 	/**
 	 * Sets the parent of this device. Override this method to control which types are allowed.
 	 * After checking call the method of the super class.
 	 * 
 	 * @param parent
 	 */
 	protected void setParent( final AbstractDevice parent ) throws ParentNotAllowedException {
 		this.parent = parent;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((options == null) ? 0 : options.hashCode());
 		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals( final Object obj ) {
 		if( this == obj ) {
 			return true;
 		}
 		if( obj == null ) {
 			return false;
 		}
 		if( getClass() != obj.getClass() ) {
 			return false;
 		}
 		final AbstractDevice other = (AbstractDevice)obj;
 		if( id == null ) {
 			if( other.id != null ) {
 				return false;
 			}
 		} else if( !id.equals( other.id ) ) {
 			return false;
 		}
 		if( name == null ) {
 			if( other.name != null ) {
 				return false;
 			}
 		} else if( !name.equals( other.name ) ) {
 			return false;
 		}
 		if( options == null ) {
 			if(other.options != null)
 				return false;
 		} else if (!options.equals(other.options))
 			return false;
 		if (unit == null) {
 			if (other.unit != null)
 				return false;
 		} else if (!unit.equals(other.unit))
 			return false;
 		return true;
 	}
 	
 	
 }
