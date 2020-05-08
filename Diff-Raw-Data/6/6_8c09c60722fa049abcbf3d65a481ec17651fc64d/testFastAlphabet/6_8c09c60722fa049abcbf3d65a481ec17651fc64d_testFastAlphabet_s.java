 package test.FastChainAlgoritms.FastChain;
 
 import junit.framework.TestCase;
 import libiada.FastChainAlgorithms.FastChain.FastAlphabet;
 
 /**
  * Created by IntelliJ IDEA.
  * User: alex
  * Date: 20.01.12
  * Time: 1:13
  */
 public class testFastAlphabet extends TestCase {
     private FastAlphabet alphabet = new FastAlphabet();
 
     public void testAddGet() throws Exception {
         alphabet.add("123");
         alphabet.add("11");
 
         assertEquals("123", alphabet.get(0));
         assertEquals("11", alphabet.get(1));
     }
 
     public void testAddNull() throws Exception {
         try {
             alphabet.add(null);
         } catch (Exception e) {
             return;
         }
         fail();
     }
 
     public void testAddEmptyString() throws Exception {
         try {
             alphabet.add("");
         } catch (Exception e) {
             return;
         }
         fail();
     }
 
     public void testAddDuplicateValue() throws Exception {
         alphabet.add("1");
         try {
             alphabet.add("1");
         } catch (Exception e) {
             return;
         }
         fail();
     }
 
     public void testIndexOf() throws Exception {
         alphabet.add("1");
         alphabet.add("2");
         alphabet.add("12");
 
         assertEquals(alphabet.indexOf("1"), 0);
         assertEquals(alphabet.indexOf("2"), 1);
         assertEquals(alphabet.indexOf("12"), 2);
     }
 
     public void testSize() throws Exception {
         alphabet.add("1");
         alphabet.add("2");
         alphabet.add("12");
         assertEquals(alphabet.size(), 3);
     }
 }
