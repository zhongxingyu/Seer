 package RESTSystemServices;
 
 import systemHibernateEntities.Charity;
 import systemHibernateManagers.CharityManager;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.GenericEntity;
 
 @Path("/charityService")
 public class CharityService {
 	
 	
 	 @GET
 	 @Path("/charityApprovals")
 	 @Produces("application/json")
 	 public List<Charity> getCharitiesForApproval(){
 		 	System.out.println("Get all Charity Requests");
 		 	CharityManager charityManager = new CharityManager();
 	    	List<Charity> charities = charityManager.getCharitiesRequests();
 	     	System.out.println("Charity Requests Array Populated");
 	    	return charities;
 	 }
 	 
 	 @GET
 	 @Path("/charities")
 	 @Produces("application/json")
 	 public List<Charity> getCharities() {
 		 CharityManager charityManager = new CharityManager();
 		 List<Charity> charities = charityManager.getCharities();
 		 return charities;
 	 }
 	 
 	 
 	 @GET
 	 @Path("/charities/{charityid}")
 	 @Produces("application/json")
 	 public Charity getCharity(@PathParam("charityid") int charityID) {
 		 CharityManager charityManager = new CharityManager();
 		 Charity ch = charityManager.getCharity(charityID);
 		 return ch;   
 	 }
 	 
 	 /**
 	   * PUT method for updating or creating an instance of CharityResource
 	   * @param content representation for the resource
 	   * @return an HTTP response with content of the updated or created resource.
 	   */
 	 @Path("/addCharity/")
 	 @POST
 	 @Consumes("application/json")
 	 public void postCharity(Charity charity) {		 
 		 //Charity newCharity = (Charity)ch.getEntity();
 		 CharityManager charityManager = new CharityManager();
 		 charityManager.addCharity(charity);
 	 }
	 
 	 @POST
 	 @Path("/generateSchema/{charityId}")
 	 @Produces("application/json")
 	 public Boolean postGenerateSchema(@PathParam("charityId") String charityId){   
 		 try{
 			 System.out.println("Get Charity ID"); 
 			 int charID = Integer.parseInt(charityId);     
 			 Boolean result = GenerateSchemaManager.generateSchema(charID);
 			 System.out.println("Charity Schema Generated");
 			 System.out.println(result);
 			 return result;	   
	   }catch (Exception ex)
	   {
	    ex.printStackTrace();
	    return false;
	   }
	      
	  }
 }
