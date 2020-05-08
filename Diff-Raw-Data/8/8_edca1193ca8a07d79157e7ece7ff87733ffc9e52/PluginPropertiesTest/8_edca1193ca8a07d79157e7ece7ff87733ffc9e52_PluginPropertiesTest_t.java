 /**
  * 
  */
 package cytoscape.plugin;
 
 import junit.framework.TestCase;
 
 /**
  * @author skillcoy
  *
  */
 public class PluginPropertiesTest extends TestCase {
 
 	PluginProperties pp;
 	
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		String FS = java.io.File.separator;
 		String TestProps = System.getProperty("user.dir") + FS + "testData" + FS + "plugins" + FS + "test_plugin.props";
 		pp = new PluginProperties(TestProps);
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginProperties#fillPluginInfoObject(cytoscape.plugin.PluginInfo)}.
 	 */
 	public void testNoMatchingCyVersion() throws cytoscape.plugin.ManagerException {
		cytoscape.CytoscapeInit.getProperties().setProperty("cytoscape.version.number", "2.4");
 		PluginInfo info = pp.fillPluginInfoObject(null);
		assertEquals(info.getCytoscapeVersion(), "2.4");
 	}
 
 	public void testMatchingCyVersion() throws cytoscape.plugin.ManagerException {
		cytoscape.CytoscapeInit.getProperties().setProperty("cytoscape.version.number", "2.3.3");
 		PluginInfo info = pp.fillPluginInfoObject(null);
 		assertNotNull(info);
 	}
 
 
 }
