 package net.metadata.openannotation.lorestore.servlet.rdf2go;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.metadata.openannotation.lorestore.access.LoreStoreAccessPolicy;
 import net.metadata.openannotation.lorestore.exceptions.LoreStoreException;
 import net.metadata.openannotation.lorestore.model.rdf2go.NamedGraphImpl;
 import net.metadata.openannotation.lorestore.model.rdf2go.OpenAnnotationImpl;
 import net.metadata.openannotation.lorestore.servlet.LoreStoreControllerConfig;
 import net.metadata.openannotation.lorestore.servlet.LoreStoreValidationHandler;
 import net.metadata.openannotation.lorestore.servlet.OREController;
 import net.metadata.openannotation.lorestore.triplestore.TripleStoreConnectorFactory;
 import net.metadata.openannotation.lorestore.util.OATripleCallback;
 
 import org.apache.log4j.Logger;
 import org.ontoware.rdf2go.Reasoning;
 import org.ontoware.rdf2go.exception.ModelRuntimeException;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.ontoware.rdf2go.model.QueryResultTable;
 import org.ontoware.rdf2go.model.QueryRow;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.Node;
 import org.openrdf.query.Query;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.rdf2go.RepositoryModelFactory;
 import org.springframework.web.servlet.ModelAndView;
 
 import de.dfki.km.json.JSONUtils;
 import de.dfki.km.json.jsonld.JSONLD;
 import de.dfki.km.json.jsonld.JSONLDProcessingError;
 
 import au.edu.diasb.chico.mvc.RequestFailureException;
 
 /**
  * The RDF2GoOAQueryHandler class handles queries from the
  * {@link OREController}.
  */
 public class OAValidationHandler implements LoreStoreValidationHandler {
 
     protected final LoreStoreControllerConfig occ;
     protected final TripleStoreConnectorFactory cf;
     protected LoreStoreAccessPolicy ap;
     protected final Logger LOG = Logger.getLogger(OAValidationHandler.class);
     private List<Map<String,Object>> validationRules;
     
     public OAValidationHandler(LoreStoreControllerConfig occ) {
         this.occ = occ;
         this.cf = occ.getContainerFactory();
         this.ap = occ.getAccessPolicy();
     }
 
     @Override
     public ModelAndView validate(InputStream inputRDF, String contentType)
             throws RequestFailureException, IOException, LoreStoreException,
             InterruptedException {
         ModelAndView mav = new ModelAndView("validation");
         // create temp model for annotation(s) from inputRDF
         RepositoryModelFactory mf = new RepositoryModelFactory();
         Model model = mf.createModel(Reasoning.owl);
         try {
             model.open();
             if (contentType.equals(Syntax.RdfXml.getMimeType())
                     || contentType.equals(Syntax.Trix.getMimeType()) 
                     || contentType.equals(Syntax.Trig.getMimeType())){
                 //StringReader reader = new StringReader(inputRDF);
                 model.readFrom(inputRDF, Syntax.forMimeType(contentType), occ.getBaseUri());
             
             } else if (contentType.equals("application/json")){
                 //Object jsonObject = JSONUtils.fromString(inputRDF);
                 Object jsonObject = JSONUtils.fromInputStream(inputRDF);
                 // TODO if no @context, inject default context
                 // TODO if no @id, inject dummy identifier (will be replaced)
                 OATripleCallback callback = new OATripleCallback();
                 callback.setModel(model);
                 JSONLD.toRDF(jsonObject, callback);
             } else {
                throw new RequestFailureException(HttpServletResponse.SC_BAD_REQUEST, "Acceptable content types are RDF/XML or JSON-LD");
             }
         } catch (ModelRuntimeException e) {
             try{
                 LOG.debug("Model read failed " + e.getMessage());
                 model.close();
             } catch (Exception e2){
                 // ignore errors closing model
             }
             throw new RequestFailureException(HttpServletResponse.SC_BAD_REQUEST, "Error reading RDF " + e.getMessage());
         } catch (JSONLDProcessingError e) {
             LOG.info("Post: Model read failed " + e.getMessage());
             throw new RequestFailureException(HttpServletResponse.SC_BAD_REQUEST, "Error reading JSON " + e.getMessage());
         }
 
         // Load validation rules 
         if (this.validationRules == null){
             InputStream in = this.getClass().getClassLoader().getResourceAsStream("OAConstraintsSPARQL.json");
             this.validationRules = (List<Map<String,Object>>) JSONUtils.fromInputStream(in);
         }
         int totalPass = 0, totalError = 0, totalWarn = 0, totalSkip = 0, totalTotal = 0;
         // clone the validation rules into an object that we will pass to the ModelAndView
         ArrayList<Map<String,Object>> result = new ArrayList<Map<String,Object>>(validationRules);
         for (Map<String,Object> section : result){
             // for each section
             int sectionPass = 0, sectionError = 0, sectionWarn = 0, sectionSkip = 0;
             for (Map<String,Object> rule: ((List<Map<String,Object>>)section.get("constraints"))){
                 totalTotal++;
                 // process each rule
                 String queryString = (String) rule.get("query");
                 String preconditionQueryString = (String) rule.get("precondition");
                 if(queryString == null || "".equals(queryString)){
                     totalSkip++;
                     sectionSkip++;
                     rule.put("status", "skip");
                     rule.put("result", "Validation rule not implemented");
                 } else {
                     
                     int count = 0;
                     int precondcount = 0;
                     try{
                         boolean preconditionOK = true;
                         // TODO check if there is a precondition query and run that first to determine whether rule applies
                         if (preconditionQueryString != null && ! "".equals(preconditionQueryString)){
                             QueryResultTable precondResultTable = model.sparqlSelect(preconditionQueryString);
                             for(QueryRow row1 : precondResultTable) {
                                 precondcount++;
                             }
                             if (precondcount == 0){
                                 // if precondition did not produce any matches, set status to skip
                                 rule.put("status", "skip");
                                 rule.put("result", "Rule does not apply to supplied data: " + rule.get("preconditionMessage"));
                                 
                                 totalSkip++;
                                 sectionSkip++;
                                 preconditionOK = false;
                             } 
                         }
                         if (preconditionOK){
                             // run the query and store the result back into the constraint object
                             QueryResultTable resultTable = model.sparqlSelect(queryString);
                             List<String> vars = resultTable.getVariables();
                             List<Map<String,String>> matches = new ArrayList<Map<String,String>>();
                             for(QueryRow row : resultTable) {
                                 boolean nullValues = true;
                                 for(String var: vars){
                                     Node val = row.getValue(var);
                                     //LOG.info(var + " " + row.toString());
                                     if (val != null && !val.toString().equals("0")){
                                         nullValues = false;
                                         HashMap<String,String> res = new HashMap<String,String>();
                                         res.put(var, row.getValue(var).toString());
                                         matches.add(res);
                                     } 
                                 }
                                 if (!nullValues){
                                     count++;
                                 }
                             }
                             if (count == 0){
                                 rule.put("status", "pass");
                                 rule.put("result","");
                                 totalPass++;
                                 sectionPass++;
                             } else {
                                 // if there are results, the validation failed, so set the status from the severity
                                 // add results to the result so that they can be displayed
                                 rule.put("result", matches);
                                 String severity = (String) rule.get("severity");
                                 rule.put("status", severity);
                                 if ("error".equals(severity)){
                                     totalError++;
                                     sectionError++;
                                 } else {
                                     totalWarn++;
                                     sectionWarn++;
                                 }
                             }
                         }
                     } catch (Exception e){
                         // if there were any errors running queries, set status to skip
                         LOG.info("error validating " + rule.get("description") + " " + e.getMessage());
                         rule.put("status", "skip");
                         rule.put("result","Error evaluating validation rule: " + e.getMessage());
                         totalSkip++;
                         sectionSkip++;
                     }
                 }
                 // section summaries for validation report
                 section.put("pass", sectionPass);
                 section.put("error", sectionError);
                 section.put("warn", sectionWarn);
                 section.put("skip", sectionSkip);
                 if (sectionError > 0){
                     section.put("status","error");
                 } else if (sectionWarn > 0){
                     section.put("status", "warn");
                 } else if (sectionPass == 0){
                     section.put("status", "skip");
                 } else {
                     section.put("status", "pass");
                 }
             }
         }
         
         // store results of validation in ModelAndView:
         HashMap<String,Object> finalResult = new HashMap<String, Object>();
         finalResult.put("result",result);
         finalResult.put("error",totalError);
         finalResult.put("warn", totalWarn);
         finalResult.put("pass",totalPass);
         finalResult.put("skip",totalSkip);
         finalResult.put("total", totalTotal);
         mav.addObject("result", finalResult);
         // destroy temp rdf model
         model.close();
         return mav;
     }
 
 }
