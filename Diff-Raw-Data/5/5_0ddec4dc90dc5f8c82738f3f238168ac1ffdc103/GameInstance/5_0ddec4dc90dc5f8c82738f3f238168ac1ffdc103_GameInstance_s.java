 /*******************************************************
 * Class for game instances, will be associated with
 * sessions through being the session data.
 *******************************************************/
 
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class GameInstance {
     PlayerCharacter playerChar;
     AresCharacter aresChar;
     stateEnum currentState;
     String accountName;
     
     GameInstance()
     {
         playerChar = null;
         aresChar = null;
         currentState = stateEnum.INIT;
         accountName = null;
     }
     
     /****************************************************
      * state machine case switch function, called from 
      *      the servlet.
      * @param out output PrintWriter
      * @param request the servlet request
      ***************************************************/
     void advanceGame(PrintWriter out, HttpServletRequest request)
     {
         stateEnum nextState = currentState;
         do
         {
             currentState = nextState;
             
             switch(currentState)
             {
                 case INIT:
                     //first connection
                     nextState = initState(out, request);
                     break;
 
                 case BATTLE:
                     //battle function
                     nextState = battleState(out, request);
                     break;
 
                 case STORE:
                     //store function
                     nextState = storeState(out, request);
                     break;
 
                 case REGISTERED_CHARACTER_CREATION:
                     //character creation
                     nextState = registeredCharacterCreationState(out, request);
                     break;
 
                case UNREGISTERED_CHARACTER_CREATION:
                     //character creation
                     nextState = unregisteredCharacterCreationState(out, request);
                     break;
 
                 case IDLING:
                     //idle state
                     nextState = idling(out, request);
                     break;
 
                 case DECISION:
                     //this state is for asking what to do next
                     nextState = decisionState(out, request);
                     break;
 
                 case BLACKSMITH:
                     //upgrade items
                     nextState = blackSmithState(out, request);
                     break;
                     
                 case LOGIN:
                     //login
                     nextState = loginState(out, request);
                     break;
                     
                 case ACCOUNT_CREATION:
                     nextState = accountCreation(out, request);
                     break;
 
                 default:
                     //this should go to a specified state
                     nextState = stateEnum.INIT;
                     initState(out, request);
                     break;
             }
         }while(currentState != nextState);
     }
     
     
     /****************************************************
      * Generates an inventory for the store based on the
      *      player character's level
      * @param level the level of the player character
      * @return an array of new items for the store
      ***************************************************/
     Item[] getStoreInventory(int level)
     {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     /****************************************************
      * Loads a new enemy from the database
      * @param level the players level
      ***************************************************/
     void getNextEnemy(int level)
     {
     
     }
     
     /****************************************************
      * Loads the players current character from the 
      *      database
      ***************************************************/
     void getCurrentCharacter()
     {
         
     }
     
     /****************************************************
      * Adds a newly created character to the database
      * @param name the character's name
      * @param level the level of the character
      * @param bio  a biography about the character
      * @param health the health of the character
      * @param strength the strength of the character
      * @param agility the agility of the character
      * @param magic  the magic of the character
      * @param itemsHeld the items held by the character
      ***************************************************/
     void newCharacter(String name, int level, String bio, int health, int strength, int agility, int magic,Item[] itemsHeld)
     {
         
     }
 
     
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum initState(PrintWriter out, HttpServletRequest request) {
         //can log in or unregistered user creation
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum storeState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 		
 		// have store level as well as the items be static so that it is the same each time the player comes back to the 
 		// store unless the player has increased in level
		static store_level = 1;
 		const int STORE_SIZE = 20;
 		static item[] item_array = new item[STORE_SIZE];
 		
 		// if level has changed create a new item inventory for the store
 		// based on some hash function of the character's level
 		if(playerChar.getLevel() != store_level)
 		{
 			store_level = playerChar.getLevel();
 			for(int i = 0; i < STORE_SIZE; i++)
 				{
 				// need to place the parameters for how each item could be created
 				item_array[i] = new Item(name = "", id = null, type = (i % 9), upgradeCount = 0, strength = , agility = , magic =  );
 				}
 		}
 		
 		
 		// if item bought, add to inventory
		
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum battleState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Create a registered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum registeredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /****************************************************
      * Create an unregistered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum unregisteredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * The idling state, logs out after a certain amount of time
      * may be removed
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum idling(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Asking what the player wants to do next
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum decisionState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * At the blacksmith and can upgrade items
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum blackSmithState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Registered user login state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum loginState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Registered user creation state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum accountCreation(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
