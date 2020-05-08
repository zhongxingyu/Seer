 /*
  * DiceResponder.java
  * 
  *  $Id $
  *  Author: smadden
  *
  *    Copyright (C) 2010 Sean Madden
  *
  *    Please see the pertinent documents for licensing information.
  *
  */
 
 package com.seanmadden.deepthought.responders;
 
 import java.util.Random;
 
 import com.seanmadden.deepthought.IRCClient;
 import com.seanmadden.deepthought.Message;
 import com.seanmadden.deepthought.MessageHandler;
 
 public class DiceResponder implements MessageHandler {
 
 	@Override
 	public boolean handleMessage(IRCClient irc, Message m) {
 		String message = m.getMessage();
 		if(!message.contains(irc.getNick())){
 			return false;
 		}
 		if(!message.contains("roll")){
 			return false;
 		}
 		String[] args = message.split(" ", 3);
 		if(args.length != 3){
 			return false;
 		}
 		if(!args[1].equals("roll")){
 			return false;
 		}
 		
 		try{
 			String dice = args[2];
 			String[] fmt = dice.split("d", 2);
 			int die = Integer.parseInt(fmt[0]);
 			int sides = Integer.parseInt(fmt[1]);
			if(die > 30 || die < 1){
 				Message msg = new Message(m.getUsermask() + ": No.", m.getTarget());
 				irc.sendMessage(msg);
 				return true;
 			}
			if(sides > 9999 || sides < 1){
 				Message msg = new Message(m.getUsermask() + ": No.", m.getTarget());
 				irc.sendMessage(msg);
 				return true;
 			}
 			String response = "you rolled:";
 			int total = 0;
 			Random r = new Random();
 			for(int i = 0; i < die; ++i){
				int res = r.nextInt(sides)+1;
 				response += res + " ";
 				total += res;
 			}
 			if(die > 1){
 				response += "for a total of: " + total;
 			}
 			
 			Message msg = new Message(m.getUsermask() + ": " + response, m.getTarget());
 			irc.sendMessage(msg);
 		}catch(NumberFormatException e){
 			Message msg = new Message(m.getUsermask() + ": can't figure out what you meant!", m.getTarget());
 			irc.sendMessage(msg);
 		}
 		return true;
 	}
 
 }
