 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.integration.rest;
 
 import com.gooddata.exception.*;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.Project;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.util.FileUtil;
 import net.sf.json.JSON;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.cookie.CookieSpec;
 import org.apache.commons.httpclient.cookie.MalformedCookieException;
 import org.apache.commons.httpclient.cookie.RFC2109Spec;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The GoodData REST API Java wrapper.
  *
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcRESTApiWrapper {
 
     private static Logger l = Logger.getLogger(GdcRESTApiWrapper.class);
 
     /**
      * GDC URIs
      */
     private static final String MD_URI = "/gdc/md/";
     private static final String QUERY_URI = "/query/";
     private static final String LOGIN_URI = "/gdc/account/login";
     private static final String TOKEN_URI = "/gdc/account/token";
     private static final String DATA_INTERFACES_URI = "/ldm/singleloadinterface";
     private static final String PROJECTS_URI = "/gdc/projects";
     private static final String PULL_URI = "/etl/pull";
     private static final String IDENTIFIER_URI = "/identifiers";
     private static final String SLI_DESCRIPTOR_URI = "/descriptor";
     public static final String MAQL_EXEC_URI = "/ldm/manage";
     public static final String REPORT_QUERY = "/query/reports";
     public static final String EXECUTOR = "/gdc/xtab2/executor3";
     public static final String INVITATION_URI = "/invitations";
     public static final String OBJ_URI = "/obj";
     public static final String VARIABLES_SEARCH_URI = "/variables/search";
     public static final String VARIABLES_CREATE_URI = "/variables/item";
 
     public static final String OBJ_TYPE_FILTER = "filtres";
     public static final String OBJ_TYPE_METRIC = "metrics";
     public static final String OBJ_TYPE_REPORT = "reports";
     public static final String OBJ_TYPE_REPORT_DEFINITION = "reportdefinition";
     public static final String OBJ_TYPE_FACT = "facts";
     public static final String OBJ_TYPE_FOLDER = "folders";
     public static final String OBJ_TYPE_ATTRIBUTE = "attributes";
     public static final String OBJ_TYPE_DIMENSION = "dimensions";
     public static final String OBJ_TYPE_DATASET = "datasets";
     public static final String OBJ_TYPE_VARIABLE = "prompts";
     public static final String OBJ_TYPE_DASHBOARD = "projectdashboards";
     public static final String OBJ_TYPE_DOMAIN = "domains";
 
     public static final String DLI_MANIFEST_FILENAME = "upload_info.json";
 
     protected HttpClient client;
     protected NamePasswordConfiguration config;
     private String ssToken;
     private JSONObject profile;
 
     private static HashMap<String,Integer> ROLES = new HashMap<String,Integer>();
 
     private static final CookieSpec COOKIE_SPEC = new RFC2109Spec();
 
 
     /* TODO This is fragile and may not work for all projects and/or future versions.
      * Use /gdc/projects/{projectId}/roles to retrieve roles for a particular project.
      */
     static {
         ROLES.put("ADMIN",new Integer(1));
         ROLES.put("EDITOR",new Integer(2));
         ROLES.put("DASHBOARD ONLY",new Integer(3));
     }
 
     /**
      * Constructs the GoodData REST API Java wrapper
      *
      * @param config NamePasswordConfiguration object with the GDC name and password configuration
      */
     public GdcRESTApiWrapper(NamePasswordConfiguration config) {
         this.config = config;
         client = new HttpClient();
 
         final String proxyHost = System.getProperty("http.proxyHost");
         final int proxyPort = System.getProperty("http.proxyPort") == null
             ? 8080 : Integer.parseInt(System.getProperty("http.proxyPort"));
 
         if (proxyHost != null) {
             client.getHostConfiguration().setProxy(proxyHost,proxyPort);
         }
     }
 
     /**
      * GDC login - obtain GDC SSToken
      *
      * @return the new SS token
      * @throws GdcLoginException
      */
     public String login() throws GdcLoginException {
         l.debug("Logging into GoodData.");
         JSONObject loginStructure = getLoginStructure();
         PostMethod loginPost = createPostMethod(getServerUrl() + LOGIN_URI);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(loginStructure.toString().getBytes()));
         loginPost.setRequestEntity(request);
         try {
             String resp = executeMethodOk(loginPost, false); // do not re-login on SC_UNAUTHORIZED
             // read SST from cookie
             ssToken = extractCookie(loginPost, "GDCAuthSST");
             setTokenCookie();
             l.debug("Succesfully logged into GoodData.");
             JSONObject rsp = JSONObject.fromObject(resp);
             JSONObject userLogin =  rsp.getJSONObject("userLogin");
             String profileUri = userLogin.getString("profile");
             if(profileUri != null && profileUri.length()>0) {
                 GetMethod gm = createGetMethod(getServerUrl() + profileUri);
                 resp = executeMethodOk(gm);
                 this.profile = JSONObject.fromObject(resp);
             }
             else {
                 l.debug("Empty account profile.");
                 throw new GdcRestApiException("Empty account profile.");
             }
             return ssToken;
         } catch (HttpMethodException ex) {
             l.debug("Error logging into GoodData.", ex);
             throw new GdcLoginException("Login to GDC failed.", ex);
         } finally {
             loginPost.releaseConnection();
         }
 
     }
 
     /**
      * Creates a new login JSON structure
      *
      * @return the login JSON structure
      */
     private JSONObject getLoginStructure() {
         JSONObject credentialsStructure = new JSONObject();
         credentialsStructure.put("login", config.getUsername());
         credentialsStructure.put("password", config.getPassword());
         credentialsStructure.put("remember", 1);
 
         JSONObject loginStructure = new JSONObject();
         loginStructure.put("postUserLogin", credentialsStructure);
         return loginStructure;
     }
 
     private String extractCookie(HttpMethod method, String cookieName) {
 
         for (final Header cookieHeader : method.getResponseHeaders("Set-Cookie")) {
             try {
                 final Cookie[] cookies = COOKIE_SPEC.parse(
                         config.getGdcHost(),
                         "https".equals(config.getProtocol()) ? 443 : 80,
                         "/",  // force all cookie paths to be accepted
                         "https".equals(config.getProtocol()),
                         cookieHeader);
 
                 for (final Cookie cookie : cookies) {
                     if (cookieName.equals(cookie.getName())) {
                         return cookie.getValue();
                     }
                 }
             } catch (MalformedCookieException e) {
                 l.warn("Ignoring malformed cookie: " + e.getMessage());
                 l.debug("Ignoring malformed cookie", e);
             }
         }
         throw new GdcRestApiException(cookieName + " cookie not found in the response to "
                 + method.getName() + " to " + method.getPath());
     }
 
     /**
      * Sets the SS token
      *
      * @throws GdcLoginException
      */
     private void setTokenCookie() throws GdcLoginException {
         HttpMethod secutityTokenGet = createGetMethod(getServerUrl() + TOKEN_URI);
 
 
         // set SSToken from config
         Cookie sstCookie = new Cookie(config.getGdcHost(), "GDCAuthSST", ssToken, TOKEN_URI, -1, false);
         sstCookie.setPathAttributeSpecified(true);
         client.getState().addCookie(sstCookie);
 
         try {
             executeMethodOk(secutityTokenGet, false);
         } catch (HttpMethodException ex) {
             l.debug("Cannot login to:" + getServerUrl() + TOKEN_URI + ".",ex);
             throw new GdcLoginException("Cannot login to:" + getServerUrl() + TOKEN_URI + ".",ex);
         } finally {
             secutityTokenGet.releaseConnection();
         }
     }
 
 
     /**
      * Retrieves the project info by the project's name
      *
      * @param name the project name
      * @return the GoodDataProjectInfo populated with the project's information
      * @throws HttpMethodException
      * @throws GdcProjectAccessException
      */
     public Project getProjectByName(String name) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting project by name="+name);
         for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter.hasNext();) {
             JSONObject link = (JSONObject) linksIter.next();
             String cat = link.getString("category");
             if (!"project".equalsIgnoreCase(cat)) {
                 continue;
             }
             String title = link.getString("title");
             if (title.equals(name)) {
                 Project proj = new Project(link);
                 l.debug("Got project by name="+name);
                 return proj;
             }
         }
         l.debug("The project name=" + name + " doesn't exists.");
         throw new GdcProjectAccessException("The project name=" + name + " doesn't exists.");
     }
 
     /**
      * Retrieves the project info by the project's ID
      *
      * @param id the project id
      * @return the GoodDataProjectInfo populated with the project's information
      * @throws HttpMethodException
      * @throws GdcProjectAccessException
      */
     public Project getProjectById(String id) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting project by id="+id);
         for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter.hasNext();) {
             JSONObject link = (JSONObject) linksIter.next();
             String cat = link.getString("category");
             if (!"project".equalsIgnoreCase(cat)) {
                 continue;
             }
             String name = link.getString("identifier");
             if (name.equals(id)) {
                 Project proj = new Project(link);
                 l.debug("Got project by id="+id);
                 return proj;
             }
         }
         l.debug("The project id=" + id + " doesn't exists.");
         throw new GdcProjectAccessException("The project id=" + id + " doesn't exists.");
     }
 
     /**
      * Returns the existing projects links
      *
      * @return accessible projects links
      * @throws com.gooddata.exception.HttpMethodException
      */
     @SuppressWarnings("unchecked")
     private Iterator<JSONObject> getProjectsLinks() throws HttpMethodException {
         l.debug("Getting project links.");
         HttpMethod req = createGetMethod(getServerUrl() + MD_URI);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             JSONObject about = parsedResp.getJSONObject("about");
             JSONArray links = about.getJSONArray("links");
             l.debug("Got project links "+links);
             return links.iterator();
         }
         finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Returns the List of GoodDataProjectInfo structures for the accessible projects
      *
      * @return the List of GoodDataProjectInfo structures for the accessible projects
      * @throws HttpMethodException
      */
     public List<Project> listProjects() throws HttpMethodException {
         l.debug("Listing projects.");
         List<Project> list = new ArrayList<Project>();
         for (Iterator<JSONObject> linksIter = getProjectsLinks(); linksIter.hasNext();) {
             JSONObject link = linksIter.next();
             String cat = link.getString("category");
             if (!"project".equalsIgnoreCase(cat)) {
                 continue;
             }
             Project proj = new Project(link);
             list.add(proj);
         }
         l.debug("Found projects "+list);
         return list;
     }
 
     /**
      * Returns a list of project's SLIs
      *
      * @param projectId project's ID
      * @return a list of project's SLIs
      * @throws HttpMethodException if there is a communication error
      * @throws GdcProjectAccessException if the SLI doesn't exist
      */
     public List<SLI> getSLIs(String projectId) throws HttpMethodException, GdcProjectAccessException {
         l.debug("Getting SLIs from project id="+projectId);
         List<SLI> list = new ArrayList<SLI>();
         String ifcUri = getSLIsUri(projectId);
         HttpMethod interfacesGet = createGetMethod(ifcUri);
         try {
             String response = executeMethodOk(interfacesGet);
             JSONObject responseObject = JSONObject.fromObject(response);
             if (responseObject.isNullObject()) {
                 l.debug("The project id=" + projectId + " doesn't exist!");
                 throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
             }
             JSONObject interfaceQuery = responseObject.getJSONObject("about");
             if (interfaceQuery.isNullObject()) {
                 l.debug("The project id=" + projectId + " doesn't exist!");
                 throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
             }
             JSONArray links = interfaceQuery.getJSONArray("links");
             if (links == null) {
                 l.debug("The project id=" + projectId + " doesn't exist!");
                 throw new GdcProjectAccessException("The project id=" + projectId + " doesn't exist!");
             }
             for (Object ol : links) {
                 JSONObject link = (JSONObject) ol;
                 SLI ii = new SLI(link);
                 list.add(ii);
             }
             l.debug("Got SLIs "+list+" from project id="+projectId);
         }
         finally {
             interfacesGet.releaseConnection();
         }
         return list;
     }
 
     
 
     /**
      * Retrieves the SLI columns
      *
      * @param uri the SLI uri
      * @return list of SLI columns
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException if there is a communication issue with the GDC platform
      */
     public List<Column> getSLIColumns(String uri) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Retrieveing SLI columns for SLI uri="+uri);
         List<Column> list = new ArrayList<Column>();
         HttpMethod sliGet = createGetMethod(getServerUrl() + uri + "/manifest");
         try {
             String response = executeMethodOk(sliGet);
             JSONObject responseObject = JSONObject.fromObject(response);
             if (responseObject.isNullObject()) {
                 l.debug("The SLI uri=" + uri + " doesn't exist!");
                 throw new GdcProjectAccessException("The SLI uri=" + uri + " doesn't exist!");
             }
             JSONObject dataSetSLIManifest = responseObject.getJSONObject("dataSetSLIManifest");
             if (dataSetSLIManifest.isNullObject()) {
                 l.debug("The SLI uri=" + uri + " doesn't exist!");
                 throw new GdcProjectAccessException("The SLI uri=" + uri + " doesn't exist!");
             }
             JSONArray parts = dataSetSLIManifest.getJSONArray("parts");
             for(Object oPart : parts) {
                 list.add(new Column((JSONObject)oPart));
             }
         }
         finally {
             sliGet.releaseConnection();
         }
         return list;
     }
 
  /**
      * Retrieves the SLI column data type
      *
      * @param projectId projectId
      * @param sliColumnIdentifier SLI column identifier (name in the SLI manifest)
      * @return the SLI column datatype
      */
     public String getSLIColumnDataType(String projectId, String sliColumnIdentifier)  {
         l.debug("Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier);
         MetadataObject o = getMetadataObject(projectId, sliColumnIdentifier);
         if(o!=null) {
             JSONObject c = o.getContent();
             if(c != null) {
                 String type = c.getString("columnType");
                 if(type != null && type.length() > 0) {
                     return type;
                 }
                 else {
                     l.debug("Error Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier+" No columnType key in the content.");
                     throw new GdcRestApiException("Error Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier+" No columnType key in the content.");
                 }
             }
             else {
                 l.debug("Error Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier+" No content structure.");
                 throw new GdcRestApiException("Error Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier+" No content structure.");
             }
         }
         else {
             l.debug("Error Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier+" MD object doesn't exist.");
             throw new GdcRestApiException("Error Retrieveing SLI column datatype projectId="+projectId+" SLI column name="+sliColumnIdentifier+" MD object doesn't exist.");
         }
     }
 
     /**
      * Retrieves the SLI columns
      *
      * @param uri the SLI uri
      * @return JSON manifest
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException if there is a communication issue with the GDC platform
      */
     public JSONObject getSLIManifest(String uri) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Retrieveing SLI columns for SLI uri="+uri);
         List<Column> list = new ArrayList<Column>();
         HttpMethod sliGet = createGetMethod(getServerUrl() + uri + "/manifest");
         try {
             String response = executeMethodOk(sliGet);
             JSONObject responseObject = JSONObject.fromObject(response);
             if (responseObject.isNullObject()) {
                 l.debug("The SLI uri=" + uri + " doesn't exist!");
                 throw new GdcProjectAccessException("The SLI uri=" + uri + " doesn't exist!");
             }
             return responseObject;
         }
         finally {
             sliGet.releaseConnection();
         }
     }
 
 
     /**
      * Finds a project SLI by it's name
      *
      * @param name the SLI name
      * @param projectId the project id
      * @return the SLI
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException if there is a communication issue with the GDC platform
      */
     public SLI getSLIByName(String name, String projectId) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Get SLI by name="+name+" project id="+projectId);
         List<SLI> slis = getSLIs(projectId);
         for (SLI sli : slis) {
             if (name.equals(sli.getName())) {
                 l.debug("Got SLI by name="+name+" project id="+projectId);
                 return sli;
             }
         }
         l.debug("The SLI name=" + name + " doesn't exist in the project id="+projectId);
         throw new GdcProjectAccessException("The SLI name=" + name + " doesn't exist in the project id="+projectId);
     }
 
     /**
      * Finds a project SLI by it's id
      *
      * @param id the SLI id
      * @param projectId the project id
      * @return the SLI
      * @throws GdcProjectAccessException if the SLI doesn't exist
      * @throws HttpMethodException if there is a communication issue with the GDC platform
      */
     public SLI getSLIById(String id, String projectId) throws GdcProjectAccessException, HttpMethodException {
         l.debug("Get SLI by id="+id+" project id="+projectId);
         List<SLI> slis = getSLIs(projectId);
         for (SLI sli : slis) {
             if (id.equals(sli.getId())) {
                 l.debug("Got SLI by id="+id+" project id="+projectId);
                 return sli;
             }
         }
         l.debug("The SLI id=" + id+ " doesn't exist in the project id="+projectId);
         throw new GdcProjectAccessException("The SLI id=" + id+ " doesn't exist in the project id="+projectId);
     }
 
 
     public String getToken() {
         return ssToken;
     }
 
 
     /**
      * Enumerates all reports on in a project
      * @param projectId project Id
      * @return LIst of report uris
      */
     public List<String> enumerateReports(String projectId) {
         l.debug("Enumerating reports for project id="+projectId);
         List<String> list = new ArrayList<String>();
         String qUri = getProjectMdUrl(projectId) + REPORT_QUERY;
         HttpMethod qGet = createGetMethod(qUri);
         try {
             String qr = executeMethodOk(qGet);
             JSONObject q = JSONObject.fromObject(qr);
             if (q.isNullObject()) {
                 l.debug("Enumerating reports for project id="+projectId+" failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id="+projectId+" failed.");
             }
             JSONObject qry = q.getJSONObject("query");
             if (qry.isNullObject()) {
                 l.debug("Enumerating reports for project id="+projectId+" failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id="+projectId+" failed.");
             }
             JSONArray entries = qry.getJSONArray("entries");
             if (entries == null) {
                 l.debug("Enumerating reports for project id="+projectId+" failed.");
                 throw new GdcProjectAccessException("Enumerating reports for project id="+projectId+" failed.");
             }
             for(Object oentry : entries) {
                 JSONObject entry = (JSONObject)oentry;
                 int deprecated = entry.getInt("deprecated");
                 if(deprecated == 0)
                     list.add(entry.getString("link"));
             }
         }
         finally {
             qGet.releaseConnection();
         }
         return list;
     }
 
     /**
      * Gets a report definition from the report uri (/gdc/obj...)
      * @param reportUri report uri (/gdc/obj...)
      * @return report definition
      */
     public String getReportDefinition(String reportUri) {
         HttpMethod qGet = null;
         try {
             l.debug("Getting report definition for report uri="+reportUri);
             String qUri = getServerUrl() + reportUri;
             qGet = createGetMethod(qUri);
             String qr = executeMethodOk(qGet);
             JSONObject q = JSONObject.fromObject(qr);
             if (q.isNullObject()) {
                 l.debug("Error getting report definition for report uri="+reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri="+reportUri);
             }
             JSONObject report = q.getJSONObject("report");
             if (report.isNullObject()) {
                 l.debug("Error getting report definition for report uri="+reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri="+reportUri);
             }
             JSONObject content = report.getJSONObject("content");
             if (content.isNullObject()) {
                 l.debug("Error getting report definition for report uri="+reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri="+reportUri);
             }
             JSONArray results = content.getJSONArray("results");
             if (results == null) {
                 l.debug("Error getting report definition for report uri="+reportUri);
                 throw new GdcProjectAccessException("Error getting report definition for report uri="+reportUri);
             }
             if(results.size()>0) {
                 String lastResultUri = results.getString(results.size()-1);
                 qUri = getServerUrl() + lastResultUri;
                 qGet = createGetMethod(qUri);
                 qr = executeMethodOk(qGet);
                 q = JSONObject.fromObject(qr);
                 if (q.isNullObject()) {
                     l.debug("Error getting report definition for result uri="+lastResultUri);
                     throw new GdcProjectAccessException("Error getting report definition for result uri="+lastResultUri);
                 }
                 JSONObject result = q.getJSONObject("reportResult2");
                 if (result.isNullObject()) {
                     l.debug("Error getting report definition for result uri="+lastResultUri);
                     throw new GdcProjectAccessException("Error getting report definition for result uri="+lastResultUri);
                 }
                 content = result.getJSONObject("content");
                 if (result.isNullObject()) {
                     l.debug("Error getting report definition for result uri="+lastResultUri);
                     throw new GdcProjectAccessException("Error getting report definition for result uri="+lastResultUri);
                 }
                 return content.getString("reportDefinition");
             }
             // Here we haven't found any results. Let's try the defaultReportDefinition
             if(content.containsKey("defaultReportDefinition")) {
                 String defaultRepDef = content.getString("defaultReportDefinition");
                 if(defaultRepDef != null && defaultRepDef.length() > 0)
                     return defaultRepDef;
             }
             l.debug("Error getting report definition for report uri="+reportUri+" . No report results!");
             throw new GdcProjectAccessException("Error getting report definition for report uri="+reportUri+
                     " . No report results!");
         }
         finally {
             if(qGet != null)
                 qGet.releaseConnection();
         }
     }
 
     private String getProjectIdFromObjectUri(String uri) {
         Pattern regexp = Pattern.compile("gdc/md/.*?/");
         Matcher m = regexp.matcher(uri);
         if(m.find()) {
             return m.group().split("/")[2];
         }
         else {
             l.debug("The passed string '"+uri+"' doesn't have the GoodData URI structure!");
             throw new InvalidParameterException("The passed string '"+uri+"' doesn't have the GoodData URI structure!");
         }
     }
 
     /**
      * Computes the metric value
      * @param metricUri metric URI
      * @return the metric value
      */
     public double computeMetric(String metricUri) {
         l.debug("Computing metric uri="+metricUri);
         double retVal = 0;
         String projectId = getProjectIdFromObjectUri(metricUri);
         JSONObject reportDefinition = new JSONObject();
 
         JSONObject metric = new JSONObject();
         metric.put("alias", "");
         metric.put("uri", metricUri);
         JSONArray metrics = new JSONArray();
         metrics.add(metric);
         JSONArray columns = new JSONArray();
         columns.add("metricGroup");
         JSONObject grid = new JSONObject();
         grid.put("metrics", metrics);
         grid.put("columns", columns);
         grid.put("rows",new JSONArray());
         grid.put("columnWidths",new JSONArray());
 
         JSONObject sort = new JSONObject();
         sort.put("columns",new JSONArray());
         sort.put("rows",new JSONArray());
 
         grid.put("sort", sort);
 
         JSONObject content = new JSONObject();
         content.put("grid", grid);
         content.put("filters",new JSONArray());
 
         reportDefinition.put("content", content);
 
         JSONObject meta = new JSONObject();
         meta.put("category","reportDefinition");
         meta.put("title", "N/A");
 
         reportDefinition.put("meta", meta);
 
         MetadataObject obj = new MetadataObject();
         obj.put("reportDefinition", reportDefinition);
         MetadataObject resp = new MetadataObject(createMetadataObject(projectId, obj));
 
         String dataResultUri = executeReportDefinition(resp.getUri());
         JSONObject result = getObjectByUri(dataResultUri);
         if(result != null && !result.isEmpty() && !result.isNullObject()) {
             JSONObject xtabData = result.getJSONObject("xtab_data");
             if(xtabData != null && !xtabData.isEmpty() && !xtabData.isNullObject()) {
                 JSONArray data = xtabData.getJSONArray("data");
                 if(data != null && !data.isEmpty()) {
                     retVal = data.getJSONArray(0).getDouble(0);
                 }
                 else {
                     l.debug("Can't compute the metric. No data structure in result.");
                     throw new InvalidParameterException("Can't compute the metric. No data structure in result.");
                 }
             }
             else {
                 l.debug("Can't compute the metric. No xtab_data structure in result.");
                 throw new InvalidParameterException("Can't compute the metric. No xtab_data structure in result.");
             }
         }
         else {
             l.debug("Can't compute the metric. No result from XTAB.");
             throw new InvalidParameterException("Can't compute the metric. No result from XTAB.");
         }
         l.debug("Metric uri="+metricUri+ " computed. Result is "+retVal);
         return retVal;
     }
 
 
     /**
      * Report definition to execute
      * @param reportDefUri report definition to execute
      */
     public String executeReportDefinition(String reportDefUri) {
         l.debug("Executing report definition uri="+reportDefUri);
         PostMethod execPost = createPostMethod(getServerUrl() + EXECUTOR);
         JSONObject execDef = new JSONObject();
         execDef.put("reportDefinition",reportDefUri);
         JSONObject exec = new JSONObject();
         exec.put("report_req", execDef);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(exec.toString().getBytes()));
         execPost.setRequestEntity(request);
         String taskLink = null;
         try {
             String task = executeMethodOk(execPost);
             if(task != null && task.length()>0) {
                 JSONObject tr = JSONObject.fromObject(task);
                 if(tr.isNullObject()) {
                     l.debug("Executing report definition uri="+reportDefUri + " failed. Returned invalid result result="+tr);
                     throw new GdcRestApiException("Executing report definition uri="+reportDefUri + " failed. " +
                             "Returned invalid result result="+tr);
                 }
                 JSONObject reportResult = tr.getJSONObject("reportResult2");
                 if(reportResult.isNullObject()) {
                     l.debug("Executing report definition uri="+reportDefUri + " failed. Returned invalid result result="+tr);
                     throw new GdcRestApiException("Executing report definition uri="+reportDefUri + " failed. " +
                             "Returned invalid result result="+tr);
                 }
                 JSONObject content = reportResult.getJSONObject("content");
                 if(content.isNullObject()) {
                     l.debug("Executing report definition uri="+reportDefUri + " failed. Returned invalid result result="+tr);
                     throw new GdcRestApiException("Executing report definition uri="+reportDefUri + " failed. " +
                             "Returned invalid result result="+tr);
                 }
                 return content.getString("dataResult");
             }
             else {
                 l.debug("Executing report definition uri="+reportDefUri + " failed. Returned invalid task link uri="+task);
                 throw new GdcRestApiException("Executing report definition uri="+reportDefUri +
                         " failed. Returned invalid task link uri="+task);
             }
         } catch (HttpMethodException ex) {
             l.debug("Executing report definition uri="+reportDefUri + " failed.", ex);
             throw new GdcRestApiException("Executing report definition uri="+reportDefUri + " failed.");
         } finally {
             execPost.releaseConnection();
         }
     }
 
     /**
      * Kicks the GDC platform to inform it that the FTP transfer is finished.
      *
      * @param projectId the project's ID
      * @param remoteDir the remote (FTP) directory that contains the data
      * @return the link that is used for polling the loading progress
      * @throws GdcRestApiException
      */
     public String startLoading(String projectId, String remoteDir) throws GdcRestApiException {
         l.debug("Initiating data load project id="+projectId+" remoteDir="+remoteDir);
         PostMethod pullPost = createPostMethod(getProjectMdUrl(projectId) + PULL_URI);
         JSONObject pullStructure = getPullStructure(remoteDir);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(pullStructure.toString().getBytes()));
         pullPost.setRequestEntity(request);
         String taskLink = null;
         try {
             String response = executeMethodOk(pullPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             taskLink = responseObject.getJSONObject("pullTask").getString("uri");
         } catch (HttpMethodException ex) {
             throw new GdcRestApiException("Loading fails: " + ex.getMessage());
         } finally {
             pullPost.releaseConnection();
         }
         l.debug("Data load project id="+projectId+" remoteDir="+remoteDir+" initiated. Status is on uri="+taskLink);
         return taskLink;
     }
 
     /**
      * Returns the pull API JSON structure
      *
      * @param directory the remote directory
      * @return the pull API JSON structure
      */
     private JSONObject getPullStructure(String directory) {
         JSONObject pullStructure = new JSONObject();
         pullStructure.put("pullIntegration", directory);
         return pullStructure;
     }
 
 
     /**
      * Checks if the loading is finished
      *
      * @param link the link returned from the start loading
      * @return the loading status
      */
     public String getLoadingStatus(String link) throws HttpMethodException {
         l.debug("Getting data loading status uri="+link);
         HttpMethod ptm = createGetMethod(getServerUrl() + link);
         try {
             String response = executeMethodOk(ptm);
             JSONObject task = JSONObject.fromObject(response);
             String status = task.getString("taskStatus");
             l.debug("Loading status="+status);
             return status;
         }
         finally {
             ptm.releaseConnection();
         }
     }
 
 
 
     /**
      * Create a new GoodData project
      *
      * @param name project name
      * @param desc project description
      * @param templateUri project template uri
      * @return the project Id
      * @throws GdcRestApiException
      */
     public String createProject(String name, String desc, String templateUri) throws GdcRestApiException {
         l.debug("Creating project name="+name);
         PostMethod createProjectPost = createPostMethod(getServerUrl() + PROJECTS_URI);
         JSONObject createProjectStructure = getCreateProject(name, desc, templateUri);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 createProjectStructure.toString().getBytes()));
         createProjectPost.setRequestEntity(request);
         String uri = null;
         try {
             String response = executeMethodOk(createProjectPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             uri = responseObject.getString("uri");
         } catch (HttpMethodException ex) {
             l.debug("Creating project fails: ",ex);
             throw new GdcRestApiException("Creating project fails: ",ex);
         } finally {
             createProjectPost.releaseConnection();
         }
 
         if(uri != null && uri.length() > 0) {
             String id = getProjectId(uri);
             l.debug("Created project id="+id);
             return id;
         }
         l.debug("Error creating project.");
         throw new GdcRestApiException("Error creating project.");
     }
 
 /**
      * Returns the create project JSON structure
      *
      * @param name project name
      * @param desc project description
      * @param templateUri project template uri
      * @return the create project JSON structure
      */
     private JSONObject getCreateProject(String name, String desc, String templateUri) {
         JSONObject meta = new JSONObject();
         meta.put("title", name);
         meta.put("summary", desc);
         if(templateUri != null && templateUri.length() > 0) {
             meta.put("projectTemplate", templateUri);
         }
         JSONObject content = new JSONObject();
         //content.put("state", "ENABLED");
         content.put("guidedNavigation","1");
         JSONObject project = new JSONObject();
         project.put("meta", meta);
         project.put("content", content);
         JSONObject createStructure  = new JSONObject();
         createStructure.put("project", project);
         return createStructure;
     }
 
     /**
      * Returns the project status
      * @param projectId project ID
      * @return current project status
      */
     public String getProjectStatus(String projectId) {
         l.debug("Getting project status for project "+projectId);
         String uri = getProjectDeleteUri(projectId);
         HttpMethod ptm = createGetMethod(getServerUrl() + uri);
         try {
             String response = executeMethodOk(ptm);
             JSONObject jresp = JSONObject.fromObject(response);
             JSONObject project = jresp.getJSONObject("project");
             JSONObject content = project.getJSONObject("content");
             String status = content.getString("state");
             l.debug("Project "+projectId+" status="+status);
             return status;
         }
         finally {
             ptm.releaseConnection();
         }
     }
 
     /**
      * Drops a GoodData project
      *
      * @param projectId project id
      * @throws GdcRestApiException
      */
     public void dropProject(String projectId) throws GdcRestApiException {
         l.debug("Dropping project id="+projectId);
         DeleteMethod dropProjectDelete = createDeleteMethod(getServerUrl() + getProjectDeleteUri(projectId));
         try {
             executeMethodOk(dropProjectDelete);
         } catch (HttpMethodException ex) {
             l.debug("Dropping project id="+projectId + " failed.",ex);
             throw new GdcRestApiException("Dropping project id="+projectId + " failed.",ex);
         } finally {
             dropProjectDelete.releaseConnection();
         }
         l.debug("Dropped project id="+projectId);
     }
 
     /**
      * Retrieves the project id from the URI returned by the create project
      * @param uri the create project URI
      * @return project id
      * @throws GdcRestApiException in case the project doesn't exist
      */
     protected String getProjectId(String uri) throws GdcRestApiException {
         l.debug("Getting project id by uri="+uri);
         HttpMethod req = createGetMethod(getServerUrl() + uri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if(parsedResp.isNullObject()) {
                 l.debug("Can't get project from "+uri);
                 throw new GdcRestApiException("Can't get project from "+uri);
             }
             JSONObject project = parsedResp.getJSONObject("project");
             if(project.isNullObject()) {
                 l.debug("Can't get project from "+uri);
                 throw new GdcRestApiException("Can't get project from "+uri);
             }
             JSONObject links = project.getJSONObject("links");
             if(links.isNullObject()) {
                 l.debug("Can't get project from "+uri);
                 throw new GdcRestApiException("Can't get project from "+uri);
             }
             String mdUrl = links.getString("metadata");
             if(mdUrl != null && mdUrl.length()>0) {
                 String[] cs = mdUrl.split("/");
                 if(cs != null && cs.length > 0) {
                     l.debug("Got project id="+cs[cs.length -1]+" by uri="+uri);
                     return cs[cs.length -1];
                 }
             }
             l.debug("Can't get project from "+uri);
             throw new GdcRestApiException("Can't get project from "+uri);
         }
         finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Executes the MAQL and creates/modifies the project's LDM
      *
      * @param projectId the project's ID
      * @param maql String with the MAQL statements
      * @return result String
      * @throws GdcRestApiException
      */
     public String[] executeMAQL(String projectId, String maql) throws GdcRestApiException {
         l.debug("Executing MAQL projectId="+projectId+" MAQL:\n"+maql);
         PostMethod maqlPost = createPostMethod(getProjectMdUrl(projectId) + MAQL_EXEC_URI);
         JSONObject maqlStructure = getMAQLExecStructure(maql);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 maqlStructure.toString().getBytes()));
         maqlPost.setRequestEntity(request);
         String result = null;
         try {
             String response = executeMethodOk(maqlPost);
             JSONObject responseObject = JSONObject.fromObject(response);
             JSONArray uris = responseObject.getJSONArray("uris");
             return (String[])uris.toArray(new String[]{""});
         } catch (HttpMethodException ex) {
             l.debug("MAQL execution: ",ex);
             throw new GdcRestApiException("MAQL execution: ",ex);
         } finally {
             maqlPost.releaseConnection();
         }
     }
 
     /**
      * Returns the pull API JSON structure
      *
      * @param maql String with the MAQL statements
      * @return the MAQL API JSON structure
      */
     private JSONObject getMAQLExecStructure(String maql) {
         JSONObject maqlStructure = new JSONObject();
         JSONObject maqlObj = new JSONObject();
         maqlObj.put("maql", maql);
         maqlStructure.put("manage", maqlObj);
         return maqlStructure;
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @return response body as String
      * @throws HttpMethodException
      */
     protected String executeMethodOk(HttpMethod method) throws HttpMethodException {
         return executeMethodOk(method, true);
     }
 
     /**
      * Executes HttpMethod and test if the response if 200(OK)
      *
      * @param method the HTTP method
      * @param reLoginOn401 flag saying whether we should call login() and retry on
      * @return response body as String
      * @throws HttpMethodException
      */
     private String executeMethodOk(HttpMethod method, boolean reLoginOn401) throws HttpMethodException {
         if (reLoginOn401) {
             setTokenCookie();
         }
         try {
             client.executeMethod(method);
             if (method.getStatusCode() == HttpStatus.SC_OK) {
                 return method.getResponseBodyAsString();
             } else if (method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED && reLoginOn401) {
                 // refresh the temporary token
                 setTokenCookie();
                 return executeMethodOk(method, false);
             } else if (method.getStatusCode() == HttpStatus.SC_CREATED) {
                 return method.getResponseBodyAsString();
             } else if (method.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                 throw new HttpMethodNotFinishedYetException(method.getResponseBodyAsString());
             } else if (method.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                 throw new HttpMethodNoContentException(method.getResponseBodyAsString());
             } else {
                 String msg = method.getStatusCode() + " " + method.getStatusText();
                 String body = method.getResponseBodyAsString();
                 if (body != null) {
                     msg += ": ";
                     try {
                         JSONObject parsedBody = JSONObject.fromObject(body);
                         msg += parsedBody.toString();
                     } catch (JSONException jsone) {
                         msg += body;
                     }
                 }
                 l.debug("Exception executing " + method.getName() + " on " + method.getPath() + ": " + msg);
                 throw new HttpMethodException("Exception executing " + method.getName() + " on " + method.getPath() + ": " + msg);
             }
         } catch (HttpException e) {
             l.debug("Error invoking GoodData REST API.",e);
             throw new HttpMethodException("Error invoking GoodData REST API.",e);
         } catch (IOException e) {
             l.debug("Error invoking GoodData REST API.",e);
             throw new HttpMethodException("Error invoking GoodData REST API.",e);
         }
     }
 
     /**
      * Returns the data interfaces URI
      *
      * @param projectId project ID
      * @return SLI collection URI
      */
     public String getSLIsUri(String projectId) {
         return getProjectMdUrl(projectId) + DATA_INTERFACES_URI;
     }
 
     /**
      * Returns the SLI URI
      *
      * @param sliId SLI ID
      * @param projectId project ID
      * @return DLI URI
      */
     public String getSLIUri(String sliId, String projectId) {
         return getProjectMdUrl(projectId) + DATA_INTERFACES_URI + "/" + sliId + SLI_DESCRIPTOR_URI;
     }
 
 
     protected String getServerUrl() {
         return config.getUrl();
     }
 
     /**
      * Constructs project's metadata uri
      *
      * @param projectId project ID
      */
     protected String getProjectMdUrl(String projectId) {
         return getServerUrl() + MD_URI + projectId;
     }
 
     /**
      * Gets the project ID from the project URI
      * @param projectUri project URI
      * @return the project id
      */
     public String getProjectIdFromUri(String projectUri) {
         String[] cmpnts = projectUri.split("/");
         if(cmpnts != null && cmpnts.length > 0) {
             String id = cmpnts[cmpnts.length-1];
             return id;
         }
         else
             throw new GdcRestApiException("Invalid project uri structure uri="+projectUri);
     }
 
     /**
      * Gets the project delete URI from the project id
      * @param projectId project ID
      * @return the project delete URI
      */
     protected String getProjectDeleteUri(String projectId) {
         if(profile != null) {
             JSONObject as = profile.getJSONObject("accountSetting");
             if(as!=null) {
                 JSONObject lnks = as.getJSONObject("links");
                 if(lnks != null) {
                     String projectsUri = lnks.getString("projects");
                     if(projectsUri != null && projectsUri.length()>0) {
                         HttpMethod req = createGetMethod(getServerUrl()+projectsUri);
                         try {
                             String resp = executeMethodOk(req);
                             JSONObject rsp = JSONObject.fromObject(resp);
                             if(rsp != null) {
                                 JSONArray projects = rsp.getJSONArray("projects");
                                 for(Object po : projects) {
                                     JSONObject p = (JSONObject)po;
                                     JSONObject project = p.getJSONObject("project");
                                     if(project != null) {
                                         JSONObject links = project.getJSONObject("links");
                                         if(links != null) {
                                             String uri = links.getString("metadata");
                                             if(uri != null && uri.length() > 0) {
                                                 String id = getProjectIdFromUri(uri);
                                                 if(projectId.equals(id)) {
                                                     String sf = links.getString("self");
                                                     if(sf != null && sf.length()>0)
                                                         return sf;
                                                 }
                                             }
                                             else {
                                                 l.debug("Project with no metadata uri.");
                                                 throw new GdcRestApiException("Project with no metadata uri.");
                                             }
                                         }
                                         else {
                                             l.debug("Project with no links.");
                                             throw new GdcRestApiException("Project with no links.");
                                         }
                                     }
                                     else {
                                         l.debug("No project in the project list.");
                                         throw new GdcRestApiException("No project in the project list.");
                                     }
                                 }
                             }
                             else {
                                 l.debug("Can't get project from "+projectsUri);
                                 throw new GdcRestApiException("Can't get projects from uri="+projectsUri);
                             }
                         }
                         finally {
                             req.releaseConnection();
                         }
                     }
                     else {
                         l.debug("No projects link in the account settings.");
                         throw new GdcRestApiException("No projects link in the account settings.");
                     }
                 }
                 else {
                     l.debug("No links in the account settings.");
                     throw new GdcRestApiException("No links in the account settings.");
                 }
             }
             else {
                 l.debug("No account settings.");
                 throw new GdcRestApiException("No account settings.");
             }
         }
         else {
             l.debug("No active account profile found. Perhaps you are not connected to the GoodData anymore.");
             throw new GdcRestApiException("No active account profile found. Perhaps you are not connected to the GoodData anymore.");
         }
         l.debug("Project "+projectId+" not found in the current account profile.");
         throw new GdcRestApiException("Project "+projectId+" not found in the current account profile.");
     }
 
     /**
      * Profile getter
      * @return the profile of the currently logged user
      */
     protected JSONObject getProfile() {
         return profile;
     }
 
     /**
      * Invites a new user to a project
      * @param projectId project ID
      * @param eMail invited user e-mail
      * @param message invitation message
      */
     public void inviteUser(String projectId, String eMail, String message) {
         this.inviteUser(projectId, eMail, message, null);
     }
 
     /**
      * Invites a new user to a project
      * @param projectId project ID
      * @param eMail invited user e-mail
      * @param message invitation message
      */
     public void inviteUser(String projectId, String eMail, String message, String role) {
         l.debug("Executing inviteUser projectId="+projectId+" e-mail="+eMail+" message="+message);
         PostMethod invitePost = createPostMethod(getServerUrl() + getProjectDeleteUri(projectId) + INVITATION_URI);
         JSONObject inviteStructure = getInviteStructure(projectId, eMail, message, role);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 inviteStructure.toString().getBytes()));
         invitePost.setRequestEntity(request);
         try {
             executeMethodOk(invitePost);
         } catch (HttpMethodException ex) {
             l.debug("Failed executing inviteUser projectId="+projectId+" e-mail="+eMail+" message="+message);
             throw new GdcRestApiException("Failed executing inviteUser projectId="+projectId+" e-mail="+eMail+" message="+message,ex);
         } finally {
             invitePost.releaseConnection();
         }
     }
 
     /**
      * Creates a new invitation structure
      * @param pid project id
      * @param eMail e-mail
      * @param msg invitation message
      * @return the new invitation structure
      */
     private JSONObject getInviteStructure(String pid, String eMail, String msg, String role) {
         JSONObject content = new JSONObject();
         content.put("firstname","");
         content.put("lastname","");
         content.put("email",eMail);
         String puri = getServerUrl() + getProjectDeleteUri(pid);
         if(role != null && role.length() > 0) {
             Integer roleId = ROLES.get(role.toUpperCase());
             if(roleId == null)
                 throw new InvalidParameterException("The role '"+role+"' is not recognized by the GoodData platform.");
             content.put("role",puri+"/"+roleId);
         }
         JSONObject action = new JSONObject();
         action.put("setMessage",msg);
         content.put("action", action);
         JSONObject invitation = new JSONObject();
         invitation.put("content", content);
         JSONObject invitations = new JSONObject();
         JSONArray ia = new JSONArray();
         JSONObject inve = new JSONObject();
         inve.put("invitation", invitation);
         ia.add(inve);
         invitations.put("invitations", ia);
         return invitations;
     }
 
     /**
      * Converst MD identifier to uri
      * @param projectId project ID
      * @param identifiers MD object identifiers
      * @return map identifier:uri
      */
     public Map<String,String> identifierToUri(String projectId, String[] identifiers) {
         l.debug("Executing identifierToUri identifier="+identifiers);
         Map<String, String> result = new HashMap<String,String>();
         PostMethod p = createPostMethod(getProjectMdUrl(projectId) +  IDENTIFIER_URI);
         JSONObject is = getIdentifiersStructure(identifiers);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 is.toString().getBytes()));
         p.setRequestEntity(request);
         try {
             String resp = executeMethodOk(p);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             JSONArray idents = parsedResp.getJSONArray("identifiers");
             if(idents != null && !idents.isEmpty()) {
                 for(int i=0; i<idents.size(); i++) {
                     JSONObject ident = idents.getJSONObject(i);
                     result.put(ident.getString("identifier"), ident.getString("uri"));
                 }
 
             }
 
         } catch (HttpMethodException ex) {
             l.debug("Failed executing identifierToUri identifier="+identifiers);
             throw new GdcRestApiException("Failed executing identifierToUri identifier="+identifiers,ex);
         } finally {
             p.releaseConnection();
         }
         return result;
     }
 
 
     /**
      * Creates a new identifiers structure
      * @param identifiers MD object identifier
      * @return the new identifiers structure
      */
     private JSONObject getIdentifiersStructure(String[] identifiers) {
         JSONObject identifierToUri = new JSONObject();
         JSONArray ids = new JSONArray();
         for(int i=0; i< identifiers.length; i++) {
             ids.add(identifiers[i]);
         }
         identifierToUri.put("identifierToUri",ids);
         return identifierToUri;
     }
 
 
     /**
      * Retrieves a metadata object definition by Uri
      * @param objectUri object uri
      * @return the object to get
      */
     protected JSONObject getObjectByUri(String objectUri) {
         l.debug("Executing getObjectByUri uri="+objectUri);
         HttpMethod req = createGetMethod(getServerUrl() + objectUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if(parsedResp.isNullObject()) {
                 l.debug("Can't getObjectByUri object uri="+objectUri);
                 throw new GdcRestApiException("Can't getObjectByUri object uri="+objectUri);
             }
             return parsedResp;
         }
         finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Retrieves a metadata object definition
      * @param objectUri object uri
      * @return the object to get
      */
     public MetadataObject getMetadataObject(String objectUri) {
         l.debug("Executing getMetadataObject uri="+objectUri);
         MetadataObject o = new MetadataObject(getObjectByUri(objectUri));
         String tp = o.getType();
         if(tp.equalsIgnoreCase("report")) {
             try {
                 String rdf = getReportDefinition(objectUri);
                 JSONObject c = o.getContent();
                 c.put("defaultReportDefinition",rdf);
             }
             catch (GdcProjectAccessException e) {
                 l.debug("Can't extract the default report definition.");
             }
         }
         return o;
     }
     
     /**
      * Retrieves a metadata object definition
      * @param projectId project id (hash)
      * @param objectId object id (integer)
      * @return the object to get
      */
     public MetadataObject getMetadataObject(String projectId, int objectId) {
         l.debug("Executing getMetadataObject id="+objectId+" on project id="+projectId);
         return getMetadataObject(MD_URI + projectId + OBJ_URI + "/" + objectId);
     }
 
     /**
      * Retrieves a metadata object definition
      * @param projectId project id (hash)
      * @param identifier object identifier
      * @return the object to get
      */
     public MetadataObject getMetadataObject(String projectId, String identifier) {
         l.debug("Executing getObjectByIdentifier identifier="+identifier);
         Map<String,String> uris = identifierToUri(projectId, new String[] {identifier});
         if(uris != null && uris.size()>0) {
             String uri = uris.get(identifier);
             if(uri != null && uri.length()>0)
                 return getMetadataObject(uri);
             else {
                 l.debug("Can't getObjectByIdentifier identifier="+identifier+" The identifier doesn't exists.");
                 throw new GdcRestApiException("Can't getObjectByIdentifier identifier="+identifier+" The identifier doesn't exists.");
             }
         }
         else {
             l.debug("Can't getObjectByIdentifier identifier="+identifier+" The identifier doesn't exists.");
             throw new GdcRestApiException("Can't getObjectByIdentifier identifier="+identifier+" The identifier doesn't exists.");
         }
     }
 
     /**
      * Returns the JSON list of all project's prompt responses
      * @param projectId project ID
      * @return the JSON object with all variables
      */
     public JSONObject getProjectVariables(String projectId) {
         l.debug("Executing getProjectVariables on project id="+projectId);
         PostMethod p = createPostMethod(getProjectMdUrl(projectId) + VARIABLES_SEARCH_URI);
         JSONObject is = getVariableSearchStructure();
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 is.toString().getBytes()));
         p.setRequestEntity(request);
 
         try {
             String resp = executeMethodOk(p);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing getProjectVariables on project id="+projectId);
             throw new GdcRestApiException("Failed executing getProjectVariables on project id="+projectId, ex);
         } finally {
             p.releaseConnection();
         }
     }
 
 
     /**
      * Stores the variable ina project
      * @param projectId - project ID
      * @param variable - variable JSON structure
      * @return the newly created variable
      */
     public JSONObject createVariable(String projectId, JSONObject variable) {
         l.debug("Executing createVariable on project id="+projectId+ "variable='"+variable.toString(2)+"'");
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + VARIABLES_CREATE_URI);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 variable.toString().getBytes()));
         req.setRequestEntity(request);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing createVariable on project id="+projectId+ "content='"+variable.toString()+"'");
             throw new GdcRestApiException("Failed executing createVariable on project id="+projectId+ "content='"+variable.toString()+"'",ex);
         } finally {
             req.releaseConnection();
         }
 
     }
 
     /**
      * Stores the variable ina project
      * @param uri - variable uri
      * @param variable - variable JSON structure
      * @return the newly created variable
      */
     public JSONObject modifyVariable(String uri, JSONObject variable) {
         l.debug("Executing modifyVariable uri="+uri+ "variable='"+variable.toString(2)+"'");
         PostMethod req = createPostMethod(getServerUrl() + uri);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 variable.toString().getBytes()));
         req.setRequestEntity(request);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing modifyVariable uri="+uri+ "content='"+variable.toString()+"'");
             throw new GdcRestApiException("Failed executing modifyVariable uri="+uri+ "content='"+variable.toString()+"'",ex);
         } finally {
             req.releaseConnection();
         }
 
     }
 
     /**
      * Returns the default variable search structure
      * @return the default variable search structure
      */
     private JSONObject getVariableSearchStructure() {
         JSONObject srch = new JSONObject();
         JSONObject variableSearch = new JSONObject();
         variableSearch.put("variables", new JSONArray());
         variableSearch.put("context", new JSONArray());
         srch.put("variablesSearch", variableSearch);
         return srch;
     }
 
     /**
      * List the metadata server objects by their type
      * @param projectId project ID
      * @param objectType object type
      * @return the list of object URIs
      */
     public List<String> listMetadataObjects(String projectId, String objectType) {
         ArrayList<String> ret = new ArrayList<String>();
         l.debug("Executing listMetadataObjects type="+objectType+" in project id="+projectId);
         HttpMethod req = createGetMethod(getProjectMdUrl(projectId) + QUERY_URI+objectType);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if(parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't listMetadataObjects type="+objectType+" in project id="+projectId+". Invalid response.");
                 throw new GdcRestApiException("Can't listMetadataObjects type="+objectType+" in project id="+projectId+". Invalid response.");
             }
             JSONObject query = parsedResp.getJSONObject("query");
             if(query == null || query.isNullObject() || query.isEmpty()) {
                 l.debug("Can't listMetadataObjects type="+objectType+" in project id="+projectId+". No query key in the response.");
                 throw new GdcRestApiException("Can't listMetadataObjects type="+objectType+" in project id="+projectId+". No query key in the response.");
             }
             JSONArray entries = query.getJSONArray("entries");
             if(entries == null) {
                 l.debug("Can't listMetadataObjects type="+objectType+" in project id="+projectId+". No entries key in the response.");
                 throw new GdcRestApiException("Can't listMetadataObjects type="+objectType+" in project id="+projectId+". No entries key in the response.");
             }
             for(Object o : entries) {
                 JSONObject obj = (JSONObject)o;
                 ret.add(obj.getString("link"));
             }
             return ret;
         }
         finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Returns the dependent objects
      * @param uri the uri of the top-level object
      * @return list of dependent objects
      */
     public List<JSONObject> using(String uri) {
         l.debug("Executing using uri="+uri);
         List<JSONObject> ret = new ArrayList<JSONObject>();
         //HACK!
         String usedUri = uri.replace("/obj/","/using/");
         HttpMethod req = createGetMethod(getServerUrl() + usedUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if(parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't call using on uri="+uri+". Invalid response.");
                 throw new GdcRestApiException("Can't call using on uri="+uri+". Invalid response.");
             }
             JSONObject using = parsedResp.getJSONObject("using");
             if(using == null || using.isNullObject() || using.isEmpty()) {
                 l.debug("Can't call using on uri="+uri+". No using data.");
                 throw new GdcRestApiException("Can't call using on uri="+uri+". No using data.");
             }
             JSONArray nodes = using.getJSONArray("nodes");
             if(nodes == null) {
                 l.debug("Can't call using on uri="+uri+". No nodes key in the response.");
                 throw new GdcRestApiException("Can't call using on uri="+uri+". No nodes key in the response.");
             }
             for(Object o : nodes) {
                 JSONObject obj = (JSONObject)o;
                 ret.add(obj);
             }
             return ret;
         }
         finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Returns the dependent objects
      * @param uri the uri of the top-level object
      * @return list of dependent objects
      */
     public List<JSONObject> usedBy(String uri) {
         l.debug("Executing usedby uri="+uri);
         List<JSONObject> ret = new ArrayList<JSONObject>();
         //HACK!
         String usedUri = uri.replace("/obj/","/usedby/");
         HttpMethod req = createGetMethod(getServerUrl() + usedUri);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             if(parsedResp == null || parsedResp.isNullObject() || parsedResp.isEmpty()) {
                 l.debug("Can't call usedby on uri="+uri+". Invalid response.");
                 throw new GdcRestApiException("Can't call usedby on uri="+uri+". Invalid response.");
             }
             JSONObject usedby = parsedResp.getJSONObject("usedby");
             if(usedby == null || usedby.isNullObject() || usedby.isEmpty()) {
                 l.debug("Can't call usedby on uri="+uri+". No usedby data.");
                 throw new GdcRestApiException("Can't call usedby on uri="+uri+". No usedby data.");
             }
             JSONArray nodes = usedby.getJSONArray("nodes");
             if(nodes == null) {
                 l.debug("Can't call usedby on uri="+uri+". No nodes key in the response.");
                 throw new GdcRestApiException("Can't call usedby on uri="+uri+". No nodes key in the response.");
             }
             for(Object o : nodes) {
                 JSONObject obj = (JSONObject)o;
                 ret.add(obj);
             }
             return ret;
         }
         finally {
             req.releaseConnection();
         }
     }
 
     private HashSet ignoredTypes = new HashSet<String>(Arrays.asList(new String[] {"column","table","tableDataLoad","dimension","reportResult","reportResult2","dataLoadingColumn"}));
 
     /**
      * Exports a MD object to disk with all dependencies
      * @param uris the object uris
      * @param dir the target directory
      */
     public void exportMetadataObjectWithDependencies(String[] uris, String dir) throws IOException {
         l.debug("Executing exportMetadataObjectWithDependencies uris="+uris+" dir="+dir);
         Map<String,MetadataObject> m = getMetadataObjectWithDependencies(uris);
         for(String identifier : m.keySet()) {
             MetadataObject o = m.get(identifier);
             String t = o.getType();
             String uri = o.getUri();
             String id = uri.substring(uri.lastIndexOf("/")+1);
             String name = t+"."+identifier+"."+id+".gmd";
             name = name.replace("..",".");
             FileUtil.writeJSONToFile(o, dir+"/"+name);
         }
         if(uris.length > 0) {
             String pid = getProjectIdFromObjectUri(uris[0]);
             JSONObject o = getProjectVariables(pid);
             String name = "variables."+pid+".gvr";
             FileUtil.writeJSONToFile(o, dir+"/"+name);
         }
     }
 
     /**
      * Gets a MD object to and all its dependencies
      * @param uris the object uris
      * @return map of <identifier, object>
      */
     public Map<String,MetadataObject> getMetadataObjectWithDependencies(String[] uris) throws IOException {
         l.debug("Executing getMetadataObjectWithDependencies uris="+uris);
         Map<String,MetadataObject> m = new HashMap<String,MetadataObject>();
         Set<String> exported = new HashSet();
         for(String uri : uris) {
             l.debug("Exporting dependencies of uri="+uri);
             MetadataObject o = getMetadataObject(uri);
             o.stripKeysForRead();
             String identifier = o.getIdentifier();
             m.put(identifier, o);
             List<JSONObject> dependencies = using(uri);
             l.debug("There are "+dependencies.size() +" using dependencies.");
             for(JSONObject d : dependencies) {
                 String tp = d.getString("category");
                 if(!ignoredTypes.contains(tp)) {
                     String link = d.getString("link");
                     l.debug("Exporting uri="+link);
                     if(!exported.contains(link)) {
                         MetadataObject od = getMetadataObject(link);
                         String dif = od.getIdentifier();
                         od.stripKeysForRead();
                         m.put(dif, od);
                         exported.add(link);
                         l.debug("Exported uri="+link);
                     }
                     else {
                         l.debug("Cache hit for uri="+link);
                     }
                 }
             }
             dependencies = usedBy(uri);
             l.debug("There are "+dependencies.size() +" usedBy dependencies.");
             for(JSONObject d : dependencies) {
                 String tp = d.getString("category");
                 if(!ignoredTypes.contains(tp)) {
                     String link = d.getString("link");
                     l.debug("Exporting uri="+link);
                     if(!exported.contains(link)) {
                         MetadataObject od = getMetadataObject(link);
                         String dif = od.getIdentifier();
                         od.stripKeysForRead();
                         m.put(dif, od);
                         exported.add(link);
                         l.debug("Exported uri="+link);
                     }
                     else {
                         l.debug("Cache hit for uri="+link);
                     }
                 }
             }
             
 
         }
         return m;
     }
 
     /**
      * Stores all MD objects to dir
      * @param pid project ID
      * @param dir directory
      * @throws IOException
      */
     public void storeMetadataObjects(String pid, String dir) throws IOException {
         List<String> uris = new ArrayList<String>();
         List<String> links = listMetadataObjects(pid, OBJ_TYPE_DASHBOARD);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_DOMAIN);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_REPORT);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_METRIC);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_VARIABLE);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_DATASET);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_FOLDER);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_ATTRIBUTE);
         for(String link : links) {
             uris.add(link);
         }
         links = listMetadataObjects(pid, OBJ_TYPE_FACT);
         for(String link : links) {
             uris.add(link);
         }
         exportMetadataObjectWithDependencies(uris.toArray(new String[] {}), dir);
     }
 
     /**
      * Loads indexes for the metadata object copy/refresh
      * @param dir
      * @param identifiers
      * @param ids
      * @throws IOException
      */
     protected void loadMdIndexes(String dir, Map identifiers, Map ids) throws IOException {
         File d = new File(dir);
         FileFilter fileFilter = new FileFilter() {
             public boolean accept(File file) {
                 return file.getName().endsWith(".gmd");
             }
         };
         File[] mdObjects = d.listFiles(fileFilter);
         for(File of : mdObjects) {
             MetadataObject o = new MetadataObject(FileUtil.readJSONFromFile(of.getAbsolutePath()));
             String identifier = o.getIdentifier();
             String id = o.getUri();
             identifiers.put(identifier, o);
             ids.put(id, o);
         }
     }
 
     /**
      * Loads indexes for the metadata object copy/refresh
      * @param dir
      * @return List of variables
      * @throws IOException
      */
     protected List<JSONObject> loadVariables(String dir) throws IOException {
         List<JSONObject> vars = new ArrayList<JSONObject>();
         File d = new File(dir);
         FileFilter fileFilter = new FileFilter() {
             public boolean accept(File file) {
                 return file.getName().endsWith(".gvr");
             }
         };
         File[] mdObjects = d.listFiles(fileFilter);
         for(File of : mdObjects) {
             JSONObject variables = JSONObject.fromObject(FileUtil.readJSONFromFile(of.getAbsolutePath()));
             vars.addAll(parseVariables(variables));
         }
         return vars;
     }
 
     private List<JSONObject> parseVariables(JSONObject o) throws IOException {
         List<JSONObject> vars = new ArrayList<JSONObject>();
         JSONArray variables = o.getJSONArray("variables");
         Iterator i = variables.iterator();
         while(i.hasNext()) {
             vars.add((JSONObject)i.next());
         }
         return vars;
     }
 
 
     /**
      * Stores all metadata objects from a specified directory and adjusts the IDs by identifiers
      * Always creates new objects
      * @param srcDir source objects input directory
      * @param dstDir destination objects input directory
      * @throws IOException
      */
     public void copyMetadataObjects(final String pid, String srcDir, String dstDir, final boolean overwriteExisting) throws IOException {
         final Map<String,MetadataObject> storedObjectsByIdentifier = new HashMap<String,MetadataObject>();
         final Map<String,MetadataObject> storedObjectsById = new HashMap<String,MetadataObject>();
         final Map<String,MetadataObject> sourceObjectsByIdentifier = new HashMap<String,MetadataObject>();
         final Map<String,MetadataObject> sourceObjectsById = new HashMap<String,MetadataObject>();
         final Map<String,MetadataObject> processedObjectsByIdentifier = (overwriteExisting)?(new HashMap<String,MetadataObject>()):(storedObjectsByIdentifier);
 
         loadMdIndexes(srcDir, sourceObjectsByIdentifier, sourceObjectsById);
         loadMdIndexes(dstDir, storedObjectsByIdentifier, storedObjectsById);
 
         class Store {
             public String storeObjectWithDependencies(MetadataObject o) {
                 l.debug("Executing storeObjectWithDependencies "+o.toString());
                 MetadataObject r = null;
                 String identifier = o.getIdentifier();
                 String tp = o.getType();
                 if(!processedObjectsByIdentifier.containsKey(identifier)) {
                     l.debug("Storing object identifier="+identifier+" type="+tp);
                     if(!tp.equalsIgnoreCase("attributeDisplayForm") && !tp.equalsIgnoreCase("attribute") &&
                             !tp.equalsIgnoreCase("fact") && !tp.equalsIgnoreCase("dataSet")) {
                         List<String> ids = o.getDependentObjectUris();
                         String content = o.toString();
                         for(String id : ids) {
                             l.debug("storeObjectWithDependencies resolving dependent ID id="+id);
                             MetadataObject src = sourceObjectsById.get(id);
                             l.debug("storeObjectWithDependencies found id="+id+" in source objects src="+src.toString());
                             if(src != null) {
                                 String srcUri = id;
                                 String newUri = storeObjectWithDependencies(src);
                                 l.debug("storeObjectWithDependencies replacing srcUri="+"\""+srcUri+"\""+" with newUri="+"\""+newUri+"\"");
                                 content = content.replace("\""+srcUri+"\"", "\""+newUri+"\"");
                                 l.debug("storeObjectWithDependencies replacing srcUri="+"["+srcUri+"]"+" with newUri="+"["+newUri+"]");
                                 content = content.replace("["+srcUri+"]", "["+newUri+"]");
                             }
                             else {
                                 l.info("Can't find object uri="+id+" in the source!");
                             }
                         }
                         MetadataObject newObject = new MetadataObject(JSONObject.fromObject(content));
                         newObject.stripKeysForCreate();
                         if(storedObjectsByIdentifier.containsKey(identifier)) {
                             JSONObject m = newObject.getMeta();
                             MetadataObject eob = storedObjectsByIdentifier.get(identifier);
                             String uri = eob.getUri();
                             m.put("uri", uri);
                             if(overwriteExisting) {
                                 modifyMetadataObject(uri, newObject);
                             }
                             r = newObject;
                         }
                         else {
                             r = new MetadataObject(createMetadataObject(pid, newObject));
                             String uri = r.getUri();
                             JSONObject m = r.getMeta();
                             m.put("identifier", identifier);
                             modifyMetadataObject(uri, r);
                         }
                         storedObjectsByIdentifier.put(identifier,r);
                         processedObjectsByIdentifier.put(identifier,r);
                         String id = r.getUri();
                         storedObjectsById.put(id,r);
                     }
                     else {
                         r = storedObjectsByIdentifier.get(identifier);
                         if(r == null) {
                             l.info("Missing LDM object in the target model identifier="+identifier+" type="+tp);
                             l.debug("Missing LDM object in the target model identifier="+identifier+" type="+tp);
                             throw new GdcRestApiException("Missing LDM object in the target model identifier="+identifier+
                                     " type="+tp);
                         }
                         if(overwriteExisting) {
                             JSONObject srcMeta = o.getMeta();
                             JSONObject newMeta = r.getMeta();
                             String[] copyTags = {"title","summary","tags"};
                             for(String tag : copyTags) {
                                 newMeta.put(tag,srcMeta.getString(tag));
                             }
                             if(tp.equalsIgnoreCase("attribute")) {
                                 JSONObject srcContent = o.getContent();
                                 JSONObject newContent = r.getContent();
                                 if(srcContent.containsKey("drillDownStepAttributeDF")) {
                                     String duri = srcContent.getString("drillDownStepAttributeDF");
                                     if(duri != null && duri.length() >0) {
                                         MetadataObject drillDownDF = sourceObjectsById.get(duri);
                                         if(drillDownDF != null) {
                                             String drillDownDFIdentifier = drillDownDF.getIdentifier();
                                             MetadataObject newDrillDownDF = storedObjectsByIdentifier.get(drillDownDFIdentifier);
                                             if(newDrillDownDF != null) {
                                                 newContent.put("drillDownStepAttributeDF",newDrillDownDF.getUri());
                                             }
                                             else {
                                                 l.info("The destination project doesn't contain the label with identifier "+
                                                         drillDownDFIdentifier + " that is used in the drill down for attribute with identifier "+
                                                         identifier);
                                             }
                                         }
                                         else {
                                             l.info("The source project doesn't contain the drill down label with used in " +
                                                     "the attribute with identifier "+ identifier);
                                         }
                                     }
                                 }
                             }
                             modifyMetadataObject(r.getUri(), r);
                             storedObjectsByIdentifier.put(identifier,r);
                             processedObjectsByIdentifier.put(identifier,r);
                         }
                     }
                 }
                 else {
                     r = storedObjectsByIdentifier.get(identifier);
                 }
                 String ruri = r.getUri();
                 l.debug("Executed storeObjectWithDependencies uri="+ruri);
                 return r.getUri();
             }
 
             private String oldUriToNewUri(String oldUri) {
                 MetadataObject oldObj = sourceObjectsById.get(oldUri);
                 if(oldObj != null) {
                     String identifier = oldObj.getIdentifier();
                     if(identifier != null && identifier.length()>0) {
                         MetadataObject newObj = storedObjectsByIdentifier.get(identifier);
                         if(newObj != null) {
                             String newUri = newObj.getUri();
                             if(newUri != null && newUri.length() >0) {
                                 return newUri;
                             }
                             else {
                                 l.debug("The object with identifier="+identifier+" doesn't have any uri.");
                                 throw new GdcRestApiException("The object with identifier="+identifier+" doesn't have any uri.");
                             }
                         }
                          else {
                             l.debug("Can't find the object with identifier="+identifier+" in the project metadata.");
                             throw new GdcRestApiException("Can't find the object with identifier="+identifier+" in the project metadata.");
                         }
                     }
                     else {
                         l.debug("The object with uri="+oldUri+" doesn't have any identifier.");
                         throw new GdcRestApiException("The object with uri="+oldUri+" doesn't have any identifier.");
                     }
                 }
                 else {
                     l.debug("Can't find the object with uri="+oldUri+" in the source metadata.");
                     throw new GdcRestApiException("Can't find the object with uri="+oldUri+" in the source metadata.");
                 }
             }
 
             /**
              * Extracts the dependent objects uris from the content
              * @return list of depenedent object uris
              */
             public List<String> getVariableDependentObjectUris(JSONObject variable) {
                 List<String> uris = new ArrayList<String>();
                 String uri = variable.getString("uri");
                 String content = variable.toString();
                 Pattern p = Pattern.compile("\\\"/gdc/md/[^/]*?/obj/[0-9]+?\\\"");
                 Matcher m = p.matcher(content);
                 while(m.find()) {
                     String u = m.group();
                     u = u.replace("\"","");
                     if(!u.equalsIgnoreCase(uri) && !uris.contains(u))
                         uris.add(u);
                 }
                 return uris;
             }
 
             public void storeVariables(List<JSONObject> vars, List<JSONObject> oldVars) {
 
                 HashMap<String, JSONObject> oldVariablesByPromptIdentifier = new HashMap<String, JSONObject>();
                 for(JSONObject v : oldVars) {
                     String oldPromptUri = v.getString("prompt");
                     if(oldPromptUri != null && oldPromptUri.length()>0) {
                         MetadataObject oldPrompt = storedObjectsById.get(oldPromptUri);
                         if(oldPrompt != null && !oldPrompt.isEmpty() && !oldPrompt.isNullObject()) {
                             String oldIdentifier = oldPrompt.getIdentifier();
                             if(oldIdentifier != null && oldIdentifier.length()>0) {
                                 oldVariablesByPromptIdentifier.put(oldIdentifier, v);
                             }
                             else {
                                 l.debug("Source prompt with no identifier:"+oldPrompt.toString(2));
                                 throw new GdcRestApiException("Source prompt with no identifier:"+oldPrompt.toString(2));
                             }
                         }
                         else {
                             l.debug("No source prompt for source variable:"+v.toString(2));
                             throw new GdcRestApiException("No source prompt for source variable:"+v.toString(2));
                         }
                     }
                     else {
                         l.debug("Source project variable with no prompt specification:"+v.toString(2));
                         throw new GdcRestApiException("Source project variable with no prompt specification:"+v.toString(2));
                     }
                 }
                 for(JSONObject v : vars) {
                     String newPromptUri = v.getString("prompt");
 
                     if(newPromptUri != null && newPromptUri.length()>0) {
                         MetadataObject newPrompt = sourceObjectsById.get(newPromptUri);
                         if(newPrompt != null && !newPrompt.isEmpty() && !newPrompt.isNullObject()) {
                             String newIdentifier = newPrompt.getIdentifier();
                             if(newIdentifier != null && newIdentifier.length()>0) {
                                 List<String> ids = getVariableDependentObjectUris(v);
                                 v.discard("uri");
                                 v.discard("related");
                                 String content = v.toString();
                                 for(String id : ids) {
                                     MetadataObject src = sourceObjectsById.get(id);
                                     if(src != null) {
                                         String newUri = oldUriToNewUri(id);
                                        //content = content.replace(id, newUri);
                                        content = content.replace("\""+id+"\"", "\""+newUri+"\"");
                                        content = content.replace("["+id+"]", "["+newUri+"]");

                                     }
                                     else {
                                         l.info("Can't find object uri="+id+" in the source!");
                                     }
                                 }
                                 JSONObject variableContent = JSONObject.fromObject(content);
                                 variableContent.put("related","/gdc/projects/"+pid);
                                 JSONObject variable = new JSONObject();
                                 variable.put("variable",variableContent);
 
                                 if(oldVariablesByPromptIdentifier.containsKey(newIdentifier)) {
                                     if(overwriteExisting) {
                                         JSONObject oldVariable = oldVariablesByPromptIdentifier.get(newIdentifier);
                                         String uri = oldVariable.getString("uri");
                                         if(uri != null && uri.length()>0) {
                                             modifyVariable(uri, variable);
                                         }
                                         else {
                                             l.debug("Source project variable with no uri:"+v.toString(2));
                                             throw new GdcRestApiException("Source project variable with no uri:"+v.toString(2));   
                                         }
                                     }
                                 }
                                 else {
                                     createVariable(pid, variable);
                                 }
 
                             }
                             else {
                                 l.debug("Destination prompt with no identifier:"+newPrompt.toString(2));
                                 throw new GdcRestApiException("Destination prompt with no identifier:"+newPrompt.toString(2));
                             }
                         }
                         else {
                             l.debug("No destination prompt for source variable:"+v.toString(2));
                             throw new GdcRestApiException("No destination prompt for source variable:"+v.toString(2));
                         }
                     }
                     else {
                         l.debug("Destination project variable with no prompt specification:"+v.toString(2));
                         throw new GdcRestApiException("Destination project variable with no prompt specification:"+v.toString(2));
                     }
 
                 }
             }
 
         }
 
         Store storage = new Store();
         for(MetadataObject obj : sourceObjectsByIdentifier.values()) {
             storage.storeObjectWithDependencies(obj);
         }
 
         List<JSONObject> newVariables = loadVariables(srcDir);
         List<JSONObject> oldVariables = parseVariables(getProjectVariables(pid));
         if(newVariables != null && newVariables.size()>0) {
             storage.storeVariables(newVariables, oldVariables);
         }
     }
 
     /**
      * Creates a new object in the metadata server
      * @param projectId project id (hash)
      * @param content the new object content
      * @return the new object
      */
     public JSONObject createMetadataObject(String projectId, JSON content) {
         l.debug("Executing createMetadataObject on project id="+projectId+ "content='"+content.toString()+"'");
         PostMethod req = createPostMethod(getProjectMdUrl(projectId) + OBJ_URI + "?createAndGet=true");
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 content.toString().getBytes()));
         req.setRequestEntity(request);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing createMetadataObject on project id="+projectId+ "content='"+content.toString()+"'");
             throw new GdcRestApiException("Failed executing createMetadataObject on project id="+projectId+ "content='"+content.toString()+"'",ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Modifies an object in the metadata server
      * @param projectId project id (hash)
      * @param objectId object id (integer)
      * @param content the new object content
      * @return the new object
      */
     public JSONObject modifyMetadataObject(String projectId, int objectId, JSON content) {
         l.debug("Executing modifyMetadataObject on project id="+projectId+" objectId="+objectId+" content='"+content.toString()+"'");
         return modifyMetadataObject(getProjectMdUrl(projectId) + OBJ_URI + "/" + objectId, content);
     }
 
     /**
      * Modifies an object in the metadata server
      * @param uri object uri
      * @param content the new object content
      * @return the new object
      */
     public JSONObject modifyMetadataObject(String uri, JSON content) {
         l.debug("Executing modifyMetadataObject on uri="+uri+" content='"+content.toString()+"'");
         PostMethod req = createPostMethod(getServerUrl() + uri);
         InputStreamRequestEntity request = new InputStreamRequestEntity(new ByteArrayInputStream(
                 content.toString().getBytes()));
         req.setRequestEntity(request);
         try {
             String resp = executeMethodOk(req);
             JSONObject parsedResp = JSONObject.fromObject(resp);
             return parsedResp;
         } catch (HttpMethodException ex) {
             l.debug("Failed executing modifyMetadataObject on uri="+uri+" content='"+content.toString()+"'");
             throw new GdcRestApiException("Failed executing modifyMetadataObject on uri="+uri+" content='"+content.toString()+"'",ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     /**
      * Deletes an object in the metadata server
      * @param projectId project id (hash)
      * @param objectId object id (integer)
      * @return the new object
      */
     public void deleteMetadataObject(String projectId, int objectId) {
         l.debug("Executing deleteMetadataObject on project id="+projectId+" objectId="+objectId);
         deleteMetadataObject(getProjectMdUrl(projectId) + OBJ_URI + "/" + objectId);
     }
 
 
 
     /**
      * Deletes an object in the metadata server
      * @param uri object uri
      * @return the new object
      */
     public void deleteMetadataObject(String uri) {
         l.debug("Executing deleteMetadataObject on project uri="+uri);
         DeleteMethod req = createDeleteMethod(getServerUrl() + uri);
         try {
             String resp = executeMethodOk(req);
         } catch (HttpMethodException ex) {
             l.debug("Failed executing deleteMetadataObject on project uri="+uri);
             throw new GdcRestApiException("Failed executing deleteMetadataObject on uri="+uri,ex);
         } finally {
             req.releaseConnection();
         }
     }
 
     private static GetMethod createGetMethod(String path) {
         return configureHttpMethod(new GetMethod(path));
     }
 
     private static PostMethod createPostMethod(String path) {
         return configureHttpMethod(new PostMethod(path));
     }
 
     private static DeleteMethod createDeleteMethod(String path) {
         return configureHttpMethod(new DeleteMethod(path));
     }
 
     private static <T extends HttpMethod> T configureHttpMethod(T request) {
         request.setRequestHeader("Content-Type", "application/json");
         request.setRequestHeader("Accept", "application/json");
         request.setRequestHeader("User-Agent", "GoodData CL/1.2.5-SNAPSHOT");
         return request;
     }
 
 
 
 
     public static Logger getL() {
         return l;
     }
 }
