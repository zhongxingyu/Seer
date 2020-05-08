 import helpers.InputHelper;
 import helpers.Prompt;
 import helpers.Prompter;
 
 import javax.swing.JOptionPane;
 
 import Models.Address;
 import Models.Coach;
 import Models.Official;
 import Models.Player;
 import Models.User;
 
 
 public class RegisterView {
 	
 /*************************************************************************************************************/
 /****************************START:  Register Section ********************************************************/
 /*************************************************************************************************************/
 	public static void registerView() {
 		System.out.println(MainApplication.timeStamp() + "- SECTION: Register Section.   Method Called: registerView()");
 		registerNewUser();
 	}
 	
 	public static void registerNewUser() {
 		System.out.println(MainApplication.timeStamp() + "- SECTION: Register Section.   Method Called: registerNewUser()");
 		
 		Prompt username;
 		Prompt password;
 
 		username = Prompter.question("Please enter your email address:", "Register - username");
 	
 		if (!username.isDidPressCancel()) {
 			password = Prompter.question("Please enter your password:", "Register - password");
 			if (!password.isDidPressCancel()) {
 				
 				String u = username.getInputEntered();
 				String p = password.getInputEntered();
 				int userType = dropDownListDialog();
 				
 				String[] poss = {" ", "Official", "Coach", "Captain", "Player"};
 				String subDataString = poss[userType];
 				
 				//if they selected captain or player, either way they are still considered to be players
 				//thus their usertype is just three. 
 				if (userType == 4) {
 					userType = 3;
 				}
 				
 				MainApplication.getController().getUc().setUserObject(u, p, userType, promptForToEnterUserInfo(userType));
 				
 				String dataString = "Congratulations, your are now registered as a " + subDataString + ".";
 				dataString += "\nYou may now login to your dashboard.";
 				JOptionPane.showMessageDialog(null, dataString, "Registration Confirmation", JOptionPane.INFORMATION_MESSAGE);
 				
 				MainApplication.firstView();
 				
 			} else {
 				MainApplication.firstView(); //they pressed the cancel button on the password question.
 			}
 			
 		} else {
 			MainApplication.firstView(); //they pressed the cancel button on username question
 		}
 	
 	}
 	
 	
 	
 	public static int dropDownListDialog() {
 		Object[] poss = {"Official", "Coach", "Captain", "Player"};
 		String a = (String)JOptionPane.showInputDialog(null, "Choose what you want to register as:", "Register - user type", JOptionPane.PLAIN_MESSAGE, null, poss, "Official");
 		
 		System.out.println(a);
 		int v = -1;
 		if(a.equals("Official")) {
 			v = 1;
 		} else if (a.equals("Coach")){
 			v = 2;
 		} else if (a.equals("Captain")){
 			v = 3;
 		} else if (a.equals("Player")) {
 			v = 4;
 		} else {
 			System.out.println("Error, in the dropDownListDialog");
 		}
 		return v;
 	}
 	
 
 	public static User promptForToEnterUserInfo(int type) {
 		String firstName = InputHelper.promptString("Please enter in your first name", "firstname", "Register - first name");
 		String lastName = InputHelper.promptString("Please enter in your last name", "lastName", "Register - last name");
		String phone;
		do{phone = InputHelper.promptString("Please enter in your phone number", "phone number", "Register - phone number");}
		while(phone.length() != 10);
 		String street = InputHelper.promptString("Please enter in your street", "street", "Register - street");
 		String city = InputHelper.promptString("Please enter in your city", "city", "Register - city");
 		String state = InputHelper.promptString("Please enter in your state", "state", "Register - state");
		String zip;
		do{zip = InputHelper.promptString("Please enter in your zip code", "zip code", "Register - zip code");}
		while(zip.length() != 5);
 		
 		Address nAddress = new Address();
 		nAddress.setStreet(street);
 		nAddress.setState(state);
 		nAddress.setZip(zip);
 		nAddress.setCity(city);
 		
 		User nUser = null;
 		if (type == 1) {
 			//official
 			nUser = new Official();
 		} else if (type == 2) {
 			//coach
 			nUser = new Coach();
 		} else if (type == 3) {
 			// player captain
 			Player p = new Player();
 			p.setCaptain(true);
 			nUser = p;
 		} else {
 			//player
 			Player p = new Player();
 			p.setCaptain(false);
 			nUser = p;
 		}
 		
 		nUser.setFirstName(firstName);
 		nUser.setLastName(lastName);
 		nUser.setPhone(phone);
 		nUser.setAddress(nAddress);
 		
 		JOptionPane.showMessageDialog(null, "Sign Tournament Waiver agree refuse goes here");
 		
 		return nUser;
 	}
 
 /*************************************************************************************************************/
 /****************************END:  Register Section **********************************************************/
 /*************************************************************************************************************/
 		
 }
