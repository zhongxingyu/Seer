 package org.csdgn.fxm.cmd.impl;
 
 import org.csdgn.fxm.cmd.Command;
 import org.csdgn.fxm.cmd.Interpreter;
 import org.csdgn.fxm.net.Session;
 import org.csdgn.fxm.model.Character;
 import org.csdgn.fxm.model.Exit;
 import org.csdgn.util.StringUtils;
 
 public class Go implements Command {
 
 	@Override
 	public void execute(Session session, String input) {
 		input = StringUtils.getAfter(input, ' ');
 		if(input != null) {
 			Exit exit = Interpreter.findExit(input, session.character.room);
 			if(exit != null) {
 				doGo(session, exit);
 			} else {
 				session.write("You can't go there.");
 			}
 		} else {
 			session.writeLn("Go where?");
 		}
 	}
 	
 	
 	public void doGo(Session session, Exit exit) {
 		Character chara = session.character;
 		String name = chara.givenName;
 		
 		chara.writeLnToRoom(String.format(customMsg(exit.exitMsg,"%s moves %s."), name, exit.name));
 		
 		session.writeLn(String.format(customMsg(exit.travelMsg,"You move %s."), exit.name));
 		
 		chara.setRoom(exit.getRoom());
 		
 		Exit targetExit = exit.getRoom().getExit(exit.exitUUID);
 		
		chara.writeLnToRoom(String.format(customMsg(targetExit.enterMsg,"%s arrives from the %s."), name, targetExit.name));
 		
 		//Do look
 		session.force("look");
 	}
 	
 	private String customMsg(String custom, String standard) {
 		if(custom != null)
 			return custom;
 		return standard;
 	}
 }
