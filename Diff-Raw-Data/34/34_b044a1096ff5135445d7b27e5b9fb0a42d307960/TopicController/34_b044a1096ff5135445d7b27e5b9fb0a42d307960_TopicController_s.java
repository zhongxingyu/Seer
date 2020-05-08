 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.computas.sublima.query.service.SettingsService;
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
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class TopicController implements StatelessAppleController {
 
   private SparqlDispatcher sparqlDispatcher;
   private SparulDispatcher sparulDispatcher;
   AdminService adminService = new AdminService();
   private ApplicationManager appMan;
   private ApplicationUtil appUtil = new ApplicationUtil();
   private User user;
   private String mode;
   private String submode;
   private String userPrivileges = "<empty/>";
   private boolean loggedIn = false;
   String[] completePrefixArray = {"PREFIX rdf: 		<http://www.w3.org/1999/02/22-rdf-syntax-ns#>", "PREFIX rdfs: 		<http://www.w3.org/2000/01/rdf-schema#>", "PREFIX owl: 		<http://www.w3.org/2002/07/owl#>", "PREFIX foaf: 		<http://xmlns.com/foaf/0.1/>", "PREFIX lingvoj: 	<http://www.lingvoj.org/ontology#>", "PREFIX dcmitype: 	<http://purl.org/dc/dcmitype/>", "PREFIX dct: 		<http://purl.org/dc/terms/>", "PREFIX sub: 		<http://xmlns.computas.com/sublima#>", "PREFIX wdr: 		<http://www.w3.org/2007/05/powder#>", "PREFIX sioc: 		<http://rdfs.org/sioc/ns#>", "PREFIX xsd: 		<http://www.w3.org/2001/XMLSchema#>", "PREFIX topic: 		<topic/>", "PREFIX skos:		<http://www.w3.org/2004/02/skos/core#>"};
 
   String completePrefixes = StringUtils.join("\n", completePrefixArray);
   String[] prefixArray = {"dct: <http://purl.org/dc/terms/>", "foaf: <http://xmlns.com/foaf/0.1/>", "sub: <http://xmlns.computas.com/sublima#>", "rdfs: <http://www.w3.org/2000/01/rdf-schema#>", "wdr: <http://www.w3.org/2007/05/powder#>", "skos: <http://www.w3.org/2004/02/skos/core#>", "lingvoj: <http://www.lingvoj.org/ontology#>"};
   String prefixes = StringUtils.join("\n", prefixArray);
 
   private static Logger logger = Logger.getLogger(AdminController.class);
 
   private String getRequestXML(AppleRequest req) {
     // This is such a 1999 way of doing things. There should be a generic SAX events generator
     // or something that would serialise this data structure automatically in a one-liner,
     // but I couldn't find it. Also, the code should not be in each and every controller.
     // Arguably a TODO.
     StringBuffer params = new StringBuffer();
     String uri = req.getCocoonRequest().getRequestURI();
     int paramcount = 0;
     params.append("  <c:request xmlns:c=\"http://xmlns.computas.com/cocoon\" justbaseurl=\"" + uri + "\" ");
     if (req.getCocoonRequest().getQueryString() != null) {
       uri += "?" + req.getCocoonRequest().getQueryString();
       uri = uri.replaceAll("&", "&amp;");
       paramcount = req.getCocoonRequest().getParameters().size();
     }
     params.append("paramcount=\"" + paramcount + "\" ");
     params.append("requesturl=\"" + uri);
     params.append("\"/>\n");
     return params.toString();
   }
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     this.mode = req.getSitemapParameter("mode");
     this.submode = req.getSitemapParameter("submode");
     loggedIn = appMan.isLoggedIn("Sublima");
 
     if (appUtil.getUser() != null) {
       user = appUtil.getUser();
       userPrivileges = adminService.getRolePrivilegesAsXML(user.getAttribute("role").toString());
     }
 
     if ("emner".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         res.sendPage("xml2/emner", null);
        return;
       } else if ("nytt".equalsIgnoreCase(submode)) {
         editTopic(res, req, "nytt", null);
        return;
       } else if ("alle".equalsIgnoreCase(submode)) {
         showTopics(res, req);
        return;
       } else if ("emne".equalsIgnoreCase(submode)) {
         editTopic(res, req, "edit", null);
        return;
       } else if ("koble".equalsIgnoreCase(submode)) {
         mergeTopics(res, req);
        return;
       } else if ("tema".equalsIgnoreCase(submode)) {
         setThemeTopics(res, req);
        return;
       } else {
         res.sendStatus(404);
        return;
       }
     } else if ("browse".equalsIgnoreCase(mode)) {
       showTopicBrowsing(res, req);
      return;
     } else if ("relasjoner".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         res.sendPage("xml2/relasjoner", null);
        return;
       } else if ("relasjon".equalsIgnoreCase(submode)) {
         editRelation(res, req, null);
        return;
       } else if ("alle".equalsIgnoreCase(submode)) {
         showRelations(res, req);
        return;
       }
     } else if ("a-z".equalsIgnoreCase(mode)) {
       getTopicsByLetter(res, req, submode);
     }
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
 
     String locale = req.getCocoonRequest().getParameter("locale");
 
     if (locale == null) {
      locale = SettingsService.getProperty("sublima.default.locale");      
     }
 
     bizData.put("themetopics", adminService.getTopicsByLetter(letter, locale));
     bizData.put("mode", "browse");
     bizData.put("loggedin", "<empty></empty>");
     bizData.put("facets", getRequestXML(req));
     res.sendPage("xml/browse", bizData);
   }
 
   private void editRelation(AppleResponse res, AppleRequest req, Object o) {
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     Map<String, Object> bizData = new HashMap<String, Object>();
     String uri = req.getCocoonRequest().getParameter("the-resource");
 
     bizData.put("allanguages", adminService.getAllLanguages());
     bizData.put("userprivileges", userPrivileges);
 
     if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
 
       bizData.put("tempvalues", "<empty></empty>");
       if ("".equalsIgnoreCase(uri) || uri == null) {
         bizData.put("relationdetails", "<empty></empty>");
       } else {
         bizData.put("relationdetails", adminService.getRelationByURI(uri));
       }
 
       bizData.put("mode", "topicrelated");
 
      bizData.put("userprivileges", userPrivileges);

       bizData.put("messages", "<empty></empty>");
       bizData.put("facets", getRequestXML(req));
       res.sendPage("xml2/relasjon", bizData);
 
     } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
       parameterMap.remove("actionbutton");
       Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
       parameterMap.remove("prefix"); // The prefixes are magic variables
       if (parameterMap.get("subjecturi-prefix") != null) {
         parameterMap.put("subjecturi-prefix", new String[]{getProperty("sublima.base.url") +
                 parameterMap.get("subjecturi-prefix")[0]});
       }
       String sparqlQuery = null;
       try {
         sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
       }
       catch (IOException e) {
         messageBuffer.append("<c:message>Feil ved lagring av ny relasjonstype</c:message>\n");
       }
 
       logger.trace("TopicController.editRelation --> QUERY:\n" + sparqlQuery);
 
       boolean insertSuccess = sparulDispatcher.query(sparqlQuery);
 
       logger.trace("TopicController.editRelation --> QUERY RESULT: " + insertSuccess);
 
       if (insertSuccess) {
         messageBuffer.append("<c:message>Ny relasjonstype lagret</c:message>\n");
 
         bizData.put("relationdetails", adminService.getRelationByURI(form2SparqlService.getURI()));
 
       } else {
         messageBuffer.append("<c:message>Feil ved lagring av ny relasjonstype</c:message>\n");
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
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml/browse", bizData);
   }
 
   private void mergeTopics
           (AppleResponse
                   res, AppleRequest
                   req) {
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       bizData.put("tempvalues", "<empty></empty>");
       bizData.put("alltopics", adminService.getAllTopics());
       bizData.put("mode", "topicjoin");
 
       bizData.put("userprivileges", userPrivileges);
       bizData.put("messages", "<empty></empty>");
       bizData.put("facets", getRequestXML(req));
 
       res.sendPage("xml2/koble", bizData);
 
     } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
 
       // Lage ny URI for emne
       // Legge til URI i alle emner hvor gamle URI finnes
       // Slette alle gamle URIer
       StringBuffer topicBuffer = new StringBuffer();
 
       String uri = req.getCocoonRequest().getParameter("skos:Concept/skos:prefLabel").replace(" ", "_");
       uri = uri.replace(",", "_");
       uri = uri.replace(".", "_");
       uri = getProperty("sublima.base.url") + "topic/" + uri + "_" + uri.hashCode();
 
       String insertNewTopicString = completePrefixes + "\nINSERT\n{\n" + "<" + uri + "> a skos:Concept ;\n" + " skos:prefLabel \"" + req.getCocoonRequest().getParameter("skos:Concept/skos:prefLabel") + "\"@no .\n" + "}";
 
       logger.trace("TopicController.mergeTopics --> INSERT NEW TOPIC QUERY:\n" + insertNewTopicString);
 
      boolean insertNewSuccess = sparulDispatcher.query(insertNewTopicString);
 
       for (String s : req.getCocoonRequest().getParameterValues("skos:Concept")) {
         topicBuffer.append("?resource skos:Concept <" + s + "> .\n");
       }
 
       messageBuffer.append("</c:messages>\n");
 
       bizData.put("messages", messageBuffer.toString());
       bizData.put("userprivileges", userPrivileges);
       bizData.put("facets", getRequestXML(req));
 
       res.sendPage("xml2/koble", bizData);
     }
   }
 
   private void setThemeTopics
           (AppleResponse
                   res, AppleRequest
                   req) {
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
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
 
       StringBuffer deleteString = new StringBuffer();
       StringBuffer whereString = new StringBuffer();
       StringBuffer insertString = new StringBuffer();
 
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
         messageBuffer.append("<c:message>Emnene satt som temaemner</c:message>\n");
 
       } else {
         messageBuffer.append("<c:message>Feil ved merking av temaemner</c:message>\n");
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
 
   private void showTopics
           (AppleResponse
                   res, AppleRequest
                   req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("all_topics", adminService.getAllTopics());
     bizData.put("facets", getRequestXML(req));
 
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
 
     StringBuffer tempValues = new StringBuffer();
     String uri = "";
 
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
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
 
         if (req.getCocoonRequest().getParameter("warningSingleResource") != null) {
 
           String deleteString = "DELETE {\n" +
                   "<" + req.getCocoonRequest().getParameter("the-resource") + "> ?a ?o.\n" +
                   "} WHERE {\n" +
                   "<" + req.getCocoonRequest().getParameter("the-resource") + "> ?a ?o. }";
 
           boolean deleteTopicSuccess = sparulDispatcher.query(deleteString);
 
           logger.trace("ResourceController.editResource --> DELETE TOPIC QUERY:\n" + deleteString);
           logger.trace("ResourceController.editResource --> DELETE TOPIC QUERY RESULT: " + deleteTopicSuccess);
 
 
           if (deleteTopicSuccess) {
             messageBuffer.append("<c:message>Emne slettet!</c:message>\n");
           } else {
             messageBuffer.append("<c:message>Feil ved sletting av emne</c:message>\n");
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
           messageBuffer.append("<c:message>Feil ved lagring av emne</c:message>\n");
         }
 
         uri = form2SparqlService.getURI();
 
         logger.trace("TopicController.editTopic --> SPARUL QUERY:\n" + sparqlQuery);
         insertSuccess = sparulDispatcher.query(sparqlQuery);
 
         logger.debug("TopicController.editTopic --> SPARUL QUERY RESULT: " + insertSuccess);
 
         if (insertSuccess) {
           messageBuffer.append("<c:message>Nytt emne lagt til!</c:message>\n");
 
         } else {
           messageBuffer.append("<c:message>Feil ved lagring av nytt emne</c:message>\n");
           bizData.put("topicdetails", "<empty></empty>");
         }
       }
 
       if (insertSuccess) {
         bizData.put("topicdetails", adminService.getTopicByURI(uri));
         bizData.put("topicresources", adminService.getTopicResourcesByURI(uri));
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "topicedit");
       } else {
         bizData.put("topicdetails", adminService.getTopicByURI(req.getCocoonRequest().getParameter("the-resource")));
         bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("the-resource")));
         bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
         bizData.put("mode", "topictemp");
       }
 
       bizData.put("status", adminService.getAllStatuses());
       bizData.put("alltopics", adminService.getAllTopics());
       bizData.put("relationtypes", adminService.getAllRelationTypes());
       bizData.put("userprivileges", userPrivileges);
       messageBuffer.append("</c:messages>\n");
       bizData.put("facets", getRequestXML(req));
 
       bizData.put("messages", messageBuffer.toString());
 
       res.sendPage("xml2/emne", bizData);
     }
   }
 
 
   private StringBuffer getTempValues
           (AppleRequest
                   req) {
     //Keep all selected values in case of validation error
     String temp_title = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel");
     String[] temp_broader = req.getCocoonRequest().getParameterValues("dct:subject/skos:Concept/skos:broader/rdf:resource");
     String temp_status = req.getCocoonRequest().getParameter("wdr:describedBy");
     String temp_description = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:definition");
     String temp_note = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:note");
     String temp_synonyms = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:altLabel");
 
     //Create an XML structure for the selected values, to use in the JX template
     StringBuffer xmlStructureBuffer = new StringBuffer();
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
     StringBuffer validationMessages = new StringBuffer();
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel")) || req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel") == null) {
       validationMessages.append("<c:message>Emnets tittel kan ikke være blank</c:message>\n");
     }
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("wdr:describedBy")) || req.getCocoonRequest().getParameter("wdr:describedBy") == null) {
       validationMessages.append("<c:message>En status må velges</c:message>\n");
     } else if (!userPrivileges.contains(req.getCocoonRequest().getParameter("wdr:describedBy"))) {
       validationMessages.append("<c:message>Rollen du har tillater ikke å lagre et emne med den valgte statusen.</c:message>\n");
     }
 
     return validationMessages.toString();
   }
 
 
   public void setSparqlDispatcher
           (SparqlDispatcher
                   sparqlDispatcher) {
     this.sparqlDispatcher = sparqlDispatcher;
   }
 
   public void setSparulDispatcher
           (SparulDispatcher
                   sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 
   //todo Move to a Service-class
   private Map<String, String[]> createParametersMap
           (Request
                   request) {
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
 
   private void showRelations(AppleResponse res, AppleRequest req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("all_relations", adminService.getAllRelationTypes());
     bizData.put("facets", getRequestXML(req));
 
     res.sendPage("xml2/relasjoner_alle", bizData);
   }
 }
 
