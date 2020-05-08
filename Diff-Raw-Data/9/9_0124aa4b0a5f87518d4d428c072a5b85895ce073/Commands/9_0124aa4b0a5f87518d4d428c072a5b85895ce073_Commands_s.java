 package desperatehousepi;
 
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 public class Commands {
 	
 	//Declare variables
 	private static Crust crust;
 	
 	//Create objects that commands can alter
 	private static enum objectType{
 		CRUST
 	}
 	
 	/***************************************
 	 * Holds all of the executables for each command that can be called
 	 * @author Anthony and Michael
 	 ***************************************/
 	public Commands(Crust new_crust){
 		crust = new_crust;
 	}
 	
 	/***************************************
<<<<<<< HEAD
<<<<<<< HEAD
 	 * Prints a message explaining the proper usage of the given command or
 	 * a general help message if no parameter given
=======
	 * Prints a message explaining the proper usage of the given command
>>>>>>> 2365277ac415543f3abd6ac2940b60cde3a91af3
=======
	 * Prints a message explaining the proper usage of the given command
>>>>>>> 2365277ac415543f3abd6ac2940b60cde3a91af3
 	 * @param tkn - A string tokenizer containing the command for which to 
 	 * display the help message
 	 * @author Anthony
 	 ***************************************/
 	public void help(StringTokenizer tkn){
 		
 		//String to hold name of command to display infromation for
 		String helpName = "";
 		
 		//create a new HelpText object, which contains all of the actual help messages
 		HelpText ht = new HelpText();
 		
 		//try to grab the name of command to display help info for
 		try{
 			helpName = tkn.nextToken();
 		}catch(Exception e){
 			helpName = "";
 		}
 		//print the appropriate message to the screen
 		System.out.println(ht.displayHelp(helpName));
 		
 	}
 	/***************************************
 	 * Saves an object to file to be imported from later
 	 * @param tkn - A string tokenizer containing the rest of the save command
 	 * @author Michael
 	 ***************************************/
 	public void save(StringTokenizer tkn){
 		
 		//Try to save the object
 		try{
 			
 			//Grab the name of the object to be saved
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be saved
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If saving the crust
 				case CRUST:
 					if(tkn.hasMoreTokens())
 						crust.save(tkn.nextToken());
 					else
 						System.out.println("Invalid command.\nUsage: save [object] [profile name]");
 					break;
 				
 				//Otherwise object is not meant to be saved
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help save'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: save [object] [profile name]");
 		}
 		
 	}
 	
 	/***************************************
 	 * Loads an object from a file \
 	 * @param tkn - A string tokenizer containing the rest of the save command
 	 * @author Anthony and Michael
 	 ***************************************/
 	public void load(StringTokenizer tkn){
 		
 		try{	
 			
 			//Grab the name of the object to be loaded
 			String obj = tkn.nextToken();
 			
 			//Value to check if load works correctly
 			int loadVal = 0;
 			
 			//Find the object that is to be loaded
 			switch(objectType.valueOf(obj.toUpperCase())){
 			
 				//If Loading the crust
 				case CRUST:
 					if(tkn.hasMoreTokens()){
 						
 						//run load function and store success variable in loadVal
 						loadVal = crust.load(tkn.nextToken());
 						
 						//check that file exists and is in correct format; if not print useage message
 						if(loadVal == 1){
 							System.out.println("Invalid command.\nCrust not found.");
 						}else if(loadVal == 2){
 							System.out.println("Invalid command.\nBad file format.");
 						}
 					}else{
 						System.out.println("Invalid command.\nUsage: load [object] [profile name]");
 					}break;
 				
 				//Otherwise object is not meant to be loaded
 				default:
 					System.out.println("Invalid command, given object can not be loaded. Type 'help load'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: load [object] [profile name]");
 		}
 		
 	}
 	
 	/***************************************
 	 * Destroys an object and frees the memory
 	 * @param tkn - A string tokenizer containing the rest of the destroy command
 	 * @author Anthony and Michael
 	 ***************************************/
 	public void destroy(StringTokenizer tkn){
 		
 		//Try to destroy the object
 		try{
 			
 			//Grab the name of the object to be destroyed
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be destroyed
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If destroying the crust
 				case CRUST:
 					crust = null;
 					break;
 				
 				//Otherwise object is not meant to be destroyed
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help destroy'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: destroy [object]");
 		}
 		
 	}
 	
 	/***************************************
 	 * Prints out the information of an object
 	 * @param tkn - A string tokenizer containing the rest of the print command
 	 * @author Anthony and Michael
 	 ***************************************/
 	public void print(StringTokenizer tkn){
 		
 		//Try to print the object
 		try{
 			
 			//Grab the name of the object to be printed
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be manipulated
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If printing the crust
 				case CRUST:
 					if(crust!=null){
 						crust.print();
 						crust.printRelationships();
 					}else
 						System.out.println("No crust available or invalid command.\nUsage: print [object]");
 					break;
 				
 				//Otherwise object is not meant to be printed
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help print'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: print [object]");
 		}
 		
 	}
 	
 	/***************************************
 	 * Creates a new object with default constructors
 	 * @param tkn - A string tokenizer containing the rest of the random command
 	 * @author Anthony and Michael
 	 ***************************************/
 	public void random(StringTokenizer tkn){
 		
 		//Try to create the object
 		try{
 			
 			//Grab the name of the object to be created
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be manipulated
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If creating the crust
 				case CRUST:
 					crust = new Crust();
 					break;
 				
 				//Otherwise object is not meant to be created
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help create'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: create [object]");
 		}
 	}
 	
 	/***************************************
 	 * Creates a new object and asks user for further input to determine its values
 	 * @param tkn - A string tokenizer containing the rest of the custom command
 	 * @param scan - A Scanner which needs to be passed in from the Commandline calling custom
 	 * @author Anthony
 	 ***************************************/
 	public void custom(StringTokenizer tkn, Scanner scan){
 		
 		//Try to create the object
 		try{
 			
 			//Grab the name of the object to be created
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be manipulated
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If creating the crust
 				case CRUST:
 					crust = new Crust();
 					
 					//array to hold all of the trait names
 					String[] traits = {"firstName","middleName", "lastName", "warmth",
 							"reasoning", "emotionalStability", "dominance", "liveliness",
 							"ruleConsciousness", "socialBoldness", "sensitivity",
 							"vigilance", "abstractedness", "privateness", "apprehensivness",
 							"opennessToChange", "selfReliance", "perfectionism", "tension" };
 					
 					//other variable declarations
 					String input = ""; //Input string for storing the user's given value
 					boolean prompted = false;//boolean for displaying second prompt only once
 					
 					//Print instructions
 					System.out.println("Start by naming your Crust,");
 					System.out.println("or type 'cancel' at anytime to exit crust creation.");
 					
 					for(int i = 0; i <= 18; i++){
 						
 						//Print necessary prompts
 						if(i == 3 && !prompted){
 							System.out.println("Now set you Crust's personality traits.");
 							System.out.println("Traits should be an integer between -100 and 100.");
 							prompted = true;
 						}
 						System.out.print("$:");
 						System.out.print(" " + traits[i].toUpperCase() + ": ");
 						
 						
 						//Scan for input
 						input = scan.nextLine();
 						
 						//See if custom creation was cancelled, if so set crust = null and return
 						if(input.equals("cancel")){ 
 							crust = null;
 							return;
 						}
 						
 						//otherwise try to set the trait, recalling this iteration if bad input given
 						try{
 							crust.set(traits[i], input);
 						}catch(Exception e){
 							System.out.println("Traits must be an integer between -100 and 100");
 							i--;
 						}
 						
 					}
 					break;
 					
 				//Otherwise object is not meant to be created
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help create'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: create [object]");
 		}
 	}
 	
 	/***************************************
 	 * Sets a variable in an object
 	 * @param tkn - A string tokenizer containing the rest of the set command
 	 * @author Anthony and Michael
 	 ***************************************/
 	public void set(StringTokenizer tkn){
 		
 		//Try the list of the following objects
 		try{
 			
 			//Grab the name of the object to be manipulated
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be manipulated
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If setting the crust
 				case CRUST:
 					if(crust!=null && tkn.countTokens()>1)
 						crust.set(tkn.nextToken(), tkn.nextToken());
 					else
 						System.out.println("Crust does not exist or invalid commmand.\nUsage: set [object] [variable] [value]");
 					break;
 				
 				//Otherwise object is not meant to be manipulated
 				default:
 					System.out.println("Invalid command, given object can not be manipulated. Type 'help set'");
 					break;
 			}
 		
 		//Object not in list or not enough in the list
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: set [object] [variable] [value]");
 		}
 	}
 	
 	/***************************************
 	 * Gets a variable in an object
 	 * @param tkn - A string tokenizer containing the rest of the get command
 	 * @author Anthony and Michael
 	 ***************************************/
 	public void get(StringTokenizer tkn){
 		
 		//Try the list of the following objects
 		try{
 			
 			//Grab the name of the object to be retrieved
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be retrieved
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If getting the crust
 				case CRUST:
 					if(crust!=null && tkn.hasMoreTokens())
 						System.out.println("Value: "+crust.get(tkn.nextToken()));
 					else
 						System.out.println("Crust does not exist or invalid commmand.\nUsage: get [object] [variable]");
 					break;
 				
 				//Otherwise object is not meant to be gotten
 				default:
 					System.out.println("Invalid command, given object can not be manipulated. Type 'help get'");
 					break;
 			}
 		
 		//Object not in list or not enough in the list
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: set [object] [variable]");
 		}
 	}
 }
