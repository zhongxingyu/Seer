 package com.HuskySoft.metrobike.ui.utility.test;
 
 import com.HuskySoft.metrobike.ui.utility.History;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 /**
  * JUnit test class to test History class. Noted that wrong junit test import
  * before.
  * 
  * @author Sam Wilson
  * 
  */
 public class HistoryTest extends TestCase {
 
     /**
      * Dump value to test.
      */
     private static final String TEST_STRING = "Dump String";
 
     /**
      * The history object that we are going to test.
      */
     private static History history;
 
     /**
      * before setup.
      */
     public final void setup() {
         history = History.getInstance();
 
     }
 
     /**
      * Black box test: test this class is singleton or not.
      */
     public final void testGetInstance() {
         History firstHistoryObject = History.getInstance();
         History secondHistoryObject = History.getInstance();
         Assert.assertTrue(firstHistoryObject.equals(secondHistoryObject));
     }
 
     /**
      * White box test: test addAddress(String address).
      */
     public final void testAddSingleAddress01() {
         setup();
         // add a single address
         history.addAddress(TEST_STRING);
         Assert.assertTrue("Add single address", history.getHistory().contains(TEST_STRING));
         history.deleteAddress(TEST_STRING);
     }
 
     /**
      * White box test: test addAddress(String address).
      */
     public final void testAddSingleEmptyAddress02() {
         setup();
         // add a single address
         history.addAddress("");
         Assert.assertTrue("Add single empty address", history.getHistory().contains(""));
         history.deleteAddress("");
     }
 
     /**
      * White box test: test addAddress(String address).
      */
     public final void testAddSingleDuplicateAddress03() {
         setup();
         // add 2 address
         history.addAddress(TEST_STRING);
         history.addAddress(TEST_STRING);
         Assert.assertTrue("Add single duplicate address", history.getHistory()
                 .contains(TEST_STRING));
         history.deleteAddress(TEST_STRING);
     }
 
     /**
      * White box test: test addAddress(String address).
      */
     public final void testAddSingleNullAddress04() {
         setup();
         int expected = history.getSize();
         String str = null;
         // add a single address
         history.addAddress(str);
         int actual = history.getSize();
         Assert.assertTrue("Add null address", expected == actual);
     }
 
     /**
      * White box test: test addAddress(String address).
      */
     public final void testAddSingleDuplicateNullAddress04() {
         setup();
         int expected = history.getSize();
         String str = null;
         // add a single address
         history.addAddress(str);
         history.addAddress(str);
         // should not be crash
         int actual = history.getSize();
         Assert.assertTrue("Add 2 null address", expected == actual);
     }
 
     /**
      * White box test: test addAddress(String[] address).
      */
     public final void testAddArrayOfAddress01() {
         setup();
         String[] strArray = { "a", "b", "c", "d", "e" };
         // add array address
         history.addAddress(strArray);
         // should not be crash
         Assert.assertTrue(history.getHistory().contains(strArray[0]));
         deleteArrayAfterTesting(strArray);
     }
 
     /**
      * White box test: test addAddress(String[] address).
      */
     public final void testAddArrayOfAddress02() {
         setup();
         String[] strArray = { "a", "b", "c", "d", "e" };
         int expected = history.getSize() + strArray.length;
         // add array address
         history.addAddress(strArray);
         int actual = history.getSize();
         Assert.assertEquals(expected, actual);
         deleteArrayAfterTesting(strArray);
     }
 
     /**
      * White box test: test addAddress(String[] address).
      */
     public final void testAddArrayOfDuplicateAddress03() {
         setup();
         String[] strArray = { "a", "a", "a", "a", "a" };
         // add array address
         history.addAddress(strArray);
         // should not be crash
         Assert.assertTrue(history.getHistory().contains(strArray[0]));
         deleteArrayAfterTesting(strArray);
     }
 
     /**
      * White box test: test addAddress(String[] address).
      */
     public final void testAddArrayOfDuplicateAddress04() {
         setup();
         String[] strArray = { "a", "a", "a", "a", "a" };
         int expected = history.getSize() + 1;
         // add array address
         history.addAddress(strArray);
         int actual = history.getSize();
         Assert.assertEquals(expected, actual);
         deleteArrayAfterTesting(strArray);
     }
 
     /**
      * White box test: test getAddress(int index).
      */
     public final void testGetAddedAddress01() {
         setup();
         int index = history.getSize();
         // add array address
         history.addAddress(TEST_STRING);
         String actual = history.getAddress(index);
         Assert.assertEquals(TEST_STRING, actual);
         history.deleteAddress(TEST_STRING);
     }
 
     /**
      * White box test: test deleteAll().
      */
     public final void testDeleteAllAddress01() {
         setup();
         // add array address
         history.deleteAll();
         int actual = history.getSize();
         Assert.assertEquals(0, actual);
     }
 
     /**
      * White box test: test delete address (int index).
      */
     public final void testDeleteAddress01() {
         setup();
         history.addAddress(TEST_STRING);
         history.deleteAddress(0);
        Assert.assertTrue(history.getSize() == 0);
     }
 
     /**
      * helper method for deleting the string array that has been added.
      * 
      * @param strArray
      *            the array that need to be deleted
      */
     private void deleteArrayAfterTesting(final String[] strArray) {
         for (int i = 0; i < strArray.length; i++) {
             history.deleteAddress(strArray[i]);
         }
     }
 
 }
