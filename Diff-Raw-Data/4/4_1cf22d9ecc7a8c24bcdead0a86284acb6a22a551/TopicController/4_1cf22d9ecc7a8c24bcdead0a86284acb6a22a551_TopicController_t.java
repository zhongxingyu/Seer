 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.app.service.*;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.query.larq.LARQ;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.auth.ApplicationManager;
 import org.apache.cocoon.auth.ApplicationUtil;
 import org.apache.cocoon.auth.User;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class TopicController implements StatelessAppleController {
 
   private SparulDispatcher sparulDispatcher;
   private IndexService indexService = new IndexService();
   private AdminService adminService = new AdminService();
   private ApplicationManager appMan;
   private ApplicationUtil appUtil = new ApplicationUtil();
   private User user;
   private String mode;
   private String submode;
   private String userPrivileges = "<empty/>";
   private boolean loggedIn = false;
   String[] completePrefixArray = {"PREFIX rdf: 		<http://www.w3.org/1999/02/22-rdf-syntax-ns#>", "PREFIX rdfs: 		<http://www.w3.org/2000/01/rdf-schema#>", "PREFIX owl: 		<http://www.w3.org/2002/07/owl#>", "PREFIX foaf: 		<http://xmlns.com/foaf/0.1/>", "PREFIX lingvoj: 	<http://www.lingvoj.org/ontology#>", "PREFIX dcmitype: 	<http://purl.org/dc/dcmitype/>", "PREFIX dct: 		<http://purl.org/dc/terms/>", "PREFIX sub: 		<http://xmlns.computas.com/sublima#>", "PREFIX wdr: 		<http://www.w3.org/2007/05/powder#>", "PREFIX sioc: 		<http://rdfs.org/sioc/ns#>", "PREFIX xsd: 		<http://www.w3.org/2001/XMLSchema#>", "PREFIX topic: 		<topic/>", "PREFIX skos:		<http://www.w3.org/2004/02/skos/core#>"};
 
   String completePrefixes = StringUtils.join("\n", completePrefixArray);
 
  private static Logger logger = Logger.getLogger(TopicController.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
     this.mode = req.getSitemapParameter("mode");
     this.submode = req.getSitemapParameter("submode");
     loggedIn = appMan.isLoggedIn("Sublima");
 
     if (appUtil.getUser() != null) {
       user = appUtil.getUser();
       userPrivileges = adminService.getRolePrivilegesAsXML(user.getAttribute("role").toString());
     }
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     logger.trace("TopicController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
     logger.trace("TopicController: Language from service is " + language);
 
     if ("emner".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
 
         res.sendPage("xml2/emner", bizData);
       } else if ("nytt".equalsIgnoreCase(submode)) {
         editTopic(res, req, "nytt", null);
       } else if ("alle".equalsIgnoreCase(submode)) {
         showTopics(res, req);
       } else if ("emne".equalsIgnoreCase(submode)) {
         editTopic(res, req, "edit", null);
       } else if ("koble".equalsIgnoreCase(submode)) {
         mergeTopics(res, req);
       } else if ("tema".equalsIgnoreCase(submode)) {
         setThemeTopics(res, req);
       } else {
         res.sendStatus(404);
       }
     } else if ("browse".equalsIgnoreCase(mode)) {
       showTopicBrowsing(res, req);
     } else if ("relasjoner".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
 
         res.sendPage("xml2/relasjoner", bizData);
       } else if ("relasjon".equalsIgnoreCase(submode)) {
         if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) { // show relation
           showRelation(req, res, null);
         } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
           if (req.getCocoonRequest().getParameter("actionbuttondelete") != null) { // delete relation
             deleteRelation(req);
           } else {
             editRelation(res, req, null); // edit relation
           }
         }
       } else if ("alle".equalsIgnoreCase(submode)) {
         showRelations(res, req);
       }
     } else if ("a-z".equalsIgnoreCase(mode)) {
       getTopicsByLetter(res, req, submode);
     }
   }
 
   private void deleteRelation(AppleRequest req) {
     StringBuilder messageBuffer = new StringBuilder();
     String uri = req.getCocoonRequest().getParameter("the-resource");
     StringBuilder deleteQueries = new StringBuilder();
 
     deleteQueries.append("DELETE {\n  ?relasjon ?p <" + uri + "> .\n }\n WHERE {\n  ?relasjon ?p <" + uri + "> .\n }");
     deleteQueries.append("DELETE {\n  ?emne <" + uri + "> ?o .\n }\n WHERE {\n  ?emne <" + uri + "> ?o .\n }");
     deleteQueries.append("DELETE {\n  <" + uri + "> ?q ?r .\n }\n WHERE {\n  <" + uri + "> ?q ?r .\n }");
 
     boolean deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
     logger.trace("ResourceController.editResource --> DELETE RELATION QUERY RESULT: " + deleteSuccess);
 
     if (deleteSuccess) {
       messageBuffer.append("<c:message><i18n:text key=\"relation.deleted.ok\">Relasjon slettet!</i18n:text></c:message>\n");
     } else {
       messageBuffer.append("<c:message><i18n:text key=\"relation.deleted.error\">Feil ved sletting av relasjon</i18n:text></c:message>\n");
     }
 
   }
 
   private void showRelation(AppleRequest req, AppleResponse res, StringBuilder existingMessages) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
     if (existingMessages != null) {
       messageBuffer.append(existingMessages);
     }
     messageBuffer.append("</c:messages>\n");
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     String uri = req.getCocoonRequest().getParameter("the-resource");
     bizData.put("tempvalues", "<empty></empty>");
 
     if ("".equalsIgnoreCase(uri) || uri == null) {
       bizData.put("relationdetails", "<empty></empty>");
     } else {
       bizData.put("relationdetails", adminService.getRelationByURI(uri));
     }
 
     bizData.put("allanguages", adminService.getAllLanguages());
     bizData.put("userprivileges", userPrivileges);
     bizData.put("allrelations", adminService.getAllRelationTypes());
     bizData.put("mode", "topicrelated");
     bizData.put("messages", messageBuffer.toString());
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml2/relasjon", bizData);
   }
 
   /**
    * Method to get all topics starting with the given letter(s).
    * Used in the A-Z topic browsing
    *
    * @param res
    * @param req
    * @param letter
    */
   private void getTopicsByLetter(AppleResponse res, AppleRequest req, String letter) {
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     bizData.put("themetopics", adminService.getTopicsByLetter(letter));
     bizData.put("mode", "browse");
     bizData.put("letter", letter);
     bizData.put("loggedin", "<empty></empty>");
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml/browse", bizData);
   }
 
   private void editRelation(AppleResponse res, AppleRequest req, Object o) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
     String validationmessages = validateRelation(req);
 
     if (!validationmessages.isEmpty()) {
       showRelation(req, res, new StringBuilder(validationmessages));
     } else {
       Map<String, Object> bizData = new HashMap<String, Object>();
       String uri = req.getCocoonRequest().getParameter("the-resource");
 
       bizData.put("allanguages", adminService.getAllLanguages());
       bizData.put("userprivileges", userPrivileges);
       bizData.put("allrelations", adminService.getAllRelationTypes());
 
       Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
       parameterMap.remove("actionbutton");
       Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
       parameterMap.remove("prefix"); // The prefixes are magic variables
       if (parameterMap.get("subjecturi-prefix") != null) {
         parameterMap.put("subjecturi-prefix", new String[]{getProperty("sublima.base.url") +
                 parameterMap.get("subjecturi-prefix")[0]});
       }
       boolean createInverse = false;
       String sparqlQuery = null;
 
       String relationtype = req.getCocoonRequest().getParameter("relationtype");
       parameterMap.remove("relationtype");
 
       // Do the relation related thingies here
       if (relationtype.equals("inverse")) {
         parameterMap.remove("a");
         parameterMap.put("a", new String[]{"http://www.w3.org/2002/07/owl#ObjectProperty"});
         // We also have to tell the inverse relation that it's inverse of the new one
         // This have to be done after we have a URI for the new relation, so we flag this with a boolean
         createInverse = true;
       } else if (relationtype.equals("symmetric")) {
         parameterMap.remove("a");
         parameterMap.put("a", new String[]{"http://www.w3.org/2002/07/owl#SymmetricProperty"});
         parameterMap.remove("owl:inverseOf");
       } else if (relationtype.equals("oneway")) {
         parameterMap.remove("a");
         parameterMap.put("a", new String[]{"http://www.w3.org/2002/07/owl#ObjectProperty"});
         parameterMap.remove("owl:inverseOf");
       }
 
       if (uri != null && !uri.isEmpty()) { // Hvis endring og ikke ny
         StringBuilder deleteQueries = new StringBuilder();
         String inverseUri = adminService.getInverseRelationUriIfAny(uri);
         boolean deleteSuccess = false;
 
         if (inverseUri.isEmpty()) {                                   // Var invers relasjon
           if (relationtype.equals("oneway")) {                        // og har blitt enkel
 
             deleteQueries.append("DELETE {\n  ?relasjon ?p <" + uri + "> .\n }\n WHERE {\n  ?relasjon ?p <" + uri + "> .\n }");
             deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
           } else if (relationtype.equals("symmetric")) {              // og har blitt symmetrisk
 
             deleteQueries.append("DELETE {\n  ?relasjon ?p <" + uri + "> .\n }\n WHERE {\n  ?relasjon ?p <" + uri + "> .\n }");
             deleteQueries.append("INSERT {\n  ?emne <" + uri + "> ?o .\n }\n WHERE {\n  ?o <" + uri + "> ?emne .\n }");
             deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
           }
         } else if (adminService.isSymmetricProperty(uri)) {           // Var symmetrisk relasjon
           if (relationtype.equals("oneway")) {                        // og har blitt enkel
 
             deleteQueries.append("DELETE {\n  ?o <" + uri + "> ?emne .\n }\n WHERE {\n  ?emne <" + uri + "> ?o .\n }");
             deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
           } else if (relationtype.equals("inverse")) {                // og har blitt invers
 
             deleteQueries.append("DELETE {\n  ?o <" + uri + "> ?emne .\n }\n WHERE {\n  ?emne <" + uri + "> ?o .\n }");
             deleteQueries.append("INSERT {\n  ?emne <" + parameterMap.get("owl:inverseOf")[0] + "> ?o .\n }\n WHERE {\n  ?o <" + uri + "> ?emne .\n }");
             deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
           }
         } else {                                                      // Var enkel relasjon
           if (relationtype.equals("symmetric")) {                     // og har blitt symmetrisk
 
             deleteQueries.append("INSERT {\n  ?emne <" + uri + "> ?o .\n }\n WHERE {\n  ?o <" + uri + "> ?emne .\n }");
             deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
           } else if (relationtype.equals("inverse")) {                // og har blitt invers
 
             deleteQueries.append("INSERT {\n  ?o <" + parameterMap.get("owl:inverseOf")[0] + "> ?emne .\n }\n WHERE {\n  ?emne <" + uri + "> ?o .\n }");
             deleteSuccess = sparulDispatcher.query(deleteQueries.toString());
 
           }
         }
       }
 
 
       try {
         sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
       }
       catch (IOException e) {
         messageBuffer.append("<c:message><i18n:text key=\"topic.relation.saveerror\">Feil ved lagring av ny relasjonstype</i18n:text></c:message>\n");
       }
 
       if (createInverse) {
         StringBuilder inverse = new StringBuilder();
         inverse.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>\n");
         inverse.append("INSERT\n{\n");
         inverse.append("<" + parameterMap.get("owl:inverseOf")[0] + "> owl:inverseOf <" + form2SparqlService.getURI() + "> .\n}");
         boolean success = sparulDispatcher.query(inverse.toString());
         logger.trace("TopicController.editRelation --> INSERT INVERSE RELATION: " + success);
       }
 
       logger.trace("TopicController.editRelation --> Executing query.\n");
 
       boolean insertSuccess = sparulDispatcher.query(sparqlQuery);
 
       logger.trace("TopicController.editRelation --> QUERY RESULT: " + insertSuccess);
 
       if (insertSuccess) {
         messageBuffer.append("<c:message><i18n:text key=\"topic.relation.saved\">Ny relasjonstype lagret</i18n:text></c:message>\n");
 
         bizData.put("relationdetails", adminService.getRelationByURI(form2SparqlService.getURI()));
 
       } else {
         messageBuffer.append("<c:message><i18n:text key=\"topic.relation.saveerror\">Feil ved lagring av ny relasjonstype</i18n:text></c:message>\n");
         bizData.put("relationdetails", "<empty></empty>");
       }
 
 
       bizData.put("mode", "topicrelated");
 
       if (insertSuccess) {
         bizData.put("tempvalues", "<empty></empty>");
       } else {
         bizData.put("tempvalues", "<empty></empty>");
       }
 
       messageBuffer.append("</c:messages>\n");
 
       bizData.put("messages", messageBuffer.toString());
       bizData.put("facets", getRequestXML(req));
 
       res.sendPage("xml2/relasjon", bizData);
     }
   }
 
   private String validateRelation(AppleRequest req) {
     StringBuilder validationMessages = new StringBuilder();
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("rdfs:label-1")) || req.getCocoonRequest().getParameter("rdfs:label-1") == null) {
       validationMessages.append("<c:message><i18n:text key=\"relation.validation.titleblank\">Relasjonens tittel kan ikke være blank</i18n:text></c:message>\n");
     }
 
     return validationMessages.toString();
   }
 
   private void showTopicBrowsing
           (AppleResponse
                   res, AppleRequest
                   req) {
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     String themeTopics = adminService.getThemeTopics();
     if (!themeTopics.contains("sub:theme")) {
       bizData.put("themetopics", "<empty></empty>");
     } else {
       bizData.put("themetopics", themeTopics);
     }
 
     bizData.put("mode", "browse");
     bizData.put("loggedin", loggedIn);
     bizData.put("letter", "");
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml/browse", bizData);
   }
 
   private void mergeTopics(AppleResponse res, AppleRequest req) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       bizData.put("messages", "<empty></empty>");
     } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
 
       // Create new URI for new topic.
       // Declare the new topic to be a union of the older
       // Mark the old as inactive.
 
 
       SearchService searchService = new SearchService();
 
       String uri = searchService.sanitizeStringForURI(req.getCocoonRequest().getParameter("skos:prefLabel"));
       uri = getProperty("sublima.base.url") + "topic/" + uri;
 
       String insertNewTopicString = completePrefixes + "\nINSERT\n{\n" + "<" + uri + "> a skos:Concept ;\n"
               + " skos:prefLabel \"" + req.getCocoonRequest().getParameter("skos:prefLabel") + "\"@no ;\n"
               + " wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> .\n"
               /*           + " owl:unionOf <" + StringUtils.join(">, <", req.getCocoonRequest().getParameterValues("skos:Concept")) + "> .\n"  */
               + "}";
 
       logger.trace("TopicController.mergeTopics --> INSERT NEW TOPIC QUERY:\n" + insertNewTopicString);
       boolean updateSuccess;
       updateSuccess = sparulDispatcher.query(insertNewTopicString);
 
       for (String oldurl : req.getCocoonRequest().getParameterValues("skos:Concept")) {
         String sparulQuery = "MODIFY\nDELETE { ?s ?p <" + oldurl + "> }\nINSERT { ?s ?p <" + uri + "> }\nWHERE { ?s ?p <" + oldurl + "> }\n";
         logger.trace("Changing " + oldurl + " to " + uri + " in objects.");
         updateSuccess = sparulDispatcher.query(sparulQuery);
         logger.debug("Object edit status: " + updateSuccess);
         sparulQuery = "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\nPREFIX status: <http://sublima.computas.com/status/>\n" + "" +
                 "MODIFY\nDELETE { <" + oldurl + "> wdr:describedBy ?status . }\nINSERT { <" + oldurl + "> wdr:describedBy status:inaktiv . }\nWHERE { <" + oldurl + "> wdr:describedBy ?status . }\n";
         logger.trace("Setting " + oldurl + " topics inactive.");
         updateSuccess = sparulDispatcher.query(sparulQuery);
         logger.debug("Topic inactive status: " + updateSuccess);
 
       }
 
       if (updateSuccess) {
         messageBuffer.append("<c:message><i18n:text key=\"topic.merged.ok\">Emnene er slått sammen</i18n:text></c:message>\n");
       } else {
         messageBuffer.append("<c:message><i18n:text key=\"topic.merged.failed\">En feil oppsto ved sammenslåing</i18n:text></c:message>\n");
       }
 
       messageBuffer.append("</c:messages>\n");
 
       bizData.put("messages", messageBuffer.toString());
 
     }
     bizData.put("tempvalues", "<empty></empty>");
     bizData.put("alltopics", adminService.getAllTopics());
     bizData.put("mode", "topicjoin");
     bizData.put("userprivileges", userPrivileges);
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml2/koble", bizData);
 
   }
 
   private void setThemeTopics(AppleResponse res, AppleRequest req) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       bizData.put("themetopics", adminService.getThemeTopics());
       bizData.put("tempvalues", "<empty></empty>");
       bizData.put("alltopics", adminService.getAllTopics());
       bizData.put("mode", "theme");
 
       bizData.put("userprivileges", userPrivileges);
       bizData.put("messages", "<empty></empty>");
       bizData.put("facets", getRequestXML(req));
 
       res.sendPage("xml2/tema", bizData);
 
     } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       Map<String, String[]> requestMap = createParametersMap(req.getCocoonRequest());
       requestMap.remove("actionbutton");
       requestMap.remove("locale");
 
       StringBuilder deleteString = new StringBuilder();
       StringBuilder whereString = new StringBuilder();
       StringBuilder insertString = new StringBuilder();
 
       deleteString.append(completePrefixes);
       deleteString.append("\nDELETE\n{\n");
       whereString.append("\nWHERE\n{\n");
       deleteString.append("?topic sub:theme ?theme .\n");
       deleteString.append("}\n");
       whereString.append("?topic sub:theme ?theme.\n");
       whereString.append("}\n");
 
       insertString.append(completePrefixes);
       insertString.append("\nINSERT\n{\n");
 
       for (String s : requestMap.keySet()) {
         for (String t : requestMap.get(s)) {
           insertString.append("<" + t + "> sub:theme \"true\"^^xsd:boolean .\n");
         }
       }
 
       insertString.append("}\n");
 
       deleteString.append(whereString.toString());
 
       logger.trace("TopicController.setThemeTopics --> DELETE QUERY:\n" + deleteString.toString());
       logger.trace("TopicController.setThemeTopics --> INSERT QUERY:\n" + insertString.toString());
 
       boolean deleteSuccess = sparulDispatcher.query(deleteString.toString());
       boolean insertSuccess = sparulDispatcher.query(insertString.toString());
 
       logger.trace("TopicController.setThemeTopics --> DELETE QUERY RESULT: " + deleteSuccess);
       logger.trace("TopicController.setThemeTopics --> INSERT QUERY RESULT: " + insertSuccess);
 
       if (deleteSuccess && insertSuccess) {
         messageBuffer.append("<c:message><i18n:text key=\"topic.theme.ok\">Emnene satt som temaemner</i18n:text></c:message>\n");
 
       } else {
         messageBuffer.append("<c:message><i18n:text key=\"topic.theme.error\">Feil ved merking av temaemner</i18n:text></c:message>\n");
         bizData.put("themetopics", "<empty></empty>");
       }
 
       if (deleteSuccess && insertSuccess) {
         bizData.put("themetopics", adminService.getThemeTopics());
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "theme");
         bizData.put("alltopics", adminService.getAllTopics());
       } else {
         bizData.put("themetopics", adminService.getThemeTopics());
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "theme");
         bizData.put("alltopics", adminService.getAllTopics());
       }
 
       bizData.put("userprivileges", userPrivileges);
       messageBuffer.append("</c:messages>\n");
 
       bizData.put("messages", messageBuffer.toString());
       bizData.put("facets", getRequestXML(req));
 
       res.sendPage("xml2/tema", bizData);
     }
   }
 
   private void showTopics(AppleResponse res, AppleRequest req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if (req.getCocoonRequest().getParameter("wdr:describedBy") != null && !"".equals(req.getCocoonRequest().getParameter("wdr:describedBy"))) {
       bizData.put("all_topics", adminService.getAllTopicsByStatus(req.getCocoonRequest().getParameter("wdr:describedBy")));
     } else {
       bizData.put("all_topics", adminService.getAllTopicsWithPrefAndAltLabel());
     }
 
     bizData.put("facets", getRequestXML(req));
     bizData.put("statuses", adminService.getDistinctAndUsedLabels("<http://www.w3.org/2007/05/powder#DR>",
             "<http://www.w3.org/2007/05/powder#describedBy>"));
 
     res.sendPage("xml2/emner_alle", bizData);
   }
 
   private void editTopic
           (AppleResponse
                   res, AppleRequest
                   req, String
                   type, String
                   messages) {
 
     boolean insertSuccess = false;
     String tempPrefixes = "<c:tempvalues \n" +
             "xmlns:topic=\"" + getProperty("sublima.base.url") + "topic/\"\n" +
             "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
             "xmlns:wdr=\"http://www.w3.org/2007/05/powder#\"\n" +
             "xmlns:lingvoj=\"http://www.lingvoj.org/ontology#\"\n" +
             "xmlns:sioc=\"http://rdfs.org/sioc/ns#\"\n" +
             "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
             "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
             "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
             "xmlns:dct=\"http://purl.org/dc/terms/\"\n" +
             "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
             "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\"\n" +
             "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
             "xmlns:c=\"http://xmlns.computas.com/cocoon\"\n" +
             "xmlns:sub=\"http://xmlns.computas.com/sublima#\">\n";
 
     StringBuilder tempValues = new StringBuilder();
     String uri = "";
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     messageBuffer.append(messages);
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("allanguages", adminService.getAllLanguages());
 
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       bizData.put("tempvalues", "<empty></empty>");
 
       if ("nytt".equalsIgnoreCase(type)) {
         bizData.put("topicdetails", "<empty></empty>");
         bizData.put("topicresources", "<empty></empty>");
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("alltopics", adminService.getAllTopics());
         bizData.put("status", adminService.getAllStatuses());
         bizData.put("mode", "topicedit");
         bizData.put("relationtypes", adminService.getAllRelationTypes());
       } else {
         bizData.put("topicdetails", adminService.getTopicByURI(req.getCocoonRequest().getParameter("uri")));
         bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("uri")));
         bizData.put("alltopics", adminService.getAllTopics());
         bizData.put("status", adminService.getAllStatuses());
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "topicedit");
         bizData.put("relationtypes", adminService.getAllRelationTypes());
       }
       bizData.put("userprivileges", userPrivileges);
       bizData.put("messages", "<empty></empty>");
       bizData.put("facets", getRequestXML(req));
 
       res.sendPage("xml2/emne", bizData);
 
       // When POST try to save the resource. Return error messages upon failure, and success message upon great success
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
 
       if (req.getCocoonRequest().getParameter("actionbuttondelete") != null) {
 
         if (req.getCocoonRequest().getParameter("warningSingleResource") == null) {
 
           String deleteString = "DELETE {\n" +
                   "<" + req.getCocoonRequest().getParameter("the-resource") + "> ?a ?o.\n" +
                   "} WHERE {\n" +
                   "<" + req.getCocoonRequest().getParameter("the-resource") + "> ?a ?o. }";
 
           boolean deleteTopicSuccess = sparulDispatcher.query(deleteString);
 
           logger.trace("ResourceController.editResource --> DELETE TOPIC QUERY:\n" + deleteString);
           logger.trace("ResourceController.editResource --> DELETE TOPIC QUERY RESULT: " + deleteTopicSuccess);
 
 
           if (deleteTopicSuccess) {
             messageBuffer.append("<c:message><i18n:text key=\"topic.deleted.ok\">Emne slettet!</i18n:text></c:message>\n");
           } else {
             messageBuffer.append("<c:message><i18n:text key=\"topic.deleted.error\">Feil ved sletting av emne</i18n:text></c:message>\n");
           }
         } else {
           messageBuffer.append("<c:message><i18n:text key=\"validation.topic.resourceempty\">En eller flere ressurser vil bli stående uten tilknyttet emne dersom du sletter dette emnet. Vennligst kontroller disse ressursene fra listen nederst, og tildel de nye emner eller slett de.</i18n:text></c:message>\n");
         }
 
       } else {
 
         Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
         // 1. Mellomlagre alle verdier
         // 2. Valider alle verdier
         // 3. Forsk  lagre
 
         tempValues = getTempValues(req);
         String validationmessages = validateRequest(req);
 
         if (!validationmessages.isEmpty()) {
           messageBuffer.append(validationmessages);
           bizData.put("topicdetails", adminService.getTopicByURI(req.getCocoonRequest().getParameter("uri")));
           bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("uri")));
           bizData.put("alltopics", adminService.getAllTopics());
           bizData.put("status", adminService.getAllStatuses());
           bizData.put("tempvalues", "<empty></empty>");
           bizData.put("mode", "topicedit");
           bizData.put("relationtypes", adminService.getAllRelationTypes());
           bizData.put("userprivileges", userPrivileges);
           bizData.put("messages", "<empty></empty>");
           bizData.put("facets", getRequestXML(req));
 
           res.sendPage("xml2/emne", bizData);
         } else {
 
 
           Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
           parameterMap.remove("prefix"); // The prefixes are magic variables
           parameterMap.remove("actionbutton"); // The name of the submit button
           parameterMap.remove("warningSingleResource");
           if (parameterMap.get("subjecturi-prefix") != null) {
             parameterMap.put("subjecturi-prefix", new String[]{getProperty("sublima.base.url") +
                     parameterMap.get("subjecturi-prefix")[0]});
           }
 
           String sparqlQuery = null;
           try {
             sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
           }
           catch (IOException e) {
             messageBuffer.append("<c:message><i18n:text key=\"topic.save.error\">Feil ved lagring av emne</i18n:text></c:message>\n");
           }
 
           uri = form2SparqlService.getURI();
 
           //String insertInverseTriples = createInverseInsert(uri, parameterMap);
           String insertRelations = createRelationsInsert(uri, parameterMap);
 
           String deleteRelatedToTopic = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n DELETE { ?s ?p <" + uri + "> . } WHERE { ?s a skos:Concept . ?s ?p <" + uri + "> . }";
           boolean deletedRelatedToTopic = sparulDispatcher.query(deleteRelatedToTopic);
           logger.trace("Deleted ?s ?p <" + uri + "> to remove inverse relations before new insert of topic. Result: " + insertRelations);
 
           // Check if a topic with the same uri already exists
           if ((req.getCocoonRequest().getParameter("the-resource") == null) && adminService.getTopicByURI(uri).contains("skos:Concept ")) {
             messageBuffer.append("<c:message><i18n:text key=\"topic.exists\">Et emne med denne tittelen og URI finnes allerede</i18n:text></c:message>\n");
           } else {
             logger.trace("TopicController.editTopic executing");
             insertSuccess = sparulDispatcher.query(sparqlQuery);
 
 
             logger.debug("TopicController.editTopic --> SPARUL QUERY RESULT: " + insertSuccess);
 
             if (insertSuccess) {
               if (!"".equals(insertRelations)) {
                 //sparulDispatcher.query(insertInverseTriples);
                 sparulDispatcher.query(insertRelations);
               }
               if (req.getCocoonRequest().getParameter("the-resource") == null) {
                 messageBuffer.append("<c:message><i18n:text key=\"topic.save.ok\">Nytt emne lagt til</i18n:text></c:message>\n");
               } else {
                 messageBuffer.append("<c:message><i18n:text key=\"topic.updated\">Emne oppdatert</i18n:text></c:message>\n");
               }
 
               indexService.indexTopic(uri, SettingsService.getProperty("sublima.topic.searchfields").split(";"), SettingsService.getProperty("sublima.prefixes").split(";"));
               logger.trace("AdminController.editResource --> Added the resource to the index");
               LARQ.setDefaultIndex(SettingsService.getIndexBuilderNode(null).getIndex());
 
 
             } else {
               messageBuffer.append("<c:message><i18n:text key=\"topic.save.error\">Feil ved lagring av emne</i18n:text></c:message>\n");
               bizData.put("topicdetails", "<empty></empty>");
             }
           }
         }
 
         if (insertSuccess) {
           bizData.put("result-list", adminService.getTopicDetailsForTopicPageFromAdmin("<" + uri + ">"));
           bizData.put("navigation", adminService.getNavigationDetailsForTopicPage("<" + uri + ">"));
           bizData.put("mode", "topic");
           StringBuilder params = adminService.getMostOfTheRequestXML(req);
 
           // These will not be brought along unless we add it as request parameters, which is hackish.
           params.append("\n    <param key=\"prefix\">");
           params.append("\n      <value>wdr: &lt;http://www.w3.org/2007/05/powder%23&gt;</value>");
           params.append("\n      <value>dct: &lt;http://purl.org/dc/terms/&gt;</value>");
           params.append("\n      <value>rdfs: &lt;http://www.w3.org/2000/01/rdf-schema%23&gt;</value>");
           params.append("\n      <value>skos: &lt;http://www.w3.org/2004/02/skos/core%23&gt;</value>");
           params.append("\n    </param>");
           params.append("\n  </request>\n");
 
           bizData.put("request", params.toString());
           bizData.put("loggedin", loggedIn);
           bizData.put("searchparams", "<empty/>");
           messageBuffer.append("</c:messages>\n");
           bizData.put("messages", messageBuffer.toString());
           bizData.put("abovemaxnumberofhits", "false");
           System.gc();
           res.sendPage("xml/sparql-result", bizData);
 
         } else {
           bizData.put("topicdetails", adminService.getTopicByURI(req.getCocoonRequest().getParameter("the-resource")));
           bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("the-resource")));
           bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
           bizData.put("mode", "topictemp");
           bizData.put("status", adminService.getAllStatuses());
           bizData.put("alltopics", adminService.getAllTopics());
           bizData.put("relationtypes", adminService.getAllRelationTypes());
           bizData.put("userprivileges", userPrivileges);
           messageBuffer.append("</c:messages>\n");
           bizData.put("facets", getRequestXML(req));
           bizData.put("messages", messageBuffer.toString());
           res.sendPage("xml2/emne", bizData);
         }
 
         //Invalidate the Topic cache for autocompletion
         AutocompleteCache.invalidateTopicCache();
         AutocompleteCache.getTopicList();
       }
     }
   }
 
   private String createRelationsInsert(String uri, Map<String, String[]> parameterMap) {
 
     StringBuilder relationsInsert = new StringBuilder();
     relationsInsert.append("INSERT\n{\n");
     boolean containsTriples = false;
 
     Iterator it = parameterMap.entrySet().iterator();
     while (it.hasNext()) {
 
       Map.Entry<String, String[]> pairs = (Map.Entry<String, String[]>) it.next();
 // Check if the value in the key is the uri of a relation
 // Since the relations are in the form <uri> we can check if it starts with < first
       String key = pairs.getKey();
       if (key.startsWith("<")) { // Then it is a uri and possibly a relation
         if (adminService.isRelation(key)) { // If the uri from the key is a relation
 
           // Check if the relation is one-way, symmetric or inverse
           String inverseRelationURI = adminService.getInverseRelationUriIfAny(key);
           if (!inverseRelationURI.isEmpty()) { // Inverse relation -- > a rel1 b and b rel2 a
             for (String s : pairs.getValue()) {
               if (!s.isEmpty()) {
                 relationsInsert.append("<").append(uri).append("> ").append(key).append(" <").append(s).append("> .\n");
                 relationsInsert.append("<").append(s).append("> <").append(inverseRelationURI).append("> <").append(uri).append("> .\n");
                 containsTriples = true;
               }
             }
           } else if (adminService.isSymmetricProperty(key)) { // Symmetric relation --> a rel b and b rel a
             for (String s : pairs.getValue()) {
               if (!s.isEmpty()) {
                 relationsInsert.append("<").append(uri).append("> ").append(key).append(" <").append(s).append("> .\n");
                 relationsInsert.append("<").append(s).append("> ").append(key).append(" <").append(uri).append("> .\n");
                 containsTriples = true;
               }
             }
           } else { // one-way relation --> a rel b
             for (String s : pairs.getValue()) {
               if (!s.isEmpty()) {
                 relationsInsert.append("<").append(uri).append("> ").append(key).append(" <").append(s).append("> .\n");
                 containsTriples = true;
               }
             }
           }
         }
       }
     }
 
     relationsInsert.append("}");
     return containsTriples ? relationsInsert.toString() : "";
   }
 
   private String createInverseInsert(String uri, Map<String, String[]> parameterMap) {
     StringBuilder relationsInsert = new StringBuilder();
     relationsInsert.append("INSERT\n{\n");
     boolean containsTriples = false;
 
 // Add the broader/narrower inverse relation if broader | narrower is chosen
     if (parameterMap.get("<http://www.w3.org/2004/02/skos/core#broader>") != null) {
       String[] broader = parameterMap.get("<http://www.w3.org/2004/02/skos/core#broader>");
       for (String s : broader) {
         relationsInsert.append("<" + s + "> " + "<http://www.w3.org/2004/02/skos/core#narrower> <" + uri + "> .\n");
       }
       containsTriples = true;
 
     }
 
     if (parameterMap.get("<http://www.w3.org/2004/02/skos/core#narrower>") != null) {
       String[] broader = parameterMap.get("<http://www.w3.org/2004/02/skos/core#narrower>");
       for (String s : broader) {
         relationsInsert.append("<" + s + "> " + "<http://www.w3.org/2004/02/skos/core#broader> <" + uri + "> .\n");
       }
       containsTriples = true;
     }
 
     relationsInsert.append("}");
 
     if (containsTriples)
       return relationsInsert.toString();
     else
       return "";
   }
 
   private StringBuilder getTempValues(AppleRequest req) {
     //Keep all selected values in case of validation error
     String temp_title = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel");
     String[] temp_broader = req.getCocoonRequest().getParameterValues("dct:subject/skos:Concept/skos:broader/rdf:resource");
     String temp_status = req.getCocoonRequest().getParameter("wdr:describedBy");
     String temp_description = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:definition");
     String temp_note = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:note");
     String temp_synonyms = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:altLabel");
 
 //Create an XML structure for the selected values, to use in the JX template
     StringBuilder xmlStructureBuffer = new StringBuilder();
     xmlStructureBuffer.append("<skos:prefLabel>" + temp_title + "</skos:prefLabel>\n");
 
     if (temp_broader != null) {
       for (String s : temp_broader) {
         //xmlStructureBuffer.append("<language>" + s + "</language>\n");
         xmlStructureBuffer.append("<skos:broader rdf:resource=\"" + s + "\"/>\n");
       }
     }
 
     xmlStructureBuffer.append("<wdr:describedBy rdf:resource=\"" + temp_status + "\"/>\n");
     xmlStructureBuffer.append("<skos:description>" + temp_description + "</skos:description>\n");
     xmlStructureBuffer.append("<skos:note>" + temp_note + "</skos:note>\n");
     xmlStructureBuffer.append("<skos:altLabel>" + temp_synonyms + "</skos:altLabel>\n");
 
 
     return xmlStructureBuffer;
   }
 
   /**
    * Method to validate the request upon insert of new resource.
    * Checks all parameters and gives error message if one or more required values are null
    *
    * @param req
    * @return
    */
   private String validateRequest(AppleRequest req) {
     StringBuilder validationMessages = new StringBuilder();
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel")) || req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel") == null) {
       validationMessages.append("<c:message><i18n:text key=\"topic.validation.titleblank\">Emnets tittel kan ikke være blank</i18n:text></c:message>\n");
     }
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("wdr:describedBy")) || req.getCocoonRequest().getParameter("wdr:describedBy") == null) {
       validationMessages.append("<c:message><i18n:text key=\"validation.statuschoice\">En status må velges</i18n:text></c:message>\n");
     } else if (!userPrivileges.contains(req.getCocoonRequest().getParameter("wdr:describedBy"))) {
       validationMessages.append("<c:message><i18n:text key=\"topic.validation.rolestatus\">Rollen du har tillater ikke å lagre et emne med den valgte statusen.</i18n:text></c:message>\n");
     }
 
     return validationMessages.toString();
   }
 
   public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 
   //todo Move to a Service-class
   private Map<String, String[]> createParametersMap(Request request) {
     Map<String, String[]> result = new HashMap<String, String[]>();
     Enumeration parameterNames = request.getParameterNames();
     while (parameterNames.hasMoreElements()) {
       String paramName = (String) parameterNames.nextElement();
       result.put(paramName, request.getParameterValues(paramName));
     }
     return result;
   }
 
   public void setAppMan
           (ApplicationManager
                   appMan) {
     this.appMan = appMan;
   }
 
   private void showRelations
           (AppleResponse
                   res, AppleRequest
                   req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("all_relations", adminService.getAllRelationTypes());
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml2/relasjoner_alle", bizData);
   }
 
   private String getRequestXML(AppleRequest req) {
     // This is such a 1999 way of doing things. There should be a generic SAX events generator
     // or something that would serialise this data structure automatically in a one-liner,
     // but I couldn't find it. Also, the code should not be in each and every controller.
     // Arguably a TODO.
     StringBuilder params = new StringBuilder();
     String uri = req.getCocoonRequest().getRequestURI();
     int paramcount = 0;
     params.append("  <c:request xmlns:c=\"http://xmlns.computas.com/cocoon\" justbaseurl=\"" + uri + "\" ");
     if (req.getCocoonRequest().getQueryString() != null) {
       uri += "?" + req.getCocoonRequest().getQueryString();
       uri = uri.replace("&", "&amp;");
       uri = uri.replace("<", "%3C");
       uri = uri.replace(">", "%3E");
       uri = uri.replace("#", "%23");
       paramcount = req.getCocoonRequest().getParameters().size();
     }
     params.append("paramcount=\"" + paramcount + "\" ");
     params.append("requesturl=\"" + uri);
     params.append("\"/>\n");
     return params.toString();
   }
 }
 
