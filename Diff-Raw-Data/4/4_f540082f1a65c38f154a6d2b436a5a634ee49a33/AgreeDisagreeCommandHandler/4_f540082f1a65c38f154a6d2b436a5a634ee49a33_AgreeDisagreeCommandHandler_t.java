 package com.drtshock.willie.command.fun;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 
 public class AgreeDisagreeCommandHandler implements CommandHandler{
 
 	private boolean yes = false;
 
 	/**
 	 * Creates a new YesNoCommandHandler
 	 * 
 	 * @param isAgree if true then this handler will act as an "!agree" handler, otherwise "!disagree"
 	 */
 	public AgreeDisagreeCommandHandler(boolean isAgree){
 		this.yes = isAgree;
 	}
 
 	@Override
 	public void handle(Willie bot, Channel channel, User sender, String[] args){
 		if(args.length == 0){
 			bot.sendAction(channel, (!yes ? "dis" : "") + "agrees");
 		}else{
 			StringBuilder sb = new StringBuilder();
 			for(String arg : args){
				sb.append(arg).append(" ");
 			}
 			bot.sendAction(channel, (!yes ? "dis" : "") + "agrees to " + sb.toString());
 		}
 	}
 
 }
