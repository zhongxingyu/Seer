 package info.mikaelsvensson.devtools.shared;
 
 import info.mikaelsvensson.devtools.doclet.shared.DocumentCreatorFactory;
 import info.mikaelsvensson.devtools.doclet.xml.documentcreator.ElementsOnlyDocumentCreator;
 import info.mikaelsvensson.devtools.doclet.xml.documentcreator.EnumDocumentCreator;
 import info.mikaelsvensson.devtools.doclet.xml.documentcreator.StandardDocumentCreator;
 import info.mikaelsvensson.devtools.doclet.xml.documentcreator.extensive.ExtensiveDocumentCreator;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 
 public class DocumentCreatorFactoryTest
 {
     @Test
     public void createStandard() throws Exception
     {
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(StandardDocumentCreator.NAME),
                 is(StandardDocumentCreator.class));
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(StandardDocumentCreator.class.getName()),
                 is(StandardDocumentCreator.class));
     }
 
     @Test
     public void createExtensive() throws Exception
     {
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(ExtensiveDocumentCreator.NAME),
                 is(ExtensiveDocumentCreator.class));
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(ExtensiveDocumentCreator.class.getName()),
                 is(ExtensiveDocumentCreator.class));
     }
 
     @Test
     public void createEnum() throws Exception
     {
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(EnumDocumentCreator.NAME),
                 is(EnumDocumentCreator.class));
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(EnumDocumentCreator.class.getName()),
                 is(EnumDocumentCreator.class));
     }
 
     @Test
     public void createElementsOnly() throws Exception
     {
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(ElementsOnlyDocumentCreator.NAME),
                 is(ElementsOnlyDocumentCreator.class));
         assertThat(
                 DocumentCreatorFactory.getDocumentCreator(ElementsOnlyDocumentCreator.class.getName()),
                 is(ElementsOnlyDocumentCreator.class));
     }
 
    @Test
    public void failToCreateMissing() throws Exception
    {
        assertNull(DocumentCreatorFactory.getDocumentCreator("invalid name"));
     }
 }
