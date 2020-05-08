 package dk.sst.snomedcave.controllers;
 
 import com.google.gson.*;
 import dk.sst.snomedcave.dao.ConceptRepository;
 import dk.sst.snomedcave.dao.DrugRepository;
 import dk.sst.snomedcave.model.Concept;
 import dk.sst.snomedcave.model.ConceptRelation;
 import dk.sst.snomedcave.model.Drug;
 import org.apache.commons.collections15.CollectionUtils;
 import org.apache.commons.collections15.IteratorUtils;
 import org.apache.commons.collections15.Predicate;
 import org.apache.log4j.Logger;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.traversal.Evaluators;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.kernel.Traversal;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.neo4j.graphdb.DynamicRelationshipType.withName;
 
 @Controller
 @RequestMapping("/drugs/")
 public class DrugController {
     private final static Logger logger = Logger.getLogger(DrugController.class);
     @Inject
     DrugRepository drugRepository;
 
     @Inject
     ConceptRepository conceptRepository;
 
     @Inject
     Neo4jTemplate neo4jTemplate;
 
     Gson gson = new GsonBuilder().create();
 
     private static Concept causativeAgentAttribute;
 
     @RequestMapping(value = "search", produces = "application/json;charset=utf-8")
     public ResponseEntity<String> search(@RequestParam("q") String drugQuery) {
         List<Drug> drugs = drugRepository.findByNameLike(String.format("*%s*", drugQuery));
         if (drugs.isEmpty()) {
             return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
         }
         JsonArray response = new JsonArray();
         for (Drug drug : drugs) {
             response.add(new JsonPrimitive(String.format("%s", drug.getName())));
         }
         return new ResponseEntity<String>(gson.toJson(response), HttpStatus.OK);
     }
 
     @RequestMapping(value = "concepttree", produces = "application/json;charset=utf-8")
     public ResponseEntity<String> tree(@RequestParam("name") String drugName) {
         final List<Drug> drugs = drugRepository.findByNameLike("\"" + drugName.replace("\"", "\\\"") + "\"");
         if (drugs.size() > 1) {
             logger.warn("Found more than one drug for drugName=" + drugName + ", count=" + drugs.size());
         }
         Concept refersTo = drugs.get(0).getRefersTo();
         Concept concept = null;
         Concept drugAllergy = conceptRepository.getByConceptId("416098002");
         Node drugAllergyNode = neo4jTemplate.getNode(drugAllergy.getNodeId());
         final TraversalDescription td = Traversal.description().depthFirst().relationships(withName("childs"), Direction.INCOMING).relationships(withName("child"), Direction.INCOMING).evaluator(Evaluators.returnWhereEndNodeIs(drugAllergyNode));
 
         List<ConceptRelation> causativesDrugAllergy = new ArrayList<ConceptRelation>(CollectionUtils.select(refersTo.getChilds(), new Predicate<ConceptRelation>() {
             @Override
             public boolean evaluate(ConceptRelation relation) {
                 if (conceptRepository.findOne(relation.getType().getNodeId()).getNodeId().equals(causativeAgentId())) {
                     Node childNode = neo4jTemplate.getNode(relation.getChild().getNodeId());
 
                     Traverser paths = td.traverse(childNode);
                     List<Path> pathList = IteratorUtils.toList(paths.iterator());
                     if (pathList.size() > 0) {
                         logger.info("found " + pathList.size() + " paths to Drug Allergy");
                     }
                     return pathList.size() > 0;
                 }
                 return false;
             }
         }));
         if (causativesDrugAllergy.size() > 1) {
             logger.warn("Found more than one causative agent");
             for (ConceptRelation conceptRelation : causativesDrugAllergy) {
                 Concept child = conceptRepository.findOne(conceptRelation.getChild().getNodeId());
                 logger.warn("conceptId=" + child.getConceptId() + ", name=" + child.getFullyspecifiedName());
             }
         }
 
        if (causativesDrugAllergy.size() == 0) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

         concept = conceptRepository.findOne(causativesDrugAllergy.get(0).getChild().getNodeId());
 
         JsonObject response = new JsonObject();
         response.addProperty("allergyId", concept.getConceptId());
         return new ResponseEntity<String>(gson.toJson(response), HttpStatus.OK);
     }
 
     private long causativeAgentId() {
         if (causativeAgentAttribute == null) {
             causativeAgentAttribute = conceptRepository.getByConceptId("246075003");
         }
         return causativeAgentAttribute.getNodeId();
     }
 }
