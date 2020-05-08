 package org.geogit.storage.hessian;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import junit.framework.TestCase;
 
 import org.geogit.api.ObjectId;
 import org.geotools.data.DataUtilities;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.geometry.jts.WKTReader2;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.GeometryDescriptor;
 
 import com.vividsolutions.jts.io.ParseException;
 
 public class HessianFeatureSerialisationTest extends TestCase {
     /* This defines the first of our test feature types. */
     private String namespace1 = "http://geoserver.org/test";
 
     private String typeName1 = "TestType";
 
     private String typeSpec1 = "str:String," + "bool:Boolean," + "byte:java.lang.Byte,"
             + "doub:Double," + "bdec:java.math.BigDecimal," + "flt:Float," + "int:Integer,"
            + "bint:java.math.BigInteger," + "pp:Point:srid=4326," + "lng:java.lang.Long";
 
     private SimpleFeatureType featureType1;
 
     /* The features created are stored here for comparison. */
     private Feature feature1_1;
 
     protected void setUp() throws Exception {
         super.setUp();
         /* now we will setup our feature types and test features. */
         featureType1 = DataUtilities.createType(namespace1, typeName1, typeSpec1);
         feature1_1 = feature(featureType1, "TestType.feature.1", "StringProp1_1", Boolean.TRUE,
                 Byte.valueOf("18"), new Double(100.01), new BigDecimal("1.89e1021"),
                new Float(12.5), new Integer(1000), new BigInteger("90000000"), "POINT(1 1)",
                new Long(800000));
     };
 
     public void testSerialise() throws Exception {
 
         HessianFeatureWriter writer = new HessianFeatureWriter(feature1_1);
 
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         writer.write(output);
 
         byte[] data = output.toByteArray();
         assertTrue(data.length > 0);
 
         HessianFeatureReader reader = new HessianFeatureReader(featureType1, feature1_1
                 .getIdentifier().getID(), null);
         ByteArrayInputStream input = new ByteArrayInputStream(data);
         Feature feat = reader.read(ObjectId.forString(feature1_1.getIdentifier().getID()), input);
 
         assertNotNull(feat);
         assertTrue(feat instanceof SimpleFeature);
 
         assertEquals(feature1_1, feat);
 
     }
 
     protected Feature feature(SimpleFeatureType type, String id, Object... values)
             throws ParseException {
         SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
         for (int i = 0; i < values.length; i++) {
             Object value = values[i];
             if (type.getDescriptor(i) instanceof GeometryDescriptor) {
                 if (value instanceof String) {
                     value = new WKTReader2().read((String) value);
                 }
             }
             builder.set(i, value);
         }
         return builder.buildFeature(id);
     }
 }
