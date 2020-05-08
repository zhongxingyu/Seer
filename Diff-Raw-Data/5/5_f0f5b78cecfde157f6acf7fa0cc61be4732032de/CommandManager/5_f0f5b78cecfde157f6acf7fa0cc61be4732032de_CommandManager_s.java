 package com.drtshock.willie.command;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.auth.Auth;
 
 public class CommandManager extends ListenerAdapter<Willie> implements Listener<Willie>{
 
 	private Willie bot;
 	private HashMap<String, Command> commands;
 	private String cmdPrefix;
 
 	public CommandManager(Willie bot){
 		this.bot = bot;
 		this.cmdPrefix = bot.getConfig().getCommandPrefix();
 		this.commands = new HashMap<>();
 	}
 
 	public void registerCommand(Command command){
 		this.commands.put(command.getName(), command);
 	}
 
 	public Collection<Command> getCommands(){
 		return this.commands.values();
 	}
 
 	public void setCommandPrefix(String prefix){
 		this.cmdPrefix = prefix;
 	}
 
 	@Override
 	public void onMessage(MessageEvent<Willie> event){
 		String message = event.getMessage().trim();
 
		if(message.toLowerCase().endsWith("o/")){
 			event.getChannel().sendMessage("\\o");
 			return;
 		}
		if(message.toLowerCase().endsWith("\\o")){
 			event.getChannel().sendMessage("o/");
 			return;
 		}
 
 		if(!message.startsWith(cmdPrefix)){
 			return;
 		}
 
 		String[] parts = message.substring(1).split(" ");
 		Channel channel = event.getChannel();
 
 		String commandName = parts[0].toLowerCase();
 		String[] args = new String[parts.length - 1];
 		System.arraycopy(parts, 1, args, 0, args.length);
 
 		Command command = this.commands.get(commandName);
 		if(command.isAdminOnly() && !Auth.checkAuth(event.getUser()).isAdmin){
 			channel.sendMessage(Colors.RED + String.format(
 					"%s, you aren't an admin. Maybe you forgot to identify yourself?", event.getUser().getNick()));
 			return;
 		}
 		command.getHandler().handle(this.bot, channel, event.getUser(), args);
 	}
 
 }
