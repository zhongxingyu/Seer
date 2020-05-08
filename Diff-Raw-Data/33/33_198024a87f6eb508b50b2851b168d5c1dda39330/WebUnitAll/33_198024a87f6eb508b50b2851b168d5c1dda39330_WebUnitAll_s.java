 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008 Timefields Ltd
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.tool.test.http;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * @author ieb
  */
 public class WebUnitAll extends TestCase
 {
 
 	/**
 	 * @return
 	 */
 	public static Test suite()
 	{
 		TestSuite suite = new TestSuite("Test for org.sakaiproject.sdata.tool");
 		// $JUnit-BEGIN$
 		suite.addTestSuite(JsonCHSHandlerUnitT.class);
 		suite.addTestSuite(XmlRpcCHSHandlerUnitT.class);
 		suite.addTestSuite(JsonCHSUserStorageHandlerUnitT.class);
 		suite.addTestSuite(XmlRpcCHSUserStorageHandlerUnitT.class);
 		suite.addTestSuite(JsonJcrHandlerUnitT.class);
 		suite.addTestSuite(XmlRpcJcrHandlerUnitT.class);
 		suite.addTestSuite(JsonJcrUserStorageHandlerUnitT.class);
 		suite.addTestSuite(XmlRpcJcrUserStorageHandlerUnitT.class);
 		suite.addTestSuite(HttpRangeUnitT.class);
 		// $JUnit-END$
 		return suite;
 	}
 
 }
