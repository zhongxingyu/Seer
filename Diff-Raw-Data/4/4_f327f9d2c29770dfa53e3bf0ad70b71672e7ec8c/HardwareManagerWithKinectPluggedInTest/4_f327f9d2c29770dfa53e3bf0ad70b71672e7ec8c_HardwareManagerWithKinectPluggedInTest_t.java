 /**
  * Copyright (C) 2013 Colorado School of Mines
  *
  * This file is part of the Interface Software Development Kit (SDK).
  *
  * The InterfaceSDK is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The InterfaceSDK is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the InterfaceSDK.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.mines.acmX.exhibit.input_services.hardware;
 
import static org.junit.Assert.assertTrue;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.junit.Test;
 
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
 import edu.mines.acmX.exhibit.module_management.metas.DependencyType;
 import edu.mines.acmX.exhibit.module_management.metas.ModuleMetaData;
 
 /**
  * JUnit tests for the HardwareManager.
  * This includes checking the validity of the meta data, as well checking
  * permissions for a given ModuleMetaData.
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  *
  * @see {@link ModuleMetaData}
  */
 
 public class HardwareManagerWithKinectPluggedInTest {
 	public static final Logger log = LogManager.getLogger(HardwareManagerTest.class);
 	public static final String BASE_FILE = "input_services/";
 	
 	@Test
 	public void testValidDriverCache()
 			throws HardwareManagerManifestException, BadDeviceFunctionalityRequestException,
 			InvalidConfigurationFileException, BadFunctionalityRequestException {
 		log.info("testValidDriverConfigFile");
 		
 		HardwareManager.setManifestFilepath(BASE_FILE + "GoodCompleteManifest.xml");
 		HardwareManager hm = HardwareManager.getInstance();
 
 		Map<String, DependencyType> mmd = new HashMap<String, DependencyType>();
 		hm.setRunningModulePermissions(mmd);
 		hm.resetAllDrivers();
 		
 		mmd.put("depth", DependencyType.REQUIRED);
 		hm.setRunningModulePermissions(mmd);
 
 		Map<String, String> configStore = new HashMap<String, String>();
 		configStore.put("kinectopenni", "openni_config.xml");
 		
 		hm.setConfigurationFileStore(configStore);
 		hm.resetAllDrivers();
 		
 		assertTrue(hm.getNumberofDriversInCache() == 1);
 	}
 	
 	@Test
 	public void testOptionalFunctionalityGetsLoadedAtRuntime()
 			throws HardwareManagerManifestException,
 			InvalidConfigurationFileException,
 			BadFunctionalityRequestException {
 		
 		log.info("testOptionalFunctionalityGetsLoadedAtRuntime");
 		
 		HardwareManager.setManifestFilepath(BASE_FILE + "GoodCompleteManifest.xml");
 		HardwareManager hm = HardwareManager.getInstance();
 
 		Map<String, DependencyType> mmd = new HashMap<String, DependencyType>();
 		hm.setRunningModulePermissions(mmd);
 		hm.resetAllDrivers();
 		
 		mmd.put("depth", DependencyType.OPTIONAL);
 		hm.setRunningModulePermissions(mmd);
 
 		Map<String, String> configStore = new HashMap<String, String>();
 		configStore.put("kinectopenni", "openni_config.xml");
 		
 		hm.setConfigurationFileStore(configStore);
 		hm.resetAllDrivers();
 		
 		assertTrue(hm.getNumberofDriversInCache() == 0);
 		List<String> devices = hm.getDevices("depth");
 		assertTrue(hm.getNumberofDriversInCache() == 1);
 	}
 	
 	
  }
