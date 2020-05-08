 package org.configureme;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 import net.anotheria.util.StringUtils;
 
 import org.apache.log4j.BasicConfigurator;
 import org.configureme.sources.ConfigurationSourceRegistryTest;
 import org.configureme.sources.FixtureLoader;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class AutoReConfig {
 	
 	static{
 		BasicConfigurator.configure();
 	}
 	
 	@BeforeClass public static void setupRegistry(){
 		//use the other test which can access protected methods
 		ConfigurationSourceRegistryTest.setupRegistry();
 	}
 	
 	@Test public void configureAndWaitForReconfigure(){
 		TestReConfigurable configurable = new TestReConfigurable();
 		ConfigurationManager.INSTANCE.configure(configurable);
 		
 		assertEquals(100, configurable.getShortValue());
 		assertEquals(1234567890123L, configurable.getLongValue());
 		assertEquals(1000, configurable.getIntValue());
 		assertEquals(true, configurable.getBooleanValue());
 		assertEquals("foo", configurable.getStringValue());
 		assertEquals(-125, configurable.getByteValue());
 		assertEquals(12.5f, configurable.getFloatValue());
 		assertEquals(1234.11, configurable.getDoubleValue());
 		assertEquals(0, configurable.getOnlyInA());
 		assertEquals(0, configurable.getOnlyInB());
 		assertTrue(configurable.isBeforeConfigCalled());
 		assertTrue(configurable.isAfterConfigCalled());
 		
 		assertTrue(configurable.isBeforeInitialConfigCalled());
 		assertTrue(configurable.isAfterInitialConfigCalled());
 		
 		assertFalse(configurable.isBeforeReConfigCalled());
 		assertFalse(configurable.isAfterReConfigCalled());
 		
 		//mark for reconfigure
 		String content = FixtureLoader.getContent();
 		content = StringUtils.replaceOnce(content, "foo", "bar");
 		content = StringUtils.replaceOnce(content, "1000", "999");
 		FixtureLoader.setContent(content);
		FixtureLoader.setLastUpdateTimestamp(System.currentTimeMillis()+1000);
 		//waiting for 
		long timeToWait = System.currentTimeMillis()+1000L*30;
 		boolean finished = false;
 		while(System.currentTimeMillis()<timeToWait &&!finished){
 			if (configurable.getIntValue()==999){
 				finished = true;
 			}else{
 				try{
 					System.out.println("test waits 12 seconds for the reconfiguration (Max: "+((timeToWait-System.currentTimeMillis())/1000)+") ...");
 					Thread.sleep(1000L*12);
 				}catch(InterruptedException e){}
 			}
 		}
		assertTrue("waiting for the reconfiguration should be finished, intValue="+configurable.getIntValue(), finished);
 		assertTrue(configurable.isBeforeReConfigCalled());
 		assertTrue(configurable.isAfterReConfigCalled());
 		assertEquals(configurable.getStringValue(), "bar");
 	}
 
 	
 }
