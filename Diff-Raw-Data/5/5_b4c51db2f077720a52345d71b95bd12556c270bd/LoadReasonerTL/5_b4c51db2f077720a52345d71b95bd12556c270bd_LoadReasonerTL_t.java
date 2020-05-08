 package de.langenmaier.u2r3.tests.util;
 
 import de.langenmaier.u2r3.core.U2R3Reasoner;
 import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.reasoner.NodeSet;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 
 import java.io.File;
 
 
 public class LoadReasonerTL {
 
     public static final String LOG_URI = "http://www.polizei.hessen.de/CRIME/DomusAG#";
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         try {
             if ((new File("log4j.properties")).exists()) {
                 PropertyConfigurator.configure("log4j.properties");
             } else {
                 BasicConfigurator.configure();
             }
             Logger.getRootLogger().setLevel(Level.INFO);
             Logger logger = Logger.getLogger(LoadReasoner.class);
             logger.info("Java loaded ");
 
             if (args.length <= 0) {
                 System.err.println("USAGE: java " + LoadReasoner.class.getName() + " <filename>");
                 return;
             }
 
             OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
 
             OWLOntology ont;
             ont = manager.loadOntology(IRI.create(args[0]));
             logger.info("OWLAPI loaded " + ont.getOntologyID());
 
             OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
             U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(ont);
             //reasoner.loadOntologies(Collections.singleton(ont));
             logger.info("Ontology loaded in DB");
 
             reasoner.prepareReasoner();
 
             logger.info("FERTIG");
 
             OWLDataFactory factory = manager.getOWLDataFactory();
 
             long start = System.currentTimeMillis();
             OWLClass Deutscher = factory.getOWLClass(IRI.create(LOG_URI + "Deutscher"));
             NodeSet<OWLNamedIndividual> ind2 = reasoner.getInstances(Deutscher, false);
             System.out.println("Deutscher / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                     " no of results: " + ind2.getFlattened().size());
 
            OWLClass orte = factory.getOWLClass(IRI.create(LOG_URI + "Ort"));
             start = System.currentTimeMillis();
             NodeSet<OWLNamedIndividual> ind = reasoner.getInstances(orte, true);
             System.out.println("Orte / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                     " no of results: " + ind.getFlattened().size());
             //for (OWLNamedIndividual in : ind.getFlattened()) {
             //    System.out.println("    " + in);
             //}
 
             start = System.currentTimeMillis();
             OWLObjectProperty lib = factory.getOWLObjectProperty(IRI.create(LOG_URI + "liegt-in-bundesland"));
             OWLNamedIndividual hessen = factory.getOWLNamedIndividual(IRI.create(LOG_URI + "Hessen"));
            ind = reasoner.getObjectPropertyValues(hessen, lib.getInverseProperty());
             System.out.println("Orte in Hessen / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                     " no of results: " + ind.getFlattened().size());
             for (OWLNamedIndividual in : ind.getFlattened()) {
                 System.out.println("    " + in);
             }
 
             start = System.currentTimeMillis();
             OWLClass verd = factory.getOWLClass(IRI.create(LOG_URI + "Verdaechtiger"));
             OWLObjectProperty adr = factory.getOWLObjectProperty(IRI.create(LOG_URI + "hat-adresse"));
             OWLObjectProperty ort = factory.getOWLObjectProperty(IRI.create(LOG_URI + "ort"));
             OWLObjectProperty bliegt = factory.getOWLObjectProperty(IRI.create(LOG_URI + "liegt-in-bundesland"));
 
             OWLClassExpression orthes = factory.getOWLObjectHasValue(bliegt, hessen);
             OWLClassExpression someoh = factory.getOWLObjectSomeValuesFrom(ort, orthes);
             OWLClassExpression hasvh = factory.getOWLObjectSomeValuesFrom(adr, someoh);
             OWLClassExpression verdadr = factory.getOWLObjectIntersectionOf(verd, hasvh);
 
             NodeSet<OWLNamedIndividual> verdhss = reasoner.getInstances(verdadr, false);
             System.out.println("Verd. mit Adr in Hessen Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                     " no of results: " + verdhss.getFlattened().size());
 //        for (OWLIndividual in : verdhss) {
 //            System.out.println(in);
 //        }
 
         } catch (OWLOntologyCreationException e) {
             e.printStackTrace();
         }
 
     }
 
 
 }
