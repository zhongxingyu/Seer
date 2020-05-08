 package prov.interfaces;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.reasoner.Node;
 import org.semanticweb.owlapi.reasoner.NodeSet;
 import org.semanticweb.owlapi.util.OWLOntologyMerger;
 import org.semanticweb.owlapi.util.SimpleIRIMapper;
 import org.semanticweb.owlapi.model.AddImport;
 
 import util.JSONUtils;
 
 import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
 import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
 
 public class PROVGraph
 {
 	/** Hash map for all the nodes (URI -> number in graph) */
 	static HashMap<String, PROVIndividual> individualsHM = new HashMap<String, PROVIndividual>();
 
 	static OWLOntologyManager manager;
 	static PelletReasoner reasoner;
 
 	static String provPrefix = "http://www.w3.org/ns/prov#";
 
 	public String getGraph(String URI)
 	{
 		//build graph from URI
 		try
 		{
 			OWLOntologyManager tempManager = OWLManager.createOWLOntologyManager();
 
 			// Load PROV
 			IRI iri = IRI.create(URI);//"http://www.co-ode.org/ontologies/pizza/pizza.owl");
 
 			/*
 
 			OWLOntology ontology = tempManager.loadOntologyFromOntologyDocument(iri);
 			System.out.println("Loaded ontology: " + ontology);
 
 			//**import PROV ontology
 			//OWLOntology prov = manager.loadOntologyFromOntologyDocument(IRI.create("http://www.w3.org/ns/prov-o"));
 			OWLImportsDeclaration importDeclaraton = tempManager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.w3.org/ns/prov-o")); 
 			tempManager.applyChange(new AddImport(ontology, importDeclaraton));
 
 			 */
 			OWLImportsDeclaration importDeclaraton = tempManager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.w3.org/ns/prov-o"));
 
 			manager = OWLManager.createOWLOntologyManager();
 			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(iri);
 			//manager.makeLoadImportRequest(importDeclaraton, new OWLOntologyLoaderConfiguration() );
 			System.out.println("Loaded ontology: " + ontology);
 			//***
 
 
 			//Pellet Reasoner
 			System.out.println("----- Reasoner -----");
 
 			reasoner = PelletReasonerFactory.getInstance().createReasoner( ontology );
 			reasoner.getKB().realize();
 			reasoner.getKB().printClassTree();
 
 
 			//Activity
 			OWLClass provActivityClass = manager.getOWLDataFactory().getOWLClass( IRI.create(provPrefix+"Activity") );
 
 			//get Individuals of Activity class.
 			NodeSet<OWLNamedIndividual> activityIndividuals = reasoner.getInstances( provActivityClass , false);
 			recordActivities(activityIndividuals, ontology);
 
 
 			//Entity
 			OWLClass provEntityClass = manager.getOWLDataFactory().getOWLClass( IRI.create(provPrefix+"Entity") );
 
 			//get Individuals of Entity class.
 			NodeSet<OWLNamedIndividual> entityIndividuals = reasoner.getInstances( provEntityClass , false);
 			recordEntities(entityIndividuals, ontology);
 
 
 			//Agent
 			OWLClass provAgentClass = manager.getOWLDataFactory().getOWLClass( IRI.create(provPrefix+"Agent") );
 
 			//get Individuals of Entity class.
 			NodeSet<OWLNamedIndividual> agentIndividuals = reasoner.getInstances( provAgentClass , false);
 			recordAgents(agentIndividuals, ontology);
 
 
 			System.out.println("----- End of Reasoner Example -----");
 		} 
 		catch (OWLOntologyCreationException e) {
 			e.printStackTrace();
 		}
 
 		//convert Java object of Graph to JSON
 		return toJSON();
 	}
 
 	/** Gather information about all Activities. Individuals are placed in a HashMap (individualsHM: Key=URI, value = PROVIndividual) */
 	private void recordActivities(NodeSet<OWLNamedIndividual> individuals, OWLOntology ontology)
 	{
 		System.out.println("");
 		System.out.println("Activities");
 
 		for(Node<OWLNamedIndividual> sameInd : individuals) 
 		{
 			//Rep Individual
 			OWLNamedIndividual ind = sameInd.getRepresentativeElement();
 
 			PROVIndividual provInd;
 			
 			//if not already created
 			if(!individualsHM.containsKey( ind.toStringID() ))
 			{
 				provInd = new PROVIndividual(ind.toStringID());
 			
 				PROVGraph.individualsHM.put(ind.toStringID(), provInd);
 			}
 			else//already created..
 			{
 				provInd = individualsHM.get(ind.toStringID());
 			}
 			
 			//Add Activity connections and details to PROVIndividual
 			provInd.addActivity(ind);
 			provInd.setGroup("Activity");
 
 			provInd.printObjectProperties();
 		}
 
 	}
 
 
 	/** Gather information about all Entities. Individuals are placed in a HashMap (individualsHM: Key=URI, value = PROVIndividual) */
 	private void recordEntities(NodeSet<OWLNamedIndividual> individuals, OWLOntology ontology)
 	{
 
 		System.out.println("");
 		System.out.println("Entities");
 
 		for(Node<OWLNamedIndividual> sameInd : individuals) 
 		{
 			//Rep Individual
 			OWLNamedIndividual ind = sameInd.getRepresentativeElement();
 			
 			PROVIndividual provInd;
 			
 			//if not already created
 			if(!individualsHM.containsKey( ind.toStringID() ))
 			{
 				provInd = new PROVIndividual(ind.toStringID());
 			
 				PROVGraph.individualsHM.put(ind.toStringID(), provInd);
 			}
 			else//already created..
 			{
 				provInd = individualsHM.get(ind.toStringID());
 			}
 			
 			//Add Entity connections and details to PROVIndividual
 			provInd.addEntity(ind);
 			provInd.setGroup("Entity");
 
 			provInd.printObjectProperties();
 		}
 	}
 
 
 	/** Gather information about all Agents. Individuals are placed in a HashMap (individualsHM: Key=URI, value = PROVIndividual) */
 	private void recordAgents(NodeSet<OWLNamedIndividual> individuals, OWLOntology ontology)
 	{
 
 		System.out.println("");
 		System.out.println("Agents");
 
 		for(Node<OWLNamedIndividual> sameInd : individuals) 
 		{
 			//Rep Individual
 			OWLNamedIndividual ind = sameInd.getRepresentativeElement();
 
 			PROVIndividual provInd;
 			
 			//if not already created
 			if(!individualsHM.containsKey( ind.toStringID() ))
 			{
 				provInd = new PROVIndividual(ind.toStringID());
 			
 				PROVGraph.individualsHM.put(ind.toStringID(), provInd);
 			}
 			else//already created..
 			{
 				provInd = individualsHM.get(ind.toStringID());
 			}
 			
 			//Add Agent connections and details to PROVIndividual
 			provInd.addAgent(ind);
 			provInd.setGroup("Agent");
 
 			provInd.printObjectProperties();
 		}
 	}
 
 
 	public String toJSON()
 	{
 		//convert HM to Individual Array
 		Set<String> keySet = individualsHM.keySet();
 
 		PROVIndividual[] orderedInds = new PROVIndividual[ PROVIndividual.getLastID() ];
 
 		for(String uriKey : keySet)
 		{
 			PROVIndividual ind = individualsHM.get(uriKey);
 
 			int index = ind.id;
 			System.out.println(index +" / "+ orderedInds.length);
 			orderedInds[index] = ind;
 		}
 
 		//JSON
 		String json = "";
 
 		String nodes = "\"nodes\":[";
 
 		//nodes
 		for(int i=0; i < orderedInds.length; i++)
 		{
 			if(orderedInds[i] != null)
 			{
 				String indJSON = orderedInds[i].toJSON();
 
 				nodes += indJSON+",";
 			}
 			else
 			{
 				String indJSON = " \"name\" : \""+ "null" +"\", " +
 						" \"indID\" : \""+ i +"\", " +
 						" \"group\" : \""+ "NullGroup" +"\" ";
 
 				nodes += "{"+indJSON+"},";
 			}
 		}
 		//remove last comma
 		nodes = nodes.substring(0, nodes.length() - 1);
 
 
 		//links
 		String links = "";
 
 		/**DataProperties are in addition to the orderedInds.**/
 		int dataPropertiesCntr = 0;
 
 		for(int i=0; i < orderedInds.length; i++)
 		{
 			if(orderedInds[i] != null)
 			{
 				//Data Properties
 				ArrayList<String> DPconns = orderedInds[i].DPconnections;
 				ArrayList<String> DPvalues = orderedInds[i].DPconnectionValues;
 				for(int connCntr=0; connCntr < DPconns.size(); connCntr++)
 				{
 					//add Data Property to Nodes
 					int targetid = orderedInds.length + dataPropertiesCntr;
 
 					String DPnodeJSON = " \"name\" : \""+ DPvalues.get(connCntr) +"\", " +
 							" \"indID\" : \""+ targetid +"\", " +
 							//" \"types\" : \""+ types.toJSON() +"\", " +
 							" \"group\" : \""+ "DataProperty" +"\" ";
 
 					nodes += ",{ "+ DPnodeJSON +" }";
 
 					//then add link
 					String linkJSON = "";
 
 					int source = orderedInds[i].id;
 					String predicate = DPconns.get(connCntr);
 
					linkJSON = 	" \"source\" : "+ source +", " +
							" \"target\" : "+ targetid +", " +
 							//" \"types\" : \""+ types.toJSON() +"\", " +
 							" \"predicate\" : \""+ predicate +"\" ";
 
 					linkJSON = "{ "+ linkJSON +" }";
 					links += linkJSON+",";
 
 					dataPropertiesCntr++;
 				}
 
 				//Object Properties
 				ArrayList<String> OPconns = orderedInds[i].OPconnections;
 				ArrayList<PROVIndividual> OPvalues = orderedInds[i].OPconnectionObjectInds;
 				for(int connCntr=0; connCntr < OPconns.size(); connCntr++)
 				{
 					//add OP link
 					String linkJSON = "";
 
 					int source = orderedInds[i].id;
 					int targetid = OPvalues.get(connCntr).id;
 					String predicate = OPconns.get(connCntr);
 
					linkJSON = 	" \"source\" : "+ source +", " +
							" \"target\" : "+ targetid +", " +
 							//" \"types\" : \""+ types.toJSON() +"\", " +
 							" \"predicate\" : \""+ predicate +"\" ";
 
 					linkJSON = "{ "+ linkJSON +" }";
 					links += linkJSON+",";
 				}
 			}
 		}
 
 		//remove last comma
 		links = links.substring(0, links.length() - 1);
 
 		//put nodes and links together
 		json += nodes + "], \"links\":[ " + links + "]";
 
 		return "{ "+ json +" }";
 
 	}
 
 
 	public static void main(String args[])
 	{
 		//WPJustificationTree tree = new WPJustificationTree(\"http://rio.cs.utep.edu/ciserver/ciprojects/GravityMapProvenance/gravityContourMap.ps_038568341971146025.owl#answer\");
 		PROVGraph test = new PROVGraph();
 		System.out.println( test.getGraph("https://raw.github.com/hdporras/Web-Probe/master/primer-turtle-examples.ttl") );
 
 
 		System.out.println();
 		System.out.println("----JSON----");
 		System.out.println( test.toJSON() );
 	}
 
 }
