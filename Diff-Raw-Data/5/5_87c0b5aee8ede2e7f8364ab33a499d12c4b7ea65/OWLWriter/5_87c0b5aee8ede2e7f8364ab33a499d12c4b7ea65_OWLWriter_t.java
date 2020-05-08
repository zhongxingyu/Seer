 package de.elog.elReasoner;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.AddAxiom;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
 import org.semanticweb.owlapi.model.OWLObjectProperty;
 import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
 import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyStorageException;
 import org.semanticweb.owlapi.util.SimpleIRIMapper;
 
 import de.elog.Constants;
 
 public class OWLWriter {
 	private OWLDataFactory factory;
 	private OWLOntologyManager manager;
 	
 	public OWLWriter(){
 		manager  = OWLManager.createOWLOntologyManager();
 		factory = manager.getOWLDataFactory();
 	}
 	
 	public OWLOntology write(IRI ontologyIRI, String filenameToSave, ArrayList<String> axioms) throws OWLOntologyCreationException, OWLOntologyStorageException{
 		// Create the document IRI for our ontology
 		File fileToSave = new File(filenameToSave);
		IRI documentIRI = IRI.create(fileToSave.toURI());
 		// Set up a mapping, which maps the ontology to the document IRI
 		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
 		manager.addIRIMapper(mapper);
 		
 		// Now create the ontology - we use the ontology IRI (not the physical URI)
 		OWLOntology ontology = manager.createOntology(ontologyIRI);
 		
 		for(String axiomWhole: axioms){
 			String[] axiom = axiomWhole.split("\\|");
 			OWLAxiom owlAxiom = null;
 			
 			if(axiom[0].equals("subsumes")){
 				owlAxiom = this.getSubsumes(axiom);
 			}else if(axiom[0].equals("intersection")){
 				owlAxiom = this.getIntersection(axiom);				
 			}else if(axiom[0].equals("opsub")){
 				owlAxiom = this.getOpsub(axiom);
 			}else if(axiom[0].equals("opsup")){
 				owlAxiom = this.getOpsup(axiom);				
 			}else if(axiom[0].equals("psubsumes")){
 				owlAxiom = this.getPsubsumes(axiom);
 			}else if(axiom[0].equals("pcom")){
 				owlAxiom = this.getPcom(axiom);
 			}else{
 				System.out.println("- " + axiomWhole);
 			}
 			
 			if(owlAxiom!=null){
 				AddAxiom addAxiom = new AddAxiom(ontology, owlAxiom);
 				manager.applyChange(addAxiom);
 			}
 		}
 		
 		// Now save the ontology.
		manager.saveOntology(ontology, documentIRI);
 		System.out.println("Filename of output: "+ filenameToSave);
 		return ontology;
 	}
 	
 	private OWLClass createClass(String name){
 		if(name.equals(Constants.BOTTOM_ELEMENT)){
 			return factory.getOWLNothing();
 		}else if(name.equals(Constants.TOP_ELEMENT)){
 			return factory.getOWLThing();
 		}else{
 			IRI iri = IRI.create(name);
 			return factory.getOWLClass(iri);
 		}
 	}
 	
 	private OWLObjectProperty createObjectProperty(String name){
 		return factory.getOWLObjectProperty(IRI.create(name));
 	}
 	
 	private OWLAxiom getSubsumes(String[] components){
 		if(components[1].equals(components[2])) {
 			return null;
 		}
 		
 		return factory.getOWLSubClassOfAxiom(this.createClass(components[1]), this.createClass(components[2]));
 	}
 	
 	private OWLAxiom getIntersection(String[] components){
 		OWLClass c1 = this.createClass(components[1]);
 		OWLClass c2 = this.createClass(components[2]);
 		OWLClass d = this.createClass(components[3]);
 		if(d.equals(factory.getOWLNothing())){
 			return factory.getOWLDisjointClassesAxiom(c1, c2);
 		}else{
 			OWLObjectIntersectionOf objectIntersectionOf = factory.getOWLObjectIntersectionOf(c1, c2);
 			return factory.getOWLSubClassOfAxiom(objectIntersectionOf, d);
 		}
 		
 	}
 	
 	/**
 	 *  Exists R.C1 subclassof D
 	 * 
 	 * @param components
 	 * @return
 	 */
 	private OWLAxiom getOpsub(String[] components){
 		OWLObjectProperty r = this.createObjectProperty(components[1]);
 		OWLClass c1 = this.createClass(components[2]);
 		OWLClass d = this.createClass(components[3]);
 		OWLObjectSomeValuesFrom objectSomeValuesFrom = factory.getOWLObjectSomeValuesFrom(r, c1);
 		return factory.getOWLSubClassOfAxiom(objectSomeValuesFrom, d);
 	}
 	
 	/**
 	 *  Exists R.C1 superclassof D
 	 * 
 	 * which is equivalent to:
 	 * 
 	 *  D subclassof Exists R.C1
 	 *  
 	 * @param components
 	 * @return
 	 */
 	private OWLAxiom getOpsup(String[] components){
 		OWLObjectProperty r = this.createObjectProperty(components[1]);
 		OWLClass c1 = this.createClass(components[2]);
 		OWLClass d = this.createClass(components[3]);
 		OWLObjectSomeValuesFrom objectSomeValuesFrom = factory.getOWLObjectSomeValuesFrom(r, c1);
 		return factory.getOWLSubClassOfAxiom(d, objectSomeValuesFrom);
 	}
 	
 	/**
 	 *  
 	 * @param components
 	 * @return
 	 */
 	private OWLAxiom getPcom(String[] components){
 		
 		OWLObjectPropertyExpression opExp1 = this.createObjectProperty(components[1]);
 		OWLObjectPropertyExpression opExp2 = this.createObjectProperty(components[2]);
 		ArrayList<OWLObjectPropertyExpression> opExpList = new ArrayList<OWLObjectPropertyExpression>();
 		opExpList.add(opExp1);opExpList.add(opExp2);
 
 		OWLObjectPropertyExpression opSuper = this.createObjectProperty(components[3]);
 		
 		return factory.getOWLSubPropertyChainOfAxiom(opExpList, opSuper);
 	}
 	
 	private OWLAxiom getPsubsumes(String[] components){
 		if(components[1].equals(components[2])) return null;
 		
 		return factory.getOWLSubObjectPropertyOfAxiom(this.createObjectProperty(components[1]), this.createObjectProperty(components[2]));
 	}
 	
 
 	
 }
