 /**
  * @author Dominik Hansen <Dominik.Hansen at hhu.de>
  **/
 
 package de.tla2b.examples;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import de.tla2b.util.TestUtil;
 
 
 public class ExamplesForDistribution {
 
 	@Test
 	public void testAsynchInterface() throws Exception {
 		String path = "src/test/resources/examples/AsynchInterface/";
 		StringBuilder sb = TestUtil.translate(path + "AsynchInterface.tla", "AsynchInterface.cfg");
 		String expected = TestUtil.fileToString(path + "AsynchInterface.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	
 	@Test
 	public void testSumAndProduct() throws Exception {
 		String path = "src/test/resources/examples/SumAndProduct/";
 		StringBuilder sb = TestUtil.translate(path + "SumAndProductTransition.tla");
 		System.out.println(sb);
 		//String expected = TestUtil.fileToString(path + "AsynchInterface.mch");
 		//assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testMySequence() throws Exception {
 		String path = "src/test/resources/examples/MySequence/";
 		StringBuilder sb = TestUtil.translate(path + "MySequence.tla");
 		System.out.println(sb);
 		//String expected = TestUtil.fileToString(path + "AsynchInterface.mch");
 		//assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	
 	@Test
 	public void testChannel() throws Exception {
 		String path = "src/test/resources/examples/Channel/";
 		StringBuilder sb = TestUtil.translate(path + "Channel.tla", "Channel.cfg");
 		String expected = TestUtil.fileToString(path + "Channel.mch");
		//TODO order elements of a enumerated set
		//assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testClub() throws Exception {
 		String path = "src/test/resources/examples/Club/";
 		StringBuilder sb = TestUtil.translate(path + "Club.tla", "Club.cfg");
 		String expected = TestUtil.fileToString(path + "Club.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testCounter() throws Exception {
 		String path = "src/test/resources/examples/Counter/";
 		StringBuilder sb = TestUtil.translate(path + "Counter", "Counter.cfg");
 		String expected = TestUtil.fileToString(path + "Counter.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testDieHard() throws Exception {
 		String path = "src/test/resources/examples/DieHard/";
 		StringBuilder sb = TestUtil.translate(path + "DieHard", "DieHard.cfg");
 		String expected = TestUtil.fileToString(path + "DieHard.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 
 
 	@Test
 	public void testDieHarder() throws Exception {
 		String path = "src/test/resources/examples/DieHard/";
 		StringBuilder sb = TestUtil.translate(path + "DieHarder", "DieHarder.cfg");
 		String expected = TestUtil.fileToString(path + "DieHarder.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testDoors() throws Exception {
 		String path = "src/test/resources/examples/Doors/";
 		StringBuilder sb = TestUtil.translate(path + "Doors", "Doors.cfg");
 		String expected = TestUtil.fileToString(path + "Doors.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testGraphIso() throws Exception {
 		String path = "src/test/resources/examples/GraphIso/";
 		StringBuilder sb = TestUtil.translate(path+ "GraphIso", "GraphIso.cfg");
 		String expected = TestUtil.fileToString(path + "GraphIso.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 
 	@Test
 	public void testHourClock() throws Exception {
 		String path = "src/test/resources/examples/HourClock/";
 		StringBuilder sb = TestUtil.translate(path + "HourClock", "HourClock.cfg");
 		String expected = TestUtil.fileToString(path + "HourClock.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testJukebox() throws Exception {
		String path = "src/test/resources/examples/Jukebox/";
 		StringBuilder sb = TestUtil.translate(path + "Jukebox", "Jukebox.cfg");
 		String expected = TestUtil.fileToString(path + "Jukebox.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testQueens() throws Exception {
 		String path = "src/test/resources/examples/Queens/";
 		StringBuilder sb = TestUtil.translate(path + "Queens", "Queens.cfg");
 		String expected = TestUtil.fileToString(path + "Queens.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testQueens2() throws Exception {
 		String path = "src/test/resources/examples/Queens/";
 		StringBuilder sb = TestUtil.translate(path + "Queens2", "Queens2.cfg");
 		String expected = TestUtil.fileToString(path + "Queens2.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testRecursiveFunction() throws Exception {
 		String path = "src/test/resources/examples/RecursiveFunction/";
 		StringBuilder sb = TestUtil.translate(path + "RecursiveFunction");
 		String expected = TestUtil.fileToString(path + "RecursiveFunction.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testScheduler() throws Exception {
 		String path = "src/test/resources/examples/Scheduler/";
 		StringBuilder sb = TestUtil.translate(path + "Scheduler", "Scheduler.cfg");
 		String expected = TestUtil.fileToString(path + "Scheduler.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testSimpleSATProblem() throws Exception {
 		String path = "src/test/resources/examples/SimpleSATProblem/";
 		StringBuilder sb = TestUtil.translate(path + "SimpleSATProblem");
 		String expected = TestUtil.fileToString(path + "SimpleSATProblem.mch");
 		System.out.println(sb);
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	@Test
 	public void testDefCapture() throws Exception {
 		String path = "src/test/resources/examples/DefCapture/";
 		StringBuilder sb = TestUtil.translate(path + "DefCapture");
 		String expected = TestUtil.fileToString(path + "DefCapture.mch");
 		assertEquals(TestUtil.getTreeAsString(expected), TestUtil.getTreeAsString(sb.toString()));
 	}
 	
 	
 }
