 package view;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import main.AuthenticationService;
 import model.ContactInfo;
 import model.Name;
 import model.Student;
 
 import persistance.DataRepository;
 import persistance.StudentRepository;
 
 public class CreateAccountMenu implements Menu{
 
 	//Class variables
 	private static final Menu createAccountMenu = new CreateAccountMenu();
 	private final StudentRepository studentRepository;
 	//Student successfully saved
 	private boolean success = false;
 	//Variables for building new student
 	private String firstName = null;
 	private String lastName = null;
 	private String username = null;
 	private String email = null;
 	private long phoneNumber = 0L;
 	//Loop index
 	int loopIndex = 0;
 
 	private CreateAccountMenu(){
 		studentRepository = DataRepository.INSTANCE;
 	}
 
 	public static Menu getInstance(){
 		return createAccountMenu;
 	}
 	@Override
 	public void displayMenu() {
 		if (loopIndex == 0){
 			//Print Header
 			System.out.println(Decoration.DIVIDER);
 			System.out.println("Create Account");
 			System.out.println(Decoration.DIVIDER);
 			System.out.println("M = Main Menu");
 			System.out.println(Decoration.SEPARATOR);
 		}
 		//Handle details of create user loop
 		createUserLoop();
 
 		//Student saved
 		if (success){
 			System.out.println("You have successfully created a new account.");
 			System.out.println("Please use the username " + username + " to login in the future.");
 			System.out.println("Press enter to continue.");
 		}
 		else{
 			System.out.println(Decoration.SEPARATOR);
 			System.out.println("Are you sure you want to return to the main menu?");
 			System.out.println("Y = Exit to Main Menu");
 			System.out.println("N = Return to Create Account");
 			System.out.println(Decoration.SEPARATOR);
 		}
 	}
 
 	/**
 	 * Returns null for an invalid input during create user loop.
 	 */
 	@Override
 	public Menu parseInput(String input) {
		if (success && AuthenticationService.INSTANCE.validate(username))
 			return AuthenticatedMainMenu.getInstance(username);
 
 		//Ensure input is present
 		if (input == null || input.length() < 1){
 			System.out.println("Invalid input.");
 			return null;
 		}
 
 		//Check escape clause
 		String menuCheck = input.toUpperCase().substring(0, 1);
 		switch (menuCheck){
 		case "M": return MainMenu.getInstance();
 		case "Y": 
 			loopIndex = 0;
 			return MainMenu.getInstance();
 		case "N":
 			createUserLoop();
 			return createAccountMenu;
 		}
 
 		//Switch to populate student attributes
 		switch (loopIndex){
 		case 0: 
 			firstName = input;
 			loopIndex++;
 			return null;
 
 		case 1: 
 			lastName = input;
 			loopIndex++;
 			return null;
 
 		case 2: 
 			username = input;
 			loopIndex++;
 			return null;
 
 		case 3: 
 			email = input;
 			loopIndex++;
 			return null;
 
 		case 4: 
 			try {
 			phoneNumber = Long.parseLong(input);
 			loopIndex++;
 			}
 			catch (NumberFormatException e) {
 				System.out.println("Please enter phone number without any spaces or formatting.");
 			}
 			return null;
 
 		}
 		
 		//This should never be reached
 		System.out.println("Invalid Input.");
 		return null;
 	}
 
 	private void createUserLoop(){
 		//Loop variables
 		List<String> prompts = new ArrayList<String>();
 		prompts.add("Enter your first name.");
 		prompts.add("Enter your last name.");
 		prompts.add("Enter a username.");
 		prompts.add("Enter your email address.");
 		prompts.add("Enter your phone number.");
 
 		while (loopIndex != prompts.size())//Loop index is updated in parseInput method 
 		{			
 			System.out.println(prompts.get(loopIndex));
 			try {
 				Menu result = parseInput(new BufferedReader(new InputStreamReader(System.in)).readLine());
 				if (result != null)
 					return;//Exit method
 				System.out.println(Decoration.SEPARATOR);
 			} catch (IOException e) {
 				//e.printStackTrace(); //Uncomment if needed for debugging purposes
 			}
 		} 
 		//Build newStudent
 		Student newStudent = new Student(studentRepository.nextStudentID(), new Name(firstName, lastName), username, new ContactInfo(email, phoneNumber));
 		//Save student
 		if (studentRepository.saveStudent(newStudent))
 			success = true;
 	}
 
 }
