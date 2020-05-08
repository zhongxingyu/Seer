 package gov.usgs.cida.coastalhazards.wps;
 
 import gov.usgs.cida.coastalhazards.util.Constants;
 import gov.usgs.cida.coastalhazards.util.FeatureCollectionFromShp;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.apache.commons.io.IOUtils;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 
 /**
  *
  * @author jiwalker
  */
 public class CreateResultsLayerProcessTest {
     
     private static File outTest = null;
    
     @BeforeClass
     public static void setupAll() throws IOException {
        // leaks files, need to delete all the files associated with .shp
         outTest = File.createTempFile("test", ".shp");
        outTest.deleteOnExit();
     }
     
     /**
      * Test of execute method, of class CreateResultsLayerProcess.
      */
     @Test
     //@Ignore
     public void testExecute() throws Exception {
         File outTest = File.createTempFile("test", ".shp");
         outTest.deleteOnExit();
         
         InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(
                 "gov/usgs/cida/coastalhazards/jersey/NewJerseyN_results.txt");
         URL transects = getClass().getClassLoader().getResource(
                 "gov/usgs/cida/coastalhazards/jersey/NewJerseyNa_transects.shp");
         URL intersects = getClass().getClassLoader().getResource(
                 "gov/usgs/cida/coastalhazards/jersey/NewJerseyN_intersections.shp");
         BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
         StringBuffer buffer = new StringBuffer();
         String line = null;
         while (null != (line = reader.readLine())) {
             buffer.append(line);
             buffer.append("\n");
         }
         IOUtils.closeQuietly(reader);
         SimpleFeatureCollection transectfc = (SimpleFeatureCollection)
                 FeatureCollectionFromShp.featureCollectionFromShp(transects);
         SimpleFeatureCollection intersectfc = (SimpleFeatureCollection)
                 FeatureCollectionFromShp.featureCollectionFromShp(intersects);
         // need to get the matching transect layer to run against
         CreateResultsLayerProcess createResultsLayerProcess = new CreateResultsLayerProcess(new DummyImportProcess(outTest), new DummyCatalog());
         createResultsLayerProcess.execute(buffer, transectfc, intersectfc, null, null, null);
         
         validateNSD(outTest);
     }
     
     private void validateNSD(File shapefile) throws MalformedURLException, IOException {
         SimpleFeatureCollection resultsFC = (SimpleFeatureCollection)
                 FeatureCollectionFromShp.featureCollectionFromShp(shapefile.toURI().toURL());
         
         SimpleFeatureType resultsFT = resultsFC.getSchema();
         AttributeDescriptor nsdAD = resultsFT.getDescriptor(Constants.NSD_ATTR);
         assertNotNull(nsdAD);
         assertEquals(Double.class, nsdAD.getType().getBinding());
         
         SimpleFeatureIterator resultsFI = null;
         try {
             resultsFI = resultsFC.features();
             while (resultsFI.hasNext()) {
                 SimpleFeature resultsF = resultsFI.next();
                 Object nsdAsObject = resultsF.getAttribute("NSD");
                 assertThat(nsdAsObject, is(notNullValue()));
                 assertThat(nsdAsObject, is(instanceOf(Double.class)));
                 
                 Double nsd = (Double)nsdAsObject;
                 assertThat(nsd, lessThan(Double.MAX_VALUE));
                 assertThat(nsd, greaterThanOrEqualTo(0d));
             }
         } finally {
             if (resultsFI != null) {
                 resultsFI.close();
             }
         }
     }
 }
