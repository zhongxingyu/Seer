 package eu.nets.camel.route;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import java.io.File;
 import java.net.URL;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 @ContextConfiguration(locations = "classpath:META-INF/spring/spring-context.xml")
 public class ReceiveShipmentRouteTest extends AbstractJUnit4SpringContextTests
 {
     @Value("${nfs.dir}/inbound") private File fromDir;
     @Value("${local.dir}/inbound") private File toDir;
     @Value("${receipt.dir}") private File receiptDir;
 
     @Before
     public void cleanDirs() throws Exception {
         if (fromDir.isDirectory()) {
             FileUtils.forceDelete(fromDir.getParentFile());
         }
         if (toDir.isDirectory()) {
             FileUtils.forceDelete(toDir.getParentFile());
         }
         if (receiptDir.isDirectory()) {
             FileUtils.forceDelete(receiptDir);
         }
     }
 
     @Test
    @Ignore
     public void testMovingFileFromNetworkShareToLocal() throws Exception {
 
         File tmpDir = new File(fromDir, "tmp");
         assertThat(tmpDir.mkdirs()).isTrue();
 
         File file = new File(tmpDir, "jalla.txt");
         FileUtils.writeStringToFile(file, "This is a test");
         assertThat(file).isFile();
 
         FileUtils.moveFileToDirectory(file, fromDir, false);
 
         assertFileExists(new File(toDir, file.getName()));
     }
 
     @Test
     public void testReceiveInvalidShipment() throws Exception {
 
         File tmpDir = new File(toDir, "tmp");
         assertThat(tmpDir.mkdirs()).isTrue();
 
         File file = new File(tmpDir, "jalla.txt");
         FileUtils.writeStringToFile(file, "This is a test");
         assertThat(file).isFile();
 
         FileUtils.moveFileToDirectory(file, toDir, false);
 
         File receiptFile = new File(receiptDir, "jalla.txt");
         assertFileExists(receiptFile);
         assertThat(FileUtils.readFileToString(receiptFile)).contains("DID NOT pass validation.");
     }
 
     @Test
     public void testReceiveValidShipment() throws Exception {
 
         sendFile("testdata/shipment.xml");
 
         File receiptFile = new File(receiptDir, "shipment.xml");
         assertFileExists(receiptFile);
         assertThat(FileUtils.readFileToString(receiptFile)).contains("passed validation.");
     }
 
     private void sendFile(final String classpathResource) throws Exception {
 
         URL url = getClass().getClassLoader().getResource(classpathResource);
         assertThat(url).isNotNull();
 
         File tmpDir = new File(toDir, "tmp");
         if (!tmpDir.isDirectory()) {
             assertThat(tmpDir.mkdirs()).isTrue();
         }
 
         File sourceFile = new File(url.getFile());
         File targetFile = new File(tmpDir, sourceFile.getName());
         FileUtils.copyFile(sourceFile, targetFile);
         FileUtils.moveFileToDirectory(targetFile, toDir, false);
     }
 
     private void assertFileExists(final File file) {
         for (int i=0;i<10;i++) {
             if (file.isFile()) {
                 break;
             } else {
                 try
                 {
                     Thread.sleep(500);
                 } catch (InterruptedException e)
                 {
                 }
             }
         }
         assertThat(file).isFile();
     }
 
 }
