 package gov.usgs.cida.gdp.wps.algorithm.filemanagement;
 
 import gov.usgs.cida.gdp.constants.AppConstant;
 import gov.usgs.cida.gdp.dataaccess.helper.ShapeFileEPSGHelper;
 import gov.usgs.cida.gdp.dataaccess.ManageGeoserverWorkspace;
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import org.apache.commons.io.filefilter.RegexFileFilter;
 import org.n52.wps.io.data.GenericFileData;
 import org.n52.wps.io.data.IData;
 import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
 import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
 import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
 import org.opengis.referencing.FactoryException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This algorithm allows a client to upload a file to the server via WPS.
  * More info: http://privusgs2.er.usgs.gov/display/GDP/Adding+a+Shapefile+as+a+GeoServer+WFS+EndPoint
  *
  * @author isuftin
  */
 public class ReceiveFiles extends AbstractSelfDescribingAlgorithm {
     Logger log = LoggerFactory.getLogger(ReceiveFiles.class);
     private List<String> errors = new ArrayList<String>();
 
 
     @Override
     public Map<String, IData> run(Map<String, List<IData>> inputData) {
 
         if (inputData == null)  throw new RuntimeException("Error while allocating input parameters.");
         if (!inputData.containsKey("file"))  throw new RuntimeException("Error: Missing input parameter 'file'");
         if (!inputData.containsKey("wfs-url"))  throw new RuntimeException("Error: Missing input parameter 'wfs-url'");
         if (!inputData.containsKey("filename")) throw new RuntimeException("Error: Missing input parameter 'filename'");
 
         // "gdp.shapefile.temp.path" should be set in the tomcat startup script or setenv.sh as JAVA_OPTS="-Dgdp.shapefile.temp.path=/wherever/you/want/this/file/placed"
         String fileDump = AppConstant.SHAPEFILE_LOCATION.getValue() + File.separator + UUID.randomUUID();
 
         // Ensure that the temp directory exists
         File temp = new File(fileDump);
         temp.mkdirs();
 
         List<IData> dataList = inputData.get("filename");
         String desiredFilename = ((LiteralStringBinding) dataList.get(0)).getPayload();
 
         List<IData> wfsEndpointList = inputData.get("wfs-url");
         String wfsEndpoint = ((LiteralStringBinding) wfsEndpointList.get(0)).getPayload();
 
         dataList = inputData.get("file");
         IData data = dataList.get(0);
 
         // Process each input one at a time
         GenericFileData file = ((GenericFileDataBinding) data).getPayload();
         if (file == null) {
             errors.add("Error while processing file: Could not get file from server");
             throw new RuntimeException("Error while processing file: Could not get file from server");
         }
 
         String shapefilePath = file.writeData(temp);
         if (shapefilePath == null) { // Not sure if that is the only reason newFilename would be null
            errors.add("Error while processing file: Malformed zip file or incomplete shapefile");
            throw new RuntimeException("Error while processing file: Malformed zip file or incomplete shapefile");
         }
 
         
 
         File shapefileFile = new File(shapefilePath);
         File shapefileDir = shapefileFile.getParentFile();
         
         String shapefileName = shapefileFile.getName();
         String shapefileNamePrefix = shapefileName.substring(0, shapefileName.lastIndexOf("."));
 
         // Find all files with filename with any extension
         String pattern = shapefileNamePrefix + "\\..*";
         FileFilter filter = new RegexFileFilter(pattern);
 
         String[] filenames = shapefileDir.list((FilenameFilter) filter);
         List<String> filenamesList = Arrays.asList(filenames);
 
         // Make sure required files are present
         String[] requiredFiles = { ".shp", ".shx", ".prj", ".dbf" };
         for (String s : requiredFiles) {
             if (!filenamesList.contains(shapefileNamePrefix + s)) {
                 throw new RuntimeException("Zip file missing " + s + " file.");
             }
         }
 
         // Rename the files to the desired filenames
         File[] files = shapefileDir.listFiles(filter);
         for (File f : files) {
             String name = f.getName();
             String extension = name.substring(name.lastIndexOf("."));
 
             f.renameTo(new File(shapefileDir.getPath() + File.separator + desiredFilename + extension));
         }
 
         String renamedShpPath = shapefileDir.getPath() + File.separator + desiredFilename + ".shp";
         String renamedPrjPath = shapefileDir.getPath() + File.separator + desiredFilename + ".prj";
 
         // Do EPSG processing
         String declaredCRS = null;
         String nativeCRS = null;
         try {
             nativeCRS = new String(FileHelper.getByteArrayFromFile(new File(renamedPrjPath)));
             if (nativeCRS == null || nativeCRS.isEmpty()) {
                 throw new RuntimeException("Error while getting Prj/WKT information from PRJ file. Function halted.");
             }
             // The behavior of this method requires that the layer always force
             // projection from native to declared...
             declaredCRS = ShapeFileEPSGHelper.getDeclaredEPSGFromWKT(nativeCRS);
             if (declaredCRS == null || declaredCRS.isEmpty()) {
                 throw new RuntimeException("Could not attain EPSG code from shapefile. Please ensure proper projection and a valid PRJ file.");
             }
         } catch (IOException ex) {
             errors.add(ex.getMessage());
             throw new RuntimeException("Error while getting EPSG information from PRJ file. Function halted.");
         } catch (FactoryException ex) {
             errors.add(ex.getMessage());
             throw new RuntimeException("Error while getting EPSG information from PRJ file. Function halted.");
         }
 
         String workspace = "upload";
         try {
             ManageGeoserverWorkspace mws = new ManageGeoserverWorkspace(wfsEndpoint);
             mws.createDataStore(renamedShpPath, desiredFilename, workspace, nativeCRS, declaredCRS);
         } catch (IOException ex) {
             errors.add(ex.getMessage());
             throw new RuntimeException("Error while communicating with WFS server. Please try again or contact system administrator.");
         }
         
         Map<String, IData> result = new HashMap<String, IData>();
         
         // GeoServer has accepted the shapefile. Send the success response to the client.
         result.put("result", new LiteralStringBinding("OK: " + desiredFilename + " successfully uploaded to workspace '" + workspace + "'"));
         result.put("wfs-url", new LiteralStringBinding(wfsEndpoint + "?Service=WFS&Version=1.0.0&"));
         result.put("featuretype", new LiteralStringBinding(workspace + ":" + desiredFilename));
         return result;
     }
 
     @Override
     public BigInteger getMaxOccurs(String identifier) {
         if ("wfs-url".equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if ("filename".equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if ("file".equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         return super.getMaxOccurs(identifier);
     }
 
     @Override
     public BigInteger getMinOccurs(String identifier) {
         if ("wfs-url".equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if ("filename".equals(identifier)) {
             return BigInteger.valueOf(1);
         }
         if ("file".equals(identifier)) {
             return BigInteger.valueOf(1);
         }
 
         return super.getMinOccurs(identifier);
     }
 
     @Override
     public List<String> getInputIdentifiers() {
         List<String> result = new ArrayList<String>();
         result.add("wfs-url");
         result.add("filename");
         result.add("file");
         return result;
     }
 
     @Override
     public List<String> getOutputIdentifiers() {
         List<String> result = new ArrayList<String>();
         result.add("result");
         result.add("wfs-url");
         result.add("featuretype");
         return result;
     }
 
     @Override
     public Class getInputDataType(String id) {
         if ("wfs-url".equals(id)) {
             return LiteralStringBinding.class;
         }
         if ("filename".equals(id)) {
             return LiteralStringBinding.class;
         }
         if ("file".equals(id)) {
             return GenericFileDataBinding.class;
         }
         return null;
     }
 
     @Override
     public Class getOutputDataType(String id) {
         if (id.equals("result")) {
             return LiteralStringBinding.class;
         }
         if (id.equals("wfs-url")) {
             return LiteralStringBinding.class;
         }
         if (id.equals("featuretype")) {
             return LiteralStringBinding.class;
         }
         return null;
     }
 
     @Override
     public List<String> getErrors() {
         return errors;
     }
 }
