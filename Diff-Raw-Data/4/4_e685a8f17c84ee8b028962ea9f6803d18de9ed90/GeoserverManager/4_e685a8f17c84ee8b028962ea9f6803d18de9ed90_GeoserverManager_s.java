 package gov.usgs.cida.gdp.dataaccess;
 
 import gov.usgs.cida.gdp.utilities.XMLUtils;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.xpath.XPathExpressionException;
 import org.apache.commons.io.FileUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.NodeList;
 
 /**
  * Manage GeoServer with its REST interface.
  * 
  * @see http://docs.geoserver.org/latest/en/user/restconfig/rest-config-api.html
  */
 public class GeoserverManager {
     private static org.slf4j.Logger log = LoggerFactory.getLogger(GeoserverManager.class);
     
     private String url;
     private String user;
     private String password;
 
     public GeoserverManager(String url, String user, String password) {
         this.url = fixURL(url); // ensure url ends with a '/'
         this.user = user;
         this.password = password;
     }
 
     public void reloadConfiguration() throws IOException {
         sendRequest("rest/reload/", "POST", null, null);
     }
 
    public void createDataStore(String shapefilePath, String workspace,
            String layer, String nativeCRS, String declaredCRS) throws IOException {
         
         log.debug("Creating data store on WFS server located at: " + url);
         
         String workspacesPath = "rest/workspaces/";
         if (!workspaceExists(workspace)) {
             String workspaceXML = createWorkspaceXML(workspace);
             sendRequest(workspacesPath, "POST", "text/xml", workspaceXML);
         }
 
         String dataStoresPath = workspacesPath + workspace + "/datastores/";
         
         String namespace = "";
         Matcher nsMatcher = Pattern.compile(".*<uri>(.*)</uri>.*").matcher(getNameSpaceXML(workspace));
         if (nsMatcher.matches()) namespace = nsMatcher.group(1);
         
         String dataStoreXML = createDataStoreXML(layer, workspace, namespace, shapefilePath);
         if (!dataStoreExists(workspace, layer)) {
             // send POST to create the datastore if it doesn't exist
             sendRequest(dataStoresPath, "POST", "text/xml", dataStoreXML);
         } else {
             // otherwise send PUT to ensure that it's pointing to the correct shapefile
             sendRequest(dataStoresPath + layer + ".xml", "PUT", "text/xml", dataStoreXML);
         }
 
         if (!layerExists(workspace, layer, layer)) {
             // create featuretype based on the datastore
             String featureTypeXML = createFeatureTypeXML(layer, workspace, nativeCRS, declaredCRS);
             String featureTypesPath = dataStoresPath + layer + "/featuretypes.xml";
             sendRequest(featureTypesPath, "POST", "text/xml", featureTypeXML);
 
             // This directive generates an error when included with initial POST, we have to include
             // in separate update via PUT.
             String featureTypeUpdatePath = dataStoresPath + layer + "/featuretypes/" + layer + ".xml";
             sendRequest(featureTypeUpdatePath, "PUT", "text/xml",
                     "<featureType><projectionPolicy>REPROJECT_TO_DECLARED</projectionPolicy></featureType>");
         }
 
         // Make sure we render using the default polygon style, and not whatever
         // colored style might have been used before
         sendRequest("rest/layers/" + workspace + ":" + layer, "PUT", "text/xml",
                 "<layer><defaultStyle><name>polygon</name></defaultStyle>"
                 + "<enabled>true</enabled></layer>");
         
         log.debug("Datastore successfully created on WFS server located at: " + url);
     }
 
     String getNameSpaceXML(String workspace) throws IOException {
         return getResponse("rest/namespaces/" + workspace + ".xml");
     }
 
     /**
      * @See http://internal.cida.usgs.gov/jira/browse/GDP-174?focusedCommentId=18712&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_18712
      */
     public void deleteOutdatedDataStores(long maximumFileAge, String... workspaces)
             throws IOException, XPathExpressionException {
 
         log.info("Wiping old files task for existing workspaces.");
 
         long now = new Date().getTime();
 
         for (String workspace : workspaces) {
             if (!workspaceExists(workspace)) {
                 log.info("Workspace '" + workspace + "' does not exist on Geoserver. Skipping.");
                 continue;
             }
 
             log.info("Checking workspace: " + workspace);
             List<String> dataStoreNames = listDataStores(workspace);
             // Check the data stores in this workspace
             for (String dataStore : dataStoreNames) {
                 // Let's get the disk location for this store -- We must remove the "file:" part of the return string
                 String diskLocation = retrieveDiskLocationOfDataStore(workspace, dataStore).split("file:")[1];
                 File diskLocationFileObject = new File(diskLocation);
 
                 // If either the file is on Geoserver and is old or
                 // the file doesn't exist on Geoserver, we will delete the
                 // the datastore from geoserver
                 boolean deleteFromGeoserver = false;
                 if (diskLocationFileObject.exists()) {
                     if (diskLocationFileObject.lastModified() < now - maximumFileAge) {
                         log.info("File " + diskLocationFileObject.getPath() + 
                                 " older than cutoff. Will remove from Geoserver.");
                         
                         deleteFromGeoserver = true;
                     }
                 } else {
                     log.info("File " + diskLocationFileObject.getPath() + 
                             " not found on disk. Will remove from Geoserver.");
                     
                     deleteFromGeoserver = true;
                 }
 
                 if (deleteFromGeoserver) {
                     deleteAndWipeDataStore(workspace, dataStore);
                 }
             }
         }
     }
 
     /**
      * Deletes the directory the shapefiles are located in on disk.
      */
     public void deleteAndWipeDataStore(String workspace, String dataStore)
             throws IOException, XPathExpressionException {
         
         String diskLocation = retrieveDiskLocationOfDataStore(workspace, dataStore).split("file:")[1];
         
         deleteDataStore(workspace, dataStore);
 
         // Get the location of the file from GeoServer
         File diskLocationFileObject = new File(diskLocation);
         if (diskLocationFileObject.isDirectory() && diskLocationFileObject.listFiles().length == 0) {
             // The location is a directory. Delete it
             try { FileUtils.deleteDirectory(diskLocationFileObject); }
             catch (IOException e) { 
                 log.warn("An error occurred while trying to delete directory" + 
                          diskLocationFileObject.getPath() + ". This may need to " +
                          "be deleted manually. \nError: "+e.getMessage()+"\nContinuing."); 
             }
         } else {
             if(!FileUtils.deleteQuietly(new File(diskLocationFileObject.getParent()))) {
                 log.warn("Could not fully remove the directory: " + 
                          diskLocationFileObject.getParent() + "\nPossibly files left over.");
             }
         }
     }
 
     boolean workspaceExists(String workspace) throws IOException {
         int responseCode = getResponseCode("rest/workspaces/" + workspace, "GET");
         
         return isSuccessResponse(responseCode);
     }
 
 
     boolean dataStoreExists(String workspace, String dataStore) throws IOException {
         int responseCode = getResponseCode("rest/workspaces/" + workspace + 
                 "/datastores/" + dataStore, "GET");
         
         return isSuccessResponse(responseCode);
     }
 
     boolean layerExists(String workspace, String dataStore, String layerName) throws IOException {
         int responseCode = getResponseCode("rest/workspaces/" + workspace + 
                 "/datastores/" + dataStore + "/featuretypes/" + layerName + ".xml", "GET");
 
         return isSuccessResponse(responseCode);
     }
 
     boolean styleExists(String styleName) throws IOException {
         int responseCode = getResponseCode("rest/styles/" + styleName, "GET");
 
         return isSuccessResponse(responseCode);
     }
     
     boolean isSuccessResponse(int responseCode) {
         switch(responseCode) {
             case 200: return true;
             default: return false;
         }
     }
 
     boolean deleteLayer(String layerName, boolean recursive) throws IOException {
         log.info("Deleting layer '"+layerName+"'");
         int responseCode = getResponseCode("rest/layers/" + layerName + 
                 ((recursive) ? "?recurse=true" : ""), "DELETE");
         
         switch (responseCode) {
             case 200: log.info("Layer '"+layerName+"' was successfully deleted.");
             case 404: log.info("Layer '"+layerName+"' was not found on server.");
             default:  log.info("Layer '"+layerName+"' could not be deleted.");
         }
         
         return isSuccessResponse(responseCode);
     }
 
     boolean deleteDataStore(String workspace, String dataStore)
             throws IOException, XPathExpressionException {
         
         log.info("Deleting datastore '"+dataStore+"' under workspace '"+workspace+"'");
         
         int responseCode = getResponseCode("rest/workspaces/" + workspace + 
                 "/datastores/" + dataStore + "?recurse=true", "DELETE");
 
         switch (responseCode) {
             case 200: log.info("Datastore '"+workspace+":"+dataStore+"' was successfully deleted.");
             case 404: log.info("Datastore '"+workspace+":"+dataStore+"' was not found on server.");
             default:  log.info("Datastore '"+workspace+":"+dataStore+"' could not be deleted.");
         }
         
         return isSuccessResponse(responseCode);
     }
 
     /**
      * Attempts to remove a featuretype from underneath a datastore on the Geoserver server.
      */
     boolean deleteFeatureType(String workspace, String dataStore, String featureType, boolean recursive) 
             throws IOException {
 
         log.info("Deleting feature type '"+workspace+":"+featureType+"'");
 
         int responseCode = getResponseCode("rest/workspaces/" + workspace + 
                 "/datastores/" + dataStore + "/featuretypes/" + featureType + 
                 ((recursive) ? "?recurse=true" : ""), "DELETE");
         
         switch (responseCode) {
             case 200: log.info("Feature type '"+workspace+":"+featureType+"' was successfully deleted.");
             case 404: log.info("Feature type '"+workspace+":"+featureType+"' was not found on server.");
             default:  log.info("Feature type '"+workspace+":"+featureType+"' could not be deleted.");
         }
         
         return isSuccessResponse(responseCode);
     }
 
     String retrieveDiskLocationOfDataStore(String workspace, String dataStore) 
             throws IOException, XPathExpressionException {
         
         String responseXML = getResponse("rest/workspaces/" + workspace + "/datastores/" + dataStore + ".xml");
         
         String result = XMLUtils.createNodeUsingXPathExpression(
                 "/dataStore/connectionParameters/entry[@key='url']", responseXML).getTextContent();
 
         return result;
     }
 
     public List<String> listWorkspaces()
             throws IOException, XPathExpressionException {
         
         return createListFromXML("/workspaces/workspace/name", getWorkspacesXML());
     }
 
     public List<String> listDataStores(String workspace)
             throws IOException, XPathExpressionException {
         
         if (!workspaceExists(workspace)) return new ArrayList<String>();
 
         return createListFromXML("/dataStores/dataStore/name", getDataStoresXML(workspace));
     }
     
     public List<String> listFeatureTypes(String workspace, String dataStore)
             throws XPathExpressionException, IOException {
         
         if (!workspaceExists(workspace) || !dataStoreExists(workspace, dataStore)) {
             return new ArrayList<String>();
         }
         
         return createListFromXML("/featureTypes/featureType/name", 
                 getFeatureTypesXML(workspace, dataStore));
     }
     
     List<String> createListFromXML(String expression, String xml) 
             throws XPathExpressionException, UnsupportedEncodingException {
         
         List<String> result = new ArrayList<String>();
 
         NodeList nodeList = XMLUtils.createNodeListUsingXPathExpression(expression, xml);
 
         for (int nodeIndex = 0;nodeIndex < nodeList.getLength();nodeIndex++) {
             result.add(nodeList.item(nodeIndex).getTextContent());
         }
         
         return result;
     }
 
     String getWorkspacesXML() throws IOException {
         return getResponse("rest/workspaces.xml");
     }
 
     String getDataStoresXML(String workspace) throws IOException {
         return getResponse("rest/workspaces/" + workspace + "/datastores.xml");
     }
 
     String getFeatureTypesXML(String workspace, String dataStore) throws IOException {
         return getResponse("rest/workspaces/" + workspace + "/datastores/" + dataStore + "/featuretypes.xml");
     }
     
     int getResponseCode(String path, String requestMethod) throws IOException {
         HttpResponse response = sendRequest(path, requestMethod, null, null);
         return response.getStatusLine().getStatusCode();
     }
     
     String getResponse(String path) throws IOException {
         
         HttpResponse response = sendRequest(path, "GET", null, null);
         
         ByteArrayOutputStream baos = null;
         try {
             baos = new ByteArrayOutputStream();
 
             HttpEntity entity = response.getEntity();
             entity.writeTo(baos);
         } finally {
             baos.close();
         }
         
         return baos.toString();
     }
     
     HttpResponse sendRequest(String path, String requestMethod, String contentType, String content)
             throws IOException {
         
         String fullURL = url + path;
         
         HttpUriRequest request = null;
         
         if ("GET".equals(requestMethod)) {
             request = new HttpGet(fullURL);
             
         } else if ("POST".equals(requestMethod)) {
             request = new HttpPost(fullURL);
             
         } else if ("PUT".equals(requestMethod)) {
             request = new HttpPut(fullURL);
             
         } else if ("DELETE".equals(requestMethod)) {
             request = new HttpDelete(fullURL);
             
         } else {
             throw new InvalidParameterException();
         }
         
         HttpClient client = new DefaultHttpClient();
         
         //Set authentication
         String encoding = new sun.misc.BASE64Encoder().encode((user + ":" + password).getBytes());
         request.addHeader("Authorization", "Basic " + encoding);
         
         if (contentType != null) {
             request.addHeader("Content-Type", contentType);
         }
 
         if (content != null) {
             if (request instanceof HttpEntityEnclosingRequestBase) {
                 StringEntity contentEntity = new StringEntity(content);
                 ((HttpEntityEnclosingRequestBase) request).setEntity(contentEntity);
             }
         }
         
         return client.execute(request);
     }
   
     static String createWorkspaceXML(String workspace) {
         return "<workspace><name>" + workspace + "</name></workspace>";
     }
 
     static String createDataStoreXML(String name, String workspace, String namespace, String url) {
 
         return  "<dataStore>" +
                 "  <name>" + name + "</name>" +
                 "  <type>Shapefile</type>" +
                 "  <enabled>true</enabled>" +
                 "  <workspace>" +
                 "    <name>" + workspace + "</name>" +
                 "  </workspace>" +
                 "  <connectionParameters>" +
                 "    <entry key=\"memory mapped buffer\">true</entry>" +
                 "    <entry key=\"create spatial index\">true</entry>" +
                 "    <entry key=\"charset\">ISO-8859-1</entry>" +
                 "    <entry key=\"url\">file:" + url + "</entry>" +
                 "    <entry key=\"namespace\">" + namespace + "</entry>" +
                 "  </connectionParameters>" +
                 "</dataStore>";
     }
 
     static String createFeatureTypeXML(String name, String workspace, String nativeCRS, String declaredCRS) {
 
         return  "<featureType>" +
                 "  <name>" + name + "</name>" +
                 "  <nativeName>" + name + "</nativeName>" +
                 "  <namespace>" +
                 "    <name>" + workspace + "</name>" +
                 "  </namespace>" +
                 "  <title>" + name + "</title>" +
                 "  <enabled>true</enabled>" +
                 // use CDATA as this may contain WKT with XML reserved characters
                 "  <nativeCRS><![CDATA[" + nativeCRS + "]]></nativeCRS>" +
                 "  <srs>" + declaredCRS + "</srs>" +
                 "  <store class=\"dataStore\">" +
                 "    <name>" + name + "</name>" + // this is actually the datastore name (we keep it the same as the layer name)
                 "  </store>" +
                 "</featureType>";
     }
     
     /**
      *  Ensure url ends with a '/'
      */
     static String fixURL(String url) {
         Matcher matcher = Pattern.compile("/$").matcher(url);
         if (!matcher.matches()) url += "/";
 
         return url;
     }
 }
