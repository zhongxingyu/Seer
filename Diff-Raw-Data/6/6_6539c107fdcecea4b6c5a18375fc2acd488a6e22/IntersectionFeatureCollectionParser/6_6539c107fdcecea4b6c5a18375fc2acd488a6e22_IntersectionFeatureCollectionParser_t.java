 package gov.usgs.cida.coastalhazards.parser;
 
 import gov.usgs.cida.coastalhazards.wps.exceptions.UnsupportedFeatureTypeException;
 import gov.usgs.cida.coastalhazards.wps.geom.IntersectionPoint;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.n52.wps.io.data.GenericFileData;
 import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
 import org.n52.wps.io.datahandler.parser.AbstractParser;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.type.FeatureType;
 
 /**
  *
  * @author jiwalker
  */
 public class IntersectionFeatureCollectionParser extends AbstractParser {
     
     public IntersectionFeatureCollectionParser() {
         supportedIDataTypes.add(GenericFileDataBinding.class);
     }
 
     @Override
     public GenericFileDataBinding parse(InputStream input, String mimetype, String schema) {
         BufferedWriter buf = null;
         try {
             File outfile = File.createTempFile(getClass().getSimpleName(), ".tsv");
             buf = new BufferedWriter(new FileWriter(outfile));
             
             File tempFile = File.createTempFile(getClass().getSimpleName(), ".xml");
             //FileUtils.copyInputStreamToFile(input, tempFile);
             consumeInputStreamToFile(input, tempFile);
             FeatureCollection collection = new GMLStreamingFeatureCollection(tempFile);
             FeatureType type = collection.getSchema();
             if (type.getDescriptor("TransectID") == null ||
                     type.getDescriptor("Distance") == null ||
                     type.getDescriptor("Date_") == null || // get date attr by type?
                     type.getDescriptor("Uncy") == null) { // Allow user to specify?
                 throw new UnsupportedFeatureTypeException("Feature must have 'TransectID', 'Distance', 'Date_', and 'Uncy'");
             }
             Map<Integer, List<IntersectionPoint>> map = new TreeMap<Integer, List<IntersectionPoint>>();
             FeatureIterator<SimpleFeature> features = collection.features();
             while (features.hasNext()) {
                 SimpleFeature feature = features.next();
                 
                 int transectId = (Integer) feature.getAttribute("TransectID");
 
                 IntersectionPoint intersection = new IntersectionPoint(
                         (Double) feature.getAttribute("Distance"),
                         (String) feature.getAttribute("Date_"),
                         (Double) feature.getAttribute("Uncy"));
 
                 if (map.containsKey(transectId)) {
                     map.get(transectId).add(intersection);
                 } else {
                     List<IntersectionPoint> pointList = new LinkedList<IntersectionPoint>();
                     pointList.add(intersection);
                     map.put(transectId, pointList);
                 }
             }
 
             for (int key : map.keySet()) {
                 List<IntersectionPoint> points = map.get(key);
                 buf.write("# " + key);
                 buf.newLine();
                 for (IntersectionPoint p : points) {
                     buf.write(p.toString());
                     buf.newLine();
                 }
             }
            buf.flush();
            IOUtils.closeQuietly(buf);
             return new GenericFileDataBinding(new GenericFileData(outfile, "text/tsv"));
         }
         catch (IOException e) {
             throw new RuntimeException("Error creating temporary file", e);
         }
         catch (ParseException e) {
             throw new RuntimeException("Unable to parse feature collection", e);
         }
         catch (Exception e) {
             throw new RuntimeException("Unable to parse feature collection", e);
         }
         finally {
            IOUtils.closeQuietly(buf); // just in case
         }
     }
 
     @Override
     public boolean isSupportedSchema(String schema) {
         return schema == null || super.isSupportedSchema(schema);
     }
     
     private void consumeInputStreamToFile(InputStream input, File file) throws IOException {
         BufferedWriter writer = new BufferedWriter(new FileWriter(file));
         BufferedReader reader = new BufferedReader(new InputStreamReader(input));
         String line;
         try {
             while (null != (line = reader.readLine())) {
                 writer.write(line);
             }
         }
         finally {
             IOUtils.closeQuietly(reader);
             IOUtils.closeQuietly(writer);
         }
     }
 }
