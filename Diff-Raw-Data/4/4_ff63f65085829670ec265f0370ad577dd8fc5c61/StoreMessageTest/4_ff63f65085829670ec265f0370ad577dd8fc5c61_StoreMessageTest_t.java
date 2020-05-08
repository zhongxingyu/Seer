 package com.vreco.boomerang;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.UUID;
 import org.junit.*;
 
 /**
  *
  * @author Ben Aldrich
  */
 public class StoreMessageTest {
 
   StoreMessage store;
 
   public StoreMessageTest() {
   }
 
   @BeforeClass
   public static void setUpClass() throws Exception {
   }
 
   @AfterClass
   public static void tearDownClass() throws Exception {
   }
 
   @Before
   public void setUp() {
     store = new StoreMessage("localhost", "Boomerang");
   }
 
   @After
   public void tearDown() {
   }
 
   /**
    * Test of get method, of class StoreMessage.
    */
   @Test
   public void testSimple() {
     Date date = new Date();
     store.set("woo", "{woo}", date);
     String result = store.get("woo", date);
     store.delete("woo", date);
     Assert.assertEquals("{woo}", result);
   }
 
   /**
    * Test of getKeys method, of class StoreMessage.
    */
   @Test
   public void testSetKeysSimple() {
     for (int i = 0; i <= 2000; i++) {
       String k = UUID.randomUUID().toString();
       Date date = new Date();
       store.set(k, "{\"processName\":\"proc\",\"uuid\":\"" + k + "\"}", date);
     }
 
   }
 
 
   /**
    * Test of exists method, of class StoreMessage.
    */
   @Test
   public void testExists() {
     Date date = new Date();
     store.set("1", "{woo}", date);
    boolean exists = store.exists("1", date);
     store.deleteAll();
    Assert.assertTrue(exists); 
   }
 
   @Test
   public void testExistsFalse() {
     Date date = new Date();
     Assert.assertFalse(store.exists("1", date));
   }
 
   @Test
   public void testgetHashKey() throws ParseException {
     Date parse = store.sdf.parse("201207311048");
     Assert.assertEquals("woo201207311048", store.getHashKey("woo", parse));
   }
 }
