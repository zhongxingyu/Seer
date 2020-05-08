 package com.google.code.maven_replacer_plugin.include;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
import java.io.File;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
import org.apache.commons.io.FileUtils;
import org.junit.Before;
 import org.junit.Test;
 
 public class FileSelectorTest {
	private static final String TEST_FILE = "maven-replacer-plugin-test-file";
	private static final String BACK_DIR_SYMBOL = "..";
	
	private FileSelector selector;

	@Before
	public void setUp() {
		selector = new FileSelector();
	}
	
 	@Test
 	public void shouldReturnMultipleFilesToInclude() {
 		List<String> files = selector.listIncludes("test", asList("include1", "file*"), asList("file3"));
 		assertEquals(3, files.size());
 		assertEquals("file1", files.get(0));
 		assertEquals("file2", files.get(1));
 		assertEquals("include1", files.get(2));
 	}
 	
 	@Test
 	public void shouldSupportNoExcludes() {
 		List<String> files = selector.listIncludes("test", asList("include1", "file*"), null);
 		assertEquals(4, files.size());
 		assertEquals("file1", files.get(0));
 		assertEquals("file2", files.get(1));
 		assertEquals("file3", files.get(2));
 		assertEquals("include1", files.get(3));
 	}
 	
 	@Test
 	public void shouldReturnEmptyListWhenEmptyIncludes() {
 		assertTrue(selector.listIncludes("test", null, asList("file3")).isEmpty());
 		assertTrue(selector.listIncludes("test", new ArrayList<String>(), asList("file3")).isEmpty());
 	}
	
	@Test
	public void shouldSelectFilesInBackDirectories() throws IOException {
		File file = new File(BACK_DIR_SYMBOL + File.separator + TEST_FILE);
		file.deleteOnExit();
		FileUtils.writeStringToFile(file, "test");
		
		List<String> files = selector.listIncludes(BACK_DIR_SYMBOL, asList(TEST_FILE), null);
		assertEquals(1, files.size());
		assertEquals(TEST_FILE, files.get(0));
	}
 }
