 import java.util.*;
 public class IOController {
 
 	/**
 	 * @param args
 	 */
 	
 	private static boolean isExitGame = false;
 	//private static int command = 0;
 	private static String command = new String();
 	private static Scanner input = new Scanner(System.in);
 	private static boolean hasWon = false;
 	private static boolean hasLost = false;
 	private static LevelController level = new LevelController();
 	private static int levelNo = 1;
 	private static Animals animals = new Animals();
 	private static Weapons weapons = new Weapons();
 	private static GameTimer timer = new GameTimer();
 	public static HashMap<Integer,String> resultMap = new HashMap<Integer,String>();
 	
 	public IOController(){
 		resultMap.put(Constants.CONTINUE, "Continue");
 		resultMap.put(Constants.WON, "You Win!");
 		resultMap.put(Constants.LOST, "You Lose!");
 		resultMap.put(Constants.WON_WITH_TIME, "You have won a medal, you are the King of the Jungle");
 		resultMap.put(Constants.WEAPON_NOT_FOUND, "The weapon you selected cannot be used to kill this animal! Select another weapon:");
 		resultMap.put(Constants.WEAPON_USED, "The weapon has been used! Choose another weapon.");
 		resultMap.put(Constants.ANIMAL_KILLED, "The animal has been killed!");
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
         // TODO code application logic here
         IOController disp = new IOController();
         
         while(!isExitGame)
         {
         	hasWon = false;
         	hasLost = false;
             do
             {
             	levelNo = 1;
             	try
                 {
 	                if(levelNo != 1 && levelNo != 2 && levelNo!=3)
 	                {
 	                    System.out.print("Enter Game Level 1, 2 or 3\n");
 	                }
 	                else
 	                {
 	                    System.out.print("Enter Game Level\n");                
 	                                    
 	                }
 	                levelNo = Integer.parseInt(input.next());
                 }
                 catch(Exception e)
                 {
                     levelNo = 0;
                 }
             }while(levelNo != 1 && levelNo != 2 && levelNo!=3);
             level = new LevelController();
             level = LevelFactory.getController(levelNo);
             
             //testing
             //disp.displayList(animals.getNameToIndexList());
             //disp.displayList(weapons.getNameToIndexList());
             boolean startedHunting = false;
             
             command = input.nextLine();
             hasLost = false;
             hasWon = false;
             while(!hasLost && !hasWon){
 //            	System.out.println("Enter 1 to list animals");
 //            	System.out.println("Enter 2 to list weapons");
 //            	System.out.println("Enter 3 to hunt");
 //            	System.out.println("Enter 4 to exit");
             	//command = input.nextInt();
             	
            	System.out.println("\nCommands:");
             	System.out.println("\t");
             	command = input.nextLine();
             	
                 if(command.equalsIgnoreCase("list animals"))
             	//if(command == 1)
                 {
                    disp.displayList((ArrayList<String>) animals.getNameToIndexList().keySet());
                 }
                 else if(command.equalsIgnoreCase("list weapons"))
             	//else if(command == 2)
                 {
                    disp.displayList((ArrayList<String>) weapons.getNameToIndexList().keySet());
                 }
                 else if(command.equalsIgnoreCase("hunt"))
             	//else if(command == 3)
                 {
                     if (!startedHunting)
                     {
                         startedHunting = true;
                         timer.start();
                     }
                     disp.hunt();
                 }
             	//else if(command == 4)
                 else if(command.equalsIgnoreCase("exit"))
             	{ 
             		System.exit(1);
             	}
                 else if(command.equalsIgnoreCase("help"))
                 {
                 	System.out.println("I did not understand");
                 }
             } 
             System.out.println("Replay the game?[Y/N]");
             if(disp.inputString().equalsIgnoreCase("n")){
             	isExitGame = true;
             	System.out.println("The hunter left the forest!");
             }
         }
     }
     
     public void displayList(ArrayList<String> map){
         System.out.println();
         for(String s: map)
         {
         	if(!s.isEmpty()){
         		System.out.println(s);
         	}
         }
         System.out.println();
     }
     
     public void hunt(){
     	String animal = new String();
     	String weapon = new String();
     	boolean isDone = false;
     	int turnCount = 0;
     	int resultKey;
     	boolean isAnimalSelected = false;
     	long timeInSecs = 0;
     	while(true){
     		displayHuntGrid();
             //test
     		System.out.print("Please select an animal:");
 			animal = inputString();
     		do{
     			if(!level.huntMap.keySet().contains(animal)){
     				if (animals.getAnimalList().containsValue(animal)){
         				System.out.print("The animal has been killed! Select another animal: ");
         			}
     				else if (!animals.getAnimalList().containsValue(animal)){
     					System.out.print("This animal is not in the forest. Select another animal: ");
     				}
     				animal = inputString();
     			}
     			else
     			{
     				isAnimalSelected = true;
     				System.out.println("Weapon checklist for "+animal+":");
     				displayList(level.huntMap.get(animal));
     			}
     		}while(!isAnimalSelected); 
     		turnCount = 0;
     		System.out.print("Please select a weapon:");
 			weapon = inputString();
     		do{
     			timeInSecs = timer.getElapsedTimeSecs();
     			++turnCount;
     			resultKey = level.hunt(animal, weapon, timeInSecs, turnCount);
     			if(resultKey == Constants.WEAPON_NOT_FOUND || resultKey == Constants.WEAPON_USED){
     				isDone = false;
     				System.out.println(resultMap.get(resultKey));
     				weapon = inputString();
     			}
     			else{
     				isDone = true;
     			}
     		}while(!isDone);
     		
     		if(resultKey == Constants.CONTINUE){
     			System.out.println("Number of animals left: " + level.animalsLeft);
     			//displayList(level.huntMap.get(animal));
     		}
     		else
     		{
     			if(resultKey == Constants.LOST){
     				hasLost = true;
     				System.out.println(resultMap.get(Constants.LOST));
     			}
     			else if (resultKey == Constants.WON || resultKey == Constants.WON_WITH_TIME){
     				hasWon = true;
     				timer.stop();
     				if(level.isTimeNeeded){
     					System.out.println("Time taken to complete(in seconds): " + timeInSecs);
     				}
     				System.out.println(resultMap.get(Constants.WON));
     				if (resultKey == Constants.WON_WITH_TIME){
     					System.out.println(resultMap.get(Constants.WON_WITH_TIME));
     				}
     			}
     			return;
     		}
     	}
     }
     
     public void displayHuntGrid(){
     	HashMap<String,Integer> animalToIndex = new HashMap<String, Integer>();
     	animalToIndex = animals.getNameToIndexList();
         for(String s: level.huntMap.keySet()){
         	if(level.animalWeaponMap.keySet().contains(animalToIndex.get(s))){
         		if(s.length()<=7)
         		{
         			System.out.print("\t"+s+"\t");
         		}
         		else
         		{
         			System.out.print("\t"+s);
         		}
         	}
         	else
         	{
         		System.out.print("\t\t");
         	}
         }
         System.out.println("\n\t-------------------------------------------------------------------------");
         for(int i = 0; i<weapons.getWeaponCount();++i){
         	for(String s: level.huntMap.keySet()){
             	if(level.animalWeaponMap.keySet().contains(animalToIndex.get(s))){
             		if(level.huntMap.get(s).get(i).length()<=7)
             		{
             			System.out.print("\t"+level.huntMap.get(s).get(i)+"\t");
             		}
             		else
             		{
             			System.out.print("\t"+level.huntMap.get(s).get(i));
             		}
             	}
             	else
             	{
             		System.out.print("\t\t");
             	}
             } 
         	System.out.println("\n\n");
         }
     }
     
     private String inputString(){
     	try{
     		return input.next().toLowerCase();
     	}
     	catch(Exception e){
     		return "x";
     	}
     }
 }
