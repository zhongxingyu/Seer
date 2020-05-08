 package gov.usgs.cida.coastalhazards.parser;
 
 import com.vividsolutions.jts.geom.Geometry;
 import java.io.File;
 import java.net.URL;
 import org.geotools.feature.FeatureIterator;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 
 /**
  *
  * @author tkunicki
  */
 public class GMLStreamingFeatureCollectionTest {
     
     public GMLStreamingFeatureCollectionTest() {
     }
 
     @Test
    public void testSimpleWFSParse() throws URISyntaxException {
         File file = getFile();
         assertThat(file, notNullValue());
         
         GMLStreamingFeatureCollection fc = new GMLStreamingFeatureCollection(file);
         assertThat(fc, notNullValue());
         assertThat(fc.getBounds(), notNullValue());
         assertThat(fc.getSchema(), notNullValue());
         assertThat(fc.size(), equalTo(324));
         
         FeatureIterator fi = null;
         try {
             fi = fc.features();
             assertThat(fi, notNullValue());
             assertThat(fi.hasNext(), is(true));
             while(fi.hasNext()) {
                 Feature f = fi.next();
                 assertThat(f, instanceOf(SimpleFeature.class));
                 SimpleFeature sf = (SimpleFeature)f;
                 Object go = sf.getDefaultGeometry();
                 assertThat(go, instanceOf(Geometry.class));
             }
         } finally {
             if (fi != null) {
                 fi.close();
             }
         }
         
     }
     
    private File getFile() throws URISyntaxException {
         URL url = getClass().getClassLoader().getResource("collection.xml");
         assertThat(url, notNullValue());
         assertThat(url.getProtocol(), equalTo("file"));
         assertThat(url.getPath(), notNullValue());
         File file = new File(url.toURI());
         assertThat(file.canRead(), equalTo(true));
         return file;
     } 
 }
