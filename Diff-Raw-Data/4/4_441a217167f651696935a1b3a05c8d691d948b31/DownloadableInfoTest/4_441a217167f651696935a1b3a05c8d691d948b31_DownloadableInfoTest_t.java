 /**
  * 
  */
 package cytoscape.plugin;
 
 import junit.framework.TestCase;
 
 /**
  * @author skillcoy
  *
  */
 public class DownloadableInfoTest extends TestCase {
 
 	DownloadableInfo di;
 
 	
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		 di = new InfoObj();
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#DownloadableInfo()}.
 	 */
 	public void testDownloadableInfo() {
 		assertNotNull(di);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#DownloadableInfo(java.lang.String)}.
 	 */
 	public void testDownloadableInfoString() {
 		di = new InfoObj("123");
 		assertNotNull(di);
 		assertEquals(di.getID(), "123");
 	}
 	
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#DownloadableInfo(java.lang.String, cytoscape.plugin.DownloadableInfo)}.
 	 */
 	public void testDownloadableInfoStringDownloadableInfo() {
 		di = new InfoObj("123", new InfoObj("parent_123"));
 		assertNotNull(di);
 		assertEquals(di.getID(), "123");
 		assertNotNull(di.getParent());
 		assertEquals(di.getParent().getID(), "parent_123");
 	}
 
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setCategory(java.lang.String)}.
 	 */
 	public void testCategoryString() {
 		di.setCategory("Foobar");
 		assertNotNull(di.getCategory());
 		assertEquals(di.getCategory(), "Foobar");
 	}
 	
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setCategory(cytoscape.plugin.Category)}.
 	 */
 	public void testCategoryCategory() {
 		di.setCategory(Category.CORE);
 		assertNotNull(di.getCategory());
 		assertEquals(di.getCategory(), Category.CORE.toString());
 	}
 	
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setParent(cytoscape.plugin.DownloadableInfo)}.
 	 */
 	public void testParent() {
 		di.setParent(new InfoObj("parent"));
 		assertNotNull(di.getParent());
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setName(java.lang.String)}.
 	 */
 	public void testName() {
 		di.setName("Booya!");
 		assertEquals(di.getName(), "Booya!");
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setDescription(java.lang.String)}.
 	 */
 	public void testDescription() {
 		di.setDescription("Just testing");
 		assertEquals(di.getDescription(), "Just testing");
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setDownloadableURL(java.lang.String)}.
 	 */
 	public void testDownloadableURL() {
 		String url = "http://google.com";
 		di.setDownloadableURL(url);
 		assertEquals(di.getDownloadableURL(), url);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setObjectUrl(java.lang.String)}.
 	 */
 	public void testObjectUrl() {
 		String url = "http://foobar.jar";
 		di.setObjectUrl(url);
 		assertEquals(di.getObjectUrl(), url);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setCytoscapeVersion(java.lang.String)}.
 	 */
 	public void testCytoscapeVersion() {
 		di.setCytoscapeVersion("2.5");
 		assertEquals(di.getCytoscapeVersion(), "2.5");
 		
 		di.setCytoscapeVersion("2.5.6");
 		assertEquals(di.getCytoscapeVersion(), "2.5.6");
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setObjectVersion(double)}.
 	 */
 	public void testObjectVersion() {
 		di.setObjectVersion(1.3);
 		assertEquals(di.getObjectVersion(), "1.3");
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#setReleaseDate(java.lang.String)}.
 	 */
 	public void testReleaseDate() {
 		String date = "Oct. 14, 2007";
 		di.setReleaseDate(date);
 		assertEquals(di.getReleaseDate(), date);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#getType()}.
 	 */
 	public void testGetType() {
 		assertNotNull(di.getType());
 	}
 
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#getID()}.
 	 */
 	public void testGetID() {
 		// no id set initially
 		assertNull(di.getID());
 		di = new InfoObj("1234");
 		assertEquals(di.getID(), "1234");
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#isNewerObjectVersion(cytoscape.plugin.DownloadableInfo)}.
 	 */
 	public void testIsNewerObjectVersion() {
 		di = new InfoObj("123");
 		di.setObjectVersion(1.0);
 		DownloadableInfo diNew = new InfoObj("123");
 		diNew.setObjectVersion(1.3);
 		
 		assertTrue(di.isNewerObjectVersion(diNew));
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.DownloadableInfo#isCytoscapeVersionCurrent()}.
 	 */
 	public void testIsCytoscapeVersionCurrent() {
 		di.setCytoscapeVersion("2.1");
 		assertFalse(di.isCytoscapeVersionCurrent());
 		
 		di.setCytoscapeVersion(cytoscape.CytoscapeVersion.version);
 		assertTrue(di.isCytoscapeVersionCurrent());
 	}
 
 	private class InfoObj extends DownloadableInfo {
 		
 		public InfoObj() {
 			super();
 		}
 		
 		public InfoObj(String arg) {
 			super(arg);
 		}
 		
 		public InfoObj(String arg, DownloadableInfo parent) {
 			super(arg, parent);
 		}
 		
 		public DownloadableType getType() {
 			return DownloadableType.PLUGIN;
 		}
 		
 		public String htmlOutput() {
 			return null;
 		}

		public Installable getInstallable() {
			return null;
		}
 	}
 	
 }
