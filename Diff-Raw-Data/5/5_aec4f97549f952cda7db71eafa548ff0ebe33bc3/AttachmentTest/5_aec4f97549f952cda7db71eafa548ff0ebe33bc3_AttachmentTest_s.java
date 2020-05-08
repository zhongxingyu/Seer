 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.Attachment;
 
 import play.modules.morphia.Blob;
 import play.modules.morphia.MorphiaPlugin;
 
 import org.jcrom.JcrFile;
 import org.jcrom.JcrMappingException;
 
 import java.io.File;
 
 public class AttachmentTest extends UnitTest {
   
   @Test
   public void aVeryImportantThingToTest() {
     assertEquals(2, 1 + 1);
   }
 
   @Test
   public void testStoreAttachment() {
     File f = new File("test/test.txt");
    Attachment a = new Attachment("derp/derp", "test attachment", 0, 0, f);
     a.save();
     assertNotNull(a.getNode());
     assertNotNull(a.getFile());
     a.delete();
   }
   
   @Test
   public void testCreateNode(){
     File f = new File("test/test.txt");
    Attachment a = new Attachment("derp/derp", "test attachment", 0, 0, f);
     a.save();
     a.createNode(f);
     assertNotNull(a.getNode());
     assertNotNull(a.getFile());
     a.delete();
   }
 }
