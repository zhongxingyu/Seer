 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eccdh.adt;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author rolf
  */
 public class BitStringTest {
 
     public BitStringTest() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testOctetStringConversion() {
         String[] bsval = {"0", "1", "00000000", "00000001", "10000000", "11111111", "000000000", "100000000"};
        String[] res = {"00000000", "00000001", "00000000", "00000001", "00000400", "00000377", "00000000", "00001000"};
 
         for (int i = 0; i < bsval.length; i++) {
             BitString bs = new BitString(bsval[i]);
             OctetString os = new OctetString(res[i]);
             OctetString osres = bs.toOctetString();
             assertEquals("Conversion " + (i+1) + " not correct", os, osres);
         }
     }
 
     @Test
     public void testStringConversion() {
         String[] bsval = {"0", "1", "00000000", "00000001", "10000000", "11111111", "000000000", "100000000"};
        String[] res = {"0", "1", "00000000", "00000001", "10000000", "11111111", "000000000", "100000000"};
 
         for (int i = 0; i < bsval.length; i++) {
             BitString bs = new BitString(bsval[i]);
             String stringres = bs.toString();
             assertEquals("Conversion " + i + " not correct", res[i], stringres);
         }
     }
 }
