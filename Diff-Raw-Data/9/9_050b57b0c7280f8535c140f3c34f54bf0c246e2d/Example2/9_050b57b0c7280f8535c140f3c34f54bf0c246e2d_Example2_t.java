 import edu.wustl.catissuecore.domain.CollectionProtocol;
 import edu.wustl.catissuecore.domain.Participant;
 import edu.wustl.catissuecore.domain.ParticipantMedicalIdentifier;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;
 import gov.nih.nci.system.comm.client.ClientSession;
 
 
 public class Example2 {
 
 	private ApplicationService appService ;
 	
 	public static void main(String[] args) 
 	{
 
 		System.out.println("*** Example 2...");
 
 		Example2 test = new Example2();
 
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
 
 	}
 
 
 	private void addParticipant()
 	{
 
 		try {
 			Participant p = new Participant();
 			
			p.setGender("Male Gender");
 			p.setFirstName("Hector");
 			p.setLastName("Jones");
 			p.setActivityStatus("Active");
 			p =  (Participant) appService.createObject(p);
 			System.out.println("Patient added " );
 
 			// add an identifier for the patient
 			// Patients can have multiple identifiers
 			ParticipantMedicalIdentifier i = new ParticipantMedicalIdentifier();
 			String mrn = "MRN123458";
 			i.setMedicalRecordNumber(mrn);
 			i.setParticipant(p);
 			i =  (ParticipantMedicalIdentifier) appService.createObject(i);
 			System.out.println("MRN added: " + mrn);
 
 		} 
 		catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 }
