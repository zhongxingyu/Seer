 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.transform.TransformerException;
 import org.apache.commons.io.FileUtils;
 import org.bridgedb.IDMapperException;
 import org.bridgedb.IDMapperStack;
 import org.pathvisio.model.ConverterException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 
 
 public class WP2RDFConversion {
 
 	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ServiceException, ClassNotFoundException, IDMapperException, ParseException, XMLStreamException, TransformerException, ConverterException {	
 		
 		/* 
 		 * Set the preference for this to work on your local machine
 		 */
 		Properties prop = new Properties();
 		prop.load(new FileInputStream("config.properties"));
 	    
 		/* We keep a three dimensional type of versioning for the RDF dumps of WikiPathways.
 		 * First there is the software version. This indicates the incremental updates or changes in the code
 		 * responsible for the RDF generation (these files)
 		 * The schema version is to keep track of changes in the underlying data model. Base are the vocabulary.wikipathways.org
 		 * The latest revision is the highest revision number of the pathways converted into RDF. A pathway with a
 		 * higer revision number is not yet available in RDF. 
 		 */
 		//TODO The versioning system is not fully implemented yet. 
 		int softwareVersion = 0;
 		int schemaVersion = 0;
 		int latestRevision = 0;
 		
 		/* WikiPathways covers different organism, which are stored as text labels (e.g. Homo sapiens). The 
 		 * The webservice call http://www.wikipathways.org/wpi/webservice/webservice.php/listOrganisms returns 
 		 * all the covered species. 
 		 * getOrganismsTaxonomyMapping(), returns the URI for each of these organism from NCBI's taxonomy by using 
 		 * eutils. 
 		 */
 		HashMap<String, String> organismTaxonomy = WpRDFFunctionLibrary.getOrganismsTaxonomyMapping();
 			
 		/* This serializer requires a single XML file containing all pathways. 
 		 * When downloaded however each pathway is captured in invidual GPML files
 		 * mergeGpmltoSingleFile concatenates all files in a given directory into one file (i.e. /tmp/WpGPML.xml)
 		 */
 		// 
                 WpRDFFunctionLibrary.mergeGpmltoSingleFile(prop.getProperty("wikipathwaysDownloadDumps"));
 		Document wikiPathwaysDom = basicCalls.openXmlFile("/tmp/WpGPML.xml");
 		//TODO These parameters should not be part of the code, but belong in preferences files yet to be implemented.
 
 		/* We use Jena (http://jena.apache.org/) as the API to generate RDF. 
 		 * Below both model and voidModel are initiated and the appropriate prefixes are set. 
 		 * Prefixes are not perse necessary. They allow for more readable URI in the generated triples
 		 * New prefixes are to be set in WpRDFFunctionLibrary.setModelPrefix.
 		 */
 		Model model = ModelFactory.createDefaultModel();
 		Model voidModel = ModelFactory.createDefaultModel();
 		Model openPhactsLinkSets = ModelFactory.createDefaultModel();
 		WpRDFFunctionLibrary.setModelPrefix(model);
 		WpRDFFunctionLibrary.setModelPrefix(voidModel);
 		WpRDFFunctionLibrary.populateVoid(voidModel, organismTaxonomy);
 		
 		/* Similar pathways entities can have different identifiers. To allow being able to 
 		 * recognize similarity we use bridgeDb (http://www.bridgedb.org). For each pathway element
 		 * unified identifier uri are set. 
 		 */
 		Model bridgeDbmodel = WpRDFFunctionLibrary.createBridgeDbModel();
 		IDMapperStack mapper = WpRDFFunctionLibrary.createBridgeDbMapper(prop);
 
 		
 		/* From here on the actual RDF conversion starts. The concatenated pathways into a single file is loaded and now
 		 * being processed in a strait forward way. First the pathway information is converted into RDF and then each individual
 		 * pathway element. 
 		 */
 		NodeList pathwayElements = wikiPathwaysDom.getElementsByTagName("Pathway");
 		for (int i=0; i<pathwayElements.getLength(); i++){
 			Model pathwayModel = WpRDFFunctionLibrary.createPathwayModel(); // create empty rdf model
 			String wpId = pathwayElements.item(i).getAttributes().getNamedItem("identifier").getTextContent();
 			String revision = pathwayElements.item(i).getAttributes().getNamedItem("revision").getTextContent();
 			String pathwayOrganism = "";
 			if (pathwayElements.item(i).getAttributes().getNamedItem("Organism") != null)
 				pathwayOrganism = pathwayElements.item(i).getAttributes().getNamedItem("Organism").getTextContent().trim();
             if (Integer.valueOf(revision) > latestRevision){
             	latestRevision = Integer.valueOf(revision);
             }
 			File f = new File(prop.getProperty("rdfRepository")+wpId+"_r"+revision+".ttl");
 			System.out.println(f.getName());
 			if(!f.exists()) {
 				Resource pwResource = WpRDFFunctionLibrary.addPathwayLevelTriple(pathwayModel, pathwayElements.item(i), organismTaxonomy);				
 				
 				// Get the comments
 				NodeList commentElements = ((Element) pathwayElements.item(i)).getElementsByTagName("Comment");
 				WpRDFFunctionLibrary.addCommentTriples(pathwayModel, pwResource, commentElements, wpId, revision);		
 				
 				// Get the Groups
 				NodeList groupElements = ((Element) pathwayElements.item(i)).getElementsByTagName("Group");
 				for (int n=0;n<groupElements.getLength(); n++){
 					WpRDFFunctionLibrary.addGroupTriples(pathwayModel, pwResource, groupElements.item(n), wpId, revision);
 				}
 	
 				// Get all the Datanodes
 				NodeList dataNodesElement = ((Element) pathwayElements.item(i)).getElementsByTagName("DataNode");
 				for (int j=0; j<dataNodesElement.getLength(); j++){
 					WpRDFFunctionLibrary.addDataNodeTriples(pathwayModel, pwResource, dataNodesElement.item(j), wpId, revision, bridgeDbmodel, mapper, openPhactsLinkSets);
 				}
 				
 				// Get all the lines
 				NodeList linesElement = ((Element) pathwayElements.item(i)).getElementsByTagName("Interaction");
 				for (int k=0; k<linesElement.getLength(); k++){
 					WpRDFFunctionLibrary.addLineTriples(pathwayModel, pwResource, linesElement.item(k), wpId, revision);
 				}
 				
 				//Get all the labels
 				NodeList labelsElement = ((Element) pathwayElements.item(i)).getElementsByTagName("Label");
 				for (int l=0; l<labelsElement.getLength(); l++){
 					WpRDFFunctionLibrary.addLabelTriples(pathwayModel, pwResource, labelsElement.item(l), wpId, revision);
 				}
 				
 				//Get the references. There are three casing examples of publicationxref, that is why the call is repeated three times. 
 				NodeList referenceElements = ((Element) pathwayElements.item(i)).getElementsByTagName("bp:PublicationXref");
 				for (int m=0; m<referenceElements.getLength(); m++){
 					WpRDFFunctionLibrary.addReferenceTriples(pathwayModel, pwResource, referenceElements.item(m), wpId, revision);
 				}
 				NodeList referenceElements2 = ((Element) pathwayElements.item(i)).getElementsByTagName("bp:publicationXref");
 				for (int m=0; m<referenceElements2.getLength(); m++){
 					WpRDFFunctionLibrary.addReferenceTriples(pathwayModel, pwResource, referenceElements2.item(m), wpId, revision);
 				}
 				NodeList referenceElements3 = ((Element) pathwayElements.item(i)).getElementsByTagName("bp:PublicationXRef");
 				for (int m=0; m<referenceElements3.getLength(); m++){
 					WpRDFFunctionLibrary.addReferenceTriples(pathwayModel, pwResource, referenceElements3.item(m), wpId, revision);
 				}
 
 				//Get the ontologies.
 				NodeList ontologyElements = ((Element) pathwayElements.item(i)).getElementsByTagName("bp:openControlledVocabulary");
 				for (int n=0; n<ontologyElements.getLength();n++){
 					WpRDFFunctionLibrary.addPathwayOntologyTriples(pathwayModel, pwResource, ontologyElements.item(n));
 				}
 				System.out.println(wpId);
 				basicCalls.saveRDF2File(pathwayModel, "/tmp/OPSWPRDF/"+wpId+"_r"+revision+".ttl", "TURTLE");
 				//TODO OPSWPRDF should be stored in a preference file.
 				
 				model.add(pathwayModel);
 				pathwayModel.removeAll();
 			}
 		}
 		Date myDate = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		String myDateString = sdf.format(myDate);
 		FileUtils.writeStringToFile(new File("latestVersion.txt"), "v"+schemaVersion+"."+softwareVersion+"."+latestRevision+"_"+myDateString);
 		basicCalls.saveRDF2File(model, "/tmp/wpContent_v"+schemaVersion+"."+softwareVersion+"."+latestRevision+"_"+myDateString+".ttl", "TURTLE");
 		basicCalls.saveRDF2File(voidModel, "/tmp/void.ttl", "TURTLE");
 		basicCalls.saveRDF2File(openPhactsLinkSets, "/tmp/opsLinkSets_v"+schemaVersion+"."+softwareVersion+"."+latestRevision+"_"+myDateString+".ttl", "TURTLE");
 	}
 
 }
