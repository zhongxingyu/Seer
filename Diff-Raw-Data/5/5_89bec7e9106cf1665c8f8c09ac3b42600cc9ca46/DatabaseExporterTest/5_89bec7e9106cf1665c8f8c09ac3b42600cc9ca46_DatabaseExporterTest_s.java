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
 package org.openmrs.contrib.databaseexporter;
 
 import org.junit.Test;
 
 import java.lang.Exception;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DatabaseExporterTest {
 
 	@Test
 	public void shouldTest() throws Exception {
 		List<String> config = new ArrayList<String>();
 		config.add("rwanda/deidentifyPatients");
 		config.add("rwanda/deidentifyProviders");
 		config.add("rwanda/deidentifyUsers");
		config.add("rwanda/removeSyncData");
 		config.add("rwanda/trimArchiveData");
 
		config.add("rwanda/removeAllPatients");
 		config.add("rwanda/trimUsers");
 		config.add("rwanda/trimProviders");
 
 
 		config.add("-localDbName=openmrs_rwink");
 		config.add("-user=openmrs");
 		config.add("-password=openmrs");
 		config.add("-logSql=true");
 
 		DatabaseExporter.main(config.toArray(new String[] {}));
 	}
 }
 
