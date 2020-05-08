 package webServiceClient;
 
 import java.util.Hashtable;
 
 import data.LoginCred;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 import interfaces.MediatorWeb;
 import interfaces.WebServiceClient;
 
 /**
  * WebServiceClient module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebServiceClientImpl implements WebServiceClient {
 	private MediatorWeb med;
 	private WebServiceMockup webService;
 	
 	private Hashtable<String, UserProfile> users;
 	
 	public WebServiceClientImpl(MediatorWeb med) {
 		this.med = med;
 		
 		med.registerWebServiceClient(this);
 
 		/* TODO: This should be deleted.
 		 * Used only for mockup test.
 		 */
 		users = new Hashtable<String, UserProfile>();
		users.put("pvlase", new UserProfile("pvlase","Paul Vlase", UserRole.BUYER, "parola"));
		users.put("unix140", new UserProfile("unix140","Ghennadi Procopciuc", UserRole.BUYER, "marmota"));
 	}
 	
 	@Override
 	public UserProfile logIn(LoginCred cred) {
 		UserProfile profile;
 		
 		profile = getUserProfile(cred.getUsername());
 		if (profile == null) {
 			return null;
 		}
 		
 		if (profile.getPassword() != cred.getPassword()) {
 			return null;
 		}
 		
 		return profile;
 	}
 	
 	@Override
 	public void logOut() {
 		System.out.println("[WebServiceClient:logOut()] Bye bye");
 		
 		/* TODO: This should be deleted.
 		 * Used only for mockup test.
 		 */
 		try {
 			webService.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public UserProfile getUserProfile(String username) {
 		return users.get(username);
 	}
 	
 	@Override
 	public boolean setUserProfile(UserProfile profile) {
		users.put(profile.getName(), profile);
 		return true;
 	}
 
 	/* Common */
 	@Override
 	public int addOffer(String service) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int removeOffer(String service) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	@Override
 	public int launchOffer(String service) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int dropOffer(String service) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* Buyer */
 	@Override
 	public int acceptOffer() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int refuseOffer() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* Seller */
 	@Override
 	public int makeOffer() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int dropAction() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }
