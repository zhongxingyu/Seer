 package desperatehousepi;
 
 import java.util.Scanner;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import javax.swing.Timer;
 
 import desperatehousepi.ItemSet.itemType;
 
 public class Commands {
 	
 	//Declare variables
 	private static Crust crust;
 	private static ItemSet inventory = new ItemSet();
 	private static ActionLog history = new ActionLog();
 	
 	/*********************************
 	 * Crust AI defined here.
 	 * \\note: is there any way we can move this to its own class?
 	 * @author Mark
 	 *********************************/
 	//Initialize crust AI
 	boolean altFlag = true;
 	boolean talkFlag = true;
 	int delay = 0;
 	String bedMsg = "Please make me a BED (;-;)";
 	private ActionListener crustAI = new ActionListener(){
 		@Override
 		public void actionPerformed(ActionEvent e){
 			StringTokenizer tkn;
 			
 			//CASE: Hunger<49
 			if( crust.getNeed("Hunger")<49 ){
 				if( altFlag == true ){
 					System.out.println("I'm hungry.. D:");
 					altFlag = !altFlag;
 				}
 				//See if we have a consumable in our inventory first
 				for( desperatehousepi.ItemSet.Item i : inventory.encyclopedia.values() ){
 					if( i.getValue("Hunger")>0 && inventory.has(i.item) ){
 						tkn = new StringTokenizer(i.item.name());
 						use(tkn);
 						return;
 					}
 				}
 				//If not, go hunting for fish
 				tkn = new StringTokenizer("item fish");
				random(tkn);
 				//Log hunting experience in history log.txt
 				try {
 					history.logAction("Crust has gone hunting and got FISH");
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				//Consume the FISH
 				tkn = new StringTokenizer("fish");
 				use(tkn);
 			}
 			
 			//CASE: Energy<49
 			if( crust.getNeed("Energy")<49 )
 				if( !inventory.has(itemType.BED) ){
 					if( talkFlag ){
 						System.out.println(bedMsg);
 						talkFlag = false;
 					}
 					//this is neccessary so the crust doesn't spam messages
 					delay++;
 					if( delay>10 ){ delay = 0; altFlag=true; talkFlag=true; bedMsg = "Please "+bedMsg; }
 				}
 				else{
 					tkn = new StringTokenizer("BED");
 					use(tkn);
 				}
 			//CASE: Energy<45
 			if( crust.getNeed("Energy")<45 ){
 				//Look for COFFEE
 				tkn = new StringTokenizer("item coffee");
				random(tkn);
 				//Log that the crust has found COFFEE
 				try {
 					history.logAction("Crust has found COFFEE");
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				//Consume the COFFEE
 				tkn = new StringTokenizer("coffee");
 				use(tkn);
 			}
 			//CASE: Entertainment<
 			if( crust.getNeed("Entertainment")<25 )
 				System.out.println("Entertain me, human.");
 		}
 	};
 	/*********************************/
 	
 	//Create objects that commands can alter
 	private static enum objectType{
 		CRUST, ITEM
 	}
 
 	/***************************************
 	 * Holds all of the executables for each command that can be called
 	 * @author Anthony and Michael
 	 ***************************************/
 	public Commands(Crust new_crust){
 		crust = new_crust;
 	}
 	
 	/***************************************
 	 * Start a conversation with the object passed in
 	 * @param tkn - A string tokenizer containing the rest of the save command
 	 * @author Michael
 	 ***************************************/
 	public void chat(StringTokenizer tkn){
 		
 		//Try to save the object
 		try{
 			
 			//Grab the name of the object to be saved
 			String obj = tkn.nextToken();
 			
 			//Find the object that is to be saved
 			switch(objectType.valueOf(obj.toUpperCase())){
 				
 				//If saving the crust
 				case CRUST:
 					crust.chat();
 					break;
 				
 				//Otherwise object is not meant to be saved
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help chat'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: chat [object]");
 		}
 	}
 	
 	 /***************************************
 	 * Prints a message explaining the proper usage of the given command or
 	 * a general help message if no parameter given
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
 					
 
 				//If destroying specified item
 				/*********************************
 				 * Edited 10/18/13 by Mark
 				 *********************************/
 				case ITEM:
 					if( !tkn.hasMoreTokens() )
 						System.out.println("Invalid command.\nUsage: destroy item [item name]");
 					else{
 						itemType itemName = itemType.valueOf(tkn.nextToken().toUpperCase());
 						inventory.destroy( itemName );
 					}	
 					break;
 				/*********************************/
 					
 				//Otherwise object is not meant to be destroyed
 				default:
 					System.out.println("Invalid command, given object can not be destroyed. Type 'help destroy'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage:\tdestroy [object]\n\tdestroy item [item name]");
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
 				
 				/*********************************
 				 * Edited 10/18/13 by Mark
 				 *********************************/
 				//If printing item
 				case ITEM:
 					//command "print item" should print all items that exist
 					if( !tkn.hasMoreTokens() ){
 						for( itemType name : itemType.values() )
 							if( inventory.has(name) )
 								inventory.getItem(name).print();
 					}
 					//command "print item [item name]" should print the specified item, created or not
 					else{
 						itemType itemName = itemType.valueOf(tkn.nextToken().toUpperCase());
 						inventory.encyclopedia.get(itemName).print();
 					}
 					break;
 				/*********************************/
 						
 					
 				//Otherwise object is not meant to be printed
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help print'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage:\tprint [object]\n\tprint item [item name]?");
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
 					new Timer(1000*4, crustAI).start();
 					break;
 					
 				//If creating an item
 				/*********************************
 				 * Edited 10/18/13 by Mark
 				 *********************************/
 				case ITEM:
 					if( !tkn.hasMoreTokens() ){
 						//Print what items are available
 						System.out.println("Usage: \tcreate item [item name]\nItems Available: " + inventory.toString() );
 						return;
 					}else{
 						//Grab name of specified item
 						itemType itemName = itemType.valueOf(tkn.nextToken().toUpperCase());
 						inventory.create( itemName );
 					}
 					break;
 				/**********************************/
 					
 				//Otherwise object is not meant to be created
 				default:
 					System.out.println("Invalid command, given object can not be created. Type 'help create'");
 					break;
 			}
 		
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage:\tcreate [object]\n\tcreate item [item name]");
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
 	
 	/***************************************
 	 * Lets your crust object use any item object
 	 * @param tkn - A string tokenizer containing the rest of the eat command
 	 * @author Mark and Luke
 	 ***************************************/
 	public void use(StringTokenizer tkn){
 
 		try{
 			//Check if crust exists
 			if( crust==null ){
 				System.out.println("No crust available.");
 				return;
 			}		
 			
 			//Grab the name of the object to be created
 			itemType itemName = itemType.valueOf(tkn.nextToken().toUpperCase());
 	
 			//Check if item exists
 			if( !inventory.has(itemName) ){
 				System.out.println("No " + itemName + " available");
 				return;
 			}
 			
 			//Apply item to crust
 			for( String need : inventory.getItem(itemName).getNeeds() ){
 				crust.incrementNeed(need, inventory.getItem(itemName).getValue(need));
 			}
 			
 			//Check if item is consumable
 			for( String need : inventory.getItem(itemName).getNeeds() ){
 				if( need=="Hunger" )
 					break;
 				else{
 					//if not, do not destroy
 					history.logAction("Crust has used "+itemName.name());
 					return;
 				}
 			}
 			inventory.destroy(itemName);
 			history.logAction("Crust has consumed "+itemName.name());
 			return;
 			
 		//Object not in list or invalid command
 		}catch(Exception e){
 			System.out.println("Invalid command.\nUsage: use [item name]");
 		}
 	}
 }
 
