 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics;
 
 import java.util.Date;
 
 /**
  *The information for the Iteration's date
  *
  * @author justinhess
  * @version $Revision: 1.0 $
  */
 public class IterationDate {
	private Date date;
 	
 	/**
 	 * Constructor for IterationDate.
 	 * @param curr date
 	 */
 	public IterationDate(Date curr) {
 		this.date = curr;
 	}
 
 	/**
 	 * Checks whether the given date is after this date.
 	 * @param d the date to check
 	 * @return Whether the given date is after this date.
 	 *  */
 	public boolean after(Date d)
 	{
		if(date == null) return true;
 		return this.date.after(d);
 	}
 	
 	/**
 	 * Checks whether the given date is before this date.
 	 * @param d the date to check
 	 * @return Whether the given date is before this date.
 	 *  */
 	public boolean before(Date d)
 	{
		if(date == null) return true;
 		return this.date.before(d);
 	}
 	
 	/**
 	 * Checks whether the given date is equals date.
 	 * @param d the date to check
 	 * @return Whether the given date is equal date.
 	 *  */
 	public boolean during(Date d)
 	{
		if(date == null) return false;
 		return this.date.equals(d);
 	}
 	
 	
 	/**
 	 * Sets the date
 	 * @param date the new date.
 	 */
 	public void setDate(Date date)
 	{
 		this.date = date;
 	}
 }
