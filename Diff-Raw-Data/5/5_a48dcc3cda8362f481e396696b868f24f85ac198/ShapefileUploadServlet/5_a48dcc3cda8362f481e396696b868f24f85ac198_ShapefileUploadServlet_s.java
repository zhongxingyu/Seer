 package gov.usgs.cida.geoutils.geoutils.geoserver.servlet;
 
 import gov.usgs.cida.config.DynamicReadOnlyProperties;
 import gov.usgs.cida.owsutils.commons.communication.RequestResponse;
 import gov.usgs.cida.owsutils.commons.io.FileHelper;
 import gov.usgs.cida.owsutils.commons.properties.JNDISingleton;
 import gov.usgs.cida.owsutils.commons.shapefile.ProjectionUtils;
 import it.geosolutions.geoserver.rest.GeoServerRESTManager;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.UploadMethod;
 import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.LoggerFactory;
 
 public class ShapefileUploadServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ShapefileUploadServlet.class);
     private static DynamicReadOnlyProperties props = null;
     private static String applicationName;
     private static Integer maxFileSize;
     private static String geoserverEndpoint;
     private static URL geoserverEndpointURL;
     private static String geoserverUsername;
     private static String geoserverPassword;
     private static GeoServerRESTManager gsRestManager;
     // Defaults
     private static String defaultWorkspaceName;
     private static String defaultStoreName;
     private static String defaultSRS;
     private static String defaultFilenameParam = "qqfile"; // Legacy to handle jquery fineuploader
     private static Integer defaultMaxFileSize = Integer.MAX_VALUE;
     private static boolean defaultUseBaseCRSFallback = true;
     private static boolean defaultOverwriteExistingLayer = false;
     private static ProjectionPolicy defaultProjectionPolicy = ProjectionPolicy.REPROJECT_TO_DECLARED;
     private static ServletConfig servletConfig;
 
     @Override
     public void init(ServletConfig servletConfig) throws ServletException {
         super.init();
         ShapefileUploadServlet.servletConfig = servletConfig;
         props = JNDISingleton.getInstance();
 
         applicationName = servletConfig.getInitParameter("application.name");
 
         // The maximum upload file size allowd by this server, 0 = Integer.MAX_VALUE
         String mfsInitParam = servletConfig.getInitParameter("max.upload.file.size");
         String mfsJndiProp = props.getProperty(applicationName + ".max.upload.file.size");
         if (StringUtils.isNotBlank(mfsInitParam)) {
             maxFileSize = Integer.parseInt(mfsInitParam);
         } else if (StringUtils.isNotBlank(mfsJndiProp)) {
             maxFileSize = Integer.parseInt(mfsJndiProp);
         } else {
             maxFileSize = defaultMaxFileSize;
         }
         if (maxFileSize == 0) {
             maxFileSize = defaultMaxFileSize;
         }
         LOG.trace("Maximum allowable file size set to: " + maxFileSize + " bytes");
 
         String gsepInitParam = servletConfig.getInitParameter("geoserver.endpoint");
         String gsepJndiProp = props.getProperty(applicationName + ".geoserver.endpoint");
         if (StringUtils.isNotBlank(gsepInitParam)) {
             geoserverEndpoint = gsepInitParam;
         } else if (StringUtils.isNotBlank(gsepJndiProp)) {
             geoserverEndpoint = gsepJndiProp;
         } else {
             throw new ServletException("Geoserver endpoint is not defined.");
         }
         LOG.trace("Geoserver endpoint set to: " + geoserverEndpoint);
 
         try {
             geoserverEndpointURL = new URL(geoserverEndpoint);
         } catch (MalformedURLException ex) {
             throw new ServletException("Geoserver endpoint (" + geoserverEndpoint + ") could not be parsed into a valid URL.");
         }
 
         String gsuserInitParam = servletConfig.getInitParameter("geoserver.username");
         String gsuserJndiProp = props.getProperty(applicationName + ".geoserver.username");
         if (StringUtils.isNotBlank(gsuserInitParam)) {
             geoserverUsername = gsuserInitParam;
         } else if (StringUtils.isNotBlank(gsuserJndiProp)) {
             geoserverUsername = gsuserJndiProp;
         } else {
             throw new ServletException("Geoserver username is not defined.");
         }
         LOG.trace("Geoserver username set to: " + geoserverUsername);
 
         // This should only be coming from JNDI or JVM properties
         String gspassJndiProp = props.getProperty(applicationName + ".geoserver.password");
         if (StringUtils.isNotBlank(gspassJndiProp)) {
             geoserverPassword = gspassJndiProp;
         } else {
             throw new ServletException("Geoserver password is not defined.");
         }
         LOG.trace("Geoserver password is set");
 
         try {
             gsRestManager = new GeoServerRESTManager(geoserverEndpointURL, geoserverUsername, geoserverPassword);
         } catch (IllegalArgumentException ex) {
             throw new ServletException("Geoserver manager count not be built", ex);
         } catch (MalformedURLException ex) {
             // This should not happen since we take care of it above - we can probably move this into the try block above
             throw new ServletException("Geoserver endpoint (" + geoserverEndpoint + ") could not be parsed into a valid URL.");
         }
 
         String dwInitParam = servletConfig.getInitParameter("default.upload.workspace");
         String dwJndiProp = props.getProperty(applicationName + ".default.upload.workspace");
         if (StringUtils.isNotBlank(dwInitParam)) {
             defaultWorkspaceName = dwInitParam;
         } else if (StringUtils.isNotBlank(dwJndiProp)) {
             defaultWorkspaceName = dwInitParam;
         } else {
             defaultWorkspaceName = "";
             LOG.warn("Default workspace is not defined. If a workspace is not passed to during the request, the request will fail;");
         }
         LOG.trace("Default workspace set to: " + defaultWorkspaceName);
 
         String dsnInitParam = servletConfig.getInitParameter("default.upload.storename");
         String dsnJndiProp = props.getProperty(applicationName + ".default.upload.storename");
         if (StringUtils.isNotBlank(dsnInitParam)) {
             defaultStoreName = dwInitParam;
         } else if (StringUtils.isNotBlank(dsnJndiProp)) {
             defaultStoreName = dwInitParam;
         } else {
             defaultStoreName = "";
             LOG.warn("Default store name is not defined. If a store name is not passed to during the request, the request will fail;");
         }
         LOG.trace("Default store name set to: " + defaultStoreName);
 
         String dsrsInitParam = servletConfig.getInitParameter("default.srs");
         String dsrsJndiProp = props.getProperty(applicationName + ".default.srs");
         if (StringUtils.isNotBlank(dsrsInitParam)) {
             defaultSRS = dsrsInitParam;
         } else if (StringUtils.isNotBlank(dsrsJndiProp)) {
             defaultSRS = dsrsJndiProp;
         } else {
             defaultSRS = "";
             LOG.warn("Default SRS is not defined. If a SRS name is not passed to during the request, the request will fail");
         }
         LOG.trace("Default SRS set to: " + defaultSRS);
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, FileNotFoundException {
         doPost(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, FileNotFoundException {
         String filenameParam;
         boolean useBaseCRSFailover;
         boolean overwriteExistingLayer;
         // "reproject" (default), "force", "none"
         ProjectionPolicy projectionPolicy;
 
         Map<String, String> responseMap = new HashMap<String, String>();
 
         RequestResponse.ResponseType responseType = RequestResponse.ResponseType.XML;
         String responseEncoding = request.getParameter("response.encoding");
         if (StringUtils.isBlank(responseEncoding) || responseEncoding.toLowerCase().contains("json")) {
             responseType = RequestResponse.ResponseType.JSON;
         }
         LOG.trace("Response type set to " + responseType.toString());
 
         int fileSize = Integer.parseInt(request.getHeader("Content-Length"));
         if (fileSize > maxFileSize) {
             responseMap.put("error", "Upload exceeds max file size of " + maxFileSize + " bytes");
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
             return;
         }
 
         // The key to search for in the upload form post to find the file
         String fnInitParam = servletConfig.getInitParameter("filename.param");
         String fnJndiProp = props.getProperty(applicationName + ".filename.param");
         String fnReqParam = request.getParameter("filename.param");
         if (StringUtils.isNotBlank(fnReqParam)) {
             filenameParam = fnReqParam;
         } else if (StringUtils.isNotBlank(fnInitParam)) {
             filenameParam = fnInitParam;
         } else if (StringUtils.isNotBlank(fnJndiProp)) {
             filenameParam = fnJndiProp;
         } else {
             filenameParam = defaultFilenameParam;
         }
         LOG.trace("Filename parameter set to: " + filenameParam);
 
         String oelInitParam = servletConfig.getInitParameter("overwrite.existing.layer");
         String oelJndiProp = props.getProperty(applicationName + ".overwrite.existing.layer");
         String oelReqParam = request.getParameter("overwrite.existing.layer");
         if (StringUtils.isNotBlank(oelReqParam)) {
             overwriteExistingLayer = Boolean.parseBoolean(oelReqParam);
         } else if (StringUtils.isNotBlank(oelInitParam)) {
             overwriteExistingLayer = Boolean.parseBoolean(oelInitParam);
         } else if (StringUtils.isNotBlank(oelJndiProp)) {
             overwriteExistingLayer = Boolean.parseBoolean(oelJndiProp);
         } else {
             overwriteExistingLayer = defaultOverwriteExistingLayer;
         }
         LOG.trace("Overwrite existing layer set to: " + overwriteExistingLayer);
 
         String filename = request.getParameter(filenameParam);
         String tempDir = System.getProperty("java.io.tmpdir");
         File shapeZipFile = new File(tempDir + File.separator + filename);
         LOG.trace("Temporary file set to " + shapeZipFile.getPath());
 
         String workspaceName = request.getParameter("workspace");
         if (StringUtils.isBlank(workspaceName)) {
             workspaceName = defaultWorkspaceName;
         }
         if (StringUtils.isBlank(workspaceName)) {
             responseMap.put("error", "Parameter \"workspace\" is mandatory");
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
             return;
         }
         LOG.trace("Workspace name set to " + workspaceName);
 
         String storeName = request.getParameter("store");
         if (StringUtils.isBlank(storeName)) {
             storeName = defaultStoreName;
         }
         if (StringUtils.isBlank(storeName)) {
             responseMap.put("error", "Parameter \"store\" is mandatory");
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
             return;
         }
         LOG.trace("Store name set to " + storeName);
 
         String srsName = request.getParameter("srs");
         if (StringUtils.isBlank(srsName)) {
             srsName = defaultSRS;
         }
         if (StringUtils.isBlank(srsName)) {
             responseMap.put("error", "Parameter \"srs\" is mandatory");
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
             return;
         }
         LOG.trace("SRS name set to " + srsName);
 
         String bCRSfoInitParam = servletConfig.getInitParameter("use.crs.failover");
         String bCRSfoJndiProp = props.getProperty(applicationName + ".use.crs.failover");
         String bCRSfoReqParam = request.getParameter("use.crs.failover");
         if (StringUtils.isNotBlank(bCRSfoReqParam)) {
             useBaseCRSFailover = Boolean.parseBoolean(bCRSfoReqParam);
         } else if (StringUtils.isNotBlank(bCRSfoInitParam)) {
             useBaseCRSFailover = Boolean.parseBoolean(bCRSfoInitParam);
         } else if (StringUtils.isNotBlank(bCRSfoJndiProp)) {
             useBaseCRSFailover = Boolean.parseBoolean(bCRSfoJndiProp);
         } else {
             useBaseCRSFailover = defaultUseBaseCRSFallback;
         }
         LOG.trace("Use base CRS failover set to: " + useBaseCRSFailover);
 
         String projectionPolicyReqParam = request.getParameter("projection.policy");
         String ppInitParam = servletConfig.getInitParameter("projection.policy");
         String ppJndiProp = props.getProperty(applicationName + ".projection.policy");
         if (StringUtils.isNotBlank(projectionPolicyReqParam)) {
             if (projectionPolicyReqParam.equalsIgnoreCase("reproject")) {
                 projectionPolicy = ProjectionPolicy.REPROJECT_TO_DECLARED;
             } else if (projectionPolicyReqParam.equalsIgnoreCase("force")) {
                 projectionPolicy = ProjectionPolicy.FORCE_DECLARED;
             } else {
                 projectionPolicy = defaultProjectionPolicy;
             }
         } else if (StringUtils.isNotBlank(ppInitParam)) {
             if (ppInitParam.equalsIgnoreCase("reproject")) {
                 projectionPolicy = ProjectionPolicy.REPROJECT_TO_DECLARED;
             } else if (ppInitParam.equalsIgnoreCase("force")) {
                 projectionPolicy = ProjectionPolicy.FORCE_DECLARED;
             } else {
                 projectionPolicy = ProjectionPolicy.NONE;
             }
         } else if (StringUtils.isNotBlank(ppJndiProp)) {
             if (ppJndiProp.equalsIgnoreCase("reproject")) {
                 projectionPolicy = ProjectionPolicy.REPROJECT_TO_DECLARED;
             } else if (ppJndiProp.equalsIgnoreCase("force")) {
                 projectionPolicy = ProjectionPolicy.FORCE_DECLARED;
             } else {
                 projectionPolicy = ProjectionPolicy.NONE;
             }
         } else {
             projectionPolicy = defaultProjectionPolicy;
         }
         LOG.trace("Projection policy set to: " + projectionPolicy.name());
         LOG.trace("Projection policy re-set to: " + projectionPolicy);
 
         String layerName = request.getParameter("layer");
         if (StringUtils.isBlank(layerName)) {
             layerName = filename.split("\\.")[0];
         }
         layerName = layerName.trim().replaceAll("\\.", "_").replaceAll(" ", "_");
         LOG.trace("Layer name set to " + layerName);
 
         try {
             RequestResponse.saveFileFromRequest(request, shapeZipFile, filenameParam);
             LOG.trace("File saved to " + shapeZipFile.getPath());
 
             FileHelper.flattenZipFile(shapeZipFile.getPath());
             LOG.trace("Zip file directory structure flattened");
 
             if (!FileHelper.validateShapefileZip(shapeZipFile)) {
                 throw new IOException("Unable to verify shapefile. Upload failed.");
             }
             LOG.trace("Zip file seems to be a valid shapefile");
         } catch (Exception ex) {
             LOG.warn(ex.getMessage());
             responseMap.put("error", "Unable to upload file");
             responseMap.put("exception", ex.getMessage());
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
             return;
         }
 
         String nativeCRS;
         try {
             nativeCRS = ProjectionUtils.getProjectionFromShapefileZip(shapeZipFile, useBaseCRSFailover);
         } catch (Exception ex) {
             LOG.warn(ex.getMessage());
             responseMap.put("error", "Could not evince projection from shapefile");
             responseMap.put("exception", ex.getMessage());
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
             return;
         }
 
         try {
             GeoServerRESTPublisher gsPublisher = gsRestManager.getPublisher();
 
             if (overwriteExistingLayer) {
                 // TODO- Using this library, I am unable to rename a layer. If publishing the layer
                 // fails, we will have lost this layer due to removal here. 
                gsPublisher.removeLayer(workspaceName, layerName);
             }
 
             // Currently not doing any checks to see if workspace, store or layer already exist
             Boolean success = gsPublisher.publishShp(workspaceName, storeName, null, layerName, UploadMethod.FILE, shapeZipFile.toURI(), srsName, nativeCRS, projectionPolicy, null);
             if (success) {
                 LOG.debug("Shapefile has been imported successfully");
                 responseMap.put("name", layerName);
                 responseMap.put("workspace", workspaceName);
                 responseMap.put("store", storeName);
                 RequestResponse.sendSuccessResponse(response, responseMap, responseType);
             } else {
                 LOG.debug("Shapefile could not be imported successfully");
                 responseMap.put("error", "Shapefile could not be imported successfully");
                 RequestResponse.sendErrorResponse(response, responseMap, responseType);
             }
         } catch (Exception ex) {
             LOG.warn(ex.getMessage());
             responseMap.put("error", "Unable to upload file");
             responseMap.put("exception", ex.getMessage());
             RequestResponse.sendErrorResponse(response, responseMap, responseType);
         } finally {
             FileUtils.deleteQuietly(shapeZipFile);
         }
     }
 }
