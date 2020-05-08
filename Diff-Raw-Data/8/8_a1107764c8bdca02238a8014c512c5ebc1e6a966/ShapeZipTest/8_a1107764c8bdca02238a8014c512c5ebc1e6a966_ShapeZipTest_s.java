 package org.geoserver.wfs.response;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import net.opengis.wfs.FeatureCollectionType;
 import net.opengis.wfs.GetFeatureType;
 import net.opengis.wfs.WfsFactory;
 
 import org.geoserver.data.test.MockData;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.Service;
 import org.geoserver.wfs.WFSTestSupport;
 import org.geotools.data.FeatureSource;
 import org.geotools.util.Version;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 public class ShapeZipTest extends WFSTestSupport {
     
     private Operation op;
     private GetFeatureType gft;
 
     @Override
     protected void setUpInternal() throws Exception {
         super.setUpInternal();
         gft = WfsFactory.eINSTANCE.createGetFeatureType();
         op = new Operation("GetFeature", new Service("WFS", null, new Version("1.0.0")), null, new Object[] {gft});
     }
     
     public void testNoNativeProjection() throws Exception {
         FeatureSource<SimpleFeatureType, SimpleFeature> fs;
         fs = getCatalog().getFeatureTypeInfo(MockData.BASIC_POLYGONS).getFeatureSource(true);
         ShapeZipOutputFormat zip = new ShapeZipOutputFormat();
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         FeatureCollectionType fct = WfsFactory.eINSTANCE.createFeatureCollectionType();
         fct.getFeature().add(fs.getFeatures());
         zip.write(fct, bos, op);
         
         checkShapefileIntegrity(new ByteArrayInputStream(bos.toByteArray()));
     }
     
     public void testCharset() throws Exception {
         FeatureSource<SimpleFeatureType, SimpleFeature> fs;
         fs = getCatalog().getFeatureTypeInfo(MockData.BASIC_POLYGONS).getFeatureSource(true);
         ShapeZipOutputFormat zip = new ShapeZipOutputFormat();
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         FeatureCollectionType fct = WfsFactory.eINSTANCE.createFeatureCollectionType();
         fct.getFeature().add(fs.getFeatures());
         
         // add the charset
        gft.getFormatOptions().put("CHARSET", Charset.forName("ISO-8859-15"));
         zip.write(fct, bos, op);
         
         checkShapefileIntegrity(new ByteArrayInputStream(bos.toByteArray()));
         assertEquals("ISO-8859-15", getCharset(new ByteArrayInputStream(bos.toByteArray())));
     }
 
     private void checkShapefileIntegrity(final InputStream in) throws IOException {
         ZipInputStream zis = new ZipInputStream(in);
         ZipEntry entry = null;
         Set names = new HashSet(Arrays.asList(new String[] {".shp", ".shx", ".dbf", ".prj", ".cst"}));
         while((entry = zis.getNextEntry()) != null) {
             final String name = entry.getName();
             final String extension = name.substring(name.length() - 4, name.length());
             assertTrue(names.contains(extension));
             names.remove(extension);
             zis.closeEntry();
         }
         zis.close();
     }
     
     private String getCharset(final InputStream in) throws IOException {
         ZipInputStream zis = new ZipInputStream(in);
         ZipEntry entry = null;
         byte[] bytes = new byte[1024];
         while((entry = zis.getNextEntry()) != null) {
             if(entry.getName().endsWith(".cst")) {
                 zis.read(bytes);
             }
         }
         zis.close();
         
         if(bytes == null)
             return null;
         else
             return new String(bytes).trim();
     }
 }
