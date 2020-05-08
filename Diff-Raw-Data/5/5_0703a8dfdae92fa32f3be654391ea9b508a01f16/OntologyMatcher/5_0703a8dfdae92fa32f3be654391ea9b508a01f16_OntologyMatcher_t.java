 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wssemanticmatching.ontology;
 
 import java.util.HashMap;
 import org.mindswap.pellet.owlapi.Reasoner;
 import org.semanticweb.owl.model.OWLClass;
 import org.semanticweb.owl.model.OWLOntology;
 import org.semanticweb.owl.model.OWLOntologyManager;
 
 /**
  *
  * @author victor, ashwini and alex
  */
 public class OntologyMatcher {
 
     private Reasoner reasoner = null;
     private String ontLocation = "file:src/SUMO.owl";
     private OWLOntologyManager manager = null;
     private OWLOntology ontology = null;
     private MyOntManager ontsum = null;
     private static OntologyMatcher instance = null;
 
     // public methods
     public static OntologyMatcher getInstance() {
         if (instance == null) {
             instance = new OntologyMatcher();
         }
         return instance;
     }
 
     public float getScore(String clsName1, String clsName2) {
         HashMap<String, OWLClass> mapName_OWLClass = ontsum.loadClasses(reasoner);
 
         OWLClass cls1 = mapName_OWLClass.get(clsName1.toLowerCase());
         OWLClass cls2 = mapName_OWLClass.get(clsName2.toLowerCase());
        
        if (cls1 == null || cls2 == null) {
            System.err.println("One or both classes don't exist on the ontology");
            return 0.0f;
        }
 
         /*
          System.out.println(reasoner.isSubClassOf(cls1, cls2));
          System.out.println(reasoner.isSubClassOf(cls2, cls1));
          System.out.println(cls1+"-"+cls2); // for the moment cs1 is Cinput and cs2 is Coutput
          */
 
         //Vector<OWLObjectProperty> objprops = ontsum.findRelationship(cls1, cls2, reasoner);
         OntologyResult match = matching(cls1, cls2);
         System.out.println("Matching results are:   " + match);
 
         return matchingDegree(match);
 
     }
 
     // private methods
     private OntologyMatcher() {
         initializeOntology();
     }
 
     private void initializeOntology() {
         ontsum = new MyOntManager();
         manager = ontsum.initializeOntologyManager();
         ontology = ontsum.initializeOntology(manager, ontLocation);
         reasoner = ontsum.initializeReasoner(ontology, manager);
     }
 
     private OntologyResult matching(OWLClass a, OWLClass b) { //a=cl1,b=cl2
 
         /* if  (reasoner.isSameAs(b.asOWLIndividual(), a.asOWLIndividual())== true){
          return "Exact";  // TODO need to solve isssue
          }else*/ if (reasoner.isSubClassOf(b, a)) {
             return OntologyResult.PlugIn;
         } else if (reasoner.isSubClassOf(a, b)) {
             return OntologyResult.Subsumption;
         } else if (reasoner.isEquivalentClass(a, b)) {
             return OntologyResult.Subsumption;
         }
 
         // TODO we should also do for has relation ship
         return OntologyResult.NotMatched;
 
 
     }
 
     /**
      * No Standard rule for degrees, so for the time being using the one defined
      * on Project presentation, slide #10
      *
      * @param match
      * @return
      */
     private float matchingDegree(OntologyResult match) {
         switch (match) {
             case Exact:
                 return 1.0f;
             case Subsumption:
                 return 0.8f;
             case PlugIn:
                 return 0.6f;
             case Structural:
                 return 0.5f;
             case NotMatched:
                 return 0.0f;
             default:
                 return 0.0f;
         }
     }
 }
