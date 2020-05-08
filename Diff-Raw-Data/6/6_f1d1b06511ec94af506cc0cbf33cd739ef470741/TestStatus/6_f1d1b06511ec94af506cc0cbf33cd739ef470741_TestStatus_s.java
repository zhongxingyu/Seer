 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
/**
 * 
 */
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics;
 
 /**
 * Statuses an acceptance test can have
  * @author Dylan
  *
  */
 public enum TestStatus {
 	/**
 	 * The user has not assigned a status to the acceptance test
 	 */
 	STATUS_BLANK(""),
 	/**
 	 * Indicates the acceptance test has passed
 	 */
 	STATUS_PASSED("Passed"),
 	/**
 	 * Indicates the acceptance test has failed
 	 */
 	STATUS_FAILED("Failed");
 	
 	private String desc;
 	/**
 	 * constructor for test status
 	 * @param desc the description
 	 */
 	TestStatus(String desc) {
 		this.desc = desc;
 	}
 	
 	/**
 	 * @return the string value of the enum
 	 */
 	public String toString() {
 		return desc;
 	}
 	
 }
 
