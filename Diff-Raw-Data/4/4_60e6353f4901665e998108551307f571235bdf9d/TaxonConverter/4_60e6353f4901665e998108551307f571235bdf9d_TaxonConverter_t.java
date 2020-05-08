 package org.iucn.sis.shared.conversions;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.hibernate.HibernateException;
 import org.iucn.sis.server.api.io.InfratypeIO;
 import org.iucn.sis.server.api.io.IsoLanguageIO;
 import org.iucn.sis.server.api.io.ReferenceIO;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.utils.FormattedDate;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Infratype;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.helpers.CommonNameData;
 import org.iucn.sis.shared.helpers.Note;
 import org.iucn.sis.shared.helpers.ReferenceUI;
 import org.iucn.sis.shared.helpers.SynonymData;
 import org.iucn.sis.shared.helpers.TaxonNode;
 import org.iucn.sis.shared.helpers.TaxonNodeFactory;
 
 import com.solertium.lwxml.factory.NativeDocumentFactory;
 import com.solertium.lwxml.java.JavaNativeDocument;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.TrivialExceptionHandler;
 
 public class TaxonConverter extends GenericConverter<String> {
 	
 	public static void main(String[] args) throws Exception {
 		//List<File> allFiles = FileListing.main("/home/iucn/complete_vfs/HEAD/browse/nodes");
 		List<File> allFiles = new ArrayList<File>();
 		allFiles.add(new File("/home/iucn/complete_vfs/HEAD/browse/nodes/166/166892.xml"));
 		allFiles.add(new File("/home/iucn/complete_vfs/HEAD/browse/nodes/166/166900.xml"));
 		allFiles.add(new File("/home/iucn/complete_vfs/HEAD/browse/nodes/172/172323.xml"));
 		int size = allFiles.size();
 		
 		Map<Integer, Integer> childToParent = new HashMap<Integer, Integer>();
 		
 		for (int i = 0; i < size; i++) {
 			File file = allFiles.get(i);
 			if (file.getPath().endsWith(".xml")) {
 				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
 				ndoc.parse(FileListing.readFileAsString(file));
 				TaxonNode taxon = TaxonNodeFactory.createNode(ndoc);
 				
 				Taxon newTaxon = new Taxon();
 				newTaxon.state = Taxon.ACTIVE;
 				newTaxon.setId((int) taxon.getId());
 				if (!taxon.getParentId().equals("")) {
 					newTaxon.setParentId(Integer.valueOf(taxon.getParentId()));
 					//Taxon parent = new Taxon();
 					//parent.setId(Integer.valueOf(taxon.getParentId()));
 					//parent.setName(taxon.getParentName());
 				
 				}
 				newTaxon.setStatus(taxon.getStatus());
 				newTaxon.setTaxonLevel(TaxonLevel.getTaxonLevel(taxon.getLevel()));
 				newTaxon.setName(taxon.getName());
 				try {
 					newTaxon.setFriendlyName(taxon.generateFullName());
 				} catch (IndexOutOfBoundsException e) {
 					System.out.println("--- ERROR setting friendly name for taxon " + newTaxon.getId());
 				}
 				newTaxon.setHybrid(taxon.isHybrid());
 				newTaxon.setTaxonomicAuthority(taxon.getTaxonomicAuthority());
 
 				// ADD COMMON NAMES
 				boolean bad = false;
 				int longestLength = 2000;
 				for (CommonNameData commonNameData : taxon.getCommonNames()) {
 					CommonName commonName = new CommonName();
 					commonName.setChangeReason(commonNameData.getChangeReason());
 					commonName.setName(commonNameData.getName());			
 					commonName.setPrincipal(commonNameData.isPrimary());
 					commonName.setValidated(commonNameData.isValidated());
 
 					// ADD ISO LANGUAGUE
 					int len = commonName.getName().length();
 					if (len > longestLength)
 						longestLength = commonName.getName().length();
 							
 				}
 				if (longestLength > 2000)
 					System.out.println("Found bad common name with length " + longestLength + " for " + file.toString());
 				
 				if (i % 1000 == 0)
 					System.out.println(i + "...");
 			}
 		}
 	}
 	
 	private UserIO userIO;
 	private IsoLanguageIO isoLanguageIO;
 	private InfratypeIO infratypeIO;
 	private ReferenceIO referenceIO;
 	
 	private AtomicInteger taxaConverted;
 	private BufferedWriter assessments;
 	
 	public TaxonConverter() {
 		super();
 		setClearSessionAfterTransaction(true);
 	}
 	
 	public void convertAllFaster() throws Exception {
 		/*
 		 * First, add 'em all, no relationships.
 		 */
 		userIO = new UserIO(session);
 		isoLanguageIO = new IsoLanguageIO(session);
 		infratypeIO = new InfratypeIO(session);
 		referenceIO = new ReferenceIO(session);
 		taxaConverted = new AtomicInteger(0);
 		
 		File file = new File(data + "/HEAD/migration/assessments.dat");
 		file.getParentFile().mkdirs();
 		
 		assessments = new BufferedWriter(new PrintWriter(
 			new FileWriter(file)
 		));
 		
 		Map<Integer, Integer> childToParent = new HashMap<Integer, Integer>();
 		final User user = userIO.getUserFromUsername("admin");
 		final AtomicInteger converted = new AtomicInteger(0);
 		
 		File folder = new File(data + "/HEAD/browse/nodes");
 		
 		readFolder(childToParent, user, converted, folder);
 		
 		commitAndStartTransaction();
 		
 		try {
 			assessments.close();
 		} catch (IOException e) {
 			TrivialExceptionHandler.ignore(this, e);
 		}
 		
 		taxaConverted.set(0);
 		
 		for (Map.Entry<Integer, Integer> entry : childToParent.entrySet()) {
 			if (entry.getValue().intValue() == 0)
 				continue;
 			
 			Taxon taxon = (Taxon)session.get(Taxon.class, entry.getKey());
 			
 			if (taxon.getLevel() == TaxonLevel.KINGDOM)
 				continue;
 			
 			Taxon parent = (Taxon)session.get(Taxon.class, entry.getValue());
 			if (parent == null)
 				continue;
 			
 			if (parent.getLevel() == TaxonLevel.KINGDOM) {
 				Integer trueParent = null;
 				if ("ANIMALIA".equals(parent.getName()))
 					trueParent = 100003;
 				else if ("FUNGI".equals(parent.getName()))
 					trueParent = 100001;
 				else if ("PLANTAE".equals(parent.getName()))
 					trueParent = 100002;
 				else if ("PROTISTA".equals(parent.getName()))
 					trueParent = 100004;
 			
 				if (trueParent != null) {
 					if (parent.getId() != trueParent.intValue())
 						parent = (Taxon)session.get(Taxon.class, trueParent);
 					/*TaxonDAO.deleteAndDissociate(taxon, session);
 					commitAndStartTransaction();*/
 					//parent = (Taxon)session.get(Taxon.class, trueParent);
 				}
 					
 			}
 			
 			if (taxon != null)
 				taxon.setParent(parent);
 			if (taxon.getParent() != null) {
 				session.update(taxon);
 				commitAndStartTransaction();
 			}
 			else
 				printf("No parent found for taxon %s at level %s with parent id %s", taxon.getId(), taxon.getTaxonLevel(), entry.getValue());
 			
 			if (taxaConverted.incrementAndGet() % 100 == 0) {
 				printf("Updated %s taxa...", taxaConverted.get());
 			}
 		}
 	}
 	
 	private void readFolder(Map<Integer, Integer> childToParent, User user, AtomicInteger converted, File folder) throws Exception {
 		for (File file : folder.listFiles()) {
 			if (file.isDirectory())
 				readFolder(childToParent, user, converted, file);
 			else if (file.getName().endsWith(".xml"))
 				readFile(childToParent, user, converted, file);
 		}
 	}
 	
 	private void readFile(Map<Integer, Integer> childToParent, User user, AtomicInteger converted, File file) throws Exception {
 		NativeDocument ndoc = new JavaNativeDocument();
 		ndoc.parse(FileListing.readFileAsString(file));
 		
 		Taxon taxon = convertTaxonNode(TaxonNodeFactory.createNode(ndoc), new Date(file.lastModified()), user);
 		
 		if (taxon != null) {
 			if (taxon.getLevel() == TaxonLevel.KINGDOM) {
 				Integer trueParent = null;
 				if ("ANIMALIA".equals(taxon.getName()))
 					trueParent = 100003;
 				else if ("FUNGI".equals(taxon.getName()))
 					trueParent = 100001;
 				else if ("PLANTAE".equals(taxon.getName()))
 					trueParent = 100002;
 				else if ("PROTISTA".equals(taxon.getName()))
 					trueParent = 100004;
 			
 				if (trueParent != null && taxon.getId() != trueParent.intValue())
 					return;
 			}
 			childToParent.put(taxon.getId(), taxon.getParentId());
 			taxon.setParentId(0);
 			taxon.setParent(null);
 			
 			if (taxon.getLastEdit() == null) {
 				Edit edit = new Edit("Data migration.");
 				edit.setUser(user);
 				edit.getTaxon().add(taxon);
 				taxon.getEdits().add(edit);							
 			}
 			
 			Collection<Assessment> assessments = new ArrayList<Assessment>(taxon.getAssessments());
 			taxon.setAssessments(new HashSet<Assessment>());
 			
 			session.save(taxon);
 			
 			if (!assessments.isEmpty()) {
 				for (Assessment assessment : assessments)
 					this.assessments.write(taxon.getId() + ":" + assessment.getInternalId() + "\n");
 			}
 			
 			if (taxaConverted.incrementAndGet() % 100 == 0) {
 				commitAndStartTransaction();
 				printf("Converted %s taxa...", taxaConverted.get());
 			}
 		}
 	}
 	
 	protected void run() throws Exception {
 		convertAllFaster();
 		/*
 		userIO = new UserIO(session);
 		isoLanguageIO = new IsoLanguageIO(session);
 		infratypeIO = new InfratypeIO(session);
 		referenceIO = new ReferenceIO(session);
 		taxaConverted = new AtomicInteger(0);
 		
 		User user = userIO.getUserFromUsername("admin");
 		if (user == null)
 			throw new NullPointerException("No user admin exists");
 		
 		Date date = Calendar.getInstance().getTime();
 		
 		List<File> allFiles = FileListing.main(data + "/HEAD/browse/nodes");
 		int size = allFiles.size();
 		
 		Map<Integer, Integer> childToParent = new HashMap<Integer, Integer>();
 		
 		for (File file : allFiles) {
 			if (file.getPath().endsWith(".xml")) {
 				NativeDocument ndoc = new JavaNativeDocument();
 				ndoc.parse(FileListing.readFileAsString(file));
 				
 				Taxon taxon = convertTaxonNode(TaxonNodeFactory.createNode(ndoc), new Date(file.lastModified()));
 				
 				if (taxon != null) {
 					childToParent.put(taxon.getId(), taxon.getParentId());
 					taxon.setParentId(0);
 					taxon.setParent(null);
 					
 					if (taxon.getLastEdit() == null) {
 						Edit edit = new Edit();
 						edit.setUser(user);
 						edit.setCreatedDate(date);
 						edit.getTaxon().add(taxon);
 						taxon.getEdits().add(edit);							
 					}
 					
 					session.save(taxon);
 					
 					if (taxaConverted.incrementAndGet() % 100 == 0) {
 						commitAndStartTransaction();
 						printf("Converted %s/%s taxa...", taxaConverted.get(), size);
 					}
 				}
 			}
 		}
 		
 		commitAndStartTransaction();
 		
 		taxaConverted.set(0);
 		
 		for (Map.Entry<Integer, Integer> entry : childToParent.entrySet()) {
 			if (entry.getValue().intValue() == 0)
 				continue;
 			
 			Taxon taxon = (Taxon)session.get(Taxon.class, entry.getKey());
 			
 			if (taxon.getLevel() == TaxonLevel.KINGDOM)
 				continue;
 			
 			Taxon parent = (Taxon)session.get(Taxon.class, entry.getValue());
 			if (parent == null)
 				continue;
 			
 			if (parent.getLevel() == TaxonLevel.KINGDOM) {
 				Integer trueParent = null;
 				if ("ANIMALIA".equals(parent.getName()))
 					trueParent = 100003;
 				else if ("FUNGI".equals(parent.getName()))
 					trueParent = 100001;
 				else if ("PLANTAE".equals(parent.getName()))
 					trueParent = 100002;
 				else if ("PROTISTA".equals(parent.getName()))
 					trueParent = 100004;
 			
 				if (trueParent != null) {
 					session.delete(parent);
 					commitAndStartTransaction();
 					parent = (Taxon)session.get(Taxon.class, trueParent);
 				}
 					
 			}
 			
 			if (taxon != null)
 				taxon.setParent((Taxon)session.get(Taxon.class, entry.getValue()));
 			if (taxon.getParent() != null) {
 				session.update(taxon);
 				commitAndStartTransaction();
 			}
 			else
 				printf("No parent found for taxon %s at level %s with parent id %s", taxon.getId(), taxon.getTaxonLevel(), entry.getValue());
 			
 			if (taxaConverted.incrementAndGet() % 100 == 0) {
 				printf("Updated %s taxa...", taxaConverted.get());
 			}
 		}*/
 	}
 
 	protected void runOutOfMemory() throws Exception {
 		userIO = new UserIO(session);
 		isoLanguageIO = new IsoLanguageIO(session);
 		infratypeIO = new InfratypeIO(session);
 		referenceIO = new ReferenceIO(session);
 		taxaConverted = new AtomicInteger(0);
 		
 		List<File> allFiles = FileListing.main(data + "/HEAD/browse/nodes");
 		User user = userIO.getUserFromUsername("admin");
 		if (user == null)
 			throw new NullPointerException("No user admin exists");
 		
 		Date date = new Date();
 		
 		Map<Integer, Taxon> taxa = new HashMap<Integer, Taxon>();
 		ArrayList<Taxon> kingdomList = new ArrayList<Taxon>();
 		ArrayList<Taxon> phylumList = new ArrayList<Taxon>();
 		ArrayList<Taxon> classList = new ArrayList<Taxon>();
 		ArrayList<Taxon> orderList = new ArrayList<Taxon>();
 		ArrayList<Taxon> familyList = new ArrayList<Taxon>();
 		ArrayList<Taxon> genusList = new ArrayList<Taxon>();
 		ArrayList<Taxon> speciesList = new ArrayList<Taxon>();
 		ArrayList<Taxon> infrarankList = new ArrayList<Taxon>();
 		ArrayList<Taxon> subpopulationList = new ArrayList<Taxon>();
 		ArrayList<Taxon> infrarankSubpopulationList = new ArrayList<Taxon>();
 
 		
 		for (File file : allFiles) {
 			try {
 				if (file.getPath().endsWith(".xml")) {
 					NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
 					ndoc.parse(FileListing.readFileAsString(file));
 					TaxonNode node = TaxonNodeFactory.createNode(ndoc);
 					Taxon taxon = convertTaxonNode(node, new Date(file.lastModified()), user);
 					
 					if (taxon != null) {
 						
 						if (taxon.getLastEdit() == null) {
 							Edit edit = new Edit("Data migration");
 							edit.setUser(user);
 							edit.setCreatedDate(date);
 							edit.getTaxon().add(taxon);
 							taxon.getEdits().add(edit);							
 						}							
 						
 						Integer nodeLevel = taxon.getLevel();
 						if (nodeLevel == TaxonLevel.KINGDOM) {
 							kingdomList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.PHYLUM) {
 							phylumList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.CLASS) {
 							classList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.ORDER) {
 							orderList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.FAMILY) {
 							familyList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.GENUS) {
 							genusList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.SPECIES) {
 							speciesList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.INFRARANK) {
 							infrarankList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.SUBPOPULATION) {
 							subpopulationList.add(taxon);
 						} else if (nodeLevel == TaxonLevel.INFRARANK_SUBPOPULATION) {
 							infrarankSubpopulationList.add(taxon);
 						}
 						
 					} else {
 						throw new Exception("The taxon " + file.getPath() + " is null");
 					}
 
 				}
 			} catch (Throwable e) {
 				print("Failed on file " + file.getPath());
 				e.printStackTrace();
 				throw new Exception(e);
 			}
 		}
 		
 		while (kingdomList.size() > 0) {
 			writeTaxon(kingdomList.remove(0), taxa);
 
 		}
 		while (phylumList.size() > 0) {
 			writeTaxon(phylumList.remove(0), taxa);
 
 		}
 		while (classList.size() > 0) {
 			writeTaxon(classList.remove(0), taxa);
 
 		}
 		while (orderList.size() > 0) {
 			writeTaxon(orderList.remove(0), taxa);
 
 		}
 		while (familyList.size() > 0) {
 			writeTaxon(familyList.remove(0), taxa);
 
 		}
 		while (genusList.size() > 0) {
 			writeTaxon(genusList.remove(0), taxa);
 
 		}
 		while (speciesList.size() > 0) {
 			writeTaxon(speciesList.remove(0), taxa);
 
 		}
 		while (infrarankList.size() > 0) {
 			writeTaxon(infrarankList.remove(0), taxa);
 
 		}
 		while (subpopulationList.size() > 0) {
 			writeTaxon(subpopulationList.remove(0), taxa);
 
 		}
 		while (infrarankSubpopulationList.size() > 0) {
 			writeTaxon(infrarankSubpopulationList.remove(0), taxa);
 
 		}
 		
 	}
 	
 	protected void writeTaxon(Taxon taxon, Map<Integer, Taxon> taxa ) throws HibernateException, PersistentException {
 		if (taxon.getParent() != null)
 			taxon.setParent((Taxon)session.get(Taxon.class, taxon.getParentId()));
 		session.save(taxon);
 		
 		if (taxaConverted.incrementAndGet() % 100 == 0) {
 			commitAndStartTransaction();
 			printf("Converted %s taxa...", taxaConverted.get());
 		}
 		
 		//taxon.getFootprint();
 		/*taxon.toXML();
 		taxonIO.afterSaveTaxon(taxon);*/
 	}
 
 	public Taxon convertTaxonNode(TaxonNode taxon, Date lastModified, User user) throws PersistentException {
 
 		Taxon newTaxon = new Taxon();
 		newTaxon.state = Taxon.ACTIVE;
 		newTaxon.setId((int) taxon.getId());
 		if (!taxon.getParentId().equals("")) {
 			newTaxon.setParentId(Integer.valueOf(taxon.getParentId()));
 			//Taxon parent = new Taxon();
 			//parent.setId(Integer.valueOf(taxon.getParentId()));
 			//parent.setName(taxon.getParentName());
 		
 		}
 		newTaxon.setStatus(taxon.getStatus());
 		newTaxon.setTaxonLevel(TaxonLevel.getTaxonLevel(taxon.getLevel()));
 		newTaxon.setName(taxon.getName());
 		try {
 			newTaxon.setFriendlyName(taxon.generateFullName());
 		} catch (IndexOutOfBoundsException e) {
 			System.out.println("--- ERROR setting friendly name for taxon " + newTaxon.getId());
 		}
 		newTaxon.setHybrid(taxon.isHybrid());
 		newTaxon.setTaxonomicAuthority(taxon.getTaxonomicAuthority());
 
 		// ADD COMMON NAMES
 		int generationID = 1;
 		for (CommonNameData commonNameData : taxon.getCommonNames()) {
 			if (commonNameData.getName().length() > 2000)
 				continue;
 			
 			CommonName commonName = new CommonName();
 			commonName.setGenerationID(generationID++); //Ensure uniqueness for set
 			commonName.setChangeReason(commonNameData.getChangeReason());
 			commonName.setName(commonNameData.getName());			
 			commonName.setPrincipal(commonNameData.isPrimary());
 			commonName.setValidated(commonNameData.isValidated());
 
 			// ADD ISO LANGUAGUE
 			commonName.setIso(isoLanguageIO.getIsoLanguageByCode(commonNameData.getIsoCode()));
 			newTaxon.getCommonNames().add(commonName);
 			commonName.setTaxon(newTaxon);
 			
 			if (commonNameData.getNotes() != null) {
 				for (Note note : commonNameData.getNotes()) {
 					User author = null;
 					if (note.getUser() != null && !"".equals(note.getUser()))
 						author = userIO.getUserFromUsername(note.getUser());
 					if (author == null)
 						author = user;
 					
 					Date created = null;
 					if (!"".equals(note.getDate()))
 						created = FormattedDate.impl.getDate(note.getDate());
 					if (created == null)
 						created = Calendar.getInstance().getTime();
 					
 					Edit edit = new Edit("Data migration.");
 					edit.setUser(author);
 					edit.setCreatedDate(created);
 					
 					Notes notes = new Notes();
 					notes.setCommonName(commonName);
 					notes.setValue(note.getBody());
 					notes.setEdit(edit);
 					
 					edit.getNotes().add(notes);
 					
 					commonName.getNotes().add(notes);
 				}
 			}
 		}
 
 		// ADD SYNONYMS
 		generationID = 1;
 		for (SynonymData synData : taxon.getSynonyms()) {
 			Synonym synonym = new Synonym();
 			synonym.setGenerationID(generationID++); //Ensure uniqueness for set
 			synonym.setTaxon_level(TaxonLevel.getTaxonLevel(synData.getLevel()));
 			
 			if (synData.getLevel() == TaxonNode.INFRARANK) {
 				//Adding 1 because SIS 1 starts @ 0, SIS 2 starts @ 1.
 				int infrarankLevel;
 				if (synData.getInfrarankType() == -1)
 					infrarankLevel = Infratype.INFRARANK_TYPE_SUBSPECIES;
 				else
 					infrarankLevel = synData.getInfrarankType() + 1;
 				
 				synonym.setTaxon_level(TaxonLevel.getTaxonLevel(synData.getLevel()));
 				synonym.setInfraTypeObject(Infratype.getInfratype(infrarankLevel));
 			}
 			
 			for (Entry<String, String> entry : synData.getAuthorities().entrySet())
 				synonym.setAuthority(entry.getValue(), Integer.valueOf(entry.getKey()));
 
 			synonym.setInfraName(synData.getInfrarank());
 			synonym.setSpeciesName(synData.getSpecie());
 			synonym.setStockName(synData.getStockName());
 
 			if (synData.getLevel() >= TaxonLevel.GENUS)
 				synonym.setGenusName(synData.getGenus());
 			else
 				synonym.setName(synData.getUpperLevelName());
 
 			//This is now auto-generated.
 			//synonym.setFriendlyName(synData.getName());
 			synonym.setStatus(synData.getStatus());
 
 			if (synData.getNotes() != null) {
 				Edit edit = new Edit("Data migration.");
 				edit.setUser(user);
 				
 				Notes note = new Notes();
 				note.setSynonym(synonym);
 				note.setValue(synData.getNotes());
 				note.setEdit(edit);
 				
 				edit.getNotes().add(note);
 				
 				synonym.getNotes().add(note);
 			}
 			
 			newTaxon.getSynonyms().add(synonym);
 			synonym.setTaxon(newTaxon);
 			
 		}
 
 		// ADD INFRARANK
 		// As per #423...
 		if (newTaxon.getId() == 40853 || newTaxon.getId() == 37068 || newTaxon.getId() == 37103)
 			newTaxon.setInfratype(infratypeIO.getInfratype(Infratype.INFRARANK_TYPE_FORMA));
 		else {
 			Infratype infratype = infratypeIO.getInfratype(taxon.getInfrarankType());
 			if (infratype != null)
 				newTaxon.setInfratype(infratype);
			else { //As per #638
				newTaxon.setInfratype(infratypeIO.getInfratype(Infratype.INFRARANK_TYPE_SUBSPECIES));
				printf("Warning: Taxon {0} ({1}) has no infratype specified, defaulting to subspecies.", newTaxon.getFriendlyName(), newTaxon.getId());
			}
 		}
 
 		// ADD REFERENCES
 		for (ReferenceUI refUI : taxon.getReferencesAsList()) {
 			String hash = refUI.getReferenceID();
 			Reference ref = referenceIO.getReferenceByHashCode(hash);
 			if (ref != null) {
 				newTaxon.getReference().add(ref);
 			} else {
 				print("ERROR -- Couldn't find reference " + hash + " in taxon " + taxon.getId());
 			}
 		}
 
 		// ADD LAST EDIT
 		if (taxon.getLastUpdatedBy() != null) {
 			Edit edit = new Edit("Data migration.");
 			edit.setUser(userIO.getUserFromUsername(taxon.getLastUpdatedBy()));
 			edit.setCreatedDate(lastModified);
 		}
 		
 		// ADD PUBLISHED ASSESSMENT PLACEHOLDERS
 		int fauxID = 0;
 		for (String assessmentID : taxon.getAssessments()) {
 			Assessment assessment = new Assessment();
 			assessment.setId(++fauxID);
 			assessment.setInternalId(assessmentID);
 			assessment.setTaxon(newTaxon);
 			assessment.setAssessmentType(AssessmentType.getAssessmentType(AssessmentType.PUBLISHED_ASSESSMENT_TYPE));
 			assessment.setState(3); //so I can find these later.
 			
 			newTaxon.getAssessments().add(assessment);
 		}
 		
 		return newTaxon;
 	}
 }
