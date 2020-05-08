 package com.eclipseninja.archimedes.application;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.osgi.framework.BundleContext;
 
 public class ActivatorTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testActivator() {
 		Activator activator = new Activator();
 		BundleContext bundleContext = Mockito.mock(BundleContext.class);
 		try {
 			activator.start(bundleContext);
 			BundleContext bc = Activator.getContext();
 			assertNotNull(bc);
 			activator.stop(bundleContext);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 
 }
