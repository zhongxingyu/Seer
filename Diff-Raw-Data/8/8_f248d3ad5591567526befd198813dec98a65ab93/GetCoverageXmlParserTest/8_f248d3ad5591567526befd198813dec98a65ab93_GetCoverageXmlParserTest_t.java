 package org.geoserver.wcs.xml;
 
 import java.io.StringReader;
 
 import junit.framework.TestCase;
 import net.opengis.gml.GridType;
 import net.opengis.wcs10.AxisSubsetType;
 import net.opengis.wcs10.GetCoverageType;
 import net.opengis.wcs10.IntervalType;
 import net.opengis.wcs10.RangeSubsetType;
 
 import org.geoserver.wcs.xml.v1_0_0.WcsXmlReader;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.referencing.CRS;
 import org.geotools.wcs.WCSConfiguration;
 import org.opengis.coverage.grid.GridEnvelope;
 import org.vfny.geoserver.wcs.WcsException;
 
 public class GetCoverageXmlParserTest extends TestCase {
 
     private WCSConfiguration configuration;
 
     private WcsXmlReader reader;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         configuration = new WCSConfiguration();
         reader = new WcsXmlReader("GetCoverage", "1.0.0", configuration);
     }
 
     public void testInvalid() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                 + //
                 "<GetCoverage service=\"WCS\" version=\"1.0.0\""
                 + "  xmlns=\"http://www.opengis.net/wcs\" "
                 + "  xmlns:nurc=\"http://www.nurc.nato.int\""
                 + "  xmlns:ogc=\"http://www.opengis.net/ogc\""
                 + "  xmlns:gml=\"http://www.opengis.net/gml\" "
                 + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                 + "  xsi:schemaLocation=\"http://www.opengis.net/wcs schemas/wcs/1.0.0/getCoverage.xsd\">"
                 + " <sourceCoverage>nurc:Pk50095</sourceCoverage>" + "</GetCoverage>";
 
         try {
             GetCoverageType cov = (GetCoverageType) reader.read(null, new StringReader(request),
                     null);
             fail("This request is not valid!!!");
         } catch (WcsException e) {
             // ok, we do expect a validation exception in fact
             /*
              * The content of element 'GetCoverage' is not complete. One of
              * '{"http://www.opengis.net/wcs":domainSubset}' is expected.
              */
         }
     }
 
     public void testBasic() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                 + //
                 "<GetCoverage service=\"WCS\" version=\"1.0.0\""
                 + //
                 "  xmlns=\"http://www.opengis.net/wcs\" "
                 + //
                 "  xmlns:nurc=\"http://www.nurc.nato.int\""
                 + //
                 "  xmlns:ogc=\"http://www.opengis.net/ogc\""
                 + //
                 "  xmlns:gml=\"http://www.opengis.net/gml\" "
                 + //
                 "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                 + //
                 "  xsi:schemaLocation=\"http://www.opengis.net/wcs schemas/wcs/1.0.0/getCoverage.xsd\">"
                 + //
                 "  <sourceCoverage>nurc:Pk50095</sourceCoverage>" + //
                 "    <domainSubset>" + //
                 "      <spatialSubset>" + //
                 "        <gml:Envelope srsName=\"EPSG:32633\">" + //
                 "          <gml:pos>347649.93086859107 5176214.082539256</gml:pos>" + //
                 "          <gml:pos>370725.976428591 5196961.352859256</gml:pos>" + // 
                 "        </gml:Envelope>" + //
                 "        <gml:Grid dimension=\"2\" srsName=\"EPSG:4326\">" + //
                 "          <gml:limits>" + //
                 "            <gml:GridEnvelope>" + //
                 "              <gml:low>0 0</gml:low>" + //
                 "              <gml:high>545 490</gml:high>" + //
                 "            </gml:GridEnvelope>" + //
                 "          </gml:limits>" + //
                 "          <gml:axisName>Lon</gml:axisName>" + //
                 "          <gml:axisName>Lat</gml:axisName>" + //
                 "        </gml:Grid>" + //
                 "      </spatialSubset>" + //
                 "    </domainSubset>" + //
                 "    <output>" + //
                 "      <crs>EPSG:4326</crs>" + //
                 "      <format>TIFF</format>" + //
                 "    </output>" + //
                 "</GetCoverage>";
 
         // smoke test, we only try out a very basic request
         GetCoverageType gc = (GetCoverageType) reader.read(null, new StringReader(request), null);
         assertEquals("WCS", gc.getService());
         assertEquals("1.0.0", gc.getVersion());
         assertEquals("nurc:Pk50095", gc.getSourceCoverage());
 
         GeneralEnvelope envelope = ((GeneralEnvelope) gc.getDomainSubset().getSpatialSubset()
                 .getEnvelope().get(0));
         assertEquals("EPSG:32633", CRS.lookupIdentifier(envelope.getCoordinateReferenceSystem(),
                 true));
         assertEquals(347649.93086859107, envelope.getLowerCorner().getOrdinate(0));
         assertEquals(5176214.082539256, envelope.getLowerCorner().getOrdinate(1));
         assertEquals(370725.976428591, envelope.getUpperCorner().getOrdinate(0));
         assertEquals(5196961.352859256, envelope.getUpperCorner().getOrdinate(1));
         assertNotNull(gc.getOutput().getCrs());
         assertEquals("EPSG:4326", gc.getOutput().getCrs().getValue());
         assertNotNull(gc.getOutput().getFormat());
         assertEquals("TIFF", gc.getOutput().getFormat().getValue());
     }
 
     public void testRangeSubsetKeys() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                 + //
                 "<GetCoverage service=\"WCS\" version=\"1.0.0\""
                 + //
                 "  xmlns=\"http://www.opengis.net/wcs\" "
                 + //
                 "  xmlns:nurc=\"http://www.nurc.nato.int\""
                 + //
                 "  xmlns:ogc=\"http://www.opengis.net/ogc\""
                 + //
                 "  xmlns:gml=\"http://www.opengis.net/gml\" "
                 + //
                 "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                 + //
                 "  xsi:schemaLocation=\"http://www.opengis.net/wcs schemas/wcs/1.0.0/getCoverage.xsd\">"
                 + //
                 "  <sourceCoverage>nurc:Pk50095</sourceCoverage>" + //
                 "    <domainSubset>" + //
                 "      <spatialSubset>" + //
                 "        <gml:Envelope srsName=\"EPSG:32633\">" + //
                 "          <gml:pos>347649.93086859107 5176214.082539256</gml:pos>" + //
                 "          <gml:pos>370725.976428591 5196961.352859256</gml:pos>" + // 
                 "        </gml:Envelope>" + //
                 "        <gml:Grid dimension=\"2\" srsName=\"EPSG:4326\">" + //
                 "          <gml:limits>" + //
                 "            <gml:GridEnvelope>" + //
                 "              <gml:low>0 0</gml:low>" + //
                 "              <gml:high>545 490</gml:high>" + //
                 "            </gml:GridEnvelope>" + //
                 "          </gml:limits>" + //
                 "          <gml:axisName>Column</gml:axisName>" + //
                 "          <gml:axisName>Row</gml:axisName>" + //
                 "        </gml:Grid>" + //
                 "      </spatialSubset>" + //
                 "    </domainSubset>" + //
                 "    <rangeSubset>" + //
                 "       <axisSubset name=\"Band\">" + //
                 "          <interval atomic=\"false\">" + // 
                 "               <min>1</min>" + //
                 "               <max>3</max>" + //
                 "               <res>1</res>" + //
                 "          </interval>" + //
                 "       </axisSubset>" + //
                 "    </rangeSubset>" + //
                 "    <output>" + //
                 "      <crs>EPSG:4326</crs>" + //
                 "      <format>TIFF</format>" + //
                 "    </output>" + //
                 "</GetCoverage>";
 
         GetCoverageType gc = (GetCoverageType) reader.read(null, new StringReader(request), null);
         assertEquals(1, gc.getRangeSubset().getAxisSubset().size());
 
         GridType grid = (GridType) gc.getDomainSubset().getSpatialSubset().getGrid().get(0);
         assertEquals(grid.getSrsName(), "EPSG:4326");
         assertEquals(grid.getAxisName().get(0), "Column");
         assertEquals(grid.getAxisName().get(1), "Row");
 
         GridEnvelope gridLimits = grid.getLimits();
        assertEquals(0, gridLimits.getLow(0));
        assertEquals(0, gridLimits.getLow(1));
        assertEquals(545, gridLimits.getHigh(0));
        assertEquals(490, gridLimits.getHigh(1));
 
         RangeSubsetType rangeSet = gc.getRangeSubset();
         AxisSubsetType axisSubset = (AxisSubsetType) rangeSet.getAxisSubset().get(0);
         assertEquals("Band", axisSubset.getName());
         assertEquals(axisSubset.getSingleValue().size(), 0);
         assertEquals(axisSubset.getInterval().size(), 1);
 
         IntervalType interval = (IntervalType) axisSubset.getInterval().get(0);
         assertEquals("1", interval.getMin().getValue());
         assertEquals("3", interval.getMax().getValue());
         assertEquals("1", interval.getRes().getValue());
     }
 
 }
