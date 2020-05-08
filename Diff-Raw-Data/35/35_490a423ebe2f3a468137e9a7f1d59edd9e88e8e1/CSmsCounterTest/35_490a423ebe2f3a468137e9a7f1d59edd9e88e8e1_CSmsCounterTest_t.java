 package com.monster.pocketsafe.test;
 
 import com.monster.pocketsafe.CSmsCounter;
 
 import junit.framework.TestCase;
 
 public class CSmsCounterTest extends TestCase {
 	
 	public void test0() {
 		CSmsCounter sut = new CSmsCounter("");
 		
 		assertEquals(1, sut.getCount());
 		assertEquals(0, sut.getLen());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getNextLimit());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getOst());
 		assertEquals("160/160", sut.toString());
 	}
 
 	public void test3eng() {
 		CSmsCounter sut = new CSmsCounter("abc");
 		
 		assertEquals(1, sut.getCount());
 		assertEquals(3, sut.getLen());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getNextLimit());
 		assertEquals(CSmsCounter.LEN_LONG-3, sut.getOst());
 		assertEquals("157/160", sut.toString());
 	}
 
 	public void test160eng() {
 		String txt = new String();
 		for (int i=0; i<160; i++) {
 			txt+="a";
 		}
 		
 		CSmsCounter sut = new CSmsCounter(txt);
 		
 		assertEquals(1, sut.getCount());
 		assertEquals(160, sut.getLen());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getNextLimit());
 		assertEquals(0, sut.getOst());
 		assertEquals("0/160", sut.toString());
 	}
 	
 	public void test161eng() {
 		String txt = new String();
 		for (int i=0; i<161; i++) {
 			txt+="a";
 		}
 		
 		CSmsCounter sut = new CSmsCounter(txt);
 		
 		assertEquals(2, sut.getCount());
 		assertEquals(161, sut.getLen());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_LONG*2, sut.getNextLimit());
 		assertEquals(159, sut.getOst());
 		assertEquals("159/320(2)", sut.toString());
 	}	
 	
 	public void test320eng() {
 		String txt = new String();
 		for (int i=0; i<320; i++) {
 			txt+="a";
 		}
 		
 		CSmsCounter sut = new CSmsCounter(txt);
 		
 		assertEquals(2, sut.getCount());
 		assertEquals(320, sut.getLen());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_LONG*2, sut.getNextLimit());
 		assertEquals(0, sut.getOst());
 		assertEquals("0/320(2)", sut.toString());
 	}	
 	
 	public void test321eng() {
 		String txt = new String();
 		for (int i=0; i<321; i++) {
 			txt+="a";
 		}
 		
 		CSmsCounter sut = new CSmsCounter(txt);
 		
 		assertEquals(3, sut.getCount());
 		assertEquals(321, sut.getLen());
 		assertEquals(CSmsCounter.LEN_LONG, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_LONG*3, sut.getNextLimit());
 		assertEquals(159, sut.getOst());
 		assertEquals("159/480(3)", sut.toString());
 	}	
 	
 	public void test3rus() {
 		CSmsCounter sut = new CSmsCounter("xx");
 		
 		assertEquals(1, sut.getCount());
 		assertEquals(3, sut.getLen());
 		assertEquals(CSmsCounter.LEN_SMALL, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_SMALL, sut.getNextLimit());
 		assertEquals(CSmsCounter.LEN_SMALL-3, sut.getOst());
		assertEquals("67/70", sut.toString());
 	}
 
	public void test70rus() {
 		String txt = new String();
		for (int i=0; i<70; i++) {
 			txt+="";
 		}
 		
 		CSmsCounter sut = new CSmsCounter(txt);
 		
 		assertEquals(1, sut.getCount());
		assertEquals(70, sut.getLen());
 		assertEquals(CSmsCounter.LEN_SMALL, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_SMALL, sut.getNextLimit());
 		assertEquals(0, sut.getOst());
		assertEquals("0/70", sut.toString());
 	}
 	
	public void test71rus() {
 		String txt = new String();
		for (int i=0; i<71; i++) {
 			txt+="";
 		}
 		
 		CSmsCounter sut = new CSmsCounter(txt);
 		
 		assertEquals(2, sut.getCount());
		assertEquals(71, sut.getLen());
 		assertEquals(CSmsCounter.LEN_SMALL, sut.getLenOne());
 		assertEquals(CSmsCounter.LEN_SMALL*2, sut.getNextLimit());
		assertEquals(69, sut.getOst());
		assertEquals("69/140(2)", sut.toString());
 	}		
 }
