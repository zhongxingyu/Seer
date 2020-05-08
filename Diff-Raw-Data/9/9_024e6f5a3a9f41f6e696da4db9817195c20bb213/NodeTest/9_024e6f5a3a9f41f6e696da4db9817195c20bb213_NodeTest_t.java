 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.Node;
 
 import java.io.File;
 
 import play.libs.MimeTypes;
 
 import org.jcrom.JcrFile;
 import org.jcrom.JcrMappingException;
 
 // import play.modules.morphia.Blob;
 // import play.modules.morphia.MorphiaPlugin;
 
 public class NodeTest extends UnitTest {
   
   @Test
   public void aVeryImportantThingToTest() {
     assertEquals(2, 1 + 1);
   }
 
   @Test
   public void testStoreNode() {
     Node n = new Node("derp/derp", "test node");
     n.save();
     n.delete();
   }
   
   @Test
   public void testAttachFile() {
     Node n = new Node("derp", "test node");
    File f = new File("test/test.txt");
     n.file = JcrFile.fromFile("test", f, MimeTypes.getContentType(f.getName()));
 
     n.save();
     n.delete();
   }
 
   @Test
   public void testRetrieveFile() {
     List<Node> nodes = Node.findAll();
     Node n = nodes.get(0);
     assertNotNull(n);
     // System.out.println(n.getId());
     // System.out.println(n.file.getMimeType());
     // System.out.println(nodes.size());
     // System.out.println(nodes.get(0));
     assertNotNull(nodes);
   }
 }
 
