 package fi.helsinki.cs.tmc.utilities.zip;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Writer;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class NbProjectUnzipperTest {
     
    private final File tempRoot = new File("tmp/test");
     private ByteArrayOutputStream zipBuffer;
     private ZipOutputStream zipOut;
     
     private NbProjectUnzipper unzipper;
     
     @Before
     public void setUp() throws IOException {
         FileUtils.forceMkdir(tempRoot);
         FileUtils.forceDelete(tempRoot);
         FileUtils.forceMkdir(tempRoot);
         
         zipBuffer = new ByteArrayOutputStream();
         zipOut = new ZipOutputStream(zipBuffer);
         
         unzipper = new NbProjectUnzipper();
     }
     
     @After
     public void tearDown() throws IOException {
         FileUtils.forceDelete(tempRoot);
     }
     
     private void addFakeProjectToZip(String path, String name) throws IOException {
         writeDirToZip(path + "/");
         writeDirToZip(path + "/nbproject/");
         writeFileToZip(path + "/nbproject/project.xml", "Fake project.xml of " + name);
         writeDirToZip(path + "/src/");
         writeFileToZip(path + "/src/Hello.java", "Fake Java file of " + name);
     }
     
     private void writeDirToZip(String path) throws IOException {
         assertTrue(path.endsWith("/"));
         zipOut.putNextEntry(new ZipEntry(path));
     }
 
     private void writeFileToZip(String path, String content) throws IOException {
         ZipEntry zent = new ZipEntry(path);
         zipOut.putNextEntry(zent);
         Writer w = new OutputStreamWriter(zipOut, "UTF-8");
         w.write(content);
         w.flush();
     }
     
     @Test
     public void itShouldUnzipTheFirstProjectDirectoryItSeesInAZip() throws IOException {
         addFakeProjectToZip("dir1/dir12/project1", "P1");
         addFakeProjectToZip("dir2/project2", "P2");
         addFakeProjectToZip("project3", "P3");
         zipOut.close();
         
         unzipper.unzipProject(zipBuffer.toByteArray(), tempRoot, "my-project");
         
         assertEquals(1, tempRoot.listFiles().length);
         String contents = FileUtils.readFileToString(new File(tempRoot.getAbsolutePath() + File.separator + "my-project/nbproject/project.xml"));
         assertEquals("Fake project.xml of P1", contents);
         contents = FileUtils.readFileToString(new File(tempRoot.getAbsolutePath() + File.separator + "my-project/src/Hello.java"));
         assertEquals("Fake Java file of P1", contents);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void itShouldFailIfTheZipContainsNoProjectDirectory() throws IOException {
         writeDirToZip("dir1/");
         writeDirToZip("dir1/dir2/");
         writeFileToZip("dir1/dir2/oops.txt", "oops");
         zipOut.close();
         
         unzipper.unzipProject(zipBuffer.toByteArray(), tempRoot, "my-project");
     }
 }
