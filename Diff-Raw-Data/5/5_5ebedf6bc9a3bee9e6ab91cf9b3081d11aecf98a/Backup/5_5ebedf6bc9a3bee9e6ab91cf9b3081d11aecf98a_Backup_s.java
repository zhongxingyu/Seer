 package fileHandler.logic;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 import partiesList.factories.IPartiesListFactory;
 import partiesList.factories.IPartyFactory;
 import partiesList.model.IPartiesList;
 import partiesList.model.IParty;
 import votersList.factories.IVoterDataFactory;
 import votersList.factories.IVotersListFactory;
 import votersList.model.IVoterData;
 import votersList.model.IVotersList;
 
 
 /**
  * the class which implements IBackup
  * provides all the backup capabilities of the project
  * @author Emil
  *
  */
 public class Backup implements IBackup {
 	private ReadXMLFile readXMLFilesService;	
 	
 	/*
 	 * these strings will hold the path to all the XML files
 	 */
 	/**
 	 * the location of the file to which we will put the backup of the unregistered voters
 	 */
 	private String unregisteredVotersFile;
 	/**
 	 * the location of the file to which we will put the backup of the voters list
 	 */
 	private String backupedVotersListFile;
 	/**
 	 * the location of the file to which we will put the backup of the parties list
 	 */
 	private String backupedPartiesListFile;
 	
 	private WriteXMLFileUnregisteredVoters unregisteredVotersService;
 	
 	/**
 	 * the constructor of Backup
 	 * @param partiesListFactory the parties list factory
 	 * @param partyFactory the party factory
 	 * @param votersListFactory the voters list factory
 	 * @param voterDataFactory the voter's data factory
 	 * @param backupedVotersListFile the location of the file to which we will put the backup of the voters list
 	 * @param backupedPartiesListFile the location of the file to which we will put the backup of the parties list
 	 * @param unregisteredVotersFile the location of the file to which we will put the backup of the unregistered voters
 	 */
 	public Backup(IPartiesListFactory partiesListFactory,
 			IPartyFactory partyFactory, IVotersListFactory votersListFactory,
 			IVoterDataFactory voterDataFactory,
 			String backupedVotersListFile, String backupedPartiesListFile, 
 			String unregisteredVotersFile){
 		
 		this.readXMLFilesService = new ReadXMLFile(partiesListFactory,
 									partyFactory, votersListFactory,
 									voterDataFactory);
 		
 		this.backupedPartiesListFile = backupedPartiesListFile;
 		this.backupedVotersListFile = backupedVotersListFile;
 		this.unregisteredVotersFile = unregisteredVotersFile;
 		
 		this.unregisteredVotersService = new WriteXMLFileUnregisteredVoters(unregisteredVotersFile);
 	}
 	
 	@Override
 	public IVotersList restoreVoters() {
 		/*
 		 * "VotersListBackup.xml" - this is the recommended file name
 		 */		
 		return this.readXMLFilesService.readXMLVotersListBackup(this.backupedVotersListFile);
 	}
 
 	@Override
 	public IPartiesList restoreParties() {
 		/*
 		 * "PartiesListBackup.xml" - this is the recommended file name
 		 */
 		return this.readXMLFilesService.readXMLPartiesListBackup(this.backupedPartiesListFile);
 	}
 
 
 	@Override
 	public void storeState(IPartiesList parties, IVotersList voters, IVotersList unregistered) {
		//TODO save unregistered to the unregistered voters file
		/*
		 * "PartiesListBackup.xml"
		 * "VotersListBackup.xml"
		 */
 
 		//Parties
 		this.new BackupPartiesListToXMLFile().createEmptyPartiesListXMLFile();
 		for (IParty party : parties) {
 			this.new BackupPartiesListToXMLFile().addPartyToXMLFile(party);
 		}
 		
 		//Voters
 		this.new BackupVotersListToXMLFile().createEmptyVotersListXMLFile();
 		for (IVoterData voter : voters) {
 			this.new BackupVotersListToXMLFile().addVoterToXMLFile(voter);
 		}
 		
 		//Unregistered Voters
 		this.unregisteredVotersService.createEmptyUnregisteredVotersXMLFile();
 		for (IVoterData voter : unregistered) {
 			this.unregisteredVotersService.addVoterToXMLFile(voter);
 		}
 		
 		
 		
 	}
 	
 	
 	
 	
 	/***
 	 * this class is implementing backup of IPartiesList
 	 * @author Emil
 	 *
 	 */
 	private class BackupPartiesListToXMLFile {
 		
 		
 		/**
 		 * creates a raw XML file to save partiesList in it 
 		 */
 		public void createEmptyPartiesListXMLFile() {
 			
 			String fileName = backupedPartiesListFile;
 			try {
 				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		 
 				// root elements
 				Document doc = docBuilder.newDocument();
 				Element rootElement = doc.createElement("votingRecords");
 				doc.appendChild(rootElement);
 			 
 				// write the content into xml file
 				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 				Transformer transformer = transformerFactory.newTransformer();
 				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 				DOMSource source = new DOMSource(doc);
 				//StreamResult result = new StreamResult(new File("file.xml"));
 				StreamResult result = new StreamResult(new File(fileName));
 		 
 				transformer.transform(source, result);
 		 
 				//System.out.println("File saved!");
 	 
 		  } catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		  } catch (TransformerException tfe) {
 			tfe.printStackTrace();
 		  }
 		}
 		
 
 		/**
 		 * adds a party to the XML file that his name is given as parameter
 		 * @param givenParty the party to add to the XML file
 		 */
 		public void addPartyToXMLFile(IParty givenParty){
 			
 			String fileName = backupedPartiesListFile;
 			
 			try {
 				
 				//File file = new File("file.xml");
 				File file = new File(fileName);
 			 
 				//Create instance of DocumentBuilderFactory
 				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			 
 				//Get the DocumentBuilder
 				DocumentBuilder docBuilder = factory.newDocumentBuilder();
 			 
 				//Using existing XML Document
 				Document doc = docBuilder.parse(file);
 				 
 				//create the root element
 				Element root = doc.getDocumentElement();
 		 
 				
 				// party elements
 				Element newParty = doc.createElement("votingRecord");
 				root.appendChild(newParty);
 		 
 				// recordName
 				Element recordNameElement = doc.createElement("recordName");
 				recordNameElement.appendChild(doc.createTextNode(givenParty.getName().toString()));
 				newParty.appendChild(recordNameElement);
 				
 				// ballotLetters
 				Element ballotLettersElement = doc.createElement("ballotLetters");
 				ballotLettersElement.appendChild(doc.createTextNode(givenParty.getSymbol().toString()));
 				newParty.appendChild(ballotLettersElement);
 				
 				// voteNumber
 				Element voteNumberElement = doc.createElement("voteNumber");
 				voteNumberElement.appendChild(doc.createTextNode(Integer.toString(givenParty.getVoteNumber())));
 				newParty.appendChild(voteNumberElement);
 					
 					
 					
 					
 				//the code below writes back the modified XML file with charSet = "UTF8"
 				BufferedWriter xmlOutput =
 						new BufferedWriter (
 								new OutputStreamWriter(new FileOutputStream(fileName),"UTF8"));
 				    
 				// Write the Modified XML as usual
 			    TransformerFactory tFactory =
 			        TransformerFactory.newInstance();
 			    Transformer transformer = tFactory.newTransformer();
 	
 			    DOMSource source = new DOMSource(doc);
 			    StreamResult result = new StreamResult(xmlOutput);
 			    transformer.transform(source, result);
 			    xmlOutput.close();
 
 		     }
 		     catch(SAXException e) {
 			e.printStackTrace();
 		     }
 		     catch(IOException e) {
 		        e.printStackTrace();
 		     }
 		     catch(ParserConfigurationException e) {
 		       e.printStackTrace();
 		     }
 		     catch(TransformerConfigurationException e) {
 		       e.printStackTrace();
 		     }
 		     catch(TransformerException e) {
 		       e.printStackTrace();
 		     }
 		}
 	}
 		
 		
 
 		
 	/***
 	 * this class is implementing backup of IVotersList
 	 * @author Emil
 	 *
 	 */	
 	private class BackupVotersListToXMLFile {
 		/**
 		 * creates a raw XML file to save votersList in it 
 		 */
 		public void createEmptyVotersListXMLFile() {
 			
 			String fileName = backupedVotersListFile;
 			try {
 	 
 				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		 
 				// root elements
 				Document doc = docBuilder.newDocument();
 				Element rootElement = doc.createElement("voters");
 				doc.appendChild(rootElement);
 		 
 				// write the content into xml file
 				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 				Transformer transformer = transformerFactory.newTransformer();
 				DOMSource source = new DOMSource(doc);
 				//StreamResult result = new StreamResult(new File("file.xml"));
 				StreamResult result = new StreamResult(new File(fileName));
 		 
 				// Output to console for testing
 				// StreamResult result = new StreamResult(System.out);
 		 
 				transformer.transform(source, result);
 		 
 				//System.out.println("File saved!");
 	 
 		  } catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		  } catch (TransformerException tfe) {
 			tfe.printStackTrace();
 		  }
 		}
 		
 
 		/**
 		 * adds a voter to the XML file that his name is given as parameter
 		 * @param givenVoter the voter needed to be added to the XML file
 		 */
 		public void addVoterToXMLFile(IVoterData givenVoter){
 			
 			String fileName = backupedVotersListFile;
 			
 			try {
 				//File file = new File("file.xml");
 				File file = new File(fileName);
 			 
 				//Create instance of DocumentBuilderFactory
 				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			 
 				//Get the DocumentBuilder
 				DocumentBuilder docBuilder = factory.newDocumentBuilder();
 			 
 				//Using existing XML Document
 				Document doc = docBuilder.parse(file);
 			 
 				//create the root element
 				Element root = doc.getDocumentElement();
 		 
 				
 				// voter elements
 				Element newVoter = doc.createElement("voter");
 				root.appendChild(newVoter);
 		 
 				// id
 				Element idElement = doc.createElement("id");
 				idElement.appendChild(doc.createTextNode(Integer.toString(givenVoter.getId())));
 				newVoter.appendChild(idElement);
 				
 				// identified
 				Element identifiedElement = doc.createElement("identified");
 				identifiedElement.appendChild(doc.createTextNode(Boolean.toString(givenVoter.isIdentified())));
 				newVoter.appendChild(identifiedElement);
 				
 				// voted
 				Element votedElement = doc.createElement("voted");
 				votedElement.appendChild(doc.createTextNode(Boolean.toString(givenVoter.hasVoted())));
 				newVoter.appendChild(votedElement);
 				
 				
 				//set up a transformer
 				TransformerFactory transfac = TransformerFactory.newInstance();
 				Transformer trans = transfac.newTransformer();
 			 
 		        //create string from xml tree
 		        StringWriter sw = new StringWriter();
 		        StreamResult result = new StreamResult(sw);
 		        DOMSource source = new DOMSource(doc);
 		        trans.transform(source, result);
 		        String xmlString = sw.toString();
 		 
 		        OutputStream f0;
 				byte buf[] = xmlString.getBytes();
 				//f0 = new FileOutputStream("file.xml");
 				f0 = new FileOutputStream(fileName);
 				for(int i=0;i<buf .length;i++) {
 				   f0.write(buf[i]);
 				}
 				f0.close();
 				buf = null;
 			     }
 			     catch(SAXException e) {
 				e.printStackTrace();
 			     }
 			     catch(IOException e) {
 			        e.printStackTrace();
 			     }
 			     catch(ParserConfigurationException e) {
 			       e.printStackTrace();
 			     }
 			     catch(TransformerConfigurationException e) {
 			       e.printStackTrace();
 			     }
 			     catch(TransformerException e) {
 			       e.printStackTrace();
 			     }
 		}
 	}
 
 
 
 
 	@Override
 	public IVotersList restoreUnregisteredVoters() {
 		return this.readXMLFilesService.readUnregisteredVotersXMLFile(this.unregisteredVotersFile);
 	}
 	
 	
 	
 	
 }
