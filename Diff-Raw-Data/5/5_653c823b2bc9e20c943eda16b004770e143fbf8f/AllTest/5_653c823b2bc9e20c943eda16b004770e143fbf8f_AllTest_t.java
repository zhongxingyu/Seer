 package com.photon.phresco.framework.param.impl;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
import com.photon.phresco.framework.impl.ConfigurationReaderTest;

 @RunWith(Suite.class)
@SuiteClasses({ MacosSdkParameterImplTest.class, IosSdkParameterImplTest.class, IosSimSDKVersionsParameterImplTest.class, ConfigurationReaderTest.class})
 
 public class AllTest {
 	// intentionally blank. All tests were added via annotations
 }
