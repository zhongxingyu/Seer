 package edu.mayo.phenoportal.server.phenotype;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import edu.mayo.phenoportal.server.upload.ImportServlet;
 import edu.mayo.phenoportal.shared.MatImport;
 import mayo.edu.cts2.editor.server.Cts2EditorServiceProperties;
 import org.apache.commons.io.FileUtils;
 import org.jboss.resteasy.util.Base64;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import edu.mayo.phenoportal.client.Htp;
 import edu.mayo.phenoportal.client.core.AlgorithmData;
 import edu.mayo.phenoportal.client.phenotype.PhenotypeService;
 import edu.mayo.phenoportal.server.database.DBConnection;
 import edu.mayo.phenoportal.server.utils.DOMXmlParser;
 import edu.mayo.phenoportal.server.utils.DateConverter;
 import edu.mayo.phenoportal.server.utils.SmtpClient;
 import edu.mayo.phenoportal.shared.Demographic;
 import edu.mayo.phenoportal.shared.Drools;
 import edu.mayo.phenoportal.shared.Execution;
 import edu.mayo.phenoportal.shared.Image;
 import edu.mayo.phenoportal.shared.News;
 import edu.mayo.phenoportal.shared.SharpNews;
 import edu.mayo.phenoportal.shared.User;
 import edu.mayo.phenoportal.shared.UserRoleRequest;
 import edu.mayo.phenoportal.shared.database.CategoryColumns;
 import edu.mayo.phenoportal.shared.database.DroolsColumns;
 import edu.mayo.phenoportal.shared.database.ExecutionColumns;
 import edu.mayo.phenoportal.shared.database.NewsColumns;
 import edu.mayo.phenoportal.shared.database.SharpNewsColumns;
 import edu.mayo.phenoportal.shared.database.UploadColumns;
 import edu.mayo.phenoportal.shared.database.UserColumns;
 import edu.mayo.phenoportal.shared.database.UserRoleRequestColumns;
 import edu.mayo.phenoportal.utils.SQLStatements;
 import edu.mayo.phenotype.server.BasePhenoportalServlet;
 
 public class PhenotypeServiceImpl extends BasePhenoportalServlet implements PhenotypeService {
 
     private static Logger s_logger = Logger.getLogger(PhenotypeServiceImpl.class.getName());
     private static final long serialVersionUID = 1L;
     private static final int BASE_VAL = 10000;
     private static final String ERROR_HTML = "<b>Could not retrieve the criteria information.</b>";
 
     // XML Settings
     public static final String ROOT = "List";
 
     @Override
     /*
      * Make database connection and queries Mysql to generate the PhenotypeTree
      * XML.
      */
     public String getPhenotypeCategories(String categoryId) throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         CategoryXmlGenerator generator = new CategoryXmlGenerator();
 
         // Create the root element
         generator.createDocumentAndRootElement(ROOT);
 
         // if categoryId is null, default to "0"
         categoryId = categoryId == null ? "0" : categoryId;
 
         // Create the Categories Xml
         getCategoriesFromDB(generator, categoryId);
 
         // Create the Algorithms Xml
         getAlgorithmsFromDB(generator, categoryId);
 
         // Convert the Xml to String
         String xml = generator.xmlToString();
 
         return xml;
     }
 
     // Get the categories and create the Xml
     private void getCategoriesFromDB(CategoryXmlGenerator generator, String categoryId) {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
 
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
 
                 st = conn.prepareStatement(SQLStatements.selectCategoriesStatement(categoryId));
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     int id = rs.getInt(CategoryColumns.ID.getColNum());
                     String name = rs.getString(CategoryColumns.NAME.getColNum());
                     int parentId = rs.getInt(CategoryColumns.PARENTID.getColNum());
                     int count = rs.getInt(CategoryColumns.COUNT.getColNum());
                     int level = rs.getInt(CategoryColumns.LEVEL.getColNum());
 
                     generator.createPhenotypeCategoriesDOMTree(id, name, parentId, count, level);
 
                 }
 
             } catch (Exception ex) {
                 s_logger.log(Level.SEVERE,
                         "Failed fetching categories from DB" + ex.getStackTrace());
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
 
         }
     }
 
     // Create Phenotype Algorithms
     private void getAlgorithmsFromDB(CategoryXmlGenerator generator, String categoryId)
             throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
                 st = conn.prepareStatement(SQLStatements.selectAlgorithmsStatement(categoryId));
 
                 rs = st.executeQuery();
 
                 int categoryIdInt = convertToInt(categoryId) * BASE_VAL;
                 int increment = 0;
                 int algorithmId;
 
                 while (rs.next()) {
 
                     // create a unique id for the algorithm. This is needed for
                     // the UI tree.
                     algorithmId = rs.getInt(1);
 
                     String parentId = categoryId;
 
                     int count = 0; // count is ignored for algorithms
                     int level = 4; // algorithms are always level 4
                     String algorithmName = rs.getString(2);
                     String algorithmDesc = rs.getString(3);
                     String algorithmVersion = rs.getString(4);
                     String algorithmUser = rs.getString(5);
 
                     generator.createPhenotypeAlgorithmsDOMTree(algorithmId, categoryId + algorithmId,
                             parentId, count, level, algorithmName, algorithmDesc, algorithmUser,
                             algorithmVersion);
                 }
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed fetching algorithms from DB" + ex.getMessage(),
                         ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
     }
 
     @Override
     public String getPopulationCriteria(AlgorithmData algorithmData) {
         /* TODO: check for cached version */
         String html = getHtml(algorithmData);
         StringBuilder sb = new StringBuilder();
 
         if (!html.equals(ERROR_HTML)) {
             String startTitle = "<title>";
             String endTitle = "</title>";
             String title = getHtmlSnippet(html, startTitle, endTitle);
 
             String startMatch = "<b>Initial Patient Population";
             String endMatch = "</div>";
             sb = new StringBuilder(getHtmlSnippet(html, startMatch, endMatch));
             sb.insert(
                     0,
                     "<h3>"
                             + title.substring(startTitle.length(),
                                     title.length() - endTitle.length()) + "</h3><ul><li>");
         } else {
             sb.append(html);
         }
         /* TODO: cache result */
 
         return sb.toString();
     }
 
 	private Object cts2ServerPropertiesLock = new Object();
 	private boolean cts2RestPropertiesSet = false;
 
     @Override
     public List<String> getDataCriteriaOids(AlgorithmData algorithmData) {
         /* TODO: check for cached version */
         String html = getHtml(algorithmData);
 
         String startMatch = "href=\"#toc\">Data criteria (QDM Data Elements)</a></h3>";
         String endMatch = "</div>";
 
         List<String> oids = getOids(getHtmlSnippet(html, startMatch, endMatch));
         /* TODO: cache result */
 
 	    synchronized (cts2ServerPropertiesLock) {
 		    if (!cts2RestPropertiesSet) {
 			    Cts2EditorServiceProperties.setValueSetDefinitionMaintenanceUrl(getCts2RestUrl());
 			    Cts2EditorServiceProperties.setValueSetDefinitionMaintenanceCredentials(getCts2RestUser(), getCts2RestPassword());
 			    Cts2EditorServiceProperties.setValueSetDefinitionMaintenanceEntitiesUrl(getCts2EntityRestUrl());
 			    Cts2EditorServiceProperties.setValueSetRestPageSize(getCts2RestPageSize());
 		    }
 	    }
 
         return oids;
     }
 
     @Override
     public List<String> getSupplementalCriteriaOids(AlgorithmData algorithmData) {
         /* TODO: check for cached version */
         String html = getHtml(algorithmData);
 
         String startMatch = "href=\"#toc\">Supplemental Data Elements</a></h3>";
         String endMatch = "</div>";
 
         List<String> oids = getOids(getHtmlSnippet(html, startMatch, endMatch));
         /* TODO: cache result */
 
 	    synchronized (cts2ServerPropertiesLock) {
 		    if (!cts2RestPropertiesSet) {
 			    Cts2EditorServiceProperties.setValueSetDefinitionMaintenanceUrl(getCts2RestUrl());
 			    Cts2EditorServiceProperties.setValueSetDefinitionMaintenanceCredentials(getCts2RestUser(), getCts2RestPassword());
 			    Cts2EditorServiceProperties.setValueSetDefinitionMaintenanceEntitiesUrl(getCts2EntityRestUrl());
 			    Cts2EditorServiceProperties.setValueSetRestPageSize(getCts2RestPageSize());
 		    }
 	    }
 
         return oids;
     }
 
     private String getHtml(AlgorithmData algorithmData) {
         String completeHtml = null;
 
         Connection conn = DBConnection.getDBConnection(getBasePath());
         PreparedStatement st;
         ResultSet rs;
         s_logger.fine("Basepath:" + getBasePath());
 
         if (conn != null) {
             try {
                 st = conn.prepareStatement(SQLStatements.selectCriteriaStatement(algorithmData.getId()));
 
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     String location = rs.getString(4);
                     String criteriaFileLocation = getPathFromStartupPropertiesFile();
 
                     s_logger.fine("criteriaFileLocation:" + criteriaFileLocation);
 
                     String fileLocation = criteriaFileLocation + '/' + location;
                     s_logger.fine("fileLocation:" + fileLocation);
 
                     completeHtml = readFile(fileLocation, ERROR_HTML);
                 }
             } catch (SQLException sqle) {
                 s_logger.log(Level.SEVERE,
                         "failed to fetch Criteria from the HTML file" + sqle.getMessage(), sqle);
             }
         }
         if (completeHtml == null)
             completeHtml = ERROR_HTML;
         return completeHtml;
     }
 
     private String getHtmlSnippet(String document, String start, String end) {
         int idxStart = document.indexOf(start);
         idxStart = idxStart == -1 ? 0 : idxStart;
 
         String snippet = document.substring(idxStart);
         int idxEnd = snippet.indexOf(end);
         idxEnd = idxEnd == -1 ? snippet.length() : idxEnd + end.length();
 
         return snippet.substring(0, idxEnd);
     }
 
     private List<String> getOids(String html) {
         List<String> oids = new ArrayList<String>();
         Pattern pattern = Pattern.compile("\\((\\d+(\\.(?=\\d+))?)+\\)");
         Matcher matcher = pattern.matcher(html);
         while (matcher.find()) {
             String oid = matcher.group(0);
             oid = oid.substring(1, oid.length() - 1);
            oids.add(oid);
         }
 
         return oids;
     }
 
     /**
      * Request to execute the phenotype. Will return List<Demographic> object
      * from server
      */
 
     @Override
     public Execution executePhenotype(AlgorithmData algorithmData, Date fromDate, Date toDate, String userName) throws IllegalArgumentException {
 
         String locationUrl;
         String executionStatus = "";
         Execution execution = new Execution();
         String zipPathInfo = getZipFile(algorithmData.getId());
         String zipPath = getPathFromStartupPropertiesFile() + '/' + zipPathInfo;
         File zipFile = new File(zipPath);
         String executionDateRangeFrom = DateConverter.getDateString(fromDate);
         String executionDateRangeTo = DateConverter.getDateString(toDate);
         long startExecution = System.currentTimeMillis();
 
         try {
             // execute the algorithm. This will return immediately with an id to
             // the resource that is executing.
             locationUrl = RestExecuter.getInstance(getBasePath()).createExecution(zipFile,
                     executionDateRangeFrom, executionDateRangeTo);
             execution.setUrl(locationUrl);
 
             // poll on the status until it is complete
             try {
                 while (!executionStatus.equals(RestExecuter.STATUS_COMPLETE)
                         && !executionStatus.equals(RestExecuter.STATUS_FAILED)) {
                     Thread.sleep(500);
                     executionStatus = RestExecuter.getInstance(getBasePath()).pollStatus(
                             locationUrl);
                 }
             } catch (InterruptedException ie) {
                 executionStatus = RestExecuter.STATUS_ERROR;
                 s_logger.log(Level.SEVERE, "Rest execution not complete or failed.", ie);
 
             }
 
             // continue if the status was successful
             if (executionStatus.equals(RestExecuter.STATUS_COMPLETE)) {
                 setFileName(algorithmData.getAlgorithmName(), algorithmData.getAlgorithmVersion(), algorithmData.getParentId());
                 persistExecution(execution);
             }
 
         } catch (Exception e) {
             s_logger.log(Level.SEVERE, "execution failed", e);
             executionStatus = RestExecuter.STATUS_ERROR;
         } finally {
             Connection conn = null;
             try {
                 long endExecution = System.currentTimeMillis();
                 long elapsedTime = endExecution - startExecution;
                 conn = DBConnection.getDBConnection(getBasePath());
                 conn.setAutoCommit(false);
 
                 // insertDrools(conn, executionResults, userName, "test");
 
                 Execution exeItem = new Execution();
                 exeItem.setUser(userName);
                 exeItem.setAlgorithmName(algorithmData.getAlgorithmName());
                 exeItem.setAlgorithmVersion(algorithmData.getAlgorithmVersion());
                 exeItem.setAlgorithmCategoryPath(getCategoryPath(algorithmData.getParentId()));
                 exeItem.setAlgorithmCategoryId(algorithmData.getParentId());
                 exeItem.setStartDate(DateConverter.getTimeString(new Date(startExecution)));
                 exeItem.setEndDate(DateConverter.getTimeString(new Date(endExecution)));
                 exeItem.setStatus(executionStatus);
                 exeItem.setElapsedTime(elapsedTime + " ms");
                 exeItem.setId(UUID.randomUUID().toString());
                 exeItem.setDateRangeFrom(executionDateRangeFrom);
                 exeItem.setDateRangeTo(executionDateRangeTo);
                 exeItem.setXmlPath(execution.getXmlPath());
                 exeItem.setImage(execution.getImage());
                 exeItem.setBpmnPath(execution.getBpmnPath());
                 exeItem.setRulesPath(execution.getRulesPath());
 
                 insertExecution(conn, exeItem);
             } catch (Exception e) {
                 s_logger.log(Level.SEVERE, "Failed to insert Algorithm values", e);
                 DBConnection.rollback(conn);
             } finally {
                 DBConnection.commit(conn);
                 DBConnection.close(conn);
             }
         }
 
         return execution;
     }
 
     private void persistExecution(Execution executionResults) throws Exception {
         String locationUrl = executionResults.getUrl();
         String basePath = getBasePath();
         executionResults.setId(UUID.randomUUID().toString());
 
         /* Save the execution results to the file system */
         String relativePath = executionResults.getId() + File.separator;
         String executionResultsPath = getExecutionResultsPath() + File.separator + relativePath;
 
         /* Demographics */
         String returnedXml = RestExecuter.getInstance(getBasePath()).getXml(locationUrl + "/xml");
         String demographicsFileName = "demographics.xml";
         File xmlFile = new File(executionResultsPath + demographicsFileName);
         FileUtils.writeStringToFile(xmlFile, returnedXml, "utf-8");
         List<Demographic> demographics = getDemographics(returnedXml);
         executionResults.setDemographics(demographics);
         executionResults.setXmlPath(relativePath + demographicsFileName);
 
         /* Drools workflow Image */
         String returnedImage = RestExecuter.getImage(locationUrl + "/image", basePath + "images/",
                 getFileName());
         String imageFileName = "workflow.png";
         File imageFile = new File(executionResultsPath + imageFileName);
         FileUtils.copyFile(new File(basePath + "images/" + returnedImage), imageFile);
         executionResults.setImage(getImage(relativePath + imageFileName));
 
         /* Drools workflow bpmn */
         /* TODO: get/set Drools bpmn file. */
         // String returnedBpmn = RestExecuter.getBpmn(locationUrl + "/bpmn",
         // basePath + "bpmn/",
         // getFileName());
         // String bpmnFileName = "workflow.bpmn";
         // File bpmnFile = new File(executionResultsPath + bpmnFileName);
         // FileUtils.copyFile(new File(basePath + "bpmn/" + returnedBpmn),
         // bpmnFile);
         // executionResults.setBpmnPath(relativePath + bpmnFileName);
 
         /* Drools rules */
         /* TODO: get/set Drools rules. */
     }
 
     private Image getImage(String imagePath) throws IOException {
         String basePath = getExecutionResultsPath();
         File imageFile = new File(basePath + File.separator + imagePath);
         Image image = new Image();
         image.setImagePath(imagePath);
         ImageInputStream in = ImageIO.createImageInputStream(imageFile);
         try {
             final Iterator<?> readers = ImageIO.getImageReaders(in);
             if (readers.hasNext()) {
                 ImageReader reader = (ImageReader) readers.next();
                 try {
                     reader.setInput(in);
                     image.setWidth(reader.getWidth(0));
                     image.setHeight(reader.getHeight(0));
                 } finally {
                     reader.dispose();
                 }
             }
         } finally {
             if (in != null)
                 in.close();
         }
 
         return image;
     }
 
     public String getCategoryPath(String parentId) {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         String currCategoryId = parentId;
         String categoryPath = "";
 
         while (!currCategoryId.equals("0")) { // stop when reach root
             try {
                 // get the name of the current category
                 st = conn.prepareStatement(SQLStatements
                         .selectCategoryNameStatement(currCategoryId));
                 rs = st.executeQuery();
                 while (rs.next()) {
                     categoryPath = rs.getString(1) + "/" + categoryPath;
                 }
 
                 // get the parent of the Category
                 String categoryParentId = "-1";
                 st = conn.prepareStatement(SQLStatements
                         .selectCategoryParentStatement(currCategoryId));
                 rs = st.executeQuery();
                 while (rs.next()) {
                     categoryParentId = rs.getString(1);
                 }
 
                 // take a step up the tree - the parent is now current
                 currCategoryId = categoryParentId;
             } catch (SQLException ex) {
 
                 s_logger.log(Level.INFO, "Failed to get the category path" + ex.getStackTrace(), ex);
 
             }
         }
         return categoryPath;
     }
 
     @Override
     public String getExecutions() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         ExecutionsXmlGenerator xmlGenerator = new ExecutionsXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(ExecutionsXmlGenerator.EXECUTIONS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
                 st = conn.prepareStatement(SQLStatements.selectExecutionsStatement());
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this execution
                     xmlGenerator.createExecutionXml(
                             rs.getString(ExecutionColumns.USER_NAME.colNum()),
                             rs.getString(ExecutionColumns.ALG_NAME.colNum()),
                             rs.getString(ExecutionColumns.VERSION.colNum()),
                             rs.getString(ExecutionColumns.CATEGORY.colNum()),
                             rs.getString(ExecutionColumns.START_DATE.colNum()),
                             rs.getString(ExecutionColumns.END_DATE.colNum()),
                             rs.getString(ExecutionColumns.STATUS.colNum()),
                             rs.getString(ExecutionColumns.ELAPSED_TIME.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed: getExecutions()" + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         String executionXml = xmlGenerator.xmlToString();
         return executionXml;
     }
 
     @Override
     public String getUploaders() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         UploadersXmlGenerator xmlGenerator = new UploadersXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(UploadersXmlGenerator.UPLOADERS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
                 st = conn.prepareStatement(SQLStatements.selectUploadersStatement());
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this user
                     xmlGenerator.createUploaderXml(rs.getString(UploadColumns.USER.colName()),
                             rs.getString(UploadColumns.NAME.colName()),
                             rs.getString(UploadColumns.VERSION.colName()),
                             rs.getString(UploadColumns.TYPE.colName()),
                             rs.getString(UploadColumns.PARENT_ID.colName()),
                             rs.getString(UploadColumns.UPLOAD_DATE.colName()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to get uploaders" + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         String uploaderXml = xmlGenerator.xmlToString();
         return uploaderXml;
     }
 
     @Override
     public User getUser(String userId) throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         User user = new User();
 
         if (conn != null) {
             try {
 
                 // get the user based on the user name. we will check the pw
                 // later
                 st = conn.prepareStatement(SQLStatements.selectUserStatement(userId));
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create a User object
                     user.setUserName(rs.getString(UserColumns.USER.colNum()));
                     user.setFirstName(rs.getString(UserColumns.FNAME.colNum()));
                     user.setLastName(rs.getString(UserColumns.LNAME.colNum()));
                     user.setEmail(rs.getString(UserColumns.EMAIL.colNum()));
                     user.setRole(rs.getInt(UserColumns.ROLE.colNum()));
                     user.setEnable(rs.getInt(UserColumns.ENABLE.colNum()));
                     user.setRegistrationDate(rs.getString(UserColumns.REGISTRATIONDATE.colNum()));
                     user.setCountryRegion(rs.getString(UserColumns.COUNTRY_OR_REGION.colNum()));
                     user.setAddress(rs.getString(UserColumns.STREET_ADDRESS.colNum()));
                     user.setCity(rs.getString(UserColumns.CITY.colNum()));
                     user.setState(rs.getString(UserColumns.STATE.colNum()));
                     user.setZipCode(rs.getString(UserColumns.ZIP.colNum()));
                     user.setPhoneNumber(rs.getString(UserColumns.PHONE_NUMBER.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "exceptions while fetching users" + ex.getStackTrace(),
                         ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return user;
     }
 
     @Override
     public String getUsers() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         UsersXmlGenerator xmlGenerator = new UsersXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(UsersXmlGenerator.USERS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
 
                 // get the user based on the user name. we will check the pw
                 // later
                 st = conn.prepareStatement(SQLStatements.selectUserStatement());
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this user
                     xmlGenerator.createUserXml(rs.getString(UserColumns.USER.colNum()),
                             rs.getString(UserColumns.FNAME.colNum()),
                             rs.getString(UserColumns.LNAME.colNum()),
                             rs.getString(UserColumns.EMAIL.colNum()),
                             rs.getString(UserColumns.ROLE.colNum()),
                             rs.getString(UserColumns.ENABLE.colNum()),
                             rs.getString(UserColumns.REGISTRATIONDATE.colNum()),
                             rs.getString(UserColumns.COUNTRY_OR_REGION.colNum()),
                             rs.getString(UserColumns.STREET_ADDRESS.colNum()),
                             rs.getString(UserColumns.CITY.colNum()),
                             rs.getString(UserColumns.STATE.colNum()),
                             rs.getString(UserColumns.ZIP.colNum()),
                             rs.getString(UserColumns.PHONE_NUMBER.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "exceptions while fetching users" + ex.getStackTrace(),
                         ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         String userXml = xmlGenerator.xmlToString();
         return userXml;
     }
 
     @Override
     public Boolean updateUser(User user) throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // update the user values
                 st = conn.prepareStatement(SQLStatements.updateUserStatement(user));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to update users" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public Boolean removeUser(User user) throws IllegalArgumentException {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // remove the user
                 st = conn.prepareStatement(SQLStatements.removeUserStatement(user));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to remove users" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public Boolean requestPermissionUpgrade(User user) throws IllegalArgumentException {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 long requestDate = System.currentTimeMillis();
                 String requestDateStr = DateConverter.getTimeString(new Date(requestDate));
 
                 // add request
                 st = conn.prepareStatement(SQLStatements.insertUserRoleRequestStatement(user,
                         requestDateStr));
 
                 int result = st.executeUpdate();
 
                 User fullUser = getUser(user.getUserName());
 
                 // send an email to the admin and one to the user
                 sendRequestPersmissionUpgradeEmailAdmin(fullUser);
                 sendRequestPersmissionUpgradeEmailUser(fullUser);
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to remove users" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public UserRoleRequest getUserRoleRequest(User user) throws IllegalArgumentException {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         UserRoleRequest userRoleRequest = new UserRoleRequest();
 
         if (conn != null) {
             try {
 
                 // get the UserRoleRequest based on the user name. It may not
                 // exist.
                 st = conn.prepareStatement(SQLStatements.selectUserRoleRequestStatement(user));
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create a UserRoleRequest object
                     userRoleRequest.setId(rs.getString(UserRoleRequestColumns.ID.colNum()));
                     userRoleRequest.setUserName(rs.getString(UserRoleRequestColumns.USERNAME
                             .colNum()));
                     userRoleRequest.setRequestDate(rs.getString(UserRoleRequestColumns.REQUESTDATE
                             .colNum()));
                     userRoleRequest.setResponseDate(rs
                             .getString(UserRoleRequestColumns.RESPONSEDATE.colNum()));
                     userRoleRequest.setRequestGranted(rs
                             .getBoolean(UserRoleRequestColumns.REQUESTGRANTED.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(
                         Level.SEVERE,
                         "Failed to get UserRoleRequest for user " + user.getUserName() + ".\n"
                                 + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
         return userRoleRequest;
     }
 
     @Override
     public String getUserRoleRequests() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         UserRoleRequestXmlGenerator xmlGenerator = new UserRoleRequestXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(UserRoleRequestXmlGenerator.USER_ROLE_REQUESTS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
 
                 // get the all user role requests.
                 st = conn.prepareStatement(SQLStatements.selectUserRoleRequestsStatement());
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this user role request
                     xmlGenerator.createUserRoleRquestXml(
                             rs.getString(UserRoleRequestColumns.ID.colNum()),
                             rs.getString(UserRoleRequestColumns.USERNAME.colNum()),
                             rs.getString(UserRoleRequestColumns.REQUESTDATE.colNum()),
                             rs.getString(UserRoleRequestColumns.RESPONSEDATE.colNum()),
                             rs.getString(UserRoleRequestColumns.REQUESTGRANTED.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE,
                         "exceptions while fetching user role requests." + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         String userRoleRequestXml = xmlGenerator.xmlToString();
         return userRoleRequestXml;
     }
 
     @Override
     public Boolean updateUserRoleRequest(UserRoleRequest userRoleRequest)
             throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
                 // update the UserRoleRequest values
                 st = conn.prepareStatement(SQLStatements
                         .updateUserRoleRequestStatement(userRoleRequest));
                 int result = st.executeUpdate();
 
                 if (result == 1) {
                     // now update the role based on if the request was granted
                     // or denied. 2 = execute, 3 = read only
                     int role = userRoleRequest.isRequestGranted() ? 2 : 3;
 
                     // update the User role
                     st = conn.prepareStatement(SQLStatements.updateUserRoleStatement(
                             userRoleRequest.getUserName(), role));
                     result = st.executeUpdate();
 
                     User user = getUser(userRoleRequest.getUserName());
                     sendResponsePersmissionUpgradeEmail(user, userRoleRequest.isRequestGranted());
                 }
 
                 success = true;
 
             } catch (Exception ex) {
                 s_logger.log(Level.SEVERE, "Failed to update UserRoleRequest" + ex.getStackTrace(),
                         ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public HashMap<String, String> getHelpPages(HashMap<String, String> fileInfo)
             throws IllegalArgumentException {
         String errorHtml = "<b>Information not available</b>";
 
         HashMap<String, String> fileContents = new HashMap<String, String>();
 
         Set<String> keys = fileInfo.keySet();
         Iterator<String> it = keys.iterator();
 
         while (it.hasNext()) {
             String fileTitle = it.next();
             String fileName = fileInfo.get(fileTitle);
 
             String fileData = readFile(getBasePath() + fileName, errorHtml);
             fileContents.put(fileTitle, fileData);
 
         }
 
         return fileContents;
     }
 
     /**
      * Get the zip file path from the db.
      * 
      * @param fileName
      * @param parentId
      * @param version
      * @return
      */
     private String getZipFile(int algorithmId) {
         String zipPath = null;
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
                 st = conn.prepareStatement(SQLStatements.selectZipFileStatement(algorithmId));
                 rs = st.executeQuery();
 
                 if (rs.next()) {
                     zipPath = rs.getString(1);
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to fetch zip files" + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
         return zipPath;
 
     }
 
     private List<Demographic> getDemographics(String xml) {
         List<Demographic> demographics = new ArrayList<Demographic>();
 
         if (xml != null && xml.length() > 1) {
             DOMXmlParser parser = new DOMXmlParser();
             demographics = parser.parseXmlFromString(xml);
         }
 
         return demographics;
     }
 
     /**
      * Read a file and return it as a String.
      * 
      * @param fileName
      * @return
      */
     private String readFile(String fileName, String errorString) {
 
         StringBuffer sb = new StringBuffer();
 
         try {
 
             FileInputStream fstream = new FileInputStream(fileName);
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 sb.append(strLine);
             }
 
             in.close();
         } catch (Exception e) {// Catch exception if any
 
             s_logger.log(Level.INFO, "Error reading the file:" + e.getMessage());
             return errorString;
         }
 
         return sb.toString();
     }
 
     /**
      * Convert a String value to an int. Any invalid numbers will be returned as
      * 0.
      * 
      * @param val
      * @return
      */
     private int convertToInt(String val) {
 
         int intVal = 0;
         try {
             intVal = Integer.parseInt(val);
         } catch (NumberFormatException ex) {
 
             s_logger.log(Level.INFO, "Number format exception" + ex.getStackTrace(), ex);
         }
         return intVal;
     }
 
     /**
      * Method to read the logging properties
      */
 
     @Override
     public void initializeLogging() throws IllegalArgumentException {
 
         try {
 
             FileInputStream fileInput = new FileInputStream(getBasePath()
                     + "data/LogProperties.properties");
 
             LogManager.getLogManager().readConfiguration(fileInput);
 
             fileInput.close();
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Get the last n algorithms uploaded.
      */
     @Override
     public String getLatestUploadedAlgorithms() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         AlgorithmXmlGenerator xmlGenerator = new AlgorithmXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(AlgorithmXmlGenerator.ALGORITHMS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
                 int limit = getAlgorithmLimitFromStartupPropertiesFile();
 
                 // get the latest algorithms based on uploaded date.
                 st = conn.prepareStatement(SQLStatements.selectRecentlyUploadedAlgorithms(limit));
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this algorithm
                     xmlGenerator.createAlgorithmXml(rs.getString(UploadColumns.PARENT_ID.colNum()),
                             rs.getString(UploadColumns.NAME.colNum()),
                             rs.getString(UploadColumns.VERSION.colNum()),
                             rs.getString(UploadColumns.USER.colNum()),
                             rs.getString(UploadColumns.DESCRIPTION.colNum()),
                             rs.getString(UploadColumns.INSTITUTION.colNum()),
                             rs.getString(UploadColumns.UPLOAD_DATE.colNum()),
                             rs.getString(UploadColumns.CREATEDATE.colNum()),
                             rs.getString(UploadColumns.COMMENT.colNum()),
                             rs.getString(UploadColumns.STATUS.colNum()),
                             rs.getString(UploadColumns.ASSOC_LINK.colNum()),
                             rs.getString(UploadColumns.ASSOC_NAME.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE,
                         "exceptions while fetching algorithms" + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return xmlGenerator.xmlToString();
     }
 
     /**
      * Get the news from the News table
      */
     @Override
     public String getNews() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         NewsXmlGenerator xmlGenerator = new NewsXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(NewsXmlGenerator.NEW_ITEMS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
 
                 // get the news.
                 st = conn.prepareStatement(SQLStatements.selectNews());
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this news item
                     xmlGenerator.createNewsXml(rs.getString(NewsColumns.ID.colNum()),
                             rs.getString(NewsColumns.DATE.colNum()),
                             rs.getString(NewsColumns.INFO.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE,
                         "exceptions while fetching news items" + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return xmlGenerator.xmlToString();
     }
 
     /**
      * Add a news item.
      */
     @Override
     public Boolean addNews(News news) throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // add the news item
                 st = conn.prepareStatement(SQLStatements.insertNewsStatement(news));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to add news item" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     /**
      * Update a news item.
      */
     @Override
     public Boolean updateNews(News news) throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // update the news values
                 st = conn.prepareStatement(SQLStatements.updateNewsStatement(news));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to update news item" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public Boolean removeNews(News news) throws IllegalArgumentException {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // remove the news item
                 st = conn.prepareStatement(SQLStatements.removeNewsStatement(news));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
                 s_logger.log(Level.SEVERE, "Failed to remove news item" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     /**
      * Get the Sharp News from the SharpNews table.
      */
     @Override
     public String getSharpNews() throws IllegalArgumentException {
 
         // Create an instance of class DOMXmlGenerator
         SharpNewsXmlGenerator xmlGenerator = new SharpNewsXmlGenerator();
 
         // Create the root element
         xmlGenerator.createDocumentAndRootElement(SharpNewsXmlGenerator.SHARP_NEWS_ITEMS);
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         if (conn != null) {
             try {
 
                 // get the latest sharp news.
                 st = conn.prepareStatement(SQLStatements.selectSharpNews());
                 rs = st.executeQuery();
 
                 while (rs.next()) {
                     // create an xml node for this news item
                     xmlGenerator.createSharpNewsXml(rs.getString(SharpNewsColumns.ID.colNum()),
                             rs.getString(SharpNewsColumns.INFO.colNum()));
                 }
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE,
                         "exceptions while fetching sharp news items" + ex.getStackTrace(), ex);
 
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return xmlGenerator.xmlToString();
     }
 
     @Override
     public Boolean addSharpNews(SharpNews news) throws IllegalArgumentException {
 
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // add the news item
                 st = conn.prepareStatement(SQLStatements.insertSharpNewsStatement(news));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to add Sharp news item" + ex.getStackTrace(), ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public Boolean updateSharpNews(SharpNews news) throws IllegalArgumentException {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // update the news values
                 st = conn.prepareStatement(SQLStatements.updateSharpNewsStatement(news));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
 
                 s_logger.log(Level.SEVERE, "Failed to update Sharp news item" + ex.getStackTrace(),
                         ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     @Override
     public Boolean removeSharpNews(SharpNews news) throws IllegalArgumentException {
         Connection conn = null;
         PreparedStatement st = null;
         ResultSet rs = null;
         conn = DBConnection.getDBConnection(getBasePath());
 
         boolean success = false;
 
         if (conn != null) {
             try {
 
                 // remove the news item
                 st = conn.prepareStatement(SQLStatements.removeSharpNewsStatement(news));
 
                 int result = st.executeUpdate();
                 success = true;
 
             } catch (Exception ex) {
                 s_logger.log(Level.SEVERE, "Failed to remove Sharp news item" + ex.getStackTrace(),
                         ex);
             } finally {
                 DBConnection.closeConnection(conn, st, rs);
             }
         }
 
         return new Boolean(success);
     }
 
     /**
      * Read in the startup properties.
      * 
      * @return Properties
      */
     private Properties getStartupPropertiesFile() {
 
         FileInputStream in = null;
         Properties starupProps = new Properties();
 
         try {
             in = new FileInputStream(getBasePath() + "data/Startup.properties");
             starupProps.load(in);
 
         } catch (FileNotFoundException ex) {
             s_logger.log(Level.SEVERE, ex.getMessage(), ex);
         } catch (IOException ex) {
             s_logger.log(Level.SEVERE, ex.getMessage(), ex);
         } finally {
             try {
                 if (in != null) {
                     in.close();
                 }
             } catch (IOException ex) {
                 s_logger.log(Level.SEVERE, ex.getMessage(), ex);
             }
         }
         return starupProps;
     }
 
     // Read the path from startup properties
     private String getPathFromStartupPropertiesFile() {
         Properties starupProps = getStartupPropertiesFile();
         return starupProps.getProperty("algorithmPath");
     }
 
     // Read the recently.uploaded.algorithms value from startup properties
     private int getAlgorithmLimitFromStartupPropertiesFile() {
 
         int limit = 5; // default
 
         Properties starupProps = getStartupPropertiesFile();
         String algorithmLimit = starupProps.getProperty("recently.uploaded.algorithms");
 
         try {
             limit = Integer.parseInt(algorithmLimit);
         } catch (NumberFormatException nfe) {
             s_logger.log(Level.SEVERE, "Error reading recently.uploaded.algorithms property. "
                     + nfe.getMessage(), nfe);
         }
         return limit;
     }
 
     @Override
     public Execution getDbStats(String type) throws IllegalArgumentException {
 
         // TODO - This is getting execution results from a file...
         // We need to make a REST call to get the actual data in the future.
 
         String xml = readFile(getBasePath() + "data/diseaseData.xml",
                 "Error reading DB stats file.");
 
         List<Demographic> demographics = null;
         Execution executionResults = new Execution();
 
         demographics = getDemographics(xml);
         executionResults.setDemographics(demographics);
 
         return executionResults;
     }
 
     private void insertExecution(Connection conn, Execution execution) throws SQLException {
         PreparedStatement st = SQLStatements.insertExecutionStatement(conn, execution);
         try {
             if (st != null) {
                 st.execute();
             }
         } finally {
             DBConnection.close(st);
         }
     }
 
     // public String openEditor() {
     // DroolsMetadata droolsMd = new DroolsMetadata();
     // droolsMd.setBpmnPath(getBasePath() + "/data/Disease.bpmn");
     // insertGuvnor(droolsMd);
     // s_logger.log(Level.INFO, "id: " + droolsMd.getGuvnorId());
     //
     // String id = droolsMd.getGuvnorId() != null ? droolsMd.getGuvnorId() :
     // "1234";
     // return getDesignerUrl() + "?profile=jbpm&uuid=" + id;
     // }
 
     @Override
     public String openEditor(Execution execution) {
         String editorUrl = getDesignerUrl() + "/designer/editor?profile=jbpm&uuid=";
 
         /* Copy from execution to drools */
         Drools droolsMetadata = new Drools();
         String droolsId = UUID.randomUUID().toString();
 
         /* TODO: copy rules files */
 
         try {
             String droolsPath = getDroolsFilesPath();
             File bpmnFile = new File(droolsPath + "workflow.bpmn");
             FileUtils.copyFile(new File(execution.getBpmnPath()), bpmnFile);
             droolsMetadata.setBpmnPath(bpmnFile.getPath());
             droolsMetadata.setId(droolsId);
         } catch (IOException ioe) {
             s_logger.log(Level.WARNING,
                     "Unable to copy the execution bpmn file to the drools directory.", ioe);
         }
 
         /* insert into guvnor/drools */
         insertGuvnor(droolsMetadata);
         insertDrools(null, droolsMetadata, droolsId, Htp.getLoggedInUser().getUserName(), "test");
 
         return editorUrl + droolsId;
     }
 
 	@Override
 	public String getMatEditorUrl(User user) {
 		String url = "";
 		if (user != null) {
 			url = super.getMatEditorUrl() + "/Login.html?userId="+user.getUserName()+"&htpId="+user.getPassword();
 		}
 		else {
 			url = super.getMatEditorUrl();
 		}
 		return url;
 	}
 
 	@Override
 	public MatImport getMatImport(String tokenId) throws IllegalArgumentException {
 		return ImportServlet.getMatImport(tokenId);
 	}
 
 	private void insertDrools(Connection connection, Drools droolsMetadata, String droolsId,
             String username, String algorithmName) {
         /* insert into drools table */
         PreparedStatement st = null;
         try {
             st = SQLStatements.insertDroolsStatement(connection, droolsMetadata);
             if (st != null) {
                 st.execute();
             }
         } catch (SQLException sqle) {
             s_logger.log(Level.WARNING,
                     "Unable to insert the drools files into the guvnor repository.", sqle);
         } finally {
             DBConnection.close(st);
         }
     }
 
     private void insertGuvnor(Drools droolsMetadata) {
         s_logger.log(Level.INFO, "inserting guvnor...");
         File bpmnFile = new File(droolsMetadata.getBpmnPath());
         String addAssetUrl = getGuvnorUrl() + "/rest/packages/defaultPackage/assets";
 
         s_logger.log(Level.INFO, "file: " + bpmnFile.getPath());
         s_logger.log(Level.INFO, "addAssetUrl: " + addAssetUrl);
 
         String charset = "UTF-8";
         String CRLF = "\r\n"; // Line separator required by multipart/form-data.
 
         try {
             URL url = new URL(addAssetUrl);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
             connection.setConnectTimeout(0);
             connection.setDoOutput(true);
             connection.setInstanceFollowRedirects(false);
             connection.setRequestProperty("Accept", "application/atom+xml");
             String authHeader = "Basic "
                     + Base64.encodeBytes((getGuvnorUser() + ":" + getGuvnorPass()).getBytes());
             connection.setRequestProperty("Authorization", authHeader);
             connection.setRequestMethod("POST");
             PrintWriter writer = null;
             try {
                 OutputStream output = connection.getOutputStream();
                 // true = autoFlush,important!
                 writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
 
                 // Send binary file.
                 writer.append("Content-Type: application/octet-stream").append(CRLF);
                 // writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                 writer.append(
                         "Content-Disposition: form-data; name=\"Slug\"; filename=\""
                                 + bpmnFile.getName() + "\"").append(CRLF);
 
                 writer.append(CRLF).flush();
                 InputStream input = null;
                 try {
                     input = new FileInputStream(bpmnFile);
                     byte[] buffer = new byte[1024];
                     for (int length = 0; (length = input.read(buffer)) > 0;) {
                         output.write(buffer, 0, length);
                     }
                     output.flush(); // Important! Output cannot be closed. Close
                                     // of
                     // writer will close output as well.
                 } finally {
                     if (input != null) {
                         input.close();
                     }
                 }
                 writer.append(CRLF).flush(); // CRLF is important! It indicates
                                              // end
                 // of binary boundary.
                 // End of multipart/form-data.
 
             } finally {
                 if (writer != null) {
                     writer.close();
                 }
                 try {
                     s_logger.log(Level.INFO, "Getting response...");
                     Object obj = connection.getContent();
                     InputStream in = connection.getInputStream();
                     DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                     DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                     Document doc = docBuilder.parse(in);
 
                     TransformerFactory transFactory = TransformerFactory.newInstance();
                     Transformer trans = transFactory.newTransformer();
                     trans.setOutputProperty(OutputKeys.METHOD, "xml");
                     trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
                     StringWriter sw = new StringWriter();
                     StreamResult result = new StreamResult(sw);
                     DOMSource source = new DOMSource(doc.getDocumentElement());
 
                     trans.transform(source, result);
 
                     s_logger.log(Level.INFO, "sw: " + sw.toString());
                     s_logger.log(Level.INFO, "result: " + result.toString());
 
                     droolsMetadata.setGuvnorId(getGuvnorUuid(sw.toString()));
                 } catch (IOException ioe) {
                     s_logger.log(Level.WARNING, connection.getErrorStream().toString(), ioe);
                 } catch (Exception e) {
                     s_logger.log(Level.WARNING, "An error has occurred.", e);
 
                 }
                 connection.disconnect();
             }
 
         } catch (MalformedURLException e) {
             s_logger.log(Level.WARNING, "", e);
         } catch (IOException e) {
             s_logger.log(Level.WARNING, "", e);
         }
 
         // if (bpmnFile.exists()) {
         // try {
         // String authHeader = "Basic "
         // + Base64.encodeBytes((getGuvnorUser() + ":" +
         // getGuvnorPass()).getBytes());
         //
         // GuvnorClient guvnorClient = null;
         // try {
         // guvnorClient = ProxyFactory.create(GuvnorClient.class,
         // guvnorRestUrl);
         // } catch (Exception e) { s_logger.log(Level.INFO, "error: ", e);}
         //
         // if (guvnorClient != null) {
         // s_logger.log(Level.INFO, "adding asset to guvnor");
         // String xmlResponse = guvnorClient.addAsset("defaultPackage",
         // authHeader, UUID.randomUUID()
         // .toString() + ".bpmn", FileUtils.readFileToByteArray(bpmnFile));
         // droolsMetadata.setGuvnorId(getGuvnorUuid(xmlResponse));
         // }
         // }
         // catch (IOException ioe) {
         // s_logger.log(Level.WARNING,
         // "Unable to insert bpmn into Guvnor repository.", ioe);
         // }
         // }
         // else {
         // s_logger.log(Level.WARNING, "The BPMN_PATH file '" +
         // bpmnFile.getAbsolutePath()
         // + "' did not exist");
         // }
     }
 
     private String getGuvnorUuid(String xmlString) {
         s_logger.log(Level.INFO, "Guvnor Response: " + xmlString);
         String uuidString = null;
 
         if (xmlString != null) {
             try {
                 Document document;
                 DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                         .newInstance();
                 DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                 document = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xmlString
                         .getBytes("utf-8"))));
                 document.getDocumentElement().normalize();
 
                 XPath xpath = XPathFactory.newInstance().newXPath();
                 String xtractUUID = "/entry/metadata/uuid/value";
                 XPathExpression expression = xpath.compile(xtractUUID);
                 uuidString = expression.evaluate(document);
                 s_logger.log(Level.INFO, "Extracted UUID: " + uuidString);
             } catch (ParserConfigurationException pce) {
                 s_logger.log(Level.WARNING, "Error parsing the xml string.", pce);
             } catch (SAXException saxe) {
                 s_logger.log(Level.WARNING, "Error building the xml document.", saxe);
             } catch (IOException ioe) {
                 s_logger.log(Level.WARNING, "Error parsing the xml string.", ioe);
             } catch (XPathExpressionException xpee) {
                 s_logger.log(Level.WARNING, "Unable to compile xpath expression.", xpee);
             }
         }
 
         return uuidString;
     }
 
     /**
      *
      */
     @Override
     public Boolean saveJbpm(String uuid, String bpmn, String title, String comment) {
         Boolean success = new Boolean(false);
         Connection conn = DBConnection.getDBConnection(getBasePath());
         PreparedStatement st = null;
         ResultSet rs = null;
 
         Drools parent = new Drools();
         try {
             st = SQLStatements.selectDroolsStatement(conn, uuid);
 
             rs = st.executeQuery();
             boolean editable = false;
             if (rs.next()) {
                 editable = rs.getBoolean(DroolsColumns.EDITABLE.getColName());
                 parent.setId(rs.getString(DroolsColumns.ID.getColName()));
                 parent.setParentId(rs.getString(DroolsColumns.PARENT_ID.getColName()));
                 parent.setBpmnPath(rs.getString(DroolsColumns.BPMN_PATH.getColName()));
                 parent.setImagePath(rs.getString(DroolsColumns.IMAGE_PATH.getColName()));
                 parent.setRulesPath(rs.getString(DroolsColumns.RULES_PATH.getColName()));
                 parent.setTitle(rs.getString(DroolsColumns.TITLE.getColName()));
                 parent.setComment(rs.getString(DroolsColumns.COMMENT.getColName()));
                 parent.setUsername(rs.getString(DroolsColumns.USERNAME.getColName()));
                 parent.setEditable(editable);
             }
 
             String droolsFilesPath = getDroolsFilesPath();
 
             if (editable) {
                 /* TODO: Update file */
                 /* TODO: backup original file in case sql fails */
 
                 /* Update Drools in database */
                 st = SQLStatements.updateDroolsStatement(conn, uuid, bpmn, title, comment);
                 success = st.execute();
 
             } else {
                 /* Create new file */
                 String droolsId = UUID.randomUUID().toString();
                 File bpmnFile = new File(droolsFilesPath + File.separator + droolsId
                         + File.separator + "workflow.bpmn");
                 FileUtils.writeStringToFile(bpmnFile, bpmn, "utf-8");
 
                 /* Copy drools rules from parent */
                 // String rulesPath = droolsFilesPath + File.separator + "%s" +
                 // File.separator + "rules";
                 // File parentRulesDir = new File(String.format(rulesPath,
                 // parent.getId()));
                 // File rulesDir = new File(String.format(rulesPath, droolsId));
                 // FileUtils.copyDirectory(parentRulesDir, rulesDir);
 
                 /* Insert new Drools into database */
                 Drools drools = parent.clone();
                 drools.setId(droolsId);
                 drools.setParentId(uuid);
                 drools.setBpmnPath(bpmnFile.getPath());
                 drools.setTitle(title);
                 drools.setComment(comment);
                 drools.setEditable(true);
                 st = SQLStatements.insertDroolsStatement(conn, drools);
                 success = st.execute();
             }
         } catch (SQLException sqle) {
             s_logger.log(Level.WARNING, "Unable to save the edited bpmn file.", sqle);
             /* TODO: clean up file system - remove the new files. */
             // FileUtils.deleteDirectory();
             // FileUtils.deleteQuietly();
         } catch (IOException ioe) {
             s_logger.log(Level.WARNING, "Unable to save the edited bpmn file.", ioe);
         } finally {
             DBConnection.closeConnection(conn, st, rs);
             /* TODO: remove backup bpmn file */
         }
 
         return success;
 
     }
 
     @Override
     public Execution getLatestExecution(String algorithmName, String algorithmVersion,
             String algorithmCategoryId, String algorithmUser) {
         Connection connection = DBConnection.getDBConnection(getBasePath());
         PreparedStatement statement = null;
         ResultSet resultSet = null;
 
         Execution execution = new Execution();
         execution.setAlgorithmName(algorithmName);
         execution.setAlgorithmVersion(algorithmVersion);
         execution.setAlgorithmCategoryId(algorithmCategoryId);
         execution.setUser(algorithmUser);
 
         try {
             statement = SQLStatements.selectLatestUsersExecutionStatement(connection, execution);
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 execution.setId(resultSet.getString(ExecutionColumns.ID.colName()));
                 execution.setStartDate(resultSet.getString(ExecutionColumns.START_DATE.colName()));
                 execution.setElapsedTime(resultSet.getString(ExecutionColumns.ELAPSED_TIME
                         .colName()));
                 execution.setDateRangeFrom(resultSet.getString(ExecutionColumns.DATE_RANGE_FROM
                         .colName()));
                 execution.setDateRangeTo(resultSet.getString(ExecutionColumns.DATE_RANGE_TO
                         .colName()));
                 execution.setXmlPath(resultSet.getString(ExecutionColumns.XML_PATH.colName()));
                 execution.setAlgorithmCategoryId(resultSet.getString(ExecutionColumns.CATEGORY_NUM
                         .colName()));
                 execution.setAlgorithmCategoryPath(resultSet.getString(ExecutionColumns.CATEGORY
                         .colName()));
                 execution.setBpmnPath(resultSet.getString(ExecutionColumns.BPMN_PATH.colName()));
                 execution.setRulesPath(resultSet.getString(ExecutionColumns.RULES_PATH.colName()));
                 String imagePath = resultSet.getString(ExecutionColumns.IMAGE_PATH.colName());
                 if (imagePath != null)
                     execution.setImage(getImage(imagePath));
 
                 /* Demographics */
                 if (execution.getXmlPath() != null) {
                     File xmlFile = new File(getExecutionResultsPath() + File.separator
                             + execution.getXmlPath());
                     String xmlString = FileUtils.readFileToString(xmlFile);
                     List<Demographic> demographics = getDemographics(xmlString);
                     execution.setDemographics(demographics);
                 }
             }
 
         } catch (SQLException sqle) {
             s_logger.log(Level.WARNING, "Unable to get the latest users executions.", sqle);
         } catch (IOException ioe) {
             s_logger.log(Level.WARNING, "Unable to get the latest users executions.", ioe);
         } finally {
             DBConnection.closeConnection(connection, statement, resultSet);
         }
 
         return execution;
     }
 
     private void sendRequestPersmissionUpgradeEmailAdmin(User user) {
         String host = getSmtpHost();
         String from = getSmtpFromAddress();
         String messageText = getEmailContentsUserRoleRequestAdmin();
         String port = getSmtpPort();
         String pw = getSmtpPassword();
 
         SmtpClient.sendRequestPersmissionUpgradeEmailAdmin(host, from, pw, port, messageText, user);
     }
 
     private void sendRequestPersmissionUpgradeEmailUser(User user) {
         String host = getSmtpHost();
         String from = getSmtpFromAddress();
         String messageText = getEmailContentsUserRoleRequestUser();
         String port = getSmtpPort();
         String pw = getSmtpPassword();
 
         SmtpClient.sendRequestPersmissionUpgradeEmailUser(host, from, pw, port, messageText, user);
     }
 
     private void sendResponsePersmissionUpgradeEmail(User user, boolean granted) {
         String host = getSmtpHost();
         String from = getSmtpFromAddress();
         String messageText = granted ? getEmailContentsUserRoleReplyGranted()
                 : getEmailContentsUserRoleReplyDenied();
         String port = getSmtpPort();
         String pw = getSmtpPassword();
 
         SmtpClient.sendResponsePersmissionUpgradeEmail(host, from, pw, port, messageText, user);
     }
 
     private String convertDate(String dateStr) {
         String outDate = "";
         try {
             Date inDate = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:s").parse(dateStr);
             outDate = new SimpleDateFormat("MMMM dd, yyyy HH:mm").format(inDate);
         } catch (ParseException pe) {
             s_logger.log(Level.WARNING, "Unable to convert date " + dateStr + ".", pe);
         }
 
         return outDate;
     }
 
     private String convertTime(String timeStr) {
         long millis = Long.parseLong(timeStr.split("\\s+")[0]);
         String outTime = String.format(
                 "%d min, %d sec",
                 TimeUnit.MILLISECONDS.toMinutes(millis),
                 TimeUnit.MILLISECONDS.toSeconds(millis)
                         - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
 
         return outTime;
     }
 
 }
