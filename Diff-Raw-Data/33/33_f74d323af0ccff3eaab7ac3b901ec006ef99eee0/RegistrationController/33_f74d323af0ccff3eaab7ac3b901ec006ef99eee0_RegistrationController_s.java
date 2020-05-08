 package contollers;
 
 import ass2.FormManager;
 import ass2.ParameterManager;
 
 public class RegistrationController extends MasterFormBasedController {
 	FormManager formManager;
 	public RegistrationController(ParameterManager params) {
 		super(params);
 		formManager = new FormManager();
 		
 		createForm();
 		
 		// TODO Auto-generated constructor stub
 	}
 
 	private void createForm() {
 		formManager.addForm("username", paramManager.getIndividualParam("username"),FormManager.RESTRICT_ALPHHANUMERIC_NOSPACE);
 		formManager.addForm("password", paramManager.getIndividualParam("password"));
 		formManager.addForm("email", paramManager.getIndividualParam("email"), FormManager.RESTRICT_EMAIL);
 		formManager.addForm("nickname", paramManager.getIndividualParam("nickname"));
 		formManager.addForm("firstName", paramManager.getIndividualParam("firstName"));
 		formManager.addForm("lastName", paramManager.getIndividualParam("lastName"));
 		formManager.addForm("yearOfBirth", paramManager.getIndividualParam("yearOfBirth"));
 		formManager.addForm("address", paramManager.getIndividualParam("address"));
 		formManager.addForm("ccNumber", paramManager.getIndividualParam("ccNumber"));
 
 		
 
 
 
 
 	}
 
 	public boolean isInvalidForm() {
 		return formManager.isMissingDetails();
 	}
 
 	public String getMessage() {
 		return formManager.getMessage();
 	}
 }
