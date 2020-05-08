 package info.mikaelsvensson.devtools.xml.documentcreator;
 
 import enumeration.Fruit;
 import info.mikaelsvensson.devtools.*;
import info.mikaelsvensson.devtools.doclet.shared.DocumentCreatorFactoryException;
 import info.mikaelsvensson.devtools.doclet.xml.documentcreator.extensive.ExtensiveDocumentCreator;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 public class ExtensiveDocumentCreatorTest extends AbstractDocumentCreatorTest {
     @Test
     public void testClassA() throws Exception {
         performTest(ClassA.class,
                 "-format.property." + ExtensiveDocumentCreator.EXCLUDE_PACKAGE,
                 "java");
     }
 
     @Test
     public void testFruit() throws Exception {
         performTest(Fruit.class);
     }
 
     @Test
     public void testSourceFileInlineTag() throws Exception {
         performTest(EmbedSourceCodeNoNS.class);
     }
 
     @Test
     public void testEmbedSourceCodeDefaultNS() throws Exception {
         performTest(EmbedSourceCodeDefaultNS.class);
     }
 
     @Test
     public void testEmbedSourceCodeRegularNS() throws Exception {
         performTest(EmbedSourceCodeRegularNS.class);
     }
 
     @Test
     public void testEmbedSourceCodeNoNS() throws Exception {
         performTest(EmbedSourceCodeNoNS.class);
     }
 
     @Test
     public void testAnnotated() throws Exception {
         performTest(AnnotatedClass.class,
                 "-format.property." + ExtensiveDocumentCreator.SHOW_ANNOTATIONS,
                 "true");
     }
 
     @Test
     public void testContact() throws Exception {
         performTest(
                 Contact.class,
                 "-format.property." + ExtensiveDocumentCreator.SHOW_ANNOTATIONS,
                 "true");
     }
 
     @Test
     @Ignore
     public void testVehicle() throws Exception {
         performTest(
                 Vehicle.class,
                 "-format.property." + ExtensiveDocumentCreator.CLASS_MEMBER_TYPE_FILTER,
                 "si",
                 "-format.property." + ExtensiveDocumentCreator.INTERFACE_MEMBER_TYPE_FILTER,
                 "si");
     }
 
    private void performTest(final Class<?> testClass, String... documentCreatorArgs) throws IOException, URISyntaxException, SAXException, ParserConfigurationException, DocumentCreatorFactoryException {
         performTest(ExtensiveDocumentCreator.NAME, testClass, documentCreatorArgs);
     }
 
     protected Node findClassElement(final Class cls, final Document doc) {
         return AbstractDocumentCreatorTest.findClassElementByQName(cls, doc, "class", "qualified-name");
     }
 }
