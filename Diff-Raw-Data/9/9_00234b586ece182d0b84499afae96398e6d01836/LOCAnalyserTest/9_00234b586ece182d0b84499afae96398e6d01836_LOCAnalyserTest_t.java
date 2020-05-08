 package org.pescuma.buildhealth.analyser.loc;
 
 import static org.junit.Assert.*;
 import static org.pescuma.buildhealth.core.BuildStatus.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.pescuma.buildhealth.analyser.BaseAnalyserTest;
 import org.pescuma.buildhealth.core.Report;
 
 public class LOCAnalyserTest extends BaseAnalyserTest {
 	
 	private LOCAnalyser analyser;
 	
 	@Before
 	public void setUp() {
 		analyser = new LOCAnalyser();
 		
 		super.setUp(analyser);
 	}
 	
 	private void createSource(double size) {
 		data.add(size, "LOC", "java", "source");
 	}
 	
 	private void createComment(double size) {
 		data.add(size, "LOC", "java", "comment");
 	}
 	
 	private void createBlank(double size) {
 		data.add(size, "LOC", "java", "blank");
 	}
 	
 	private void createAll(double size) {
 		data.add(size, "LOC", "java", "all");
 	}
 	
 	private void createFiles(double size) {
 		data.add(size, "LOC", "java", "files");
 	}
 	
 	@Test
 	public void testJustAll() {
 		createAll(10);
 		createAll(20);
 		
 		assertEquals(new Report(Good, "Lines of code", "30"), createReport());
 	}
 	
 	@Test
 	public void testAllAndFiles() {
 		createAll(10);
 		createFiles(20);
 		
 		assertEquals(new Report(Good, "Lines of code", "10", "in 20 files"), createReport());
 	}
 	
 	@Test
 	public void testAllAndFilesSingular() {
 		createAll(1);
 		createFiles(1);
 		
 		assertEquals(new Report(Good, "Lines of code", "1", "in 1 file"), createReport());
 	}
 	
 	@Test
 	public void testAllAndFilesWihtUnits() {
 		createAll(12 * 1000);
 		createFiles(1 * 1000 * 1000);
 		
		assertEquals(new Report(Good, "Lines of code", "12.0 k", "in 1.0 M files"), createReport());
 	}
 	
 	@Test
 	public void testSourceBlankComments() {
 		createSource(123);
 		createBlank(12);
 		createComment(1234567);
 		
 		assertEquals(new Report(Good, "Lines of code", "1.2 M", "123 of sources, 1.2 M of comments, 12 blank lines"),
 				createReport());
 	}
 	
 	@Test
 	public void testSourceBlankCommentsAndFiles() {
 		createSource(123);
 		createBlank(12);
 		createComment(1234567);
 		createFiles(1);
 		
 		assertEquals(new Report(Good, "Lines of code", "1.2 M",
 				"123 of sources, 1.2 M of comments, 12 blank lines, in 1 file"), createReport());
 	}
 	
 	@Test
 	public void testSourceBlankCommentsAllAndFiles() {
 		createSource(123);
 		createBlank(12);
 		createComment(6);
 		createAll(66);
 		createFiles(1);
 		
 		assertEquals(new Report(Good, "Lines of code", "207",
 				"123 of sources, 6 of comments, 12 blank lines, 66 unknown, in 1 file"), createReport());
 	}
 	
 }
