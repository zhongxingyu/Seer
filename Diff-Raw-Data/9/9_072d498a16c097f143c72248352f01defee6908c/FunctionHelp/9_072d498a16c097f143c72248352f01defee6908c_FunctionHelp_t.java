 package net.acomputerdog.TerrainEdit.functions;
 
 import net.acomputerdog.BlazeLoader.api.chat.EChatColor;
 import net.acomputerdog.TerrainEdit.main.CommandTE;
 import net.acomputerdog.TerrainEdit.main.ModTerrainEdit;
 import net.minecraft.src.ICommandSender;
 
 /**
  * A function to return a list of all TE functions and their descriptions.
  */
 public class FunctionHelp extends BaseFunction {
 
     public FunctionHelp(ModTerrainEdit baseMod, CommandTE baseCommand) {
         super(baseMod, baseCommand);
     }
 
     /**
      * Gets the number of arguments required.  For a varied amount return 0 and process manually.
      *
      * @return Return the number of required args.
      */
     @Override
     public int getNumRequiredArgs() {
         return 0;
     }
 
     /**
      * Gets the name of the function.
      *
      * @return Return the name of the function.
      */
     @Override
     public String getFunctionName() {
         return "help";
     }
 
     /**
      * Gets the usage of the function.
      *
      * @return Return the usage of the function.
      */
     @Override
     public String getUsage() {
         return "help [page]";
     }
 
     /**
      * Checks if the user has the required permissions to use the command.
      *
      * @param user The user attempting to perform the commands.
      * @return Return true if the user can use the command, false if not.
      */
     @Override
     public boolean canUserUseCommand(ICommandSender user) {
         return user.canCommandSenderUseCommand(4, "te");
     }
 
     /**
      * Executes the command.
      *
      * @param user The user executing the command.
      * @param args The arguments passed to the module.
     * -WARNING: args[0] is always the name of the sub-command!  Skip it!-
      */
     @Override
     public void execute(ICommandSender user, String[] args) {
         int numPages = Math.max(baseCommand.functionList.size() / 6, 1);
         int currPage = 1;
         try{
             if(args.length >= 2){
                 currPage = Integer.parseInt(args[1]);
             }
             if(currPage > numPages){
                 sendChatLine(user, EChatColor.COLOR_RED + "" + "Page does not exist!  There are only " + numPages + " pages!");
             }else{
                 sendChatLine(user, EChatColor.COLOR_DARK_GREEN + "" + EChatColor.FORMAT_UNDERLINE + "Available TE functions (Page " +  + currPage + " of " + numPages + "):");
                 sendChatLine(user, "");
                 Object[] functions = baseCommand.functionList.values().toArray();
                 for(int index = 6 * (currPage - 1); index < 6 * currPage && index < baseCommand.functionList.size(); index++){
                     BaseFunction function = (BaseFunction)functions[index];
                     sendChatLine(user, EChatColor.COLOR_YELLOW + function.getFunctionName() + EChatColor.COLOR_ORANGE + " - " + function.getFunctionDescription());
                 }
             }
         }catch(NumberFormatException e){
             sendChatLine(user, EChatColor.COLOR_RED + "" + "Invalid page number!  Must be an integer!");
         }
 
     }
 
     /**
      * Gets a concise description of what the function does.
      *
      * @return Return a concise description of what the function does.
      */
     @Override
     public String getFunctionDescription() {
         return "Gets a list of TE functions";
     }
 }
