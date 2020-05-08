 package org.x3.mail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.x3.mail.event.MessageSentEvent;
 import org.x3.mail.util.MailPriority;
 import org.x3.mail.util.Mailbox;
 import org.x3.mail.util.Mailbox.BoxType;
 import org.x3.mail.util.Message;
 import org.x3.mail.util.Parser;
 
 public class SMExecutor implements CommandExecutor {
 
 	private static HashMap<String, String> helpTopics = new HashMap<String, String>();
 	private final SimpleMail sm;
 	private final PluginManager pm;
 	private final Logger log;
 
 	public SMExecutor(SimpleMail sm) {
 		this.sm = sm;
 		this.pm = sm.getServer().getPluginManager();
 		this.log = sm.getLogger();
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
 			args[0] = sender;
 			Message message = new Message(new Parser(args));
 			message.setCancelled(sm.getPerms().shouldCancel(message));
 			send(message);
 			pm.callEvent(new MessageSentEvent(message));
 			return true;
 		} else if (args[0].equalsIgnoreCase("get")) {
 			String whichBox = (args.length == 1) ? "unread" : args[1]
 					.toLowerCase();
 			BoxType boxType = (BoxType.getFromType(whichBox) != null) ? BoxType
 					.getFromType(whichBox) : BoxType.getFromType("unread");
 			Mailbox box = sm.getMailbox(sender);
 			ArrayList<Message> messages = box.get(boxType);
 			if (messages.size() == 0) {
 				cmdSender.sendMessage(ChatColor.GREEN + "You have no "
 						+ boxType.getType() + " mail.");
 			} else {
 				ChatColor defaultColor = ChatColor.GRAY;
 				cmdSender.sendMessage(defaultColor + "== Mail: " + sender
 						+ " - " + boxType.getType() + " (" + messages.size()
						+ ") ==");
 				for (Message m : messages) {
 					Boolean urgent = m.getPriority() == MailPriority.URGENT;
 					ChatColor color = (urgent) ? ChatColor.RED : defaultColor;
 					String pre = (urgent) ? "URGENT: " : "";
 					cmdSender.sendMessage(color
 							+ pre
 							+ String.format(m.getFormat(), m.getSender(),
 									m.getMessage()));
					m.setUnread(false);
 				}
				box.refresh();
 			}
 			return true;
 		} else if (args[0].equalsIgnoreCase("help")) {
 			String topic = (args.length == 2) ? getTopic(args[1].toLowerCase())
 					: getTopic("help");
 			cmdSender.sendMessage(topic);
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
 	public String getTopic(String topic) {
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
 
 	private void send(Message message) {
 		if (message.isCancelled()) {
 			Player sender = sm.getPlayer(message.getSender());
 			sender.sendMessage(ChatColor.RED
 					+ "You don't have permission to do that.");
 		} else {
 			String recipient = message.getRecipient();
 			Mailbox box = sm.getMailbox(recipient);
 			box.addUnread(message);
 			sm.updateMailbox(box);
 			if (message.getSender().equalsIgnoreCase("Console")) {
 				log.info("Message sent to " + recipient + ".");
 			} else {
 				sm.getPlayer(message.getSender()).sendMessage(
 						ChatColor.GREEN + "Message sent to " + recipient + ".");
 			}
 			if (sm.isOnline(box.getOwner())) {
 				Player _recip = sm.getPlayer(box.getOwner());
 				int count = box.getUnread().size();
 				_recip.sendMessage(ChatColor.GREEN
 						+ String.format("You have %s new messages.", count + ""));
 			} else if (recipient.equalsIgnoreCase("console")) {
 				log.info("New mail for Console.");
 			}
 		}
 	}
 
 	static {
 		helpTopics.put("send", "/mail send <target> [priority:#] <message>");
 		helpTopics.put("get",
 				"/mail get [box] - Use \'/mail help boxes\' for more help");
 		helpTopics.put("help", "Topics: send, get");
 	}
 
 }
