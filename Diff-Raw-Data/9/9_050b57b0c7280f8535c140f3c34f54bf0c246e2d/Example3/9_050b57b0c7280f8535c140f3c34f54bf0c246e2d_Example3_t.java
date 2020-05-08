 import java.util.Iterator;
 import java.util.List;
 
 import edu.wustl.catissuecore.domain.Quantity;
 import edu.wustl.catissuecore.domain.CollectionProtocol;
 import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
 import edu.wustl.catissuecore.domain.Participant;
 import edu.wustl.catissuecore.domain.ParticipantMedicalIdentifier;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;
 import gov.nih.nci.system.comm.client.ClientSession;
 
 
 public class Example3 {
 
 	private CollectionProtocol collectionProtocol;
 	private ApplicationService appService ;
 	
 	public static void main(String[] args) 
 	{
 
 		System.out.println("*** Example 3...");
 
 		Example3 test = new Example3();
 
 		test.setup();
 		test.addParticipant();
 		
 	}
 
 	
 	private void setup() {
 
 	 appService = ApplicationServiceProvider.getRemoteInstance();
 
 	ClientSession cs = ClientSession.getInstance();
 	try
 	{ 
 		cs.startSession("admin@admin.com", "Login123");
 	} 
 	catch (Exception ex) 
 	{ 
 		System.out.println(ex.getMessage()); 
 		ex.printStackTrace();
 	}
 
 	// Query for a collection protocol
 	// You should add this protocol manually before running this program
 	collectionProtocol = new CollectionProtocol();
 	collectionProtocol.setTitle("Woodward Colon Trial");
 	try {
 		List resultList = appService.search(CollectionProtocol.class,collectionProtocol);
 		for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();) 
 		{
 			collectionProtocol = (CollectionProtocol) resultsIterator.next();
 			System.out.println(" Collection protocol Found ---->  :: " + collectionProtocol.getShortTitle());
 		
 		
 		}
 	}
 		catch (Exception ex) 
 		{ 
 			System.out.println(ex.getMessage()); 
 			ex.printStackTrace();
 		}
 	}
 
 
 	private void addParticipant()
 	{
 
 		try {
 			Participant p = new Participant();
 			
			p.setGender("Female Gender");
 			p.setFirstName("Heather");
 			p.setLastName("Jacobs");
 			p.setActivityStatus("Active");
 			p =  (Participant) appService.createObject(p);
 			System.out.println("Patient added " );
 
 			// add an identifier for the patient
 			// Patients can have multiple identifiers
 			ParticipantMedicalIdentifier i = new ParticipantMedicalIdentifier();
 			String mrn = "MRN123457";
 			i.setMedicalRecordNumber(mrn);
 			i.setParticipant(p);
 			i =  (ParticipantMedicalIdentifier) appService.createObject(i);
 			System.out.println("MRN added: " + mrn);
 
 			// Register the patient to the protocol retrieved earlier
 			CollectionProtocolRegistration cpr = new CollectionProtocolRegistration();
 			cpr.setCollectionProtocol(collectionProtocol);
 			cpr.setParticipant(p);
 			cpr.setProtocolParticipantIdentifier("");
 			cpr.setActivityStatus("Active");
 			cpr =  (CollectionProtocolRegistration) appService.createObject(cpr);
 			System.out.println("Patient added to protocol: " + collectionProtocol.getShortTitle());
 
 		} 
 		catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 }
