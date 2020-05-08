 /**
  * 
  */
 package at.jku.ce.ue.source.entities;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import at.jku.ce.ue.source.businessLogic.BOMServiceUtil;
 import at.jku.ce.ue.source.businessLogic.impl.BOMServiceUtilImpl;
 
 /**
  * @author Schnitzi
  * 
  */
 public class Database {
 
 	private static Logger log = Logger.getLogger("Database");
 
 	private static final int PRODUCER_COUNT = 15;
 
 	private Map<String, Producer> producers;
 
 	private static Database database;
 
 	private Map<String, Part> partsOnPlattform;
 	
 	public Database() {
 
 		this.producers = new HashMap<String, Producer>();
 
 		fillWithData();
 
 	}
 
 	public static Database getInstance() {
 		if (database == null) {
 			database = new Database();
 		}
 
 		return database;
 	}
 
 	private void fillWithData() {
 
 		createProducers();
 
 		producePartsForProducer();
 		
 	}
 
 	private void createProducers() {
 		for (int i = 0; i < PRODUCER_COUNT; i++) {
 
 			Producer prod = new Producer("GW01Producer" + i, "GW01Producer" + i);
 
 			this.producers.put("GW01Producer"+i, prod);
 
 		}
 	}
 
 	private void producePartsForProducer() {
 
 		LinkedList<Part> parts = new LinkedList<Part>();
 
 		BOMServiceUtil bomService = new BOMServiceUtilImpl();
 
 		partsOnPlattform = new HashMap<String, Part>();
 
 		// List of all parts
 		List<String> productList = bomService.getAllPartsOfBOM();
 		log.severe("PART COUNT: " + productList.size());
 		int count = 0;
 		// Iterator through all parts and put every part and a list of it's
 		// subparts in the map
 		for (String partName : productList) {
 			Random rand = new Random();
 
 			Part part = null;
 			for (Part partInList : partsOnPlattform.values()) {
 				if (partInList.getName().equals(partName)) {
 					part = partInList;
 				}
 			}
 
 			if (part == null) {
 				int prodId = rand.nextInt(PRODUCER_COUNT);
 				part = new Part(count, partName, producers.get(prodId));
 				count += 1;
				producers.get(prodId).getParts().add(part);
 			}
 
 			// Get all subParts of actual looked part
 			List<String> subPartList = bomService
 					.getAllDirectSubpartsOfPart(partName);
 
 			// Iterate through all subParts of 'part'
 			for (String subPartName : subPartList) {
 
 				Part subPart = null;
 
 				// Check if 'subPartName' already exists as Part in this
 				// database
 				for (Part partInList : partsOnPlattform.values()) {
 
 					if (partInList.getName().equals(subPartName)) {
 						subPart = partInList;
 					}
 
 				}
 
 				if (subPart == null) {
 					int prodId = rand.nextInt(PRODUCER_COUNT);
 					subPart = new Part(count, subPartName,
 							producers.get(prodId));
 					count += 1;
					producers.get(prodId).getParts().add(part);
 				}
 
 				// Add 'subPart' as subpart of 'part'
 				if (part != null && subPart != null) {
 					part.getSubParts().add(subPart);
 				}
 			}
 
 			if (part != null)
 				partsOnPlattform.put(part.getIdString(), part);
 
 		}
 
 		printAllParts(partsOnPlattform);
 
 	}
 
 	private void printAllParts(Map<String, Part> allPartsWithSubParts) {
 
 		for (Part part : allPartsWithSubParts.values()) {
 			String outPut = "Part: No: " + part.getId() + part.getName();
 			// System.out.println("Part: " + part.getName());
 
 			if (part.getSubParts().size() > 0) {
 				for (Part subPart : part.getSubParts()) {
 					// System.out.println("\t+" + subPart.getName());
 					outPut += "\n\t+ No: " + subPart.getId()
 							+ subPart.getName();
 				}
 			} else {
 				// System.out.println("\t No more subParts");
 				outPut += "\n\t No more subParts";
 			}
 			System.out.println(outPut);
 		}
 
 	}
 
 	/**
 	 * @param producerID
 	 * @return
 	 */
 	public Producer getProducer(int producerID) {
 		return producers.get(producerID);
 	}
 
 	public Part getPart(String partID) {
 		return partsOnPlattform.get(partID);
 	}
 
 	public int registerProducer(String producerName, String password, String adress) {
 
 		int prodId = producerName.hashCode();
 
 		Producer producer = new Producer("GW01Producer"+prodId, producerName);
 
 		if (!producers.containsKey(prodId))
 			producers.put("GW01Producer"+prodId, producer);
 		else
 			log.info("Producer was not able to be registered!");
 		
 		return prodId;
 
 	}
 
 	public Producer getProducer(String prodId) {
 		return null;
 	}
 
 	/**
 	 * @return the producers
 	 */
 	public Map<String, Producer> getProducers() {
 		return producers;
 	}
 
 	/**
 	 * @param producers the producers to set
 	 */
 	public void setProducers(Map<String, Producer> producers) {
 		this.producers = producers;
 	}
 
 	public void addProducer(String name) {
 	}
 
 	/**
 	 * @return the partsOnPlattform
 	 */
 	public Map<String, Part> getPartsOnPlattform() {
 		return partsOnPlattform;
 	}
 
 	/**
 	 * @param partsOnPlattform the partsOnPlattform to set
 	 */
 	public void setPartsOnPlattform(Map<String, Part> partsOnPlattform) {
 		this.partsOnPlattform = partsOnPlattform;
 	}
 
 
 }
