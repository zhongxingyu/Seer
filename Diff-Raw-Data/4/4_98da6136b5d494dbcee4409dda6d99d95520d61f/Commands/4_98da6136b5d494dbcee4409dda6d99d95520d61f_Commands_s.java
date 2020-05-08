 package dk.cphbusiness.commands;
 
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.Maps;
 
 import dk.cphbusiness.commands.handlers.CommandHandler;
 import dk.cphbusiness.commands.handlers.HelpHandler;
 import dk.cphbusiness.commands.handlers.QuitHandler;
 import dk.cphbusiness.exceptions.CommandNotRegisteredException;
 import dk.cphbusiness.utils.Translator;
 
 public class Commands {
     private Map<Command, Class<? extends CommandHandler>> commandsAndHandlers;
 
     public Commands() {
         commandsAndHandlers = Maps.newHashMap();
         
         commandsAndHandlers.put(new Command("help", Translator.getMessage("help.command")), HelpHandler.class);
         commandsAndHandlers.put(new Command("quit", Translator.getMessage("quit.command")), QuitHandler.class);
        commandsAndHandlers.put(new Command("move", Translator.getMessage("quit.command")), QuitHandler.class);
     }
     
     public Command[] listCommands() {
         Set<Command> keySet = commandsAndHandlers.keySet();
         
         return keySet.toArray(new Command[keySet.size()]);
     }
     
     public Class<? extends CommandHandler> getCommandHandler(Command cmd) throws CommandNotRegisteredException {
         if (!commandsAndHandlers.containsKey(cmd))
             throw new CommandNotRegisteredException(cmd.getName());
         return commandsAndHandlers.get(cmd);
     }
 }
