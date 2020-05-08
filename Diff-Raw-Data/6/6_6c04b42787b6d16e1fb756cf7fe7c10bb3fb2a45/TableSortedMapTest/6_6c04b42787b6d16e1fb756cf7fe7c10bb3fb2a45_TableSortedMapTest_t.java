 /**
  * 
  */
 package org.melati.poem.test;
 
 import java.util.NoSuchElementException;
 
 import org.melati.poem.Setting;
 import org.melati.poem.TableMap;
 import org.melati.poem.TableSortedMap;
 
 /**
  * @author timp
  * @since 8 Jun 2007
  *
  */
 public class TableSortedMapTest extends TableMapTest {
   private TableSortedMap it;
   private TableSortedMap noArg;
 
   /**
    * @param name
    */
   public TableSortedMapTest(String name) {
     super(name);
   }
 
   /** 
    * {@inheritDoc}
    * @see org.melati.poem.test.TableMapTest#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
   
   protected void createObjectsUnderTest() {
     it = new TableSortedMap(getDb().getUserTable());
     noArg = new TableSortedMap();
   }
   protected TableMap getObjectUnderTest() { 
     return it;
   }
   protected TableMap getNoArgObjectUnderTest() { 
     return noArg;
   }
 
   /** 
    * {@inheritDoc}
    * @see org.melati.poem.test.TableMapTest#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#TableSortedMap()}.
    */
   public void testTableSortedMap() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#TableSortedMap(org.melati.poem.Table)}.
    */
   public void testTableSortedMapTable() {
     
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#comparator()}.
    */
   public void testComparator() {
     assertNull(it.comparator());
     assertNull(noArg.comparator());
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#firstKey()}.
    */
   public void testFirstKey() {
     assertEquals(new Integer(0), it.firstKey());
     it.setTable(getDb().getSettingTable());
     try { 
       it.firstKey();
       fail("Should have bombed.");
     } catch (NoSuchElementException e) { 
       e = null;
     }
     Setting s1 = getDb().getSettingTable().ensure("s1", "s1", "S1", "test setting S1");
     Setting s2 = getDb().getSettingTable().ensure("s2", "s2", "S2", "test setting S2");
     s1.delete();
     assertEquals(new Integer(1), it.firstKey());
     s2.delete();
     try { 
       noArg.firstKey();
       fail("Should have bombed");
     } catch (NullPointerException e) { 
       e = null;
     }
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#lastKey()}.
    */
   public void testLastKey() {
    assertEquals(new Integer(1), it.lastKey());
     it.setTable(getDb().getSettingTable());
     try { 
       it.lastKey();
       fail("Should have bombed.");
     } catch (NoSuchElementException e) { 
       e = null;
     }
     try { 
       noArg.lastKey();
       fail("Should have bombed");
     } catch (NullPointerException e) { 
       e = null;
     }    
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#subMap(java.lang.Object, java.lang.Object)}.
    */
   public void testSubMap() {
     try { 
       it.subMap(new Integer(0),new Integer(1));
       fail("Should have bombed");
     } catch (UnsupportedOperationException e) { 
       e = null;
     }                    
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#headMap(java.lang.Object)}.
    */
   public void testHeadMap() {
     try { 
       it.headMap(new Integer(1));
       fail("Should have bombed");
     } catch (UnsupportedOperationException e) { 
       e = null;
     }                
   }
 
   /**
    * Test method for {@link org.melati.poem.TableSortedMap#tailMap(java.lang.Object)}.
    */
   public void testTailMap() {
     try { 
       it.tailMap(new Integer(1));
       fail("Should have bombed");
     } catch (UnsupportedOperationException e) { 
       e = null;
     }                
   }
 
 }
