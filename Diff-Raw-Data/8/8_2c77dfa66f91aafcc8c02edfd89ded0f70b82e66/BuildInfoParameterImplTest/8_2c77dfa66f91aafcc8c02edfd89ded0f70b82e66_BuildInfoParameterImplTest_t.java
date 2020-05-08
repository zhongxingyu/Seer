 package com.photon.phresco.commons.api;
 
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 import junit.framework.Assert;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.impl.BuildInfoParameterImpl;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter.PossibleValues;
 
 public class BuildInfoParameterImplTest {
 	
 	Map<String, Object> map = new HashMap<String, Object>();
 	BuildInfoParameterImpl buildInfoParmas = new BuildInfoParameterImpl();
 	
 	
 	@Test
 	public void buildParamsTest() throws PhrescoException {
 		map.put("applicationInfo",getApplicationInfo());
		map.put("rootModule", "");
 		PossibleValues values = buildInfoParmas.getValues(map);
 		Assert.assertEquals(0, values.getValue().size());
 		
 	}
 	
 	private static ApplicationInfo getApplicationInfo() {
 		ApplicationInfo info = new ApplicationInfo();
 		info.setAppDirName("TestProject");
 		info.setCode("TestProject");
 		info.setId("TestProject");
 		info.setCustomerIds(Collections.singletonList("photon"));
 		info.setEmailSupported(false);
 		info.setPhoneEnabled(false);
 		info.setTabletEnabled(false);
 		info.setDescription("Simple java web service Project");
 		info.setHelpText("Help");
 		info.setName("TestProject");
 		info.setPilot(false);
 		info.setUsed(false);
 		info.setDisplayName("TestProject");
 		info.setSelectedJSLibs(Collections.singletonList("99aa3901-a088-4142-8158-000f1e80f1bf"));
 		info.setVersion("1.0");
 		return info;
 	}
 }
