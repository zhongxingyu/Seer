 package com.computas.sublima.app.service;
 
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import com.hp.hpl.jena.db.IDBConnection;
 import com.hp.hpl.jena.db.ModelRDB;
 import com.hp.hpl.jena.query.*;
 import com.hp.hpl.jena.query.larq.IndexBuilderString;
 import com.hp.hpl.jena.query.larq.IndexLARQ;
 import com.hp.hpl.jena.query.larq.LARQ;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.shared.DoesNotExistException;
 import com.hp.hpl.jena.shared.JenaException;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.log4j.Logger;
 import org.postgresql.util.PSQLException;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * A class to support Lucene/LARQ indexing in the web app
  * Has methods for creating and accessing indexes
  *
  * @author: mha
  * Date: 13.mar.2008
  */
 public class IndexService {
 
   private static Logger logger = Logger.getLogger(IndexService.class);
   private DefaultSparulDispatcher sparulDispatcher = new DefaultSparulDispatcher();
   private SearchService searchService = new SearchService();
 
   /**
    * Method to create an index based on the internal content
    */
   public void createInternalResourcesMemoryIndex() {
 
     DatabaseService myDbService = new DatabaseService();
     IDBConnection connection = myDbService.getConnection();
     ResultSet resultSet;
     IndexBuilderString larqBuilder;
 
     logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Created database connection " + connection.getDatabaseType());
 
     // -- Read and index all literal strings.
     File indexDir = new File(SettingsService.getProperty("sublima.index.directory"));
     logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Read and index all literal strings");
     if ("memory".equals(SettingsService.getProperty("sublima.index.type"))) {
       larqBuilder = new IndexBuilderString();
     } else {
       larqBuilder = new IndexBuilderString(indexDir);
     }
 
     // Insert all literals to be indexed
     ArrayList<String> list = getFreetextToIndex(SettingsService.getProperty("sublima.searchfields").split(";"), SettingsService.getProperty("sublima.prefixes").split(";"));
 
     int steps = 500;
     int partsOfArray = steps;
     int j = 0;
 
     StringBuffer deleteString = new StringBuffer();
     deleteString.append("PREFIX sub: <http://xmlns.computas.com/sublima#>\n");
     deleteString.append("DELETE { ?s sub:literals ?o . }\n");
     deleteString.append("WHERE { ?s sub:literals ?o . }\n");
     boolean deleteSuccess = sparulDispatcher.query(deleteString.toString());
     logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Delete existing literals: " + deleteSuccess);
 
     for (int i = 0; i <= list.size(); i++) {
       StringBuffer insertString = new StringBuffer();
       insertString.append("PREFIX sub: <http://xmlns.computas.com/sublima#>\n");
       insertString.append("INSERT DATA {\n");
 
       while (j < partsOfArray) {
         insertString.append(list.get(j).toString() + "\n");
         j++;
       }
 
       insertString.append("}\n");
 
       boolean insertSuccess = sparulDispatcher.query(insertString.toString());
       logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Insert literal from index " + i + " to index " + partsOfArray + ": " + insertSuccess);
 
       j = partsOfArray;
       i = partsOfArray;
 
       if (partsOfArray > list.size() || (partsOfArray + steps) > list.size()) {
         partsOfArray = list.size();
       } else {
         partsOfArray += steps;
       }
     }
 
 
     System.out.println("List size: " + list.size());
 
     //IndexBuilderSubject larqBuilder = new IndexBuilderSubject();
 
     //Create a model based on the one in the DB
     try {
       ModelRDB model = ModelRDB.open(connection);
       // -- Create an index based on existing statements
       larqBuilder.indexStatements(model.listStatements());
 
       logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Indexed all model statements");
       // -- Finish indexing
       larqBuilder.closeWriter();
       logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Closed index for writing");
       // -- Create the access index
       IndexLARQ index = larqBuilder.getIndex();
       model.close();
 
       // -- Make globally available
       LARQ.setDefaultIndex(index);
       logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Index now globally available");
     }
     catch (DoesNotExistException e) {
       logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - NO CONTENT IN DATABASE. Please fill DB from Admin/Database and restart Tomcat.");
     }
 
     logger.info("SUBLIMA: createInternalResourcesMemoryIndex() --> Indexing - Created RDF model from database");
     try {
       connection.close();
     } catch (SQLException e) {
       e.printStackTrace();
     }
   }
 
 
   /**
    * Method to extract all URLs of the resources in the model
    *
    * @return ResultSet containing all resource URLS from the model
    */
   // todo Use SparqlDispatcher (needs to return ResultSet)
   private ResultSet getAllExternalResourcesURLs() {
     DatabaseService myDbService = new DatabaseService();
     IDBConnection connection = myDbService.getConnection();
     ModelRDB model = ModelRDB.open(connection);
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "SELECT ?url",
             "WHERE {",
             "        ?url dct:title ?title }"});
 
     Query query = QueryFactory.create(queryString);
     QueryExecution qExec = QueryExecutionFactory.create(query, model);
     ResultSet resultSet = qExec.execSelect();
     //model.close();
 
     try {
       connection.close();
     } catch (SQLException e) {
       e.printStackTrace();
     }
 
     logger.info("SUBLIMA: getAllExternalResourcesURLs() --> Indexing - Fetched all resource URLs from the model");
     return resultSet;
   }
 
   /**
    * A method to validate all urls on the resources. Adds the URL to the list along with
    * the http code.
    *
    * @return A map containing the URL and its HTTP Code. In case of exceptions a String
    *         representation of the exception is used.
    */
   public void validateURLs() {
     ResultSet resultSet;
     resultSet = getAllExternalResourcesURLs();
     HashMap<String, HashMap<String, String>> urlCodeMap = new HashMap<String, HashMap<String, String>>();
 
     // For each URL, do a HTTP GET and check the HTTP code
     URL u = null;
     HashMap<String, String> result;
 
     while (resultSet.hasNext()) {
       String resultURL = resultSet.next().toString();
       String url = resultURL.substring(10, resultURL.length() - 3).trim();
 
       URLActions urlAction = new URLActions(url);
       try {
         urlAction.updateResourceStatus();
       } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
       }
       catch (PSQLException e) {
         logger.warn("SUBLIMA: validateURLs() --> Indexing - Could not index " + url
                 + " due to database malfunction, probably caused by invalid characters in resource.");
         e.printStackTrace();
       }
       catch (JenaException e) {
         logger.warn("SUBLIMA: validateURLs() --> Indexing - Could not index " + url
                 + " due to backend storage malfunction, probably caused by invalid characters in resource.");
         e.printStackTrace();
       }
     }
   }
 
   public String getQueryForIndex(String[] fieldsToIndex, String[] prefixes) {
     Form2SparqlService form2SparqlService = new Form2SparqlService(prefixes);
     return getTheIndexQuery(fieldsToIndex, form2SparqlService);
   }
 
   public String getQueryForIndex(String[] fieldsToIndex, String[] prefixes, String resource) {
     Form2SparqlService form2SparqlService = new Form2SparqlService(prefixes);
     form2SparqlService.setResourceSubject(resource);
     return getTheIndexQuery(fieldsToIndex, form2SparqlService);
   }
 
   private String getTheIndexQuery(String[] fieldsToIndex, Form2SparqlService form2SparqlService) {
     StringBuffer queryBuffer = new StringBuffer();
     queryBuffer.append(form2SparqlService.getPrefixString());
     queryBuffer.append("SELECT");
     if (form2SparqlService.getResourceSubject().equals("?resource")) {
       queryBuffer.append(" ?resource");
     }
     for (int i = 1; i <= fieldsToIndex.length; i++) {
       queryBuffer.append(" ?object");
       queryBuffer.append(i);
     }
     queryBuffer.append(" WHERE {");
     ArrayList nullValues = new ArrayList<String>();
     for (String field : fieldsToIndex) {
       nullValues.add(null);
       queryBuffer.append(form2SparqlService.convertFormField2N3(field,
               (String[]) nullValues.toArray(new String[nullValues.size()]) // Don't ask me why
       ));
     }
     queryBuffer.append("\n}");
 
     return queryBuffer.toString();
   }
 
 
   public String getFreetextToIndex(String[] fieldsToIndex, String[] prefixes, String resource) {
     String queryString = getQueryForIndex(fieldsToIndex, prefixes, resource);
     ResultSet resultSet = getFreetextToIndexResultSet(queryString);
     StringBuffer resultBuffer = new StringBuffer();
     Set literals = new HashSet<String>();
 
     while (resultSet.hasNext()) {
       QuerySolution soln = resultSet.nextSolution();
       Iterator<String> it = soln.varNames();
       while (it.hasNext()) {
         String var = it.next();
         if (soln.get(var).isLiteral()) {
           Literal l = soln.getLiteral(var);
           String literal = l.getString().replace("\\", "\\\\");
           literals.add(literal);  // This should ensure uniqueness
         } else {
           logger.warn("SUBLIMA: Indexing - variable " + var + " contained no literal. Verify that sublima.searchfields config is correct.");
         }
       }
     }
     resultBuffer.append(resource);
     resultBuffer.append(" sub:literals \"\"\"");
     resultBuffer.append(literals.toString());
     resultBuffer.append("\"\"\" .\n");
 
     return resultBuffer.toString();
   }
 
 
   public ArrayList<String> getFreetextToIndex(String[] fieldsToIndex, String[] prefixes) {
     String queryString = getQueryForIndex(fieldsToIndex, prefixes);
     ResultSet resultSet = getFreetextToIndexResultSet(queryString);
     ArrayList<String> list = new ArrayList<String>();

     Set<String> literals = new HashSet<String>();
     boolean indexExternalContent = Boolean.valueOf(SettingsService.getProperty("sublima.checkurl.onstartup"));
     if (indexExternalContent) {
       StringBuffer deleteString = new StringBuffer();
       deleteString.append("PREFIX sub: <http://xmlns.computas.com/sublima#>\n");
       deleteString.append("DELETE { ?s sub:externaliterals ?o . }\n");
       deleteString.append("WHERE { ?s sub:externaliterals ?o . }\n");
       boolean deleteSuccess = sparulDispatcher.query(deleteString.toString());
       logger.info("SUBLIMA: getFreetextToIndex() --> Delete external literals: " + deleteSuccess);
     }
     String resource = null;
     while (resultSet.hasNext()) {
       QuerySolution soln = resultSet.nextSolution();
       Iterator<String> it = soln.varNames();
       while (it.hasNext()) {
        StringBuffer resultBuffer = new StringBuffer();
         String var = it.next();
         if (soln.get(var).isResource()) {
           Resource r = soln.getResource(var);
           if (!r.getURI().equals(resource)) { // So, we have a new Resource and we get the external content if the checkurl.onstartup paramtere is true
 
             if (indexExternalContent && (resource != null)) {
               StringBuffer insertString = new StringBuffer();
               insertString.append("PREFIX sub: <http://xmlns.computas.com/sublima#>\n");
               insertString.append("INSERT DATA {\n");
 
               URLActions urlAction = new URLActions(resource);
               String code = urlAction.getCode();
 
               if ("302".equals(code) ||
                       "303".equals(code) ||
                       "304".equals(code) ||
                       "305".equals(code) ||
                       "307".equals(code) ||
                       code.startsWith("2")) {
                 try {
                   insertString.append("<" + resource + "> sub:externaliterals \"\"\"");
                   for (String s : literals) {
                     insertString.append(s);
                   }
                   HashMap<String, String> headers = urlAction.getHTTPmap();
                   String contentType = headers.get("httph:content-type");
 
                   if ("application/xhtml+xml".equalsIgnoreCase(contentType) ||
                           "text/html".equalsIgnoreCase(contentType) ||
                           "text/plain".equalsIgnoreCase(contentType) ||
                           "text/xml".equalsIgnoreCase(contentType)) {
                     insertString.append("\n" + urlAction.strippedContent(null).replace("\\", "\\\\") + "\"\"\" .\n");
 
                     insertString.append("}\n");
 
                     boolean insertSuccess = sparulDispatcher.query(insertString.toString());
                     logger.info("SUBLIMA: getFreetextToIndex() --> Insert external literals: " + insertSuccess);
                   }
                 } catch (UnsupportedEncodingException e) {
                   logger.warn("SUBLIMA: Indexing external content gave UnsupportedEncodingException for resource " + resource);
                 }
               }
             }
 
             // Add the old one to the output buffer
             resultBuffer.append("<" + resource);
             resultBuffer.append("> sub:literals \"\"\"");
             for (String s : literals) {
               resultBuffer.append(s);
             }
             resultBuffer.append("\"\"\" .\n");
 
             //list.add("<" + resource + "> sub:literals \"\"\"" + literals.toString() + "\"\"\" .");
             list.add(resultBuffer.toString());
 
             // Reset to the new resource
             resource = r.getURI();
             literals.clear();
           }
         } else if (soln.get(var).isLiteral()) {
           Literal l = soln.getLiteral(var);
           String literal = l.getString().replace("\\", "\\\\");
           literals.add(literal);  // This should ensure uniqueness
         } else {
           logger.warn("SUBLIMA: Indexing - variable " + var + " contained neither the resource name or a literal. Verify that sublima.searchfields config is correct.");
         }
         if (!resultSet.hasNext()) {
           resultBuffer.append("<" + resource);
           resultBuffer.append("> sub:literals \"\"\"");
           for (String s : literals) {
             resultBuffer.append(s);
           }
           resultBuffer.append("\"\"\" .\n");
           //list.add("<" + resource + "> sub:literals \"\"\"" + literals.toString() + "\"\"\" .");
           list.add(resultBuffer.toString());
         }
       }
     }
     //return resultBuffer.toString();
     return list;
   }
 
   private ResultSet getFreetextToIndexResultSet(String queryString) {
     DatabaseService myDbService = new DatabaseService();
     IDBConnection connection = myDbService.getConnection();
     ModelRDB model = ModelRDB.open(connection);
 
     Query query = QueryFactory.create(queryString);
     QueryExecution qExec = QueryExecutionFactory.create(query, model);
     ResultSet resultSet = qExec.execSelect();
     //model.close();
 
     try {
       connection.close();
     } catch (SQLException e) {
       e.printStackTrace();
     }
 
     logger.info("SUBLIMA: getFreetextToIndex() --> Indexing - Fetched all literals that we need to index");
     return resultSet;
   }
 }
