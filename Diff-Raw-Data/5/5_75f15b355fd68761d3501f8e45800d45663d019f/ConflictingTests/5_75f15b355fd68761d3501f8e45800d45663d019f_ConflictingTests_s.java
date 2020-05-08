 package org.openflexo.hannah;
 
 import static org.openflexo.hannah.TestUtil.assertContents;
 import static org.openflexo.hannah.TestUtil.writeFile;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class ConflictingTests {
 
 	private File baseFolder = new File("tmp/conflicting");
 	
 	private VersionnedFileGenerator createGenerator(String name) {
 		return new VersionnedFileGenerator(new File(baseFolder, name));
 	}
 	
 	@Test
 	public void testOneFile1() throws IOException {
 		VersionnedFileGenerator generator = createGenerator("oneFile1");
 		generator.start(null);
 		generator.generate("file1.txt", "abc\ndef\nijk\n");
 		generator.end(ConflictHandler.userResolution);
 		
		assertContents(generator, "file1.txt", "abc");
 		
 		writeFile(generator, "file1.txt", "abc\nddd\nijk\n");
 		assertContents(generator, "file1.txt", "abc\nddd\nijk\n");
 		
 		generator.start(null);
 		generator.generate("file1.txt", "abc\nfed\nijk\n");
 		generator.end(ConflictHandler.userResolution);
 		
 		assertContents(generator, "file1.txt", "abc\nddd\nijk\n");
 	}
 
 	@Test
 	public void testOneFile2() throws IOException {
 		VersionnedFileGenerator generator = createGenerator("oneFile2");
 		generator.start(null);
 		generator.generate("file1.txt", "abc\ndef\nijk\n");
 		generator.end(ConflictHandler.generationResolution);
 		
		assertContents(generator, "file1.txt", "abc");
 		
 		writeFile(generator, "file1.txt", "abc\nddd\nijk\n");
 		assertContents(generator, "file1.txt", "abc\nddd\nijk\n");
 		
 		generator.start(null);
 		generator.generate("file1.txt", "abc\nfed\nijk\n");
 		generator.end(ConflictHandler.generationResolution);
 		
 		assertContents(generator, "file1.txt", "abc\nfed\nijk\n");
 	}
 	
 
 }
