 import java.util.HashMap;
import java.util.Set;
 
 /**
  * This class is part of the "World of Zuul" application. 
  * "World of Zuul" is a very simple, text based adventure game.  
  * 
  * This class holds an enumeration of all command words known to the game.
  * It is used to recognise commands as they are typed in.
  *
 
  */
 
 public class CommandWords
 {
     // a constant array that holds all valid command words
     private static final String[] validCommands = {
         "quit", "help" , "look", "back", "undo","redo"
     };
     
     private final static HashMap<String, String> reversibleCommands = new HashMap<String, String>();
     static {
     	reversibleCommands.put("pick", "drop");
     	reversibleCommands.put("drop", "pick");
     	reversibleCommands.put("attack", "heal");
     	reversibleCommands.put("heal", "attack");
     	reversibleCommands.put("go", "go");
 	}
     
     
     public final static HashMap<String, String> reverseSecondWord = new HashMap<String, String>();
     static {
     	reverseSecondWord.put("north", "south");
     	reverseSecondWord.put("south", "north");
     	reverseSecondWord.put("east", "west");
     	reverseSecondWord.put("west", "east");
 	}
 
     /**
      * Constructor - initialise the command words.
      */
     public CommandWords()
     {
         // nothing to do at the moment...
     }
 
     /**
      * Check whether a given String is a valid command word. 
      * @return true if a given string is a valid command,
      * false if it isn't.
      */
     public boolean isCommand(String aString)
     {
     	//Check to see if the string is in the reversible commands
     	if (reversibleCommands.containsKey(aString)) {
 			return true;
     	}
     	
     	//Check to see if the string is in the array of other commands
         for(int i = 0; i < validCommands.length; i++) {
             if(validCommands[i].equals(aString))
                 return true;
         }
         // if we get here, the string was not found in the commands
         return false;
     }
     /**
      * returns string of all valid commands
      */
     public String getCommandList(){
         String commandList = "";
         for(String command : validCommands ){
             commandList+=(command + " " );
         }
        
        Set<String> reverseCommandsSet = reversibleCommands.keySet();
        for(String command : reverseCommandsSet ){
            commandList+=(command + " " );
        }
        
         return commandList;
     }
     
     /**
      * Returns a copy of the list of reversible commands
      * @return
      */
     public HashMap<String, String> getReversibleCommands() {
     	return reversibleCommands;
     }
     
     public boolean isReversible(String s)
     {
     	return reversibleCommands.containsKey(s);
     }
     
     public Command getReverse(Command c)
     {
     	//If command is non-reversible, return a null Command
     	if(!isReversible(c.getCommandWord()))return null;
     	
     	//If first words reverse is equal to itself (such as go)
     	//Return a command with original first word and reverse second word
     	if(c.getCommandWord().equals(reversibleCommands.get(c.getCommandWord())))
     	{
     		return new Command(c.getCommandWord(), reverseSecondWord.get(c.getSecondWord()));
     	}
     	
  
     	//Return a command with reversed first word and original second word
     	return new Command(reversibleCommands.get(c.getCommandWord()), c.getSecondWord());
     }
     
 }
