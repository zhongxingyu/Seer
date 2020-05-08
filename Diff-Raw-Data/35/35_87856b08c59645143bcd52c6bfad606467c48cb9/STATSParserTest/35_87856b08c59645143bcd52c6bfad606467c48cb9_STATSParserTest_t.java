 
 package gov.usgs.cida.watersmart.parse.file;
 
 import gov.usgs.cida.watersmart.common.ModelType;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip;
 import gov.usgs.cida.watersmart.parse.column.PRMSParserTest;
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
 public class STATSParserTest {
 
     public STATSParserTest() {
     }
     private static final String outputDir = System.getProperty("java.io.tmpdir") + "/stats";
     private static File sampleFile = null;
     
     @BeforeClass
     public static void setupClass() throws IOException {
         FileUtils.forceMkdir(new File(outputDir));
         sampleFile = new File(PRMSParserTest.class.getClassLoader()
                 .getResource("gov/usgs/cida/watersmart/parse/test/STATS.zip")
                 .getFile());
     }
     
     @AfterClass
     public static void tearDownClass() throws IOException {
         FileUtils.deleteDirectory(new File(outputDir));
     }
 
     @Test
     public void testNetCDF() throws IOException, XMLStreamException {
         // TODO remove the gdp2qa dependency
         RunMetadata runMeta = new RunMetadata(ModelType.STATS, "1", "test", "1", "1", "2012-07-10T00:00:00Z", 
            "Special", "comments", "jiwalker@usgs.gov", "http://cida-wiwsc-wsdev.er.usgs.gov:8080/geoserver/NWC/ows", 
             "NWC:Dense1", "site_no");
         CreateDSGFromZip.ReturnInfo info = CreateDSGFromZip.create(sampleFile, runMeta);
         File ncFile = new File(new File(outputDir), info.filename);
         
         assertEquals(info.filename, "STATS.nc");
         assertEquals(FileUtils.sizeOf(ncFile), 18929L);
         FileUtils.deleteQuietly(ncFile);
     }
 
 }
