 import java.util.HashMap;
 import java.util.ArrayList;
 
 public class Command
 {
     private static HashMap<String, Commands> validCommands;
 
     /**
      * Initialize the HashMap for the validcommands and
      * put the commands in the HashMap.
      */
     public static void initCommandList()
     {
         validCommands = new HashMap<String, Commands>();
         validCommands.put("help", Commands.HELP);
         validCommands.put("cp", Commands.COPY);
         validCommands.put("exit", Commands.QUIT);
         validCommands.put("cd", Commands.CD);
         validCommands.put("cat", Commands.CAT);
         validCommands.put("uptime", Commands.UPTIME);
         validCommands.put("ls", Commands.LIST);
     }
     
     /**
      * Check whether a given String is a valid command word. 
      * @return true if it is, false if it isn't.
      */
     public static boolean isCommand(String aString)
     {
         return validCommands.containsKey(aString);
     }
     
      /**
      * Find the CommandWord associated with a command word.
      * @param commandWord The word to look up.
      * @return The CommandWord correspondng to commandWord, or UNKNOWN
      *         if it is not a valid command word.
      */
     public static Commands getCommand(String commandWord)
     {
         Commands command = validCommands.get(commandWord);
         if(command != null) {
             return command;
         }
         else {
             return Commands.UNKNOWN;
         }
     }
 
     /**
      * Processes the commands and the words which he receives
      * from the terminal and will look if the first word 
      * is a command, if so he will excute the method of that command,
      * if not he will say that it is a unknown command.
      * 
      * checks if the player wants to exit the game,
      * if not wantToQuit will be false and checks if the word
      * is another command. If the player types exit, wantToQuit
      * will be true and the game will stop.
      * 
      * @return      wantToQuit will return true if the players wants to exit.
      */
     public static boolean processCommand(ArrayList<String> words)
     {
         boolean wantToQuit = false;
         
         Commands commands = getCommand(words.get(0));       
         
         if(commands == Commands.UNKNOWN) {
             Terminal.print("Unknown Command.");
         }
         else if(commands == Commands.HELP) {
             if(words.size() == 2) { 
                 showCommandHelp(words.get(1));
             }
             else
             {
                 showAllCommands();
             }
         }
        else if (commands == Commands.COPY) {
           if(words.size() == 3) { 
               cp(words.get(1),words.get(2));
           }
           else {
             Terminal.print("Error: Insufficient parameters.");
             Terminal.print("For more information about the usage of this command type \"help cp\"");
           }
         }
         else if (commands == Commands.CD) {
             if(words.size() == 2){  
                 cd(words.get(1));
             }else{
                 Terminal.print("Wrong parameter");
             }
         }
         else if (commands == Commands.CAT) {
            //printHelp();
         }
         else if (commands == Commands.LIST) {
                 ls();
         }
        else if (commands == Commands.QUIT) {
            wantToQuit = true;
         } 
        return wantToQuit;
     }
     
     /**
      * Print all valid commands to System.out.
      */
     public static void showAllCommands() 
     {
         for(String existingCommand : validCommands.keySet()) {
             Terminal.printInline(existingCommand + "  ");
         }
         Terminal.print("");
         Terminal.print("for more information about a command type help [command]");
     }
     
     public static void showCommandHelp(String command)
     {
         //check trough all available help messages to find the one the user wants more information abou
         if(command.equals("help")) Terminal.print("There is no help about help because this would destroy the earth, just use help without a parameter");
         else if(command.equals("cp")) Terminal.print("WRITE THIS!!!");
         else if(command.equals("cd")) Terminal.print("WRITE THIS!!!");
         else if(command.equals("cat")) Terminal.print("WRITE THIS!!!");
         else if(command.equals("ls")) Terminal.print("WRITE THIS!!!");
         else if(command.equals("exit")) Terminal.print("This command exits the game/shell.");
         else Terminal.print("Sorry, there is no help topic found for the that command.");
     }
     
     public static void cd(String options) 
     {
         if(options == null)
         {
             Terminal.print("faal.");
         }
         else
         {
             Directory find = Filesystem.findDirectoryByName(options);
             if(find != null)
             {
                 if(find.getPassword() == null)
                 {
                     Terminal.print("test");
                     //Terminal.print(Terminal.getPassPrompt());
                     Filesystem.setCurrentDirectory(find);
                 }else{ 
                     Terminal.print("enter password:");
                     String input = Terminal.getUserInput();
                     String password = find.getPassword();
                     
                     if(input.equals(password)){
                         Filesystem.setCurrentDirectory(find);
                        // Terminal.setPrompt(Terminal.getPrompt());
                      }else{
                         Terminal.print("Bad password");
                      }
                 }
                 //Terminal.print(Filesystem.getCurrentDirectory().getPassword());
             }
             else if(options.equals(".."))
             {
                 Filesystem.setCurrentDirectory(Filesystem.getCurrentDirectory().getParent());
             }
             else
             {
                 Terminal.print("Sorry no such file or directory");
             }
         }
     }
     
     public static void ls() 
     {
         for(Directory dir : Filesystem.getCurrentChilds())
         {
             Terminal.print("[dir]     " + dir.getName());
         }
         for(File file : Filesystem.getCurrentFiles())
         {
             Terminal.print("[file]    " + file.getName() + "    " + file.getSize() + " kb");
         }
     }
     
     public static void cp(String param1, String param2)
     {
         File searchResult = Filesystem.findFileByName(param1);
         if(searchResult != null) {
            if(param2.equals("/home/"+GameController.getUsername())) {
                 Filesystem.copyFile(searchResult);
             }
              else {
                 Terminal.print("Error: You don't have the required permissions to copy the file " + param1 + " to the folder " + param2);     
              }
         }
         else {
             Terminal.print("Error: The source file you specified can not be found.");
         }  
     }
 }
