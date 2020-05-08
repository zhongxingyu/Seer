 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.facade.properties;
 
import de.tuilmenau.ics.fog.ui.Logging;

 
 public class DelayProperty extends MinMaxProperty
 {
 	public static int DefaultMaxValueMSec = 100;
 	
 	
 	public DelayProperty(int minDelayMSec, int maxDelayMSec, double variance)
 	{
 		super(minDelayMSec, maxDelayMSec, variance);
 	}
 	
 	public DelayProperty(int delayMilliSec, Limit minValue)
 	{
 		super(delayMilliSec, minValue);
 	}
 	
 	public DelayProperty()
 	{
 		super(DefaultMaxValueMSec, Limit.MAX);
 	}
 
 	@Override
 	public Property create(int min, int max, double variance)
 	{
 		return new DelayProperty(min, max, variance);
 	}
 	
 	@Override
 	public Property deriveRequirements(Property property) throws PropertyException
 	{	
 		if(property instanceof DelayProperty) {
 			// do NoS introduces delay?
 			if(getMin() != UNDEFINED) {
 				int maxDelay = ((DelayProperty) property).getMax();
 				
 				if(maxDelay != UNDEFINED) {
 					// min delay introduced and max delay required
 					if(getMin() <= maxDelay) {
						return create(getMin(), UNDEFINED, getVariance());
 					} else {
						Logging.getInstance().err(this, "Min of " +this +" exceeds max value " +property);;
 						throw new PropertyException(this, "Min of " +this +" exceeds max value " +property);
 					}
 				} else {
 					// delay introduced and not limited by requirements
 					return create(getMin(), UNDEFINED, 0);
 				}
 			} else {
 				// no delay introduced by NoS
 				return create(0, 0, 0);
 			}
 		} else {
			Logging.getInstance().err(this, "Parameter " +property +" is not a " +DelayProperty.class + " object.");
 			throw new PropertyException(this, "Parameter " +property +" is not a " +DelayProperty.class + " object.");
 		}
 	}
 	
 	@Override
 	public Property removeCapabilities(Property property) throws PropertyException
 	{	
 		if(property instanceof DelayProperty) {
 			// delay restricted by requirements?
 			if(getMax() != UNDEFINED) {
 				int maxDelay = ((DelayProperty) property).getMax();
 				
 				if(maxDelay != UNDEFINED) {
 					if(getMax() >= maxDelay) {
 						return create(UNDEFINED, getMax() -maxDelay, getVariance() +((DelayProperty) property).getVariance());
 					} else {
 						throw new PropertyException(this, "Max of " +property +" exceeds max value " +this);
 					}
 				} else {
 					// is there a minimum value?
 					if(((DelayProperty) property).getMin() != UNDEFINED) {
 						throw new PropertyException(this, "Max restricted to " +this +" but " +property +" does not restrict it.");
 					} else {
 						// delay restricted and not introduced -> no changes
 						return this;
 					}
 				}
 			} else {
 				// no delay limit -> no changes
 				return this;
 			}
 		} else {
 			throw new PropertyException(this, "Parameter " +property +" is not a " +DelayProperty.class + " object.");
 		}
 	}
 	
 	@Override
 	public boolean isBE()
 	{
 		return getMax() == UNDEFINED;
 	}
 	
 	@Override
 	public String getUnit()
 	{
 		return "msec";
 	}
 	
 	@Override
 	public Property clone()
 	{
 		return new DelayProperty(getMin(), getMax(), getVariance());
 	}
 }
