 package net.jeebiss.questmanager.denizencommands;
 
 import net.aufdemrand.denizen.exceptions.CommandExecutionException;
 import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
 import net.aufdemrand.denizen.scripts.ScriptEntry;
 import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
 import net.aufdemrand.denizen.utilities.arguments.aH;
 import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
 import net.aufdemrand.denizen.utilities.debugging.dB;
 import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
 import net.jeebiss.questmanager.quests.QuestController;
 
 public class QuestCommand extends AbstractCommand{
 	private enum QuestType { START, FINISH, FAIL }
 	private QuestType TYPE = null;
 	
 	private String scriptName = null;
 	private String questName = null;
 	
 	@Override
 	public void parseArgs(ScriptEntry scriptEntry)
 			throws InvalidArgumentsException {
 		for (String arg : scriptEntry.getArguments()) {
 			if (aH.matchesArg("START, FINISH, FAIL",  arg)){
 				try {
 					TYPE = QuestType.valueOf(aH.getStringFrom(arg));
 					dB.echoDebug("...set TYPE to: " + TYPE.name());
 				} catch (Exception e) {e.printStackTrace();}
			} else if (aH.matchesValueArg("SCRIPT", arg, ArgumentType.Script)) {
 					scriptName = aH.getStringFrom(arg);
 					dB.echoDebug("...set SCRIPT to use '%s'", scriptName);
 			} else if (aH.matchesValueArg("NAME", arg, ArgumentType.Script)) {
 					questName = aH.getStringFrom(arg);
 					dB.echoDebug("...set NAME to use '%s'", questName);
 			}
			else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
 
 		}
 		
 		scriptEntry.addObject("TYPE", TYPE);
 		scriptEntry.addObject("scriptName", scriptName);
 		scriptEntry.addObject("questName", questName);
 	}
 
 	@Override
 	public void execute(ScriptEntry scriptEntry)
 			throws CommandExecutionException {
 		switch ((QuestType)scriptEntry.getObject("TYPE")) {
 		case START:
 			QuestController questController = new QuestController((String) scriptEntry.getObject("scriptName"), (String) scriptEntry.getObject("questName"));
 			break;
 
 		case FINISH:
 			break;
 
 		case FAIL:
 			break;
 		}
 		
 	}
 }
