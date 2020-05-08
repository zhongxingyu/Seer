 
 package gov.usgs.cida.watersmart.parse.column;
 
 import gov.usgs.cida.watersmart.common.ModelType;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip.ReturnInfo;
 import java.io.File;
 import java.io.IOException;
 import javax.xml.stream.XMLStreamException;
 import org.apache.commons.io.FileUtils;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class PRMSParserTest {
 
     public PRMSParserTest() {
     }
     
     private static final String outputDir = System.getProperty("java.io.tmpdir") + "/prms";
     private static File sampleFile = null;
     
     @BeforeClass
     public static void setupClass() throws IOException {
         FileUtils.forceMkdir(new File(outputDir));
         sampleFile = new File(PRMSParserTest.class.getClassLoader()
                 .getResource("gov/usgs/cida/watersmart/parse/test/PRMS.zip")
                 .getFile());
     }
     
     @AfterClass
     public static void tearDownClass() throws IOException {
         FileUtils.deleteDirectory(new File(outputDir));
     }
 
     @Test
     public void testNetCDFFromPRMS() throws IOException, XMLStreamException {
         // TODO fix to not use gdp2qa
         RunMetadata runMeta = new RunMetadata(ModelType.PRMS, "1", "test", "1", "1", "2012-07-10T00:00:00Z", 
            "Special", "comments", "jiwalker@usgs.gov", "http://cida-wiwsc-gdp2qa.er.usgs.gov:8082/geoserver/NWC/ows", 
             "NWC:Dense1", "site_no");
         ReturnInfo info = CreateDSGFromZip.create(sampleFile, runMeta);
         File ncFile = new File(new File(outputDir), info.filename);
         
         assertEquals(info.filename, "PRMS.nc");
         assertEquals(FileUtils.sizeOf(ncFile), 25035L);
         FileUtils.deleteQuietly(ncFile);
     }
 
 }
