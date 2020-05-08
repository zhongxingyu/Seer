 /**
  * Copyright (c) 2012 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.reqif10.tests.uc000;
 
 import org.eclipse.rmf.reqif10.tests.uc000.tc1000.TC0001000ContainmentEStructuralFeatureTests;
 import org.eclipse.rmf.reqif10.tests.uc000.tc1001.TC0001001FullModelTests;
 import org.eclipse.rmf.reqif10.tests.uc000.tc4000.TC0004000ToolExtensionWithEcoreMetamodelTests;
 import org.eclipse.rmf.reqif10.tests.uc000.tc4001.TC0004001ToolExtensionWithCustomRuntimeCreatedMetamodelTests;
import org.eclipse.rmf.reqif10.tests.uc000.tc9000.TC0009000_SchemaValidationOnLoadTests;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 @RunWith(Suite.class)
 @SuiteClasses({ TC0001000ContainmentEStructuralFeatureTests.class, TC0001001FullModelTests.class,
		TC0004000ToolExtensionWithEcoreMetamodelTests.class, TC0004001ToolExtensionWithCustomRuntimeCreatedMetamodelTests.class,
		TC0009000_SchemaValidationOnLoadTests.class })
 public class AllUc000LowLevelSerializationTests {
 
 }
