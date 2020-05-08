 package com.computas.sublima.app.service;
 
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.impl.DefaultSparqlDispatcher;
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * A class to support the administration of Sublima
  * Has methods for getting topics, statuses, languages, media types, audience etc.
  *
  * @author: mha
  * Date: 13.mar.2008
  */
 //todo Use selected interface language for all labels
 public class AdminService {
 
   private static Logger logger = Logger.getLogger(AdminService.class);
   private SparqlDispatcher sparqlDispatcher = new DefaultSparqlDispatcher();
   private SparulDispatcher sparulDispatcher = new DefaultSparulDispatcher();
 
   /**
    * Method to get all relation types
    *
    * @return RDF XML result
    */
   public String getAllRelationTypes() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?relation rdfs:subPropertyOf skos:semanticRelation ;",
             "    rdfs:label ?label .",
             "}",
             "WHERE {",
             "?relation rdfs:subPropertyOf skos:semanticRelation ;",
             "rdfs:label ?label .",
             "FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllRelationTypes() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all publishers
    *
    * @return A String RDF/XML containing all the publishers
    */
   public String getAllPublishers() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?publisher a foaf:Agent ;",
             "    foaf:name ?name .",
             "}",
             "WHERE {",
             "?publisher a foaf:Agent ;",
             "foaf:name ?name .",
             "FILTER langMatches( lang(?name), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllPublishers() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all statuses
    *
    * @return A String RDF/XML containing all the statuses
    */
   public String getAllStatuses() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?status a wdr:DR ;",
             "    rdfs:label ?label .",
             "}",
             "WHERE {",
             "    ?status a wdr:DR ;",
             "    rdfs:label ?label .",
             "    FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
 
     logger.trace("AdminService.getAllStatuses() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all languages
    *
    * @return A String RDF/XML containing all the languages
    */
   public String getAllLanguages() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "?language a lingvoj:Lingvo ;",
             "          rdfs:label ?label .",
             "}",
             "WHERE {",
             "?language a lingvoj:Lingvo ;",
             "          rdfs:label ?label .",
             "FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllLanguages() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all media types
    *
    * @return A String RDF/XML containing all the media types
    */
   public String getAllMediaTypes() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?mediatype a dct:MediaType ;",
             "             rdfs:label ?label .",
             "}",
             "WHERE {",
             "    ?mediatype a dct:MediaType ;",
             "             rdfs:label ?label .",
             "    FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllMediaTypes() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all audiences
    *
    * @return A String RDF/XML containing all the audiences
    */
   public String getAllAudiences() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?audience a dct:AgentClass ;",
             "             rdfs:label ?label .",
             "}",
             "WHERE {",
             "    ?audience a dct:AgentClass ;",
             "             rdfs:label ?label .",
             "    FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllAudiences() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get a resource by its URI
    *
    * @return A String RDF/XML containing the resource
    */
   public Object getResourceByURI(String uri) {
 
     try {
       //uri = URLEncoder.encode(uri, "UTF-8");
       uri = "<" + uri + ">";
 
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX sub: <http://xmlns.computas.com/sublima#>",
             "DESCRIBE " + uri + " ?comment",
             "WHERE {",
             "  OPTIONAL { " + uri + " sub:comment ?comment . }",
             "}"});
 
     logger.trace("AdminService.getResourceByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String insertPublisher(String publishername) {
     String publisherURI = publishername.replace(" ", "_");
     publisherURI = publisherURI.replace(".", "_");
     publisherURI = publisherURI.replace(",", "_");
     publisherURI = publisherURI.replace("/", "_");
     publisherURI = publisherURI.replace("-", "_");
     publisherURI = publisherURI.replace("'", "_");
     publisherURI = getProperty("sublima.base.url") + "agent/" + publisherURI;
 
 
     String insertPublisherByName =
             "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                     "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                     "INSERT\n" +
                     "{\n" +
                     "<" + publisherURI + "> a foaf:Agent ;\n" +
                     "foaf:name \"" + publishername + "\"@no .\n" +
                     "}";
 
     logger.info("updatePublisherByURI() ---> " + publisherURI + " -- SPARUL INSERT  --> " + insertPublisherByName);
     boolean success = false;
     success = sparulDispatcher.query(insertPublisherByName);
     logger.info("updatePublisherByURI() ---> " + publisherURI + " -- INSERT NEW NAME --> " + success);
     if (success) {
       return publisherURI;
     } else {
       return "";
     }
   }
 
   /**
    * Method to get all topics
    *
    * @return A String RDF/XML containing all the topics
    */
   public String getAllTopics() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "CONSTRUCT {",
             "    ?topic a skos:Concept ;",
             "        skos:prefLabel ?label .",
             "}",
             "WHERE {",
             "    ?topic a skos:Concept ;",
             "        skos:prefLabel ?label .",
             "FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllTopics() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "DESCRIBE <" + uri + ">",
             "WHERE {",
             "<" + uri + "> a skos:Concept .",
             "}"});
 
     logger.trace("AdminService.getTopicByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicResourcesByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "DESCRIBE ?resource",
             "WHERE {",
             "    ?resource dct:subject <" + uri + "> . ",
             "}"});
 
     logger.trace("AdminService.getTopicResourcesByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getThemeTopics() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sub: <http://xmlns.computas.com/sublima#>",
             "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>",
             "DESCRIBE ?theme",
             "WHERE {",
             "    ?theme sub:theme \"true\"^^xsd:boolean .",
             "}"});
 
     logger.trace("AdminService.getTopicByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getAllUsers() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?user",
             "WHERE {",
             "    ?user a sioc:User ;",
             "        rdfs:label ?label .",
             "FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllUsers() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getUserByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "DESCRIBE <" + uri + ">"});
 
     logger.trace("AdminService.getTopicResourcesByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getRelationByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "DESCRIBE <" + uri + ">"});
 
     logger.trace("AdminService.getRelationByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   private static String convertToHex(byte[] data) {
     StringBuffer buf = new StringBuffer();
     for (int i = 0; i < data.length; i++) {
       int halfbyte = (data[i] >>> 4) & 0x0F;
       int two_halfs = 0;
       do {
         if ((0 <= halfbyte) && (halfbyte <= 9))
           buf.append((char) ('0' + halfbyte));
         else
           buf.append((char) ('a' + (halfbyte - 10)));
         halfbyte = data[i] & 0x0F;
       } while (two_halfs++ < 1);
     }
     return buf.toString();
   }
 
   public String generateSHA1(String text)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
     MessageDigest md;
     md = MessageDigest.getInstance("SHA-1");
     byte[] sha1hash = new byte[40];
     md.update(text.getBytes("UTF-8"), 0, text.length());
     sha1hash = md.digest();
     return convertToHex(sha1hash);
   }
 
 
   public String getTopicsByLetter(String letter) {
     if (letter.equalsIgnoreCase("0-9")) {
       letter = "[0-9]";
     }
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "DESCRIBE ?topic",
             "WHERE {",
             "?topic a skos:Concept ;",
             "    skos:prefLabel ?label .",
             "FILTER regex(str(?label), \"^" + letter + "\", \"i\")",
             "}"});
 
     logger.trace("AdminService.getTopicResourcesByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public boolean validateURL(String url) {
     String ourcode;
     try {
       // Do a URL check so that we know we have a valid URL
       URLActions urlAction = new URLActions(url);
       ourcode = urlAction.getCode();
     }
     catch (NullPointerException e) {
       e.printStackTrace();
       return false;
     }
 
     if ("302".equals(ourcode) ||
             "303".equals(ourcode) ||
             "304".equals(ourcode) ||
             "305".equals(ourcode) ||
             "307".equals(ourcode) ||
             ourcode.startsWith("2")) {
       return true;
     } else {
       return false;
     }
   }
 
   public String getAllRoles() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?role",
             "WHERE {",
             "    ?role a sioc:Role ;",
             "        rdfs:label ?label .",
             "FILTER langMatches( lang(?label), \"no\" )",
             "}"});
 
     logger.trace("AdminService.getAllRoles() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getRoleByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "DESCRIBE <" + uri + ">"});
 
     logger.trace("AdminService.getRoleByURI() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get the role privileges based on the given URI.
    * Returns the privileges as XML.
    *
    * @param roleuri
    * @return
    */
   public String getRolePrivilegesAsXML(String roleuri) {
     DatabaseService dbService = new DatabaseService();
     Statement statement;
     ResultSet resultSet;
     StringBuffer xmlBuffer = new StringBuffer();
 
     String getRolePrivilegesString = "SELECT privilege FROM roleprivilege WHERE role = '" + roleuri + "';";
 
     xmlBuffer.append("<c:privileges xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
 
     logger.trace("AdminService.getRolePrivilegesAsXML --> " + getRolePrivilegesString);
 
     try {
       statement = dbService.doSQLQuery(getRolePrivilegesString);
       resultSet = statement.getResultSet();
 
       while (resultSet.next()) {
         xmlBuffer.append("<c:privilege>" + resultSet.getString(1) + "</c:privilege>");
       }
 
       xmlBuffer.append("</c:privileges>\n");
     } catch (SQLException e) {
       xmlBuffer.append("</c:privileges>\n");
       e.printStackTrace();
       logger.trace("AdminService.getRolePrivilegesAsXML --> FAILED\n");
 
     }
     return xmlBuffer.toString();
   }
 
   /**
    * Method to get the user role based on the username
    * This method use JAXP to perform a XPATH operation on the results from Joseki.
    *
    * @param name
    * @return role
    */
   public String getUserRole(String name) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "SELECT ?role",
             "WHERE {",
             "?user a sioc:User ;",
            "    sioc:Role ?role ;",
            "    sioc:email <mailto:" + name + "> . }"});
 
     logger.trace("AdminService.getUserRole() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
     try {
 
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(queryResult.toString().getBytes("UTF-8")));
       XPathExpression expr = XPathFactory.newInstance().newXPath().compile("/sparql/results/result/binding/uri");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
       return nodes.item(0).getTextContent();
 
     } catch (Exception e) {
       e.printStackTrace();
       return "<empty/>";
     }
   }
 }
