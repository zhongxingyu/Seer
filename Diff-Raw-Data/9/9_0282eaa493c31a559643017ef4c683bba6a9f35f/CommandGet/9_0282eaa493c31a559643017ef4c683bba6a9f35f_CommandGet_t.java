 package pl.shockah.shocky2.module.botcontrol;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import pl.shockah.shocky2.Command;
 import pl.shockah.shocky2.CommandCallback;
 import pl.shockah.shocky2.Data;
 import pl.shockah.shocky2.ETarget;
 import pl.shockah.shocky2.Module;
 import pl.shockah.shocky2.smodule.login.LoginData;
 
 public class CommandGet extends Command {
 	public CommandGet(Module module) {
 		super(module);
 	}
 	
 	public String command() {return "get";}
 	public String help() {
 		return ".get [.|channel] {key} - retrieves a value from bot's config (. = current channel)";
 	}
 	public void call(PircBotX bot, ETarget target, CommandCallback callback, Channel channel, User sender, String message) {
 		if (callback.target != ETarget.Console) callback.target = ETarget.Notice;
 		
 		String aChannel = null, aKey = null;
 		
 		String[] split = message.split("\\s");
 		if (split.length == 1 || split.length > 3) {
 			callback.append(help());
 			return;
 		}
 		
 		if (split.length == 2) {
 			if (".#".contains(""+split[1].charAt(0))) {
 				callback.append(help());
 				return;
 			} else aKey = split[1];
 		} else {
			if (channel == null && split[1].equals(".")) {
				callback.append(help());
				return;
			}
 			aChannel = split[1].equals(".") ? channel.getName() : split[1];
 			aKey = split[2];
 		}
 		
		LoginData ld = sender == null ? null : LoginData.getLoginData(sender.getNick());
		if (sender == null || (aChannel == null && ld.isController()) || (aChannel != null && ld.isOp(aChannel))) {
 			callback.append(aKey+": ");
 			
 			Object o = Data.get(aChannel,aKey);
 			if (o instanceof Boolean) callback.append("b:");
 			else if (o instanceof Integer) callback.append("i:");
 			else if (o instanceof Long) callback.append("l:");
 			else if (o instanceof Float) callback.append("f:");
 			else if (o instanceof Double) callback.append("d:");
 			else if (o instanceof String) callback.append("s:");
 			
 			callback.append(o);
 		} else callback.append("Restricted command.");
 	}
 }
