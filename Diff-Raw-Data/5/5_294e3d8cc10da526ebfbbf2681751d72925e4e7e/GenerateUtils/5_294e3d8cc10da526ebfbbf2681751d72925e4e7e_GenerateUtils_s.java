 package com.computas.sublima.app.index;
 
 import com.computas.sublima.query.impl.DefaultSparqlDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import com.computas.sublima.query.service.SettingsService;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 
 /**
  * @author: mha
  * Date: 05.feb.2009
  */
 public class GenerateUtils {
 
   private DefaultSparqlDispatcher sq = new DefaultSparqlDispatcher();
 
   public GenerateUtils() {
   }
 
 
   /**
    * Method to extract the topic URIs from a XML format using XPATH
    *
    * @return ArrayList<String> containing all URIs
    */
   public ArrayList<String> getListOfTopicURIs() {
     String queryString = StringUtils.join("\n", new String[]{
             "SELECT DISTINCT ?uri",
             "WHERE {",
             "        ?uri a <http://www.w3.org/2004/02/skos/core#Concept> }"});
 
     String xmlResult = (String) sq.query(queryString);
 
     ArrayList<String> uriList = new ArrayList<String>();
 
     try {
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(xmlResult.getBytes("UTF-8")));
      XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//td");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 
       for (int i = 0; i < nodes.getLength(); i++) {
         uriList.add(nodes.item(i).getTextContent());
       }
 
     } catch (Exception e) {
       System.out.println("Could not get list of URIs from XML");
       e.printStackTrace();
     }
 
     return uriList;
   }
 
   /**
    * Method to extract the resource URIs from a XML format using XPATH
    *
    * @return ArrayList<String> containing all URIs
    */
   public ArrayList<String> getListOfResourceURIs() {
     String queryString = StringUtils.join("\n", new String[]{
             "SELECT DISTINCT ?uri",
             "WHERE {",
             "        ?uri a <http://xmlns.computas.com/sublima#Resource> }"});
 
     String xmlResult = (String) sq.query(queryString);
 
     ArrayList<String> uriList = new ArrayList<String>();
 
     try {
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(xmlResult.getBytes("UTF-8")));
      XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//td");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 
       for (int i = 0; i < nodes.getLength(); i++) {
         uriList.add(nodes.item(i).getTextContent());
       }
 
     } catch (Exception e) {
       System.out.println("Could not get list of URIs from XML");
       e.printStackTrace();
     }
 
     return uriList;
   }
 
   /**
    * This method takes the uri, fields to index and prefixes as input parameters and returns a SPARQL SELECT query
    * for getting the text to index
    *
    * @param uri           URI
    * @param fieldsToIndex fields to index
    * @param prefixes      prefixes
    * @param graphs        graphname
    * @return query
    */
   public String createSelectQueryToGetFields(String uri, String[] fieldsToIndex, String[] prefixes, String[] graphs) {
     StringBuilder query = new StringBuilder();
 
     // Add prefixes for the query
     for (String prefix : prefixes) {
       query.append("prefix ").append(prefix).append("\n");
     }
 
     query.append("SELECT");
 
     // For each field to index, add a ?objectx to the query string
     for (int i = 1; i <= fieldsToIndex.length; i++) {
       query.append(" ?object").append(i);
     }
 
     query.append("\n");
 
     for (String graph : graphs) {
       query.append("FROM <" + graph + ">\n");
     }
 
     query.append("WHERE {\n");
 
     int objectcounter = 1;
     int varcounter = 1;
 
     for (String field : fieldsToIndex) {
       query.append("OPTIONAL {\n");
 
       // For each field to index, iterate and check if the field is nested (ie. dct:publisher/foaf:name)
       if (field.contains("/")) {
         String[] fragments = field.split("/");
 
         query.append(uri);
         query.append(" ").append(fragments[0]).append(" ?var").append(varcounter).append(" .\n");
 
         int j = 1;
 
         while (j < fragments.length - 1) {
           if (fragments.length > 2) {
             int l = 1;
             while (l < fragments.length - 1) {
               query.append("?var").append(varcounter).append(" ").append(fragments[j]).append(" ?var").append(varcounter + 1).append(" .\n");
               varcounter++;
               l++;
               j++;
             }
           }
         }
 
         query.append("?var").append(varcounter).append(" ").append(fragments[fragments.length - 1]).append(" ?object").append(objectcounter).append(" .");
 
         varcounter++;
 
       } else {
         query.append(uri).append(" ").append(field).append(" ?object").append(objectcounter).append(" .\n");
       }
       query.append("}\n");
       objectcounter++;
     }
     query.append("}\n");
 
     return query.toString();
   }
 
   /**
    * This method takes the uri, fields to index and prefixes as input parameters and returns a SPARQL CONSTRUCT query
    * for getting the text to index
    *
    * @param uri           URI
    * @param fieldsToIndex fields to index
    * @param prefixes      prefixes
    * @param graphs        graphname
    * @return query
    */
   public String createConstructQueryToGetFields(String uri, String[] fieldsToIndex, String[] prefixes, String[] graphs) {
     StringBuilder construct = new StringBuilder();
     StringBuilder where = new StringBuilder();
 
     // Add prefixes for the construct
     for (String prefix : prefixes) {
       construct.append("prefix ").append(prefix).append("\n");
     }
 
     construct.append("CONSTRUCT {");
     where.append("WHERE {\n");
 
     int objectcounter = 1;
     int varcounter = 1;
 
     for (String field : fieldsToIndex) {
       where.append("OPTIONAL {\n");
 
       // For each field to index, iterate and check if the field is nested (ie. dct:publisher/foaf:name)
       if (field.contains("/")) {
         String[] fragments = field.split("/");
 
         construct.append(uri);
         where.append(uri);
         construct.append(" ").append(fragments[0]).append(" ?var").append(varcounter).append(" .\n");
         where.append(" ").append(fragments[0]).append(" ?var").append(varcounter).append(" .\n");
 
         int j = 1;
 
         while (j < fragments.length - 1) {
           if (fragments.length > 2) {
             int l = 1;
             while (l < fragments.length - 1) {
               construct.append("?var").append(varcounter).append(" ").append(fragments[j]).append(" ?var").append(varcounter + 1).append(" .\n");
               where.append("?var").append(varcounter).append(" ").append(fragments[j]).append(" ?var").append(varcounter + 1).append(" .\n");
               varcounter++;
               l++;
               j++;
             }
           }
         }
 
         construct.append("?var").append(varcounter).append(" ").append(fragments[fragments.length - 1]).append(" ?object").append(objectcounter).append(" .");
         where.append("?var").append(varcounter).append(" ").append(fragments[fragments.length - 1]).append(" ?object").append(objectcounter).append(" .");
 
         varcounter++;
 
       } else {
         construct.append(uri).append(" ").append(field).append(" ?object").append(objectcounter).append(" .\n");
         where.append(uri).append(" ").append(field).append(" ?object").append(objectcounter).append(" .\n");
       }
       where.append("}\n");
       objectcounter++;
     }
     construct.append("}\n");
 
     for (String graph : graphs) {
       construct.append("FROM <" + graph + ">\n");
     }
     where.append("}\n");
 
     return construct.toString() + where.toString();
   }
 
 
   /**
    * This method reads a property file and returns the prefixes that will be used
    *
    * @return String[] with prefixes
    */
   public String[] getPrefixes() {
     String[] prefixes = new String[0];
     try {
       prefixes = SettingsService.getProperty("sublima.prefixes").split(";");
     } catch (Exception e) {
       System.err.print("Could not read the property sublima.prefixes from the properties files.\n" +
               "Please check that the properties file contains the correct configurations.");
     }
 
     return prefixes;
   }
 
   /**
    * This method reads a property file and returns the properties that will be shown in the infobox
    *
    * @return String[] with fields to index
    */
   public String[] getResourceFreetextFieldsToIndex() {
 
     String[] fieldsToIndex = new String[0];
     try {
       fieldsToIndex = new String(SettingsService.getProperty("sublima.resource.searchfields").getBytes("ISO-8859-1"), "UTF-8").split(";");
     } catch (Exception e) {
       System.err.print("Could not read the property sublima.resource.searchfields from the properties files.\n" +
               "Please check that the properties file contains the correct configurations.");
     }
 
     return fieldsToIndex;
   }
 
   /**
    * This method reads a property file and returns the properties that will be shown in the infobox
    *
    * @return String[] with fields to index
    */
   public String[] getTopicFieldsToIndex() {
 
     String[] fieldsToIndex = new String[0];
     try {
       fieldsToIndex = new String(SettingsService.getProperty("sublima.topic.searchfields").getBytes("ISO-8859-1"), "UTF-8").split(";");
     } catch (Exception e) {
       System.err.print("Could not read the property sublima.topic.searchfields from the properties files.\n" +
               "Please check that the properties file contains the correct configurations.");
     }
 
     return fieldsToIndex;
   }
 
   public String getBaseGraph() {
     String graph = "";
     try {
       graph = new String(SettingsService.getProperty("mediasone.basegraph").getBytes("ISO-8859-1"), "UTF-8");
     } catch (Exception e) {
       System.err.print("Could not read the property mediasone.search.mapping from the properties files.\n" +
               "Please check that the properties file contains the correct configurations.");
     }
 
     return graph;
   }
 
   public void updateIndexStatistics(String type) {
     DatabaseService dbService = new DatabaseService();
 
     Calendar cal = Calendar.getInstance();
     SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
     String date = sdf.format(cal.getTime());
 
     try {
       String updateSQL = "UPDATE DB.DBA.indexstatistics "
               + "SET \"date\" = '" + date + "' "
               + "WHERE type =  '" + type + "'";
 
       int insertedRows = dbService.doSQLUpdate(updateSQL);
 
       if (insertedRows == 0) {
        String insertSQL = "INSERT INTO DB.DBA.indexstatistics "
                     + "(type, \"date\") "
                     + "VALUES "
                     + "('" + type + "', '" + date + "')";
 
         insertedRows = dbService.doSQLUpdate(insertSQL);
       }
 
     } catch (Exception e) {
       System.out.println(e.getMessage());
       dbService = null;
     }
     dbService = null;
   }
 }
