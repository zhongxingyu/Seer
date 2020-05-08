 /**
  * 
  */
 package org.opf_labs.arc_cd.cdrdao;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 /**
  * Test class for CdrdaoExecutable
  *
  * @author  <a href="mailto:carl@openplanetsfoundation.org">Carl Wilson</a>.</p>
  *          <a href="https://github.com/carlwilson">carlwilson AT github</a>.</p>
  * @version 0.1
  * 
  * Created 7 Oct 2013:09:24:16
  */
 public class CdrdaoExecutableTest {
 	private static final String EMPTY = "";
 	private static final String COMMAND = "cdrdao";
 	private static final String VERSION = "3.2.1";
 	private static final String DEFAULT_DEVICE = "/dev/sr0";
 	private static final String SECOND_DEVICE = "/dev/sr1";
 	private static final List<String> DEVICES = Arrays.asList(new String[] { DEFAULT_DEVICE, SECOND_DEVICE });
 	/**
 	 * Test method for CdrdaoExecutable#getNewInstance(java.lang.String, java.lang.String, java.util.List)
 	 */
 	@Test(expected=NullPointerException.class)
 	public void testGetNewInstance() {
 		CdrdaoExecutable testInstance = CdrdaoExecutable.getNewInstance(null, VERSION, DEVICES);
 		assertTrue(testInstance != null);
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getNewInstance(java.lang.String, java.lang.String, java.util.List)
 	 */
 	@Test(expected=NullPointerException.class)
 	public void testGetNewInstanceNullCommand() {
 		CdrdaoExecutable.getNewInstance(null, VERSION, DEVICES);
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getNewInstance(java.lang.String, java.lang.String, java.util.List)
 	 */
 	@Test(expected=NullPointerException.class)
 	public void testGetNewInstanceNullVersion() {
 		CdrdaoExecutable.getNewInstance(COMMAND, null, DEVICES);
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getNewInstance(java.lang.String, java.lang.String, java.util.List)
 	 */
 	@Test(expected=NullPointerException.class)
 	public void testGetNewInstanceNullDevices() {
 		CdrdaoExecutable.getNewInstance(COMMAND, VERSION, null);
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getNewInstance(java.lang.String, java.lang.String, java.util.List)
 	 */
 	@Test(expected=IllegalArgumentException.class)
 	public void testGetNewInstanceEmptyCommand() {
 		CdrdaoExecutable.getNewInstance(EMPTY, VERSION, DEVICES);
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getNewInstance(java.lang.String, java.lang.String, java.util.List)
 	 */
 	@Test(expected=IllegalArgumentException.class)
 	public void testGetNewInstanceEmptyVersion() {
 		CdrdaoExecutable.getNewInstance(COMMAND, EMPTY, DEVICES);
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getCommand()
 	 */
 	@Test
 	public void testGetCommand() {
 		CdrdaoExecutable testInstance = CdrdaoExecutable.getNewInstance(COMMAND, VERSION, DEVICES);
 		assertTrue(testInstance.getCommand().equals(COMMAND));
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getVersion()
 	 */
 	@Test
 	public void testGetVersion() {
 		CdrdaoExecutable testInstance = CdrdaoExecutable.getNewInstance(COMMAND, VERSION, DEVICES);
 		assertTrue(testInstance.getVersion().equals(VERSION));
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getDevices()
 	 */
 	@Test
 	public void testGetDevices() {
 		CdrdaoExecutable testInstance = CdrdaoExecutable.getNewInstance(COMMAND, VERSION, DEVICES);
 		assertTrue(testInstance.getDevices().equals(DEVICES));
 	}
 
 	/**
 	 * Test method for CdrdaoExecutable#getDefaultDevice()
 	 */
 	@Test
 	public void testGetDefaultDevice() {
 		CdrdaoExecutable testInstance = CdrdaoExecutable.getNewInstance(COMMAND, VERSION, DEVICES);
 		assertTrue(testInstance.getDefaultDevice().equals(DEFAULT_DEVICE));
 	}
 
 }
