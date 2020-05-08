 package net.ishchenko.omfp.pdf;
 
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Max
  * Date: 05.05.2010
  * Time: 22:24:30
  */
 public class JsonImportTest {
 
 
     @Test
     public void testJsonImport() throws IOException, InvalidConfigurationException {
 
 
         InputStream input = SettingsBuilderTest.class.getResourceAsStream("/new.stylesheet.json");
         PdfSettings settings = new PdfSettings.Builder(input, PdfSettings.Builder.StyleType.FB2PDF).build();
 
         assertEquals(111.11f, settings.getPageWidth(), .001);
         assertEquals(222.22f, settings.getPageHeight(), .001);
         assertEquals(13f, settings.getSize(), .001);
        assertEquals(11f, settings.getSizeSmall(), .001);
        assertEquals(9f, settings.getSizeVerysmall(), .001);
         assertEquals(3.14f, settings.getMargin(), .001);
 
     }
 }
