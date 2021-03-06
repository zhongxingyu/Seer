 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.test;
 
 /**
  * Modules using the unit test framework should use this class instead of
  * {@link BaseContextSensitiveTest}.
  * 
  * Developers need to fill in the {@link #getModulesToLoad()} method with their
  * current module's omod location and any dependent omods
  */
 public abstract class BaseModuleContextSensitiveTest extends BaseContextSensitiveTest {
 	
    /**
	 * Add in the module application context files to the config locations and
	 * the test application context (so that the module services are loaded from
	 * the system classloader)
	 * 
	 * @return Array of String locations for the application context files
	 */
	protected String[] getConfigLocations() {
	    return new String[] {
	    		"classpath:applicationContext-service.xml",
	    		"classpath*:TestingApplicationContext.xml",
	    		"classpath*:moduleApplicationContext.xml"
	    };
	}
	
 }
