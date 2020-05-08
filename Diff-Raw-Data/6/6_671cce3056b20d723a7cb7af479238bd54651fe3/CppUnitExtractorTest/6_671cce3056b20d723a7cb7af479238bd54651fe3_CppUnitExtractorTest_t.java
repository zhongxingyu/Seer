 package org.pescuma.buildhealth.extractor.xunit;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.File;
 import java.io.InputStream;
 
 import org.junit.Test;
 import org.pescuma.buildhealth.extractor.BaseExtractorTest;
 import org.pescuma.buildhealth.extractor.PseudoFiles;
 import org.pescuma.buildhealth.extractor.unittest.xunit.CppUnitExtractor;
 
 public class CppUnitExtractorTest extends BaseExtractorTest {
 	
 	@Test
 	public void test6PassedGeral() {
 		InputStream stream = load("cppunit.9passed.1error.xml");
 		
 		CppUnitExtractor extractor = new CppUnitExtractor(new PseudoFiles(stream));
 		
 		extractor.extractTo(table, tracker);
 		
 		verify(tracker).onStreamProcessed();
 		verify(tracker, never()).onFileProcessed(any(File.class));
 		
		assertTable(20, 10, table.filter("Unit test", "C++", "CppUnit"));
 		assertTable(9, 9, table.filter("Unit test").filter(3, "passed"));
 		assertTable(1, 1, table.filter("Unit test").filter(3, "error"));
		
		// This should not be here, but the converter puts it here
		assertTable(10, 0, table.filter("Unit test").filter(3, "time"));
 	}
 	
 }
