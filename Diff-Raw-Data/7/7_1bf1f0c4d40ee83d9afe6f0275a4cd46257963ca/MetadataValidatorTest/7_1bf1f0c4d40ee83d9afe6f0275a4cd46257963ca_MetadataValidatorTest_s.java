 package gov.usgs.cida.utilities.xml;
 
 import gov.usgs.cida.coastalhazards.metadata.MetadataValidator;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import javax.xml.xpath.XPathExpressionException;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class MetadataValidatorTest {
 
     @Test
    public void testValidateFGDC() throws IOException, XPathExpressionException {
         URL meta = getClass().getClassLoader().getResource(
                 "gov/usgs/cida/coastalhazards/metadata/OR_transects_LT.shp.FGDCclean.xml");
        MetadataValidator validator = new MetadataValidator(new File(meta.getFile()));
         assertTrue(validator.validateFGDC());
     }
     
 }
