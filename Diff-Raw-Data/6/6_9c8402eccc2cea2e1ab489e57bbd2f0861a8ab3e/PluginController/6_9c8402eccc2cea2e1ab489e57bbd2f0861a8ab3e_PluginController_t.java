 package roujo.emily.plugins.basic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import roujo.emily.core.MessageContext;
 import roujo.emily.core.commands.Command;
 import roujo.emily.core.extensibility.Plugin;
 import roujo.emily.core.extensibility.capabilities.CommandManager;
 import roujo.emily.plugins.basic.commands.*;
 
 public class PluginController implements Plugin, CommandManager {
 	private List<Command> commands;
 	
 	public PluginController() {
 		commands = new ArrayList<Command>();
 	}
 
 	@Override
 	public boolean processMessage(MessageContext context) {
		for(Command command : commands) {
			if(command.getPattern().matcher(context.getMessage()).matches())
 				return command.execute(context);
		}
 		return false;
 	}
 
 	@Override
 	public boolean load() {
 		commands.add(new EchoCommand());
 		commands.add(new HatsCommand());
 		commands.add(new JoinCommand());
 		commands.add(new LionCommand());
 		commands.add(new PartCommand());
 		commands.add(new QuitCommand());
 		commands.add(new RollCommand());
 		commands.add(new SlapCommand());
 		commands.add(new TellCommand());
 		commands.add(new TimeCommand());
 		commands.add(new VoiceCommand());
 		return true;
 	}
 
 	@Override
 	public boolean unload() {
 		commands.clear();
 		return true;
 	}
 
 }
