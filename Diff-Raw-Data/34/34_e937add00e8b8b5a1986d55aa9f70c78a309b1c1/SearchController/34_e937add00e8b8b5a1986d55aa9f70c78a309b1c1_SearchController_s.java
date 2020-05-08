 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.service.SearchService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.auth.ApplicationManager;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 public class SearchController implements StatelessAppleController {
   private SparqlDispatcher sparqlDispatcher;
   private ApplicationManager appMan;
   private String mode;
 
   private static Logger logger = Logger.getLogger(SearchController.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
     boolean loggedIn = appMan.isLoggedIn("Sublima");
 
     this.mode = req.getSitemapParameter("mode");
 
     logger.trace("SearchController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
 
     // The initial advanced search page
     if ("advancedsearch".equalsIgnoreCase(mode)) {
       res.sendPage("xhtml/search-form", null);
       return;
     }
 
     // If it's search-results for advanced search, topic instance or resource
     if ("resource".equalsIgnoreCase(mode) || "search-result".equalsIgnoreCase(mode)) {
       doAdvancedSearch(res, req, loggedIn);
       return;
     }
 
     if ("topic".equalsIgnoreCase(mode)) {
       doGetTopic(res, req, loggedIn);
       return;
     }
   }
 
   private void doGetTopic(AppleResponse res, AppleRequest req, boolean loggedIn) {
 
     String subject = "<" + getProperty("sublima.base.url")
             + "topic/" + req.getSitemapParameter("topic") + ">";
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                     "DESCRIBE ?resource " + subject + " ?publisher ?subjects ?rest",
             "WHERE {",
             "        ?resource dct:language ?lang;",
             "				 dct:publisher ?publisher ;",
             "                dct:subject " + subject + ", ?subjects ;",
             "                ?p ?rest .}"});
 
     logger.trace("doGetTopic: SPARQL query sent to dispatcher: " + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("result-list", queryResult);
 
    // This query, which bears its name for historical reasons, relies
    // on that all relations are explicitly stated. I.e. a triple for
    // both skos:broader and skos:narrower must exist.
/*
    String sparqlConstructQuery =
            "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "prefix skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                    "DESCRIBE " + subject + " ?therelation ?object WHERE {\n" +
                    "  OPTIONAL {\n" +
                    "    ?therelation rdfs:subPropertyOf skos:semanticRelation .\n    " +
                    subject + " ?therelation ?object ;\n" +
                    "            a skos:Concept .\n" +
                    "    ?object a skos:Concept .\n" +
                    "  }\n}";
*/


String sparqlConstructQuery =
         "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
         "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n"+
         "CONSTRUCT {\n"+
          subject + "  ?semrelation ?object ; \n"+
         "skos:prefLabel ?preflabel ; \n"+
         "skos:altLabel ?altlabel ;  \n"+     
         "a skos:Concept .\n"+
         "?semrelation rdfs:subPropertyOf skos:semanticRelation ;\n"+
        "rdfs:label ?semrellabel .\n"+
         "?object skos:prefLabel ?preflabel2 ; \n"+
         " a skos:Concept .\n"+
         "}\n"+
         "WHERE {\n"+
         "OPTIONAL {\n"+
          subject + " ?semrelation ?object ;\n"+
          "   a skos:Concept ;\n"+
          "   skos:prefLabel ?preflabel ;\n"+
          "   skos:altLabel ?altlabel .\n"+
          "?semrelation rdfs:subPropertyOf skos:semanticRelation ;\n"+
         "                rdfs:label ?semrellabel .\n"+
          "?object  a skos:Concept ;\n"+
          "     skos:prefLabel ?preflabel2 .\n"+
          "}\n"+ 
         "}"
 
 
 
 
 
     logger.trace("doGetTopic: SPARQL CONSTUCT query sent to dispatcher: " + sparqlConstructQuery);
     queryResult = sparqlDispatcher.query(sparqlConstructQuery);
 
     bizData.put("navigation", queryResult);
     bizData.put("mode", mode);
 
     // This is such a 1999 way of doing things. There should be a generic SAX events generator
     // or something that would serialise this data structure automatically in a one-liner,
     // but I couldn't find it. Arguably a TODO.
     StringBuffer params = new StringBuffer();
     params.append("  <request>\n");
     params.append("\n    <param key=\"prefix\">");
     params.append("\n      <value>dct: &lt;http://purl.org/dc/terms/&gt;</value>");
     params.append("\n      <value>rdfs: &lt;http://www.w3.org/2000/01/rdf-schema%23&gt;</value>");
     params.append("\n      <value>skos: &lt;http://www.w3.org/2004/02/skos/core%23&gt;</value>");
     params.append("\n    </param>");
     params.append("\n  </request>\n");
 
     bizData.put("request", params.toString());
     bizData.put("loggedin", loggedIn);
     bizData.put("searchparams", "<empty/>");
     res.sendPage("xml/sparql-result", bizData);
   }
 
 
   private String freeTextSearchString(AppleResponse res, AppleRequest req) {
     String defaultBooleanOperator = getProperty("sublima.default.boolean.operator");
     String chosenOperator = req.getCocoonRequest().getParameter("booleanoperator");
 
     SearchService searchService;
 
     //Use user chosen boolean operator when it doesn't equal the default
     if (!chosenOperator.equalsIgnoreCase(defaultBooleanOperator)) {
       searchService = new SearchService(chosenOperator);
       logger.debug("SUBLIMA: Use " + chosenOperator + " as boolean operator for search");
     } else {
       searchService = new SearchService(defaultBooleanOperator);
       logger.debug("SUBLIMA: Use " + defaultBooleanOperator + " as boolean operator for search");
     }
 
     return searchService.buildSearchString(req.getCocoonRequest().getParameter("searchstring"));
   }
 
 
   public void doAdvancedSearch(AppleResponse res, AppleRequest req, boolean loggedIn) {
     // Get all parameteres from the HTML form as Map
     Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     // Boolean that can be set to false if we don't want the actual search to be performed. Typically when the search string is empty.
     boolean doSearch = true;
 
     // Temporary to override the sparql query upopn freetext search
     boolean freetext = false;
     String searchStringOverriden = null;
 
     // Create an XML structure of the search criterias. This could probably be nore generic.
     StringBuffer xmlSearchParametersBuffer = new StringBuffer();
     xmlSearchParametersBuffer.append("<c:searchparams xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     xmlSearchParametersBuffer.append("\t<c:searchstring>" + req.getCocoonRequest().getParameter("searchstring") + "</c:searchstring>\n");
     xmlSearchParametersBuffer.append("\t<c:operator>" + req.getCocoonRequest().getParameter("booleanoperator") + "</c:operator>\n");
     xmlSearchParametersBuffer.append("\t<c:deepsearch>" + req.getCocoonRequest().getParameter("deepsearch") + "</c:deepsearch>\n");
     xmlSearchParametersBuffer.append("\t<c:sortby>" + req.getCocoonRequest().getParameter("sort") + "</c:sortby>\n");
     xmlSearchParametersBuffer.append("</c:searchparams>\n");
 
 
     if ("resource".equalsIgnoreCase(mode)) {
       parameterMap.put("prefix", new String[]{"dct: <http://purl.org/dc/terms/>", "rdfs: <http://www.w3.org/2000/01/rdf-schema#>", "skos: <http://www.w3.org/2004/02/skos/core#>"});
       parameterMap.put("interface-language", new String[]{req.getSitemapParameter("interface-language")});
       parameterMap.put("dct:identifier", new String[]{getProperty("sublima.base.url") + "resource/"
               + req.getSitemapParameter("name")});
       parameterMap.put("dct:subject/skos:prefLabel", new String[]{""});
     }
 
     if (parameterMap.get("searchstring") != null) {
       if (req.getCocoonRequest().getParameter("searchstring").trim().equalsIgnoreCase("")) {
         doSearch = false;
       } else {
         // When true, we override the searchstring later in the code
         freetext = true;
         searchStringOverriden = freeTextSearchString(res, req);
         parameterMap.put("searchstring", new String[]{freeTextSearchString(res, req)});
         parameterMap.put("prefix", new String[]{"dct: <http://purl.org/dc/terms/>",
                 "rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                 "pf: <http://jena.hpl.hp.com/ARQ/property#>", "skos: <http://www.w3.org/2004/02/skos/core#>"});
         parameterMap.remove("booleanoperator");
         parameterMap.remove("sort");
       }
     }
     // sending the result
     String sparqlQuery = null;
     // Check for magic prefixes
     if (parameterMap.get("prefix") != null) {
       // Calls the Form2SPARQL service with the parameterMap which returns
       // a SPARQL as String
       Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"), parameterMap.get("freetext-field"));
       parameterMap.remove("prefix"); // The prefixes are magic variables
       parameterMap.remove("freetext-field"); // The freetext-fields are magic variables too
       sparqlQuery = form2SparqlService.convertForm2Sparql(parameterMap);
     } else {
       res.sendStatus(400);
     }
     /*
     if (freetext) {
       sparqlQuery =
               "PREFIX dct: <http://purl.org/dc/terms/>\n" +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
               "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n" +
               "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
               "PREFIX sub: <http://xmlns.computas.com/subima#>\n" +
               "CONSTRUCT {\n" +
               "  ?subject ?px2 ?ox2 .\n" +
               "  ?publisher ?px3 ?ox3 .\n" +
               "  ?resource ?px1 ?ox1 .\n" +
               "  ?resource sub:score ?score . \n" +
               "  ?ox1 ?px4 ?ox4 .\n" +
               "}\n" +
               "\n" +
               "\n" +
               "WHERE {\n" +
               "  (?lit ?score) pf:textMatch '" + searchStringOverriden + "' .\n" +
               "  {\n" +
               "    ?resource ?p1 ?lit;\n" +
               "              dct:subject ?subject ;\n" +
               "              dct:publisher ?publisher .\n" +
               "  }\n" +
               "  UNION\n" +
               "  {\n" +
               "      ?resource dct:subject ?subject1 .\n" +
               "      ?subject1 ?p2 ?lit .\n" +
               "      ?resource dct:subject ?subject ;\n" +
               "                dct:publisher ?publisher .\n" +
               "  }\n" +
               "  UNION\n" +
               "  {\n" +
               "      ?resource dct:publisher ?publisher1 .\n" +
               "      ?publisher1 ?p3 ?lit .\n" +
               "      ?resource dct:subject ?subject ;\n" +
               "                dct:publisher ?publisher .\n" +
               "  }\n" +
               "?resource ?px1 ?ox1 .\n" +
               "?ox1 ?px4 ?ox4 .\n" +
               "?subject ?px2 ?ox2 .\n" +
               "?publisher ?px3 ?ox3 .\n" +
               "}";
     }*/
 
     logger.trace("doAdvancedSearch: SPARQL query sent to dispatcher:\n" + sparqlQuery);
 
     Object queryResult;
     if (doSearch) {
       queryResult = sparqlDispatcher.query(sparqlQuery);
     }
     else {
       queryResult = "<empty/>";
     }
 
 
 
     bizData.put("result-list", queryResult);
     bizData.put("navigation", "<empty></empty>");
     bizData.put("mode", mode);
 
     bizData.put("searchparams", xmlSearchParametersBuffer.toString());
 
     // This is such a 1999 way of doing things. There should be a generic SAX events generator 
     // or something that would serialise this data structure automatically in a one-liner, 
     // but I couldn't find it. Arguably a TODO.
     StringBuffer params = new StringBuffer();
     params.append("  <request>\n");
     for (Enumeration keys = req.getCocoonRequest().getParameterNames(); keys.hasMoreElements();) {
       String key = keys.nextElement().toString();
       params.append("\n    <param key=\"" + key + "\">");
       String[] values = req.getCocoonRequest().getParameterValues(key);
       for (String value : values) {
         value = value.replaceAll("<", "&lt;");
         value = value.replaceAll(">", "&gt;");
         value = value.replaceAll("#", "%23"); // A hack to get the hash alive through a clickable URL
         params.append("\n      <value>" + value + "</value>");
       }
       params.append("\n    </param>");
     }
     params.append("\n  </request>\n");
 
     bizData.put("request", params.toString());
     bizData.put("loggedin", loggedIn);
     res.sendPage("xml/sparql-result", bizData);
   }
 
   private Map<String, String[]> createParametersMap(Request request) {
     Map<String, String[]> result = new HashMap<String, String[]>();
     Enumeration parameterNames = request.getParameterNames();
     while (parameterNames.hasMoreElements()) {
       String paramName = (String) parameterNames.nextElement();
       result.put(paramName, request.getParameterValues(paramName));
     }
     return result;
   }
 
   public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
     this.sparqlDispatcher = sparqlDispatcher;
   }
 
   public void setAppMan(ApplicationManager appMan) {
     this.appMan = appMan;
   }
 }
