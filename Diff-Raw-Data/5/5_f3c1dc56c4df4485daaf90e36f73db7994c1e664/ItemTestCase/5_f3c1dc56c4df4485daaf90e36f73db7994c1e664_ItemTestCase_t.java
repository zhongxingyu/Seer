 package org.xbrlapi.fragment.tests;
 
 import org.xbrlapi.DOMLoadingTestCase;
 import org.xbrlapi.FragmentList;
 import org.xbrlapi.Item;
 
 /**
  * Tests the implementation of the org.xbrlapi.Item interface.
  * Uses the DOM-based data store to ensure rapid testing.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class ItemTestCase extends DOMLoadingTestCase {
 	private final String STARTING_POINT = "test.data.tuple.instance";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURL(STARTING_POINT));		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public ItemTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * Test getting context.
 	 */
 	public void testGetContext() {
         try {
             FragmentList<Item> fragments = store.<Item>getFragments("SimpleNumericItem");
             assertTrue(fragments.getLength() > 0);
             for (Item fragment: fragments) {
                 assertEquals("org.xbrlapi.impl.ContextImpl", fragment.getContext().getType());
             }
         } catch (Exception e) {
             fail(e.getMessage());
         }
 	}	
 	
 	/**
 	 * Test is the item nill.
 	 */
	public void testIsNil() {
         try {
             FragmentList<Item> fragments = store.<Item>getFragments("SimpleNumericItem");
             assertTrue(fragments.getLength() > 0);
             for (Item fragment: fragments) {
                assertEquals(false, fragment.isNil());
             }
         } catch (Exception e) {
             fail(e.getMessage());
         }
 	}	
 		
 }
