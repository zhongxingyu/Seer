 package edu.upenn.cis350.test;
 
 import edu.upenn.cis350.IOBasic;
 import junit.framework.TestCase;
 
 
 public class IOBasicTest extends TestCase {
 	public void testReading()
 	{
//		IOBasic.initRead(null);
 		
 		assertEquals(IOBasic.fullName("zhangka"),"Alex Zhang");
 		assertEquals(IOBasic.fullName("pgurns"),"Paul Gurniak");
 		assertEquals(IOBasic.password("sriramr"),"sriramr");
 		
 		assertEquals(IOBasic.fullName("something that doesn't exist"),null);
 	}
 	
 	public void testWriting()
 	{
//		IOBasic.initRead(null);
 		IOBasic.addUser("herp", "derp", "Herp Derp");
//		IOBasic.finalWrite(null);
 		
//		IOBasic.initRead(null);
 		
 		assertEquals(IOBasic.fullName("herp"),"Herp Derp");
 		assertEquals(IOBasic.fullName("zhangka"),"Alex Zhang");
 		assertEquals(IOBasic.fullName("abaldwin"),"abaldwin");
 		
 	}
 	
 	public void testErrorCheck()
 	{
//		IOBasic.initRead(null);
 		
 		assertEquals(IOBasic.addUser("herp", "derp", "Herp Derp"),true);
 		assertEquals(IOBasic.addUser("herp", "derp", "Herp Derp"),false);
 		
 		assertEquals(IOBasic.fullName("boooya"),null);
 		assertEquals(IOBasic.getPoints("boooya"),-1);
 		
 		assertEquals(IOBasic.setPoints("something stupid", 100000),false);
 		assertEquals(IOBasic.password("something stupid"),null);
 	}
 	
 	public void testPoints()
 	{
//		IOBasic.initRead(null);
 		
 		assertEquals(IOBasic.getPoints("pgurns"),0);
 		assertEquals(IOBasic.getPoints("abaldwin"),100);
 		
 		IOBasic.setPoints("pgurns", 1);
 		assertEquals(IOBasic.getPoints("pgurns"),1);
 		
 		IOBasic.addUser("herp", "derp", "Herp Derp");
 		assertEquals(IOBasic.getPoints("herp"),0);
 	
 	}
 	
 }
