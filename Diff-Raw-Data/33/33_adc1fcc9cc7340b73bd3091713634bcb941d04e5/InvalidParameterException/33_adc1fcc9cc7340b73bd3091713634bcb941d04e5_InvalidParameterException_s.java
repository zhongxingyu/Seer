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
 package de.tuilmenau.ics.fog.exceptions;
 
 /**
  * Exception thrown if parameters passed to a method are invalid.
  */
 public class InvalidParameterException extends Exception
 {
 	public InvalidParameterException(String errorMessage)
 	{
 		super(errorMessage);
 	}
 	
 	public InvalidParameterException(String errorMessage, Throwable cause)
 	{
 		super(errorMessage, cause);
 	}
 	
 }
