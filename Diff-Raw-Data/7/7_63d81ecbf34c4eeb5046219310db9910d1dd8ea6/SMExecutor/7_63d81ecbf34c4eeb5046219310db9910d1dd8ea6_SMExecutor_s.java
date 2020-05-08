 package org.x3.mail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.x3.mail.event.MessageReadEvent;
 import org.x3.mail.event.MessageSentEvent;
 import org.x3.mail.util.Message;
 import org.x3.mail.util.Parser;
 
 public class SMExecutor implements CommandExecutor {
 
 	private final SimpleMail sm;
 	private final PluginManager pm;
 	private static HashMap<String, String> helpTopics = new HashMap<String, String>();
 
 	public SMExecutor(SimpleMail sm) {
 		this.sm = sm;
 		this.pm = sm.getServer().getPluginManager();
 	}
 
 	// TODO CLEAN THE HELL OUT OF THIS.
 
 	@Override
 	public boolean onCommand(CommandSender cmdSender, Command command,
 			String label, String[] args) {
 		if (args.length == 0) {
 			return false;
 		}
 		String sender = getName(cmdSender);
 		if (args[0].equalsIgnoreCase("send")) {
 			if (args.length < 3) {
 				return false;
 			}
 			args[0] = sender;
 			Parser parser = new Parser(args);
 			parser.debugInfo(sm.getLogger());
 			Message message = new Message(parser);
 			cmdSender.sendMessage("Sending message to " + parser.getRecipient()
 					+ " with priority " + parser.getPriority());
 			sm.send(message);
 			pm.callEvent(new MessageSentEvent(message));
 			return true;
 		} else if (args[0].equalsIgnoreCase("help")) {
 			String topic = (args.length > 1) ? getTopic(args[1].toLowerCase())
 					: getTopic("help");
 			cmdSender.sendMessage(topic);
 			return true;
 		} else if (args[0].equalsIgnoreCase("get")) {
 			if (!(sm.hasMail(sender))) {
 				cmdSender.sendMessage(ChatColor.GRAY + "You have no new mail.");
 				return true;
 			}
 			if (args.length > 1) {
 				return false;
 			}
 			ArrayList<Message> playerMail = sm.getMail(sender);
 			Message[] messages = reorderMessages(playerMail);
			cmdSender.sendMessage(ChatColor.GRAY
					+ String.format("----- Mail: %s -----", sender));
 			readMessages(cmdSender, messages);
 			sm.removeMail(sender);
 			pm.callEvent(new MessageReadEvent(cmdSender, messages));
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes the player's request for a help topic
 	 * 
 	 * @param topic
 	 *            The requested topic
 	 * @return The information on that topic
 	 */
 	private String getTopic(String topic) {
 		topic = topic.toLowerCase();
 		if (helpTopics.containsKey(topic)) {
 			return helpTopics.get(topic);
 		} else {
 			return ChatColor.RED
 					+ String.format("Help topic %s does not exist.", topic);
 		}
 	}
 
 	/**
 	 * Assigns a name to the CommandSender. If the CommandSender is a player, it
 	 * returns the name of the player; if not, it returns "Console".
 	 * 
 	 * @param sender
 	 *            The CommandSender
 	 * @return The assigned name of the CommandSender
 	 */
 
 	private String getName(CommandSender sender) {
 		if (sender instanceof Player) {
 			return ((Player) sender).getName();
 		} else {
 			return "Console";
 		}
 	}
 
 	/**
 	 * Reorders the messages in a player's inbox by priority (currently only
 	 * performed when mail is requested)
 	 * 
 	 * @param mail
 	 *            The player's inbox
 	 * @return Reordered {@code Message[]} object (for more accurate order than
 	 *         an {@code ArrayList<Message>}
 	 */
 	private Message[] reorderMessages(ArrayList<Message> mail) {
 		Message[] result = new Message[mail.size()];
 		int count = 0;
 		for (int i = 4; i >= 0; i--) {
 			for (Iterator<Message> it = mail.iterator(); it.hasNext();) {
 				Message m = it.next();
 				if (m.getPriority().getCode() == i) {
 					result[count] = m;
 					it.remove();
 					count++;
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Executes '/mail get' from the perspective of the given CommandSender.
 	 * This method should only be called after the player's mail has been
 	 * checked and messages have been reordered.
 	 * 
 	 * @param sender
 	 *            The sender of the command (CommandSender used for console
 	 *            support)
 	 * @param messages
 	 *            Reordered messages
 	 */
 
 	public void readMessages(CommandSender sender, Message[] messages) {
 		sender.sendMessage(ChatColor.GRAY
 				+ String.format("----- Mail: %s -----", getName(sender)));
 		for (int i = 0; i < messages.length; i++) {
 			Message message = messages[i];
 			int priority = message.getPriority().getCode();
			ChatColor color = (priority < 3) ? ChatColor.RED : ChatColor.GRAY;
			String prefix = (priority < 3) ? "URGENT: " : "";
 			String text = String.format(message.getFormat(),
 					message.getSender(), message.getMessage());
 			sender.sendMessage(color + prefix + text);
 		}
 	}
 
 	static {
 		helpTopics.put("send", "/mail send <target> [priority:#] <message>");
 		helpTopics.put("get", "/mail get");
 		helpTopics.put("help", "Topics: send, get");
 	}
 
 }
